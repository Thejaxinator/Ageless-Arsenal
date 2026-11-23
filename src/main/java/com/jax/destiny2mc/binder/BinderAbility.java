package com.jax.destiny2mc.binder;

/** Stable IDs are saved in player NBT. Costs are inventory motes and whole vanilla XP levels.
 * Cooldowns use server game ticks. Display strings and balance can change without migrating slots. */
public record BinderAbility(String id, String name, String element, String category, String description,
                            int motes, int levels, int cooldownTicks) {
    public BinderAbility {
        if (!id.matches("[a-z][a-z0-9_]{0,47}") || name.isBlank() || description.isBlank()
                || motes < 0 || levels < 0 || cooldownTicks < 0)
            throw new IllegalArgumentException("Invalid Energy Binder entry " + id);
    }

    public boolean isPassive() {
        return category.equals("movement") || category.equals("jump") || category.equals("passive")
                || category.equals("warframe_passive") || category.equals("warframe_movement");
    }

    public boolean isWarframe() {
        return category.startsWith("warframe_");
    }
}
