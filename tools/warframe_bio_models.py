"""Author the Grineer, Infested, Cedo and Aksondol models in native item-model JSON.

Distances along the drawing go from the grip toward the muzzle. The baked gun
looks toward negative Z, which matches the camera and hand space used by the mod.
A full inventory mesh and a partition into three animated draw calls are emitted.
"""
from __future__ import annotations
import argparse
import copy
import json
import math
import random
from pathlib import Path
from PIL import Image, ImageColor, ImageDraw

FACES = ('north', 'south', 'west', 'east', 'up', 'down')
PALETTES = {
    # painted shell, shadow shell, frame, edge highlight, rubber, energy, stripe, inlay
    'ignis':        ('b12e35','69252c','323840','89959e','24282e','ed8d4e','e9d1bd','3b6680'),
    'ignis_wraith': ('444953','82202a','282936','afb1bd','1d1c23','ef7852','d92d38','703047'),
    'brakk':        ('9b5633','5d301e','32322d','b0a89a','34332e','c8e991','8a8c45','c48351'),
    'kuva_brakk':   ('95afb4','607681','302e33','d9dcce','28282c','ffe360','dc443a','fc812e'),
    'nukor':        ('9b579e','66426d','33333c','aeb4ae','352b38','fce258','4a7056','cf85c2'),
    'kuva_nukor':   ('93a8af','56616b','292c33','d9d7c9','22252a','ffd680','c74338','ff733c'),
    'cedo':         ('5baaab','3a7378','252a30','d0cac2','25262a','b4f6f1','ebbe56','787b80'),
    'cedo_prime':   ('e1d9c6','777671','252a2e','ebdbab','292a2b','81c8ce','ad8642','4c5f66'),
    'torid':        ('845237','52362d','332f31','c99763','322826','f5bf3e','b57242','635975'),
    'bubonico':     ('4a596b','35323b','292a32','dcd9d2','292522','c9fc34','728cb0','9c7760'),
    'coda_bubonico':('7c4359','4c2c43','2f202c','e8d9e4','271e25','ff58df','a46d85','946674'),
    'aksondol':     ('aab3c5','555e7a','34354c','dfdfd3','41445b','519bcf','cda56b','788bad')
}
MATERIAL = {k:i for i,k in enumerate(('shell','shade','frame','edge','grip','energy','trim','inlay'))}
def export_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, separators=(',',':'))+'\n')
def add_rgb(rgb, delta):
    return tuple(max(0,min(255,c+delta)) for c in rgb)
def atlas(key, path):
    """Paint eight 16-pixel material tiles. UVs keep their native texel density."""
    im=Image.new('RGB',(64,32)); brush=ImageDraw.Draw(im)
    for i,hexcolor in enumerate(PALETTES[key]):
        rgb=ImageColor.getrgb('#'+hexcolor);x,y=(i%4)*16,(i//4)*16
        brush.rectangle((x,y,x+15,y+15),fill=rgb)
        rng=random.Random(key+str(i));organic=key in ('torid','bubonico','coda_bubonico')
        for row in range(16):
            for col in range(16):
                wave=math.cos((col*.65 + math.sin(row*.55)*1.35) if organic else row*.3)*2
                if i==5: wave=math.sin(row*.22)*7
                im.putpixel((x+col,y+row),add_rgb(rgb,round(wave+rng.choice([-2,-1,0,0,1,2]))))
        if i in (0,1,6):
            if organic:
                for px,py,w in ((2,3,3),(9,7,2),(6,12,3)):
                    brush.line((x+px,y+py,x+px+w,y+py-1),fill=add_rgb(rgb,11))
                    brush.point((x+px+1,y+py+1),fill=add_rgb(rgb,-10))
            else:
                brush.line((x+3,y+4,x+9,y+4),fill=add_rgb(rgb,9))
                brush.line((x+9,y+4,x+11,y+2),fill=add_rgb(rgb,10))
                brush.line((x+3,y+5,x+8,y+5),fill=add_rgb(rgb,-8))
        if i==4:
            for row in (3,7,11,15):
                brush.line((x,y+row,x+14,y+row),fill=add_rgb(rgb,-7))
                brush.line((x+1,y+row-1,x+14,y+row-1),fill=add_rgb(rgb,6))
    path.parent.mkdir(parents=True,exist_ok=True); im.save(path)
class Mesh:
    def __init__(self, key):
        self.key=key
        self.el=[]
    def box(self,name,t0,t1,y0,y1,width,mat,x=0.0,tilt=0.0,part='body',axis='x'):
        """A centered box, optionally rotated by a vanilla baking angle about its own center."""
        p=[8+x-width/2,y0,13-t1];q=[8+x+width/2,y1,13-t0]
        if not (all(b>a for a,b in zip(p,q)) and min(p)>=-16 and max(q)<=32):raise ValueError((name,p,q))
        mi=MATERIAL[mat];sx=(mi%4)*4;sy=(mi//4)*8
        faces={}
        for face in FACES:
            w,h=(width,y1-y0) if face in ('north','south') else ((t1-t0,y1-y0) if face in ('west','east') else (width,t1-t0))
            # Model UV space is always sixteen units across, including rectangular PNGs.
            w=min(3.75,max(.42,w*.42));h=min(7.5,max(.75,h*.85))
            u=sx+.125;v=sy+.25
            faces[face]={'uv':[round(u,3),round(v,3),round(u+w,3),round(v+h,3)],'texture':'#finish'}
        o={'name':name,'from':[round(v,4) for v in p],'to':[round(v,4) for v in q],'faces':faces}
        if tilt:
            o['rotation']={'origin':[round((a+b)/2,4) for a,b in zip(p,q)],'axis':axis,'angle':tilt,'rescale':False}
        self.el.append((part,o))
    def profile(self,name,sections,mat,part='body',x=0):
        """Stepped octagonal longitudinal section. Cut corners without coplanar surfaces."""
        for j,(t0,t1,y,w,h) in enumerate(sections):
            w0=w*.68;h0=h*.61
            self.box(f'{name} {j+1}',t0,t1,y-h/2,y+h/2,w0,mat,x=x,part=part)
            for side in (-1,1):
                self.box(f'{name} {j+1} bevel',t0+.012,t1-.012,y-h0/2,y+h0/2,(w-w0)/2,mat,
                         x=x+side*(w+w0)/4,part=part)
    def path(self,name,points,depth,width,mat,x=0,part='body'):
        """A continuous stylized cut line in the side plane, built from legal vanilla rotations."""
        for j,((t0,y0),(t1,y1)) in enumerate(zip(points,points[1:])):
            if t1<t0:t0,t1,y0,y1=t1,t0,y1,y0
            dt=t1-t0;dy=y1-y0
            if dt<.01:
                self.box(name,t0-depth/2,t0+depth/2,min(y0,y1)-.02,max(y0,y1)+.02,width,mat,x,part=part)
                continue
            angle=math.degrees(math.atan2(dy,dt))
            if abs(angle)>56:
                count=max(1,round(abs(dy)/.7))
                for step in range(count):
                    low=step/count;high=(step+1)/count
                    a=y0+dy*low;b=y0+dy*high
                    self.box(name,t0+dt*low-depth/2,t0+dt*high+depth/2,min(a,b)-.05,max(a,b)+.05,width,mat,x,part=part)
                continue
            snapped=min((-45,-22.5,0,22.5,45),key=lambda n:abs(angle-n))
            count=max(1,math.ceil(abs(dy-dt*math.tan(math.radians(snapped)))/max(.55,depth*.76)))
            for step in range(count):
                along=(step+.5)/count;t=t0+dt*along;y=y0+dy*along
                length=dt/max(.7,math.cos(math.radians(snapped)))/count+.12
                self.box(name,t-length/2,t+length/2,y-depth/2,y+depth/2,width,mat,x,tilt=snapped,part=part)
    def ring(self,name,t0,t1,y,w,h,wall,mat,part='body'):
        for sign in (-1,1):
            self.box(name+' lip',t0,t1,y+sign*(h-wall)/2-wall/2,y+sign*(h-wall)/2+wall/2,w,mat,part=part)
            self.box(name+' side',t0+.012,t1-.012,y-h/2+wall,y+h/2-wall,wall,mat,x=sign*(w-wall)/2,part=part)
    def marker(self,name,t,y,side,mat='energy',w=.6,h=.42,part='body'):
        self.box(name,t-w/2,t+w/2,y-h/2,y+h/2,.14,mat,x=side,part=part)
    def notch(self,rear,front,base=11.85,rear_seat=10.5,front_seat=10.2):
        self.box('rear sight riser',rear-.36,rear+.36,rear_seat,base+.12,2.12,'frame')
        self.box('front sight base',front-.35,front+.35,front_seat,base-.13,.57,'frame')
        for side in (-1,1):
            self.box('rear open sight ear',rear-.3,rear+.3,base,12.88,.3,'frame',side*.92)
            self.box('rear fiber optic',rear-.35,rear-.28,12.49,12.72,.2,'energy',side*.92)
        self.box('front sight blade',front-.24,front+.24,base-.22,12.34,.26,'frame')
        self.box('front fiber optic',front-.27,front-.20,12.15,12.32,.18,'energy')
    def save(self,assets):
        key=self.key;atlas(key,assets/'textures/item'/('tenno_'+key+'.png'))
        short=key in ('brakk','kuva_brakk','nukor','kuva_nukor')
        scale=.83 if short else .64
        display={
            'gui':{'rotation':[8,77,-15],'translation':[0,-.4,0],'scale':[scale]*3},
            'ground':{'rotation':[0,0,0],'translation':[0,2.8,0],'scale':[.43]*3},
            'fixed':{'rotation':[0,90,0],'translation':[0,0,0],'scale':[scale]*3}}
        for hand in ('right','left'):
            display['thirdperson_'+hand+'hand']={'rotation':[74,0,0],'translation':[0,1,1.8],'scale':[.65]*3}
            display['firstperson_'+hand+'hand']={'rotation':[0,0,0],'translation':[0,-1,0],'scale':[.72]*3}
        tex={'finish':'destiny2mc:item/tenno_'+key,'particle':'destiny2mc:item/tenno_'+key}
        output={'ambientocclusion':False,'textures':tex,'display':display,'elements':[g for _,g in self.el]}
        if key=='aksondol':
            stem='item/tenno_pairs/aksondol'
            export_json(assets/'models'/(stem+'_main.json'),output)
            pair=copy.deepcopy(output)
            pair['elements']=[]
            for i,offset in enumerate(((-1.7,1.05,-1.65),(1.7,-1.05,1.65))):
                for source in output['elements']:
                    element=copy.deepcopy(source)
                    for field in ('from','to'):
                        element[field]=[round(a+b,4) for a,b in zip(element[field],offset)]
                    if 'rotation' in element:
                        element['rotation']['origin']=[round(a+b,4) for a,b in zip(element['rotation']['origin'],offset)]
                    element['name']=('left ' if i==0 else 'right ')+element['name']
                    pair['elements'].append(element)
            pair['display']['gui']['scale']=[.57]*3
            export_json(assets/'models'/(stem+'_pair.json'),pair)
            wrapper={'loader':'neoforge:separate_transforms','textures':{'particle':tex['particle']},
                     'base':{'parent':'destiny2mc:'+stem+'_main'},
                     'perspectives':{view:{'parent':'destiny2mc:'+stem+'_pair'} for view in ('gui','ground','fixed')}}
            export_json(assets/'models/item'/(key+'.json'),wrapper)
        else:
            export_json(assets/'models/item'/(key+'.json'),output)
        for part in ('body','action','load'):
            block=[g for p,g in self.el if p==part]
            if not block:raise ValueError('Missing moving part for '+key+': '+part)
            export_json(assets/'models/armory'/key/(part+'.json'),{'textures':tex,'ambientocclusion':False,'elements':block})
def ignis(m,wraith):
    b,p,s=m.box,m.path,m.profile
    # The long upper shell, loop handle, curved feed line and caged pilot are visible at item scale.
    s('slender tail',[(-8,-6,8.8,.95,.85),(-6,-3,9.0,1.65,1.15),(-3,0,9.22,2.3,1.6)],'shell')
    s('flame reservoir',[(0,3,9.35,3.15,2.6),(3,8,9.52,3.65,2.9),(8,13,9.85,3.8,2.7),(13,16,10.1,3.35,2.4)],'shell')
    p('upper swept shroud',[(1,10.2),(6,10.7),(15,11.1),(16,11.65)],.72,2.25,'shell')
    b('forward scorched collar',15.5,16.7,9.2,10.95,2.4,'frame')
    s('pilot shaft',[(16.5,21.2,10.18,1.18,1.17),(21.2,22.5,10.18,.8,.8)],'frame',part='action')
    m.ring('pilot cage crown',22,22.5,10.18,2.7,2.65,.31,'frame',part='action')
    m.ring('pilot cage base',20,20.5,10.18,2.5,2.45,.32,'shade',part='action')
    for side in (-1,1):
        b('pilot cage strut',20.2,22.3,9.98,10.33,.29,'frame',side*1.15,part='action')
        p('fuel return pipe',[(-1.5,7.6),(1.3,7),(4.2,9.25),(7.2,9.8)],.37,.32,'trim' if wraith else 'edge',side*1.94)
        p('fore tank upper',[ (6.5,7.7),(8,8.15),(11.5,8.15),(12.9,7.7)],.45,.44,'frame',side*.86,part='load')
        p('shroud piping',[(7.4,9.45),(12.3,9.65),(15.45,10.5)],.25,.12,'trim' if wraith else 'shade',side*1.87)
        for j in range(3):
            m.marker('stencilled hazard',13.75+j*.48,10.5,side*1.7,'trim',.21,.5)
    s('detachable forward tank',[(5.9,7,7.15,1.7,1.42),(7,11,7.0,2.7,2.1),(11,12.9,7.3,2.1,1.65)],'shade' if wraith else 'shell',part='load')
    p('rear grip',[(-.25,8.15),(-1.08,4.1)],.95,1.15,'grip')
    p('knuckle guard',[(-1.08,4.1),(.35,4.0),(1.6,4.35),(2.3,7.65)],.47,.63,'trim' if wraith else 'frame')
    s('pressure gauge',[(.3,2.1,10.8,1.65,1.55)],'inlay')
    m.marker('gauge enamel',1.4,10.95,-.91,'energy',.7,.4)
    m.notch(2.2,15)
    if wraith:
        for side in (-1,1):
            p('Wraith forked laquer',[(-5,9.2),(1.7,9.95),(6.1,9.93),(8.5,10.6)],.38,.13,'trim',side*1.54)
            p('Wraith shroud slash',[(8.6,10.5),(11,9.95),(13.8,10.75)],.32,.18,'trim',side*1.92)
        b('Wraith wraparound gauge',-.1,1.9,10.6,11.1,1.83,'frame')

def brakk(m,kuva):
    b,p,s=m.box,m.path,m.profile
    p('open chrome-backed grip',[(-.1,8.3),(-1.2,3.65),(-.6,3.4)],1.4,1.46,'grip')
    for side in (-1,1):
        p('grip spine',[(-1.04,3.9),(.0,7.8)],.29,.16,'edge',side*.76)
    s('rear housing',[(-2.6,-1.25,9.0,1.8,1.75),(-1.25,2.6,9.25,2.75,2.35),(2.6,4.6,9.28,3.25,2.1)],'shell')
    s('floating shot cradle',[(3.0,5,9.95,3.45,2.4),(5,10.5,9.55,4.3,2.6),(10.5,12.25,9.6,3.85,2.45)],'frame')
    s('split upper jaw',[(3.9,5.2,10.86,2.75,1.68),(5.2,8.4,11.04,3.5,1.95),(8.4,11.5,10.95,4.05,1.98),(11.5,12.5,10.75,3.4,1.56)],'shell',part='action')
    s('underslung knucklebone',[(4.4,6.1,7.65,2.75,2.1),(6.1,9.1,7.95,3.5,1.9),(9.1,11.5,8.1,3.9,1.8),(11.5,12.7,8.36,3.35,1.65)],'shell',part='load')
    s('snub muzzle',[(11.4,12.65,9.62,2.8,1.82)],'shade')
    m.ring('shotgun bore',12.48,12.9,9.62,2.54,1.76,.32,'frame')
    for side in (-1,1):
        p('exposed cartridge feed',[(5,9.05),(8.5,9),(11.4,9.36)],.35,.17,'energy' if kuva else 'trim',side*2.2)
        p('jaw brow',[(6.2,10.86),(8.8,11.28),(11.35,11.0)],.38,.18,'trim' if kuva else 'shade',side*1.88,part='action')
        p('power cable',[(-2.65,8.9),(-2.65,10.05),(-.8,10.6),(.9,10.55)],.36,.4,'trim' if not kuva else 'frame',side*.79)
        for t,y in [(1.75,9.78),(5.7,7.5),(11.4,10.7)]:
            m.marker('latch light',t,y,side*(1.42 if t<3 else 1.8),'energy',.42,.3)
    if kuva:
        for side in (-1,1):
            p('Kuva molten cheek',[(6.4,9.45),(8.85,9.32),(11.78,9.85)],.74,.45,'inlay',side*2.18)
            for j in range(3):
                b('glowing Kuva vent',7.1+j*1.15,7.52+j*1.15,9.09+j*.14,9.6+j*.14,.25,'energy',side*2.44)
    m.notch(.85,10.85,11.7,rear_seat=10.31,front_seat=11.41)

def nukor(m,kuva):
    b,p,s=m.box,m.path,m.profile
    p('reed grip',[(-1.8,7.9),(-2.45,4.7),(-2.2,3.42)],1.03,1.28,'grip')
    s('rear magnet pods',[(-3.5,-2.9,8.4,2.3,1.55),(-2.9,.5,8.7,3.05,2.3),(.5,1.8,8.9,2.25,1.75)],'shell',part='load')
    s('axial support',[(-.8,2.65,9.2,2.15,1.65),(2.65,6.45,9.6,2.5,1.85),(6.45,9.65,9.65,2.0,1.62)],'frame')
    for side in (-1,1):
        p('current loop',[(.0,9.4),(.4,10.5),(2.3,11.0),(4.2,11.08),(5.6,10.4)],.38,.44,'trim',side*.77)
        p('upper swept emitter',[(4.3,10.6),(5.65,12.05),(7.8,12.47),(9.2,11.65),(10.1,10.1)],1.06,.91,'shell',side*1.35)
        p('deep crescent',[(9.9,10.3),(9.2,8.48),(7.15,6.83),(5.82,5.82)],1.3,1.05,'shell',side*1.52)
        p('emitter seam',[(6.45,12.1),(7.75,11.83),(8.55,10.5),(8.25,9.55),(7.5,8.56),(6.55,7.31)],.28,.18,'inlay' if kuva else 'energy',side*2.11)
        p('mandible prong',[(7.35,7.2),(5.6,5.5),(4.75,5.0)],.52,.63,'shade',side*1.35)
        p('separate upper reed',[(-2.9,10.58),(-.8,10.57),(1.15,9.75)],.8,.9,'shell',side*.34,part='load')
        for t,y in [(-1.08,8.9),(5.8,11.1),(7.7,9)]:
            m.marker('phase contact',t,y,side*(1.55 if t<1 else 2.23),'energy',.32,.26)
    m.ring('central lens bezel',8.5,9.2,9.63,2.65,2.8,.38,'frame',part='action')
    s('electromagnetic lens',[(8.76,9.02,9.63,1.7,1.8)],'energy',part='action')
    if kuva:
        for side in (-1,1):
            p('Kuva ceramic secondary lamella',[(7.9,12.08),(9.45,11.42),(10.5,9.4),(9.83,8.25)],.49,.22,'trim',side*2.01)
    # External emitter fins cover the lateral extremes but leave the center of sight entirely clear.
def cedo(m,prime):
    b,p,s=m.box,m.path,m.profile
    s('receiver',[(.3,5,9.35,2.65,2.3),(5,11,9.5,3.8,2.55),(11,13.7,9.64,3.25,2.35)],'frame')
    s('glazed upper canopy',[(1.55,3.45,10.62,1.85,1.3),(3.45,6.05,10.75,3.15,1.6),(6.05,10.95,10.85,3.55,1.65),(10.95,12.85,10.76,2.75,1.4)],'shell',part='action')
    s('shotgun barrel',[(12.45,14.1,9.44,2.15,1.9),(14.1,20.35,9.43,1.4,1.15),(20.35,21.1,9.45,1.8,1.3),(21.1,22.32,9.45,2.45,1.85)],'edge' if not prime else 'trim')
    m.ring('twinned muzzle lips',22.22,22.65,9.45,2.65,1.93,.32,'frame' if not prime else 'trim')
    for side in (-1,1):
        p('underslung barrel runner',[(14.4,9.0),(15.2,8.52),(19.4,8.34),(21.65,8.83)],.4,.48,'edge' if not prime else 'trim',side*.58)
        p('serpentine air valve',[(.68,8.65),(3.8,8.57),(6.8,8.92),(10.1,8.1),(12.3,7.98),(13.36,8.6),(13.15,9.5),(12.3,10.08)],.4,.39,'trim',side*1.94)
        p('second tube',[(3.3,8.88),(6.4,9.17),(9.2,8.78),(11.4,8.33),(12.25,8.5)],.28,.30,'energy',side*1.97)
        p('upper enamel trim',[(2.0,10.76),(4.95,11.23),(10.3,11.55),(12.95,11.09)],.23,.25,'trim' if prime else 'edge',side*1.69,part='action')
    s('rear glaive drum',[(-6.9,-6.2,8.45,2.3,2.45),(-6.2,-3.0,8.56,3.3,3.04),(-3.0,-1.24,8.62,3.25,2.8),(-1.24,-.45,8.77,2.7,2.15)],'grip',part='load')
    for side in (-1,1):
        p('glaive guard arc',[(-7.45,7.23),(-7.02,9.18),(-5.36,10.3),(-2.17,10.55),(1.25,10.61)],.49,.45,'trim' if prime else 'edge',side*1.45)
        for j in range(3):
            m.marker('glaive drum slot',-5.7+j*1.12,8.3,side*1.68,'inlay',.32,.9,part='load')
    p('curving grip',[(2.26,8.1),(1.77,6.8),(1.25,5.86),(1.97,5.46)],.94,1.36,'trim' if not prime else 'shell')
    p('open guard',[(2.8,8.32),(4.26,8.2),(4.3,7.42),(3.12,7.13)],.22,.53,'frame')
    if prime:
        for side in (-1,1):
            p('Prime gilded rear filigree',[(-7.55,7.55),(-5.93,8.4),(-5.1,9.42),(-3.3,9.75),(-2.1,9.49),(-1.32,8.66)],.43,.30,'trim',side*1.77,part='load')
            p('Prime upper fin',[(2,10.5),(4.3,11.7),(7,11.9)],.34,.34,'trim',side*.91,part='action')
            p('Prime muzzle tine',[(19.75,9.15),(21.55,10.35),(23.97,10.7)],.35,.46,'trim',side*.97)
        b('Prime gilded heel',.58,2.75,5.1,5.42,1.53,'trim')
    m.notch(2.35,20.45,11.39,rear_seat=10.93,front_seat=9.88)

def torid(m):
    b,p,s=m.box,m.path,m.profile
    p('rear branch',[(-7,9.65),(-5.5,9.18),(-2.45,9.4),(.2,9.65)],.82,1.3,'shell')
    p('underswept rear spur',[(-6.7,8.43),(-3.1,8.21),(-.65,8.65),(2,8.68)],.69,.9,'inlay')
    s('knotted barrel',[(-1.4,1,9.12,2.45,1.95),(1,4.5,9.25,3.27,2.07),(4.5,8.1,9.35,3.9,2.65),(8.1,11.2,9.5,3.86,2.53),(11.2,13.4,9.6,3.25,2.2)],'shade')
    s('seed organ',[(3.75,5.45,8.7,3.0,2.23),(5.45,8.65,8.94,3.82,2.66),(8.65,11.45,9.13,3.46,2.32)],'inlay',part='load')
    p('curved tooth grip',[(.5,8.6),(.75,7),(.15,5.3),(-.35,4.9)],.85,1.18,'shell')
    for side in (-1,1):
        p('upper chitin stave',[(4.05,9.97),(8.8,10.73),(11.1,11),(14.6,11.15),(20.9,11.11),(22.7,11.41)],.91,.78,'shell',side*.88)
        p('lower chitin stave',[(6.6,8.54),(13.2,8.42),(18.2,8.68),(21.7,8.88)],.75,.9,'shell',side*.9)
        p('upper splinter',[(10.45,10.88),(14.8,11.74),(17.75,11.94)],.32,.33,'trim',side*.7)
        p('outer tendril',[(-3,9.77),(-1.2,10.28),(2.6,9.57),(7,8.97),(9.3,9.48),(11.25,10.19)],.41,.36,'trim',side*1.6)
        for j in range(5):
            t=5.45+j*1.47;y=9.36+(j%2)*.48
            p('lit spore blister',[(t,y),(t+.63,y+.28)],.36,.19,'energy',side*(1.97 if j<4 else 1.64),part='load')
        for j in range(3):
            p('petrified muzzle thorn',[(17.5+j*1.3,11.22),(18.5+j*1.3,11.84)],.22,.29,'edge',side*.74)
    s('spore outlet',[(12,15,9.64,2.3,1.55),(15,17,9.72,1.68,1.23),(17,20.17,9.87,1.23,.91)],'grip',part='action')
    b('toxin pore',20.16,20.24,9.58,10.02,.68,'energy',part='action')

def bubonico(m,coda):
    b,p,s=m.box,m.path,m.profile
    # The cuff around a player's right forearm remains open all the way through the wrist.
    for side in (-1,1):
        p('shoulder carapace',[(-6.75,9.35),(-5.1,11.12),(-2.65,11.81),(.1,11.65),(2.0,10.55),(4.8,10.21)],1.0,1.33,'shell' if coda else 'edge',side*1.8)
        p('lower split cuff',[(-7.0,6.65),(-4.9,6.08),(-2.8,6.47),(.78,7.45),(3.65,9.07)],.9,1.18,'shade',side*2)
        p('forked carapace flange',[(-5.0,8.0),(-3.65,9.78),(-1.4,10.4),(.32,10.35)],.65,.57,'shell' if coda else 'edge',side*2.48)
        p('neon tendon',[(-7.15,7.33),(-5.8,7.05),(-3,7.34),(-1.15,8.09)],.34,.37,'energy',side*2.26,part='load')
        p('needle at wrist',[(-8.86 if coda else -7.95,8.0),(-7.05,7.36),(-5.79,7.44)],.32,.3,'edge' if coda else 'energy',side*1.88,part='load')
        p('underslung mobile talons',[(2.3,8.82),(2.54,7.26),(1.62,5.93),(.9,5.57)],.49,.45,'inlay',side*1.79,part='action')
        p('second under-talon',[(3.64,9.07),(3.7,7.8),(3.06,6.28)],.42,.37,'frame',side*1.59,part='action')
        p('split projectile horn',[(3.09,9.6),(6.7,10.33),(10.4,11.07),(14.1,11.08),(17.76,11.42),(20.52,11.57)],.85,.94,'shade' if coda else 'shell',side*.99)
        p('lower rail fang',[(3.29,9.57),(6.9,9.02),(9.88,8.83),(13.6,9.12),(16.53,10.08),(19.68,11.36)],.64,.68,'shell' if coda else 'trim',side*1.44)
        p('horn tip',[(17.92,11.37),(19.99,11.8),(22.12,11.95)],.31,.47,'edge' if coda else 'energy',side*.98)
        for n,t in enumerate((5.3,7.2,10.25,14.76)):
            m.marker('bioluminescent pores',t,10.21+.23*n,side*1.39,'energy',.34,.21)
    s('spore palm hub',[(.12,2.04,9.76,3.43,2.86),(2.04,4.65,9.53,3.65,2.57),(4.65,6.56,9.67,2.72,2.06)],'frame')
    s('seed sac',[(.45,1.8,9.7,3.68,2.06),(1.8,3.5,9.65,3.83,1.86)],'shade',part='load')
    if coda:
        for side in (-1,1):
            p('Coda crystallized magenta rupture',[(-.64,10.65),(.45,10.19),(1.34,9.07),(2.35,9.88),(3.34,9.55)],.39,.28,'energy',side*1.98,part='load')
            p('Coda beat horn',[(-2.55,11.21),(-4.34,12.15),(-5.67,13.9),(-6.33,15.35)],.51,.57,'shade',side*2.03)
            p('Coda horn hot tip',[(-6.26,15.17),(-6.55,15.82)],.33,.32,'energy',side*2.1)
            p('Coda needle fluke',[(15.36,10.5),(18.43,10.6),(21.49,11.43)],.37,.43,'shell',side*1.87)
    else:
        p('rear shoulder ridge',[(-4.99,11.15),(-3.3,11.95),(-.7,11.92),(1.27,11.31)],.58,2.61,'shade')

def aksondol(m):
    b,p,s=m.box,m.path,m.profile
    # The user-facing dual set draws this single long pistol once for each hand.
    s('folded silver housing',[(-2.8,-.6,9.78,1.36,1.63),(-.6,2.9,10.05,2.05,2.0),(2.9,5.1,10.26,1.96,1.68),(5.1,8.75,10.36,1.46,1.12)],'shell')
    p('ring grip',[(-2.1,9.8),(-1.72,8.6),(-2.49,7.1),(-3.69,6.8)],.92,1.12,'inlay')
    for side in (-1,1):
        p('open swept trigger guard',[(-4.19,7.45),(-3.81,6.89),(-2.4,6.54),(-.23,6.73),(1.0,7.63),(1.25,8.74),(.45,9.85)],.44,.35,'edge',side*.61)
        p('lower gilt arch',[(.0,6.8),(2.36,7.65),(4.47,8.66),(5.86,8.9)],.39,.38,'trim',side*.82)
        p('upper rear horn',[(-3.65,11.58),(-2.83,10.73),(-.8,10.26),(2.05,10.26),(4.50,10.97)],.35,.37,'trim',side*.92)
        p('split silver face',[(.85,9.72),(3.05,10.39),(6.36,10.61),(10.3,10.62)],.29,.25,'edge',side*.83)
    s('ice needle',[(6.4,10.5,10.42,1.29,.94),(10.5,14.2,10.46,1.05,.74),(14.2,18.15,10.5,.75,.52),(18.15,21.8,10.51,.45,.33),(21.8,24.2,10.52,.21,.16)],'energy',part='action')
    p('polished spine',[(5.65,10.81),(12.8,10.94),(18.0,10.73)],.16,.37,'inlay',part='action')
    s('cold round chamber',[(-.3,2.55,9.15,1.47,1.24)],'frame',part='load')
    for side in(-1,1): m.marker('cold charge readout',.65,9.29,side*.79,'energy',.68,.23,part='load')

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('assets',type=Path,help='Path to assets/destiny2mc')
    parser.add_argument('--report',type=Path)
    args=parser.parse_args(); result={}
    groups=[(ignis,['ignis','ignis_wraith']),(brakk,['brakk','kuva_brakk']),(nukor,['nukor','kuva_nukor']),
            (cedo,['cedo','cedo_prime']),(torid,['torid']),(bubonico,['bubonico','coda_bubonico']),(aksondol,['aksondol'])]
    for craft,keys in groups:
        for i,key in enumerate(keys):
            m=Mesh(key)
            if len(keys)==1:craft(m)
            else:craft(m,bool(i))
            m.save(args.assets)
            result[key]={'axis':12.8,'elements':len(m.el),'groups':{g:sum(t==g for t,_ in m.el) for g in ('body','action','load')}}
    if args.report:export_json(args.report,result)
if __name__=='__main__':main()
