package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.gunfeel.SniperScopeOverlay;
import com.jax.destiny2mc.client.gunfeel.WeaponSwayController;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RenderHandEvent;

public class FirstPersonGunRenderer {
    public static void onRenderHand(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof DestinyGunItem gun)) return;
        float partial = event.getPartialTick();
        float aim = ClientWeaponState.getSmoothAds(partial);
        if (event.getHand() == InteractionHand.OFF_HAND) {
            if (aim > 0.12f) event.setCanceled(true);
            return;
        }
        event.setCanceled(true);
        var stack = player.getMainHandItem();
        int index = gun.spec.index;
        float size = SightFit.SCALE[index];
        float sign = player.getMainArm() == HumanoidArm.LEFT ? -1 : 1;
        float recoil = ClientWeaponState.getSmoothRecoil(partial);
        float reload = GunMechanism.progress(stack, gun.spec, player.level().getGameTime(), partial);
        float tilt = GunMechanism.envelope(reload);

        var pose = event.getPoseStack();
        pose.pushPose();
        try {
            WeaponSwayController.applySway(pose, player, aim, tilt, partial, sign, weaponWeight(gun.spec));

            if (SniperScopeOverlay.shouldHideFirstPersonModel(stack, aim)) {
                return;
            }

            float ax = WeaponSwayController.adsX(aim);
            float ay = WeaponSwayController.adsY(aim);
            float az = WeaponSwayController.adsZ(aim);
            float ar = WeaponSwayController.adsRot(aim);

            float hip = (1.0f - aim) * (1.0f - tilt);
            float swing = Mth.sin(event.getSwingProgress() * (float) Math.PI) * hip;
            pose.mulPose(Axis.YP.rotationDegrees(sign * 1.5f * swing));

            float adsY = -(SightFit.AXIS[index] - 8.0f) / 16.0f * size;
            float adsZ = Mth.clamp(SightFit.ADS_Z[index], -0.82f, -0.52f);

            float x = sign * 0.32f * (1.0f - ax);
            float y = Mth.lerp(ay, -0.34f, adsY);
            float z = Mth.lerp(az, -0.99f, adsZ);

            float recoilZ = Math.min(0.068f, recoil * 0.025f) * Mth.lerp(aim, 1.0f, 0.72f);
            float recoilPitchDeg = Math.min(7.5f, recoil * (1.2f + 0.42f * gun.spec.recoilPitch)) * Mth.lerp(aim, 1.0f, 0.65f);

            pose.translate(x, y - 0.08f * tilt, z + recoilZ);
            pose.mulPose(Axis.YP.rotationDegrees(sign * 17.0f * (1.0f - ar)));
            pose.mulPose(Axis.XP.rotationDegrees(3.2f * (1.0f - ar) + recoilPitchDeg));
            pose.mulPose(Axis.ZP.rotationDegrees(sign * (tilt * 25.0f + ClientWeaponState.recoilRoll * 0.38f)));
            pose.scale(size, size, size);

            int light = ClientWeaponState.muzzleFlashTicks > 0 ? LightTexture.FULL_BRIGHT : event.getPackedLight();
            FirstPersonWeaponHands.render(pose, event.getMultiBufferSource(), player, gun.spec,
                    event.getPackedLight(), reload, partial);
            GunMechanism.render(pose, event.getMultiBufferSource(), stack, gun.spec, light, reload, partial);
        } finally {
            pose.popPose();
        }
    }

    private static float weaponWeight(WeaponSpec spec) {
        return switch (spec.archetype) {
            case 1 -> spec.rpm >= 200 ? 0.78f : 0.90f;
            case 2 -> spec.fovZoom <= 0.44f ? 1.25f : 1.05f;
            case 7, 8 -> 1.32f;
            case 3, 4, 5 -> 1.12f;
            default -> spec.magSize >= 55 ? 1.28f : (spec.range <= 48.0f ? 0.82f : 1.0f);
        };
    }
}
