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
    '15181d 262a32 9c7c5c 323d47 68737c 8d99a3 a2adb5 ced5da '
    '755b42 9c763a d7a748 a4f8ff 2ac2e8 5ce6ff 1f5a68 c68a39'
)


def palette(**overrides):
    colors = BASE.split()
    for key, color in overrides.items():
        colors[globals()[key]] = color
    return [list(bytes.fromhex(c)) for c in colors]


def cap(r, n, x, y, z, w, h, d, m):
    """Chamfered industrial Corpus block."""
    r.box(n, x, y, z, w * 0.80, h, d, m)
    r.box(n, x, y + h * 0.10, z, w, h * 0.80, d * 0.85, m)


def bar(r, name, parent, start, end, width, depth, color):
    """Orient local +Y down a mechanical strut or breather hose between two points."""
    x, y, z = start
    dx, dy, dz = [b - a for a, b in zip(start, end)]
    length = hypot(hypot(dx, dy), dz)
    bone = r.joint(name, parent, x, y, z, degrees(atan2(dz, hypot(dx, dy))), 0, -degrees(atan2(dx, dy)))
    r.box(bone, 0, 0, 0, width, length, depth, color)
    return bone


def dera_rifle(r):
    """Iconic Corpus Dera plasma rifle: boxy receiver + twin parallel prongs + cyan plasma core."""
    g = r.joint('gun', 'r_hand', 0, 0.4, -0.3)
    r.box(g, 0, -1.2, 0.2, 1.0, 2.6, 1.1, RUBBER)
    r.box(g, 0, -2.6, 2.3, 1.4, 2.1, 3.6, DEEP)
    r.box(g, 0, -2.5, 4.0, 1.2, 2.3, 0.8, RUBBER)
    r.box(g, 0, -2.8, -1.9, 1.9, 2.5, 5.2, PLATE)
    r.box(g, 0, -3.2, -1.8, 1.2, 0.5, 4.4, EDGE)
    r.box(g, 0, -2.3, -1.9, 2.1, 0.7, 2.4, POWER)
    r.box(g, 0, -3.4, -7.2, 1.35, 0.75, 5.8, PLATE)
    r.box(g, 0, -1.6, -7.2, 1.35, 0.75, 5.8, PLATE)
    r.box(g, 0, -2.5, -6.9, 0.65, 0.90, 5.4, GLOW)
    r.box(g, 0, -2.5, -10.1, 0.80, 0.80, 0.45, LIGHT)


def prova_baton(r):
    """Iconic Corpus Prova electrified stun baton: insulated grip, boxy guard, glowing cyan arc rod."""
    g = r.joint('gun', 'r_hand', 0, 0.4, -0.3)
    r.box(g, 0, -0.8, 0.0, 1.1, 2.8, 1.1, RUBBER)
    r.box(g, 0, -2.6, -0.2, 1.8, 1.4, 2.0, DEEP)
    r.box(g, 0, -3.1, -0.2, 2.0, 0.55, 1.5, EDGE)
    r.box(g, 0, -3.4, -5.4, 1.15, 1.15, 9.2, PLATE)
    r.box(g, 0, -3.4, -5.6, 0.65, 1.45, 8.0, POWER)
    r.box(g, 0, -3.4, -9.8, 1.35, 1.35, 1.4, STEEL)
    r.box(g, 0, -3.4, -10.4, 0.85, 0.85, 0.6, LIGHT)


def supra_lmg(r):
    """Iconic Corpus Supra heavy plasma LMG: massive boxy chassis, triple barrel cluster, glowing side cells."""
    g = r.joint('gun', 'r_hand', 0, 0.5, -0.4)
    r.box(g, 0, -1.4, 0.3, 1.2, 2.8, 1.3, RUBBER)
    r.box(g, 0, -3.2, 2.6, 2.2, 2.8, 3.8, DEEP)
    r.box(g, 0, -3.4, -2.6, 2.8, 3.3, 7.6, PLATE)
    r.box(g, 0, -4.4, -2.2, 2.0, 0.9, 6.2, EDGE)
    for s in (-1, 1):
        r.box(g, s * 1.55, -3.1, -2.4, 0.45, 1.3, 4.8, POWER)
        r.box(g, s * 1.65, -3.0, -2.4, 0.25, 0.65, 3.8, LIGHT)
    r.box(g, 0, -4.1, -8.8, 1.4, 0.85, 5.2, STEEL)
    r.box(g, -0.8, -2.5, -8.8, 0.85, 0.85, 5.2, STEEL)
    r.box(g, 0.8, -2.5, -8.8, 0.85, 0.85, 5.2, STEEL)
    r.box(g, 0, -3.1, -11.3, 1.8, 1.6, 0.55, GLOW)


def lanka_railgun(r):
    """Corpus Lanka sniper railgun: long dark receiver, twin magnetic rails, cyan plasma coils, top scope."""
    g = r.joint('gun', 'r_hand', 0, 0.4, -0.3)
    r.box(g, 0, -1.2, 0.2, 1.0, 2.5, 1.1, RUBBER)
    r.box(g, 0, -2.5, 2.5, 1.3, 1.8, 3.8, DEEP)
    r.box(g, 0, -2.2, 4.4, 1.2, 2.1, 0.7, RUBBER)
    r.box(g, 0, -2.6, -2.8, 1.55, 2.1, 7.2, DEEP)
    r.box(g, 0, -2.8, -3.2, 1.75, 1.15, 5.8, PLATE)
    r.box(g, 0, -4.1, -2.4, 0.95, 0.95, 4.4, INK)
    r.box(g, 0, -4.1, -4.7, 0.75, 0.75, 0.35, LIGHT)
    r.box(g, 0, -3.3, -9.8, 1.15, 0.68, 7.6, STEEL)
    r.box(g, 0, -1.6, -9.4, 1.15, 0.68, 6.8, STEEL)
    r.box(g, 0, -2.45, -8.8, 0.65, 0.85, 6.6, POWER)
    r.box(g, 0, -2.45, -10.8, 0.42, 0.42, 4.2, LIGHT)
    for k in range(3):
        r.box(g, 0, -2.45, -7.2 - k * 2.0, 1.45, 2.25, 0.42, EDGE)


def corpus_box_helmet(r, parent, neck_y, wide=False):
    """Deep horizontal rectangular Corpus hood overhanging backward + bottom cyan visor slit + tapered chin shield."""
    h = r.joint('head', parent, 0, neck_y, -0.15)
    w = 5.2 if wide else 4.55
    d = 7.8 if wide else 7.3
    cz = 0.75
    front_z = cz - d / 2
    r.box(h, 0, -2.2, 0.4, 3.0, 2.3, 2.8, RUBBER)
    r.box(h, 0, -8.0, cz, w, 4.7, d, PLATE)
    r.box(h, 0, -8.25, cz + 0.15, w - 0.55, 0.55, d - 0.55, EDGE)
    r.box(h, 0, -8.65, front_z + 0.55, 1.15, 0.90, 1.15, EDGE)
    r.box(h, 0, -8.45, front_z - 0.02, 0.55, 0.55, 0.25, POWER)
    r.box(h, 0, -7.75, front_z - 0.18, w - 0.45, 4.15, 0.40, EDGE)
    r.box(h, 0, -3.95, front_z - 0.28, w - 0.65, 0.55, 0.35, INK)
    r.box(h, 0, -3.78, front_z - 0.38, w - 1.15, 0.32, 0.22, POWER)
    r.box(h, 0, -3.72, front_z - 0.44, w - 1.85, 0.18, 0.16, LIGHT)
    for s in (-1, 1):
        r.box(h, s * (w / 2 + 0.06), -7.1, cz + 1.8, 0.32, 2.7, 2.9, EDGE)
        r.box(h, s * (w / 2 + 0.14), -6.4, cz + 2.3, 0.22, 1.35, 1.35, DEEP)
        bar(r, f'{"r" if s < 0 else "l"}_cheek', h,
            (s * (w / 2 + 0.08), -5.8, front_z + 1.9),
            (s * (w / 2 - 0.32), -3.5, front_z + 0.35),
            0.55, 0.55, STEEL)
    r.box(h, 0, -3.6, cz + 1.45, w - 1.25, 2.35, 2.75, DEEP)
    r.box(h, 0, -3.0, cz + 2.75, w - 1.85, 1.45, 0.42, STEEL)
    chin = r.joint('chin_shield', h, 0, -3.15, front_z + 0.25, -10, 0, 0)
    r.box(chin, 0, 0.0, 0.0, w - 0.75, 1.6, 1.25, EDGE)
    r.box(chin, 0, 1.4, 0.12, w - 1.45, 1.7, 1.15, PLATE)
    r.box(chin, 0, 2.9, 0.22, w - 2.15, 1.8, 1.05, EDGE)
    r.box(chin, 0, 4.4, 0.28, w - 2.65, 1.4, 0.95, STEEL)
    return h


def crewman_body(r, *, weapon='dera', heavy=False, knee_mat=TRIM, nullifier=False):
    """Shared Corpus Crewman / Prod Crewman / Corpus Tech / Nullifier Crewman articulated chassis."""
    tw = 8.4 if heavy else 7.4
    td = 4.8 if heavy else 4.3
    r.box('body', 0, -25.8, 0.1, tw, 7.4, td, CLOTH)
    r.box('body', 0, -18.8, 0.1, tw - 1.3, 5.2, td - 0.5, CLOTH)
    for i in range(4):
        r.box('body', 0, -24.3 + i * 1.65, -td / 2 - 0.05, tw - 2.4 - i * 0.15, 0.36, 0.24, BONE)
    r.box('body', 0, -25.5, -td / 2 + 0.1, tw - 1.4, 4.8, 1.35, RUBBER)
    r.box('body', 0, -25.8, -td / 2 - 0.45, 2.9, 2.4, 0.55, EDGE)
    r.box('body', 0, -23.6, -td / 2 - 0.48, 2.2, 2.2, 0.55, PLATE)
    r.box('body', 0, -21.6, -td / 2 - 0.45, 1.5, 1.8, 0.50, DEEP)
    r.box('body', 0, -20.2, -td / 2 - 0.42, 1.9, 0.75, 0.55, STEEL)
    r.box('body', 0, -15.4, 0.1, tw - 0.7, 1.5, td + 0.1, RUBBER)
    r.box('body', 0, -15.3, -td / 2 - 0.1, 1.8, 1.2, 0.55, STEEL)
    for s in (-1, 1):
        r.box('body', s * (tw * 0.38), -15.2, -1.35, 1.2, 1.7, 1.2, BONE)
        bar(r, f'{"r" if s < 0 else "l"}_hose', 'body',
            (s * 0.85, -20.0, -td / 2 - 0.4), (s * (tw * 0.42), -19.2, -0.4), 0.55, 0.55, INK)
    if nullifier:
        for s in (-1, 1):
            r.box('body', s * 2.4, -22.5, -td / 2 - 0.08, 0.65, 7.2, 0.22, GOLD)
        npack = r.joint('nullifier_pack', 'body', 0, -25.0, td / 2 + 1.2)
        for idx, yy in enumerate((-4.6, -1.2, 2.2)):
            cap(r, npack, 0, yy, 1.2, 6.6 - idx * 0.35, 2.6, 3.4, PLATE)
            r.box(npack, 0, yy + 0.5, 2.95, 4.2, 0.95, 0.40, POWER)
            for s in (-1, 1):
                r.box(npack, s * (3.35 - idx * 0.18), yy + 0.4, 1.2, 0.45, 1.6, 2.1, EDGE)
        r.box(npack, 0, -10.8, 1.2, 1.8, 6.8, 1.8, DEEP)
        r.box(npack, 0, -10.2, 1.2, 2.2, 3.8, 0.75, POWER)
        drone = r.joint('nullifier_drone', npack, 0, -13.2, 1.2)
        r.box(drone, 0, -0.8, 0.0, 2.4, 1.6, 2.4, EDGE)
        r.box(drone, 0, -0.5, 0.0, 1.6, 1.0, 1.6, LIGHT)
        for dx, dz, bw, bd in ((-3.8, 0, 2.8, 0.65), (3.8, 0, 2.8, 0.65), (0, -3.8, 0.65, 2.8), (0, 3.8, 0.65, 2.8)):
            r.box(drone, dx, -0.45, dz, bw, 0.85, bd, POWER)
            r.box(drone, dx * 1.18, -0.30, dz * 1.18, bw * 0.65, 0.45, bd * 0.65, LIGHT)
    else:
        r.box('body', 0, -25.2, td / 2 + 0.9, tw - 2.2, 6.6, 2.1, DEEP)
        r.box('body', 0, -24.4, td / 2 + 1.95, tw - 4.0, 4.2, 0.5, PLATE)
        r.box('body', 0, -23.6, td / 2 + 2.20, 1.5, 0.55, 0.22, GLOW)

    sh_x = 5.05 if heavy else 4.45
    for s, side in ((-1, 'r'), (1, 'l')):
        arm = r.joint(f'{side}_arm', 'body', s * sh_x, -24.8, 0.05, 2, 0, -s * 5)
        if heavy:
            cap(r, arm, s * 0.35, -1.6, 0.05, 4.6, 4.2, 4.8, PEARL)
            r.box(arm, s * 0.55, -1.1, 0.05, 4.8, 1.3, 4.2, DEEP)
        else:
            cap(r, arm, s * 0.2, -1.1, 0.05, 3.3, 3.0, 3.6, RUBBER)
            r.box(arm, s * 0.42, -0.7, 0.05, 3.1, 0.42, 3.1, EDGE)
        r.box(arm, 0, 1.6, 0.05, 2.55 if heavy else 2.30, 4.1, 2.65 if heavy else 2.40, CLOTH)

        fore = r.joint(f'{side}_forearm', arm, 0, 5.5, 0, -10, 0, s * 3)
        r.box(fore, 0, 0.0, 0.0, 2.4 if heavy else 2.15, 2.3, 2.45 if heavy else 2.20, CLOTH)
        cap(r, fore, 0, 1.9, -0.05, 3.0 if heavy else 2.70, 3.7, 3.1 if heavy else 2.80, PLATE)
        r.box(fore, s * (1.50 if heavy else 1.32), 2.3, -0.1, 0.26, 2.2, 1.1, EDGE)

        hand = r.joint(f'{side}_hand', fore, 0, 5.5, 0)
        r.box(hand, 0, -0.2, 0.0, 2.0 if heavy else 1.85, 1.7, 1.95 if heavy else 1.80, RUBBER)

        leg = r.joint(f'{side}_leg', 'body', s * (2.15 if heavy else 1.95), -14.0, 0.1, -3, 0, -s * 2)
        r.box(leg, 0, 0.0, 0.0, 3.25 if heavy else 2.95, 6.8, 3.45 if heavy else 3.20, CLOTH)
        r.box(leg, -s * 0.25, 1.8, -0.1, 2.6, 3.2, 3.1, RUBBER)

        shin = r.joint(f'{side}_shin', leg, 0, 6.8, 0.05, 5)
        r.box(shin, 0, 0.0, 0.05, 2.95 if heavy else 2.75, 5.9, 3.15 if heavy else 2.95, PLATE)
        r.box(shin, 0, 0.8, -1.45, 2.45, 4.6, 0.55, EDGE)
        cap(r, shin, 0, -0.35, -1.55, 2.25, 2.0, 0.95, knee_mat)

        foot = r.joint(f'{side}_foot', shin, 0, 5.9, 0.0, -2)
        r.box(foot, 0, 0.0, -0.75, 3.1 if heavy else 2.95, 1.5, 4.5, DEEP)
        r.box(foot, 0, 0.95, -0.85, 3.2 if heavy else 3.05, 0.55, 4.65, INK)

    corpus_box_helmet(r, 'body', -26.0, wide=heavy)
    if weapon == 'prova':
        prova_baton(r)
    elif weapon == 'supra':
        supra_lmg(r)
    elif weapon == 'lanka':
        lanka_railgun(r)
    else:
        dera_rifle(r)


def moa_body(r, *, shockwave=False):
    """Corpus MOA / Shockwave MOA: sleek low-slung gun-like dorsal plasma cannon + rear battery backpack + reverse legs."""
    pelvis = r.joint('thorax', 'body', 0, -21.2, 0.2, 6)
    r.box(pelvis, 0, -2.4, 0.2, 5.4, 3.8, 7.2, DEEP)
    for s in (-1, 1):
        r.box(pelvis, s * 2.95, -2.2, 0.0, 1.15, 3.1, 6.0, PLATE)
        r.box(pelvis, s * 3.45, -1.5, 0.2, 0.38, 1.4, 4.0, EDGE)
    pack = r.joint('backpack', pelvis, 0, -2.2, 4.2, 16, 0, 0)
    cap(r, pack, 0, -0.8, 1.4, 4.8, 5.6, 3.6, DEEP)
    r.box(pack, 0, -1.1, 1.6, 4.2, 1.4, 3.2, PLATE)
    r.box(pack, 0, 0.2, 3.25, 3.8, 3.8, 0.55, PLATE)
    for s in (-1, 1):
        r.box(pack, s * 2.45, -0.2, 1.4, 0.45, 4.2, 2.4, RUBBER)
        r.box(pack, s * 2.65, 0.4, 1.4, 0.24, 2.8, 1.4, STEEL)
    for i in range(3):
        r.box(pack, 0, 0.6 + i * 1.05, 3.55, 2.8, 0.32, 0.28, STEEL)
    r.box(pack, 0, 3.8, 3.45, 2.2, 0.55, 0.32, POWER)
    r.box(pack, 0, 3.9, 3.55, 1.4, 0.30, 0.20, LIGHT)

    h = r.joint('head', pelvis, 0, -0.4, -3.6)
    cap(r, h, 0, -1.1, -1.0, 3.2, 2.3, 2.8, DEEP)
    r.box(h, 0, -1.25, -2.35, 2.2, 1.2, 0.40, INK)
    r.box(h, 0, -0.95, -2.50, 1.35, 0.48, 0.20, POWER)
    r.box(h, 0, -0.88, -2.56, 0.78, 0.28, 0.14, LIGHT)

    turret = r.joint('turret', pelvis, 0, -2.8, 0.4)
    r.box(turret, 0, -1.5, 0.2, 2.6, 1.6, 2.8, RUBBER)
    r.box(turret, 0, -1.7, 0.2, 3.0, 0.45, 3.1, STEEL)

    gun = r.joint('gun', turret, 0, -2.6, -0.1, -4, 0, 0)
    r.box(gun, 0, -2.15, -3.4, 2.25, 2.35, 10.6, INK)
    for s in (-1, 1):
        r.box(gun, s * 1.25, -2.35, -2.6, 0.55, 2.55, 8.8, GOLD)
        r.box(gun, s * 1.50, -2.05, -2.2, 0.22, 1.15, 6.8, BRASS)
        if shockwave:
            r.box(gun, s * 1.62, -2.45, -4.4, 0.24, 0.45, 3.8, PLATE)
    r.box(gun, 0, -2.70, -2.8, 2.55, 0.55, 9.2, GOLD)
    r.box(gun, 0, -3.05, -1.6, 1.35, 0.40, 5.4, STEEL)
    r.box(gun, 0, -0.15, -4.8, 1.25, 0.65, 6.2, DEEP)
    r.box(gun, 0, 0.15, -6.2, 0.85, 0.45, 3.4, STEEL)
    r.box(gun, 0, -2.20, 2.9, 2.65, 2.45, 2.4, DEEP)
    r.box(gun, 0, -1.65, 4.15, 1.65, 0.95, 0.40, POWER)
    r.box(gun, 0, -2.25, -8.8, 2.65, 0.35, 1.2, GOLD)
    r.box(gun, 0, -0.15, -8.8, 2.65, 0.35, 1.2, GOLD)
    for s in (-1, 1):
        r.box(gun, s * 1.18, -2.05, -8.8, 0.35, 2.15, 1.2, GOLD)
    r.box(gun, 0, -1.65, -8.7, 1.35, 1.35, 0.75, POWER)
    r.box(gun, 0, -1.45, -9.05, 0.82, 0.82, 0.35, LIGHT)

    for s, side in ((-1, 'r'), (1, 'l')):
        leg = r.joint(f'{side}_leg', 'body', s * 3.55, -20.8, 0.4, -26, 0, -s * 3)
        r.box(leg, -s * 0.4, -0.8, 0.0, 1.7, 2.1, 2.3, RUBBER)
        cap(r, leg, 0, 0.0, -0.1, 2.7, 7.8, 3.4, PLATE)
        r.box(leg, s * 1.36, 1.2, -0.1, 0.34, 5.2, 2.0, EDGE)
        r.box(leg, 0, 1.6, 1.80, 1.05, 5.6, 1.05, STEEL)

        shin = r.joint(f'{side}_shin', leg, 0, 7.6, 0.0, 54)
        r.box(shin, 0, -0.7, 0.0, 2.4, 2.1, 2.6, INK)
        cap(r, shin, 0, 0.6, 0.0, 2.25, 8.2, 2.55, PLATE)
        r.box(shin, 0, 1.6, -1.30, 1.20, 6.2, 0.62, DEEP)
        r.box(shin, s * 1.14, 2.2, 0.0, 0.30, 4.8, 1.35, EDGE)

        ankle = r.joint(f'{side}_ankle', shin, 0, 8.6, 0.0, -30)
        r.box(ankle, 0, -0.6, 0.0, 1.9, 1.8, 2.1, RUBBER)
        r.box(ankle, 0, 0.4, 0.0, 1.65, 4.8, 1.75, STEEL)
        r.box(ankle, 0, 1.0, -0.92, 1.30, 3.6, 0.58, DEEP)

        foot = r.joint(f'{side}_foot', ankle, 0, 5.1, 0.0, 2)
        cap(r, foot, 0, -0.3, 0.0, 2.5, 1.4, 2.7, DEEP)
        for ti, tx in ((-1, -1.15), (1, 1.15)):
            toe = r.joint(f'{side}_toe_{"in" if ti * s < 0 else "out"}', foot, tx * 0.75, 0.2, -1.2, 0, -ti * 16, 0)
            r.box(toe, 0, 0.0, -1.65, 1.0, 1.0, 3.4, RUBBER)
            r.box(toe, 0, 0.2, -3.2, 0.82, 0.8, 1.0, STEEL)
        r.box(foot, 0, 0.25, 2.1, 1.05, 0.95, 2.8, RUBBER)


def shield_osprey(r):
    """Corpus Shield Osprey: wide swept teal wing shrouds, cyan thruster pods, hanging twin-prong tail."""
    core = r.joint('thorax', 'body', 0, -16.5, 0.0, 4)
    cap(r, core, 0, -3.2, -0.2, 6.2, 3.4, 7.6, PLATE)
    r.box(core, 0, -3.8, -0.3, 4.2, 0.85, 6.0, EDGE)
    r.box(core, 0, -4.1, 0.4, 2.2, 0.55, 4.2, TRIM)
    cap(r, core, 0, -0.2, 0.1, 4.6, 3.4, 5.8, DEEP)
    r.box(core, 0, 2.8, -0.4, 3.0, 0.75, 3.0, RUBBER)
    r.box(core, 0, 3.35, -0.4, 2.1, 0.45, 2.1, POWER)
    r.box(core, 0, 3.65, -0.4, 1.2, 0.28, 1.2, LIGHT)

    h = r.joint('head', core, 0, 0.0, -3.6)
    cap(r, h, 0, -1.2, -0.8, 3.4, 2.3, 2.2, INK)
    r.box(h, -0.75, -0.5, -1.95, 0.55, 0.55, 0.22, MARK)
    r.box(h, 0.75, -0.5, -1.95, 0.55, 0.55, 0.22, MARK)
    r.box(h, 0, -0.85, -1.95, 0.95, 0.42, 0.22, LIGHT)

    for s, side in ((-1, 'r'), (1, 'l')):
        wing = r.joint(f'{side}_wing', core, s * 3.1, -1.8, -0.2, 4, s * 10, -s * 6)
        bar(r, f'{side}_strut', wing, (-s * 0.4, 2.4, 0.2), (s * 2.8, 0.6, 0.0), 0.75, 0.85, RUBBER)
        cap(r, wing, s * 3.8, -0.8, -0.3, 7.6, 1.65, 6.2, PLATE)
        cap(r, wing, s * 8.6, -0.5, 0.2, 4.4, 1.15, 4.4, DEEP)
        r.box(wing, s * 4.6, -0.3, -3.45, 8.2, 0.55, 0.55, STEEL)
        r.box(wing, s * 10.6, -0.2, 0.3, 1.4, 0.75, 3.2, EDGE)
        cap(r, wing, s * 4.4, -2.55, -0.1, 5.2, 1.9, 4.2, DEEP)
        r.box(wing, s * 4.4, -2.85, -0.1, 4.0, 0.45, 3.0, EDGE)
        r.box(wing, s * 4.4, -1.95, -2.22, 3.6, 0.48, 0.22, POWER)
        r.box(wing, s * 4.4, -1.85, -2.30, 2.8, 0.24, 0.14, LIGHT)
        r.box(wing, s * 4.4, 0.75, -0.2, 3.4, 0.35, 2.6, GLOW)

    tail = r.joint('tail', core, 0, 2.6, 1.2, -24)
    r.box(tail, 0, 0.0, 0.0, 3.2, 3.6, 2.2, DEEP)
    r.box(tail, 0, 1.2, -1.15, 1.6, 2.4, 0.45, STEEL)
    for s, side in ((-1, 'r'), (1, 'l')):
        prong = r.joint(f'{side}_prong', tail, s * 1.45, 2.8, -0.2, -8, 0, s * 3)
        r.box(prong, 0, 0.0, 0.0, 1.25, 6.2, 1.65, DEEP)
        r.box(prong, 0, 0.6, -0.88, 0.75, 4.8, 0.35, STEEL)
        r.box(prong, 0, 5.8, -0.15, 0.95, 1.15, 1.15, RUBBER)
        r.box(prong, 0, 6.55, -0.35, 0.55, 0.45, 0.45, GLOW)


def bursa_body(r):
    """Corpus Isolator Bursa: squat heavy riot-shield walker with twin blast shields, dorsal pods, rear console."""
    hull = r.joint('thorax', 'body', 0, -15.8, 0.0, 4)
    cap(r, hull, 0, -6.2, 0.2, 9.8, 7.6, 8.8, DEEP)
    cap(r, hull, 0, -7.8, -0.2, 8.8, 2.4, 7.8, PLATE)
    r.box(hull, 0, -8.3, -0.4, 6.4, 0.75, 5.8, EDGE)
    h = r.joint('head', hull, 0, -3.2, -4.4)
    cap(r, h, 0, -1.6, -0.8, 4.2, 3.0, 2.6, INK)
    r.box(h, 0, -1.1, -2.15, 2.8, 1.25, 0.35, POWER)
    r.box(h, 0, -0.9, -2.28, 1.6, 0.65, 0.22, LIGHT)
    r.box(h, 0, -2.0, -1.7, 3.4, 0.55, 1.4, STEEL)

    for s, side in ((-1, 'r'), (1, 'l')):
        sh = r.joint(f'{side}_shield', hull, s * 5.2, -3.4, -2.8, 6, -s * 20, s * 4)
        cap(r, sh, s * 1.4, -5.2, -2.2, 5.4, 11.4, 2.6, PLATE)
        r.box(sh, s * 1.4, -4.8, -3.55, 4.4, 9.8, 0.55, EDGE)
        r.box(sh, s * 1.4, -4.2, -1.0, 4.6, 9.2, 0.95, DEEP)
        r.box(sh, s * 1.4, 2.6, -3.75, 4.0, 1.35, 0.40, GOLD)
        r.box(sh, s * 1.4, -3.6, -3.75, 3.2, 0.75, 0.38, POWER)
        r.box(sh, s * 1.4, -3.45, -3.86, 2.2, 0.35, 0.22, LIGHT)
        r.box(sh, s * 1.4, 5.8, -2.8, 5.2, 1.4, 3.0, STEEL)
        r.box(sh, -s * 0.6, -1.0, 0.4, 2.4, 3.2, 2.6, RUBBER)

    for s, side in ((-1, 'r'), (1, 'l')):
        pod = r.joint(f'{side}_pod', hull, s * 3.8, -8.4, -0.6, -4, 0, 0)
        cap(r, pod, 0, -2.6, 0.0, 3.2, 2.8, 5.8, DEEP)
        r.box(pod, 0, -2.9, -0.2, 2.6, 0.75, 4.8, PLATE)
        if s < 0:
            for bx in (-0.65, 0.65):
                r.box(pod, bx, -1.8, -5.2, 0.72, 0.72, 5.2, STEEL)
                r.box(pod, bx, -1.8, -7.8, 0.85, 0.85, 0.65, INK)
            r.box(pod, 0, -1.7, -3.0, 1.8, 0.45, 2.8, POWER)
        else:
            r.box(pod, 0, -2.4, -4.6, 2.3, 2.1, 3.8, STEEL)
            r.box(pod, 0, -1.9, -6.5, 1.4, 1.2, 0.55, POWER)
            r.box(pod, 0, -1.7, -6.7, 0.8, 0.65, 0.30, LIGHT)

    cons = r.joint('console', hull, 0, -3.4, 4.6, 14, 0, 0)
    cap(r, cons, 0, -2.6, 1.2, 6.2, 6.4, 3.0, DEEP)
    r.box(cons, 0, -1.6, 2.75, 4.6, 3.4, 0.45, INK)
    r.box(cons, 0, -1.2, 2.95, 3.8, 2.4, 0.25, POWER)
    r.box(cons, 0, -0.8, 3.08, 2.6, 1.2, 0.16, LIGHT)
    for i in range(3):
        r.box(cons, 0, 2.2 + i * 0.75, 2.75, 4.4, 0.32, 0.35, STEEL)

    for s, side in ((-1, 'r'), (1, 'l')):
        leg = r.joint(f'{side}_leg', 'body', s * 5.6, -15.4, 0.6, -24, 0, -s * 4)
        r.box(leg, -s * 0.5, -1.0, 0.0, 2.4, 2.6, 2.8, RUBBER)
        cap(r, leg, 0, 0.0, 0.0, 3.8, 6.6, 4.4, PLATE)
        r.box(leg, s * 1.95, 1.2, 0.0, 0.45, 4.6, 2.8, EDGE)

        shin = r.joint(f'{side}_shin', leg, 0, 6.2, 0.2, 46)
        r.box(shin, 0, -0.8, 0.0, 3.2, 2.4, 3.4, INK)
        cap(r, shin, 0, 0.4, 0.0, 3.3, 7.6, 3.8, DEEP)
        r.box(shin, 0, 1.4, -1.9, 2.2, 5.4, 0.65, PLATE)
        r.box(shin, s * 1.65, 1.8, 0.0, 0.40, 4.4, 2.0, STEEL)

        foot = r.joint(f'{side}_foot', shin, 0, 7.8, 0.0, -22)
        cap(r, foot, 0, -0.2, -0.6, 4.4, 2.0, 6.2, DEEP)
        r.box(foot, 0, 1.1, -1.0, 4.8, 0.95, 6.8, RUBBER)
        r.box(foot, 0, 0.2, -3.7, 3.4, 1.3, 1.4, STEEL)


def hyena_body(r):
    """Corpus Hyena Proxy: crouched quadrupedal robotic predator with gold/obsidian armor & dorsal turret."""
    thorax = r.joint('thorax', 'body', 0, -13.6, -2.6, -8)
    cap(r, thorax, 0, -3.2, 0.0, 7.2, 5.2, 8.8, PLATE)
    r.box(thorax, 0, -3.8, -0.4, 5.2, 1.2, 7.4, EDGE)
    r.box(thorax, 0, 1.4, -0.4, 5.4, 2.4, 7.6, DEEP)
    r.box(thorax, 0, -4.1, -0.2, 1.6, 0.65, 6.8, POWER)
    r.box(thorax, 0, -4.25, -0.2, 0.85, 0.35, 5.6, LIGHT)
    for s in (-1, 1):
        r.box(thorax, s * 3.8, -2.8, -1.6, 1.4, 3.8, 5.2, PLATE)
        r.box(thorax, s * 4.45, -1.8, -1.8, 0.38, 2.0, 3.4, GOLD)

    h = r.joint('head', thorax, 0, 0.2, -4.8, 12)
    cap(r, h, 0, -2.4, -2.6, 4.6, 3.2, 5.4, DEEP)
    r.box(h, 0, -3.1, -2.2, 5.2, 1.1, 4.8, PLATE)
    r.box(h, 0, -1.3, -5.35, 2.6, 1.4, 0.45, INK)
    r.box(h, 0, -1.05, -5.55, 1.5, 0.95, 0.28, MARK)
    r.box(h, 0, -0.90, -5.68, 0.75, 0.50, 0.18, LIGHT)
    for s in (-1, 1):
        r.box(h, s * 1.4, 0.6, -5.2, 0.48, 2.2, 0.48, STEEL)
        r.box(h, s * 2.35, -1.6, -2.4, 0.45, 1.4, 3.6, EDGE)

    turret = r.joint('turret', thorax, 0, -4.2, 0.6)
    r.box(turret, 0, -1.4, 0.0, 3.2, 1.6, 4.2, DEEP)
    for s in (-1, 1):
        r.box(turret, s * 0.95, -1.3, -3.8, 0.65, 0.65, 4.4, STEEL)
        r.box(turret, s * 0.95, -1.3, -6.0, 0.45, 0.45, 0.45, LIGHT)
    r.box(turret, 0, -2.1, 0.4, 1.8, 0.65, 3.2, GOLD)

    abd = r.joint('abdomen', thorax, 0, 0.2, 4.4, 14)
    cap(r, abd, 0, -2.2, 3.2, 5.6, 4.2, 6.8, DEEP)
    r.box(abd, 0, -2.8, 3.0, 4.4, 1.2, 5.6, PLATE)
    r.box(abd, 0, -3.1, 3.2, 1.2, 0.55, 4.6, POWER)

    tail = r.joint('tail', abd, 0, -1.4, 6.6, -22)
    r.box(tail, 0, -0.6, 2.2, 1.8, 1.4, 4.6, RUBBER)
    r.box(tail, 0, -0.9, 5.6, 1.2, 1.0, 3.8, PLATE)
    r.box(tail, 0, -0.8, 7.6, 0.75, 0.65, 1.4, GLOW)

    for s, side in ((-1, 'r'), (1, 'l')):
        fl = r.joint(f'{side}f_leg', 'body', s * 4.8, -13.2, -5.2, 18, 0, -s * 14)
        cap(r, fl, s * 0.4, 0.0, 0.0, 2.8, 6.8, 3.2, PLATE)
        r.box(fl, s * 1.65, 1.2, 0.0, 0.40, 4.6, 1.8, EDGE)

        fs = r.joint(f'{side}f_shin', fl, s * 0.4, 6.4, 0.0, -34, 0, s * 14)
        r.box(fs, 0, -0.6, 0.0, 2.4, 2.0, 2.4, INK)
        cap(r, fs, 0, 0.4, 0.0, 2.2, 7.2, 2.5, DEEP)
        r.box(fs, 0, 1.6, -1.3, 1.2, 5.2, 0.48, GOLD)

        fp = r.joint(f'{side}f_paw', fs, 0, 7.2, 0.0, 16)
        r.box(fp, 0, -0.2, -0.6, 2.8, 1.3, 3.4, RUBBER)
        for tx in (-1.0, 0.0, 1.0):
            r.box(fp, tx, 0.2, -2.6, 0.68, 0.85, 2.2, STEEL)

        bl = r.joint(f'{side}b_leg', 'body', s * 4.4, -12.2, 4.8, -28, 0, -s * 8)
        cap(r, bl, s * 0.3, 0.0, 0.0, 2.9, 6.6, 3.6, PLATE)
        r.box(bl, s * 1.60, 1.4, 0.0, 0.38, 4.2, 2.0, GOLD)

        bs = r.joint(f'{side}b_shin', bl, s * 0.3, 6.2, 0.0, 52, 0, s * 8)
        r.box(bs, 0, -0.6, 0.0, 2.3, 2.0, 2.5, INK)
        cap(r, bs, 0, 0.4, 0.0, 2.1, 7.0, 2.4, DEEP)
        r.box(bs, 0, 1.6, 1.2, 1.0, 4.8, 0.45, STEEL)

        bp = r.joint(f'{side}b_paw', bs, 0, 6.8, 0.0, -24)
        r.box(bp, 0, -0.2, -0.8, 2.6, 1.3, 3.6, RUBBER)
        for tx in (-0.85, 0.85):
            r.box(bp, tx, 0.2, -2.8, 0.72, 0.85, 2.0, STEEL)


def raknoid_body(r):
    """Corpus Scyto Raknoid: Orb Vallis four-legged spider walker with giant frosted-white domed sac."""
    core = r.joint('thorax', 'body', 0, -11.8, 0.0, 0)
    cap(r, core, 0, -2.6, 0.0, 8.2, 4.8, 9.2, DEEP)
    cap(r, core, 0, -3.8, 0.0, 9.2, 2.2, 10.0, PLATE)
    r.box(core, 0, -4.1, 0.0, 9.6, 0.65, 10.4, EDGE)

    dome = r.joint('dome', core, 0, -3.6, 0.8, -6)
    cap(r, dome, 0, -6.2, 0.0, 9.4, 6.4, 10.6, PEARL)
    cap(r, dome, 0, -8.2, 0.2, 7.8, 2.6, 9.0, PEARL)
    r.box(dome, 0, -9.2, 0.3, 5.4, 1.2, 6.6, LIGHT)
    for s in (-1, 1):
        r.box(dome, s * 4.75, -2.6, 0.0, 0.65, 2.8, 7.2, PLATE)
        r.box(dome, s * 4.95, -2.2, -1.6, 0.45, 1.4, 1.8, POWER)

    h = r.joint('head', core, 0, -0.6, -4.8)
    cap(r, h, 0, -1.8, -1.2, 4.8, 3.2, 3.2, PLATE)
    r.box(h, 0, -1.2, -2.85, 3.6, 1.6, 0.45, INK)
    for ex, ey in ((-1.0, -0.8), (0.0, -1.35), (1.0, -0.8)):
        r.box(h, ex, ey, -3.05, 0.72, 0.65, 0.25, LIGHT)
    for s in (-1, 1):
        r.box(h, s * 1.45, 0.9, -2.2, 0.75, 2.4, 0.95, STEEL)
    r.box(core, 0, 2.2, -0.4, 4.4, 2.2, 4.8, RUBBER)
    r.box(core, 0, 3.6, -0.6, 2.6, 1.2, 2.8, STEEL)
    r.box(core, 0, 4.2, -0.6, 1.4, 0.55, 1.4, POWER)

    leg_configs = [
        ('fr', -1, -4.4, -3.4,  42),
        ('fl',  1,  4.4, -3.4, -42),
        ('br', -1, -4.4,  3.4,  138),
        ('bl',  1,  4.4,  3.4, -138),
    ]
    for prefix, s, px, pz, yaw in leg_configs:
        coxa = r.joint(f'{prefix}_leg', 'body', px, -11.4, pz, 0, yaw, 0)
        r.box(coxa, 0, -1.0, -0.6, 2.2, 2.2, 3.0, RUBBER)
        femur = r.joint(f'{prefix}_femur', coxa, 0, 0.0, -1.4, 66, 0, 0)
        cap(r, femur, 0, -10.4, 0.0, 2.2, 10.6, 2.5, DEEP)
        r.box(femur, 0, -10.6, -1.25, 2.7, 9.2, 0.95, PLATE)
        r.box(femur, 0, -9.8, -1.75, 1.35, 6.2, 0.42, EDGE)
        r.box(femur, 0, -10.4, 0.0, 1.15, 1.15, 2.85, POWER)

        tibia = r.joint(f'{prefix}_shin', femur, 0, -10.4, 0.0, -48, 0, 0)
        r.box(tibia, 0, -0.9, 0.0, 2.6, 2.5, 2.7, INK)
        cap(r, tibia, 0, 0.6, 0.0, 2.7, 9.4, 2.8, PLATE)
        r.box(tibia, 0, 2.2, -1.42, 1.45, 7.0, 0.52, EDGE)
        r.box(tibia, 0, 9.8, 0.0, 1.55, 5.2, 1.65, DEEP)
        r.box(tibia, 0, 14.8, 0.0, 0.88, 3.6, 0.95, STEEL)
        r.box(tibia, 0, 2.0, 1.38, 0.85, 2.2, 0.42, LIGHT)


ROSTER = {
    'corpus_crewman': (
        143, 1.00, crewman_body, {'weapon': 'dera', 'heavy': False, 'knee_mat': DEEP},
        dict(CLOTH='9c7c5c', BONE='755b42', PLATE='707c86', EDGE='9aa6b0', DEEP='303a44',
             RUBBER='23272e', POWER='28c8f0', LIGHT='b4fbff', GLOW='5ce6ff'),
    ),
    'corpus_moa': (
        144, 1.04, moa_body, {'shockwave': False},
        dict(PLATE='4b7668', EDGE='79a394', DEEP='2b353c', RUBBER='1f242b',
             GOLD='d7a748', BRASS='9a7335', POWER='2bc4e8', LIGHT='b0fbff', GLOW='62e8ff'),
    ),
    'corpus_shield_osprey': (
        145, 1.00, shield_osprey, {},
        dict(PLATE='2b5f6e', TRIM='1c4350', EDGE='698c96', DEEP='222c34', RUBBER='191f26',
             STEEL='9aa7b0', POWER='28c8f0', LIGHT='b4fbff', GLOW='5ee6ff', MARK='e09b3d'),
    ),
    'corpus_prod_crewman': (
        146, 0.98, crewman_body, {'weapon': 'prova', 'heavy': False, 'knee_mat': TRIM},
        dict(CLOTH='8f7d64', BONE='695a47', PLATE='7a838a', EDGE='a4aeb5', DEEP='323a42',
             RUBBER='23272e', TRIM='23868e', POWER='2be0ff', LIGHT='bafcff', GLOW='66ecff'),
    ),
    'corpus_tech': (
        147, 1.18, crewman_body, {'weapon': 'supra', 'heavy': True, 'knee_mat': GOLD},
        dict(CLOTH='b54334', BONE='872f24', PLATE='a8b1b8', EDGE='cfd6dc', DEEP='343b42',
             PEARL='d5dce0', TRIM='29687a', RUBBER='22262c', GOLD='d6b643', MARK='e89b32',
             POWER='28c8f0', LIGHT='c0fcff', GLOW='5ce6ff'),
    ),
    'corpus_shockwave_moa': (
        148, 1.08, moa_body, {'shockwave': True},
        dict(PLATE='d98324', EDGE='f0a84b', DEEP='2b3238', RUBBER='1d2228',
             GOLD='c89b42', BRASS='8f682b', POWER='2bc4e8', LIGHT='b0fbff', GLOW='62e8ff'),
    ),
    'corpus_nullifier': (
        149, 1.04, crewman_body, {'weapon': 'lanka', 'heavy': False, 'knee_mat': MARK, 'nullifier': True},
        dict(CLOTH='1d222a', BONE='141820', PLATE='7c8791', EDGE='a7b3bd', DEEP='2b343e',
             RUBBER='191d24', GOLD='e0b238', MARK='cc4832', STEEL='98a5b0',
             POWER='24d4ff', LIGHT='ccfaff', GLOW='62ecff'),
    ),
    'corpus_bursa': (
        150, 1.14, bursa_body, {},
        dict(PLATE='2b5c8a', EDGE='6fa1c9', DEEP='26303a', RUBBER='1a2027', STEEL='9aa7b3',
             GOLD='e0a738', BRASS='9a6f28', POWER='24d0ff', LIGHT='c0fcff', GLOW='5ce6ff'),
    ),
    'corpus_hyena': (
        151, 1.08, hyena_body, {},
        dict(PLATE='d9a72e', EDGE='f5cc5c', DEEP='20252d', RUBBER='161a20', STEEL='8d9aa6',
             GOLD='eab83c', MARK='ff4238', POWER='26d2ff', LIGHT='c6fcff', GLOW='5ee8ff'),
    ),
    'corpus_raknoid': (
        152, 1.12, raknoid_body, {},
        dict(PEARL='ddebf2', PLATE='c9682e', EDGE='e89862', DEEP='27313b', RUBBER='1a2027',
             STEEL='96a4b0', POWER='24d4ff', LIGHT='d4fcff', GLOW='60eaff'),
    ),
}


def make(name):
    kind, scale, draw, options, pigments = ROSTER[name]
    r = Rig(kind, name)
    r.joint('body', None, 0, 24, 0)
    draw(r, **options)
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
