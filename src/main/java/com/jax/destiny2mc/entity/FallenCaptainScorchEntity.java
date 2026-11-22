package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenCaptainScorchEntity extends CombatMobEntity {
   public int phaseInt;

   public FallenCaptainScorchEntity(EntityType var1, Level var2) {
      super(var1, var2, 4, 28.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(75.0, 0.29, 4.5, 48.0);
   }
}
