package com.jax.destiny2mc.tenno.abilities;

import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WarframeAbilityState {
    private static final Map<UUID, WarframeAbilityState> STATES = new ConcurrentHashMap<>();

    private WarframeId activeWarframe = WarframeId.EXCALIBUR;
    private float energy = WarframeId.EXCALIBUR.maxEnergy();
    private final EnumMap<WarframeAbilityId, Integer> cooldowns = new EnumMap<>(WarframeAbilityId.class);

    private int infernalisTicks;
    private int wrathfulCritTicks;
    private int shroudTicks;
    private int warcryTicks;
    private int inkwellTicks;
    private int snowGlobeTicks;
    private Vec3 snowGlobeCenter;
    private float voltStaticCharge;
    private Vec3 lastPosition;

    public static WarframeAbilityState get(UUID playerId) {
        return STATES.computeIfAbsent(playerId, id -> new WarframeAbilityState());
    }

    public static void remove(UUID playerId) {
        STATES.remove(playerId);
    }

    public WarframeId activeWarframe() {
        return activeWarframe;
    }

    public void setActiveWarframe(WarframeId warframe) {
        if (warframe == null || warframe == this.activeWarframe) {
            return;
        }
        this.activeWarframe = warframe;
        this.energy = Math.min(this.energy, warframe.maxEnergy());
    }

    public float energy() {
        return energy;
    }

    public void addEnergy(float delta) {
        this.energy = Math.max(0.0f, Math.min(activeWarframe.maxEnergy(), this.energy + delta));
    }

    public boolean consumeEnergy(float amount) {
        if (this.energy < amount) {
            return false;
        }
        this.energy -= amount;
        return true;
    }

    public int cooldown(WarframeAbilityId id) {
        return cooldowns.getOrDefault(id, 0);
    }

    public void setCooldown(WarframeAbilityId id, int ticks) {
        cooldowns.put(id, Math.max(0, ticks));
    }

    public void tickTimers() {
        addEnergy(activeWarframe.energyRegenPerSecond() / 20.0f);
        for (WarframeAbilityId id : WarframeAbilityId.values()) {
            int remaining = cooldowns.getOrDefault(id, 0);
            if (remaining > 0) {
                cooldowns.put(id, remaining - 1);
            }
        }
        if (infernalisTicks > 0) infernalisTicks--;
        if (wrathfulCritTicks > 0) wrathfulCritTicks--;
        if (shroudTicks > 0) shroudTicks--;
        if (warcryTicks > 0) warcryTicks--;
        if (inkwellTicks > 0) inkwellTicks--;
        if (snowGlobeTicks > 0) {
            snowGlobeTicks--;
            if (snowGlobeTicks == 0) {
                snowGlobeCenter = null;
            }
        }
    }

    public int infernalisTicks() { return infernalisTicks; }
    public void setInfernalisTicks(int ticks) { this.infernalisTicks = Math.max(0, ticks); }

    public int wrathfulCritTicks() { return wrathfulCritTicks; }
    public void setWrathfulCritTicks(int ticks) { this.wrathfulCritTicks = Math.max(0, ticks); }

    public int shroudTicks() { return shroudTicks; }
    public void setShroudTicks(int ticks) { this.shroudTicks = Math.max(0, ticks); }

    public int warcryTicks() { return warcryTicks; }
    public void setWarcryTicks(int ticks) { this.warcryTicks = Math.max(0, ticks); }

    public int inkwellTicks() { return inkwellTicks; }
    public void setInkwellTicks(int ticks) { this.inkwellTicks = Math.max(0, ticks); }

    public int snowGlobeTicks() { return snowGlobeTicks; }
    public Vec3 snowGlobeCenter() { return snowGlobeCenter; }
    public void setSnowGlobe(Vec3 center, int ticks) {
        this.snowGlobeCenter = center;
        this.snowGlobeTicks = Math.max(0, ticks);
    }

    public float voltStaticCharge() { return voltStaticCharge; }
    public void addVoltStaticCharge(float amount) {
        this.voltStaticCharge = Math.min(100.0f, Math.max(0.0f, this.voltStaticCharge + amount));
    }
    public float consumeVoltStaticCharge() {
        float charge = this.voltStaticCharge;
        this.voltStaticCharge = 0.0f;
        return charge;
    }

    public Vec3 lastPosition() { return lastPosition; }
    public void setLastPosition(Vec3 pos) { this.lastPosition = pos; }
}
