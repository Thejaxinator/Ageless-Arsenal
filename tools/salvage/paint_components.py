"""Pixel artwork in the production 32 px inventory grid. Coordinates below are source art."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from build_data import FACTIONS, STAGE, HERE, NS

def new():
    im = Image.new('RGBA',(32,32))
    return im,ImageDraw.Draw(im)
def poly(d,p,fill,edge=None):d.polygon(p, fill=fill, outline=edge)
def line(d,p,fill,w=1):d.line(p,fill=fill,width=w)
def pts(s):
    return [tuple(int(n) for n in v.split(',')) for v in s.split()]
def shape(d,points,color,edge=None):poly(d,pts(points),color,edge)
def stroke(d,points,color,w=1):line(d,pts(points),color,w)

def fallen():
    im,d=new()
    # The angled armature sticks out past the wound copper. Each loop passes over the core.
    shape(d,'2,24 8,17 10,13 19,4 26,3 30,8 28,14 20,22 9,28 5,29 2,27','#1a2531')
    shape(d,'19,5 25,4 29,8 24,13 17,9','#637c8b')
    shape(d,'20,5 25,4 26,6 23,8 18,8','#a4c4c8')
    shape(d,'23,8 26,6 28,8 24,12 22,11','#354956')
    shape(d,'19,9 24,13 10,24 7,19','#22454d')
    stroke(d,'7,22 18,11 22,11','#7de1dc',2)
    for x,y in [(8,17),(11,14),(14,11),(17,8)]:
        shape(d,f'{x-2},{y+2} {x},{y-1} {x+3},{y-2} {x+8},{y+2} {x+8},{y+5} {x+5},{y+8} {x+4},{y+6} {x+6},{y+3} {x+2},{y} {x},{y+1} {x-1},{y+3}', '#583d34')
        stroke(d,f'{x-1},{y+2} {x+1},{y-1} {x+3},{y-1} {x+7},{y+2} {x+7},{y+4}', '#d68b56')
        stroke(d,f'{x+1},{y-1} {x+3},{y-1} {x+4},{y}', '#ffdc8d')
    shape(d,'3,24 8,19 12,22 12,25 7,29 3,27','#32424f','#192630')
    stroke(d,'4,24 8,21 10,22','#a8bac1')
    stroke(d,'5,26 7,27 10,24','#57e1ed')
    stroke(d,'20,7 23,6','#86f3fb')
    return im

def vex():
    im,d=new()
    shape(d,'3,23 9,11 18,3 21,2 30,21 29,25 20,28 5,29 2,26','#35312e')
    shape(d,'4,24 11,12 20,4 28,21 26,24 9,26','#b58e4b')
    shape(d,'5,25 15,23 27,21 27,25 8,28','#685b3b')
    shape(d,'7,22 19,7 23,14 23,19 18,23 10,24','#4c695a')
    shape(d,'9,21 11,15 16,10 19,9 22,12 23,17 20,20 15,23 11,23','#30444a')
    shape(d,'11,19 12,16 17,11 20,12 21,15 19,19 15,21 12,21','#69c9c7')
    shape(d,'12,18 15,14 18,13 20,14 18,17 14,20 12,20','#c8f4e5')
    stroke(d,'13,18 16,15 18,15','#f4fff0',2)
    stroke(d,'4,23 10,12 18,5 20,4','#ebd999')
    stroke(d,'22,9 27,20 25,21','#d9bc75')
    for x,y in [(7,23),(19,6),(25,21)]:
        d.rectangle((x-1,y-1,x+1,y+1),fill='#4e4c3a')
        d.point((x,y-1),fill='#fae1a5')
    stroke(d,'11,25 15,25 17,24','#ded28b')
    return im

def hive():
    im,d=new()
    shape(d,'3,28 6,20 9,17 10,12 12,8 14,7 14,12 17,7 20,4 22,4 21,8 25,5 28,4 29,5 28,10 25,14 23,15 24,18 19,19 19,21 14,22 11,25 5,29','#292e25')
    shape(d,'4,27 8,20 10,17 12,10 13,9 12,16 16,11 18,7 20,5 18,11 23,8 27,5 28,6 26,11 22,14 20,18 14,20 10,24','#a69468')
    shape(d,'5,26 9,20 12,17 17,12 23,10 26,7 24,11 20,14 18,17 13,19','#e4d6a4')
    shape(d,'9,24 14,21 18,21 18,19 22,18 22,16 17,17 13,20','#6c7150')
    stroke(d,'8,22 13,17 17,14 22,11','#fff3c3')
    stroke(d,'13,15 14,11','#cec594')
    stroke(d,'16,19 19,16 21,16','#d2d79a')
    # A crooked three-stroke Hive rune, set into the bone.
    stroke(d,'15,15 16,13 17,15 20,12','#39593e',2)
    stroke(d,'15,14 16,13 17,15 19,13','#9af09e')
    d.point((20,12),'#e7ffd0')
    return im

def cabal():
    im,d=new()
    shape(d,'2,14 7,8 19,4 26,6 30,12 30,19 25,24 13,28 7,26 2,21','#292c30')
    shape(d,'3,17 8,21 15,23 27,18 29,14 29,19 24,23 13,27 7,25 3,21','#60676b')
    stroke(d,'5,21 12,25 15,25 26,21','#a0a19b')
    shape(d,'3,14 8,9 19,5 26,7 29,12 26,17 15,22 8,20 3,17','#923e31')
    shape(d,'4,13 8,9 19,5 24,6 24,8 13,12 8,16 4,16','#cf6544')
    shape(d,'7,15 10,11 20,8 25,10 26,13 23,16 15,19 10,18','#d5a760')
    shape(d,'9,14 12,11 20,9 24,11 24,13 21,15 15,17 11,17','#302f32')
    shape(d,'11,14 15,12 20,11 22,12 20,13 14,15 11,15','#151e25')
    stroke(d,'13,16 20,14 23,12','#a8b2ad')
    for x,y in [(6,14),(20,7),(25,16),(13,20)]:
        d.rectangle((x-1,y,x+1,y+1),fill='#3a3d41')
        d.point((x,y-1),'#f6d699')
    stroke(d,'5,12 8,10','#f99362')
    d.line((20,20,21,19),fill='#e9b457')
    return im

def taken():
    im,d=new()
    shape(d,'2,22 7,17 7,12 13,7 17,7 23,3 27,3 25,7 23,9 28,10 30,14 27,19 24,20 23,25 15,28 10,27 4,29 6,25','#354956')
    shape(d,'4,22 9,17 9,12 14,8 18,8 24,4 26,4 23,8 17,13 13,13 10,17 9,20 5,23','#eefbf7')
    shape(d,'7,26 13,24 14,20 20,18 24,14 26,10 29,13 26,18 22,20 21,24 15,27 12,26','#a8d9df')
    shape(d,'7,23 10,18 10,14 14,10 18,10 23,6 22,10 26,11 25,15 22,18 18,20 16,24 12,25','#090d18')
    shape(d,'12,17 15,14 19,13 22,10 21,14 18,17 16,17 14,19 12,22 10,22','#516c7a')
    stroke(d,'12,20 15,17 18,16 22,12','#d5fff7')
    stroke(d,'15,25 19,24 21,21','#f3fff4')
    d.rectangle((27,6,28,7),fill='#d2ebe7');d.point((30,3),'#f1ffff')
    d.rectangle((3,16,4,17),fill='#64919e');d.point((3,26),'#e9fff6')
    return im

def scorn():
    im,d=new()
    shape(d,'4,22 6,15 10,12 12,10 13,7 18,5 24,7 26,11 25,15 24,20 20,24 14,27 10,27 7,29 2,26 2,23','#322a30')
    shape(d,'6,21 7,16 12,12 15,10 20,9 24,11 23,16 22,20 17,24 11,26 7,25','#583958')
    shape(d,'8,20 10,15 14,12 20,11 23,12 21,16 17,19 11,22','#94649b')
    shape(d,'10,19 11,16 15,13 18,13 18,14 14,17 12,20','#d391d3')
    # Asymmetric brass-and-leather pleats carry the profile at inventory scale.
    for x,y in [(8,21),(12,18),(16,15)]:
        stroke(d,f'{x-1},{y-4} {x-2},{y-2} {x},{y+1} {x+5},{y+4} {x+7},{y+3}', '#463946',2)
        stroke(d,f'{x-2},{y-2} {x},{y} {x+5},{y+3} {x+7},{y+2}', '#b19953')
        d.point((x,y),'#f2c578')
    shape(d,'14,8 17,5 22,4 27,6 28,8 24,11 19,12 15,10','#514736','#292831')
    stroke(d,'15,8 19,6 23,5 26,7 22,8','#c69e62',2)
    stroke(d,'19,8 22,7 24,7','#402d45')
    shape(d,'3,23 6,21 11,25 8,28 5,28 2,26','#665945','#302b30')
    stroke(d,'3,24 6,23 8,25','#c4ad6f')
    stroke(d,'22,21 24,17 25,14','#a97bc4')
    return im

def dread():
    im,d=new()
    shape(d,'3,28 5,23 8,22 10,17 12,15 12,10 15,7 16,3 19,2 19,7 17,13 18,16 20,14 21,10 25,6 27,3 29,3 28,10 26,14 25,17 22,20 20,20 18,23 13,24 10,27 6,30','#291f27')
    shape(d,'5,27 9,22 12,17 14,14 13,11 16,7 17,4 18,3 18,7 16,13 18,17 16,20 12,21 9,25','#474149')
    shape(d,'16,19 21,14 22,10 26,7 28,4 27,10 24,14 24,16 20,20 17,23 12,24','#634844')
    stroke(d,'5,27 9,24 11,23 13,19 16,19 18,17 21,16 24,12','#dc8a46',2)
    stroke(d,'9,23 13,18 16,18 18,16 21,15 24,11','#ffd882')
    stroke(d,'14,13 16,10 17,7','#c4824c')
    stroke(d,'25,9 27,6','#ffdb93')
    stroke(d,'9,27 12,25 16,24','#83767e')
    d.point((14,16),'#fff0af')
    return im

DRAWINGS = (fallen,vex,hive,cabal,taken,scorn,dread)

def build():
    imgs = []
    for (faction,(ident,name,*_)),paint in zip(FACTIONS.items(),DRAWINGS):
        im=paint();target=STAGE/f'assets/{NS}/textures/item/{ident}.png'
        target.parent.mkdir(parents=True,exist_ok=True);im.save(target)
        imgs.append((faction,name,im))
    # Pixel-exact enlargements of the very files the game draws, plus the 16px HUD rendition.
    screen=Image.new('RGB',(1176,574),'#171d22');d=ImageDraw.Draw(screen)
    fontroot=Path('/usr/share/fonts/truetype/dejavu')
    def font(size,bold=False):return ImageFont.truetype(str(fontroot/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')),size)
    d.text((38,23),'Faction salvage',font=font(42,True),fill='#e8e5dc')
    d.text((40,80),'Ageless Arsenals   /   Gunsmith components',font=font(19),fill='#b6c3bf')
    for i,(faction,name,im) in enumerate(imgs):
        x=36+(i%4)*286;y=132+(i//4)*216
        d.rectangle((x,y,x+250,y+132),fill='#252e34')
        tile=im.resize((125,125),Image.Resampling.NEAREST)
        screen.paste(tile,(x+60,y+3),tile)
        small=im.resize((16,16),Image.Resampling.NEAREST)
        screen.paste(small,(x+220,y+102),small)
        d.text((x,y+141),name,font=font(20,True),fill='#e9e7dd')
        d.text((x,y+170),faction.capitalize(),font=font(16),fill='#bdac87')
    screen.save(HERE/'Ageless-Faction-Salvage.png',optimize=True)

if __name__=='__main__':build()
