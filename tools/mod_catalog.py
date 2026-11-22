"""Render the reference sheet from the shipping mod definitions and original mod icon textures."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw,ImageFont
here=Path(__file__).parent
ap=argparse.ArgumentParser()
ap.add_argument('--catalog',type=Path,default=here.parent/'docs/warframe-mod-catalog.json')
ap.add_argument('--icons',type=Path,default=here.parent/'src/main/resources/assets/destiny2mc/textures/gui/warframe/mods')
ap.add_argument('--output',type=Path,default=here.parent/'docs/previews/mod-fusion/Ageless-Arsenals-Mod-Catalog.png')
a=ap.parse_args()
rows=json.loads(a.catalog.read_text()); assert len(rows)==22
width,height=1350,1466
img=Image.new('RGB',(width,height),'#111c20');p=ImageDraw.Draw(img)
fontDir=Path('/usr/share/fonts/truetype/open-sans')
def font(size,bold=False):
    path=fontDir/('OpenSans-Semibold.ttf' if bold else 'OpenSans-Regular.ttf')
    if not path.exists():path=Path('/usr/share/fonts/truetype/dejavu')/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')
    return ImageFont.truetype(str(path),size)
gold='#dfc493';paper='#f1ebdd';dim='#a8bcb8';aqua='#9fe3d4'
p.text((38,21),'AGELESS ARSENALS',font=font(18,True),fill=gold)
p.text((36,47),'Weapon mod collection',font=font(46,True),fill=paper)
p.text((38,119),'Each card starts at rank 0. Fusion costs the new rank in Minecraft levels.',font=font(20),fill=dim)
p.line((38,166,1312,166),fill=gold,width=2)
frame={'Common':'#ad876e','Uncommon':'#94a9b3','Rare':'#c9a363'}
step=96;col=628
def group(x,y,title,items):
    p.text((x,y),title,font=font(25,True),fill=paper)
    y+=44
    for row in items:
        box=(x,y,x+col,y+87)
        p.rectangle(box,fill='#1a282d')
        p.rectangle((x,y,x+2,y+87),fill=frame[row['rarity']])
        ico=Image.open(a.icons/(row['id']+'.png')).convert('RGBA').resize((64,64),Image.Resampling.NEAREST)
        img.paste(ico,(x+13,y+12),ico)
        tx=x+87
        p.text((tx,y+1),row['title'],font=font(24,True),fill=paper)
        p.text((x+col-14,y+7),row['category'],font=font(17),fill=dim,anchor='ra')
        p.text((tx,y+36),'Rank 0',font=font(15),fill=dim)
        p.text((tx+70,y+34),row['baseEffect'],font=font(17),fill=paper)
        p.text((tx,y+60),'Rank '+str(row['maxRank']),font=font(15),fill=dim)
        p.text((tx+70,y+58),row['maxEffect'],font=font(17,True),fill=aqua)
        assert p.textlength(row['maxEffect'],font=font(17,True)) < col-170,row['id']
        y+=step
    return y+8
p.text((38,182),'Primary packs',font=font(32,True),fill=gold)
p.text((690,182),'Secondary & melee packs',font=font(32,True),fill=gold)
left=group(38,239,'Rifles',[r for r in rows if r['category']=='Rifle'])
left=group(38,left,'Shotguns',[r for r in rows if r['category']=='Shotgun'])
left=group(38,left,'All primaries',[r for r in rows if r['category']=='Primary'])
right=group(690,239,'Sidearms',[r for r in rows if r['category']=='Pistol'])
right=group(690,right,'Melee',[r for r in rows if r['category']=='Melee'])
end=max(left,right)+14
img=img.crop((0,0,width,min(height,end)))
assert end<=height
img.save(a.output,optimize=True)
