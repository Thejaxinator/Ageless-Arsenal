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
    public static final String PASSIVE_CITRINE = "wf_passive_citrine";
    public static final String PASSIVE_STYANAX = "wf_passive_styanax";
    public static final String PASSIVE_GYRE = "wf_passive_gyre";
    public static final String PASSIVE_CALIBAN = "wf_passive_caliban";
    public static final String PASSIVE_YARELI = "wf_passive_yareli";
    public static final String PASSIVE_LAVOS = "wf_passive_lavos";

    private static final List<BinderAbility> ACTIVES = List.of(
        active(WarframeAbilityId.EXCALIBUR_SLASH_DASH, "kinetic", 10, 3,
            "Chain-dash through up to 5 enemies while invulnerable, inflicting heavy Slash bleed and an Exalted energy wave."),
        active(WarframeAbilityId.URIEL_INFERNALIS, "solar", 12, 3,
            "Launch into hellfire flight for 7s, scorching and weakening nearby foes in a blazing demon-flame aura."),
        active(WarframeAbilityId.URIEL_BRIMSTONE, "solar", 16, 4,
            "Erupt a 10-block molten brimstone field that pulses with ramping hellfire and Wither for 6s."),
        active(WarframeAbilityId.MAG_PULL, "arc", 10, 3,
            "Violently yank enemies and loot into a magnetic vortex in front of you, dealing bonus damage to armored foes and granting Absorption."),
        active(WarframeAbilityId.VOLT_SPEED, "arc", 11, 3,
            "Overcharge yourself and allies with Speed III, Haste III, faster reloads, and a Shocking Speed lightning aura for 12s."),
        active(WarframeAbilityId.NYX_CHAOS, "void", 14, 4,
            "Shatter hostile minds in an 18-block field for 12s, forcing mobs to fight each other while taking +35% bonus damage."),
        active(WarframeAbilityId.KULLERVO_WRATHFUL_ADVANCE, "kinetic", 12, 3,
            "Teleport to your aimed foe for a devastating Heavy Attack cleave and 10s of +120% red-crit melee power."),
        active(WarframeAbilityId.VORUNA_SHROUD_OF_DYNAR, "void", 12, 3,
            "Cloak in invisibility with Speed III; breaking stealth triggers 4s of 2x melee crits and heavy Slash bleed extended by kills."),
        active(WarframeAbilityId.VALKYR_WARCRY, "solar", 14, 4,
            "Unleash an Eternal Warcry granting Resistance II, Haste IV, Strength II, Absorption, and 18% melee lifesteal extended by melee kills."),
        active(WarframeAbilityId.FROST_SNOW_GLOBE, "stasis", 14, 4,
            "Flash-freeze and slam foes outward inside an 18s cryo dome that blocks projectiles, buffs allies, chills enemies, and shatters on expiry."),
        active(WarframeAbilityId.FOLLIE_INKWELL_MIRAGE, "void", 12, 3,
            "Vault back in brief invisibility, leaving a taunting Ink Effigy for 8s that lashes foes with blinding ink before detonating."),
        active(WarframeAbilityId.CITRINE_PRISMATIC_GEM, "prismatic", 14, 4,
            "Deploy a hovering Prismatic Gem for 12s that fires Heat, Cold, Toxin, and Electric beams at foes you attack and buffs nearby allies."),
        active(WarframeAbilityId.CITRINE_CRYSTALLIZE, "stasis", 16, 4,
            "Send seeking crystal fractals to paralyze enemies for 6s; striking crystallized foes triggers a 2x Critical Shatter burst."),
        active(WarframeAbilityId.STYANAX_THARROS_STRIKE, "kinetic", 12, 3,
            "Sweep your shield in a wide frontal cone, knocking foes back, stripping defenses (+40% damage taken), and healing per enemy struck."),
        active(WarframeAbilityId.STYANAX_FINAL_STAND, "arc", 16, 4,
            "Rise into the air for 3.5s and rain a rapid barrage of 14 explosive Axios Javelins toward your aim reticle."),
        active(WarframeAbilityId.GYRE_COIL_HORIZON, "arc", 12, 3,
            "Launch a Gyratory Sphere that implodes after 1s, pulling enemies into an 11-block electric vortex and shocking them."),
        active(WarframeAbilityId.GYRE_ROTORSWELL, "arc", 16, 4,
            "Surround yourself in an Arc field for 14s; every hit you land triggers chaining high-voltage lightning bolts, extended on kills."),
        active(WarframeAbilityId.CALIBAN_SENTIENT_WRATH, "void", 14, 4,
            "Slam the ground to unleash a 14-block Tau shockwave that lifts enemies helplessly into the air with +45% damage vulnerability."),
        active(WarframeAbilityId.CALIBAN_RAZOR_GYRE, "void", 12, 3,
            "Spin into a high-speed Tau vortex dash that shreds foes (2x vs lifted targets) while restoring Health and Absorption."),
        active(WarframeAbilityId.YARELI_AQUABLADES, "stasis", 12, 3,
            "Orbit yourself with 3 whirling blades of pressurized water for 13s, continuously slicing and bleeding nearby enemies."),
        active(WarframeAbilityId.YARELI_RIPTIDE, "stasis", 16, 4,
            "Conjure a crushing water cyclone at your aim point that traps enemies for 2.5s before detonating for Cold damage scaling per captive."),
        active(WarframeAbilityId.LAVOS_VIAL_RUSH, "stasis", 12, 3,
            "Dash forward shattering alchemical vials that smash foes and leave freezing, toxic pools for 6s."),
        active(WarframeAbilityId.LAVOS_CATALYZE, "solar", 16, 4,
            "Unleash a 16-block wave of Catalyst Probes dealing massive alchemical damage multiplied by +50% per active status effect on each foe.")
    );

    private static final List<BinderAbility> PASSIVES = List.of(
        new BinderAbility(TENNO_PARKOUR, "Void-Bound Parkour", "kinetic", "warframe_movement",
            "Unlock Void-Bound movement: Crouch+Jump to Bullet Jump, Double Jump, Wall Hop, Wall Latch, Aim Glide, and Slide.",
            12, 3, 0),
        passive(PASSIVE_EXCALIBUR, "Excalibur: Swordsmanship", "kinetic",
            "Swords strike 25% harder and grant Haste II while held.", 14, 3),
        passive(PASSIVE_URIEL, "Uriel: Hellfire Body", "solar",
            "Permanent Fire Resistance, Regeneration near fire or burning foes, and attackers ignite on contact.", 14, 3),
        passive(PASSIVE_MAG, "Mag: Magnetic Vacuum", "arc",
            "Continuously pull dropped items and XP orbs within 11 blocks, granting short Absorption on pickup.", 12, 3),
        passive(PASSIVE_VOLT, "Volt: Static Discharge", "arc",
            "Build up to 150 static charge while moving to unleash +18 chain lightning damage on your next hit.", 16, 4),
        passive(PASSIVE_NYX, "Nyx: Psychic Evasion", "void",
            "25% chance to phase through incoming attacks and reflect a weakening psychic bolt back at the attacker.", 18, 4),
        passive(PASSIVE_KULLERVO, "Kullervo: Heavy Blade", "kinetic",
            "Deal 25% increased melee damage, and every 3rd hit delivers a guaranteed 1.5x heavy strike.", 16, 4),
        passive(PASSIVE_VORUNA, "Voruna: Pack Stride", "void",
            "Maintain constant Speed II and Jump Boost I while navigating the battlefield.", 14, 3),
        passive(PASSIVE_VALKYR, "Valkyr: Nimble Landing", "solar",
            "Negate all fall damage and unleash a radial shockwave slam when landing from over 4 blocks.", 14, 3),
        passive(PASSIVE_FROST, "Frost: Cryogenic Shell", "stasis",
            "35% chance to deep-freeze and counter-blast attackers, plus +15% damage against slowed or frozen foes.", 16, 4),
        passive(PASSIVE_FOLLIE, "Follie: Inkblot", "void",
            "Sprinting leaves a blinding, slowing ink wake that damages pursuing enemies.", 12, 3),
        passive(PASSIVE_CITRINE, "Citrine: Geoluminescence", "solar",
            "Continuously regenerate health for yourself and nearby allies, scaling higher with every kill.", 16, 4),
        passive(PASSIVE_STYANAX, "Styanax: Hoplite", "arc",
            "Deal +20% damage while above 75% health or shielded, and gain stacking Absorption shields on kills.", 16, 4),
        passive(PASSIVE_GYRE, "Gyre: Cathode Surge", "arc",
            "Deal +25% bonus critical damage to shocked, slowed, or glowing foes with a 30% chance to arc chain lightning.", 18, 4),
        passive(PASSIVE_CALIBAN, "Caliban: Adaptive Armor", "void",
            "Build up to 50% stacking damage reduction as you take hits and share Resistance with nearby allies.", 18, 4),
        passive(PASSIVE_YARELI, "Yareli: Critical Flow", "stasis",
            "Moving continuously for 1.5s grants +35% outgoing damage and Speed I until you stop.", 16, 4),
        passive(PASSIVE_LAVOS, "Lavos: Valence Alchemist", "strand",
            "Automatically cleanse harmful status effects and imbue attacks with cycling elemental procs every 3s.", 18, 4)
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
