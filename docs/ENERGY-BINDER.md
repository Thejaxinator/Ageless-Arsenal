# Energy Binder character state

The Energy Binder combines permanent unlocks with a six-slot loadout. Slots 1–4 can hold any mixture of active abilities. Slots 5–6 can hold any movement or passive ability. Unlocking an ability spends the listed Motes of Light from inventory and the listed number of whole vanilla experience levels. A purchase does not equip the ability. Equipping and clearing unlocked slots are free.

Active keys may bind the same ability twice. Both keys share that ability’s cooldown. A passive already occupying one slot moves when equipped in the other. Equipping two different jump abilities is supported by the movement subsystem. Cooldowns, unlocked IDs and equipped IDs belong to the character and survive death, respawn and reconnect. Time spent on a running server counts toward cooldowns.

## Source and state format

`com.jax.destiny2mc.binder.BinderAbility` defines the ID, display text, element, category, mote price, vanilla level price and cooldown ticks. Catalog definitions come from `ActiveAbilities.catalog()` and `PassiveAbilities.catalog()`. References use stable string IDs. `BinderCatalog` fails initialization when IDs collide. When extending the roster, retain shipped IDs and keep the client and server on the same mod version.

NeoForge persists the following compound under the player’s persistent data. Missing keys represent a new character. The example shows two equipped abilities and one ability on cooldown.

```snbt
AgelessEnergyBinder: {
  Unlocked: {solar_grenade:1b,triple_jump:1b},
  Slot0:"solar_grenade",
  Slot4:"triple_jump",
  ReadyAt:{solar_grenade:18050L}
}
```

`ReadyAt` uses the Overworld game-time clock, including while the player is in another dimension. Bindings and unlocks are copied during the vanilla player clone event. The temporary session tracks the exact dimension and block position of the station which the player used. It ends on close, dimension change or disconnect. Every transaction independently checks reach, block identity, ownership and available resources.

`BinderNetwork.Request` carries an operation, slot index, and at most 48 characters of ability ID. The server resolves the actual cast, prices and inventory. On success, the owner receives a bounded snapshot containing permanent unlocks, six slots, future cooldown deadlines, available motes, vanilla levels and a one-line action result. Opening the shop and refreshing balances are independent of level progression in the optional edition.

The fixture `BinderEconomyCheck.java` exercises payment, slot typing, stacking, cooldown identity, station reach, player isolation and death cloning on a disposable dedicated server.
