# Faction sites

The templates retain the nine existing structure IDs and their bounding boxes. Each site has one accessible cache. The arena floor, altar and boss arrival volume still occupy the same coordinates in each dungeon.

| Site | Detail carried into the Minecraft build | Visual reference |
| --- | --- | --- |
| Fallen Ether Bunker | A dark ribbed hull shelters the ether tanks. The purple sail above the entrance drapes down from its roof, and paired engine housings project from the sides. | [Ketch exterior](https://www.destinypedia.com/File:DuskLair4.jpg) |
| Vex Transit Gate | A luminous aperture sits between weathered stone piers beneath a broken aqueduct. A two-block-high path passes through the lower gate. | [Endless Steps](https://www.destinypedia.com/File:Endless_Steps.jpg) |
| Cabal Forward Base | Recessed lit gates, projecting armored shoulders and a squat roof cabin give the outpost the low profile of the Rubicon buildings. The cabin carries the signal mast. | [Firebase Rubicon](https://www.destinypedia.com/File:Firebase_Rubicon.jpg) |
| Scorn Ether Still | An exposed Servitor eye faces out from a copper cage. Toothed pylons, service rails and a purple canopy surround the ether vessel. | [Ether Harvester](https://www.destinypedia.com/File:Ether_Harvester.png) |
| Dread Resonance Needle | Black slabs enclose inset amber seams. Dark glass supports a translucent central prism, with a narrow walk around the blades. | [Salvation’s Edge opening](https://www.destinypedia.com/File:Salvation%27s_Edge_Opening_Single.jpg) |
| Court of Petitions | Two pointed arcades frame the processional floor. A stepped apse holds an illuminated purple oculus behind the loot cache. | [The Shattered Throne](https://www.destinypedia.com/File:ShatteredThrone2.jpg) |
| Cradle of Embers | Six black and crimson towers stand on a ring around the central arena. Each has a lit core and a forked crown. | [Pit of Heresy entrance](https://www.destinypedia.com/File:PitofHersey2.png) |
| Rusted Vault | An orange spherical salvage vessel hangs from a crossbeam. Stairs lead up to cargo galleries beside the armored vault bulkhead. | [Grasp of Avarice](https://www.destinypedia.com/File:GraspofAvariceMain.jpg) |
| Siren’s Watch | A tapered Seraph tower rises through a sand-colored armored housing. Long antenna arms cross its cap, and a maintenance hatch passes beneath to the cache. | [Spire of the Watcher press image](https://www.destinypedia.com/File:2022_S19_Dungeon_PressKit_Dungeon_Compressed_005.jpg) |

Run `python sites.py` to regenerate the nine gzip NBT files beneath `stage/data/destiny2mc/structure/`. The generator uses the Python standard library. The sites are original compact layouts based on the referenced spaces.
