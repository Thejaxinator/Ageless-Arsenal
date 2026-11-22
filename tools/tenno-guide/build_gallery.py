"""Produce a portable offline weapon list from this release's stock prices, actual game captures and profiles."""
import base64, html, io, json, re, sys
from pathlib import Path
from urllib.parse import quote
from PIL import Image,ImageChops
from foundry.content import rows
HERE=Path(__file__).resolve().parent
OUT=HERE.parents[1]/'docs'
GUNS=HERE.parents[1]/'src/main/java/com/jax/destiny2mc/tenno/TennoGunSpec.java'
MELEE=HERE.parents[1]/'src/main/resources/tenno/melee.json'
def wiki(name):return 'https://wiki.warframe.com/w/'+quote(name.replace(' ','_'),safe='-_')
def costpairs(raw):
    return [(int(s.split('=')[1]),s.split('=')[0].removeprefix('d/').replace('_',' ')) for s in raw.split()]
def page():
    source=GUNS.read_text()
    gun={x[0].lower():x[1:] for x in re.findall(r'^    ([A-Z_0-9]+)\("([^"]+)", Family\.([A-Z_]+), "([^"]+)", (\d+), ([.\df]+), (\d+), (\d+), (\d+)\)[,;]',source,re.M)}
    families={x[0]:x[1:] for x in re.findall(r'^        ([A-Z_]+)\("([^"]+)",\s*"([^"]+)"\)[,;]',source,re.M)}
    override=dict(re.findall(r'if\s*\(this\s*==\s*([A-Z_0-9]+)\)\s*return\s+"([^"]+)"',source))
    melee={x['id']:x for x in json.loads(MELEE.read_text())}
    names={row[0]:row[1] for row in rows}
    imgs={'AX-52':'https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/AK47Weapon.png'}
    for mod in ['artA','artB','melee']:
        for entry in json.loads((HERE/mod/'refs/catalog.json').read_text()):
            if isinstance(entry,dict):imgs[entry['name']]=entry.get('icon') or entry.get('image')
            else:imgs[entry[0]]=entry[1]
    if len(sys.argv)>1:shots=Path(sys.argv[1])
    else:shots=OUT/'previews/tenno'
    intro='Minecraft 1.21.1 · NeoForge · 33 weapons'
    tiles=[];md=['# Ageless Arsenals · Warframe roster','',intro,'',
        'Open **Warframe → Blueprints** at either Gunsmith to assemble equipment. Costs and damage here are the Minecraft adaptations. Each weapon links to its Warframe Wiki reference.','',
        '| Weapon | Slot | Ageless Arsenals action | Foundry ingredients |','|---|---|---|---|']
    for ident,name,slot,variant,ingredients in rows:
        if ident in gun:
            _,family,kind,mag,power,delay,reload,reach=gun[ident]
            action,tech=families[family]
            tech=override.get(ident.upper(),tech)
            stats=[('Damage',power.strip('f')+' HP'),('Battery' if family=='BUBONICO' else 'Magazine',mag),('Range',reach+' blocks'),('Trigger',action)]
            if family!='BUBONICO':stats.append(('Reload',('%.2f'%(int(reload)/20)).rstrip('0').rstrip('.')+' s'))
        else:
            v=melee[ident];kind=v['family'];action=v['trait'];tech=v['description']
            stats=[('Damage',f"{v['damage']} HP"),('Reach',f"{v['reach']} blocks"),('Swing rate',f"{v['attackSpeed']}/s"),('Guard',str(v['guardAngle'])+'°')]
        costs=' · '.join(f'{c} {n}' for c,n in costpairs(ingredients))
        md.append(f'| [{name}]({wiki(name)}) | {slot.title()} | {action}. {tech} | {costs} |')
        f=shots/('Mesh-'+ident+'.png')
        im=Image.open(f).convert('RGB')
        base=Image.new('RGB',im.size,im.getpixel((0,0)))
        mask=ImageChops.difference(im,base).convert('L').point(lambda v:255 if v>3 else 0)
        box=mask.getbbox();assert box,(ident,'empty frame')
        im=im.crop(box);im.thumbnail((600,315),Image.Resampling.LANCZOS)
        b=io.BytesIO();im.save(b,format='WEBP',lossless=True,method=4)
        src='data:image/webp;base64,'+base64.b64encode(b.getvalue()).decode()
        extra=imgs.get(name)
        t='<dl>'+''.join('<div><dt>'+html.escape(k)+'</dt><dd>'+html.escape(v)+'</dd></div>' for k,v in stats)+'</dl>'
        links='<a href="'+wiki(name)+'">Warframe Wiki ↗</a>'
        if extra:links+=' <a href="'+html.escape(extra,quote=True)+'">Reference render ↗</a>'
        tiles.append(f'<article data-slot="{slot}" data-name="{html.escape(name.lower(),quote=True)}" id="{ident}"><div class="art"><img src="{src}" alt="Minecraft model of {html.escape(name,quote=True)}"></div><div class="body"><small>{slot.upper()} · {variant.upper()}</small><h2>{html.escape(name)}</h2><div class="kind">{html.escape(kind)}</div><p>{html.escape(tech)}</p>{t}<h3>Foundry</h3><p class="cost">{html.escape(costs)}</p><div class="links">{links}</div></div></article>')
    assert len(gun)==27 and len(melee)==6 and len(tiles)==33
    template=(HERE/'roster-template.html').read_text()
    (OUT/'WARFRAME-FIELD-GUIDE.html').write_text(template.replace('<!--CARDS-->','\n'.join(tiles)))
    (OUT/'WARFRAME-ROSTER.md').write_text('\n'.join(md)+'\n')
if __name__=='__main__':page()
