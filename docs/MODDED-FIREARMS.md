# Warframe firearm mod effects

An installed mod is active at the current wielder’s unlocked rank. The same fitted gun can perform differently when handed to a friend. The right side of the Warframe inspection card and the combat HUD use the same live statistics as the server. A clean firearm uses its existing firing behavior and profile values.

## Damage and ammunition

Damage equals the base attack times `(1 + base damage bonus) × (1 + Heat bonus + Cold bonus)`. This factor scales shotgun patterns, beams and their chains, returning-blade attacks, glaive bounces, viral explosions and toxin-cloud ticks. The original weapon’s weak-point, falloff, intrinsic status and target armor rules still apply. Damaging a target rolls for any socketed Heat or Cold status. Its probability equals the elemental bonus, capped at one. Heat ignites the victim for three seconds. Cold applies Slowness I for three seconds. For a shotgun, pellets hitting the same victim are batched into one damage and status application per discharge. A cloud rolls on each damaging pulse.

Chance-based multishot is independent for each original projectile. A rank-five Vigilante Armaments gives each original round or pellet a 60% chance to generate another. A complete six-pellet Cedo pattern thus emits six to twelve pellets. One paid round can create multiple independently moving thrown blades, spore grenades, glaives or viral projectiles. Each additional path can hit its own target, and the extra shots are free. Paired pistols calculate multishot for both muzzles. The Ignis resolves extra stream damage against its existing cone. Perigale counts actual weak-point contacts, including multishot, toward Gale Force.

The capacity modifier affects rounds in a magazine or battery. Capacity is `round(base capacity × (1 + magazine bonus))` with a minimum of one. Installing a magazine card opens extra space. A reload or normal battery recharge fills it. A borrowed weapon only exposes as many stored rounds as the current wielder’s active mods support.

| Braton example | Result |
|---|---|
| Stock | 3.3 base HP, 45-round magazine, 40-tick reload |
| Rank-five Serration | 5.28 base HP |
| Rank-five Magazine Warp | 67-round magazine |
| Rank-five Fast Hands | 25-tick reload |
| Rank-five Speed Trigger | 15 rounds across the first 30 ticks of sustained fire, compared with 10 stock |

## Timing and projectiles

The server schedules firing intervals as `base ticks / (1 + fire-rate bonus)` with a one-tick minimum. Fractional progress carries to the next trigger during sustained fire, letting small rank upgrades influence fast automatic weapons. The shot queue applies the same adjustment to a weapon’s intra-burst cadence. Magazine reload and Bubonico battery recovery durations divide their original lengths by `1 + reload-speed bonus` and round to the nearest tick (minimum one). At maximum ranks, Coda Bubonico with Ammo Stock holds 58 charges, and Tactical Pump shortens its normal 30-tick regeneration wait to 19 ticks.

Hitscan resolves the current physical weapon and current player on the authoritative server. Projectiles and their lingering effects snapshot the launch bonuses. Reloading Cantare while holding the original weapon recalls its knives with their original launch damage. Changing sockets or gifting the weapon during flight leaves those pending attacks intact. No mod stacks are consumed by firing.

## Source and testing

`com.jax.destiny2mc.tenno.TennoGunStats.of(Player,ItemStack)` resolves current values from the collection and installed sockets. Its `damage()` already includes elemental damage. Its `multishot()` is a bonus fraction. `magazine()` reports maximum capacity, and `TennoGunItem.magazine(Player,ItemStack)` reports rounds available to that player. The native firearm test opens real packs, ranks mods with XP levels, equips them on registered guns, and measures actual projectiles, statuses, target health and magazine timing in a dedicated server world.
