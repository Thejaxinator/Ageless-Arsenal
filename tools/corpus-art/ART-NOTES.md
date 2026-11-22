# Corpus Voxel Art Notes

- **Pipeline parity**: Uses the exact `Rig` / `ordered` / `split_long_faces` / `editor` / `build_geometry` pipeline from `tools/dread-art/` and `tools/bake_rigs.py`.
- **UV & Texture Atlas**: 512x128 sheet divided into 16 64x64 material tiles (`0..15`). Faces longer than `15.5` model pixels are split automatically by `split_long_faces()` so vanilla box-UVs never bleed across tiles.
- **Selective Emissives (`_glow.png`)**: Only slots `LIGHT` (11), `POWER` (12), and `GLOW` (13) are written to `<id>_glow.png` (alpha 0 everywhere else), keeping jumpsuits, armor plating, and gunmetal hulls dark in shadow while Crewman visors, Dera plasma rails, MOA optics/muzzles, and Shield Osprey thruster louvers and shield emitters stay luminous at night.
- **Animation Rig Contracts (`CorpusMotion.java`)**:
  - `corpus_crewman` (`143`): humanoid bone set (`body`, `head`, `r_arm`/`l_arm`, `r_forearm`/`l_forearm`, `r_hand`/`l_hand`, `r_leg`/`l_leg`, `r_shin`/`l_shin`, `r_foot`/`l_foot`, `gun`).
  - `corpus_moa` (`144`): reverse-jointed walker bone set (`body`, `thorax`, `head`, `turret`, `gun`, `r_leg`/`l_leg`, `r_shin`/`l_shin`, `r_ankle`/`l_ankle`, `r_foot`/`l_foot`).
  - `corpus_shield_osprey` (`145`): hovering drone bone set (`body`, `thorax`, `head`, `r_wing`/`l_wing`, `tail`, `r_prong`/`l_prong`).
