package com.jax.destiny2mc.binder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One authoritative catalog drives the shop, server prices, slot type and cooldown checks. */
public final class BinderCatalog {
    private static final Map<String, BinderAbility> INDEX = new LinkedHashMap<>();
    private static final List<BinderAbility> ALL;
    static {
        ActiveAbilities.catalog().forEach(BinderCatalog::add);
        PassiveAbilities.catalog().forEach(BinderCatalog::add);
        ALL = List.copyOf(INDEX.values());
    }
    private static void add(BinderAbility ability) {
        if (INDEX.putIfAbsent(ability.id(), ability) != null)
            throw new IllegalStateException("Duplicate ability ID " + ability.id());
    }
    public static List<BinderAbility> all() { return ALL; }
    public static BinderAbility get(String id) { return INDEX.get(id); }
}
