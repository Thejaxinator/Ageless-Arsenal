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

public class VexOracleEntity extends FallenMobEntity {
   public int phaseInt;

   public VexOracleEntity(EntityType var1, Level var2) {
      super(var1, var2, 38, 0.0F);
      this.setNoGravity(true);
      this.phaseInt = 0;
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(28.0, 0.0, 0.0, 48.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      if (super.tickCount % 35 == 0) {
         ServerBallisticsEngine.playSound(var1, this.getX(), this.getY(), this.getZ(), "block.note_block.chime", 1.2F, 1.35F);
         var1.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5, this.getZ(), 8, 0.3, 0.3, 0.3, 0.03);
      }

      if (super.tickCount >= 220) {
         var1.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.8, this.getZ(), 18, 0.5, 0.5, 0.5, 0.08);
         AABB var10 = this.getBoundingBox().inflate(24.0);
         List var11 = var1.getEntities(this, var10);

         for (int var12 = 0; var12 < var11.size(); var12++) {
            Entity var13 = (Entity)var11.get(var12);
            if (var13 instanceof Player) {
               ((LivingEntity)var13).hurt(this.damageSources().mobAttack(this), 5.5F);
            }
         }

         this.discard();
      }
   }
}
