"""Bake first-person grip and ammunition contact points from the authored item geometry."""
import argparse
import json
from pathlib import Path

def contact(elements, names):
    for name in names:
        for elem in elements:
            if elem["name"] == name:
                return [(a+b)/2 for a,b in zip(elem["from"],elem["to"])]
    return None

def bake(models,catalog,output):
    rows=[]
    for row in catalog.read_text().splitlines():
        if not row or row.startswith("#"):
            continue
        key,kind,length,*_=row.split()
        length=float(length)
        z=lambda t:8+(t-12)*length/24
        elements=json.loads((models/"item"/(key+".json")).read_text())["elements"]
        moving=json.loads((models/"armory"/key/"load.json").read_text())["elements"]
        trigger=contact(elements,["raked grip","grip wrap","grip support","rear grip","rubber pistol grip","grip","raked handgrip","black armory grip","ossified wrist","loop front","rear wrist"])
        if trigger is None:
            trigger=[8,5.7,z(16)]
        support=contact(elements,["angled foregrip","dangling foregrip","crimson foregrip","launcher grip"])
        if kind in ("slab","claw","revolver"):
            support=[trigger[0],trigger[1]-.5,trigger[2]-.65]
        elif support is None:
            support=contact(elements,["bronze slide","fore-end","ivory fore-end"])
            if support is None:
                support=[8,6.9,z(8)]
            else:
                support[1]-=.85
        load=contact(moving,["long box magazine","magazine","drum","power cassette","grip cartridge","loading gate","cylinder body","rear loading collar"])
        if kind in ("twin","breach"):
            load=[8,8.3,z(11.4)]
        if kind=="bow":
            support=trigger
            arrow=next(e for e in moving if e['name']=='shaft')
            trigger=[(arrow['to'][0]+arrow['from'][0])/2,(arrow['to'][1]+arrow['from'][1])/2,arrow['to'][2]]
            load=support
        if key=="jotunn":
            cuff=contact(elements,["cuff side","rear wrist cuff"])
            if cuff:
                trigger=[8,cuff[1],cuff[2]]
        if not load:
            load=[8,6.5,z(14)]
        floats=lambda point:', '.join(f'{(v-8)/16:.5f}f' for v in point)
        rows.append(f'        new Mounts(new Point({floats(trigger)}), new Point({floats(support)}), new Point({floats(load)})), // {key}')
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text('''package com.jax.destiny2mc.client;

// Weapon-space coordinates in blocks, generated from the inventory and reload geometry.
final class WeaponHandFit {
    record Point(float x, float y, float z) {}
    record Mounts(Point grip, Point support, Point loader) {}
    static final Mounts[] BY_WEAPON = {
'''+ '\n'.join(rows)+'\n    };\n}\n')

if __name__=='__main__':
    cli=argparse.ArgumentParser(description=__doc__)
    cli.add_argument('models',type=Path);cli.add_argument('catalog',type=Path);cli.add_argument('output',type=Path)
    args=cli.parse_args();bake(args.models,args.catalog,args.output)
