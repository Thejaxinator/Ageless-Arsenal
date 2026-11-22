# Energy Binder interface

Use the Energy Binder block to open **Paracausal**. Choose a category and a technique. The purchase button shows the required Motes of Light and Minecraft XP levels. A purchase permanently unlocks the technique for that character. Select one of the four active slots or two passive slots, then select **Bind**. The active slots accept any mix of grenades, melee, and class abilities. **Clear slot** removes the binding while keeping ownership. Movement and subclass buffs share the two passive slots.

Keyboard defaults are **Z**, **X**, **C**, and **V**. Change each under Minecraft's **Options → Controls → Key Binds → Ageless Arsenals**. The station and HUD display the current binding. The gameplay HUD draws the four abilities in slot order under the ammunition card. Each uses its own icon and current key. The selected theme controls how it shows readiness and cooldown. Duplicated techniques share a cooldown. The HUD uses the synchronized server deadline, and opens no client-side casting shortcut.

Open your inventory and click the small left-side chevron to inspect the current character loadout away from a station. The leveling edition shows independent career XP, hearts and armor gained from career level, and the local veteran spawn chance. Read the difficulty presets from the station's **Difficulty** control.

Mouse, Tab/Shift+Tab and Enter/Space operate the station's native widgets. Scroll the catalogue using the wheel, page controls or Page Up/Down. Focus outlines and narrator labels follow the actual action; purchases and binds appear when the server confirms. The element shapes distinguish fire, electrical, void, ice and strand energy independently of color.

The Java classes under `com.jax.destiny2mc.binder.client` read the common ability catalog and the owner-only character snapshot. Register additional powers on the server catalog to make them appear in the station. The inventory's optional leveling readout consumes `BinderProgressView.Info` and has no compile-time dependency on the career module.

## Native screen fixture

`tests/fixtures/BinderScreenCheck.java` is a client-only acceptance fixture for a disposable development world. Package it as a separate test mod with mod ID `binder_screen_check`. Start a dedicated localhost NeoForge server, give the fixture's offline player operator permission in the test world, and start the modded client with `-Dbinder.port=25733` and an existing writable `-Dbinder.shots=/path/to/screenshots` directory. Use a fresh test character on every run. The fixture places a real Binder, operates the rendered screens, verifies the six server purchases and bindings, rebinds and exercises a real cast key, and captures the native screens. In the Progression edition it also defeats four faction enemies with player credit and waits for the career rank to update. Successful execution prints `BINDER UI COMPLETE` to the client log.

## Appearance controls

The left inventory drawer slides out over the available side margin. Use its edge notch to change the saved view. Open HUD & recoil settings with **O** to switch the ammo and ability style together, change their scale or hide the combat ability display. The inventory and combat visibility settings are independent. See [Inventory drawer](INVENTORY.md), [HUD styles and references](HUD-STYLES.md), [HUD placement](COMBAT-HUD.md), and [Ability glyphs](ABILITY-GLYPHS.md).
