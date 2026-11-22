"""Validate the baked resources and render a catalog from their textured cuboids.

Usage: python check_warframe_bio_models.py <assets/destiny2mc> [--sheet out.png]
"""
import argparse
import itertools
import json
import math
from collections import Counter
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFont
from warframe_bio_models import PALETTES

QUADS = {'east':(4,6,7,5),'west':(1,3,2,0),'north':(0,2,6,4),'south':(5,7,3,1),'up':(3,7,6,2),'down':(0,4,5,1)}
LABELS={
    'ignis':'Grineer  /  flame projector','ignis_wraith':'Wraith  /  flame projector','brakk':'Grineer  /  hand cannon','kuva_brakk':'Kuva  /  hand cannon',
    'nukor':'Grineer  /  microwave pistol','kuva_nukor':'Kuva  /  arc beam','cedo':'Lavos  /  glaive shotgun','cedo_prime':'Prime  /  glaive shotgun',
    'torid':'Infested  /  spore launcher','bubonico':'Infested  /  arm cannon','coda_bubonico':'Coda  /  arm cannon','aksondol':'Dual pistols  /  ice needles'}
def unit(v):
    return v/np.linalg.norm(v)
def verts(el):
    pts=np.array(list(itertools.product(*zip(el['from'],el['to']))),dtype=float)
    r=el.get('rotation')
    if r:
        assert r['axis']=='x' and r['angle'] in {-45,-22.5,22.5,45}
        o=np.array(r['origin']);a=np.deg2rad(r['angle']);c=np.cos(a);s=np.sin(a)
        rot=np.array([[1,0,0],[0,c,-s],[0,s,c]])
        pts=(pts-o)@rot.T+o
    return pts
def check(assets,key):
    model=json.loads((assets/'models/item'/(key+'.json')).read_text())
    if model.get('loader')=='neoforge:separate_transforms':
        main=assets/'models'/(model['base']['parent'].split(':')[1]+'.json')
        pair=assets/'models'/(model['perspectives']['gui']['parent'].split(':')[1]+'.json')
        model=json.loads(main.read_text())
        assert len(json.loads(pair.read_text())['elements'])==2*len(model['elements'])
    shape=model['elements']
    pieces=[]
    for p in ('body','action','load'):
        piece=json.loads((assets/'models/armory'/key/(p+'.json')).read_text())
        assert piece['elements'];pieces+=piece['elements']
    assert Counter(json.dumps(p,sort_keys=True) for p in shape)==Counter(json.dumps(p,sort_keys=True) for p in pieces)
    # Test a half-unit-wide channel across the camera line. Rotations all leave X
    # unchanged, so a qualifying element's maximum Y bounds the whole longitudinal ray.
    for el in shape:
        v=verts(el)
        assert ((v>=-16)&(v<=32)).all()
        if v[:,0].min()<8.25 and v[:,0].max()>7.75:
            assert v[:,1].max()<12.55 or v[:,1].min()>13.05,(key,el['name'],v[:,1].max())
        for f in el['faces'].values():
            assert all(0<=n<=16 for n in f['uv']) and f['texture']=='#finish'
    tex=np.array(Image.open(assets/'textures/item'/('tenno_'+key+'.png')).convert('RGB'))
    return shape,tex
def render(shape,tex,width,height):
    right=unit(np.array([-.24,0,-1.0]))
    camera=unit(np.array([-1,.42,.24]))
    top=unit(np.cross(camera,right))
    if top[1]<0:top*=-1
    # Tip the entire view up a little, like the inventory's diagonal presentation.
    a=math.radians(11); right,top=right*np.cos(a)-top*np.sin(a),top*np.cos(a)+right*np.sin(a)
    basis=np.stack([right,-top,camera],axis=1)
    projected=np.stack([verts(el)@basis for el in shape])
    xy=projected[:,:,:2]
    lo=xy.min(axis=(0,1));hi=xy.max(axis=(0,1))
    scale=.91*min(width/(hi[0]-lo[0]),height/(hi[1]-lo[1]))
    xy[:]=(xy-(lo+hi)/2)*scale+np.array([width/2,height/2])
    canvas=np.zeros((height,width,4),dtype=np.uint8)
    zbuf=np.full((height,width),-np.inf)
    light=unit(np.array([-.65,.82,-.24]))
    for el,screen in zip(shape,projected):
        world=verts(el)
        for face,quad in QUADS.items():
            P=screen[list(quad)]
            normal=unit(np.cross(world[quad[1]]-world[quad[0]],world[quad[2]]-world[quad[0]]))
            # QUADS are clockwise in world space.
            if normal@camera<0:normal=-normal
            brightness=.65+.37*max(0,normal@light)
            u0,v0,u1,v1=el['faces'][face]['uv']
            uv=np.array([(u0,v1),(u0,v0),(u1,v0),(u1,v1)])
            if u0>=4 and u0<8 and v0>8:brightness=max(brightness,1.02)
            for tri in ((0,1,2),(0,2,3)):
                p,q,r=P[list(tri)]
                left=max(0,math.floor(min(p[0],q[0],r[0]))); right=min(width-1,math.ceil(max(p[0],q[0],r[0])))
                top_px=max(0,math.floor(min(p[1],q[1],r[1])));bottom=min(height-1,math.ceil(max(p[1],q[1],r[1])))
                if left>right or top_px>bottom:continue
                xx,yy=np.meshgrid(np.arange(left,right+1)+.5,np.arange(top_px,bottom+1)+.5)
                den=(q[1]-r[1])*(p[0]-r[0])+(r[0]-q[0])*(p[1]-r[1])
                if abs(den)<.001:continue
                w0=((q[1]-r[1])*(xx-r[0])+(r[0]-q[0])*(yy-r[1]))/den
                w1=((r[1]-p[1])*(xx-r[0])+(p[0]-r[0])*(yy-r[1]))/den;w2=1-w0-w1
                z=w0*p[2]+w1*q[2]+w2*r[2]
                depths=zbuf[top_px:bottom+1,left:right+1]
                keep=(w0>=0)&(w1>=0)&(w2>=0)&(z>=depths)
                if not keep.any():continue
                weights=np.stack([w0[keep],w1[keep],w2[keep]],axis=-1)
                coord=weights@uv[list(tri)]
                coord=(coord/16*np.array([tex.shape[1],tex.shape[0]])).astype(int)
                color=np.clip(tex[coord[:,1],coord[:,0]]*brightness,0,255).astype(np.uint8)
                depths[keep]=z[keep];pixels=canvas[top_px:bottom+1,left:right+1]
                pixels[keep,:3]=color;pixels[keep,3]=255
    return Image.fromarray(canvas)
def font(bold,size):
    p='/usr/share/fonts/truetype/dejavu/DejaVuSans'+('-Bold' if bold else '')+'.ttf'
    try:return ImageFont.truetype(p,size)
    except OSError:return ImageFont.load_default()
def main():
    ap=argparse.ArgumentParser();ap.add_argument('assets',type=Path);ap.add_argument('--sheet',type=Path)
    args=ap.parse_args()
    cardw,cardh=328,178;cols=3;rows=4;pad=14
    out=Image.new('RGB',(cardw*cols+pad*(cols+1),cardh*rows+pad*(rows+1)+47),'#141d23')
    d=ImageDraw.Draw(out);d.text((pad+3,10),'Tenno Arsenal',font=font(True,28),fill='#e4e7e2')
    d.text((pad+240,22),'Warframe weapon models  /  Ageless Arsenals',font=font(False,13),fill='#a1bbbd')
    total=0
    for i,key in enumerate(PALETTES):
        shape,tex=check(args.assets,key);total+=len(shape)
        if args.sheet:
            x=pad+(i%cols)*(cardw+pad);y=47+pad+(i//cols)*(cardh+pad)
            d.rectangle((x,y,x+cardw-1,y+cardh-1),fill='#202c32')
            d.line((x,y,x+cardw-1,y),fill='#35484f')
            pic=render(shape,tex,2*cardw,2*(cardh-41)).resize((cardw,cardh-41),Image.Resampling.LANCZOS)
            if key=='aksondol':
                # The runtime draws the exact same mesh in both hands.
                single=pic.resize((int(cardw*.71),int((cardh-41)*.71)),Image.Resampling.LANCZOS)
                pic=Image.new('RGBA',pic.size);pic.alpha_composite(single,(9,5));pic.alpha_composite(single,(85,28))
            out.paste(pic,(x,y+2),pic)
            name=key.replace('_',' ').title();d.text((x+12,y+cardh-42),name,font=font(True,17),fill='#eef0e9')
            d.text((x+13,y+cardh-19),LABELS[key],font=font(False,11),fill='#aec4c5')
    if args.sheet:out.save(args.sheet)
    print(f'{len(PALETTES)} inventory models, {len(PALETTES)*3} animated groups, {total} textured cuboids. All views have a clear center sight lane.')
if __name__=='__main__':main()
