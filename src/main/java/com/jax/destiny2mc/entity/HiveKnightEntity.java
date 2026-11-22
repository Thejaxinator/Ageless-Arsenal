package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HiveKnightEntity extends CombatMobEntity {
   public int phaseInt;

   public HiveKnightEntity(EntityType var1, Level var2) {
      super(var1, var2, 24, 16.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(68.0, 0.24, 4.0, 48.0);
   }
}
