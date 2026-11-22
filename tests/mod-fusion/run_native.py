"""Run Ageless Arsenals weapon-mod fixtures on an isolated NeoForge dedicated server."""
import argparse
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time
from zipfile import ZipFile

HERE = Path(__file__).resolve().parent
# Short name, Java source, test mod registry ID, first words of the server success line.
SUITES = {
    'collection': ('ModCollectionCheck.java', 'mod_collection_check', 'MOD COLLECTION COMPLETE'),
    'sockets': ('ModSocketCheck.java', 'tenno_socket_check', 'SOCKET CHECK PASS'),
    'ranged': ('ModdedRangedCheck.java', 'modded_ranged_check', 'WF RANGED MODS PASS'),
    'melee': ('MeleeWeaponModsCheck.java', 'tenno_melee_mods', 'MELEE MODS PASS'),
    'station': ('ModStationCheck.java', 'mod_station_check', 'MOD STATION COMPLETE'),
    'transfer': ('ModTransferCheck.java', 'arsenal_transfer_check', 'TRANSFER PASS'),
}

# Resolve the same mapped dependencies used by the release. This init script applies only
# to this Gradle invocation and leaves the source project untouched.
GRADLE_INIT = '''
rootProject { p ->
    p.plugins.withId('net.neoforged.moddev') {
        p.neoForge.runs.maybeCreate('server').server()
        p.tasks.register('exportModTestSdk') {
            dependsOn 'prepareServerRun', 'writeServerLegacyClasspath'
            dependsOn p.configurations.compileClasspath.buildDependencies
            dependsOn p.configurations.runtimeClasspath.buildDependencies
            doLast {
                def out = new File(System.getenv('MOD_TEST_SDK'))
                out.mkdirs()
                new File(out, 'classpath.txt').text = p.configurations.compileClasspath.asPath
                new File(out, 'runtime.txt').text = p.configurations.runtimeClasspath.asPath
                new File(out, 'moddev.txt').text = p.layout.buildDirectory.dir('moddev').get().asFile.absolutePath
                new File(out, 'java-home.txt').text = p.javaToolchains.launcherFor(p.java.toolchain).get().metadata.installationPath.asFile.absolutePath
            }
        }
    }
}
'''


def prepare(project, sdk):
    wrapper = project / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
    if not wrapper.is_file():
        raise ValueError(f'Gradle wrapper missing in {project}. Pass --project with the source root.')
    with tempfile.TemporaryDirectory(prefix='ageless-sdk-init-') as temporary:
        init = Path(temporary) / 'mod-tests.init.gradle'
        init.write_text(GRADLE_INIT, encoding='utf-8')
        command = [str(wrapper)] if os.name == 'nt' else ['sh', str(wrapper)]
        subprocess.run([*command, '-I', str(init), 'exportModTestSdk'], cwd=project,
                       env={**os.environ, 'MOD_TEST_SDK': str(sdk)}, check=True)


def sdk_settings(sdk):
    def path_record(name, fallback):
        record = sdk / name
        return Path(record.read_text(encoding='utf-8').strip()) if record.is_file() else fallback
    binaries = path_record('java-home.txt', sdk / 'jdk') / 'bin'
    moddev = path_record('moddev.txt', sdk / 'compile' / 'build' / 'moddev')
    executable = '.exe' if os.name == 'nt' else ''
    java, javac = (binaries / (name + executable) for name in ('java', 'javac'))
    vm, program = (moddev / f'serverRun{kind}Args.txt' for kind in ('Vm', 'Program'))
    compile_file, runtime_file = sdk / 'classpath.txt', sdk / 'runtime.txt'
    for required in (java, javac, vm, program, compile_file, runtime_file):
        if not required.is_file():
            raise ValueError(f'SDK file missing: {required}. Run with --prepare to create a local SDK.')
    return java, javac, vm, program, compile_file.read_text().strip(), runtime_file.read_text().strip()


def argument_file(path, arguments):
    # Java and javac both support a UTF-8 @file. This keeps large classpaths out of the
    # operating system's command-line length limit and handles spaces in project paths.
    def quoted(value):
        return '"' + str(value).replace('\\', '\\\\').replace('"', '\\"') + '"'
    path.write_text('\n'.join(quoted(v) for v in arguments) + '\n', encoding='utf-8')
    return '@' + str(path)


def run_fixture(name, source, jar, directory, settings, timeout):
    _, mod_id, marker = SUITES[name]
    java, javac, vm, program, compile_cp, runtime_cp = settings
    directory.mkdir()
    classes = directory / 'test-classes'
    mods = directory / 'mods'
    classes.mkdir()
    mods.mkdir()
    compile_args = ['--release', '21', '-proc:none', '-cp', str(jar) + os.pathsep + compile_cp,
                    '-d', classes, source]
    subprocess.run([javac, argument_file(directory / 'javac.args', compile_args)], check=True)
    shutil.copyfile(jar, mods / 'arsenals.jar')
    with ZipFile(mods / 'fixture.jar', 'w') as bundle:
        for compiled in classes.rglob('*.class'):
            bundle.write(compiled, compiled.relative_to(classes))
        bundle.writestr('META-INF/neoforge.mods.toml', 'modLoader="javafml"\nloaderVersion="[4,)"\n'
                        f'license="Tests"\n[[mods]]\nmodId="{mod_id}"\nversion="1"\n')
    (directory / 'eula.txt').write_text('eula=true\n')
    (directory / 'server.properties').write_text(
        'server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-type=minecraft:flat\n'
        'generate-structures=false\nview-distance=3\nsimulation-distance=3\nspawn-protection=0\n')
    switches = ['-XX:ActiveProcessorCount=3', '-Xmx850m', '-cp', runtime_cp]
    command = [java, '@' + str(vm), argument_file(directory / 'server.args', switches),
               '@' + str(program), '--nogui']
    log = directory / 'server.log'
    with log.open('w') as output:
        process = subprocess.Popen(command, cwd=directory, stdin=subprocess.PIPE,
                                   stdout=output, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + timeout
            with log.open(encoding='utf-8', errors='replace') as reader:
                # A write may have flushed only a prefix of the expected line. Keep it until
                # its newline arrives instead of splitting the marker across two reads.
                pending = ''
                while time.monotonic() < deadline:
                    content = pending + reader.read()
                    lines = content.split('\n')
                    pending = lines.pop()
                    for line in lines:
                        if line.startswith(marker):
                            return line.rstrip()
                    if process.poll() is not None:
                        raise RuntimeError(f'Server exited before {marker}. See {log}')
                    time.sleep(.3)
            raise RuntimeError(f'Timed out waiting for {marker}. See {log}')
        finally:
            if process.poll() is None:
                try:
                    process.stdin.write(b'stop\n')
                    process.stdin.flush()
                except (BrokenPipeError, OSError):
                    pass
                try:
                    process.wait(timeout=30)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
            process.stdin.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('jar', nargs='?', type=Path, help='release JAR to test')
    parser.add_argument('--sdk', type=Path, help='prepared launcher SDK (default: MOD_TEST_SDK)')
    parser.add_argument('--prepare', action='store_true', help='generate the SDK using the source Gradle wrapper')
    parser.add_argument('--project', type=Path, default=HERE.parents[1], help='exported source root for --prepare')
    parser.add_argument('--only', nargs='+', choices=SUITES, metavar='SUITE', help='choose suite(s) from --list')
    parser.add_argument('--list', action='store_true', help='list fixture names and exit')
    parser.add_argument('--fixtures', type=Path, default=HERE / 'native', help='directory of fixture Java sources')
    parser.add_argument('--runs-dir', type=Path, help='parent for fresh, isolated instances (default: system temp)')
    parser.add_argument('--timeout', type=int, default=180, help='seconds per server startup and fixture')
    args = parser.parse_args()
    if args.list:
        for name, (source, mod_id, _) in SUITES.items():
            print(f'{name:12} {mod_id:24} {source}')
        return
    sdk = args.sdk or os.environ.get('MOD_TEST_SDK')
    if not sdk:
        parser.error('set MOD_TEST_SDK or pass --sdk')
    sdk = Path(sdk).expanduser().resolve()
    if args.prepare:
        prepare(args.project.resolve(), sdk)
    settings = sdk_settings(sdk)
    if args.prepare and not args.jar:
        print(f'SDK ready: {sdk}')
        return
    if not args.jar or not args.jar.is_file() or args.timeout < 1:
        parser.error('provide a release JAR and a positive timeout')
    jar = args.jar.resolve()
    selected = list(dict.fromkeys(args.only or SUITES))
    sources = {name: args.fixtures.resolve() / SUITES[name][0] for name in selected}
    for source in sources.values():
        if not source.is_file():
            parser.error(f'fixture source missing: {source}')
    if args.runs_dir:
        args.runs_dir.mkdir(parents=True, exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix='ageless-mod-tests-', dir=args.runs_dir)).resolve()
    print(f'Test instances: {run}', flush=True)
    with (run / 'results.txt').open('w', encoding='utf-8') as results:
        results.write(f'{jar.name}\nSHA256 {hashlib.sha256(jar.read_bytes()).hexdigest()}\n')
        for name, source in sources.items():
            print(f'Running {name}', flush=True)
            try:
                result = run_fixture(name, source, jar, run / name, settings, args.timeout)
            except (RuntimeError, OSError, subprocess.CalledProcessError) as error:
                results.write(f'FAIL {name}: {error}\n')
                raise
            print(result, flush=True)
            results.write(f'PASS {name}: {result}\n')
            results.flush()
    print(run / 'results.txt')


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, ValueError, OSError, subprocess.CalledProcessError) as error:
        sys.exit(str(error))
