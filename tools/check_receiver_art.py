"""Audit the packaged item meshes and render a contact sheet of the actual faces and UVs."""
import argparse, json, math
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFont
from tenno_receiver_art import PALETTES
ALLOWED={-45,-22.5,0,22.5,45}
QUADS={'east':(4,5,7,6),'west':(0,2,3,1),'up':(2,6,7,3),'down':(0,1,5,4),'south':(1,3,7,5),'north':(0,4,6,2)}
ANGLES={'east':.96,'west':.8,'up':1.15,'down':.61,'south':.84,'north':.74}
AXES={'braton':13.7,'braton_prime':14.5,'braton_vandal':14.65,'burston':13.9,'burston_prime':14.23,'perigale':13.44,'perigale_prime':14.14,'vesper_77':12.15}
def vertex_array(e):
    a,b=np.array(e['from']),np.array(e['to'])
    m=np.array([[x,y,z] for x in [a[0],b[0]] for y in [a[1],b[1]] for z in [a[2],b[2]]])
    if 'rotation' in e:
        r=e['rotation']; o=np.array(r['origin']);theta=math.radians(r['angle']);co,si=math.cos(theta),math.sin(theta)
        rot=np.array([[1,0,0],[0,co,-si],[0,si,co]])
        m=(m-o)@rot.T+o
    return m
def reserve_center(e,eye,ar=0.20):
    a,b=np.array(e['from']),np.array(e['to'])
    if not a[0]<8+ar or not b[0]>8-ar:return True
    m=vertex_array(e)
    return float(m[:,1].max())<eye-ar or float(m[:,1].min())>eye+ar
def render(elements,atlas,w=440,h=136):
    verts=[vertex_array(el) for el in elements]
    # Three-quarter orthographic, muzzle right. Depth test accounts for every face.
    axes=np.array([[.19,0,-.9818],[-.20,.978,-.039],[.96,.205,.19]])
    p=[m@axes.T for m in verts]; allp=np.concatenate(p)
    bounds=np.ptp(allp,axis=0);z=min((w-28)/bounds[0],(h-12)/bounds[1]); center=(allp.max(0)+allp.min(0))/2
    tex=np.asarray(atlas.convert('RGB'));bg=(20,31,37)
    im=np.full((h,w,3),bg,dtype=np.uint8); depth=np.full((h,w),-999.0)
    def triangle(points,uv,shade):
        a,b,c=points
        mi=np.floor(points[:,:2].min(0)).astype(int);ma=np.ceil(points[:,:2].max(0)).astype(int)
        x0,y0=max(mi[0],0),max(mi[1],0);x1,y1=min(w-1,ma[0]),min(h-1,ma[1])
        if x1<x0 or y1<y0:return
        xv,yv=np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5)
        den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
        if abs(den)<1.e-6:return
        a0=((b[1]-c[1])*(xv-c[0])+(c[0]-b[0])*(yv-c[1]))/den
        a1=((c[1]-a[1])*(xv-c[0])+(a[0]-c[0])*(yv-c[1]))/den;a2=1-a0-a1
        d=depth[y0:y1+1,x0:x1+1];v=a0*a[2]+a1*b[2]+a2*c[2];inside=(a0>=-1e-5)&(a1>=-1e-5)&(a2>=-1e-5)&(v>=d-1e-4)
        if not inside.any():return
        tx=np.clip(((a0*uv[0][0]+a1*uv[1][0]+a2*uv[2][0])*4).astype(int),0,63)
        ty=np.clip(((a0*uv[0][1]+a1*uv[1][1]+a2*uv[2][1])*4).astype(int),0,63)
        im[y0:y1+1,x0:x1+1][inside]=(np.minimum(255,tex[ty[inside],tx[inside]]*shade)).astype(np.uint8);d[inside]=v[inside]
    for el,points in zip(elements,p):
        ps=points.copy();ps[:,0]=w/2+(ps[:,0]-center[0])*z;ps[:,1]=h/2-(ps[:,1]-center[1])*z
        for side,inds in QUADS.items():
            us=el['faces'][side]['uv']; uv=np.array([(us[0],us[1]),(us[2],us[1]),(us[2],us[3]),(us[0],us[3])])
            q=ps[list(inds)]; triangle(q[[0,1,2]],uv[[0,1,2]],ANGLES[side]);triangle(q[[0,2,3]],uv[[0,2,3]],ANGLES[side])
    return Image.fromarray(im)
def run(base, out):
    res=base/'src/main/resources/assets/destiny2mc'
    try:font=ImageFont.truetype('DejaVuSans.ttf',16)
    except OSError:font=ImageFont.load_default()
    w,h=410,145;keys=list(PALETTES)
    sheet=Image.new('RGB',(w*3,74+(h+34)*5),(13,23,28));draw=ImageDraw.Draw(sheet)
    try:head=ImageFont.truetype('DejaVuSans.ttf',27)
    except OSError:head=font
    draw.text((20,7),'AGELESS ARSENALS    /    TENNO FOUNDRY',font=head,fill='#cbe4e0')
    draw.text((21,42),'Weapon model studies     •     15 item meshes',font=font,fill='#7fa29f')
    rows=[]
    for i,key in enumerate(keys):
        m=json.loads((res/'models/item'/(key+'.json')).read_text());axis=AXES.get(key,12.8)
        archive=res/'models/armory'/key
        if 'loader' in m:
            assert m['loader']=='neoforge:separate_transforms'
            el=json.loads((archive/'single.json').read_text())['elements']
            icon=json.loads((archive/'paired_icon.json').read_text())['elements']
            assert len(icon)==2*len(el)
        else:
            el=icon=m['elements']
        moving=[json.loads((res/'models/armory'/key/(p+'.json')).read_text())['elements'] for p in ('body','action','load')]
        assert sum(moving,[])==el and all(moving)
        for e in el:
            assert np.all(np.array(e['from']) < e['to']) and min(e['from'])>=-16 and max(e['to'])<=32
            assert e.get('rotation',{}).get('angle',0) in ALLOWED
            assert set(e['faces'])==set(QUADS)
            for face in e['faces'].values():
                u0,v0,u1,v1=face['uv']
                assert 0<=u0<u1<=16 and 0<=v0<v1<=16 and face['texture']=='#finish'
        hits=[e['name'] for e in el if not reserve_center(e,axis)]
        rows.append((key,len(el),' / '.join(str(len(p)) for p in moving),axis,hits))
        img=render(icon,Image.open(res/'textures/item'/('tenno_'+key+'.png')),w-6,h)
        x=(i%3)*w;y=74+(i//3)*(h+34);sheet.paste(img,(x+3,y));title=key.replace('_',' ').title().replace('Ax 52','AX-52')
        draw.text((x+14,y+h+1),title,font=font,fill='#d5dfdb')
    out.mkdir(parents=True,exist_ok=True);sheet.save(out/'TENNO-RECEIVER-LINEUP.png')
    print('\n'.join(str(r) for r in rows))
    assert all(not r[-1] for r in rows),'solid geometry intersects center sight corridor'
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--project',type=Path,default=Path(__file__).resolve().parents[1]);p.add_argument('--out',type=Path);a=p.parse_args();run(a.project,a.out or a.project/'docs')
