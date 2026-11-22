"""Independent assertions on actual Minecraft framebuffers (3× GUI, 1440×810).

Usage: python tests/fixtures/check_pixels.py <directory of GearUiCheck PNGs>
Only Pillow is required. This reads final pixels; it does not call the layout renderer.
"""
from pathlib import Path
from math import ceil, floor
import sys
from PIL import Image

shots=Path(sys.argv[1])
def image(stem):
    with Image.open(shots/(stem+'.png')) as im:
        assert im.size==(1440,810)
        return im.convert('RGB')

# The first power in the test loadout is Marksman's Dodge. Its opaque foreground color
# and leftmost ink pixel are distinct in each skin. BB4 additionally tints its outer rim.
styles=(
    ('Destiny',134,102,34,(247,247,239),12),
    ('Compact',128,82,32,(133,221,246),10),
    ('Minimal',112,98,25,(133,221,246),12),
    ('BL2',139,123,34,(239,255,179),20),
    ('BL4',142,119,34,(180,222,130),2),
    ('Tenno',136,103,29,(247,247,239),10))
for name,ammo,row,height,ink,glyph_offset in styles:
    delta=0 if name in ('Minimal','BL2') else floor((ammo-row)*.82/2+.5)
    top=3*(262-ceil(height*.82))
    im=image('HUD-'+name)
    # The preceding terrain/surround contains no pixels of the exact ready-state pigment.
    area=im.crop((0,top,3*(10+delta)+60,top+int(2.46*dict(Destiny=19,Compact=19,Minimal=13,BL2=20,BL4=20,Tenno=18)[name])))
    pixels=[(x,y) for y in range(area.height) for x in range(area.width) if area.getpixel((x,y))==ink]
    assert len(pixels)>120,(name,len(pixels))
    physical_left=min(x for x,_ in pixels)-glyph_offset
    assert abs(physical_left-3*(10+delta))<=1,(name,physical_left,delta)
    print(f'GEAR FRAME {name}: ability row global x={physical_left} physical px, inset={delta} GUI px inside ammo card')

def mix(accent,t): return tuple(int(a*(1-t)+b*t) for a,b in zip(accent,(246,245,241)))
def stripe(im,rgb,minimum):
    key=bytes(rgb)
    for y in range(im.height):
        row=im.crop((0,y,im.width,y+1)).tobytes()
        if row.find(key*minimum)>=0:return True
    return False

exotic=(203,164,65); legendary=(128,98,152); solar=(255,172,101); paracausal=(85,156,152)
for stem,accent in (('Notch-Hover',paracausal),('Ability-Binder',solar),('Ability-Inventory',solar),
                    ('Weapon-Card',exotic),('Armor-Card',legendary),('Exotic-Card',exotic),('Gunsmith-Card',exotic)):
    im=image(stem)
    assert stripe(im,mix(accent,.39),120),(stem,'missing card header in native render')
    print('GEAR FRAME actual rarity/element header:',stem)
vanilla=image('Vanilla-Tooltip')
assert not any(stripe(vanilla,mix(a,.39),200) for a in (exotic,legendary,solar))
print('GEAR FRAME vanilla bread retains ordinary tooltip')
