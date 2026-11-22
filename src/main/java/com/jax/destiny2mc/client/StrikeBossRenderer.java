package com.jax.destiny2mc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class StrikeBossRenderer extends MobRenderer {
   public static final ResourceLocation[] TEX;
   private final int bossKind;

   static {
      ResourceLocation[] var0 = TEX = new ResourceLocation[4];
      var0[0] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/devil_walker.png");
      var0[1] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/sepiks_prime.png");
      var0[2] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/blast_servitor.png");
      var0[3] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/vanok_arclite.png");
   }

   public StrikeBossRenderer(Context var1, EntityModel var2, float var3, int var4) {
      super(var1, var2, var3);
      this.bossKind = var4;
   }

   public ResourceLocation getTextureLocation(Entity var1) {
      return TEX[this.bossKind];
   }

   protected void scale(LivingEntity var1, PoseStack var2, float var3) {
      int var4 = this.bossKind;
      float var5;
      if (var4 == 0) {
         var5 = 1.45F;
      } else if (var4 == 1) {
         var5 = 1.4F;
      } else if (var4 == 2) {
         var5 = 0.82F;
      } else {
         var5 = 1.25F;
      }

      var2.scale(var5, var5, var5);
   }
}
