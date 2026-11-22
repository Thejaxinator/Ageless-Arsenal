package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Resolved for a single wielder and physical weapon. Flights keep the immutable launch
 * snapshot so swapping hands, trading, or upgrading a card cannot alter a projectile in flight. */
public record TennoGunStats(TennoGunSpec spec, double powerFactor, double rateFactor, double reloadFactor,
                            int magazine, double multishot, double heat, double cold) {
    public static TennoGunStats of(Player wielder, ItemStack stack) {
        if (!(stack.getItem() instanceof TennoGunItem gun)) throw new IllegalArgumentException("Expected a Warframe firearm");
        var spec = gun.profile();
        var bonuses = WeaponMods.bonuses(wielder,stack);
        double heat = bonuses.getOrDefault(ModStat.HEAT,0d);
        double cold = bonuses.getOrDefault(ModStat.COLD,0d);
        double power = (1 + bonuses.getOrDefault(ModStat.DAMAGE,0d)) * (1 + heat + cold);
        int magazine = (int)Math.floor(spec.magazine() * (1 + bonuses.getOrDefault(ModStat.MAGAZINE,0d)) + .5d);
        return new TennoGunStats(spec,power,1 + bonuses.getOrDefault(ModStat.FIRE_RATE,0d),
            1 + bonuses.getOrDefault(ModStat.RELOAD_SPEED,0d),Math.max(1,magazine),
            bonuses.getOrDefault(ModStat.MULTISHOT,0d),heat,cold);
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
        if (!TennoHits.hurt(owner,victim,scaleDamage(originalHp))) return false;
        if (heat > 0 && owner.getRandom().nextDouble() < heat) victim.igniteForSeconds(3);
        if (cold > 0 && owner.getRandom().nextDouble() < cold)
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0,false,true),owner);
        return true;
    }
}
