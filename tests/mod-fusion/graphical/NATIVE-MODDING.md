# Testing weapon mods on a dedicated server

Run this fixture against each release JAR. It uses one ordinary survival character, a vanilla crafting table and a Gunsmith on a fresh offline test server. The Java test driver is packaged as a separate development mod. Commands set up the equipment, ingredients and starting levels; recipe placement, taking the output, opening a pack, ranking a card and changing a weapon use the live client controls. The test reads the replicated inventory and collection to verify what the server accepted.

## Running the fixture

Use Linux with Java 21, the project's prepared NeoForge 21.1.266 ModDev SDK, Xorg with its dummy driver, and Mesa software OpenGL. The SDK supplies the compiled mapped classpath in `classpath.txt`, the runtime classpath in `runtime.txt`, generated client/server program and VM arguments under `compile/build/moddev`, a Java 21 JDK in `jdk`, and `xorg.conf`. The framebuffer checks need Python, Pillow and NumPy. The optional presentation script also uses Open Sans.

```sh
export MOD_TEST_SDK=/absolute/path/to/sdk
python run.py /absolute/path/to/release.jar /tmp/ageless-mod-qa
python check_frames.py /tmp/ageless-mod-qa/shots
```

Each launch chooses a new character. Use a new output directory for each run. Specify `MOD_MC_BENCH=true` to run against the vanilla-styled Gunsmith block. The default exercises the futuristic Gunsmith. Set `BINDER_UI_DISPLAY` and `BINDER_UI_PORT` to unused numbers when running multiple instances. `results.txt` records the digest of the runtime mod and player-state assertions. `server.log` and `client.log` contain the native launch and connection records.

## Player actions under test

1. Verify the server synced all four crafting recipes and the client baked four pack models.
2. Craft a universal pack using two paper, copper and redstone. Refine that pack with iron. The result occupies a real inventory slot and the table consumes the ingredients.
3. Right-click the focused pack and verify one owned, rank-zero card. Open more primary packs to complete the primary pool. Further openings leave the surplus packs in the inventory.
4. Enter the Warframe weapon Mods page at the Gunsmith. Try ranking an owned card with zero levels, then install Serration on a Braton. Starting at eight levels, fuse it to ranks one and two and verify seven and five levels remain. The UI follows the dedicated server's weapon-selection acknowledgement before sending the next action.
5. Install the same card on a Soma. Remove it there, and verify the Braton still carries Serration and its owner retains rank two. Fill the Braton with the remaining compatible cards and verify the final six-card loadout and unchanged XP balance.
6. Check the inventory hovers for both a modded weapon and a focused pack. The fixture captures these after the real tooltip event reports the expected stack.

## Captured frames

| Native frame | What to inspect |
|---|---|
| `01-Craft-Pack.png` | Ingredients and pack result at the vanilla crafting table |
| `02-Reveal.png` | Name, effect and rank of the randomly drawn card in the world |
| `03-Collection.png` | Installed list before filling it and the unranked Serration card |
| `04-Fusion.png` | Two illuminated pips, current bonus, available levels and next cost |
| `05-All-Equipped.png` | Six compatible mods installed on Braton |
| `05-Weapon-Inspect.png` | Warframe weapon inspect in inventory |
| `06-Pack-Inspect.png` | Focused pack description and the four pack items |

The framebuffer checks sample the actual card, rank lamps, loadout cells and reveal banner. To assemble the shareable overview from a verified session, run `python make_preview.py RUN_DIRECTORY/shots OUTPUT_DIRECTORY RELEASE.jar`. The overview uses the captured interface and the four icon textures from the supplied release.
