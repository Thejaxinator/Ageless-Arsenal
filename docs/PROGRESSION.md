# Paracausal progression

The progression edition adds a personal level from 1 through 50. Open the inventory's ability panel to read equipped abilities, career XP, health, armor and the local veteran chance. Character unlocks and active/passive slots work identically in both Binder editions.

## Earning experience

A credited survival/adventure kill of a faction mob awards 12 career XP, a veteran grants 18, and a boss grants 90. Hostiles from the base game or another mod that implement Minecraft's Enemy interface grant 5. The existing loot and XP drops still happen. A credited faction kill also deposits 1 Minecraft XP point immediately, or 5 for a boss. Creative and spectator kills grant neither bonus. Combat XP respects the world's doMobLoot rule. Buying abilities spends vanilla experience levels and Motes of Light; it leaves career points intact.

## Growth

Level 1 begins at zero. If the current level is L, advancing to the next costs 40 + 12 × (L − 1) career points. The total required for level L is 40n + 6n(n − 1), where n = L − 1. Level 50 caps career XP at 16,072. The vanilla XP bonus continues after the cap.

| Level | Lifetime career XP | Max health with standard base | Armor bonus | Nearby veteran chance |
|---|---:|---:|---:|---:|
| 1 | 0 | 20 HP | 0 | 0% |
| 10 | 792 | 29.47 HP | 1 | 5% |
| 20 | 2,812 | 40 HP | 2 | 10% |
| 35 | 8,092 | 50 HP | 3 | 19% |
| 50 | 16,072 | 60 HP | 5 | 28% |

One heart is two HP. The health bonus increases linearly from 0 to 20 between levels 1 and 20, then from 20 to 40 between levels 20 and 50. A level gained adds only the newly unlocked health to the player's current health. Armor gains one point each ten levels. Equipped armor, enchantments and other attribute modifiers continue to apply.

## Veterans

On a natural faction-mob spawn, the nearest living survival/adventure player within 96 blocks supplies the level. The veteran roll is floor(28 × (L − 1) / 49) percent. One natural spawn can become one veteran; this keeps the mob count stable. Named and boss spawns keep their existing tuning. Egg, spawner, sigil, command and scripted encounter spawns skip the natural-spawn roll. Veterans have a visible Veteran name, 25% additional base health, 2 additional armor points and a larger career XP reward. Their existing attacks keep the damage selected in difficulty options.

## Implementation

CareerCurve owns every threshold and bonus. CareerProgress keeps the server-authoritative integer AgelessCareerXp in player persistent data, copies it to fresh player entities on clone, and synchronizes on login, respawn, dimension change and earnings. A progression-only player-load hook restores the health allowance before Minecraft reads the saved health, preserving injured bonus hearts after autosaves, logouts and server restarts. Stable transient player attribute IDs prevent stacking and let players change back to the standard edition. A character's XP balance survives the edition switch when retained by Minecraft's player data. Veterans save their earned rank and modifiers with their own entity. CareerSync sends only the points total, from server to client. BinderProgressView bridges the optional readout to the base inventory panel.

CareerCurveCheck checks all 16,073 attainable XP totals under Java 21. CareerServerCheck is an isolated native fixture intended for a disposable NeoForge server using the progression edition.
