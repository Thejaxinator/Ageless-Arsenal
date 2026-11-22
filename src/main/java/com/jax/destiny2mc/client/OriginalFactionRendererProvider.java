package com.jax.destiny2mc.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class OriginalFactionRendererProvider implements EntityRendererProvider {
   private final int kind;

   public OriginalFactionRendererProvider(int var1) {
      this.kind = var1;
   }

   public EntityRenderer create(Context var1) {
      int var2 = this.kind;
      if (var2 >= 42) {
         return new ThrownItemRenderer(var1, 1.15F, true);
      } else if (var2 == 16) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[0]), 0), 0.375F, "textures/entity/exploder_shank.png", 1.0F
         );
      } else if (var2 == 17) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[0]), 0), 0.375F, "textures/entity/tracer_shank.png", 1.0F
         );
      } else if (var2 == 18) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[0]), 0), 0.625F, "textures/entity/heavy_shank.png", 1.55F
         );
      } else if (var2 == 19) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[1]), 1), 0.675F, "textures/entity/devils_servitor.png", 1.25F
         );
      } else if (var2 == 20) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.525F, "textures/entity/archon_priest.png", 1.42F
         );
      } else if (var2 == 21) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.325F, "textures/entity/hive_thrall.png", 1.0F
         );
      } else if (var2 == 22) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.34F, "textures/entity/hive_cursed_thrall.png", 1.0F
         );
      } else if (var2 == 23) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.36F, "textures/entity/hive_acolyte.png", 1.0F
         );
      } else if (var2 == 24) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.45F, "textures/entity/hive_knight.png", 1.18F
         );
      } else if (var2 == 25) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.4F, "textures/entity/hive_wizard.png", 1.0F
         );
      } else if (var2 == 26) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[3]), 3), 0.825F, "textures/entity/hive_ogre.png", 1.68F
         );
      } else if (var2 == 27) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[1]), 1), 0.575F, "textures/entity/hive_shrieker.png", 1.0F
         );
      } else if (var2 == 28) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.475F, "textures/entity/lucent_knight.png", 1.25F
         );
      } else if (var2 == 29) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[1]), 1), 0.275F, "textures/entity/hive_ghost.png", 0.55F
         );
      } else if (var2 == 30) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[0]), 0), 0.25F, "textures/entity/lucent_moth.png", 0.55F
         );
      } else if (var2 == 31) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[4]), 4), 0.725F, "textures/entity/oryx_boss.png", 1.88F
         );
      } else if (var2 == 32) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.36F, "textures/entity/vex_goblin.png", 1.0F
         );
      } else if (var2 == 33) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.36F, "textures/entity/vex_hobgoblin.png", 1.0F
         );
      } else if (var2 == 34) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[1]), 1), 0.425F, "textures/entity/vex_harpy.png", 1.0F
         );
      } else if (var2 == 35) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.475F, "textures/entity/vex_minotaur.png", 1.24F
         );
      } else if (var2 == 36) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[6]), 6), 0.75F, "textures/entity/vex_hydra.png", 1.38F
         );
      } else if (var2 == 37) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[5]), 5), 0.575F, "textures/entity/vex_wyvern.png", 1.32F
         );
      } else if (var2 == 38) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[1]), 1), 0.475F, "textures/entity/vex_oracle.png", 0.85F
         );
      } else if (var2 == 39) {
         return new V200FactionRenderer(
            var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[5]), 5), 0.825F, "textures/entity/persys_boss.png", 1.85F
         );
      } else {
         return var2 == 40
            ? new V200FactionRenderer(
               var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.775F, "textures/entity/atheon_boss.png", 1.95F
            )
            : new V200FactionRenderer(
               var1, new V200FactionModel(var1.bakeLayer(V200FactionModel.RIG_LAYERS[2]), 2), 0.675F, "textures/entity/protheon_boss.png", 1.72F
            );
      }
   }
}
