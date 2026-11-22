package com.jax.destiny2mc.client;

import com.jax.destiny2mc.combat.CombatMoves;
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

public class FallenHumanoidModel extends HierarchicalModel {
   public static final ModelLayerLocation[] LAYERS;
   private final int unitKind;
   private final ModelPart root;
   private final ModelPart pelvis;
   private final ModelPart sash;
   private final ModelPart torso;
   private final ModelPart head;
   private final ModelPart rUpperArm;
   private final ModelPart rForearm;
   private final ModelPart lUpperArm;
   private final ModelPart lForearm;
   private final ModelPart rLowerArm;
   private final ModelPart rLowerForearm;
   private final ModelPart lLowerArm;
   private final ModelPart lLowerForearm;
   private final ModelPart rThigh;
   private final ModelPart rShin;
   private final ModelPart rFoot;
   private final ModelPart lThigh;
   private final ModelPart lShin;
   private final ModelPart lFoot;

   static {
      ModelLayerLocation[] var0 = LAYERS = new ModelLayerLocation[7];
      var0[0] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_dreg"), "main");
      var0[1] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_wretch"), "main");
      var0[2] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_vandal_sniper"), "main");
      var0[3] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_captain_rifle"), "main");
      var0[4] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_captain_scorch"), "main");
      var0[5] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_shank"), "main");
      var0[6] = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_bruiser"), "main");
   }

   public FallenHumanoidModel(ModelPart var1, int var2) {
      this.unitKind = var2;
      this.root = var1;
      ModelPart var3 = var1.getChild("pelvis");
      this.pelvis = var3;
      this.sash = var3.getChild("sash");
      ModelPart var4 = var3.getChild("torso");
      this.torso = var4;
      this.head = var4.getChild("head");
      ModelPart var5 = var4.getChild("r_upper_arm");
      this.rUpperArm = var5;
      this.rForearm = var5.getChild("r_forearm");
      ModelPart var6 = var4.getChild("l_upper_arm");
      this.lUpperArm = var6;
      this.lForearm = var6.getChild("l_forearm");
      ModelPart var7 = var4.getChild("r_lower_arm");
      this.rLowerArm = var7;
      this.rLowerForearm = var7.getChild("r_lower_forearm");
      ModelPart var8 = var4.getChild("l_lower_arm");
      this.lLowerArm = var8;
      this.lLowerForearm = var8.getChild("l_lower_forearm");
      ModelPart var9 = var3.getChild("r_thigh");
      this.rThigh = var9;
      ModelPart var10 = var9.getChild("r_shin");
      this.rShin = var10;
      this.rFoot = var10.getChild("r_foot");
      ModelPart var11 = var3.getChild("l_thigh");
      this.lThigh = var11;
      ModelPart var12 = var11.getChild("l_shin");
      this.lShin = var12;
      this.lFoot = var12.getChild("l_foot");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer(int var0) {
      MeshDefinition var1 = new MeshDefinition();
      PartDefinition var2 = var1.getRoot();
      PartDefinition var3 = var2.addOrReplaceChild(
         "pelvis", CubeListBuilder.create().texOffs(26, 14).addBox(-3.5F, -2.5F, -2.0F, 7.0F, 5.0F, 4.0F), PartPose.offset(0.0F, 11.2F, 0.4F)
      );
      if (var0 != 6 && var0 != 1) {
         var3.addOrReplaceChild(
            "sash",
            CubeListBuilder.create().texOffs(48, 16).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 11.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 1.5F, 1.8F, 0.16F, 0.0F, 0.0F)
         );
      } else {
         var3.addOrReplaceChild(
            "sash",
            CubeListBuilder.create().texOffs(48, 16).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 9.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 1.8F, -1.8F, -0.08F, 0.0F, 0.0F)
         );
      }

      PartDefinition var4 = var3.addOrReplaceChild(
         "torso",
         CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -7.0F, -2.5F, 8.0F, 7.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, -2.0F, -0.2F, 0.16F, 0.0F, 0.0F)
      );
      var4.addOrReplaceChild(
         "ether_pack", CubeListBuilder.create().texOffs(26, 23).addBox(-3.0F, -6.5F, 2.2F, 6.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      if (var0 == 6) {
         var4.addOrReplaceChild(
            "propulsion_core", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5F, -5.5F, -3.2F, 3.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      }

      PartDefinition var5 = var4.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.5F, 6.0F, 6.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, -6.8F, -0.8F, -0.1F, 0.0F, 0.0F)
      );
      if (var0 == 6) {
         var5.addOrReplaceChild(
            "t_horn_bar", CubeListBuilder.create().texOffs(26, 0).addBox(-7.0F, -6.4F, -2.2F, 14.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var5.addOrReplaceChild(
            "t_tip_r",
            CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(-6.8F, -6.0F, -1.2F, 0.0F, 0.0F, -0.45F)
         );
         var5.addOrReplaceChild(
            "t_tip_l",
            CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(6.8F, -6.0F, -1.2F, 0.0F, 0.0F, 0.45F)
         );
         var5.addOrReplaceChild(
            "cheek_hose",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-2.5F, -1.5F, -2.6F, 0.28F, 0.0F, -0.24F)
         );
      } else if (var0 >= 3) {
         var5.addOrReplaceChild(
            "capt_brow", CubeListBuilder.create().texOffs(26, 0).addBox(-4.5F, -6.8F, -3.0F, 9.0F, 2.0F, 3.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var5.addOrReplaceChild(
            "capt_horn_r",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -5.5F, -1.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-4.2F, -4.5F, -1.0F, -0.18F, 0.0F, -0.48F)
         );
         var5.addOrReplaceChild(
            "capt_horn_l",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -5.5F, -1.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(4.2F, -4.5F, -1.0F, -0.18F, 0.0F, 0.48F)
         );
      } else {
         var5.addOrReplaceChild(
            "crest_plume",
            CubeListBuilder.create().texOffs(26, 0).addBox(-1.5F, -8.2F, -1.5F, 3.0F, 3.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.22F, 0.0F, 0.0F)
         );
         var5.addOrReplaceChild(
            "cheek_fin_r",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(-3.4F, -3.2F, -2.2F, 0.0F, 0.0F, -0.32F)
         );
         var5.addOrReplaceChild(
            "cheek_fin_l",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(3.4F, -3.2F, -2.2F, 0.0F, 0.0F, 0.32F)
         );
      }

      PartDefinition var6 = var4.addOrReplaceChild(
         "r_upper_arm",
         CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(-4.8F, -5.8F, 0.0F, -0.18F, 0.0F, 0.14F)
      );
      var6.addOrReplaceChild(
         "r_pauldron",
         CubeListBuilder.create().texOffs(14, 28).addBox(-2.2F, -1.6F, -2.0F, 4.0F, 4.0F, 4.0F),
         PartPose.offsetAndRotation(-0.4F, -0.5F, 0.0F, 0.0F, 0.0F, -0.26F)
      );
      PartDefinition var7 = var6.addOrReplaceChild(
         "r_forearm",
         CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 4.8F, -0.2F, -0.48F, -0.08F, 0.0F)
      );
      PartDefinition var8 = var4.addOrReplaceChild(
         "l_upper_arm",
         CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(4.8F, -5.8F, 0.0F, -0.16F, 0.0F, -0.14F)
      );
      var8.addOrReplaceChild(
         "l_pauldron",
         CubeListBuilder.create().texOffs(14, 28).addBox(-1.8F, -1.6F, -2.0F, 4.0F, 4.0F, 4.0F),
         PartPose.offsetAndRotation(0.4F, -0.5F, 0.0F, 0.0F, 0.0F, 0.26F)
      );
      PartDefinition var9 = var8.addOrReplaceChild(
         "l_forearm",
         CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 4.8F, -0.2F, -0.44F, 0.08F, 0.0F)
      );
      if (var0 == 0) {
         var7.addOrReplaceChild(
            "wpn_main", CubeListBuilder.create().texOffs(0, 48).addBox(-1.0F, 4.0F, -7.0F, 2.0F, 3.0F, 7.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var9.addOrReplaceChild(
            "wpn_off", CubeListBuilder.create().texOffs(32, 48).addBox(-0.5F, 4.8F, -6.0F, 1.0F, 2.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else if (var0 == 1) {
         var7.addOrReplaceChild(
            "spear_shaft", CubeListBuilder.create().texOffs(32, 48).addBox(-0.5F, 4.5F, -13.0F, 1.0F, 1.0F, 21.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var7.addOrReplaceChild(
            "spear_prong1", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5F, 4.0F, -17.0F, 1.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var7.addOrReplaceChild(
            "spear_prong2", CubeListBuilder.create().texOffs(0, 48).addBox(0.5F, 4.0F, -17.0F, 1.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else if (var0 == 2) {
         var7.addOrReplaceChild(
            "wire_body", CubeListBuilder.create().texOffs(0, 48).addBox(-1.0F, 3.5F, -15.5F, 2.0F, 3.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var7.addOrReplaceChild(
            "wire_optic", CubeListBuilder.create().texOffs(32, 48).addBox(-0.5F, 1.8F, -8.0F, 1.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else if (var0 == 3) {
         var7.addOrReplaceChild(
            "shrap_body", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5F, 3.2F, -11.0F, 3.0F, 4.0F, 11.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else if (var0 == 4) {
         var7.addOrReplaceChild(
            "scorch_main", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 2.8F, -13.5F, 4.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var7.addOrReplaceChild(
            "scorch_drum", CubeListBuilder.create().texOffs(32, 48).addBox(-1.5F, 6.5F, -9.5F, 3.0F, 3.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else {
         var7.addOrReplaceChild(
            "claw_r", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5F, 5.2F, -3.8F, 3.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         var9.addOrReplaceChild(
            "claw_l", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5F, 5.2F, -3.8F, 3.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      }

      if (var0 < 2) {
         PartDefinition var10 = var4.addOrReplaceChild(
            "r_lower_arm",
            CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(-4.0F, -2.2F, 0.4F, 0.22F, 0.0F, 0.36F)
         );
         var10.addOrReplaceChild(
            "r_lower_forearm", CubeListBuilder.create().texOffs(0, 48).addBox(-0.5F, 2.8F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
         PartDefinition var11 = var4.addOrReplaceChild(
            "l_lower_arm",
            CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(4.0F, -2.2F, 0.4F, 0.22F, 0.0F, -0.36F)
         );
         var11.addOrReplaceChild(
            "l_lower_forearm", CubeListBuilder.create().texOffs(0, 48).addBox(-0.5F, 2.8F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
         );
      } else {
         PartDefinition var16 = var4.addOrReplaceChild(
            "r_lower_arm",
            CubeListBuilder.create().texOffs(0, 28).addBox(-1.2F, 0.0F, -1.2F, 2.0F, 5.0F, 2.0F),
            PartPose.offsetAndRotation(-4.2F, -2.4F, 0.4F, -0.1F, 0.0F, 0.34F)
         );
         var16.addOrReplaceChild(
            "r_lower_forearm",
            CubeListBuilder.create().texOffs(0, 38).addBox(-1.2F, 0.0F, -1.2F, 2.0F, 5.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.6F, -0.2F, -0.55F, -0.12F, 0.0F)
         );
         PartDefinition var17 = var4.addOrReplaceChild(
            "l_lower_arm",
            CubeListBuilder.create().texOffs(0, 28).addBox(-1.0F, 0.0F, -1.2F, 2.0F, 5.0F, 2.0F),
            PartPose.offsetAndRotation(4.2F, -2.4F, 0.4F, -0.1F, 0.0F, -0.34F)
         );
         var17.addOrReplaceChild(
            "l_lower_forearm",
            CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, 0.0F, -1.2F, 2.0F, 5.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.6F, -0.2F, -0.55F, 0.12F, 0.0F)
         );
      }

      PartDefinition var12 = var3.addOrReplaceChild(
         "r_thigh",
         CubeListBuilder.create().texOffs(32, 32).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(-2.3F, 1.2F, 0.0F, -0.38F, 0.06F, 0.04F)
      );
      PartDefinition var13 = var12.addOrReplaceChild(
         "r_shin",
         CubeListBuilder.create().texOffs(46, 32).addBox(-1.4F, 0.0F, -1.4F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 5.2F, -0.3F, 0.76F, 0.0F, 0.0F)
      );
      var13.addOrReplaceChild(
         "r_foot",
         CubeListBuilder.create().texOffs(46, 39).addBox(-1.5F, 0.0F, -3.2F, 3.0F, 2.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 5.4F, 0.3F, -0.38F, 0.0F, 0.0F)
      );
      PartDefinition var14 = var3.addOrReplaceChild(
         "l_thigh",
         CubeListBuilder.create().texOffs(32, 32).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(2.3F, 1.2F, 0.0F, -0.38F, -0.06F, -0.04F)
      );
      PartDefinition var15 = var14.addOrReplaceChild(
         "l_shin",
         CubeListBuilder.create().texOffs(46, 32).addBox(-1.6F, 0.0F, -1.4F, 3.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 5.2F, -0.3F, 0.76F, 0.0F, 0.0F)
      );
      var15.addOrReplaceChild(
         "l_foot",
         CubeListBuilder.create().texOffs(46, 39).addBox(-1.5F, 0.0F, -3.2F, 3.0F, 2.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 5.4F, 0.3F, -0.38F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(var1, 64, 64);
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      ModelPart var7 = this.pelvis;
      var7.x = 0.0F;
      var7.y = 11.2F;
      var7.z = 0.4F;
      var7.xRot = 0.0F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      var7 = this.torso;
      var7.x = 0.0F;
      var7.y = -2.0F;
      var7.z = -0.2F;
      var7.xRot = 0.16F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      var7 = this.head;
      var7.x = 0.0F;
      var7.y = -6.8F;
      var7.z = -0.8F;
      var7.xRot = -0.1F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      var7 = this.rUpperArm;
      var7.x = -4.8F;
      var7.y = -5.8F;
      var7.z = 0.0F;
      var7.xRot = -0.18F;
      var7.yRot = 0.0F;
      var7.zRot = 0.14F;
      var7 = this.rForearm;
      var7.x = 0.0F;
      var7.y = 4.8F;
      var7.z = -0.2F;
      var7.xRot = -0.48F;
      var7.yRot = -0.08F;
      var7.zRot = 0.0F;
      var7 = this.lUpperArm;
      var7.x = 4.8F;
      var7.y = -5.8F;
      var7.z = 0.0F;
      var7.xRot = -0.16F;
      var7.yRot = 0.0F;
      var7.zRot = -0.14F;
      var7 = this.lForearm;
      var7.x = 0.0F;
      var7.y = 4.8F;
      var7.z = -0.2F;
      var7.xRot = -0.44F;
      var7.yRot = 0.08F;
      var7.zRot = 0.0F;
      var7 = this.rLowerArm;
      var7.x = -4.2F;
      var7.y = -2.4F;
      var7.z = 0.4F;
      var7.xRot = -0.1F;
      var7.yRot = 0.0F;
      var7.zRot = 0.34F;
      var7 = this.rLowerForearm;
      var7.x = 0.0F;
      var7.y = 4.6F;
      var7.z = -0.2F;
      var7.xRot = -0.55F;
      var7.yRot = -0.12F;
      var7.zRot = 0.0F;
      var7 = this.lLowerArm;
      var7.x = 4.2F;
      var7.y = -2.4F;
      var7.z = 0.4F;
      var7.xRot = -0.1F;
      var7.yRot = 0.0F;
      var7.zRot = -0.34F;
      var7 = this.lLowerForearm;
      var7.x = 0.0F;
      var7.y = 4.6F;
      var7.z = -0.2F;
      var7.xRot = -0.55F;
      var7.yRot = 0.12F;
      var7.zRot = 0.0F;
      var7 = this.rThigh;
      var7.x = -2.3F;
      var7.y = 1.2F;
      var7.z = 0.0F;
      var7.xRot = -0.38F;
      var7.yRot = 0.06F;
      var7.zRot = 0.04F;
      var7 = this.rShin;
      var7.x = 0.0F;
      var7.y = 5.2F;
      var7.z = -0.3F;
      var7.xRot = 0.76F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      var7 = this.rFoot;
      var7.x = 0.0F;
      var7.y = 5.4F;
      var7.z = 0.3F;
      var7.xRot = -0.38F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      var7 = this.lThigh;
      var7.x = 2.3F;
      var7.y = 1.2F;
      var7.z = 0.0F;
      var7.xRot = -0.38F;
      var7.yRot = -0.06F;
      var7.zRot = -0.04F;
      var7 = this.lShin;
      var7.x = 0.0F;
      var7.y = 5.2F;
      var7.z = -0.3F;
      var7.xRot = 0.76F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      var7 = this.lFoot;
      var7.x = 0.0F;
      var7.y = 5.4F;
      var7.z = 0.3F;
      var7.xRot = -0.38F;
      var7.yRot = 0.0F;
      var7.zRot = 0.0F;
      this.head.yRot = var5 * (float) (Math.PI / 180.0);
      this.head.xRot = -0.1F + var6 * (float) (Math.PI / 180.0);
      float var8 = (float)Math.sin((double)(var4 * 0.14F));
      this.torso.xRot = 0.16F + var8 * 0.04F;
      this.sash.xRot = 0.12F + var3 * 0.45F + var8 * 0.06F;
      this.rLowerArm.zRot = 0.34F + var8 * 0.1F;
      this.lLowerArm.zRot = -0.34F - var8 * 0.1F;
      float var9 = (float)Math.cos((double)(var2 * 0.6662F)) * var3 * 1.15F;
      this.rThigh.xRot = -0.38F + var9;
      this.lThigh.xRot = -0.38F - var9;
      this.rShin.xRot = 0.76F - var9 * 0.45F;
      this.lShin.xRot = 0.76F + var9 * 0.45F;
      this.pelvis.y = 11.2F - Math.abs(var9) * 0.65F;
      this.rUpperArm.xRot = -0.18F - var9 * 0.55F;
      this.lUpperArm.xRot = -0.16F + var9 * 0.55F;
      if (var1 instanceof FallenMobEntity var10) {
         int var11 = CombatMoves.legacyState(var10.getAnimState());
         float var12 = (float)var10.getAnimTimer();
         int var13 = this.unitKind;
         if (var11 == 1) {
            if (var13 != 1 && var13 != 6) {
               float var32 = (float)Math.sin((double)(var4 * 1.3F)) * 0.1F;
               this.rUpperArm.xRot = -1.15F - var32;
               this.rForearm.xRot = -0.35F;
               this.lUpperArm.xRot = -0.95F;
               this.lUpperArm.yRot = 0.38F;
               this.rLowerArm.xRot = -0.85F;
               return;
            }

            this.torso.xRot = 0.42F;
            this.rUpperArm.xRot = -0.65F;
            this.lUpperArm.zRot = 0.45F;
            this.rLowerArm.xRot = -0.75F;
            this.lLowerArm.xRot = -0.75F;
            return;
         }

         if (var11 == 2) {
            this.pelvis.y = 13.5F;
            this.torso.xRot = 0.55F;
            this.rUpperArm.xRot = -1.45F;
            this.lUpperArm.xRot = -1.45F;
            this.rLowerArm.zRot = 0.75F;
            this.lLowerArm.zRot = -0.75F;
            return;
         }

         if (var11 == 3) {
            if (var13 == 1) {
               float var31 = var12 / 14.0F * (float) (Math.PI * 2);
               this.pelvis.yRot = var31;
               this.rUpperArm.xRot = -1.35F;
               this.rUpperArm.zRot = -0.75F;
               return;
            }

            this.lUpperArm.xRot = -2.35F;
            return;
         }

         if (var11 == 4) {
            float var14 = (float)Math.sin((double)(var12 * 0.85F));
            this.torso.xRot = 0.35F;
            this.torso.yRot = var14 * 0.35F;
            this.rUpperArm.xRot = -1.45F + var14 * 0.75F;
            this.lUpperArm.xRot = -1.45F - var14 * 0.75F;
            this.rLowerArm.xRot = -1.25F - var14 * 0.65F;
            this.lLowerArm.xRot = -1.25F + var14 * 0.65F;
            return;
         }

         if (var11 == 5) {
            if (!(var12 < 7.0F)) {
               this.rUpperArm.xRot = -2.45F;
               this.lUpperArm.xRot = -2.45F;
               this.rLowerArm.xRot = -1.95F;
               this.lLowerArm.xRot = -1.95F;
               return;
            }

            this.torso.xRot = 0.58F;
            this.rUpperArm.xRot = -0.65F;
            this.rUpperArm.yRot = -0.45F;
            this.lUpperArm.xRot = -0.65F;
            this.lUpperArm.yRot = 0.45F;
            return;
         }

         if (var11 == 6) {
            this.torso.xRot = -0.32F;
            this.head.xRot = -0.45F;
            this.rUpperArm.zRot = 0.75F;
            this.lUpperArm.zRot = -0.75F;
         }
      }
   }
}
