package com.jax.destiny2mc.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class BlastServitorEntity extends FallenMobEntity {
   public BlastServitorEntity(EntityType var1, Level var2) {
      super(var1, var2, 12, 24.0F);
      this.setNoGravity(true);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(110.0, 0.26, 6.5, 48.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      if (super.tickCount % 6 == 0) {
         var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.8, this.getZ(), 4, 0.6, 0.6, 0.6, 0.05);
      }

      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 30.0F, 30.0F);
         if (super.dodgeCd <= 0) {
            super.dodgeCd = 95;
            this.blinkTeleport(var1, 3.5, ParticleTypes.ELECTRIC_SPARK);
         }

         if (super.attackCd <= 0) {
            super.attackCd = 36;
            this.fireAimedProjectile(new ShockBoltEntity(var1, this), var2, 1.5F, 1.2F);
         }
      }
   }
}
