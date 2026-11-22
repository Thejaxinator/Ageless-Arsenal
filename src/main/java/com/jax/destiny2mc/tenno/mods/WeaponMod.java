package com.jax.destiny2mc.tenno.mods;

import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.jax.destiny2mc.tenno.mods.ModStat.*;

/**
 * Warframe mod names and equipment restrictions, interpreted for the Minecraft combat system.
 * Rank zero already applies one increment. One definition powers the UI, packs, and combat.
 */
public enum WeaponMod {
    SERRATION("Serration", Equipment.RIFLE, 5, "Increases the base damage of rifle attacks.", DAMAGE, .10),
    SPEED_TRIGGER("Speed Trigger", Equipment.RIFLE, 5, "Shortens the interval between rifle attacks.", FIRE_RATE, .08),
    FAST_HANDS("Fast Hands", Equipment.RIFLE, 5, "Refills the rifle magazine faster.", RELOAD_SPEED, .10),
    MAGAZINE_WARP("Magazine Warp", Equipment.RIFLE, 5, "Expands the rifle magazine.", MAGAZINE, .08),
    HELLFIRE("Hellfire", Equipment.RIFLE, 5, "Adds Heat damage and a chance to ignite for three seconds.", HEAT, .08),
    POINT_BLANK("Point Blank", Equipment.SHOTGUN, 5, "Increases the base damage of primary shotgun attacks.", DAMAGE, .10),
    SHOTGUN_BARRAGE("Shotgun Barrage", Equipment.SHOTGUN, 5, "Shortens the interval between primary shotgun attacks.", FIRE_RATE, .08),
    TACTICAL_PUMP("Tactical Pump", Equipment.SHOTGUN, 5, "Speeds reloading and the Bubonico battery's recovery.", RELOAD_SPEED, .10),
    AMMO_STOCK("Ammo Stock", Equipment.SHOTGUN, 5, "Expands the shotgun magazine or arm cannon battery.", MAGAZINE, .10),
    CHILLING_GRASP("Chilling Grasp", Equipment.SHOTGUN, 5, "Adds Cold damage and a chance to slow for three seconds.", COLD, .08),
    VIGILANTE_ARMAMENTS("Vigilante Armaments", Equipment.PRIMARY, 5, "Chance to fire additional projectiles for the same ammunition.", MULTISHOT, .10),
    HORNET_STRIKE("Hornet Strike", Equipment.PISTOL, 5, "Increases the base damage of secondary attacks.", DAMAGE, .10),
    LETHAL_TORRENT("Lethal Torrent", Equipment.PISTOL, 5, "Speeds the action and adds a chance for extra projectiles.", new Step(FIRE_RATE,.06), new Step(MULTISHOT,.08)),
    QUICKDRAW("Quickdraw", Equipment.PISTOL, 5, "Shortens the secondary's reload.", RELOAD_SPEED, .10),
    SLIP_MAGAZINE("Slip Magazine", Equipment.PISTOL, 5, "Expands the secondary magazine, including thrown blades.", MAGAZINE, .10),
    HEATED_CHARGE("Heated Charge", Equipment.PISTOL, 5, "Adds Heat damage and a chance to ignite for three seconds.", HEAT, .08),
    PRESSURE_POINT("Pressure Point", Equipment.MELEE, 5, "Increases the weapon's base melee damage.", DAMAGE, .10),
    FURY("Fury", Equipment.MELEE, 5, "Speeds the recovery between fully charged melee attacks.", MELEE_SPEED, .06),
    REACH("Reach", Equipment.MELEE, 3, "Extends the distance at which this weapon can strike.", ModStat.REACH, .20),
    KILLING_BLOW("Killing Blow", Equipment.MELEE, 5, "Multiplies damage from a charged crouching heavy strike.", HEAVY_DAMAGE, .12),
    MOLTEN_IMPACT("Molten Impact", Equipment.MELEE, 5, "Adds Heat against fire-susceptible foes and ignites on a charged hit.", HEAT, .08),
    PARRY("Parry", Equipment.MELEE, 5, "A successful frontal melee guard can slow the attacker for two seconds.", ModStat.PARRY, .10);

    public record Step(ModStat stat, double increment) {}
    private enum Equipment {
        RIFLE("Rifle","primary"), SHOTGUN("Shotgun","primary"), PRIMARY("Primary","primary"),
        PISTOL("Pistol","secondary"), MELEE("Melee","melee");
        final String title, pack;
        Equipment(String title,String pack) { this.title = title; this.pack = pack; }
    }

    private static final Map<String,WeaponMod> BY_ID = new HashMap<>();
    static { for (var mod : values()) BY_ID.put(mod.id(),mod); }

    private final String id;
    private final String title;
    private final Equipment equipment;
    private final String description;
    private final int maxRank;
    private final List<Step> steps;
    private final ResourceLocation icon;

    WeaponMod(String title, Equipment equipment, int maxRank, String description, ModStat stat, double increment) {
        this(title, equipment, maxRank, description, new Step(stat, increment));
    }
    WeaponMod(String title, Equipment equipment, int maxRank, String description, Step... steps) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.title = title;
        this.equipment = equipment;
        this.maxRank = maxRank;
        this.description = description;
        this.steps = List.of(steps);
        this.icon = ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/gui/warframe/mods/" + id + ".png");
    }

    public static WeaponMod byId(String id) { return BY_ID.get(id); }
    public String id() { return id; }
    public String title() { return title; }
    public String category() { return equipment.title; }
    public String pack() { return equipment.pack; }
    /** Frame metal, from Warframe's exported mod rarity: bronze, silver or gold. */
    public int rarity() {
        return switch (this) {
            case FAST_HANDS, MAGAZINE_WARP, VIGILANTE_ARMAMENTS, QUICKDRAW, SLIP_MAGAZINE,
                 PRESSURE_POINT, REACH, PARRY -> 0;
            case LETHAL_TORRENT -> 2;
            default -> 1;
        };
    }
    public int maxRank() { return maxRank; }
    public List<Step> steps() { return steps; }
    public ResourceLocation icon() { return icon; }
    public String description() { return description; }
    public boolean forPack(String group) { return equipment.pack.equals(group); }

    /** Unknown or unreleased weapon families must be classified explicitly before gaining mod sockets. */
    private static Equipment weaponClass(ItemStack item) {
        if (item == null || item.isEmpty()) return null;
        if (item.getItem() instanceof TennoMeleeItem) return Equipment.MELEE;
        if (!(item.getItem() instanceof TennoGunItem weapon)) return null;
        return switch (weapon.profile().family) {
            case AX, GRAKATA, RIFLE, BURST, FLAME, PERIGALE, SOMA, TORID -> Equipment.RIFLE;
            case CEDO, BUBONICO -> Equipment.SHOTGUN;
            case TWINS, BRAKK, MICROWAVE, VESPER, CANTARE, AKSONDOL -> Equipment.PISTOL;
        };
    }

    /** Uses the same slot classification for the Arsenal catalog and card compatibility. */
    public static boolean isSecondary(ItemStack item) {
        return weaponClass(item) == Equipment.PISTOL;
    }

    public boolean fits(ItemStack item) {
        Equipment actual = weaponClass(item);
        return equipment == actual || equipment == Equipment.PRIMARY && (actual == Equipment.RIFLE || actual == Equipment.SHOTGUN);
    }
    /** A zero-based rank, with -1 for a locked card. High future/invalid values are clamped. */
    public double value(Step step, int rank) {
        return rank < 0 ? 0 : step.increment() * (Math.min(maxRank,rank) + 1);
    }
    public String effect(int rank) {
        return steps.stream().map(s -> s.stat().format(value(s,rank))).collect(Collectors.joining("  /  "));
    }
}
