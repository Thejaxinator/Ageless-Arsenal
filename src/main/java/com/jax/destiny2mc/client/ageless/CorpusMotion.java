package com.jax.destiny2mc.client.ageless;

import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Per-instance additive animations for all 10 Warframe Corpus rigs (kinds 143-152):
 * Crewman / Prod / Tech / Nullifier, MOA / Shockwave MOA, Shield Osprey,
 * Isolator Bursa, Hyena Proxy, and Scyto Raknoid.
 */
public final class CorpusMotion {
    private final ModelPart root;
    private final int kind;
    private final Map<String, ModelPart> bones = new HashMap<>();
    private static final String[] SIDES = {"r", "l"};

    public CorpusMotion(FrontierModel model, int kind) {
        this.kind = kind;
        this.root = model.root();
        String parts = "body thorax abdomen head turret gun tail r_prong l_prong dome console "
            + "nullifier_pack nullifier_drone r_shield l_shield r_pod l_pod "
            + "r_arm l_arm r_forearm l_forearm r_hand l_hand "
            + "r_leg l_leg r_shin l_shin r_ankle l_ankle r_foot l_foot r_wing l_wing "
            + "rf_leg lf_leg rf_shin lf_shin rf_paw lf_paw rb_leg lb_leg rb_shin lb_shin rb_paw lb_paw "
            + "fr_leg fl_leg br_leg bl_leg fr_femur fl_femur br_femur bl_femur fr_shin fl_shin br_shin bl_shin";
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
        if (kind == 149) {
            ModelPart drone = bones.get("nullifier_drone");
            if (drone != null) {
                drone.y += wave(age * 0.18f) * 0.45f;
                drone.yRot += age * 0.12f;
            }
        }
        if (kind == 146 && (state == FrontierMob.MELEE || state == FrontierMob.FIRE || state == FrontierMob.CAST)) {
            float slash = wave(t * 0.5f) * 0.95f;
            turn("r_arm", -1.45f + slash, 0.25f, 0.12f);
            turn("r_forearm", -0.45f, 0, 0);
        } else if (aim || state == FrontierMob.FIRE || state == FrontierMob.CAST) {
            float kick = (state == FrontierMob.FIRE || state == FrontierMob.CAST) ? recoil(t, 10) * 0.18f : 0;
            turn("r_arm", -1.26f + pitch * 0.010f - kick, 0.12f, 0.08f);
            turn("r_forearm", -0.28f, 0, 0);
            turn("r_hand", 1.54f + kick * 0.8f, 0, 0);
            turn("l_arm", -1.04f + pitch * 0.008f - kick * 0.5f, -0.38f, -0.14f);
            turn("l_forearm", -0.52f, 0, 0);
        }
    }

    private void moa(float stride, float amount, float age, float yaw, float pitch, int state, float t) {
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
        float radYaw = Math.max(-65, Math.min(65, yaw)) * 0.0174533f;
        float radPitch = Math.max(-30, Math.min(35, pitch)) * 0.0174533f;
        float kick = (state == FrontierMob.FIRE || state == FrontierMob.CAST) ? recoil(t, 12) * 0.24f : 0;
        turn("turret", 0, radYaw * 0.75f, 0);
        turn("gun", radPitch * 0.65f - kick, 0, 0);
    }

    private void osprey(float age, int state) {
        float hover = wave(age * 0.16f);
        float pulse = state == FrontierMob.SHIELD ? (0.5f + 0.5f * wave(age * 0.35f)) : 0;
        ModelPart thorax = bones.get("thorax");
        if (thorax != null) thorax.y += hover * 0.85f;
        turn("thorax", 0.06f + pulse * 0.14f + wave(age * 0.12f) * 0.04f, 0, wave(age * 0.09f) * 0.05f);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            String side = SIDES[i];
            turn(side + "_wing", wave(age * 0.22f) * 0.04f, sign * (pulse * 0.12f),
                sign * (0.07f * wave(age * 0.18f) + pulse * 0.14f));
            turn(side + "_prong", 0.08f * wave(age * 0.16f - 0.5f) - pulse * 0.15f, 0, 0);
        }
        turn("tail", wave(age * 0.16f - 0.4f) * 0.10f - pulse * 0.18f, 0, 0);
    }

    private void bursa(float stride, float amount, float age, float yaw, float pitch, int state, float t) {
        for (int i = 0; i < 2; i++) {
            String side = SIDES[i];
            float phase = i == 0 ? stride : -stride;
            turn(side + "_leg", 0.38f * phase, 0, 0);
            turn(side + "_shin", -0.28f * phase, 0, 0);
            turn(side + "_foot", -0.10f * phase, 0, 0);
        }
        ModelPart body = bones.get("body");
        if (body != null) body.y -= Math.abs(stride) * (0.35f + amount * 0.15f);
        turn("thorax", wave(age * 0.09f) * 0.02f, 0, stride * 0.04f);
        boolean brace = state == FrontierMob.SHIELD || state == FrontierMob.CAST;
        float shieldIn = brace ? 0.24f : (wave(age * 0.10f) * 0.04f);
        turn("r_shield", 0, -shieldIn, 0);
        turn("l_shield", 0, shieldIn, 0);
        float kick = (state == FrontierMob.FIRE) ? recoil(t, 10) * 0.22f : 0;
        turn("r_pod", -kick, 0, 0);
        turn("l_pod", -kick * 0.6f, 0, 0);
    }

    private void hyena(float stride, float amount, float age, float yaw, float pitch, int state, float t) {
        turn("rf_leg", 0.55f * stride, 0, 0);
        turn("lf_leg", -0.55f * stride, 0, 0);
        turn("rb_leg", -0.52f * stride, 0, 0);
        turn("lb_leg", 0.52f * stride, 0, 0);
        turn("rf_shin", Math.max(0, -stride) * 0.38f, 0, 0);
        turn("lf_shin", Math.max(0, stride) * 0.38f, 0, 0);
        turn("rb_shin", Math.max(0, stride) * 0.35f, 0, 0);
        turn("lb_shin", Math.max(0, -stride) * 0.35f, 0, 0);
        ModelPart body = bones.get("body");
        if (body != null) body.y -= Math.abs(stride) * (0.40f + amount * 0.22f);
        turn("thorax", wave(age * 0.14f) * 0.03f, 0, stride * 0.05f);
        turn("tail", wave(age * 0.20f) * 0.08f, wave(age * 0.15f) * 0.14f, 0);
        float kick = (state == FrontierMob.FIRE || state == FrontierMob.CAST) ? recoil(t, 10) * 0.20f : 0;
        turn("turret", -kick, Math.max(-45, Math.min(45, yaw)) * 0.0174533f * 0.6f, 0);
    }

    private void raknoid(float stride, float amount, float age, int state) {
        String[] legs = {"fr", "fl", "br", "bl"};
        float[] signs = {1f, -1f, -1f, 1f};
        for (int i = 0; i < 4; i++) {
            float ph = stride * signs[i];
            turn(legs[i] + "_leg", 0, ph * 0.28f, 0);
            turn(legs[i] + "_femur", -Math.max(0, ph) * 0.22f, 0, 0);
            turn(legs[i] + "_shin", Math.max(0, ph) * 0.24f, 0, 0);
        }
        ModelPart body = bones.get("body");
        if (body != null) body.y -= Math.abs(stride) * 0.35f;
        float pulse = wave(age * 0.15f) * 0.04f + (state == FrontierMob.CAST ? 0.10f : 0);
        turn("dome", pulse, 0, wave(age * 0.11f) * 0.03f);
    }

    public void sample(float swing, float amount, float age, float yaw, float pitch, int state, float t, boolean aim, int weak) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float stride = wave(swing * 0.6662f) * Math.min(1, amount);
        turn("head", pitch * 0.0174533f, Math.max(-72, Math.min(72, yaw)) * 0.0174533f, 0);
        if (kind == 145) {
            osprey(age, state);
        } else if (kind == 144 || kind == 148) {
            moa(stride, amount, age, yaw, pitch, state, t);
        } else if (kind == 150) {
            bursa(stride, amount, age, yaw, pitch, state, t);
        } else if (kind == 151) {
            hyena(stride, amount, age, yaw, pitch, state, t);
        } else if (kind == 152) {
            raknoid(stride, amount, age, state);
        } else {
            crewman(stride, amount, age, yaw, pitch, state, t, aim);
        }
    }
}
