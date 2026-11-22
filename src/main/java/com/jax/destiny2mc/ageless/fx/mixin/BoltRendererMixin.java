package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.client.BoltMesh;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ThrownItemRenderer.class,remap=false)
abstract class BoltRendererMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void ageless$projectile(Entity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buf,int light,CallbackInfo ci){
        if(BoltMesh.draw(entity,partial,pose,buf))ci.cancel();
    }
}
