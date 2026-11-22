"""Rebuild the ten Dread rigs, native geometry classes, selective emissive masks, and Blockbench sources.

Run from any directory with Python 3 and Pillow. All authoring tools are bundled beside this script.
"""
from pathlib import Path
import json, math, sys
from PIL import Image
from sculpt import ROOT, ROSTER, make, CLOTH, PLATE, DEEP, PEARL, BRASS, GOLD, LIGHT, GLOW, STEEL, EDGE, RUBBER
from rig_tools import editor, png, ordered, split_long_faces
from build_rigs import build_geometry

def paint(rig):
    palette=rig.d['palette'];emit=rig.d['emissive_palette']
    base=Image.new('RGBA',(512,128));glow=Image.new('RGBA',base.size)
    diffuse=base.load();mask=glow.load()
    for m,color in enumerate(palette):
        for y in range(64):
            for x in range(64):
                # Ridges use continuous pixels across a 64px material tile. Reserved emissive slots keep the articulated
                # silhouette dark at night except the eye, the thoracic slit, the glacial blade or the wing filaments.
                pix=(x*13+y*37+x*y*7)%11-5
                if m in (CLOTH,RUBBER): value=(4 if (x+2*y)%7==0 else -1)+pix*.32
                elif m in (PLATE,DEEP,EDGE):value=(5 if (x//3+y)%9==0 else -2)+pix*.54
                elif m in (PEARL,STEEL):value=(7 if (x-2*y)%31==0 else 1)+pix*.28
                elif m in (BRASS,GOLD,LIGHT,GLOW):value=(7 if (x+y//2)%8<2 else -2)+pix*.27
                else:value=pix*.49
                rgb=tuple(max(0,min(255,round(c+value))) for c in color)
                X,Y=(m%8)*64+x,(m//8)*64+y
                diffuse[X,Y]=rgb+(255,)
                if m in emit:mask[X,Y]=rgb+(255,)
    return png(base),png(glow)

def save(p,data):
    p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)

def main():
    for name in ROSTER:
        r=make(name)
        r.parts=r.d['parts']=ordered(r.parts)
        if len(r.parts)>=250:raise ValueError(f'{name} exceeds JVM local register budget')
        for node in r.parts:split_long_faces(node)
        diffuse,mask=paint(r)
        save(ROOT/'models'/(name+'.json'),json.dumps(r.d,separators=(',',':')).encode())
        save(ROOT/'blockbench'/(name+'.bbmodel'),json.dumps(editor(r,diffuse),separators=(',',':')).encode())
        cls,code=build_geometry(r.d);save(ROOT/'overlay'/(cls+'.class'),code)
        dest=ROOT/'overlay/assets/destiny2mc/textures/entity'
        save(dest/(name+'.png'),diffuse);save(dest/(name+'_glow.png'),mask)
        print(name,len(r.parts),'bones',sum(len(p['boxes']) for p in r.parts),'cuboids')
if __name__=='__main__':main()
