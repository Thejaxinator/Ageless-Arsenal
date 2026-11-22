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

public class V200FactionModel extends HierarchicalModel {
   public static final ModelLayerLocation[] RIG_LAYERS;
   private final int rigType;
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart rArm;
   private final ModelPart lArm;
   private final ModelPart rLeg;
   private final ModelPart lLeg;
   private final ModelPart extra;

   static {
      ModelLayerLocation[] var0 = RIG_LAYERS = new ModelLayerLocation[7];
      var0[0] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_0"), "main");
      var0[1] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_1"), "main");
      var0[2] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_2"), "main");
      var0[3] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_3"), "main");
      var0[4] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_4"), "main");
      var0[5] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_5"), "main");
      var0[6] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "v200_rig_6"), "main");
   }

   public V200FactionModel(ModelPart var1, int var2) {
      this.root = var1;
      this.rigType = var2;
      ModelPart var3 = var1.getChild("body");
      this.body = var3;
      this.head = var3.getChild("head");
      this.rArm = var3.getChild("r_arm");
      this.lArm = var3.getChild("l_arm");
      this.rLeg = var3.getChild("r_leg");
      this.lLeg = var3.getChild("l_leg");
      this.extra = var3.getChild("extra");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer(int var0) {
      MeshDefinition var1 = new MeshDefinition();
      PartDefinition var2 = var1.getRoot();
      if (var0 == 0) {
         PartDefinition var3 = var2.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -3.0F, -5.0F, 9.0F, 6.0F, 10.0F), PartPose.offset(0.0F, 16.0F, 0.0F)
         );
         var3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -7.5F, 6.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
         var3.addOrReplaceChild(
            "r_arm",
            CubeListBuilder.create().texOffs(0, 28).addBox(-3.5F, -1.0F, -3.0F, 3.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.25F)
         );
         var3.addOrReplaceChild(
            "l_arm",
            CubeListBuilder.create().texOffs(0, 28).addBox(0.5F, -1.0F, -3.0F, 3.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.25F)
         );
         var3.addOrReplaceChild(
            "r_leg", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, 0.0F, -4.0F, 2.0F, 3.0F, 6.0F), PartPose.offset(-2.0F, 3.0F, -1.0F)
         );
         var3.addOrReplaceChild(
            "l_leg", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, 0.0F, -4.0F, 2.0F, 3.0F, 6.0F), PartPose.offset(2.0F, 3.0F, -1.0F)
         );
         var3.addOrReplaceChild(
            "extra", CubeListBuilder.create().texOffs(32, 48).addBox(-2.5F, -2.0F, -5.5F, 5.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else if (var0 == 1) {
         PartDefinition var6 = var2.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 14).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F), PartPose.offset(0.0F, 14.0F, 0.0F)
         );
         var6.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(32, 48).addBox(-3.5F, -3.5F, -6.6F, 7.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var6.addOrReplaceChild(
            "r_arm", CubeListBuilder.create().texOffs(26, 0).addBox(-2.0F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F), PartPose.offset(-6.5F, 0.0F, 0.0F)
         );
         var6.addOrReplaceChild(
            "l_arm", CubeListBuilder.create().texOffs(26, 0).addBox(0.0F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F), PartPose.offset(6.5F, 0.0F, 0.0F)
         );
         var6.addOrReplaceChild(
            "r_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, -2.0F, -2.0F, 10.0F, 2.0F, 4.0F), PartPose.offset(0.0F, -6.5F, 0.0F)
         );
         var6.addOrReplaceChild(
            "l_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, 0.0F, -2.0F, 10.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 6.5F, 0.0F)
         );
         var6.addOrReplaceChild(
            "extra", CubeListBuilder.create().texOffs(48, 0).addBox(-3.0F, -3.0F, 5.5F, 6.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else if (var0 == 5) {
         PartDefinition var7 = var2.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 14).addBox(-5.5F, -7.0F, -4.5F, 11.0F, 11.0F, 9.0F),
            PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.22F, 0.0F, 0.0F)
         );
         var7.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.8F, 8.0F, 7.0F, 4.0F), PartPose.offset(0.0F, -1.5F, 0.0F)
         );
         var7.addOrReplaceChild(
            "r_arm",
            CubeListBuilder.create().texOffs(48, 14).addBox(-9.0F, -9.0F, -1.5F, 9.0F, 14.0F, 2.0F),
            PartPose.offsetAndRotation(-5.5F, -2.0F, 1.0F, 0.0F, 0.25F, 0.18F)
         );
         var7.addOrReplaceChild(
            "l_arm",
            CubeListBuilder.create().texOffs(48, 14).addBox(0.0F, -9.0F, -1.5F, 9.0F, 14.0F, 2.0F),
            PartPose.offsetAndRotation(5.5F, -2.0F, 1.0F, 0.0F, -0.25F, -0.18F)
         );
         var7.addOrReplaceChild(
            "r_leg", CubeListBuilder.create().texOffs(28, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F), PartPose.offset(-4.2F, 3.5F, 0.5F)
         );
         var7.addOrReplaceChild(
            "l_leg", CubeListBuilder.create().texOffs(28, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F), PartPose.offset(4.2F, 3.5F, 0.5F)
         );
         var7.addOrReplaceChild(
            "extra", CubeListBuilder.create().texOffs(32, 48).addBox(-2.5F, -2.5F, -5.2F, 5.0F, 5.0F, 2.0F), PartPose.offset(0.0F, 0.5F, 0.0F)
         );
      } else if (var0 == 6) {
         PartDefinition var8 = var2.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 14).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 15.0F, 10.0F), PartPose.offset(0.0F, 11.0F, 0.0F)
         );
         var8.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -6.0F, -4.5F, 9.0F, 6.0F, 9.0F), PartPose.offset(0.0F, -8.0F, 0.0F)
         );
         var8.addOrReplaceChild(
            "r_arm", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, -3.0F, -9.0F, 3.0F, 4.0F, 8.0F), PartPose.offset(-6.0F, -2.0F, 0.0F)
         );
         var8.addOrReplaceChild(
            "l_arm", CubeListBuilder.create().texOffs(0, 48).addBox(-1.0F, -3.0F, -9.0F, 3.0F, 4.0F, 8.0F), PartPose.offset(6.0F, -2.0F, 0.0F)
         );
         var8.addOrReplaceChild(
            "r_leg", CubeListBuilder.create().texOffs(26, 14).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
         );
         var8.addOrReplaceChild(
            "l_leg", CubeListBuilder.create().texOffs(26, 14).addBox(-2.0F, 5.0F, -2.0F, 4.0F, 5.0F, 4.0F), PartPose.offset(0.0F, 9.0F, 0.0F)
         );
         var8.addOrReplaceChild(
            "extra", CubeListBuilder.create().texOffs(32, 48).addBox(-11.0F, -6.0F, -11.0F, 22.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else {
         PartDefinition var9 = var2.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -12.0F, -2.8F, 9.0F, 12.0F, 5.6F), PartPose.offset(0.0F, 12.0F, 0.0F)
         );
         PartDefinition var4 = var9.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F), PartPose.offset(0.0F, -12.0F, 0.0F)
         );
         var4.addOrReplaceChild(
            "crest", CubeListBuilder.create().texOffs(26, 0).addBox(-6.5F, -10.5F, -1.5F, 13.0F, 5.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         PartDefinition var5 = var9.addOrReplaceChild(
            "r_arm",
            CubeListBuilder.create().texOffs(0, 28).addBox(-3.5F, -2.0F, -2.0F, 3.5F, 12.0F, 4.0F),
            PartPose.offsetAndRotation(-5.0F, -10.5F, 0.0F, -0.35F, 0.0F, 0.0F)
         );
         var5.addOrReplaceChild(
            "r_wpn", CubeListBuilder.create().texOffs(0, 48).addBox(-2.5F, 7.5F, -12.0F, 2.5F, 3.5F, 13.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var9.addOrReplaceChild(
            "l_arm",
            CubeListBuilder.create().texOffs(14, 28).addBox(0.0F, -2.0F, -2.0F, 3.5F, 12.0F, 4.0F),
            PartPose.offsetAndRotation(5.0F, -10.5F, 0.0F, -0.2F, 0.0F, 0.0F)
         );
         var9.addOrReplaceChild(
            "r_leg", CubeListBuilder.create().texOffs(28, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.4F, 0.0F, 0.0F)
         );
         var9.addOrReplaceChild(
            "l_leg", CubeListBuilder.create().texOffs(28, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.4F, 0.0F, 0.0F)
         );
         if (var0 == 4) {
            var9.addOrReplaceChild(
               "extra",
               CubeListBuilder.create().texOffs(48, 14).addBox(-15.0F, -16.0F, 2.8F, 30.0F, 16.0F, 1.5F),
               PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.15F, 0.0F, 0.0F)
            );
         } else {
            var9.addOrReplaceChild(
               "extra", CubeListBuilder.create().texOffs(32, 48).addBox(-2.5F, -9.5F, -3.2F, 5.0F, 5.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
            );
         }
      }

      return LayerDefinition.create(var1, 64, 64);
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      this.head.yRot = var5 * (float) (Math.PI / 180.0);
      this.head.xRot = var6 * (float) (Math.PI / 180.0);
      float var7 = (float)Math.cos((double)(var2 * 0.6662F)) * var3;
      this.rLeg.xRot = var7;
      this.lLeg.xRot = -var7;
      this.rArm.xRot = -0.45F - var7 * 0.4F;
      this.lArm.xRot = -0.25F + var7 * 0.4F;
      if (this.rigType == 6 || this.rigType == 1) {
         this.extra.yRot = var4 * 0.085F;
      }
   }
}
