package com.jax.destiny2mc.binder.client;

/** Optional read-only extension for the inventory sidebar. Core never sends a career update.
 * The progression edition publishes a snapshot after login, respawn, or a credited kill. */
public final class BinderProgressView {
    private BinderProgressView() {}
    public record Info(int level, int xp, int needed, float healthBonus, float armorBonus, int veteranChance) {}
    private static volatile Info latest;
    public static Info read() { return latest; }
    public static void publish(Info value) { latest = value; }
}
