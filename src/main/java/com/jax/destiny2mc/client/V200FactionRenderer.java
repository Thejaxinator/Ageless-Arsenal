package com.jax.destiny2mc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class V200FactionRenderer extends MobRenderer {
   private final ResourceLocation tex;
   private final float scaleVal;

   public V200FactionRenderer(Context var1, EntityModel var2, float var3, String var4, float var5) {
      super(var1, var2, var3);
      this.tex = ResourceLocation.fromNamespaceAndPath("destiny2mc", var4);
      this.scaleVal = var5;
   }

   public ResourceLocation getTextureLocation(Entity var1) {
      return this.tex;
   }

   protected void scale(LivingEntity var1, PoseStack var2, float var3) {
      var2.scale(this.scaleVal, this.scaleVal, this.scaleVal);
   }
}
