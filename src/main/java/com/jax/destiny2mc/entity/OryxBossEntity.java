package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class OryxBossEntity extends CombatMobEntity {
   public int phaseInt;

   public OryxBossEntity(EntityType var1, Level var2) {
      super(var1, var2, 31, 35.0F);
   }

   public static AttributeSupplier createAttributes() {
      return FallenMobEntity.makeAttrs(440.0, 0.26, 5.2, 48.0);
   }
}
