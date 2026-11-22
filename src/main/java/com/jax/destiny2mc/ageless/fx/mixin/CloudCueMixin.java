package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.combat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=WizardClouds.class,remap=false)
abstract class CloudCueMixin {
    @Inject(method="add",at=@At("RETURN"))
    private static void ageless$poison(ServerLevel l,CombatMobEntity owner,Vec3 center,CallbackInfo ci){
        VoxelEffects.emit(l,VoxelCue.Kind.CLOUD,EffectProfile.SOULFIRE,center,center,3);
    }
}
