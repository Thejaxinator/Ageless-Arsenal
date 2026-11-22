# Ageless Arsenals weapon art

The eighteen additions occupy indices 52 through 69. Existing saves keep their original item IDs and the `destiny2mc` namespace. Each new model has three independently rendered groups. All coordinates and animation mounts are generated from the same authored recipe.

| Workbench file | Destination in a standard source tree |
|---|---|
| `stage/assets/destiny2mc/` | `src/main/resources/assets/destiny2mc/` |
| `src/com/jax/destiny2mc/client/SightFit.java` | `src/main/java/com/jax/destiny2mc/client/SightFit.java` |
| `src/com/jax/destiny2mc/client/WeaponHandFit.java` | `src/main/java/com/jax/destiny2mc/client/WeaponHandFit.java` |
| `blockbench/*.bbmodel` | `blockbench/` |
| `weapons70.py`, `baseline-fits.json` | Keep together under `tools/weapon-art/` |
| `fits.json` | Optional exported camera, mechanism and contact values for indices 52–69 |
| `catalog-additions.txt` | Additional rows for the workshop catalog |

## Rebuild the art

Python 3 and Pillow generate the assets. NumPy runs the optional sight validation. From the directory containing the script and `baseline-fits.json`, run the following.

```sh
python weapons70.py --output build/ageless-art --check-sights
```

The output directory contains `stage/`, `src/`, `blockbench/`, the catalog extension and the exported fit data. Copy those results to the destinations above. An existing 52-entry Java table can also be imported directly using `--baseline-src path/to/java`; ordinary rebuilds use the compact baseline manifest. The supplied reference photographs are documentation and are unnecessary at runtime.

## Change a sculpt

1. Find its function in `weapons70.py`. Each function defines how the frame, barrel, ammunition and sight fit together. The `SPECS` row controls its length, finish, optic and two animation modes.
2. Dimensions use model pixels. Positive X is lateral, positive Y is up, and longitudinal coordinate `t` runs from 0 at the muzzle to 24 at the stock. The recipe scales longitudinal dimensions to the physical length in the catalog.
3. Give each cube a rigid group. `body` is fixed, `action` moves while firing or charging, and `load` moves during the reload. Keep the three groups nonempty. Edges, lights and decals on moving parts belong to the same group as their carrier.
4. Set the three `contact` points where the palms meet the model. The `grip` point attaches the trigger hand. The `support` and `loader` points guide the other hand. The renderer uses these coordinates for both handedness settings.
5. Leave room above the receiver for the camera. `sight()` creates real apertures and locates their feet on stationary geometry. `--check-sights` tests the aim axis and eight adjacent rays against every rotated cube. Check the native first-person view after changing the camera or optic.
6. `paint()` generates the material patches and identifying markings. `paint=True` maps the side faces of a plate to the matching full-width marking. Use `glow=True` on luminous components such as the Taken filaments, exposed Arc core and Strand blade.
7. Regenerate the art, launch the mod and test hip fire, full aim, reload, inventory view and the third-person pose.

## Edit in Blockbench

Open the weapon's `.bbmodel`, which embeds both textures. Each cube belongs directly under `body`, `action`, or `load`. The item is authored facing negative Z. For Java item format, rotate individual cubes about one axis in steps of 22.5° or 45°. When editing the source recipe as well, keep a copy of manual Blockbench edits because regeneration writes the model projects again.

## Source comparisons

The adjacent `WEAPON-REFERENCES.md` identifies the actual reference used per sculpt. `showroom.py` can produce the faction and Guardian reference sheets against the generated meshes. Pass `--shots path/to/captures` to use native inventory screenshots containing the exact item and lighting from the game. The source photographs belong to their original creators.
