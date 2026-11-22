"""Regenerate the checked-in wearable meshes, painted UVs and inventory renders.

Run from the release checkout: python tools/armor_art.py src/main
Requires Python 3, Pillow and NumPy. The runtime loads only generated Java and PNG assets.
"""
from pathlib import Path
import argparse,hashlib,json
from PIL import Image
import numpy as np
from armor_shapes import pieces,sets

# The names represent materials in the individual armor sculpts, not biome tints.
colors={
'kabr':dict(cloth='373d47',metal='958667',trim='c4ab71',plate='537989',mark='dfded0',dark='252b30',iron='61676b',lens='ed5036',signal='78cce2'),
'willbreaker':dict(cloth='37313d',iron='524d45',dark='27232b',plate='777051',bone='beb084',mark='753a42',trim='94805e',signal='9ed554',lens='d8ef83'),
'rull':dict(cloth='61516f',iron='787584',plate='b8b5be',bone='e6e2d9',trim='c7a25b',mark='78538f',dark='45404d',signal='f2e8ba'),
'saint14':dict(iron='626d7d',dark='292830',plate='bac0bd',metal='8d93a1',trim='9d8472',signal='d295f7'),
'armamentarium':dict(cloth='3e4643',cloth2='6e604e',plate='888e78',metal='67795c',iron='566262',trim='b3b2a0',mark='bea479'),
'bakris':dict(cloth='44424a',bone='cdc7b5',plate='a9a7a1',iron='5a5a5f',dark='353337',metal='7d7777',mark='665055',signal='91d6ff')}
joints={'head':(0,0,0),'hat':(0,0,0),'body':(0,0,0),'right_arm':(-5,2,0),'left_arm':(5,2,0),'right_leg':(-1.9,12,0),'left_leg':(1.9,12,0)}
A=256
def rgb(hexcode):return np.array(tuple(bytes.fromhex(hexcode)),dtype=float)
def noise(x,y,seed):return ((x*37+y*71+x*y*11+seed*17)%19)-9
def tex(face,box,base,seed):
    x0,y0,w,h=face; im=Image.new('RGB',(w,h))
    linen=box.material in ('cloth','cloth2');lit=box.material in ('signal','lens');ivory=box.material=='bone'
    for y in range(h):
        for x in range(w):
            val=noise(x,y,seed)*(.32 if lit else .55)+(3-y/max(1,h-1)*6)
            if min(w,h)>1:
                if x==0:val+=6
                if x==w-1:val-=6
                if y==0:val+=12 if not linen else 5
                if y==h-1:val-=8
            if linen:val+=2 if (x+2*y)%3==1 else -1
            if min(w,h)>=3 and w*h>12 and not lit:
                # Short authored edge nicks and a retained raised midpoint rather than full-surface dithering.
                if ((x*13+y*7+seed)%31)==0:val+=23
                if (x+y+seed)%29==0 and not linen:val-=12
                if min(w,h)>4:
                    # In small exposed plates, an interrupted bevel or pitted bone
                    # catches the same directional highlight as the silhouette.
                    if ivory and x==1+seed%max(1,w-3) and y in (1,2): val-=16
                    if not linen and not ivory and y==h-2 and 1<x<w-2: val-=10
                    if linen and x==1 and (y+seed)%4<2: val+=10
                if y==h-2 and x in (1,w-2) and not ivory and not linen:val-=7
            if lit:val+=18 if (x+y)%4==1 else 0
            im.putpixel((x,y),tuple(np.clip(base+val,0,255).astype(int)))
    return im
def face_regions(w,h,d):
    return [(d,0,w,d),(d+w,0,w,d),(0,d,d,h),(d,d,w,h),(d+w,d,d,h),(2*d+w,d,w,h)]
def decimal(n):
    return ('%g'%n)+'f'
def rotate(turn):
    r=np.radians(turn);c=np.cos(r);s=np.sin(r)
    mx=np.array([[1,0,0],[0,c[0],-s[0]],[0,s[0],c[0]]])
    my=np.array([[c[1],0,s[1]],[0,1,0],[-s[1],0,c[1]]])
    mz=np.array([[c[2],-s[2],0],[s[2],c[2],0],[0,0,1]])
    return mz@my@mx
faces=[(4,5,1,0),(3,2,6,7),(4,0,3,7),(0,1,2,3),(1,5,6,2),(5,4,7,6)]
def projected_icon(item, atlas,rects,big=False,yaw=-24,pitch=10):
    # The four corners of each face go through exactly the same joint-local transforms
    # used by Minecraft. A software Z buffer avoids drawing a far pauldron over the chest.
    triangles=[];points=[];matrix=rotate([pitch,yaw,0])
    vertices=np.array([(-1,-1,-1),(1,-1,-1),(1,1,-1),(-1,1,-1),(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)])
    for k,box in enumerate(pieces[item]):
        center=np.array(box.center)+joints[box.joint]
        half=np.array(box.size)/2+box.inflate
        xyz=((vertices*half)@rotate(box.turn).T+center)@matrix.T
        points.append(xyz)
        ux,uy=rects[(item,k)]
        for idx,(corners,uv) in enumerate(zip(faces,face_regions(*box.size))):
            a,b,w,h=uv
            p=xyz[list(corners)]
            # faces ordered clockwise in screen coordinates from outside; derive true outward normal.
            normal=(p.mean(axis=0)-xyz.mean(axis=0));normal=normal/(np.linalg.norm(normal) or 1)
            light=.77+.25*max(0,np.dot(normal,[-.4,-.6,-.71]))
            shade=1 if box.material in ('signal','lens') else light
            if normal[2]>.0001:continue
            uvpts=np.array([(ux+a+.03,uy+b+.03),(ux+a+w-.03,uy+b+.03),(ux+a+w-.03,uy+b+h-.03),(ux+a+.03,uy+b+h-.03)])
            for tri in [(0,1,2),(0,2,3)]:triangles.append((p[list(tri)],uvpts[list(tri)],shade))
    radii=np.vstack(points); lo=radii[:,:2].min(axis=0); hi=radii[:,:2].max(axis=0)
    size=256 if big else 32; inset=4 if big else 1.75;factor=(size-2*inset)/max(hi-lo)
    center=(hi+lo)/2; target=np.zeros((size,size,4),dtype=np.uint8); zbuf=np.full((size,size),np.inf)
    pixels=np.asarray(atlas.convert('RGB'))
    for v,uv,shade in triangles:
        xy=(v[:,:2]-center)*factor+size/2;p0,p1,p2=xy;u=p1-p0;w=p2-p0;den=u[0]*w[1]-u[1]*w[0]
        if abs(den)<1e-7:continue
        bound0=np.maximum(0,np.floor(xy.min(axis=0)).astype(int));bound1=np.minimum(size,np.ceil(xy.max(axis=0)).astype(int))
        x0,y0=bound0;x1,y1=bound1
        yy,xx=np.mgrid[y0:y1,x0:x1]
        dx=xx+.5-p0[0];dy=yy+.5-p0[1]
        wa=(dx*w[1]-dy*w[0])/den;wb=(dy*u[0]-dx*u[1])/den;wc=1-wa-wb
        depth=wc*v[0,2]+wa*v[1,2]+wb*v[2,2];zb=zbuf[y0:y1,x0:x1]
        visible=(wa>=-1e-6)&(wb>=-1e-6)&(wc>=-1e-6)&(depth<zb)
        U=(wc*uv[0,0]+wa*uv[1,0]+wb*uv[2,0]).astype(int);V=(wc*uv[0,1]+wa*uv[1,1]+wb*uv[2,1]).astype(int)
        block=target[y0:y1,x0:x1];block[visible,:3]=(pixels[V[visible],U[visible]]*shade).clip(0,255).astype(np.uint8)
        block[visible,3]=255;zb[visible]=depth[visible]
    return Image.fromarray(target)
def generate(root):
    source=root/'java/com/jax/destiny2mc/client/armor';assets=root/'resources/assets/destiny2mc'
    argsrc=source/'ArmorGeometry.java'
    for p in [source,assets/'textures/models/armor',assets/'textures/item',assets/'models/item']:p.mkdir(parents=True,exist_ok=True)
    cases=[]
    for family,items in sets.items():
        atlas=Image.new('RGB',(A,A),'#29282d');rects={};cx=1;cy=1;row=0
        for item in items:
            lines=['            case "'+item+'" -> {'];seen={}
            for k,box in enumerate(pieces[item]):
                w,h,d=box.size;tw=2*w+2*d;th=h+d
                if cx+tw+1>A:cx=1;cy+=row+2;row=0
                assert cy+th+1<=A,(family,cy,th)
                rects[(item,k)]=(cx,cy);pal=rgb(colors[family][box.material])
                seed=int(hashlib.sha256((item+str(k)).encode()).hexdigest()[:6],16)
                for i,uv in enumerate(face_regions(w,h,d)):
                    face=tex(uv,box,pal,seed+i)
                    atlas.paste(face,(cx+uv[0],cy+uv[1]))
                name=box.name
                count=seen.get((box.joint,name),0)+1;seen[(box.joint,name)]=count
                if count>1:name+='_'+str(count)
                params=', '.join(decimal(v) for v in [*box.center,*box.size,*box.turn,box.inflate])
                lines.append(f'                box(root, "{box.joint}", "{name}", {cx}, {cy}, {params});')
                cx+=tw+2;row=max(row,th)
            cases.append('\n'.join(lines+['            }']))
        for layer in ([1,2] if len(items)>1 else [1]):
            atlas.save(assets/f'textures/models/armor/{family}_layer_{layer}.png',optimize=True)
        for item in items:
            pic=projected_icon(item,atlas,rects)
            assert pic.getchannel('A').getbbox() is not None
            pic.save(assets/f'textures/item/{item}.png',optimize=True)
            (assets/f'models/item/{item}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'destiny2mc:item/'+item}},indent=2)+'\n')
    body='''package com.jax.destiny2mc.client.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated by tools/armor_art.py from tools/armor_shapes.py. Each piece owns only
 * its own cubes. The seven empty vanilla roots adopt the wearer's current pose. */
final class ArmorGeometry {
    static final String[] ITEMS = {
ITEMS
    };
    static LayerDefinition layer(String id) {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
JOINTS
        switch (id) {
CASES
            default -> throw new IllegalArgumentException("Unregistered wearable " + id);
        }
        return LayerDefinition.create(mesh,256,256);
    }
    private static void box(PartDefinition root,String joint,String name,int u,int v,
                            float x,float y,float z,float w,float h,float d,
                            float rx,float ry,float rz,float inflation) {
        var builder = CubeListBuilder.create().texOffs(u,v).addBox(-w/2,-h/2,-d/2,w,h,d,
            new CubeDeformation(inflation));
        root.getChild(joint).addOrReplaceChild(name,builder,PartPose.offsetAndRotation(x,y,z,
            (float)Math.toRadians(rx),(float)Math.toRadians(ry),(float)Math.toRadians(rz)));
    }
    private ArmorGeometry() {}
}
'''
    body=body.replace('ITEMS\n',',\n'.join('        '+json.dumps(i) for i in pieces)+'\n')
    body=body.replace('JOINTS','\n'.join('        root.addOrReplaceChild("'+j+'", CubeListBuilder.create(), PartPose.offset('+', '.join(map(decimal,pos))+'));' for j,pos in joints.items()))
    argsrc.write_text(body.replace('CASES','\n'.join(cases)))
    print(len(pieces),'wearable pieces;',sum(map(len,pieces.values())),'articulated shell parts')
if __name__=='__main__':
    ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('main',type=Path)
    generate(ap.parse_args().main)
