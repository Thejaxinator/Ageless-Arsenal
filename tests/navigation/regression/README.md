# Physical Gunsmith regression

The dedicated-server fixture places each Gunsmith block and uses it through Minecraft's normal server-side right-click path as a Survival player. It checks that a Destiny craft and paid perk swap, a Warframe blueprint, and a weapon mod installation succeed at each station. It verifies that replayed requests, distance and a removed station protect the player's materials and equipment. The fixture also checks that a spectator cannot use station authorization.

The runner invokes two existing source tests in the same world. `WarframeFoundryCheck` pays for every Warframe blueprint. `ModSocketCheck` exercises the complete compatibility matrix and verifies that item-component serialization preserves a weapon's installed mods.

From the source root, run the following with a Java 21 NeoForge 21.1.266 server development kit. The `--sdk` directory contains `jdk/`, `classpath.txt`, `runtime.txt` and the NeoForge development launch arguments under `compile/build/moddev/`. The port defaults to 27973 on loopback.

```sh
python tests/navigation/regression/run.py \
  --sdk /path/to/sdk \
  --source . \
  --jar /path/to/release.jar \
  --work /tmp/station-regression \
  --results /tmp/station-results
```

The test-only mod runs inside Minecraft, invokes the registered server payload handlers on the server thread, and uses a NeoForge FakePlayer in place of a remote client. The results filename begins with the first ten characters of the tested JAR's SHA-256. The full hash and test outcomes are recorded inside.
