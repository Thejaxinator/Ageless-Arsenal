"""Present the decoded native navigation headers, page bodies and creative item pickers."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

shots,out=map(Path,sys.argv[1:3]);out.mkdir(parents=True,exist_ok=True)
width=1472; bodyw=704
paper='#151d20'; ink='#f0eee6'; secondary='#c5cfc9'; line='#526469'
def font(size):
    return ImageFont.truetype('/usr/share/fonts/truetype/open-sans/OpenSans-Semibold.ttf',size)
def frame(name):
    return Image.open(shots/(name+'.png')).convert('RGB')
def crop_view(im,box):
    scale=min(im.width/800,im.height/450)
    dx=(im.width-800*scale)/2;dy=(im.height-450*scale)/2
    return im.crop(tuple(round(v*scale+(dx if i%2==0 else dy)) for i,v in enumerate(box)))
def place(canvas,view,x,y,w):
    resized=view.resize((w,round(w*view.height/view.width)),Image.Resampling.LANCZOS)
    canvas.paste(resized,(x,y));return resized.height
im=Image.new('RGB',(width,1582),paper);draw=ImageDraw.Draw(im)
draw.text((24,13),'Gunsmith navigation',font=font(36),fill=ink)
draw.line((24,74,width-24,74),fill=line,width=2)
# At each step the native highlighted tool is enlarged above its complete playable body.
for row,(left,right) in enumerate([('01-Destiny-Crafting','02-Destiny-Perks'),('03-Warframe-Blueprints','04-Warframe-Mods')]):
    y=90+row*466
    for col,name in enumerate((left,right)):
        x=24+col*(bodyw+16);f=frame(name)
        tail=place(im,crop_view(f,(393,3,776,66)),x,y,bodyw)
        place(im,crop_view(f,(17,71,783,442)),x,y+tail+4,bodyw)
draw.line((24,1014,width-24,1014),fill=line,width=2)
draw.text((24,1031),'Creative inventory',font=font(30),fill=ink)
for col,name in enumerate(('05-Creative-Warframe','06-Creative-Combat')):
    f=frame(name)
    # The client uses GUI scale 3 at 1440 × 810. Crop to its native picker and page controls.
    cx,cy=f.width//2,f.height//2
    crop=f.crop((cx-321,cy-343,cx+324,cy+286))
    x=24+col*(bodyw+16)
    view=crop.resize((468,456),Image.Resampling.LANCZOS)
    im.paste(view,(x+(bodyw-view.width)//2,1090))
im.save(out/'Ageless-Arsenals-Gunsmith-Navigation.png',optimize=True)
