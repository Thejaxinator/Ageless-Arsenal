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

public class VanokArcliteModel extends HierarchicalModel {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "vanok_arclite"), "main");
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart torso;
   private final ModelPart armUR;
   private final ModelPart armUL;
   private final ModelPart legR;
   private final ModelPart legL;
   private final ModelPart arcHalo;

   public VanokArcliteModel(ModelPart var1) {
      this.root = var1;
      ModelPart var2 = var1.getChild("torso");
      this.torso = var2;
      this.head = var2.getChild("head");
      this.armUR = var2.getChild("arm_ur");
      this.armUL = var2.getChild("arm_ul");
      this.arcHalo = var2.getChild("arc_halo");
      this.legR = var1.getChild("leg_r");
      this.legL = var1.getChild("leg_l");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition var0 = new MeshDefinition();
      PartDefinition var1 = var0.getRoot();
      PartDefinition var2 = var1.addOrReplaceChild(
         "torso", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -11.0F, -3.0F, 10.0F, 12.0F, 6.0F), PartPose.offset(0.0F, 11.0F, 0.0F)
      );
      var2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F), PartPose.offset(0.0F, -11.0F, 0.0F));
      var2.addOrReplaceChild(
         "coil_l",
         CubeListBuilder.create().texOffs(40, 0).addBox(-9.0F, -21.0F, 2.0F, 3.0F, 13.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.22F)
      );
      var2.addOrReplaceChild(
         "coil_r",
         CubeListBuilder.create().texOffs(40, 0).addBox(6.0F, -21.0F, 2.0F, 3.0F, 13.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.22F)
      );
      var2.addOrReplaceChild(
         "arc_halo", CubeListBuilder.create().texOffs(40, 0).addBox(-7.0F, -23.0F, 3.0F, 14.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "cloak",
         CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -10.0F, 3.0F, 11.0F, 20.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.12F, 0.0F, 0.0F)
      );
      PartDefinition var3 = var2.addOrReplaceChild(
         "arm_ur",
         CubeListBuilder.create().texOffs(8, 20).addBox(-3.5F, -1.0F, -2.0F, 3.5F, 11.0F, 4.0F),
         PartPose.offsetAndRotation(-5.0F, -9.5F, 0.0F, -1.15F, -0.15F, 0.1F)
      );
      var3.addOrReplaceChild(
         "wire_rifle", CubeListBuilder.create().texOffs(40, 0).addBox(-2.5F, 7.0F, -18.0F, 2.5F, 3.0F, 22.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "arm_ul",
         CubeListBuilder.create().texOffs(8, 20).addBox(0.0F, -1.0F, -2.0F, 3.5F, 11.0F, 4.0F),
         PartPose.offsetAndRotation(5.0F, -9.5F, 0.0F, -0.85F, 0.25F, -0.1F)
      );
      var2.addOrReplaceChild(
         "arm_lr",
         CubeListBuilder.create().texOffs(8, 24).addBox(-3.0F, 0.0F, -1.5F, 2.5F, 9.0F, 3.0F),
         PartPose.offsetAndRotation(-4.5F, -4.0F, 0.0F, -0.35F, 0.0F, 0.25F)
      );
      var2.addOrReplaceChild(
         "arm_ll",
         CubeListBuilder.create().texOffs(8, 24).addBox(0.5F, 0.0F, -1.5F, 2.5F, 9.0F, 3.0F),
         PartPose.offsetAndRotation(4.5F, -4.0F, 0.0F, -0.35F, 0.0F, -0.25F)
      );
      var1.addOrReplaceChild(
         "leg_r", CubeListBuilder.create().texOffs(16, 20).addBox(-2.2F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F), PartPose.offset(-2.6F, 11.0F, 0.0F)
      );
      var1.addOrReplaceChild(
         "leg_l", CubeListBuilder.create().texOffs(16, 20).addBox(-1.8F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F), PartPose.offset(2.6F, 11.0F, 0.0F)
      );
      return LayerDefinition.create(var0, 64, 64);
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      this.head.yRot = var5 * 0.015F;
      this.head.xRot = var6 * 0.014F;
      float var7 = (float)Math.cos((double)(var2 * 0.66F)) * var3 * 0.75F;
      this.legR.xRot = var7;
      this.legL.xRot = -var7;
   }
}
