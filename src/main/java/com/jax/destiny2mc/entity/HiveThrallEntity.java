package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HiveThrallEntity extends CombatMobEntity {
   public int phaseInt;

   public HiveThrallEntity(EntityType var1, Level var2) {
      super(var1, var2, 21, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(18.0, 0.36, 2.4, 48.0);
   }
}
