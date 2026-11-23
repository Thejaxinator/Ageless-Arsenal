package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.tenno.abilities.WarframeAbilityId;
import com.jax.destiny2mc.tenno.abilities.WarframeId;
import java.util.List;

/** Void-Bound active powers, passives, and parkour exposed as first-class Energy Binder entries.
 * Active IDs match WarframeAbilityId.key() so BinderState cooldowns and slots drive them directly. */
public final class WarframeBinderCatalog {
    public static final String TENNO_PARKOUR = "wf_tenno_parkour";
    public static final String PASSIVE_URIEL = "wf_passive_uriel";
    public static final String PASSIVE_EXCALIBUR = "wf_passive_excalibur";
    public static final String PASSIVE_MAG = "wf_passive_mag";
    public static final String PASSIVE_VOLT = "wf_passive_volt";
    public static final String PASSIVE_NYX = "wf_passive_nyx";
    public static final String PASSIVE_KULLERVO = "wf_passive_kullervo";
    public static final String PASSIVE_VORUNA = "wf_passive_voruna";
    public static final String PASSIVE_VALKYR = "wf_passive_valkyr";
    public static final String PASSIVE_FROST = "wf_passive_frost";
    public static final String PASSIVE_FOLLIE = "wf_passive_follie";

    private static final List<BinderAbility> ACTIVES = List.of(
        active(WarframeAbilityId.EXCALIBUR_SLASH_DASH, "kinetic", 10, 3,
            "Dash forward along your sightline, cleaving every hostile in your path."),
        active(WarframeAbilityId.URIEL_INFERNALIS, "solar", 12, 3,
            "Launch upward in hellfire, gliding on flames while scorching nearby enemies."),
        active(WarframeAbilityId.URIEL_BRIMSTONE, "solar", 16, 4,
            "Erupt in a ring of brimstone that blasts and ignites all nearby hostiles."),
        active(WarframeAbilityId.MAG_PULL, "arc", 10, 3,
            "Magnetically yank hostiles in front of you into close range with electric force."),
        active(WarframeAbilityId.VOLT_SPEED, "arc", 11, 3,
            "Surge with lightning, granting Speed III and Haste II to yourself and nearby allies."),
        active(WarframeAbilityId.NYX_CHAOS, "void", 14, 4,
            "Fracture hostile minds in a wide radius, weakening them and turning them on each other."),
        active(WarframeAbilityId.KULLERVO_WRATHFUL_ADVANCE, "kinetic", 12, 3,
            "Teleport behind your aimed target for a heavy strike and amplified melee critical power."),
        active(WarframeAbilityId.VORUNA_SHROUD_OF_DYNAR, "void", 12, 3,
            "Cloak in invisibility, break enemy target lock, and empower your next melee ambush."),
        active(WarframeAbilityId.VALKYR_WARCRY, "solar", 14, 4,
            "Unleash a rallying roar for Resistance, Haste, Strength, and melee lifesteal while slowing foes."),
        active(WarframeAbilityId.FROST_SNOW_GLOBE, "stasis", 14, 4,
            "Create a protective freezing dome that pushes back foes and intercepts enemy projectiles."),
        active(WarframeAbilityId.FOLLIE_INKWELL_MIRAGE, "void", 12, 3,
            "Vault backward in a cloud of blinding ink that slows and damages pursuing hostiles.")
    );

    private static final List<BinderAbility> PASSIVES = List.of(
        new BinderAbility(TENNO_PARKOUR, "Void-Bound Parkour", "kinetic", "warframe_movement",
            "Unlock Void-Bound movement: Crouch+Jump to Bullet Jump, Double Jump, Wall Hop, Wall Latch, Aim Glide, and Slide.",
            12, 3, 0),
        passive(PASSIVE_EXCALIBUR, "Excalibur: Swordsmanship", "kinetic",
            "Swords strike 15% harder and grant Haste I while held.", 14, 3),
        passive(PASSIVE_URIEL, "Uriel: Hellfire Body", "solar",
            "Extinguish flames automatically and maintain permanent Fire Resistance.", 14, 3),
        passive(PASSIVE_MAG, "Mag: Magnetic Vacuum", "arc",
            "Continuously pull nearby dropped items within 6 blocks toward you.", 12, 3),
        passive(PASSIVE_VOLT, "Volt: Static Discharge", "arc",
            "Build static charge while moving on the ground, discharging bonus shock damage on melee hits.", 16, 4),
        passive(PASSIVE_NYX, "Nyx: Psychic Evasion", "void",
            "20% chance to phase through and completely negate incoming attacks.", 18, 4),
        passive(PASSIVE_KULLERVO, "Kullervo: Heavy Blade", "kinetic",
            "Deal 15% increased outgoing melee damage with all weapons.", 16, 4),
        passive(PASSIVE_VORUNA, "Voruna: Pack Stride", "void",
            "Maintain constant Speed I while navigating the battlefield.", 14, 3),
        passive(PASSIVE_VALKYR, "Valkyr: Nimble Landing", "solar",
            "Absorb hard landings seamlessly, negating fall damage from any height.", 14, 3),
        passive(PASSIVE_FROST, "Frost: Cryogenic Shell", "stasis",
            "25% chance to freeze and heavily slow attackers that strike you.", 16, 4),
        passive(PASSIVE_FOLLIE, "Follie: Ink Trail", "void",
            "Leave a shadowy ink trail behind you while sprinting.", 10, 2)
    );

    private WarframeBinderCatalog() {}

    private static BinderAbility active(WarframeAbilityId id, String element, int motes, int levels, String desc) {
        return new BinderAbility(id.key(), id.displayName(), element, "warframe_active", desc, motes, levels, id.cooldownTicks());
    }

    private static BinderAbility passive(String id, String name, String element, String desc, int motes, int levels) {
        return new BinderAbility(id, name, element, "warframe_passive", desc, motes, levels, 0);
    }

    public static List<BinderAbility> actives() { return ACTIVES; }
    public static List<BinderAbility> passives() { return PASSIVES; }

    public static WarframeId warframeFor(String abilityId) {
        if (abilityId == null || abilityId.isEmpty()) return null;
        WarframeAbilityId active = WarframeAbilityId.byKey(abilityId);
        if (active != null) return active.warframe();
        if (abilityId.startsWith("wf_passive_")) {
            return WarframeId.byKey(abilityId.substring("wf_passive_".length()));
        }
        return null;
    }

    public static int accentColor(String abilityId) {
        WarframeId wf = warframeFor(abilityId);
        return wf != null ? (0xFF000000 | wf.accentRgb()) : 0xFF3FA9D6;
    }
}
