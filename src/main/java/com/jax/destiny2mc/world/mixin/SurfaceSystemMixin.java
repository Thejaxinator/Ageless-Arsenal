package com.jax.destiny2mc.world.mixin;

import com.jax.destiny2mc.world.FactionSurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Prepends custom surface rules for the four Destiny faction biomes before vanilla surface evaluation.
 */
@Mixin(value = SurfaceSystem.class, remap = false)
public abstract class SurfaceSystemMixin {
    @ModifyVariable(method = "buildSurface", at = @At("HEAD"), argsOnly = true)
    private SurfaceRules.RuleSource ageless$prependFactionSurfaceRules(SurfaceRules.RuleSource original) {
        return FactionSurfaceRules.wrap(original);
    }
}
