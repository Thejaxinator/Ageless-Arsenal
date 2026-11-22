"""Create the four original foil-pack sprites, crafting recipes and recipe-book entries."""
from pathlib import Path
import argparse
import json
from PIL import Image, ImageDraw

# The dark foil wrapper and repeated bright crimp are common to every channel. Each
# cassette has a different panel arrangement and symbol, readable without relying on hue.
PACKS = {
    'mod_pack':           ('Arsenal Mod Pack', 'Any weapon mod', '',               '#f2c986', '#b08f56', '#585448'),
    'primary_mod_pack':   ('Primary Mod Pack', 'Primary weapon mods', 'iron_ingot', '#a8eeea', '#4bbab2', '#36595d'),
    'secondary_mod_pack': ('Secondary Mod Pack', 'Secondary weapon mods', 'copper_ingot', '#eac0fa', '#b680d1', '#5c4769'),
    'melee_mod_pack':     ('Melee Mod Pack', 'Melee weapon mods', 'flint',         '#ffc3a5', '#df856f', '#68494b')
}

def json_file(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False)+'\n', encoding='utf-8')

def sprite(kind, bright, medium, muted):
    im=Image.new('RGBA', (32,32))
    p=ImageDraw.Draw(im)
    # Short clipped corners with the top and bottom crimps visible even at half resolution.
    p.polygon([(6,1),(24,1),(27,4),(26,9),(27,25),(25,30),(6,30),(3,27),(4,22),(4,6)],fill='#15242a')
    p.polygon([(7,2),(23,2),(26,4),(25,7),(5,7),(5,5)],fill='#819697')
    p.polygon([(6,8),(24,7),(24,25),(5,26)],fill='#475b60')
    p.polygon([(8,8),(23,8),(21,11),(21,22),(17,25),(7,25)],fill='#283a40')
    p.polygon([(7,9),(22,8),(19,11),(8,12)],fill='#3b5053')
    p.polygon([(23,9),(25,8),(25,24),(21,25),(23,21)],fill='#33474d')
    p.line([(6,8),(6,22),(5,25)],fill='#a0a7a0')
    p.line([(4,27),(8,26),(25,25)],fill='#b0aaa0')
    p.polygon([(5,28),(8,27),(24,26),(24,29),(7,29)],fill='#697c7d')
    for x in (7,10,13,16,19,22):
        p.line((x,3,x-1,5),fill='#d1d6c6')
        p.line((x+1,27,x,29),fill='#344a50')
    p.line([(7,7),(15,7),(17,6),(23,6)],fill=bright)
    p.line([(8,23),(11,24),(22,23)],fill=medium)
    # A foil inset with clipped corners leaves space around the embossed center mark.
    p.polygon([(9,13),(12,10),(20,10),(22,12),(22,19),(18,22),(11,22),(8,19)],fill=muted)
    p.polygon([(9,14),(12,11),(20,11),(21,13),(21,18),(18,21),(11,21),(9,19)],fill='#213338')
    if kind=='mod_pack':
        p.line([(11,16),(15,12),(19,16),(15,20),(11,16)],fill=medium,width=2)
        p.line([(12,15),(15,12),(18,15)],fill=bright)
        p.rectangle((14,15,16,17),fill=bright)
    elif kind=='primary_mod_pack':
        # Broad three-chevron magazine mark.
        for x,y in [(11,17),(13,16),(15,15)]:
            p.line([(x,y+2),(x+4,y-2)],fill=medium,width=2)
            p.point((x+4,y-2),fill=bright)
        p.line([(11,13),(14,13)],fill=bright)
    elif kind=='secondary_mod_pack':
        # A pair of short offset sidearm grips.
        p.polygon([(10,15),(13,12),(16,12),(13,15),(12,18),(10,18)],fill=bright)
        p.polygon([(15,17),(18,14),(20,14),(18,17),(17,20),(15,20)],fill=medium)
    else:
        # One long diagonal blade, pommel and a small contrasting crossguard.
        p.polygon([(12,18),(16,13),(20,12),(18,16),(14,19)],fill=bright)
        p.line([(11,17),(15,20)],fill=medium,width=2)
        p.line([(12,19),(10,21)],fill='#d8bf91',width=2)
    p.point((19,9),fill=bright)
    p.line([(5,12),(5,16)],fill='#d2ccc2')
    return im

def make(root):
    assets=root/'assets/destiny2mc';data=root/'data/destiny2mc';lang={}
    for ident,(name,desc,focus,bright,medium,muted) in PACKS.items():
        im=sprite(ident,bright,medium,muted)
        tex=assets/'textures/item/tenno_packs'/(ident+'.png')
        tex.parent.mkdir(parents=True,exist_ok=True);im.save(tex)
        json_file(assets/'models/item'/(ident+'.json'),{
            'parent':'minecraft:item/generated','textures':{'layer0':'destiny2mc:item/tenno_packs/'+ident}
        })
        inputs=['minecraft:paper','minecraft:paper','minecraft:copper_ingot','minecraft:redstone'] if not focus else ['destiny2mc:mod_pack','minecraft:'+focus]
        key='destiny2mc:'+ident
        json_file(data/'recipe'/(ident+'.json'),{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':i} for i in inputs], 'result':{'id':key}})
        json_file(data/'advancement/recipes/misc'/(ident+'.json'),{
            'parent':'minecraft:recipes/root',
            'criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':inputs[0]}]}},
                'recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':key}}},
            'requirements':[['material','recipe']],'rewards':{'recipes':[key]}})
        lang['item.destiny2mc.'+ident]=name
        lang['item.destiny2mc.'+ident+'.hint']=desc+'  •  Use to open'
    lang.update({'tooltip.destiny2mc.mod_pack.duplicate':'Unlocks one missing card at rank 0.',
                 'message.destiny2mc.mod_pack.unlocked':'Unlocked %s  •  Rank 0',
                 'message.destiny2mc.mod_pack.complete':'%s complete. All cards in this pack are already unlocked.'})
    lang_path = assets/'lang/en_us.json'
    translations = json.loads(lang_path.read_text(encoding='utf-8')) if lang_path.exists() else {}
    translations.update(lang)
    json_file(lang_path, translations)
    json_file(data/'tags/item/warframe/mod_packs.json',{'replace':False,'values':['destiny2mc:'+ident for ident in PACKS]})

if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('resources',nargs='?',type=Path,default=Path(__file__).resolve().parents[1]/'src/main/resources')
    make(parser.parse_args().resources)
