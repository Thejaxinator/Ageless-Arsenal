# Warframe creative inventory

Open Creative inventory and use the page arrows to reach **Warframe**, marked by the Twin Grakatas. The tab follows **Paracausal** in the default ordering. Minecraft's global Search also indexes the items here.

| Slots, counted from one | Contents |
| --- | --- |
| 1–19 | Primary weapons, starting with the AX-52 |
| 20–27 | Secondary weapons, starting with Twin Grakatas |
| 28–33 | Melee weapons, starting with Amanata |
| 34–37 | Mod Pack, Primary Mod Pack, Secondary Mod Pack and Melee Mod Pack |

## Source

- `TennoCreativeTab` registers `destiny2mc:warframe` with the translation key `itemGroup.destiny2mc.warframe`. This tab builds new default item stacks from the existing item registry.
- `TennoGunSpec` and `MeleeProfile.ALL` order their respective weapons. Rifle and sidearm membership follow the same equipment classification used for mod compatibility in `WeaponMod`. Keep variants adjacent in declaration order.
- `ModPacks.Pool` supplies the four registered pack items. Each new entry uses the native parent-and-search visibility.
- Destiny's existing tab uses `destiny2mc:arsenal`. Scope tab-content subscribers to the specific tab key. The Sphinx egg subscriber also supplies vanilla Spawn Eggs.
- Survival continues to use foundry blueprints and crafting recipes. Creative tab registration creates no new item IDs and writes no custom data to stacks or player saves.

## Release verification

Run `WarframeCreativeTabCheck` as a test-only event subscriber with the release on a disposable NeoForge 21.1.266 server. The test rebuilds vanilla tabs with and without operator items, checks the hand-authored 37-item sequence and the search index, and crafts through the survival Foundry and crafting recipe manager. The accompanying result records two fingerprints for the ordered contents of the existing category tabs, excluding Warframe items. They are compared with the original release running the same fixture and Minecraft version.

From an extracted source root, run the following command with Java 21 to repeat the creative checks. On the first run, `--prepare` generates the SDK from the included Gradle project.

```sh
python tests/navigation/creative/run.py /path/to/release.jar --sdk /path/to/test-sdk --prepare
```
