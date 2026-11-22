# Tenno receiver models

The weapons use hand-built cuboids, sloped ribs and original pixel-art material atlases. The table links the wiki entry and the specific inventory render inspected for each model. The reference images are credited to Digital Extremes. The WFCD Warframe Items project distributes public item renders. Only the new model geometry and material atlases ship in the mod.

| Model | Details reproduced | Reference |
|---|---|---|
| AX-52 | Wire stock with a russet heel, long belly sling, curved magazine and short warm handguard. | [Wiki](https://wiki.warframe.com/w/AX-52) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/AK47Weapon.png) |
| Grakata | Open rear frame, bare pistol grip with a bulb reservoir and a heavy drum beneath the twin nose rails. | [Wiki](https://wiki.warframe.com/w/Grakata) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/Grakata.png) |
| Prisma Grakata | Marbled enamel stock cheeks, polished cyan barrel jacket and cool jewel accents. | [Wiki](https://wiki.warframe.com/w/Prisma_Grakata) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/PrismaGrakata.png) |
| Twin Grakatas | Two separate short-heeled receivers. The paired variant drops the round Grakata grip reservoirs. | [Wiki](https://wiki.warframe.com/w/Twin_Grakatas) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/TwinGrakatas.png) |
| Braton | Sloped charcoal cheek, red seam, cyan flank lamp and an angular suspended carry rail. | [Wiki](https://wiki.warframe.com/w/Braton) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/Braton.png) |
| Braton Prime | Ivory receiver, navy curved grip, looped heel, gold carrying arch and a stack of muzzle bands. | [Wiki](https://wiki.warframe.com/w/Braton_Prime) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/BratonPrime.png) |
| Braton Vandal | Box-shaped Lotus-badged stock, cyan steel body and black squared ribbed forend. | [Wiki](https://wiki.warframe.com/w/Braton_Vandal) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/BratonVandal.png) |
| Burston | Pearlescent humpback carry opening, a long curved stock ridge and low tilted power pack. | [Wiki](https://wiki.warframe.com/w/Burston) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/Burston.png) |
| Burston Prime | Floating gold bow beneath the grip and long gilt nose prong around a darker slab receiver. | [Wiki](https://wiki.warframe.com/w/Burston_Prime) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/BurstonPrime.png) |
| Soma | Exposed metal stock loop and linked crescent magazine passing above and below the receiver. | [Wiki](https://wiki.warframe.com/w/Soma) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/Soma.png) |
| Soma Prime | Gold forked magazine horns, curved muzzle guard and cyan receiver gem. | [Wiki](https://wiki.warframe.com/w/Soma_Prime) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/SomaPrime.png) |
| Perigale | Bone-colored open rear cage, russet overlapping longitudinal blades and a narrow barrel fork. | [Wiki](https://wiki.warframe.com/w/Perigale) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/TnQuadSniperRifle.png) |
| Perigale Prime | Elongated gilded jaw filaments, ivory converging blades, gold muzzle forks and an open stock. | [Wiki](https://wiki.warframe.com/w/Perigale_Prime) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/PerigalePrime.png) |
| Vesper 77 | Long integral suppressor, low laser block, paneled black grip and narrow rear slide. | [Wiki](https://wiki.warframe.com/w/Vesper_77) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/LasSilencedPistolWeapon.png) |
| Cantare | Green-bound handle, opposing red crescents and a long blush-colored split blade with jade tips. | [Wiki](https://wiki.warframe.com/w/Cantare) · [Item render](https://raw.githubusercontent.com/WFCD/warframe-items/master/data/img/TnChoirframeKunai.png) |

## Rendering and model maintenance

Each item has stationed geometry, a moving action and a separately animated magazine, cartridge or blade. Minecraft reads these from `assets/destiny2mc/models/armory/<id>/{body,action,load}.json`. The visible face textures point to `textures/item/tenno_<id>.png`. The 64×64 atlas contains sixteen hand-designed material finishes. The Twin Grakatas and Cantare use a paired sculpture for inventory, item frame and ground display, with a single model for each active hand.

A sight ray runs down the central 0.40-model-unit corridor. Each rifle’s front bead ends below it. The Soma feed arc sits to the side, allowing an unobstructed view down its open stock. The paired weapons use a converging dual stance. The source generator records the sight geometry in model units and the renderer applies the corresponding camera offset.

With Python, Pillow and NumPy installed, run `python tools/tenno_receiver_art.py --project .` to regenerate this group. Run `python tools/check_receiver_art.py --project . --out docs` to validate its geometry and render the preview. The audit checks all faces, texture coordinates used for render, complete body/action/load partitions, vanilla rotation angles, legal element coordinates, paired-model counts and the center sight corridor. The preview computes color from the actual exported cuboids and atlas UVs. In-game screenshots include Minecraft’s own lighting and native interaction.

[Perigale Prime release notes](https://www.warframe.com/en/patch-notes/switch/42-0-6) · [Jade Shadows release notes](https://www.warframe.com/en/patch-notes/pc/36-0-0) · [NeoForge separate-transforms loader](https://docs.neoforged.net/docs/1.21.1/resources/client/models/modelloaders/#separate-transforms-model)
