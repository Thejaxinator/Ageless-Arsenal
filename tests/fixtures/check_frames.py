"""Validate native 1440×810 entity renders. python check_frames.py path/to/shots"""
from itertools import combinations
from pathlib import Path
import sys
import numpy as np
from PIL import Image

shots = Path(sys.argv[1])
suits = ('Kabr', 'Willbreaker', 'Rull', 'Saint-14', 'Armamentarium', 'Bakris')
images = {}
front = np.s_[139:706, 25:435]
for suit in suits:
    with Image.open(shots / (suit + '.png')) as image:
        assert image.size == (1440,810), (suit, image.size)
        raw = np.asarray(image.convert('RGB'), dtype='int16')
    images[suit] = raw
    for area in (front, np.s_[139:706, 472:860], np.s_[142:700, 905:1390]):
        panel = raw[area]
        backdrop = raw[area[0], 2:3]
        occupied = np.max(np.abs(panel - backdrop), axis=2) > 25
        assert occupied.mean() > 0.13, (suit, area, occupied.mean())
        # Bright magenta in a checker texture signals an unresolved Minecraft asset.
        r,g,b = panel.transpose(2,0,1)
        missing = (r>110) & (b>110) & (g<12) & (np.abs(r-b)<16)
        assert missing.mean() < .008, (suit, 'unresolved texture', missing.mean())
    print('ARMOR PIXEL front, reverse and detail are occupied on', suit)

def distinct(a,b,area,min_ratio):
    difference=np.max(np.abs(images[a][area]-images[b][area]),axis=2)>36
    assert difference.mean()>min_ratio, (a,b,difference.mean())
    print('ARMOR PIXEL', a, '/', b, 'changed',f'{difference.mean():.1%}','of comparison rectangle')
for a,b in combinations(suits[:3],2):
    distinct(a,b,front,.05)
distinct('Kabr','Saint-14',np.s_[147:360,117:359],.055)
distinct('Willbreaker','Bakris',np.s_[147:360,117:359],.055)
distinct('Kabr','Armamentarium',np.s_[300:492,86:395],.075)
print('ARMOR PIXEL all six kits confirmed')
