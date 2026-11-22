# Warframe Foundry and Arsenal inspection

Interact with either Gunsmith block and choose **Warframe → Blueprints**. Use **All**, **Ranged**, or **Melee** to filter blueprints. Select a weapon to see its model, action and three ingredient costs. **Assemble weapon** uses the backpack and hotbar. The confirmation appears after the server completes the transaction. For Destiny weapons, choose **Destiny → Crafting** to build a gun or **Destiny → Perks** to modify an owned roll.

Scroll the list with the wheel. Tab or Shift+Tab switches franchise. Left bracket or right bracket switches tools within that franchise. On Blueprints, Left and Right change filters, Up and Down select a blueprint, and Enter assembles it.

The Arsenal card is shared between Foundry blueprint rows, the selected model, and native inventory stacks. It reads the item's actual magazine or battery charge and melee durability. The name respects anvil renames. Guns report base power, range and trigger. Melee reports base power, swing speed, reach and frontal blocking angle. The card shows the player’s current block and charged heavy bindings on melee and the alternate-fire binding on supported guns. One Minecraft block is treated as one metre in the range fields. Hold Shift for the native item components. Advanced tooltips and the vanilla tooltip visibility flags are respected.

The card uses a dark green-black field, thin metallic trim, compact right-aligned values and an original pixel emblem. Prime, Kuva, Coda, Vandal, Wraith and Prisma have distinct trim. The design references the equipment and statistics panels in the Warframe Arsenal. [Arsenal reference](https://wiki.warframe.com/w/Arsenal) · [Official Arsenal UI update](https://www.warframe.com/updates/pc/37-0-0)

## Network and recipe integration

The request specifies a registered item ID and a monotonically increasing connection-local sequence. The server checks the actual block interaction, player reach, dimension, station identity, recipe and materials. A repeated packet cannot produce a second item or debit. Sequence state is discarded on logout or server stop. Pack-defined recipes are resolved using the vanilla recipe manager for both the list and the server transaction. The output is inserted into inventory or dropped if full.

`FoundryNetwork` is the network entry point. `TennoFoundryPage` implements the Blueprints tool. `TennoInspection` draws the weapon cards. The Blueprints tool uses `TennoBlueprints` for listing, quoting, and the server transaction. The card uses the ranged and melee gameplay profiles directly.

The **Weapon Mods** page shares this station. [Install and rank cards](WEAPON-MOD-BENCH.md).
