"""Launch the Warframe foundry and arsenal acceptance fixture on a dedicated server and real graphical client.
Usage: python run.py RELEASE.jar RUN_DIRECTORY with MOD_TEST_SDK pointing to the prepared ModDev SDK."""
import hashlib, json, os, shutil, subprocess, sys, time, uuid, zipfile
from pathlib import Path
Q=Path(__file__).resolve().parent
S=Path(os.environ['MOD_TEST_SDK']).resolve()
jar=Path(sys.argv[1]).resolve()
out=Path(sys.argv[2]).resolve() if len(sys.argv)>2 else Q/'run'
out.mkdir(parents=True,exist_ok=True)
shots=out/'shots';shots.mkdir(exist_ok=True)
# Use a fresh character per run to keep each native run's collection and equipment independent.
name='UpgradeQA'+os.urandom(3).hex()
raw=bytearray(hashlib.md5(('OfflinePlayer:'+name).encode()).digest());raw[6]=(raw[6]&15)|48;raw[8]=(raw[8]&63)|128
uid=uuid.UUID(bytes=bytes(raw))
J=S/'jdk/bin';classes=out/'fixture-classes';classes.mkdir(exist_ok=True)
cp=(S/'classpath.txt').read_text().strip()
subprocess.run([J/'javac','--release','21','-proc:none','-classpath',str(jar)+os.pathsep+cp,'-d',classes,Q/'UpgradeClientCheck.java'],check=True)
with zipfile.ZipFile(out/'screens.jar','w') as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes))
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="Private"\n[[mods]]\nmodId="ageless_mod_qa"\nversion="1"\n')
program=S/'compile/build/moddev'
for side in ['server','client']:
 folder=out/side;mods=folder/'mods';mods.mkdir(parents=True,exist_ok=True)
 for n,f in [('ageless.jar',jar),('screens.jar',out/'screens.jar')]:shutil.copyfile(f,mods/n)
 args=(program/(side+'RunProgramArgs.txt')).read_text()
 if side=='client':args+='\n--username\n'+name+'\n--width\n1440\n--height\n810\n'
 (out/(side+'-args.txt')).write_text(args)
server=out/'server';client=out/'client'
# Avoid an early-display file-watcher race during simultaneous headless runs.
fml=client/'config/fml.toml'
fml.parent.mkdir(exist_ok=True)
fml.write_text('disableConfigWatcher = true\nearlyWindowControl = false\nmaxThreads = -1\nversionCheck = true\ndefaultConfigPath = "defaultconfigs"\ndisableOptimizedDFU = true\nearlyWindowProvider = "fmlearlywindow"\nearlyWindowWidth = 1440\nearlyWindowHeight = 810\nearlyWindowFBScale = 1\nearlyWindowMaximized = false\nearlyWindowSkipGLVersions = []\nearlyWindowSquir = false\ndependencyOverrides = {}\n')
(server/'eula.txt').write_text('eula=true\n')
(server/'ops.json').write_text(json.dumps([{'uuid':str(uid),'name':name,'level':4,'bypassesPlayerLimit':True}]))
port=int(os.getenv('BINDER_UI_PORT','26591'));display=os.getenv('BINDER_UI_DISPLAY','209')
(server/'server.properties').write_text(f"""server-ip=127.0.0.1
server-port={port}
online-mode=false
enforce-secure-profile=false
spawn-protection=0
spawn-monsters=true
spawn-animals=false
max-tick-time=-1
view-distance=4
simulation-distance=3
level-type=minecraft:normal
level-seed=973314
generate-structures=false
""")
(client/'options.txt').write_text('version:3955\nautoJump:false\nonboardAccessibility:false\nguiScale:3\nrenderDistance:4\nsimulationDistance:5\nenableVsync:false\nmaxFps:120\n')
procs=[]
env=dict(os.environ,DISPLAY='127.0.0.1:'+display,LIBGL_ALWAYS_SOFTWARE='1',LIBGL_DRI3_DISABLE='1',LP_NUM_THREADS='2',ALSOFT_DRIVERS='null')
def launch(cmd,cwd,name):
 with (out/(name+'.log')).open('w') as log:
  p=subprocess.Popen(cmd,cwd=cwd,env=env,stdin=subprocess.PIPE,stdout=log,stderr=subprocess.STDOUT)
 procs.append(p);return p
runtime=Path(os.getenv('BINDER_RUNTIME',str(S/'runtime.txt'))).read_text().strip()
def cmd(side):
 return [J/'java','@'+str(program/(side+'RunVmArgs.txt')),'-XX:ActiveProcessorCount=4','-Xmx'+('1300m' if side=='client' else '800m'),'-Dageless.packonly='+os.getenv('MOD_PACKONLY','false'),'-Dageless.preflight='+os.getenv('MOD_PREFLIGHT','false'),'-Dageless.mcbench='+os.getenv('MOD_MC_BENCH','false'),'-Dbinder.port='+str(port),'-Dbinder.shots='+str(shots),'-cp',runtime,'@'+str(out/(side+'-args.txt'))]+(['--nogui'] if side=='server' else [])
try:
 launch(['/usr/lib/xorg/Xorg',':'+display,'-config',str(S/'xorg.conf'),'-logfile',str(out/'xorg.log'),'-noreset','-nolock','-nolisten','local','-nolisten','unix','-listen','tcp','-ac'],out,'display')
 svc=launch(cmd('server'),server,'server')
 for i in range(150):
  if svc.poll() is not None:raise RuntimeError('server failed, see server.log')
  if 'Done (' in (out/'server.log').read_text():break
  time.sleep(1)
 else:raise RuntimeError('server readiness timeout')
 cli=launch(cmd('client'),client,'client');status=cli.wait(420)
 log=(out/'client.log').read_text()
 if status or 'UPGRADE QA COMPLETE' not in log:raise RuntimeError('client failed, see client.log')
 svc.stdin.write(b'stop\n');svc.stdin.flush();svc.wait(30)
 (out/'results.txt').write_text('SHA256 '+hashlib.sha256((client/'mods/ageless.jar').read_bytes()).hexdigest()+'\n'+'\n'.join(s for s in log.splitlines() if s.startswith('UPGRADE QA'))+'\n')
 print((out/'results.txt').read_text())
finally:
 for p in reversed(procs):
  if p.poll() is None:p.terminate()
