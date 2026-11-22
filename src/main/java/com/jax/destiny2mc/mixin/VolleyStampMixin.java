package com.jax.destiny2mc.mixin;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ServerBallisticsEngine.class,remap=false)
abstract class VolleyStampMixin {
    @Inject(method="fireHitscanVolley",at=@At("HEAD"))
    private static void destiny2mc$markVolley(ServerLevel level,ServerPlayer player,ItemStack weapon,WeaponSpec spec,
                                             float yaw,float pitch,int ads,float damage,int flags,CallbackInfo call){
        GunDataHelper.setLong(weapon,"d2_visual_shot",level.getGameTime());
    }
}
