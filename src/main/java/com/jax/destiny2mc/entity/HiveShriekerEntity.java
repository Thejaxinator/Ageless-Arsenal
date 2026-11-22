package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HiveShriekerEntity extends CombatMobEntity {
   public int phaseInt;

   public HiveShriekerEntity(EntityType var1, Level var2) {
      super(var1, var2, 27, 12.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(62.0, 0.0, 1.0, 48.0);
   }
}
