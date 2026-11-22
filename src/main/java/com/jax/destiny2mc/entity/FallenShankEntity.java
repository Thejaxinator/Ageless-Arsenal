package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenShankEntity extends FallenMobEntity {
   public FallenShankEntity(EntityType var1, Level var2) {
      super(var1, var2, 5, 0.0F);
      this.setNoGravity(true);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(20.0, 0.33, 3.5, 34.0);
   }

   public boolean causeFallDamage(float var1, float var2, DamageSource var3) {
      return false;
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      this.setNoGravity(true);
      LivingEntity var2 = this.getTarget();
      if (var2 == null) {
         double var19 = Math.sin((double)super.tickCount * 0.15) * 0.025;
         this.setDeltaMovement(0.0, var19, 0.0);
      } else {
         if (var2.isAlive()) {
            this.getLookControl().setLookAt(var2, 35.0F, 35.0F);
            double var3 = var2.getX() - this.getX();
            double var5 = var2.getY() + 2.35 - this.getY();
            double var7 = var2.getZ() - this.getZ();
            double var9 = Math.sqrt(var3 * var3 + var7 * var7) + 0.001;
            double var11 = (var9 - 4.0) * 0.035;
            double var13 = var3 / var9 * var11 + -var7 / var9 * 0.11;
            double var15 = var7 / var9 * var11 + var3 / var9 * 0.11;
            double var17 = var5 * 0.08;
            this.setDeltaMovement(var13, var17, var15);
            if (super.attackCd <= 0 && this.hasLineOfSight(var2)) {
               this.setAnim(1, 10);
               super.attackCd = 22;
               this.fireAimedProjectile(new ShockBoltEntity(var1, this), var2, 1.55F, 1.8F);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "entity.breeze.shoot", 0.65F, 1.95F);
            }
         }
      }
   }
}
