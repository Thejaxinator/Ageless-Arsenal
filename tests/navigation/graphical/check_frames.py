"""Check both navigation levels against the exact colors in a Minecraft framebuffer."""
from pathlib import Path
import sys
import numpy as np
from PIL import Image

shots=Path(sys.argv[1])
pages=['01-Destiny-Crafting','02-Destiny-Perks','03-Warframe-Blueprints','04-Warframe-Mods']
def load(name):
    return np.asarray(Image.open(shots/(name+'.png')).convert('RGB'))
def matches(a,color):
    return (a==np.frombuffer(bytes.fromhex(color),dtype=np.uint8)).all(axis=-1).sum()
def box(a,x,y,w,h):
    height,width=a.shape[:2];scale=min(width/800,height/450)
    dx=(width-800*scale)/2;dy=(height-450*scale)/2
    return a[round(dy+y*scale):round(dy+(y+h)*scale),round(dx+x*scale):round(dx+(x+w)*scale)]
frames=[load(page) for page in pages]
for i,(name,f) in enumerate(zip(pages,frames)):
    game,tool=divmod(i,2)
    active='ddcfad' if game else 'e8e3d6'
    accent='a8d6c0' if game else 'dac492'
    active_x=(397,588)[game]; other_x=(588,397)[game]
    assert matches(box(f,active_x+2,8,179,24),active)>2000 and matches(box(f,other_x+2,8,179,24),active)==0,name
    active_x=(421,597)[tool]; other_x=(597,421)[tool]
    bright=matches(box(f,active_x+5,60,150,5),accent)
    dark=matches(box(f,other_x+5,60,150,5),accent)
    assert bright>100 and dark<10,(name,bright,dark)
    print(f'FRAME {name} | {f.shape[1]}×{f.shape[0]} | one selected franchise and one selected tool')
for i,j in [(0,1),(2,3)]:
    a,b=frames[i],frames[j]
    def body(frame):return box(frame,24,74,750,338).astype(np.int16)
    ratio=(np.max(np.abs(body(a)-body(b)),axis=-1)>28).mean()
    assert ratio>.07,ratio
    print(f'FRAME {pages[i]} and {pages[j]} have distinct usable bodies')
for name in ['05-Creative-Warframe','06-Creative-Combat']:
    f=load(name)
    assert matches(f,'8b8b8b')>4000 and matches(f,'c6c6c6')>4000
    print('FRAME '+name+' | native item-picker slots and vanilla tab frame')
