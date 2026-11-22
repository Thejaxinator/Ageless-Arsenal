package com.jax.destiny2mc.client;

import com.jax.destiny2mc.item.DestinyGunItem;
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
        float reload = GunMechanism.progress(stack,gun.spec,player.level().getGameTime(),partial);
        var pose = event.getPoseStack();
        pose.pushPose();
        try {
            // The item renderer subtracts (8,8,8) before drawing the baked meshes.
            float tilt = GunMechanism.envelope(reload);
            float hip = (1 - aim) * (1 - tilt);
            float step = player.walkAnimation.position(partial) * .6662f;
            float stride = mc.options.bobView().get() && player.onGround()
                    ? Mth.clamp(player.walkAnimation.speed(partial), 0, 1) * hip : 0;
            float sway = Mth.sin(step) * stride;
            pose.translate(.013f * sway, -.015f * Math.abs(Mth.cos(step)) * stride, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(.9f * sway));
            float swing = Mth.sin(event.getSwingProgress() * (float)Math.PI) * hip;
            pose.mulPose(Axis.YP.rotationDegrees(sign * 1.5f * swing));
            float y = Mth.lerp(aim,-.34f,-(SightFit.AXIS[index]-8f) / 16f * size);
            float z = Mth.lerp(aim,-.99f,SightFit.ADS_Z[index]);
            pose.translate(sign * .32f * (1-aim), y-.08f*tilt, z+Math.min(.065f,recoil*.024f));
            pose.mulPose(Axis.YP.rotationDegrees(sign*(17f)*(1-aim)));
            pose.mulPose(Axis.XP.rotationDegrees(3.2f*(1-aim)+Math.min(7f,recoil*(1.2f+.42f*gun.spec.recoilPitch))));
            pose.mulPose(Axis.ZP.rotationDegrees(sign*(tilt*25f+ClientWeaponState.recoilRoll*.36f)));
            pose.scale(size,size,size);
            int light = ClientWeaponState.muzzleFlashTicks>0 ? LightTexture.FULL_BRIGHT : event.getPackedLight();
            FirstPersonWeaponHands.render(pose, event.getMultiBufferSource(), player, gun.spec,
                    event.getPackedLight(), reload, partial);
            GunMechanism.render(pose,event.getMultiBufferSource(),stack,gun.spec,light,reload,partial);
        } finally { pose.popPose(); }
    }
}
