# Faction salvage and the gunsmith

Ageless Arsenals adds seven components that players can recover from enemy deaths. Each component has a hand-authored 32 × 32 icon and a tooltip describing its construction. The gunsmith accepts each component as the third ingredient of its faction's weapon.

| Combatants | Component | Registry ID under `destiny2mc` | Faction weapon |
|---|---|---|---|
| Fallen | Ether Induction Coil | `ether_induction_coil` | Wire Rifle |
| Vex | Conduit Lens | `conduit_lens` | Vex Slap Rifle |
| Hive | Runed Marrow | `runed_marrow` | Hive Boomer |
| Cabal | Cabal Breech Seal | `breech_seal` | Cabal Slug Rifle |
| Taken | Inversion Knot | `inversion_knot` | Taken Shrapnel Launcher |
| Scorn | Dark-Ether Bellows | `dark_ether_bellows` | Scorn Crossbow |
| Dread | Resonant Tine | `resonant_tine` | Subjugator Glaive |

## Earning and spending parts

A tagged enemy credited to a recent player hit gives one component. An eligible boss gives three. The drop joins the normal mutable death-drop collection, and the server honors the `doMobLoot` rule. Summoned Hive ghosts, Lucent moths, Vex Oracles and Dread Geists have no salvage tag by default.

At either Gunsmith, choose **Destiny → Crafting**. Scroll to the selected weapon to see exactly which three stacks it uses. The seven faction weapons each require four of their faction's part. The six new exotic blueprints each require eight parts. The five new legendary blueprints each require five parts. The two additional ingredients differ by weapon.

Under **Destiny → Perks**, select an owned gun, a socket and a new roll. The 18 new weapons have four barrel, four magazine, four first-trait, four second-trait, five masterwork and five mod options. A change costs one Plundered Ether Alloy and two Copper Ingots, matching the existing economy. Reapplying the current roll consumes nothing. The server verifies interaction with a real gunsmith in reach, and rechecks the slot, eligible roll and inventory balance at the time of the request.

## Editing a blueprint

A server data pack can override `data/destiny2mc/recipe/gunsmith/<weapon_id>.json`. Minecraft 1.21.1 uses the singular `recipe` directory. The result must match the registered weapon and filename. The GUI currently presents three distinct required items with 1–64 of each. Both the recipe serializer and its network codec enforce these limits. Example from the Wire Rifle blueprint:

```json
{
  "type": "destiny2mc:gunsmith",
  "result": "destiny2mc:wire_rifle",
  "costs": [
    { "item": "minecraft:iron_ingot", "count": 10 },
    { "item": "minecraft:amethyst_shard", "count": 4 },
    { "item": "destiny2mc:ether_induction_coil", "count": 4 }
  ]
}
```

Reload the server data pack with `/reload`. RecipeManager supplies the costs used by the server. Minecraft's regular recipe synchronization delivers the same data to the connected client. If a blueprint is absent or its intended output and recipe key disagree, the bench omits its row. Existing worlds retain the first 59 gunsmith recipe IDs. For the appended guns, the wire recipe index is the registered weapon index plus seven, accounting for the older auxiliary recipes.

## Editing salvage and art

Source tags live in `data/destiny2mc/tags/entity_type/salvage/<faction>.json`. A pack can append a registered entity to one tag using `"replace": false`. If packs add an entity to several factions, the fixed order in FactionComponents resolves it once. Additions participate in player kill credit and boss drop sizing just like built-in enemies.

The source generator `build_data.py` holds the blueprint cost and tag tables. `paint_components.py` draws the game textures and the inspection sheet from the same pixel art. Run the two scripts in their source folder after editing these tables or drawings. The former requires Python 3. The latter also requires Pillow. Copy the generated `stage/` contents into the common resource tree and merge the `lang.json` entries into its English language file.

## Perk compatibility

The new common perk asset is `assets/destiny2mc/perks/ageless_pools.json`. It keys each weapon by ID. Its four arrays are barrel, magazine, first trait and second trait. Each array contains four distinct valid IDs in the ranges 101–106, 201–206, 301–306 or 401–406 respectively. The masterwork and mod sockets use the common five-choice pools. A newly registered weapon without an explicit table starts with the first four rolls in each applicable range. Distribute changes to this common asset on both sides.

AdditionalPerkPoolMixin forwards indices 52 and above to the same lookup for rolled stats and UI, because the original legacy implementation internally resolves its first four sockets. Earlier equipment keeps the original lookup and integer slot keys. The required mixin config is `destiny2mc.ageless.gunsmith.mixins.json` in the common mod metadata.

## Earlier Gunsmith verification

The dedicated-server fixture in `tests/fixtures/GunsmithCheck.java` uses actual registries, a survival FakePlayer, the recipe wire codec, player inventory and native mob death events. It checks missing-material rejection, a paid craft of every appended weapon, all six paid socket changes on each gun, rejected invalid perk IDs, actual magazine growth, player reach and bench removal, seven normal salvage drops, a boss drop and the disabled loot rule. [Archived server results](../tests/inherited-4.1/results/gunsmith-server.txt)

The archived multiplayer fixture in `tests/fixtures/SmithScreenCheck.java` used the earlier Craft and Customize buttons. It verifies the received blueprint, the player's replicated Wire Rifle, all five charged input stacks and the selected Backup Mag after the server confirms it. The two frames are captured from Minecraft's own render target. [Archived client results](../tests/inherited-4.1/results/gunsmith-client.txt)
