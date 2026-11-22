package com.jax.destiny2mc.client;

import com.jax.destiny2mc.combat.CombatMoves;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class FallenMobRenderer extends MobRenderer {
   private static final ResourceLocation[] TEXS;
   private final int unitKind;

   static {
      ResourceLocation[] var0 = TEXS = new ResourceLocation[7];
      var0[0] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_dreg.png");
      var0[1] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_wretch.png");
      var0[2] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_vandal_sniper.png");
      var0[3] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_captain_rifle.png");
      var0[4] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_captain_scorch.png");
      var0[5] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_shank.png");
      var0[6] = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/fallen_bruiser.png");
   }

   private static EntityModel bakeModelForKind(Context var0, int var1) {
      ModelPart var2 = var0.bakeLayer(FallenHumanoidModel.LAYERS[var1]);
      return (EntityModel)(var1 == 5 ? new FallenShankModel(var2) : new FallenHumanoidModel(var2, var1));
   }

   public FallenMobRenderer(Context var1, int var2) {
      super(var1, bakeModelForKind(var1, var2), 0.5F);
      this.unitKind = var2;
   }

   public ResourceLocation getTextureLocation(Entity var1) {
      return TEXS[this.unitKind];
   }

   protected void scale(LivingEntity var1, PoseStack var2, float var3) {
      int var4 = this.unitKind;
      float var5 = 0.98F;
      if (var4 == 2) {
         var5 = 1.06F;
      } else if (var4 == 3 || var4 == 4) {
         var5 = 1.24F;
      } else if (var4 == 6) {
         var5 = 1.16F;
      }

      if (((FallenMobEntity)var1).getAnimState() == 12) {
         var5 *= CombatMoves.blinkScale(20.0F - (float)((FallenMobEntity)var1).getAnimTimer() + var3);
      }

      var2.scale(var5, var5, var5);
   }
}
