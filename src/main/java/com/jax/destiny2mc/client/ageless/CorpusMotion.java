package com.jax.destiny2mc.client.ageless;

import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Per-instance additive animations for Warframe Corpus rigs (Crewman, MOA, Shield Osprey).
 * Uses the same baked ModelPart clock contract as DreadMotion for live mobs and Bestiary previews.
 */
public final class CorpusMotion {
    private final ModelPart root;
    private final int kind;
    private final Map<String, ModelPart> bones = new HashMap<>();
    private static final String[] SIDES = {"r", "l"};

    public CorpusMotion(FrontierModel model, int kind) {
        this.kind = kind;
        this.root = model.root();
        String parts = "body thorax head turret gun tail r_prong l_prong "
            + "r_arm l_arm r_forearm l_forearm r_hand l_hand "
            + "r_leg l_leg r_shin l_shin r_ankle l_ankle r_foot l_foot "
            + "r_wing l_wing";
        for (String part : parts.split(" ")) {
            model.getAnyDescendantWithName(part).ifPresent(p -> bones.put(part, p));
        }
    }

    private void turn(String name, float x, float y, float z) {
        ModelPart bone = bones.get(name);
        if (bone != null) {
            bone.xRot += x;
            bone.yRot += y;
            bone.zRot += z;
        }
    }

    private static float wave(float t) { return (float) Math.sin(t); }
    private static float clamp(float v) { return Math.max(0, Math.min(1, v)); }
    private static float ease(float v) { v = clamp(v); return v * v * (3 - 2 * v); }
    private static float recoil(float t, float hit) { return ease((t - hit + 4) / 4) * (1 - ease((t - hit) / 6)); }

    private void crewman(float stride, float amount, float age, float yaw, float pitch, int state, float t, boolean aim) {
        for (int i = 0; i < 2; i++) {
            String side = SIDES[i];
            float phase = i == 0 ? stride : -stride;
            turn(side + "_leg", 0.62f * phase, 0, 0);
            turn(side + "_shin", Math.max(0, -phase) * 0.65f, 0, 0);
            turn(side + "_foot", Math.min(0, phase) * 0.22f, 0, 0);
            turn(side + "_arm", -phase * 0.25f, 0, 0);
        }
        ModelPart body = bones.get("body");
        if (body != null) {
            body.y -= Math.abs(stride) * (0.30f + amount * 0.14f);
            turn("body", wave(age * 0.08f) * 0.015f, 0, 0);
        }
        // Dera rifle shoulder-aim & burst recoil
        if (aim || state == FrontierMob.FIRE || state == FrontierMob.CAST) {
            float kick = (state == FrontierMob.FIRE || state == FrontierMob.CAST) ? recoil(t, 10) * 0.18f : 0;
            turn("r_arm", -1.26f + pitch * 0.010f - kick, 0.12f, 0.08f);
            turn("r_forearm", -0.28f, 0, 0);
            turn("r_hand", 1.54f + kick * 0.8f, 0, 0);
            turn("l_arm", -1.04f + pitch * 0.008f - kick * 0.5f, -0.38f, -0.14f);
            turn("l_forearm", -0.52f, 0, 0);
        }
    }

    private void moa(float stride, float amount, float age, float yaw, float pitch, int state, float t, boolean aim) {
        // Reverse-jointed digitigrade walker gait
        for (int i = 0; i < 2; i++) {
            String side = SIDES[i];
            float phase = i == 0 ? stride : -stride;
            turn(side + "_leg", 0.55f * phase, 0, 0);
            turn(side + "_shin", -0.42f * phase + Math.max(0, -phase) * 0.30f, 0, 0);
            turn(side + "_ankle", 0.24f * phase, 0, 0);
            turn(side + "_foot", -0.20f * phase, 0, 0);
        }
        ModelPart body = bones.get("body");
        if (body != null) body.y -= Math.abs(stride) * (0.45f + amount * 0.20f);
        turn("thorax", wave(age * 0.11f) * 0.025f + Math.abs(stride) * 0.06f, 0, 0);
        // Dorsal plasma cannon tracks target yaw/pitch and kicks back on fire
        float radYaw = Math.max(-65, Math.min(65, yaw)) * 0.0174533f;
        float radPitch = Math.max(-30, Math.min(35, pitch)) * 0.0174533f;
        float kick = (state == FrontierMob.FIRE || state == FrontierMob.CAST) ? recoil(t, 12) * 0.24f : 0;
        turn("turret", 0, radYaw * 0.75f, 0);
        turn("gun", radPitch * 0.65f - kick, 0, 0);
    }

    private void osprey(float age, float yaw, float pitch, int state, float t) {
        float hover = wave(age * 0.16f);
        float pulse = state == FrontierMob.SHIELD ? (0.5f + 0.5f * wave(age * 0.35f)) : 0;
        ModelPart thorax = bones.get("thorax");
        if (thorax != null) thorax.y += hover * 0.85f;
        turn("thorax", 0.06f + pulse * 0.14f + wave(age * 0.12f) * 0.04f, 0, wave(age * 0.09f) * 0.05f);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            String side = SIDES[i];
            turn(side + "_wing",
                wave(age * 0.22f) * 0.04f,
                sign * (pulse * 0.12f),
                sign * (0.07f * wave(age * 0.18f) + pulse * 0.14f));
            turn(side + "_prong", 0.08f * wave(age * 0.16f - 0.5f) - pulse * 0.15f, 0, 0);
        }
        turn("tail", wave(age * 0.16f - 0.4f) * 0.10f - pulse * 0.18f, 0, 0);
    }

    public void sample(float swing, float amount, float age, float yaw, float pitch, int state, float t, boolean aim, int weak) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float stride = wave(swing * 0.6662f) * Math.min(1, amount);
        turn("head", pitch * 0.0174533f, Math.max(-72, Math.min(72, yaw)) * 0.0174533f, 0);
        if (kind == 145) {
            osprey(age, yaw, pitch, state, t);
        } else if (kind == 144) {
            moa(stride, amount, age, yaw, pitch, state, t, aim);
        } else {
            crewman(stride, amount, age, yaw, pitch, state, t, aim);
        }
    }
}
