"""Idempotently sync all 10 Corpus entries into bestiary.json and en_us.json."""
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
            "Protected by regenerating Corpus Proto-Shields (Cold shreds shields faster; Toxin bypasses straight to health).",
            "Engages at mid-range with sustained Arc plasma bursts from a twin-prong Dera rifle."
        ],
        "threats": [
            "Shields recharge after 4 seconds out of fire, and faster near a Shield Osprey.",
            "Eliminate escorting Ospreys first or bring Cold/Toxin Warframe weapon mods."
        ],
        "reward": "Drops Resonant Shards and rare Warframe Mod Packs."
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
            "Sprints rapidly on reverse-jointed walker legs beneath regenerating Proto-Shields.",
            "Fires high-impact Arc plasma bolts from its low-slung dorsal plasma cannon."
        ],
        "threats": [
            "High movement speed makes it harder to kite than standard Crewmen.",
            "Break its Proto-Shields with Cold-modded weapons before it closes to point-blank range."
        ],
        "reward": "Drops Resonant Shards and rare Warframe Mod Packs."
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
            "Projects an Arc shield tether to nearby Corpus allies, accelerating their Proto-Shield regeneration by 1.75x.",
            "Tethered allies also gain damage resistance and health recovery until the Osprey is destroyed."
        ],
        "threats": [
            "High-priority support unit: leaving it alive lets heavy Corpus units recharge shields mid-fight.",
            "Use hitscan or fast projectile weapons to pick it out of the air first."
        ],
        "reward": "Drops Resonant Shards and rare Warframe Mod Packs."
    },
    {
        "id": "corpus_prod_crewman",
        "name": "Prod Crewman",
        "faction": "corpus",
        "role": "Melee Rusher",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Charges ahead of Corpus patrols across snowy plains, frozen peaks, and stony shores.",
        "mechanics": [
            "Closes distance aggressively under regenerating Proto-Shields to strike with an electrified Prova shock baton.",
            "Flanks players pinned down by Crewman Dera bursts and Corpus Tech suppression fire."
        ],
        "threats": [
            "High sprint speed and close-quarters Arc baton swings punish slow reloads.",
            "Interrupt its rush early before it reaches melee range."
        ],
        "reward": "Drops Resonant Shards and rare Warframe Mod Packs."
    },
    {
        "id": "corpus_tech",
        "name": "Corpus Tech",
        "faction": "corpus",
        "role": "Heavy Gunner",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Leads elite Corpus squads in frozen peaks, ice spikes, and snowy slopes.",
        "mechanics": [
            "Towers over standard Crewmen with heavy 72-point Proto-Shields and relentless Supra plasma LMG volleys.",
            "Anchors the squad's front line while Shield Ospreys reinforce its shield pool."
        ],
        "threats": [
            "Sustained Supra fire shreds cover quickly at medium range.",
            "Strip its shields with Cold-modded weapons or bypass them with Toxin."
        ],
        "reward": "Drops Resonant Shards and elevated Warframe Mod Pack chances."
    },
    {
        "id": "corpus_shockwave_moa",
        "name": "Shockwave MOA",
        "faction": "corpus",
        "role": "Heavy Walker",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Deploys alongside Corpus Techs and Crewmen in frozen Overworld biomes.",
        "mechanics": [
            "Telegraphs a heavy crouch stomp before unleashing a 6.5-block expanding ground-level Arc shockwave ring.",
            "The shockwave only strikes grounded targets—jumping cleanly avoids both damage and knockback."
        ],
        "threats": [
            "Punishes stationary melee fighters who stay planted on the ground.",
            "Watch for the pulsing cyan charge ring and jump over the expanding wave."
        ],
        "reward": "Drops Resonant Shards and rare Warframe Mod Packs."
    },
    {
        "id": "corpus_nullifier",
        "name": "Nullifier Crewman",
        "faction": "corpus",
        "role": "Anti-Ability Sniper",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Escorts elite Corpus formations across frozen peaks and snowy plains.",
        "mechanics": [
            "Projects a shrinking Nullifier energy dome that intercepts outside fire (8-damage cap per hit favors rapid-fire weapons) and strips player buffs/absorption inside.",
            "Sniping the spinning dorsal emitter drone collapses the dome instantly; fires high-velocity Lanka railgun bolts."
        ],
        "threats": [
            "Single-shot sniper hits barely shrink the dome unless you snipe the top emitter drone.",
            "Either shred the dome with automatic fire, snipe the drone, or rush inside to fight close-quarters."
        ],
        "reward": "Drops Resonant Shards and elevated Warframe Mod Pack chances."
    },
    {
        "id": "corpus_bursa",
        "name": "Isolator Bursa",
        "faction": "corpus",
        "role": "Heavy Riot Walker",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Rare heavy reinforcement on frozen peaks, ice spikes, and snowy slopes.",
        "mechanics": [
            "Twin front riot shields deflect 80% of frontal non-Toxin damage while dorsal pods launch Arc mortar hazards.",
            "Flanking behind the Bursa to strike its glowing rear console deals 2.0x critical weakpoint damage."
        ],
        "threats": [
            "140-point Proto-Shields and frontal armor make head-on engagements brutal.",
            "Circle behind its slower turn radius to hit the rear console or bypass shields with Toxin."
        ],
        "reward": "Drops Resonant Shards and high Warframe Mod Pack drop chances."
    },
    {
        "id": "corpus_hyena",
        "name": "Hyena Proxy",
        "faction": "corpus",
        "role": "Quadruped Hunter",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Hunts in packs across snowy plains and rocky shores at night.",
        "mechanics": [
            "Sprints rapidly on four predator limbs and launches leaping pounce bursts that inflict brief Magnetic disruption.",
            "Protected by fast-recharging 84-point Proto-Shields."
        ],
        "threats": [
            "Fastest ground unit in the Corpus roster; overwhelms players caught in the open.",
            "Use Cold/slowing effects to pin it down before it chains leaps."
        ],
        "reward": "Drops Resonant Shards and elevated Warframe Mod Pack chances."
    },
    {
        "id": "corpus_raknoid",
        "name": "Scyto Raknoid",
        "faction": "corpus",
        "role": "Arachnid Walker",
        "site": "",
        "origin": "Warframe — Corpus",
        "spawn": "Stalks ice spikes, snowy slopes, and frozen peaks.",
        "mechanics": [
            "Fires sticky Cryo-Web snare volleys that heavily slow targets and leave frost hazard patches.",
            "Striking its elevated frosted-white dorsal abdomen dome deals 1.75x critical weakpoint damage."
        ],
        "threats": [
            "Snare volleys pin you in place while nearby Crewmen and MOAs focus fire.",
            "Aim high at the exposed frosted dome to crack its 78-point Proto-Shields quickly."
        ],
        "reward": "Drops Resonant Shards and elevated Warframe Mod Pack chances."
    },
]

CORPUS_LANG = {
    "entity.destiny2mc.corpus_crewman": "Corpus Crewman",
    "entity.destiny2mc.corpus_moa": "Corpus MOA",
    "entity.destiny2mc.corpus_shield_osprey": "Shield Osprey",
    "entity.destiny2mc.corpus_prod_crewman": "Prod Crewman",
    "entity.destiny2mc.corpus_tech": "Corpus Tech",
    "entity.destiny2mc.corpus_shockwave_moa": "Shockwave MOA",
    "entity.destiny2mc.corpus_nullifier": "Nullifier Crewman",
    "entity.destiny2mc.corpus_bursa": "Isolator Bursa",
    "entity.destiny2mc.corpus_hyena": "Hyena Proxy",
    "entity.destiny2mc.corpus_raknoid": "Scyto Raknoid",
    "item.destiny2mc.corpus_crewman_spawn_egg": "Corpus Crewman Spawn Egg",
    "item.destiny2mc.corpus_moa_spawn_egg": "Corpus MOA Spawn Egg",
    "item.destiny2mc.corpus_shield_osprey_spawn_egg": "Shield Osprey Spawn Egg",
    "item.destiny2mc.corpus_prod_crewman_spawn_egg": "Prod Crewman Spawn Egg",
    "item.destiny2mc.corpus_tech_spawn_egg": "Corpus Tech Spawn Egg",
    "item.destiny2mc.corpus_shockwave_moa_spawn_egg": "Shockwave MOA Spawn Egg",
    "item.destiny2mc.corpus_nullifier_spawn_egg": "Nullifier Crewman Spawn Egg",
    "item.destiny2mc.corpus_bursa_spawn_egg": "Isolator Bursa Spawn Egg",
    "item.destiny2mc.corpus_hyena_spawn_egg": "Hyena Proxy Spawn Egg",
    "item.destiny2mc.corpus_raknoid_spawn_egg": "Scyto Raknoid Spawn Egg",
}


def main():
    b_path = REPO / 'src/main/resources/assets/destiny2mc/atlas/bestiary.json'
    bestiary = json.loads(b_path.read_text(encoding='utf-8'))
    by_id = {e['id']: i for i, e in enumerate(bestiary)}
    for entry in CORPUS_BESTIARY:
        if entry['id'] in by_id:
            bestiary[by_id[entry['id']]] = entry
        else:
            bestiary.append(entry)
    b_path.write_text(json.dumps(bestiary, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

    l_path = REPO / 'src/main/resources/assets/destiny2mc/lang/en_us.json'
    lang = json.loads(l_path.read_text(encoding='utf-8'))
    lang.update(CORPUS_LANG)
    l_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    print(f'Synced bestiary ({len(bestiary)} entries) and en_us.json ({len(lang)} keys)')


if __name__ == '__main__':
    main()
