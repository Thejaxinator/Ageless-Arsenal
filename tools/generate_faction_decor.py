#!/usr/bin/env python3
"""Generate 10 Destiny-inspired small decorative NBT micro-structures and worldgen JSONs."""

import gzip
import json
import pathlib
import struct

REPO = pathlib.Path(__file__).resolve().parent.parent
RES = REPO / "src/main/resources/data/destiny2mc"
NBT_DIR = RES / "structure/factions/decor"
POOL_DIR = RES / "worldgen/template_pool/factions/decor"
STRUCT_DIR = RES / "worldgen/structure"
SET_DIR = RES / "worldgen/structure_set"

for d in (NBT_DIR, POOL_DIR, STRUCT_DIR, SET_DIR):
    d.mkdir(parents=True, exist_ok=True)


# Minimal strict big-endian NBT writer (DataVersion 3955 = MC 1.21.1)
def _str(s: str) -> bytes:
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def _tag_int(name: str, val: int) -> bytes:
    return b"\x03" + _str(name) + struct.pack(">i", val)


def _tag_str(name: str, val: str) -> bytes:
    return b"\x08" + _str(name) + _str(val)


def _tag_list(name: str, elem_type: int, items: list[bytes]) -> bytes:
    return b"\x09" + _str(name) + struct.pack(">bi", elem_type, len(items)) + b"".join(items)


def _tag_compound(name: str, body: bytes) -> bytes:
    return b"\x0a" + _str(name) + body + b"\x00"


class VoxelGrid:
    def __init__(self, sx: int, sy: int, sz: int):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.blocks: dict[tuple[int, int, int], tuple[str, tuple[tuple[str, str], ...]]] = {}

    def set(self, x: int, y: int, z: int, block: str, **props):
        if 0 <= x < self.sx and 0 <= y < self.sy and 0 <= z < self.sz:
            self.blocks[(x, y, z)] = (block, tuple(sorted(props.items())))

    def fill(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int, block: str, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, block, **props)

    def clear(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.blocks.pop((x, y, z), None)

    def write_nbt(self, out_path: pathlib.Path):
        palette_map: dict[tuple[str, tuple[tuple[str, str], ...]], int] = {}
        palette_list: list[tuple[str, tuple[tuple[str, str], ...]]] = []

        def get_idx(key):
            if key not in palette_map:
                palette_map[key] = len(palette_list)
                palette_list.append(key)
            return palette_map[key]

        block_entries = []
        for (x, y, z), key in sorted(self.blocks.items()):
            st = get_idx(key)
            pos_payload = _tag_list("pos", 3, [struct.pack(">i", x), struct.pack(">i", y), struct.pack(">i", z)])
            entry = pos_payload + _tag_int("state", st) + b"\x00"
            block_entries.append(entry)

        pal_entries = []
        for name, props in palette_list:
            body = _tag_str("Name", name)
            if props:
                pbody = b"".join(_tag_str(k, v) for k, v in props)
                body += _tag_compound("Properties", pbody)
            pal_entries.append(body + b"\x00")

        root_body = (
            _tag_int("DataVersion", 3955)
            + _tag_list("size", 3, [struct.pack(">i", self.sx), struct.pack(">i", self.sy), struct.pack(">i", self.sz)])
            + _tag_list("palette", 10, pal_entries)
            + _tag_list("blocks", 10, block_entries)
            + _tag_list("entities", 10, [])
        )
        nbt_bytes = _tag_compound("", root_body)
        with gzip.open(out_path, "wb") as f:
            f.write(nbt_bytes)
        return len(self.blocks), len(palette_list)


# ---------------------------------------------------------------------------
# 1. FALLEN DECORATIONS (fallen_timberland)
# ---------------------------------------------------------------------------
def build_fallen_ether_cache() -> VoxelGrid:
    """9x7x9 House Dusk scrap supply depot with Ether tank and purple tarp."""
    g = VoxelGrid(9, 7, 9)
    # Gritty foundation pad
    g.fill(1, 0, 1, 7, 0, 7, "minecraft:coarse_dirt")
    for x, z in [(2, 2), (6, 2), (2, 6), (6, 6), (4, 4), (3, 5), (5, 3)]:
        g.set(x, 0, z, "minecraft:mossy_cobblestone")
    # 4 scrap iron-bar / copper support struts
    for x, z in [(1, 1), (7, 1), (1, 7), (7, 7)]:
        g.fill(x, 1, z, x, 3, z, "minecraft:iron_bars")
        g.set(x, 1, z, "minecraft:exposed_cut_copper")
    # Sloped purple House Dusk tarp canopy
    g.fill(1, 4, 1, 7, 4, 7, "minecraft:purple_wool")
    g.clear(1, 4, 1, 1, 4, 1)
    g.clear(7, 4, 7, 7, 4, 7)
    g.fill(2, 5, 2, 6, 5, 6, "minecraft:purple_wool")
    g.set(4, 5, 4, "minecraft:oxidized_cut_copper")
    # Glowing Ether tank in center-back
    g.set(4, 1, 2, "minecraft:deepslate_bricks")
    g.set(4, 2, 2, "minecraft:sea_lantern")
    g.set(4, 3, 2, "minecraft:cyan_stained_glass")
    g.set(3, 2, 2, "minecraft:copper_grate")
    g.set(5, 2, 2, "minecraft:copper_grate")
    # Stacked barrels and scrap crates
    g.set(2, 1, 3, "minecraft:barrel")
    g.set(2, 2, 3, "minecraft:barrel")
    g.set(2, 1, 5, "minecraft:blast_furnace")
    g.set(6, 1, 4, "minecraft:barrel")
    g.set(6, 1, 5, "minecraft:anvil")
    # Tall crooked Fallen antenna on one corner
    g.fill(7, 4, 1, 7, 6, 1, "minecraft:lightning_rod")
    g.set(6, 6, 1, "minecraft:iron_bars")
    g.set(4, 3, 6, "minecraft:soul_lantern")
    return g


def build_fallen_pike_wreck() -> VoxelGrid:
    """11x6x7 crashed Fallen Pike and rusted Ketch hull plating."""
    g = VoxelGrid(11, 6, 7)
    # Impact ground scar
    g.fill(1, 0, 1, 9, 0, 5, "minecraft:coarse_dirt")
    g.fill(2, 0, 2, 8, 0, 4, "minecraft:mossy_cobblestone")
    # Crashed Pike fuselage (long twin-prong nose pointing +X)
    g.fill(2, 1, 3, 7, 1, 3, "minecraft:deepslate_tiles")
    g.set(3, 2, 3, "minecraft:purple_wool")
    g.set(4, 2, 3, "minecraft:polished_deepslate")
    g.set(5, 2, 3, "minecraft:cyan_stained_glass")
    g.set(2, 1, 2, "minecraft:exposed_cut_copper")
    g.set(2, 1, 4, "minecraft:exposed_cut_copper")
    # Twin forward Pike prongs
    g.fill(7, 1, 2, 9, 1, 2, "minecraft:oxidized_cut_copper")
    g.fill(7, 1, 4, 9, 1, 4, "minecraft:oxidized_cut_copper")
    g.set(10, 1, 2, "minecraft:lightning_rod")
    g.set(10, 1, 4, "minecraft:lightning_rod")
    # Broken hull fin / scrap rib sticking up
    g.set(3, 1, 1, "minecraft:cut_copper")
    g.set(3, 2, 1, "minecraft:copper_grate")
    g.set(4, 3, 1, "minecraft:exposed_cut_copper")
    # House Dusk tattered marker post
    g.fill(1, 1, 5, 1, 4, 5, "minecraft:iron_bars")
    g.set(1, 5, 5, "minecraft:lightning_rod")
    g.set(2, 4, 5, "minecraft:purple_wool")
    g.set(2, 3, 5, "minecraft:purple_wool")
    g.set(6, 1, 5, "minecraft:barrel")
    return g


# ---------------------------------------------------------------------------
# 2. HIVE DECORATIONS (hive_crags)
# ---------------------------------------------------------------------------
def build_hive_ritual_altar() -> VoxelGrid:
    """9x9x9 jagged Hive summoning plinth with soul fire and bone teeth."""
    g = VoxelGrid(9, 9, 9)
    # Octagonal deepslate / blackstone base
    g.fill(2, 0, 1, 6, 0, 7, "minecraft:deepslate_bricks")
    g.fill(1, 0, 2, 7, 0, 6, "minecraft:deepslate_bricks")
    g.fill(2, 1, 2, 6, 1, 6, "minecraft:polished_blackstone_bricks")
    g.fill(3, 2, 3, 5, 2, 5, "minecraft:chiseled_polished_blackstone")
    # Central Hive soul brazier
    g.set(4, 2, 4, "minecraft:soul_soil")
    g.set(4, 3, 4, "minecraft:soul_fire")
    g.set(4, 1, 4, "minecraft:verdant_froglight")
    # Four curved bone-chitin teeth arching inward
    corners = [(1, 1, 1, 1), (7, 1, -1, 1), (1, 7, 1, -1), (7, 7, -1, -1)]
    for cx, cz, dx, dz in corners:
        g.set(cx, 1, cz, "minecraft:blackstone")
        g.set(cx, 2, cz, "minecraft:bone_block")
        g.set(cx, 3, cz, "minecraft:bone_block")
        g.set(cx + dx, 4, cz + dz, "minecraft:bone_block")
        g.set(cx + dx, 5, cz + dz, "minecraft:polished_blackstone_bricks")
        g.set(cx + 2 * dx, 6, cz + 2 * dz, "minecraft:bone_block")
    # Hovering green Hive crystal shard at top
    g.set(4, 7, 4, "minecraft:verdant_froglight")
    g.set(4, 8, 4, "minecraft:blackstone_wall")
    g.set(4, 6, 4, "minecraft:chain")
    return g


def build_hive_rib_arch() -> VoxelGrid:
    """11x10x7 fossilized Hive bone-and-blackstone ribcage archway."""
    g = VoxelGrid(11, 10, 7)
    # Rocky footing along both sides
    g.fill(0, 0, 1, 2, 0, 5, "minecraft:deepslate_tiles")
    g.fill(8, 0, 1, 10, 0, 5, "minecraft:deepslate_tiles")
    g.fill(3, 0, 2, 7, 0, 4, "minecraft:blackstone")
    # Two parallel arched chitin ribs at z=1 and z=5, plus central ridge at z=3
    for z in (1, 5):
        # Left leg
        g.fill(1, 1, z, 1, 3, z, "minecraft:polished_blackstone_bricks")
        g.set(1, 4, z, "minecraft:bone_block")
        g.set(2, 5, z, "minecraft:bone_block")
        g.set(2, 6, z, "minecraft:bone_block")
        g.set(3, 7, z, "minecraft:bone_block")
        g.set(4, 8, z, "minecraft:bone_block")
        # Right leg
        g.fill(9, 1, z, 9, 3, z, "minecraft:polished_blackstone_bricks")
        g.set(9, 4, z, "minecraft:bone_block")
        g.set(8, 5, z, "minecraft:bone_block")
        g.set(8, 6, z, "minecraft:bone_block")
        g.set(7, 7, z, "minecraft:bone_block")
        g.set(6, 8, z, "minecraft:bone_block")
        # Crown keystone
        g.set(5, 8, z, "minecraft:chiseled_polished_blackstone")
        g.set(5, 9, z, "minecraft:blackstone_wall")
    # Spine connecting both ribs across the top
    g.fill(5, 8, 2, 5, 8, 4, "minecraft:bone_block")
    g.set(5, 9, 3, "minecraft:verdant_froglight")
    # Hanging soul lanterns under the arch
    g.set(5, 7, 3, "minecraft:chain")
    g.set(5, 6, 3, "minecraft:soul_lantern")
    g.set(2, 1, 3, "minecraft:verdant_froglight")
    g.set(8, 1, 3, "minecraft:verdant_froglight")
    return g


# ---------------------------------------------------------------------------
# 3. VEX DECORATIONS (vex_expanse)
# ---------------------------------------------------------------------------
def build_vex_conflux_node() -> VoxelGrid:
    """7x11x7 stepped brass-and-basalt Conflux pedestal over glowing Fake Radiolaria."""
    g = VoxelGrid(7, 11, 7)
    # Base pool ringed by stone/deepslate
    g.fill(0, 0, 0, 6, 0, 6, "minecraft:deepslate_bricks")
    g.fill(1, 0, 1, 5, 0, 5, "destiny2mc:fake_radiolaria")
    # Central stepped Vex plinth
    g.fill(2, 0, 2, 4, 1, 4, "minecraft:smooth_stone")
    g.set(3, 2, 3, "minecraft:gold_block")
    g.set(3, 3, 3, "minecraft:chiseled_stone_bricks")
    # Floating luminous Conflux beam
    g.fill(3, 4, 3, 3, 9, 3, "minecraft:end_rod")
    g.set(3, 6, 3, "minecraft:white_stained_glass")
    g.set(3, 7, 3, "destiny2mc:fake_radiolaria")
    g.set(3, 10, 3, "minecraft:gold_block")
    # 4 geometric Vex floating corner brackets
    for x, z in [(1, 1), (5, 1), (1, 5), (5, 5)]:
        g.set(x, 1, z, "minecraft:cut_copper")
        g.set(x, 5, z, "minecraft:cut_copper")
        g.set(x, 6, z, "minecraft:gold_block")
        g.set(x, 7, z, "minecraft:oxidized_cut_copper")
    return g


def build_vex_radiolaria_fountain() -> VoxelGrid:
    """9x8x9 terraced Vex cube basin overflowing with luminous Fake Radiolaria."""
    g = VoxelGrid(9, 8, 9)
    # Outer basin rim
    g.fill(0, 0, 0, 8, 0, 8, "minecraft:deepslate_tiles")
    g.fill(1, 0, 1, 7, 0, 7, "destiny2mc:fake_radiolaria")
    g.fill(0, 1, 0, 8, 1, 8, "minecraft:stone_bricks")
    g.clear(1, 1, 1, 7, 1, 7)
    # Terraced inner bowl
    g.fill(2, 1, 2, 6, 2, 6, "minecraft:smooth_stone")
    g.fill(3, 2, 3, 5, 2, 5, "destiny2mc:fake_radiolaria")
    # Central spout pouring fake radiolaria on 4 sides
    g.fill(4, 1, 4, 4, 5, 4, "minecraft:gold_block")
    g.set(4, 6, 4, "destiny2mc:fake_radiolaria")
    for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
        g.fill(4 + dx, 2, 4 + dz, 4 + dx, 5, 4 + dz, "destiny2mc:fake_radiolaria")
    # 4 corner brass pylons
    for x, z in [(0, 0), (8, 0), (0, 8), (8, 8)]:
        g.fill(x, 2, z, x, 4, z, "minecraft:cut_copper")
        g.set(x, 5, z, "minecraft:gold_block")
        g.set(x, 6, z, "minecraft:end_rod")
    return g


def build_vex_mini_obelisk() -> VoxelGrid:
    """7x12x7 split floating Vex monolith with glowing radiolaria core."""
    g = VoxelGrid(7, 12, 7)
    # Stepped base
    g.fill(1, 0, 1, 5, 0, 5, "minecraft:deepslate_bricks")
    g.fill(2, 1, 2, 4, 1, 4, "minecraft:stone_bricks")
    # Lower shaft
    g.fill(2, 2, 2, 4, 4, 4, "minecraft:smooth_stone")
    g.set(3, 3, 2, "minecraft:gold_block")
    g.set(3, 3, 4, "minecraft:gold_block")
    g.set(2, 3, 3, "minecraft:gold_block")
    g.set(4, 3, 3, "minecraft:gold_block")
    # Suspended luminous radiolaria core gap (y=5..6)
    g.set(3, 5, 3, "destiny2mc:fake_radiolaria")
    g.set(3, 6, 3, "destiny2mc:fake_radiolaria")
    for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
        g.set(3 + dx, 5, 3 + dz, "minecraft:white_stained_glass")
    # Floating upper monolith block (y=7..11)
    g.fill(2, 7, 2, 4, 10, 4, "minecraft:deepslate_tiles")
    g.set(2, 8, 2, "minecraft:cut_copper")
    g.set(4, 8, 4, "minecraft:cut_copper")
    g.set(2, 9, 4, "minecraft:gold_block")
    g.set(4, 9, 2, "minecraft:gold_block")
    g.set(3, 11, 3, "minecraft:gold_block")
    return g


# ---------------------------------------------------------------------------
# 4. CABAL DECORATIONS (cabal_barrens)
# ---------------------------------------------------------------------------
def build_cabal_drop_pod() -> VoxelGrid:
    """9x9x9 crashed Red Legion Drop Pod embedded in a scorched impact crater."""
    g = VoxelGrid(9, 9, 9)
    # Scorched impact crater rim (y=0..1, structure starts at -1)
    g.fill(1, 0, 1, 7, 0, 7, "minecraft:blackstone")
    for x in range(9):
        for z in range(9):
            if (x in (0, 8) and 2 <= z <= 6) or (z in (0, 8) and 2 <= x <= 6):
                g.set(x, 1, z, "minecraft:red_terracotta")
    # Heavy octagonal Drop Pod hull (y=1..7)
    g.fill(2, 1, 2, 6, 6, 6, "minecraft:gray_concrete")
    # Chamfer vertical corners
    for y in range(1, 7):
        for cx, cz in [(2, 2), (6, 2), (2, 6), (6, 6)]:
            g.set(cx, y, cz, "minecraft:black_concrete")
    # Red Legion racing stripes and gold emblem band
    for y in (3, 4):
        g.set(4, y, 2, "minecraft:red_concrete")
        g.set(4, y, 6, "minecraft:red_concrete")
        g.set(2, y, 4, "minecraft:red_concrete")
        g.set(6, y, 4, "minecraft:red_concrete")
    g.fill(3, 5, 2, 5, 5, 6, "minecraft:red_concrete")
    g.fill(2, 5, 3, 6, 5, 5, "minecraft:red_concrete")
    g.set(4, 5, 2, "minecraft:yellow_concrete")
    g.set(4, 5, 6, "minecraft:yellow_concrete")
    # Hollow interior + open front blast hatch (+Z)
    g.clear(3, 2, 3, 5, 5, 5)
    g.clear(3, 2, 6, 5, 4, 6)
    g.fill(3, 1, 7, 5, 1, 8, "minecraft:iron_block")  # Dropped blast door ramp
    # Glowing interior thruster/console light
    g.set(4, 3, 3, "minecraft:shroomlight")
    # Tapered armored cap + thruster exhausts
    g.fill(3, 7, 3, 5, 7, 5, "minecraft:black_concrete")
    g.set(4, 8, 4, "minecraft:shroomlight")
    for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
        g.set(4 + dx, 7, 4 + dz, "minecraft:red_stained_glass")
    return g


def build_cabal_barricade() -> VoxelGrid:
    """11x6x7 Red Legion forward defensive checkpoint with blast shields."""
    g = VoxelGrid(11, 6, 7)
    # Armored ground pad
    g.fill(1, 0, 1, 9, 0, 5, "minecraft:deepslate_bricks")
    # Angled heavy brutalist front wall (z=1..2)
    g.fill(2, 1, 1, 8, 3, 1, "minecraft:gray_concrete")
    g.fill(1, 1, 2, 2, 3, 2, "minecraft:black_concrete")
    g.fill(8, 1, 2, 9, 3, 2, "minecraft:black_concrete")
    # Red Legion stripe & firing slit
    g.fill(3, 2, 1, 7, 2, 1, "minecraft:red_concrete")
    g.fill(4, 3, 1, 6, 3, 1, "minecraft:iron_bars")
    g.set(5, 1, 1, "minecraft:yellow_concrete")
    # Rear supply crates and tactical floodlight post
    g.set(3, 1, 4, "minecraft:barrel")
    g.set(4, 1, 4, "minecraft:blast_furnace")
    g.set(7, 1, 4, "minecraft:iron_block")
    g.fill(9, 1, 4, 9, 4, 4, "minecraft:black_concrete")
    g.set(9, 5, 4, "minecraft:shroomlight")
    g.set(8, 5, 4, "minecraft:red_stained_glass")
    return g


def build_cabal_comm_beacon() -> VoxelGrid:
    """7x13x7 heavy Red Legion orbital relay tower."""
    g = VoxelGrid(7, 13, 7)
    # Heavy splayed base
    g.fill(1, 0, 1, 5, 0, 5, "minecraft:black_concrete")
    g.fill(2, 1, 2, 4, 3, 4, "minecraft:gray_concrete")
    for x, z in [(1, 1), (5, 1), (1, 5), (5, 5)]:
        g.set(x, 1, z, "minecraft:red_nether_bricks")
    # Red-and-gold armor collar
    g.fill(2, 4, 2, 4, 4, 4, "minecraft:red_concrete")
    g.set(3, 4, 2, "minecraft:yellow_concrete")
    g.set(3, 4, 4, "minecraft:yellow_concrete")
    # Narrow brutalist mast
    g.fill(3, 5, 3, 3, 11, 3, "minecraft:black_concrete")
    for y in (6, 8, 10):
        g.set(2, y, 3, "minecraft:iron_bars")
        g.set(4, y, 3, "minecraft:iron_bars")
        g.set(3, y, 2, "minecraft:iron_bars")
        g.set(3, y, 4, "minecraft:iron_bars")
    # Glowing Red Legion beacon crown
    g.set(3, 11, 3, "minecraft:shroomlight")
    for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
        g.set(3 + dx, 11, 3 + dz, "minecraft:red_stained_glass")
    g.set(3, 12, 3, "minecraft:lightning_rod")
    return g


DECOR_SPECS = [
    # (id, builder, biome_tag, start_y, set_name, weight)
    ("fallen_ether_cache", build_fallen_ether_cache, "#destiny2mc:has_fallen_base", 0, "fallen_decor", 3),
    ("fallen_pike_wreck", build_fallen_pike_wreck, "#destiny2mc:has_fallen_base", 0, "fallen_decor", 2),
    ("hive_ritual_altar", build_hive_ritual_altar, "#destiny2mc:has_hive_citadel", 0, "hive_decor", 3),
    ("hive_rib_arch", build_hive_rib_arch, "#destiny2mc:has_hive_citadel", 0, "hive_decor", 2),
    ("vex_conflux_node", build_vex_conflux_node, "#destiny2mc:has_vex_structures", 0, "vex_decor", 3),
    ("vex_radiolaria_fountain", build_vex_radiolaria_fountain, "#destiny2mc:has_vex_structures", 0, "vex_decor", 2),
    ("vex_mini_obelisk", build_vex_mini_obelisk, "#destiny2mc:has_vex_structures", 0, "vex_decor", 3),
    ("cabal_drop_pod", build_cabal_drop_pod, "#destiny2mc:has_cabal_base", -1, "cabal_decor", 3),
    ("cabal_barricade", build_cabal_barricade, "#destiny2mc:has_cabal_base", 0, "cabal_decor", 2),
    ("cabal_comm_beacon", build_cabal_comm_beacon, "#destiny2mc:has_cabal_base", 0, "cabal_decor", 2),
]

SET_CONFIGS = {
    "fallen_decor": {"spacing": 10, "separation": 4, "salt": 714290811},
    "hive_decor": {"spacing": 10, "separation": 4, "salt": 714290822},
    "vex_decor": {"spacing": 9, "separation": 4, "salt": 714290833},
    "cabal_decor": {"spacing": 10, "separation": 4, "salt": 714290844},
}


def main():
    sets_map: dict[str, list[dict]] = {k: [] for k in SET_CONFIGS}

    for sid, fn, biome_tag, start_y, set_name, weight in DECOR_SPECS:
        grid = fn()
        nbt_path = NBT_DIR / f"{sid}.nbt"
        n_blocks, n_pal = grid.write_nbt(nbt_path)
        print(f"Built {sid}: {grid.sx}x{grid.sy}x{grid.sz}, {n_blocks} blocks, {n_pal} palette")

        # Template pool
        pool = {
            "name": f"destiny2mc:factions/decor/{sid}",
            "fallback": "minecraft:empty",
            "elements": [
                {
                    "weight": 1,
                    "element": {
                        "element_type": "minecraft:single_pool_element",
                        "location": f"destiny2mc:factions/decor/{sid}",
                        "processors": {"processors": []},
                        "projection": "rigid",
                    },
                }
            ],
        }
        (POOL_DIR / f"{sid}.json").write_text(json.dumps(pool, indent=2) + "\n", encoding="utf-8")

        # Structure JSON
        struct_json = {
            "type": "minecraft:jigsaw",
            "biomes": biome_tag,
            "step": "surface_structures",
            "terrain_adaptation": "beard_thin",
            "start_pool": f"destiny2mc:factions/decor/{sid}",
            "size": 1,
            "start_height": {"absolute": start_y},
            "project_start_to_heightmap": "WORLD_SURFACE_WG",
            "max_distance_from_center": 24,
            "use_expansion_hack": False,
            "spawn_overrides": {},
        }
        (STRUCT_DIR / f"{sid}.json").write_text(json.dumps(struct_json, indent=2) + "\n", encoding="utf-8")

        sets_map[set_name].append({"structure": f"destiny2mc:{sid}", "weight": weight})

    # Write 4 structure sets
    for set_name, entries in sets_map.items():
        cfg = SET_CONFIGS[set_name]
        ss = {
            "structures": entries,
            "placement": {
                "type": "minecraft:random_spread",
                "spacing": cfg["spacing"],
                "separation": cfg["separation"],
                "salt": cfg["salt"],
            },
        }
        (SET_DIR / f"{set_name}.json").write_text(json.dumps(ss, indent=2) + "\n", encoding="utf-8")
        print(f"Wrote structure_set {set_name}.json ({len(entries)} structures)")


if __name__ == "__main__":
    main()
