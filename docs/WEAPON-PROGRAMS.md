# Ageless Arsenals weapon authoring

## Try a different firing system

Copy the included `examples/repulsion-pack` folder into a test world's `datapacks/` directory. With the server running, use `/reload`. Equip `destiny2mc:hung_jury_sr4`, hold fire for four ticks, then release. The rifle now emits three physical bolts that pull living targets toward the shooter. The trigger spends two capacitor units. A unit returns every six ticks while carried after the last shot, including in a holstered slot. Removing the pack and reloading restores its ordinary single-shot scout behavior. Its figure, input bindings, scope, ammo stack and existing perks carry over.

## How a shot is assembled

The registration manifest at `data/destiny2mc/armory_manifest.json` supplies the eighteen additions, their fixed identity and base stats. Every program uses the regular `DestinyGunItem`. IDs 0–51 retain their previous registry order and saved stack components. New guns occupy 52–69. IDs in worlds continue to use the stable `destiny2mc` namespace. To append another registered item, add a manifest row, a matching program, model parts, sight/grip fit, material recipe and language entry, rebuild the mod, and restart both sides. A live datapack can change the firing behavior of any of the 70 registered guns. Damage, cadence and capacities in the shipped definitions were tuned for Minecraft’s 20-tick combat simulation.

A program at `data/destiny2mc/weapon_programs/<item-id>.json` replaces that item's firing sequence. Minecraft chooses the highest-priority file for each ID. Programs are parsed into a fresh immutable map at server reload. Invalid syntax, unknown names, unsupported combinations and out-of-bounds values reject the reload. Clients receive validated programs on join and on successful reload. An integrated server uses independent client and server maps. The server checks the actual held item, cadence, magazine, charge heartbeat, active hand and living player before advancing a shot.

A trigger can schedule several **rounds**. Each round consumes its configured cost in magazine rounds or capacitor units. A round can then emit multiple **pellets**, each of which performs an independent ray test or owns a separately tracked game entity. Distinct burst, volley and cooldown stages are important for ammo, recoil, delay, multi-hit effects and other players' view of the weapon. All time values are server ticks at 20 ticks per second. Distances are blocks and speeds are blocks per tick.

| Field | Allowed values or bounds | Meaning |
|---|---|---|
| `version` | `1`, required | Schema version |
| `trigger` | `press`, `automatic`, `charge`, `release` | Press takes one input, automatic repeats held fire, charge fires on reaching the threshold, release fires when a sufficiently charged input ends |
| `charge_ticks` | 1–200 for charge/release, otherwise 0 | Charge requires periodic held input and cancels when the item is holstered or reloaded |
| `cooldown` | 1–600, default 6 | Minimum time from accepted trigger to next trigger |
| `feed`, `cost`, `recharge_ticks` | `magazine` or `capacitor`, 1–128 units per round within base capacity, 1–1200 ticks per unit | Magazine feed uses ordinary reloads and ammo bricks. A capacitor ignores manual reload and regenerates after a delay from the most recent shot. A capacitor has the same effective capacity as the magazine it replaces |
| `burst`, `interval` | 1–12 rounds, 1–40 ticks between rounds | Pending bursts stop when the player changes held item or runs out of ammo. The final round must precede the next trigger |
| `delivery` | `ray` or `projectile` | Immediate world-clipped ray, or a persistent entity with collision and flight |
| `count` | 1–16, default 1 | Independent pellets per round |
| `pattern`, `angle` | `cone`, `fan`, `ring` with half-angle 0–45 degrees | Cone samples a disk. Fan has evenly spaced horizontal paths. Ring has a center pellet plus an evenly spaced rim. Aim spread rotates the formation as a unit |
| `damage_scale`, `last_mult` | 0.01–10, 0.1–5 | Per-pellet scale of manifest base damage and additional multiplier for the final round of a burst |
| `fx` | Short lowercase profile ID, default `kinetic` | Optional renderer style. Base build uses the weapon's element for its fallback tracer |
| `pierce` | 1–8, rays only | Maximum eligible living targets in distance order before the first blocking voxel |
| `on_hit` | `none`, `swarm`, `collapse`, `conductor`, `precision_stack`, `unarmored_precision`, `anti_armor`, `refund`, `master` | Optional ray reaction defined in ProgramTraits. Secondary hits bypass reactions to avoid recursive chains |
| `flight` | `bolt`, `sticky`, `seeker`, `rocket`, `blight`, `arrow`, `frost`, `pull`, `dark`, `lob` | Physical projectile and impact family |
| `speed`, `radius` | 0.05–5 blocks/tick, 0–8 blocks | Projectile launch speed and optional splash radius. Splash checks visibility and allegiance |
 
## Source map

- `PackagedWeapons` loads new base stats before the item registry freezes. `WeaponPrograms` owns datapack reload and immutable snapshots. `ProgramSync` transports the result server to client.
- `ProgramInput` handles first-person intent, recoil and local charge feedback. `ProgramBallistics` schedules charges and bursts, clamps aim to the server's current look direction, raycasts, clips to walls and applies damage. Trigger requests use the existing fire/reload/hold/release packet.
- `FrontierBallistics` routes profiled weapons through this pipeline. Untouched weapons retain their specialized legacy firing path. The program reuses shared magazine, reload, perk, and feedback methods.
- `FrontierProjectile` owns travel, collision, element, owner and saved state. Sticky charges anchor at the impact point, follow an attached living target, save their fuse and anchor, and detonate independently. Telesto emits one center bolt and six outer bolts, separated by a 4.7° half-angle.
- `WeaponVisuals.trace` receives the actual ray endpoints. `WeaponVisuals.projectile` tags the real entity with its profile. Either build can run the complete game simulation. New presentation code can draw extra geometry without performing an additional damage query.
- `ProgramTraits` contains named hit reactions selected by JSON. Add enum values and implementations here when a future author needs genuinely new mechanics. Append flight IDs to preserve existing persisted entity mode values.
- `qa/AgelessWeaponCheck.java` validates all registered item identities, every added trigger, Telesto flight directions and saved attachments, burst timing, holster cancellation, magazine refill and a live external datapack override on a disposable dedicated server.
