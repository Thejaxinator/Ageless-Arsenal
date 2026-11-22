"""Bake all eight Scorn designs. Paths are relative to this editable folder."""
import sys, json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT/'tools'))
from rig_tools import Rig,ordered,png,editor,split_long_faces
from build_rigs import build_geometry
import art
def write(p,body):
    p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(body)
for kind,name in art.UNITS.items():
    r=Rig(kind,name);art.build(r)
    r.parts=r.d['parts']=ordered(r.parts)
    assert len(r.parts)<250 and [p['name'] for p in r.parts if p['parent'] is None]==['body']
    for p in r.parts:
        for pos,size,material in p['boxes']:assert len(pos)==3 and min(size)>0 and 0<=material<len(art.PALETTE)
        split_long_faces(p)
    base,glow=art.textures(r);color=png(base)
    model,geometry=build_geometry(r.d)
    write(ROOT/'stage'/(model+'.class'),geometry)
    for suffix,data in [('.png',color),('_glow.png',png(glow))]:
        write(ROOT/'stage/assets/destiny2mc/textures/entity'/(name+suffix),data)
    editor_model=editor(r,color);editor_model['resolution']={'width':512,'height':192}
    write(ROOT/'blockbench'/(name+'.bbmodel'),json.dumps(editor_model,separators=(',',':')).encode())
    write(ROOT/'models'/(name+'.json'),json.dumps(r.d,separators=(',',':')).encode())
    print(name,len(r.parts),'joints',sum(len(p['boxes']) for p in r.parts),'cubes')
