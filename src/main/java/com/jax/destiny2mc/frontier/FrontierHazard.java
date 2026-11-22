package com.jax.destiny2mc.frontier;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
/** Bounded, saved area effect. No block edits and no static cross-world entity lists. */
public class FrontierHazard extends Entity {
 private UUID owner;private float radius=3,damage=2;private int life=80,element=3,kind=0;
 public FrontierHazard(EntityType<?> t,Level l){super(t,l);setNoGravity(true);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 public static void spawn(ServerLevel l,LivingEntity owner,Vec3 p,float r,int ticks,float damage,int element,int kind){
  if(l.getEntitiesOfClass(FrontierHazard.class,new AABB(p,p).inflate(8)).size()>=8)return;
  FrontierHazard h=new FrontierHazard(FrontierContent.HAZARD,l);h.owner=owner.getUUID();h.setPos(p);h.radius=r;h.life=ticks;h.damage=damage;h.element=element;h.kind=kind;l.addFreshEntity(h);
 }
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel l))return;if(--life<=0){discard();return;}
  Entity source=owner==null?null:l.getEntity(owner);if(!(source instanceof LivingEntity living)||!living.isAlive()){discard();return;}
  if(tickCount%5==0){for(int i=0;i<16;i++){double a=i*Math.PI/8;l.sendParticles(ServerBallisticsEngine.getElementParticle(element),getX()+Math.cos(a)*radius,getY()+.16,getZ()+Math.sin(a)*radius,1,0,.05,0,0);}l.sendParticles(ServerBallisticsEngine.getElementParticle(element),getX(),getY()+.25,getZ(),7,radius*.6,.15,radius*.6,.01);}
  if(tickCount%10!=0)return;
  for(LivingEntity v:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(radius,2,radius),e->CombatRules.canDamage(living,e))){
   if(v.position().distanceToSqr(position())>radius*radius+3||!CombatRules.visible(this,position().add(0,.5,0),v))continue;
   v.hurt(damageSources().indirectMagic(this,living),damage);
   if(element==1)v.igniteForSeconds(2);
   if(element==4)v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,1),living);
   if(kind==1){v.addEffect(new MobEffectInstance(MobEffects.POISON,40,0),living);v.addEffect(new MobEffectInstance(MobEffects.DARKNESS,40,0),living);}
   if(kind==2)v.addEffect(new MobEffectInstance(MobEffects.LEVITATION,15,0),living);
  }
 }
 @Override protected void readAdditionalSaveData(CompoundTag t){if(t.hasUUID("Owner"))owner=t.getUUID("Owner");radius=t.getFloat("Radius");damage=t.getFloat("Damage");life=t.getInt("Life");element=t.getInt("Element");kind=t.getInt("Kind");}
 @Override protected void addAdditionalSaveData(CompoundTag t){if(owner!=null)t.putUUID("Owner",owner);t.putFloat("Radius",radius);t.putFloat("Damage",damage);t.putInt("Life",life);t.putInt("Element",element);t.putInt("Kind",kind);}
}
