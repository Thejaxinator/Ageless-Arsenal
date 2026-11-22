package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HiveOgreEntity extends CombatMobEntity {
   public int phaseInt;

   public HiveOgreEntity(EntityType var1, Level var2) {
      super(var1, var2, 26, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(135.0, 0.23, 5.0, 48.0);
   }
}
