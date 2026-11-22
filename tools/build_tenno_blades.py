"""Author the six Warframe melee pieces as native, editable, stepped blade meshes.

Coordinates use a 16-unit block and a fixed grip at x=8, y=5.5, z=8. Narrow
mesh strips articulate a changing cutting edge without alpha-card geometry.
Run with an optional path to the project's src/main/resources directory.
"""
from copy import deepcopy
from pathlib import Path
import json
import math
import random
import sys
from PIL import Image, ImageColor

resource = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1]/'src/main/resources'
root = resource/'assets/destiny2mc'
models = root/'models/item'
parts = models/'tenno_blades'
tex = root/'textures/item/tenno_blades'
for folder in (models, parts, tex): folder.mkdir(parents=True, exist_ok=True)
# Shadow metal, chassis, plate, edge, trim, energy, leather, light metal.
palettes = {
    'amanata':['272b2a','594738','bcb6a2','f2de93','bc913e','991d32','532a2f','ebd8b2'],
    'hate':['181b22','30303a','525561','a5afae','50515b','941d32','231d24','777982'],
    'harmony':['35393b','606063','d2c9b8','ffeedb','b77779','6fce94','694044','f1d8c7'],
    'heat_sword':['222832','4a4c52','afb4b3','e0ded1','bd6643','f79051','353b3d','7b817f'],
    'dual_heat_swords':['222832','484a50','a4aaa8','dadbd0','aa573e','ff9654','313739','838886'],
    'sun_and_moon':['252d36','364651','a99057','f3d789','bc9955','74a399','34485a','e6cb88'],
}
UV = {}
SIDES = ('north','south','up','down','east','west')
def atlas(name):
    colors = palettes[name]; image = Image.new('RGB',(64,32)); rng=random.Random(name)
    for i,s in enumerate(colors):
        base = ImageColor.getrgb('#'+s); x0=(i%4)*16; y0=(i//4)*16
        UV[i]=[x0/4+.14,y0/2+.18,(x0+16)/4-.14,(y0+16)/2-.18]
        for y in range(16):
            for x in range(16):
                ridge = 7 if y in (0,1) else -3 if y>=14 else 0
                shade = ridge+rng.choice([-5,-3,0,0,2,4])
                image.putpixel((x0+x,y0+y),tuple(min(255,max(0,n+shade)) for n in base))
    image.save(tex/(name+'.png'))
def coords(v): return [round(i,3) for i in v]
def box(out,name,x,y,w,h,t,mat,z=8,rot=None):
    assert w>0 and h>0 and t>0
    faces={side:{'texture':'#metal','uv':UV[mat]} for side in SIDES}
    e={'name':name,'from':coords((x-w/2,y-h/2,z-t/2)), 'to':coords((x+w/2,y+h/2,z+t/2)), 'faces':faces}
    if rot: e['rotation']={'axis':'z','angle':rot,'origin':coords((x,y,z)),'rescale':False}
    out.append(e)
def strip(out,name,rows,plate,edge,depth=.64,edge_left=True):
    # Each row describes left, right cutting bounds at a different height. Bias
    # the near-tip bevel away from the midrib, preserving an identifiable point.
    for a,b in zip(rows,rows[1:]):
        y0,l0,r0=a; y1,l1,r1=b
        count=max(1,int(math.ceil((y1-y0)/.68)))
        step=(y1-y0)/count
        for k in range(count):
            v=(k+.5)/count;l=l0+(l1-l0)*v;r=r0+(r1-r0)*v;w=r-l
            if w<.13:continue
            y=y0+(k+.5)*step; lip=min(.51,w*.32);lip_x=l+lip/2 if edge_left else r-lip/2
            box(out,name+' blade',(l+r)/2,y,w,step+.018,depth,plate)
            box(out,name+' bevel',lip_x,y,lip,step+.02,depth+.06,edge)
def shaft(out,bottom,top,mat,width=.73):
    box(out,'shaft',8,(top+bottom)/2,width,top-bottom,width,mat)
def binding(out,ys,mat,width=1.02):
    for y in ys:box(out,'grip ring',8,y,width,.20,width,mat)
def inset(out,x,y,w,h,mat,z=8.41,rot=None):
    box(out,'face inlay',x,y,w,h,.13,mat,z,rot);box(out,'reverse inlay',x,y,w,h,.13,mat,16-z,rot)
def amanata():
    out=[];shaft(out,-7,18,5,.68);binding(out,[-6.7,-5.8,-4,0,1,3,5.3,6.7,8,12.1,14.3,16.4],4)
    box(out,'capped pommel',8,-6.9,1.05,1.02,1.03,4,rot=45)
    box(out,'naginata throat',8,16.8,1.65,2.6,1.15,1,rot=-22.5)
    strip(out,'naginata',[(16.5,7.35,8.5),(18.1,7.0,9.3),(21,6.9,9.5),(23,7.55,10),(26,9.4,11.2),(28.6,11.4,11.6)],2,3,.66)
    box(out,'long curved handguard',9.1,17.3,1.2,3.75,1.0,4,rot=-45)
    inset(out,8.0,20,.32,2.2,4);inset(out,8.8,22.1,.36,2.2,4,rot=-22.5)
    box(out,'red cord',6.8,15.6,.35,3.45,.45,5,rot=-22.5)
    for x,y in [(6.3,14),(6.6,13.2),(6.7,12.5)]:
        box(out,'cord bauble',x,y,.64,.61,.65,4,rot=45)
    # Koumei's open oval surrounds the blade throat; the central void is visible from either side.
    for x,y,w,h,a in [(8.4,17.35,2.8,.48,0),(6.75,17.95,.5,1.85,-45),(6.08,19.2,.48,1.24,0),(6.72,20.25,.48,1.88,45),(8.23,20.85,2.0,.5,0),(9.53,20.12,.48,1.71,-45),(9.82,18.77,.48,1.55,22.5)]:
        box(out,'gilt throat hoop',x,y,w,h,1.19,4,rot=a)
    box(out,'cast die',9.0,13.1,1.2,1.23,.95,7,rot=22.5)
    inset(out,9.0,13.08,.27,.27,5,8.53)
    return out
def hate():
    out=[];shaft(out,-4.7,17.4,1,1);binding(out,[-4.35,-3.6,-1.5,1.5,2,4.25,4.8,7,11.6,14.4],0,1.19)
    box(out,'reinforced middle',8,10.8,1.33,3.1,1.23,2)
    for x,y in [(7.4,9.9),(8.65,11.2),(8.74,15.0)]:inset(out,x,y,.42,.42,5,8.71)
    box(out,'crown shoulder',8.7,15.75,3,2.4,1.68,1,rot=22.5)
    box(out,'back spur',10.15,16.8,1.1,3.35,.78,2,rot=-45)
    strip(out,'widow hook',[(10,-3.3,-3.06),(12.8,-2.6,-1.6),(14.55,-1.2,.7),(16,0.1,3.2),(17.65,1.7,7.7),(19.1,4.9,9.5),(20.15,7.1,9.3),(20.9,8.95,9.19)],1,7,.85)
    # A taper cut on the inside of the black wing separates a heavy spine and steel edge.
    strip(out,'inner flange',[(15.8,6.1,7.2),(16.4,4.1,7.6),(17.1,3.1,8.25),(17.75,5.5,8.3),(18.25,7,8.25)],2,2,1.10,False)
    inset(out,7.8,16.55,.65,.65,5,8.90,45)
    return out
def harmony():
    out=[];shaft(out,-5.5,17.1,2,.72)
    for y in [-4.9,-4,1,2,5,7.3,11.8,13.2,15.3]:binding(out,[y],4,.98)
    for y,d in [(-4,1),(-2,2),(9.5,2.8),(12.5,2.5)]:
        box(out,'fluted handle',8.1,y,1.06,d,1.05,1,rot=22.5 if y>0 else 0)
    strip(out,'leading silver plume',[(18,4.4,7.4),(19.2,1.9,7.7),(20.3,-.8,6.9),(21.3,-3.2,5.0),(22.2,-4.7,2.1),(23.05,-5.3,-3.1),(23.52,-6,-5.8)],2,3,.72)
    strip(out,'inner petal',[(16,6.6,8.4),(17.6,4.7,9.15),(19.4,2.7,8.6),(20.8,1.9,5.9),(21.3,1.35,2.25)],4,7,.8)
    strip(out,'rear plume',[(16.4,8,9.3),(17.9,8.05,10.1),(19.8,8.45,10.7),(21.2,9.48,10.95),(22.6,10.6,10.9)],2,7,.64,False)
    box(out,'emerald resonator',8.1,17.4,2.5,2.35,1.46,1,rot=45)
    inset(out,8.1,17.4,1.55,1.45,5,8.78,45)
    inset(out,8.05,17.5,.49,.73,3,8.87,45)
    # The second visible green jewel sits inside the split wing at the butt of Jade's scythe.
    box(out,'counterweight rose ring',8,-4.25,1.92,1.92,1.04,4,rot=45)
    inset(out,8,-4.25,1.04,1.04,5,8.57,45)
    strip(out,'pommel wing',[(-6.5,4.8,5),(-5.7,5.2,6.25),(-4.75,6.1,7.9),(-4.0,7.0,7.85)],2,7,.52)
    strip(out,'reverse pommel wing',[(-4.7,8.26,9.4),(-3.8,8.65,9.95),(-2.8,9.2,9.9),(-1.9,9.45,9.65)],2,3,.52)
    box(out,'split ferrule',7.65,14.1,1.0,3.4,1.05,7,rot=-22.5)
    return out
def heat(short=False):
    out=[];f=.69 if short else 1.0
    shaft(out,1,8,6,1.0);binding(out,[1.4,1.9,2.8,3.7,4.6,5.5,6.4,7.4],0,1.17)
    box(out,'pommel',8,.94,1.39,.8,1.36,2)
    box(out,'side knuckle bow',6.7,5,.43,4.0,.65,1,rot=-22.5)
    box(out,'upturned guard',7.0,7.0,1.75,.55,.88,2,rot=22.5)
    box(out,'guard flange',8.3,7.7,2.65,.75,1.23,1)
    rows=[(8,7.28,8.85),(9.9,6.81,9.35),(13.4,6.9,9.35),(16.4,7.0,9.73),(19.2,7.35,9.92),(21.55,8.2,9.7),(22.3,9.46,9.64)]
    strip(out,'thermal',[(8+(y-8)*f,l,r) for y,l,r in rows],2,3,.62)
    # A broad incandescent fuller leaves only a narrow steel bevel exposed.
    strip(out,'ember core',[(8+(y-8)*f,l+.27,r-.19) for y,l,r in rows[:-1]],4,5,.75)
    for y in [9.2,10.9,12.6,14.3,16.0,17.7]:
        yy=8+(y-8)*f
        inset(out,7.87+(y-9.2)*.07,yy,.73,1.4*f,4,8.37)
        inset(out,7.77+(y-9.2)*.07,yy,.22,1.18*f,5,8.46)
    inset(out,8.4,8.83,1.2,.48,1,8.58)
    return out
def nikana(moon=False):
    out=[];height=13.9 if moon else 17.8
    shaft(out,-.4,7.7,6,.84);binding(out,[.3,1.35,2.4,3.45,4.5,5.55,6.6,7.4],1,1)
    for y in [1.0,3.1,5.2]:inset(out,8,y,.4,.68,4,8.53,45)
    box(out,'crowned pommel',8,-.37,1.25,.80,1.05,4)
    box(out,'basket guard',8.05,7.7,2.7,.45,1.8,4,rot=-22.5)
    box(out,'guard point',7.13,7.95,.43,2.05,.7,3,rot=45)
    rows=[(0,7.65,8.75),(.2,7.67,8.88),(.6,7.83,9.04),(.8,8.2,9.36),(.93,8.75,9.65),(1,9.61,9.75)]
    strip(out,'duviri steel',[(8+h*height,l,r) for h,l,r in rows],2 if not moon else 7,3 if not moon else 4,.45)
    inset(out,8.16,9.22,.34,1.6,4,8.28)
    return out
def offset(elements,dx,dy,dz,scale):
    result=deepcopy(elements)
    for e in result:
        for key in ['from','to']:
            x,y,z=e[key];e[key]=coords((8+(x-8)*scale+dx,8+(y-8)*scale+dy,8+(z-8)*scale+dz))
        if 'rotation' in e:
            x,y,z=e['rotation']['origin'];e['rotation']['origin']=coords((8+(x-8)*scale+dx,8+(y-8)*scale+dy,8+(z-8)*scale+dz))
    return result
def disp(rot,shift,scale):return {'rotation':rot,'translation':shift,'scale':[scale]*3}
def model(id,geom):
    top=max(e['to'][1] for e in geom); bottom=min(e['from'][1] for e in geom)
    scythe=id in ('hate','harmony')
    scale=min(.83,18.9/(top-bottom));gui=disp([4,-12,-31 if not scythe else -23],[1 if scythe else 0,round((8-(bottom+top)*.5)*scale*.77,2),0],round(scale,3))
    third=disp([-85,-5,-7],[0,2.3,1.5],.89)
    first=disp([-16,10,-21],[0,0,-1.2],.79 if not scythe else .68)
    return {'credit':'Ageless Arsenals • original Tenno melee mesh','ambientocclusion':False,
            'textures':{'metal':'destiny2mc:item/tenno_blades/'+id,'particle':'destiny2mc:item/tenno_blades/'+id},
            'display':{'gui':gui,'ground':disp([0,0,-90],[0,2,0],.45),'fixed':gui,
            'thirdperson_righthand':third,'thirdperson_lefthand':third,'firstperson_righthand':first,'firstperson_lefthand':first},
            'elements':geom}
def write(path,data):path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':'))+'\n')
for name in palettes:
    atlas(name)
    make={'amanata':amanata,'hate':hate,'harmony':harmony,'heat_sword':heat,'dual_heat_swords':heat,'sun_and_moon':nikana}[name]
    main=make()
    if name not in ('dual_heat_swords','sun_and_moon'):
        write(models/(name+'.json'),model(name,main)); continue
    off=make(True)
    for suffix,geometry in [('main',main),('off',off),('pair',offset(main,2.2,0,-1,.91)+offset(off,-1.9,-.9,1,.91))]:
        write(parts/(name+'_'+suffix+'.json'),model(name,geometry))
    path='destiny2mc:item/tenno_blades/'+name
    write(models/(name+'.json'),{'loader':'neoforge:separate_transforms','textures':{'particle':'destiny2mc:item/tenno_blades/'+name},
        'base':{'parent':path+'_main'},'perspectives':{k:{'parent':path+'_pair'} for k in ['gui','ground','fixed']}})
