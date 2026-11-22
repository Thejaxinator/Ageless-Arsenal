package com.jax.destiny2mc.client;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class FallenShankModel extends HierarchicalModel {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart lPod;
   private final ModelPart rPod;
   private final ModelPart turret;

   public FallenShankModel(ModelPart var1) {
      this.root = var1;
      ModelPart var2 = var1.getChild("body");
      this.body = var2;
      this.lPod = var2.getChild("l_pod");
      this.rPod = var2.getChild("r_pod");
      this.turret = var2.getChild("turret");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition var0 = new MeshDefinition();
      PartDefinition var1 = var0.getRoot();
      PartDefinition var2 = var1.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 6.0F, 10.0F), PartPose.offset(0.0F, 15.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "nose",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.22F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "l_pod",
         CubeListBuilder.create().texOffs(0, 20).addBox(0.0F, -1.5F, -3.0F, 5.0F, 3.0F, 6.0F),
         PartPose.offsetAndRotation(4.6F, -2.2F, 1.5F, 0.14F, 0.0F, -0.18F)
      );
      var2.addOrReplaceChild(
         "r_pod",
         CubeListBuilder.create().texOffs(0, 20).addBox(-5.0F, -1.5F, -3.0F, 5.0F, 3.0F, 6.0F),
         PartPose.offsetAndRotation(-4.6F, -2.2F, 1.5F, 0.14F, 0.0F, 0.18F)
      );
      var2.addOrReplaceChild(
         "l_fin",
         CubeListBuilder.create().texOffs(28, 20).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 7.0F),
         PartPose.offsetAndRotation(2.8F, 1.2F, 3.8F, -0.42F, 0.18F, 0.46F)
      );
      var2.addOrReplaceChild(
         "r_fin",
         CubeListBuilder.create().texOffs(28, 20).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 7.0F),
         PartPose.offsetAndRotation(-2.8F, 1.2F, 3.8F, -0.42F, -0.18F, -0.46F)
      );
      var2.addOrReplaceChild("turret", CubeListBuilder.create().texOffs(0, 36).addBox(-1.5F, 0.0F, -6.5F, 3.0F, 3.0F, 9.0F), PartPose.offset(0.0F, 2.8F, -0.5F));
      return LayerDefinition.create(var0, 64, 64);
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = (float)Math.sin((double)(var4 * 0.18F));
      this.body.y = 15.0F + var7 * 1.1F;
      this.body.xRot = var3 * 0.35F;
      this.body.zRot = var7 * 0.08F;
      this.lPod.xRot = 0.14F + var7 * 0.12F;
      this.rPod.xRot = 0.14F - var7 * 0.12F;
      this.turret.yRot = var5 * 0.012F;
      this.turret.xRot = var6 * 0.015F;
   }
}
