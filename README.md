# Ageless Arsenals 6.3.1 · Progression

Includes the Tenno Arsenal, Energy Binder and character leveling. Install on client and server with Minecraft 1.21.1, NeoForge 21.1.266 and Java 21. Choose one edition per installation. Existing worlds retain their registry namespace.

## Gunsmith and creative inventory

Right-click either Gunsmith. Each visit opens on **Destiny → Crafting**. Select a franchise across the top, then choose a tool underneath.

| Franchise | First tool | Second tool |
| --- | --- | --- |
| Destiny | **Crafting** builds Destiny guns from salvage and materials. | **Perks** changes a socket on a gun you own. |
| Warframe | **Blueprints** assembles ranged and melee weapons. | **Weapon Mods** installs, removes and ranks cards. |

**Tab** or **Shift+Tab** switches franchise. **[** or **]** switches tools within the selected franchise. **M** also switches tools while Warframe is selected. Switching franchises recalls the last tool you used in each during this visit. **Esc** closes the Gunsmith.

In Creative, **Paracausal** holds the Destiny items, armor, spawn eggs and stations. The **Warframe** tab follows it and uses Twin Grakatas as its icon. It lists 19 primary weapons, eight secondary weapons, six melee weapons and four mod packs, in that order. Minecraft's Search tab also finds these items. The creative tab IDs are `destiny2mc:arsenal` and `destiny2mc:warframe`, respectively.

[Current in-game Gunsmith screens](docs/previews/navigation/) · [Navigation test results](tests/navigation/results/)

## Weapon mods

Craft an Arsenal Mod Pack with two paper, one copper ingot and one redstone dust. Hold it and press Use to draw a new card. Open **Warframe → Weapon Mods** at either Gunsmith to fit cards to a specific weapon or buy ranks with Minecraft XP levels. All 33 Warframe arms support mods.

[Player guide](docs/MOD-FUSION.md) · [22-card reference](docs/previews/mod-fusion/Ageless-Arsenals-Mod-Catalog.png) · [Earlier Mod Fusion test results](tests/mod-fusion/results/)

## Warframe Arsenal

Open either Gunsmith and select **Warframe → Blueprints**. Choose a ranged or melee blueprint, inspect the listed materials, then assemble. Try a Heat Sword first with three iron, one coal and one leather. Hold Use to block, attack for a cut, or crouch for a charged heavy. With a firearm, use Attack to fire, Use to aim, R to reload, and G for alternate fire on supported weapons. The Controls menu can rebind both keys.

The roster contains 27 ranged and thrown weapons, six melee weapons and the previous 70 Destiny guns. Each Warframe item uses its own inspection card. The Foundry lists the prices in your world.

- [Illustrated offline roster](docs/WARFRAME-FIELD-GUIDE.html) and [plain Markdown list](docs/WARFRAME-ROSTER.md)
- [Controls and ranged mechanics](docs/WARFRAME-FIREARMS.md), [melee mechanics](docs/TENNO-MELEE.md), and [Foundry UI](docs/WARFRAME-FOUNDRY.md)
- [Crafting and tag reference](docs/TENNO-BLUEPRINTS.md), [model references A](docs/TENNO-RECEIVER-ART.md) and [model references B](docs/WARFRAME-FIELD-ART.md)
- [Earlier Tenno Arsenal verification records](tests/tenno/results/) and [test instructions](tests/README.md)

## Getting started

Collect Motes of Light from defeated Ageless Arsenals enemies. The first mote unlocks the workstation recipe in the recipe book. Arrange the following three rows at a crafting table:

| | | |
|---|---|---|
| Empty | Amethyst shard | Empty |
| Copper ingot | Mote of Light | Copper ingot |
| Smooth stone | Iron ingot | Smooth stone |

Use the Energy Binder to buy an ability with motes and Minecraft experience levels, then assign its icon to a slot. Your four active slots accept every active type. Your two passive slots accept jumps or subclass buffs. The active keys start at Z, X, C and V. Rebind them in Minecraft Controls. Airborne movement uses your normal Jump key.

Permanent unlocks, assignments and cooldowns belong to your character. Click the small chevron on the inventory’s left rail to slide open the ability drawer. The world difficulty controls incoming damage from Ageless Arsenals enemies. Open Difficulty at the Binder. The server controls the shared setting.

## Source

Build this complete release using `./gradlew -PpythonExe=python3 assemble` or `gradlew.bat -PpythonExe=python assemble`. Output goes into `build/libs`. Java 21 and Python 3.10+ are required. For a pre-resolved NeoForge development classpath, use `python build.py --classpath-file platform-classpath.txt --javac /path/to/javac`.

The Java implementation is under `src/main/java/com/jax/destiny2mc/binder`. Runtime assets and the recipe live in `src/main/resources`. Consult `docs/` for the mechanics and authoring notes and `tests/` for the isolated test fixtures. The previous release's armory and model workflow is preserved in `docs/INHERITED-ARMORY.md`.

## Release guide

Regular enemies drop 1 Mote of Light and bosses drop 6 when mob loot is enabled. Compare the [30 active forms and prices](docs/ACTIVE-CATALOG.md), [combat mechanics](docs/ABILITIES.md), [eight passives](docs/PASSIVES.md), and [difficulty presets](docs/ENCOUNTER-DIFFICULTY.md). Destiny items appear in the **Paracausal** creative tab. The default difficulty is **Patrol**. The world owner or an operator can change it at the workstation or through `/ageless difficulty`.

[Career progression](docs/PROGRESSION.md) grants XP for credited enemy kills, with 40 max HP at level 20 and 60 at level 50 for a standard player. The cap is +5 armor. Nearby natural spawns can become veterans at up to a 28% chance. Spending Minecraft levels at the workstation leaves career XP intact.

## HUD patch

Open the ordinary inventory and click the small chevron at its left edge. The drawer keeps the current Minecraft container interactive and remembers your preference. On narrow windows the drawer switches to icons, with full descriptions on hover.

Press **O** to open HUD & recoil settings. The six themes apply together to the ammo counter and four active skills underneath it. The separate **Ability HUD** setting toggles combat visibility. The preview uses the same renderer as gameplay. New profiles start on Destiny 2 FUI. Previously saved preferences and character unlocks carry forward. The same 38 individual ability and passive glyphs appear in the shop and drawer. The source includes the [skin guide](docs/HUD-STYLES.md), [drawer controls](docs/INVENTORY.md), [icon artwork](docs/ABILITY-GLYPHS.md) and native captures in `docs/previews/combat-hud/`.


## Raid armor and inspection

Kabr, Willbreaker and Rull each have four wearable, repairable pieces. Any two equipped pieces of the same set activate its defensive trait. A player can start the Kabr set by defeating two Vex Goblins and crafting the helmet and boots with five iron ingots, two Conduit Lenses and two Motes of Light. The recipe book reveals the set when its salvage is acquired. Helm of Saint-14, The Armamentarium and Mask of Bakris have their own recipes and survival effects.

The inventory drawer’s chevron opens and closes it, follows its edge, and supports keyboard focus. Hover an ability at the Binder or in the drawer to inspect its own element, icon, power and equipped key or cost. Destiny weapons, including Gunsmith blueprints and owned rolls, and all 15 armor pieces use the same inspection design. Hold Shift on an item to read its full Minecraft tooltip. In Destiny, Compact, Tenno and Borderlands 4 HUDs the abilities sit centered below the weapon card. Minimal and Borderlands 2 keep their left-aligned rows.

- [Armor recipes, statistics and exotic effects](docs/SURVIVAL-ARMOR.md)
- [Wearable models and art references](docs/ARMOR-ART.md)
- [Inspection cards](docs/INSPECTION-CARDS.md) and [inventory controls](docs/INVENTORY.md)
- Native game screenshots and the armor lineup are in `docs/previews/gear/`.
