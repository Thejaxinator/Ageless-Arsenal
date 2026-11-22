package com.jax.destiny2mc.tenno;

import java.util.Locale;

/** Magazine sizes and weapon identities are based on their Warframe counterparts.
 * Damage, tick cadence, projectile speed and ranges are tuned to Minecraft's health scale. */
public enum TennoGunSpec {
    AX_52("AX-52", Family.AX, "Rifle", 40, 3.8f, 3, 40, 66),
    GRAKATA("Grakata", Family.GRAKATA, "Rifle", 60, 2.35f, 2, 48, 52),
    PRISMA_GRAKATA("Prisma Grakata", Family.GRAKATA, "Rifle", 120, 2.65f, 2, 40, 56),
    TWIN_GRAKATAS("Twin Grakatas", Family.TWINS, "Dual machine pistols", 120, 2.05f, 2, 60, 46),
    BRATON("Braton", Family.RIFLE, "Rifle", 45, 3.3f, 3, 40, 62),
    BRATON_PRIME("Braton Prime", Family.RIFLE, "Rifle", 75, 3.8f, 3, 43, 68),
    BRATON_VANDAL("Braton Vandal", Family.RIFLE, "Rifle", 50, 4.05f, 3, 35, 66),
    BURSTON("Burston", Family.BURST, "Burst rifle", 45, 3.4f, 8, 40, 68),
    BURSTON_PRIME("Burston Prime", Family.BURST, "Burst rifle", 45, 4.0f, 7, 40, 74),
    IGNIS("Ignis", Family.FLAME, "Flamethrower", 150, 2.5f, 3, 40, 12),
    IGNIS_WRAITH("Ignis Wraith", Family.FLAME, "Flamethrower", 200, 2.85f, 3, 34, 15),
    BRAKK("Brakk", Family.BRAKK, "Hand shotgun", 5, 7.8f, 5, 20, 24),
    KUVA_BRAKK("Kuva Brakk", Family.BRAKK, "Hand shotgun", 11, 9.0f, 5, 22, 27),
    NUKOR("Nukor", Family.MICROWAVE, "Beam pistol", 50, 2.8f, 3, 40, 20),
    KUVA_NUKOR("Kuva Nukor", Family.MICROWAVE, "Chain beam pistol", 77, 3.15f, 3, 40, 23),
    CEDO("Cedo", Family.CEDO, "Glaive shotgun", 40, 6.8f, 5, 44, 38),
    CEDO_PRIME("Cedo Prime", Family.CEDO, "Glaive shotgun", 40, 7.6f, 5, 36, 43),
    VESPER_77("Vesper 77", Family.VESPER, "Silenced pistol", 8, 5.7f, 5, 32, 64),
    PERIGALE("Perigale", Family.PERIGALE, "Burst sniper", 12, 5.4f, 15, 60, 96),
    PERIGALE_PRIME("Perigale Prime", Family.PERIGALE, "Burst sniper", 24, 5.9f, 14, 54, 104),
    SOMA("Soma", Family.SOMA, "Spool rifle", 100, 2.8f, 2, 60, 65),
    SOMA_PRIME("Soma Prime", Family.SOMA, "Spool rifle", 200, 3.1f, 2, 60, 70),
    CANTARE("Cantare", Family.CANTARE, "Throwing blades", 6, 4.4f, 4, 24, 40),
    TORID("Torid", Family.TORID, "Toxin launcher", 5, 6.0f, 12, 34, 48),
    AKSONDOL("Aksondol", Family.AKSONDOL, "Dual cold pistols", 8, 5.1f, 5, 52, 54),
    BUBONICO("Bubonico", Family.BUBONICO, "Infested arm-cannon", 27, 6.4f, 5, 30, 40),
    CODA_BUBONICO("Coda Bubonico", Family.BUBONICO, "Infested arm-cannon", 36, 7.1f, 5, 30, 44);

    public enum Family {
        AX("Auto","Hip fire spends one round every four shots. Aimed weak-point hits deal double damage."),
        GRAKATA("Auto","Fast automatic fire. Aim to tighten its spread."),
        TWINS("Dual auto","Both pistols fire per trigger cycle, spending one round per muzzle."),
        RIFLE("Auto","Sustained automatic fire. Aiming tightens the shot group."),
        BURST("3-round burst","One trigger press fires three individually timed rounds."),
        FLAME("Heat stream","An expanding stream ignites enemies within its cone."),
        BRAKK("Semi-auto","Ten pellets spread from each shot and lose damage with distance."),
        MICROWAVE("Radiation beam","A continuous microwave beam weakens its target. The Kuva beam chains twice."),
        CEDO("Auto / glaive","Shot damage rises with distinct status effects. Alternate fire ricochets an elemental glaive."),
        VESPER("Semi-auto","A quiet report. Its laser marks a sighted weak point and amplifies aimed weak-point damage."),
        PERIGALE("4-round burst","Four weak-point hits in one burst or a weak-point kill grants four seconds of free shots."),
        SOMA("Spool auto","The action accelerates over sustained fire. Precise shots gain a critical bonus."),
        CANTARE("Auto throw","Thrown blades lodge at impact. Reload recalls them, cutting targets across their return paths."),
        TORID("Semi-auto","An arcing spore grenade bursts into a lingering field of toxin."),
        AKSONDOL("Dual semi-auto","Alternating cold rounds slow enemies. Reload lays freezing mist scaled to spent rounds."),
        BUBONICO("Auto / salvo","Regenerating battery. Alternate fire spends up to nine charge to launch three viral bursts.");

        final String mode, note;
        Family(String mode, String note) { this.mode=mode; this.note=note; }
    }

    public final Family family;
    private final String title, kind;
    private final int magazine, delay, reload, range;
    private final float damage;
    TennoGunSpec(String title, Family family, String kind, int magazine, float damage, int delay, int reload, int range) {
        this.title=title; this.family=family; this.kind=kind; this.magazine=magazine;
        this.damage=damage; this.delay=delay; this.reload=reload; this.range=range;
    }
    public String id() { return name().toLowerCase(Locale.ROOT); }
    public String title() { return title; }
    public String kind() { return kind; }
    public int magazine() { return magazine; }
    public float damage() { return damage; }
    public int delay() { return delay; }
    public int reloadTicks() { return reload; }
    public int range() { return range; }
    public String mode() { return family.mode; }
    public String note() {
        if (this==NUKOR) return "A focused microwave beam weakens the target under the reticle.";
        if (this==KUVA_NUKOR) return "A weakening microwave beam jumps to up to two nearby visible targets.";
        return family.note;
    }
    public String variant() {
        if(name().endsWith("_PRIME")) return "Prime";
        if(name().startsWith("KUVA_")) return "Kuva";
        if(name().startsWith("CODA_")) return "Coda";
        if(name().startsWith("PRISMA_")) return "Prisma";
        if(name().endsWith("_WRAITH")) return "Wraith";
        if(name().endsWith("_VANDAL")) return "Vandal";
        return "Standard";
    }
    public boolean dual() { return family==Family.TWINS || family==Family.AKSONDOL || family==Family.CANTARE; }
    public boolean alternate() { return family==Family.CEDO || family==Family.BUBONICO; }
    public boolean automatic() {
        return switch(family) {
            case BURST, BRAKK, VESPER, PERIGALE, TORID, AKSONDOL -> false;
            default -> true;
        };
    }
}
