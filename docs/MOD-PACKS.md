# Mod packs and Fusion

Craft a pack in any ordinary crafting grid, hold it in either hand, and press Use. One previously missing mod from the pack's pool enters the character's permanent collection at rank 0. Rank 0 already grants the card's first stat increment. Each missing mod in the pool has the same chance. Owned cards are excluded from the draw and the same character can use its unlocked card on multiple matching weapons.

| Crafting output | Shapeless inputs | Unlock pool |
|---|---|---|
| Arsenal Mod Pack | 2 paper, 1 copper ingot, 1 redstone dust | All 22 cards |
| Primary Mod Pack | 1 unopened Arsenal Mod Pack, 1 iron ingot | 11 rifle, shotgun and shared primary cards |
| Secondary Mod Pack | 1 unopened Arsenal Mod Pack, 1 copper ingot | 5 secondary cards |
| Melee Mod Pack | 1 unopened Arsenal Mod Pack, 1 flint | 6 melee cards |

Pick up paper to discover the Arsenal Mod Pack recipe. Picking up the unopened pack reveals its three focus recipes. Use two separate crafting-grid slots for the paper in the four-ingredient Arsenal Mod Pack. A successful survival opening consumes one pack. Opening a finished pool keeps the pack, reports completion and grants no duplicate or XP. An unopened focused pack can be given to a friend. Creative players can use packs without consuming them.

## Ranking a card

At the Gunsmith, the selected card's next rank costs its next rank number in ordinary Minecraft experience levels. Rank 0 to 1 costs one level, then two, then three. Going from 0 to 5 costs 15 levels in total, or 6 levels from 0 to 3 for cards capped at 3. A card's Fusion rank belongs to the player and applies to every weapon on which that player has installed it. Each weapon keeps its installed-card choices, and each new holder uses their own collection rank. Consult the weapon mod catalog for compatibility and cap per card.

## Source and saved data

The IDs are `destiny2mc:mod_pack`, `destiny2mc:primary_mod_pack`, `destiny2mc:secondary_mod_pack`, and `destiny2mc:melee_mod_pack`. Each recipe uses the same ID as its output. All are tagged `#destiny2mc:warframe/mod_packs`. Assets are original 32 × 32 foil wrapper sprites under `assets/destiny2mc/textures/item/tenno_packs/`. The primary, secondary and melee wraps each have their own color and stamped symbol.

`com.jax.destiny2mc.tenno.mods.ModCollection` owns player state. The player's persisted NeoForge data contains an `AgelessWarframeMods` compound whose keys are stable mod IDs and whose values are integer ranks. A missing key means locked. Saved ranks are copied on the ordinary player clone and survive logouts, dimension changes and death. The current catalog clamps previously higher values at read time. Unrecognized saved IDs stay in the server data to allow a later compatible catalog to restore them. The UI receives known, owned entries only.

There are no client-originated unlock packets. The server receives Minecraft's normal held-item use, picks a missing card, writes it to the ledger, charges the pack and returns a full owner-only snapshot with the awarded ID. Login, respawn, dimension travel and Fusion refresh the snapshot without replaying a reveal. A Fusion request includes the expected current rank and the server enforces both payment and that rank, preventing a repeated click packet from paying for two upgrades. User-facing resource locations retain the established `destiny2mc` namespace.

## Verification

`ModCollectionCheck.java` runs as a separate test mod. It crafts all four packs with the native recipe manager, exhausts every pool through the actual use action in both hands, checks full-pool preservation, XP expenditure, repeat rejection and player isolation, posts clone events, and reloads serialized player NBT. It also round-trips the full collection and optional revealed-card ID through the actual packet codec.
