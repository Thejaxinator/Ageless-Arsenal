# Weapon Mods at the Gunsmith

## Using the station

1. Craft a Mod Pack at a crafting table and use it in either hand. A pack draws one missing card from its pool. A completed pool leaves the pack in your hand. Cards start at Rank 0 and already provide their first bonus.
2. Right-click either Gunsmith, choose **Warframe**, then **Weapon Mods**. The left column lists each Warframe weapon in your hotbar, inventory and offhand. The slot label distinguishes two copies of the same weapon. Selecting a row shows the cards installed on that particular copy.
3. Choose a card. **Usable** shows the cards you own that fit the selected weapon. **Owned** includes your entire collection. **All** includes locked cards and lets you preview their category, art and starting effect. With no Warframe weapon in your inventory, **Usable** shows your collection and you can still rank cards up.
4. Select **Install mod** to fit the card. **Remove mod** frees its place while preserving your unlocked card and rank. Each weapon accepts every distinct compatible mod, and a card can be installed on several weapons at once. Installation and removal are free. The six visible installed tiles form one page. Further tiles automatically get another page.
5. **Rank up** shows the XP level price and the next rank's stat change before purchase. Rank 0 to 1 costs one ordinary Minecraft level, 1 to 2 costs two, and each later upgrade costs the new rank in levels. Increasing a card's rank immediately improves it across your own equipped copies. A traded weapon keeps its installed card IDs. The new owner's collection determines each card's effective rank. An unowned installed card is marked **Dormant** and can be removed.

| Control | Action |
| --- | --- |
| Tab or Shift+Tab | Switch franchise and recall its last tool during this visit |
| Left bracket or right bracket | Switch between the selected franchise’s two tools |
| M | Switch between Blueprints and Weapon Mods on the Warframe tab |
| Arrow keys | Move the selected collection card and show its page |
| Enter or keypad Enter | Install or remove the selected card on the selected weapon |
| F | Purchase the displayed next rank |
| Mouse wheel | Scroll the list or page under the pointer |
| Esc | Close the station |

## Code entry points

The classes below live under `src/main/java/com/jax/destiny2mc/`.

| Class | Responsibility |
| --- | --- |
| `client/ui/GunsmithScreen` | Tracks the selected franchise and its tool at either Gunsmith. Dispatches input using virtual canvas coordinates and revokes the station session on close. |
| `client/ui/GunsmithNavigation` | Supplies navigation drawing, layout and hit testing for the two franchise tabs and their tools. |
| `tenno/client/TennoFoundryPage` | Displays blueprint filters, ingredient costs and assembly controls. |
| `tenno/client/TennoModPage` | Projects carried slots, installed cards and the collection onto the 800 × 450 virtual canvas. Rebuilds from synchronized player and inventory state after opening or modifying a card. |
| `tenno/client/ModCardArt` | Draws the frame, illustration, rarity markers, rank pips, selection and lock state. Its detail view uses the same card definition as the collection tile. |
| `tenno/mods/ModBenchNetwork` | Issues a per-selection token, verifies the station and target, handles edits on the server thread and acknowledges the result. |
| `tenno/mods/ModCollection` | Owns permanent character ranks, pack unlocks, XP level purchases and synchronization. |
| `tenno/mods/WeaponMods` | Reads and updates the installed ID list on one stack, and resolves stat bonuses against its current user. |
| `tenno/mods/WeaponMod` | Supplies the card ID, title, rarity, category, compatibility, cap, art and effect text. New compatible definitions automatically populate these lists. |

## Transaction protocol

The serverbound payload `destiny2mc:weapon_mod_action` contains a request serial (VarLong), the server's selection token (long), operation (byte), inventory slot (VarInt), item registry ID, mod ID (UTF, 64 characters maximum) and expected current rank (VarInt). `destiny2mc:weapon_mod_result` returns the request serial, the current selection token and a status byte. `ModBenchNetwork` defines operation and status constants. Empty-handed collection browsing and Fusion bind slot `-1` and `minecraft:air`.

A real right click opens station access. Each edit checks the current dimension and bench reach, the active inventory menu, player state, the fresh serial, the selected token, the actual stack occupying the selected slot, and the expected rank. A bind captures the actual server `ItemStack` instance so that swapping two identical rifles during a request cannot modify the replacement. The server handles collection XP and stack mutations, then synchronizes the authoritative state. The client tracks a pending request and accepts only its own acknowledgement. Closing the station, changing dimension, respawning or logging out ends access.

## Regression fixture

`ModStationCheck.java` is a separate NeoForge test mod. It opens real packs, exercises the production handler with a server-side player and asserts the following behaviors on a dedicated server.

- Fusion works with an empty inventory and debits one XP level for Rank 1.
- A repeated request or outdated rank cannot debit XP again.
- Binding targets one physical rifle, including when two same-ID rifles occupy different slots.
- Swapping rifles during a request leaves the replacement untouched.
- Removal preserves collection rank. Leaving reach prevents edits. Closing the station revokes access until the next interaction.
