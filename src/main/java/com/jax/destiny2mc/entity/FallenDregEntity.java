package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenDregEntity extends FallenMobEntity {
   public FallenDregEntity(EntityType var1, Level var2) {
      super(var1, var2, 0, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(24.0, 0.3, 4.5, 32.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 30.0F, 30.0F);
         float var3 = this.distanceTo(var2);
         if (super.burstLeft > 0 && super.tickCount % 5 == 0) {
            super.burstLeft--;
            this.fireAimedProjectile(new ShockBoltEntity(var1, this), var2, 1.45F, 2.2F);
            ServerBallisticsEngine.playSound(var1, this.getX(), this.getEyeY(), this.getZ(), "entity.breeze.shoot", 0.7F, 1.75F);
         }

         if (!(var3 > 3.2F)) {
            if (super.attackCd <= 0) {
               this.setAnim(4, 12);
               super.attackCd = 22;
               this.doHurtTarget(var2);
               return;
            }
         } else {
            if (super.specialCd <= 0 && !(var3 < 7.0F) && !(var3 > 19.0F)) {
               this.setAnim(3, 16);
               super.specialCd = 110;
               this.fireAimedProjectile(new ShockGrenadeEntity(var1, this), var2, 0.95F, 1.5F);
               return;
            }

            if (super.attackCd <= 0 && this.hasLineOfSight(var2)) {
               this.setAnim(1, 16);
               super.burstLeft = 2;
               super.attackCd = 38;
               this.fireAimedProjectile(new ShockBoltEntity(var1, this), var2, 1.45F, 2.0F);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getEyeY(), this.getZ(), "entity.breeze.shoot", 0.7F, 1.75F);
            }
         }

         if (!(var3 <= 11.0F)) {
            this.getNavigation().moveTo(var2, 1.05);
         }
      }
   }
}
