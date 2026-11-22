#!/usr/bin/env python3
"""
Generate 32x32 RGBA pixel-art Warframe elemental mod card icons matching the
chamfered octagonal badge style of hellfire.png and chilling_grasp.png, plus
the 4 tiered 16x16 Corpus/Orokin Proto-Shield item icons and lang entries.
"""
import json
from pathlib import Path
from PIL import Image

REPO = Path(__file__).resolve().parents[2]
OUT_DIR = REPO / "src/main/resources/assets/destiny2mc/textures/gui/warframe/mods"
ITEM_DIR = REPO / "src/main/resources/assets/destiny2mc/textures/item"
LANG_PATH = REPO / "src/main/resources/assets/destiny2mc/lang/en_us.json"

OUTLINE = (19, 32, 39, 255)      # #132027
SHADOW  = (19, 32, 39, 255)      # inner drop shadow

THEMES = {
    "cold": ((34, 52, 68, 255), (56, 86, 108, 255), (140, 222, 240, 255)),
    "electricity": ((42, 40, 66, 255), (68, 66, 108, 255), (182, 168, 248, 255)),
    "toxin": ((30, 56, 44, 255), (52, 92, 68, 255), (148, 224, 132, 255)),
    "heat": ((34, 62, 64, 255), (66, 103, 96, 255), (150, 191, 142, 255)),
}

PALETTES = {
    "cold":        ((106, 206, 232, 255), (172, 242, 252, 255), (236, 248, 250, 255)),
    "electricity": ((142, 126, 240, 255), (206, 196, 255, 255), (244, 244, 255, 255)),
    "toxin":       ((92, 204, 96, 255),   (164, 242, 134, 255), (232, 252, 212, 255)),
    "heat":        ((200, 83, 67, 255),   (239, 150, 119, 255), (228, 232, 216, 255)),
}

GLYPHS = {
    "cryo_rounds": [
        ".....++.....", "....+**+....", "....+**+....", "..+.+##+.+..",
        ".+++####+++.", "...##**##...", "....#**#....", ".+++####+++.",
        "..+.+##+.+..", "....+**+....", "....+##+....", ".....##.....",
        "....#++#....", "...#++++#...",
    ],
    "deep_freeze": [
        ".....++.....", "....+**+....", "...+#**#+...", "..+######+..",
        ".+##****##+.", "..########..", "...#****#...", "..+######+..",
        ".+###++###+.", "..+#++++#+..", "...+####+...", "....+##+....",
        ".....++.....", "............",
    ],
    "north_wind": [
        ".........++.", ".......++*+.", "......+##++.", ".....+##+...",
        "....+##+....", "...+##+..+..", "..+#*#..+++.", ".+#*#..+##+.",
        ".+##..+#*#..", "..#..+#*#...", "....+##+....", "...+##+.....",
        "..+++.......", ".++.........",
    ],
    "stormbringer": [
        "......+++...", ".....+**+...", "....+#*+....", "...+#*+.....",
        "..+#*++++...", ".+#*****+...", "..++++*#+...", ".....+#+....",
        "....+#+.....", "...+#+......", "..+#+.......", "..++........",
        "............", "............",
    ],
    "charged_shell": [
        "....++++....", "...+****+...", "...+####+...", "..++#**#++..",
        ".+**####**+.", "..++#**#++..", "....+##+....", "...+#**#+...",
        "..+#*++*#+..", "...+#**#+...", "....+##+....", "...++..++...",
        "..+......+..", "............",
    ],
    "convulsion": [
        ".......++...", "......+*+...", ".....+#+....", "..+++#+.....",
        ".+*###+++...", "..++#***+...", "....+#*#....", "...+#*#++...",
        "..+*#+++*+..", "...+#+..++..", "..+#+.......", "..++........",
        "............", "............",
    ],
    "shocking_touch": [
        "........++..", ".......+*+..", "......+#+...", ".....+#+....",
        "....+#*+....", "...+#*#.....", "..+#*#..++..", ".+#*#..+*+..",
        "..##..+#+...", ".....+#+....", "..+++*#.....", ".+**##......",
        "..++........", "............",
    ],
    "infected_clip": [
        ".....++.....", "....+**+....", "....+##+....", "...+#**#+...",
        "..+#****#+..", "..+######+..", ".+#**##**#+.", ".+###**###+.",
        ".+########+.", "..+#****#+..", "...+####+...", "....+##+....",
        ".....++.....", "............",
    ],
    "contagious_spread": [
        "....++++....", "..++****++..", ".+*######*+.", ".+#**##**#+.",
        ".+###**###+.", "..+######+..", ".++..##..++.", "+*+..##..+*+",
        ".++.#**#.++.", "....#**#....", "...+####+...", "....+##+....",
        "............", "............",
    ],
    "pathogen_rounds": [
        ".....++.....", "....+**+....", "...+#**#+...", "...+####+...",
        "..+#****#+..", "..+#*##*#+..", ".+##****##+.", ".+########+.",
        "..+#*##*#+..", "...+####+...", "..+..++..+..", ".++......++.",
        "............", "............",
    ],
    "fever_strike": [
        "........+++.", ".......+**+.", "......+#*+..", ".....+#*+...",
        "....+#*+....", "...+#*+.....", "..+#*+..++..", ".+#*+..+*+..",
        ".+##..+##...", "..+..+#+....", "....+#+.....", "...+*+......",
        "...++.......", "............",
    ],
    "incendiary_coat": [
        "......++....", ".....+##+...", "....+#**+...", "..+.+#**+...",
        ".+++#****+..", ".+###****#+.", ".+###**###+.", "..+##**##+..",
        "..+######+..", "...+####+...", "....+##+....", ".....++.....",
        "............", "............",
    ],
}

MOD_ELEMENTS = {
    "cryo_rounds": "cold", "deep_freeze": "cold", "north_wind": "cold",
    "stormbringer": "electricity", "charged_shell": "electricity",
    "convulsion": "electricity", "shocking_touch": "electricity",
    "infected_clip": "toxin", "contagious_spread": "toxin",
    "pathogen_rounds": "toxin", "fever_strike": "toxin",
    "incendiary_coat": "heat",
}

SHIELD_TIERS = {
    "proto_shield_mk1": ((72, 92, 108, 255), (124, 152, 172, 255), (72, 224, 240, 255), (212, 252, 255, 255)),
    "proto_shield_mk2": ((56, 78, 124, 255), (104, 138, 198, 255), (96, 178, 255, 255), (220, 244, 255, 255)),
    "proto_shield_mk3": ((72, 58, 114, 255), (132, 108, 196, 255), (178, 132, 255, 255), (242, 232, 255, 255)),
    "proto_shield_prime": ((184, 146, 62, 255), (236, 206, 118, 255), (242, 246, 252, 255), (120, 242, 255, 255)),
}

SHIELD_SPRITE = [
    ".....OOOOOO.....",
    "...OOMMMMMMOO...",
    "..OMMMHHHHMMMO..",
    ".OMMHCCCCCCCHMO.",
    ".OMHCCCGGGCCCDO.",
    ".OMHCCGGGGGCCHO.",
    ".OMHCCGGGGGCHMO.",
    ".OMMHCCGGGCHMMO.",
    "..OMMHCCCCCHMO..",
    "..OMMMHCCCCHMO..",
    "...OMMMHCHMMO...",
    "....OMMMHMMO....",
    ".....OMMMMO.....",
    "......OMMO......",
    ".......OO.......",
    "................",
]

PROTO_SHIELD_LANG = {
    "item.destiny2mc.proto_shield_mk1": "MK-I Corpus Proto-Shield",
    "item.destiny2mc.proto_shield_mk2": "MK-II Corpus Proto-Shield",
    "item.destiny2mc.proto_shield_mk3": "MK-III Corpus Proto-Shield",
    "item.destiny2mc.proto_shield_prime": "Prime Proto-Shield Generator",
}


def inside_badge(x: int, y: int) -> bool:
    if x < 2 or x > 29 or y < 1 or y > 30:
        return False
    if x + y < 7 or (31 - x) + y < 9 or x + (31 - y) < 8 or (31 - x) + (31 - y) < 8:
        return False
    return True


def bake_icon(mod_id: str, element: str) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    base, sheen, glint = THEMES[element]
    c_main, c_bright, c_core = PALETTES[element]
    for y in range(32):
        for x in range(32):
            if not inside_badge(x, y):
                continue
            border = any(not inside_badge(x + dx, y + dy) for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)))
            px[x, y] = OUTLINE if border else (sheen if 26 <= x + y <= 34 else base)
    for (hx, hy) in ((6, 2), (7, 2), (8, 2), (9, 2), (5, 3), (4, 4), (3, 5), (3, 6), (3, 7)):
        if inside_badge(hx, hy) and px[hx, hy] != OUTLINE:
            px[hx, hy] = glint
    rows = GLYPHS[mod_id]
    ox, oy = 10, 9
    glyph_coords = {}
    for gy, row in enumerate(rows):
        for gx, ch in enumerate(row):
            if ch != ".":
                glyph_coords[(ox + gx, oy + gy)] = c_main if ch == "#" else c_bright if ch == "+" else c_core
    for (gx, gy) in glyph_coords:
        sx, sy = gx + 2, gy + 2
        if 0 <= sx < 32 and 0 <= sy < 32 and inside_badge(sx, sy):
            px[sx, sy] = SHADOW
    for (gx, gy) in glyph_coords:
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = gx + dx, gy + dy
            if (nx, ny) not in glyph_coords and 0 <= nx < 32 and 0 <= ny < 32 and inside_badge(nx, ny):
                px[nx, ny] = OUTLINE
    for (gx, gy), color in glyph_coords.items():
        if 0 <= gx < 32 and 0 <= gy < 32:
            px[gx, gy] = color
    return img


def bake_shield_item(tier_id: str) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    metal, hi, core, glow = SHIELD_TIERS[tier_id]
    dark = (max(0, metal[0] - 28), max(0, metal[1] - 28), max(0, metal[2] - 28), 255)
    cmap = {"O": OUTLINE, "M": metal, "H": hi, "D": dark, "C": core, "G": glow}
    for y, row in enumerate(SHIELD_SPRITE):
        for x, ch in enumerate(row):
            if ch in cmap:
                px[x, y] = cmap[ch]
    return img


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for mod_id, element in MOD_ELEMENTS.items():
        bake_icon(mod_id, element).save(OUT_DIR / f"{mod_id}.png")
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    for tier_id in SHIELD_TIERS:
        bake_shield_item(tier_id).save(ITEM_DIR / f"{tier_id}.png")
    if LANG_PATH.exists():
        lang = json.loads(LANG_PATH.read_text(encoding="utf-8"))
        lang.update(PROTO_SHIELD_LANG)
        LANG_PATH.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Baked {len(MOD_ELEMENTS)} mod icons and {len(SHIELD_TIERS)} Proto-Shield item textures")


if __name__ == "__main__":
    main()
