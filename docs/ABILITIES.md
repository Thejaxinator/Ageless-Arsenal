# Active Energy Binder forms

The Binder ships 30 active forms. An active slot can hold a grenade, melee, or class ability in any combination. Unlock prices are paid once per character. Listed cooldowns are per ability ID and use server time at 20 ticks per second. “Prismatic” marks crossover abilities. Prism Cleave and Storm Snare are original forms for Ageless Arsenals.

| Element | Grenades | Melee |
|---|---|---|
| Solar | Solar Grenade leaves a burning field. Healing Grenade heals 5 HP and grants regeneration in a five-block radius. | Incinerator Snap scorches a close cone. Throwing Hammer returns healing light when its impact is within 20 blocks and the return path is clear. |
| Arc | Pulse Grenade strikes in four pulses and chains from the primary target. Flashbang disorients for 65 ticks and briefly slows movement. | Chain Lightning arcs through up to three enemies. Thunderclap releases a seven-block directional shock wave after a ten-tick windup. |
| Void | Vortex Grenade damages and pulls with five pulses. Suppressor Grenade silences outgoing damage for 40 ticks and applies weakness for 110. | Shield Throw ricochets between up to three enemies and grants absorption. Pocket Singularity pushes targets away from its impact point. |
| Stasis | Duskfield grows from 2.5 blocks, building slowness and frozen ticks. Coldsnap seeks up to three visible enemies in sequence. | Shiver Strike lunges forward and deals 11 HP against chilled targets or 6 otherwise. Penumbral Blast inflicts a close-radius freeze. |
| Strand | Shackle briefly suspends nearby enemies. Threadling Grenade releases three independently steered seekers. | Threaded Spike ricochets through up to four different targets and weakens them. Arcane Needle homes towards the reticle target and unravels it twice after impact. |
| Prismatic | Freezing Singularity pulls with Void and bursts with Stasis. Hailfire Spike freezes on impact, followed by a Solar explosion 19 ticks later. | Prism Cleave chills in a cone and follows with Solar damage after 12 ticks. Storm Snare lifts the primary target and chains Arc to nearby enemies. |

## Class abilities

- **Marksman's Dodge** dashes in the current movement direction. At rest, it uses the look direction projected onto the ground. It advances the held gun's normal reload completion clock. Special ammo, available reserves, perk completion hooks and custom capacitor feeds follow their regular rules.
- **Gambler's Dodge** dashes and refreshes the first equipped, cooling melee when a visible enemy is within seven blocks.
- **Healing Rift** heals friendly Guardians inside a 3.2-block circle every ten ticks for eight seconds. **Empowering Rift** grants 25% additional outgoing damage to Guardians standing in the circle for eight seconds. Overlapping empowering rifts apply the multiplier once. Both use line of sight.
- **Towering Barricade** covers a 5 × 2.85-block plane for eight seconds. **Rally Barricade** covers a 5 × 1.35-block plane for seven seconds and accelerates the normal reload while crouched behind it. Incoming projectiles and directed attacks must cross the plane to be blocked. Shoot out through the back. Opponents crossing the plane experience a short slow. These projections never place blocks or overwrite structures.

## Integration

`ActiveAbilities.catalog()` returns immutable entries shared by purchasing, keybinds, menus and previews. The action enum is the canonical source for purchase cost and cooldown. A new action needs a stable identifier, visible metadata and a handler in `AbilityCombat` or `AbilityWorld`. Player input flows through the Binder ledger before `cast` starts a simulation. A cast rejected for an invalid target does not consume its cooldown.

A grenade is a swept volume with ballistic drop. Its server position advances one game tick at a time and stops at the first intersected entity or solid voxel. The simulation sends the Voxel Effects renderer ribbons, rings and bursts. Temporary fields and flights expire on owner death, logout, dimension changes or chunk unload. Ownership and hit queries use server UUIDs and the game's team/PvP rules. Elemental strikes mark the victim's persistent data before calling damage so a same-tick defeat can activate a subclass passive.
