# Warframe Ability & Passive Icon References (Warframe Wiki)

All 22 Energy Binder Warframe sprites in `src/main/resources/assets/destiny2mc/textures/gui/sprites/binder/ability/` are sourced via `tools/bake_warframe_ability_icons.py` from official Warframe Wiki ability icons (`wiki.warframe.com`, falling back to `warframe.fandom.com` / `warframe.fandom.com/ru`).

## Active Abilities (11)
| Binder ID | Warframe | Mod Name | Warframe Wiki Icon File |
| :--- | :--- | :--- | :--- |
| `excalibur_slash_dash` | Excalibur | Slash Dash | `File:SlashDash130xWhite.png` |
| `uriel_infernalis` | Uriel | Infernalis | `File:Infernalis130xWhite.png` / `File:Инферналис_иконка_вики.png` |
| `uriel_brimstone` | Uriel | Brimstone | `File:Brimstone130xWhite.png` / `File:Адское_Пламя_иконка_вики.png` |
| `mag_pull` | Mag | Pull | `File:Pull130xWhite.png` |
| `volt_speed` | Volt | Speed | `File:Speed130xWhite.png` |
| `nyx_chaos` | Nyx | Chaos | `File:Chaos130xWhite.png` |
| `kullervo_wrathful_advance` | Kullervo | Wrathful Advance | `File:WrathfulAdvance130xWhite.png` / `File:WrathfulAdvanceIcon.png` |
| `voruna_shroud_of_dynar` | Voruna | Shroud of Dynar | `File:ShroudOfDynar130xWhite.png` / `File:ShroudofDynarIcon.png` |
| `valkyr_warcry` | Valkyr | Warcry | `File:Warcry130xWhite.png` |
| `frost_snow_globe` | Frost | Snow Globe | `File:SnowGlobe130xWhite.png` |
| `follie_inkwell_mirage` | Follie | Inkwell Mirage *(custom mod name)* | `File:SelfPortrait130xWhite.png` / `File:Автопортрет_иконка_вики.png` *(fallback: Forced Perspective)* |

## Passives & Parkour (11)
Warframe Wiki does not host separate white-on-alpha passive icons for most Warframes (it displays each Warframe's `*Icon272.png` portrait next to the passive, except Uriel's Demon Legion HUD gauge). `bake_warframe_ability_icons.py` converts those portraits into monochrome white-on-alpha emblems so `AbilityGlyph` tints them cleanly.

| Binder ID | Entry | Warframe Wiki Icon File |
| :--- | :--- | :--- |
| `wf_tenno_parkour` | Tenno Parkour | `File:IconClanObstacleCourse.png` (fallback: `File:LotusLogoWhite.png`) |
| `wf_passive_excalibur` | Excalibur: Swordsmanship | `File:ExcaliburIcon272.png` |
| `wf_passive_uriel` | Uriel: Hellfire Body | `File:Демоны_Уриэля_вики.png` (fallback: `File:UrielIcon272.png`) |
| `wf_passive_mag` | Mag: Magnetic Vacuum | `File:MagIcon272.png` |
| `wf_passive_volt` | Volt: Static Discharge | `File:VoltIcon272.png` |
| `wf_passive_nyx` | Nyx: Psychic Evasion | `File:NyxIcon272.png` |
| `wf_passive_kullervo` | Kullervo: Heavy Blade | `File:KullervoIcon272.png` |
| `wf_passive_voruna` | Voruna: Pack Stride | `File:VorunaIcon272.png` |
| `wf_passive_valkyr` | Valkyr: Nimble Landing | `File:ValkyrIcon272.png` |
| `wf_passive_frost` | Frost: Cryogenic Shell | `File:FrostIcon272.png` |
| `wf_passive_follie` | Follie: Ink Trail | `File:FollieIcon272.png` / `File:Фолли_вики.png` |
