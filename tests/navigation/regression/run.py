"""Run two existing gameplay fixtures and the physical-station regression on a Java 21 NeoForge 21.1.266 server."""
import argparse
from hashlib import sha256
from pathlib import Path
import os
import shutil
import subprocess
import time
from zipfile import ZipFile

parser = argparse.ArgumentParser()
parser.add_argument("--sdk", required=True, type=Path, help="Development server SDK with jdk, classpath.txt, runtime.txt and compile/build/moddev launch arguments")
parser.add_argument("--jar", required=True, type=Path)
parser.add_argument("--source", required=True, type=Path, help="Mod source root with tests/tenno and tests/mod-fusion")
parser.add_argument("--work", required=True, type=Path, help="Disposable server directory")
parser.add_argument("--results", required=True, type=Path)
parser.add_argument("--port", default=27973, type=int)
a = parser.parse_args()
sdk, jar, src, work = (p.resolve() for p in (a.sdk, a.jar, a.source, a.work))
digest = sha256(jar.read_bytes()).hexdigest()
test_id = "arsenal_station_regression"
classes = work / "classes"
mods = work / "mods"
classes.mkdir(parents=True, exist_ok=True)
mods.mkdir(exist_ok=True)
subprocess.run([sdk/"jdk/bin/javac", "--release", "21", "-proc:none", "-cp", str(jar) + os.pathsep + (sdk/"classpath.txt").read_text().strip(), "-d", classes, Path(__file__).with_name("StationRegression.java").resolve(), src/"tests/tenno/foundry/WarframeFoundryCheck.java", src/"tests/mod-fusion/native/ModSocketCheck.java"], check=True)
shutil.copyfile(jar, mods/"arsenals.jar")
with ZipFile(mods/"fixture.jar", "w") as archive:
    for f in classes.rglob("*.class"):
        archive.write(f, f.relative_to(classes))
    archive.writestr("META-INF/neoforge.mods.toml", f'modLoader="javafml"\nloaderVersion="[4,)"\nlicense="Tests"\n[[mods]]\nmodId="{test_id}"\nversion="1"\n')
(work/"eula.txt").write_text("eula=true\n")
(work/"server.properties").write_text(f"online-mode=false\nserver-ip=127.0.0.1\nserver-port={a.port}\nlevel-type=minecraft:flat\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n")
launch = sdk/"compile/build/moddev"
cmd = [sdk/"jdk/bin/java", "@"+str(launch/"serverRunVmArgs.txt"), "-XX:ActiveProcessorCount=2", "-Xmx680m", "-cp", (sdk/"runtime.txt").read_text().strip(), "@"+str(launch/"serverRunProgramArgs.txt"), "--nogui"]
log = work/"server.log"
with log.open("w") as output:
    proc = subprocess.Popen(cmd, cwd=work, stdout=output, stderr=output, stdin=subprocess.PIPE)
    try:
        for _ in range(150):
            time.sleep(1)
            raw = log.read_text(errors="replace")
            if "STATION REGRESSION PASS" in raw:
                break
            if proc.poll() is not None or "Encountered an unexpected exception" in raw:
                raise RuntimeError(raw[-5200:])
        else:
            raise TimeoutError(log)
    finally:
        if proc.poll() is None:
            proc.stdin.write(b"stop\n")
            proc.stdin.flush()
            try:
                proc.wait(22)
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.wait()
markers = ("TENNO FOUNDRY PASS", "SOCKET CHECK", "STATION CHECK", "STATION REGRESSION PASS")
proof = [f"Release: {jar.name}", f"SHA-256: {digest}"]
for line in raw.splitlines():
    for marker in markers:
        if marker in line:
            proof.append(line[line.index(marker):])
            break
a.results.mkdir(parents=True, exist_ok=True)
target = a.results/(digest[:10]+".txt")
target.write_text("\n".join(proof)+"\n")
print(target)
