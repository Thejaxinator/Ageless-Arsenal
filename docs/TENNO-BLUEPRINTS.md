# Warframe Foundry blueprints

At either Gunsmith, choose **Warframe → Blueprints**. A starter Braton takes five iron ingots, three copper ingots and two oak planks. A Heat Sword takes three iron ingots, one coal and one leather. Make two Grakatas and combine them with two copper ingots and two leather for Twin Grakatas. Each Prime, Kuva, Prisma, Vandal, Wraith and Coda recipe uses its matching base weapon. The upgraded weapon is a new stack with its own profile. Weapons with custom names, enchantments or installed mods and items in the offhand and armor slots are excluded from payment. All unlocked blueprint listings can be inspected from the start. Components set the survival cost. The Warframe creative tab contains these weapons and all four mod packs.

## Recipe format

Place a blueprint at `data/destiny2mc/recipe/tenno_foundry/<item_id>.json`, matching the registered item's path. Minecraft 1.21.1 synchronizes this custom recipe type to clients. A server datapack can override each price or disable an entry through a NeoForge recipe condition. A failed or absent recipe hides that item on the Foundry page. Server and client use the same current recipe, including after a data reload.

```json
{
  "type": "destiny2mc:tenno_foundry",
  "result": "destiny2mc:twin_grakatas",
  "materials": [
    {"item": "destiny2mc:grakata", "count": 2},
    {"item": "minecraft:copper_ingot", "count": 2},
    {"item": "minecraft:leather", "count": 2}
  ]
}
```

Each blueprint accepts one to six unique non-air item IDs with counts from 1 to 64. The output must be a registered Warframe arm and cannot be an ingredient. The stock blueprints have three ingredients apiece. Supply an ordinary base weapon without installed mods, enchantments or a custom name for that recipe. Recipe inputs are quoted and debited in a single server-side transaction. If the backpack fills up because only part of an ingredient stack was consumed, the character drops the output at their feet.

## Item tags

The catalog uses the existing `destiny2mc` namespace for compatibility. Minecraft 1.21.1 item tag JSON belongs under `data/destiny2mc/tags/item/warframe/`. The complete set is `#destiny2mc:warframe/weapons`. Use `warframe/ranged`, `warframe/primary`, `warframe/secondary`, or `warframe/melee` for a slot. Variant tags live below `warframe/variants/` for `standard`, `prime`, `kuva`, `coda`, `vandal`, `wraith`, and `prisma`.

For example, `/clear @s #destiny2mc:warframe/melee 0` counts Warframe melee weapons in the player's inventory. `/give @s destiny2mc:coda_bubonico` creates the Coda arm cannon.

## Extending the family

`TennoGunSpec` and its action families define ranged behavior independently of the Destiny weapon index. Melee item profiles are bundled in `src/main/resources/tenno/melee.json` and registered during startup. New item IDs require installation on both sides. Add the item's art, language entry, item tags and a blueprint. The stock recipe contents can be customized with datapacks at runtime. Server-authoritative combat, item registration, and client presentation are independent from the recipe JSON.
