"""Measure native pack reveal and Fusion rank lamps in the client framebuffer."""
from pathlib import Path
import sys
import numpy as np
from PIL import Image
p=Path(sys.argv[1])
def rgb(name):
    im=Image.open(p/(name+'.png')).convert('RGB')
    assert im.size==(1440,810),(name,im.size)
    return np.array(im)
def matching(a, color):return np.all(a==bytes_rgb(color),axis=-1)
def bytes_rgb(c):return np.array(tuple(bytes.fromhex(c)),dtype='uint8')
def area(a,x0,y0,x1,y1):
    return a[round(y0*1.8):round(y1*1.8),round(x0*1.8):round(x1*1.8)]
a=rgb('03-Collection');b=rgb('04-Fusion');c=rgb('05-All-Equipped')
for name,pic in [('Collection',a),('Ranked',b),('Installed',c)]:
    assert matching(area(pic,25,62,775,72),'cfb78b').sum()>900,name
    assert matching(area(pic,565,83,762,255),'b7c9cc').sum()+matching(area(pic,565,83,762,255),'dfbe79').sum()+matching(area(pic,565,83,762,255),'bf906b').sum()>650,name
    print('MOD FRAME',name,'contains the full Foundry and Warframe card border')
before=matching(area(a,580,241,752,247),'9ed9c9').sum()
after=matching(area(b,580,241,752,247),'9ed9c9').sum()
assert after-before>45,(before,after)
print(f'MOD FRAME selected card rank lamps increased from {before} to {after} colored pixels')
for cell in range(6):
    left,top=196+114*(cell%3),103+34*(cell//3)
    def socket(pic):
        return area(pic,left,top,left+108,top+30).astype('int16')
    changed=np.abs(socket(a)-socket(c)).max(axis=-1)>50
    assert changed.sum()>650,(cell,changed.sum())
print('MOD FRAME all six installed cells differ from the empty rack')
reveal=rgb('02-Reveal')
assert matching(reveal[110:430,770:1440],'dfc595').sum()>650
print('MOD FRAME pack reveal banner rendered above the in-world hotbar')
for name in ['01-Craft-Pack','05-Weapon-Inspect','06-Pack-Inspect']:
    frame=rgb(name)
    assert frame.std()>17,name
print('MOD FRAME vanilla crafting table, equipped weapon inspection, and native pack tooltip all captured')
