"""Translate the editable joint hierarchies to the ModelPart layers Minecraft loads.

Geometry slots are stable renderer binding IDs. Some Eliksni use a logical unit ID
different from their geometry slot. Never derive the slot from a display name.
"""
from pathlib import Path
import argparse,json
from build_rigs import build_geometry

def bake(project, output):
    seen = set()
    for path in sorted((project / "models/rigs").glob("*.json")):
        rig = json.loads(path.read_text(encoding="utf-8"))
        slot = rig.get("geometry_slot", rig["kind"])
        if slot in seen:
            raise ValueError(f"Two rigs claim slot {slot}")
        seen.add(slot)
        path_in_jar, bytecode = build_geometry(dict(rig, kind=slot))
        dest = output / (path_in_jar + ".class")
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(bytecode)
    if not seen:
        raise ValueError("No editable rigs found.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    bake(Path(__file__).resolve().parents[1], parser.parse_args().output)
