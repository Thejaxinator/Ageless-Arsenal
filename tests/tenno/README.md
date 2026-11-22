# Tenno Arsenal verification

The current native test sources and results are in `tenno/`. Each result records the release SHA-256. The melee record also identifies byte-for-byte equivalent melee code and art in the two editions. The runs use Java 21 and NeoForge 21.1.266. Test subscribers are separate developer mods and are excluded from the release JAR.

The Foundry fixture assembles every weapon from survival materials. The ranged fixture hits real targets with every registered ranged item and traces its distinct actions. The melee fixture checks every item's native durability, reach, guard and technique. The interface and graphical fixtures run a native client against a dedicated server and exercise both the regular inventory and Gunsmith. See `tenno/graphical/NATIVE-ARSENAL.md` for captures and controls. Earlier armor, Destiny and abilities verification is retained in the versioned inherited folders.
