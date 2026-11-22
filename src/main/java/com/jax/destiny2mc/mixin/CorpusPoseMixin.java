package com.jax.destiny2mc.mixin;

import com.jax.destiny2mc.client.ageless.CorpusMotion;
import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierCatalog;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes Corpus rigs to CorpusMotion so world mobs and Bestiary previews share the same pose clock. */
@Mixin(value = FrontierModel.class, remap = false)
public abstract class CorpusPoseMixin {
    @Shadow @Final private FrontierCatalog.Unit unit;
    @Unique private CorpusMotion ageless$corpusMotion;

    @Inject(method = "pose", at = @At("HEAD"), cancellable = true)
    private void ageless$poseCorpus(float swing, float amount, float age, float yaw, float pitch, int state, float t,
                                    boolean aiming, int weakMask, CallbackInfo ci) {
        if (!unit.faction().equals("corpus")) return;
        if (ageless$corpusMotion == null) ageless$corpusMotion = new CorpusMotion((FrontierModel) (Object) this, unit.kind());
        ageless$corpusMotion.sample(swing, amount, age, yaw, pitch, state, t, aiming, weakMask);
        ci.cancel();
    }
}
