package com.jax.destiny2mc.frontier;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Registers natural spawn placements for legacy Fallen, Hive, and Vex entities.
 * Hive spawns freely underground (caves / below surface) and only rarely on the
 * surface in field and forest biomes.
 */
@EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
public final class FactionSpawnPlacements {
    public static final TagKey<Biome> HIVE_SURFACE_BIOMES = TagKey.create(
        Registries.BIOME, ResourceLocation.fromNamespaceAndPath("destiny2mc", "has_hive_surface"));
    private static final int SURFACE_Y_THRESHOLD = 56;
    private static final int HIVE_SURFACE_RARITY = 8; // ~12.5% of valid dark surface checks in fields/forests

    private FactionSpawnPlacements() {}

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void register(RegisterSpawnPlacementsEvent event) {
        // Fallen (leaders + smaller squad units)
        EntityType<Monster>[] fallen = new EntityType[] {
            (EntityType<Monster>) ModRegistries.FALLEN_CAPTAIN_RIFLE.get(),
            (EntityType<Monster>) ModRegistries.FALLEN_CAPTAIN_SCORCH.get(),
            (EntityType<Monster>) ModRegistries.FALLEN_BRUISER.get(),
            (EntityType<Monster>) ModRegistries.DEVILS_SERVITOR.get(),
            (EntityType<Monster>) ModRegistries.HEAVY_SHANK.get(),
            (EntityType<Monster>) ModRegistries.FALLEN_DREG.get(),
            (EntityType<Monster>) ModRegistries.FALLEN_WRETCH.get(),
            (EntityType<Monster>) ModRegistries.FALLEN_VANDAL_SNIPER.get(),
            (EntityType<Monster>) ModRegistries.FALLEN_SHANK.get(),
            (EntityType<Monster>) ModRegistries.TRACER_SHANK.get(),
            (EntityType<Monster>) ModRegistries.EXPLODER_SHANK.get()
        };
        for (EntityType<Monster> type : fallen) {
            event.register(
                type,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }

        // Vex (leaders + smaller squad units)
        EntityType<Monster>[] vex = new EntityType[] {
            (EntityType<Monster>) ModRegistries.VEX_MINOTAUR.get(),
            (EntityType<Monster>) ModRegistries.VEX_HYDRA.get(),
            (EntityType<Monster>) ModRegistries.VEX_WYVERN.get(),
            (EntityType<Monster>) ModRegistries.VEX_GOBLIN.get(),
            (EntityType<Monster>) ModRegistries.VEX_HOBGOBLIN.get(),
            (EntityType<Monster>) ModRegistries.VEX_HARPY.get()
        };
        for (EntityType<Monster> type : vex) {
            event.register(
                type,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }

        // Hive (underground-heavy, rare on surface in fields and forests)
        EntityType<Monster>[] hive = new EntityType[] {
            (EntityType<Monster>) ModRegistries.HIVE_KNIGHT.get(),
            (EntityType<Monster>) ModRegistries.HIVE_WIZARD.get(),
            (EntityType<Monster>) ModRegistries.HIVE_OGRE.get(),
            (EntityType<Monster>) ModRegistries.LUCENT_KNIGHT.get(),
            (EntityType<Monster>) ModRegistries.HIVE_THRALL.get(),
            (EntityType<Monster>) ModRegistries.HIVE_CURSED_THRALL.get(),
            (EntityType<Monster>) ModRegistries.HIVE_ACOLYTE.get()
        };
        for (EntityType<Monster> type : hive) {
            event.register(
                type,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FactionSpawnPlacements::checkHiveSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }
    }

    /**
     * Hive spawns freely underground (no sky view or below Y=56). On the surface,
     * Hive only spawns rarely (~12.5%) and only in field/forest biomes tagged by
     * {@code #destiny2mc:has_hive_surface}.
     */
    public static boolean checkHiveSpawnRules(
        EntityType<? extends Monster> type,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        BlockPos pos,
        RandomSource random
    ) {
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) {
            return false;
        }
        if (spawnType != MobSpawnType.NATURAL) {
            return true;
        }
        boolean underground = !level.canSeeSky(pos) || pos.getY() < SURFACE_Y_THRESHOLD;
        if (underground) {
            return true;
        }
        return level.getBiome(pos).is(HIVE_SURFACE_BIOMES) && random.nextInt(HIVE_SURFACE_RARITY) == 0;
    }
}
