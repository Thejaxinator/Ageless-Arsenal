"""Idempotently sync Corpus entries into bestiary.json and en_us.json."""
import json
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]

CORPUS_BESTIARY = [
    {
        "id": "corpus_crewman",
        "name": "Corpus Crewman",
        "faction": "corpus",
        "role": "Rifleman",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Patrols snowy plains, frozen peaks, ice spikes, and stony shores at night.",
        "mechanics": [
            "Engages at mid-range with sustained rapid-fire Arc plasma bursts from a twin-prong Dera rifle.",
            "Advances in squads alongside MOAs and relies on Shield Ospreys for defensive over-shielding."
        ],
        "threats": [
            "Accurate burst fire pressures exposed targets across open snowy terrain.",
            "Take out nearby Shield Ospreys first so Crewman squads lose their shield tether."
        ],
        "reward": "Drops Resonant Shards and standard combat loot."
    },
    {
        "id": "corpus_moa",
        "name": "Corpus MOA",
        "faction": "corpus",
        "role": "Bipedal Walker",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Spawns alongside Corpus Crewmen across frozen Overworld biomes and rocky coasts.",
        "mechanics": [
            "Sprints rapidly on reverse-jointed walker legs to close distance and flank entrenched players.",
            "Fires high-impact Arc plasma bolts from its swiveling gold dorsal cannon turret."
        ],
        "threats": [
            "High movement speed makes it harder to kite than standard Crewmen.",
            "Focus fire on the central chassis and turret before it closes to point-blank range."
        ],
        "reward": "Drops Resonant Shards and standard combat loot."
    },
    {
        "id": "corpus_shield_osprey",
        "name": "Shield Osprey",
        "faction": "corpus",
        "role": "Support Drone",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Hovers above Corpus patrols in snowy plains, frozen peaks, ice spikes, and stony shores.",
        "mechanics": [
            "Maintains elevated hover above the squad while projecting an Arc shield tether to nearby Corpus allies.",
            "Tethered allies regenerate health and gain heavy damage resistance until the Osprey is destroyed or staggered."
        ],
        "threats": [
            "High-priority support unit: ignoring it lets Crewmen and MOAs shrug off sustained fire.",
            "Use hitscan or fast projectile weapons to pick it out of the air first."
        ],
        "reward": "Drops Resonant Shards and standard combat loot."
    },
]

CORPUS_LANG = {
    "entity.destiny2mc.corpus_crewman": "Corpus Crewman",
    "entity.destiny2mc.corpus_moa": "Corpus MOA",
    "entity.destiny2mc.corpus_shield_osprey": "Shield Osprey",
    "item.destiny2mc.corpus_crewman_spawn_egg": "Corpus Crewman Spawn Egg",
    "item.destiny2mc.corpus_moa_spawn_egg": "Corpus MOA Spawn Egg",
    "item.destiny2mc.corpus_shield_osprey_spawn_egg": "Shield Osprey Spawn Egg",
}


def main():
    b_path = REPO / 'src/main/resources/assets/destiny2mc/atlas/bestiary.json'
    bestiary = json.loads(b_path.read_text(encoding='utf-8'))
    existing = {e['id'] for e in bestiary}
    for entry in CORPUS_BESTIARY:
        if entry['id'] not in existing:
            bestiary.append(entry)
    b_path.write_text(json.dumps(bestiary, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

    l_path = REPO / 'src/main/resources/assets/destiny2mc/lang/en_us.json'
    lang = json.loads(l_path.read_text(encoding='utf-8'))
    lang.update(CORPUS_LANG)
    l_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    print(f'Synced bestiary ({len(bestiary)} entries) and en_us.json ({len(lang)} keys)')


if __name__ == '__main__':
    main()
