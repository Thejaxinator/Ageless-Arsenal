"""Verify native framebuffer positions and visibility using opaque glyph ink from the actual HUD.
Run alongside ThemedHudCheck using: python check_pixels.py .../shots
"""
from pathlib import Path
import collections
import math
import sys
from PIL import Image

shots=Path(sys.argv[1])
def pixels(name, box):
    with Image.open(shots/(name+'.png')) as image:
        assert image.size==(1440,810),image.size
        return collections.Counter(image.convert('RGB').crop(tuple(n*3 for n in box)).getdata())

# Default combat HUD runs at 82% of the logical GUI. At 480 × 270 the four
# Destiny cells begin at (10,234). The gun bottom sits 3 logical px above.
glyph=(247,247,239)
visible='HUD-Destiny'
per_slot=[]
for i in range(4):
    x=10+round(i*26*.82)
    ink=pixels(visible,(x,234,x+20,254))
    assert ink[glyph]>40,(i,ink[glyph]);per_slot.append(ink[glyph])
print('PIXEL CHECK four Destiny slot glyphs at expected left anchor:',per_slot)
for stem in ('Hidden-F1','Weapon-Only'):
    ink=pixels(stem,(10,234,94,262))
    assert ink[glyph]<35,(stem,ink[glyph])
print('PIXEL CHECK no ability icons in the lower-left strip when hidden by F1 or the Ability HUD toggle')
assert pixels('Ability-Only',(10,234,94,262))[glyph]>300
assert pixels(visible,(10,202,121,232))[(255,255,255)]>400
assert pixels('Weapon-Only',(10,235,128,262))[(255,255,255)]>400
print('PIXEL CHECK independent empty-hand ability strip and ammo-only HUD')
styles=('HUD-Destiny','HUD-Compact','HUD-Minimal','HUD-Borderlands-2','HUD-Borderlands-4','HUD-Tenno')
# Compare exact glyph ink families; each entire HUD crop is a real game render.
regions=[]
for style in styles:
    with Image.open(shots/(style+'.png')) as image:regions.append(image.crop((25,690,350,795)).tobytes())
assert len(set(regions))==6
print('PIXEL CHECK six distinct bound ability rows')
opened=Image.open(shots/'Inventory-Open.png').convert('RGB')
closed=Image.open(shots/'Inventory-Closed.png').convert('RGB')
import numpy as np
raw=np.asarray(opened).astype(int);base=np.asarray(closed).astype(int)
# Drawer is wholly left of the 176px-wide centered vanilla inventory.
drawer=raw[170:640,10:447];vanilla=base[170:640,10:447]
assert np.mean(np.max(np.abs(drawer-vanilla),axis=2)>65)>.62
slots=(slice(410,637),slice(469,713))
assert np.mean(np.max(np.abs(raw[slots]-base[slots]),axis=2)>8)<.04
print('PIXEL CHECK left drawer changes the paper area while preserving the vanilla inventory slot grid')
print('PIXEL CHECK opening animation captured',len(list((shots/'drawer-frames').glob('*.png'))),'native rendered frames')
