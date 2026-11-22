# Warframe weapon sockets

A Warframe weapon stores the ordered IDs of the mods installed on that physical stack. Each compatible ID can be installed once. Every compatible card can be used together. There is no points budget, slot ceiling, polarity, or drain. An unlock can be fitted to any number of separate weapons, including multiple copies of the same model. Removing a socket preserves the character's unlock and rank.

## Compatibility

| Card type | Eligible equipment | Examples |
|---|---|---|
| Rifle | Primary weapons using rifle mods, including launchers, snipers and flamethrowers | Braton, Perigale, Ignis, Torid |
| Shotgun | Primary shotguns and primary shotgun arm cannons | Cedo, Cedo Prime, Bubonico, Coda Bubonico |
| Primary | Both rifle and shotgun families | Vigilante Armaments fits both a Soma and a Bubonico |
| Pistol | All secondaries, including machine pistols, shotgun sidearms, beams and thrown blades | Twin Grakatas, Brakk, Nukor, Cantare |
| Melee | All six Warframe melee weapons | Amanata, Hate, Harmony, Heat Sword, Dual Heat Swords, Sun & Moon |

A pack unlocks a rank-zero copy with one active increment. For a card's stat increment `s` and a holder rank `r` between zero and the maximum, the contribution is `s × (r + 1)`. An unowned card contributes zero. Distinct cards contributing to the same stat add together. Reach uses blocks. Other stat increments are fractions, such as 0.1 for 10%. Combat consumes these bonuses according to the weapon family.

## Transfers and persistence

A player who receives a modded weapon sees the installed IDs. Each active mod uses **that wielder's** unlock and rank. A holder can remove an inherited, dormant card. Raising a rank improves every compatible equipped copy of that mod when that player uses it. The server captures the current combat bonuses when firing delayed attacks.

Minecraft 1.21.1's `minecraft:custom_data` component stores the list under `ageless_weapon_mods`. For example, a Braton with Serration and Hellfire stores `{ageless_weapon_mods:["serration","hellfire"]}`. Ammo, durability, anvil names, enchantments and unrelated components continue to belong to the stack. Save/load uses Minecraft's ordinary item serialization. Weapons created before this release begin with an empty socket list.

Unknown IDs survive an unrelated socket edit, allowing a future server to recover its own cards. Incompatible and duplicate stored IDs have no additional effect. Removing a duplicate removes every instance of that ID to normalize a malformed stack.

## Authoring

Add a `WeaponMod` definition and its original 32 × 32 RGBA glyph under `assets/destiny2mc/textures/gui/warframe/mods/<id>.png`. The enum supplies the compatible equipment family, pack, rarity, display name, rank limit, stat increments and description. The `WeaponMod.byId` and `WeaponMods.installed` entry points use stable lowercase IDs. The bench and pack system consume the same catalog. A new stat needs a `ModStat` entry and a game-side integration for the affected attack family.

The included `warframe-mod-catalog.json` lists every card with the exact effects at every rank. Its per-card Wiki links document the original identities. Rank caps, coefficients and status interactions in this mod are Minecraft adaptations. The public Warframe export supplies the original card rarity. [Rifle mods](https://wiki.warframe.com/w/Rifle_Mods) · [Shotgun mods](https://wiki.warframe.com/w/Shotgun_Mods) · [Pistol mods](https://wiki.warframe.com/w/Pistol_Mods) · [Melee mods](https://wiki.warframe.com/w/Melee_Mods) · [Warframe public-export mirror](https://github.com/WFCD/warframe-items/blob/master/data/json/Mods.json)
