package com.jax.destiny2mc.binder.progression;

/** Pure arithmetic shared by the server and the sidebar. NBT stores lifetime career points;
 * spending vanilla levels at a binder never changes this balance. */
public final class CareerCurve {
    public static final int CAP = 50;
    private CareerCurve() {}

    /** First level starts at zero. Each new threshold takes twelve additional points. */
    public static int threshold(int level) {
        int steps = Math.clamp(level, 1, CAP) - 1;
        return 40 * steps + 6 * steps * (steps - 1);
    }

    public static int clampPoints(long points) {
        return (int)Math.clamp(points, 0L, (long)threshold(CAP));
    }

    public static int level(int points) {
        int result = 1;
        while (result < CAP && points >= threshold(result + 1)) result++;
        return result;
    }

    /** Bonus in health points (two points per heart). Fixed additions preserve equipment bonuses. */
    public static float healthBonus(int level) {
        int n = Math.clamp(level, 1, CAP);
        return n <= 20 ? 20f * (n - 1) / 19f : 20f + 20f * (n - 20) / 30f;
    }
    public static int armorBonus(int level) { return Math.clamp(level, 1, CAP) / 10; }
    public static int veteranChance(int level) { return (Math.clamp(level, 1, CAP) - 1) * 28 / 49; }
}
