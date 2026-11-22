package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class AtheonBossEntity extends CombatMobEntity {
   public int phaseInt;

   public AtheonBossEntity(EntityType var1, Level var2) {
      super(var1, var2, 40, 35.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(460.0, 0.24, 5.0, 48.0);
   }
}
