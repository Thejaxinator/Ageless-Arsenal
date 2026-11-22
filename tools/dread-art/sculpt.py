"""Ageless Arsenals Dread sculptures. Y points down, front is -Z and sockets use local coordinates."""
from math import atan2, hypot, degrees
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT / 'tools'))
from rig_tools import Rig

# These indices are shared across the roster so material behavior remains consistent while hues change.
INK, RUBBER, CLOTH, DEEP, PLATE, EDGE, STEEL, PEARL, BONE, BRASS, GOLD, LIGHT, POWER, GLOW, TRIM, MARK = range(16)
BASE = '16151d 26232c 35303a 3c3443 554858 817384 676974 d5d0ca b3a9ae 8b6550 e4a64f fff1bf 6f486a bb7395 362535 988181'
def palette(**overrides):
    colors = BASE.split()
    for key, color in overrides.items(): colors[globals()[key]] = color
    return [list(bytes.fromhex(c)) for c in colors]

def slab(r,n,rows):
    for row in rows:r.box(n,*row)

def bar(r,name,parent,start,end,width,depth,color):
    """Orient the local positive Y axis down a rail, tendon or spoke joining two sockets."""
    x,y,z=start;dx,dy,dz=[b-a for a,b in zip(start,end)]
    length=hypot(hypot(dx,dy),dz)
    bone=r.joint(name,parent,x,y,z,degrees(atan2(dz,hypot(dx,dy))),0,-degrees(atan2(dx,dy)))
    r.box(bone,0,0,0,width,length,depth,color)
    return bone

def tip(r,n,x,y,z,width,length,depth,mat, end=0.16):
    """Taper away from the base. A negative length points the tip upward."""
    for i in range(4):
        a=1-(1-end)*i/3
        y0=y+length*i/4
        r.box(n,x,min(y0,y0+length/4)-.02,z,width*a,abs(length)/4+.08,depth*a,mat)

def cap(r,n,x,y,z,w,h,d,m):
    r.box(n,x,y,z,w*.74,h,d,m)
    r.box(n,x,y+h*.12,z,w,h*.76,d*.80,m)

def digit(r,name,parent,x,y,z,sgn,mat):
    finger=r.joint(name,parent,x,y,z,-14,0,sgn*7)
    r.box(finger,0,0,0,.47,1.30,.65,mat)
    r.box(finger,0,1.13,-.18,.39,.72,.46,mat)

def boot(r,parent,s,w,z,model):
    # Separate toes and a high heel give the knuckled Dread feet a characteristic forward sweep.
    cap(r,parent,0,0,z,w,1.1,w*1.30,DEEP)
    for i in (-1,1):
        toenail=r.joint(parent+('_outer' if i==s else '_inner'),parent,i*w*.21,.47,z-.62,0,-i*10,0)
        cap(r,toenail,0,0,-.28,w*.4,.72,w*.94,EDGE if model=='rhulk' else PLATE)

def limbs(r,*,hip,shoulder,thigh,shin,upper,lower,arm,leg,family):
    """Set length and pivot for each species; the hand remains the sole parent of held equipment."""
    for s,side in ((-1,'r'),(1,'l')):
        a=r.joint(side+'_arm','body',s*shoulder[0],shoulder[1],shoulder[2],3,0,-s*9)
        cap(r,a,0,0,0,arm,upper*.84,arm*.98,PLATE)
        cap(r,a,0,upper*.57,0,arm*.76,upper*.44,arm*.76,RUBBER)
        f=r.joint(side+'_forearm',a,0,upper,0,-12,0,s*5)
        cap(r,f,0,0,0,arm*.88,lower*.96,arm*.92,CLOTH)
        cap(r,f,0,lower*.13,-.20,arm*.89,lower*.50,arm,PLATE)
        h=r.joint(side+'_hand',f,0,lower,0)
        cap(r,h,0,-.25,0,arm*.73,1.64,arm*.68,DEEP)
        for k in range(3):digit(r,side+'_digit_'+str(k),h,(k-1)*arm*.2,1.01,-.15,s,INK)
        r.box(h,-s*arm*.35,.44,-.23,.56,1.23,.61,DEEP)
        legroot=r.joint(side+'_leg','body',s*hip[0],hip[1],hip[2],-6,0,-s*4)
        cap(r,legroot,0,0,.04,leg,thigh*.80,leg*1.04,PLATE)
        cap(r,legroot,0,thigh*.55,.05,leg*.76,thigh*.46,leg*.72,CLOTH)
        knee=r.joint(side+'_shin',legroot,0,thigh,0,12)
        cap(r,knee,0,0,.03,leg*.82,shin*.91,leg*.77,DEEP)
        cap(r,knee,0,shin*.12,-.34,leg*.82,shin*.48,leg*.78,PLATE)
        # The boot pivots at the ankle. Extend a narrow tendon through its full range of knee flexion.
        cap(r,knee,0,shin*.68,.05,leg*.54,shin*.32+.48,leg*.55,RUBBER)
        toe=r.joint(side+'_foot',knee,0,shin,0,-6)
        boot(r,toe,s,leg*.75,-.72,family)

def resonator(r,parent, width, y, depth, rows, mat=GOLD):
    """Exposed ribbed resonance cavity. Upper and lower bone lips surround brighter saddles."""
    cap(r,parent,0,y,depth,width,rows*.62+1,1.30,BRASS)
    for i in range(rows):
        span=width*(.63+.27*(1-abs((i+.5)/rows*2-1)))
        r.box(parent,0,y+.34+i*.61,depth-.72,span,.37,.37,mat if i!=rows//2 else LIGHT)

def tri_eye(r,n,z,y):
    # Octagonal front-facing lens. The cross-section is drawn in XY and separated along Z.
    for width,height,depth,material in [(2.30,2.30,0,INK),(1.90,1.90,-.30,BRASS),
                                          (1.48,1.48,-.47,INK),(1.15,1.15,-.63,GOLD)]:
        for row,span in enumerate((.54,.84,1,.84,.54)):
            r.box(n,0,y+(2.3-height)/2+row*height/5,z+depth,width*span,height/5+.02,.41,material)
    r.box(n,-.17,y+.85,z-.91,.44,.49,.17,LIGHT)

def foregun(r):
    g=r.joint('gun','r_hand',0,.50,-.05)
    cap(r,g,0,-1.15,0,1.20,2.7,1.18,RUBBER)
    cap(r,g,0,-2.20,-1.80,2.83,2.14,5.52,INK)
    for s,side in ((-1,'r'),(1,'l')):
        rail=r.joint('fork_'+side,g,s*.52,-1.02,-4.01,-89,0,s*5)
        tip(r,rail,0,0,0,.85,4.8,1.30,DEEP)
        for i in range(4):r.box(rail,0,.38+i*.74,-.64,.69,.34,.36,BRASS)
        sp=r.joint('talon_'+side,g,s*.89,-1.90,-1.0,-20,0,s*11)
        tip(r,sp,0,0,0,.61,-2.3,.63,INK)
    r.box(g,0,-1.85,-5.9,.62,.47,3.65,GOLD)

def acolyte(r,stasis):
    # The cloned Psion has a high squared helmet and a circular face port, a very different head from a Subjugator.
    limbs(r,hip=(2.01,-12.6,.25),shoulder=(3.32,-23.3,.12),thigh=5.9,shin=5.4,upper=4.55,lower=4.35,arm=2.30,leg=2.92,family='psion')
    cap(r,'body',0,-24.05,.12,6.53,7.8,4.1,DEEP)
    cap(r,'body',0,-17.1,.10,4.37,4.8,3.48,CLOTH)
    for s,side in ((-1,'r'),(1,'l')):
        breast=r.joint(side+'_chest_wrap','body',s*1.44,-22.8,-1.62,-6,0,s*24)
        cap(r,breast,0,0,0,2.97,5.1,1.60,PLATE)
        for idx in range(3):
            rib=r.joint(side+'_lamellar_'+str(idx),'body',s*(1.12+.32*idx),-20.34+idx*.82,-1.78,5,0,-s*41)
            cap(r,rib,0,0,0,.48,1.89,.56,EDGE)
        hip=r.joint(side+'_hip_wrap','body',s*1.92,-15.01,0,1,s*5,-s*15)
        tip(r,hip,0,0,0,2.20,4.55,3.15,PLATE)
        mantle=r.joint(side+'_shoulder_flare',side+'_arm',s*.19,-1.39,.12,0,0,-s*17)
        cap(r,mantle,0,0,.15,3.49,2.52,3.99,PLATE)
        c=r.joint(side+'_scapula','body',s*2.6,-24,1.86,18,0,s*21)
        tip(r,c,0,0,0,2.22,-3.45,2.20,DEEP)
        for i in range(5):
            # The forearms are wrapped in parallel Strand cable or faceted with diagonal ice.
            b=r.joint(side+'_wrap_'+str(i),side+'_forearm',0,.22+i*.60,-.12,0,0,s*(16 if stasis else -14))
            cap(r,b,0,0,0,2.20-i*.08,.42,2.13-i*.07,EDGE if i%2==0 else PLATE)
            if stasis and i%2==0:tip(r,b,s*.75,.02,.42,.58,-1.27,.57,POWER)
    h=r.joint('head','body',0,-24.25,-.57)
    cap(r,h,0,-4.2,.0,3.38,4.51,3.38,PLATE)
    cap(r,h,0,-2.1,-.78,2.47,2.74,2.53,EDGE)
    cap(r,h,0,-8.47,.26,2.34,5.28,2.45,PLATE)
    r.box(h,0,-7.98,-.98,.47,3.82,.39,EDGE)
    for s,side in ((-1,'r'),(1,'l')):
        brow=r.joint(side+'_visor_flange',h,s*.70,-3.67,-1.62,9,s*9,s*47)
        tip(r,brow,0,0,0,.80,-2.58,.87,EDGE)
        if stasis:
            for i in range(3):
                ice=r.joint(side+'_crown_shard_'+str(i),h,s*(.92+i*.32),-3.42+i*.20,i*.39,12+i*12,s*7,s*(23+i*14))
                tip(r,ice,0,0,0,.64,-(2.25+i*.36),.66,POWER)
    tri_eye(r,h,-1.76,-3.26)
    r.joint('tail','body',0,-13.6,1.34,13);tip(r,'tail',0,0,0,2.34,5.51,.62,DEEP)
    foregun(r)

def subjugator(r,stasis):
    limbs(r,hip=(2.2,-17.2,.10),shoulder=(4.75,-30.5,.13),thigh=8.9,shin=7.2,upper=6.28,lower=5.33,arm=2.86,leg=3.49,family='subjugator')
    cap(r,'body',0,-31.2,0,8.7,8.9,4.52,CLOTH)
    cap(r,'body',0,-23.30,0,5.4,7.22,3.82,DEEP)
    # Split white ceramic collar wraps inward at the throat like folded paper; dark trenches remain between leaves.
    for s,side in ((-1,'r'),(1,'l')):
        p=r.joint(side+'_ivory_cuirass','body',s*2.13,-31.14,-1.67,-7,-s*7,-s*20)
        cap(r,p,0,0,0,4.51,7.38,2.02,PEARL)
        for i in range(3):
            leaf=r.joint(side+'_gorget_leaf_'+str(i),'body',s*(1.22+.91*i),-30.85-.39*i,-1.19+.41*i,5,0,s*(31+i*10))
            tip(r,leaf,0,0,0,1.75,-(3.02+.51*i),1.27,PEARL)
            r.box(leaf,0,-1.92,-.64,.19,1.52,.18,BONE)
        for i in range(2):
            rib=r.joint(side+'_ribbon_'+str(i),'body',s*1.75,-24.42+i*2.1,-1.76,0,0,-s*23)
            cap(r,rib,0,0,0,2.23,2.97,1.28,BONE)
        a=side+'_arm';cap(r,a,0,-.83,.09,4.35,3.53,4.48,PLATE)
        for i in range(3):
            seam=r.joint(side+'_vambrace_'+str(i),side+'_forearm',0,.6+i*1.17,0,-4,0,0)
            cap(r,seam,0,0,0,2.98,.41,2.84,BRASS if i==2 else EDGE)
        if stasis:
            for i in range(4):
                spike=r.joint(side+'_glacial_fan_'+str(i),a,s*(.58+i*.28),-.85,.19+i*.24,7+i*11,0,s*(35+i*13))
                tip(r,spike,0,0,0,.66,-(3.39+i*.45),.74,POWER)
        else:
            crest=r.joint(side+'_shell_lip',a,s*.91,-1.47,-.15,-3,0,s*24)
            cap(r,crest,0,0,0,2.52,3.62,4.27,EDGE)
            for i in range(3):r.box(a,s*(1.00+i*.21),.42+i*.62,-1.88,.94,.30,.48,POWER)
        coat=r.joint(side+'_coat','body',s*2.45,-18.31,1.40,9,0,-s*5)
        tip(r,coat,0,0,0,2.80,11.53,.89,CLOTH,.65)
    cap(r,'body',0,-21.47,-1.73,3.15,4.82,1.44,PEARL)
    panel=r.joint('tabard','body',0,-17.5,-1.37,-4)
    r.box(panel,0,0,0,2.12,9.65,.65,PEARL)
    r.box(panel,-.72,.20,-.35,.26,9.13,.22,BONE)
    h=r.joint('head','body',0,-30.72,-.21)
    cap(r,h,0,-4.63,0,2.62,5.24,2.47,INK)
    if stasis:
        for s,side in ((-1,'r'),(1,'l')):
            blade=r.joint(side+'_mitre',h,s*.62,-3.98,.15,-6,0,s*23)
            tip(r,blade,0,0,0,1.50,-3.74,2.52,DEEP,.85)
    else:
        cap(r,h,0,-7.26,.03,2.36,4.32,2.46,DEEP)
        r.box(h,0,-6.54,-1.28,.29,3.91,.14,BRASS)
    r.box(h,-.37,-3.94,-1.30,.88,.16,.16,BONE)
    glaive(r,'r_hand',False,True,stasis)

def glaive(r,parent,big,straight,ice=False):
    """Bent climbing scythe or long crystalline staff. Built in the grip frame for all attack poses."""
    g=r.joint('weapon',parent,0,.32,0,42 if straight else 34,0,-26 if straight else -40)
    r.box(g,0,-9.9,0,.72,18.68,.76,STEEL)
    for k in range(7):r.box(g,0,-2.03+.59*k,0,.9,.28,.92,RUBBER)
    cap(r,g,0,7.98,0,.92,1.63,.91,BRASS)
    cap(r,g,0,-10.78,0,1.51,2.03,1.48,DEEP)
    if straight:
        blade=r.joint('glaive_blade',g,0,-10.43,-.07,-8,0,11)
        tip(r,blade,0,0,0,2.40,-7.25,.92,POWER if ice else EDGE)
        tip(r,blade,-.63,-.27,.04,.81,-4.81,1.05,STEEL)
        for k in range(3):r.box(blade,0,-1.08-k*1.09,-.52,.34,.61,.19,GLOW if ice else BRASS)
    else:
        # Asymmetric hand-forged hook, thick near the neck and thinning out over the head.
        coords=[(0,-10.32,0),(2.65,-12.03,0),(5.72,-12.91,0),(8.22,-12.15,-.06),(10.05,-10.41,-.05)]
        for i,(a,b) in enumerate(zip(coords,coords[1:])):
            rail=bar(r,'scythe_arc_'+str(i),g,a,b,1.77-i*.33,1.35-i*.23,PLATE if big else DEEP)
            r.box(rail,-.65+i*.13,.19,-.47, .29,1.68-i*.15,.38,EDGE)
        outer=r.joint('scythe_needle',g,10.05,-10.41,-.05,0,0,-23)
        tip(r,outer,0,0,0,.57,2.79,.49,EDGE)
def husk(r):
    limbs(r,hip=(2.27,-13.34,.17),shoulder=(4.52,-23.20,.26),thigh=6.46,shin=5.72,upper=4.92,lower=5.05,arm=2.73,leg=3.44,family='husk')
    cap(r,'body',0,-24.38,.15,8.64,6.85,5.37,DEEP)
    cap(r,'body',0,-18.36,.08,6.08,4.36,4.80,BRASS)
    resonator(r,'body',5.97,-18.60,-2.12,6)
    resonator(r,'body',5.05,-20.31,2.52,4)
    cap(r,'body',0,-14.47,.02,5.81,2.68,4.56,PLATE)
    for s,side in ((-1,'r'),(1,'l')):
        for i in range(3):
            ch=r.joint(side+'_carapace_ridge_'+str(i),'body',s*(2.78-i*.37),-23.82+i*1.43,-1.94,-7,0,-s*(22+i*5))
            cap(r,ch,0,0,0,2.45,1.4,1.63,PLATE if i!=1 else EDGE)
    for i in range(3):
        t=r.joint('hanging_sash_'+str(i),'body',(i-1)*1.56,-13.37,1.73,8+i*4,0,(i-1)*6)
        tip(r,t,0,0,0,1.81,8.8+i*.75,.70,DEEP,.60)
    h=r.joint('head','body',0,-23.71,-1.30,6)
    cap(r,h,0,-3.68,.12,4.87,4.25,3.53,INK)
    # A faceted hood arches across the face; its mouth and helmet never gain anthropomorphic eyes.
    for s,side in ((-1,'r'),(1,'l')):
        brow=r.joint(side+'_hood',h,s*1.46,-4.07,-.05,6,s*11,s*38)
        cap(r,brow,0,0,0,2.45,3.83,3.96,PLATE)
        horn=r.joint(side+'_crown_hook',h,s*2.11,-3.25,.47,9,0,s*50)
        tip(r,horn,0,0,0,1.85,-3.72,2.14,EDGE)
        cap(r,side+'_arm',s*.26,-.78,0,3.57,3.49,4.02,PLATE)
        sp=r.joint(side+'_shoulder_barb',side+'_arm',s*.70,-.09,.48,13,0,s*50)
        tip(r,sp,0,0,0,1.59,-3.45,1.76,PLATE)
        B=r.joint(side+'_blade',side+'_hand',0,.23,-.62,-77,s*9,s*7)
        cap(r,B,0,-.25,0,1.27,2.22,1.42,STEEL)
        tip(r,B,0,1.65,.03,1.44,7.30,1.38,DEEP)
        tip(r,B,-s*.46,1.89,-.41,.52,6.31,.44,GOLD)
        thigh=r.joint(side+'_cuisses',side+'_leg',s*.68,-.56,.19,5,0,-s*12)
        tip(r,thigh,0,0,0,2.34,5.38,3.63,EDGE)
    cap(r,h,0,-2.80,-1.89,1.80,2.27,.52,BRASS)
    r.box(h,0,-2.19,-2.26,.67,1.09,.34,GOLD)

def torment(r,boss):
    # Both units have the low central face/chest and huge shoulders of an inverted triangle.
    # Red musculature and open prismatic wings distinguish Nezarec from the smaller armored Tormentor.
    limbs(r,hip=(2.65,-14.52,.52),shoulder=(6.22,-27.61,-.04),thigh=7.24,shin=6.20,upper=7.07,lower=6.23,arm=4.10,leg=4.35,family='tormentor')
    cap(r,'body',0,-27.72,.18,11.84,7.85,6.57,PLATE)
    cap(r,'body',0,-21.22,.29,8.19,6.77,5.47,DEEP)
    cap(r,'body',0,-16.18,.44,6.58,3.36,5.37,RUBBER)
    resonator(r,'body',8.29,-22.98,-2.78,6)
    spine=r.joint('spinal_ridge','body',0,-29.92,2.48,8)
    for i in range(6):cap(r,spine,0,i*1.55,0,2.28-i*.16,1.31,2.28-i*.12,DEEP)
    h=r.joint('head','body',0,-27.14,-2.42,8)
    if boss:
        cap(r,h,0,-2.14,.27,3.12,3.25,2.95,INK)
        tip(r,h,0,-1.67,.14,2.83,-6.68,2.27,STEEL,.18)
        for s,side in ((-1,'r'),(1,'l')):
            horn=r.joint(side+'_mask_prong',h,s*.89,-1.75,.10,2,0,s*20)
            tip(r,horn,0,0,0,.63,-3.13,.83,INK)
            r.box(h,s*.64,-.92,-1.23,.43,.39,.23,GLOW)
    else:
        cap(r,h,0,-2.27,.33,2.67,3.29,2.51,INK)
        tip(r,h,0,-1.47,-1.05,1.06,3.10,.54,BRASS)
        r.box(h,0,-1.50,-1.23,.44,2.36,.25,LIGHT)
    for s,side in ((-1,'r'),(1,'l')):
        trap=r.joint(side+'_trapezius','body',s*3.01,-27.18,-.13,1,0,-s*33)
        cap(r,trap,0,0,0,3.93,5.86,4.63,PLATE)
        a=side+'_arm'
        cap(r,a,s*.36,-1.85,.14,5.92,4.80,6.38,PLATE)
        for i in range(3):
            band=r.joint(side+'_deltoid_fold_'+str(i),a,s*(.65+i*.17),-.25+i*.80,-2.60+.13*i,0,0,-s*17)
            r.box(band,0,0,0,3.97-i*.51,.34,.49,EDGE)
        scar=r.joint(side+'_weak',a,s*.48,-1.0,-2.73,-7,0,-s*26)
        cap(r,scar,0,0,0,2.25,1.99,1.48,BRASS)
        cap(r,scar,0,.22,-.66,1.52,1.28,.52,LIGHT)
        for i in range(3):
            spur=r.joint(side+'_scapular_spike_'+str(i),'body',s*(3.06+i*1.22),-27.58-i*.30,2.17+.24*i,16+i*5,0,s*(16+16*i))
            tip(r,spur,0,0,0,1.55-i*.16,-(3.36+i*.85),1.66-i*.14,DEEP)
        shin=side+'_shin'
        cap(r,shin,s*.20,.38,-.90,3.14,3.87,2.49,STEEL)
        fore=side+'_forearm'
        for i in range(2):cap(r,fore,s*.13,1.62+i*1.42,0,3.85-.32*i,.54,3.89-.24*i,STEEL)
        if boss:
            solarwing(r,s,side)
    tab=r.joint('tabard','body',0,-15.71,2.04,8);tip(r,tab,0,0,0,3.57,8.34,.71,CLOTH,.38)
    glaive(r,'r_hand',boss,False)

def solarwing(r,s,side):
    """Individual back spines define Nezarec's Light wings, with visible space between ribbons."""
    root=r.joint(side+'_wing','body',s*3.90,-28.30,2.88,12,0,s*8)
    for i,(dx,dy,dz) in enumerate(((5,-11,1),(9,-13,3),(13,-11,5),(14,-7,6),(14,-2,4))):
        mid=(s*dx*.46,dy*.60,dz*.39)
        shaft=bar(r,side+'_root_quill_'+str(i),root,(0,.12*i,.29*i),mid,1.12,.94,POWER if i%2==0 else MARK)
        bar(r,side+'_flight_quill_'+str(i),root,mid,(s*dx,dy,dz),.67,.66,GLOW if i%2 else EDGE)
        end=r.joint(side+'_ribbon_tip_'+str(i),root,s*dx,dy,dz,34,0,s*(56+i*9))
        tip(r,end,0,0,0,.56,-(2.1+i*.16),.56,GLOW)

def rhulk(r):
    # The Disciple's height is mostly leg. The upper ridges surround an unbroken red central breastplate.
    limbs(r,hip=(1.91,-22.2,.1),shoulder=(4.39,-34.6,.05),thigh=11.12,shin=9.95,upper=7.47,lower=6.14,arm=2.46,leg=3.18,family='rhulk')
    cap(r,'body',0,-35.84,.17,8.39,8.03,4.79,DEEP)
    cap(r,'body',0,-28.66,.19,5.40,7.02,3.60,PLATE)
    for i in range(5):
        w=6.63-.64*i
        cap(r,'body',0,-33.81+i*2.10,-1.93+.12*i,w,.86,1.02,PLATE if i%2 else EDGE)
    for s,side in ((-1,'r'),(1,'l')):
        for i in range(3):
            rib=r.joint(side+'_pectoral_fold_'+str(i),'body',s*(2.28-.22*i),-34.12+i*1.48,-1.99,2,0,-s*28)
            cap(r,rib,0,0,0,2.35,.88,1.25,EDGE)
        pad=r.joint(side+'_epaulet',side+'_arm',s*.1,-1.62,.21,3,0,s*7)
        cap(r,pad,0,0,0,3.98,3.97,4.74,DEEP)
        fin=r.joint(side+'_shoulder_pinnacle',pad,s*.91,.13,.47,7,0,s*12)
        tip(r,fin,0,0,0,.92,-2.72,1.25,PLATE)
        for i in range(4):r.box(pad,0,.53+i*.63,-2.21,2.72,.26,.40,PLATE)
        cap(r,side+'_leg',0,.2,-.33,3.15,4.32,3.52,PLATE)
        cap(r,side+'_shin',0,0,-.34,2.87,3.65,2.89,DEEP)
        for i in range(3):
            r.box(side+'_shin',0,7.47+i*.46,-.85,1.4,.14,.36,BRASS)
    h=r.joint('head','body',0,-35.6,-.14,-2)
    cap(r,h,0,-3.64,.02,2.68,4.07,2.31,INK)
    cap(r,h,0,-8.08,.18,2.86,5.32,2.42,DEEP)
    cap(r,h,0,-8.93,.20,2.35,2.06,2.04,INK)
    for side in (-1,1):
        for i in range(3):
            r.box(h,side*.59,-4.49+i*.57,-1.18,.52,.22,.19,LIGHT)
    # Lubrae's Ruin sports a long, narrow, forward-mounted blade beyond its angular socket.
    glaive(r,'r_hand',False,True)
    spear=r.nodes['glaive_blade'];spear['rotation'][2]=-.16

def grim(r):
    b,j=r.box,r.joint
    core=j('thorax','body',0,-13.10,0,22)
    cap(r,core,0,-4.51,0,5.74,6.84,5.08,DEEP)
    cap(r,core,0,1.00,.53,3.58,4.72,3.79,CLOTH)
    resonator(r,core,3.49,-1.78,-2.04,4)
    h=j('head',core,0,-3.47,-1.19,-17)
    cap(r,h,0,-3.04,.36,3.37,3.65,3.90,PLATE)
    cap(r,h,0,-2.26,-1.04,2.74,2.41,2.29,BRASS)
    mouth=j('jaw',h,0,-.53,-1.85,-8)
    cap(r,mouth,0,0,-.49,2.04,.58,1.64,GOLD)
    tri_eye(r,h,-2.01,-2.28)
    for k in range(7):
        s=(k-3)/3
        spike=j('dorsal_spike_'+str(k),h,s*1.41,-2.42,1.1,24+abs(s)*12,0,s*37)
        tip(r,spike,0,0,0,.52,-(3.19+(1-abs(s))*1.74),.67,EDGE)
    for s,side in ((-1,'r'),(1,'l')):
        leg=j(side+'_leg',core,s*1.39,3.97,.63,-37,0,-s*11)
        cap(r,leg,0,0,0,1.67,4.10,1.81,PLATE)
        shin=j(side+'_shin',leg,0,3.87,0,69)
        cap(r,shin,0,0,0,1.20,3.65,1.19,DEEP)
        talon=j(side+'_foot',shin,0,3.42,-.09,-68);tip(r,talon,0,0,0,.86,2.21,.78,BRASS)
        arm=j(side+'_arm',core,s*2.24,-2.68,0,-23,0,-s*37);cap(r,arm,0,0,0,1.16,3.33,1.19,PLATE)
        fore=j(side+'_forearm',arm,0,3.21,0,-47);cap(r,fore,0,0,0,.92,3.60,.97,DEEP)
        hand=j(side+'_hand',fore,0,3.42,0,-19);tip(r,hand,0,0,0,1.08,1.80,1.19,INK)
        W=j(side+'_wing',core,s*2.09,-2.94,1.51,-22,-s*6,s*5)
        bar(r,side+'_leading_spar',W,(0,0,0),(s*6.84,-2.91,1.18),1.39,1.38,PLATE)
        outer=j(side+'_wingtip',W,s*6.83,-2.90,1.15,0,-s*8)
        bar(r,side+'_outer_spar',outer,(0,0,0),(s*10.5,-3.01,.95),.89,.81,EDGE)
        for parent,spans,w,y0,dy in ((W,[2.31,3.68,4.64,5.59,6.51,7.13,7.49,7.12],.86,0,-.37),(outer,[7.45,6.83,5.60,4.89,5.64,4.77,3.80,3.26,2.47,1.76,1.15,.46],.83,0,-.245)):
            for i,depth in enumerate(spans):
                strip=j(side+('_inner_' if parent==W else '_outer_')+str(i),parent,s*(i+.45)*w,y0+i*dy,.26,6+i*.75)
                b(strip,0,0,0,w+.066,depth,.53,BONE if parent==W and i<2 else PEARL)
                b(strip,0,0,-.02,w+.052,.37,.51,DEEP)
                if i in (0,4,8):b(strip,-s*.22,0,-.22,.29,depth*.95,.42,BRASS)
        hook=j(side+'_wing_claw',outer,0,0,0,-9,0,s*19);tip(r,hook,0,0,0,.86,-2.24,.89,EDGE)
    tail=j('tail',core,0,4.61,1.25,53);tip(r,tail,0,0,0,.96,5.11,1.09,DEEP)
    g=j('gun','r_hand',0,.20,-.44,-34);cap(r,g,0,-.57,-1.62,1.02,1.58,4.24,INK);b(g,0,-.23,-3.62,.51,.54,1.03,GOLD)

def geist(r):
    # The body is a luminous airborne parasite inside the Husk, with two jaws and successive rattling hooks.
    core=r.joint('head','body',0,-3.26,.03,-8)
    cap(r,core,0,-1.45,0,2.34,2.78,2.35,BRASS)
    cap(r,core,0,-.9,-.69,1.82,1.80,1.59,GOLD)
    r.box(core,0,-.57,-1.58,.57,1.13,.30,LIGHT)
    for s,side in ((-1,'r'),(1,'l')):
        jaw=r.joint(side+'_jaw',core,s*.8,-.78,-.48,-33,0,-s*30)
        tip(r,jaw,0,0,0,.69,2.09,.74,INK)
        wing=r.joint(side+'_wing',core,s*.85,-.85,.46,23,0,s*45)
        tip(r,wing,0,0,0,.79,-2.46,.68,BRASS)
        for i in range(3):
            hook=r.joint(side+'_bristle_'+str(i),core,s*.76,-.52+i*.55,.04+i*.34,18+i*17,0,-s*(61+i*14))
            tip(r,hook,0,0,0,.34,2.34-.31*i,.40,GOLD if i==1 else DEEP)
    tail=r.joint('tail',core,0,1.06,.32,56);tip(r,tail,0,0,0,1.14,2.71,1.05,DEEP)
    spine=r.joint('tailtip',tail,0,2.43,0,31);tip(r,spine,0,0,0,.59,2.04,.59,GOLD)
# Registry indices are already embedded in saves and are not model enumeration offsets.
ROSTER={
 'dread_grim':(120,1.24,grim,{},dict(PLATE='65434b',DEEP='483038',EDGE='8b6363',BONE='bd9e86',PEARL='d7cbb9',POWER='b17b4a',GLOW='ffe996')),
 'dread_husk':(121,1.26,husk,{},dict(PLATE='524767',DEEP='342a43',EDGE='766b86',PEARL='bdb4ac',STEEL='706383',BRASS='b5824a',MARK='3f4349')),
 'dread_attendant':(122,.96,acolyte,{'stasis':True},dict(PLATE='3c538e',DEEP='25345c',EDGE='7186b3',STEEL='576285',POWER='5c83ea',GLOW='a3c5ff')),
 'dread_weaver':(123,.96,acolyte,{'stasis':False},dict(PLATE='33775b',DEEP='253f38',EDGE='6ba383',STEEL='60796a',POWER='59d68d',GLOW='9bf4a3')),
 'dread_omen':(124,1.29,subjugator,{'stasis':True},dict(PLATE='59658e',DEEP='303242',EDGE='8194b7',POWER='5b79d4',GLOW='92befc',BONE='b2b1bf',PEARL='e4ddde')),
 'dread_harbinger':(125,1.29,subjugator,{'stasis':False},dict(PLATE='377963',DEEP='283d39',EDGE='71a18a',POWER='51a979',GLOW='a4e8b1',BONE='b1b3ab',PEARL='e1ded2')),
 'dread_tormentor':(126,1.72,torment,{'boss':False},dict(PLATE='4a4156',EDGE='81758a',DEEP='2b263b',STEEL='6f717d',BRASS='9f734e',POWER='614883',GLOW='d3a5fd')),
 'dread_geist':(127,1.0,geist,{},dict(DEEP='493325',BRASS='946035',GOLD='f8b54e',LIGHT='ffe6a0',EDGE='b69867')),
 'rhulk':(137,1.59,rhulk,{},dict(PLATE='7f3030',EDGE='ad4e43',DEEP='4d292b',RUBBER='292226',CLOTH='46232a',STEEL='77534d',BRASS='aa7c51',POWER='d38453',GLOW='f8b872')),
 'nezarec':(138,1.78,torment,{'boss':True},dict(PLATE='ae403c',EDGE='d37977',DEEP='682f39',STEEL='4a4e5a',CLOTH='312d3c',BRASS='d19f67',POWER='6754aa',GLOW='b2d9fc',MARK='b66589')),
}

def make(name):
    kind,scale,draw,options,pigments=ROSTER[name]
    r=Rig(kind,name);r.joint('body',None,0,24,0)
    draw(r,**options)
    # Body is the vanilla feet-origin transform. Scaling its 24 px pivot moves the entire unit off the floor.
    for p in r.parts:
        if p['parent'] is not None:p['pivot']=[round(x*scale,4) for x in p['pivot']]
        for box in p['boxes']:
            for k in (0,1):box[k]=[round(x*scale,4) for x in box[k]]
    r.d.update(palette=palette(**pigments),emissive_palette=[LIGHT,GLOW]+([GOLD] if name in ('dread_geist','dread_husk','dread_grim','nezarec','dread_tormentor') else []),
               geometry_slot=kind,source='Ageless Arsenals / Dread / see REFERENCES.md')
    return r
