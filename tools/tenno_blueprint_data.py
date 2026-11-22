"""Source-of-truth for the shipped Warframe roster, vanilla-material Foundry economy and tags.
Run from either this authoring module or pass --output src/main/resources in an exported project.
Minecraft reads the resulting recipe JSON on /reload; the generator is never needed to play.
"""
import argparse
import json
from pathlib import Path
from urllib.parse import quote

# Registry ID, wiki title, slot, series, three counted costs. d/ marks a mod item.
rows = [
    ("ax_52","AX-52","primary","standard","iron_ingot=8 copper_ingot=4 oak_planks=3"),
    ("grakata","Grakata","primary","standard","iron_ingot=4 copper_ingot=4 gunpowder=1"),
    ("prisma_grakata","Prisma Grakata","primary","prisma","d/grakata=1 amethyst_shard=8 prismarine_crystals=4"),
    ("twin_grakatas","Twin Grakatas","secondary","standard","d/grakata=2 copper_ingot=2 leather=2"),
    ("braton","Braton","primary","standard","iron_ingot=5 copper_ingot=3 oak_planks=2"),
    ("braton_prime","Braton Prime","primary","prime","d/braton=1 gold_ingot=6 amethyst_shard=4"),
    ("braton_vandal","Braton Vandal","primary","vandal","d/braton=1 prismarine_shard=6 lapis_lazuli=4"),
    ("burston","Burston","primary","standard","iron_ingot=6 copper_ingot=3 redstone=2"),
    ("burston_prime","Burston Prime","primary","prime","d/burston=1 gold_ingot=6 amethyst_shard=4"),
    ("ignis","Ignis","primary","standard","iron_ingot=6 blaze_powder=2 copper_ingot=4"),
    ("ignis_wraith","Ignis Wraith","primary","wraith","d/ignis=1 coal=6 redstone=8"),
    ("brakk","Brakk","secondary","standard","iron_ingot=4 copper_ingot=3 gunpowder=2"),
    ("kuva_brakk","Kuva Brakk","secondary","kuva","d/brakk=1 nether_wart=6 redstone=8"),
    ("nukor","Nukor","secondary","standard","copper_ingot=6 redstone=4 glass=3"),
    ("kuva_nukor","Kuva Nukor","secondary","kuva","d/nukor=1 nether_wart=6 redstone=8"),
    ("cedo","Cedo","primary","standard","iron_ingot=7 quartz=4 copper_ingot=5"),
    ("cedo_prime","Cedo Prime","primary","prime","d/cedo=1 gold_ingot=6 amethyst_shard=4"),
    ("vesper_77","Vesper 77","secondary","standard","iron_ingot=4 copper_ingot=3 black_dye=2"),
    ("perigale","Perigale","primary","standard","iron_ingot=8 spyglass=1 redstone=4"),
    ("perigale_prime","Perigale Prime","primary","prime","d/perigale=1 gold_ingot=6 amethyst_shard=4"),
    ("soma","Soma","primary","standard","iron_ingot=7 copper_ingot=5 redstone=3"),
    ("soma_prime","Soma Prime","primary","prime","d/soma=1 gold_ingot=6 amethyst_shard=4"),
    ("cantare","Cantare","secondary","standard","iron_ingot=4 feather=4 amethyst_shard=2"),
    ("torid","Torid","primary","standard","iron_ingot=5 fermented_spider_eye=2 slime_ball=3"),
    ("aksondol","Aksondol","secondary","standard","iron_ingot=6 packed_ice=2 quartz=4"),
    ("bubonico","Bubonico","primary","standard","bone=8 slime_ball=4 fermented_spider_eye=2"),
    ("coda_bubonico","Coda Bubonico","primary","coda","d/bubonico=1 echo_shard=2 slime_ball=8"),
    ("amanata","Amanata","melee","standard","iron_ingot=5 bamboo=3 amethyst_shard=2"),
    ("hate","Hate","melee","standard","iron_ingot=6 obsidian=2 bone=3"),
    ("harmony","Harmony","melee","standard","iron_ingot=5 amethyst_shard=4 feather=3"),
    ("heat_sword","Heat Sword","melee","standard","iron_ingot=3 coal=1 leather=1"),
    ("dual_heat_swords","Dual Heat Swords","melee","standard","d/heat_sword=1 iron_ingot=2 leather=1"),
    ("sun_and_moon","Sun & Moon","melee","standard","iron_ingot=6 gold_ingot=2 lapis_lazuli=2")
]

def encode(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n",encoding="utf-8")

def generate(root):
    data = root / "data/destiny2mc"
    tags = {"weapons": [], "ranged": []}
    lang = {}
    for item, label, slot, variant, costs in rows:
        key = "destiny2mc:" + item
        tags["weapons"].append(key)
        tags.setdefault(slot, []).append(key)
        if slot != "melee":
            tags["ranged"].append(key)
        tags.setdefault("variants/" + variant, []).append(key)
        materials = []
        for pair in costs.split():
            name, amount = pair.split("=")
            name = "destiny2mc:" + name[2:] if name.startswith("d/") else "minecraft:" + name
            materials.append({"item":name, "count":int(amount)})
        encode(data / "recipe/tenno_foundry" / (item + ".json"), {
            "type":"destiny2mc:tenno_foundry","result":key,"materials":materials})
        lang["item.destiny2mc." + item] = label
    for group, items in tags.items():
        encode(data / "tags/item/warframe" / (group + ".json"), {"replace":False,"values":items})
    encode(root / "assets/destiny2mc/lang/en_us.json", lang)

if __name__ == "__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--output",type=Path,default=Path(__file__).parent/"src/main/resources")
    generate(parser.parse_args().output)
