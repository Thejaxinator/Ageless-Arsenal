package com.jax.destiny2mc.mixin;
import com.jax.destiny2mc.client.ThirdPersonGunRenderer;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ItemInHandRenderer.class,remap=false)
abstract class HeldGunMixin {
    @Inject(method="renderItem",at=@At("HEAD"),cancellable=true)
    private void destiny2mc$heldGun(LivingEntity entity,ItemStack stack,ItemDisplayContext ctx,boolean left,
                                   PoseStack p,MultiBufferSource out,int light,CallbackInfo call){
        if(!(entity instanceof Player player)||!(stack.getItem() instanceof DestinyGunItem gun))return;
        if(ctx!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND&&ctx!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)return;
        ThirdPersonGunRenderer.render(player,stack,gun,ctx,left,p,out,light);
        call.cancel();
    }
}
