package com.jax.destiny2mc.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class ArchonPriestEntity extends FallenMobEntity {
   public int phaseInt = 0;

   public ArchonPriestEntity(EntityType var1, Level var2) {
      super(var1, var2, 20, 22.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(145.0, 0.27, 4.5, 48.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 30.0F, 30.0F);
         if (super.attackCd <= 0) {
            super.attackCd = 34;
            this.setAnim(1, 10);
            this.fireAimedProjectile(new ShrapnelPelletEntity(var1, this), var2, 1.6F, 1.6F);
         }

         if (super.specialCd <= 0) {
            super.specialCd = 100;
         }
      }
   }
}
