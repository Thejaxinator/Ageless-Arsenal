"""Tangled Shore Scorn. Coordinates are in Minecraft pixels, local Y points down.
Joint names shared with ScornPose are the animation contract. Weapon bones are
children of the palm or wrist so an animator can never leave an item in midair.
"""
import math
from PIL import Image, ImageDraw
import random

# Armor, exposed flesh, linen and energy have separate swatches. Emission is limited
# to the actual active chamber, ether cyst, or discharge point.
COAL, IRON, STEEL, LEATHER, STRAP, GOLD, EDGE, RUST, SKIN, LIGHT, SCAR, BONE, CLOTH, SHADE, ARC, ICE, VOID, SOLAR, EMBER, TEAL, HOLE, PURPLE, RED, VIOLET = range(24)
PALETTE = '29292e 424044 77757a 392d30 544144 b98b3f dbb365 83542b b29894 d4bbb2 885859 e0cdb8 816951 50515a 57b8f0 c2eeff af72ff ffeab0 fb913e 678c8b 282126 70607a 974a40 7864b3'.split()
UNITS = {113:'scorn_stalker',114:'scorn_raider',115:'scorn_lurker',116:'scorn_ravager',117:'scorn_chieftain',118:'scorn_screeb',119:'scorn_abomination',133:'fikrul'}

def rod(r,name,parent,start,end,w,d,tile):
    """A continuous cuboid linking two local points (used for chains and straps)."""
    dx,dy,dz=[b-a for a,b in zip(start,end)]
    length=math.sqrt(dx*dx+dy*dy+dz*dz)
    rx=math.degrees(math.atan2(dz,math.hypot(dx,dy)))
    rz=math.degrees(math.atan2(-dx,dy))
    p=r.joint(name,parent,*start,rx,0,rz)
    r.box(p,0,0,0,w,length,d,tile)
    return p

def ring(r,name,parent,where,radius,w,d,tile,count=8):
    """Faceted ring in the local XY plane, each raised rim plate meets its neighbors."""
    bone=r.joint(name,parent,*where)
    for i in range(count):
        a=math.tau*i/count
        p=r.joint(f'{name}_{i}',bone,radius*math.sin(a),-radius*math.cos(a),0,0,0,math.degrees(a))
        r.box(p,0,-w/2,0,2*radius*math.tan(math.pi/count)+.09,w,d,tile)
    return bone

def strap(r,name,parent,a,b,w=.85):
    rod(r,name,parent,a,b,w,.43,LEATHER)

def skull(r,base,chieftain=False,boss=False,lurker=False,stalker=False):
    b,j=r.box,r.joint
    # A flattened iron slat hides the top part of the face. The thin jaw remains
    # visible below it. The helmet's perforations are cold black, never glowing.
    b(base,0,-3.7,-.12,3.65,3.75,3.18,SKIN)
    b(base,0,-.85,-.95,2.27,1.13,2.15,SKIN)
    b(base,0,-1.23,-1.96,2.30,.86,.42,HOLE)
    for s in [-1,1]:
        b(base,s*.85,-1.39,-2.10,.5,.62,.30,BONE)
        b(base,s*.46,-.65,-2.01,.28,.32,.34,BONE)
    b(base,0,-4.36,0,3.37,1.03,3.42,RUST)
    b(base,0,-4.06,-.60,3.48,2.74,2.75,GOLD)
    b(base,0,-4.38,-.51,2.80,.46,2.9,EDGE)
    b(base,0,-1.64,-1.83,2.8,.45,.58,IRON)
    for s in [-1,1]:
        p=j(('l' if s==1 else 'r')+'_cheek',base,s*1.65,-3.8,-.40,-5,0,-s*13)
        b(p,0,0,.07,.78,2.55,2.3,GOLD); b(p,0,2.22,-.26,.60,.75,1.3,RUST)
        for y,x in [(-3.48,1.23),(-2.73,1.52),(-2.08,.93)]:
            b(base,s*x,y,-2.013,.31,.36,.19,HOLE)
        if not lurker:
            paddle=j(('l' if s==1 else 'r')+'_paddle',base,s*1.35,-3.89,.57,-8,s*4,s*(52 if chieftain or stalker else 15))
            b(paddle,0,-2.13,0,.60,2.36,.65,RUST)
            b(paddle,s*.15,-(2.44 if chieftain else 2.78),0,1.66,1.32,.49,GOLD)
            b(paddle,s*.25,-(2.65 if chieftain else 2.95),.04,1.92,.44,.52,EDGE)
    if chieftain:
        b(base,0,-3.47,.81,8.81,.67,.70,RUST)
        b(base,0,-3.76,.81,9.21,.43,.74,GOLD)
    if boss:
        # Forsaken Fikrul carries a tall, split scaffold-shaped coronet. Transverse
        # struts and daylight between them are important at first-person distance.
        for s,side in ((-1,'r'),(1,'l')):
            q=j(side+'_crown_post',base,s*1.90,-3.76,.90,-8,0,s*6)
            b(q,0,-3.80,0,1.03,4.57,1.02,GOLD)
            b(q,0,-4.15,0,1.24,.72,1.12,RUST)
        for i,y in enumerate((-5.58,-7.13)):
            b(base,0,y,1.07,8.1-i*.26,.66,.81,GOLD)
            b(base,0,y-.21,.98,7.88-i*.26,.26,.88,EDGE)

def legs(r,role):
    b,j=r.box,r.joint
    for sign,side in ((-1,'r'),(1,'l')):
        leg=j(side+'_leg','body',sign*2.01,0,.05,-17,0,-sign*14)
        b(leg,0,0,0,2.68,5.90,2.77,SKIN)
        b(leg,0,.15,.1,2.92,1.75,2.97,LEATHER)
        band=j(side+'_thigh_binding',leg,0,3.03,0,9,0,sign*14)
        b(band,0,0,0,2.86,.72,2.92,STRAP)
        shin=j(side+'_shin',leg,0,5.48,-.19,30,0,sign*10)
        b(shin,0,.06,0,2.25,5.43,2.26,SKIN)
        b(shin,0,5.31,-.28,1.94,1.65,2.33,STRAP)
        plated=role in ('chieftain','fikrul','raider') or (role in ('lurker','stalker') and sign==1)
        if plated:
            plate=j(side+'_greave',shin,0,-.14,-.74,-5)
            b(plate,0,0,0,2.62,2.01,1.45,GOLD)
            b(plate,0,1.83,.08,2.26,2.81,1.16,RUST)
            b(plate,0,2.01,-.47,1.65,2.18,.57,GOLD)
            b(plate,0,4.57,0,1.35,.97,.81,GOLD)
            for sx in (-1,1):b(plate,sx*.85,.5,-.68,.36,.39,.30,IRON)
        else:
            for i in range(3):
                band=j(side+'_shin_band'+str(i),shin,0,.63+i*1.57,.01,7,0,sign*(11 if i%2 else -14))
                b(band,0,0,0,2.46,.81,2.48,IRON if role=='ravager' else STRAP)
        foot=j(side+'_foot',shin,0,6.47,-.13,-10)
        b(foot,0,0,-.24,2.16,1.09,2.83,SKIN)
        for k in [-1,1]:
            toe=j(side+'_claw'+str(k).replace('-','n'),foot,k*.64,.46,-.92,7,-k*13)
            b(toe,0,0,-.49,.80,.60,1.59,SKIN);b(toe,0,.1,-1.37,.46,.40,.67,BONE)

def arm(r,role,side,s,lower=False):
    b,j=r.box,r.joint
    # Secondary arms stay behind the rib cage where the references show them.
    prefix=side+('_under' if lower else '_')
    sh=j(prefix+'arm','thorax',s*(3.12 if lower else 3.55),-2.84 if lower else -5.36,1.03 if lower else .10,-2 if lower else -9,s*(18 if lower else 0),s*(-41 if lower else -17))
    w=1.53 if lower else 2.16; length=3.42 if lower else 4.22
    b(sh,0,0,0,w,length,w+.18,SKIN);b(sh,0,.24,-.13,w+.2,1.54,w+.20,LIGHT)
    b(sh,0,length-.84,0,w+.08,.63,w+.29,STRAP)
    if not lower and role in ('raider','chieftain','fikrul','ravager'):
        shoulder=j(side+'_mantle',sh,s*.2,-.37,.08,-8,s*5,-s*9)
        b(shoulder,0,0,0,2.89,1.41,2.86,GOLD if role!='ravager' else IRON)
        b(shoulder,s*.12,-.26,.08,2.34,.51,2.61,EDGE if role!='ravager' else STEEL)
    fore=j(prefix+'forearm',sh,0,length-.26,0,-23 if lower else -21)
    b(fore,0,0,0,w*.85,length-.28,w*.97,SKIN)
    for k,y in enumerate((.56,1.72,length-1.18)):
        b(fore,0,y,0,w+.19,.58,w+.21,(GOLD if role in ('chieftain','fikrul') and k!=1 else STRAP))
    hand=j(prefix+'hand',fore,0,length-.50,0,15)
    b(hand,0,.02,-.08,w*.83,1.04,w*.92,SKIN)
    for k in [-1,1]:
        b(hand,k*w*.24,.83,-.18,w*.28,.85,.64,BONE if role=='ravager' else SKIN)
    return hand

def pistol(r,hand):
    b,j=r.box,r.joint
    p=j('gun',hand,0,.65,-.39,-1)
    b(p,0,-.34,-.08,.75,1.54,.83,LEATHER)
    b(p,0,-1,-1.18,1.72,1.69,3.45,IRON)
    b(p,0,-.58,-3.02,1.92,.97,1.60,GOLD)
    b(p,0,-.73,-3.78,1.39,1.30,.65,COAL)
    b(p,0,-.31,-4.13,.56,.52,.11,VOID)
    b(p,0,-1.20,-1.61,.82,.34,1.91,STEEL)

def crossbow(r):
    b,j=r.box,r.joint
    p=j('gun','r_hand',0,.6,-.22)
    b(p,0,-.6,-1.20,1.26,1.04,5.22,LEATHER);b(p,0,-.89,-1.61,1.13,.75,4.56,IRON)
    b(p,0,-.60,-4.41,1.80,1.49,2.00,GOLD);b(p,0,-.27,-5.66,.74,.62,1.71,VOID)
    for s,side in ((-1,'r'),(1,'l')):
        q=rod(r,side+'_crossbow_limb',p,(s*.57,-.12,-3.94),(s*3.0,-.35,-2.32),.79,1.0,RUST)
        b(q,0,1.93,0,.95,.62,1.15,GOLD)
        rod(r,side+'_crossbow_tendon',p,(s*2.88,-.20,-2.31),(s*.22,-.17,-1.12),.19,.19,VIOLET)

def launcher(r):
    b,j=r.box,r.joint
    p=j('gun','r_hand',0,.5,-1)
    b(p,0,-1,-1.9,2.62,2.02,4.98,IRON);b(p,0,-1.40,-1,2.56,.81,3.1,GOLD)
    for x,y in ((-.79,-.54),(.79,-.54),(0,.51)):
        b(p,x,y,-4.2,1.14,1.06,1.49,RUST);b(p,x,y+.18,-5,.65,.64,.28,ARC)

def shield(r):
    b,j=r.box,r.joint
    p=j('guard','l_hand',0,-.23,-1.27,-12)
    # A short center and two chamfered courses form a heavy convex disc. Rim bolts
    # sit in a second plane forward from the tangential outer segments.
    b(p,0,-4.48,0,5.31,8.96,.85,RUST);b(p,0,-3.69,0,7.40,7.38,.85,GOLD)
    b(p,0,-2.68,0,8.90,5.36,.85,GOLD)
    ring(r,'brass_rim',p,(0,0,-.40),4.24,.83,.83,GOLD,12)
    ring(r,'verdigris_channel',p,(0,0,-.88),3.12,.60,.50,TEAL,12)
    b(p,0,-2.24,-.80,3.83,4.48,1.09,GOLD)
    b(p,0,-1.71,-1.06,4.88,3.42,1.03,GOLD)
    b(p,0,-1.65,-1.39,3.28,3.30,1.04,EDGE)
    for i in range(8):
        a=math.tau*i/8;b(p,3.93*math.sin(a),3.93*math.cos(a)-.18,-.91,.42,.38,.28,IRON)

def censer(r):
    b,j=r.box,r.joint
    chain=j('chain','r_hand',0,.69,-.5)
    for i in range(3):
        p=j('link_'+str(i),chain,0,i*.83,0,0,(i%2)*70,0)
        for s in [-1,1]: b(p,s*.35,.13,0,.26,.77,.28,IRON)
        b(p,0,0,0,.81,.27,.30,STEEL);b(p,0,.79,0,.75,.29,.29,IRON)
    lamp=j('censer',chain,0,2.42,0)
    for s,side in ((-1,'r'),(1,'l')):
        rod(r,side+'_bail',lamp,(0,0,0),(s*1.20,2.03,0),.32,.37,IRON)
    b(lamp,0,1.83,0,2.66,.61,2.64,IRON);b(lamp,0,2.51,0,1.51,2.57,1.51,SOLAR)
    for x in [-1,1]:
        for z in [-1,1]:b(lamp,x*1.06,2.36,z*1.02,.41,2.79,.45,COAL)
    b(lamp,0,4.88,0,2.91,.60,2.82,IRON);b(lamp,0,5.48,0,1.92,.36,1.87,RUST)

def staff(r):
    b,j=r.box,r.joint
    # The staff is upright at rest. A wrist-coupled swing arcs the entire fork;
    # independent coils and a caged source make the Arc head readable when casting.
    p=j('staff','r_hand',0,.45,-.56,4,0,-5)
    b(p,0,-11.66,0,.66,18.66,.71,RUST)
    for y in (-8.01,-1.93,1.71,5.22):b(p,0,y,0,.96,.73,1.01,IRON)
    b(p,0,-11.9,0,1.85,1.85,1.67,GOLD)
    for s,side in ((-1,'r'),(1,'l')):
        rod(r,side+'_staff_tine',p,(s*.69,-11.63,0),(s*1.61,-14.48,.2),.64,.79,GOLD)
        rod(r,side+'_staff_hook',p,(s*1.60,-14.40,.2),(s*.81,-15.09,0),.49,.64,EDGE)
    b(p,0,-14.30,0,.87,1.75,.95,ARC)

def scrapper(r,role):
    b,j=r.box,r.joint
    j('body',None,0,11.25,0)
    b('body',0,-2.20,.13,5.39,3.08,3.68,LEATHER)
    b('body',0,-2.44,-.10,5.71,.81,3.78,STRAP)
    t=j('thorax','body',0,-2.02,.09,13 if role in ('raider','ravager') else 9)
    b(t,0,-5.5,.22,6.74,3.91,4.03,SKIN)
    b(t,0,-2.04,.10,5.14,2.24,3.35,SKIN)
    b(t,0,-5.95,.60,5.66,1.33,3.76,LIGHT)
    for s,side in ((-1,'r'),(1,'l')):
        strap(r,side+'_suspender',t,(s*2.40,-5.64,-1.65),(s*1.75,.35,-1.49),.94)
        strap(r,side+'_backstrap',t,(s*2.37,-5.68,2.13),(s*1.80,-.03,1.92),.85)
        b(t,s*1.42,-1.87,-1.63,.91,.38,.34,RUST)
    b(t,0,-3.30,-1.85,6.67,.87,.56,LEATHER)
    for s,side in ((-1,'r'),(1,'l')):
        p=j(side+'_pectoral',t,s*1.12,-4.95,-1.70,-4,0,-s*9)
        b(p,0,0,0,1.48,1.47,.48,LIGHT)
        b(t,s*.57,-1.92,-1.56,.86,.87,.52,SKIN)
        b(t,s*.53,-.93,-1.51,.75,.63,.49,LIGHT)
    # Ravagers carry a sealed stump, with a deep hollow framed by metal high on
    # the chest. Their head swivel must not turn the entire shoulder collar.
    head=j('head',t,0,-5.72,-.80,-4)
    if role=='ravager':
        b(head,0,-.33,0,2.47,.76,2.46,HOLE)
        ring(r,'neck_brace',t,(0,-4.88,-1.89),1.72,.85,.86,COAL)
        for s,side in ((-1,'r'),(1,'l')):
            q=j(side+'_neck_bar',t,s*1.94,-4.33,.3,-9,0,s*20)
            b(q,0,-1.46,.2,.87,3.36,2.80,IRON)
    else:skull(r,head,role in ('chieftain','fikrul'),role=='fikrul',role=='lurker',role=='stalker')
    if role in ('raider','ravager','chieftain','fikrul'):
        apron=j('apron','body',0,-1.5,-1.91,-8)
        b(apron,-.45,0,0,2.18,4.49,.42,CLOTH if role=='fikrul' else LEATHER)
        b(apron,-.66,4.16,-.02,1.38,2.61,.42,CLOTH if role=='fikrul' else LEATHER)
        b(apron,1.12,.25,.12,1.37,4.78,.45,SHADE)
    if role in ('chieftain','fikrul'):
        for s,side in ((-1,'r'),(1,'l')):
            flap=j(side+'_coat','body',s*2.44,-2.12,1.07,8,s*7,-s*9)
            b(flap,0,0,0,1.93,5.61,1.61,CLOTH);b(flap,s*.15,5.33,-.03,1.45,3.97,1.29,LEATHER)
            b(flap,s*.31,9.08,-.04,.78,1.17,1.01,CLOTH)
            b(flap,-s*.39,.60,-.77,.49,4.62,.27,GOLD)
    legs(r,role)
    for s,side in ((-1,'r'),(1,'l')):
        arm(r,role,side,s)
        if role in ('raider','chieftain','fikrul'):arm(r,role,side,s,True)
        elif role in ('stalker','lurker'):
            b(t,s*2.82,-2.51,.96,1.80,1.39,1.79,SKIN)
            b(t,s*3.26,-2.14,.97,.77,.85,1.47,SCAR)
    if role=='raider':crossbow(r)
    elif role=='ravager':censer(r)
    elif role=='fikrul':staff(r)
    elif role=='chieftain':launcher(r)
    else:pistol(r,'r_hand')
    if role=='lurker':shield(r)

def abomination(r):
    b,j=r.box,r.joint
    j('body',None,0,7,0)
    b('body',0,-4,0,8.81,5.26,6.27,SKIN)
    b('body',0,-3.77,-.15,9.13,1.59,6.53,LEATHER)
    apron=j('apron','body',.54,-2.85,-2.90,-8,0,5)
    b(apron,0,0,0,3.60,5.97,1.00,IRON);b(apron,.02,.81,-.47,.73,4.69,.2,GOLD)
    t=j('thorax','body',0,-3.18,-.28,11)
    b(t,0,-9.13,.92,13.80,7.09,8.47,SKIN)
    b(t,0,-9.91,1.26,9.92,3.42,7.55,SKIN)
    b(t,0,-4.16,.24,10.31,4.47,6.37,SKIN)
    b(t,0,-3.12,-2.99,7.09,3.11,1.31,SCAR)
    strap(r,'chest_belt',t,(-6.47,-5.84,-3.34),(6.3,-3.5,-3.02),1.25)
    for s,side in ((-1,'r'),(1,'l')):
        b(t,s*3.9,-8.65,-2.3,4.83,4.02,3.25,LIGHT)
        b(t,s*2.3,-2.66,-2.83,1.8,1.51,1.82,SKIN)
        for i,(x,y,z) in enumerate(((2.24,-10.20,2.88),(4.46,-9.82,.21),(5.39,-8.93,3.18))):
            n=j(side+'_ether_nodule'+str(i),t,s*x,y,z,-7,s*8,s*15)
            b(n,0,0,0,1.68,1.28,1.7,SCAR);b(n,0,-.43,0,1.32,1.09,1.37,COAL)
        a=j(side+'_arm',t,s*6.85,-7.57,.22,6,0,-s*13)
        b(a,0,-.7,.09,4.67,4.42,4.79,LIGHT);b(a,.07*s,2.42,0,4.05,3.48,4.02,SKIN)
        b(a,0,3.48,0,4.31,1.19,4.35,LEATHER)
        f=j(side+'_forearm',a,0,5.62,-.14,-17)
        b(f,0,0,0,3.44,5.50,3.36,SKIN)
        for i in range(3):
            q=j(side+'_binding'+str(i),f,0,1.21+i*1.16,0,4,0,s*(14 if i%2 else -8))
            b(q,0,0,0,3.93,.88,3.79,STEEL if i==0 else IRON)
        hand=j(side+'_hand',f,0,5.07,0)
        b(hand,0,0,-.33,3.5,2.72,3.55,LIGHT)
        b(hand,-s*1.67,.67,-.73,1.05,1.82,1.31,SKIN)
        for i in range(3):
            b(hand,i-1,2.45,-.52,.83,1.38,1.36,SKIN)
            b(hand,i-1,3.56,-.64,.62,.62,1.01,SCAR)
        b(hand,0,1.11,-2.14,1.08,.46,.15,ARC)
        l=j(side+'_leg','body',s*2.99,0,.02,-9,0,-s*10)
        b(l,0,.03,0,4.40,7.38,4.48,SKIN);b(l,0,.74,0,4.61,1.79,4.66,LEATHER)
        f=j(side+'_shin',l,0,7.06,-.22,18)
        b(f,0,0,0,3.54,7.26,3.83,SKIN);b(f,0,-.23,-.96,3.58,2.98,2.73,LIGHT)
        b(f,0,.41,-2.25,1.86,1.84,.59,SCAR)
        b(f,0,4.98,0,3.82,1.59,3.99,STRAP)
        foot=j(side+'_foot',f,0,7.02,0,-7)
        b(foot,0,.15,-.45,3.71,1.78,4.34,SKIN)
        for sx in [-1,1]:b(foot,sx*.96,1.07,-2.74,1.48,.78,1.53,BONE)
    h=j('head',t,0,-6.70,-3.55,4)
    b(h,0,-1.56,-.14,4.37,4.69,3.36,SCAR)
    b(h,0,1.45,-.95,3.11,1.55,2.77,SKIN)
    for s in [-1,1]:
        cheek=j(('l' if s>0 else 'r')+'_faceplate',h,s*1.25,-2.14,-1.31,6,0,-s*11)
        b(cheek,0,0,0,1.59,3.93,1.45,RUST);b(cheek,0,.20,-.65,.88,2.51,.44,GOLD)
        b(h,s*.85,1.97,-2.08,.53,.6,.38,BONE)
    b(h,0,-2.13,-1.36,.67,3.63,.88,COAL)

def screeb(r):
    b,j=r.box,r.joint
    j('body',None,0,16.95,0)
    t=j('thorax','body',0,0,-.16,-4)
    b(t,0,-2.07,.35,5.53,4.08,8.42,SCAR);b(t,0,-2.44,-.05,4.75,2.22,7.59,SKIN)
    b(t,0,-.67,4.12,3.35,2.59,2.75,SKIN)
    h=j('head',t,0,-.37,-3.71,12)
    b(h,0,-1.57,-.52,3.72,2.98,3.2,SKIN);b(h,0,.36,-2.09,2.48,1.11,1.20,HOLE)
    b(h,0,1.24,-1.98,2.06,.58,1.10,SCAR)
    for s in [-1,1]:
        b(h,s*1.19,-.70,-2.08,.63,.52,.59,BONE)
        b(h,s*.83,.41,-2.65,.4,.67,.30,BONE)
    sacs=j('sacs',t,0,-1.87,.47)
    # Asymmetric tiers of translucent blue-looking bladders, with a slightly lit
    # top face and mauve root. They share one breathing joint.
    for i,(x,z,w) in enumerate(((-1.75,-1.58,2.36),(.68,-2.19,2.20),(1.81,-.28,2.47),(-.53,.60,3.04),(-1.56,2.69,2.34),(1.04,2.63,2.49))):
        p=j('bladder_'+str(i),sacs,x,.1,z,-4*((i%3)-1),0,(i%3-1)*11)
        b(p,0,-w*.34,0,w*.87,w*.54,w*.87,PURPLE)
        b(p,0,-w*.83,0,w*.86,w*.63,w*.84,ARC)
        b(p,0,-w*.98,-.02,w*.57,w*.24,w*.61,ICE)
    for s,side in ((-1,'r'),(1,'l')):
        # Three sets of feet contact the ground on alternating phases. The
        # outstretched hand is distinct from the rear springlike folded limb.
        for prefix,x,y,z,lo,up,rot in (('_front',2.36,-.35,-2.67,3.89,4.95,-44),('_mid',2.66,.33,.14,2.68,3.22,-24),('_',1.57,.16,3.48,3.52,3.88,-31)):
            a=j(side+prefix+'leg','body',s*x,y,z,-15 if prefix=='_front' else 37,s*(18 if prefix=='_front' else -12),s*rot)
            w=1.25 if prefix=='_mid' else 1.69
            b(a,0,0,0,w,up,w*1.06,SKIN);b(a,.13*s,-.23,.03,w*1.24,1.83,w*1.21,LIGHT)
            f=j(side+prefix+'shin',a,0,up-.32,0,-31 if prefix=='_front' else -82,0,s*27)
            b(f,0,-.11,0,w*.83,lo,w*.97,SCAR)
            b(f,0,lo*.37,0,w*.87,.65,w*1.03,SKIN)
            q=j(side+prefix+'foot',f,0,lo-.46,0,18 if prefix=='_front' else 62,0,-s*8)
            b(q,0,0,-.41,w*1.24,.79,1.76,SKIN)
            for k in [-1,1]:b(q,k*.44,.40,-1.29,.39,.39,1.27,BONE)

def build(r):
    k=r.d['kind'];name=UNITS[k]
    r.d.update(texture_height=192,palette=[list(bytes.fromhex(x)) for x in PALETTE],emissive_palette=[ARC,ICE,VOID,SOLAR,EMBER],source='Ageless Arsenals • Tangled Shore Scorn reference study')
    if k==119:abomination(r)
    elif k==118:screeb(r)
    else:scrapper(r,name.removeprefix('scorn_'))
    # Rest-pose geometry scales around the world ground plane. Native walk and
    # hitbox logic remain keyed to the existing entity kind.
    scale={113:1.13,114:1.17,115:1.17,116:1.30,117:1.38,118:1.04,119:1.34,133:1.75}[k]
    for p in r.parts:
        if p['parent'] is None:p['pivot']=[p['pivot'][0]*scale,24+(p['pivot'][1]-24)*scale,p['pivot'][2]*scale]
        else:p['pivot']=[round(x*scale,4) for x in p['pivot']]
        for box in p['boxes']:
            for idx in (0,1):box[idx]=[round(x*scale,4) for x in box[idx]]

def textures(r):
    """Each swatch is painted only with working-size pixels. Scrapes, badly
    pitted ochre metal, and stretched skin receive visibly different marks."""
    base=Image.new('RGB',(512,192));glow=Image.new('RGBA',base.size)
    for i,col in enumerate(r.d['palette']):
        local=Image.new('RGB',(64,64),tuple(col));draw=ImageDraw.Draw(local)
        rng=random.Random(93011+i*193);mat='metal' if i in (IRON,STEEL,GOLD,EDGE,RUST,TEAL) else 'flesh' if i in (SKIN,LIGHT,SCAR) else 'cloth'
        def shade(delta):return tuple(max(0,min(255,int(x+delta))) for x in col)
        for n in range(440):
            x,y=rng.randrange(64),rng.randrange(64);d=rng.choice((-10,-5,-3,4,7))
            draw.point((x,y),fill=shade(d))
        if i not in r.d['emissive_palette']:
            for n in range(105 if mat=='metal' else 83):
                x,y=rng.randrange(64),rng.randrange(64)
                if mat=='metal':
                    draw.line((x,y,x+rng.randrange(1,3),y),fill=shade(-19))
                    draw.point((x,y+1),fill=shade(10))
                elif mat=='flesh':draw.line((x,y,x+1,y+2),fill=shade(-9))
                else:draw.line((x,y,x+1,y+1),fill=shade(-11))
        offset=(i%8*64,i//8*64);base.paste(local,offset)
        if i in r.d['emissive_palette']:glow.paste(local.convert('RGBA'),offset)
    return base,glow
