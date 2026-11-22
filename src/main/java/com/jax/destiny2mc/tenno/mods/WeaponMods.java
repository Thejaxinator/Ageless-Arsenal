package com.jax.destiny2mc.tenno.mods;

import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * The physical weapon owns its ordered socket IDs. The wielder owns the cards and their ranks.
 * A gift, chest, item entity, copied stack or changed hotbar slot cannot convey another player's rank.
 * Absence of the list is the default for existing weapons from earlier releases.
 */
public final class WeaponMods {
    private static final String KEY = "ageless_weapon_mods";
    private WeaponMods() {}

    private static boolean isWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty()
            && (stack.getItem() instanceof TennoGunItem || stack.getItem() instanceof TennoMeleeItem);
    }

    public static List<WeaponMod> installed(ItemStack stack) {
        if (!isWeapon(stack)) return List.of();
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.contains(KEY)) return List.of();
        var result = new LinkedHashSet<WeaponMod>();
        for (Tag entry : data.copyTag().getList(KEY,Tag.TAG_STRING)) {
            var mod = WeaponMod.byId(entry.getAsString());
            if (mod != null && mod.fits(stack)) result.add(mod);
        }
        return List.copyOf(result);
    }

    /** Validate these mutations again on the server thread after resolving the actual inventory slot. */
    public static boolean install(Player player, ItemStack stack, WeaponMod mod) {
        if (!(player instanceof ServerPlayer) || mod == null || !mod.fits(stack)
                || ModCollection.rank(player,mod.id()) < 0 || installed(stack).contains(mod)) return false;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,root -> {
            ListTag ids = root.getList(KEY,Tag.TAG_STRING);
            ids.add(StringTag.valueOf(mod.id()));
            root.put(KEY,ids);
        });
        return true;
    }

    /** Players may remove a dormant socket inherited from a different wielder. */
    public static boolean remove(ItemStack stack, WeaponMod mod) {
        if (mod == null || !installed(stack).contains(mod)) return false;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,root -> {
            var ids = root.getList(KEY,Tag.TAG_STRING);
            ids.removeIf(id -> mod.id().equals(id.getAsString()));
            if (ids.isEmpty()) root.remove(KEY);
        });
        return true;
    }

    /** Resolve once when launching a delayed projectile or starting an authoritative attack.
     * The returned immutable snapshot can safely travel with a beam, shot or lingering effect. */
    public static Map<ModStat,Double> bonuses(Player wielder, ItemStack stack) {
        if (wielder == null) return Map.of();
        var cards = installed(stack);
        if (cards.isEmpty()) return Map.of();
        var active = new EnumMap<ModStat,Double>(ModStat.class);
        for (var card : cards) {
            int rank = ModCollection.rank(wielder,card.id());
            if (rank < 0) continue;
            for (var step : card.steps())
                active.merge(step.stat(),card.value(step,rank),Double::sum);
        }
        return Map.copyOf(active);
    }
    public static double bonus(Player wielder, ItemStack stack, ModStat stat) {
        return bonuses(wielder,stack).getOrDefault(stat,0d);
    }
}
