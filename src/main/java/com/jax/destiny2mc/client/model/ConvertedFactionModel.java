package com.jax.destiny2mc.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public class ConvertedFactionModel extends HierarchicalModel {
   private final ModelPart root;
   private final boolean floating;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart r_arm;
   private final ModelPart l_arm;
   private final ModelPart r_forearm;
   private final ModelPart l_forearm;
   private final ModelPart r_leg;
   private final ModelPart l_leg;
   private final ModelPart r_shin;
   private final ModelPart l_shin;
   private final ModelPart extra;

   public ConvertedFactionModel(ModelPart var1, boolean var2) {
      this.root = var1;
      this.floating = var2;
      this.body = var1.getChild("body");
      this.head = this.body.getChild("head");
      this.r_arm = this.body.getChild("r_arm");
      this.l_arm = this.body.getChild("l_arm");
      this.r_forearm = this.r_arm.getChild("r_forearm");
      this.l_forearm = this.l_arm.getChild("l_forearm");
      this.r_leg = this.body.getChild("r_leg");
      this.l_leg = this.body.getChild("l_leg");
      this.r_shin = this.r_leg.getChild("r_shin");
      this.l_shin = this.l_leg.getChild("l_shin");
      this.extra = this.body.getChild("extra");
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      this.head.yRot = var5 * 0.0174533F;
      this.head.xRot = var6 * 0.0174533F;
      if (this.floating) {
         this.body.y = (float)Math.sin((double)(var4 * 0.08F)) * 0.5F + 23.0F;
      } else {
         float var7 = (float)Math.cos((double)(var2 * 0.6662F)) * var3 * 0.8F;
         this.r_leg.xRot = var7 * 1.0F;
         this.l_leg.xRot = var7 * -1.0F;
         this.r_arm.xRot = var7 * -0.38F;
         this.l_arm.xRot = var7 * 0.38F;
         this.r_forearm.xRot = var7 * -0.16F;
         this.l_forearm.xRot = var7 * 0.16F;
         this.r_shin.xRot = var7 * -0.35F;
         this.l_shin.xRot = var7 * 0.35F;
      }
   }
}
