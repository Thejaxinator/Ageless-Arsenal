"""Assemble the in-game presentation from native framebuffers and distributed item art."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import zipfile, sys, io, numpy as np
shots=Path(sys.argv[1]); out=Path(sys.argv[2]);jar=Path(sys.argv[3])
out.mkdir(exist_ok=True,parents=True)
def shot(n):return Image.open(shots/(n+'.png')).convert('RGB')
bg='#142021';sep='#59675c';ivory='#efe8ce';muted='#becac4';brass='#cfb78b';panel='#1b2927'
def font(size,bold=False):
    f=Path('/usr/share/fonts/truetype/open-sans')/('OpenSans-Semibold.ttf' if bold else 'OpenSans-Regular.ttf')
    return ImageFont.truetype(str(f),size)
def paste_scaled(canvas,img,xywh,up=True):
    x,y,w,h=xywh
    s=min(w/img.width,h/img.height)
    if not up:s=min(1,s)
    im=img.resize((max(1,round(s*img.width)),max(1,round(s*img.height))),Image.Resampling.LANCZOS)
    canvas.paste(im,(x+(w-im.width)//2,y+(h-im.height)//2))
def component_bounds(im,color,minx=0,miny=0):
    a=np.asarray(im);mask=np.all(a==tuple(bytes.fromhex(color)),axis=2)
    mask[:miny]=False;mask[:,:minx]=False
    y,x=np.where(mask)
    assert len(x)>40
    return (x.min(),y.min(),x.max(),y.max())
im=Image.new('RGB',(1320,1016),bg);d=ImageDraw.Draw(im)
d.text((26,16),'Weapon mods',font=font(34,True),fill=ivory)
label='AGELESS ARSENALS'
d.text((1320-26-d.textlength(label,font=font(16,True)),35),label,font=font(16,True),fill=brass)
d.line((26,71,1293,71),fill=sep,width=2)
# Whole game interface at the top. Crops below give phone-readable details from the same session.
forge=shot('05-All-Equipped');paste_scaled(im,forge,(26,84,1268,667))
d.text((28,769),'Pack reveal',font=font(21,True),fill=ivory)
d.text((620,769),'Craftable packs',font=font(21,True),fill=ivory)
reveal=shot('02-Reveal')
x,y,r,b=component_bounds(reveal,'dfc595',700,80)
_,yb,_,bottom=component_bounds(reveal,'687068',x,y+6)
raw=reveal.crop((x,y,r+1,bottom+1))
paste_scaled(im,raw,(26,806,565,173))
d.rectangle((620,806,1293,980),fill=panel)
pack_ids=['mod_pack','primary_mod_pack','secondary_mod_pack','melee_mod_pack']
with zipfile.ZipFile(jar) as z:
    for i,(name,title) in enumerate(zip(pack_ids,['Universal','Primary','Secondary','Melee'])):
        icon=Image.open(io.BytesIO(z.read('assets/destiny2mc/textures/item/tenno_packs/'+name+'.png'))).convert('RGBA')
        icon=icon.crop(icon.getbbox()); icon.thumbnail((112,112))
        s=min(112/icon.width,112/icon.height);icon=icon.resize((int(icon.width*s),int(icon.height*s)),Image.Resampling.NEAREST)
        center=704+166*i
        im.paste(icon,(center-icon.width//2,813+(117-icon.height)//2),icon)
        d.text((center-d.textlength(title,font=font(17))/2,942),title,font=font(17),fill=ivory)
d.line((26,997,1293,997),fill=sep,width=1)
im.save(out/'Ageless-Arsenals-Mod-Fusion.png',optimize=True)
