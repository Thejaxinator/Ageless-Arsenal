package com.jax.destiny2mc.client;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SepiksServoModel extends HierarchicalModel {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "sepiks_prime"), "main");
   private final ModelPart root;
   private final ModelPart orb;
   private final ModelPart eyeCore;
   private final ModelPart irisN;
   private final ModelPart irisS;
   private final ModelPart irisE;
   private final ModelPart irisW;

   public SepiksServoModel(ModelPart var1) {
      this.root = var1;
      ModelPart var2 = var1.getChild("orb");
      this.orb = var2;
      this.eyeCore = var2.getChild("eye_core");
      this.irisN = var2.getChild("iris_n");
      this.irisS = var2.getChild("iris_s");
      this.irisE = var2.getChild("iris_e");
      this.irisW = var2.getChild("iris_w");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition var0 = new MeshDefinition();
      PartDefinition var1 = var0.getRoot();
      PartDefinition var2 = var1.addOrReplaceChild(
         "orb", CubeListBuilder.create().texOffs(0, 16).addBox(-10.0F, -10.0F, -10.0F, 20.0F, 20.0F, 20.0F), PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "crown_cap", CubeListBuilder.create().texOffs(0, 20).addBox(-8.0F, -13.0F, -8.0F, 16.0F, 3.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "base_cap", CubeListBuilder.create().texOffs(0, 20).addBox(-8.0F, 10.0F, -8.0F, 16.0F, 3.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "side_l", CubeListBuilder.create().texOffs(8, 24).addBox(-13.0F, -8.0F, -8.0F, 3.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "side_r", CubeListBuilder.create().texOffs(8, 24).addBox(10.0F, -8.0F, -8.0F, 3.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "eye_core", CubeListBuilder.create().texOffs(40, 0).addBox(-5.0F, -5.0F, -11.5F, 10.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "iris_n", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -12.2F, 12.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild("iris_s", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 5.0F, -12.2F, 12.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
      var2.addOrReplaceChild(
         "iris_e", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -5.0F, -12.2F, 3.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild("iris_w", CubeListBuilder.create().texOffs(0, 0).addBox(5.0F, -5.0F, -12.2F, 3.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(var0, 64, 64);
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = (float)Math.sin((double)(var4 * 0.08F)) * 2.2F;
      this.orb.y = 10.0F + var7;
      this.orb.yRot = var5 * 0.015F;
      this.orb.xRot = var6 * 0.012F;
   }
}
