# Gunsmith navigation verification

[Open the release results](navigation/results/) for the two Gunsmith blocks, their four tools, and the dedicated Warframe creative tab. [View the native screenshots](../docs/previews/navigation/).

## Earlier Mod Fusion verification

The results in [mod-fusion/results](mod-fusion/results/) identify the earlier Mod Fusion edition tested by SHA-256. The socket suite exercises 33 weapons against 22 cards. Separate dedicated-server fixtures cover recipe discovery, unique draws, experience charges, weapon transfer, damage, reload, multishot, reach, guarding and station requests. A real Minecraft client crafted and opened packs, fitted and ranked cards, and rendered the inventory tooltips against the dedicated server.

[Dedicated-server test instructions](mod-fusion/NATIVE-TESTS.md) · [Client test source](mod-fusion/graphical/) · [Native screenshots](../docs/previews/mod-fusion/)

To reproduce that earlier edition’s graphical run, use `MOD_TEST_SDK=/path/to/sdk python tests/mod-fusion/graphical/run.py RELEASE.jar OUTPUT` on a Linux host with Xorg, a prepared Java 21 NeoForge 21.1.266 ModDev SDK and the Xorg configuration in that SDK. Use `python tests/mod-fusion/graphical/check_frames.py OUTPUT/shots` to check the native framebuffers. The latter needs NumPy and Pillow.

The [Tenno Arsenal records](tenno/results/) likewise belong to the earlier Tenno Arsenal releases.

Historical graphical fixtures contain coordinates or keyboard sequences from their original screens. These include `fixtures/SmithScreenCheck.java`, `fixtures/GearUiCheck.java`, `tenno/ui/FoundryAudit.java`, `tenno/graphical/TennoClientCheck.java` and `mod-fusion/graphical/UpgradeClientCheck.java`. Use the [navigation fixtures](navigation/) to verify the current franchise and tool selectors.
