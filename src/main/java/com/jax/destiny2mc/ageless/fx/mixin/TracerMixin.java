package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ServerBallisticsEngine.class,remap=false)
abstract class TracerMixin {
    /** Both legacy hitscan and elemental trace rifles arrive here after hit resolution. */
    @Inject(method="spawnTracerBeam",at=@At("HEAD"),cancellable=true)
    private static void ageless$trace(ServerLevel level,Vec3 from,Vec3 to,int element,CallbackInfo ci){
        VoxelEffects.emit(level,VoxelCue.Kind.TRACE,EffectProfile.fromElement(element),from,to,1);ci.cancel();
    }
}
