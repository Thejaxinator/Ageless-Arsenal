package com.jax.destiny2mc.ageless.scorn.mixin;

import com.jax.destiny2mc.ageless.scorn.ScornPose;

import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierCatalog;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** World rendering, the bestiary, and local rig tests all call the same pose sampler. */
@Mixin(value=FrontierModel.class,remap=false)
abstract class ScornModelMixin {
    @Unique private ScornPose ageless$scornPose;

    @Inject(method="<init>",at=@At("TAIL"))
    private void ageless$bindScorn(ModelPart root, FrontierCatalog.Unit unit, CallbackInfo ci) {
        if (unit.faction().equals("scorn")) {
            ageless$scornPose = new ScornPose((FrontierModel)(Object)this,unit.kind());
        }
    }

    @Inject(method="pose",at=@At("HEAD"),cancellable=true)
    private void ageless$sampleScorn(float swing,float amount,float age,float yaw,float pitch,
                                      int move,float t,boolean aimed,int weakMask,CallbackInfo ci) {
        if (ageless$scornPose != null) {
            ageless$scornPose.sample(swing,amount,age,yaw,pitch,move,t,aimed);
            ci.cancel();
        }
    }
}
