# Destiny inspection cards

Hover an ability at the Energy Binder or in the inventory drawer to read its effect, element and type. An active form displays its cooldown and any remaining recharge time. A locked form lists its permanent unlock price in Motes of Light and Minecraft levels. An equipped form lists all current bindings, including duplicate active slots sharing a cooldown. Passives identify their passive socket. The drawer notch has a smaller Paracausal card with the action and actual number of occupied slots.

Hover a Destiny weapon in the inventory or in the Gunsmith’s Destiny tools for its rarity, frame, element, base damage per hit, ammunition, effective range, reload time, intrinsic effect and two installed trait rolls. Multi-projectile weapons show damage per pellet and projectile count. Capacitors show stored charge and recovery interval. The weapon program and the stack's current socket choices supply the values.

Armor cards use each item's real slot, rarity, protection, durability, origin and trait conditions. Toughness, knockback resistance and enchantment count appear when applicable. The authentic Destiny armor names, original Minecraft traits, recipes and damage conversions belong to the armor catalog.

Hold Shift on a gear item to read its entire gathered Minecraft tooltip with the same rarity border. This exposes the full six-socket roll, enchantments, lore, custom names and lines added by other mods. Advanced tooltips (F3+H) use this detailed presentation automatically. Items carrying hide-tooltip components honor their visibility settings. Other Minecraft gear keeps its ordinary appearance.

## Client hooks

`GearHoverCard` is a client-only NeoForge game-event subscriber. It intercepts `RenderTooltipEvent.Pre` for `DestinyGunItem` and `ArsenalArmorItem` and colors the expanded item tooltip through `RenderTooltipEvent.Color`. Screens with custom canvases invoke `drawItem`, `drawAbility` or `drawDrawerHint` with the original screen-space mouse coordinates after popping their local GUI transform. Classifying the event's actual stack keeps a hovered inventory slot independent of the held item. Inspecting a card only reads the replicated character and item data.

`InspectionPainter` measures text using the currently active Minecraft font. The header wraps long names; stat values and trait descriptions wrap separately. It chooses a side of the pointer, then fits and clamps the measured card within the visible GUI. A deliberate Z offset places it over the item-slot grid without applying a screen overlay. Rarity-colored header bands, the large primary stat, and separate intrinsic and installed trait lines derive from Bungie's in-game item hierarchy. Every displayed value uses this mod's units.

## Design references

- David Candland’s Destiny UI presentation includes the original Party Crasher +1 tooltip, with its title, rarity, weapon type and prominent attack value. [Bungie at GDC](https://media.gdcvault.com/gdc2016/Presentations/Candland_David_Tenacious_Design_and.pdf)
- The Destiny 2 item hover vocabulary includes weapon rarity, damage type and installed perks. [light.gg item tooltip reference](https://www.light.gg/tools/tooltips/)
