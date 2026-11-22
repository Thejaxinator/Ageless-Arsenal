package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class VexGoblinEntity extends CombatMobEntity {
   public int phaseInt;

   public VexGoblinEntity(EntityType var1, Level var2) {
      super(var1, var2, 32, 0.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(24.0, 0.25, 2.6, 48.0);
   }
}
