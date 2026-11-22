package com.jax.destiny2mc.world.mixin;

import com.jax.destiny2mc.world.FactionBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes dedicated positive-weirdness climate slices in OverworldBiomeBuilder
 * to the four Destiny faction biomes without removing any vanilla biomes.
 */
@Mixin(value = OverworldBiomeBuilder.class, remap = false)
public abstract class OverworldBiomeBuilderMixin {
    @Shadow
    private void addSurfaceBiome(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter weirdness,
        float offset,
        ResourceKey<Biome> biome
    ) {}

    @Inject(method = "addSurfaceBiome", at = @At("HEAD"), cancellable = true)
    private void ageless$injectFactionBiomes(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter weirdness,
        float offset,
        ResourceKey<Biome> biome,
        CallbackInfo ci
    ) {
        ResourceKey<Biome> replacement = FactionBiomes.pickReplacement(
            temperature, humidity, continentalness, erosion, weirdness, biome
        );
        if (replacement != null) {
            this.addSurfaceBiome(consumer, temperature, humidity, continentalness, erosion, weirdness, offset, replacement);
            ci.cancel();
        }
    }
}
