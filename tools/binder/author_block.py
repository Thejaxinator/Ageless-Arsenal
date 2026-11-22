"""Author the inset four-socket table, suspended crystal and mote icon at native pixel scale."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random
r=Path(__file__).resolve().parents[2]/'src/main/resources';a=r/'assets/destiny2mc';d=r/'data/destiny2mc'
def write(p,o):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(o,indent=2)+'\n')
def save(im,p):p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
colors={'hull':(41,54,70),'edge':(90,112,123),'recess':(17,28,38),'signal':(99,226,216),'copper':(198,140,86),'glass':(178,251,239)}
for i,(k,c) in enumerate(colors.items()):
 im=Image.new('RGB',(16,16));rng=random.Random(43+i)
 for y in range(16):
  for x in range(16):
   n=rng.choice([-3,-2,0,0,2,4])+(3 if y in (1,13) else 0);im.putpixel((x,y),tuple(max(0,min(255,v+n)) for v in c))
 save(im,a/f'textures/block/binder_{k}.png')
els=[]
def box(n,p,q,k):
 e={'name':n,'from':p,'to':q,'faces':{s:{'texture':'#'+k} for s in ['up','down','east','west','north','south']}};els.append(e);return e
box('Foot',[1,0,1],[15,1.5,15],'edge')
box('Pedestal',[3,1.5,3],[13,10.5,13],'hull')
box('Recessed waist',[2.8,4,2.8],[13.2,5,13.2],'recess')
box('Circuit band',[2.7,4.4,2.7],[13.3,4.65,13.3],'signal')
box('Deck rim',[0,10,0],[16,11.5,16],'copper');box('Socket deck',[0,11.5,0],[16,12.5,16],'hull')
for x,z in [(3,3),(11,3),(3,11),(11,11)]:
 box('Channel socket',[x-1,12.5,z-1],[x+3,13,z+3],'edge');box('Contact',[x,13,z],[x+2,13.3,z+2],'signal')
box('Emitter well',[6,12.5,6],[10,13,10],'recess')
box('Emitter',[7,13,7],[9,13.4,9],'copper');e=box('Suspended prism',[6.7,14.2,6.7],[9.3,16.8,9.3],'glass');e['rotation']={'origin':[8,15.5,8],'axis':'z','angle':45}
write(a/'models/block/energy_binder.json',{'parent':'minecraft:block/block','textures':{'particle':'destiny2mc:block/binder_hull',**{k:'destiny2mc:block/binder_'+k for k in colors}},'elements':els})
write(a/'blockstates/energy_binder.json',{'variants':{'':{'model':'destiny2mc:block/energy_binder'}}})
write(a/'models/item/energy_binder.json',{'parent':'destiny2mc:block/energy_binder'})
im=Image.new('RGBA',(32,32));g=ImageDraw.Draw(im)
for rad,col in [(9,'#916648'),(7,'#e1aa60'),(5,'#ffdb8b'),(3,'#fffadd')]:
 g.polygon([(16,15-rad),(16+rad,15),(16,15+rad),(16-rad,15)],fill=col)
for x,y,l in [(13,5,4),(22,6,3),(5,16,4),(20,24,5),(25,18,3),(9,23,2)]:
 g.line((x,y,x+1,y-l),fill='#e4bf87',width=1)
g.rectangle((14,12,17,15),fill='#ffffee');save(im,a/'textures/item/mote_of_light.png')
write(a/'models/item/mote_of_light.json',{'parent':'minecraft:item/generated','textures':{'layer0':'destiny2mc:item/mote_of_light'}})
write(d/'recipe/energy_binder.json',{'type':'minecraft:crafting_shaped','category':'redstone','pattern':[' A ','CMC','SIS'],'key':{c:{'item':i} for c,i in {'A':'minecraft:amethyst_shard','C':'minecraft:copper_ingot','M':'destiny2mc:mote_of_light','S':'minecraft:smooth_stone','I':'minecraft:iron_ingot'}.items()},'result':{'id':'destiny2mc:energy_binder'}})
write(d/'loot_table/blocks/energy_binder.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'destiny2mc:energy_binder'}]}]})
