# Run the six weapon-mod server tests

From the exported project root, use JDK 21 and Python 3.8 or newer. The supplied Gradle wrapper prepares a Minecraft 1.21.1 and NeoForge 21.1.266 development launcher. The initial setup downloads Gradle and game dependencies. Point `MOD_TEST_SDK` to a writable local directory. The runner writes `eula=true` for its temporary Minecraft test servers. Run it on a machine where you accept the Minecraft EULA.

```sh
export MOD_TEST_SDK="$PWD/build/mod-test-sdk"
python3 tests/mod-fusion/run_native.py --prepare
python3 tests/mod-fusion/run_native.py /path/to/Ageless-Arsenals.jar
```

On Windows PowerShell, set `$env:MOD_TEST_SDK = "$PWD/build/mod-test-sdk"` and use `py -3` in place of `python3`. You can also pass `--sdk DIRECTORY`. If you move the source, Gradle cache or JDK, run `--prepare` again to refresh the absolute launcher paths.

| Suite | Fixture mod ID | Coverage |
| --- | --- | --- |
| `collection` | `mod_collection_check` | Pack use, unique unlocks, Fusion costs and caps, persistence and synchronization |
| `sockets` | `tenno_socket_check` | Per-stack installation, compatibility and effective ranks |
| `ranged` | `modded_ranged_check` | Modified firing, reload and ammunition behavior |
| `melee` | `tenno_melee_mods` | Melee damage, speed, reach and blocking |
| `station` | `mod_station_check` | Binding the selected physical weapon, Fusion requests, distance and session checks |
| `transfer` | `arsenal_transfer_check` | Loadouts on traded or saved weapons, rank reuse and Foundry payment |

Each suite compiles one Java fixture from `native/`, then starts a fresh dedicated server with the selected release and fixture as its only mod JARs. Test servers bind to loopback on an automatically assigned port and stop after their success line. The runner prints the temporary directory holding each server log, each test world and `results.txt` with the release SHA-256. You can delete this directory after reviewing it.

```sh
python3 tests/mod-fusion/run_native.py --list
python3 tests/mod-fusion/run_native.py /path/to/Ageless-Arsenals.jar --only station transfer --runs-dir build/test-runs
```

A failed assertion leaves its full `server.log` in the named fixture directory. If the game takes longer than three minutes to start, set `--timeout 360`. These six checks exercise game code on dedicated servers. Client UI captures and their recorded results are documented separately.
