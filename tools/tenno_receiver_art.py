"""Build Warframe armory geometry. Coordinates follow the reference side elevation.
u increases toward the muzzle, y is height and x is the sideways displacement.
Source PNGs are references only. All geometry and material tiles below are original.
"""
from pathlib import Path
from PIL import Image, ImageDraw
from math import atan2, hypot, cos, sin, radians
import json, random, argparse
from copy import deepcopy

LAYERS=('body','action','load')
SIDES=('north','south','up','down','east','west')
MATERIALS=('shell','metal','dark','shade','accent','edge','lamp','inset','plate','vents','etched','rivets','ribs','stain','crest','stripe')
# shell, bare metal, darkest steel, shadow, accent, edge, emissive-colored lacquer, inlay
PALETTES={
'ax_52':'754139 535361 24242c 393c44 935244 9f9690 dbbc86 643a2d',
'grakata':'a47739 525352 202323 373b38 c49759 8c9391 dca34e 654c30',
'prisma_grakata':'288fa7 4aa7ba 172f3c 244a5f 62c7d9 a0ddee 98e6ff 236780',
'twin_grakatas':'9a713e 4e5556 242626 393c37 c29b63 8a8e87 e49c55 665139',
'braton':'9d9ea3 6e737c 272831 40424c a43e3b c3c4cd 70d5e7 484853',
'braton_vandal':'609fa8 438598 242733 393340 91c1c0 bedfdf c6f3f2 414954',
'braton_prime':'d0cbc7 9babc2 273047 536373 d2a94e f6dd86 9ac2fc 576a81',
'burston':'a6b4b8 73838c 293338 4b585c 8999a1 dce5e2 79e4e9 63747a',
'burston_prime':'605d53 8c8674 292c29 42453c c4a443 f4d887 a5e5e7 686143',
'soma':'a59b59 666f75 333e46 50595d aaa459 d4cc96 85c6cd 7c7960',
'soma_prime':'988545 6b6858 292d2a 48483d d3b65e ece091 80e7f4 4e5449',
'perigale':'c68465 705354 282c35 41323d 9c3f35 deab88 fd6d53 673c3a',
'perigale_prime':'e3c4aa 68525e 2d2634 463444 c1a249 f1d88a 78b9da 704447',
'vesper_77':'4e4f58 676971 232630 32343b 6c6470 9c9aa1 c8dc8b 363a47',
'cantare':'dba09b b2a29c 3a4045 65464a aa4842 f5e3d8 8ddc93 416152',
}

def rgb(h): return tuple(bytes.fromhex(h))
def mix(a,b,t): return tuple(round(p*(1-t)+q*t) for p,q in zip(a,b))
def textured(model,output):
    """Pixel-art material swatches containing finish changes and legible inset motifs."""
    palette=[rgb(c) for c in PALETTES[model].split()]
    atlas=Image.new('RGB',(64,64)); atlas_draw=ImageDraw.Draw(atlas)
    for n,kind in enumerate(MATERIALS):
        primary=n if n<8 else dict(plate=0,vents=3,etched=0,rivets=1,ribs=3,stain=0,crest=3,stripe=0)[kind]
        c=palette[primary]; lighter=mix(c,(242,242,234),.24); darker=mix(c,palette[2],.53)
        tile=Image.new('RGB',(16,16),c); d=ImageDraw.Draw(tile)
        rng=random.Random(model+kind)
        for y in range(16):
            for x in range(16):
                v=rng.choice((-5,-3,-1,0,1,3,5))
                tile.putpixel((x,y),tuple(min(255,max(0,i+v)) for i in c))
        if kind in ('metal','edge','lamp','dark','inset'):
            d.line((1,2,12,2),fill=mix(c,lighter,.36));d.line((8,13,14,13),fill=mix(c,darker,.33))
        if kind=='lamp':
            d.line((2,7,13,7),fill=mix(c,(255,255,248),.7));d.line((4,8,11,8),fill=lighter)
        if kind=='plate':
            d.line([(0,13),(9,13),(15,7)],fill=darker,width=1)
            d.line([(0,12),(9,12),(15,6)],fill=lighter)
        if kind=='vents':
            for x in (3,7,11):
                d.line([(x,11),(x+2,5)],fill=palette[2],width=2); d.point((x+3,5),fill=lighter)
        if kind=='etched':
            d.line([(0,10),(3,10),(7,5),(10,5),(15,2)],fill=palette[4])
            d.line([(0,12),(4,12),(8,8),(12,8),(15,5)],fill=palette[3])
        if kind=='rivets':
            for x,y in ((2,3),(13,12)):
                d.rectangle((x-1,y-1,x+1,y+1),fill=darker); d.point((x,y),fill=lighter)
        if kind=='ribs':
            for x in (2,6,10,14): d.line((x,1,x-1,14),fill=darker,width=2);d.line((x+1,1,x,14),fill=lighter)
        if kind=='stain':
            for j in range(6):
                x,y=rng.randrange(-2,15),rng.randrange(-1,14); d.polygon([(x,y+1),(x+2,y),(x+4,y+1),(x+5,y+3),(x+2,y+4)], fill=mix(c,palette[3],.22))
        if kind=='crest':
            # Fan of three lilies on the Vandal's trade dress. Pixel-applied on both stock faces.
            d.line([(1,12),(5,13),(8,14),(10,11),(8,8),(6,6),(7,3),(8,1),(10,5),(12,7),(12,10),(9,13)],fill=palette[4],width=1)
            d.line([(8,13),(5,10),(3,9),(2,6),(1,8),(2,11),(6,13)],fill=palette[4])
            d.line([(10,13),(13,11),(15,7),(15,10),(13,13)],fill=palette[4])
        if kind=='stripe':
            d.line([(0,12),(8,12),(14,4),(15,3)],fill=palette[4],width=2)
        atlas.paste(tile,(16*(n%4),16*(n//4)))
    output.parent.mkdir(parents=True,exist_ok=True);atlas.save(output)
    return palette

class Model:
    def __init__(self,key):
        self.key=key; self.parts={k:[] for k in LAYERS};self.axis=12.8
    def box(self,name,u0,u1,y0,y1,width,mat,off=0,rotation=0,part='body',sides=None):
        assert u1>u0 and y1>y0 and width>0,name
        xyz=[8+off-width/2,y0,8-u1]; end=[8+off+width/2,y1,8-u0]
        def uv(k):
            i=MATERIALS.index(k);return [4*(i%4)+.14,4*(i//4)+.14,4*(i%4+1)-.14,4*(i//4+1)-.14]
        faces={s:{'uv':uv(sides if sides and s in ('east','west') else mat),'texture':'#finish'} for s in SIDES}
        e={'name':name,'from':list(map(lambda x:round(x,4),xyz)),'to':list(map(lambda x:round(x,4),end)),'faces':faces}
        if rotation:
            e['rotation']={'angle':rotation,'axis':'x','origin':[8+off,round((y0+y1)/2,4),round(8-(u0+u1)/2,4)],'rescale':False}
        self.parts[part].append(e)
    def stroke(self,name,points,width,thickness,mat,off=0,part='body'):
        """A rib or shell lip with legal Minecraft rotations and a square-ended silhouette."""
        for j,((u,y),(v,h)) in enumerate(zip(points,points[1:])):
            if v<u:u,v,y,h=v,u,h,y
            theta=atan2(h-y,v-u)*180/3.141592654
            vertical=abs(theta)>56.25
            a=theta-90 if theta>0 and vertical else theta+90 if vertical else theta
            a=min((-45,-22.5,0,22.5,45),key=lambda r:abs(a-r))
            length=hypot(v-u,h-y)+min(.19,thickness*.2)
            lu,ly=(thickness,length) if vertical else (length,thickness)
            self.box(f'{name} {j+1}',(u+v-lu)/2,(u+v+lu)/2,(y+h-ly)/2,(y+h+ly)/2,width,mat,off,a,part)
    def pair(self,label,points,gap,width,height,mat,part='body'):
        for s in (-1,1):self.stroke(label+(' L' if s<0 else ' R'),points,width,height,mat,s*gap,part)
    def plaques(self,name,u0,u1,y0,y1,off,mat):
        for s in (-1,1):self.box(name,u0,u1,y0,y1,.18,mat,s*off)
    def sights(self,rear,front,base=10.4,style='metal'):
        # This leaf and the front ears deliberately flank the center ray at (8,12.8).
        for u,label,gap in [(rear,'rear',.82),(front,'front',.66)]:
            for s in (-1,1):self.box(f'{label} sight ear',u-.34,u+.34,base,self.axis+.12,.29,style,s*gap)
        self.box('front bead below eye line',front-.26,front+.26,base,self.axis-.47,.25,'lamp')
    def muzzle(self,u,y,width,mat='metal'):
        self.box('muzzle ring',u-.62,u+.1,y-width/2,y+width/2,width,mat)
        self.box('recessed muzzle face',u+.1,u+.16,y-width*.29,y+width*.29,width*.59,'dark')
    def export(self,base):
        res=base/'src/main/resources/assets/destiny2mc'
        textured(self.key,res/'textures/item'/('tenno_'+self.key+'.png'))
        textures={'finish':'destiny2mc:item/tenno_'+self.key,'particle':'destiny2mc:item/tenno_'+self.key}
        elements=sum(self.parts.values(),[])
        def write(path,obj):
            path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,separators=(',',':'))+'\n')
        for p,el in self.parts.items():
            write(res/'models/armory'/self.key/(p+'.json'),{'textures':textures,'ambientocclusion':False,'elements':el})
        scale=.53 if self.key not in ('vesper_77','cantare') else .72
        def transform(rot,translate,s):return {'rotation':rot,'translation':translate,'scale':[s]*3}
        display={'gui':transform([9,72,-14],[0,.35,0],scale),'fixed':transform([0,90,0],[0,0,0],scale),'ground':transform([0,0,0],[0,3,0],.47)}
        for side in ('right','left'):
            display['thirdperson_'+side+'hand']=transform([74,0,0],[0,1.0,2.1],.65)
            display['firstperson_'+side+'hand']=transform([0,0,0],[0,-1,0],.70)
        model={'credit':'Ageless Arsenals | original Tenno weapon geometry','ambientocclusion':False,'textures':textures,'display':display,'elements':elements}
        if self.key in ('twin_grakatas','cantare'):
            # The runtime draws the single fallback once in each hand. Only detached views
            # use the paired sculpture, so an inventory pair never becomes four held guns.
            target=res/'models/armory'/self.key
            write(target/'single.json',model)
            paired=[]
            for i,offset in enumerate(([-2.4,1.15,-1.6],[2.4,-1.15,1.6])):
                for original in elements:
                    e=deepcopy(original);e['name']=('near ' if i else 'far ')+e['name']
                    for tag in ('from','to'):
                        e[tag]=[round(x+d,4) for x,d in zip(e[tag],offset)]
                    if 'rotation' in e:e['rotation']['origin']=[round(x+d,4) for x,d in zip(e['rotation']['origin'],offset)]
                    paired.append(e)
            write(target/'paired_icon.json',{**model,'elements':paired})
            ref='destiny2mc:armory/'+self.key+'/'
            model={'loader':'neoforge:separate_transforms','textures':textures,'display':display,'base':{'parent':ref+'single'},'perspectives':{k:{'parent':ref+'paired_icon'} for k in ('gui','ground','fixed')}}
        write(res/'models/item'/(self.key+'.json'),model)

def ax(m):
    b,s=m.box,m.stroke
    b('stamped receiver',-5.7,4.1,7.85,10.65,2.92,'metal',sides='rivets')
    b('ridged top cover',-5.4,2.4,10.5,11.3,2.52,'shade')
    b('cover crest',-4.4,1.5,11.22,11.53,1.8,'metal')
    s('folding stock upper wire',[(-15.6,9.5),(-9.5,9.5),(-7.3,10.45),(-5.55,10.45)],.46,.48,'metal')
    s('folding stock lower wire',[(-15.8,8.15),(-9.9,8.15),(-6.0,9.6)],.46,.48,'metal')
    s('stock heel',[(-15.55,9.85),(-16.0,9.4),(-16.3,8.6),(-15.7,7.8)],1.23,.73,'shell')
    s('ruddy grip',[(-5.6,3.9),(-4.55,6.45),(-4.3,7.85)],1.6,1.63,'shell')
    s('trigger bow',[(-4.4,7.9),(-4.65,6.35),(-2.05,6.45),(-1.7,7.85)],.44,.32,'metal')
    b('round action face',-.25,2.15,9.32,10.12,.32,'edge',1.55,part='action')
    b('knob',.25,.88,9.3,9.9,.75,'metal',1.91,part='action')
    s('swept box magazine',[(-.4,7.8),(.5,5.65),(1.8,3.5),(3.3,1.9)],2.05,2.8,'shell',part='load')
    s('magazine seam',[(.05,7.5),(1.48,4.3),(2.8,2.65)],.15,.22,'accent',1.04,'load')
    b('handguard lower',4,9.6,8.1,9.36,2.36,'shell',sides='plate')
    b('handguard upper',4.25,8.95,9.5,10.43,2.08,'shell')
    b('exposed metal tube',8.85,16.6,8.7,9.4,1.08,'metal')
    b('gas tube',8.9,12.85,9.7,10.15,.82,'shade')
    s('gas junction',[(12.0,10.15),(13.18,9.12)],.99,.62,'shade')
    m.muzzle(16.8,9.05,1.37)
    # The long russet sling is a conspicuous part of the Höllvania reference.
    s('slack russet sling',[(-15.4,7.9),(-13.0,4.85),(-9.8,2.6),(-5.8,1.2),(-1.5,.65),(3.5,.9),(6.8,2.5),(9.0,6.1),(9.4,8.25)],.34,.32,'shell',-.96)
    m.sights(2.9,14.5,9.2)

def grakata(m):
    b,s=m.box,m.stroke;pr=m.key=='prisma_grakata';dual=m.key=='twin_grakatas'
    b('rear open rail bottom',-13,-5.8,8.65,9.48,2.0,'metal')
    b('rear open rail top',-13,-6.45,10.63,11.2,2.18,'shell' if pr else 'shade')
    for u in (-12.85,-6.6): b('rail crossmember',u,u+.55,9.25,10.8,1.95,'shade')
    if pr:
        b('Prisma sealed stock cheeks',-12.89,-6.75,9.26,10.79,2.43,'shell',sides='stain')
        s('Prisma stock relief',[(-12.49,10.49),(-10.15,10.16),(-8.31,9.52)],.16,.28,'lamp',1.25)
        b('polished forward cylinder jacket',6.85,12.25,5.24,7.22,2.08,'shell',sides='stain')
    b('chamber spine',-6.65,9.4,8.88,10.77,2.2,'shade',sides='vents')
    b('humped forebody',-3.55,4.95,8.3,10.48,3.5,'shell',sides='stain')
    s('forebody tapered chin',[(-3.25,8.5),(-1.55,7.8),(1.85,7.8),(4.2,8.7)],2.75,.74,'shell')
    for u in (-1.9,-.45,1.0,2.45):m.plaques('cooling slash',u,u+.27,9.25,10.12,1.77,'inset')
    for u in (2.95,3.8):s('dorsal quill',[(u,10.32),(u-.9,12.2)],.29,.25,'metal',.93)
    s('trigger grip',[(-8.05,6.0),(-7.4,8.7)],1.36,1.45,'shade')
    s('trigger ring',[(-7.3,8.45),(-5.45,8.18),(-5.12,7.25),(-6.12,6.75),(-6.9,7.0)],.56,.46,'accent')
    if not dual:
        b('pommel capsule waist',-9,-7,4.1,5.95,1.87,'shade')
        b('rounded charge reservoir',-9.6,-6.7,3.0,5.1,2.84,'shade',sides='ribs')
        b('reservoir ferrule',-9.15,-7.05,2.5,3.1,1.95,'metal')
        b('reservoir plug',-8.55,-7.65,1.92,2.68,1.15,'shade')
    else:b('dual short grip heel',-8.85,-7.14,5.52,6.22,1.5,'metal')
    b('long upper barrel',4.75,13.55,9.46,10.45,1.05,'metal')
    b('barrel sleeve',10.0,13.45,9.26,10.65,1.38,'shell' if pr else 'shade');m.muzzle(13.5,9.94,1.4)
    s('forward magazine hinge',[(5.7,8.9),(4.48,6.1),(4.55,4.5)],2.06,2.1,'shell')
    b('lower forward drum',3.0,6.48,3.4,6.6,3.42,'metal',part='load',sides='rivets')
    b('faceted drum face',3.4,6.0,3.08,6.76,2.88,'shade',part='load',sides='stripe')
    b('parallel lower cylinder',6.2,12.55,5.49,6.99,1.62,'shell' if pr else 'shade')
    b('front upright',11.7,12.7,6.65,9.3,1.03,'metal');b('reciprocating breech',-5.15,-3.22,9.9,10.58,.48,'metal',-1.45,part='action')
    m.sights(-4.4,8.5,10.18)

def braton(m):
    b,s=m.box,m.stroke;prime=m.key.endswith('prime');vandal=m.key.endswith('vandal')
    if vandal:
        b('box stock',-14.5,-3.8,7.14,10.62,3.1,'shade',sides='crest')
        s('raised comb',[(-12.7,10.7),(-10.8,11.5),(-7.8,10.66)],2.72,.65,'shade')
        b('flat receiver',-4.05,9.0,7.46,10.07,2.9,'shell',sides='rivets')
        b('square slide',-2,8.5,10.01,10.55,2.45,'metal',part='action')
        b('black handguard',4.5,11.7,6.78,9.22,3.26,'shade',sides='ribs')
        b('front tower',10.35,12.15,9.75,13.7,1.9,'metal')
        b('rear handle tower',-4.0,-2.6,10.4,13.9,2.45,'metal')
        m.pair('straight carrying rails',[(-3.6,13.8),(11.3,13.8)],.86,.48,.59,'shade')
        b('grip magazine',-6.15,-3.9,3.4,7.6,1.77,'shade',part='load',sides='rivets')
        b('magazine charge pip',-5.63,-5.21,4.0,4.49,.10,'lamp',.92,part='load')
        m.muzzle(12.75,9.04,1.73);m.axis=14.65;m.sights(-3.6,11.0,13.45)
    elif not prime:
        b('graphite cheek',-13.9,-3.6,7.0,10.9,3.0,'shade',sides='plate')
        s('silver recoil heel',[(-14.45,7.14),(-13.2,10.73)],2.95,1.1,'shell')
        s('red seam',[(-13.4,7.38),(-6.9,7.38),(-4.4,9.44),(-3.1,11.4)],.22,.28,'accent',1.53)
        b('receiver shroud',-4.14,12.2,7.5,10.18,2.9,'shell',sides='plate')
        b('underslung front rail',2.75,12.2,6.88,7.76,2.65,'shade')
        b('cyan status window',2.65,5.22,7.8,8.1,.21,'lamp',1.51)
        b('ejection slide',.45,4.9,9.22,9.89,.28,'metal',1.55,part='action')
        s('curved grip',[(-6.61,3.43),(-5.5,5.04),(-5.27,7.22)],1.58,1.48,'shade')
        b('short magazine',-1.8,1.07,5.37,7.64,1.84,'shade',part='load',sides='rivets')
        m.pair('angular carrying bridge',[(-5.15,10.48),(-3.35,12.65),(-2.3,13.55),(.45,12.95),(10.35,12.95)],.93,.55,.67,'metal')
        m.pair('scarlet handle trim',[(-3.5,12.2),(-2.3,13.1),(.45,12.56),(10.0,12.56)],.98,.33,.19,'accent')
        s('front carry mount',[(9.8,10.06),(10.72,12.91)],1.68,.68,'metal');m.muzzle(12.89,9.16,1.56)
        m.axis=13.7;m.sights(-3.4,10.4,12.8)
    else:
        b('ivory receiver',-10.55,7.7,8.3,10.8,2.67,'shell',sides='etched')
        b('navy butt heel',-13.95,-10.15,8.23,10.79,2.52,'metal')
        s('heel bow',[(-9.8,8.1),(-12.2,8.02),(-14.45,8.6),(-14.8,9.6),(-14.0,10.83),(-11.88,11.03)],2.95,.41,'accent')
        s('ivory curved grip',[(-4.5,3.6),(-5.0,5.05),(-4.85,6.8),(-3.15,8.54)],1.94,1.97,'metal')
        m.pair('grip gilding',[(-4.05,3.8),(-4.45,5.12),(-4.15,6.95),(-2.52,8.46)],.95,.3,.29,'accent')
        b('forward sapphire action',6.6,10.1,7.86,10.34,3.1,'metal',part='action',sides='rivets')
        b('receiver gold rail',-2.65,4.8,10.3,10.67,2.94,'accent')
        b('ventral load',-.63,3.8,7.48,8.51,1.85,'shade',part='load',sides='ribs')
        m.pair('gold carry spines',[(-9.7,10.7),(-7.0,11.2),(-3.7,13.4),(-2.1,13.9),(9.3,13.5),(10.4,12.44),(10.9,10.37)],.86,.50,.47,'accent')
        m.pair('gilt nose tendril',[(6.15,9.75),(8.25,8.65),(10.7,8.74),(12.0,7.4),(13.8,6.72)],1.14,.28,.38,'accent')
        b('front shroud',10.45,13.03,9.37,11.09,2.16,'metal')
        for u in (10.6,11.62,12.6):b('gilded muzzle collar',u,u+.32,9.02,11.4,2.66,'accent')
        m.muzzle(13.22,10.1,1.72);m.axis=14.5;m.sights(-2,9.2,13.35,'accent')

def burston(m):
    b,s=m.box,m.stroke;prime=m.key.endswith('prime')
    if not prime:
        b('arced pale shoulder stock',-13.95,-3.35,7.6,10.74,3.0,'shell',sides='plate')
        s('stock heel flare',[(-13.72,7.46),(-14.48,8.53),(-14.23,11.33),(-13.73,11.7)],2.81,.72,'edge')
        s('long silver dorsal ridge',[(-13.65,10.4),(-10.35,10.8),(-7,10.42),(-3.4,10.77),(.65,11.32)],2.35,.85,'shell')
        m.pair('arched integrated carry handle',[(-3.23,10.65),(-1,12.02),(1.55,12.95),(5.18,13.27),(8.95,12.94),(10.76,12.43),(11.48,11.78)],1.00,.8,.81,'shell')
        s('rounded muzzle support',[(9.98,11.97),(10.52,10.94),(10.2,9.76)],2.19,1.59,'shell')
        b('receiver bridge',-4.2,12.74,8.15,9.99,3.02,'metal',sides='plate')
        b('breech pull',-4.3,-1.1,9.9,10.4,1.84,'metal',part='action')
        m.plaques('cyan heel jewel',-12.38,-11.48,8.98,9.49,1.55,'lamp')
        m.plaques('cyan nose jewel',9.95,11.13,8.77,9.24,1.54,'lamp')
        s('rounded pistol grip',[(-7.15,3.78),(-7.24,4.94),(-6.0,7.35),(-4.96,8.64)],1.6,1.56,'shade')
        s('oval trigger bow',[(-6.05,6.6),(-4.1,6.22),(-2.3,6.45),(-.78,7.62)],.48,.47,'metal')
        s('angled ventral cartridge',[(.08,6.1),(2.67,7.83)],2.24,2.18,'metal',part='load')
        b('cartridge jewel',1.45,1.90,7.1,7.5,.18,'lamp',1.14,part='load')
        b('lower arc under nose',3.4,12.7,7.7,8.36,2.42,'shade');m.muzzle(13.3,9.03,1.74)
        m.axis=13.9;m.sights(.0,10.25,12.61)
    else:
        b('bronze slab receiver',-9.55,8.6,7.75,10.61,2.6,'shade',sides='etched')
        b('underslung stock heel',-14.2,-8.75,7.21,9.56,2.6,'shade')
        s('stock face', [(-14.0,9.78),(-8.72,9.86),(-5.5,10.65)],2.84,.9,'metal')
        m.pair('blade-edged top rail',[(-13.7,10.1),(-7.35,10.48),(-3.85,10.88),(-.3,12.44),(3.62,13.4),(6.8,13.36),(9.08,12.63),(9.64,11.45)],.84,.52,.43,'accent')
        s('swept grip',[(-6.6,4.17),(-5.9,5.6),(-4.05,8.23)],1.44,1.53,'metal')
        m.pair('long lower bow',[(-7.5,4.08),(-3.65,4.18),(.52,4.63),(3.96,5.71),(7.1,7.78),(8.94,8.79),(12.7,8.95)],.78,.43,.36,'accent')
        s('trigger spur',[(-3.6,8.0),(-3.43,7.27),(-2.75,6.8)],.38,.42,'accent')
        b('recessed jade cartridge',-1.2,5.74,8.18,9.77,2.78,'metal',part='load',sides='plate')
        m.plaques('jade lamp',1.2,2.24,8.95,9.31,1.41,'lamp')
        b('upper forward latch',3.0,7.2,10.52,10.98,1.75,'accent',part='action')
        b('tapered lance at muzzle',8.37,12.83,8.66,9.39,1.05,'accent')
        b('lance blade',12.8,16.38,8.77,9.18,.67,'accent');b('lance tip',16.37,17.65,8.95,9.15,.32,'edge')
        m.axis=14.23;m.sights(-.1,8.8,12.49,'accent')

def soma(m):
    b,s=m.box,m.stroke;prime=m.key.endswith('prime')
    s('long open butt top',[(-15.0,10.12),(-13.7,10.78),(-9.3,10.75),(-6.4,10.46),(-2.4,10.51)],1.41,.97,'accent' if prime else 'shell')
    s('open loop heel',[(-13.95,10.8),(-15.48,10.15),(-16.05,9.22),(-15.94,8.12),(-15.1,7.58),(-13.6,7.63),(-12.95,8.55),(-13.05,9.6)],1.46,.58,'accent' if prime else 'metal')
    s('stock lower wire',[(-15.1,7.6),(-12.1,7.32),(-7.25,7.32),(-5.28,7.7),(-4.1,9.04)],.77,.48,'metal')
    b('chamber',-5.23,1.22,8.73,10.69,2.38,'metal',sides='rivets')
    s('grip hook',[(-6.1,4.56),(-5.7,5.18),(-4.95,5.8),(-4.44,7.62),(-3.23,9.0)],1.37,1.48,'shell')
    b('charging slide',-4.5,-2.0,9.27,10.01,2.67,'edge',part='action',sides='plate')
    b('long barrel shroud',-.2,10.95,8.81,10.44,1.97,'shell' if not prime else 'accent',sides='etched' if prime else 'plate')
    s('open barrel spine',[(5.6,10.4),(8.7,10.85),(15.9,10.96)],.84,.48,'edge' if prime else 'metal')
    b('thin barrel',10.85,16.82,9.11,9.79,.75,'metal')
    m.muzzle(17.12,9.49,1.1,'accent' if prime else 'metal')
    # Off-axis magazine clears the reticle, even with its visibly taller upper horn.
    if not prime:
        for label,pts in [('upper',[(-1.5,10.02),(-1.2,11.89),(-.16,13.9),(1.2,15.65),(3.05,16.71),(5.2,17.12)]),
                          ('lower',[(-1.52,9.36),(-1.2,7.48),(-.1,5.53),(1.39,3.97),(3.21,2.92),(5.13,2.55)])]:
            s('curved '+label+' cartridge',pts,.63,1.07,'metal',1.16,'load')
            for j,((u,y),(v,h)) in enumerate(zip(pts,pts[1:])):
                for t in (.23,.76):
                    x=u+(v-u)*t;hy=y+(h-y)*t;b('recessed magazine link',x-.17,x+.17,hy-.28,hy+.28,.09,'dark',1.50,part='load')
    else:
        s('gold upper crescent',[(-1.3,10.23),(-.32,12.13),(1.5,14.62),(3.13,15.8),(5.55,16.53)],.6,.71,'accent',1.23,'load')
        s('upper inner prong',[(.3,10.3),(1.34,11.85),(2.8,12.81),(4.1,13.32)],.43,.66,'edge',1.19,'load')
        s('lower crescent',[(-1.24,9.29),(-1.0,7.4),(.57,5.4),(2.5,3.65),(5.38,2.8)],.6,.67,'accent',1.23,'load')
        s('lower inner prong',[(.44,9.71),(1.47,8.02),(2.79,6.98),(4.46,6.47)],.45,.53,'accent',1.13,'load')
        for side in (-1,1):
            s('muzzle crown',[(12.89,6.6),(13.54,8.25),(13.25,10.25),(12.34,12.05)],.53,.49,'accent',side*.83)
        m.plaques('turquoise receiver gem',-4.65,-3.45,9.15,9.89,1.36,'lamp')
        s('gilded grip spur',[(-5.45,5.35),(-5.98,4.19),(-4.9,3.12)],.63,.44,'accent')
    m.sights(-3.2,11.9,10.18,'accent' if prime else 'metal')

def perigale(m):
    b,s=m.box,m.stroke;prime=m.key.endswith('prime')
    outline='accent' if prime else 'shell'; silver='edge' if prime else 'accent'
    b('recessed void spine',-8.14,15.65,8.2,9.27,1.24,'dark')
    s('top arc of hollow rear cage',[(-15.1,9.75),(-14.12,10.09),(-10.1,9.51),(-8.9,10.26),(-8.3,11.88),(-6.63,12.53),(-4.3,12.13),(-2.2,10.73),(.7,10.39),(2.31,10.8)],2.1,.78,outline)
    s('lower returning bow',[(-15.47,10.1),(-14.64,8.1),(-12.6,6.68),(-10.84,5.69),(-9.12,5.14),(-7.1,5.48),(-4.52,7.1),(-1.31,8.73),(1.06,8.6)],1.87,.8,outline)
    m.pair('rib running into chamber',[(-10.25,6.05),(-7.67,6.2),(-5.12,7.31),(-1.23,8.07),(1.69,9.25),(4.82,10.08),(8.69,10.16)],.78,.63,.63,'metal')
    s('shallow bony palm rest',[(-9.65,6.15),(-9.2,7.56),(-9.65,8.74)],1.19,.77,'shade')
    b('receiver socket',-1.8,4.9,8.23,10.0,2.31,'shade',sides='vents')
    b('exposed internal bolt',.64,4.23,9.35,10.13,1.54,'metal',part='action')
    b('recessed cylinder',-1.13,2.11,7.89,8.55,1.57,'inset',part='load')
    for side in (-1,1):
        for u,h in ((1.05,9.2),(3.23,9.35),(5.73,9.4),(8.51,9.38),(11.35,9.4)):
            s('overlapping bone blade',[(u-1.18,h-.6),(u+.7,h+.53),(u+2.48,h+.47)],.72,.48,'shell',side*.92)
        s('barrel side spar',[(1.5,8.5),(4.63,8.1),(7.15,8.49),(11.43,8.65),(15.49,8.75)],.49,.5,outline,side*.77)
        s('muzzle fork',[(9.63,9.82),(12.05,9.54),(15.95,9.91),(17.11,10.47 if prime else 9.87)],.41,.56,outline,side*.75)
        s('faceted side plate',[(-8.04,12.1),(-6.46,11.95),(-4.62,10.77),(-2.66,10.23)],.79,1.06,'shell',side*.7)
        if prime:
            s('gold lower whisker',[(-10.4,5.3),(-7.8,4.62),(-4.4,4.92),(-.21,5.62),(3.35,5.54)],.38,.37,'accent',side*.97)
            s('upper gemstone frame',[(-8.15,12.84),(-6.73,13.65),(-5.7,13.18)],.45,.40,'accent',side*.84)
            s('forearm whisker',[(.1,8.4),(2.35,7.4),(4.65,6.91)],.38,.33,'accent',side*1.07)
    m.plaques('energy trace',.4,1.45,9.19,9.57,1.17,'lamp')
    s('forward horn',[(11.84,9.52),(12.29,11.13)],.52,.52,silver,.8)
    b('barrel tip',14.77,17.0,8.92,9.38,.72,'dark')
    m.axis=14.14 if prime else 13.44;m.sights(-5.8,11.7,10.4,outline)

def vesper(m):
    b,s=m.box,m.stroke
    b('slide frame',-7.75,1.8,8.72,10.54,1.95,'shade',sides='plate')
    s('beavertail',[(-9,8.72),(-7.4,9.32),(-5.66,9.37)],1.68,.78,'shade')
    s('black polymer grip',[(-8.05,3.46),(-7.42,5.22),(-6.47,8.18),(-5.82,8.88)],2.02,2.48,'shade')
    s('backstrap edge',[(-8.87,3.75),(-8.18,5.72),(-7.31,8.41)],1.87,.47,'dark')
    b('magazine floor',-9.21,-6.84,2.88,3.64,2.1,'metal',part='load')
    b('magazine foot bevel',-8.95,-6.92,2.61,3.05,1.83,'shade',part='load')
    s('square trigger guard',[(-5.5,8.29),(-5.34,6.55),(-4.53,6.07),(-.73,6.07),(-.77,8.37)],.62,.47,'metal')
    s('trigger',[(-3.61,8.33),(-4.02,7.22),(-3.72,6.89)],.46,.34,'metal')
    b('slide',-7.55,1.25,9.89,11.20,2.0,'shade',part='action',sides='vents')
    b('chamber window',-3.16,-.56,10.42,10.91,2.07,'metal',part='action')
    b('suppressor full tube',.6,10.75,9.36,10.9,2.1,'shell')
    b('square to round transition',.47,1.61,9.1,11.19,2.4,'metal')
    b('steel central sleeve',4.08,7.49,9.19,11.07,2.34,'metal')
    b('silencer nose',9.92,10.85,9.16,11.18,2.31,'shade');m.muzzle(10.98,10.17,2.05)
    b('underslung optic mount',-.64,3.4,7.77,8.48,1.39,'dark')
    b('rectangular laser unit',-.1,3.95,6.05,7.8,1.95,'shell',sides='plate')
    b('laser focus lugs',3.96,5.28,6.45,7.36,1.1,'metal',sides='ribs')
    b('laser lens',5.28,5.39,6.69,7.12,.54,'lamp')
    m.axis=12.15;m.sights(-6.15,9.57,10.92,'shade')

def cantare(m):
    b,s=m.box,m.stroke
    # Rear crescents split, leaving conspicuous air around the dark green knotted handle.
    s('top crescent',[(-9.35,8.95),(-8.39,9.56),(-7.83,10.62),(-8.04,11.5),(-8.92,11.99),(-9.51,12.07)],.64,.81,'accent')
    s('lower crescent',[(-9.35,8.67),(-8.3,8.26),(-7.76,7.14),(-7.94,6.16),(-8.87,5.31),(-9.56,5.12)],.64,.81,'accent')
    s('upper pale cutting lip',[(-9.13,9.44),(-8.7,9.96),(-8.55,10.59),(-8.78,11.56)],.7,.28,'edge')
    s('lower pale cutting lip',[(-9.05,8.16),(-8.58,7.54),(-8.48,7.09),(-8.77,6.07)],.7,.28,'edge')
    s('fork join',[(-10.72,8.82),(-9.7,9.0),(-8.55,9.12),(-7.0,8.88)],.92,.78,'accent',part='action')
    b('green pommel bridge',-7.4,-3.0,8.37,9.25,.84,'inset',sides='ribs')
    for u in (-6.85,-4.07):b('handle ear',u,u+.51,8.12,9.46,1.19,'shade')
    b('jade inlay',-5.65,-4.69,8.77,9.03,.94,'lamp')
    s('petal throat',[(-3.24,8.86),(-2.5,9.38),(-2.04,9.45),(-1.42,9.04)],.56,.52,'shell',part='load')
    s('petal neckline',[(-3.16,8.78),(-2.44,8.25),(-1.82,8.3),(-1.29,8.66)],.56,.45,'shell',part='load')
    b('long pale blade core',-1.6,9.54,8.63,9.11,.49,'edge',part='load')
    for y,sign in ((9.05,1),(8.65,-1)):
        s('flared blade edge',[(-1.6,y),(.6,y+sign*.14),(5.07,y+sign*.24),(8.29,y+sign*.41),(9.8,y+sign*.65),(10.48,y+sign*1.18)],.64,.32,'shell',part='load')
    s('split cutting tip',[(10.53,10.17),(10.13,9.55),(9.98,8.9),(10.14,8.17),(10.49,7.47)],.69,.37,'inset',part='load')
    m.axis=12.8

def build(base):
    for key in PALETTES:
        family=next((name for name in ('grakata','braton','burston','soma','perigale') if name in key),key)
        m=Model(key); {'ax_52':ax,'grakata':grakata,'braton':braton,'burston':burston,'soma':soma,'perigale':perigale,'vesper_77':vesper,'cantare':cantare}[family](m)
        m.export(base)
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--project',type=Path,default=Path(__file__).resolve().parents[1]);build(parser.parse_args().project)
