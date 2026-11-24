#!/usr/bin/env python3
"""
Bake CC0 1.0 Destiny 2 HUD icons from https://github.com/justrealmilk/destiny-icons
into crisp white-on-alpha Minecraft 1.21.1 GUI PNG textures:

  - src/main/resources/assets/destiny2mc/textures/gui/silhouettes/sil_*.png  (108x40)
  - src/main/resources/assets/destiny2mc/textures/gui/hud/elem_*.png         (32x32)
  - src/main/resources/assets/destiny2mc/textures/gui/hud/ammo_*.png         (32x32)
"""
from __future__ import annotations

import io
import re
import urllib.request
from pathlib import Path
import cairosvg
from PIL import Image

REPO_ROOT = Path(__file__).resolve().parents[1]
SIL_DIR = REPO_ROOT / "src/main/resources/assets/destiny2mc/textures/gui/silhouettes"
HUD_DIR = REPO_ROOT / "src/main/resources/assets/destiny2mc/textures/gui/hud"

RAW_BASE = "https://raw.githubusercontent.com/justrealmilk/destiny-icons/master"
USER_AGENT = "AgelessArsenalsDestinyIconBaker/1.0 (https://github.com/Thejaxinator/Ageless-Arsenal)"

WEAPON_SILHOUETTES: dict[str, str] = {
    "sil_auto": "weapons/auto_rifle.svg",
    "sil_pulse": "weapons/pulse_rifle.svg",
    "sil_scout": "weapons/scout_rifle.svg",
    "sil_smg": "weapons/smg.svg",
    "sil_hc": "weapons/hand_cannon.svg",
    "sil_sidearm": "weapons/sidearm.svg",
    "sil_sniper": "weapons/sniper_rifle.svg",
    "sil_shotgun": "weapons/shotgun.svg",
    "sil_fusion": "weapons/fusion_rifle.svg",
    "sil_linear": "weapons/wire_rifle.svg",
    "sil_trace": "weapons/trace_rifle.svg",
    "sil_rocket": "weapons/rocket_launcher.svg",
    "sil_grenade": "weapons/grenade_launcher.svg",
    "sil_mg": "weapons/machinegun.svg",
    "sil_bow": "weapons/bow.svg",
    "sil_glaive": "weapons/glaive.svg",
}

HUD_ICONS: dict[str, str] = {
    "elem_kinetic": "general/damage_kinetic.svg",
    "elem_solar": "general/damage_solar.svg",
    "elem_arc": "general/damage_arc.svg",
    "elem_void": "general/damage_void.svg",
    "elem_stasis": "general/damage_stasis.svg",
    "ammo_primary": "general/ammo-primary.svg",
    "ammo_special": "general/ammo-special.svg",
    "ammo_heavy": "general/ammo-heavy.svg",
}


def fetch_svg(rel_path: str) -> bytes:
    url = f"{RAW_BASE}/{rel_path}"
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=30) as resp:
        raw = resp.read().decode("utf-8")
    raw = re.sub(r'fill="[^"]*"', 'fill="#ffffff"', raw)
    if "<svg" in raw and 'fill="' not in raw.split(">", 1)[0]:
        raw = raw.replace("<svg", '<svg fill="#ffffff"', 1)
    return raw.encode("utf-8")


def rasterize_fit(svg_bytes: bytes, target_w: int, target_h: int, pad: int) -> Image.Image:
    png_bytes = cairosvg.svg2png(bytestring=svg_bytes, output_width=target_w * 2, output_height=target_h * 2)
    img = Image.open(io.BytesIO(png_bytes)).convert("RGBA")
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    avail_w = max(1, target_w - pad * 2)
    avail_h = max(1, target_h - pad * 2)
    scale = min(avail_w / img.width, avail_h / img.height)
    new_w = max(1, round(img.width * scale))
    new_h = max(1, round(img.height * scale))
    resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)

    _, _, _, alpha = resized.split()
    white = Image.new("RGBA", (new_w, new_h), (255, 255, 255, 255))
    white.putalpha(alpha)

    canvas = Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))
    canvas.paste(white, ((target_w - new_w) // 2, (target_h - new_h) // 2), white)
    return canvas


def main() -> None:
    SIL_DIR.mkdir(parents=True, exist_ok=True)
    HUD_DIR.mkdir(parents=True, exist_ok=True)

    for name, rel in WEAPON_SILHOUETTES.items():
        svg = fetch_svg(rel)
        out = SIL_DIR / f"{name}.png"
        rasterize_fit(svg, 108, 40, pad=3).save(out, "PNG")
        print(f"Baked silhouette: {out.relative_to(REPO_ROOT)} <- {rel}")

    for name, rel in HUD_ICONS.items():
        svg = fetch_svg(rel)
        out = HUD_DIR / f"{name}.png"
        rasterize_fit(svg, 32, 32, pad=2).save(out, "PNG")
        print(f"Baked HUD icon:   {out.relative_to(REPO_ROOT)} <- {rel}")


if __name__ == "__main__":
    main()
