package com.jax.destiny2mc.client;

import com.jax.destiny2mc.entity.FallenMobEntity;
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

public class DevilWalkerModel extends HierarchicalModel {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "devil_walker"), "main");
   private final ModelPart root;
   private final ModelPart chassis;
   private final ModelPart turret;
   private final ModelPart neckCore;
   private final ModelPart legFL;
   private final ModelPart legFR;
   private final ModelPart legML;
   private final ModelPart legMR;
   private final ModelPart legRL;
   private final ModelPart legRR;

   public DevilWalkerModel(ModelPart var1) {
      this.root = var1;
      ModelPart var2 = var1.getChild("chassis");
      this.chassis = var2;
      this.turret = var2.getChild("turret");
      this.neckCore = var2.getChild("neck_core");
      this.legFL = var2.getChild("leg_fl");
      this.legFR = var2.getChild("leg_fr");
      this.legML = var2.getChild("leg_ml");
      this.legMR = var2.getChild("leg_mr");
      this.legRL = var2.getChild("leg_rl");
      this.legRR = var2.getChild("leg_rr");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition var0 = new MeshDefinition();
      PartDefinition var1 = var0.getRoot();
      PartDefinition var2 = var1.addOrReplaceChild(
         "chassis", CubeListBuilder.create().texOffs(0, 16).addBox(-11.0F, -7.0F, -14.0F, 22.0F, 10.0F, 28.0F), PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "engine_pod", CubeListBuilder.create().texOffs(0, 32).addBox(-9.0F, -9.0F, 6.0F, 18.0F, 9.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "chin_gun", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 1.0F, -21.0F, 6.0F, 4.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "turret", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -13.0F, -24.0F, 8.0F, 6.0F, 30.0F), PartPose.offset(0.0F, 0.0F, 2.0F)
      );
      var2.addOrReplaceChild(
         "neck_core", CubeListBuilder.create().texOffs(40, 0).addBox(-5.0F, -5.0F, -16.0F, 10.0F, 7.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      var2.addOrReplaceChild(
         "leg_fl",
         CubeListBuilder.create().texOffs(8, 16).addBox(-5.0F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
         PartPose.offsetAndRotation(-11.0F, 1.0F, -10.0F, 0.0F, 0.0F, 0.42F)
      );
      var2.addOrReplaceChild(
         "leg_fr",
         CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
         PartPose.offsetAndRotation(11.0F, 1.0F, -10.0F, 0.0F, 0.0F, -0.42F)
      );
      var2.addOrReplaceChild(
         "leg_ml",
         CubeListBuilder.create().texOffs(8, 16).addBox(-5.0F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
         PartPose.offsetAndRotation(-12.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.48F)
      );
      var2.addOrReplaceChild(
         "leg_mr",
         CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
         PartPose.offsetAndRotation(12.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.48F)
      );
      var2.addOrReplaceChild(
         "leg_rl",
         CubeListBuilder.create().texOffs(8, 16).addBox(-5.0F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
         PartPose.offsetAndRotation(-11.0F, 1.0F, 10.0F, 0.0F, 0.0F, 0.42F)
      );
      var2.addOrReplaceChild(
         "leg_rr",
         CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
         PartPose.offsetAndRotation(11.0F, 1.0F, 10.0F, 0.0F, 0.0F, -0.42F)
      );
      return LayerDefinition.create(var0, 64, 64);
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      int var7 = ((FallenMobEntity)var1).getAnimState();
      if (var7 == 2) {
         this.chassis.y = 15.0F;
         this.chassis.xRot = 0.22F;
         this.neckCore.z = -6.5F;
      } else {
         this.chassis.y = 10.0F;
         this.chassis.xRot = 0.0F;
         this.neckCore.z = 2.0F;
         float var8 = (float)Math.cos((double)(var2 * 0.6F)) * var3 * 0.55F;
         this.legFL.xRot = var8 * 1.0F;
         this.legMR.xRot = var8 * 1.0F;
         this.legRL.xRot = var8 * 1.0F;
         this.legFR.xRot = var8 * -1.0F;
         this.legML.xRot = var8 * -1.0F;
         this.legRR.xRot = var8 * -1.0F;
         this.turret.yRot = var5 * 0.012F;
      }
   }
}
