package com.jax.destiny2mc.mixin;
import com.jax.destiny2mc.client.ThirdPersonGunRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=PlayerModel.class,remap=false)
abstract class PlayerArmMixin {
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void destiny2mc$poseArms(LivingEntity entity,float stride,float speed,float age,float yaw,float pitch,CallbackInfo call){
        if(!(entity instanceof Player player))return;
        var mc=net.minecraft.client.Minecraft.getInstance();
        // PlayerRenderer renders bare first-person sleeves by invoking setupAnim with age zero.
        if(player==mc.player&&age==0&&mc.options.getCameraType().isFirstPerson())return;
        ThirdPersonGunRenderer.arms((PlayerModel<?>)(Object)this,player,Mth.clamp(age-player.tickCount,0,1));
    }
}
