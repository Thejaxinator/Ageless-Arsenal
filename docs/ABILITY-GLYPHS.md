# Ability glyphs

Each of the 30 active abilities and eight passives has an original 16 × 16 pixel pictogram. A healing rift is a low well with a cross. Empowering Rift uses an upward arrow and energy lines. Towering and Rally Barricade use different cover heights. The same individual marks work across game HUD themes and the inventory drawer; any theme can supply its own vertex tint.

The resource-pack address is `assets/destiny2mc/textures/gui/sprites/binder/ability/<ability_id>.png`. Minecraft stitches these sprites into its GUI atlas at load time. Author shapes against a transparent background on a white 16 × 16 pixel grid. Keep the distinctive interior shapes visible at a destination size of 12–16 GUI pixels. A new ability automatically picks up the file whose basename matches its catalog ID. Empty slots use `unbound.png` and abilities missing an icon use `unidentified.png`. Resource packs can replace any individual file, preserving the stable catalog and save IDs.

`AbilityGlyph.draw(graphics, abilityId, x, y, size, argb)` uses pixel bounds in the active GUI pose, supplies color and opacity as per-vertex values and requests the current atlas sprite on each draw so reloading resource packs refreshes the art.

The checked-in PNGs are runtime assets. To edit and regenerate the original set, install Pillow and run `python tools/ability_glyphs.py src/main/resources` from the release source. The authoring tool validates hard alpha, foreground coverage and distinct masks at 12 px and 16 px.
