package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenBruiserEntity extends FallenMobEntity {
   public FallenBruiserEntity(EntityType var1, Level var2) {
      super(var1, var2, 6, 16.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(85.0, 0.39, 9.5, 40.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      LivingEntity var2 = this.getTarget();
      if (var2 != null && var2.isAlive()) {
         this.getLookControl().setLookAt(var2, 40.0F, 40.0F);
         float var3 = this.distanceTo(var2);
         this.getNavigation().moveTo(var2, 1.28);
         if (!(var3 < 4.2F)) {
            if (super.specialCd <= 0) {
               super.specialCd = 62;
               this.setAnim(2, 14);
               var1.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.1, this.getZ(), 30, 0.4, 0.8, 0.4, 0.15);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "entity.enderman.teleport", 1.0F, 1.45F);
               double var4 = Math.random() * 6.2831853;
               double var6 = var2.getX() + Math.cos(var4) * 1.85;
               double var8 = var2.getY();
               double var10 = var2.getZ() + Math.sin(var4) * 1.85;
               CombatSupport.safeEntityTeleport(this, var6, var8, var10);
               var1.sendParticles(ParticleTypes.REVERSE_PORTAL, var6, var8 + 1.1, var10, 28, 0.4, 0.7, 0.4, 0.1);
               return;
            }

            if (this.getAnimState() == 0) {
               this.setAnim(1, 6);
            }
         } else if (super.attackCd <= 0) {
            super.attackCd = 14;
            if (super.tickCount % 2 == 0) {
               this.setAnim(4, 12);
               this.doHurtTarget(var2);
               ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "entity.player.attack.sweep", 0.95F, 1.3F);
               return;
            }

            this.setAnim(5, 14);
            this.doHurtTarget(var2);
            var2.knockback(0.55, this.getX() - var2.getX(), this.getZ() - var2.getZ());
            var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, var2.getX(), var2.getY() + 1.0, var2.getZ(), 20, 0.4, 0.4, 0.4, 0.12);
            ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "entity.player.attack.crit", 1.0F, 0.85F);
            return;
         }
      }
   }
}
