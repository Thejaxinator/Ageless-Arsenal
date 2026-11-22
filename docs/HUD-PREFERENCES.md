# Client HUD preferences

Open **HUD & recoil** with **O**, including while empty-handed. This key is rebindable in Minecraft Controls. Cycle **Theme** to change the ammo counter and its four ability slots together. The live preview uses the held gun and equipped abilities. An empty hand selects a sample gun and a completely empty loadout uses four example abilities in this preview.

**Ability HUD** controls the in-world ability strip. The inventory drawer has its own toggle on the inventory screen. The two preferences are independent. **Reset appearance** restores the original size, recoil and telemetry options and selects the Destiny theme. It keeps both visibility choices.

Client preferences are saved in `config/destiny2mc_client.properties`. The existing six numeric theme IDs continue to work for players who already chose a HUD. New profiles default to Destiny 2 FUI. The gameplay visibility key is `showAbilities` (default 1) and the drawer state key is `inventoryDrawerOpen` (default 0).

The native acceptance fixture is `tests/fixtures/HudPreferencesCheck.java`. Run it as a separate client test mod (`hud_preferences_check`) on a disposable NeoForge 21.1.266 client and local server. Give the test player operator access, pass `-Dbinder.port=<server port>` and `-Dbinder.shots=<existing directory>`, and use a fresh client profile. It opens the screen empty-handed using the bound key, operates the buttons, checks legacy preference migration and independent visibility, checks all ten controls at the minimum GUI size, and captures the real preview. The test edits only its isolated profile.
