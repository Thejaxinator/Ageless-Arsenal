# Survival armor

Every piece equips in an ordinary Minecraft armor slot. Any character can wear any piece. The three raid sets use the helmet, cuirass, waist mark and greaves from their original designs. The cuirass includes both gauntlets. A Titan mark occupies the leggings slot and renders across the waist and upper legs. Head and chest exotics can be worn together.

## Starting a set

A player-credited Vex Goblin kill supplies a **Conduit Lens** and a **Mote of Light** while mob loot is enabled. Two such kills and **five iron ingots** pay for the Battlecage of Kabr and Kabr’s Forceful Greaves. Wear them together for projectile protection. Two Hive Thralls and five iron can instead produce the equivalent Willbreaker pair for protection from hostile melee. The equivalent Cabal pair uses Breech Seals. Acquiring the salvage unlocks the four recipes in the crafting book.

| Set | Faction salvage | Helmet | Chest | Leggings | Boots | Two-piece trait |
|---|---|---|---|---|---|---|
| Kabr Suit | Conduit Lens | Battlecage of Kabr | Kabr's Wrath | Light of the Great Prism | Kabr's Forceful Greaves | Glass Lattice reduces incoming projectile damage by 18%. |
| Willbreaker | Runed Marrow | Willbreaker's Watch | Willbreaker's Resolve | Mark of the Pit | Willbreaker's Greaves | Sword Logic reduces direct melee damage from hostile creatures by 18%. |
| Rull Suit | Breech Seal | Mask of Rull | Chassis of Rull | Shadow's Mark | Greaves of Rull | Siege Plating reduces incoming explosion damage by 25%. |

Each raid piece costs one matching salvage item, one Mote of Light and iron. The helmet takes three ingots, chest six, leggings five and boots two. A full set costs 16 iron, four salvage and four motes. Player-credited Vex, Hive and Cabal boss kills also drop one uniformly selected piece from the corresponding set while mob loot is enabled.

| Set | Defense H / C / L / F | Durability H / C / L / F | Toughness per piece | Knockback resistance per piece |
|---|---|---|---:|---:|
| Kabr | 2 / 6 / 5 / 2 | 187 / 272 / 255 / 221 | 0.5 | 0 |
| Willbreaker | 3 / 6 / 5 / 2 | 209 / 304 / 285 / 247 | 1 | 0 |
| Rull | 3 / 7 / 5 / 3 | 242 / 352 / 330 / 286 | 1 | 0.025 |

The defensive trait requires two **equipped** pieces from the same set. It applies once per hit. An explosion carried by a projectile checks the blast trait. Projectile hits check Kabr, and normal monster melee checks Willbreaker. Armor-bypassing damage retains its normal behavior. Armor points, toughness, protection enchantments, absorption and normal hit immunity all use Minecraft’s damage pipeline. Each raid piece repairs with its own faction salvage at an anvil. The native armor and enchantable tags support armor enchants and trims.

## Exotics

Every exotic recipe appears after collecting a **Conquest Token**. Arrange accent, token, accent on the first row. Place salvage, vanilla armor, salvage on the second row and a single Mote of Light in the bottom center. Each piece repairs at an anvil with the salvage in its recipe.

| Exotic | Center armor | Accent ×2 | Salvage ×2 | Defense / toughness / durability |
|---|---|---|---|---|
| Helm of Saint-14 | Iron helmet | Amethyst shard | Conduit Lens | 3 / 2 / 330 |
| The Armamentarium | Iron chestplate | Gunpowder | Breech Seal | 8 / 2 / 480 |
| Mask of Bakris | Iron helmet | Ender pearl | Ether Induction Coil | 3 / 2 / 330 |

**Starless Night.** Successfully block a hit with a shield or cast either Binder barricade. The helm grants Absorption I for five seconds (two hearts), drops nearby hostiles’ target lock on the wearer, and applies Blindness I and Slowness II to visible hostiles within 4.5 blocks for three seconds. Both triggers share a 12-second recharge.

**And Another Thing.** The Armamentarium pays for one bonus Binder grenade every 40 seconds. The bonus cast leaves the grenade’s HUD slot ready. The second cast starts its normal cooldown. All grenade elements, copies of the chestplate and duplicate slot bindings share the same reserve. A charged reserve is consumed on a successful cast of a ready grenade. The normal grenade clock continues even if the chestplate is removed.

**Light Shift.** Either Binder dodge moves the wearer up to five blocks in the dodge direction. The server sweeps the player's full collision box in quarter-block steps, stops before solid blocks, the world border or unloaded terrain, and applies Slowness II for three seconds to visible hostiles within 3.5 blocks of the landing position. A blocked shift retains the ordinary dodge. The dodge uses its normal Binder cooldown.

## Provenance and data

The collection names and design lineage come from the original Destiny armor. The survival recipes, progression tiers and set/exotic adaptations above are Ageless Arsenals mechanics. [Kabr Suit](https://www.destinypedia.com/Kabr_Suit) · [Willbreaker](https://www.destinypedia.com/Willbreaker_(armor)) · [Rull Suit](https://www.destinypedia.com/Rull_Suit) · [Helm of Saint-14](https://www.destinypedia.com/Helm_of_Saint-14) · [Armamentarium](https://www.destinypedia.com/Armamentarium) · [Mask of Bakris](https://www.destinypedia.com/Mask_of_Bakris)

`com.jax.destiny2mc.armor.ArmorPiece` owns the IDs, display information, suits, material and defensive attributes. `ArmorContent` registers six materials and 15 items. `ArmorEffects` resolves authoritative exotic and set effects. `ArmorDrops` supplements the existing cancellable death-drop event and uses the same faction tags as weapon salvage. The two timed exotic reserves are stored under `AgelessArmorClocks` in player persistent NBT with absolute Overworld game ticks. Their deadlines survive death, relog and equipment changes. The server commits the normal ability cooldown and charges exotic reserves only after a successful cast.

Recipes live in `data/destiny2mc/recipe/<item_id>.json`; discovery conditions live in `data/destiny2mc/advancement/recipes/armor/<item_id>.json`. Registry IDs have the `destiny2mc` namespace, are lowercase, omit apostrophes, and use underscores for spaces and hyphens. Example IDs are `destiny2mc:helm_of_saint_14` and `destiny2mc:light_of_the_great_prism`.

## Native validation

In a Normal world on the Patrol preset, a 4.5 HP Dreg shock bolt dealt **1.710 HP** to an unarmored player, **1.628 HP** to the same build with just Battlecage of Kabr, and **1.213 HP** with the Battlecage plus Kabr’s Forceful Greaves. One Minecraft heart is two HP. These figures are for a fresh impact with unenchanted items.

`ArmorSurvivalCheck.java` runs as a separate QA mod (`armor_survival_check`) on a disposable NeoForge server. It resolves and assembles the crafting recipes using Minecraft's recipe manager, equips all 15 pieces via their ordinary use action, exercises both Binder and vanilla-shield exotic triggers, and checks collision and world-border limits as well as faction drops. The fixture’s synthetic player uses vanilla’s server teleport listener on a local transport; NeoForge's normal FakePlayer listener intentionally discards movement packets.
