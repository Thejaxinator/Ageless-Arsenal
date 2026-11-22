"""Sculpted item models. Local Z runs from the muzzle (0) to the butt (24)."""
import json, math, random
from pathlib import Path
from PIL import Image,ImageDraw
L=Path(__file__).resolve().parent
A=L/'stage/assets/destiny2mc'
for d in ['models/item','textures/item']:(A/d).mkdir(parents=True,exist_ok=True)
ROSTER=[a.split() for a in (L/'catalog.txt').read_text().splitlines() if a and not a.startswith('#')]
DARK,PLATE,TRIM,METAL,LIGHT,RUBBER,EDGE,MARK=range(8)
class Weapon:
 def __init__(self,row):
  self.id,self.kind,ln,c1,c2,c3,c4,self.optic,self.feature=row
  self.length=int(ln);self.parts=[]
  rgb=lambda c:tuple(bytes.fromhex(c))
  brighten=lambda c,v:tuple(min(255,i+v) for i in c)
  self.colors=[(38,41,46),rgb(c1),rgb(c2),rgb(c3),rgb(c4),(46,41,41),brighten(rgb(c1),30),brighten(rgb(c2),24)]
 def box(self,name,x,y,t,w,h,d,mat,angle=0,axis='x',sz=True):
  # t,d are normalized longitudinal dimensions. Weapon pivot and sight axis are shared in native model space.
  l=self.length/24 if sz else 1
  z=8+(t-12)*self.length/24
  rnd=lambda v:round(v,4)
  a=[rnd(8+x-w/2),rnd(y-h/2),rnd(z-d*l/2)];b=[rnd(8+x+w/2),rnd(y+h/2),rnd(z+d*l/2)]
  uvcol=mat%4*4;uvrow=mat//4*8;faces={}
  for face in ('north','south','up','down','east','west'):
   # Broad sides carry painted panel seams. The end face uses a quieter patch.
   wide=face in ('east','west');du=min(3.72,max(.35,(d*l if wide else w)*.28));dv=min(7.6,max(.32,h*.64))
   u=uvcol+.13;v=uvrow+.2
   faces[face]={'texture':'#all','uv':[rnd(u+(du if face=='east' else 0)),rnd(v),rnd(u+(0 if face=='east' else du)),rnd(v+dv)]}
  e={'name':name,'from':a,'to':b,'faces':faces}
  if angle:e['rotation']={'origin':[rnd(8+x),rnd(y),rnd(z)],'axis':axis,'angle':angle,'rescale':False}
  self.parts.append(e)
 def paint(self):
  rng=random.Random(self.id);im=Image.new('RGB',(64,64))
  for i,c in enumerate(self.colors):
   ox=i%4*16;oy=i//4*32
   for v in range(32):
    for u in range(16):
     n=rng.choice((-3,-1,0,0,0,1,2))+ (5 if u==0 or v==0 else 0)
     if i==RUBBER and v%5==3:n-=10
     if i in (PLATE,TRIM,EDGE) and v==14 and 6<=u<=12:n-=12
     im.putpixel((ox+u,oy+v),tuple(max(0,min(255,k+n)) for k in c))
  return im
 def model(self):
  k='destiny2mc:item/'+self.id
  def xf(rot,trans,scale):return dict(rotation=rot,translation=trans,scale=[scale]*3)
  g= min(.86,16.8/self.length)
  return {'credit':'Ageless Arsenals / Armory sculpt','ambientocclusion':False,'textures':{'all':k,'particle':k},'display':{
   'gui':xf([11,68,-13],[0,-.2,0],g if self.kind!='bow' else .56),'ground':xf([0,0,0],[0,3,0],.48),
   'fixed':xf([0,90,0],[0,0,0],g),'thirdperson_righthand':xf([74,0,0],[0,1,1.8],.65),'thirdperson_lefthand':xf([74,0,0],[0,1,1.8],.65),'firstperson_righthand':xf([0,0,0],[0,-1,0],.7),'firstperson_lefthand':xf([0,0,0],[0,-1,0],.7)},'elements':self.parts}
 def sculpt(self):
  b=self.box;k=self.kind;f=self.feature
  def p(n,y,t,w,h,d,m,ang=0):b(n,0,y,t,w,h,d,m,ang)
  def flank(n,y,t,w,h,d,m,ang=0,x=1.53):
   for s in (-1,1):b(n,s*x,y,t,w,h,d,m,ang)
  def grip(t,lean=22.5):
   p('raked grip',5.5,t,1.73,4.45,2.35,RUBBER,lean)
   p('grip shoe',3.55,t+.55,1.95,.69,2.35,METAL)
   p('guard',5.5,t-2.05,.62,.44,2.9,METAL);p('finger bow',6.14,t-3.3,.63,1.7,.48,METAL,-22.5)
  def stock(start,mode='open'):
   end=23.;length=end-start
   p('comb',8.38,(end+start)/2,2.1,1.25,length,PLATE)
   p('heel',7.08,end-.2,2.55,3.65,1.15,RUBBER,-22.5)
   if mode=='open':p('lower stock brace',6.3,(end+start)/2,1.1,.78,length-.2,METAL,-22.5)
   else:p('stock cheek',7.4,(end+start)/2,2.35,2.05,length-.8,TRIM)
  def barrel(t,d,w=1.7,h=1.63,y=8.7):
   p('barrel',y,t,w,h,d,METAL);p('muzzle ferrule',y,t-d/2+.42,w+.37,h+.37,.86,DARK)
   p('muzzle shadow',y,t-d/2-.03,w*.59,h*.60,.14,RUBBER)
  if k=='bow':
   for s in (-1,1):
    b('swept limb',-1.45,8+s*5.3,12.8,1.25,8.9,1.65,PLATE,22.5*s)
    b('limb tip',-1.45,8+s*10.85,16.1,.92,4.5,1.35,TRIM,45*s)
    b('upper string' if s==1 else 'lower string',-1.42,8+s*6.13,19.78,.20,13.26,.22,METAL,-22.5*s)
    b('cam',-1.44,8+s*12.25,17.25,1.47,1.85,1.85,DARK,45,'y')
    b('limb inlay',-2.1,8+s*5.65,12.7,.17,5.5,.62,LIGHT,22.5*s)
   b('riser',-1.35,8,12.9,1.45,7.2,2.3,METAL);b('grip wrap',-.94,7.1,13.7,1.45,3.65,1.7,RUBBER)
   b('shaft',-1.42,8,11.35,.29,.29,21.93,METAL);b('broadhead',-1.42,8,.3,.76,.8,1.3,TRIM)
  elif k in ('revolver','slab','claw'):
   grip(19);p('frame',7.8,14.6,2.4,2.05,11.8,PLATE);p('upper barrel',9.24,8.5,2.03,2.05,15.8,PLATE)
   p('underlug',7.72,6.4,1.73,1.17,11,TRIM);p('crown',9.18,1,2.20,2.2,1.05,METAL)
   p('bore',9.23,.42,.92,.95,.18,DARK)
   p('rear shoulder',8.97,18.7,2.25,2.15,4.9,METAL);p('hammer',10.1,20.2,.80,.98,1.25,DARK,22.5)
   if k=='revolver':
    p('cylinder body',8.53,14.1,3.34,2.20,5,METAL);p('cylinder flats',8.54,14.1,2.63,2.95,4.9,TRIM)
    flank('flutes',8.65,14.1,.13,.4,3.12,DARK,x=1.7)
   else:flank('slide panels',8.9,8.4,.32,1.23,12.2,TRIM,x=1.04)
  elif k in ('launcher','dual','gauntlet','grav'):
   if k=='dual':
    for side in (-1,1):
     b('stacked rocket tube',0,8.4+side*1.4,8.7,4.12,2.65,16,PLATE)
     b('recessed muzzle',0,8.4+side*1.4,.61,3.23,1.85,.3,DARK)
    flank('outer shell',8.4,10,.65,4.7,15.2,TRIM,x=2.15)
   elif k=='gauntlet':
    p('furnace housing',8.4,10,5.75,4.35,18,PLATE);p('box muzzle',8.32,1.8,5.9,4,2.7,TRIM)
    p('hot chamber',8.32,.39,4.62,3.17,.3,DARK);p('forge core',8.25,.20,2.3,1.23,.2,LIGHT)
   else:
    p('cast tube',8.55,10,4.72,4.00,18,PLATE);p('faceted body',8.6,9.8,3.55,4.8,16,PLATE)
    for t in (2,8,17):p('reinforced collar',8.6,t,5.08,4.4,1.15,TRIM)
    p('mouth',8.55,.9,4.35,3.63,.5,DARK);p('throat',8.55,.59,2.9,2.4,.17,METAL)
   grip(16);p('shoulder rest',7.75,20.5,3.12,2.6,6,METAL)
  else:
   heavy=k in ('rotary','heavy','cluster','worm','rail','trace','breach')
   short=k in ('compact','pump','twin','lever','breach')
   bz={'sheath':12,'rod':12,'sniper':12,'scout':9,'worm':10,'rail':12,'trace':12,'fusion':10,'rotary':12,'cluster':11,'lever':10,'twin':12}.get(k,8)
   width=3.35 if heavy else 2.76
   p('receiver core',8.22,(bz+18.8)/2,width,2.8,19.6-bz,PLATE)
   p('upper rail',9.68,(bz+18)/2,width*.77,.64,18.9-bz,METAL)
   grip(15.8 if k!='bullpup' else 12.3)
   if k not in ('worm','rotary','twin','sheath','rod','breach'):stock(17,'solid' if k in ('bullpup','compact','hive') else 'open')
   else:stock(17.7,'open')
   if k in ('rotary','cluster','twin'):
    if k=='rotary': positions=[(math.cos(i*math.tau/6)*1.34,math.sin(i*math.tau/6)*1.34) for i in range(6)]
    elif k=='twin':positions=[(-1.05,0),(1.05,0)]
    else:positions=[(-.95,.84),(.95,.84),(-.95,-.84),(.95,-.84)]
    for j,(x,y) in enumerate(positions):
     b('individual bore',x,8.7+y,bz/2+.25,1.36,1.36,bz+1,METAL if f!='roots' else (TRIM if j==0 else LIGHT))
     b('dark bore',x,8.7+y,-.29,.83,.83,.12,DARK)
    for t in (2.3,bz-2):p('reinforcing band',8.7,t,4.15,3.8,1.2,TRIM)
   elif k=='sheath':
    p('ceramic blade sheath',8.82,6.75,2.7,3.04,14.3,PLATE);p('chisel face',8.62,.22,2.4,2.4,2.0,PLATE,-22.5);p('blade spine',10.3,7.2,2.24,.29,13.7,EDGE)
   elif k=='worm':
    barrel(6.4,13,2.42,2.0)
    for i in range(6):
     t=.7+i*1.96;p('worm carapace',8.63,t,2.48+i*.09,2.25,2.8,EDGE if i<3 else METAL,22.5)
     flank('swept Taken splinter',9.1,t-.4,.42,1.43,3.45,LIGHT if i<3 else TRIM,22.5,1.14+i*.08)
   elif k in ('rail','trace','fusion'):
    for s in (-1,1):b('split energy rail',s*1.16,8.8,bz/2,.78,1.62,bz,PLATE)
    p('emitter spine',8.18,bz/2,1.08,.89,bz-.45,METAL)
    p('emitter point',8.18,.32,.68,.5,.52,LIGHT)
    p('charge chamber',8.18,bz,2.81,2.49,4.1,TRIM)
   else:
    barrel(bz/2,bz+.4,2.35 if k in ('pump','breach','lever') else 1.32,2.2 if k in ('breach','lever') else 1.32)
    if k not in ('sniper','rod'):
     p('fore-end',7.76,bz*.65,2.5,1.45,bz*.68,TRIM)
    if k=='pump':p('tube below barrel',7.15,bz*.48,1.29,1.21,bz-.68,METAL)
   if k in ('rifle','scout','compact','sniper','bullpup','hive','heavy'):
    t=19.0 if k=='bullpup' else 11.7
    p('magazine',5.8,t,2.18,3.64,2.55,METAL,-22.5 if k in ('rifle','compact') else 0)
    p('magazine floor',4.05,t-.42 if k in ('rifle','compact') else t,2.30,.53,2.69,DARK)
  self.signature()
  self.sight()
 def sight(self):
  b=self.box;short=self.kind in ('revolver','slab','claw')
  rear=18.6 if not short else 18.2
  # The clear axis is x=8, y=14. Body, clamp and posts all stop below it where centered.
  if self.optic=='open':
   for s in (-1,1):b('rear notch ear',s*1.28,12.81,rear,.58,2.19,.84,METAL)
   b('notch bridge',0,11.68,rear,2.95,.51,.88,DARK);b('rear dovetail',0,10.65,rear,1.2,1.66,.91,METAL)
   b('front blade',0,12.55,2.3,.36,2.08,.68,METAL);b('front bead',0,13.65,2.3,.42,.26,.66,LIGHT)
   b('front saddle',0,10.75,2.3,.72,1.8,.79,DARK)
  else:
   bow=self.kind=='bow'; barrel=self.optic=='scope'
   rear=16.3 if not bow else 13.7
   wide=2.15 if barrel else 2.03
   for t in ([rear,rear-3.6] if barrel else [rear]):
    depth=1.03 if barrel else .54
    for s in (-1,1):
     b('optic wall',s*(wide+.25),14,t,.49,3.45,depth,PLATE if barrel else METAL)
     b('rim bevel',s*(wide+.02),15.9,t,.64,.65,depth,TRIM,45,'z')
    b('hood',0,16.21,t,wide*2,.48,depth,TRIM);b('sill',0,11.98,t,wide*2+.3,.53,depth,DARK)
   if barrel:
    for s in (-1,1):b('scope rail',s*(wide+.25),14,rear-1.8,.47,3.05,3.55,DARK)
    b('scope roof',0,16.16,rear-1.8,wide*2,.38,3.5,PLATE)
   b('optic shoe',-1.05 if bow else 0,10.84,rear,1.18,1.84,1.65,METAL)
 def signature(self):
  f=self.feature;b=self.box
  def part(n,y,t,w,h,d,m,a=0):b(n,0,y,t,w,h,d,m,a)
  def sides(n,x,y,t,w,h,d,m,a=0):
   for s in (-1,1):b(n,x*s,y,t,w,h,d,m,a)
  def bands(ts,w,h,y=8.6,m=TRIM):
   for t in ts:part('cast band',y,t,w,h,.65,m)
  if f.startswith('eclipse'):
   long=f=='eclipse_long'; pistol=f=='eclipse_hc';twin=f=='eclipse_sg'
   # Overlapping enamel shell, silver exposed casting and the inspection model's circular shoulder pivots.
   sides('ivory carapace',1.27,9.3,11.5,.64,2,12.8,PLATE)
   sides('scalloped nose',1.12,9.08,4.6,.64,1.36,4.4,TRIM,-22.5)
   sides('porcelain lip',1.47,8.43,7.7,.46,.54,6.8,EDGE)
   sides('hinge boss',1.78,9.05,15.6,.42,1.7,1.7,TRIM,45)
   sides('hinge pin',2.02,9.05,15.6,.12,.8,.8,METAL,45)
   if not twin:
    sides('ruby channel',1.51,8.06,10 if long else 12.6,.34,.76,11 if long else 6,LIGHT,-22.5 if not long else 0)
   if not twin:
    for s in (-1,1):b('sensor whisker',s*.78,5.34,5.25,.15,3.4,.24,METAL,45)
   if twin:
    for s in (-1,1):b('engraved barrel jacket',s*1.03,8.8,4.7,1.86,1.96,9.2,METAL)
   if long:part('red dorsal plate',10.2,10.1,1.85,.85,8,LIGHT)
   if pistol:part('pointed prow',9.25,1.28,1.9,1.14,3.85,PLATE,-22.5)
  elif f.startswith('plunder'):
   pistol=f.endswith('side');scout=f.endswith('scout')
   bands([4.8,6.0,8.4,10],2.64,2.43,8.65,METAL)
   sides('brass housing',1.5,8.58,13,.39,1.98,4.8,TRIM)
   sides('keel blade',.9,6.55,5.6,.26,.7,5.9,MARK,-22.5)
   sides('lashings',1.76,8.4,12.5,.25,2,.47,DARK,22.5)
   part('crowned tang',10.85,15.75,.71,2.35,.63,TRIM,-22.5)
   if scout:part('long brass tube',8.76,3.7,1.98,1.76,7.5,TRIM)
   if pistol:part('flared round muzzle',8.97,1.1,3.2,3.16,1.56,METAL)
   if f.endswith('smg'):part('long box magazine',3.9,11.7,1.91,4.25,2.15,METAL)
  elif f.startswith('hive_') or f=='necro':
   for j,t in enumerate((3.5,7,10.4,13.55,16.5)):
    sides('chitin overlapping scute',1.29,9.14,t,.64,1.83,3.65,TRIM if j%3==0 else PLATE,-22.5)
    sides('luminous fissure',1.64,8.66,t-.65,.13,.41,1.53,LIGHT,-22.5)
   if f in ('necro','hive_pump','hive_ar'):part('chitin jaw',7.55,2.5,2.26,.98,5.6,PLATE,-22.5)
   if f=='necro':part('lower fang',7.15,1.2,.86,.52,5.6,METAL,-22.5)
   if f=='hive_mg':
    part('drum',5.92,12.5,4.6,3.08,3.44,METAL)
    for j in range(5):b('ammo links',2.15,7.6-j*.45,14.5,.9,.42,1.4,MARK)
  elif f in ('western','spade','lamp','sun','teeth','thorns'):
   if f=='western':
    part('gold barrel rib',10.30,9.05,1.79,.58,17.3,TRIM);part('walnut backstrap',5.4,19.8,1.92,3.62,1.31,PLATE,22.5)
   if f=='spade':
    for j in (0,1):sides('ivory diagonal stripe',1.055,9.25,6.0+j*1.64,.13,1.95,.72,TRIM,-22.5)
    sides('spade suit',1.79,8.7,14.2,.13,1,1,TRIM,45);sides('spade point',1.79,8.14,14.2,.13,.4,.49,TRIM)
   if f=='lamp':
    b('side-mounted light',2.04,8,4.8,1.25,1.32,3.23,METAL);b('light face',2.04,8,3.12,.98,.92,.16,LIGHT)
   if f=='sun':
    part('golden upper vane',10.43,9.2,2.17,.63,17.65,TRIM)
    part('silver lower vane',7.07,8.8,2.07,.62,17.4,MARK)
    sides('copper heat conduit',1.18,8.78,8.82,.24,.57,11.9,LIGHT)
   if f=='teeth':
    part('bayonet spine',7.0,1.7,.72,.86,7.5,DARK);part('bloody tip',6.92,-1.8,.59,.56,2.15,PLATE,-22.5)
    for t in (2,4.1,6.2):part('serration',6.54,t,.7,.57,1.0,METAL,45)
   if f=='thorns':
    for j,t in enumerate((2.1,5.8,10.8,15.6)):
     sides('poison barb',1.16,9.32,t,.53,.97,3.31,PLATE,22.5)
    part('hooked lower jaw',7.35,2,1.22,1.51,4.2,TRIM,-22.5);sides('soulfire eye',1.37,8.65,7.8,.34,1.02,1.14,LIGHT,45)
  elif f=='izanagi':
   part('sword guard',9.06,14.0,3.28,5.81,1.02,DARK,-22.5)
   sides('blue sheath line',1.38,8.99,7.1,.12,.23,12.5,LIGHT)
   for t,y in ((6.8,9.35),(7.8,9.1),(8.55,8.86)):
    sides('blue armory crest',1.4,y,t,.14,.62,.98,LIGHT,-22.5)
  elif f=='taken':
   for j in range(4):
    sides('white muzzle tendril',1.17+j*.12,9.12+j*.23,.8+j*.73,.46,.60,3.2,LIGHT,22.5)
   part('white muzzle core',8.73,.22,2.8,2.43,2.9,LIGHT)
   for j in range(5):sides('Taken filament',1.43,8.63,4.55+j*1.75,.19,.28,1.52,MARK,-22.5)
  elif f=='service':
   part('wood stock',7.8,20.7,2.35,2.4,5.3,TRIM);bands((6.4,8.3),2.68,2.39,8.18)
   for t in (6.9,7.8,8.7,9.6):sides('fore-end slots',1.28,8.1,t,.17,.46,.51,DARK)
  elif f=='liquid':
   part('Omolon slab',9.15,7.7,2.55,2.15,14.45,PLATE);part('lower fork',6.73,6.53,1.42,.84,12.65,PLATE)
   sides('fluid canister',1.66,7.45,12.0,.63,2.32,2.04,LIGHT,45)
  elif f=='gatling':
   part('carrying chassis',7.24,7.65,3.15,.78,14.11,PLATE);part('angled foregrip',5.33,10.4,1,3.4,1.3,TRIM,22.5)
   bands((2.2,10.4,14.1),4.30,3.85,8.65,TRIM)
  elif f=='rapier':
   part('long bayonet',7.52,.95,.51,.93,12.0,METAL);part('bayonet point',7.58,-5.1,.41,.52,1.28,METAL,-22.5)
   part('ivory shroud',9.33,10.7,2.65,2.11,13.3,PLATE)
  elif f=='junk':
   sides('scrap braces',1.97,8.63,8,.42,3.75,1.21,TRIM,22.5);b('salvaged wrap',-1.7,9.3,5.9,.57,1.53,3.2,PLATE)
   part('tall rear cage',10.69,17.42,2.1,1.0,3.4,METAL)
  elif f=='storm':
   part('storm staff',8.9,7.2,.88,.95,17.1,TRIM);b('side conduit',1.0,8.07,7.2,.39,.62,15.7,PLATE)
   for t in (.4,3.1,8.1):part('staff bands',8.9,t,1.67,1.7,.48,METAL)
  elif f=='computer':
   sides('rangefinder console',1.63,10.2,15.7,.77,2.26,6.3,TRIM);b('cyan display',2.09,10.2,16,.15,1.4,3.6,LIGHT)
   for j in range(3):sides('data cable',1.45,6.9,11+j*1.55,.4,1.92,.53,LIGHT,-22.5)
  elif f=='heat':
   part('power pack',8.05,15.2,3.64,3.2,6.25,PLATE)
   for t in (1.4,3,4.6,6.2,7.8):part('radiator',8.7,t,2.44,2.38,.55,DARK,45)
   sides('white containment shell',1.77,9.25,13.55,.47,1.62,4.2,TRIM)
  elif f=='lever':
   part('engraved barrel shroud',8.76,7.95,2.67,2.82,15.9,METAL)
   part('lever loop heel',4.73,15.22,.56,.57,3.72,TRIM);part('lever return',5.73,13.38,.53,2.39,.5,TRIM,-22.5)
  elif f in ('ikelos','sleeper'):
   for t in (4,7.25,10.5,13.75):
    sides('Warmind diamond truss',1.57,8.81,t,.34,2.34,2.34,METAL,45);sides('inset truss',1.76,8.81,t,.12,1.65,1.65,DARK,45)
   if f=='sleeper':part('splitter point',7.89,1.42,1.34,1.77,4.16,DARK,-22.5)
  elif f=='roots':
   for j in range(4):
    for s in (-1,1):b('elemental root',s*1.53,8.21+j*.23,5.4+j*3.5,.48,.58,4.3,TRIM if s<0 else LIGHT,22.5 if j%2 else -22.5)
   part('forked wrist spur',10.87,21,.64,.72,4.7,TRIM,-22.5)
  elif f=='tractor':
   for s in (-1,1):
    b('field claw',s*1.62,8.9,.1,.89,1.06,4.5,DARK)
    b('swept white handle',s*2.12,10.5,15.35,.91,1.22,7.35,PLATE,22.5)
   part('white tail hoop',10.21,20.33,4.7,1.3,1.36,PLATE)
  elif f=='wolves':
   for t in (6.5,10.25,15.8):bands([t],4.33,3.66,8.64,METAL)
   part('dangling foregrip',5.62,6.34,1.33,3.85,1.41,RUBBER)
  elif f=='mytho':
   part('long Vex muzzle',8.9,1.2,1.12,1.07,8.56,METAL)
   sides('sweeping Vex casing',1.42,9.85,15,.72,1.55,10.6,TRIM,-22.5)
   part('gold lower fork',6.77,10.3,1.35,.83,9.55,PLATE,-22.5);sides('Vex socket',1.89,9.34,16.3,.31,1.85,1.85,PLATE,45)
  elif f.startswith('hezen'):
   sides('brass receiver yoke',1.38,8.62,12.2,.49,1.69,7.25,TRIM)
   if f=='hezen_long':part('barrel underbrace',7.49,4.7,.59,.57,8.05,METAL)
   if f=='hezen_scout':
    for s in (-1,1):b('folded bipod',s*.7,6.02,4.54,.42,.42,6.42,METAL,-22.5)
   if f=='hezen_pump':part('bronze slide',7.09,7.3,2.44,1.22,7.6,TRIM)
  elif f in ('telesto','merciless','crucible','veist'):
   part('cantilever top casing',10.03,7.8,2.66,1.41,13.42,PLATE)
   sides('charge cell window',1.46,8.44,9.52,.24,.66,4.63,LIGHT)
   if f=='telesto':part('loop grip return',4.98,15.12,1.2,.7,6.63,DARK)
   if f=='merciless':
    for t in (6,7.45,8.9,10.35,11.8):part('exposed hot cell',8.56,t,2.52,.94,.72,LIGHT)
   if f=='crucible':sides('Crucible slash',1.49,9.84,8,.16,1.56,.83,TRIM,45)
   if f=='veist':
    for t in (2.7,4.1,5.5,6.9,8.3):sides('vent',1.46,9.77,t,.17,.56,.64,DARK)
  elif f=='forge':
   sides('forge badge',2.98,8.37,8.43,.26,2.12,2.12,TRIM,45);sides('black armory inset',3.13,8.37,8.43,.16,1.21,1.21,DARK,45)
  elif f in ('cryo','prism'):
   sides('projector fork',1.35,9.26,4.14,.92,1.45,8.9,TRIM)
   sides('ventral fork',1.21,6.86,4.95,.69,.72,9.85,PLATE)
   sides('central focusing crystal',1.62,8.36,12.18,.45,2,2,LIGHT,45)
   if f=='cryo':
    for t in (9.45,10.8,12.25,13.7):bands([t],3.12,2.8,8.54,TRIM)
  elif f=='wolf':
   sides('gold wolf cheek',2.4,8.66,12.3,.41,2.0,2.8,TRIM,22.5)
   sides('long wolf snout',2.47,8.1,10.36,.38,.83,2.2,TRIM)
   sides('wolf ear',2.34,10.15,13.15,.38,1.42,.92,MARK,-22.5)
   sides('wolf eye',2.66,9.08,11.72,.13,.33,.49,DARK);bands((3.3,15.3),5.2,4.48,8.56,TRIM)
  elif f=='fox':
   sides('fox ear stripe',2.54,9.5,12.65,.16,1.13,2.14,MARK,45)
   sides('painted eye',2.54,8.5,10.83,.16,.45,1.85,PLATE,-22.5)
  elif f=='blight':
   part('break-action chamber',8.7,8.32,3,3.0,7.8,DARK)
   for t in (3.5,7.8,10.6):bands([t],3.08,3.04,8.7,METAL)
   sides('glass blight vessel',1.61,8.65,12.8,.36,1.63,1.77,LIGHT,45)
  elif f=='butterfly':
   for s in (-1,1):b('butterfly wing',-1.72,8+s*3.34,13.3,.57,2.44,1.53,TRIM,45*s)
def build():
 for row in ROSTER:
  w=Weapon(row);w.sculpt()
  from detail import detail,make_paint
  from motion import groups,bake
  import energy,exotics
  detail(w);energy.finish(w);exotics.finish(w);a,r,parts=groups(w);m=w.model()
  m['textures']['detail']='destiny2mc:item/'+w.id+'_finish'
  bake(w,m,parts,A)
  exotics.paint(w,energy.paint(w,make_paint(w))).save(A/'textures/item'/f'{w.id}_finish.png')
  assert all(-16<=v<=32 for e in w.parts for k in ('from','to') for v in e[k]),w.id
  (A/'models/item'/f'{w.id}.json').write_text(json.dumps(m,separators=(',',':')))
  w.paint().save(A/'textures/item'/f'{w.id}.png')
if __name__=='__main__':build()
