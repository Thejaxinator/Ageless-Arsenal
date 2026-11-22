#!/usr/bin/env python3
"""
Generate 32x32 RGBA pixel-art Warframe elemental mod card icons matching the
chamfered octagonal badge style of hellfire.png and chilling_grasp.png.
"""
from pathlib import Path
from PIL import Image

OUT_DIR = Path(__file__).resolve().parents[2] / "src/main/resources/assets/destiny2mc/textures/gui/warframe/mods"

OUTLINE = (19, 32, 39, 255)      # #132027
SHADOW  = (19, 32, 39, 255)      # inner drop shadow

# Badge base themes (base, sheen, corner_highlight) matching existing mod plates
THEMES = {
    "cold": (
        (34, 52, 68, 255),       # deep slate blue
        (56, 86, 108, 255),      # diagonal sheen
        (140, 222, 240, 255),    # ice corner glint
    ),
    "electricity": (
        (42, 40, 66, 255),       # deep indigo
        (68, 66, 108, 255),      # diagonal sheen
        (182, 168, 248, 255),    # arc violet glint
    ),
    "toxin": (
        (30, 56, 44, 255),       # deep forest green
        (52, 92, 68, 255),       # diagonal sheen
        (148, 224, 132, 255),    # venom green glint
    ),
    "heat": (
        (34, 62, 64, 255),       # matches hellfire.png
        (66, 103, 96, 255),      # diagonal sheen
        (150, 191, 142, 255),    # corner highlight
    ),
}

# Foreground glyph palettes (main, bright, core)
PALETTES = {
    "cold":        ((106, 206, 232, 255), (172, 242, 252, 255), (236, 248, 250, 255)),
    "electricity": ((142, 126, 240, 255), (206, 196, 255, 255), (244, 244, 255, 255)),
    "toxin":       ((92, 204, 96, 255),   (164, 242, 134, 255), (232, 252, 212, 255)),
    "heat":        ((200, 83, 67, 255),   (239, 150, 119, 255), (228, 232, 216, 255)),
}

# 12x14 centered icon templates ('#'=main, '+'=bright, '*'=core, '.'=transparent)
GLYPHS = {
    # Cold variants
    "cryo_rounds": [
        ".....++.....",
        "....+**+....",
        "....+**+....",
        "..+.+##+.+..",
        ".+++####+++.",
        "...##**##...",
        "....#**#....",
        ".+++####+++.",
        "..+.+##+.+..",
        "....+**+....",
        "....+##+....",
        ".....##.....",
        "....#++#....",
        "...#++++#...",
    ],
    "deep_freeze": [
        ".....++.....",
        "....+**+....",
        "...+#**#+...",
        "..+######+..",
        ".+##****##+.",
        "..########..",
        "...#****#...",
        "..+######+..",
        ".+###++###+.",
        "..+#++++#+..",
        "...+####+...",
        "....+##+....",
        ".....++.....",
        "............",
    ],
    "north_wind": [
        ".........++.",
        ".......++*+.",
        "......+##++.",
        ".....+##+...",
        "....+##+....",
        "...+##+..+..",
        "..+#*#..+++.",
        ".+#*#..+##+.",
        ".+##..+#*#..",
        "..#..+#*#...",
        "....+##+....",
        "...+##+.....",
        "..+++.......",
        ".++.........",
    ],
    # Electricity variants
    "stormbringer": [
        "......+++...",
        ".....+**+...",
        "....+#*+....",
        "...+#*+.....",
        "..+#*++++...",
        ".+#*****+...",
        "..++++*#+...",
        ".....+#+....",
        "....+#+.....",
        "...+#+......",
        "..+#+.......",
        "..++........",
        "............",
        "............",
    ],
    "charged_shell": [
        "....++++....",
        "...+****+...",
        "...+####+...",
        "..++#**#++..",
        ".+**####**+.",
        "..++#**#++..",
        "....+##+....",
        "...+#**#+...",
        "..+#*++*#+..",
        "...+#**#+...",
        "....+##+....",
        "...++..++...",
        "..+......+..",
        "............",
    ],
    "convulsion": [
        ".......++...",
        "......+*+...",
        ".....+#+....",
        "..+++#+.....",
        ".+*###+++...",
        "..++#***+...",
        "....+#*#....",
        "...+#*#++...",
        "..+*#+++*+..",
        "...+#+..++..",
        "..+#+.......",
        "..++........",
        "............",
        "............",
    ],
    "shocking_touch": [
        "........++..",
        ".......+*+..",
        "......+#+...",
        ".....+#+....",
        "....+#*+....",
        "...+#*#.....",
        "..+#*#..++..",
        ".+#*#..+*+..",
        "..##..+#+...",
        ".....+#+....",
        "..+++*#.....",
        ".+**##......",
        "..++........",
        "............",
    ],
    # Toxin variants
    "infected_clip": [
        ".....++.....",
        "....+**+....",
        "....+##+....",
        "...+#**#+...",
        "..+#****#+..",
        "..+######+..",
        ".+#**##**#+.",
        ".+###**###+.",
        ".+########+.",
        "..+#****#+..",
        "...+####+...",
        "....+##+....",
        ".....++.....",
        "............",
    ],
    "contagious_spread": [
        "....++++....",
        "..++****++..",
        ".+*######*+.",
        ".+#**##**#+.",
        ".+###**###+.",
        "..+######+..",
        ".++..##..++.",
        "+*+..##..+*+",
        ".++.#**#.++.",
        "....#**#....",
        "...+####+...",
        "....+##+....",
        "............",
        "............",
    ],
    "pathogen_rounds": [
        ".....++.....",
        "....+**+....",
        "...+#**#+...",
        "...+####+...",
        "..+#****#+..",
        "..+#*##*#+..",
        ".+##****##+.",
        ".+########+.",
        "..+#*##*#+..",
        "...+####+...",
        "..+..++..+..",
        ".++......++.",
        "............",
        "............",
    ],
    "fever_strike": [
        "........+++.",
        ".......+**+.",
        "......+#*+..",
        ".....+#*+...",
        "....+#*+....",
        "...+#*+.....",
        "..+#*+..++..",
        ".+#*+..+*+..",
        ".+##..+##...",
        "..+..+#+....",
        "....+#+.....",
        "...+*+......",
        "...++.......",
        "............",
    ],
    # Heat variant (Shotgun: Incendiary Coat)
    "incendiary_coat": [
        "......++....",
        ".....+##+...",
        "....+#**+...",
        "..+.+#**+...",
        ".+++#****+..",
        ".+###****#+.",
        ".+###**###+.",
        "..+##**##+..",
        "..+######+..",
        "...+####+...",
        "....+##+....",
        ".....++.....",
        "............",
        "............",
    ],
}

MOD_ELEMENTS = {
    "cryo_rounds": "cold",
    "deep_freeze": "cold",
    "north_wind": "cold",
    "stormbringer": "electricity",
    "charged_shell": "electricity",
    "convulsion": "electricity",
    "shocking_touch": "electricity",
    "infected_clip": "toxin",
    "contagious_spread": "toxin",
    "pathogen_rounds": "toxin",
    "fever_strike": "toxin",
    "incendiary_coat": "heat",
}


def inside_badge(x: int, y: int) -> bool:
    if x < 2 or x > 29 or y < 1 or y > 30:
        return False
    # Chamfered octagonal silhouette matching hellfire.png / chilling_grasp.png
    if x + y < 7:
        return False
    if (31 - x) + y < 9:
        return False
    if x + (31 - y) < 8:
        return False
    if (31 - x) + (31 - y) < 8:
        return False
    return True


def bake_icon(mod_id: str, element: str) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    base, sheen, glint = THEMES[element]
    c_main, c_bright, c_core = PALETTES[element]

    # 1. Fill badge plate + diagonal sheen + outline
    for y in range(32):
        for x in range(32):
            if not inside_badge(x, y):
                continue
            border = any(not inside_badge(x + dx, y + dy) for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)))
            if border:
                px[x, y] = OUTLINE
            else:
                # Diagonal sheen band running from bottom-left to top-right
                diag = x + y
                px[x, y] = sheen if 26 <= diag <= 34 else base

    # 2. Top-left chamfer specular highlight (matches hellfire.png corner accent)
    for (hx, hy) in ((6, 2), (7, 2), (8, 2), (9, 2), (5, 3), (4, 4), (3, 5), (3, 6), (3, 7)):
        if inside_badge(hx, hy) and px[hx, hy] != OUTLINE:
            px[hx, hy] = glint

    # 3. Stamp glyph with 1px dark outline and drop shadow
    rows = GLYPHS[mod_id]
    ox, oy = 10, 9
    glyph_coords = {}
    for gy, row in enumerate(rows):
        for gx, ch in enumerate(row):
            if ch == ".":
                continue
            color = c_main if ch == "#" else c_bright if ch == "+" else c_core
            glyph_coords[(ox + gx, oy + gy)] = color

    # Drop shadow (offset +2, +2)
    for (gx, gy) in glyph_coords:
        sx, sy = gx + 2, gy + 2
        if 0 <= sx < 32 and 0 <= sy < 32 and inside_badge(sx, sy):
            px[sx, sy] = SHADOW

    # 1px outline around glyph
    for (gx, gy) in glyph_coords:
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = gx + dx, gy + dy
            if (nx, ny) not in glyph_coords and 0 <= nx < 32 and 0 <= ny < 32 and inside_badge(nx, ny):
                px[nx, ny] = OUTLINE

    # Foreground glyph pixels
    for (gx, gy), color in glyph_coords.items():
        if 0 <= gx < 32 and 0 <= gy < 32:
            px[gx, gy] = color

    return img


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for mod_id, element in MOD_ELEMENTS.items():
        out = OUT_DIR / f"{mod_id}.png"
        bake_icon(mod_id, element).save(out)
        print(f"Baked {out.name} ({element})")


if __name__ == "__main__":
    main()
