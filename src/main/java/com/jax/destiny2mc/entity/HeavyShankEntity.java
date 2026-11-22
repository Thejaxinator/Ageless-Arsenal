package com.jax.destiny2mc.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HeavyShankEntity extends FallenMobEntity {
   public int phaseInt;

   public HeavyShankEntity(EntityType var1, Level var2) {
      super(var1, var2, 18, 12.0F);
      this.setNoGravity(true);
      this.phaseInt = 0;
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(56.0, 0.22, 3.0, 48.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 30.0F, 30.0F);
         double var3 = (var2.getX() - this.getX()) * 0.035;
         double var5 = (var2.getY() + 1.3 - this.getY()) * 0.05;
         double var7 = (var2.getZ() - this.getZ()) * 0.035;
         if (this.distanceTo(var2) <= 8.0F) {
            var3 = 0.0;
            var7 = 0.0;
         }

         this.setDeltaMovement(var3, var5, var7);
         if (super.attackCd <= 0) {
            super.attackCd = 24;
            this.setAnim(1, 10);
            this.fireAimedProjectile(new ShockBoltEntity(var1, this), var2, 1.7F, 1.6F);
         }

         if (super.specialCd <= 0) {
            super.specialCd = 100;
         }
      }
   }
}
