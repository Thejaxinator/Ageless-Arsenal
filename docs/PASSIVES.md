# Guardian passives

A character can equip any two different passives. Movement upgrades and subclass perks use the same two sockets. Equip order determines which movement style fires on the next airborne press. For example, Triple Jump followed by Strafe Lift gives a jump, a lift and a last jump before landing. The ordinary ground jump always works. Hold the rebound Minecraft Jump button to sustain a lift or glide. Release it early to cut power. Directions follow the movement keys held at activation and the player’s current facing.

| Passive | Motes | Vanilla XP levels | Effect |
|---|---:|---:|---|
| Triple Jump | 12 | 3 | Two extra airborne taps per trip. Each imparts 0.45 blocks/tick of vertical velocity. |
| Strafe Lift | 12 | 3 | One lift per trip, up to 12 ticks of powered climbing with a 2.5-block powered height target. |
| Burst Glide | 12 | 3 | One burst per trip with up to 24 ticks of slow descent. It defaults to forward travel when no movement key is down. |
| Ember of Solace | 16 | 4 | A burning-target kill or a direct Solar ability kill heals 3 HP. Four-second cooldown. |
| Spark of Haste | 14 | 3 | One full second of continuous sprinting grants Speed I. The short buff refreshes as long as the sprint continues. |
| Echo of Reprisal | 18 | 4 | A crouching kill gives three seconds of invisibility, and mobs targeting the player within eight blocks lose that target. Eight-second cooldown. |
| Whisper of Chains | 16 | 4 | Resistance I refreshes near a slowed hostile within six blocks. The effect wears off shortly after the player leaves its radius. |
| Thread of Warding | 16 | 4 | Collecting an experience orb grants up to two absorption hearts for six seconds. Twelve-second cooldown. |

## Extending a passive

Add a stable ID to the passive catalog. Buff hooks check acknowledged passive ownership and the equipped slots on the server before applying an effect. Existing potion effects use Minecraft’s normal amplifier/duration precedence. The dedicated server accepts only a Jump press or release and digital direction inputs. It chooses the movement style, checks the remaining airborne budget and produces the velocity and voxel cue. The per-style budget survives slot changes until landing and resets when changing dimension. Proc cooldowns are stored as absolute game times in persistent player data. The two passive sockets never require another ability from the same element or class.
