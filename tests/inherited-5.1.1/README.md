# HUD and inventory verification

The client and dedicated server ran the shipped release together on Minecraft 1.21.1 / NeoForge 21.1.266. The SHA in each result file identifies its actual JAR. Both editions share the same client, HUD logic and artwork.

- **ThemedHudCheck** buys and binds abilities, equips Ace of Spades, casts a rebound key, switches all six live themes, toggles combat visibility, and exercises the inventory drawer in the real Minecraft client. Open its screenshots under `docs/previews/combat-hud/`.
- **DrawerInputCheck** clicks through the native mouse path, crafts four planks while the drawer is open, protects the carried stack on drawer paper and checks true outside clicks. **LayoutCheck** checks the bounds of the ammo and ability HUD across all themes, scales, viewport sizes and offhand states.
- **HudPreferencesCheck** exercises first-run and saved HUD settings, the settings key with empty hands, control layout and preview.
- **BinderScreenCheck** exercises the Binder purchase and loadout controls. It opens the current left drawer through the native inventory button.
- The preceding Energy Binder edition’s gameplay checks and data live under `tests/inherited-5.1.0/`. The 4.1 armory checks remain under `tests/inherited-4.1/`.

Compile native QA subscribers against the edition and the mapped NeoForge development classpath as separate test mods; the fixtures are excluded from the release JAR. Run them against a disposable local server as an operator. Their source headers document launch properties and image capture. The independent HUD placement and data validation use no remote services.
