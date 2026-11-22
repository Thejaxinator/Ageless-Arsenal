package com.jax.destiny2mc.mixin;

import com.jax.destiny2mc.client.ageless.DreadMotion;
import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierCatalog;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The mob, the inventory preview, and the bestiary feed identical clocks to the same baked model. */
@Mixin(value=FrontierModel.class,remap=false)
public abstract class DreadPoseMixin {
    @Shadow @Final private FrontierCatalog.Unit unit;
    @Unique private DreadMotion ageless$dreadMotion;

    @Inject(method="pose",at=@At("HEAD"),cancellable=true)
    private void ageless$poseDread(float swing,float amount,float age,float yaw,float pitch,int state,float t,
                                   boolean aiming,int weakMask,CallbackInfo ci) {
        if (!unit.faction().equals("dread")) return;
        if (ageless$dreadMotion==null) ageless$dreadMotion=new DreadMotion((FrontierModel)(Object)this,unit.kind());
        ageless$dreadMotion.sample(swing,amount,age,yaw,pitch,state,t,aiming,weakMask);
        ci.cancel();
    }
}
