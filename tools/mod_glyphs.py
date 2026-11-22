"""Original 32-pixel Arsenal emblems for Ageless Arsenals. Run with optional texture directory.

The item-class edge bands distinguish rifle, shotgun, sidearm, melee and universal primary.
Each central silhouette describes the mechanic. High-contrast pixel bevels keep the 32 px
master legible at a reduced GUI scale. The UI supplies the card's rarity frame separately.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import sys

INK='#132027'
BONE='#e4e8d8'
GOLD='#d1b078'
SILVER='#acc4c2'
STEEL='#506e79'
DIM='#3a494a'
CORAL='#ef9677'
EMBERS='#c85343'
BLUE='#7de2ec'
MINT='#81cbb4'
MAROON='#6d4b50'
FIELDS={
    'rifle':('#223e40','#426760','#96bf8e'),
    'shotgun':('#433835','#70524e','#da9777'),
    'pistol':('#333d4a','#4d667f','#a1bbd8'),
    'melee':('#3b3543','#68576e','#c4a0c4'),
    'primary':('#39403a','#646957','#b8be85')
}

def render(name,family):
    shadow,edge,glint=FIELDS[family]
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    tile=[(2,4),(5,1),(23,1),(29,7),(29,25),(24,30),(8,30),(2,24)]
    d.polygon(tile,fill=INK);d.polygon([(3,5),(6,2),(22,2),(28,8),(28,24),(23,29),(9,29),(3,23)],fill=shadow)
    d.polygon([(3,21),(20,4),(25,6),(4,27)],fill=edge)
    d.polygon([(9,28),(27,10),(27,19),(18,28)],fill=INK)
    d.line([(3,8),(3,5),(6,2),(11,2)],fill=glint)
    d.line([(21,29),(24,29),(28,25)],fill=edge)
    def poly(points,fill,outline=INK):
        d.polygon(points,fill=fill,outline=outline)
    def line(points,color,width=1):d.line(points,fill=color,width=width)
    def box(x,y,x2,y2,fill,outline=INK):d.rectangle((x,y,x2,y2),fill=fill,outline=outline)
    def ellipse(x,y,x2,y2,fill=None,outline=INK,w=1):d.ellipse((x,y,x2,y2),fill=fill,outline=outline,width=w)
    def blade(x,y,sz=16,metal=SILVER):
        # Long diagonal Tenno blade with stepped, hard-surface tang and handle.
        poly([(x+3,y+sz-4),(x+sz-3,y+1),(x+sz,y),(x+sz-2,y+6),(x+7,y+sz-3)],metal);line([(x+5,y+sz-4),(x+sz-1,y+2)],BONE)
        line([(x+8,y+sz-1),(x+3,y+sz-6)],GOLD,2);line([(x+4,y+sz-3),(x,y+sz+2)],GOLD,2)
    def bullet(x,y,color=GOLD):
        poly([(x,y+3),(x+6,y+3),(x+9,y+1),(x+6,y-1),(x,y-1)],color);line([(x+1,y),(x+5,y)],BONE);line([(x+2,y+2),(x+2,y+3)],INK)
    def arc_arrow(bounds,start,end,tip,color):
        d.arc(bounds,start,end,fill=INK,width=4);d.arc(bounds,start,end,fill=color,width=2);poly(tip,color)
    def flare(x,y,c=GOLD):
        line([(x-3,y),(x+3,y)],c);line([(x,y-3),(x,y+3)],c);box(x-1,y-1,x+1,y+1,BONE,None)
    if name=='serration':
        for i in range(3):
            x=10+4*i
            poly([(x,8),(x+3,6),(x+1,12),(x-1,14)],STEEL)
            poly([(x-1,19),(x+2,17),(x,23),(x-3,25)],GOLD)
        bullet(15,15);line([(5,16),(17,16)],BONE,2)
    elif name=='speed_trigger':
        for x,c in [(4,STEEL),(10,GOLD),(16,BONE)]:
            poly([(x,9),(x+3,8),(x+10,15),(x+3,23),(x,23),(x+6,15)],c)
    elif name=='fast_hands':
        poly([(9,7),(15,8),(14,18),(12,21),(7,19)],SILVER);line([(10,9),(12,9)],BONE)
        poly([(17,10),(22,11),(20,23),(18,25),(15,23)],GOLD);line([(18,13),(20,13)],BONE)
        arc_arrow((3,8,24,27),56,150,[(3,18),(5,25),(9,21)],MINT)
    elif name=='magazine_warp':
        poly([(8,6),(18,8),(17,16),(20,24),(14,26),(9,19)],SILVER)
        for y in (11,15,19):line([(10,y),(15,y+1)],STEEL,2)
        poly([(21,14),(26,18),(21,22),(21,19),(18,19),(18,16),(21,16)],GOLD)
    elif name=='hellfire':
        poly([(6,21),(6,16),(10,18),(12,11),(11,7),(16,9),(20,4),(20,12),(24,11),(24,17),(21,24),(14,27),(9,25)],EMBERS)
        poly([(10,21),(14,16),(14,13),(17,14),(19,9),(19,16),(22,16),(18,23),(14,25)],CORAL,None)
        poly([(14,22),(17,17),(18,19),(17,22),(15,24)],BONE,None)
    elif name=='point_blank':
        ellipse(12,7,27,23,None,GOLD,2);ellipse(16,11,23,19,None,BONE);box(19,14,21,16,CORAL,None)
        poly([(4,13),(13,13),(15,16),(12,20),(4,20)],STEEL);line([(5,14),(12,14)],BONE)
        for x,y in [(16,11),(17,15),(16,19)]:box(x,y,x+1,y+1,GOLD,None)
    elif name=='shotgun_barrage':
        for y in (11,18):
            poly([(4,y),(14,y),(19,y-3),(17,y+2),(23,y+1),(16,y+5),(5,y+5)],SILVER)
            line([(5,y+1),(13,y+1)],BONE)
            for x,dy in [(21,-2),(25,0),(23,3)]:box(x,y+dy,x+1,y+dy+1,GOLD,None)
    elif name=='tactical_pump':
        poly([(5,10),(22,8),(26,9),(26,13),(6,15)],SILVER);line([(6,11),(23,9)],BONE)
        poly([(7,16),(20,14),(20,20),(9,22),(6,20)],GOLD)
        for j in range(3):line([(9+3*j,17),(10+3*j,20)],STEEL)
        line([(13,24),(23,22)],MINT,2);poly([(24,19),(27,22),(23,25)],MINT)
    elif name=='ammo_stock':
        for x,y,c in [(6,10,STEEL),(12,7,SILVER),(18,10,CORAL)]:
            poly([(x,y+2),(x+2,y),(x+5,y+1),(x+6,y+14),(x,y+14)],c)
            box(x,y+11,x+6,y+14,GOLD);line([(x+1,y+3),(x+1,y+9)],BONE)
    elif name=='chilling_grasp':
        for pts,c in [([(6,19),(6,11),(10,16),(12,24)],SILVER), ([(11,19),(14,4),(17,8),(15,24)],BLUE), ([(16,20),(23,8),(23,15),(19,24)],BONE)]:
            poly(pts,c)
        poly([(7,20),(12,22),(19,20),(23,17),(23,22),(18,26),(11,26)],STEEL);line([(9,21),(13,23),(18,21)],MINT)
    elif name=='vigilante_armaments':
        for x,y,c in [(13,7,SILVER),(16,14,BONE),(13,21,GOLD)]:
            bullet(x,y,c);line([(6,y+1),(x-1,y+1)],MINT)
        line([(7,8),(4,14),(7,22)],GOLD)
    elif name=='hornet_strike':
        poly([(6,8),(15,13),(15,7),(22,15),(27,15),(17,21),(12,24),(13,18),(6,19),(10,15)],GOLD)
        line([(7,9),(17,15),(24,15)],BONE);line([(8,23),(12,20)],CORAL,2)
    elif name=='lethal_torrent':
        for n in range(3):
            x=5+5*n;y=19-4*n
            line([(x-1,y+4),(x+5,y+1)],SILVER,2);bullet(x+4,y,GOLD if n<2 else CORAL)
        line([(5,11),(11,8)],MINT);line([(4,15),(12,11)],MINT)
    elif name=='quickdraw':
        poly([(4,10),(17,10),(20,8),(26,9),(25,13),(15,14),(12,16),(10,22),(6,21),(8,15),(4,14)],SILVER)
        line([(6,11),(22,10)],BONE)
        arc_arrow((5,11,26,26),12,105,[(18,21),(12,25),(18,27)],GOLD)
    elif name=='slip_magazine':
        for x,y,c in [(4,12,STEEL),(9,9,SILVER),(14,7,GOLD)]:
            poly([(x,y),(x+5,y),(x+7,y+12),(x+4,y+15),(x+2,y+13)],c)
        line([(15,9),(17,18)],BONE);line([(19,24),(24,19)],MINT,2)
    elif name=='heated_charge':
        bullet(5,15,CORAL);line([(4,12),(10,12)],GOLD);line([(5,20),(9,20)],GOLD)
        poly([(14,12),(18,11),(24,6),(22,12),(26,12),(22,16),(26,19),(20,19),(21,24),(15,20),(12,18)],EMBERS)
        poly([(13,14),(18,14),(21,11),(19,15),(23,16),(18,18),(19,20),(13,18)],GOLD,None)
    elif name=='pressure_point':
        blade(6,5,18)
        line([(6,12),(12,12),(12,7)],CORAL,2);line([(17,23),(17,19),(23,19)],GOLD,2)
    elif name=='fury':
        blade(7,4,17);blade(10,11,13,GOLD)
        line([(5,13),(11,7)],MINT);line([(4,18),(12,10)],MINT,2)
    elif name=='reach':
        blade(9,4,16)
        line([(4,25),(23,6)],BONE);line([(4,21),(4,25),(8,25)],GOLD,2)
        line([(20,5),(24,5),(24,9)],GOLD,2)
    elif name=='killing_blow':
        poly([(7,5),(12,9),(15,9),(23,16),(19,21),(12,18),(8,11)],SILVER)
        line([(8,5),(13,10),(20,17)],BONE);line([(12,19),(20,12)],GOLD,2)
        line([(16,17),(22,23)],GOLD,3)
        poly([(4,24),(10,20),(8,24),(15,23),(13,26),(5,27)],CORAL)
    elif name=='molten_impact':
        poly([(5,22),(9,16),(7,10),(12,12),(15,9),(16,5),(19,10),(18,14),(24,11),(23,17),(16,22)],EMBERS)
        blade(6,7,17,CORAL);line([(12,18),(21,9)],GOLD)
        line([(17,22),(25,20)],CORAL,2);line([(20,25),(24,23)],GOLD)
    elif name=='parry':
        poly([(5,8),(11,9),(15,7),(19,9),(24,8),(23,16),(20,21),(15,25),(8,20),(6,15)],STEEL)
        line([(8,10),(13,11),(15,9)],BONE);line([(9,18),(15,22),(19,19)],SILVER)
        blade(12,5,15,GOLD);flare(19,14,BLUE)
    else:raise ValueError(name)
    return im

groups={
 'rifle':'serration speed_trigger fast_hands magazine_warp hellfire',
 'shotgun':'point_blank shotgun_barrage tactical_pump ammo_stock chilling_grasp',
 'primary':'vigilante_armaments',
 'pistol':'hornet_strike lethal_torrent quickdraw slip_magazine heated_charge',
 'melee':'pressure_point fury reach killing_blow molten_impact parry'}
if __name__=='__main__':
    out=Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1]/'src/main/resources/assets/destiny2mc/textures/gui/warframe/mods'
    out.mkdir(parents=True,exist_ok=True)
    for group,names in groups.items():
        for name in names.split():render(name,group).save(out/(name+'.png'),optimize=True)
