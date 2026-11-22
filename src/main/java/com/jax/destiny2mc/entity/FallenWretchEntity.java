package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenWretchEntity extends FallenMobEntity {
   public FallenWretchEntity(EntityType var1, Level var2) {
      super(var1, var2, 1, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(26.0, 0.36, 6.0, 32.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 35.0F, 35.0F);
         float var3 = this.distanceTo(var2);
         this.getNavigation().moveTo(var2, 1.22);
         if (!(var3 < 3.5F)) {
            if (!(var3 > 3.2F) && super.specialCd <= 0) {
               this.setAnim(2, 14);
               super.specialCd = 55;
               double var4 = (var2.getX() - this.getX()) / (double)var3 * 0.68;
               double var6 = (var2.getZ() - this.getZ()) / (double)var3 * 0.68;
               this.setDeltaMovement(var4, 0.28, var6);
               var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0, this.getZ(), 14, 0.25, 0.25, 0.25, 0.08);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "entity.player.attack.sweep", 0.85F, 1.45F);
               return;
            }

            if (this.getAnimState() == 0) {
               this.setAnim(1, 6);
            }
         } else if (super.attackCd <= 0) {
            super.attackCd = 18;
            if (Math.random() <= 0.45) {
               this.setAnim(3, 14);
            } else {
               this.setAnim(4, 11);
            }

            this.doHurtTarget(var2);
            var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, var2.getX(), var2.getY() + 1.0, var2.getZ(), 16, 0.3, 0.3, 0.3, 0.09);
            return;
         }
      }
   }
}
