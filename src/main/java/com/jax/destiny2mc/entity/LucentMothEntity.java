package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class LucentMothEntity extends CombatMobEntity {
   public int phaseInt;

   public LucentMothEntity(EntityType var1, Level var2) {
      super(var1, var2, 30, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(10.0, 0.34, 2.0, 48.0);
   }
}
