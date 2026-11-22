# Release checks

The installed JAR hash is recorded in `results/release.sha256`. The full native armory capture exercised every registered item, and rechecked the two final sight adjustments on the delivered Core. Geometry comparisons use both a visible item and a hidden item from an identical camera. The 11 × 11 center region remained clear for each weapon.

Core and Voxel Effects have identical registered items, recipes, models, data, and simulation class bytes. The editions differ in mod metadata and the additional rendering/network presentation module. The Core dedicated-server and Gunsmith fixtures use ordinary input or the real menu handlers. They cover physical Telesto entities, target damage, fuse persistence, all seven salvage drops, 18 new recipes, 108 perk changes, and a live program override. The effects client fixture observes actual server shots and an accepted Captain teleport.

To work on native QA, compile each fixture as a separate test-only mod against the development classpath and production JAR. Refer to its EventBusSubscriber mod ID, test properties and scratch-world setup in its source. Unit fixtures can clear blocks, spawn test targets, install test datapacks, or modify inventory. Run these in a throwaway Minecraft instance. The standard production build excludes the entire tests folder.
