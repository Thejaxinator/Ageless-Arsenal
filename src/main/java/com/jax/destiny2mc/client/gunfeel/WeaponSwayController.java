package com.jax.destiny2mc.client.gunfeel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Tactile first-person weapon motion controller for both Destiny and Tenno weapons.
 * Combines TACZ-style spring-damped jump/land inertia, split-axis ADS curves, and procedural
 * firing micro-jitter with Destiny-style mouse-look turn lag, strafe roll, and idle breathing.
 */
public final class WeaponSwayController {
    private static final SpringDamper LOOK_YAW_LAG = new SpringDamper(4.2f, 0.78f, 0.25f, 0.0f);
    private static final SpringDamper LOOK_PITCH_LAG = new SpringDamper(4.2f, 0.78f, 0.25f, 0.0f);
    private static final SpringDamper STRAFE_ROLL = new SpringDamper(3.4f, 0.85f, 0.2f, 0.0f);
    private static final SpringDamper JUMP_INERTIA = new SpringDamper(3.0f, 0.82f, 0.55f, 0.0f);
    private static final SpringDamper SPRINT_CANT = new SpringDamper(3.6f, 0.92f, 0.15f, 0.0f);

    private static long lastFrameNanos = -1L;
    private static float lastYaw = Float.NaN;
    private static float lastPitch = Float.NaN;
    private static boolean lastOnGround = true;
    private static float jumpTarget = 0.0f;
    private static long lastShotMillis = -1L;
    private static float shootSeedX = 0.0f;
    private static float shootSeedYaw = 0.0f;

    private static float currentScopeSwayX = 0.0f;
    private static float currentScopeSwayY = 0.0f;

    private WeaponSwayController() {}

    public static void onShotFired() {
        lastShotMillis = System.currentTimeMillis();
        shootSeedX = (float) ((Math.random() - 0.5) * 2.0);
        shootSeedYaw = (float) ((Math.random() - 0.5) * 2.0);
    }

    public static void reset() {
        lastFrameNanos = -1L;
        lastYaw = Float.NaN;
        lastPitch = Float.NaN;
        lastOnGround = true;
        jumpTarget = 0.0f;
        LOOK_YAW_LAG.reset(0.0f);
        LOOK_PITCH_LAG.reset(0.0f);
        STRAFE_ROLL.reset(0.0f);
        JUMP_INERTIA.reset(0.0f);
        SPRINT_CANT.reset(0.0f);
        currentScopeSwayX = 0.0f;
        currentScopeSwayY = 0.0f;
    }

    public static void applySway(PoseStack pose, LocalPlayer player, float aim, float tilt,
                                 float partial, float sign, float weight) {
        long nowNanos = System.nanoTime();
        float dt = lastFrameNanos < 0L ? (1.0f / 60.0f) : (nowNanos - lastFrameNanos) * 1.0e-9f;
        lastFrameNanos = nowNanos;
        dt = Mth.clamp(dt, 0.001f, 0.05f);

        float yaw = player.getViewYRot(partial);
        float pitch = player.getViewXRot(partial);
        float dYaw = 0.0f;
        float dPitch = 0.0f;
        if (!Float.isNaN(lastYaw)) {
            dYaw = Mth.wrapDegrees(yaw - lastYaw);
            dPitch = pitch - lastPitch;
        }
        lastYaw = yaw;
        lastPitch = pitch;

        float targetYawLag = Mth.clamp(-dYaw * 0.42f, -6.5f, 6.5f);
        float targetPitchLag = Mth.clamp(-dPitch * 0.36f, -5.0f, 5.0f);
        float yawLag = LOOK_YAW_LAG.update(dt, targetYawLag);
        float pitchLag = LOOK_PITCH_LAG.update(dt, targetPitchLag);

        float strafe = Mth.clamp(player.xxa, -1.0f, 1.0f);
        float strafeRoll = STRAFE_ROLL.update(dt, -strafe * 2.6f);

        double vy = player.getDeltaMovement().y;
        boolean onGround = player.onGround();
        if (onGround && !lastOnGround) {
            jumpTarget = Mth.clamp((float) (-vy * 1.9), 0.15f, 1.0f);
        } else if (!onGround && lastOnGround) {
            jumpTarget = -Mth.clamp((float) (vy * 1.5), 0.15f, 0.9f);
        } else {
            jumpTarget *= (float) Math.exp(-dt * (onGround ? 11.0 : 5.5));
        }
        lastOnGround = onGround;
        float jumpSway = JUMP_INERTIA.update(dt, jumpTarget);

        float sprintTarget = (player.isSprinting() && aim < 0.08f && tilt < 0.08f) ? 1.0f : 0.0f;
        float sprint = SPRINT_CANT.update(dt, sprintTarget);

        double t = nowNanos * 1.0e-9;
        float breathScale = Mth.lerp(aim, 1.0f, 0.22f) * (1.0f - tilt * 0.7f);
        float breathX = (float) Math.sin(t * 1.45) * 0.0032f * breathScale;
        float breathY = (float) Math.sin(t * 2.90) * 0.0022f * breathScale;

        currentScopeSwayX = yawLag * 1.15f + (float) Math.sin(t * 1.25) * 2.4f;
        currentScopeSwayY = pitchLag * 1.15f + (float) Math.sin(t * 2.50) * 1.8f + jumpSway * 5.0f;

        float hipFactor = (1.0f - aim * 0.84f) * (1.0f - tilt * 0.55f) * weight;

        var mc = Minecraft.getInstance();
        float stride = (mc.options.bobView().get() && onGround)
                ? Mth.clamp(player.walkAnimation.speed(partial), 0.0f, 1.0f) * (1.0f - aim * 0.78f) * (1.0f - tilt)
                : 0.0f;
        float step = player.walkAnimation.position(partial) * 0.6662f;
        float walkX = Mth.sin(step) * 0.015f * stride;
        float walkY = -Math.abs(Mth.cos(step)) * 0.016f * stride;
        float walkRoll = Mth.sin(step) * 1.15f * stride;

        float shootX = 0.0f;
        float shootY = 0.0f;
        float shootYaw = 0.0f;
        if (lastShotMillis > 0L) {
            long elapsed = System.currentTimeMillis() - lastShotMillis;
            if (elapsed >= 0L && elapsed < 240L) {
                float p = SpringDamper.easeOutCubic(1.0f - elapsed / 240.0f);
                float damp = 1.0f - aim * 0.75f;
                shootX = shootSeedX * 0.0075f * p * damp;
                shootY = -0.0045f * p * damp;
                shootYaw = shootSeedYaw * 0.95f * p * damp;
            }
        }

        float tx = walkX + breathX + shootX + yawLag * 0.0048f * hipFactor + sign * strafe * 0.009f * hipFactor - sign * 0.032f * sprint;
        float ty = walkY + breathY + shootY + pitchLag * 0.0042f * hipFactor - jumpSway * 0.036f * hipFactor - 0.038f * sprint;
        float tz = -0.025f * sprint;
        pose.translate(tx, ty, tz);

        pose.mulPose(Axis.YP.rotationDegrees(yawLag * 0.62f * hipFactor + shootYaw - sign * 18.0f * sprint));
        pose.mulPose(Axis.XP.rotationDegrees(-pitchLag * 0.55f * hipFactor - jumpSway * 3.2f * hipFactor - 8.0f * sprint));
        pose.mulPose(Axis.ZP.rotationDegrees(walkRoll + strafeRoll * hipFactor + yawLag * 0.55f * hipFactor + sign * 7.5f * sprint));
    }

    public static float adsX(float aim) {
        return SpringDamper.easeOutCubic(aim);
    }

    public static float adsY(float aim) {
        return SpringDamper.smoothstep(SpringDamper.window(aim, 0.0f, 0.85f));
    }

    public static float adsZ(float aim) {
        return SpringDamper.easeInOutCubic(SpringDamper.window(aim, 0.18f, 1.0f));
    }

    public static float adsRot(float aim) {
        return SpringDamper.easeOutSine(aim);
    }

    public static float scopeSwayX() {
        return currentScopeSwayX;
    }

    public static float scopeSwayY() {
        return currentScopeSwayY;
    }
}
