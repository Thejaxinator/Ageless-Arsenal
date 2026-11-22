package com.jax.destiny2mc.binder.progression.mixin;

import com.jax.destiny2mc.binder.progression.CareerProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Entity.load has already restored NeoForgeData and the saved permanent attributes here.
 * Reconstruct transient career bonuses before vanilla clamps the saved current health.
 * This also covers periodic autosaves and shutdowns that never fired player logout. */
@Mixin(value=LivingEntity.class, remap=false)
abstract class CareerHealthLoad {
    @Inject(method="readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void ageless$loadCareerBeforeHealth(CompoundTag data, CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer player) CareerProgress.restoreStats(player);
    }
}
