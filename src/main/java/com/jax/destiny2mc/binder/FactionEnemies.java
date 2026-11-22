package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.atlas.DungeonNode;
import com.jax.destiny2mc.frontier.BossSigilItem;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/** Shared classification for enemy loot, combat tuning and optional career rewards. */
public final class FactionEnemies {
    private static final Set<String> FIELD_BOSSES = Set.of(
        "devil_walker", "vanok_arclite", "archon_priest", "persys_boss");

    private FactionEnemies() {}

    public static boolean isEnemy(LivingEntity entity) {
        // Dungeon puzzle targets inherit Monster so they can respond to weapon hits.
        return entity instanceof Mob && !(entity instanceof DungeonNode)
            && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals("destiny2mc");
    }

    public static boolean isBoss(LivingEntity entity) {
        return isEnemy(entity) && (entity instanceof FrontierMob mob && mob.unit.boss()
            || BossSigilItem.isEncounter(entity)
            || FIELD_BOSSES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath()));
    }
}
