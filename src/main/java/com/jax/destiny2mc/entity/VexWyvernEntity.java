package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class VexWyvernEntity extends CombatMobEntity {
   public int phaseInt;

   public VexWyvernEntity(EntityType var1, Level var2) {
      super(var1, var2, 37, 16.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(88.0, 0.28, 4.5, 48.0);
   }
}
