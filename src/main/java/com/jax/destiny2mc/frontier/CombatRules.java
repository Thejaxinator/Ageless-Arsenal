package com.jax.destiny2mc.frontier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
public final class CombatRules {
 public static boolean canDamage(Entity owner,Entity victim){
  if(!(victim instanceof LivingEntity v)||!v.isAlive()||owner==victim||v.isSpectator()||owner==null||owner.isAlliedTo(v))return false;
  if(v instanceof Player p&&(p.isCreative()||p.isSpectator()))return false;
  if(owner instanceof Player p)return !(v instanceof Player other)||p.canHarmPlayer(other);
  return v instanceof Player||v instanceof IronGolem;
 }
 public static boolean visible(Entity from,Vec3 origin,LivingEntity to){return from.level().clip(new ClipContext(origin,to.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,from)).getType()==HitResult.Type.MISS;}
}
