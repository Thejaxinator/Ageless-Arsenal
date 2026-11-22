"""Author the Ageless Arsenals native 16×16 ability symbols. Requires Pillow only for authoring.

Run with a destination resources directory, e.g. python tools/ability_glyphs.py src/main/resources.
Each path is drawn on the final pixel grid. White pixels take the player's HUD accent color;
a transparent cut carves a legible counter or separates two parts of a pictogram.
"""
import argparse
import itertools
from pathlib import Path
from PIL import Image, ImageDraw

class Glyph:
    def __init__(self):
        self.image = Image.new('RGBA', (16,16))
        self.pen = ImageDraw.Draw(self.image)

    def stroke(self, *points, width=1, cut=False):
        self.pen.line(points, fill=(0,0,0,0) if cut else 'white', width=width)

    def shape(self, *points, cut=False):
        self.pen.polygon(points, fill=(0,0,0,0) if cut else 'white')

    def ring(self, box, width=1):
        self.pen.ellipse(box, outline='white', width=width)

    def dot(self, x, y, r=1):
        self.pen.ellipse((x-r,y-r,x+r,y+r), fill='white')


def symbols():
    art = {}
    def new(id):
        g = Glyph()
        art[id] = g.image
        return g

    # Grenades. Side profiles, fields or projectiles represent the actual area effect.
    g = new('solar_grenade')
    g.shape((8,1),(9,4),(12,3),(11,6),(14,7),(11,9),(12,12),(9,11),(7,15),(6,11),(3,12),(4,9),(1,8),(4,6),(3,3),(6,4))
    g.ring((5,5,10,10))
    g.shape((6,6),(9,6),(10,8),(8,10),(6,9),(5,8),cut=True)

    g = new('healing_grenade')
    g.stroke((3,4),(6,2),(9,2),(12,4),(14,7),(14,10),(11,13),(6,14),(2,11),(1,8),(2,6),width=2)
    g.shape((7,5),(8,5),(8,7),(10,7),(10,8),(8,8),(8,10),(7,10),(7,8),(5,8),(5,7),(7,7))

    g = new('pulse_grenade')
    g.stroke((1,8),(4,8),(6,3),(8,12),(10,6),(11,8),(14,8),width=2)
    g.stroke((1,4),(2,2),(5,1))
    g.stroke((10,14),(13,13),(14,11))

    g = new('flashbang')
    g.shape((7,1),(9,6),(14,4),(11,8),(15,11),(10,10),(10,15),(7,11),(3,14),(4,10),(0,8),(5,7),(3,3),(7,5))
    g.stroke((7,7),(9,9),width=2,cut=True)

    g = new('vortex_grenade')
    g.stroke((2,7),(3,4),(6,2),(10,1),(13,3),(15,6),width=2)
    g.stroke((14,8),(12,12),(9,14),(5,14),(2,12),(1,10),width=2)
    g.stroke((4,8),(6,5),(10,4),(12,6),(12,8),(9,10),(6,10),(5,8),(8,7),width=2)

    g = new('suppressor_grenade')
    g.stroke((7,1),(12,4),(14,8),(12,12),(8,14),(4,12),(2,8),(4,4),(7,1),width=2)
    g.stroke((4,8),(11,8),width=2)
    g.stroke((8,4),(8,5))
    g.stroke((8,11),(8,12))

    g = new('duskfield_grenade')
    g.stroke((1,11),(2,7),(4,4),(7,3),(10,3),(12,5),(14,10),width=2)
    g.stroke((1,11),(4,13),(11,13),(14,11))
    g.shape((7,5),(9,5),(10,8),(8,11),(6,8))
    g.stroke((3,10),(4,9))
    g.stroke((11,10),(12,9))

    g = new('coldsnap_grenade')
    g.shape((1,13),(3,8),(5,9),(4,13))
    g.shape((5,12),(7,4),(9,5),(9,10))
    g.shape((9,10),(12,1),(14,2),(14,6))
    g.stroke((2,14),(7,13),(11,10),(14,8))

    g = new('shackle_grenade')
    g.stroke((1,4),(3,2),(6,2),(10,6),(10,8),(8,10),width=2)
    g.stroke((14,11),(12,13),(9,13),(5,9),(5,7),(7,5),width=2)
    g.stroke((2,5),(3,6))
    g.stroke((12,8),(13,9))

    g = new('threadling_grenade')
    for x,y in ((1,12),(4,7),(7,2)):
        g.stroke((x,y),(x+2,y+1),(x+4,y+1),(x+6,y-1),width=2)
        g.shape((x+4,y-1),(x+7,y-1),(x+6,y+1))

    g = new('freezing_singularity')
    g.shape((8,1),(11,6),(8,11),(5,6))
    g.stroke((8,4),(8,7),cut=True)
    g.stroke((3,5),(1,8),(2,11),(5,13),(10,13),(13,11),width=2)
    g.stroke((13,6),(14,8),(12,10),(9,11))

    g = new('hailfire_spike')
    g.shape((5,1),(9,2),(8,9),(6,11),(5,8))
    g.stroke((6,3),(7,3),cut=True)
    g.shape((1,8),(5,12),(7,12),(11,7),(10,11),(14,9),(13,13),(9,15),(5,14),(2,12))

    # Melees. Aimed diagonals allow long weapons to read even at twelve GUI pixels.
    g = new('incinerator_snap')
    g.shape((2,14),(1,10),(3,8),(4,7),(4,5),(5,5),(6,9),(8,6),(8,4),(10,4),(10,7),(8,10),(10,9),(11,8),(12,9),(10,12),(7,14))
    g.stroke((9,1),(10,2))
    g.stroke((12,3),(14,2))
    g.stroke((12,5),(14,6))

    g = new('throwing_hammer')
    g.shape((2,13),(8,6),(10,7),(4,15))
    g.shape((5,3),(8,1),(10,2),(10,3),(14,6),(14,8),(11,10),(9,8),(9,6),(6,5))
    g.stroke((7,3),(8,3),cut=True)
    g.stroke((11,7),(12,6),cut=True)
    g.stroke((1,8),(3,6))
    g.stroke((1,5),(3,3))

    g = new('chain_lightning')
    g.shape((9,0),(4,7),(7,7),(4,14),(12,5),(8,5))
    g.stroke((2,4),(1,7),(3,9),width=2)
    g.stroke((11,9),(14,9),(14,12),(12,13),width=2)

    g = new('thunderclap')
    g.shape((1,11),(4,8),(4,6),(6,6),(7,7),(8,5),(11,7),(12,10),(8,14),(5,14),(3,15))
    g.stroke((5,8),(7,10),cut=True)
    g.stroke((8,7),(9,8),cut=True)
    g.stroke((4,4),(3,2),width=2)
    g.stroke((8,3),(8,1),width=2)
    g.stroke((11,4),(13,2),width=2)
    g.stroke((13,6),(15,6))

    g = new('shield_throw')
    g.shape((6,4),(12,1),(15,3),(14,8),(11,11),(7,13),(5,9))
    g.stroke((7,5),(12,3),(13,4),(12,8),(8,11),(7,8),(7,5),cut=True)
    g.stroke((1,6),(4,4))
    g.stroke((0,10),(3,8))
    g.stroke((2,13),(5,11))

    g = new('pocket_singularity')
    g.ring((8,1,14,7),2)
    g.stroke((1,13),(4,8),(7,6),width=2)
    g.stroke((5,14),(8,11),(11,9),width=2)
    g.stroke((1,7),(3,4),(6,3))

    g = new('shiver_strike')
    g.shape((1,10),(5,7),(5,4),(7,5),(8,2),(10,4),(13,2),(12,6),(14,6),(12,10),(7,13),(3,14))
    g.stroke((5,9),(7,11),cut=True)
    g.stroke((8,5),(9,8),cut=True)
    g.stroke((2,7),(1,8))
    g.stroke((3,4),(1,5))

    g = new('penumbral_blast')
    g.stroke((2,14),(9,6),width=2)
    g.shape((7,2),(9,5),(11,1),(13,2),(12,6),(15,4),(14,7),(10,8),(7,6))
    g.stroke((5,1),(5,2))
    g.stroke((14,10),(12,10))
    g.stroke((5,6),(3,5))

    g = new('threaded_spike')
    g.shape((3,10),(5,7),(14,1),(11,7),(6,10))
    g.stroke((5,9),(1,11),(1,13),(3,14),(7,14),(10,12))
    g.stroke((9,11),(11,11),(11,13))

    g = new('arcane_needle')
    g.shape((1,14),(5,8),(14,1),(9,9))
    g.stroke((5,5),(9,1))
    g.stroke((10,10),(14,6))
    g.stroke((9,14),(12,11))

    g = new('prism_cleave')
    g.shape((2,13),(2,10),(7,5),(14,1),(10,8),(5,13))
    g.stroke((4,10),(10,5),cut=True)
    g.stroke((1,6),(4,3),(8,1))
    g.stroke((7,14),(11,12),(14,8),width=2)

    g = new('storm_snare')
    g.stroke((2,3),(4,2),(7,3),(8,5),(6,8),(3,9),(2,7),(4,5),width=2)
    g.shape((11,2),(8,8),(11,8),(9,14),(15,6),(12,6))

    # Class abilities: two readable runner poses, two well profiles and two different cover heights.
    g = new('marksmans_dodge')
    g.dot(8,4)
    g.stroke((7,6),(5,9),(7,10),(9,14),width=2)
    g.stroke((5,9),(3,12),(1,12),width=2)
    g.stroke((7,6),(10,7),(12,5),width=2)
    g.shape((11,4),(15,4),(15,5),(13,5),(12,7))
    g.stroke((1,6),(4,6))
    g.stroke((2,3),(5,3))

    g = new('gamblers_dodge')
    g.dot(8,6)
    g.stroke((7,8),(5,9),(5,11),(8,12),(10,10),width=2)
    g.stroke((4,6),(3,4),(5,2),(9,1),(12,2),(14,5),width=2)
    g.shape((11,4),(15,4),(14,7))
    g.stroke((1,9),(2,12),(5,14),(10,14),(13,11),width=2)

    g = new('healing_rift')
    g.stroke((1,10),(3,8),(5,8))
    g.stroke((11,8),(13,9),(14,11),(11,13),(5,13),(1,11),width=2)
    g.shape((7,1),(9,1),(9,4),(12,4),(12,6),(9,6),(9,9),(7,9),(7,6),(4,6),(4,4),(7,4))

    g = new('empowering_rift')
    g.stroke((1,11),(4,9))
    g.stroke((12,9),(14,11),(11,13),(5,13),(1,11),width=2)
    g.shape((5,6),(8,1),(11,6),(9,6),(9,10),(7,10),(7,6))
    g.stroke((2,7),(4,3))
    g.stroke((12,3),(14,6))

    g = new('towering_barricade')
    g.shape((1,5),(4,2),(11,2),(14,5),(14,13),(12,13),(12,6),(10,4),(5,4),(3,6),(3,13),(1,13))
    g.stroke((5,6),(5,12))
    g.stroke((8,5),(8,11))
    g.stroke((10,6),(10,12))

    g = new('rally_barricade')
    g.shape((1,9),(4,7),(6,8),(6,10),(9,10),(9,8),(12,7),(14,9),(14,13),(12,13),(12,10),(10,12),(5,12),(3,10),(3,13),(1,13))
    g.stroke((5,4),(7,6),(9,4),width=2)
    g.stroke((6,1),(8,1))

    # Passives are shown in the vanilla-style inventory drawer.
    g = new('triple_jump')
    for y in (1,6,11):
        g.stroke((3,y+3),(7,y),(11,y+3),width=2)

    g = new('strafe_lift')
    g.shape((1,8),(5,3),(6,5),(4,8),(8,8),(8,10),(4,10),(6,13))
    g.shape((9,5),(12,2),(15,5),(13,5),(13,10),(11,10),(11,5))
    g.stroke((10,13),(14,13))

    g = new('burst_glide')
    g.shape((1,3),(13,5),(14,6),(11,9),(7,13),(6,10),(3,9),(7,7))
    g.stroke((4,5),(10,6),cut=True)
    g.stroke((1,13),(4,11))

    g = new('ember_of_solace')
    g.shape((8,1),(10,5),(10,7),(13,6),(14,10),(12,13),(8,15),(4,13),(2,10),(4,6),(5,8))
    g.shape((7,8),(9,8),(9,10),(11,10),(11,11),(9,11),(9,13),(7,13),(7,11),(5,11),(5,10),(7,10),cut=True)

    g = new('spark_of_haste')
    g.shape((8,2),(13,2),(9,7),(14,6),(5,14),(7,9),(3,9))
    g.stroke((1,5),(5,5))
    g.stroke((1,12),(4,12))

    g = new('echo_of_reprisal')
    g.stroke((1,8),(4,5),(7,4),(10,4),(14,7),(11,10),(8,11),(5,11),(1,8),width=2)
    g.stroke((3,14),(12,1),width=2)
    g.dot(8,8)

    g = new('whisper_of_chains')
    g.stroke((2,2),(5,1),(9,5),(9,7),(7,9),(5,9),(1,5),(2,2),width=2)
    g.stroke((8,7),(10,6),(14,10),(14,12),(12,14),(10,14),(6,10),width=2)
    g.stroke((2,10),(2,14))
    g.stroke((0,12),(4,12))

    g = new('thread_of_warding')
    g.stroke((2,3),(7,1),(13,3),(13,8),(11,11),(7,14),(4,12),(2,8),(2,3),width=2)
    g.stroke((5,6),(6,5),(8,7),(10,5),(11,6),(8,10),(5,6))

    g = new('unbound')
    g.stroke((3,6),(3,3),(6,3))
    g.stroke((9,3),(12,3),(12,6))
    g.stroke((12,9),(12,12),(9,12))
    g.stroke((6,12),(3,12),(3,9))

    g = new('unidentified')
    g.stroke((7,2),(13,8),(7,14),(1,8),(7,2))
    g.stroke((6,6),(8,6),(8,8),(7,8))
    g.dot(7,10,0)

    return art

def validate(art):
    for size in [12,16]:
        images = {}
        for name, image in art.items():
            mask = image.resize((size,size),Image.Resampling.NEAREST).getchannel('A')
            pixels = mask.tobytes()
            assert set(pixels) == {0,255}, name
            occupied = {i for i,value in enumerate(pixels) if value}
            assert len(occupied) >= 8 and len(occupied) < size*size*.7, (name, size)
            images[name] = occupied
        closest = max((len(a & b) / len(a | b), x,y) for (x,a),(y,b) in itertools.combinations(images.items(),2))
        assert closest[0] < .82, closest
        print(f'{size:2} px: {len(art)} distinct masks; highest pair overlap {closest[0]:.3f} ({closest[1]}, {closest[2]})')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('resources',type=Path)
    args = parser.parse_args()
    out = args.resources/'assets/destiny2mc/textures/gui/sprites/binder/ability'
    out.mkdir(parents=True,exist_ok=True)
    art = symbols()
    validate(art)
    for name,image in art.items():
        image.save(out/(name+'.png'), optimize=True)
