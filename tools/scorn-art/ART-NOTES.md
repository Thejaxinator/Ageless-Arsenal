# Tangled Shore Scorn

The eight existing Scorn registry entries have new independent rigs, materials and poses. Keep the registry namespace and the kind numbers below when loading an old world. The game and the bestiary both draw the same model and sample the same attack clock.

| Kind | Model | Material and equipment study | Animated detail |
|---|---|---|---|
| 113 | Stalker | Pale flesh, two docked shoulder stumps, ochre perforated mask, crossed leather harness, uneven leg armor | Upper body turns into the three-round burst. The pistol follows the palm. |
| 114 | Raider | Raised helmet blades, two additional arms behind the rib cage, ragged loincloth, forked Scorn crossbow | The extra arms counterbalance the stride and the body compresses during mist travel. |
| 115 | Lurker | Compact mask, layered round shield with oxidized green channel and a convex yellow boss | Wrist and shield counterrotate so the face of the buckler stays between the Lurker and its target during a block. |
| 116 | Ravager | Sealed neck stump, exposed throat brace, metal wraps, hot censer suspended from an open chain | The raised hand leads, followed by chain and lamp. Impact lines up with the native tick-14 melee hit. |
| 117 | Chieftain | Horizontal helmet bars, segmented shoulder iron, yellow-laced skirts and a three-aperture launcher | The upper left palm raises for an Ether totem while the lower arms spread. |
| 118 | Screeb | Low hunched flesh, six contact limbs, split claws, six asymmetric blue Ether bladders | Alternating limbs scuttle. During its existing detonation cast the body compresses and the sacs swell. |
| 119 | Abomination | Exposed bloated chest, stubby black shoulder nodules, embedded face iron and bound heavy forearms | Chest rolls over short strides. Both hands open and lift to channel Arc. |
| 133 | Fikrul, the Fanatic | Forsaken crown with paired open crossbars, four arms, long split skirts, upright forked staff | Lower arms and coat panels counterbalance movement. The staff follows the upper right wrist during sweep and cast. |

## Edit and build

Run `python bake.py` with Python 3 and Pillow from this folder. No network connection is needed. The script produces all eight native geometry classes, base and glow textures in `stage/`, part hierarchies in `models/`, and textured Blockbench projects in `blockbench/`. Edit the recipe in `art.py` for repeatable geometry or material changes. The Blockbench exports are editable studies. This recipe build reads `art.py`; it does not import manual Blockbench edits. The Python rig and bytecode tools are included under `tools/`.

The body pivot uses Minecraft's ground plane at Y=24. X and Z inputs to `Rig.box` are center coordinates. Y is the top of the cuboid. Children use local coordinates and inherit their parent's rotation. Rest-pose angles passed to `joint` are degrees. Material cells are 64 × 64 pixels in a 512 × 192 atlas. Indices 14–18 are reserved for emissive surfaces, including different elements. Helmet ventilation holes and straps stay dark.

Compile the Java files in `src/` with JDK 21, the Ageless Arsenals or Overdrive base JAR, and the project's NeoForge development compile classpath. Place the class files in `stage/`. Copy the entire stage into the JAR and retain the client mixin entry below in `META-INF/neoforge.mods.toml`.

```toml
[[mixins]]
config="ageless.scorn.mixins.json"
```

`ScornModelMixin` intercepts the common model's pose method only for the Scorn faction. Other factions keep their renderer and pose. `ScornPose` binds each optional named joint once at model construction and resets to its native bind pose at the start of every sample. If a Scorn unit is added, update the kind-to-artist table, add the model-specific motion, and capture both rest and active poses. Its server attack, animation duration and damage timing still belong in the encounter logic.

## Visual references

The original Tangled Shore / Forsaken appearance is the baseline. Alternate Throne World and Revenant variants were used to check articulation and equipment where their screenshots showed a clearer angle. Paint, geometry and animation in the distributable mod are original Minecraft adaptations. Reference photos are kept outside the JAR.

| Subject | Inspected reference frames |
|---|---|
| Stalker | [Tangled Shore](https://www.destinypedia.com/File:StalkerInfo26.jpg) · [Combat screenshot](https://www.destinypedia.com/File:Scornstalker.jpg) |
| Raider | [Tangled Shore](https://www.destinypedia.com/File:RaiderInfo26.jpg) · [Crossbow and profile](https://www.destinypedia.com/File:Eugene-kim-ek-screenshot-13.jpg) |
| Lurker | [Ochre shield](https://www.destinypedia.com/File:Lurker26.jpg) · [Shield face in another finish](https://www.destinypedia.com/File:LurkerThroneWorldInfo.jpg) |
| Ravager | [Solar censer](https://www.destinypedia.com/File:RavagerInfo26.jpg) · [Alternate element](https://www.destinypedia.com/File:RavTWStasis.jpg) |
| Chieftain | [Tangled Shore arms and harness](https://www.destinypedia.com/File:VoidReefChieftan.jpg) · [Arc crest](https://www.destinypedia.com/File:Chieftain_(Arc)_(Yellow).jpg) |
| Screeb | [Body and sacs](https://www.destinypedia.com/File:ScreebInfo.jpg) · [Forelimbs and belly](https://www.destinypedia.com/File:Screeby2.jpg) |
| Abomination | [Face plate and exposed body](https://www.destinypedia.com/File:AbominationInfo.jpg) · [Arc cast](https://www.destinypedia.com/File:Abe-abomination.jpg) |
| Fikrul | [Forsaken staff and crown](https://www.destinypedia.com/File:Fikrulthefanatic1.png) · [Full figure](https://www.destinypedia.com/File:FikrulInfoboxTemp.jpg) · [Later crown variant](https://www.destinypedia.com/File:RevenantFikrulCharacter.jpg) |
