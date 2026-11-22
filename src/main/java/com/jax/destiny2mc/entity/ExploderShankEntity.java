package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ExploderShankEntity extends FallenMobEntity {
   public int phaseInt;

   public ExploderShankEntity(EntityType var1, Level var2) {
      super(var1, var2, 16, 0.0F);
      this.setNoGravity(true);
      this.phaseInt = 0;
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(16.0, 0.34, 2.5, 48.0);
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
         }

         this.setDeltaMovement(var3, var5, var7);
         if (this.distanceTo(var2) <= 2.5F) {
            var1.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.8, this.getZ(), 18, 0.5, 0.5, 0.5, 0.08);
            AABB var10 = this.getBoundingBox().inflate(3.4);
            List var11 = var1.getEntities(this, var10);

            for (int var12 = 0; var12 < var11.size(); var12++) {
               Entity var13 = (Entity)var11.get(var12);
               if (var13 instanceof Player) {
                  ((LivingEntity)var13).hurt(this.damageSources().mobAttack(this), 4.8F);
               }
            }

            ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "entity.generic.explode", 0.95F, 1.25F);
            this.discard();
         }
      }
   }
}
