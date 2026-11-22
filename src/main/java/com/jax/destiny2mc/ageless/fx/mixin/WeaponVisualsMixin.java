package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.frontier.FrontierProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets="com.jax.destiny2mc.ageless.WeaponVisuals",remap=false)
abstract class WeaponVisualsMixin {
    @Inject(method="trace",at=@At("HEAD"),cancellable=true)
    private static void ageless$dataBeam(ServerLevel l,Vec3 a,Vec3 b,String id,int element,CallbackInfo ci){
        VoxelEffects.emit(l,VoxelCue.Kind.TRACE,EffectProfile.named(id,element),a,b,1);ci.cancel();
    }
    @Inject(method="projectile",at=@At("RETURN"))
    private static void ageless$skin(FrontierProjectile p,String profile,CallbackInfo ci){
        ((ProjectileLook)p).ageless$sync(EffectProfile.named(profile,p.element()));
    }
}
