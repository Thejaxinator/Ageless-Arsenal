"""Generate the eighteen Ageless Arsenals sculpts, their rig groups and the camera mounts.
 
Coordinates in each recipe use a shared workbench: x measures lateral distance from the
centerline, y is height in model pixels, and t runs from muzzle (0) to stock (24).
Longitudinal lengths scale with the catalog row. A part gets one rigid group and can
therefore never render twice when split for first- or third-person animation.
"""
import argparse
import base64
import json
import math
import random
import re
import uuid
from copy import deepcopy
from pathlib import Path
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parent
# id, body class, scale length, four key colors, optic and mechanism.
SPECS=[
 ('wire_rifle','sniper',34,'966a4e bf996e 51474b ff9875','scope','BOLT','CELL'),
 ('vex_slap_rifle','fusion',23,'b89752 cfba76 525652 ef6f38','open','CHARGE','CELL'),
 ('hive_boomer','launcher',24,'494942 817e71 383639 83aee9','open','CHARGE','CELL'),
 ('cabal_slug_rifle','rifle',23,'92765b 655852 4c4642 ee9b56','open','BOLT','CELL'),
 ('taken_shrapnel_launcher','cluster',25,'252d36 56636d 383944 efffff','reflex','PUMP','GATE'),
 ('scorn_crossbow','sniper',28,'82694f bab193 403f3e b98bea','scope','BOLT','CELL'),
 ('subjugator_glaive','trace',35,'343c40 a29578 73675c 70e7b3','open','CHARGE','CELL'),
 ('outbreak_perfected','bullpup',28,'3b373a b54342 585353 f84742','reflex','BOLT','CELL'),
 ('graviton_lance','bullpup',29,'362e43 a48a80 575263 b095f4','reflex','CHARGE','CELL'),
 ('riskrunner','compact',23,'d9dbd4 62a899 515e60 a5f9ef','open','BOLT','CELL'),
 ('dead_mans_tale','lever',33,'5e4a39 9d8054 555558 d6bc88','open','LEVER','GATE'),
 ('forerunner','slab',17,'e7e7db 91928e 43484c 9edca7','open','SLIDE','CELL'),
 ('arbalest','rail',30,'686a63 b3afa1 494949 caa775','reflex','CHARGE','CELL'),
 ('hammerhead','heavy',30,'463b3e 9a383d 484950 e76862','reflex','BOLT','CELL'),
 ('midnight_coup','revolver',18,'e9e1ce b79a50 79716f d2bde9','open','HAMMER','CYLINDER'),
 ('the_recluse','compact',23,'deded9 bd3d4e 58565a d36b7c','reflex','BOLT','CELL'),
 ('hung_jury_sr4','scout',28,'dededa 524e57 69727d 9b89d9','reflex','BOLT','CELL'),
 ('bxr_55_battler','bullpup',30,'859386 c8c7b7 515b56 9abc8f','scope','BOLT','CELL'),
]
DARK,PLATE,TRIM,STEEL,LIGHT,RUBBER,EDGE,MARK=range(8)
SIDES=('north','south','up','down','east','west')
def write(path,obj):
 path.parent.mkdir(exist_ok=True,parents=True);path.write_text(json.dumps(obj,separators=(',',':'),ensure_ascii=False))
def rgb(c):return tuple(bytes.fromhex(c))
def tint(c,v):return tuple(min(255,max(0,i+v)) for i in c)
class Sculpt:
 def __init__(self,row):
  self.key,self.kind,self.length,palette,self.optic,self.action,self.reload=row
  c=list(map(rgb,palette.split()))
  self.colors=[(35,36,41),*c,(45,41,43),tint(c[0],26),tint(c[1],28)]
  if self.key=='subjugator_glaive':self.colors[MARK]=(175,230,183)
  self.axis={'scope':12.55,'reflex':12.15,'open':12.02}[self.optic]
  self.pieces=[];self.group_names=[];self.contact={}
 def z(self,t):return 8+(t-12)*self.length/24
 def box(self,name,x,y,t,w,h,d,m,a=0,axis='x',g='body',paint=False,glow=False):
  """Bind visible paint to each side explicitly. 'glow' keeps only designated faces emissive."""
  z=self.z(t);d=d*self.length/24
  origin=[8+x,y,z]; low=[round(i-j/2,4) for i,j in zip(origin,(w,h,d))]; high=[round(i+j/2,4) for i,j in zip(origin,(w,h,d))]
  u=m%4*4+.14;v=m//4*8+.2; faces={}
  for f in SIDES:
   side=f in ('east','west'); du=min(3.7,max(.35,(d if side else w)*.45));dv=min(7.4,max(.4,(h if f not in ('up','down') else d)*.55))
   coords=[u+du,v,u,v+dv] if f=='east' else [u,v,u+du,v+dv]
   if paint and side:coords=[15.9,m*2+.09,.1,m*2+1.91] if f=='east' else [.1,m*2+.09,15.9,m*2+1.91]
   faces[f]={'texture':'#detail' if paint and side else '#all','uv':[round(j,4) for j in coords]}
   if glow:faces[f]['neoforge_data']={'block_light':10,'sky_light':10}
  e={'name':name,'from':low,'to':high,'faces':faces}
  if a:e['rotation']={'origin':[round(i,4) for i in origin],'axis':axis,'angle':a,'rescale':False}
  self.pieces.append(e);self.group_names.append(g)
 def body(self,n,y,t,w,h,d,m,a=0,**kw):self.box(n,0,y,t,w,h,d,m,a,**kw)
 def twin(self,n,x,y,t,w,h,d,m,a=0,**kw):
  for side in (-1,1):self.box(n,side*x,y,t,w,h,d,m,a,**kw)
 def handle(self,t,y=5.8,mat=RUBBER):
  self.body('raked grip',y,t,1.56,3.85,1.8,mat,22.5,paint=True);self.contact['grip']=(0,y,t)
 def fore(self,t,y,style=STEEL):
  self.body('fore-end',y,t,1.46,.94,3.22,style,paint=True);self.contact['support']=(0,y-.27,t)
 def charge(self,t,y=8.6,w=1.6,h=1.4,d=2.35):
  self.body('charge chamber',y,t,w,h,d,LIGHT,g='action',glow=True)
 def carrier(self,t,y=9.0,x=1.22):
  self.box('bolt carrier',x,y,t,.50,.78,2.41,STEEL,g='action')
  self.box('charging tab',x+.32,y,t+.58,.78,.37,.52,EDGE,g='action')
 def cell(self,t,y,w,h,d,m=STEEL,a=0):
  self.body('power cassette',y,t,w,h,d,m,a,g='load',paint=True)
  self.contact['loader']=(0,y,t)
 def gate(self,t=13.5):
  self.box('loading gate',1.21,7.45,t,.24,1.1,2.45,TRIM,g='load');self.contact['loader']=(1.22,7.45,t)
 def rifle_stock(self,t=20,h=8.3,cut=True,mat=PLATE):
  self.body('cheek rest',h,t,2.03,1.05,6.05,mat,paint=True)
  self.body('butt pad',h-.69,t+2.67,2.15,2.64,.8,RUBBER,-22.5)
  self.body('lower stock web',h-1.3,t-.15,1.32,.64 if cut else 1.66,5.65,mat,22.5)
 def barrel(self,t,d,w=1.46,h=1.4,y=8.62,m=STEEL):
  self.body('barrel',y,t,w,h,d,m)
  self.body('crown',y,t-d/2+.29,w+.26,h+.26,.65,m)
  self.body('bore',y,t-d/2-.07,w*.59,h*.6,.16,DARK)
 def sight_foot(self,t,bottom):
  # Find the topmost fixed surface underneath this optic, including pitched pieces.
  # A narrow riser joins the sight to a real shell while keeping the eye channel clear.
  z=self.z(t)
  def inside(e,y):
   p=[8,y,z]
   if 'rotation' in e:
    r=e['rotation'];o=r['origin'];x,y0,z0=(v-base for v,base in zip(p,o))
    ang=math.radians(-r['angle']);c=math.cos(ang);s=math.sin(ang)
    q={'x':(x,y0*c-z0*s,y0*s+z0*c),'y':(x*c+z0*s,y0,-x*s+z0*c),'z':(x*c-y0*s,x*s+y0*c,z0)}[r['axis']]
    p=[v+base for v,base in zip(q,o)]
   return all(lo-1e-5<=v<=hi+1e-5 for lo,v,hi in zip(e['from'],p,e['to']))
  for i in range(62):
   y=bottom-i*.12
   if any(g=='body' and inside(e,y) for g,e in zip(self.group_names,self.pieces)):
    if i>0:self.body('sight riser', (bottom+y)/2,t,.68,bottom-y+.1,.64,STEEL)
    return
 def sight(self):
  """Physical holes, front pip below the center ray, no solid crosshair lens."""
  if self.key=='subjugator_glaive':
   # Preserve the polearm profile. Paired runes on the forward collar mark the direction
   # of travel underneath the unobstructed native crosshair.
   self.twin('collar direction rune',.78,8.81,8.4,.13,.34,1.75,LIGHT,glow=True)
   return
  y=self.axis; scope=self.optic=='scope';ring=self.optic!='open'
  front=6.35 if self.key=='riskrunner' else 2
  rear=16.3 if ring else 18.6
  if ring:
   half=1.6 if scope else 1.76
   for t in ([rear,rear-3.6] if scope else [rear]):
    self.sight_foot(t,y-2.7)
    self.twin('ocular sidewall',half,y,t,.38,2.30,.67 if scope else .49,STEEL)
    self.body('ocular bridge',y+1.16,t,half*2+.38,.33,.67 if scope else .49,PLATE)
    self.body('ocular sill',y-1.23,t,half*2+.38,.38,.67 if scope else .49,STEEL)
    self.body('optic dovetail',y-2.05,t,1.08,1.30,1.05,STEEL)
   if scope:
    self.twin('telescopic side rail',half,y,rear-1.8,.33,.83,3.5,STEEL)
   else:self.twin('reflex fiducial',half-.23,y-.19,rear+.03,.09,.27,.59,LIGHT,glow=True)
  else:
   self.sight_foot(rear,y-2.005)
   if self.key!='hive_boomer':self.sight_foot(front,y-2.295)
   self.twin('notch',1.05,y-.59,rear,.54,1.44,.62,STEEL)
   self.body('rear dovetail',y-1.65,rear,2.53,.71,.90,STEEL)
   if self.key!='hive_boomer':
    self.body('bead pedestal',y-1.51,front,.55,1.57,.7,STEEL)
    self.body('front bead',y-.65,front,.35,.27,.58,LIGHT)
 def rings(self,n,ts,y,w,h,d,m):
  for t in ts:self.body(n,y,t,w,h,d,m)
 def carve(self):
  globals()[self.key](self)
  self.sight()
  assert all(-16<=p<=32 for e in self.pieces for side in ('from','to') for p in e[side]),self.key
  assert set(self.group_names)=={'body','load','action'},self.key
  assert set(self.contact)=={'grip','support','loader'},self.key

# Faction hardware is modeled in seven separate recipes. Each has different barrel,
# grip and ammunition geometry, even when the weapon shares a firing family.
def wire_rifle(w):
 b=w.body
 w.barrel(5.65,11.5,1.14,1.22);w.twin('wire guide',.56,8.3,4.5,.24,.43,7.9,STEEL)
 b('red ion tip',8.23,.16,.39,.55,.36,LIGHT,glow=True)
 for t in (2.1,3.5,4.95):b('back swept muzzle barb',7.98,t,.48,.65,1.25,STEEL,-45)
 b('limb housing',8.75,13.4,2.8,2.53,8.58,PLATE,paint=True)
 w.twin('socket cheek',1.21,8.3,15.1,.8,2.8,3.6,TRIM,-22.5)
 b('receiver saddle',9.67,15.3,2.25,1.08,5,EDGE)
 w.rings('ether coupling',(7.8,8.5,9.2),8.6,1.83,1.80,.39,TRIM)
 w.twin('wishbone strut',.82,7.78,20,.48,.72,6.52,STEEL,-22.5)
 b('rear bridge',8.72,22.9,2.30,1.44,.96,TRIM)
 w.handle(17);w.fore(9.2,7.27);w.carrier(15.8,9.14,1.52);w.cell(12.3,6.56,2.25,2.30,2.8,TRIM)
def vex_slap_rifle(w):
 b=w.body
 b('upper emitter spar',9.23,7.1,1.48,1.1,12.5,TRIM);b('ventral spar',6.69,8.55,2.15,1.16,13.2,PLATE)
 w.twin('bifurcated bronze shell',1.12,9.46,12.9,.85,1.6,9.9,PLATE,paint=True)
 b('emitter jaw',8.9,2.15,1.45,1.54,2.72,TRIM,-22.5)
 b('hollow frame back',8.3,16.9,2.71,3.3,2.2,STEEL)
 w.twin('rear fork',1.12,8.85,20,.71,1.12,6.4,PLATE)
 b('upper rear prong',10.0,21.9,2.91,.86,3.52,TRIM)
 w.charge(7.3,8.05,1.55,.64,4.68);w.handle(15.65);w.fore(8.7,6.64,PLATE);w.cell(13.55,6.08,2.14,1.66,3.85)
 for t in (11.4,12.4,13.4):w.twin('radiolarian slit',1.63,9.35,t,.14,.37,.49,LIGHT,glow=True)
def hive_boomer(w):
 b=w.body
 b('sawn fossil receiver',8.22,14,3.43,3.6,9.7,PLATE,paint=True)
 b('ossified tail',7.88,20,2.72,2.62,5.8,STEEL,-22.5)
 for s in (-1,1):
  w.box('outer mandible',s*1.65,8.63,7.26,.88,2.73,11.5,TRIM,22.5,paint=True)
  w.box('tusk tip',s*1.6,9.94,2.84,.65,1.22,5.8,EDGE,22.5)
  w.box('ventral root',s*1.38,6.92,9,.71,.85,7.7,STEEL,22.5)
 for i in range(5):w.twin('carapace ring',1.76,8.65,10.4+i*1.22,.46,2.85,.56,TRIM,22.5)
 w.charge(6.0,8.47,1.75,.94,8.5);w.handle(17.55,5.82,STEEL);w.fore(9.15,6.75,STEEL);w.cell(12.8,5.96,2.43,1.62,3.23,TRIM)
def cabal_slug_rifle(w):
 b=w.body
 b('square pressure chamber',8.66,11,3.52,3.06,11.7,PLATE,paint=True)
 b('front block',8.58,4.2,3.03,2.65,5.07,TRIM);w.barrel(1.48,3.5,1.91,1.61)
 b('rear casing',9.12,19.3,2.94,2.42,6.46,PLATE,paint=True)
 b('top spine',10.41,11.8,2.31,.45,12.3,STEEL)
 w.twin('forward armor lip',1.76,8.8,7.7,.31,3.45,1.12,TRIM)
 b('heavy angled handguard',5.24,13.4,.94,.89,6.18,STEEL,-22.5)
 w.handle(17);w.fore(6.85,6.52);w.carrier(14.3,9.18,1.86);w.cell(11.8,4.98,2.72,4.72,4.75,TRIM,-22.5)
 b('slug box floor',2.85,11.03,2.85,.72,4.70,STEEL,-22.5,g='load')
 for t in (3.2,4.2,5.2):w.twin('slug vent',1.55,8.85,t,.17,.64,.51,DARK)
def taken_shrapnel_launcher(w):
 b=w.body
 b('blackened support tube',7.9,12.6,2.73,2.22,13.4,PLATE,paint=True)
 for x,y in ((-.82,8.45),(.82,8.45),(0,9.62)):
  w.box('stacked intake',x,y,6.3,1.24,1.25,10.48,STEEL)
  w.box('Taken muzzle flare',x,y,1.0,1.59,1.57,.92,EDGE)
  w.box('negative bore',x,y,.5,.82,.83,.14,DARK)
 w.rings('triple clamp',(4.8,8.8),8.72,3.37,2.9,.71,TRIM)
 b('forward slide',7.08,8.3,2.63,1.38,4.52,TRIM,g='action',paint=True)
 w.rifle_stock(20.2,8.18,True,STEEL);w.handle(16.7);w.fore(8.22,6.5);w.gate()
 for t in (10.9,12.2,13.5):w.twin('rift filament',1.39,8.3,t,.18,.42,1.5,LIGHT,-22.5,glow=True)
def scorn_crossbow(w):
 b=w.body
 b('bolt trough',8.58,10.1,1.6,1.05,16.3,STEEL);b('stacked salvaged housing',7.88,14.7,2.68,2.42,7.3,PLATE,paint=True)
 # The Raider mount holds the limbs upright. A lateral offset leaves a sight window
 # beside the limb. Both emitters turn back toward the string, as on the Scorn render.
 for side in (-1,1):
  x=side*1.86
  w.box('offset bow shoulder',x,8.3+side*1.05,6.6,1.24,2.46,2.1,STEEL)
  w.box('upright recurve limb',x,8.3+side*3.48,7.35,.88,5.42,.96,TRIM,side*22.5)
  w.box('bent ivory tip',x,8.3+side*6.22,8.84,.72,1.94,.75,PLATE,side*45)
  w.box('tension cable',x,8.3+side*3.41,12.06,.14,8.41,.19,STEEL,-side*45)
  w.box('arc prong',x,8.3+side*3.35,6.75,.31,4.56,.44,LIGHT,side*22.5,glow=True)
 b('wrapped dark-ether drum',8.02,12.0,3.27,2.54,5.90,TRIM,paint=True)
 b('faceted scavenged jacket',8.02,12.0,2.84,2.96,5.92,TRIM)
 w.rings('salvaged leather lashing',(10.5,13.6),8.02,3.45,3.13,.64,STEEL)
 w.twin('split bone scope hood',2.03,11.41,15.9,.49,1.15,3.1,TRIM,-22.5)
 b('bolt carrier',9.06,8.36,.46,.52,12.01,TRIM,g='action')
 b('forked bolt head',9.04,2.45,.88,.73,1.23,STEEL,g='action')
 w.rifle_stock(19.9,7.96,True,PLATE);w.handle(16.5);w.fore(9.6,7.22);w.cell(13.8,6.05,2.17,1.46,4.56,TRIM)
def subjugator_glaive(w):
 b=w.body
 b('segmented shaft',7.88,14,1.01,1.04,21.0,STEEL)
 for t in (5.9,7.8,8.4,12.2,17.2,20.3,22.45):b('bronze binding',7.88,t,1.41,1.44,.52,TRIM)
 b('rear spear pommel',7.89,23.3,1.84,1.72,1.52,PLATE,45)
 b('strand blade spine',8.48,4.69,.80,1.36,9.18,LIGHT,glow=True)
 b('fine strand point',8.49,.31,.50,.61,2.05,LIGHT,glow=True)
 for i,t in enumerate((2.0,3.35,4.70,6.05,7.40)):
  width=1.10+i*.36
  b('faceted strand blade',8.46,t,width,.81+i*.25,1.84,LIGHT,glow=True)
  for side in (-1,1):
   w.box('fluted blade edge',side*(width*.5-.04),8.51,t,.35,.75+i*.15,1.95,MARK,22.5*side,'y')
 for t in (7.75,8.8,9.85):
  b('carved black root',8.16,t,2.35,2.20,.66,PLATE,-22.5,paint=True)
 b('crystal haft socket',8.01,9.48,1.82,1.74,3.91,STEEL,paint=True)
 w.charge(7.2,8.22,2.15,1.09,2.92);w.contact['grip']=(0,7.88,16);w.contact['support']=(0,7.88,10.66)
 w.cell(8.43,7.57,1.22,1.32,1.7,PLATE)
 # A displaced rear sight keeps the long glaive usable as an aimed player weapon.
# Player weapons. In these recipes secondary masses, cut-outs and the position of the
# operating group are chosen from the reference image rather than a common receiver.
def outbreak_perfected(w):
 b=w.body
 b('SIVA containment spine',8.67,9.3,1.88,1.55,15.4,STEEL,paint=True)
 b('black nanite muzzle cap',8.56,2.0,2.72,2.66,2.45,PLATE,45)
 for i,t in enumerate((4.35,6.7,9.05,11.4)):
  w.twin('triangular SIVA rib',1.39,8.31,t,.46,2.17,.84,PLATE,45)
  w.twin('red rib edge',1.65,8.55,t+.21,.19,1.34,.36,TRIM,45)
 b('black receiver keel',7.26,14.2,2.73,2.63,5.22,PLATE,22.5)
 b('SIVA dorsal fairing',9.44,15.2,2.62,1.97,5.15,STEEL,-22.5,paint=True)
 w.twin('red cell clamp',1.39,8.53,16.23,.31,2.44,1.13,TRIM,-22.5)
 b('containment shoulder',8.12,20.25,2.46,2.72,6.21,PLATE,paint=True)
 b('angled rear plate',7.85,22.74,2.60,2.85,.87,RUBBER,-22.5)
 w.twin('nanite vein',1.31,8.56,20.4,.15,.59,3.54,LIGHT,22.5,glow=True)
 w.handle(13.75);w.fore(7.77,7.47);w.carrier(14.33,9.2,1.44);w.cell(18.21,5.89,2.37,2.46,3.38,TRIM,-22.5)
def graviton_lance(w):
 b=w.body
 b('violet upper shroud',9.13,10.8,2.55,2.26,19.63,PLATE,paint=True)
 b('copper underbarrel',7.35,7.5,2.31,1.12,13.47,TRIM,paint=True)
 b('linear focus rail',7.87,14.0,1.70,.55,13.2,STEEL)
 b('shroud endcap',9.17,1.20,2.76,2.59,.84,STEEL)
 b('open pulse aperture',9.13,.73,1.59,1.48,.16,DARK)
 b('rear bearing',9.11,21.54,2.53,2.63,3.37,TRIM)
 b('rear absorber',8.66,23,2.66,2.72,1.0,RUBBER)
 b('swept trigger guard',5.75,16.93,.91,.61,4.12,STEEL,-22.5)
 w.handle(18.2);w.fore(9.72,6.98,TRIM);w.charge(7.8,8.05,1.69,.67,9.8);w.cell(20.4,6.9,1.86,2.88,2.07)
def riskrunner(w):
 b=w.body
 w.twin('forward prong',1.02,8.34,4.22,.48,.69,7.86,STEEL)
 w.twin('prong point',1.02,8.18,1.26,.45,.45,3.25,EDGE,-22.5)
 b('emerald forward charge housing',8.41,10.26,2.76,2.08,10.44,TRIM,paint=True)
 b('ceramic bolt cover',8.53,17.85,2.65,2.29,8.8,PLATE,paint=True)
 b('forward ceramic sleeve',9.04,6.03,2.01,1.28,2.2,PLATE)
 b('dark heat sink',9.29,9.75,1.71,1.65,3.87,DARK)
 for t in (8.2,9.1,10.0,10.9):b('sink cooling rib',9.39,t,1.88,1.62,.37,STEEL)
 for i in range(4):b('exposed resistor',9.93,16.8+i*1.15,1.38,1.25,.55,TRIM,-22.5)
 b('wire stock upper',8.88,21.27,2.0,.93,4.2,PLATE)
 b('wire stock heel',7.96,23.0,2.05,2.68,.68,PLATE)
 b('ceramic guard base',4.18,18.8,.98,.78,4.8,PLATE,-22.5)
 w.handle(20.1);w.fore(8.2,7.10);w.carrier(16.75,8.84);w.cell(10.73,5.48,2.32,4.43,3.08,TRIM,-22.5)
def dead_mans_tale(w):
 b=w.body
 w.barrel(5.78,11.45,1.16,1.16,8.77)
 b('magazine tube',7.82,6.3,.97,.98,10.87,STEEL)
 w.rings('brass barrel band',(3.68,10.88),8.27,1.74,2.07,.43,TRIM)
 b('walnut forestock',7.78,10.18,2.09,1.53,6.91,PLATE,paint=True)
 b('case hardened receiver',8.5,14.8,2.27,1.96,3.8,STEEL,paint=True)
 b('drop wrist',7.11,17.73,1.80,1.69,4.71,PLATE,22.5)
 b('cheek plate',7.74,20.9,2.08,2.33,5.23,PLATE,paint=True)
 b('swept brass butt plate',7.69,23.26,2.17,2.64,.63,TRIM,-22.5)
 b('lever return',6.28,14.84,.57,.65,3.21,TRIM,g='action')
 b('lever loop heel',6.85,16.38,.58,1.94,.64,TRIM,-22.5,g='action')
 b('lever loop toe',6.94,13.36,.58,1.65,.64,TRIM,22.5,g='action')
 w.contact['grip']=(0,7.0,17.85);w.fore(9.5,7.19,PLATE);w.gate()
def forerunner(w):
 b=w.body
 b('black pulse frame',7.59,10.83,2.07,2.1,15.6,STEEL,paint=True)
 b('white squared slide',9.43,8.30,2.83,2.44,13.22,PLATE,g='action',paint=True)
 b('stepped slide heel',9.05,16.2,1.71,1.75,3.56,PLATE,g='action')
 b('vented muzzle insert',9.31,1.50,2.22,1.76,.25,DARK,g='action')
 b('underslung module',6.49,5.61,1.89,1.73,6.06,STEEL)
 b('underslung lens',6.46,2.50,.90,.91,.18,DARK)
 b('front knuckle guard curve',4.96,9.36,.78,.80,5.2,PLATE,45)
 b('guard bridge',3.22,13.44,.82,.85,5.98,PLATE)
 b('guard heel',3.79,16.76,.81,.91,2.22,PLATE,-22.5)
 w.handle(16.05);w.contact['support']=(0,5.25,15.45);w.cell(16.7,3.58,1.65,.81,2.03,STEEL)
 w.twin('blue status lamp',1.09,7.67,10.5,.14,.36,.43,LIGHT,glow=True)
def arbalest(w):
 b=w.body
 w.twin('magnetic accelerator rail',.71,8.77,7.7,.59,1.01,14.12,STEEL)
 for t in (2.4,3.8,5.2,6.6,8.0,9.4,10.8):w.twin('coil contact',.94,8.87,t,.34,1.19,.56,TRIM)
 b('linear accelerator frame',8.71,15.5,2.43,2.01,8.22,PLATE,paint=True)
 b('padded cheek',9.39,20.3,1.92,1.01,4.12,RUBBER)
 b('slender stock spine',8.64,21.1,1.15,.68,5,STEEL)
 b('wire heel',7.67,23.14,1.32,2.21,.55,STEEL,-22.5)
 b('underbarrel truss',6.05,8.63,.62,.57,9.44,STEEL,-22.5)
 b('front truss bracket',7.73,4.58,.85,1.72,.59,STEEL)
 b('wrapped chamber',8.07,16.2,2.68,1.88,2.95,EDGE,paint=True)
 w.charge(8.27,8.72,.82,.61,10.14);w.handle(17.42);w.fore(11.83,6.54);w.cell(13.18,5.67,2.13,3.54,3.09)
def hammerhead(w):
 b=w.body
 b('long Black Armory heat jacket',9.08,8.84,3.13,2.25,14.64,PLATE,paint=True)
 w.twin('forward cooling rail',1.19,7.56,6.8,.73,.71,11.77,TRIM)
 b('projecting top barrel',9.08,2.21,1.42,1.15,4.36,STEEL)
 b('ported nose block',9.09,.91,2.05,1.82,1.02,STEEL)
 b('front bore',9.09,.34,.88,.86,.21,DARK)
 b('belt housing',8.4,16.74,3.25,2.96,6.60,PLATE,paint=True)
 for t in (14.7,15.8,16.9,18):w.twin('red feeder rib',1.65,8.74,t,.25,1.82,.54,TRIM,-22.5)
 w.rifle_stock(20.62,8.64,True,PLATE)
 w.handle(17.84);w.fore(9.3,7.08);w.carrier(15.8,9.54,1.76)
 w.cell(14.75,5.73,4.3,2.52,3.24,STEEL)
 b('octagonal drum',5.74,14.76,4.29,2.55,3.22,STEEL,45,axis='x',g='load')
def midnight_coup(w):
 b=w.body
 b('underbarrel charcoal spine',7.8,9.64,2.16,1.31,15.81,STEEL,paint=True)
 b('ivory cowl',9.04,8.74,2.52,2.23,13.88,PLATE,paint=True)
 b('inclined snout',8.80,2.20,2.29,1.47,3.31,PLATE,-22.5)
 b('front sight band',8.8,5.17,2.69,2.55,.71,TRIM)
 b('gilded shark fin',10.18,10.77,.72,.91,5.04,TRIM,-22.5)
 w.twin('golden inset',1.31,9.01,10.48,.15,.52,5.83,TRIM,-22.5)
 b('exposed cylinder',8.55,16.03,2.8,2.34,4.34,TRIM,g='load',paint=True)
 b('faceted cylinder',8.55,16.03,2.30,2.77,4.28,TRIM,g='load')
 w.twin('purple chamber cap',1.44,8.58,14.37,.17,1.47,.83,LIGHT,g='load')
 b('hammer',9.77,19.87,.78,.78,1.55,STEEL,-22.5,g='action')
 b('frame heel',8.76,19.13,2.12,2.27,3.75,PLATE)
 w.handle(19.02);w.contact['support']=(0,5.24,18.1);w.contact['loader']=(0,8.54,16.03)
def the_recluse(w):
 b=w.body
 w.barrel(4.6,8.7,1.59,1.55,8.64,STEEL)
 for t in (1.78,2.78,3.78):w.twin('suppressor slot',.81,8.98,t,.12,.36,.55,DARK)
 b('Veist receiver',8.5,13.8,2.45,2.31,11.83,PLATE,paint=True)
 b('low hood',10.04,12.41,1.95,.74,4.42,PLATE,-22.5)
 b('scarlet cheek comb',9.26,19.89,2.45,.97,6.53,TRIM,paint=True)
 b('angular stock web',6.92,19.54,1.71,.98,6.81,PLATE,-22.5)
 b('stock heel',7.94,22.82,2.29,3.22,.83,TRIM)
 w.twin('crimson arachnid panel',1.28,8.25,17.48,.16,1.89,3.19,TRIM,paint=True)
 w.handle(17.7);w.fore(9.80,7.31);w.carrier(14.63,9.27);w.cell(11.79,5.42,1.95,4.08,2.35,RUBBER,-22.5)
def hung_jury_sr4(w):
 b=w.body
 b('Omolon rectangular jacket',9.07,7.22,3.05,2.63,12.82,PLATE,paint=True)
 b('black receiver underside',7.50,9.57,2.56,1.24,13.44,STEEL)
 b('twin aperture muzzle',8.86,.83,3.13,2.94,.57,PLATE)
 for y in (8.23,9.53):b('bore insert',y,.47,1.12,.82,.17,DARK)
 b('upper spine',9.68,17.86,2.35,1.55,9.48,PLATE)
 b('ivory loop heel',7.97,22.20,2.65,3.16,1.30,PLATE,-22.5)
 b('recessed loop grip',6.23,19.24,1.38,.81,4.69,PLATE,-22.5)
 b('black liquid action',8.63,14.98,2.58,2.32,4.29,DARK)
 for x in (-1.28,1.28):w.box('amethyst reservoir',x,8.10,14.8,.87,1.75,1.79,LIGHT,45,glow=True)
 w.handle(17.48);w.fore(8.20,7.19);w.carrier(14.9,9.35,1.34);w.cell(12.76,5.86,1.77,3.43,2.35,STEEL)
def bxr_55_battler(w):
 b=w.body
 w.barrel(3.29,6.23,1.61,1.56,8.58,STEEL)
 b('ivory forward receiver',8.50,10.54,2.89,2.45,9.38,TRIM,paint=True)
 b('green bullpup receiver',8.60,18.64,2.70,2.50,7.94,PLATE,paint=True)
 b('carry handle base',10.05,6.8,1.57,.82,1.36,TRIM)
 b('carry handle front slope',10.38,7.2,1.45,1.15,.62,TRIM,-22.5)
 b('carry handle top',10.82,10.58,1.66,.39,6.53,TRIM)
 b('carry handle rear footing',10.21,14.21,1.55,1.29,.74,TRIM)
 b('front block lug',7.07,6.51,1.47,1.75,1.37,TRIM)
 b('service butt',8.42,22.27,2.55,2.79,1.96,PLATE)
 b('shoulder rubber',8.38,23.13,2.65,2.92,.64,RUBBER)
 w.handle(13.86);w.fore(9.67,7.16);w.carrier(17.87,9.18,1.40);w.cell(20.29,5.76,2.08,3.74,2.75,STEEL)
def paint(w):
 """The material atlas carries wear at game-pixel scale. The second atlas keeps each
 long panel's markings intact from one end to the other; flipping the opposite face
 preserves the reading direction of asymmetric lines."""
 rng=random.Random(w.key)
 material=Image.new('RGB',(64,64)); detail=Image.new('RGB',(128,128));pen=ImageDraw.Draw(detail)
 name=w.key
 for k,c in enumerate(w.colors):
  for x in range(16):
   for y in range(32):
    level=rng.choice((-3,-2,-1,0,0,0,0,1,2,3))
    if x==0 or y==0:level+=6
    if k==RUBBER and y%5==0:level-=14
    material.putpixel((16*(k%4)+x,32*(k//4)+y),tint(c,level))
  oy=k*16;pen.rectangle((0,oy,127,oy+15),fill=c)
  def line(points,col,width=1):pen.line([(x,oy+y) for x,y in points],fill=col,width=width)
  def rect(x,y,r,h,col):pen.rectangle((x,oy+y,x+r,oy+y+h),fill=col)
  line([(1,2),(27,2),(31,4),(99,4),(106,2),(124,2)],tint(c,16))
  line([(2,13),(78,13),(88,11),(123,11)],tint(c,-17))
  ink=tint(c,-36); hi=tint(c,36); accent=w.colors[LIGHT]
  if name=='wire_rifle':
   for x in (11,25,39,89,103):line([(x,12),(x+6,4),(x+8,5),(x+4,11)],hi,2)
  elif name=='vex_slap_rifle':
   line([(3,9),(24,9),(24,6),(44,6),(44,11),(75,11),(75,8),(111,8)],hi)
   for x in (16,53,94):rect(x,5,4,3,ink)
  elif name=='hive_boomer':
   for x in range(4,120,15):
    line([(x,14),(x+5,10),(x+6,5),(x+11,1)],hi,2);line([(x+4,10),(x+10,8),(x+12,4)],ink)
  elif name=='cabal_slug_rifle':
   for x in (10,19,28):line([(x,11),(x+5,4),(x+8,4),(x+3,11)],ink)
   rect(83,5,15,1,hi);rect(83,8,8,1,hi)
  elif name=='taken_shrapnel_launcher':
   for x in range(0,126,2):
    env=(math.sin(x/18)+1)*.5
    for j in range(int(env*4)):
     y=rng.randint(1,14);rect(x,y,rng.randint(0,3),0,tint(accent,-rng.randint(20,100)))
  elif name=='scorn_crossbow':
   for x in range(9,120,24):
    line([(x,14),(x+5,2)],ink,3);line([(x+2,14),(x+7,2)],hi)
  elif name=='subjugator_glaive':
   for x in (6,22,42,68,91):
    line([(x,10),(x+5,4),(x+14,9),(x+26,6)],hi,2)
  elif name=='outbreak_perfected':
   for x in range(1,110,23):
    line([(x,13),(x+15,2),(x+22,13),(x,13)],w.colors[TRIM],2)
    rect(x+12,7,2,1,accent)
  elif name=='graviton_lance':
   for x in range(3,124,4):line([(x,12),(x+5,3)],ink)
  elif name=='riskrunner':
   for x in (8,17,26,36):line([(x,13),(x+5,4),(x+9,4)],w.colors[TRIM],2)
   line([(69,6),(82,6),(85,9),(109,9)],hi)
  elif name=='dead_mans_tale':
   for x in (5,27,52,73,98):line([(x,10),(x+7,5),(x+15,4),(x+18,7),(x+15,9),(x+12,8)],w.colors[TRIM])
  elif name=='forerunner':
   for x in (15,22,29,36):rect(x,5,2,6,ink)
   line([(72,12),(79,6),(107,6)],hi)
  elif name=='arbalest':
   for x in range(5,125,7):line([(x,14),(x+6,4)],tint(c,-23))
  elif name=='hammerhead':
   line([(2,11),(33,11),(39,8),(94,8),(101,4),(124,4)],w.colors[TRIM],2)
   for x in (85,91,97):rect(x,11,2,1,hi)
  elif name=='midnight_coup':
   line([(3,12),(23,12),(41,9),(61,6),(92,6),(109,3),(126,2)],w.colors[TRIM],2)
   for x in range(22,102,12):line([(x,13),(x+4,9),(x+8,9)],hi)
  elif name=='the_recluse':
   x=85
   rect(x-4,5,8,5,w.colors[TRIM])
   for j in range(4):
    line([(x-3,7),(x-11-j*2,3+j*2),(x-20-j,1+j*4)],w.colors[TRIM]);line([(x+3,7),(x+11+j*2,3+j*2),(x+20+j,1+j*4)],w.colors[TRIM])
   for x in (17,29,41):line([(x,13),(x+7,3)],w.colors[TRIM],2)
  elif name=='hung_jury_sr4':
   for x in (19,26,33):pen.ellipse((x,oy+5,x+5,oy+10),outline=ink,width=1)
   for x in (74,81,90):rect(x,7,3,1,tint(c,-24))
  elif name=='bxr_55_battler':
   for x in (10,20,30,79,89,99):rect(x,6,4,4,ink);rect(x,6,3,0,hi)
  for x in (2,123):rect(x,6,2,2,ink);rect(x,6,0,0,hi)
 return material,detail

def fit(w):
 size=.76 if w.kind in ('slab','revolver') else .79
 rear=16.3 if w.optic!='open' else 18.6
 eye=(rear-12)*w.length/24/16*size
 distance={'scope':.26,'reflex':.36,'open':.44}[w.optic]
 def contact(v):
  x,y,t=w.contact[v];return [round(x/16,6),round((y-8)/16,6),round((w.z(t)-8)/16,6)]
 return dict(id=w.key,axis=w.axis,length=w.length,scale=size,ads_z=round(-distance-eye,5),action=w.action,load=w.reload,grip=contact('grip'),support=contact('support'),loader=contact('loader'))

def make_models(w,assets,editor):
 name=w.key; mat,finish=paint(w);texdir=assets/'textures/item';texdir.mkdir(parents=True,exist_ok=True)
 textures={'all':'destiny2mc:item/'+name,'detail':'destiny2mc:item/'+name+'_finish','particle':'destiny2mc:item/'+name}
 paths=[texdir/(name+'.png'),texdir/(name+'_finish.png')];mat.save(paths[0]);finish.save(paths[1])
 def xf(rot,trans,scale):return dict(rotation=rot,translation=trans,scale=[scale]*3)
 g=min(.86,16.8/w.length)
 display={'gui':xf([11,68,-13],[0,-.2,0],g),'ground':xf([0,0,0],[0,3,0],.48),'fixed':xf([0,90,0],[0,0,0],g)}
 for hand in ('left','right'):
  display['thirdperson_'+hand+'hand']=xf([74,0,0],[0,1,1.8],.65)
  display['firstperson_'+hand+'hand']=xf([0,0,0],[0,-1,0],.7)
 model={'credit':'Ageless Arsenals','ambientocclusion':False,'textures':textures,'display':display,'elements':w.pieces}
 write(assets/'models/item'/(name+'.json'),model)
 # Each element occurs exactly once in the grouped projects; keep order for stable reimports.
 group_ids={g:[] for g in ('body','action','load')}; cubes=[]; uid=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'ageless-arsenals/'+name+'/'+s))
 for i,(e,gname) in enumerate(zip(w.pieces,w.group_names)):
  ident=uid(str(i));group_ids[gname].append(ident); r=e.get('rotation',{});angles=[0.,0.,0.]
  if r:angles['xyz'.index(r['axis'])]=r['angle']
  cube={**deepcopy(e),'uuid':ident,'type':'cube','armory_order':i,'box_uv':False,'origin':r.get('origin',[8,8,8]),'rotation':angles,'rescale':False}
  for face in cube['faces'].values():face['texture']=1 if face['texture']=='#detail' else 0
  cubes.append(cube)
 for gn,children in group_ids.items():
  sub={'textures':textures,'ambientocclusion':False,'elements':[e for g,e in zip(w.group_names,w.pieces) if g==gn]};write(assets/'models/armory'/name/(gn+'.json'),sub)
 images=[dict(name=p.name,id=s,uuid=uid(s),source='data:image/png;base64,'+base64.b64encode(p.read_bytes()).decode(),mode='bitmap',uv_width=16,uv_height=16) for p,s in zip(paths,('all','detail'))]
 outliner=[dict(name=g,origin=[8,8,8],uuid=uid(g),children=ids,export=True,visibility=True,isOpen=False) for g,ids in group_ids.items()]
 write(editor/(name+'.bbmodel'),dict(meta={'format_version':'4.10','model_format':'java_block','box_uv':False},name=name,resolution={'width':16,'height':16},elements=cubes,outliner=outliner,textures=images,display=display))

def baseline_rows(source):
 """Convert the previously shipped tables into a portable source manifest once."""
 d=Path(source)/'com/jax/destiny2mc/client';old=(d/'SightFit.java').read_text()
 mounts=re.findall(r'new Mounts\((.*?)\), // (\w+)',(d/'WeaponHandFit.java').read_text())
 rows=[{'id':key,**{k:[float(z.strip().removesuffix('f')) for z in v.split(',')]
         for k,v in zip(('grip','support','loader'),re.findall(r'new Point\(([^)]+)\)',line))}} for line,key in mounts]
 for name,key in [('AXIS','axis'),('LENGTH','length'),('SCALE','scale'),('ADS_Z','ads_z'),('ACTION','action'),('LOAD','load')]:
  values=re.search(r'\b'+name+r'=\{([^}]+)\}',old)[1].split(',')
  for r,val in zip(rows,values,strict=True):
   r[key]=val.split('.')[-1] if key in ('action','load') else float(val.removesuffix('f'))
 assert len(rows)==52
 return rows

def java_tables(dest,rows):
 """Emit the same specification order as WeaponSpec.ALL for native hot-path lookups."""
 out=dest/'com/jax/destiny2mc/client';out.mkdir(parents=True,exist_ok=True)
 head='package com.jax.destiny2mc.client;\n\n// Generated from Ageless Arsenals model geometry in weapon registry order.\n'
 sight=head+'final class SightFit {\n'
 for var,field in [('AXIS','axis'),('LENGTH','length'),('ACTION','action'),('LOAD','load'),('SCALE','scale'),('ADS_Z','ads_z')]:
  rig=var in ('ACTION','LOAD');ty='GunMechanism.'+var.title() if rig else 'float'
  values=[ty+'.'+s[field] if rig else str(s[field])+'f' for s in rows]
  sight+='    static final '+ty+'[] '+var+'={'+','.join(values)+'};\n'
 (out/'SightFit.java').write_text(sight+'}\n')
 hand=head+'''final class WeaponHandFit {
    record Point(float x, float y, float z) {}
    record Mounts(Point grip, Point support, Point loader) {}
    static final Mounts[] BY_WEAPON = {
'''
 for s in rows:
  pts=['new Point('+', '.join(f'{v:.5f}f' for v in s[k])+')' for k in ('grip','support','loader')]
  hand+='        new Mounts('+', '.join(pts)+'), // '+s['id']+'\n'
 (out/'WeaponHandFit.java').write_text(hand+'    };\n}\n')

def verify_ray(w):
 """Transform the longitudinal sight line into each rotated local box's coordinate frame.
 Use slab clipping for the full segment. Check a square of empty space at the aim point."""
 import numpy as np
 def intersects(e,x,y):
  a=np.array(e['from']);b=np.array(e['to']);p=np.array([x,y,-40]);q=np.array([x,y,44])
  r=e.get('rotation')
  if r:
   ang=math.radians(-r['angle']);c=math.cos(ang);s=math.sin(ang);o=np.array(r['origin'])
   rot={'x':[[1,0,0],[0,c,-s],[0,s,c]],'y':[[c,0,s],[0,1,0],[-s,0,c]],'z':[[c,-s,0],[s,c,0],[0,0,1]]}[r['axis']]
   p=np.array(rot)@(p-o)+o;q=np.array(rot)@(q-o)+o
  diff=q-p;lo=0;hi=1
  for j,v in enumerate(diff):
   if abs(v)<1e-9:
    if p[j]<a[j] or p[j]>b[j]:return False
   else:
    t1=(a[j]-p[j])/v;t2=(b[j]-p[j])/v;lo=max(lo,min(t1,t2));hi=min(hi,max(t1,t2))
    if lo>hi:return False
  return True
 hits={e['name'] for dx in (-.21,0,.21) for dy in (-.21,0,.21) for e in w.pieces if intersects(e,8+dx,w.axis+dy)}
 if hits:raise ValueError(f'{w.key} obstructed ADS: {hits}')
if __name__=='__main__':
 cli=argparse.ArgumentParser(description=__doc__)
 cli.add_argument('--baseline-src',type=Path,help='Optionally import the original 52-entry renderer tables from a Java source directory')
 cli.add_argument('--output',type=Path,default=ROOT)
 cli.add_argument('--check-sights',action='store_true',help='Check the complete physical sight channel (requires NumPy).')
 args=cli.parse_args();rows=[];text=[]
 for row in SPECS:
  w=Sculpt(row);w.carve()
  if args.check_sights:verify_ray(w)
  make_models(w,args.output/'stage/assets/destiny2mc',args.output/'blockbench');rows.append(fit(w))
  text.append(' '.join(map(str,(row[0],row[1],row[2],row[3],row[4],row[0]))))
  print(w.key,len(w.pieces),w.action,w.reload)
 write(args.output/'fits.json',rows);(args.output/'catalog-additions.txt').write_text('\n'.join(text)+'\n')
 base=baseline_rows(args.baseline_src) if args.baseline_src else json.loads((ROOT/'baseline-fits.json').read_text())
 java_tables(args.output/'src',base+rows)
