"""Compile a complete release with Java 21, a NeoForge development classpath and Python 3.

The Gradle wrapper can resolve the platform classpath. This entry point is also
usable directly in CI. No previous mod JAR, Minecraft installation or texture
authoring software is required to rebuild an exported source tree.
"""
import argparse
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import zipfile
PROJECT = Path(__file__).resolve().parent
sys.path.insert(0, str(PROJECT / "tools"))
from bake_rigs import bake

def build(classpath, javac, target):
    target.parent.mkdir(parents=True,exist_ok=True)
    # Stage all outputs together. A failed compile leaves the previous known-good JAR intact.
    with tempfile.TemporaryDirectory(dir=target.parent) as temporary:
        scratch = Path(temporary)
        classes = scratch / "classes"
        bake(PROJECT, classes)
        files = sorted((PROJECT / "src/main/java").rglob("*.java"))
        cp = str(classes) + os.pathsep + classpath.read_text(encoding="utf-8-sig").strip()
        args = ["--release", "21", "-proc:none", "-encoding", "UTF-8", "-classpath", cp, "-d", str(classes), *map(str, files)]
        # A javac argument file also works on Windows when the platform classpath is long.
        response = scratch / "javac.args"
        response.write_text("\n".join('"' + a.replace("\\", "\\\\").replace('"', '\\"') + '"' for a in args), encoding="utf-8")
        subprocess.run([javac, "@" + str(response)], check=True)
        entries = {}
        for root in [PROJECT / "src/main/resources", classes]:
            for path in root.rglob("*"):
                if path.is_file():
                    key = path.relative_to(root).as_posix()
                    if key in entries:
                        raise ValueError("Duplicate JAR member " + key)
                    entries[key] = path
        jar = scratch / "release.jar"
        with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as out:
            # Fixed ZIP attributes make identical sources reproducible on the same JDK.
            for name,path in sorted(entries.items()):
                info = zipfile.ZipInfo(name,(2026,1,1,0,0,0))
                info.compress_type=zipfile.ZIP_DEFLATED
                info.create_system=3
                info.external_attr=0o100644 << 16
                out.writestr(info,path.read_bytes())
        jar.replace(target)
    print(target)

if __name__ == "__main__":
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--classpath-file", type=Path, required=True)
    parser.add_argument("--javac", default="javac")
    parser.add_argument("--output", type=Path, default=PROJECT/"build/libs"/(PROJECT.joinpath("release-name.txt").read_text().strip()+".jar"))
    args=parser.parse_args()
    build(args.classpath_file,args.javac,args.output)
