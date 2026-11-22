package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenVandalSniperEntity extends FallenMobEntity {
   public FallenVandalSniperEntity(EntityType var1, Level var2) {
      super(var1, var2, 2, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(34.0, 0.29, 5.0, 44.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 30.0F, 30.0F);
         float var3 = this.distanceTo(var2);
         if (this.getAnimState() == 1) {
            this.getNavigation().stop();
            int var4 = this.getAnimTimer();
            if (var4 % 2 == 0) {
               for (int var5 = 1; var5 <= 5; var5++) {
                  double var6 = (double)var5 / 6.0;
                  double var8 = this.getX() + (var2.getX() - this.getX()) * var6;
                  double var10 = this.getEyeY() + (var2.getEyeY() - 0.2 - this.getEyeY()) * var6;
                  double var12 = this.getZ() + (var2.getZ() - this.getZ()) * var6;
                  var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, var8, var10, var12, 1, 0.0, 0.0, 0.0, 0.0);
               }
            }

            if (var4 <= 2) {
               this.setAnim(0, 0);
               super.attackCd = 52;
               this.fireAimedProjectile(new WireRifleBoltEntity(var1, this), var2, 2.95F, 0.45F);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getEyeY(), this.getZ(), "entity.warden.sonic_boom", 0.55F, 1.95F);
               return;
            }
         } else {
            if (!(var3 > 2.8F) && super.dodgeCd <= 0) {
               this.setAnim(2, 14);
               super.dodgeCd = 75;
               this.blinkTeleport(var1, 2.8, ParticleTypes.CLOUD);
               return;
            }

            if (super.attackCd <= 0 && this.hasLineOfSight(var2)) {
               this.setAnim(1, 20);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getEyeY(), this.getZ(), "block.beacon.activate", 0.75F, 1.9F);
               return;
            }

            if (!(var3 <= 18.0F)) {
               this.getNavigation().moveTo(var2, 1.0);
            }
         }
      }
   }
}
