# Energy Binder release tests

The fixtures in `fixtures/` are outside the production Java source set. The results name the release used for each run. Both editions share the Binder gameplay classes byte for byte. Both had separate graphical client/server runs and separate incoming-damage runs.

| Area | Check |
|---|---|
| Character | Exact mote and Minecraft-level prices, inventory sources, duplicate purchase, slot type, station reach, independent characters, shared cooldown and death clone. |
| Powers | Dedicated server casts and effect assertions for all 30 actives, movement budgets, lift and glide release, stacking and all five subclass buffs. |
| Loot | Workstation recipe, Motes of Light for all seven factions, legacy field boss and encounter boss payouts, puzzle target exclusion and doMobLoot. |
| Incoming damage | Unarmored Dreg bolt, rapid follow-up, Valus melee versus Protection IV netherite, permission checks, world persistence and unrelated damage. |
| Interface | Shop, exact payments, all six slots, inventory, difficulty and rebound input, using a graphical Minecraft client connected to a dedicated server. Screenshots are under `docs/previews/energy-binder/`. |
| Progression | All 16,073 attainable career XP balances, stat milestones, health save/load above 20 HP, death clone, capped growth and natural veterans. |

Each fixture's Java subscriber declares its test mod ID. Compile the fixture against the release JAR and mapped NeoForge 21.1.266 development dependencies using Java 21. Package its classes into a separate test mod with its own neoforge.mods.toml. Use a fresh disposable world with just the release and the fixture. See the feature notes in docs/ for additional test instructions. EncounterFormulaCheck and CareerCurveCheck (progression edition) are standalone Java checks.

The inherited 4.1 armory results and their original hashes live in `inherited-4.1/`.
