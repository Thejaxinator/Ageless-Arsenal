# Gunsmith and Creative inventory acceptance

Run the test with the Java 21 NeoForge 21.1.266 ModDev SDK used to build these releases, Linux Xorg with its dummy driver and software OpenGL. The Java fixture runs in a separate test mod alongside the actual release JAR. The Python launcher creates a disposable local server, fresh player and graphical client. The server's admin commands supply the materials and equipment for the test.

From this directory:

```sh
export MOD_TEST_SDK=/absolute/path/to/sdk
python run.py /absolute/path/to/release.jar /tmp/ageless-nav-test
python check_frames.py /tmp/ageless-nav-test/shots
python make_preview.py /tmp/ageless-nav-test/shots /tmp/ageless-nav-preview
```

The default test window is 1440 × 810. For a smaller window set `NAV_WIDTH=854 NAV_HEIGHT=480`. Setting `MOD_MC_BENCH=true` selects the Minecraft-styled workbench block. Use `BINDER_UI_PORT` and `BINDER_UI_DISPLAY` for independent concurrent runs. The SDK directory must contain the JDK, assembled classpath files, generated ModDev run arguments, client assets and the Xorg configuration from the release setup. The screenshot tools use Pillow, NumPy and Open Sans.

The survival player right-clicks the placed Gunsmith and clicks the rendered two-level controls. The driver verifies all four page identities and their nested selection state, including returning to each franchise's last tool. It changes the perk on an Ace of Spades for one Ether Alloy and two Copper Ingots, assembles a Warframe blueprint with the displayed ingredients, then installs Serration on a Braton and fuses it to rank one for one Minecraft level. It checks the resulting owned weapon and collection through the replicated game state.

The player then switches to Creative mode and opens Minecraft's native item picker. The fixture clicks the actual page arrow and tab buttons, verifies the 37 distinct Warframe items in display order, and checks Combat, Tools, Paracausal and global Search. The first and last Warframe categories are checked against registry-backed entries, including the Twin Grakatas tab icon.

`results.txt` records player-action assertions and the SHA-256 of the JAR installed in the client. `frames.txt` can be produced by redirecting `check_frames.py` output. The six full native PNGs show Destiny Crafting, Destiny Perks, Warframe Blueprints, Warframe Weapon Mods, the Warframe creative tab and vanilla Combat. The image checks read the per-franchise tab fills, per-tool underlines, the distinct usable contents and native Creative slot colors. The presentation sheet uses crops from these frames.
