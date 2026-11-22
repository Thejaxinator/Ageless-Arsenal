package com.jax.destiny2mc.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

/**
 * Biome keys and Overworld climate-slice routing for the four main Destiny factions.
 * Each biome claims a dedicated positive-weirdness sub-band of a matching vanilla host climate,
 * leaving all vanilla biomes intact while guaranteeing natural Overworld generation without external dependencies.
 */
public final class FactionBiomes {
    public static final ResourceKey<Biome> FALLEN_TIMBERLAND = key("fallen_timberland");
    public static final ResourceKey<Biome> HIVE_CRAGS = key("hive_crags");
    public static final ResourceKey<Biome> VEX_EXPANSE = key("vex_expanse");
    public static final ResourceKey<Biome> CABAL_BARRENS = key("cabal_barrens");

    private static final long WEIRDNESS_MID_POSITIVE = Climate.quantizeCoord(0.266F);
    private static final long WEIRDNESS_HIGH_POSITIVE = Climate.quantizeCoord(0.4F);
    private static final long MID_INLAND_MIN = Climate.quantizeCoord(0.03F);

    private FactionBiomes() {}

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("destiny2mc", path));
    }

    /**
     * Returns a faction biome key to substitute into OverworldBiomeBuilder.addSurfaceBiome,
     * or null to keep the vanilla biome unchanged.
     */
    public static ResourceKey<Biome> pickReplacement(
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter weirdness,
        ResourceKey<Biome> vanillaBiome
    ) {
        if (vanillaBiome == null) {
            return null;
        }
        long weirdMin = weirdness.min();
        long contMin = continentalness.min();

        // Fallen: overcast pine/foresty EDZ-style timberland on positive high-weirdness taiga slices
        if ((vanillaBiome.equals(Biomes.TAIGA) || vanillaBiome.equals(Biomes.OLD_GROWTH_PINE_TAIGA))
            && weirdMin >= WEIRDNESS_HIGH_POSITIVE) {
            return FALLEN_TIMBERLAND;
        }

        // Hive: craggy rocky Hellmouth highlands carved from windswept hills
        if ((vanillaBiome.equals(Biomes.WINDSWEPT_HILLS) || vanillaBiome.equals(Biomes.WINDSWEPT_GRAVELLY_HILLS))
            && weirdMin >= WEIRDNESS_MID_POSITIVE) {
            return HIVE_CRAGS;
        }

        // Vex: broad Nessus/Pyramidion radiolaria expanse on positive high-weirdness savanna slices
        if ((vanillaBiome.equals(Biomes.SAVANNA) || vanillaBiome.equals(Biomes.SAVANNA_PLATEAU))
            && weirdMin >= WEIRDNESS_HIGH_POSITIVE) {
            return VEX_EXPANSE;
        }

        // Cabal: scorched dead-tree barrens on inland positive high-weirdness badlands/desert slices
        if ((vanillaBiome.equals(Biomes.BADLANDS) || vanillaBiome.equals(Biomes.WOODED_BADLANDS)
            || vanillaBiome.equals(Biomes.DESERT))
            && weirdMin >= WEIRDNESS_HIGH_POSITIVE
            && contMin >= MID_INLAND_MIN) {
            return CABAL_BARRENS;
        }

        return null;
    }
}
