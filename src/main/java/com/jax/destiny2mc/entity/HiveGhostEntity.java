package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatSupport;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HiveGhostEntity extends FallenMobEntity {
   public int phaseInt;

   public HiveGhostEntity(EntityType var1, Level var2) {
      super(var1, var2, 29, 0.0F);
      this.setNoGravity(true);
      this.phaseInt = 0;
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(12.0, 0.0, 0.0, 48.0);
   }

   @Override
   protected void tickFallenCombat(ServerLevel var1) {
      if (super.tickCount % 10 == 0) {
         var1.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.3, this.getZ(), 4, 0.15, 0.15, 0.15, 0.02);
      }

      if (super.tickCount >= 140) {
         CombatSupport.completeHiveGhostRevive(var1, this);
         this.discard();
      }
   }
}
