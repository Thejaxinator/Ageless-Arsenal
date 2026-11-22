package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.frontier.FrontierWeaponInput;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/** Render the vanilla skin and sleeve with the palm fixed to a model-space point. */
public final class FirstPersonWeaponHands {
    private FirstPersonWeaponHands() {}

    static void render(PoseStack pose, MultiBufferSource buffers, LocalPlayer player,
                       WeaponSpec spec, int light, float reload, float partial) {
        if (player.isInvisible() || player.isSpectator()) return;
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (!(dispatcher.getRenderer(player) instanceof PlayerRenderer renderer)) return;

        var main = player.getMainArm();
        float sign = main == HumanoidArm.RIGHT ? 1 : -1;
        int i = spec.index;
        var fit = WeaponHandFit.BY_WEAPON[i];
        var grip = fit.grip();
        var rest = fit.support();
        var load = fit.loader();
        boolean drawing = SightFit.ACTION[i] == GunMechanism.Action.DRAW;
        float charge = drawing && FrontierWeaponInput.duration > 0
                ? Mth.clamp((float) FrontierWeaponInput.charge / FrontierWeaponInput.duration, 0, 1) : 0;
        float cycle = Mth.sin((float) Math.PI * Mth.clamp(
                (ClientWeaponState.fireCooldown - partial) / Math.max(1f, spec.fireDelayTicks), 0, 1));
        float travel = GunMechanism.envelope(reload);
        float gripY = grip.y() - .015f;
        float gripZ = grip.z();
        if (drawing) {
            gripZ += .18f * charge + .11f * travel;
            gripY -= .16f * travel;
        } else if (SightFit.ACTION[i] == GunMechanism.Action.LEVER) {
            gripY -= .075f * cycle;
            gripZ -= .035f * cycle;
        }
        arm(pose, buffers, renderer, player, main, light, grip.x() + .044f * sign, gripY, gripZ,
                sign * (drawing ? 38 : 24), drawing ? -53 : -58, sign * 7);

        // Keep the off hand available for a shield, map, or second item.
        if (!player.getOffhandItem().isEmpty()) return;
        float restX = rest.x() - .060f * sign;
        float restY = rest.y() - .018f;
        float restZ = rest.z();
        if (SightFit.ACTION[i] == GunMechanism.Action.PUMP) restZ += .145f * cycle;
        float targetX = load.x() - .062f * sign;
        float targetY = load.y() - .025f;
        float targetZ = load.z();
        switch (SightFit.LOAD[i]) {
            case CELL -> {
                // The palm stays on the lower corner as the magazine drops and cants out.
                float angle = travel * 7 * Mth.DEG_TO_RAD;
                float x = targetX * Mth.cos(angle) - targetY * Mth.sin(angle);
                targetY = targetX * Mth.sin(angle) + targetY * Mth.cos(angle) - .31f * travel;
                targetX = x - .045f * travel;
            }
            case CYLINDER -> {
                // The cylinder itself always opens to the model's left.
                targetX = load.x() - .112f - .19f * travel;
            }
            case BREAK, GATE -> {
                boolean gate = SightFit.LOAD[i] == GunMechanism.Load.GATE;
                float pivotY = ((gate ? 7.2f : 7.6f) - 8) / 16;
                float pivotZ = ((gate ? 13.5f : 13.1f) - 12) * SightFit.LENGTH[i] / 384;
                float angle = (gate ? 47 : -33) * travel * Mth.DEG_TO_RAD;
                float y = targetY - pivotY;
                float z = targetZ - pivotZ;
                targetY = pivotY + y * Mth.cos(angle) - z * Mth.sin(angle);
                targetZ = pivotZ + y * Mth.sin(angle) + z * Mth.cos(angle);
            }
            case PORT -> targetZ += .23f * travel;
            case NOCK -> {
                // The support hand braces the riser throughout the draw and nock.
                targetX = restX; targetY = restY; targetZ = restZ;
            }
        }
        float reach = ease(reload / .17f) * ease((1 - reload) / .19f);
        float sweep = 4 * reach * (1 - reach);
        arm(pose, buffers, renderer, player, main.getOpposite(), light,
                Mth.lerp(reach, restX, targetX) - sign * sweep * .068f,
                Mth.lerp(reach, restY, targetY) - sweep * .095f,
                Mth.lerp(reach, restZ, targetZ),
                -sign * (30 + reach * 10), -58 + reach * 14, -sign * (12 + reach * 20));
    }
    private static float ease(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }
    public static void arm(PoseStack pose, MultiBufferSource buffers, PlayerRenderer renderer,
                            LocalPlayer player, HumanoidArm side, int light,
                            float x, float y, float z, float yaw, float pitch, float roll) {
        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        boolean right = side == HumanoidArm.RIGHT;
        pose.pushPose();
        try {
            pose.translate(x, y, z);
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            pose.mulPose(Axis.XP.rotationDegrees(pitch));
            pose.mulPose(Axis.ZP.rotationDegrees(roll));
            pose.scale(.82f, 1.22f, .82f);
            // Vanilla animation moves the distal face slightly outwards at age zero.
            float lateral = (slim ? 5.5f : 6f) + .95f;
            pose.translate((right ? lateral : -lateral) / 16, -(slim ? 12.5f : 12f) / 16, 0);
            if (right) renderer.renderRightHand(pose, buffers, light, player);
            else renderer.renderLeftHand(pose, buffers, light, player);
        } finally {
            pose.popPose();
        }
    }
}
