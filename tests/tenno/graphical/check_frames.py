"""Inspect the captured native framebuffers for ownership styles, model coverage and clipping."""
import argparse
from pathlib import Path
import numpy as np
from PIL import Image

def load(folder,name):
    im=Image.open(folder/(name+'.png')).convert('RGB')
    assert im.size==(1440,810),(name,im.size)
    return np.asarray(im)
def tint_count(image,hex_value):
    rgb=tuple(bytes.fromhex(hex_value))
    return int(np.all(image==rgb,axis=-1).sum())
def run(folder,all_models):
    for slug,color in [('Blueprint-Card','dbc695'),('Card-sun_and_moon','dbc695'),('Card-cedo_prime','eed08a')]:
        count=tint_count(load(folder,slug),color)
        assert count>1100,(slug,count)
        print(f'TENNO FRAME {slug}: {count} pixels of its Arsenal accent')
    for slug in ['Foundry','Card-ace_of_spades','Card-bread']:
        r=load(folder,slug)
        for col in ['dbc695','eed08a']:
            assert tint_count(r,col)<600,(slug,col)
        print('TENNO FRAME correct independent styling: '+slug)
    guarded=load(folder,'Guard-sun_and_moon')
    armed=load(folder,'Equipped-twin_grakatas')
    aim=load(folder,'ADS-Burston')
    # The camera view stays fixed in the range. Both guard and dual wield must change
    # a substantial area of each side; icon or selected-item text alone cannot pass.
    for area in [np.s_[365:670,190:680],np.s_[365:670,760:1250]]:
        delta=np.abs(guarded[area].astype('int16')-armed[area].astype('int16')).max(axis=-1)
        assert (delta>20).sum()>1500
    print('TENNO FRAME crossed guard and dual equipment change both halves of the first-person view')
    # The target and sight are present on the optical centerline at different heights.
    target=aim[275:390,690:750];upper=np.asarray(target,dtype=np.int16)
    assert (((upper[:,:,0]-upper[:,:,1])>28)&(upper[:,:,0]>120)).sum()>180
    post=aim[405:645,635:805].astype('int16')
    assert ((post.max(-1)-post.min(-1)<55)&(post.min(-1)>110)).sum()>1000
    print('TENNO FRAME Burston target remains visible directly above its central sight')
    if all_models:
        models=sorted(folder.glob('Mesh-*.png'));assert len(models)==33
        for fn in models:
            a=load(folder,fn.stem);bg=(25,41,50);assert tuple(a[0,0])==bg
            mask=np.max(np.abs(a.astype('int16')-bg),axis=-1)>4
            y,x=np.where(mask);assert len(x)>650
            assert min(x.min(),y.min(),a.shape[1]-1-x.max(),a.shape[0]-1-y.max())>1,fn.name
            missing=(a[:,:,0]>150)&(a[:,:,1]<20)&(a[:,:,2]>150)
            assert missing.sum()<max(500,.02*len(x)),fn.name
        print('TENNO FRAME 33/33 registered weapons appear in distinct unclipped native item frames')
if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('shots',type=Path);p.add_argument('--models',action='store_true')
    a=p.parse_args();run(a.shots,a.models)
