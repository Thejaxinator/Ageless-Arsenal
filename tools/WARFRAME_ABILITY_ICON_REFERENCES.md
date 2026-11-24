# Warframe Ability & Passive Icon References (Warframe Wiki)

All 40 Void-Bound Energy Binder sprites in `src/main/resources/assets/destiny2mc/textures/gui/sprites/binder/ability/` are sourced via `tools/bake_warframe_ability_icons.py` from official Warframe Wiki ability icons (`wiki.warframe.com`, falling back to `warframe.fandom.com` / `warframe.fandom.com/ru`).

## Active Abilities (23)
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
| `follie_inkwell_mirage` | Follie | Inkwell Mirage *(Self Portrait)* | `File:SelfPortrait130xWhite.png` / `File:Автопортрет_иконка_вики.png` |
| `citrine_prismatic_gem` | Citrine | Prismatic Gem | `File:PrismaticGem130xWhite.png` |
| `citrine_crystallize` | Citrine | Crystallize | `File:Crystallize130xWhite.png` |
| `styanax_tharros_strike` | Styanax | Tharros Strike | `File:TharrosStrike130xWhite.png` |
| `styanax_final_stand` | Styanax | Final Stand | `File:FinalStand130xWhite.png` |
| `gyre_coil_horizon` | Gyre | Coil Horizon | `File:CoilHorizon130xWhite.png` |
| `gyre_rotorswell` | Gyre | Rotorswell | `File:Rotorswell130xWhite.png` |
| `caliban_sentient_wrath` | Caliban | Sentient Wrath | `File:SentientWrath130xWhite.png` |
| `caliban_razor_gyre` | Caliban | Razor Gyre | `File:RazorGyre130xWhite.png` |
| `yareli_aquablades` | Yareli | Aquablades | `File:Aquablades130xWhite.png` |
| `yareli_riptide` | Yareli | Riptide | `File:Riptide130xWhite.png` |
| `lavos_vial_rush` | Lavos | Vial Rush | `File:VialRush130xWhite.png` |
| `lavos_catalyze` | Lavos | Catalyze | `File:Catalyze130xWhite.png` |

## Passives & Parkour (17)
Warframe Wiki displays each Warframe's `*Icon272.png` portrait next to the passive (except Uriel's Demon Legion HUD gauge). `bake_warframe_ability_icons.py` converts those portraits into monochrome white-on-alpha emblems so `AbilityGlyph` tints them cleanly.

| Binder ID | Entry | Warframe Wiki Icon File |
| :--- | :--- | :--- |
| `wf_tenno_parkour` | Void-Bound Parkour | `File:IconClanObstacleCourse.png` (fallback: `File:LotusLogoWhite.png`) |
| `wf_passive_excalibur` | Excalibur: Swordsmanship | `File:ExcaliburIcon272.png` |
| `wf_passive_uriel` | Uriel: Hellfire Body | `File:Демоны_Уриэля_вики.png` (fallback: `File:UrielIcon272.png`) |
| `wf_passive_mag` | Mag: Magnetic Vacuum | `File:MagIcon272.png` |
| `wf_passive_volt` | Volt: Static Discharge | `File:VoltIcon272.png` |
| `wf_passive_nyx` | Nyx: Psychic Evasion | `File:NyxIcon272.png` |
| `wf_passive_kullervo` | Kullervo: Heavy Blade | `File:KullervoIcon272.png` |
| `wf_passive_voruna` | Voruna: Pack Stride | `File:VorunaIcon272.png` |
| `wf_passive_valkyr` | Valkyr: Nimble Landing | `File:ValkyrIcon272.png` |
| `wf_passive_frost` | Frost: Cryogenic Shell | `File:FrostIcon272.png` |
| `wf_passive_follie` | Follie: Inkblot | `File:FollieIcon272.png` / `File:Фолли_вики.png` |
| `wf_passive_citrine` | Citrine: Geoluminescence | `File:CitrineIcon272.png` |
| `wf_passive_styanax` | Styanax: Hoplite | `File:StyanaxIcon272.png` |
| `wf_passive_gyre` | Gyre: Cathode Surge | `File:GyreIcon272.png` |
| `wf_passive_caliban` | Caliban: Adaptive Armor | `File:CalibanIcon272.png` |
| `wf_passive_yareli` | Yareli: Critical Flow | `File:YareliIcon272.png` |
| `wf_passive_lavos` | Lavos: Valence Alchemist | `File:LavosIcon272.png` |
