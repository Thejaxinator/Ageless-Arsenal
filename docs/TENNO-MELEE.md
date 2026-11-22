# Tenno melee

Use the ordinary attack control for a cut. Hold Use to raise the blade. The guard becomes effective after five ticks (0.25 seconds). It covers the horizontal arc shown on the weapon's inspection card and blocks incoming ordinary hits and projectiles inside that arc. The native shield pipeline handles hit origin, armor, shield-bypassing sources, piercing arrows, durability and other mods' shield events. An occupied offhand takes priority over the second blade's visual attachment.

Crouch and strike with at least 90% of the vanilla attack meter for a heavy. A landed charged light cut adds one combo hit. A heavy multiplies its ordinary incoming attack damage by `1.25 + 0.15 × min(3, preceding combo hits)` and spends the chain. Five seconds without a charged hit or attacking with a different weapon resets the chain. Rapid weak cuts can still inflict ordinary Minecraft damage. They earn no combo and trigger no signature effect. The chain only advances on damage to the clicked target. Secondary sweep victims and hits stopped by shields cannot advance it.

| ID | Name / stance | HP | Attacks/s | Reach (blocks) | Full guard arc | Durability | Signature effect |
|---|---|---:|---:|---:|---:|---:|---|
| `amanata` | Amanata · Naginata | 7 | 1.35 | 4.25 | 85° | 512 | Koumei’s Dice. The first charged hit rolls a blessing and each 30 subsequent charged hits reroll it. |
| `hate` | Hate · Scythe | 8 | 1.25 | 3.55 | 60° | 650 | Reaping Spiral. Each third charged light or any landed heavy inflicts three one-HP bleed pulses, one second apart. A new bleed refreshes the three pulses. |
| `harmony` | Harmony · Scythe | 7 | 1.50 | 3.55 | 60° | 625 | Harmonic Resonance. A heavy converts remaining fire, Poison, Wither and Hate bleed into up to eight extra HP on impact, then removes those effects. |
| `heat_sword` | Heat Sword · Sword | 6 | 1.75 | 3.05 | 55° | 400 | Heat Wave. A heavy ignites the target and visible hostile mobs within 2.6 blocks of it for four seconds. |
| `dual_heat_swords` | Dual Heat Swords · Dual Swords | 5.5 | 2.05 | 3.05 | 60° | 460 | Paired Embers. Every second charged light ignites for two seconds. A heavy burns the target and visible hostiles within 1.9 blocks for three seconds. |
| `sun_and_moon` | Sun & Moon · Dual Nikanas | 6.5 | 1.85 | 3.35 | 60° | 675 | Mountain’s Edge. Every second consecutive charged light on the same target adds two HP. Changing targets starts a fresh pair. |

Amanata's five faces grant **Edge** (+20% attack damage), **Frost** (Slowness II for 45 ticks on heavy or third charged hit), **Force** (+2 heavy damage), **Reach** (+0.65 block), and **Tempo** (+0.35 attacks per second). Six grants all five. The roll appears above the hotbar. The current blessing is stored on the player. Reach and Tempo apply only while holding Amanata. These are Minecraft adaptations.

## Extending the framework

The registry and item attributes load the same bundled JSON catalogue on both physical sides at startup. Native registry contents and attributes require a new build and a matching mod version on server and clients. The six definitions are in `src/main/resources/tenno/melee.json`. Each entry declares `id`, `name`, `family`, `damage`, `attackSpeed`, `reach`, `guardAngle`, `durability`, `technique`, `trait`, and `description`. Damage includes the player's one base HP and reach includes their three-block base interaction range. MeleeProfile validates input once. MeleeRegistration and TennoMeleeItem create ordinary sword items with default components and native durability, enchantment and repair behavior.

To add a similar melee weapon, add a row, translation, model, weapon-maker entry and the item ID to `#destiny2mc:warframe/melee`. Choose an existing technique (`dice`, `reap`, `resonance`, `ignition`, `embers` or `crosscut`). To add a new signature mechanic, extend MeleeProfile.Technique and its authoritative event handling in MeleeCombat. Pair display for the existing dual techniques uses a main mesh and an offhand mesh. MeleeHands renders the latter only when the physical offhand is free. Left-handed players receive the mirrored layout. The base item still consumes one inventory slot. GUI and dropped-item views show both blades.

MeleeCombat records the target, swing strength and crouch state at the native attack event, computes damage before mitigation and commits the signature only after positive damage. The server owns combo state, guard limits and spillover targeting. First-person stance interpolation, swing poses and the player's third-person offhand blade reside exclusively on the client. Reaping Cut records the remaining pulses on its target and follows ordinary damage attribution and invulnerability rules.

## Reference and visual direction

Each mesh uses original cuboids and an authored 64×32 material atlas. Reference images informed the choice of mass, blade curvature, handle length, material separation and ornament. No reference image is used as a game texture.

| Weapon | Design cues and reference |
|---|---|
| Amanata | The long red shaft, pale naginata edge, gilded throat and hanging die distinguish Koumei’s weapon. [WARFRAME Wiki](https://wiki.warframe.com/w/Amanata) |
| Hate | Stalker’s wide graphite hook bends down toward the tip. Dark grip rings and restrained red nodes keep the cutting edge clear. [WARFRAME Wiki](https://wiki.warframe.com/w/Hate) · [archived weapon image](https://warframe.fandom.com/wiki/Hate) |
| Harmony | The pale, upswept split blade, rose-metal insets and green resonator separate Jade’s scythe from Hate. [WARFRAME Wiki](https://wiki.warframe.com/w/Harmony) · [weapon image](https://wikiwiki.jp/warframe/HARMONY) |
| Heat Sword and Dual Heat Swords | A stepped vented edge, hot center seam, dark hand guard and the short matching heat dagger distinguish the two versions. [Heat Sword](https://wiki.warframe.com/w/Heat_Sword) · [Dual Heat Swords](https://wiki.warframe.com/w/Dual_Heat_Swords) |
| Sun & Moon | Long and short Duviri nikanas carry warm gold-finished blades, dark wrapped grips and compact swept guards. [WARFRAME Wiki](https://wiki.warframe.com/w/Sun_%26_Moon) |

The reload-safe client model hooks register standalone secondary meshes and the two player-skin layers. Mesh sources are ordinary Minecraft item-model JSON under `assets/destiny2mc/models/item/`. Recreate the meshes and textures with `python tools/build_tenno_blades.py src/main/resources` and Pillow.

## Verification

MeleeFrameworkCheck is an external NeoForge server fixture. It equips each registered item, verifies native attribute application, calls its Use entrypoint and takes real damage from the front, rear, and just beyond the narrow cone. The test checks the guard start delay, durability use, shield-bypassing damage, rejection of attacks while guarding, fully charged attacks, combo spending, target changes and all six signature techniques. Its FakePlayer supplies a full vanilla attack meter when simulating a measured cut and leaves the normal player attack, shield and damage pipelines in charge of the result.
