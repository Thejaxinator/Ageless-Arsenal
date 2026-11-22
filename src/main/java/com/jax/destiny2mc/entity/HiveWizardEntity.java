package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HiveWizardEntity extends CombatMobEntity {
   public int phaseInt;

   public HiveWizardEntity(EntityType var1, Level var2) {
      super(var1, var2, 25, 18.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(58.0, 0.26, 2.8, 48.0);
   }
}
