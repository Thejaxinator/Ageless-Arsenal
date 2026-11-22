# Ageless Arsenals

**4.1.0 · Voxel Effects**, for Minecraft 1.21.1, NeoForge 21.1.266 and Java 21. Install this edition in the `mods/` folder on the server and every client. Replace the old mod when upgrading. Core and Voxel Effects share the internal `destiny2mc` registry namespace. Choose one JAR per installation. Existing item, entity, structure and saved component IDs are preserved. The same field guide, gunsmith, 70 weapons, seven salvage materials and eighteen Scorn/Dread rigs ship in both releases.

## Play

Craft new guns on the gunsmith crafting page. Select an owned gun on its customization page to change a socket. The field guide lists salvage under the rewards of eligible enemies. [Faction components and recipes](docs/GUNSMITH-AND-SALVAGE.md) • [New weapon roster](docs/ARMORY.md)

Telesto consumes one round after a charge and fires seven individual Void bolts. Bolts can attach to a block or a living target and detonate after a fuse. Reload and trigger bindings are under Minecraft’s Ageless Arsenals controls.

## Build from this folder

1. Install a JDK 21 and Python 3.10 or later. The first Gradle run downloads the mapped game and NeoForge dependencies.
2. Run `./gradlew -PpythonExe=python3 assemble` on macOS/Linux or `gradlew.bat -PpythonExe=python assemble` on Windows. Find the JAR under `build/libs/`.
3. For CI with a pre-resolved NeoForge development classpath, run `python build.py --classpath-file platform-classpath.txt --javac /path/to/jdk-21/bin/javac`. The text file holds one platform-separated classpath. The build uses the same source and model emitter as Gradle.

The build compiles the complete Java source under `src/main/java` and the 82 editable native rigs under `models/rigs`. It requires no previous mod JAR. Runtime textures, sounds, model parts, translations, structures, loot, recipes, mod metadata and mixin configurations live in `src/main/resources`. The stable legacy namespace also appears in Java package paths and the client preferences filename.

## Edit

- **Weapon behavior** — [Weapon program guide](docs/WEAPON-PROGRAMS.md), [JSON schema](docs/weapon_program.schema.json), and [live-reload example](examples/repulsion-pack/pack.mcmeta). The example changes an existing item into a charge-and-release repulsion weapon with regenerating energy.
- **Weapon geometry** — Open any of the 70 files in `blockbench/weapons/`. Keep the part groups and texture names. Run `python tools/blockbench.py --apply blockbench/weapons/wire_rifle.bbmodel` to import changes into the live resources, then build. Hand positions and sight heights are in `WeaponHandFit.java` and `SightFit.java`. Test both handedness settings and check the actual aim view after moving sights.
- **Art recipes** — `tools/weapon-art/` holds generators for the 18 additions, with reference sources and camera-fit notes. The original 52 recipes under `tools/armory-52/` generate into their own `stage/` directory. Copy changed resource paths into `src/main/resources/` before building. `sites.py` regenerates the compact faction structures into that same staging directory. Install Pillow when regenerating art.
- **Enemies** — `blockbench/scorn/` and `blockbench/dread/` are editor copies. Edit the canonical joint pivots in `models/rigs/` or regenerate the rigs using the respective faction recipe in `tools/`. Minecraft and the bestiary share those native layers, textures, glow masks and action clocks. Dread scene study and Scorn reference comparisons are in `docs/previews/`.
- **Survival economy** — Data under `data/destiny2mc/recipe/gunsmith/` sets the 18 new blueprints. Entity-type tags under `tags/entity_type/salvage/` select which species provide each part. `tools/salvage/` contains the component artwork and data authoring scripts.
- **Build provenance** — Where early generated logic had no original Java file, the executable classes of the last playable revision were recovered into Java. The original hand-written sources were preferred where they matched. The Python rig emitter comes from the original project. Keep `THIRD_PARTY_NOTICES/` when redistributing this private workshop.
- **Validation** — `tests/fixtures/` contains QA-only sources for disposable servers/clients. They operate on synthetic players, targets, test blocks and datapacks. Keep fixtures outside the production mod. `tests/results/` records the checks exercised on this release family.

## Voxel Effects

This edition adds world-space beams, kinetic stripes, projectile meshes and faction effects. Rendering consumes server-confirmed gameplay cues. See [renderer and network notes](docs/VOXEL-EFFECTS.md).

[Open the offline 70-model armory inspector](docs/Armory-inspector.html) to rotate each textured weapon, highlight moving groups, and compare its native aim view and reference.
