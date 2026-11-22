# Ability HUD skins

The selected ammo HUD skin also styles the four active ability slots. The controls settings supply the current binding for each slot. The Energy Binder supplies the ability ID and cooldown deadline. Moving the same ability to another slot preserves its symbol and remaining cooldown.

| HUD setting | Ready | Recharging |
|---|---|---|
| Destiny 2 FUI | A white ability pictogram sits in a square of its own element color. Mixed loadouts retain each element. | Color refills upward behind the pictogram. Remaining seconds appear at its lower edge. |
| Minecraft Compact | Four cells use Minecraft's hotbar sprite. Each cell carries the ability pictogram in its element color. | A translucent cooldown wipe follows the vanilla item overlay convention. |
| Simple Minimal | Four small pictograms with binding labels and a one-pixel charge line. | The line grows beneath the muted icon and a timer gives the remaining seconds. |
| Borderlands 2 | Four green action-skill badges with inked edges, a lit rim and pale skill emblems. | The badge turns graphite and a narrow green gauge refills as the cooldown expires. |
| Borderlands 4 | Four angular action-skill plates with colored bracket corners and illustrated emblems. Offensive powers use warm red, defensive and restorative powers green, and crowd-control powers blue. | The emblem mutes, three charge marks illuminate in sequence, and a timer shows the remaining seconds. |
| Warframe Tenno | Four floating pale glyphs with shadows and the actual bound key beneath each. | The silhouette dims and the timer appears in ice blue. |

The renderer reserves all four positions whenever at least one active ability is equipped. An empty bind has a quiet socket glyph. In the gameplay HUD the shared layout owns the lower-left anchor, HUD scale, visibility and current ammo-counter bounds. The same style renderer runs inside the HUD settings preview.

## Artwork and extensions

`com.jax.destiny2mc.binder.client.AbilityHudStyles.draw` accepts four `Slot` records. Each holds a nullable catalog entry, a translated key name and remaining cooldown ticks. `width` and `height` return logical GUI dimensions including labels. Outer callers own the pose and scale.

The three original Borderlands plates live under `assets/destiny2mc/textures/gui/abilities/`. Minecraft Compact reads the resource pack's `minecraft:hud/hotbar` GUI sprite. Each ability's stencil is a separate 16-pixel GUI sprite, tintable by skin. New power IDs can supply `assets/destiny2mc/textures/gui/sprites/binder/ability/<id>.png`. The Tenno renderer uses no surrounding plate, meter or back panel.

## Visual references

- Bailey Kalesti's in-game Revenant, Behemoth and Shadebinder screenshots show the element-colored square ability grouping and white pictograms. [Destiny 2 HUD and ability artwork](https://www.baileykalesti.com/destiny-2)
- Digital Extremes places the ability readout below weapon status. Warframe's four independent glyphs, muted unavailable state and numerical timers informed the floating skin. [Mission Interface](https://support.warframe.com/hc/en-us/articles/38801911653517-Mission-Interface) · [Warframe HUD, Abilities](https://wiki.warframe.com/w/Heads-Up_Display)
- The Borderlands 2 PC manual illustrates the skill with a charged HUD symbol, and the ready/used states give the in-game action skill a green/gray distinction. [Borderlands 2 PC manual, “Killing with Skill”](https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/49520/manuals/BL2_PC_Long_ENG_ManInt_081712_HighRes.pdf) · [HUD states](https://www.trueachievements.com/game/borderlands-2/walkthrough/2)
- Kade O'Casey's Borderlands 4 work distinguishes aggressive, generative and control roles with red, green and blue action-skill illustrations. The mod uses those role colors on its own stencils in the four-slot layout. [Borderlands 4 Action Skill Artwork](https://kadespo.artstation.com/projects/3EGnEE?album_id=15005365)
