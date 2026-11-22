package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class VexHobgoblinEntity extends CombatMobEntity {
   public int phaseInt;

   public VexHobgoblinEntity(EntityType var1, Level var2) {
      super(var1, var2, 33, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(32.0, 0.24, 2.5, 48.0);
   }
}
