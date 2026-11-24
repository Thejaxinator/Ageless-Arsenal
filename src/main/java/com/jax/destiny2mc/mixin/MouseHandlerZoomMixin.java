package com.jax.destiny2mc.mixin;

import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.client.gunfeel.SniperScopeOverlay;
import com.jax.destiny2mc.client.gunfeel.SpringDamper;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.client.TennoGunClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * TACZ-style MDV133 (Monitor Distance Vertical 133%) mouse sensitivity scaling during ADS/scoping.
 */
@Mixin(value = MouseHandler.class, remap = false)
abstract class MouseHandlerZoomMixin {
    private static final double MDV_COEFFICIENT = 1.33;

    @ModifyArgs(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void destiny2mc$scaleAdsSensitivity(Args args) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        float aim = 0.0f;
        if (stack.getItem() instanceof DestinyGunItem) {
            aim = ClientWeaponState.getSmoothAds(ClientWeaponState.lastPartialTick);
        } else if (stack.getItem() instanceof TennoGunItem) {
            aim = TennoGunClient.getAimProgress();
        } else {
            return;
        }
        if (aim <= 0.01f) return;

        float zoom = SniperScopeOverlay.getOpticalMagnification(stack);
        if (zoom <= 1.02f) return;

        double baseFov = mc.options.fov().get();
        double currentZoom = 1.0 + (zoom - 1.0) * aim;
        double currentFov = SpringDamper.magnificationToFov(currentZoom, baseFov);
        double ratio = SpringDamper.zoomSensitivityRatio(currentFov, baseFov, MDV_COEFFICIENT);

        args.set(0, (double) args.get(0) * ratio);
        args.set(1, (double) args.get(1) * ratio);
    }
}
