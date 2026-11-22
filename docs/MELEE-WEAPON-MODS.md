# Warframe melee mods

Install melee cards on a specific Warframe blade at the Gunsmith. That blade carries its socket assignments. The wielder supplies the ranks. A borrowed blade uses only the cards its new wielder has unlocked. All six original weapons still guard with Use, which defaults to holding right-click. The guard needs five ticks to become active and uses the weapon's original frontal arc.

Each unlocked card has one increment at rank zero. Use `steps = rank + 1` for the formulas below.

| Card | Effect in Ageless Arsenals |
|---|---|
| Pressure Point | Adds 10% of the blade’s base damage per step to the native attack-damage attribute. |
| Fury | Adds 6% of the blade’s base swings per second per step. The native attack meter recharges at the resulting speed. |
| Reach | Adds 0.20 blocks of actual entity interaction range per step. The guard angle and block interaction range stay at their normal values. |
| Killing Blow | Multiplies a charged crouching heavy by `1 + 0.12 × steps`, after the existing combo multiplier. |
| Molten Impact | Adds 8% contact Heat per step against targets susceptible to fire. A charged cut that deals damage ignites the target for `ceil(0.08 × steps × 5)` seconds. Fire immunity and Fire Resistance prevent these additions. Harmony first consumes pre-hit statuses, then applies the new burn. |
| Parry | A frontal, successful native block has a 10% chance per step to stagger a direct melee attacker with Slowness III for two seconds. Arrows and bolts remain blockable. They do not stagger their shooter. |

For example, an unmodified Hate has 8 base damage, 1.25 swings per second and 3.55 blocks of reach. With rank-zero Pressure Point, rank-two Fury and rank-three Reach, it has **8.8 base damage**, **1.475 swings per second** and **4.35 blocks of reach**. These are pre-armor values. The original combos, heavy multipliers and signature techniques use the resulting attack.

The server uses transient attribute modifiers for damage, swing speed and reach. The character's active stack and collection are resolved on each equipment tick, and once more when an attack starts. Holstering the weapon or switching to an unmodded duplicate removes the bonuses. A weapon stack saved in a chest records its cards without storing any previous owner's effective attributes.

`MeleeModStats.of(player,stack)` returns base damage, swing speed, attack reach, conditional Heat, bonus heavy damage and Parry chance for the inspection card. Percentage bonuses are fractions in this API. Combat applies the same stat definitions through `WeaponMods`.
