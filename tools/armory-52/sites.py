"""Original single-piece faction architecture. Coordinates are local block positions."""
from pathlib import Path
import json,gzip,struct,math
L=Path(__file__).resolve().parent
SITES=[
 ('cosmodrome_remnant','Fallen Ether Bunker','fallen','fallen_dreg','A ribbed salvage shelter. The curved roof covers the ether tanks and a service passage.',25,14),
 ('vex_conduit','Vex Transit Gate','vex','vex_goblin','A stepped bronze portal on a causeway. Follow the luminous channel beneath the gate to its cache.',25,18),
 ('warmind_relay','Cabal Forward Base','cabal','cabal_legionary','A red-armored command bunker, with a recessed gate, paired firing slits and a rooftop signal mast.',27,16),
 ('exodus_bulkhead','Scorn Ether Still','scorn','scorn_raider','A scavenged refinery built around a caged dark-ether vat. The service deck runs behind the vessel.',23,17),
 ('creation_annex','Dread Resonance Needle','dread','dread_attendant','A split black pyramid conducts amber resonance through a suspended central prism.',23,19),
 ('petition_court','Court of Petitions','taken',None,'An open-roof Awoken chapel. Pointed arches surround the petition basin and leave the Keepers room to appear.',31,18),
 ('heresy_pit','Cradle of Embers','hive',None,'A Hive sacrificial crucible with paired horned buttresses and a fiery crown above the far apse.',31,18),
 ('avarice_vault','Rusted Vault','fallen',None,'An Eliksni salvage hangar. A great circular vault door fills the rear bulkhead and two gantries flank the crystal floor.',31,15),
 ('siren_spire',"Siren’s Watch",'vex',None,'A Warmind communications platform occupied by Vex. Four receiver circuits converge below its antenna gantry.',31,20)]
def st(s):v=s.encode();return struct.pack('>H',len(v))+v
def it(v):return 3,struct.pack('>i',v)
def txt(v):return 8,st(v)
def seq(k,vs):return 9,bytes([k])+struct.pack('>i',len(vs))+b''.join(v[1] for v in vs)
def cmp(d):return 10,b''.join(bytes([t])+st(k)+v for k,(t,v) in d.items())+b'\0'
def iv(p):return seq(3,[it(int(v)) for v in p])
STONE='polished_deepslate'; IRON='smooth_stone'; BONE='end_stone_bricks'; BLACK='polished_blackstone_bricks'; COP='waxed_cut_copper'; OLD='waxed_oxidized_cut_copper'; LIGHT='sea_lantern'; GOLD='ochre_froglight'; RED='red_terracotta'; RUST='waxed_exposed_cut_copper'
class Site:
 def __init__(self,row):
  self.id,self.name,self.faction,self.guard,self.note,self.n,self.h=row;self.c=self.n//2;self.grid={};self.foot=set();self.sparse={};self.cache=None
 def put(self,x,y,z,m):
  if 0<=x<self.n and 0<=z<self.n and 0<=y<self.h:
   self.grid[x,y,z]=m
   if y==0:self.foot.add((x,z))
 def fill(self,x0,y0,z0,x1,y1,z1,m):
  for x in range(x0,x1+1):
   for y in range(y0,y1+1):
    for z in range(z0,z1+1):self.put(x,y,z,m)
 def floor(self,rx,rz,mat,shape='bevel'):
  for x in range(self.n):
   for z in range(self.n):
    dx=abs(x-self.c);dz=abs(z-self.c)
    if dx<=rx and dz<=rz and (dx+dz<=rx+rz-4 if shape=='bevel' else (dx/rx)**2+(dz/rz)**2<=1.04):
     self.put(x,0,z,mat)
 def chest(self,x,z):
  self.put(x,1,z,'chest');self.cache=(x,1,z)
 def tube(self,x,z,r,h,wall,ends):
  for dx in range(-r,r+1):
   for dz in range(-r,r+1):
    v=math.hypot(dx,dz)
    if v<=r+.24:
     for y in range(1,h+1):
      if v>r-1.1 or y in (1,h):self.put(x+dx,y,z+dz,ends if y in (1,h) else wall)
 def ring(self,cx,cy,z,r,depth,rim,edge):
  for x in range(cx-r-1,cx+r+2):
   for y in range(max(1,cy-r-1),cy+r+2):
    d=math.hypot(x-cx,y-cy)
    if r-1.23<=d<=r+.53:
     for k in range(depth):self.put(x,y,z+k,edge if k in (0,depth-1) else rim)
 def landmark(self):
  i=self.id;c=self.c;b=self.put;f=self.fill
  if i=='cosmodrome_remnant':
   self.floor(10,10,STONE)
   for z in range(4,22):
    rib=z in (4,8,13,18,21)
    for x in range(3,22):
     dx=abs(x-c);y=3+round(5.2*math.sqrt(max(0,1-(dx/9)**2)))
     m=RUST if rib else ('blue_terracotta' if (z//4)%2 else 'cyan_terracotta')
     b(x,y,z,m)
     if rib:b(x,y+1,z,COP)
     if dx>=8:f(x,1,z,x,y,z,STONE if not rib else RUST)
     if z in (4,21) and (z==21 or dx>=3 or y<=2):f(x,1,z,x,y,z,m)
     elif z==4 and dx<3:f(x,5,z,x,y,z,IRON)
   for x in (6,18):
    self.tube(x,15,2,4,'cyan_stained_glass',IRON);b(x,2,14,LIGHT)
   for z in (7,12,18):b(c,7,z,'soul_lantern')
   f(10,7,8,14,7,8,'purple_wool');f(10,6,8,10,6,8,'purple_wool')
   self.chest(c,18)
  elif i=='vex_conduit':
   self.floor(9,11,'smooth_quartz','oval')
   for x in range(c-8,c+9):
    for z in range(2,23):
     if (x,z) in self.foot and (abs(x-c) in (2,6) or z in (4,17)):b(x,0,z,OLD)
   self.ring(c,9,12,7,4,RUST,COP);self.ring(c,9,11,6,1,OLD,OLD)
   for s in (-1,1):
    x=c+7*s;f(x-2,1,10,x+2,3,18,'smooth_quartz');f(x-1,4,11,x+1,7,16,RUST)
    f(x,4,10,x,7,10,LIGHT)
    f(c+4*s-1,1,5,c+4*s+1,2,6,COP)
   for x in range(c-5,c+6):b(x,1,13,LIGHT if abs(x-c)<3 else OLD)
   f(c-1,15,11,c+1,16,14,COP);b(c,15,10,LIGHT);self.chest(c,20)
  elif i=='warmind_relay':
   self.floor(12,12,STONE)
   for z in range(4,25):
    half=min(10,6+min(z-4,24-z))
    for x in range(c-half,c+half+1):
     dx=abs(x-c)
     if dx==half or z in (4,24):
      if z==4 and dx<=2:continue
      f(x,1,z,x,5,z,RED)
      b(x,1,z,BLACK);b(x,5,z,'polished_andesite')
     if z>=9 or dx>=4:b(x,6,z,RED if dx>half-2 else STONE)
   for s in (-1,1):
    x=c+6*s;f(x-2,1,4,x+2,5,6,RED);f(x-1,3,3,x+1,3,4,'black_stained_glass')
    f(x-2,5,3,x+2,5,6,IRON);f(x,4,3,x,4,3,'yellow_terracotta')
   f(c-3,5,4,c+3,6,6,BLACK);f(c-1,6,4,c+1,6,4,'yellow_terracotta')
   f(c+4,7,15,c+6,9,17,BLACK);f(c+5,10,16,c+5,13,16,'iron_bars')
   f(c+2,12,16,c+6,12,16,'red_wool');f(c+2,11,16,c+4,11,16,'red_wool')
   f(c-6,7,15,c-2,7,18,'polished_andesite');b(c,5,11,GOLD);self.chest(c,19)
  elif i=='exodus_bulkhead':
   self.floor(9,9,'dark_oak_planks','oval')
   self.tube(c,c+2,4,8,'purple_stained_glass',RUST)
   for y in (2,5,7):
    for dx in range(-4,5):
     for dz in range(-4,5):
      if 3.25<=math.hypot(dx,dz)<=4.25:b(c+dx,y,c+2+dz,'dark_oak_log' if y==2 else COP)
   f(c-1,2,c+1,c+1,7,c+3,'amethyst_block')
   for x in (c-5,c+5):
    f(x,1,4,x,11,4,'stripped_dark_oak_log');f(x,1,17,x,10,17,'stripped_dark_oak_log')
    f(x,8,4,x,8,17,'chain');b(x,9,3,'soul_lantern')
   f(c-5,11,4,c+5,11,4,RUST);f(c-4,10,4,c-1,10,4,'purple_wool')
   f(c-1,9,c+2,c+1,9,c+3,RUST);f(c,10,c+2,c,13,c+2,'copper_grate');self.chest(c-5,10)
  else:
   self.floor(10,10,'blackstone','oval')
   for s in (-1,1):
    for y in range(1,17):
     width=max(1,7-y//2);x=c+s*(2+width//2)
     for z in range(c-width//2-1,c+width//2+2):
      f(x-width//2,y,z,x+(width-1)//2,y,z,BLACK if y%4 else 'polished_blackstone')
     b(c+s*2,y,c-width//2-2,'gilded_blackstone' if y%3 else GOLD)
   for y,w in ((7,0),(8,1),(9,1),(10,0)):
    f(c-w,y,c-w,c+w,y,c+w,'orange_stained_glass' if w else GOLD)
   for j in range(-8,9):b(c,0,c+j,'gilded_blackstone');b(c+j,0,c,'polished_blackstone_bricks')
   self.chest(c,18)
 def dungeon(self):
  c=self.c;b=self.put;f=self.fill;i=self.id
  floor={'petition_court':'polished_blackstone','heresy_pit':'deepslate_tiles','avarice_vault':'polished_andesite','siren_spire':'smooth_stone'}[i]
  self.floor(15,15,floor)
  if i=='petition_court':
   for z in (4,12,21,27):
    for s in (-1,1):
     x=c+s*13;f(x-1,1,z-1,x+1,8,z+1,'polished_blackstone_bricks')
     for h in range(6):
      xx=x-s*(h//2);f(xx,8+h,z-1,xx,8+h,z+1,'quartz_bricks')
     b(x-s,6,z-2,'pearlescent_froglight')
   for z in (5,26):
    for s in (-1,1):
     for h in range(8):b(c+s*(7-h),7+h,z,'smooth_quartz')
     f(c+s*7,1,z,c+s*7,7,z,'quartz_bricks')
   for x in range(3,28):
    for z in range(3,28):
     if abs(math.hypot(x-c,z-c)-8.0)<.47:b(x,0,z,'calcite')
   for x in (c-1,c+1):b(x,0,8,'amethyst_block')
  elif i=='heresy_pit':
   for s in (-1,1):
    for z in (5,15,25):
     for y in range(1,12):
      x=c+s*(13-y//4);w=max(0,2-y//4)
      f(x-w,y,z-w,x+w,y,z+w,'basalt' if y<7 else BONE)
      if y in (2,3,4):b(x-s*(w+1),y,z,'shroomlight')
   for x in range(4,27):
    dx=abs(x-c)
    for y in range(1,max(2,7-dx//3)):b(x,y,29,'red_nether_bricks')
    if dx in (0,3,6):
     f(x,4,29,x,9+(6-dx)//2,29,'bone_block');b(x,10+(6-dx)//2,29,'shroomlight')
   for x in range(2,29):
    for z in range(2,29):
     if abs(math.hypot(x-c,z-c)-9)<.52:b(x,0,z,'red_nether_bricks')
   for x,z in ((15,25),(24,10),(6,10)):b(x,0,z,'verdant_froglight')
  elif i=='avarice_vault':
   for s in (-1,1):
    x=c+s*13
    for z in range(5,28):
     f(x-1,1,z,x+1,2,z,RUST);b(x,6,z,IRON)
     if z%5==0:f(x,2,z,x,8,z,STONE);b(x-s,6,z,GOLD)
   f(5,1,29,25,8,29,STONE);self.ring(c,6,28,6,2,STONE,RUST)
   for x in range(c-5,c+6):
    for y in range(1,12):
     if math.hypot(x-c,y-6)<5:b(x,y,28,'dark_oak_planks' if x!=c else 'waxed_copper_block')
   for x in (4,26):
    f(x-1,1,7,x+1,3,9,'waxed_cut_copper')
   b(c,1,8,'amethyst_block');b(c,2,8,'yellow_stained_glass');b(c,3,8,GOLD)
  else:
   for s in (-1,1):
    for z in (4,26):
     x=c+s*11
     f(x-1,1,z-1,x+1,8,z+1,RUST);f(x,8,z,x,13,z,STONE)
     f(x-2,6,z-1,x+2,6,z+1,IRON);b(x,9,z-1,LIGHT)
   f(4,12,26,26,13,26,STONE);f(6,12,25,24,12,25,COP)
   for dx,height in ((-3,3),(0,5),(3,3)):
    f(c+dx,14,26,c+dx,13+height,26,'iron_bars')
   for x,z in ((7,7),(23,7),(7,23),(23,23)):
    for xx in range(min(x,c),max(x,c)+1):b(xx,0,z,OLD)
    for zz in range(min(z,c),max(z,c)+1):b(c,0,zz,OLD)
    b(x,0,z,LIGHT)
  if i!='avarice_vault':b(c,0,8,{'heresy_pit':'shroomlight','siren_spire':LIGHT,'petition_court':'pearlescent_froglight'}[i])
  b(c,1,3,'destiny2mc:'+i+'_altar');self.chest(c,27)
 def write(self):
  from collections import defaultdict
  # Only carve the occupied floor plan, leaving hillside corners and the terrain beyond the site intact.
  grid=dict(self.grid)
  for x,z in self.foot:
   for y in range(1,(max(7,self.h-3) if not self.guard else 7)+1):grid.setdefault((x,y,z),'air')
  mats=[];mapping={};blocks=[]
  for pos,m in sorted(grid.items(),key=lambda p:(p[0][1],p[0][0],p[0][2])):
   props={'facing':'south'} if m.endswith('_altar') else {'facing':'north','type':'single','waterlogged':'false'} if m=='chest' else {}
   if m not in mapping:
    name,_,attributes=m.partition('[')
    if attributes:props.update(pair.split('=') for pair in attributes.rstrip(']').split(','))
    values={'Name':txt(name if ':' in name else 'minecraft:'+name)}
    if props: values['Properties']=cmp({k:txt(v) for k,v in props.items()})
    mapping[m]=len(mats);mats.append(cmp(values))
   data={'pos':iv(pos),'state':it(mapping[m])}
   if m=='chest':data['nbt']=cmp({'id':txt('minecraft:chest'),'LootTable':txt('destiny2mc:chests/'+('frontier_cache' if self.guard else 'dungeon_cache'))})
   blocks.append(cmp(data))
  nbt=cmp({'DataVersion':it(3955),'size':iv((self.n,self.h,self.n)),'palette':seq(10,mats),'blocks':seq(10,blocks),'entities':seq(10,[])})
  dest=L/'stage/data/destiny2mc/structure'/('frontier' if self.guard else 'dungeons')/(self.id+'.nbt')
  dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(gzip.compress(b'\x0a'+st('')+nbt[1],mtime=0))
# Directional states rotate with the template during placement.
def facing(block, direction):
 return block+'[facing='+direction+']'
def slab(mat):
 return mat+'[type=top]'
def front_disk(s,cx,cy,z,r,rim,field,core):
 for x in range(cx-r,cx+r+1):
  for y in range(cy-r,cy+r+1):
   d=math.hypot(x-cx,y-cy)
   if d<=r+.35:s.put(x,y,z,rim if d>=r-.65 else core if d<=.85 else field)

def ether_bunker(s):
 # The Ketch's curved dark ribs, swept purple awning, and external engine housings.
 c=s.c;f=s.fill;b=s.put
 for x in (5,19):
  s.tube(x,9,2,4,'stripped_warped_hyphae',RUST)
  for z in (6,10):
   f(x-1,3,z,x+1,3,z,OLD);b(x,3,z-1,'cyan_stained_glass')
  f(x-1,6,6,x+1,6,13,'blue_terracotta')
 for dx in range(-6,7):
  # A draped triangular tarp joined to the roof above the entrance.
  width=4-abs(dx)//2
  for j in range(width+1):
   y=7-j//2;b(c+dx,y,4-j,'purple_wool' if dx%5 else 'blue_wool')
  b(c+dx,7,4,RUST)
 for dx in range(-6,7):
  for j in (2,4):
   if j<=4-abs(dx)//2:b(c+dx,8-j//2,4-j,'purple_wool')
 for x in (6,18):
  f(x,1,3,x,7,3,'stripped_dark_oak_log');b(x,5,2,'lantern')
 for sgn in (-1,1):
  x=c+sgn*7;f(x-1,9,13,x+1,10,16,STONE)
  for z in range(11,18):
   y=8 if z%3==0 else 9
   f(x-1,y,z,x+1,y,z,'blue_terracotta')
  f(x,8,11,x,9,16,'oxidized_copper')
 for z in range(5,21):
  for x in (c-2,c+2):b(x,0,z,'exposed_cut_copper')
 f(c-3,2,20,c+3,2,20,'polished_deepslate');b(c,3,20,'sea_lantern')
 for x in (c-4,c+4):
  f(x-1,1,5,x+1,2,5,'gray_concrete');b(x,2,4,'waxed_exposed_copper_bulb[lit=true,powered=false]')

def transit_gate(s):
 c=s.c;f=s.fill;b=s.put
 # A broken aqueduct frames the time gate at the Endless Steps.
 for sign in (-1,1):
  x=c+sign*8;z=15
  f(x-2,1,z-1,x+2,5,z+4,'quartz_bricks')
  f(x-1,5,z,x+1,10,z+3,'smooth_quartz')
  f(x,7,z-1,x,9,z-1,OLD)
  for y in range(10,14):
   off=sign*(y-10)//2
   f(x-off-1,y,z,x-off+1,y,z+2,'weathered_cut_copper' if y%2 else 'quartz_bricks')
  f(x-2,13,z,x+2,14,z+3,'waxed_weathered_copper')
  f(x-1,14,z+1,x+1,15,z+2,'moss_block')
  for z in (3,7,18):
   xx=c+sign*(4 if z==3 else 6);f(xx-1,1,z-1,xx+1,2,z+1,'smooth_quartz')
   b(xx,2,z-1,'ochre_froglight');b(xx,3,z,'waxed_weathered_cut_copper')
 for x in range(c-5,c+6):
  for y in range(4,15):
   q=math.hypot(x-c,y-9)
   if 4.65<=q<=5.42:b(x,y,12,'sea_lantern' if (x+y)%4==0 else 'waxed_oxidized_copper')
 # A transparent gate membrane sits behind a walk-through central opening.
 for dx in range(-5,6):
  for y in range(5,14):
   if math.hypot(dx,y-9)<=5.1 and (abs(dx)>=3 or y>=11):
    b(c+dx,y,14,'light_blue_stained_glass' if (abs(dx)+y)%4 else 'cyan_stained_glass')

 # Make the inner channel a walk-through arcade at ground level.
 f(c-1,1,10,c+1,3,16,'air')
 f(c-1,0,11,c+1,0,15,LIGHT)
def cabal_base(s):
 c=s.c;f=s.fill;b=s.put
 # Red angled sponsons, a sunken gate and a roof command cabin.
 for sign in (-1,1):
  x=c+sign*8
  for z in (9,14,19):
   f(x-2,1,z-1,x+2,3,z+2,'red_terracotta')
   f(x-2,4,z-1,x+2,4,z+1,'polished_blackstone')
   f(x-1,5,z,x+1,6,z+1,'red_concrete')
   f(x+sign*2,2,z,x+sign*2,2,z+1,'yellow_terracotta')
  f(x-2,1,7,x+2,2,8,BLACK)
  for z in range(7,21):
   b(c+sign*5,0,z,'polished_basalt' if z%3 else 'yellow_terracotta')
  f(c+sign*4,2,7,c+sign*4,5,9,'polished_andesite')
  f(c+sign*4,1,13,c+sign*4,3,15,'gray_concrete')
  b(c+sign*4,4,14,'waxed_copper_bulb[lit=true,powered=false]')
 for z in range(9,20):
  half=5 if 11<=z<=17 else 3
  f(c-half,7,z,c+half,8,z,'polished_blackstone')
  f(c-half+1,9,z,c+half-1,9,z,'red_concrete')
 f(c-2,8,9,c+2,8,9,'black_stained_glass')
 for x in (-3,3):b(c+x,8,10,'ochre_froglight')
 f(c-1,10,14,c+1,10,17,STONE);f(c,11,15,c,13,15,'iron_bars')
 for x in (c-4,c+4):
  f(x,1,21,x+1,2,22,'gray_concrete');b(x,3,21,'yellow_terracotta')
 f(c-2,1,23,c+2,3,23,STONE);f(c-1,2,22,c+1,2,22,'redstone_lamp[lit=true]')
 for y in (1,2,3):f(c-3,y,4,c-3,y,6,'polished_andesite');f(c+3,y,4,c+3,y,6,'polished_andesite')

def ether_still(s):
 c=s.c;f=s.fill;b=s.put
 # The Ether Harvester's captured Servitor body and luminous circular eye.
 front_disk(s,c,5,9,4,'polished_blackstone','polished_deepslate','light_blue_stained_glass')
 front_disk(s,c,5,8,2,'waxed_exposed_cut_copper','blue_stained_glass','sea_lantern')
 for side in (-1,1):
  x=c+side*6
  f(x,1,6,x,10,7,'stripped_dark_oak_log')
  for y in (3,5,7,9):f(min(x,x-side*2),y,6,max(x,x-side*2),y,6,'cut_copper')
  f(x,1,13,x,4,15,'dark_oak_planks')
  for z in range(7,16):
   b(x+side,4,z,slab('dark_oak_slab'))
   if z in (7,11,15):f(x+side,1,z,x+side,4,z,'dark_oak_fence')
  f(min(x,c+side*3),7,13,max(x,c+side*3),7,13,'waxed_copper_block')
  b(x,8,13,'soul_lantern');b(x,4,7,'soul_lantern')
 for x in range(c-5,c+6):
  y=10+(abs(x-c)//4);b(x,y,17,'chain[axis=x]')
  b(x,9,17,'purple_wool' if x%4 else 'brown_wool')
 f(c-2,9,14,c+2,9,17,'dark_oak_planks')
 f(c,10,14,c,10,17,'waxed_copper_grate')

def dark_needle(s):
 c=s.c;f=s.fill;b=s.put
 for side in (-1,1):
  for z in range(4,19):
   taper=abs(z-c)//2
   x=c+side*(8-taper)
   f(x,1,z,x,2+(4-taper),z,'polished_blackstone')
   b(x,2+(4-taper),z,'chiseled_polished_blackstone')
   if z in (7,11,15): b(x,1,z,'gilded_blackstone')
  # Recess a tall glowing seam into the face of each blade.
  for y in range(3,15):
   w=max(1,7-y//2);z=c-w//2-1;x=c+side*(2+w//2)
   b(x,y,z,'orange_stained_glass');b(x,y,z+1,'shroomlight')
  f(c+side*2,8,c,c+side*2,8,c+1,'black_stained_glass')
  for z in (4,6,8,15,17):
   f(min(c+side*2,c+side*6),0,z,max(c+side*2,c+side*6),0,z,'gilded_blackstone')
  for z in (4,18):
   x=c+side*4;f(x,1,z,x,3,z,'polished_blackstone');b(x,4,z,'chiseled_polished_blackstone')
 # A gold socket physically supports the central suspended-looking prism.
 f(c,1,c,c,6,c,'brown_stained_glass');f(c-1,1,c-1,c+1,2,c+1,'chiseled_polished_blackstone')
 for x in range(c-4,c+5):
  for z in range(c-4,c+5):
   d=abs(x-c)+abs(z-c)
   if d in (3,6):b(x,0,z,'waxed_copper_block' if d==3 else 'gilded_blackstone')

 # Set the blade's seam two blocks into the shell and open a path around the rear.
 for side in (-1,1):
  for y in range(11,17):
   f(min(c+side*2,c+side*3),y,c-1,max(c+side*2,c+side*3),y,c,'polished_blackstone')
  for z in range(5,18):
   for offset in (8,9,10):b(c+side*offset,0,z,'polished_blackstone')
 for x in range(c-1,c+2):
  for z in range(2,21):
   if abs(z-c)>=3:b(x,0,z,'polished_blackstone')
def awoken_court(s):
 c=s.c;f=s.fill;b=s.put
 # Two arcaded aisles surround a bridge-like processional floor.
 for side in (-1,1):
  x=c+side*13
  for z in range(4,28):
   b(x,7,z,'polished_blackstone_bricks')
   b(x,8,z,'smooth_quartz')
   for z0 in (8,17,25):
    dz=abs(z-z0)
    if dz<=3:
     top=5+(3-dz)//2
     f(x,top,z,x,7,z,'polished_blackstone')
   b(x-side,1,z,'chiseled_polished_blackstone')
  for z in (4,12,21,27):
   f(x,9,z,x,14,z,'quartz_pillar');b(x,15,z,'amethyst_block')
   f(x-side,1,z,x-side,4,z,'polished_blackstone_bricks')
   f(x-side*2,1,z,x-side*2,2,z,'polished_blackstone_bricks')
   b(x-side,4,z-1,'pearlescent_froglight')
  for z in range(2,29):
   b(c+side*3,0,z,'chiseled_polished_blackstone')
   if z%4==0:b(c+side*4,0,z,'smooth_quartz')
  # A curved stone apse built around an amethyst oculus.
  for off in range(3,13):
   x2=c+side*off;top=13-(off//3)
   f(x2,1,29,x2,top,29,'polished_blackstone_bricks')
   b(x2,top+1,29,'quartz_bricks')
   if off in (6,10):f(x2,2,28,x2,top+1,28,'quartz_pillar')
 for x in range(c-3,c+4):f(x,1,29,x,13,29,'polished_blackstone_bricks')
 front_disk(s,c,9,28,4,'smooth_quartz','purple_stained_glass','pearlescent_froglight')
 for x in range(c-2,c+3):
  for y in range(7,12):
   if abs(x-c)+abs(y-9)<=2:b(x,y,28,'tinted_glass' if abs(x-c)+abs(y-9)>1 else 'amethyst_block')
 b(c,9,27,'end_rod');b(c,9,28,'pearlescent_froglight')
 for z in (4,26):
  f(c-1,14,z,c+1,14,z,'smooth_quartz');b(c,15,z,'chiseled_quartz_block')
 for x in range(4,27):
  for z in range(5,26):
   d=math.hypot(x-c,z-c)
   if 5.45<=d<6.30:b(x,0,z,'smooth_basalt' if (x+z)%4 else 'pearlescent_froglight')
   if d<3.8:b(x,0,z,'crying_obsidian' if abs(x-c)==abs(z-c) else 'polished_blackstone')

 # Bond the diagonally stepped arch voussoirs into a continuous load path.
 for z in (5,26):
  for side in (-1,1):
   for h in range(1,8):b(c+side*(8-h),7+h,z,'smooth_quartz')
 for z in (4,12,21,27):
  for side in (-1,1):
   for h in (2,4):
    x=c+side*(13-(h//2));f(x,7+h,z-1,x,7+h,z+1,'quartz_bricks')
def ember_cradle(s):
 c=s.c;f=s.fill;b=s.put
 # Crimson ceremonial towers flank the bridge into the Necropolis.
 for side in (-1,1):
  for z in (5,15,25):
   x=c+side*12
   for y in range(1,12):
    xx=x-side*(y//5);span=2 if y<=3 else 1
    f(xx-span,y,z-1,xx+span,y,z+1,'polished_basalt' if y<=5 else 'red_nether_bricks')
    if 2<=y<=8:b(xx-side*(span+1),y,z,'ochre_froglight' if y in (3,7) else 'orange_stained_glass')
   cx=x-side*2
   f(cx-2,11,z-2,cx+2,11,z+2,'polished_blackstone_bricks')
   f(cx-1,12,z-1,cx+1,12,z+1,'magma_block')
   for ox,oz in ((-2,-2),(-2,2),(2,-2),(2,2)):
    f(cx+ox,12,z+oz,cx+ox,14,z+oz,'polished_blackstone')
    b(cx+ox,15,z+oz,'polished_blackstone_wall')
   b(cx,13,z,'shroomlight');b(cx,14,z,'red_stained_glass')
  for z in range(5,26):
   x=c+side*13;f(x,1,z,x,2,z,'deepslate_bricks')
   if z not in (5,15,25):b(x,3,z,'nether_brick_fence')
  for z in range(6,26):
   x=c+side*4;b(x,0,z,'red_nether_bricks')
   if z%3==0:b(x-side,0,z,'magma_block')
 for x in range(6,25):
  dx=abs(x-c);top=14-dx//2
  f(x,1,29,x,top,29,'red_nether_bricks')
  b(x,top+1,29,'polished_blackstone')
  if dx in (0,3,6):
   f(x,4,28,x,top-1,28,'orange_stained_glass')
   f(x,4,29,x,top-1,29,'shroomlight')
 for x in range(6,25):
  for z in range(6,25):
   d=math.hypot(x-c,z-c)
   if 6.6<d<7.5:b(x,0,z,'red_nether_bricks')
   if 7.5<=d<8.2:b(x,0,z,'polished_basalt')
   if d<4 and (x==c or abs(x-c)==abs(z-c)):b(x,0,z,'red_glazed_terracotta')
 # Ribbed altar canopy at the rear. Its middle opens above the loot cache.
 for side in (-1,1):
  for h in range(7):
   x=c+side*(8-h);f(x-1,7+h,25,x+1,7+h,26,'basalt')
  f(c+side*8,1,25,c+side*8,7,26,'red_nether_bricks')
 f(c-2,13,25,c+2,13,26,'basalt')

def rusted_vault(s):
 c=s.c;f=s.fill;b=s.put
 # The hanging orange salvage sphere anchors the upper crane. Places reachable
 # from the encounter floor stay clear beneath it.
 for side in (-1,1):
  x=c+side*12
  for z in range(4,29):
   for off in (-1,0,1):
    b(x+off,5,z,slab('waxed_exposed_cut_copper_slab'))
   if z not in (6,7,8,14,15,16):b(x-side,6,z,'iron_bars')
   if z%5==0:f(x,1,z,x,7,z,'polished_deepslate')
   if z>=20:f(x+side,2,z,x+side,4+(z%3==0),z,'waxed_exposed_cut_copper')
  for z in (4,10,21,27):
   f(x-1,6,z,x+1,8,z,'polished_andesite');b(x-side,7,z-1,'ochre_froglight')
  # Rear stairs reach each service gantry.
  for j in range(5):
   z=12-j
   f(x-1,1,z,x+1,j+1,z,'polished_andesite')
   f(x-1,j+1,z,x+1,j+1,z,facing('polished_andesite_stairs','north'))
  for z0 in (11,22):
   xx=c+side*9;f(xx-1,1,z0-1,xx+1,2,z0+1,'cut_copper')
   f(xx-1,2,z0,xx+1,2,z0,'gray_terracotta');b(xx,3,z0,'exposed_copper_grate')
 for x in (3,27):f(x,1,23,x,8,23,'polished_deepslate')
 f(3,8,23,27,9,23,'polished_deepslate')
 for x in range(4,27):b(x,10,23,'waxed_exposed_cut_copper' if x%3 else 'iron_bars')
 # Grasp's round mechanism, suspended from the gantry's underside.
 sx=9;sz=23
 for x in range(sx-3,sx+4):
  for y in range(3,10):
   for z in range(sz-3,sz+4):
    radius=math.sqrt((x-sx)**2+(y-6)**2+(z-sz)**2)
    if radius<=3.35:b(x,y,z,'waxed_exposed_cut_copper' if z==sz or y==5 else 'orange_terracotta')
 front_disk(s,sx,6,sz-3,2,'polished_blackstone','blue_stained_glass','sea_lantern')
 # Recess the bulkhead door inside a red and gray industrial casing.
 for x in range(3,28):
  xx=abs(x-c)
  for y in range(1,13):
   if xx>=7 or y>=10:b(x,y,29,'gray_concrete' if (x//3)%2 else 'polished_andesite')
  if xx in (7,10):f(x,1,28,x,10,28,'red_terracotta')
  if xx>=8:b(x,11,28,'ochre_froglight' if xx==10 else 'waxed_exposed_cut_copper')
 for dx in range(-5,6):
  for dy in range(-5,6):
   if 2.7<math.hypot(dx,dy)<3.6:b(c+dx,6+dy,27,'waxed_cut_copper')
 front_disk(s,c,6,27,2,'polished_deepslate','waxed_exposed_cut_copper','ochre_froglight')
 for z in range(4,26):
  for x in (c-3,c+3):b(x,0,z,'polished_deepslate' if z%4 else 'waxed_copper_grate')
 for x in range(6,25):
  if x%2==0:b(x,0,20,'yellow_terracotta')

def siren_array(s):
 c=s.c;f=s.fill;b=s.put
 # A miniature tapered Seraph tower terminates in the broad lateral antenna
 # from the Spire press photographs. Below it the arena stays open.
 for y in range(1,18):
  w=3 if y<4 or 9<=y<=10 else 2 if y<13 else 1
  for dx in range(-w,w+1):
   for dz in range(-w,w+1):
    if abs(dx)+abs(dz)>w+1:continue
    skin='smooth_sandstone' if y<12 else 'polished_andesite'
    b(c+dx,y,27+dz,'polished_deepslate' if y in (4,9,10,14) or dx==0 and abs(dz)==w and y>2 else skin)
  if y in (6,7,8,11,12):b(c,y,27-w,'ochre_froglight')
 # The bottom of the tower is a through-passage to the chest maintenance hatch.
 f(c-1,1,23,c+1,3,29,'air')
 for side in (-1,1):
  x=c+side*11
  for z in (4,26):
   for dx in (-1,1):
    f(x+dx,1,z-1,x+dx,6,z+1,'cut_sandstone')
    b(x+dx,6,z-1,'black_concrete')
   f(x-2,6,z-1,x+2,7,z+1,'smooth_sandstone');f(x-1,8,z-1,x+1,9,z+1,'gray_terracotta')
   b(x,8,z-2,'ochre_froglight')
  for z in range(6,25):
   # maintenance conduits under the outboard handrail
   b(x+side*2,1,z,'waxed_exposed_cut_copper');b(x+side*2,2,z,'polished_deepslate_slab')
  for z in (7,20):
   f(x-1,1,z,x+1,3,z+2,'smooth_sandstone')
   f(x-1,3,z-1,x+1,3,z-1,'black_stained_glass')
   f(x-2,4,z-1,x+2,4,z+2,slab('cut_sandstone_slab'))
   b(x-side,3,z-1,'waxed_copper_bulb[lit=true,powered=false]')
  for j in range(1,9):
   x2=c+side*j;y=16-j//5
   f(x2,y,26,x2,y+1,28,'waxed_cut_copper' if j%3==0 else 'smooth_sandstone')
   if j in (3,6):b(x2,y-1,27,'polished_deepslate')
 f(c,16,27,c,19,27,'iron_bars')
 for z in range(4,26):
  for x in (c-2,c+2):b(x,0,z,'polished_deepslate' if z%4 else 'exposed_copper_grate')
 for x in range(6,25):
  for z in range(6,25):
   d=abs(x-c)+abs(z-c)
   if d in (7,8) and (abs(x-c)>2 and abs(z-c)>2):b(x,0,z,'waxed_cut_copper' if d==8 else 'light_gray_concrete')

DETAILS=(ether_bunker,transit_gate,cabal_base,ether_still,dark_needle,awoken_court,ember_cradle,rusted_vault,siren_array)


def build():
 for r,details in zip(SITES,DETAILS):
  s=Site(r)
  if s.guard:s.landmark()
  else:s.dungeon()
  details(s)
  s.put(*s.cache,'chest')
  s.write()
if __name__=='__main__':build()
