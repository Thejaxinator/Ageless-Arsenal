/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package com.jax.destiny2mc.weapon;

import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class GunDataHelper {
    public static CompoundTag readTag(ItemStack itemStack) {
        CustomData customData = (CustomData)itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return new CompoundTag();
        }
        return customData.copyTag();
    }

    public static void writeTag(ItemStack itemStack, CompoundTag compoundTag) {
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
    }

    public static int getInt(ItemStack itemStack, String string, int n) {
        CompoundTag compoundTag = GunDataHelper.readTag(itemStack);
        if (compoundTag.contains(string)) {
            return compoundTag.getInt(string);
        }
        return n;
    }

    public static void setInt(ItemStack itemStack, String string, int n) {
        CompoundTag compoundTag = GunDataHelper.readTag(itemStack);
        compoundTag.putInt(string, n);
        GunDataHelper.writeTag(itemStack, compoundTag);
    }

    public static long getLong(ItemStack itemStack, String string) {
        CompoundTag compoundTag = GunDataHelper.readTag(itemStack);
        if (compoundTag.contains(string)) {
            return compoundTag.getLong(string);
        }
        return 0L;
    }

    public static void setLong(ItemStack itemStack, String string, long l) {
        CompoundTag compoundTag = GunDataHelper.readTag(itemStack);
        compoundTag.putLong(string, l);
        GunDataHelper.writeTag(itemStack, compoundTag);
    }

    public static int getMag(ItemStack itemStack, WeaponSpec weaponSpec) {
        return GunDataHelper.getInt(itemStack, "d2_mag", WeaponPerkRegistry.getEffectiveMagSize(itemStack, weaponSpec));
    }

    public static int getReserve(ItemStack itemStack, WeaponSpec weaponSpec) {
        if (!weaponSpec.usesSpecialAmmo) {
            return 999;
        }
        if (weaponSpec.index>=40) return GunDataHelper.getInt(itemStack,"d2_res",weaponSpec.archetype==6?300:weaponSpec.archetype==7?6:weaponSpec.archetype==8?12:24);
        if (weaponSpec.perkId == 17) {
            return 0;
        }
        if (weaponSpec.perkId == 35) {
            return GunDataHelper.getInt(itemStack, "d2_res", 144);
        }
        if (weaponSpec.perkId == 20) {
            return GunDataHelper.getInt(itemStack, "d2_res", 90);
        }
        return GunDataHelper.getInt(itemStack, "d2_res", 21);
    }
}

