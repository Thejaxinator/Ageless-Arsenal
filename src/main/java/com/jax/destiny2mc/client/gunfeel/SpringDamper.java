package com.jax.destiny2mc.client.gunfeel;

import net.minecraft.util.Mth;

/**
 * Framerate-independent 2nd-order spring-damper inspired by TACZ's SecondOrderDynamics,
 * integrated analytically on the render thread with clamped dt (no background thread pool),
 * plus split-axis ADS easing curves and true optical magnification / MDV133 sensitivity math.
 */
public final class SpringDamper {
    private final float k1;
    private final float k2;
    private final float k3;

    private float xp;
    private float y;
    private float yd;

    public SpringDamper(float f, float z, float r, float x0) {
        float twoPiF = (float) (2.0 * Math.PI * f);
        this.k1 = z / (float) (Math.PI * f);
        this.k2 = 1.0f / (twoPiF * twoPiF);
        this.k3 = (r * z) / twoPiF;
        this.xp = x0;
        this.y = x0;
        this.yd = 0.0f;
    }

    public float update(float dt, float x) {
        float step = Mth.clamp(dt, 0.0005f, 0.05f);
        float xd = (x - xp) / step;
        xp = x;
        float k2Stable = Math.max(k2, Math.max(step * step * 0.5f + step * k1 * 0.5f, step * k1));
        y += step * yd;
        yd += step * (x + k3 * xd - y - k1 * yd) / k2Stable;
        if (!Float.isFinite(y)) y = x;
        if (!Float.isFinite(yd)) yd = 0.0f;
        return y;
    }

    public float value() {
        return y;
    }

    public void reset(float x0) {
        this.xp = x0;
        this.y = x0;
        this.yd = 0.0f;
    }

    public static float window(float progress, float start, float end) {
        return Mth.clamp((progress - start) / (end - start), 0.0f, 1.0f);
    }

    public static float smoothstep(float t) {
        float c = Mth.clamp(t, 0.0f, 1.0f);
        return c * c * (3.0f - 2.0f * c);
    }

    public static float easeOutCubic(float t) {
        float u = 1.0f - Mth.clamp(t, 0.0f, 1.0f);
        return 1.0f - u * u * u;
    }

    public static float easeInOutCubic(float t) {
        float c = Mth.clamp(t, 0.0f, 1.0f);
        return c < 0.5f ? 4.0f * c * c * c : 1.0f - (float) Math.pow(-2.0 * c + 2.0, 3.0) * 0.5f;
    }

    public static float easeOutSine(float t) {
        return (float) Math.sin(Mth.clamp(t, 0.0f, 1.0f) * (Math.PI * 0.5));
    }

    public static double magnificationToFov(double magnification, double baseFov) {
        if (magnification <= 1.0001) return baseFov;
        double currentTan = Math.tan(Math.toRadians(baseFov * 0.5));
        double newTan = currentTan / magnification;
        return Math.toDegrees(Math.atan(newTan)) * 2.0;
    }

    public static double zoomSensitivityRatio(double currentFov, double baseFov, double mdvCoefficient) {
        double cur = Math.max(1.0, currentFov);
        double orig = Math.max(1.0, baseFov);
        return Math.atan(Math.tan(Math.toRadians(cur * 0.5)) * mdvCoefficient)
                / Math.atan(Math.tan(Math.toRadians(orig * 0.5)) * mdvCoefficient);
    }
}
