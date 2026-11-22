package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Resolved for a single wielder and physical weapon. Flights keep the immutable launch
 * snapshot so swapping hands, trading, or upgrading a card cannot alter a projectile in flight. */
public record TennoGunStats(TennoGunSpec spec, double powerFactor, double rateFactor, double reloadFactor,
                            int magazine, double multishot, double heat, double cold,
                            Map<ModStat, Double> elements) {

    public TennoGunStats(TennoGunSpec spec, double powerFactor, double rateFactor, double reloadFactor,
                         int magazine, double multishot, double heat, double cold) {
        this(spec, powerFactor, rateFactor, reloadFactor, magazine, multishot, heat, cold, Map.of());
    }

    public static TennoGunStats of(Player wielder, ItemStack stack) {
        if (!(stack.getItem() instanceof TennoGunItem gun)) throw new IllegalArgumentException("Expected a Warframe firearm");
        var spec = gun.profile();
        var bonuses = WeaponMods.bonuses(wielder,stack);
        var elemMap = new EnumMap<ModStat, Double>(ModStat.class);
        double totalElem = 0d;
        for (var entry : bonuses.entrySet()) {
            if (entry.getKey().isElemental() && entry.getValue() > 0d) {
                elemMap.put(entry.getKey(), entry.getValue());
                totalElem += entry.getValue();
            }
        }
        double heat = elemMap.getOrDefault(ModStat.HEAT, 0d);
        double cold = elemMap.getOrDefault(ModStat.COLD, 0d);
        double power = (1 + bonuses.getOrDefault(ModStat.DAMAGE,0d)) * (1 + totalElem);
        int magazine = (int)Math.floor(spec.magazine() * (1 + bonuses.getOrDefault(ModStat.MAGAZINE,0d)) + .5d);
        return new TennoGunStats(spec,power,1 + bonuses.getOrDefault(ModStat.FIRE_RATE,0d),
            1 + bonuses.getOrDefault(ModStat.RELOAD_SPEED,0d),Math.max(1,magazine),
            bonuses.getOrDefault(ModStat.MULTISHOT,0d),heat,cold,Map.copyOf(elemMap));
    }

    public double element(ModStat stat) {
        return elements.getOrDefault(stat, 0d);
    }

    public float damage() { return scaleDamage(spec.damage()); }
    public float scaleDamage(float base) { return (float)(base * powerFactor); }
    public int delay() { return shotTicks(spec.delay()); }
    public int reloadTicks() { return rechargeTicks(spec.reloadTicks()); }
    public int shotTicks(int base) { return roundedTicks(base / rateFactor); }
    /** Fractional deadlines preserve small rank improvements across sustained fire even on
     * a 20 Hz server. A long pause starts a new firing cycle; it never banks previous shots. */
    public double nextShot(long now, double precedingDeadline, int nominalTicks) {
        double phase = precedingDeadline > now - 1.0 && precedingDeadline <= now ? precedingDeadline : now;
        return phase + Math.max(1d, nominalTicks / rateFactor);
    }
    public int rechargeTicks(int base) { return roundedTicks(base / reloadFactor); }
    private static int roundedTicks(double value) { return Math.max(1,(int)Math.round(value)); }

    /** Each pellet gets an independent opportunity to reproduce. The game charges ammunition
     * only for the original round. Ordinary unmodded shots preserve their original RNG sequence. */
    int projectileCount(int originals, RandomSource rng) {
        int count = originals + (int)Math.floor(multishot) * originals;
        double fraction = multishot - Math.floor(multishot);
        if (fraction > 0) for (int i=0;i<originals;i++) if (rng.nextDouble() < fraction) count++;
        return count;
    }

    boolean hit(ServerPlayer owner, LivingEntity victim, float originalHp) {
        float dealt = scaleDamage(originalHp);
        if (!TennoHits.hurt(owner,victim,dealt)) return false;
        var rng = owner.getRandom();
        if (heat > 0 && rng.nextDouble() < heat) victim.igniteForSeconds(3);
        if (cold > 0 && rng.nextDouble() < cold)
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0,false,true),owner);

        double elec = element(ModStat.ELECTRICITY);
        if (elec > 0 && rng.nextDouble() < elec) {
            Vec3 center = victim.getBoundingBox().getCenter();
            int arcs = 0;
            for (var nearby : TennoHits.near(owner, center, 4.5, Set.of(victim.getUUID()))) {
                TennoHits.trace(owner, center, nearby.getBoundingBox().getCenter(), EffectProfile.ARC);
                TennoHits.hurt(owner, nearby, Math.max(1f, dealt * 0.25f));
                if (++arcs >= 2) break;
            }
        }
        double toxin = element(ModStat.TOXIN);
        if (toxin > 0 && rng.nextDouble() < toxin) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 0, false, true), owner);
        }
        double magnetic = element(ModStat.MAGNETIC);
        if (magnetic > 0 && rng.nextDouble() < Math.min(0.85, magnetic)) {
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 0, false, true), owner);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0, false, true), owner);
            TennoHits.flash(owner, victim.getBoundingBox().getCenter(), EffectProfile.ARC, 0.7f, VoxelCue.Kind.BURST);
        }
        double viral = element(ModStat.VIRAL);
        if (viral > 0 && rng.nextDouble() < Math.min(0.85, viral)) {
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true), owner);
            TennoHits.flash(owner, victim.getBoundingBox().getCenter(), EffectProfile.STRAND, 0.7f, VoxelCue.Kind.BURST);
        }
        double radiation = element(ModStat.RADIATION);
        if (radiation > 0 && rng.nextDouble() < Math.min(0.85, radiation)) {
            victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true), owner);
            TennoHits.flash(owner, victim.getBoundingBox().getCenter(), EffectProfile.SOLAR, 0.7f, VoxelCue.Kind.BURST);
        }
        double gas = element(ModStat.GAS);
        if (gas > 0 && rng.nextDouble() < Math.min(0.85, gas)) {
            Vec3 center = victim.getBoundingBox().getCenter();
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 0, false, true), owner);
            for (var nearby : TennoHits.near(owner, center, 3.0, Set.of(victim.getUUID()))) {
                nearby.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true), owner);
            }
            TennoHits.flash(owner, center, EffectProfile.STRAND, 1.3f, VoxelCue.Kind.RING);
        }
        double blast = element(ModStat.BLAST);
        if (blast > 0 && rng.nextDouble() < Math.min(0.85, blast)) {
            victim.knockback(0.65, owner.getX() - victim.getX(), owner.getZ() - victim.getZ());
            TennoHits.flash(owner, victim.getBoundingBox().getCenter(), EffectProfile.SOLAR, 1.1f, VoxelCue.Kind.BURST);
        }
        double corrosive = element(ModStat.CORROSIVE);
        if (corrosive > 0 && rng.nextDouble() < Math.min(0.85, corrosive)) {
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true), owner);
        }
        return true;
    }
}
