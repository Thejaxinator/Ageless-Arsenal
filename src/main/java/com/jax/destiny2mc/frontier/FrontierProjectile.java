package com.jax.destiny2mc.frontier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.*;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.server.level.ServerPlayer;
import com.jax.destiny2mc.network.CombatFeedbackPayload;
import net.neoforged.neoforge.network.PacketDistributor;
public class FrontierProjectile extends ThrowableItemProjectile {
 private static final EntityDataAccessor<Integer> ELEMENT=SynchedEntityData.defineId(FrontierProjectile.class,EntityDataSerializers.INT);
 // Synced separately from motion so wall-attached charges stay put on remote clients.
 private static final EntityDataAccessor<Boolean> ATTACHED=SynchedEntityData.defineId(FrontierProjectile.class,EntityDataSerializers.BOOLEAN);
 private UUID anchorEntity;
 private Vec3 anchorOffset=Vec3.ZERO,anchorPos=Vec3.ZERO;
 public boolean attached(){return entityData.get(ATTACHED);}
 public int behaviorMode(){return mode;}
 public int sourceWeaponIndex(){return weaponIndex;}
 private int weaponIndex=-1;private ItemStack weaponSnapshot=ItemStack.EMPTY,sourceStack=ItemStack.EMPTY;
 private float damage=4,radius; private int mode,life=100,fuse=-1; private UUID seek;
 // Modes: 0 bolt, 1 sticky, 2 burning seeker, 3 wolfpack carrier, 4 void rocket,
 // 5 solar rocket, 6 blight, 7 poison arrow, 8 wolfpack child, 9 stasis, 10 strand.
 public FrontierProjectile(EntityType<? extends FrontierProjectile> type,Level l){super(type,l);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ELEMENT,1);b.define(ATTACHED,false);}
 @Override protected Item getDefaultItem(){return Items.FIRE_CHARGE;}
 public int element(){return entityData.get(ELEMENT);}
 public static FrontierProjectile launch(ServerLevel l,LivingEntity owner,Vec3 start,Vec3 dir,float speed,float damage,int el,int mode,float radius){
  FrontierProjectile p=new FrontierProjectile(FrontierContent.BOLT,l);p.setOwner(owner);p.setPos(start);p.damage=damage;p.mode=mode;p.radius=radius;p.entityData.set(ELEMENT,el);p.setNoGravity(mode!=6&&mode!=7&&mode!=12);
  p.setItem(new ItemStack(switch(el){case 2->Items.PRISMARINE_CRYSTALS;case 3->Items.AMETHYST_SHARD;case 4->Items.SNOWBALL;case 5->Items.SLIME_BALL;default->Items.FIRE_CHARGE;}));
  p.shoot(dir.x,dir.y,dir.z,speed,0);l.addFreshEntity(p);return p;
 }
 public FrontierProjectile weapon(ItemStack stack,WeaponSpec w){weaponIndex=w.index;sourceStack=stack;weaponSnapshot=stack.copy();life=Math.max(8,Math.min(180,(int)(WeaponPerkRegistry.getEffectiveRange(stack,w,level().getGameTime())/Math.max(.1,getDeltaMovement().length()))));return this;}
 private void damageTarget(LivingEntity v,float amount,Vec3 point,boolean direct){
  Entity owner=getOwner();int crit=0;WeaponSpec w=weaponIndex>=0&&weaponIndex<WeaponSpec.ALL.length?WeaponSpec.ALL[weaponIndex]:null;
  if(w!=null&&!weaponSnapshot.isEmpty()){crit=direct&&weaponIndex==51&&point.y>v.getY()+v.getBbHeight()*.78?1:0;amount*=WeaponPerkRegistry.computeSwappableDamageMult(weaponSnapshot,w,v,crit,level().getGameTime())*(crit!=0?w.critMult:1);}
  v.invulnerableTime=0;if(!v.hurt(damageSources().thrown(this,owner),amount))return;
  if(owner instanceof ServerPlayer p&&w!=null){
   ItemStack feedback=weaponSnapshot;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i)==sourceStack){feedback=sourceStack;break;}
   WeaponPerkRegistry.onSwappablePostHit((ServerLevel)level(),p,feedback,w,v,point,crit,v.isAlive()?0:1,level().getGameTime());
   PacketDistributor.sendToPlayer(p,new CombatFeedbackPayload(amount,(byte)crit,(float)point.x,(float)point.y,(float)point.z));
  }
 }
 public void target(LivingEntity e){seek=e.getUUID();}
 @Override protected double getDefaultGravity(){return mode==6?.035:mode==7?.012:mode==12?.045:0;}
 @Override protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&CombatRules.canDamage(getOwner(),e);}
 @Override public void tick(){
  if(attached()){
   setDeltaMovement(Vec3.ZERO);
   if(level() instanceof ServerLevel l){
    if(anchorEntity!=null&&l.getEntity(anchorEntity) instanceof LivingEntity target&&target.isAlive())anchorPos=target.position().add(anchorOffset);
    setPos(anchorPos);if(--fuse<=0){detonate(anchorPos);discard();}
   }
   return;
  }
  super.tick();
  if(level().isClientSide)return;
  if(--life<=0){discard();return;}
  ServerLevel l=(ServerLevel)level();
  if((mode==2||mode==3||mode==4||mode==5||mode==8)&&tickCount>4){
   LivingEntity target=seek==null?null:l.getEntity(seek) instanceof LivingEntity v?v:null;
   if(target==null||!target.isAlive()){
    target=l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(7),e->CombatRules.canDamage(getOwner(),e)&&CombatRules.visible(this,position(),e)&&getDeltaMovement().normalize().dot(e.getBoundingBox().getCenter().subtract(position()).normalize())>.5).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    if(target!=null)seek=target.getUUID();
   }
   if(target!=null&&CombatRules.visible(this,position(),target)){double speed=getDeltaMovement().length();Vec3 dir=target.getBoundingBox().getCenter().subtract(position()).normalize();setDeltaMovement(getDeltaMovement().scale(.92).add(dir.scale(speed*.08)).normalize().scale(speed));}
  }
  if(tickCount%2==0)l.sendParticles(ServerBallisticsEngine.getElementParticle(element()),getX(),getY(),getZ(),2,.03,.03,.03,.005);
 }
 @Override protected void onHit(HitResult hit){
  if(level().isClientSide)return;
  Vec3 point=hit.getLocation();
  if(mode==1){
   // Pin to the incoming side of the surface so the terrain itself does not occlude the delayed blast.
   anchorPos=point.subtract(getDeltaMovement().normalize().scale(.04));
   if(hit instanceof EntityHitResult h){anchorEntity=h.getEntity().getUUID();anchorOffset=anchorPos.subtract(h.getEntity().position());}
   setPos(anchorPos);setDeltaMovement(Vec3.ZERO);hasImpulse=true;setNoGravity(true);fuse=16;entityData.set(ATTACHED,true);return;
  }
  if(hit instanceof EntityHitResult h&&h.getEntity() instanceof LivingEntity v&&CombatRules.canDamage(getOwner(),v)){
   damageTarget(v,damage,point,true);
   if(mode==11)v.addEffect(new MobEffectInstance(MobEffects.DARKNESS,50,0),getOwner());
   if(mode==7)v.addEffect(new MobEffectInstance(MobEffects.POISON,80,0),getOwner());
   if(mode==9){v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,1),getOwner());v.setTicksFrozen(Math.min(200,v.getTicksFrozen()+65));}
   if(mode==10&&getOwner()!=null){Vec3 d=getOwner().position().subtract(v.position()).normalize().scale(.55);v.push(d.x,.18,d.z);}
  }
  detonate(point);discard();
 }
 private void detonate(Vec3 p){
  if(!(level() instanceof ServerLevel l))return;
  Entity owner=getOwner();
  if(radius>0&&owner!=null){
   l.sendParticles(ParticleTypes.EXPLOSION,p.x,p.y,p.z,1,0,0,0,0);
   for(LivingEntity v:l.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(radius),e->CombatRules.canDamage(owner,e))){
    double dist=v.getBoundingBox().getCenter().distanceTo(p);
    if(dist>radius||!CombatRules.visible(this,p.add(0,.15,0),v))continue;
    damageTarget(v,damage*.65f*(1-(float)dist/(radius*1.5f)),v.getBoundingBox().getCenter(),false);
    if(element()==1)v.igniteForSeconds(3);
    if(mode==4)v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0),owner);
   }
  }
  if(owner instanceof LivingEntity living){
   if(mode==2||mode==6)FrontierHazard.spawn(l,living,p,mode==6?3.4f:2.2f,mode==6?120:60,mode==6?3:2,element(),0);
   if(mode==3)for(int i=0;i<4;i++){Vec3 d=new Vec3(Math.cos(i*Math.PI/2),.65,Math.sin(i*Math.PI/2)).normalize();launch(l,living,p.add(0,.25,0),d,.65f,5,1,8,1.5f);}
  }
 }
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("WeaponIndex",weaponIndex);if(!weaponSnapshot.isEmpty())t.put("WeaponSnapshot",weaponSnapshot.save(registryAccess()));t.putInt("Mode",mode);t.putInt("Element",element());t.putInt("Life",life);t.putInt("Fuse",fuse);
  if(attached()){t.putDouble("AnchorX",anchorPos.x);t.putDouble("AnchorY",anchorPos.y);t.putDouble("AnchorZ",anchorPos.z);
   if(anchorEntity!=null){t.putUUID("AnchorEntity",anchorEntity);t.putDouble("OffsetX",anchorOffset.x);t.putDouble("OffsetY",anchorOffset.y);t.putDouble("OffsetZ",anchorOffset.z);}}
  t.putFloat("Damage",damage);t.putFloat("Radius",radius);if(seek!=null)t.putUUID("Seek",seek);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);weaponIndex=t.contains("WeaponIndex")?t.getInt("WeaponIndex"):-1;if(t.contains("WeaponSnapshot"))weaponSnapshot=ItemStack.parseOptional(registryAccess(),t.getCompound("WeaponSnapshot"));mode=t.getInt("Mode");entityData.set(ELEMENT,t.getInt("Element"));life=t.getInt("Life");fuse=t.contains("Fuse")?t.getInt("Fuse"):-1;
  if(fuse>=0){entityData.set(ATTACHED,true);anchorPos=t.contains("AnchorX")?new Vec3(t.getDouble("AnchorX"),t.getDouble("AnchorY"),t.getDouble("AnchorZ")):position();
   if(t.hasUUID("AnchorEntity")){anchorEntity=t.getUUID("AnchorEntity");anchorOffset=new Vec3(t.getDouble("OffsetX"),t.getDouble("OffsetY"),t.getDouble("OffsetZ"));}}
  damage=t.getFloat("Damage");radius=t.getFloat("Radius");if(t.hasUUID("Seek"))seek=t.getUUID("Seek");}
}
