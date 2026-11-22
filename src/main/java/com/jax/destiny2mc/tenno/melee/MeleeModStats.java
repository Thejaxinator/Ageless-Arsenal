package com.jax.destiny2mc.tenno.melee;

import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Values for the exact held copy and its wielder. Damage excludes conditional elemental and heavy bonuses.
 * Bonuses for Heat, heavy attacks and Parry are fractions; speed is swings/s and reach is measured in blocks. */
public record MeleeModStats(double damage, double speed, double reach, double heat, double heavy, double parry) {
    public static MeleeModStats of(Player wielder, ItemStack stack) {
        if (!(stack.getItem() instanceof TennoMeleeItem sword))
            throw new IllegalArgumentException("Expected a Warframe melee weapon");
        var base = sword.profile();
        var mods = WeaponMods.bonuses(wielder,stack);
        return new MeleeModStats(
            base.damage() * (1 + mods.getOrDefault(ModStat.DAMAGE,0d)),
            base.attackSpeed() * (1 + mods.getOrDefault(ModStat.MELEE_SPEED,0d)),
            base.reach() + mods.getOrDefault(ModStat.REACH,0d),
            mods.getOrDefault(ModStat.HEAT,0d),
            mods.getOrDefault(ModStat.HEAVY_DAMAGE,0d),
            mods.getOrDefault(ModStat.PARRY,0d));
    }
}
