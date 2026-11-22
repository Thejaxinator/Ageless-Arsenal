#!/usr/bin/env python3
"""
Generates all data, textures, biomes, features, and Destiny-referenced .nbt structures
for the four main faction biomes (Fallen, Hive, Vex, Cabal) in Ageless Arsenals (NeoForge 1.21.1).
"""
import gzip
import json
import math
import os
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src" / "main" / "resources"
DATA = RES / "data" / "destiny2mc"
ASSETS = RES / "assets" / "destiny2mc"
MC_DATA = RES / "data" / "minecraft"

DATA_VERSION = 3955  # Minecraft 1.21.1


def write_json(path: Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


# ---------------------------------------------------------------------------
# Minimal NBT Binary Writer (Big-Endian Java Edition StructureTemplate)
# ---------------------------------------------------------------------------
TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12


class NbtWriter:
    def __init__(self):
        self.buf = bytearray()

    def u8(self, v):
        self.buf.append(v & 0xFF)

    def i16(self, v):
        self.buf.extend(struct.pack(">h", v))

    def u16(self, v):
        self.buf.extend(struct.pack(">H", v))

    def i32(self, v):
        self.buf.extend(struct.pack(">i", v))

    def string(self, s: str):
        b = s.encode("utf-8")
        self.u16(len(b))
        self.buf.extend(b)

    def tag_header(self, tag_type: int, name: str):
        self.u8(tag_type)
        self.string(name)

    def write_int(self, name: str, val: int):
        self.tag_header(TAG_INT, name)
        self.i32(val)

    def write_string(self, name: str, val: str):
        self.tag_header(TAG_STRING, name)
        self.string(val)

    def write_int_list(self, name: str, vals):
        self.tag_header(TAG_LIST, name)
        self.u8(TAG_INT)
        self.i32(len(vals))
        for v in vals:
            self.i32(v)

    def write_compound_list(self, name: str, items, writer_fn):
        self.tag_header(TAG_LIST, name)
        self.u8(TAG_COMPOUND if items else TAG_END)
        self.i32(len(items))
        for item in items:
            writer_fn(self, item)
            self.u8(TAG_END)


class StructureBuilder:
    """Voxel builder that emits valid Minecraft 1.21.1 gzipped .nbt StructureTemplate files."""

    def __init__(self, sx: int, sy: int, sz: int):
        self.sx = sx
        self.sy = sy
        self.sz = sz
        self.grid = {}  # (x, y, z) -> (block_name, tuple_of_sorted_props)

    def set(self, x: int, y: int, z: int, block: str, props=None):
        if 0 <= x < self.sx and 0 <= y < self.sy and 0 <= z < self.sz:
            if props:
                key = (block, tuple(sorted((str(k), str(v)) for k, v in props.items())))
            else:
                key = (block, ())
            self.grid[(x, y, z)] = key

    def clear(self, x: int, y: int, z: int):
        self.set(x, y, z, "minecraft:air")

    def fill(self, x0, y0, z0, x1, y1, z1, block: str, props=None):
        xa, xb = min(x0, x1), max(x0, x1)
        ya, yb = min(y0, y1), max(y0, y1)
        za, zb = min(z0, z1), max(z0, z1)
        for x in range(xa, xb + 1):
            for y in range(ya, yb + 1):
                for z in range(za, zb + 1):
                    self.set(x, y, z, block, props)

    def hollow_box(self, x0, y0, z0, x1, y1, z1, wall: str, floor: str = None, roof: str = None, interior: str = "minecraft:air"):
        xa, xb = min(x0, x1), max(x0, x1)
        ya, yb = min(y0, y1), max(y0, y1)
        za, zb = min(z0, z1), max(z0, z1)
        for x in range(xa, xb + 1):
            for y in range(ya, yb + 1):
                for z in range(za, zb + 1):
                    if y == ya and floor is not None:
                        self.set(x, y, z, floor)
                    elif y == yb and roof is not None:
                        self.set(x, y, z, roof)
                    elif x in (xa, xb) or z in (za, zb) or y in (ya, yb):
                        self.set(x, y, z, wall)
                    elif interior is not None:
                        self.set(x, y, z, interior)

    def save(self, path: Path):
        palette_index = {}
        palette = []
        blocks = []

        for (x, y, z), state in sorted(self.grid.items()):
            idx = palette_index.get(state)
            if idx is None:
                idx = len(palette)
                palette_index[state] = idx
                palette.append(state)
            blocks.append((x, y, z, idx))

        w = NbtWriter()
        w.tag_header(TAG_COMPOUND, "")
        w.write_int("DataVersion", DATA_VERSION)
        w.write_int_list("size", [self.sx, self.sy, self.sz])

        def write_palette_entry(nw: NbtWriter, entry):
            name, props = entry
            nw.write_string("Name", name)
            if props:
                nw.tag_header(TAG_COMPOUND, "Properties")
                for k, v in props:
                    nw.write_string(k, v)
                nw.u8(TAG_END)

        w.write_compound_list("palette", palette, write_palette_entry)

        def write_block_entry(nw: NbtWriter, b):
            x, y, z, state_idx = b
            nw.write_int_list("pos", [x, y, z])
            nw.write_int("state", state_idx)

        w.write_compound_list("blocks", blocks, write_block_entry)
        w.write_compound_list("entities", [], lambda nw, e: None)
        w.u8(TAG_END)

        path.parent.mkdir(parents=True, exist_ok=True)
        with gzip.open(path, "wb", compresslevel=6) as f:
            f.write(w.buf)
        return len(palette), len(blocks)


# ---------------------------------------------------------------------------
# 1. Animated White Glowing Fake Radiolaria Texture (16x128 RGBA PNG, 8 frames)
# ---------------------------------------------------------------------------
def write_png_rgba(path: Path, width: int, height: int, pixels):
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # filter type 0
        row = pixels[y]
        for r, g, b, a in row:
            raw.extend((r, g, b, a))

    def chunk(ctype: bytes, data: bytes) -> bytes:
        crc = zlib.crc32(ctype + data) & 0xFFFFFFFF
        return struct.pack(">I", len(data)) + ctype + data + struct.pack(">I", crc)

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def generate_radiolaria_texture():
    frames = 8
    w, h = 16, 16 * frames
    pixels = []
    # Bright luminous milky-white palette with pale electric cyan Vex currents
    palette = [
        (255, 255, 255, 245),  # pure bright core
        (244, 252, 255, 238),  # luminous milk white
        (228, 246, 252, 232),  # soft icy white
        (198, 236, 248, 226),  # pale arc cyan ripple
        (172, 226, 244, 222),  # deeper Vex current vein
    ]
    for f in range(frames):
        phase = (f / frames) * 2.0 * math.pi
        for y in range(16):
            row = []
            for x in range(16):
                # Tileable flowing wave + subtle Vex geometric lattice shimmer
                wave1 = math.sin((x / 16.0) * 2.0 * math.pi + phase)
                wave2 = math.cos((y / 16.0) * 2.0 * math.pi - phase * 1.0 + wave1 * 0.8)
                grid = 1.0 if ((x + f) % 8 == 0 and (y + f // 2) % 4 == 0) else 0.0
                val = 0.5 * wave1 + 0.5 * wave2 + 0.35 * grid
                if val > 0.72:
                    c = palette[0]
                elif val > 0.25:
                    c = palette[1]
                elif val > -0.25:
                    c = palette[2]
                elif val > -0.68:
                    c = palette[3]
                else:
                    c = palette[4]
                row.append(c)
            pixels.append(row)
    out = ASSETS / "textures" / "block" / "fake_radiolaria.png"
    write_png_rgba(out, w, h, pixels)


# ---------------------------------------------------------------------------
# 2. Destiny-Referenced Scaled .nbt Structures
# ---------------------------------------------------------------------------
def build_fallen_outpost() -> StructureBuilder:
    """
    Fallen Forest Base (EDZ Winding Cove / Trostland Ketch & Walker Outpost)
    31 x 19 x 31: Perimeter scrap barricades, elevated Skiff landing pad & truss deck,
    glowing cyan Ether supply silos, twin sniper watchtowers, House banners, under-deck armory.
    """
    s = StructureBuilder(31, 19, 31)
    # Foundation pad & forest clearing floor
    s.fill(1, 0, 1, 29, 0, 29, "minecraft:mossy_cobblestone")
    s.fill(2, 1, 2, 28, 1, 28, "minecraft:coarse_dirt")
    s.fill(5, 1, 5, 25, 1, 25, "minecraft:polished_deepslate")
    # Clear air above
    s.fill(1, 2, 1, 29, 18, 29, "minecraft:air")

    # Perimeter angled scrap barricades with 4 gated entry gaps
    for i in range(2, 29):
        if 12 <= i <= 18:
            continue
        wall_mat = "minecraft:oxidized_cut_copper" if (i % 3 != 0) else "minecraft:exposed_cut_copper"
        for (x, z) in [(i, 2), (i, 28), (2, i), (28, i)]:
            s.set(x, 2, z, "minecraft:deepslate_bricks")
            s.set(x, 3, z, wall_mat)
            if i % 2 == 0:
                s.set(x, 4, z, "minecraft:iron_bars")

    # Entry gate arches (North & South) with Fallen Dusk/Devils banners
    for gz in (2, 28):
        for gx in (12, 18):
            s.fill(gx, 2, gz, gx, 6, gz, "minecraft:polished_deepslate")
            s.set(gx, 7, gz, "minecraft:soul_lantern")
        s.fill(12, 6, gz, 18, 6, gz, "minecraft:oxidized_cut_copper")
        s.set(15, 7, gz, "minecraft:purple_wool")
        s.set(14, 7, gz, "minecraft:red_wool")
        s.set(16, 7, gz, "minecraft:red_wool")

    # Twin Sniper Watchtowers (NW & SE corners)
    for (tx, tz) in [(3, 3), (23, 23)]:
        s.hollow_box(tx, 2, tz, tx + 4, 10, tz + 4, "minecraft:deepslate_bricks", floor="minecraft:polished_deepslate", roof="minecraft:oxidized_cut_copper")
        # Open top sniper deck
        s.fill(tx + 1, 11, tz + 1, tx + 3, 14, tz + 3, "minecraft:air")
        for dx in range(5):
            for dz in range(5):
                if dx in (0, 4) or dz in (0, 4):
                    s.set(tx + dx, 11, tz + dz, "minecraft:exposed_cut_copper")
                    if (dx in (0, 4)) and (dz in (0, 4)):
                        s.fill(tx + dx, 12, tz + dz, tx + dx, 14, tz + dz, "minecraft:iron_bars")
        s.fill(tx, 15, tz, tx + 4, 15, tz + 4, "minecraft:oxidized_cut_copper")
        s.set(tx + 2, 14, tz + 2, "minecraft:soul_lantern", {"hanging": "true"})
        # Doorway & ladder
        s.fill(tx + 2, 2, tz + 4 if tz < 15 else tz, tx + 2, 4, tz + 4 if tz < 15 else tz, "minecraft:air")
        for ly in range(2, 11):
            s.set(tx + 1, ly, tz + 1, "minecraft:ladder", {"facing": "east"})

    # Central Under-Deck Armory Bunker (x=10..20, y=2..6, z=10..20)
    s.hollow_box(10, 2, 10, 20, 6, 20, "minecraft:deepslate_tiles", floor="minecraft:polished_deepslate", roof="minecraft:copper_grate")
    # Armory entrances (East & West)
    for ax in (10, 20):
        s.fill(ax, 2, 14, ax, 4, 16, "minecraft:air")
    # Fallen Loot Chest & Salvage Workstations inside
    s.set(15, 2, 15, "destiny2mc:fallen_chest", {"facing": "north"})
    s.set(12, 2, 12, "minecraft:barrel", {"facing": "up"})
    s.set(18, 2, 12, "minecraft:barrel", {"facing": "up"})
    s.set(12, 2, 18, "minecraft:anvil")
    s.set(18, 2, 18, "minecraft:blast_furnace", {"facing": "north"})
    s.set(15, 5, 15, "minecraft:sea_lantern")

    # Elevated Skiff Landing Pad & Command Platform (y=7, x=8..22, z=8..22)
    s.fill(8, 7, 8, 22, 7, 22, "minecraft:polished_deepslate")
    s.fill(10, 7, 10, 20, 7, 20, "minecraft:gray_concrete")
    # Landing pad cyan/light-blue guidance markings
    for p in range(11, 20):
        s.set(p, 7, 11, "minecraft:light_blue_concrete")
        s.set(p, 7, 19, "minecraft:light_blue_concrete")
        s.set(11, 7, p, "minecraft:light_blue_concrete")
        s.set(19, 7, p, "minecraft:light_blue_concrete")
    s.set(15, 7, 15, "minecraft:sea_lantern")
    # Support trusses at pad corners
    for cx in (8, 22):
        for cz in (8, 22):
            s.fill(cx, 2, cz, cx, 6, cz, "minecraft:iron_block")
    # Ramp stairs up to the Skiff deck (South side)
    for step in range(6):
        s.fill(14, 2 + step, 27 - step, 16, 2 + step, 27 - step, "minecraft:polished_deepslate_stairs", {"facing": "north"})
        s.fill(14, 2, 27 - step, 16, 1 + step, 27 - step, "minecraft:deepslate_bricks")

    # Overhanging Ketch-style Corrugated Canopy & House Banner Mast (North side of deck)
    for x in range(10, 21):
        s.fill(x, 8, 8, x, 11, 8, "minecraft:oxidized_cut_copper")
        s.fill(x, 12, 8, x, 12, 12, "minecraft:exposed_cut_copper")
    s.fill(15, 8, 8, 15, 17, 8, "minecraft:iron_bars")
    s.fill(14, 14, 7, 16, 16, 7, "minecraft:purple_wool")
    s.set(15, 18, 8, "minecraft:sea_lantern")

    # Two Iconic Glowing Cyan Ether Supply Tanks (NE & SW quadrants)
    for (ex, ez) in [(24, 6), (6, 24)]:
        s.fill(ex - 1, 2, ez - 1, ex + 1, 2, ez + 1, "minecraft:polished_deepslate")
        s.fill(ex - 1, 8, ez - 1, ex + 1, 8, ez + 1, "minecraft:oxidized_cut_copper")
        for ty in range(3, 8):
            s.set(ex, ty, ez, "minecraft:sea_lantern")
            for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                s.set(ex + dx, ty, ez + dz, "minecraft:light_blue_stained_glass")
            for dx, dz in [(-1, -1), (-1, 1), (1, -1), (1, 1)]:
                s.set(ex + dx, ty, ez + dz, "minecraft:iron_bars")
        s.set(ex, 9, ez, "minecraft:lightning_rod")

    return s


def build_hive_citadel() -> StructureBuilder:
    """
    Deep Hive Citadel that goes UP (+21 above surface) and DOWN (-26 deep underground).
    Inspired by Moon Hellmouth + Scarlet Keep + Summoning Pits.
    31 x 48 x 31, placed with start_height = -26 relative to WORLD_SURFACE_WG.
    y=26 is ground level:
      - y=26..47: Jagged gothic Hive spire, arched calcified bone ribs, soulfire braziers, upper catwalks.
      - y=24..26: Surface grand gateway & central Hellmouth Pit opening.
      - y=0..25: Descending spiral/tiered stair shaft -> Mid-level ribbed catacomb galleries -> Deep Summoning Pit ritual hall.
    """
    s = StructureBuilder(31, 48, 31)
    GROUND = 26

    # --- LOWER HALF: DEEP UNDERGROUND HIVE (y = 0 .. 25) ---
    # Solid carved stone/deepslate shell so underground rooms stay clean and enclosed
    s.hollow_box(2, 0, 2, 28, 12, 28, "minecraft:deepslate_tiles", floor="minecraft:polished_blackstone_bricks", roof="minecraft:blackstone")
    # Deep Summoning Pit Ritual Chamber (y=1..11, x=3..27, z=3..27)
    s.fill(3, 1, 3, 27, 11, 27, "minecraft:air")
    # Central raised Summoning Altar dais
    s.fill(11, 1, 11, 19, 1, 19, "minecraft:red_nether_bricks")
    s.fill(13, 2, 13, 17, 2, 17, "minecraft:polished_blackstone_bricks")
    s.set(15, 3, 15, "minecraft:verdant_froglight")
    # Four corner soulfire braziers around the deep altar
    for bx in (10, 20):
        for bz in (10, 20):
            s.fill(bx, 1, bz, bx, 2, bz, "minecraft:chiseled_polished_blackstone")
            s.set(bx, 3, bz, "minecraft:soul_soil")
            s.set(bx, 4, bz, "minecraft:soul_fire")
    # Ribbed bone-vault pillars lining the deep chamber
    for px in (5, 11, 19, 25):
        for pz in (5, 25):
            s.fill(px, 1, pz, px, 10, pz, "minecraft:polished_blackstone_bricks")
            s.set(px, 6, pz, "minecraft:verdant_froglight")
            # Curved bone rib arching inward
            step_z = 1 if pz == 5 else -1
            s.set(px, 9, pz + step_z, "minecraft:bone_block", {"axis": "z"})
            s.set(px, 10, pz + 2 * step_z, "minecraft:bone_block", {"axis": "z"})
            s.set(px, 11, pz + 3 * step_z, "minecraft:bone_block", {"axis": "z"})
    # Deep Sanctum Loot Vault (North alcove)
    s.fill(13, 1, 4, 17, 2, 6, "minecraft:red_nether_bricks")
    s.set(15, 3, 5, "destiny2mc:awoken_chest", {"facing": "south"})
    s.set(13, 3, 5, "minecraft:soul_lantern")
    s.set(17, 3, 5, "minecraft:soul_lantern")

    # Mid-Level Catacomb Gallery (y=13..23, x=6..24, z=6..24)
    s.hollow_box(6, 13, 6, 24, 23, 24, "minecraft:blackstone", floor="minecraft:deepslate_tiles", roof="minecraft:polished_blackstone_bricks")
    s.fill(7, 14, 7, 23, 22, 23, "minecraft:air")
    # Open central Hellmouth Pit Shaft cutting straight from y=1..28 (x=12..18, z=12..18)
    s.fill(12, 12, 12, 18, GROUND, 18, "minecraft:air")
    # Ring the pit opening on mid-level with jagged bone teeth & verdant runes
    for i in range(11, 20):
        for (rx, rz) in [(i, 11), (i, 19), (11, i), (19, i)]:
            s.set(rx, 13, rz, "minecraft:red_nether_bricks")
            if i % 2 == 0:
                s.set(rx, 14, rz, "minecraft:blackstone_wall")
    # Hanging chain & soulfire censers down the central shaft
    for cx, cz in [(13, 13), (17, 17), (13, 17), (17, 13)]:
        s.fill(cx, 16, cz, cx, 23, cz, "minecraft:chain")
        s.set(cx, 15, cz, "minecraft:verdant_froglight")
    # Mid-level catacomb alcoves & lighting
    for mx, mz in [(8, 8), (22, 8), (8, 22), (22, 22)]:
        s.fill(mx, 14, mz, mx, 21, mz, "minecraft:bone_block", {"axis": "y"})
        s.set(mx, 17, mz, "minecraft:verdant_froglight")

    # Descending Wide Staircase hugging the inner wall from Surface (y=26) -> Mid (y=14) -> Deep (y=1)
    # Segment 1: Surface (y=26) down to Mid-Level (y=14) along East wall (x=21..22, z=8..20)
    for i in range(12):
        sy = GROUND - i
        sz = 9 + i
        s.fill(21, sy, sz, 22, sy, sz, "minecraft:polished_blackstone_brick_stairs", {"facing": "north"})
        s.fill(21, sy + 1, sz, 22, sy + 4, sz, "minecraft:air")
        s.fill(21, sy - 1, sz, 22, sy - 1, sz, "minecraft:blackstone")
    # Segment 2: Mid-Level (y=14) down to Deep Chamber (y=2) along West wall (x=8..9, z=20..8)
    for i in range(12):
        sy = 14 - i
        sz = 20 - i
        s.fill(8, sy, sz, 9, sy, sz, "minecraft:polished_blackstone_brick_stairs", {"facing": "south"})
        s.fill(8, sy + 1, sz, 9, sy + 4, sz, "minecraft:air")
        s.fill(8, sy - 1, sz, 9, sy - 1, sz, "minecraft:blackstone")

    # --- SURFACE FOUNDATION & GRAND GATEWAY (y = 24 .. 26) ---
    s.fill(3, GROUND - 2, 3, 27, GROUND, 27, "minecraft:deepslate")
    s.fill(5, GROUND, 5, 25, GROUND, 25, "minecraft:polished_blackstone_bricks")
    # Re-open the central Hellmouth shaft and stair entrance at surface
    s.fill(12, GROUND - 2, 12, 18, GROUND, 18, "minecraft:air")
    s.fill(21, GROUND - 2, 9, 22, GROUND, 12, "minecraft:air")
    # Clear air above ground
    s.fill(1, GROUND + 1, 1, 29, 47, 29, "minecraft:air")

    # --- UPPER HALF: TOWERING GOTHIC HIVE CITADEL SPIRE (y = 27 .. 47) ---
    # Outer jagged bastion walls with 4 pointed arch entrances
    s.hollow_box(6, GROUND + 1, 6, 24, GROUND + 9, 24, "minecraft:polished_blackstone_bricks", floor=None, roof="minecraft:blackstone", interior=None)
    # 4 Pointed Gothic Hive Gateways (N/S/E/W)
    for (gx0, gz0, gx1, gz1) in [(13, 6, 17, 6), (13, 24, 17, 24), (6, 13, 6, 17), (24, 13, 24, 17)]:
        s.fill(gx0, GROUND + 1, gz0, gx1, GROUND + 5, gz1, "minecraft:air")
        # Pointed apex
        midx, midz = (gx0 + gx1) // 2, (gz0 + gz1) // 2
        s.set(midx, GROUND + 6, midz, "minecraft:air")
        s.set(midx, GROUND + 7, midz, "minecraft:verdant_froglight")

    # Scarlet Keep / Hive corner towers rising to y=43
    for (cx, cz) in [(5, 5), (23, 5), (5, 23), (23, 23)]:
        s.fill(cx, GROUND + 1, cz, cx + 2, GROUND + 13, cz + 2, "minecraft:blackstone")
        s.fill(cx + 1, GROUND + 2, cz + 1, cx + 1, GROUND + 12, cz + 1, "minecraft:red_nether_bricks")
        s.fill(cx + 1, GROUND + 14, cz + 1, cx + 1, GROUND + 17, cz + 1, "minecraft:bone_block", {"axis": "y"})
        s.set(cx + 1, GROUND + 18, cz + 1, "minecraft:verdant_froglight")

    # Iconic Giant Calcified Bone Ribs arching high over the central Hellmouth Pit
    for offset in (-5, 0, 5):
        for step in range(6):
            rx_l = 8 + step
            rx_r = 22 - step
            ry = GROUND + 8 + int(step * 1.4)
            s.set(rx_l, ry, 15 + offset, "minecraft:bone_block", {"axis": "y"})
            s.set(rx_r, ry, 15 + offset, "minecraft:bone_block", {"axis": "y"})

    # Central Upper Spire Crown (y=36..47) with open gothic ribs & soulfire beacon
    s.hollow_box(10, GROUND + 10, 10, 20, GROUND + 15, 20, "minecraft:red_nether_bricks", floor=None, roof=None, interior="minecraft:air")
    for sx in (10, 20):
        for sz in (10, 20):
            s.fill(sx, GROUND + 10, sz, sx, GROUND + 20, sz, "minecraft:polished_blackstone_bricks")
            s.set(sx, GROUND + 21, sz, "minecraft:bone_block", {"axis": "y"})
    # Floating Hive Soulfire Crown at top
    s.fill(13, GROUND + 16, 13, 17, GROUND + 16, 17, "minecraft:blackstone")
    s.set(15, GROUND + 17, 15, "minecraft:soul_soil")
    s.set(15, GROUND + 18, 15, "minecraft:soul_fire")
    for dx, dz in [(-2, 0), (2, 0), (0, -2), (0, 2)]:
        s.set(15 + dx, GROUND + 17, 15 + dz, "minecraft:verdant_froglight")

    return s


def build_vex_gate(s: StructureBuilder, cx: int, base_y: int, cz: int, radius: int = 6, along_x: bool = True):
    """Builds an iconic Destiny Vex circular/octagonal Transfer Gate with glowing energy aperture."""
    # Pedestal & radiant fake_radiolaria base conduit
    for d in range(-radius - 1, radius + 2):
        for w in (-1, 0, 1):
            x = cx + d if along_x else cx + w
            z = cz + w if along_x else cz + d
            s.set(x, base_y, z, "minecraft:polished_deepslate")
            if abs(d) <= radius - 2 and w == 0:
                s.set(x, base_y, z, "destiny2mc:fake_radiolaria")

    center_y = base_y + radius + 1
    for dy in range(-radius - 1, radius + 2):
        for dd in range(-radius - 1, radius + 2):
            dist = math.hypot(dd, dy)
            x = cx + dd if along_x else cx
            z = cz if along_x else cz + dd
            y = center_y + dy
            if y <= base_y:
                continue
            if radius - 0.6 <= dist <= radius + 0.9:
                # Main geometric Vex bronze/stone ring
                mat = "minecraft:cut_copper" if ((abs(dd) + abs(dy)) % 3 == 0) else "minecraft:polished_deepslate"
                s.set(x, y, z, mat)
                # Depth ribs on front/back of ring
                if (abs(dd) + abs(dy)) % 2 == 0:
                    if along_x:
                        s.set(x, y, z - 1, "minecraft:stone_bricks")
                        s.set(x, y, z + 1, "minecraft:stone_bricks")
                    else:
                        s.set(x - 1, y, z, "minecraft:stone_bricks")
                        s.set(x + 1, y, z, "minecraft:stone_bricks")
            elif dist < radius - 0.6:
                # Glowing luminous white-cyan Vex gate portal membrane
                if dist < 1.6:
                    s.set(x, y, z, "minecraft:sea_lantern")
                else:
                    s.set(x, y, z, "minecraft:white_stained_glass")
    # Radiating top & side geometric Vex fins
    for fin_y in range(center_y + radius + 1, center_y + radius + 4):
        s.set(cx, fin_y, cz, "minecraft:smooth_stone")
    s.set(cx, center_y + radius + 4, cz, "minecraft:end_rod", {"facing": "up"})


def build_vex_gate_shrine() -> StructureBuilder:
    """
    Vex Transfer Gate Shrine (Nessus / Vault of Glass).
    29 x 21 x 29: Stepped geometric Vex stone plaza, glowing white fake_radiolaria channels
    and corner waterfall basins, towering circular Vex Gate, floating geometric cubes, and loot cache.
    """
    s = StructureBuilder(29, 21, 29)
    # Stepped geometric foundation
    s.fill(1, 0, 1, 27, 0, 27, "minecraft:polished_deepslate")
    s.fill(2, 1, 2, 26, 1, 26, "minecraft:smooth_stone")
    s.fill(1, 2, 1, 27, 20, 27, "minecraft:air")

    # Sunken glowing white Fake Radiolaria moat/channels crisscrossing the plaza
    for i in range(4, 25):
        s.set(i, 1, 5, "destiny2mc:fake_radiolaria")
        s.set(i, 1, 23, "destiny2mc:fake_radiolaria")
        s.set(5, 1, i, "destiny2mc:fake_radiolaria")
        s.set(23, 1, i, "destiny2mc:fake_radiolaria")
    # Bridge walkways across the radiolaria moat (N/S/E/W)
    for b in range(12, 17):
        s.set(b, 1, 5, "minecraft:chiseled_stone_bricks")
        s.set(b, 1, 23, "minecraft:chiseled_stone_bricks")
        s.set(5, 1, b, "minecraft:chiseled_stone_bricks")
        s.set(23, 1, b, "minecraft:chiseled_stone_bricks")

    # Raised central Conflux dais
    s.fill(8, 2, 8, 20, 2, 20, "minecraft:stone_bricks")
    s.fill(10, 3, 10, 18, 3, 18, "minecraft:smooth_stone")
    # Glowing fake_radiolaria pool strips flanking the gate
    s.fill(9, 2, 10, 9, 2, 18, "destiny2mc:fake_radiolaria")
    s.fill(19, 2, 10, 19, 2, 18, "destiny2mc:fake_radiolaria")

    # Iconic Towering Vex Transfer Gate at center
    build_vex_gate(s, 14, 3, 14, radius=6, along_x=True)

    # Four Stepped Vex Corner Monoliths with cascading Fake Radiolaria conduits
    for (mx, mz) in [(3, 3), (23, 3), (3, 23), (23, 23)]:
        s.fill(mx, 2, mz, mx + 2, 9, mz + 2, "minecraft:polished_deepslate")
        s.fill(mx + 1, 10, mz + 1, mx + 1, 13, mz + 1, "minecraft:smooth_stone")
        s.set(mx + 1, 14, mz + 1, "minecraft:sea_lantern")
        # Glowing fake_radiolaria column flowing down the inner corner face
        ix = mx + 2 if mx < 14 else mx
        iz = mz + 2 if mz < 14 else mz
        s.fill(ix, 2, iz, ix, 8, iz, "destiny2mc:fake_radiolaria")

    # Floating Geometric Vex Cubes hovering on East & West flanks
    for fx in (6, 22):
        s.fill(fx - 1, 12, 13, fx + 1, 14, 15, "minecraft:polished_deepslate")
        s.set(fx, 13, 14, "minecraft:sea_lantern")
        s.set(fx, 15, 14, "minecraft:cut_copper")

    # Conflux Pedestal & Loot Chest in front of the Gate
    s.set(14, 4, 20, "minecraft:chiseled_stone_bricks")
    s.set(14, 5, 20, "minecraft:end_rod", {"facing": "up"})
    s.set(14, 4, 9, "destiny2mc:cosmodrome_chest", {"facing": "south"})

    return s


def build_vex_citadel_conflux() -> StructureBuilder:
    """
    Vex Stepped Temple / Cistern Spire + Integrated Entrance Vex Gate (Pyramidion / Nessus).
    33 x 24 x 33: Multi-tiered geometric cantilevered temple, cascading fake_radiolaria aqueducts,
    sunken central radiolaria chamber, integrated Vex Transfer Gate at front entrance, and loot vault.
    """
    s = StructureBuilder(33, 24, 33)
    s.fill(1, 0, 1, 31, 0, 31, "minecraft:deepslate")
    s.fill(2, 1, 2, 30, 1, 30, "minecraft:polished_deepslate")
    s.fill(1, 2, 1, 31, 23, 31, "minecraft:air")

    # Lower Temple Outer Stepped Perimeter (x=4..28, z=4..24)
    s.hollow_box(4, 2, 4, 28, 9, 24, "minecraft:stone_bricks", floor="minecraft:smooth_stone", roof="minecraft:polished_deepslate", interior="minecraft:air")
    # Open grand front entrance facing South (z=24)
    s.fill(12, 2, 24, 20, 7, 24, "minecraft:air")
    # Side geometric window slits
    for sz in (8, 12, 16, 20):
        s.fill(4, 4, sz, 4, 7, sz, "minecraft:white_stained_glass")
        s.fill(28, 4, sz, 28, 7, sz, "minecraft:white_stained_glass")

    # Sunken Glowing White Fake Radiolaria Basin inside the main chamber
    s.fill(9, 1, 8, 23, 1, 20, "destiny2mc:fake_radiolaria")
    # Geometric stepping causeway over the radiolaria pool
    s.fill(15, 2, 8, 17, 2, 24, "minecraft:smooth_stone")
    s.fill(9, 2, 13, 23, 2, 15, "minecraft:smooth_stone")
    # Central Conflux Spire & Data Lattice
    s.fill(15, 3, 13, 17, 5, 15, "minecraft:polished_deepslate")
    s.set(16, 6, 14, "minecraft:sea_lantern")
    s.fill(16, 7, 14, 16, 12, 14, "minecraft:end_rod", {"facing": "up"})
    s.set(16, 3, 9, "destiny2mc:cosmodrome_chest", {"facing": "south"})

    # Tier 2 Cantilevered Geometric Pyramidion Upper Deck (y=10..16)
    s.hollow_box(8, 10, 6, 24, 15, 22, "minecraft:smooth_stone", floor="minecraft:polished_deepslate", roof="minecraft:polished_deepslate", interior="minecraft:air")
    # Open skylight over Conflux
    s.fill(13, 10, 11, 19, 15, 17, "minecraft:air")
    # Tier 3 Iconic Split-Apex Vex Crown (y=16..23)
    for bx in (10, 20):
        s.fill(bx, 16, 11, bx + 2, 21, 17, "minecraft:polished_deepslate")
        s.fill(bx + 1, 22, 13, bx + 1, 23, 15, "minecraft:cut_copper")
    # Cascading Fake Radiolaria Aqueducts flowing from Tier 2 down into the outer basins
    for ax in (6, 26):
        s.fill(ax, 2, 14, ax, 14, 14, "destiny2mc:fake_radiolaria")
        s.fill(ax - 1, 1, 13, ax + 1, 1, 15, "destiny2mc:fake_radiolaria")

    # Integrated Iconic Vex Transfer Gate at the South Entrance Plaza (z=28)
    s.fill(9, 1, 25, 23, 1, 30, "minecraft:smooth_stone")
    build_vex_gate(s, 16, 1, 28, radius=5, along_x=True)

    return s


def build_cabal_firebase() -> StructureBuilder:
    """
    Big Cabal Base (EDZ Firebase Hades / Red Legion Forward Operating Base).
    41 x 22 x 41: Heavy sloped brutalist armor walls, Red Legion crimson & gold trim,
    chamfered corner watchtowers, twin anti-air turrets, armored blast gate,
    central vehicle courtyard & Thresher landing pad, multi-story rear command bunker & war room.
    """
    s = StructureBuilder(41, 22, 41)
    # Armored foundation & scorched tarmac courtyard
    s.fill(1, 0, 1, 39, 0, 39, "minecraft:deepslate")
    s.fill(2, 1, 2, 38, 1, 38, "minecraft:gray_concrete")
    s.fill(1, 2, 1, 39, 21, 39, "minecraft:air")

    # Outer Sloped Brutalist Perimeter Walls (thickness 3 at base tapering to 2 at battlements)
    for y in range(2, 9):
        for i in range(3, 38):
            # South Blast Gate gap
            if 16 <= i <= 24 and y <= 7:
                pass
            else:
                mat = "minecraft:red_concrete" if y == 6 else ("minecraft:deepslate_tiles" if y < 6 else "minecraft:polished_deepslate")
                s.set(i, y, 38, mat)
                if y <= 4:
                    s.set(i, y, 39, "minecraft:blackstone")
            # North, West, East walls
            mat = "minecraft:red_concrete" if y == 6 else ("minecraft:deepslate_tiles" if y < 6 else "minecraft:polished_deepslate")
            s.set(i, y, 2, mat)
            s.set(2, y, i, mat)
            s.set(38, y, i, mat)
            if y <= 4:
                s.set(i, y, 1, "minecraft:blackstone")
                s.set(1, y, i, "minecraft:blackstone")
                s.set(39, y, i, "minecraft:blackstone")
        # Walkway ledge inside perimeter at y=7
        if y == 7:
            for i in range(4, 37):
                s.set(i, 7, 3, "minecraft:polished_deepslate")
                s.set(3, 7, i, "minecraft:polished_deepslate")
                s.set(37, 7, i, "minecraft:polished_deepslate")
                if not (16 <= i <= 24):
                    s.set(i, 7, 37, "minecraft:polished_deepslate")

    # Heavy Armored South Blast Gate Frame with Red Legion Warning Stripes
    for gx in (15, 25):
        s.fill(gx, 2, 36, gx, 10, 39, "minecraft:polished_deepslate")
        s.set(gx, 6, 39, "minecraft:red_stained_glass")
        s.set(gx, 6, 38, "minecraft:shroomlight")
    s.fill(15, 8, 36, 25, 10, 39, "minecraft:polished_deepslate")
    s.fill(16, 9, 39, 24, 9, 39, "minecraft:red_concrete")
    s.set(20, 9, 39, "minecraft:gold_block")
    # Hazard stripes on entrance floor
    for gx in range(16, 25):
        s.set(gx, 1, 37, "minecraft:yellow_concrete" if gx % 2 == 0 else "minecraft:black_concrete")

    # Four Heavy Corner Bastion Watchtowers (SW, SE, NW, NE)
    for (tx, tz) in [(2, 2), (33, 2), (2, 33), (33, 33)]:
        s.hollow_box(tx, 2, tz, tx + 5, 12, tz + 5, "minecraft:polished_deepslate", floor="minecraft:gray_concrete", roof="minecraft:deepslate_tiles")
        # Red Legion armored stripe
        for d in range(6):
            for (rx, rz) in [(tx + d, tz), (tx + d, tz + 5), (tx, tz + d), (tx + 5, tz + d)]:
                s.set(rx, 8, rz, "minecraft:red_concrete")
        # Slanted bunker vision slits
        s.fill(tx + 2, 10, tz, tx + 3, 10, tz, "minecraft:red_stained_glass")
        s.fill(tx + 2, 10, tz + 5, tx + 3, 10, tz + 5, "minecraft:red_stained_glass")
        s.fill(tx, 10, tz + 2, tx, 10, tz + 3, "minecraft:red_stained_glass")
        s.fill(tx + 5, 10, tz + 2, tx + 5, 10, tz + 3, "minecraft:red_stained_glass")
        s.set(tx + 2, 11, tz + 2, "minecraft:shroomlight")
        # Corner battlement roof lip
        s.fill(tx + 1, 13, tz + 1, tx + 4, 13, tz + 4, "minecraft:blackstone")

    # Twin Cabal Anti-Air Artillery Turrets on the front two corner towers (SW & SE)
    for (ax, az) in [(4, 35), (35, 35)]:
        s.fill(ax, 14, az, ax + 1, 15, az + 1, "minecraft:iron_block")
        s.set(ax, 15, az, "minecraft:red_concrete")
        # Sloped twin cannon barrels pointing South
        for step in range(4):
            s.set(ax, 15 + (step // 2), az + 1 + step, "minecraft:polished_deepslate")
            s.set(ax + 1, 15 + (step // 2), az + 1 + step, "minecraft:polished_deepslate")

    # Central Vehicle Courtyard & Thresher Landing Pad (x=13..27, z=18..32)
    s.fill(13, 1, 18, 27, 1, 32, "minecraft:black_concrete")
    for p in range(14, 27):
        s.set(p, 1, 19, "minecraft:yellow_concrete")
        s.set(p, 1, 31, "minecraft:yellow_concrete")
        s.set(14, 1, p + 5 if p + 5 <= 31 else 31, "minecraft:yellow_concrete")
        s.set(26, 1, p + 5 if p + 5 <= 31 else 31, "minecraft:yellow_concrete")
    # Red Legion H-Pad center emblem
    s.fill(18, 1, 22, 18, 1, 28, "minecraft:red_concrete")
    s.fill(22, 1, 22, 22, 1, 28, "minecraft:red_concrete")
    s.fill(18, 1, 25, 22, 1, 25, "minecraft:red_concrete")
    s.set(20, 1, 25, "minecraft:gold_block")
    # Munitions crates & fuel pods on courtyard edges
    for (cx, cz) in [(8, 24), (8, 28), (31, 24), (31, 28)]:
        s.fill(cx, 2, cz, cx + 1, 3, cz + 1, "minecraft:red_terracotta")
        s.set(cx, 4, cz, "minecraft:barrel", {"facing": "up"})

    # Rear Multi-Story Command Bunker & War Room (x=8..32, y=2..16, z=4..16)
    s.hollow_box(8, 2, 4, 32, 10, 16, "minecraft:polished_deepslate", floor="minecraft:deepslate_tiles", roof="minecraft:deepslate_tiles")
    # Sloped upper Command Bridge (x=11..29, y=11..16, z=5..14)
    s.hollow_box(11, 11, 5, 29, 16, 14, "minecraft:polished_deepslate", floor="minecraft:gray_concrete", roof="minecraft:blackstone")
    # Red Legion Banner & Gold Crest on Command Bunker facade
    s.fill(14, 13, 14, 26, 14, 14, "minecraft:red_stained_glass")  # Bridge panoramic viewport
    s.fill(11, 15, 14, 29, 15, 14, "minecraft:red_concrete")
    s.fill(19, 15, 14, 21, 15, 14, "minecraft:gold_block")
    # Communications radar array on roof (y=17..21)
    s.fill(19, 17, 9, 21, 17, 11, "minecraft:iron_block")
    s.fill(20, 18, 10, 20, 21, 10, "minecraft:iron_bars")
    s.set(20, 21, 11, "minecraft:shroomlight")

    # Wide Bunker Main Entrance (South facade of bunker, z=16)
    s.fill(17, 2, 16, 23, 6, 16, "minecraft:air")
    # Interior War Room: Holographic Tactical Table + Loot Caches
    s.fill(18, 2, 9, 22, 2, 11, "minecraft:polished_deepslate")
    s.fill(19, 3, 10, 21, 3, 10, "minecraft:sea_lantern")
    s.fill(19, 4, 10, 21, 4, 10, "minecraft:cyan_stained_glass")
    # Leviathan & Cosmodrome Loot Chests in the Armory Wing & Bridge
    s.set(11, 2, 7, "destiny2mc:leviathan_chest", {"facing": "south"})
    s.set(29, 2, 7, "destiny2mc:cosmodrome_chest", {"facing": "south"})
    # Interior stairs up to Command Bridge
    for step in range(9):
        s.fill(10, 2 + step, 14 - step, 11, 2 + step, 14 - step, "minecraft:polished_deepslate_stairs", {"facing": "north"})
        s.fill(10, 3 + step, 14 - step, 11, 5 + step, 14 - step, "minecraft:air")
    # Interior lighting
    for lx in (13, 20, 27):
        s.set(lx, 9, 10, "minecraft:shroomlight")
        s.set(lx, 15, 9, "minecraft:shroomlight")

    return s


# ---------------------------------------------------------------------------
# 3. Custom Configured & Placed Features
# ---------------------------------------------------------------------------
def generate_features():
    cf_dir = DATA / "worldgen" / "configured_feature"
    pf_dir = DATA / "worldgen" / "placed_feature"

    # A. Big Glowing White Fake Radiolaria Pool (minecraft:lake)
    write_json(cf_dir / "radiolaria_pool.json", {
        "type": "minecraft:lake",
        "config": {
            "fluid": {
                "type": "minecraft:simple_state_provider",
                "state": {"Name": "destiny2mc:fake_radiolaria"}
            },
            "barrier": {
                "type": "minecraft:simple_state_provider",
                "state": {"Name": "minecraft:polished_deepslate"}
            }
        }
    })
    write_json(pf_dir / "radiolaria_pool.json", {
        "feature": "destiny2mc:radiolaria_pool",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 3},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}
        ]
    })

    # B. Wide Shallow Fake Radiolaria Basin Disks (minecraft:disk)
    write_json(cf_dir / "radiolaria_basin.json", {
        "type": "minecraft:disk",
        "config": {
            "state_provider": {
                "fallback": {
                    "type": "minecraft:simple_state_provider",
                    "state": {"Name": "destiny2mc:fake_radiolaria"}
                },
                "rules": []
            },
            "target": {
                "type": "minecraft:matching_blocks",
                "blocks": [
                    "minecraft:stone",
                    "minecraft:smooth_stone",
                    "minecraft:polished_andesite",
                    "minecraft:red_terracotta",
                    "minecraft:terracotta",
                    "minecraft:deepslate",
                    "minecraft:grass_block",
                    "minecraft:dirt"
                ]
            },
            "radius": {
                "type": "minecraft:uniform",
                "min_inclusive": 4,
                "max_inclusive": 7
            },
            "half_height": 1
        }
    })
    write_json(pf_dir / "radiolaria_basin.json", {
        "feature": "destiny2mc:radiolaria_basin",
        "placement": [
            {"type": "minecraft:count", "count": 2},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}
        ]
    })

    # C. Vex Geometric Stone Monolith Pillars
    write_json(cf_dir / "vex_monolith.json", {
        "type": "minecraft:block_column",
        "config": {
            "direction": "up",
            "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
            "prioritize_tip": True,
            "layers": [
                {
                    "height": {"type": "minecraft:uniform", "min_inclusive": 3, "max_inclusive": 7},
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:polished_deepslate"}}
                },
                {
                    "height": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4},
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:smooth_stone"}}
                },
                {
                    "height": 1,
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:sea_lantern"}}
                }
            ]
        }
    })
    write_json(pf_dir / "vex_monolith.json", {
        "feature": "destiny2mc:vex_monolith",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 3},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}
        ]
    })

    # D. Cabal Dead Trees (leafless gnarled stripped dark oak + scorched basalt trunks)
    write_json(cf_dir / "cabal_dead_tree.json", {
        "type": "minecraft:tree",
        "config": {
            "ignore_vines": True,
            "force_dirt": False,
            "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
            "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:coarse_dirt"}},
            "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:stripped_dark_oak_log", "Properties": {"axis": "y"}}},
            "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:air"}},
            "trunk_placer": {
                "type": "minecraft:forking_trunk_placer",
                "base_height": 4,
                "height_rand_a": 3,
                "height_rand_b": 2
            },
            "foliage_placer": {
                "type": "minecraft:blob_foliage_placer",
                "radius": 0,
                "offset": 0,
                "height": 0
            },
            "decorators": []
        }
    })
    write_json(pf_dir / "cabal_dead_tree.json", {
        "feature": "destiny2mc:cabal_dead_tree",
        "placement": [
            {"type": "minecraft:count", "count": 3},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": ["minecraft:coarse_dirt", "minecraft:red_sand", "minecraft:orange_terracotta", "minecraft:terracotta", "minecraft:basalt", "minecraft:grass_block"]}},
            {"type": "minecraft:biome"}
        ]
    })

    write_json(cf_dir / "cabal_charred_tree.json", {
        "type": "minecraft:tree",
        "config": {
            "ignore_vines": True,
            "force_dirt": False,
            "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
            "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:coarse_dirt"}},
            "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:basalt", "Properties": {"axis": "y"}}},
            "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:air"}},
            "trunk_placer": {
                "type": "minecraft:forking_trunk_placer",
                "base_height": 3,
                "height_rand_a": 3,
                "height_rand_b": 1
            },
            "foliage_placer": {
                "type": "minecraft:blob_foliage_placer",
                "radius": 0,
                "offset": 0,
                "height": 0
            },
            "decorators": []
        }
    })
    write_json(pf_dir / "cabal_charred_tree.json", {
        "feature": "destiny2mc:cabal_charred_tree",
        "placement": [
            {"type": "minecraft:count", "count": 2},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": ["minecraft:coarse_dirt", "minecraft:red_sand", "minecraft:orange_terracotta", "minecraft:terracotta", "minecraft:basalt", "minecraft:grass_block"]}},
            {"type": "minecraft:biome"}
        ]
    })

    # E. Hive Chitin & Bone Spikes on Rocky Crags
    write_json(cf_dir / "hive_chitin_spike.json", {
        "type": "minecraft:block_column",
        "config": {
            "direction": "up",
            "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
            "prioritize_tip": True,
            "layers": [
                {
                    "height": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 5},
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:blackstone"}}
                },
                {
                    "height": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3},
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:bone_block", "Properties": {"axis": "y"}}}
                },
                {
                    "height": 1,
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:verdant_froglight"}}
                }
            ]
        }
    })
    write_json(pf_dir / "hive_chitin_spike.json", {
        "feature": "destiny2mc:hive_chitin_spike",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 3},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}
        ]
    })

    # F. Fallen Rusty Industrial Scrap Pylons
    write_json(cf_dir / "fallen_scrap_pillar.json", {
        "type": "minecraft:block_column",
        "config": {
            "direction": "up",
            "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
            "prioritize_tip": True,
            "layers": [
                {
                    "height": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4},
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:oxidized_cut_copper"}}
                },
                {
                    "height": 1,
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:exposed_cut_copper"}}
                },
                {
                    "height": 1,
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:iron_bars"}}
                }
            ]
        }
    })
    write_json(pf_dir / "fallen_scrap_pillar.json", {
        "feature": "destiny2mc:fallen_scrap_pillar",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 6},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}
        ]
    })


# ---------------------------------------------------------------------------
# 4. Four Destiny Faction Biomes (Strict Vanilla 1.21.1 Feature Ordering)
# ---------------------------------------------------------------------------
def base_overworld_features(lakes_extra=None, local_extra=None, surface_extra=None, vegetal_extra=None):
    """Returns the 11 GenerationStep lists in canonical Minecraft 1.21.1 vanilla order."""
    return [
        [],  # 0: raw_generation
        ["minecraft:lake_lava_underground", "minecraft:lake_lava_surface"] + (lakes_extra or []),  # 1: lakes
        ["minecraft:amethyst_geode"] + (local_extra or []),  # 2: local_modifications
        ["minecraft:monster_room", "minecraft:monster_room_deep"],  # 3: underground_structures
        surface_extra or [],  # 4: surface_structures
        [],  # 5: strongholds
        [  # 6: underground_ores
            "minecraft:ore_dirt",
            "minecraft:ore_gravel",
            "minecraft:ore_granite_upper",
            "minecraft:ore_granite_lower",
            "minecraft:ore_diorite_upper",
            "minecraft:ore_diorite_lower",
            "minecraft:ore_andesite_upper",
            "minecraft:ore_andesite_lower",
            "minecraft:ore_tuff",
            "minecraft:ore_coal_upper",
            "minecraft:ore_coal_lower",
            "minecraft:ore_iron_upper",
            "minecraft:ore_iron_middle",
            "minecraft:ore_iron_small",
            "minecraft:ore_gold",
            "minecraft:ore_gold_lower",
            "minecraft:ore_redstone",
            "minecraft:ore_redstone_lower",
            "minecraft:ore_diamond",
            "minecraft:ore_diamond_medium",
            "minecraft:ore_diamond_large",
            "minecraft:ore_diamond_buried",
            "minecraft:ore_lapis",
            "minecraft:ore_lapis_buried",
            "minecraft:ore_copper",
            "minecraft:underwater_magma",
            "minecraft:disk_sand",
            "minecraft:disk_clay",
            "minecraft:disk_gravel"
        ],
        [],  # 7: underground_decoration
        ["minecraft:spring_water", "minecraft:spring_lava"],  # 8: fluid_springs
        ["minecraft:glow_lichen"] + (vegetal_extra or []),  # 9: vegetal_decoration
        ["minecraft:freeze_top_layer"]  # 10: top_layer_modification
    ]


def base_carvers():
    return {
        "air": [
            "minecraft:cave",
            "minecraft:cave_extra_underground",
            "minecraft:canyon"
        ]
    }


def generate_biomes():
    bdir = DATA / "worldgen" / "biome"

    # 1. Fallen Timberland (EDZ / Winding Cove overgrown pine forest)
    write_json(bdir / "fallen_timberland.json", {
        "has_precipitation": True,
        "temperature": 0.45,
        "downfall": 0.75,
        "effects": {
            "sky_color": 0x8EA8B5,
            "fog_color": 0xA6BDC4,
            "water_color": 0x3D6E78,
            "water_fog_color": 0x1E3A42,
            "grass_color": 0x5D7A4E,
            "foliage_color": 0x4B6842,
            "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}
        },
        "carvers": base_carvers(),
        "features": base_overworld_features(
            local_extra=["minecraft:forest_rock"],
            surface_extra=["destiny2mc:fallen_scrap_pillar"],
            vegetal_extra=[
                "minecraft:trees_taiga",
                "minecraft:patch_large_fern",
                "minecraft:patch_grass_taiga_2",
                "minecraft:brown_mushroom_taiga",
                "minecraft:red_mushroom_taiga",
                "minecraft:patch_berry_common"
            ]
        ),
        "spawners": {
            "monster": [
                {"type": "destiny2mc:fallen_dreg", "weight": 35, "minCount": 2, "maxCount": 4},
                {"type": "destiny2mc:fallen_wretch", "weight": 22, "minCount": 1, "maxCount": 3},
                {"type": "destiny2mc:fallen_vandal_sniper", "weight": 20, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:fallen_shank", "weight": 18, "minCount": 1, "maxCount": 3},
                {"type": "destiny2mc:fallen_captain_rifle", "weight": 8, "minCount": 1, "maxCount": 1},
                {"type": "destiny2mc:fallen_captain_scorch", "weight": 5, "minCount": 1, "maxCount": 1}
            ],
            "creature": [
                {"type": "minecraft:wolf", "weight": 6, "minCount": 2, "maxCount": 4},
                {"type": "minecraft:fox", "weight": 6, "minCount": 2, "maxCount": 3}
            ],
            "ambient": [{"type": "minecraft:bat", "weight": 10, "minCount": 4, "maxCount": 8}],
            "axolotls": [], "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []
        },
        "spawn_costs": {}
    })

    # 2. Hive Crags (Moon Hellmouth / Scarlet Keep rocky crags)
    write_json(bdir / "hive_crags.json", {
        "has_precipitation": False,
        "temperature": 0.35,
        "downfall": 0.1,
        "effects": {
            "sky_color": 0x4A5244,
            "fog_color": 0x3B4436,
            "water_color": 0x3A5442,
            "water_fog_color": 0x1A2920,
            "grass_color": 0x4C5440,
            "foliage_color": 0x3E4635,
            "particle": {"options": {"type": "minecraft:ash"}, "probability": 0.015},
            "mood_sound": {"sound": "minecraft:ambient.soul_sand_valley.mood", "tick_delay": 4800, "block_search_extent": 8, "offset": 2.0}
        },
        "carvers": base_carvers(),
        "features": base_overworld_features(
            surface_extra=["destiny2mc:hive_chitin_spike"],
            vegetal_extra=["minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal"]
        ),
        "spawners": {
            "monster": [
                {"type": "destiny2mc:hive_thrall", "weight": 40, "minCount": 2, "maxCount": 5},
                {"type": "destiny2mc:hive_acolyte", "weight": 28, "minCount": 1, "maxCount": 3},
                {"type": "destiny2mc:hive_cursed_thrall", "weight": 14, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:hive_knight", "weight": 12, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:hive_wizard", "weight": 8, "minCount": 1, "maxCount": 1},
                {"type": "destiny2mc:hive_ogre", "weight": 4, "minCount": 1, "maxCount": 1}
            ],
            "creature": [],
            "ambient": [{"type": "minecraft:bat", "weight": 10, "minCount": 4, "maxCount": 8}],
            "axolotls": [], "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []
        },
        "spawn_costs": {}
    })

    # 3. Vex Radiolarian Expanse (Nessus / Pyramidion / Vault of Glass)
    write_json(bdir / "vex_expanse.json", {
        "has_precipitation": False,
        "temperature": 0.8,
        "downfall": 0.1,
        "effects": {
            "sky_color": 0xB8D6DE,
            "fog_color": 0xCFE6EC,
            "water_color": 0xE8F7FA,
            "water_fog_color": 0xB8DDE6,
            "grass_color": 0xA63838,
            "foliage_color": 0xB83B3B,
            "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}
        },
        "carvers": base_carvers(),
        "features": base_overworld_features(
            lakes_extra=["destiny2mc:radiolaria_pool"],
            local_extra=["destiny2mc:radiolaria_basin"],
            surface_extra=["destiny2mc:vex_monolith"],
            vegetal_extra=["minecraft:patch_grass_savanna"]
        ),
        "spawners": {
            "monster": [
                {"type": "destiny2mc:vex_goblin", "weight": 38, "minCount": 2, "maxCount": 4},
                {"type": "destiny2mc:vex_hobgoblin", "weight": 22, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:vex_harpy", "weight": 22, "minCount": 1, "maxCount": 3},
                {"type": "destiny2mc:vex_minotaur", "weight": 10, "minCount": 1, "maxCount": 1},
                {"type": "destiny2mc:vex_hydra", "weight": 5, "minCount": 1, "maxCount": 1},
                {"type": "destiny2mc:vex_wyvern", "weight": 4, "minCount": 1, "maxCount": 1}
            ],
            "creature": [],
            "ambient": [],
            "axolotls": [], "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []
        },
        "spawn_costs": {}
    })

    # 4. Cabal Barrens (EDZ Firebase Hades / Mars Freehold dead-tree wasteland)
    write_json(bdir / "cabal_barrens.json", {
        "has_precipitation": False,
        "temperature": 1.4,
        "downfall": 0.0,
        "effects": {
            "sky_color": 0xC97A53,
            "fog_color": 0xB56846,
            "water_color": 0x5C4D42,
            "water_fog_color": 0x2F2620,
            "grass_color": 0x7A5B3E,
            "foliage_color": 0x5C4A38,
            "particle": {"options": {"type": "minecraft:ash"}, "probability": 0.01},
            "mood_sound": {"sound": "minecraft:ambient.basalt_deltas.mood", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}
        },
        "carvers": base_carvers(),
        "features": base_overworld_features(
            vegetal_extra=[
                "minecraft:patch_dead_bush_badlands",
                "destiny2mc:cabal_dead_tree",
                "destiny2mc:cabal_charred_tree"
            ]
        ),
        "spawners": {
            "monster": [
                {"type": "destiny2mc:cabal_legionary", "weight": 34, "minCount": 2, "maxCount": 4},
                {"type": "destiny2mc:cabal_phalanx", "weight": 22, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:cabal_psion", "weight": 20, "minCount": 1, "maxCount": 3},
                {"type": "destiny2mc:cabal_warbeast", "weight": 22, "minCount": 2, "maxCount": 4},
                {"type": "destiny2mc:cabal_centurion", "weight": 8, "minCount": 1, "maxCount": 1},
                {"type": "destiny2mc:cabal_incendior", "weight": 7, "minCount": 1, "maxCount": 1},
                {"type": "destiny2mc:cabal_colossus", "weight": 5, "minCount": 1, "maxCount": 1}
            ],
            "creature": [],
            "ambient": [],
            "axolotls": [], "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []
        },
        "spawn_costs": {}
    })


# ---------------------------------------------------------------------------
# 5. Structures, Template Pools, Structure Sets, Biome Tags
# ---------------------------------------------------------------------------
def generate_structure_worldgen():
    sdir = DATA / "worldgen" / "structure"
    pdir = DATA / "worldgen" / "template_pool" / "factions"
    ssdir = DATA / "worldgen" / "structure_set"
    tdir = DATA / "tags" / "worldgen" / "biome"

    # Biome tags per faction + vanilla is_overworld tag
    write_json(tdir / "has_fallen_base.json", {"replace": False, "values": ["destiny2mc:fallen_timberland"]})
    write_json(tdir / "has_hive_citadel.json", {"replace": False, "values": ["destiny2mc:hive_crags"]})
    write_json(tdir / "has_vex_structures.json", {"replace": False, "values": ["destiny2mc:vex_expanse"]})
    write_json(tdir / "has_cabal_base.json", {"replace": False, "values": ["destiny2mc:cabal_barrens"]})
    write_json(MC_DATA / "tags" / "worldgen" / "biome" / "is_overworld.json", {
        "replace": False,
        "values": [
            "destiny2mc:fallen_timberland",
            "destiny2mc:hive_crags",
            "destiny2mc:vex_expanse",
            "destiny2mc:cabal_barrens"
        ]
    })

    # Template pools
    for name in ("fallen_outpost", "hive_citadel", "vex_gate_shrine", "vex_citadel_conflux", "cabal_firebase"):
        write_json(pdir / f"{name}.json", {
            "name": f"destiny2mc:factions/{name}",
            "fallback": "minecraft:empty",
            "elements": [{
                "weight": 1,
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "location": f"destiny2mc:factions/{name}",
                    "processors": {"processors": []},
                    "projection": "rigid"
                }
            }]
        })

    # Structure definitions
    write_json(sdir / "fallen_outpost.json", {
        "type": "minecraft:jigsaw",
        "biomes": "#destiny2mc:has_fallen_base",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "start_pool": "destiny2mc:factions/fallen_outpost",
        "size": 1,
        "start_height": {"absolute": 0},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 40,
        "use_expansion_hack": False,
        "spawn_overrides": {
            "monster": {
                "bounding_box": "piece",
                "spawns": [
                    {"type": "destiny2mc:fallen_dreg", "weight": 4, "minCount": 1, "maxCount": 3},
                    {"type": "destiny2mc:fallen_vandal_sniper", "weight": 3, "minCount": 1, "maxCount": 2},
                    {"type": "destiny2mc:fallen_captain_rifle", "weight": 1, "minCount": 1, "maxCount": 1},
                    {"type": "destiny2mc:fallen_shank", "weight": 2, "minCount": 1, "maxCount": 2}
                ]
            }
        }
    })

    write_json(sdir / "hive_citadel.json", {
        "type": "minecraft:jigsaw",
        "biomes": "#destiny2mc:has_hive_citadel",
        "step": "surface_structures",
        "terrain_adaptation": "none",
        "start_pool": "destiny2mc:factions/hive_citadel",
        "size": 1,
        "start_height": {"absolute": -26},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 48,
        "use_expansion_hack": False,
        "spawn_overrides": {
            "monster": {
                "bounding_box": "piece",
                "spawns": [
                    {"type": "destiny2mc:hive_thrall", "weight": 4, "minCount": 2, "maxCount": 4},
                    {"type": "destiny2mc:hive_acolyte", "weight": 3, "minCount": 1, "maxCount": 3},
                    {"type": "destiny2mc:hive_knight", "weight": 2, "minCount": 1, "maxCount": 2},
                    {"type": "destiny2mc:hive_wizard", "weight": 1, "minCount": 1, "maxCount": 1}
                ]
            }
        }
    })

    vex_spawns = {
        "monster": {
            "bounding_box": "piece",
            "spawns": [
                {"type": "destiny2mc:vex_goblin", "weight": 4, "minCount": 2, "maxCount": 3},
                {"type": "destiny2mc:vex_hobgoblin", "weight": 2, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:vex_harpy", "weight": 2, "minCount": 1, "maxCount": 2},
                {"type": "destiny2mc:vex_minotaur", "weight": 1, "minCount": 1, "maxCount": 1}
            ]
        }
    }

    write_json(sdir / "vex_gate_shrine.json", {
        "type": "minecraft:jigsaw",
        "biomes": "#destiny2mc:has_vex_structures",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "start_pool": "destiny2mc:factions/vex_gate_shrine",
        "size": 1,
        "start_height": {"absolute": 0},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 40,
        "use_expansion_hack": False,
        "spawn_overrides": vex_spawns
    })

    write_json(sdir / "vex_citadel_conflux.json", {
        "type": "minecraft:jigsaw",
        "biomes": "#destiny2mc:has_vex_structures",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "start_pool": "destiny2mc:factions/vex_citadel_conflux",
        "size": 1,
        "start_height": {"absolute": 0},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 44,
        "use_expansion_hack": False,
        "spawn_overrides": vex_spawns
    })

    write_json(sdir / "cabal_firebase.json", {
        "type": "minecraft:jigsaw",
        "biomes": "#destiny2mc:has_cabal_base",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "start_pool": "destiny2mc:factions/cabal_firebase",
        "size": 1,
        "start_height": {"absolute": 0},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 52,
        "use_expansion_hack": False,
        "spawn_overrides": {
            "monster": {
                "bounding_box": "piece",
                "spawns": [
                    {"type": "destiny2mc:cabal_legionary", "weight": 4, "minCount": 2, "maxCount": 3},
                    {"type": "destiny2mc:cabal_phalanx", "weight": 2, "minCount": 1, "maxCount": 2},
                    {"type": "destiny2mc:cabal_psion", "weight": 2, "minCount": 1, "maxCount": 2},
                    {"type": "destiny2mc:cabal_centurion", "weight": 1, "minCount": 1, "maxCount": 1},
                    {"type": "destiny2mc:cabal_colossus", "weight": 1, "minCount": 1, "maxCount": 1}
                ]
            }
        }
    })

    # Structure Sets (one per faction biome so each biome reliably spawns its own structures)
    write_json(ssdir / "fallen_bases.json", {
        "structures": [{"structure": "destiny2mc:fallen_outpost", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 18, "separation": 8, "salt": 410928371}
    })
    write_json(ssdir / "hive_citadels.json", {
        "structures": [{"structure": "destiny2mc:hive_citadel", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 20, "separation": 9, "salt": 520193847}
    })
    write_json(ssdir / "vex_structures.json", {
        "structures": [
            {"structure": "destiny2mc:vex_gate_shrine", "weight": 3},
            {"structure": "destiny2mc:vex_citadel_conflux", "weight": 2}
        ],
        "placement": {"type": "minecraft:random_spread", "spacing": 16, "separation": 7, "salt": 630918274}
    })
    write_json(ssdir / "cabal_bases.json", {
        "structures": [{"structure": "destiny2mc:cabal_firebase", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 22, "separation": 10, "salt": 740182936}
    })


# ---------------------------------------------------------------------------
# 6. Patch neoforge.mods.toml & en_us.json idempotently
# ---------------------------------------------------------------------------
def update_mods_toml():
    toml_path = RES / "META-INF" / "neoforge.mods.toml"
    text = toml_path.read_text(encoding="utf-8")
    if "ageless.world.mixins.json" not in text:
        text = text.rstrip() + '\n\n[[mixins]]\nconfig="ageless.world.mixins.json"\n'
        toml_path.write_text(text, encoding="utf-8")


def update_lang():
    lang_path = ASSETS / "lang" / "en_us.json"
    lang = json.loads(lang_path.read_text(encoding="utf-8"))
    additions = {
        "block.destiny2mc.fake_radiolaria": "Radiolaria",
        "death.attack.radiolaria": "%1$s was dissolved by Vex Radiolaria",
        "biome.destiny2mc.fallen_timberland": "Fallen Timberland",
        "biome.destiny2mc.hive_crags": "Hive Crags",
        "biome.destiny2mc.vex_expanse": "Radiolarian Expanse",
        "biome.destiny2mc.cabal_barrens": "Cabal Barrens"
    }
    lang.update(additions)
    lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def main():
    generate_radiolaria_texture()
    generate_features()
    generate_biomes()
    generate_structure_worldgen()
    update_mods_toml()
    update_lang()

    nbt_dir = DATA / "structure" / "factions"
    builders = {
        "fallen_outpost.nbt": build_fallen_outpost(),
        "hive_citadel.nbt": build_hive_citadel(),
        "vex_gate_shrine.nbt": build_vex_gate_shrine(),
        "vex_citadel_conflux.nbt": build_vex_citadel_conflux(),
        "cabal_firebase.nbt": build_cabal_firebase(),
    }
    for fname, sb in builders.items():
        p_cnt, b_cnt = sb.save(nbt_dir / fname)
        print(f"Wrote {fname}: size={sb.sx}x{sb.sy}x{sb.sz}, palette={p_cnt}, blocks={b_cnt}")


if __name__ == "__main__":
    main()
