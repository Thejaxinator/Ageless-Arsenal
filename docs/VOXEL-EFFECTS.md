# Ageless Arsenals visual effects

The effects edition is a complete alternative JAR. Install it in place of the standard Ageless Arsenals JAR on the server and on every client. Both editions retain the saved item, entity and structure namespace `destiny2mc`. The standard edition has the same 70 weapons, encounters and gameplay rules. It uses the engine’s ordinary particle fallback.

## How an effect is drawn

1. The server finishes ray clipping, spawns a projectile, validates a teleport, or enters an existing attack phase. These are the points that produce effects. They are independent of client prediction, camera framerate and the shooter’s connection speed.
2. For a spatial event the server sends one `destiny2mc:voxel_cue` to each player within 96 blocks of the nearest point on the effect. Each message includes the dimension, event type, palette, origin, endpoint offset, radius and random seed. A different dimension or invalid coordinate is discarded. Long paths are capped at 512 blocks.
3. The client keeps transient events on its current world clock. It drops them on expiration, logout and dimension change. Every recipient uses the same seed. This keeps moving shell fragments, zigzags and smoke blocks coherent between frames. Effects have no collision, damage or saved world state.
4. A single batched pass after the world’s particles draws camera-relative, four-sided bars and shaded cuboids. The pass tests terrain depth and writes only color, so smoke covers neither the depth buffer nor objects behind solid terrain. The camera-relative origin preserves precision far from spawn. Each client has a maximum of 160 transient events and 1,350 prisms per world pass. Fresh attack telegraphs draw first under load.
5. Traveling shots draw from their own tracked entity transforms in a separate entity pass. A synced profile and the base engine’s attached bit distinguish an airborne bolt from a planted Telesto mine. No world-effect packets stream a projectile’s position every tick. The profile saves in the entity NBT so loading a chunk restores its look.

## Profiles and timing

| Profile | Appearance |
|---|---|
| `nanite` | A dark red core orbited by three red micro-cubes on Outbreak’s seeking nanites |
| `kinetic` | Alternating gunmetal, grey and silver marks on a narrow tracer |
| `solar` | A hot core inside an amber shell, with orange pulse bands |
| `arc` | A blue-white beam wrapped in an irregular electrical filament |
| `void` | Violet pulse bands and three rotating prongs on physical bolts |
| `stasis` | Cold blue beam, squared ice bands and independently spaced bright chips |
| `strand` | A green filament winding around the core |
| `taken` | An ink-dark shell around a pale center; square ground warnings |
| `soulfire` | Green bone-light, floating chips and a low poison haze |
| `resonance` | Broad amber bands, three-pronged bolts and triangular telegraphs |
| `ether` | Ice-blue electricity mixed with darker, rising smoke blocks on a blink |
| `dark_ether` | A winding mauve filament and hexagonal field boundaries |

When a weapon program supplies an unknown profile, its ordinary elemental palette is used. Legacy kinetic rays live for 5 ticks and energy rays for 7. Impact splinters disperse for 13 ticks. Fallen Captains emit a 17-tick departure and arrival only after a successful blink. Wizard cloud geometry follows the 120-tick spell. Short-lived attack rings are refreshed by their ability’s existing wind-up ticks. Scorn, Taken, Dread and Hive attacks select their faction appearance from their actual owner. Cabal keeps the weapon’s elemental appearance.

## Authoring a new firing mode

Use the base gameplay API once per completed ray:

```java
WeaponVisuals.trace(level, actualStart, clippedImpact, profile, element);
```

For an already spawned `FrontierProjectile`, call `WeaponVisuals.projectile(bolt, profile)`. A projectile with no custom selection inherits its owner’s faction or its element. Core gameplay imports these two neutral hooks from `com.jax.destiny2mc.ageless`. All the drawing code, overlay mixins and the visual payload live exclusively in the optional edition under `com.jax.destiny2mc.ageless.fx`.

Inside this edition, an additional world event can use `VoxelEffects.emit`. Supply one of `TRACE`, `BURST`, `RING`, `BLINK_OUT`, `BLINK_IN` or `CLOUD`. Both endpoints must be world coordinates. For a stationary effect supply the same point twice. The ring radius is in blocks. Add behavior-specific hooks where a server action has already succeeded so decorative effects never fire during a rejected attempt. Friendly fire, line of sight, damage, magazine changes and cooldowns belong to the gameplay code.

To introduce a new palette, append its enum value in `EffectProfile.java`, choose a shape in `EffectShapes.java` and update this table. Enum wire ordinals are shared by both ends. Preserve their order when adding profiles. The custom build’s authoritative tests and screenshots are in the FX fixture directory.


## Build

Use the Gradle wrapper or build.py in this release folder. Its sources and mixin declarations include the visual implementation.
