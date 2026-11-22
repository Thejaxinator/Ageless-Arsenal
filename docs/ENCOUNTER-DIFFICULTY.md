# Encounter difficulty

Open **Encounter difficulty** from the Energy Binder or type `/ageless difficulty`. Select a preset to inspect its values, then choose **Apply to world**. A singleplayer owner or permission-level-two server operator can apply a change. Anyone can inspect the active preset. Each world starts on **Patrol**.

| Preset | Regular enemy damage | Boss damage | Regular hit cap | Boss hit cap | Rapid follow-up multiplier |
|---|---:|---:|---:|---:|---:|
| Explorer | 24% | 28% | 3 HP | 4 HP | 50% |
| Patrol | 38% | 42% | 4 HP | 6 HP | 65% |
| Veteran | 68% | 72% | 7 HP | 10 HP | 85% |
| Original | 100% | 100% | Unlimited | Unlimited | 100% |

One vanilla heart equals two hit points. The server applies the selected multiplier to incoming damage attributed to an Ageless Arsenals enemy, then limits it to the corresponding hit cap. A second accepted enemy impact within ten game ticks of a previous enemy hit receives the follow-up multiplier, including fire from a different enemy. Shields, armor, toughness, Resistance, Protection, absorption and ordinary hit immunity are processed by Minecraft. A hit that costs zero health does not start the follow-up window.

For a normal-difficulty 4.5 HP Dreg shock bolt, Patrol gives **1.71 HP** before armor and **1.1115 HP** for an eligible rapid follow-up. A 10 HP Valus Ta’aurc melee hit becomes **4.2 HP** before armor or enchantments. The 6 HP boss cap applies even when a heavy slam would normally exceed it. Boss telegraphs, reloads, health, AI, drops and encounter objectives retain their existing mechanics. Players still need to move out of repeated area attacks.

## Ownership and world saves

`EnemyDamage.java` checks the damage source’s attacking entity, including owners of projectiles and area hazards. Only damage received by a server player from a mod enemy qualifies. Eight enemy-exclusive legacy bolt types also qualify after losing an owner. New player weapons, friendly fire, falls, lava, vanilla monsters, and status ailments with independent vanilla damage sources follow the normal game rules. Keep new enemy hazards attributed to their caster with `indirectMagic` or an equivalent owner-aware damage source to pick up encounter tuning automatically.

`EncounterSettings` stores one stable string key and a revision in the Overworld data store as `ageless_encounter_settings.dat`. Every dimension reads that same object. `DifficultyNetwork` validates both edit permission and revision on the server. A stale or unauthorized edit returns the current setting. Successful changes immediately apply across dimensions, and the current state is sent to connected clients. The short follow-up window lives on the server and expires when its player object is discarded.

## Testing

`tests/fixtures/EncounterFormulaCheck.java` checks the arithmetic independently of a Minecraft launch. Compile it alongside `EncounterPreset.java` with Java 21 and run `EncounterFormulaCheck`.

`tests/fixtures/EncounterNativeCheck.java` is a test-only `ServerStartedEvent` subscriber. Compile it against the production JAR and the NeoForge development runtime, package it as a separate test mod with mod ID `encounter_check`, and start a dedicated server with both JARs in a new disposable world. The fixture creates an operator, changes the preset and reports `ENCOUNTER COMPLETE` after all assertions pass.

On Minecraft 1.21.1 and NeoForge 21.1.266, the dedicated server measured 1.7100 HP for the first unarmored Dreg bolt and 1.1115 HP for a rapid second bolt on Patrol. Valus Ta’aurc’s 10 HP basic melee hit dealt 0.3532 HP to a player wearing full Protection IV netherite on Patrol, compared with 1.0080 HP on Original. These measurements use Normal world difficulty.
