package com.jax.destiny2mc.combat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
public final class SphinxEntity extends CombatMobEntity {
 public SphinxEntity(EntityType type,Level level){super(type,level,45,20);}
 public static AttributeSupplier createAttributes(){return makeAttrs(150,.18,4.5,48);}
}
