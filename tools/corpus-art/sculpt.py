"""Ageless Arsenals Corpus sculptures (Warframe). Y points down, front is -Z, body pivot is ground (y=24)."""
from math import atan2, hypot, degrees
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
REPO_ROOT = ROOT.parents[1]
sys.path.insert(0, str(REPO_ROOT / 'tools' / 'dread-art' / 'tools'))
from rig_tools import Rig

# Shared 16-slot material palette indices (each maps to a 64x64 tile in the 512x128 sheet).
INK, RUBBER, CLOTH, DEEP, PLATE, EDGE, STEEL, PEARL, BONE, BRASS, GOLD, LIGHT, POWER, GLOW, TRIM, MARK = range(16)
BASE = (
    '15181d 262a32 9c7c5c 323d47 566570 7f909c 98a4ad ced5da '
    '755b42 9c763a d7a748 a4f8ff 2ac2e8 5ce6ff 1f3944 c68a39'
)


def palette(**overrides):
    colors = BASE.split()
    for key, color in overrides.items():
        colors[globals()[key]] = color
    return [list(bytes.fromhex(c)) for c in colors]


def cap(r, n, x, y, z, w, h, d, m):
    """Chamfered industrial Corpus block (beveled corners without excessive bone count)."""
    r.box(n, x, y, z, w * 0.78, h, d, m)
    r.box(n, x, y + h * 0.10, z, w, h * 0.80, d * 0.84, m)


def bar(r, name, parent, start, end, width, depth, color):
    """Orient local +Y down a mechanical strut or hydraulic piston between two points."""
    x, y, z = start
    dx, dy, dz = [b - a for a, b in zip(start, end)]
    length = hypot(hypot(dx, dy), dz)
    bone = r.joint(name, parent, x, y, z, degrees(atan2(dz, hypot(dx, dy))), 0, -degrees(atan2(dx, dy)))
    r.box(bone, 0, 0, 0, width, length, depth, color)
    return bone


def dera_rifle(r):
    """Iconic Corpus Dera plasma rifle: boxy receiver + twin parallel prongs + cyan plasma core."""
    g = r.joint('gun', 'r_hand', 0, 0.4, -0.3)
    # Grip & stock
    r.box(g, 0, -1.2, 0.2, 1.0, 2.6, 1.1, RUBBER)
    r.box(g, 0, -2.1, 2.2, 1.3, 2.1, 3.2, DEEP)
    r.box(g, 0, -2.2, 3.6, 1.4, 2.4, 0.8, RUBBER)
    # Main rectangular receiver housing
    cap(r, g, 0, -2.9, -1.4, 2.2, 2.5, 5.4, PLATE)
    r.box(g, 0, -3.1, -1.5, 1.7, 0.45, 4.6, EDGE)
    # Side power cell & telemetry stripe
    for s in (-1, 1):
        r.box(g, s * 1.12, -2.2, -1.2, 0.28, 1.1, 2.4, DEEP)
        r.box(g, s * 1.24, -1.9, -1.2, 0.14, 0.35, 1.8, POWER)
    # Twin parallel Dera barrel prongs
    for s in (-1, 1):
        r.box(g, s * 0.62, -2.55, -6.2, 0.56, 1.35, 5.4, STEEL)
        r.box(g, s * 0.66, -2.65, -8.4, 0.44, 0.95, 1.8, DEEP)
        r.box(g, s * 0.42, -2.1, -6.4, 0.22, 0.32, 4.8, LIGHT)
    # Central glowing plasma channel between prongs
    r.box(g, 0, -2.2, -5.8, 0.42, 0.50, 4.6, POWER)
    r.box(g, 0, -2.1, -7.8, 0.30, 0.34, 1.6, LIGHT)


def crewman(r):
    """Standard Corpus Crewman: tan ribbed jumpsuit, dark harness, tall boxy grey helmet, Dera rifle."""
    # Torso (body pivot is ground y=24; hips at -14.2, shoulders at -25.2)
    cap(r, 'body', 0, -25.8, 0.1, 7.6, 7.4, 4.4, CLOTH)
    cap(r, 'body', 0, -18.8, 0.1, 6.2, 5.2, 3.8, CLOTH)
    # Ribbed industrial jumpsuit seams across abdomen
    for i in range(4):
        r.box('body', 0, -24.4 + i * 1.65, -2.12, 5.2 - i * 0.18, 0.42, 0.28, BONE)
    # Dark chest rig harness & collar plate
    cap(r, 'body', 0, -25.6, -1.55, 6.0, 4.6, 1.6, RUBBER)
    r.box('body', 0, -25.2, -2.35, 3.4, 2.2, 0.45, DEEP)
    r.box('body', 0, -24.5, -2.52, 1.6, 0.55, 0.22, POWER)
    # Utility belt + waist pouches
    r.box('body', 0, -15.4, 0.1, 6.7, 1.5, 4.3, RUBBER)
    r.box('body', 0, -15.3, -2.05, 1.7, 1.2, 0.55, STEEL)
    for s in (-1, 1):
        r.box('body', s * 2.75, -15.2, -1.35, 1.2, 1.7, 1.2, BONE)
        # Chest breather hoses curving toward collar
        bar(r, f'{ "r" if s < 0 else "l" }_hose', 'body', (s * 1.5, -24.0, -2.2), (s * 1.1, -26.2, -1.3), 0.55, 0.55, INK)
    # Rear environmental life-support pack
    cap(r, 'body', 0, -25.2, 2.75, 5.2, 6.6, 2.2, DEEP)
    r.box('body', 0, -24.4, 3.85, 3.2, 4.2, 0.55, PLATE)
    r.box('body', 0, -23.6, 4.12, 1.4, 0.55, 0.25, GLOW)

    # Limbs (humanoid articulation matching FrontierModel / CorpusMotion bone names)
    for s, side in ((-1, 'r'), (1, 'l')):
        # Arm + rounded black Corpus shoulder pauldron
        arm = r.joint(f'{side}_arm', 'body', s * 4.55, -24.8, 0.05, 2, 0, -s * 5)
        cap(r, arm, s * 0.2, -1.1, 0.05, 3.4, 3.1, 3.7, RUBBER)
        r.box(arm, s * 0.45, -0.7, 0.05, 3.2, 0.45, 3.2, EDGE)
        r.box(arm, 0, 1.6, 0.05, 2.35, 4.1, 2.45, CLOTH)
        # Forearm with bulky grey Corpus industrial gauntlet
        fore = r.joint(f'{side}_forearm', arm, 0, 5.5, 0, -10, 0, s * 3)
        r.box(fore, 0, 0.0, 0.0, 2.2, 2.4, 2.25, CLOTH)
        cap(r, fore, 0, 2.0, -0.05, 2.75, 3.6, 2.85, PLATE)
        r.box(fore, s * 1.35, 2.4, -0.1, 0.26, 2.2, 1.1, EDGE)
        # Hand
        hand = r.joint(f'{side}_hand', fore, 0, 5.5, 0)
        r.box(hand, 0, -0.2, 0.0, 1.9, 1.7, 1.85, RUBBER)

        # Leg + knee guard + industrial work boot
        leg = r.joint(f'{side}_leg', 'body', s * 1.95, -14.0, 0.1, -3, 0, -s * 2)
        r.box(leg, 0, 0.0, 0.0, 3.0, 6.8, 3.25, CLOTH)
        r.box(leg, s * 0.45, 1.8, -0.2, 2.4, 2.4, 2.8, BONE)
        shin = r.joint(f'{side}_shin', leg, 0, 6.8, 0.05, 5)
        r.box(shin, 0, 0.0, 0.05, 2.75, 5.9, 2.95, CLOTH)
        cap(r, shin, 0, 0.3, -1.35, 2.6, 2.3, 1.0, DEEP)
        r.box(shin, 0, 3.6, 0.0, 2.95, 2.5, 3.15, RUBBER)
        foot = r.joint(f'{side}_foot', shin, 0, 5.9, 0.0, -2)
        cap(r, foot, 0, 0.0, -0.75, 2.95, 1.5, 4.4, RUBBER)
        r.box(foot, 0, 0.95, -0.85, 3.05, 0.55, 4.55, INK)

    # Iconic tall tapered boxy grey Corpus Crewman helmet
    h = r.joint('head', 'body', 0, -26.0, -0.1)
    # Neck seal
    r.box(h, 0, -1.2, 0.0, 3.2, 1.5, 3.1, RUBBER)
    # Lower jaw block
    cap(r, h, 0, -3.8, -0.15, 5.4, 2.9, 5.5, DEEP)
    # Main wide trapezoidal box-helmet casing
    cap(r, h, 0, -7.8, 0.05, 6.1, 4.4, 6.0, PLATE)
    # Upper tapered crown & flat top crest
    cap(r, h, 0, -10.3, 0.2, 5.2, 2.6, 5.2, PLATE)
    r.box(h, 0, -11.3, 0.25, 3.6, 1.1, 3.8, EDGE)
    # Top forward sensor fin/vent
    r.box(h, 0, -11.9, -0.9, 1.6, 0.75, 1.8, DEEP)
    r.box(h, 0, -11.6, -1.78, 1.1, 0.35, 0.22, GLOW)
    # Recessed horizontal glowing cyan visor slit
    r.box(h, 0, -6.2, -2.95, 4.6, 1.35, 0.45, INK)
    r.box(h, 0, -5.85, -3.12, 4.1, 0.58, 0.25, POWER)
    r.box(h, 0, -5.75, -3.20, 3.4, 0.32, 0.16, LIGHT)
    # Side acoustic/visor cheek baffles
    for s in (-1, 1):
        r.box(h, s * 3.12, -7.2, 0.1, 0.55, 3.6, 3.4, EDGE)
        r.box(h, s * 3.35, -6.4, 0.1, 0.26, 1.4, 1.6, DEEP)

    dera_rifle(r)


def moa(r):
    """Standard Corpus MOA: armless reverse-jointed bipedal walker, teal/green legs, gold dorsal turret."""
    # Compact forward-leaning chassis suspended high at y=-21.2
    pelvis = r.joint('thorax', 'body', 0, -21.2, 0.2, 6)
    cap(r, pelvis, 0, -2.6, 0.2, 6.4, 4.2, 7.6, DEEP)
    # Side sage/teal-green armored fenders
    for s in (-1, 1):
        r.box(pelvis, s * 3.35, -2.3, 0.0, 1.2, 3.3, 6.2, PLATE)
        r.box(pelvis, s * 3.85, -1.5, 0.3, 0.4, 1.5, 4.2, EDGE)
    # Rear cooling heatsink block
    r.box(pelvis, 0, -2.2, 4.2, 4.6, 2.8, 1.8, RUBBER)
    for i in range(3):
        r.box(pelvis, 0, -1.8 + i * 0.85, 5.15, 3.6, 0.32, 0.3, STEEL)

    # Small forward sensor head under chassis nose
    h = r.joint('head', pelvis, 0, -0.6, -3.8)
    cap(r, h, 0, -1.2, -1.1, 3.4, 2.6, 3.0, DEEP)
    r.box(h, 0, -1.4, -2.55, 2.4, 1.4, 0.45, INK)
    r.box(h, 0, -1.05, -2.72, 1.5, 0.55, 0.22, POWER)
    r.box(h, 0, -0.95, -2.80, 0.85, 0.32, 0.16, LIGHT)

    # Dorsal gold/brass & black plasma cannon turret mounted atop thorax
    turret = r.joint('turret', pelvis, 0, -3.4, 0.4)
    # Neck gimbal mount
    r.box(turret, 0, -2.2, 0.2, 2.8, 2.4, 2.8, RUBBER)
    r.box(turret, 0, -2.5, 0.2, 3.4, 0.6, 3.2, STEEL)
    # Iconic boxy gold/brass Corpus plasma cannon housing
    gun = r.joint('gun', turret, 0, -4.6, -0.2)
    cap(r, gun, 0, -2.4, -1.4, 4.4, 3.5, 8.6, GOLD)
    r.box(gun, 0, -2.7, -1.2, 3.6, 0.65, 7.6, BRASS)
    # Dark inner barrel shroud + rear power capacitor
    r.box(gun, 0, -1.8, 3.3, 3.2, 2.5, 2.2, DEEP)
    r.box(gun, 0, -1.4, 4.45, 1.8, 0.9, 0.4, POWER)
    for s in (-1, 1):
        r.box(gun, s * 2.25, -1.6, -1.8, 0.45, 1.8, 5.8, INK)
        r.box(gun, s * 2.45, -1.2, -2.2, 0.22, 0.55, 3.6, BRASS)
    # Extended forward plasma barrel + glowing cyan emitter tip
    r.box(gun, 0, -1.55, -7.2, 1.75, 1.75, 4.4, INK)
    r.box(gun, 0, -1.35, -7.8, 2.15, 0.55, 3.4, STEEL)
    r.box(gun, 0, -1.25, -9.55, 1.15, 1.15, 0.65, POWER)
    r.box(gun, 0, -1.05, -9.85, 0.72, 0.72, 0.35, LIGHT)

    # Tall reverse-jointed (digitigrade) bipedal walker legs
    for s, side in ((-1, 'r'), (1, 'l')):
        # Hip axle + upper thigh angling forward/down (-26 deg)
        leg = r.joint(f'{side}_leg', 'body', s * 3.85, -20.8, 0.4, -26, 0, -s * 3)
        r.box(leg, -s * 0.4, -0.8, 0.0, 1.8, 2.2, 2.4, RUBBER)
        cap(r, leg, 0, 0.0, -0.1, 2.8, 7.8, 3.5, PLATE)
        r.box(leg, s * 1.42, 1.2, -0.1, 0.35, 5.2, 2.1, EDGE)
        # Rear hydraulic damper piston
        r.box(leg, 0, 1.6, 1.85, 1.1, 5.6, 1.1, STEEL)

        # Reverse knee / hock angling sharply backward/down (+54 deg)
        shin = r.joint(f'{side}_shin', leg, 0, 7.6, 0.0, 54)
        r.box(shin, 0, -0.7, 0.0, 2.5, 2.2, 2.7, INK)
        cap(r, shin, 0, 0.6, 0.0, 2.35, 8.2, 2.65, PLATE)
        r.box(shin, 0, 1.6, -1.35, 1.25, 6.2, 0.65, DEEP)
        r.box(shin, s * 1.18, 2.2, 0.0, 0.32, 4.8, 1.4, EDGE)

        # Lower metatarsus angling forward/down (-30 deg) to ground
        ankle = r.joint(f'{side}_ankle', shin, 0, 8.6, 0.0, -30)
        r.box(ankle, 0, -0.6, 0.0, 2.0, 1.8, 2.2, RUBBER)
        r.box(ankle, 0, 0.4, 0.0, 1.75, 4.8, 1.85, STEEL)
        r.box(ankle, 0, 1.0, -0.95, 1.35, 3.6, 0.6, DEEP)

        # Three-toed mechanical bird claw foot resting flush at ground level
        foot = r.joint(f'{side}_foot', ankle, 0, 5.1, 0.0, 2)
        cap(r, foot, 0, -0.3, 0.0, 2.6, 1.4, 2.8, DEEP)
        for ti, tx in ((-1, -1.15), (1, 1.15)):
            toe = r.joint(f'{side}_toe_{"in" if ti * s < 0 else "out"}', foot, tx * 0.75, 0.2, -1.2, 0, -ti * 16, 0)
            r.box(toe, 0, 0.0, -1.65, 1.05, 1.0, 3.4, RUBBER)
            r.box(toe, 0, 0.2, -3.2, 0.85, 0.8, 1.0, STEEL)
        # Rear stabilizer talon
        r.box(foot, 0, 0.25, 2.1, 1.1, 0.95, 2.8, RUBBER)


def shield_osprey(r):
    """Corpus Shield Osprey: wide swept teal wing shrouds, cyan thruster pods, hanging twin-prong tail."""
    # Floating core centered at y=-16.5 (hovers cleanly above ground in world & Bestiary preview)
    core = r.joint('thorax', 'body', 0, -16.5, 0.0, 4)
    # Iconic sculpted teal central hood/carapace
    cap(r, core, 0, -3.2, -0.2, 6.2, 3.4, 7.6, PLATE)
    r.box(core, 0, -3.8, -0.3, 4.2, 0.85, 6.0, EDGE)
    r.box(core, 0, -4.1, 0.4, 2.2, 0.55, 4.2, TRIM)
    # Dark mechanical under-chassis core
    cap(r, core, 0, -0.2, 0.1, 4.6, 3.4, 5.8, DEEP)
    # Ventral glowing cyan shield-tether emitter dish
    r.box(core, 0, 2.8, -0.4, 3.0, 0.75, 3.0, RUBBER)
    r.box(core, 0, 3.35, -0.4, 2.1, 0.45, 2.1, POWER)
    r.box(core, 0, 3.65, -0.4, 1.2, 0.28, 1.2, LIGHT)

    # Compact forward sensor pod beneath the hood lip
    h = r.joint('head', core, 0, 0.0, -3.6)
    cap(r, h, 0, -1.2, -0.8, 3.4, 2.3, 2.2, INK)
    r.box(h, -0.75, -0.5, -1.95, 0.55, 0.55, 0.22, MARK)
    r.box(h, 0.75, -0.5, -1.95, 0.55, 0.55, 0.22, MARK)
    r.box(h, 0, -0.85, -1.95, 0.95, 0.42, 0.22, LIGHT)

    # Wide swept twin wing shrouds with raised thruster pods & glowing cyan louver vents
    for s, side in ((-1, 'r'), (1, 'l')):
        wing = r.joint(f'{side}_wing', core, s * 3.1, -1.8, -0.2, 4, s * 10, -s * 6)
        # Mechanical elbow strut linking fuselage underside to wing underside
        bar(r, f'{side}_strut', wing, (-s * 0.4, 2.4, 0.2), (s * 2.8, 0.6, 0.0), 0.75, 0.85, RUBBER)
        # Main flat swept teal wing shroud (inner + outer tapered segments)
        cap(r, wing, s * 3.8, -0.8, -0.3, 7.6, 1.65, 6.2, PLATE)
        cap(r, wing, s * 8.6, -0.5, 0.2, 4.4, 1.15, 4.4, DEEP)
        # Brushed steel leading/outer rim trim
        r.box(wing, s * 4.6, -0.3, -3.45, 8.2, 0.55, 0.55, STEEL)
        r.box(wing, s * 10.6, -0.2, 0.3, 1.4, 0.75, 3.2, EDGE)
        # Raised oval thruster pod atop each wing
        cap(r, wing, s * 4.4, -2.55, -0.1, 5.2, 1.9, 4.2, DEEP)
        r.box(wing, s * 4.4, -2.85, -0.1, 4.0, 0.45, 3.0, EDGE)
        # Horizontal glowing cyan intake louver strips along pod front & side
        r.box(wing, s * 4.4, -1.95, -2.22, 3.6, 0.48, 0.22, POWER)
        r.box(wing, s * 4.4, -1.85, -2.30, 2.8, 0.24, 0.14, LIGHT)
        r.box(wing, s * 4.4, 0.75, -0.2, 3.4, 0.35, 2.6, GLOW)

    # Iconic hanging twin-prong emitter tail nacelle angled downward/forward beneath core
    tail = r.joint('tail', core, 0, 2.6, 1.2, -24)
    r.box(tail, 0, 0.0, 0.0, 3.2, 3.6, 2.2, DEEP)
    r.box(tail, 0, 1.2, -1.15, 1.6, 2.4, 0.45, STEEL)
    for s, side in ((-1, 'r'), (1, 'l')):
        prong = r.joint(f'{side}_prong', tail, s * 1.45, 2.8, -0.2, -8, 0, s * 3)
        r.box(prong, 0, 0.0, 0.0, 1.25, 6.2, 1.65, DEEP)
        r.box(prong, 0, 0.6, -0.88, 0.75, 4.8, 0.35, STEEL)
        r.box(prong, 0, 5.8, -0.15, 0.95, 1.15, 1.15, RUBBER)
        r.box(prong, 0, 6.55, -0.35, 0.55, 0.45, 0.45, GLOW)


ROSTER = {
    'corpus_crewman': (
        143, 1.00, crewman, {},
        dict(CLOTH='9c7c5c', BONE='755b42', PLATE='5c6b75', EDGE='8b9ca6', DEEP='303a44',
             RUBBER='23272e', POWER='28c8f0', LIGHT='b4fbff', GLOW='5ce6ff'),
    ),
    'corpus_moa': (
        144, 1.04, moa, {},
        dict(PLATE='4b7668', EDGE='79a394', DEEP='2b353c', RUBBER='1f242b',
             GOLD='d7a748', BRASS='9a7335', POWER='2bc4e8', LIGHT='b0fbff', GLOW='62e8ff'),
    ),
    'corpus_shield_osprey': (
        145, 1.00, shield_osprey, {},
        dict(PLATE='2b5f6e', TRIM='1c4350', EDGE='698c96', DEEP='222c34', RUBBER='191f26',
             STEEL='9aa7b0', POWER='28c8f0', LIGHT='b4fbff', GLOW='5ee6ff', MARK='e09b3d'),
    ),
}


def make(name):
    kind, scale, draw, options, pigments = ROSTER[name]
    r = Rig(kind, name)
    r.joint('body', None, 0, 24, 0)
    draw(r, **options)
    # Body is the vanilla feet-origin transform (y=24). Scale only child pivots and box bounds.
    for p in r.parts:
        if p['parent'] is not None:
            p['pivot'] = [round(x * scale, 4) for x in p['pivot']]
        for box in p['boxes']:
            for k in (0, 1):
                box[k] = [round(x * scale, 4) for x in box[k]]
    r.d.update(
        palette=palette(**pigments),
        emissive_palette=[LIGHT, POWER, GLOW],
        geometry_slot=kind,
        source='Ageless Arsenals / Corpus (Warframe) / see tools/corpus-art/REFERENCES.md',
    )
    return r
