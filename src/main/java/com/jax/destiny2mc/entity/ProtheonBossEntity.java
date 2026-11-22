package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class ProtheonBossEntity extends CombatMobEntity {
   public int phaseInt;

   public ProtheonBossEntity(EntityType var1, Level var2) {
      super(var1, var2, 41, 28.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(390.0, 0.25, 4.8, 48.0);
   }
}
