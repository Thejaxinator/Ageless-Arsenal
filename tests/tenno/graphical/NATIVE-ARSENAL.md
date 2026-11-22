# Testing the Warframe arsenal

The fixtures use an isolated Minecraft 1.21.1 dedicated NeoForge server, a real graphical client, and Java 21. The client joins with an operator account in a disposable world. The Foundry test opens the placed Gunsmith, clicks the Warframe tab, submits an affordable blueprint and checks the synchronized inventory. Bare inventory hovers record the real Warframe, Destiny and vanilla item-tooltip dispatch.

The client checks all 33 weapon registrations against the synchronized ranged and melee tags and the effective blueprint list, retrieves each baked GUI-context item model, and captures it through the live item renderer. It equips Twin Grakatas and Kuva Nukor in first person and holds guard with Harmony and Sun & Moon. It drives the default attack, reload and alternate bindings using real X input on a private client display. The default bindings exercised are mouse 1, R and G.

The dedicated range fixture uses a real connected survival player and stationary, unarmored ravagers. Assertions follow server ticks through the impact, chain, environmental occlusion, lingering cloud, reload field, returning throw and ammunition-conservation paths. This fixture is development-only.

The native screenshots have deterministic 1440 × 810 frames. The Mesh images use a solid background (RGB 25, 41, 50). The `boards.py` script crops those frames and typesets names, preserving the models' in-game texture, shape, material, and proportions. No generated illustration supplies the model pictures.

To reproduce, compile both Java fixtures outside the mod under test with mod ID `ageless_tenno_qa`, launch them in the client/server development runtime alongside the unmodified release, and set `-Dbinder.port=<localhost server port>`, `-Dbinder.shots=<output directory>` and `-Dtenno.key.helper=<path to key.py>`. The workstation runner needs a pre-resolved NeoForge 21.1.266/Java 21 SDK, Xorg, its dummy video driver, libXtst and an assets cache. Use this fixture in a fresh test world. `TENNO QA COMPLETE` and `TENNO RANGE COMPLETE` mark successful completion. Run `python boards.py <screenshot directory> <output directory>` with Pillow and NumPy to regenerate the plates.

The portable runner reads the prepared SDK root from the TENNO_TEST_SDK environment variable.
