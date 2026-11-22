"""Finish passes made against the references. Keep the optic axis physically open."""
from PIL import ImageDraw
import math
def detail(w):
 from weapons import DARK,PLATE,TRIM,METAL,LIGHT,RUBBER,EDGE,MARK
 b=w.box
 def p(n,y,t,ww,h,d,m,a=0):b(n,0,y,t,ww,h,d,m,a)
 def side(n,x,y,t,width,h,d,m,a=0):
  for s in (-1,1):b(n,s*x,y,t,width,h,d,m,a)
 f=w.feature;k=w.kind;parts=w.parts
 def remove(*names):parts[:]=[e for e in parts if e['name'] not in names]
 # Bring hardware to the top surface of the receiver; fit a longer, tighter ocular for glass sights.
 iron=w.optic=='open';scope=w.optic=='scope'
 w.axis=11.52 if iron else 12.41 if scope else 11.92
 if w.kind=='bow':w.axis=12.35
 if f.startswith('plunder') or f=='roots':w.axis=12.62
 if f=='mytho':w.axis=12.62
 yscale=.50 if iron else .61
 xscale=.75 if iron else (.65 if scope else .76)
 def transform(e,fn):
  for a in ('from','to'):e[a]=[round(v,4) for v in fn(e[a])]
  if 'rotation'in e:e['rotation']['origin']=[round(v,4) for v in fn(e['rotation']['origin'])]
 for e in parts:
  n=e['name']
  if n.startswith(('optic ','rear notch','rear dovetail','notch bridge','front blade','front bead','front saddle')) or n in ('rim bevel','hood','sill','scope roof','scope rail'):
   transform(e,lambda pt:[8+(pt[0]-8)*xscale,w.axis+(pt[1]-14)*yscale,pt[2]])
   if n in ('front blade','front bead'):transform(e,lambda pt:[pt[0],pt[1]-.27,pt[2]])
   if n in ('front saddle','rear dovetail'):
    e['from'][1]=min(e['from'][1],8.9)
   if n=='optic shoe':
    e['from'][1]=min(e['from'][1],9.5)
  if n=='chitin overlapping scute':
   e['rotation']['angle']=-22.5
   transform(e,lambda pt:[pt[0],9.12+(pt[1]-9.12)*.66,pt[2]])
 # Additional modelling comes from the finish, the open spaces, and the cross-section of the visible hardware.
 if scope:
  for s in (-1,1):
   b('ocular chamfer',s*1.39,w.axis-.94,14.55,.45,.47,4.65,TRIM,45,'z')
   b('scope fastener',s*1.60,w.axis-.2,15.6,.15,.45,.48,METAL)
 if k in ('sniper','sheath','rod','worm','scout'):
  b('bolt carrier',1.34,9.19,15.85,.52,.55,3.5,METAL)
  b('bolt knob',1.7,9.44,16.8,.58,.69,.72,TRIM,-22.5)
 if k=='sheath':
  # The white sheath covers well over half the length, with a blade-like split end and a curved crossguard.
  p('white tang',8.89,12.82,2.7,2.80,3.19,PLATE)
  side('chisel cheek',1.17,8.81,.52,.64,2.35,2.89,EDGE,22.5)
  side('hook on guard',1.25,10.59,14.84,.67,1.74,.75,DARK,-22.5)
  side('Armory painted crest',1.435,8.78,8.15,.12,1.68,3.6,MARK)
 if k=='worm':
  p('swollen dark receiver',8.9,15.5,3.43,3.05,6.8,DARK)
  side('charred shell',1.55,9.65,13.33,.48,1.58,6.1,PLATE,22.5)
  for i in range(5):
   t=-.35+i*.87
   for s in (-1,1):b('deep white quill',s*(1.32+.17*(i%3)),8.70+(i%2)*.8,t,.42,.50,3.4-i*.26,LIGHT,(-22.5 if i%2==0 else 22.5))
  p('black flared stock',8.53,21.25,2.73,2.17,4.13,DARK)
 if f=='rapier':
  remove('magazine','magazine floor','stock cheek','lower stock brace')
  p('ceramic stock spine',9.12,19,2.36,1.70,8.89,PLATE)
  p('white foregrip guard',5.52,15.13,.7,.63,6.75,PLATE,-22.5)
  p('broad sword blade',7.25,-1.63,.62,1.5,11,METAL);p('taper of sword',7.44,-7.16,.46,.75,1.23,METAL,-22.5)
 if f=='sun':
  p('hot upper blade',10.45,8.65,2.2,.67,18.6,TRIM);p('heat sink blade',7.15,7.75,2.12,.82,16.65,MARK)
  side('copper pipe',1.28,8.67,9.0,.39,.59,12.5,LIGHT)
 if f in ('western','spade','lamp','thorns','teeth','eclipse_hc','plunder_side','hive_hc'):
  # Distinct grip cheeks, a pin, and a contrasting cylinder face make the frame legible in the hand.
  side('grip panel',.91,5.23,19.05,.35,2.94,1.92,TRIM if f in ('teeth','eclipse_hc') else (PLATE if f!='western' else RUBBER),22.5)
  side('grip pin',1.12,5.55,18.94,.12,.29,.32,MARK)
 if f in ('spade','western','lamp'):
  for s in (-1,1):
   b('cylinder bevel',s*1.45,9.15,14.1,.53,.70,4.91,TRIM,45,'z')
 if f=='teeth':
  side('white frame',1.42,9.6,16,.46,1.17,6.9,TRIM);side('red lower rail',1.21,7.62,9.1,.3,.40,5,LIGHT)
 if f=='thorns':
  p('ragged muzzle tooth',9.1,.68,1.43,1.23,3.46,TRIM,22.5)
  for t in (6.6,9.1,12.5):side('carved void between ribs',1.27,8.97,t,.16,.73,1.22,DARK,22.5)
 if f=='storm':
  remove('heel','lower stock brace','comb')
  p('raised staff crook',9.24,21.4,1.19,.86,4.29,TRIM,22.5);p('rear hook',10.38,23.3,1.27,2.64,.91,TRIM,-22.5)
 if f=='lever':
  remove('heel','comb','lower stock brace')
  p('short metal tang',8.05,18.82,2.12,1.73,4.41,PLATE);side('scrollwork plaque',1.38,8.63,8,.1,1.45,10.7,MARK)
 if f=='forge':
  remove('shoulder rest','grip shoe','guard','finger bow')
  p('flat gauntlet top',10.22,11,6.42,.74,19.22,DARK)
  side('brass gauntlet binding',2.99,8.35,7.1,.39,3.65,.95,TRIM)
  p('rear wrist cuff',7.47,19.12,4.65,2.25,3.27,METAL)
 if f in ('tractor','fox','wolf'):
  if f=='tractor':remove('shoulder rest');p('rear carry arch',9.33,19.9,2.16,.9,7.9,PLATE,-22.5)
  if f=='fox':
   side('painted fox fairing',2.57,8.94,8.9,.21,2.79,13.31,MARK)
   p('plum upper shroud',10.68,13.7,4.43,1.11,15,PLATE)
  if f=='wolf':
   for t in (5.65,7.15,17.5):side('gold stamping',2.42,8.85,t,.32,1.87,1.24,TRIM,45)
 if f in ('mytho','prism','telesto','merciless','crucible','veist','cryo','sleeper','butterfly'):
  if f=='mytho':
   remove('magazine','magazine floor','comb','lower stock brace','heel')
   p('rounded gold tail',9.45,19.55,2.98,1.52,7.75,TRIM,-22.5);p('gold palm heel',5.05,17.2,2.16,.95,4.96,TRIM)
   side('wing plate',1.67,9.85,15.2,.42,1.4,6.1,TRIM)
  if f=='sleeper':
   for t in (2.7,6.15,9.6,13.05):
    side('warmind yoke',1.48,9.32,t,.66,2.52,2.52,TRIM,45)
    side('hollow yoke inlet',1.85,9.32,t,.12,1.70,1.70,DARK,45)
  if f=='prism':
   side('yellow frame',1.53,7.32,16,.59,2.47,6.71,TRIM,-22.5);p('crystal pushrod',8.6,7.7,.99,.95,6.25,METAL)
  if f=='cryo':
   p('cryogenic chamber',8.83,12.25,3.47,3.05,5.52,TRIM)
   for t in (11,12.3,13.6):side('refrigerant lines',1.79,8.78,t,.29,1.79,.53,LIGHT)
  if f in ('telesto','merciless','crucible','veist'):
   remove('heel','comb','lower stock brace')
   p('sloped stock bridge',8.95,19.7,2.05,.99,7.71,PLATE);p('tail return',7.04,22.1,1.59,3.2,.91,TRIM,22.5)
   p('stock guard',5.65,19.8,1.09,.61,5.05,METAL,-22.5)
  if f=='crucible':
   p('red lower frame',5.46,14.96,1.27,.68,6.5,PLATE);side('white inset stripe',1.62,9.3,7.6,.19,.35,4.7,TRIM)
  if f=='butterfly':
   # Asymmetric machined lower riser and silver laminated blade ends.
   side('upper limb cap',1.48,17.8,15,.70,.76,3.22,TRIM,45)
   b('red riser',-1.39,9.1,12.9,1.60,3.05,1.6,LIGHT,-22.5)
 if f.startswith('plunder'):
  for t in (8.5,10.9,13.3):side('engraved brass stud',1.67,9.36,t,.3,.47,.56,MARK,45)
  side('top strap',1.17,10,13.05,.39,1.09,3.14,TRIM,22.5)
 if f.startswith('eclipse'):
  for t in (6,8.4,10.8):side('engraved silver curl',1.61,8.52,t,.19,.53,.98,METAL,22.5)
 if f.startswith('hive') or f=='necro':
  for j,t in enumerate((4.3,8.0,12,15.8)):side('plated flank',1.57,9.06,t,.39,1.38,2.54,PLATE if j%2 else TRIM,-22.5)
  if f=='hive_hc':
   for t in (4.5,7.7,10.9):side('Hive facial ridge',1.47,8.29,t,.3,1.94,.59,LIGHT,-22.5)
  if f=='hive_scout':side('pale receiver',1.58,9.01,12.35,.54,2.17,7.47,TRIM)
  if f=='hive_mg':p('drum axle',6.49,11.51,5.7,1.93,2.7,METAL)
 if f=='roots':
  p('paired barrel bridge',9.55,2.3,2.7,.44,.92,METAL)
  remove('heel','comb','lower stock brace','reinforcing band')
  p('arched root stock',7.9,19,1.53,1.31,6.8,TRIM,-22.5);p('lower ember tube',7.5,6.85,1.22,1.11,13.39,LIGHT)
 if f=='blight':p('front handle',5.8,5.24,1.27,3.6,1.34,DARK)
 if f=='junk':
  remove('reinforcing band')
  bands=((3.3,3.7),(11,4.0))
  for t,ww in bands:p('scrap collar',8.7,t,ww,3.37,.78,TRIM)
 if f=='service':
  for t in (4.2,5.4,6.6,7.8):side('ribbed handguard',1.27,8.06,t,.25,1.5,.42,DARK)
 # Small detachable mechanisms remain actual geometry in all item contexts.
 if k not in ('revolver','claw','slab','bow','twin','lever','breach','launcher','dual') and not any(e['name']=='magazine' for e in parts):
  p('power cassette',6.58,13.71,2.15,1.55,3.51,TRIM)
 if k in ('claw','slab'):
  p('grip cartridge',4.83,19.37,1.43,2.54,1.45,METAL,22.5);p('cartridge floorplate',3.52,19.9,2.0,.46,1.94,TRIM)
 if k in ('launcher','dual'):p('rear loading collar',8.3,19.5,3.96,3.28,2.22,TRIM)
 if k not in ('bow','rotary','sheath','rod','worm','sniper','revolver','lever','pump','claw','slab'):
  side('moving breech latch',1.63,9.35,16.4,.41,.68,1.88,METAL)
 # Give each shell an authored painted face; preserve edge colors on the other four faces.
 from signatures import finish
 finish(w)
 decorate(w)
def decorate(w):
 # Every panel has a fresh set of side-face UVs into a supplementary 64x64 sheet.
 for e in w.parts:
  n=e['name']
  style=('grip' if n in ('grip panel','raked grip','wood stock') else
         'engrave' if n in ('scrollwork plaque','Armory painted crest','painted fox fairing','engraved barrel jacket','Omolon slab','ivory carapace','plated flank','wing plate','cryogenic chamber','white containment shell','brass housing','power pack','warmind yoke','chitin jaw','ceramic blade sheath','wolf eye','receiver core','cylinder body','cast tube','white tang','ceramic stock spine') else None)
  if not style:continue
  for face in ('east','west'):
   uv=e['faces'][face]['uv']; mat=int(min(uv[0],uv[2])//4)+4*int(uv[1]//8)
   # A separate texture per item keeps mod resource-pack overrides intact.
   if style=='engrave' and n=='Armory painted crest':mat=1
   e['faces'][face]={'texture':'#detail','uv':([15.8,mat*2+.09,.2,mat*2+1.9] if face=='east' else [.2,mat*2+.09,15.8,mat*2+1.9])}
def make_paint(w):
 from PIL import Image,ImageDraw
 im=Image.new('RGB',(128,128));pen=ImageDraw.Draw(im);f=w.feature
 for i,c in enumerate(w.colors):
  oy=16*i
  def shade(v):return tuple(max(0,min(255,k+v)) for k in c)
  def line(q,color,width=1):pen.line([(a,b+oy) for a,b in q],fill=color,width=width)
  pen.rectangle((0,oy,127,oy+15),fill=c)
  line([(1,2),(28,2),(33,4),(96,4),(100,2),(124,2)],shade(19));line([(2,13),(89,13),(95,11),(124,11)],shade(-21))
  if i==5:
   for x in range(3,126,7):line([(x,14),(x+8,2)],shade(19),2)
  elif f.startswith('eclipse') or f.startswith('plunder') or f in ('lever','wolf','western'):
   for x in range(7,123,24):
    line([(x,11),(x+6,6),(x+13,6),(x+15,9),(x+12,11),(x+9,9)],shade(-33))
    line([(x+5,11),(x+9,7),(x+13,7)],shade(31))
  elif f.startswith('hive') or f in ('necro','taken','thorns','roots'):
   for x in range(5,113,23):
    line([(x,13),(x+5,10),(x+7,5),(x+15,2)],shade(39),2);line([(x+5,10),(x+14,10),(x+18,7)],w.colors[4])
  elif f=='izanagi':
   ink=w.colors[4];line([(12,10),(55,10),(62,7),(73,8),(67,5),(77,6),(74,3),(83,7),(79,11),(69,12)],ink,2)
   line([(62,8),(55,4),(65,6)],ink,2);line([(82,7),(97,8),(108,7)],ink)
  elif f=='fox':
   ink=w.colors[4];line([(21,10),(30,3),(32,7),(45,6),(48,3),(52,10),(40,14),(32,13),(21,10)],ink,2)
  else:
   for x in (9,14,19):line([(x,10),(x+4,6)],shade(-37),2)
   for x in (90,97,104):line([(x,6),(x+4,6)],shade(35))
  for x in (4,122):
   pen.rectangle((x,oy+6,x+2,oy+8),fill=shade(-36));pen.point((x,oy+6),fill=shade(49))
 return im
