package com.jax.destiny2mc.mixin;

import com.jax.destiny2mc.ageless.materials.AgelessPerkPools;
import com.jax.destiny2mc.weapon.LegacyWeaponPerks;
import com.jax.destiny2mc.weapon.WeaponSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=LegacyWeaponPerks.class,remap=false)
public abstract class AdditionalPerkPoolMixin {
    /** Preserve the original 52 by index and resolve appended equipment by registry ID. */
    @Inject(method="getPoolOption(III)I",at=@At("HEAD"),cancellable=true)
    private static void ageless$pool(int index,int socket,int option,CallbackInfoReturnable<Integer> result) {
        if (index >= 52 && index < WeaponSpec.ALL.length && socket >= 0 && socket < 4 && option >= 0 && option < 4) {
            result.setReturnValue(AgelessPerkPools.option(WeaponSpec.ALL[index].id,socket,option));
        }
    }
}
