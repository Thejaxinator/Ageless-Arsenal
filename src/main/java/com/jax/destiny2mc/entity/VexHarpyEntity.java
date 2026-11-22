package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class VexHarpyEntity extends CombatMobEntity {
   public int phaseInt;

   public VexHarpyEntity(EntityType var1, Level var2) {
      super(var1, var2, 34, 10.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(26.0, 0.27, 2.2, 48.0);
   }
}
