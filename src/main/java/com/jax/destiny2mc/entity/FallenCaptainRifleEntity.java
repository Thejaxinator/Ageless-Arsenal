package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class FallenCaptainRifleEntity extends CombatMobEntity {
   public int phaseInt;

   public FallenCaptainRifleEntity(EntityType var1, Level var2) {
      super(var1, var2, 3, 24.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(65.0, 0.31, 4.0, 48.0);
   }
}
