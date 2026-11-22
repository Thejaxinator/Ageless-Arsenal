"""Catalog plates from in-game item renders, preserving original pixels and proportions.
No weapon artwork is redrawn. Each model has its own native screenshot in shots/."""
from pathlib import Path
import sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont

shots=Path(sys.argv[1])
out=Path(sys.argv[2])
out.mkdir(parents=True,exist_ok=True)
fontroot=Path('/usr/share/fonts/truetype')
def font(size,bold=False):
    name='OpenSans-Semibold.ttf' if bold else 'OpenSans-Regular.ttf'
    path=fontroot/'open-sans'/name
    if not path.exists():path=fontroot/'dejavu'/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')
    return ImageFont.truetype(str(path),size)

# Keep family and variant together in general reading order.
groups=[
    ('AX-52','ax_52','Historical rifle'),('Grakata','grakata','Automatic rifle'),('Prisma Grakata','prisma_grakata','Prisma rifle'),
    ('Twin Grakatas','twin_grakatas','Dual machine pistols'),('Braton','braton','Automatic rifle'),('Braton Vandal','braton_vandal','Vandal rifle'),
    ('Braton Prime','braton_prime','Prime rifle'),('Burston','burston','Burst rifle'),('Burston Prime','burston_prime','Prime burst rifle'),
    ('Ignis','ignis','Flamethrower'),('Ignis Wraith','ignis_wraith','Wraith flamethrower'),('Brakk','brakk','Hand shotgun'),
    ('Kuva Brakk','kuva_brakk','Kuva hand shotgun'),('Nukor','nukor','Microwave pistol'),('Kuva Nukor','kuva_nukor','Chaining beam pistol'),
    ('Cedo','cedo','Glaive shotgun'),('Cedo Prime','cedo_prime','Prime glaive shotgun'),('Vesper 77','vesper_77','Suppressed pistol'),
    ('Perigale','perigale','Burst sniper'),('Perigale Prime','perigale_prime','Prime burst sniper'),('Soma','soma','Spooling rifle'),
    ('Soma Prime','soma_prime','Prime spooling rifle'),('Cantare','cantare','Returning blades'),('Torid','torid','Spore launcher'),
    ('Aksondol','aksondol','Cold dual pistols'),('Bubonico','bubonico','Infested arm cannon'),('Coda Bubonico','coda_bubonico','Coda arm cannon')
]
melee=[
    ('Amanata','amanata','Polearm'),('Hate','hate','Scythe'),('Harmony','harmony','Scythe'),
    ('Heat Sword','heat_sword','Sword'),('Dual Heat Swords','dual_heat_swords','Dual swords'),('Sun & Moon','sun_and_moon','Dual nikanas')
]
bg=(15,26,30); tile=(25,41,50); gold=(216,192,150); muted=(155,178,178); ivory=(238,235,219)
def model(slug):
    f=shots/('Mesh-'+slug+'.png');im=Image.open(f).convert('RGB');a=np.asarray(im)
    assert tuple(a[0,0])==tile,(slug,a[0,0])
    d=np.abs(a.astype(int)-a[0,0]).max(-1)
    mask=d>4
    yy,xx=np.where(mask)
    assert min(len(xx),len(yy))>650,slug
    bb=xx.min(),yy.min(),xx.max()+1,yy.max()+1
    assert min(bb[0],bb[1],im.width-bb[2],im.height-bb[3])>1,(slug,'clipped',bb)
    missing=(a[:,:,0]>150)&(a[:,:,1]<20)&(a[:,:,2]>150)
    assert missing.sum()<max(500,0.02*mask.sum()),(slug,'missing texture checker',int(missing.sum()))
    return im.crop(bb)
def packed(title,collection,path,cols,w,h):
    pad=24; gap=8; header=83
    rows=(len(collection)+cols-1)//cols;width=pad*2+cols*w+(cols-1)*gap
    sheet=Image.new('RGB',(width,header+rows*h+(rows-1)*gap+21),bg);draw=ImageDraw.Draw(sheet)
    draw.text((pad,14),title,font=font(33),fill=ivory)
    right='AGELESS ARSENALS'
    b=draw.textbbox((0,0),right,font=font(15,True))
    draw.text((width-pad-b[2],34),right,font=font(15,True),fill=gold)
    draw.line((pad,66,width-pad,66),fill=gold,width=1)
    for n,(name,slug,kind) in enumerate(collection):
        x=pad+(n%cols)*(w+gap);y=header+(n//cols)*(h+gap)
        draw.rectangle((x,y,x+w-1,y+h-1),fill=tile)
        art=model(slug);maxsize=(w-38,h-64)
        art=art.resize(tuple(max(1,round(v*min(maxsize[0]/art.width,maxsize[1]/art.height))) for v in art.size),Image.Resampling.LANCZOS)
        sheet.paste(art,(x+(w-art.width)//2,y+11+(maxsize[1]-art.height)//2))
        draw.line((x+16,y+h-47,x+36,y+h-47),fill=gold,width=2)
        draw.text((x+15,y+h-44),name,font=font(20,True),fill=ivory)
        draw.text((x+15,y+h-19),kind,font=font(12),fill=muted)
    sheet.save(out/path,optimize=True)
packed('Warframe ranged weapons',groups,'Ageless-Arsenals-Warframe-Ranged.png',3,350,173)
packed('Warframe melee weapons',melee,'Ageless-Arsenals-Warframe-Melee.png',3,375,280)
if (shots/'Guard-sun_and_moon.png').exists():
    panels=[('Foundry','Weapon maker'),('Card-cedo_prime','Cedo Prime inspection'),('Blueprint-Card','Aksondol blueprint inspection'),('Equipped-twin_grakatas','Twin Grakatas equipped'),('ADS-Burston','Burston iron sights'),('Guard-sun_and_moon','Sun & Moon guard')]
    gap=10;pad=18;w=600;h=338
    im=Image.new('RGB',(2*(w+pad)+gap,((len(panels)+1)//2)*(h+46)+pad),bg);d=ImageDraw.Draw(im)
    for i,(slug,label) in enumerate(panels):
        x=pad+(w+gap)*(i%2);y=pad+(h+46)*(i//2)
        d.text((x,y),label,font=font(18,True),fill=ivory)
        source=Image.open(shots/(slug+'.png')).convert('RGB');source.thumbnail((w,h),Image.Resampling.LANCZOS)
        im.paste(source,(x,y+31))
    im.save(out/'Ageless-Arsenals-Warframe-In-Game.png',optimize=True)
print('Validated 33 distinct native-rendered item frames. Wrote catalog plates in',out)
