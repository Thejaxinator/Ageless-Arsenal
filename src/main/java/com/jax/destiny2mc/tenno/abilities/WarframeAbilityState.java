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

    // Existing reworked active ability timers & anchors
    private int infernalisTicks;
    private int brimstoneTicks;
    private Vec3 brimstoneCenter;
    private int slashDashInvulnTicks;
    private int voltSpeedTicks;
    private int chaosTicks;
    private Vec3 chaosCenter;
    private int wrathfulCritTicks;
    private int shroudTicks;
    private int dynarAmbushTicks;
    private int warcryTicks;
    private int snowGlobeTicks;
    private Vec3 snowGlobeCenter;
    private int inkwellTicks;
    private Vec3 inkwellCenter;

    // New Prime active ability timers & anchors
    private int prismaticGemTicks;
    private Vec3 prismaticGemCenter;
    private int prismaticGemElementIndex;
    private int finalStandTicks;
    private int coilHorizonTicks;
    private Vec3 coilHorizonCenter;
    private int rotorswellTicks;
    private int razorGyreTicks;
    private Vec3 razorGyreDir;
    private int aquabladesTicks;
    private int riptideTicks;
    private Vec3 riptideCenter;
    private int vialRushTicks;
    private Vec3 vialRushDir;
    private int vialPoolTicks;
    private Vec3 vialPoolCenter;

    // Passive runtime trackers
    private float voltStaticCharge;
    private Vec3 lastPosition;
    private int kullervoHitCounter;
    private int magLootShieldCooldown;
    private int citrineKillStacks;
    private int calibanAdaptiveStacks;
    private int calibanAdaptiveDecayTicks;
    private int yareliMoveTicks;
    private int yareliIdleTicks;
    private int lavosElementIndex;
    private int lavosElementCooldown;

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
        if (brimstoneTicks > 0) {
            brimstoneTicks--;
            if (brimstoneTicks == 0) brimstoneCenter = null;
        }
        if (slashDashInvulnTicks > 0) slashDashInvulnTicks--;
        if (voltSpeedTicks > 0) voltSpeedTicks--;
        if (chaosTicks > 0) {
            chaosTicks--;
            if (chaosTicks == 0) chaosCenter = null;
        }
        if (wrathfulCritTicks > 0) wrathfulCritTicks--;
        if (shroudTicks > 0) shroudTicks--;
        if (dynarAmbushTicks > 0) dynarAmbushTicks--;
        if (warcryTicks > 0) warcryTicks--;
        if (snowGlobeTicks > 0) {
            snowGlobeTicks--;
            if (snowGlobeTicks == 0) snowGlobeCenter = null;
        }
        if (inkwellTicks > 0) {
            inkwellTicks--;
            if (inkwellTicks == 0) inkwellCenter = null;
        }
        if (prismaticGemTicks > 0) {
            prismaticGemTicks--;
            if (prismaticGemTicks == 0) prismaticGemCenter = null;
        }
        if (finalStandTicks > 0) finalStandTicks--;
        if (coilHorizonTicks > 0) {
            coilHorizonTicks--;
            if (coilHorizonTicks == 0) coilHorizonCenter = null;
        }
        if (rotorswellTicks > 0) rotorswellTicks--;
        if (razorGyreTicks > 0) {
            razorGyreTicks--;
            if (razorGyreTicks == 0) razorGyreDir = null;
        }
        if (aquabladesTicks > 0) aquabladesTicks--;
        if (riptideTicks > 0) {
            riptideTicks--;
            if (riptideTicks == 0) riptideCenter = null;
        }
        if (vialRushTicks > 0) {
            vialRushTicks--;
            if (vialRushTicks == 0) vialRushDir = null;
        }
        if (vialPoolTicks > 0) {
            vialPoolTicks--;
            if (vialPoolTicks == 0) vialPoolCenter = null;
        }
        if (magLootShieldCooldown > 0) magLootShieldCooldown--;
        if (lavosElementCooldown > 0) lavosElementCooldown--;
        if (calibanAdaptiveStacks > 0) {
            if (calibanAdaptiveDecayTicks > 0) {
                calibanAdaptiveDecayTicks--;
            } else {
                calibanAdaptiveStacks = Math.max(0, calibanAdaptiveStacks - 1);
                calibanAdaptiveDecayTicks = 40;
            }
        }
    }

    public int infernalisTicks() { return infernalisTicks; }
    public void setInfernalisTicks(int ticks) { this.infernalisTicks = Math.max(0, ticks); }

    public int brimstoneTicks() { return brimstoneTicks; }
    public Vec3 brimstoneCenter() { return brimstoneCenter; }
    public void setBrimstone(Vec3 center, int ticks) {
        this.brimstoneCenter = center;
        this.brimstoneTicks = Math.max(0, ticks);
    }

    public int slashDashInvulnTicks() { return slashDashInvulnTicks; }
    public void setSlashDashInvulnTicks(int ticks) { this.slashDashInvulnTicks = Math.max(0, ticks); }

    public int voltSpeedTicks() { return voltSpeedTicks; }
    public void setVoltSpeedTicks(int ticks) { this.voltSpeedTicks = Math.max(0, ticks); }

    public int chaosTicks() { return chaosTicks; }
    public Vec3 chaosCenter() { return chaosCenter; }
    public void setChaos(Vec3 center, int ticks) {
        this.chaosCenter = center;
        this.chaosTicks = Math.max(0, ticks);
    }

    public int wrathfulCritTicks() { return wrathfulCritTicks; }
    public void setWrathfulCritTicks(int ticks) { this.wrathfulCritTicks = Math.max(0, ticks); }

    public int shroudTicks() { return shroudTicks; }
    public void setShroudTicks(int ticks) { this.shroudTicks = Math.max(0, ticks); }

    public int dynarAmbushTicks() { return dynarAmbushTicks; }
    public void setDynarAmbushTicks(int ticks) { this.dynarAmbushTicks = Math.max(0, ticks); }

    public int warcryTicks() { return warcryTicks; }
    public void setWarcryTicks(int ticks) { this.warcryTicks = Math.max(0, ticks); }

    public int snowGlobeTicks() { return snowGlobeTicks; }
    public Vec3 snowGlobeCenter() { return snowGlobeCenter; }
    public void setSnowGlobe(Vec3 center, int ticks) {
        this.snowGlobeCenter = center;
        this.snowGlobeTicks = Math.max(0, ticks);
    }

    public int inkwellTicks() { return inkwellTicks; }
    public Vec3 inkwellCenter() { return inkwellCenter; }
    public void setInkwell(Vec3 center, int ticks) {
        this.inkwellCenter = center;
        this.inkwellTicks = Math.max(0, ticks);
    }

    public int prismaticGemTicks() { return prismaticGemTicks; }
    public Vec3 prismaticGemCenter() { return prismaticGemCenter; }
    public void setPrismaticGem(Vec3 center, int ticks) {
        this.prismaticGemCenter = center;
        this.prismaticGemTicks = Math.max(0, ticks);
    }
    public int nextPrismaticElement() {
        int current = prismaticGemElementIndex;
        prismaticGemElementIndex = (prismaticGemElementIndex + 1) % 4;
        return current;
    }

    public int finalStandTicks() { return finalStandTicks; }
    public void setFinalStandTicks(int ticks) { this.finalStandTicks = Math.max(0, ticks); }

    public int coilHorizonTicks() { return coilHorizonTicks; }
    public Vec3 coilHorizonCenter() { return coilHorizonCenter; }
    public void setCoilHorizon(Vec3 center, int ticks) {
        this.coilHorizonCenter = center;
        this.coilHorizonTicks = Math.max(0, ticks);
    }

    public int rotorswellTicks() { return rotorswellTicks; }
    public void setRotorswellTicks(int ticks) { this.rotorswellTicks = Math.max(0, ticks); }

    public int razorGyreTicks() { return razorGyreTicks; }
    public Vec3 razorGyreDir() { return razorGyreDir; }
    public void setRazorGyre(Vec3 dir, int ticks) {
        this.razorGyreDir = dir;
        this.razorGyreTicks = Math.max(0, ticks);
    }

    public int aquabladesTicks() { return aquabladesTicks; }
    public void setAquabladesTicks(int ticks) { this.aquabladesTicks = Math.max(0, ticks); }

    public int riptideTicks() { return riptideTicks; }
    public Vec3 riptideCenter() { return riptideCenter; }
    public void setRiptide(Vec3 center, int ticks) {
        this.riptideCenter = center;
        this.riptideTicks = Math.max(0, ticks);
    }

    public int vialRushTicks() { return vialRushTicks; }
    public Vec3 vialRushDir() { return vialRushDir; }
    public void setVialRush(Vec3 dir, int ticks) {
        this.vialRushDir = dir;
        this.vialRushTicks = Math.max(0, ticks);
    }

    public int vialPoolTicks() { return vialPoolTicks; }
    public Vec3 vialPoolCenter() { return vialPoolCenter; }
    public void setVialPool(Vec3 center, int ticks) {
        this.vialPoolCenter = center;
        this.vialPoolTicks = Math.max(0, ticks);
    }

    public float voltStaticCharge() { return voltStaticCharge; }
    public void addVoltStaticCharge(float amount) {
        this.voltStaticCharge = Math.min(150.0f, Math.max(0.0f, this.voltStaticCharge + amount));
    }
    public float consumeVoltStaticCharge() {
        float charge = this.voltStaticCharge;
        this.voltStaticCharge = 0.0f;
        return charge;
    }

    public Vec3 lastPosition() { return lastPosition; }
    public void setLastPosition(Vec3 pos) { this.lastPosition = pos; }

    public int incrementKullervoHits() {
        kullervoHitCounter = (kullervoHitCounter + 1) % 3;
        return kullervoHitCounter;
    }

    public boolean tryConsumeMagLootShield() {
        if (magLootShieldCooldown > 0) return false;
        magLootShieldCooldown = 80;
        return true;
    }

    public int citrineKillStacks() { return citrineKillStacks; }
    public void addCitrineKillStack() { this.citrineKillStacks = Math.min(15, this.citrineKillStacks + 1); }

    public int calibanAdaptiveStacks() { return calibanAdaptiveStacks; }
    public void addCalibanAdaptiveStack() {
        this.calibanAdaptiveStacks = Math.min(5, this.calibanAdaptiveStacks + 1);
        this.calibanAdaptiveDecayTicks = 100;
    }

    public boolean updateYareliMovement(boolean moving) {
        if (moving) {
            yareliIdleTicks = 0;
            yareliMoveTicks = Math.min(60, yareliMoveTicks + 1);
        } else {
            yareliIdleTicks++;
            if (yareliIdleTicks >= 20) {
                yareliMoveTicks = 0;
            }
        }
        return yareliMoveTicks >= 30;
    }
    public boolean isYareliCriticalFlowActive() { return yareliMoveTicks >= 30; }

    public boolean tryConsumeLavosElement() {
        if (lavosElementCooldown > 0) return false;
        lavosElementCooldown = 60;
        return true;
    }
    public int nextLavosElement() {
        int current = lavosElementIndex;
        lavosElementIndex = (lavosElementIndex + 1) % 4;
        return current;
    }
}
