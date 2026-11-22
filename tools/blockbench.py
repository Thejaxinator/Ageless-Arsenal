"""Generate grouped Blockbench projects, or apply an edited project to the item and its animated parts."""
from pathlib import Path
from copy import deepcopy
import argparse,base64,json,uuid
L=Path(__file__).resolve().parents[1]
A=L/'src/main/resources/assets/destiny2mc'
B=L/'blockbench/weapons'
uid=lambda *args:str(uuid.uuid5(uuid.NAMESPACE_URL,'destiny2mc/armory/'+'/'.join(map(str,args))))
def write(p,o):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(o,ensure_ascii=False,separators=(',',':')))
def generate(k):
 model=json.loads((A/'models/item'/f'{k}.json').read_text());parts=A/'models/armory'/k
 textures=[];indices={}
 for slot in ('all','detail'):
  f=A/'textures'/(model['textures'][slot].split(':')[1]+'.png');indices['#'+slot]=len(textures)
  textures.append(dict(name=f.name,id=slot,uuid=uid(k,slot),source='data:image/png;base64,'+base64.b64encode(f.read_bytes()).decode(),mode='bitmap',uv_width=16,uv_height=16))
 out=[];hier=[]
 for path in sorted(parts.glob('*.json')):
  children=[]
  for i,e in enumerate(json.loads(path.read_text())['elements']):
   r=e.get('rotation',{});angle=[0,0,0]
   if r:angle['xyz'.index(r['axis'])]=r['angle']
   ident=uid(k,path.stem,i);children.append(ident)
   cube={**deepcopy(e),'uuid':ident,'type':'cube','armory_order':model['elements'].index(e),'box_uv':False,'origin':r.get('origin',[8,8,8]),'rotation':angle,'rescale':False}
   for v in cube['faces'].values():v['texture']=indices[v['texture']]
   out.append(cube)
  hier.append(dict(name=path.stem,origin=[8,8,8],uuid=uid(k,path.stem),children=children,export=True,visibility=True,isOpen=False))
 write(B/(k+'.bbmodel'),dict(meta={'format_version':'4.10','model_format':'java_block','box_uv':False},name=k,resolution={'width':16,'height':16},elements=out,outliner=hier,textures=textures,display=model['display']))
def apply(path):
 bb=json.loads(path.read_text());k=path.stem;mp=A/'models/item'/f'{k}.json';model=json.loads(mp.read_text())
 parts=A/'models/armory'/k;elements={e['uuid']:e for e in bb['elements']};allowed={p.stem for p in parts.glob('*.json')};assigned=set();layers={};ordered=[]
 if {g['name'] for g in bb['outliner']}!=allowed:raise ValueError('Keep the existing top-level mechanism groups')
 for group in bb['outliner']:
  layer=[]
  if any(group.get('rotation',[])):raise ValueError('Rotate the cubes inside each mechanism group')
  for ident in group['children']:
   if not isinstance(ident,str) or ident in assigned:raise ValueError('Place each cube directly inside one mechanism group')
   assigned.add(ident);e=elements[ident];angles=e.get('rotation',[0,0,0]);axes=[i for i,v in enumerate(angles) if v]
   out={key:deepcopy(e[key]) for key in ('name','from','to','faces')}
   if axes:
    if len(axes)>1 or abs(angles[axes[0]]) not in (22.5,45):raise ValueError('Java item cubes support one 22.5° or 45° rotation')
    a=axes[0];out['rotation']={'axis':'xyz'[a],'angle':angles[a],'origin':e['origin'],'rescale':e.get('rescale',False)}
   for face in out['faces'].values():
    tx=bb['textures'][face['texture']]['name'];slot=next((n for n in ('all','detail') if Path(model['textures'][n]).name+'.png'==tx),None)
    if not slot:raise ValueError('Keep the material and detail texture names')
    face['texture']='#'+slot
   layer.append(out);ordered.append((e.get('armory_order',len(elements)+len(ordered)),out))
  layers[group['name']]=layer
 if assigned!=set(elements):raise ValueError('Assign every cube to a mechanism group')
 paints={}
 for tx in bb['textures']:
  if tx.get('source','').startswith('data:image/png;base64,'):paints[Path(tx['name']).name]=base64.b64decode(tx['source'].split(',',1)[1],validate=True)
 model['elements']=[e for _,e in sorted(ordered,key=lambda item:item[0])];model['display']=bb.get('display',model['display']);write(mp,model)
 for group,els in layers.items():
  p=parts/(group+'.json');o=json.loads(p.read_text());o['elements']=els;write(p,o)
 for name,data in paints.items():(A/'textures/item'/name).write_bytes(data)
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--apply',type=Path);o=p.parse_args()
 if o.apply:apply(o.apply)
 else:
  for d in sorted((A/'models/armory').iterdir()):
   if d.is_dir():generate(d.name)
