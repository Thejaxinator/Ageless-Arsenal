# Gunsmith navigation

Both Gunsmith blocks open a subclass of `GunsmithScreen`. A new visit starts on **Destiny → Crafting**. Each franchise remembers its last tool for the rest of that visit. Clicking the active franchise or tool preserves the current selection and pending request.

| Franchise | First tool | Second tool |
| --- | --- | --- |
| Destiny | Crafting | Perks |
| Warframe | Blueprints | Weapon Mods |

## Input

- Click a franchise, then a tool. Tab or Shift+Tab switches franchises. Either bracket key cycles tools. M also switches between the two Warframe tools. Esc closes the bench.
- On Destiny tools, Up and Down browse weapons and Enter performs the enabled action. In Perks, Left and Right cycle sockets. Shift+Up or Shift+Down selects an option in the current socket.
- On Warframe Blueprints, Left and Right change the weapon filter, Up and Down browse weapons, and Enter assembles an affordable selection. In Weapon Mods, the arrow keys browse collection cards, Enter installs or removes, and F ranks up. The mouse wheel operates the list under the pointer.

## Ownership and input geometry

`GunsmithNavigation` draws the header and tests its rectangles. `GunsmithScreen` owns one franchise selection and one remembered tool index for each franchise. `activeFranchise()` and `activeTool()` report the indices for native UI tests. Selecting an already active tab is idempotent. Both click and keyboard paths use the same `navigate` method and announce a changed route to the Minecraft narrator.

The header is drawn after each tool body and before the normal hover inspector. Tool bodies begin below virtual Y 68. The screen preserves the 800 × 450 aspect ratio and centers it in the game viewport. Absolute screen input is transformed back into that same canvas before hit testing. Rectangles use inclusive left/top and exclusive right/bottom bounds.

| Control | Left | Top | Width | Height |
| --- | ---: | ---: | ---: | ---: |
| Destiny | 397 | 6 | 184 | 30 |
| Warframe | 588 | 6 | 184 | 30 |
| Crafting or Blueprints | 421 | 40 | 159 | 24 |
| Perks or Weapon Mods | 597 | 40 | 175 | 24 |

`GunsmithLayout` draws the Destiny tool bodies. `TennoFoundryPage` owns fabrication state. `TennoModPage` owns collection browsing, active stack selection and Fusion acknowledgements. Entering Weapon Mods calls `enter()` to bind the selected physical stack again. Closing the screen calls `close()` exactly once through the shared controller. Leaving a tool keeps the player's station session valid for their return. Server authorization, crafting and inventory synchronization still run through the existing game handlers.
