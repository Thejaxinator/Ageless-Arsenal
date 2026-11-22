package com.jax.destiny2mc.world;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * Custom surface rules for the four Destiny faction biomes, prepended safely above
 * vanilla Overworld surface rules so bedrock and vanilla biomes remain unaffected.
 */
public final class FactionSurfaceRules {
    private static volatile SurfaceRules.RuleSource cachedOriginal;
    private static volatile SurfaceRules.RuleSource cachedWrapped;
    private static SurfaceRules.RuleSource factionRules;

    private FactionSurfaceRules() {}

    public static SurfaceRules.RuleSource wrap(SurfaceRules.RuleSource original) {
        if (original == null) {
            return null;
        }
        if (original == cachedOriginal && cachedWrapped != null) {
            return cachedWrapped;
        }
        SurfaceRules.RuleSource rules = getOrBuildRules();
        SurfaceRules.RuleSource wrapped = SurfaceRules.sequence(rules, original);
        cachedOriginal = original;
        cachedWrapped = wrapped;
        return wrapped;
    }

    private static SurfaceRules.RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    private static SurfaceRules.RuleSource getOrBuildRules() {
        if (factionRules != null) {
            return factionRules;
        }

        // Fallen Timberland: mossy forest floor with podzol, coarse dirt, and weathered mossy cobble slopes
        SurfaceRules.RuleSource fallenSurface = SurfaceRules.ifTrue(
            SurfaceRules.isBiome(FactionBiomes.FALLEN_TIMBERLAND),
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), state(Blocks.MOSSY_COBBLESTONE)),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.ifTrue(
                        SurfaceRules.waterBlockCheck(-1, 0),
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.35D, 1.0D), state(Blocks.PODZOL)),
                            SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.35D, -0.15D), state(Blocks.COARSE_DIRT)),
                            state(Blocks.GRASS_BLOCK)
                        )
                    )
                ),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.DIRT))
            )
        );

        // Hive Crags: rocky deepslate, tuff, blackstone, basalt, calcified bone streaks, and soul soil hollows
        SurfaceRules.RuleSource hiveSurface = SurfaceRules.ifTrue(
            SurfaceRules.isBiome(FactionBiomes.HIVE_CRAGS),
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), state(Blocks.BLACKSTONE)),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.45D, 1.0D), state(Blocks.BONE_BLOCK)),
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.12D, 0.45D), state(Blocks.BLACKSTONE)),
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.22D, 0.12D), state(Blocks.DEEPSLATE)),
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.48D, -0.22D), state(Blocks.TUFF)),
                        state(Blocks.SOUL_SOIL)
                    )
                ),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.COBBLED_DEEPSLATE))
            )
        );

        // Vex Expanse: Nessus/Pyramidion machined stone, polished andesite, and crimson terracotta flats
        SurfaceRules.RuleSource vexSurface = SurfaceRules.ifTrue(
            SurfaceRules.isBiome(FactionBiomes.VEX_EXPANSE),
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), state(Blocks.DEEPSLATE)),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.ifTrue(
                        SurfaceRules.waterBlockCheck(-1, 0),
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.25D, 1.0D), state(Blocks.RED_TERRACOTTA)),
                            SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.05D, 0.25D), state(Blocks.SMOOTH_STONE)),
                            SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.35D, -0.05D), state(Blocks.POLISHED_ANDESITE)),
                            state(Blocks.TERRACOTTA)
                        )
                    )
                ),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.STONE))
            )
        );

        // Cabal Barrens: scorched Mars/Firebase Hades rust terrain with basalt blast scars
        SurfaceRules.RuleSource cabalSurface = SurfaceRules.ifTrue(
            SurfaceRules.isBiome(FactionBiomes.CABAL_BARRENS),
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), state(Blocks.TERRACOTTA)),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.35D, 1.0D), state(Blocks.BASALT)),
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.05D, 0.35D), state(Blocks.ORANGE_TERRACOTTA)),
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.30D, 0.05D), state(Blocks.RED_SAND)),
                        state(Blocks.COARSE_DIRT)
                    )
                ),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.RED_SANDSTONE))
            )
        );

        factionRules = SurfaceRules.ifTrue(
            SurfaceRules.abovePreliminarySurface(),
            SurfaceRules.sequence(fallenSurface, hiveSurface, vexSurface, cabalSurface)
        );
        return factionRules;
    }
}
