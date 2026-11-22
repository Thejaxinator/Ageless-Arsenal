package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class VexMinotaurEntity extends CombatMobEntity {
   public int phaseInt;

   public VexMinotaurEntity(EntityType var1, Level var2) {
      super(var1, var2, 35, 22.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(74.0, 0.26, 4.2, 48.0);
   }
}
