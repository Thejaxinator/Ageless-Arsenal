#!/usr/bin/env python3
"""
Import extracted Destiny 2 weapon sound files (e.g. from Steam Workshop #1885850593
after unpacking .gma archives locally with gmad.exe) into Ageless Arsenals:

  1. Scans --src for audio files (.wav, .ogg, .mp3) whose folder or filename matches
     a Destiny weapon ID in Ageless Arsenals.
  2. Converts each matched fire sound to 44.1 kHz Ogg Vorbis (.ogg) via ffmpeg under:
       src/main/resources/assets/destiny2mc/sounds/weapon/<weapon_id>/fire_<n>.ogg
  3. Generates/updates src/main/resources/assets/destiny2mc/sounds.json with
     `weapon.<weapon_id>.fire` entries automatically picked up by GunSoundEngine.

Usage:
  python3 tools/import_gmod_destiny_sounds.py --src "E:/ExtractedGModDestinySounds"
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
SOUNDS_DIR = REPO_ROOT / "src/main/resources/assets/destiny2mc/sounds/weapon"
SOUNDS_JSON = REPO_ROOT / "src/main/resources/assets/destiny2mc/sounds.json"

WEAPON_KEYWORDS: dict[str, list[str]] = {
    "ace_of_spades": ["ace_of_spades", "aceofspades", "ace"],
    "sunshot": ["sunshot"],
    "thorn": ["thorn"],
    "crimson": ["crimson"],
    "the_last_word": ["last_word", "lastword", "tlw"],
    "izanagis_burden": ["izanagi", "burden"],
    "cloudstrike": ["cloudstrike"],
    "whisper_of_the_worm": ["whisper"],
    "darci": ["darci"],
    "ice_breaker": ["ice_breaker", "icebreaker"],
    "khvostov_7g_0x": ["khvostov"],
    "sweet_business": ["sweet_business", "sweetbusiness"],
    "monte_carlo": ["monte_carlo", "montecarlo"],
    "cerberus_plus_1": ["cerberus"],
    "outbreak_perfected": ["outbreak"],
    "graviton_lance": ["graviton"],
    "riskrunner": ["riskrunner"],
    "dead_mans_tale": ["dead_mans_tale", "dmt"],
    "forerunner": ["forerunner"],
    "arbalest": ["arbalest"],
    "hammerhead": ["hammerhead"],
    "midnight_coup": ["midnight_coup", "midnightcoup"],
    "the_recluse": ["recluse"],
    "hung_jury_sr4": ["hung_jury", "hungjury"],
    "bxr_55_battler": ["bxr", "battler"],
    "vex_mythoclast": ["mythoclast", "vex"],
    "sleeper_simulant": ["sleeper"],
    "prometheus_lens": ["prometheus"],
    "gjallarhorn": ["gjallarhorn", "ghorn"],
    "witherhoard": ["witherhoard"],
    "telesto": ["telesto"],
    "jotunn": ["jotunn"],
    "lord_of_wolves": ["lord_of_wolves", "low"],
    "the_chaperone": ["chaperone"],
    "ikelos_sg_v103": ["ikelos_sg", "ikelos"],
    "tractor_cannon": ["tractor"],
    "fatebringer": ["fatebringer"],
    "praedyths_revenge": ["praedyth"],
    "vision_of_confluence": ["vision_of_confluence"],
    "found_verdict": ["found_verdict"],
}

FIRE_RE = re.compile(r"(fire|shoot|shot|blast|discharge|report)", re.IGNORECASE)
IGNORE_RE = re.compile(r"(reload|draw|deploy|equip|holster|bolt|clip|mag|inspect|dry|click|tail|echo)", re.IGNORECASE)


def convert_to_ogg(src: Path, dst: Path) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(
        ["ffmpeg", "-y", "-i", str(src), "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "5", str(dst)],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--src", required=True, help="Directory containing extracted GMod sound files")
    args = parser.parse_args()

    src_root = Path(args.src)
    if not src_root.is_dir():
        raise SystemExit(f"Source directory not found: {src_root}")

    sounds_map: dict[str, dict] = {}
    if SOUNDS_JSON.exists():
        sounds_map = json.loads(SOUNDS_JSON.read_text(encoding="utf-8"))

    audio_files = [p for p in src_root.rglob("*") if p.suffix.lower() in {".wav", ".ogg", ".mp3"}]
    matched_count = 0

    for weapon_id, keys in WEAPON_KEYWORDS.items():
        candidates: list[Path] = []
        for path in audio_files:
            rel_lower = str(path.relative_to(src_root)).lower()
            if any(k in rel_lower for k in keys):
                if FIRE_RE.search(path.stem) and not IGNORE_RE.search(path.stem):
                    candidates.append(path)
        if not candidates:
            continue
        entries = []
        for idx, cand in enumerate(sorted(candidates)[:4]):
            out_ogg = SOUNDS_DIR / weapon_id / f"fire_{idx}.ogg"
            convert_to_ogg(cand, out_ogg)
            entries.append(f"destiny2mc:weapon/{weapon_id}/fire_{idx}")
        sounds_map[f"weapon.{weapon_id}.fire"] = {"sounds": entries}
        matched_count += 1
        print(f"Imported {len(entries)} fire sound(s) for {weapon_id}")

    SOUNDS_JSON.parent.mkdir(parents=True, exist_ok=True)
    SOUNDS_JSON.write_text(json.dumps(sounds_map, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Done: wired {matched_count} Destiny weapons into {SOUNDS_JSON.relative_to(REPO_ROOT)}")


if __name__ == "__main__":
    main()
