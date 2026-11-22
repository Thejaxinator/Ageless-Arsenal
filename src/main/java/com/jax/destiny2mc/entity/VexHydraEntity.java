package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class VexHydraEntity extends CombatMobEntity {
   public int phaseInt;

   public VexHydraEntity(EntityType var1, Level var2) {
      super(var1, var2, 36, 20.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(96.0, 0.2, 3.0, 48.0);
   }
}
