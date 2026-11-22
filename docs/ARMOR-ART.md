# Wearable armor artwork

The fifteen equipment items use original pixel art and 283 modeled pieces attached to the head, body, arms or legs. The three raid sets cover the four Minecraft armor slots. Each chest piece carries its matching gauntlets. Each Titan mark sits at the waist and moves with the protected thighs. This keeps the hand and offhand free for items.

| Family | Shape and finish | Visual reference |
|---|---|---|
| Kabr | A single red optic enclosed by brass jaws, a dusty blue chest with pale chevrons, and cyan Vex conductor tines on the gauntlet and greaves. | [Titan set screenshot](https://www.blueberries.gg/wp-content/uploads/2023/04/Kabrs-armor-Titan-Destiny-2.jpg) · [Bungie helmet render](https://www.bungie.net/common/destiny2_content/screenshots/4029224226.jpg) |
| Willbreaker | A green cross visor sits between the split, blood-stained brow and the ivory jaw. The chest has overlapping ribs, a green core, and swept bone spurs on the pauldrons. | [Willbreaker’s Watch reference](https://destiny.fandom.com/wiki/Willbreaker%27s_Watch_(Year_1)) · [Bungie chest render](https://www.bungie.net/common/destiny2_content/screenshots/1261894567.jpg) |
| Rull | Ivory plating and crossed gold inlays carry from helmet to chest. Broad, pitched shoulder shells frame a purple sash and white embroidered mark. | [Full Rull Suit](https://www.destinypedia.com/Rull_Suit) · [Bungie mask render](https://www.bungie.net/common/destiny2_content/screenshots/1413589586.jpg) |
| Helm of Saint-14 | A high fluted crest rises over a violet visor, steel cheek guards and a central nose bridge. | [Helmet reference](https://www.gamesradar.com/games/destiny/destiny-2-exotic-armor-best/) |
| The Armamentarium | Two olive grenades sit prominently on the breastplate between harness straps, buckles and canvas ammunition pouches. | [Bungie exotic render](https://www.bungie.net/common/destiny2_content/screenshots/3874247549.jpg) |
| Mask of Bakris | Four blue eyes sit in a stitched, pale Eliksni face. Dark twin jaw filters and a metal respirator form the lower half. | [Exotic inspect reference](https://skycoach.gg/destiny-boost/products/mask-of-bakris-8684) |

## Runtime

`com.jax.destiny2mc.client.armor.WearableModels` registers one layer per item through NeoForge’s client extensions. Minecraft’s normal humanoid armor layer supplies the player or armor stand pose, the equipment slot visibility mask, lighting and glint. The model retains standard joint names; the helmet follows the wearer’s head and the mark follows its own leg. The renderer cache clears when Minecraft rebuilds its render layers.

`ArmorGeometry.java` is generated, readable Java. It constructs each fitted armor model from named local cuboids. Each cuboid has its own UV area on a 256 × 256 atlas. The set texture addresses are `assets/destiny2mc/textures/models/armor/<material>_layer_1.png` and `<material>_layer_2.png`; the latter serves the leggings. An exotic uses its own material and its own first-layer texture. Every item also has a 32 × 32 transparent icon and an item-model JSON file.

## Authoring

Edit `tools/armor_shapes.py` to change shapes, their local joint or their angles. Edit `tools/armor_art.py` to change the palette, edge highlights, wear patterns or item-icon projection. Run `python tools/armor_art.py src/main` from the release checkout with Pillow and NumPy installed. This regenerates the mesh source, painted UV sheets, item models and transparent thumbnails. It produces all of its pixels and shapes locally. Visual-reference images are linked above.

Individual armor parts can be retextured from a resource pack at the same paths. The 32 × 32 icons are particularly suitable for custom inventory themes.
