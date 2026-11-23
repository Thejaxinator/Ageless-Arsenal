#!/usr/bin/env python3
"""
Fetch official Warframe Wiki ability and passive icons and bake them into
Minecraft 1.21.1 GUI sprite PNGs for the Energy Binder (`AbilityGlyph`):

  src/main/resources/assets/destiny2mc/textures/gui/sprites/binder/ability/<id>.png

Sources are queried in priority order:
  1. Official Warframe Wiki (https://wiki.warframe.com/api.php)
  2. English Warframe Fandom Wiki (https://warframe.fandom.com/api.php)
  3. Russian Warframe Fandom Wiki (https://warframe.fandom.com/ru/api.php)
  4. Verified direct CDN URLs (static.wikia.nocookie.net)

All output sprites are normalized to pure white (255, 255, 255) with clean
alpha channels so `AbilityGlyph.draw(g, id, x, y, size, color)` tints them
accurately with each Warframe's accent color (or FROST_INK when selected).
"""
from __future__ import annotations

import io
import json
import sys
import urllib.parse
import urllib.request
from pathlib import Path
from PIL import Image, ImageOps

REPO_ROOT = Path(__file__).resolve().parents[1]
SPRITE_DIR = REPO_ROOT / "src/main/resources/assets/destiny2mc/textures/gui/sprites/binder/ability"
ABILITY_GLYPH_JAVA = REPO_ROOT / "src/main/java/com/jax/destiny2mc/binder/client/AbilityGlyph.java"

USER_AGENT = "AgelessArsenalsIconBaker/1.0 (https://github.com/Thejaxinator/Ageless-Arsenal)"

MEDIAWIKI_APIS = {
    "official": "https://wiki.warframe.com/api.php",
    "fandom_en": "https://warframe.fandom.com/api.php",
    "fandom_ru": "https://warframe.fandom.com/ru/api.php",
}

# Exact 22 Warframe Binder IDs from WarframeBinderCatalog.java & WarframeAbilityId.java.
# Each entry lists:
#   - wiki_files: ordered list of (wiki_key, File: title) candidates
#   - direct_urls: verified direct fallback URLs
#   - kind: "white_icon" (already white-on-alpha/dark) or "portrait" (Warframe icon -> silhouette/luma mask)
ICON_SPECS: dict[str, dict] = {
    # --- 11 Active Abilities (WarframeAbilityId) ---
    "excalibur_slash_dash": {
        "label": "Excalibur — Slash Dash",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:SlashDash130xWhite.png"),
            ("fandom_en", "File:SlashDash130xWhite.png"),
        ],
        "direct_urls": [],
    },
    "uriel_infernalis": {
        "label": "Uriel — Infernalis",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:Infernalis130xWhite.png"),
            ("official", "File:InfernalisIcon.png"),
            ("fandom_ru", "File:Инферналис_иконка_вики.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/3/3b/%D0%98%D0%BD%D1%84%D0%B5%D1%80%D0%BD%D0%B0%D0%BB%D0%B8%D1%81_%D0%B8%D0%BA%D0%BE%D0%BD%D0%BA%D0%B0_%D0%B2%D0%B8%D0%BA%D0%B8.png/revision/latest?cb=20251212063120&path-prefix=ru",
        ],
    },
    "uriel_brimstone": {
        "label": "Uriel — Brimstone",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:Brimstone130xWhite.png"),
            ("official", "File:BrimstoneIcon.png"),
            ("fandom_ru", "File:Адское_Пламя_иконка_вики.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/e/e9/%D0%90%D0%B4%D1%81%D0%BA%D0%BE%D0%B5_%D0%9F%D0%BB%D0%B0%D0%BC%D1%8F_%D0%B8%D0%BA%D0%BE%D0%BD%D0%BA%D0%B0_%D0%B2%D0%B8%D0%BA%D0%B8.png/revision/latest?cb=20251212063249&path-prefix=ru",
        ],
    },
    "mag_pull": {
        "label": "Mag — Pull",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:Pull130xWhite.png"),
            ("fandom_en", "File:Pull130xWhite.png"),
        ],
        "direct_urls": [],
    },
    "volt_speed": {
        "label": "Volt — Speed",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:Speed130xWhite.png"),
            ("fandom_en", "File:Speed130xWhite.png"),
        ],
        "direct_urls": [],
    },
    "nyx_chaos": {
        "label": "Nyx — Chaos",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:Chaos130xWhite.png"),
            ("fandom_en", "File:Chaos130xWhite.png"),
        ],
        "direct_urls": [],
    },
    "kullervo_wrathful_advance": {
        "label": "Kullervo — Wrathful Advance",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:WrathfulAdvance130xWhite.png"),
            ("official", "File:WrathfulAdvanceIcon.png"),
            ("fandom_en", "File:WrathfulAdvance130xWhite.png"),
            ("fandom_en", "File:WrathfulAdvanceIcon.png"),
        ],
        "direct_urls": [],
    },
    "voruna_shroud_of_dynar": {
        "label": "Voruna — Shroud of Dynar",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:ShroudOfDynar130xWhite.png"),
            ("official", "File:ShroudofDynar130xWhite.png"),
            ("official", "File:ShroudofDynarIcon.png"),
            ("fandom_en", "File:ShroudOfDynar130xWhite.png"),
            ("fandom_en", "File:ShroudofDynarIcon.png"),
        ],
        "direct_urls": [],
    },
    "valkyr_warcry": {
        "label": "Valkyr — Warcry",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:Warcry130xWhite.png"),
            ("fandom_en", "File:Warcry130xWhite.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/9/9a/Warcry.png/revision/latest?cb=20161202103519&path-prefix=pt",
        ],
    },
    "frost_snow_globe": {
        "label": "Frost — Snow Globe",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:SnowGlobe130xWhite.png"),
            ("fandom_en", "File:SnowGlobe130xWhite.png"),
        ],
        "direct_urls": [],
    },
    # Note: Follie's official abilities (Update 42) are Forced Perspective, Shadowgraph,
    # Self Portrait, and Plein Air. Self Portrait (ink effigy/mirage) matches Inkwell Mirage best.
    "follie_inkwell_mirage": {
        "label": "Follie — Inkwell Mirage (Self Portrait / Forced Perspective)",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:SelfPortrait130xWhite.png"),
            ("official", "File:ForcedPerspective130xWhite.png"),
            ("fandom_ru", "File:Автопортрет_иконка_вики.png"),
            ("fandom_ru", "File:Принудительная_Перспектива_иконка_вики.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/8/84/%D0%90%D0%B2%D1%82%D0%BE%D0%BF%D0%BE%D1%80%D1%82%D1%80%D0%B5%D1%82_%D0%B8%D0%BA%D0%BE%D0%BD%D0%BA%D0%B0_%D0%B2%D0%B8%D0%BA%D0%B8.png/revision/latest?cb=20260326015502&path-prefix=ru",
            "https://static.wikia.nocookie.net/warframe/images/6/60/%D0%9F%D1%80%D0%B8%D0%BD%D1%83%D0%B4%D0%B8%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F_%D0%9F%D0%B5%D1%80%D1%81%D0%BF%D0%B5%D0%BA%D1%82%D0%B8%D0%B2%D0%B0_%D0%B8%D0%BA%D0%BE%D0%BD%D0%BA%D0%B0_%D0%B2%D0%B8%D0%BA%D0%B8.png/revision/latest?cb=20260326015344&path-prefix=ru",
        ],
    },

    # --- 11 Passives & Parkour (WarframeBinderCatalog) ---
    "wf_tenno_parkour": {
        "label": "Tenno Parkour (Lotus / Obstacle Course)",
        "kind": "white_icon",
        "wiki_files": [
            ("official", "File:IconClanObstacleCourse.png"),
            ("official", "File:LotusLogoWhite.png"),
            ("fandom_en", "File:IconClanObstacleCourse.png"),
            ("fandom_en", "File:LotusLogoWhite.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/4/47/IconClanObstacleCourse.png/revision/latest/thumbnail/width/360/height/450?cb=20210917232511",
            "https://static.wikia.nocookie.net/warframe/images/9/9c/LotusLogoWhite.png/revision/latest/thumbnail/width/360/height/360?cb=20181121062152",
        ],
    },
    "wf_passive_excalibur": {
        "label": "Excalibur Passive (Swordsmanship)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:ExcaliburIcon272.png"),
            ("fandom_en", "File:ExcaliburIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/4/41/ExcaliburFull.png/revision/latest?cb=20141124023040",
        ],
    },
    "wf_passive_uriel": {
        "label": "Uriel Passive (Demon Legion / Hellfire Body)",
        "kind": "white_icon",
        "wiki_files": [
            ("fandom_ru", "File:Демоны_Уриэля_вики.png"),
            ("official", "File:UrielIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/6/6f/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%8B_%D0%A3%D1%80%D0%B8%D1%8D%D0%BB%D1%8F_%D0%B2%D0%B8%D0%BA%D0%B8.png/revision/latest?cb=20260328122220&path-prefix=ru",
        ],
    },
    "wf_passive_mag": {
        "label": "Mag Passive (Magnetic Vacuum)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:MagIcon272.png"),
            ("fandom_en", "File:MagIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/6/6c/MagFull.png/revision/latest?cb=20141124023147",
        ],
    },
    "wf_passive_volt": {
        "label": "Volt Passive (Static Discharge)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:VoltIcon272.png"),
            ("fandom_en", "File:VoltIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/7/7a/VoltFull.png/revision/latest?cb=20141124023359",
        ],
    },
    "wf_passive_nyx": {
        "label": "Nyx Passive (Psychic Evasion)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:NyxIcon272.png"),
            ("fandom_en", "File:NyxIcon272.png"),
        ],
        "direct_urls": [],
    },
    "wf_passive_kullervo": {
        "label": "Kullervo Passive (Heavy Blade)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:KullervoIcon272.png"),
            ("fandom_en", "File:KullervoIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/d/d1/Kullervo.png/revision/latest/scale-to-width/360?cb=20230621192807",
        ],
    },
    "wf_passive_voruna": {
        "label": "Voruna Passive (Pack Stride)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:VorunaIcon272.png"),
            ("fandom_en", "File:VorunaIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/b/b1/VorunaFull.png/revision/latest/smart/width/250/height/250?cb=20221118173237",
        ],
    },
    "wf_passive_valkyr": {
        "label": "Valkyr Passive (Nimble Landing)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:ValkyrIcon272.png"),
            ("fandom_en", "File:ValkyrIcon272.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/f/f2/ValkyrFull.png/revision/latest/smart/width/250/height/250?cb=20141124023346",
        ],
    },
    "wf_passive_frost": {
        "label": "Frost Passive (Cryogenic Shell)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:FrostIcon272.png"),
            ("fandom_en", "File:FrostIcon272.png"),
        ],
        "direct_urls": [],
    },
    "wf_passive_follie": {
        "label": "Follie Passive (Ink Trail / Inkblot)",
        "kind": "portrait",
        "wiki_files": [
            ("official", "File:FollieIcon272.png"),
            ("fandom_ru", "File:Фолли_вики.png"),
        ],
        "direct_urls": [
            "https://static.wikia.nocookie.net/warframe/images/4/43/%D0%A4%D0%BE%D0%BB%D0%BB%D0%B8_%D0%B2%D0%B8%D0%BA%D0%B8.png/revision/latest/thumbnail/width/360/height/450?cb=20260325224349&path-prefix=ru",
        ],
    },
}


def http_get(url: str, timeout: int = 20) -> bytes:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read()


def resolve_mediawiki_image_url(wiki_key: str, file_title: str) -> str | None:
    api = MEDIAWIKI_APIS[wiki_key]
    params = urllib.parse.urlencode({
        "action": "query",
        "titles": file_title,
        "prop": "imageinfo",
        "iiprop": "url",
        "format": "json",
    })
    try:
        data = json.loads(http_get(f"{api}?{params}").decode("utf-8"))
        pages = data.get("query", {}).get("pages", {})
        for page in pages.values():
            infos = page.get("imageinfo")
            if infos and infos[0].get("url"):
                return infos[0]["url"]
    except Exception as exc:
        print(f"  [warn] {wiki_key} lookup failed for {file_title}: {exc}")
    return None


def fetch_icon_image(spec: dict) -> tuple[Image.Image, str]:
    for wiki_key, file_title in spec["wiki_files"]:
        url = resolve_mediawiki_image_url(wiki_key, file_title)
        if not url:
            continue
        try:
            raw = http_get(url)
            img = Image.open(io.BytesIO(raw)).convert("RGBA")
            return img, f"{wiki_key}:{file_title} ({url})"
        except Exception as exc:
            print(f"  [warn] download failed for {url}: {exc}")

    for url in spec["direct_urls"]:
        try:
            raw = http_get(url)
            img = Image.open(io.BytesIO(raw)).convert("RGBA")
            return img, f"direct ({url})"
        except Exception as exc:
            print(f"  [warn] direct download failed for {url}: {exc}")

    raise RuntimeError(f"Could not fetch any wiki icon for {spec['label']}")


def detect_target_size() -> int:
    """Match existing sprite conventions: use 32x32 when AbilityGlyph uses blitSprite
    (which scales smoothly for both 16x16 list rows and 32x32 detail panels), or
    match solar_grenade.png's exact size if AbilityGlyph uses fixed-size blit."""
    existing_size = 32
    ref_sprite = SPRITE_DIR / "solar_grenade.png"
    if ref_sprite.exists():
        with Image.open(ref_sprite) as im:
            existing_size = im.size[0]
            print(f"Existing reference sprite solar_grenade.png: {im.size}, mode={im.mode}")

    if ABILITY_GLYPH_JAVA.exists():
        src = ABILITY_GLYPH_JAVA.read_text(encoding="utf-8", errors="replace")
        uses_blit_sprite = "blitSprite" in src
        print(f"AbilityGlyph.java uses blitSprite={uses_blit_sprite}")
        if uses_blit_sprite:
            return max(32, existing_size)
    return existing_size


def normalize_to_white_glyph(img: Image.Image, target_size: int, kind: str) -> Image.Image:
    """Convert a Warframe Wiki icon into a crisp white-on-transparent RGBA sprite
    so `AbilityGlyph.draw(..., color)` tints it cleanly in dark & light UI states."""
    img = img.convert("RGBA")
    w, h = img.size
    px = img.load()

    # Check whether the source already has meaningful transparency around its corners.
    corners = [px[0, 0][3], px[w - 1, 0][3], px[0, h - 1][3], px[w - 1, h - 1][3]]
    has_transparency = min(corners) < 32

    if kind == "white_icon":
        # Convert to white RGB with alpha derived from alpha (and luma if opaque dark bg).
        mask = Image.new("L", (w, h), 0)
        mpx = mask.load()
        for y in range(h):
            for x in range(w):
                r, g, b, a = px[x, y]
                luma = int(0.299 * r + 0.587 * g + 0.114 * b)
                if has_transparency:
                    val = a if luma >= 40 else int(a * 0.85)
                else:
                    val = 0 if luma < 36 else min(255, int((luma - 36) * 255 / 219))
                mpx[x, y] = val
    else:
        # Warframe portrait icon (*Icon272.png) -> high-contrast white-on-alpha emblem.
        if h > w * 1.15:
            img = img.crop((0, 0, w, w))
            w, h = img.size
            px = img.load()
        gray = ImageOps.autocontrast(img.convert("L"), cutoff=2)
        gpx = gray.load()
        mask = Image.new("L", (w, h), 0)
        mpx = mask.load()
        for y in range(h):
            for x in range(w):
                _, _, _, a = px[x, y]
                if has_transparency and a < 24:
                    mpx[x, y] = 0
                else:
                    lum = gpx[x, y]
                    strength = int(96 + (lum / 255.0) * 159)
                    mpx[x, y] = int(strength * (a / 255.0)) if has_transparency else (0 if lum < 28 else strength)

    bbox = mask.getbbox()
    if bbox:
        mask = mask.crop(bbox)

    pad = 2 if target_size >= 32 else 1
    inner = target_size - 2 * pad
    mw, mh = mask.size
    scale = min(inner / max(1, mw), inner / max(1, mh))
    nw = max(1, round(mw * scale))
    nh = max(1, round(mh * scale))
    resized = mask.resize((nw, nh), Image.Resampling.LANCZOS)

    rpx = resized.load()
    for y in range(nh):
        for x in range(nw):
            a = rpx[x, y]
            if a < 20:
                rpx[x, y] = 0
            else:
                rpx[x, y] = min(255, int(a * 1.18))

    out = Image.new("RGBA", (target_size, target_size), (255, 255, 255, 0))
    ox = (target_size - nw) // 2
    oy = (target_size - nh) // 2
    white_patch = Image.new("RGBA", (nw, nh), (255, 255, 255, 255))
    white_patch.putalpha(resized)
    out.paste(white_patch, (ox, oy), white_patch)
    return out


def main() -> int:
    SPRITE_DIR.mkdir(parents=True, exist_ok=True)
    target_size = detect_target_size()
    print(f"Baking {len(ICON_SPECS)} Warframe Binder icons at {target_size}x{target_size} into {SPRITE_DIR}")

    failures: list[str] = []
    for ability_id, spec in ICON_SPECS.items():
        try:
            raw_img, source = fetch_icon_image(spec)
            sprite = normalize_to_white_glyph(raw_img, target_size, spec["kind"])
            out_path = SPRITE_DIR / f"{ability_id}.png"
            sprite.save(out_path, optimize=True)
            print(f"  [ok] {ability_id}.png <- {source}")
        except Exception as exc:
            print(f"  [FAIL] {ability_id}: {exc}")
            failures.append(f"{ability_id}: {exc}")

    if failures:
        print("\nSome icons failed to fetch:", file=sys.stderr)
        for line in failures:
            print(f"  - {line}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
