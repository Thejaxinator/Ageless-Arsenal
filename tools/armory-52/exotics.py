"""Reference-sculpted exotic frames. The sight axis and moving-part names are shared with the runtime."""
import math
from PIL import ImageDraw

DARK, SHELL, TRIM, METAL, ACCENT, RUBBER, EDGE, MARK = range(8)
MODELS = {'izanagis_burden', 'whisper_of_the_worm', 'sweet_business', 'gjallarhorn'}
PALETTES = {
    'izanagis_burden': ('242733','e0e6e2','767f90','85909c','274883','333740','faf9ec','a4b8ca'),
    'whisper_of_the_worm': ('11151b','252b2e','34494b','4c6263','e7ffff','16191c','86bab9','bdeded'),
    'sweet_business': ('262e39','eee8da','b42c32','8e9ba4','ef4a42','242c33','c9d0d2','d7dbdc'),
    'gjallarhorn': ('322e2d','e5e3d5','ba9551','82827c','e7c987','292729','f6efe1','962827')
}
OPTICS = ('optic ', 'scope ', 'ocular ', 'rim bevel', 'hood', 'sill')
# Coordinates below are authored on a 128x128 paint sheet.
BROAD = (1,1,126,30)
CASE = (1,34,87,62)
BADGE = (90,33,126,63)
PORT = (1,69,44,112)
GRIP = (49,69,80,111)
DECAL = (82,69,126,112)

class Sculpt:
    def __init__(self,w):self.w=w
    def box(self,n,x,y,t,width,h,d,mat,angle=0,axis='x',paint=None,end=None,glow=0):
        self.w.box(n,x,y,t,width,h,d,mat,angle,axis)
        e=self.w.parts[-1]
        for face in ('east','west','north','south','up','down'):
            rect=paint if face in ('east','west') else end if face in ('north','south') else None
            if rect:
                a,b,c,d=rect
                if face in ('east','south'):a,c=c,a
                e['faces'][face]={'texture':'#detail','uv':[v/8 for v in (a,b,c,d)]}
            if glow:
                e['faces'][face]['neoforge_data']={'block_light':glow,'sky_light':glow,'ambient_occlusion':False}
    def p(self,n,y,t,width,h,d,mat,*args,**kw):
        self.box(n,0,y,t,width,h,d,mat,*args,**kw)
    def sides(self,n,x,y,t,width,h,d,mat,*args,**kw):
        for sign in (-1,1):self.box(n,sign*x,y,t,width,h,d,mat,*args,**kw)
    def ring(self,n,y,t,r,thick,depth,mat):
        # Eight short plates make an actual open ring around a core or moving barrel cluster.
        half=(r-thick*.5)*math.tan(math.pi/8)
        for i in range(8):
            a=math.pi/4*i
            x=math.sin(a)*(r-thick*.5);dy=math.cos(a)*(r-thick*.5)
            if i%2==0:
                width,height=(2*half+.06,thick) if i%4==0 else (thick,2*half+.06)
                self.box(n,x,y+dy,t,width,height,depth,mat)
            else:self.box(n,x,y+dy,t,2*half+.06,thick,depth,mat,-45 if i%4==1 else 45,'z')

def finish(w):
    if w.id not in MODELS:return
    w.colors=[tuple(bytes.fromhex(c)) for c in PALETTES[w.id]]
    w.parts[:]=[e for e in w.parts if e['name'].startswith(OPTICS)]
    if w.id=='sweet_business':
        w.parts.clear()
    elif w.id in ('izanagis_burden','gjallarhorn'):
        for piece in w.parts:
            if piece['name']=='optic shoe':continue
            h=.78 if w.id=='izanagis_burden' else .70
            for point in [piece['from'],piece['to']]+([piece['rotation']['origin']] if 'rotation' in piece else []):
                point[1]=round(w.axis+(point[1]-w.axis)*h,4)
            if w.id=='izanagis_burden':
                mat=METAL if piece['name'] in ('rim bevel','ocular chamfer','scope fastener') else DARK
                u=(mat%4)*4+.2;v=(mat//4)*8+.25
                for face in piece['faces'].values():face.update(texture='#all',uv=[u,v,u+1.5,v+1.5])
    a=Sculpt(w)
    {'izanagis_burden':izanagi,'whisper_of_the_worm':whisper,'sweet_business':business,'gjallarhorn':gjallarhorn}[w.id](a)

def izanagi(s):
    # The silver/white scabbard wraps the full barrel right up to the swept tsuba.
    s.p('lacquered saya',8.77,7.44,2.23,2.10,15.57,SHELL,paint=BROAD)
    s.sides('folded saya flank',1.04,9.38,7.45,.4,.89,15.52,SHELL,45,'z')
    s.p('polished ridge',9.83,7.67,1.86,.22,15.18,EDGE)
    s.p('steel cutting edge',7.65,7.25,1.77,.29,15.33,METAL)
    s.p('chisel toe',8.79,-.40,2.51,2.49,.43,DARK,end=PORT)
    s.p('upper scabbard horn',9.89,.32,2.38,.27,1.84,DARK)
    s.p('lower scabbard horn',7.71,.32,2.38,.27,1.84,DARK)
    s.p('right angle tang',8.0,16.75,2.39,1.88,4.56,DARK,paint=CASE)
    s.p('underbarrel stop',7.21,11.24,1.23,.67,4.10,DARK)
    s.p('back sight rib',10.11,12.14,1.01,.39,4.96,DARK)
    s.p('guard ferrule',8.83,14.61,2.61,2.64,.62,METAL)
    s.p('sword guard center',8.86,15.34,2.69,3.62,.91,DARK)
    s.p('ivory guard upper curve',10.60,15.11,2.60,1.53,.84,SHELL,22.5)
    s.p('ivory guard tail',6.86,15.08,2.51,1.52,.82,SHELL,-22.5)
    s.sides('painted tsuba',1.38,8.78,15.22,.16,3.21,.75,SHELL,paint=BADGE)
    s.p('guard tip',6.03,15.47,2.47,.86,.66,DARK,22.5)
    s.p('armature spine',9.13,19.45,1.23,.85,8.17,DARK)
    s.p('thin chrome return',9.64,20.49,.73,.25,5.60,METAL)
    s.p('stock strut',7.46,21.06,.86,.61,4.6,DARK,-22.5)
    s.p('stock shoulder',7.69,23.42,1.55,3.41,1.01,DARK)
    s.p('stock spur',6.52,23.23,1.60,.42,1.32,METAL)
    s.p('black armory grip',6.51,18.02,1.52,2.91,1.44,RUBBER,22.5,paint=GRIP)
    s.p('power cassette',6.79,16.27,1.74,2.49,1.63,DARK,paint=CASE)
    s.box('bolt carrier',1.11,9.11,17.60,.44,.45,3.78,METAL)
    s.box('bolt knob',1.43,8.92,18.89,.72,.72,.80,DARK,45,'z')

def whisper(s):
    # Narrow black vertebral column, a thick breech and an almost colorless, bright mouth.
    s.p('spinal barrel',8.68,6.85,1.79,1.80,15.58,DARK,paint=BROAD)
    s.p('muzzle marrow',8.67,.48,2.15,1.94,3.46,ACCENT,end=PORT,glow=13)
    for j,(t,m,sz) in enumerate([(1.8,MARK,2.17),(3.55,EDGE,2.11),(5.25,METAL,2.07),(7.04,TRIM,2.00),(8.96,SHELL,2.01),(10.88,SHELL,2.13),(12.75,DARK,2.24)]):
        s.p('spinal scute',8.71,t,sz,sz*.86,2.34,m,paint=BROAD if j>2 else None)
        for sign in (-1,1):
            y=8.71+sign*.73
            s.box('swept dorsal scale',-.31*(-1 if j%2 else 1),y,t+.33,.48,.37,1.65,m,-22.5*sign)
    for j,(t,z,dy,dx,sign) in enumerate([(-.35,3.8,.65,1.13,1),(-.35,3.4,-.54,.80,-1),
        (.82,4.60,.95,1.13,1),(1.23,3.17,-.75,.98,-1),(1.76,3.14,-.98,.7,-1),(2.78,3.69,.76,.90,1),(3.45,3.05,-.67,1.05,-1)]):
        for side in (-1,1):
            x=dx*side;y=8.70+dy;mul=1 if (j+side)%3 else .83
            s.box('Taken quill',x,y,t,.41,.47,z*mul,ACCENT if j<4 else MARK,22.5*sign,glow=14 if j<4 else 9)
            # The tip follows the projected end of its angled root in the positive-Y/negative-Z direction.
            dist=z*mul*.48
            s.box('Taken quill tip',x,y+sign*dist*.383*(s.w.length/24),t-dist*.924,.17,.18,z*.47,ACCENT,22.5*sign,glow=15)
    s.p('black carapace',8.67,15.7,3.37,3.13,6.16,DARK,paint=CASE)
    s.sides('ribbed breech cheek',1.51,9.10,15.56,.48,1.82,5.05,SHELL,paint=CASE)
    for j in range(3):
        s.sides('receiver barb',1.14,9.62,12.39+j*1.54,.27,.46,2.3,SHELL,-22.5)
    s.p('ossified wrist',6.05,16.19,1.77,3.22,1.98,RUBBER,22.5,paint=GRIP)
    s.p('power cassette',6.97,14.07,2.00,2.02,2.6,SHELL,paint=CASE)
    s.p('bone stock wrist',7.97,18.91,2.10,1.95,2.42,SHELL,-22.5)
    s.p('slotted stock comb',9.04,20.95,2.16,1.48,6.22,DARK,paint=CASE)
    s.p('swept stock heel',7.20,23.15,2.57,3.92,1.50,DARK,22.5)
    s.p('stock hook',6.43,20.84,1.74,.92,4.55,SHELL,-22.5)
    s.p('socket of stock hook',6.49,18.81,1.82,2.36,1.66,DARK,22.5)
    s.box('bolt carrier',1.60,9.43,16.32,.43,.49,3.4,METAL)
    s.box('bolt knob',1.74,9.34,17.41,.43,.76,.7,DARK,45,'z')

def business(s):
    # Six separate silver tubes turn inside a short white engine nacelle.
    for i in range(6):
        a=i*math.tau/6; x=1.27*math.cos(a);y=8.70+1.27*math.sin(a)
        s.box('individual bore',x,y,6.27,.91,.91,13.55,METAL,paint=BROAD)
        s.box('individual bore',x,y,-.14,1.00,1.00,.88,EDGE)
        s.box('dark bore',x,y,-.61,.61,.61,.12,DARK)
    s.p('individual bore',8.7,6.48,1.05,1.05,13.03,DARK)
    for t,mat in ((1.50,METAL),(4.50,TRIM),(11.15,METAL),(12.00,TRIM)):
        s.ring('cast band',8.70,t,2.05,.42,.57,mat)
    s.p('white lower barrel sled',6.89,6.36,1.52,.57,13.07,SHELL)
    s.p('angled front shoe',7.13,.28,1.47,.87,1.09,METAL,22.5)
    # Low open sights follow the red carry spine on the source model.
    s.sides('rear sight ear',.68,11.11,16.32,.31,1.58,.69,TRIM)
    s.p('rear sight bridge',10.58,16.32,1.66,.51,.73,DARK)
    s.p('receiver-mounted front bead',10.84,13.47,.34,1.13,.54,TRIM)
    s.p('drive housing',8.55,14.08,3.78,3.38,4.26,SHELL,paint=CASE)
    s.sides('open drive port',1.93,8.79,14.63,.15,1.55,3.12,DARK,paint=BADGE)
    for j in range(4):
        s.sides('drive heat fin',1.99,8.78,13.37+j*.78,.19,1.43,.34,METAL)
    s.p('stock receiver',8.90,18.93,3.26,2.85,6.0,SHELL,paint=CASE)
    s.p('tapered butt wall',8.80,22.37,2.85,3.14,2.2,SHELL,-22.5)
    s.p('cheek cap',10.42,20.28,2.15,.61,5.92,DARK)
    s.p('scarlet sightline',10.64,16.74,1.24,.34,5.49,TRIM)
    s.p('receiver belly',7.21,16.92,2.10,1.03,5.38,SHELL)
    s.p('loop front',5.56,17.11,1.52,3.31,1.53,DARK,22.5,paint=GRIP)
    s.p('loop heel',5.00,19.81,1.36,.89,5.24,DARK,-22.5)
    s.p('stock web',6.58,21.91,1.48,2.00,1.39,DARK,-22.5)
    s.p('crimson foregrip joint',6.88,10.98,2.89,1.18,1.29,TRIM)
    s.box('crimson foregrip',-.76,5.64,11.23,1.03,2.72,1.03,TRIM,-22.5,'z')
    s.box('power cassette',1.33,8.21,18.65,1.42,2.03,2.62,EDGE,paint=BADGE)

def gjallarhorn(s):
    s.p('launch tube core',8.30,11.74,3.1,3.13,24.51,METAL,paint=BROAD)
    s.p('hammered sleeve',8.33,5.68,3.51,3.55,10.8,METAL,paint=BROAD)
    for t in (.42,8.80,10.25,18.80):
        s.ring('cast collar',8.3,t,2.24,.57,.74,TRIM)
    s.ring('muzzle rim',8.30,-.28,2.24,.52,.65,DARK)
    s.p('muzzle throat',8.30,-.58,3.0,3.02,.18,DARK,end=PORT)
    for side in (-1,1):
        s.box('gold running rail',side*1.88,8.42,5.67,.27,.35,9.85,TRIM)
        s.box('gilded claw tip',side*1.77,9.00,1.82,.42,.65,2.33,ACCENT,22.5)
    s.p('white lower cradle',6.76,9.01,2.70,.83,12.37,SHELL,paint=CASE)
    s.p('gilt keel',6.21,10.95,1.63,.46,9.05,TRIM)
    s.p('wolf mantle bed',10.24,10.65,3.62,1.37,9.77,DARK)
    s.sides('forward edge of ocular cowl',1.41,11.42,12.93,.56,1.30,3.22,SHELL,-22.5)
    s.p('gilt line under aperture',11.33,13.03,2.66,.50,4.11,TRIM)
    s.p('ivory mantle',10.95,11.13,3.21,1.45,8.41,SHELL)
    s.sides('sculpted wolf and wing',1.76,10.65,9.99,.39,1.97,6.04,TRIM,paint=CASE)
    # Orient the engraved head toward the muzzle on both flanks.
    for flank in s.w.parts[-2:]:
        for face_name in ('east','west'):
            uv=flank['faces'][face_name]['uv'];uv[0],uv[2]=uv[2],uv[0]
    s.sides('wolf brow',1.56,11.00,7.39,.76,.85,1.46,ACCENT,-22.5)
    s.p('wolf mask',10.52,6.93,2.95,1.48,1.85,TRIM,paint=CASE)
    s.p('wolf snout',10.04,5.81,1.68,.83,1.81,TRIM,22.5)
    s.p('black wolf nose',9.98,5.04,1.30,.49,.49,DARK)
    s.sides('wolf eye',1.51,10.61,6.56,.22,.25,.60,DARK,-22.5)
    s.sides('wolf ear',1.40,11.24,7.94,.42,.99,.74,TRIM,-22.5)
    for t in (8.47,9.75,11.03):
        s.sides('feather relief',1.94,10.62,t,.20,.30,1.51,ACCENT,-22.5)
    s.sides('red heraldic saddle',1.88,9.60,14.80,.56,2.87,2.98,SHELL,paint=BADGE)
    s.p('rear shaft',8.40,20.20,2.23,2.4,7.25,METAL,paint=BROAD)
    s.p('rear loading collar',8.40,21.81,2.68,2.69,2.15,TRIM,paint=DECAL)
    s.p('launcher grip',5.81,7.41,1.19,3.41,1.17,RUBBER,22.5,paint=GRIP)
    s.p('ivory lower brace',6.40,16.94,1.98,.94,5.32,SHELL,paint=CASE)
    s.p('rear wrist',5.31,15.35,1.54,2.44,1.47,DARK,22.5,paint=GRIP)
    s.p('stock wing',5.36,17.95,1.67,1.23,3.90,SHELL,-22.5)
    s.p('gold heel',4.57,16.48,1.69,.52,1.67,TRIM,-22.5)
    s.box('moving breech latch',1.55,8.55,16.88,.49,.62,1.91,TRIM)

def paint(w,img):
    if w.id not in MODELS:return img
    im=img.copy()
    d=ImageDraw.Draw(im)
    def grad(rect,stops):
        x0,y0,x1,y1=rect
        for y in range(y0,y1+1):
            t=(y-y0)/(y1-y0)
            for (a,c),(b,e) in zip(stops,stops[1:]):
                if a<=t<=b:
                    v=(t-a)/(b-a);color=tuple(round(k+(j-k)*v) for k,j in zip(c,e));break
            d.line((x0,y,x1,y),fill=color)
    def metal(rect,base):
        def shift(delta):return tuple(max(0,min(255,n+delta)) for n in base)
        grad(rect,[(0,shift(-28)),(.14,shift(19)),(.32,shift(3)),(.50,shift(25)),(.62,shift(-5)),(1,shift(-37))])
    def pin(x,y):
        d.rectangle((x,y,x+2,y+2),fill='#414751');d.point((x,y),fill='#cfd1bf')
    def hatch(rect):
        x,y,x1,y1=rect;d.rectangle(rect,fill='#292d30')
        for xx in range(x-32,x1,6):
            for yy in range(y+2,y1-1):
                pos=xx+(yy-y)//2
                if x+1<pos<x1-1:d.point((pos,yy),fill='#575854')
    def ring(rect,dark,metallic):
        x,y,xx,yy=rect
        d.rectangle(rect,fill=dark)
        d.ellipse((x+1,y+1,xx-1,yy-1),fill=metallic)
        d.ellipse((x+4,y+4,xx-4,yy-4),fill='#262929')
        d.ellipse((x+9,y+9,xx-9,yy-9),outline='#5a5f60',width=2)
        d.ellipse((x+11,y+12,xx-12,yy-12),fill='#101419')
    hatch(GRIP)
    if w.id=='izanagis_burden':
        metal(BROAD,(215,219,214));metal(CASE,(44,49,57));metal(BADGE,(190,197,198))
        d.line([(1,6),(126,6)],fill='#f3f2ea')
        d.line([(3,22),(120,22),(126,20)],fill='#838b90')
        d.line([(3,25),(124,25)],fill='#49545d')
        blue='#324c7f';pale='#7a94a5'
        # Painted blue enamel broken by the bright longitudinal crease in the saya.
        d.polygon([(44,5),(56,9),(61,14),(57,18),(49,20),(50,16),(45,18),(47,13),(44,11)],fill=blue)
        d.polygon([(70,4),(83,9),(89,15),(86,23),(78,24),(82,19),(70,18),(73,15),(83,14),(77,10),(72,12)],fill=blue)
        d.polygon([(63,6),(66,8),(63,12),(65,17),(60,20),(55,20),(60,16),(60,12)],fill=blue)
        for x in (34,38,94,98):
            d.line((x,8,x-2,19),fill=blue,width=2)
        for x in (5,20,104,119):
            pin(x,4);pin(x,24)
        for x in (9,12,15,24,27,103,107,115):
            d.line((x,15,x+1,9),fill=pale)
        d.line((4,12,122,12),fill='#e4e6e0')
        d.line((3,20,122,20),fill='#bbc4be')
        for y in (38,43,49,55):d.line((98,y,121,y+2),fill=blue,width=2)
        ring(PORT,'#202627','#aeb9b6')
    elif w.id=='whisper_of_the_worm':
        metal(BROAD,(27,39,40));metal(CASE,(26,30,36));metal(BADGE,(48,56,57))
        # Irregular flecks mimic the marks in the shadowed barrel. Leave them unevenly spaced.
        for j,x in enumerate((6,13,28,49,65,87,93,111,121)):
            y=6+(j*7)%17
            d.polygon([(x,y),(x+1,y-2),(x+2,y-1),(x+1,y+2)],fill=('#799294' if j%3==0 else '#416365'))
        for x in (6,22,38,56,69):
            d.line([(x,58),(x+3,54),(x+8,52),(x+14,40)],fill='#384647',width=2)
            d.line([(x+4,55),(x+9,54),(x+16,48)],fill='#1b2328')
        ring(PORT,'#b6dfdc','#eefcf7')
        d.ellipse((10,78,34,101),fill='#e2f8ed')
        d.ellipse((17,86,27,96),fill='#a4c6c8')
    elif w.id=='sweet_business':
        metal(BROAD,(139,150,159));metal(CASE,(218,219,214));metal(BADGE,(73,81,89))
        for y,color in ((3,'#d7dedc'),(8,'#a9b6b6'),(15,'#58636e'),(19,'#65717a'),(25,'#afb6b6')):
            d.line((1,y,126,y),fill=color)
        d.line([(6,54),(34,54),(36,49),(57,49)],fill='#91999e',width=2)
        d.line([(42,40),(79,40)],fill='#f6f0e4',width=2)
        d.polygon([(56,43),(77,43),(75,46),(55,46)],fill='#3a414b')
        d.polygon([(50,42),(52,42),(51,49),(49,49)],fill='#b52b32')
        for x in (6,81):pin(x,40)
        for x in (96,101,111,116):d.line((x,37,x-3,58),fill='#a8b3b6',width=2)
    else:
        metal(BROAD,(115,114,105));metal(CASE,(191,155,84));metal(BADGE,(226,221,205));metal(DECAL,(180,143,75))
        # The barrel is a twisted silver casting. Each bevel gets a shadow and a narrow silver edge.
        for x in range(-20,129,20):
            points=[(x+int((y-1)*.75),y) for y in range(1,31) if 1<=x+int((y-1)*.75)<=126]
            if len(points)>1:
                d.line(points,fill='#454847',width=4)
                d.line([(min(126,a+3),b) for a,b in points],fill='#dddcc7',width=2)
        # Profile of the embossed winged wolf on the ivory upper cowl.
        d.rectangle(CASE,fill='#dbd9c9')
        wing=[(13,51),(25,40),(36,38),(49,39),(80,35),(73,40),(81,39),(71,45),(77,43),(64,51),(66,51),(48,58),(33,60),(18,57)]
        d.polygon(wing,fill='#8b6a37',outline='#53452d',width=1)
        for j in range(5):
            start=(28+j*3,53+j%2);end=(70+j*2,38+j*2)
            d.line([start,(44+j*3,51-j),end],fill='#dfc28a',width=2)
        d.polygon([(4,53),(9,48),(13,43),(18,42),(18,37),(23,40),(26,36),(27,43),(33,46),(26,50),(23,56),(17,60),(11,59),(12,55),(5,56)],fill='#bda05d',outline='#58442c')
        d.line([(5,53),(13,51),(16,48),(19,48)],fill='#f0d195',width=2)
        d.line([(14,51),(18,50)],fill='#282a29',width=2)
        d.line([(8,56),(16,54),(20,58)],fill='#6b5234')
        for x in (95,104):
            d.polygon([(x,34),(x+5,34),(x+12,56),(x+13,62),(x+7,60)],fill='#a62a29')
            d.line([(x,35),(x+6,51)],fill='#ce5947')
        for x in range(86,122,8):
            d.line([(x,94),(x+4,83),(x+7,90),(x+3,100)],fill='#e3c683')
        ring(PORT,'#4c4c46','#968b70')
    return im
