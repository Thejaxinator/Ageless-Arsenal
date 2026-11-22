"""Rebuild the three Corpus rigs, selective emissive textures, and Blockbench sources.

Run from the repository root or from tools/corpus-art/ with Python 3 and Pillow.
Generates:
  - models/rigs/corpus_*.json
  - blockbench/corpus_*.bbmodel
  - src/main/resources/assets/destiny2mc/textures/entity/corpus_*[_glow].png
"""
from pathlib import Path
import json
import sys
from PIL import Image

HERE = Path(__file__).resolve().parent
REPO = HERE.parents[1]
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(REPO / 'tools' / 'dread-art' / 'tools'))
sys.path.insert(0, str(REPO / 'tools'))

from sculpt import ROSTER, make, CLOTH, PLATE, DEEP, PEARL, BRASS, GOLD, LIGHT, POWER, GLOW, STEEL, EDGE, RUBBER, TRIM
from rig_tools import editor, png, ordered, split_long_faces
from build_rigs import build_geometry


def paint(rig):
    palette = rig.d['palette']
    emit = set(rig.d['emissive_palette'])
    base = Image.new('RGBA', (512, 128))
    glow = Image.new('RGBA', base.size)
    diffuse = base.load()
    mask = glow.load()
    for m, color in enumerate(palette):
        for y in range(64):
            for x in range(64):
                pix = (x * 13 + y * 37 + x * y * 7) % 11 - 5
                if m in (CLOTH, RUBBER):
                    # Ribbed industrial jumpsuit / matte synthetic weave
                    value = (5 if (y % 4 == 0) else -1) + pix * 0.30
                elif m in (PLATE, DEEP, EDGE, TRIM):
                    # Brushed Corpus alloy panels with subtle seam lines
                    value = (5 if (x % 16 == 0 or y % 16 == 0) else -1) + pix * 0.42
                elif m in (PEARL, STEEL):
                    value = (6 if (x - 2 * y) % 29 == 0 else 1) + pix * 0.28
                elif m in (BRASS, GOLD):
                    value = (6 if (x + y // 2) % 8 < 2 else -2) + pix * 0.30
                elif m in (LIGHT, POWER, GLOW):
                    value = (8 if (x + y) % 6 < 2 else -2) + pix * 0.22
                else:
                    value = pix * 0.45
                rgb = tuple(max(0, min(255, round(c + value))) for c in color)
                X, Y = (m % 8) * 64 + x, (m // 8) * 64 + y
                diffuse[X, Y] = rgb + (255,)
                if m in emit:
                    mask[X, Y] = rgb + (255,)
    return png(base), png(glow)


def save(p, data):
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_bytes(data)


def main():
    tex_dir = REPO / 'src/main/resources/assets/destiny2mc/textures/entity'
    for name in ROSTER:
        r = make(name)
        r.parts = r.d['parts'] = ordered(r.parts)
        if len(r.parts) >= 250:
            raise ValueError(f'{name} exceeds JVM local register budget ({len(r.parts)} bones)')
        for node in r.parts:
            split_long_faces(node)
        diffuse, mask = paint(r)
        save(REPO / 'models/rigs' / f'{name}.json', json.dumps(r.d, separators=(',', ':')).encode('utf-8'))
        save(REPO / 'blockbench' / f'{name}.bbmodel', json.dumps(editor(r, diffuse), separators=(',', ':')).encode('utf-8'))
        save(tex_dir / f'{name}.png', diffuse)
        save(tex_dir / f'{name}_glow.png', mask)
        # Verify native JVM ModelPart compilation succeeds cleanly
        cls, bytecode = build_geometry(dict(r.d, kind=r.d['geometry_slot']))
        print(f'{name}: kind={r.d["kind"]}, {len(r.parts)} bones, '
              f'{sum(len(p["boxes"]) for p in r.parts)} cuboids, {cls} ({len(bytecode)} bytes)')


if __name__ == '__main__':
    main()
