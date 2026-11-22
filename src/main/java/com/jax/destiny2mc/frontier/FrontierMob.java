package com.jax.destiny2mc.frontier;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;

/** Shared locomotion and synchronized animation with explicitly authored unit attacks. */
public class FrontierMob extends Monster {
 public static final int IDLE=0,FIRE=1,MELEE=2,CAST=3,SHIELD=4,BLINK=5,LEAP=6,SLAM=7,CHARGE=8,STAGGER=9;
 private static final EntityDataAccessor<Integer> MOVE=SynchedEntityData.defineId(FrontierMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(FrontierMob.class,EntityDataSerializers.LONG);
 private static final EntityDataAccessor<Integer> WEAK=SynchedEntityData.defineId(FrontierMob.class,EntityDataSerializers.INT);
 public final FrontierCatalog.Unit unit;
 private final ServerBossEvent bar;
 private int cooldown=35,special=100,duration,cycle,phase,shieldTicks,leashTicks;
 private Vec3 mark=Vec3.ZERO,home;
 private boolean chestKill;
 private float leftWeak,rightWeak;
 private final List<UUID> anchors=new ArrayList<>();
 public FrontierMob(EntityType<? extends FrontierMob> type,Level level,FrontierCatalog.Unit unit){
  super(type,level);this.unit=unit;xpReward=unit.boss()?unit.raid()?140:70:7;setNoGravity(unit.flying());
  bar=new ServerBossEvent(Component.literal(unit.name()),unit.raid()?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS);
  if(unit.boss())setPersistenceRequired();
 }
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MOVE,0);b.define(START,0L);b.define(WEAK,0);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,.65));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,IronGolem.class,true));}
 public int move(){return entityData.get(MOVE);} public int moveAge(){return (int)Math.max(0,level().getGameTime()-entityData.get(START));}public int weakMask(){return entityData.get(WEAK);}
 private void begin(int state,int ticks){entityData.set(MOVE,state);entityData.set(START,level().getGameTime());duration=ticks;if(getTarget()!=null)mark=getTarget().position();getNavigation().stop();}
 private void finish(int cd){entityData.set(MOVE,IDLE);cooldown=cd;}
 public void animate(int state,int ticks){begin(state,ticks);}
 public void previewAnimation(int state,int age){entityData.set(MOVE,state);entityData.set(START,level().getGameTime()-age);}
 public void encounterTitle(String text){bar.setName(Component.literal(unit.name()+" | "+text));}
 private boolean meleeUnit(){return Set.of("pounce","blink","censer","explode","blades","prod","torment","geist","darkblade","crota","rhulk","nezarec").contains(unit.move());}
 @Override public void aiStep(){
  super.aiStep();if(!(level() instanceof ServerLevel l)||!isAlive())return;
  if(home==null)home=position();if(cooldown>0)cooldown--;if(special>0)special--;if(shieldTicks>0)shieldTicks--;CorpusCombat.tick(this,l);
  bar.setProgress(Mth.clamp(getHealth()/getMaxHealth(),0,1));
  if(com.jax.destiny2mc.atlas.DungeonEncounters.isDungeon(this)){com.jax.destiny2mc.atlas.DungeonEncounters.tick(this,l);return;}
  LivingEntity target=getTarget();
  setAggressive(target!=null);
  if(target!=null&&(!target.isAlive()||!CombatRules.canDamage(this,target))){setTarget(null);target=null;}
  if(unit.boss()){
   if(target==null){leashTicks++;if(leashTicks>200){heal(.3f);if(position().distanceTo(home)>8)getNavigation().moveTo(home.x,home.y,home.z,1);}}
   else leashTicks=0;
   if(position().distanceTo(home)>56){setTarget(null);getNavigation().moveTo(home.x,home.y,home.z,1);finish(40);return;}
   phase=Math.max(phase,getHealth()<getMaxHealth()*.35?2:getHealth()<getMaxHealth()*.7?1:0);
  }
  if(move()!=IDLE){tickMove(l,target);return;}
  if(target==null){if(unit.flying()){setDeltaMovement(getDeltaMovement().scale(.8));fallDistance=0;}return;}
  getLookControl().setLookAt(target,25,25);float yaw=(float)(Mth.atan2(target.getZ()-getZ(),target.getX()-getX())*180/Math.PI)-90;setYRot(Mth.rotateIfNecessary(getYRot(),yaw,14));yBodyRot=getYRot();
  double distance=distanceTo(target);boolean sight=hasLineOfSight(target);
  if(unit.flying())fly(target);else if(distance>(meleeUnit()?2.2:10)||!sight){if(tickCount%10==0)getNavigation().moveTo(target,meleeUnit()?1.1:1);}else if(!meleeUnit()&&tickCount%30==0){Vec3 side=new Vec3(target.getZ()-getZ(),0,getX()-target.getX()).normalize().scale((cycle%2==0?1:-1)*2);getNavigation().moveTo(getX()+side.x,getY(),getZ()+side.z,.7);}
  if(!sight||distance>32)return;
  if(unit.move().equals("explode")&&distance<3.2){begin(CAST,34);return;}
  if(special<=0){
   special=unit.boss()?130-phase*15:170;cycle++;
   int state=switch(unit.move()){
    case "shield","tether","totem","repulse","bursa"->SHIELD;
    case "blink","mist","taniks"->BLINK;
    case "pounce","jetpack","torment","nezarec","hyena"->LEAP;
    case "darkblade","crota","golgoroth","shockwave"->SLAM;
    case "rhulk"->CHARGE;
    default->CAST;
   };
   begin(state,state==SHIELD?80:state==SLAM?52:state==CHARGE?54:state==LEAP?46:state==BLINK?28:60);return;
  }
  if(cooldown<=0){
   if(distance<(unit.boss()?4.5:2.8)){begin(MELEE,30);return;}
   if(!meleeUnit()){begin(FIRE,36);return;}
  }
 }
 private void fly(LivingEntity target){
  getNavigation().stop();fallDistance=0;Vec3 away=position().subtract(target.position()).multiply(1,0,1).normalize();double range=unit.body().equals("geist")?0:unit.boss()?8:6;
  Vec3 dst=target.position().add(away.scale(range)).add(0,unit.body().equals("geist")?1:2.2,0);Vec3 v=dst.subtract(position()).scale(.045);
  v=new Vec3(Mth.clamp(v.x,-.18,.18),Mth.clamp(v.y,-.16,.16),Mth.clamp(v.z,-.18,.18));if(level().noCollision(this,getBoundingBox().move(v)))setDeltaMovement(v);
 }
 private void stopMotion(){getNavigation().stop();setDeltaMovement(0,isNoGravity()?0:getDeltaMovement().y,0);}
 private void tickMove(ServerLevel l,LivingEntity target){
  int a=moveAge(),state=move();boolean live=target!=null&&target.isAlive();
  if(live&&a<15)getLookControl().setLookAt(target,25,25);
  switch(state){
   case FIRE->{if(live&&hasLineOfSight(target)&&(a==12||a==18||a==24)){if(unit.move().equals("flame"))flame(l,target);else shoot(l,target,0,unit.element(),unit.damage(),1.0f);} }
   case MELEE->{stopMotion();if((a==14||a==22&&unit.move().equals("blades"))&&live&&distanceTo(target)<(unit.boss()?5:3)&&hasLineOfSight(target)){target.hurt(damageSources().mobAttack(this),unit.damage());target.knockback(.45,getX()-target.getX(),getZ()-target.getZ());if(unit.move().equals("censer"))FrontierHazard.spawn(l,this,target.position(),2,60,2,1,0);if(unit.move().equals("geist")){blast(l,position(),2.5f,6,.3);discard();}}}
   case BLINK->{stopMotion();if(a%3==0)particles(l,unit.faction().equals("scorn")?3:unit.element(),8);if(a==12&&live)blink(l,target);if(unit.move().equals("taniks")&&a==22&&live)for(int i=0;i<3;i++)shoot(l,target,5,1,6,1);}
   case SHIELD->{stopMotion();if(a==40&&unit.move().equals("repulse"))blast(l,position(),6,4,1.4);if(a==40&&unit.move().equals("bursa"))CorpusCombat.specialAttack(this,l,target,mark);if(a%4==0)ring(l,position(),1.5,unit.element());
    if(unit.move().equals("tether")||unit.move().equals("totem")){if(a%10==0){for(FrontierMob ally:l.getEntitiesOfClass(FrontierMob.class,getBoundingBox().inflate(7),e->e!=this&&e.unit.faction().equals(unit.faction()))){ally.shieldTicks=15;ally.heal(1);line(l,getEyePosition(),ally.getEyePosition(),unit.element());}}}
   }
   case LEAP->{getNavigation().stop();fallDistance=0;
    if(a<15){stopMotion();if(a%4==0)ring(l,mark,unit.boss()?4:2.5,unit.element());}
    if(a==15){Vec3 v=mark.subtract(position()).multiply(1,0,1).normalize().scale(unit.move().equals("jetpack")?.12:.4);setDeltaMovement(v.x,.85,v.z);}
    if(unit.move().equals("jetpack")){if(a>=18&&a%7==0&&live)shoot(l,target,0,1,unit.damage(),1.1f);}
    else if(a==26){Vec3 v=mark.subtract(position()).normalize().scale(.85);setDeltaMovement(v.x,Math.min(-.35,v.y),v.z);}
    if(a>=27&&(onGround()||horizontalCollision||a==40)){blast(l,position(),unit.boss()?4.5f:3,unit.damage(),.8);if(unit.move().equals("nezarec"))FrontierHazard.spawn(l,this,position(),5,70,4,3,1);if(unit.move().equals("hyena"))CorpusCombat.specialAttack(this,l,target,mark);finish(35);return;}
   }
   case SLAM->{stopMotion();if(a<32&&a%4==0)ring(l,mark,unit.boss()?5:3,unit.element());if(a==32){if(unit.move().equals("shockwave")){CorpusCombat.specialAttack(this,l,target,mark);return;}blast(l,mark,5,unit.damage(),.9);if(unit.move().equals("crota")){shieldTicks=0;begin(STAGGER,80);summonOrbs(l,1);return;}if(unit.move().equals("golgoroth"))FrontierHazard.spawn(l,this,mark,4,100,3,3,1);if(unit.move().equals("darkblade")&&live)blink(l,target);}}
   case CHARGE->{getNavigation().stop();if(a<24){stopMotion();if(a%4==0)line(l,position().add(0,.1,0),mark.add(0,.1,0),1);}else if(a<36){Vec3 v=mark.subtract(position()).multiply(1,0,1).normalize().scale(.62);if(l.noCollision(this,getBoundingBox().move(v)))setDeltaMovement(v.x,getDeltaMovement().y,v.z);if(a%4==0)blast(l,position().add(v),2.5f,6,.5);}else stopMotion();if(a==38){Vec3 d=mark.subtract(position()).normalize();Vec3 side=new Vec3(-d.z,0,d.x);for(int i=-1;i<=1;i++){Vec3 end=mark.add(side.scale(i*3));line(l,getEyePosition(),end.add(0,1,0),1);FrontierHazard.spawn(l,this,end,1.3f,45,4,1,0);}}}
   case CAST->{stopMotion();if(a<28&&a%4==0)ring(l,mark,unit.boss()?4:2.5,unit.element());if(a==28)specialAttack(l,target);if(a==40&&unit.move().equals("eye")&&live)shoot(l,target,0,3,5,1);}
   case STAGGER->{stopMotion();if(a%8==0)particles(l,2,8);if(unit.move().equals("crota")&&a==70&&orbsAlive()){blast(l,position(),15,10,.4);for(UUID id:anchors){Entity orb=l.getEntity(id);if(orb!=null)orb.discard();}anchors.clear();}}
  }
  if(a>=duration)finish(unit.boss()?20:30);
 }
 private void specialAttack(ServerLevel l,LivingEntity target){
  if(CorpusCombat.specialAttack(this,l,target,mark))return;
  boolean live=target!=null&&target.isAlive();
  switch(unit.move()){
   case "missiles","valus"->{if(live)for(int i=0;i<(unit.boss()?5:3);i++){Vec3 start=getEyePosition().add((i-2)*.18,.5,0);FrontierProjectile p=FrontierProjectile.launch(l,this,start,target.getEyePosition().subtract(start).normalize(),.65f,unit.damage()*.7f,1,5,2);p.target(target);}}
   case "psion","suspend"->FrontierHazard.spawn(l,this,mark,2.8f,70,2,unit.element(),2);
   case "frostfan"->{Vec3 f=mark.subtract(position()).multiply(1,0,1).normalize();Vec3 s=new Vec3(-f.z,0,f.x);for(int i=-1;i<=1;i++)FrontierHazard.spawn(l,this,mark.add(s.scale(i*3)),2.1f,90,3,4,0);}
   case "stasis","pull"->{if(live)shoot(l,target,unit.move().equals("stasis")?9:10,unit.element(),unit.damage(),.8f);}
   case "darkness"->{if(live)shoot(l,target,11,3,6,.65f);}
   case "blightfire","censer","hashladun"->FrontierHazard.spawn(l,this,mark,unit.boss()?4:2.8f,100,3,1,unit.boss()?1:0);
   case "repulse"->{blast(l,position(),6,4,1.4);}
   case "explode"->{if(live&&distanceTo(target)<4){blast(l,position(),4,10,1);discard();}}
   case "screech"->{for(LivingEntity v:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(7),e->CombatRules.canDamage(this,e)&&hasLineOfSight(e))){v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,1),this);v.addEffect(new MobEffectInstance(MobEffects.CONFUSION,80,0),this);}particles(l,2,20);}
   case "eye"->{if(live)for(int i=-1;i<=1;i++)shoot(l,target,0,3,3,.75f);}
   case "arcbolt"->{if(live){line(l,getEyePosition(),mark.add(0,1,0),2);blast(l,mark,3,8,.3);}}
   case "flame"->{if(live)flame(l,target);}
   case "zahn"->{summon(l,"cabal_legionary",2);shieldTicks=100;FrontierHazard.spawn(l,this,mark,3,80,3,1,0);}
   case "fikrul"->{summon(l,"scorn_stalker",2);if(live){Vec3 v=position().subtract(target.position()).normalize().scale(.8);target.push(v.x,.35,v.z);FrontierHazard.spawn(l,this,mark,4,90,3,2,0);}}
   case "templar"->{summonOrbs(l,3);shieldTicks=150;}
   default->{if(live)shoot(l,target,0,unit.element(),unit.damage()*1.5f,.8f);}
  }
 }
 private void shoot(ServerLevel l,LivingEntity t,int mode,int element,float damage,float speed){Vec3 from=getEyePosition().add(getLookAngle().scale(.5));FrontierProjectile p=FrontierProjectile.launch(l,this,from,t.getEyePosition().subtract(from).normalize(),speed,damage,element,mode,mode==5?2:0);if(mode==5)p.target(t);}
 private void flame(ServerLevel l,LivingEntity target){line(l,getEyePosition(),getEyePosition().add(getLookAngle().scale(6)),1);if(distanceTo(target)<7&&hasLineOfSight(target)){target.hurt(damageSources().mobAttack(this),unit.damage());target.igniteForSeconds(2);}}
 private void blink(ServerLevel l,LivingEntity target){for(int i=0;i<12;i++){double angle=random.nextDouble()*Math.PI*2;BlockPos p=BlockPos.containing(target.getX()+Math.cos(angle)*6,target.getY(),target.getZ()+Math.sin(angle)*6);if(!l.hasChunkAt(p))continue;for(int y=2;y>=-3;y--){BlockPos q=p.offset(0,y,0);if(l.getBlockState(q.below()).isSolidRender(l,q.below())&&l.noCollision(this,getBoundingBox().move(q.getX()+.5-getX(),q.getY()-getY(),q.getZ()+.5-getZ()))){CombatSupport.safeEntityTeleport(this,q.getX()+.5,q.getY(),q.getZ()+.5);return;}}}}
 private void summon(ServerLevel l,String id,int count){
  if(l.getEntitiesOfClass(FrontierMob.class,getBoundingBox().inflate(24)).size()>14)return;
  for(int i=0;i<count;i++){FrontierMob mob=FrontierContent.ENTITIES.get(id).create(l);if(mob==null)continue;BlockPos p=blockPosition().offset(i==0?4:-4,0,3);for(int dy=3;dy>=-3;dy--){BlockPos q=p.offset(0,dy,0);mob.moveTo(q.getX()+.5,q.getY(),q.getZ()+.5);if(l.getBlockState(q.below()).isSolidRender(l,q.below())&&l.noCollision(mob)){mob.setTarget(getTarget());l.addFreshEntity(mob);break;}}}
 }
 private void summonOrbs(ServerLevel l,int count){
  orbsAlive();if(!anchors.isEmpty())return;
  for(int i=0;i<count;i++){Entity e=((EntityType<?>)ModRegistries.VEX_ORACLE.get()).create(l);if(e!=null){double angle=i*Math.PI*2/count;e.moveTo(getX()+Math.cos(angle)*5,getY()+2,getZ()+Math.sin(angle)*5);if(l.noCollision(e)){e.getPersistentData().putUUID("frontier_orb_owner",getUUID());l.addFreshEntity(e);anchors.add(e.getUUID());}}}
 }
 private boolean orbsAlive(){if(!(level() instanceof ServerLevel l))return false;anchors.removeIf(id->!(l.getEntity(id) instanceof LivingEntity e)||!e.isAlive());return !anchors.isEmpty();}
 public void blast(ServerLevel l,Vec3 p,float radius,float damage,double push){
  l.sendParticles(ParticleTypes.EXPLOSION,p.x,p.y+.2,p.z,2,.2,.1,.2,0);
  for(LivingEntity v:l.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(radius),e->CombatRules.canDamage(this,e))){if(v.position().distanceTo(p)>radius||!CombatRules.visible(this,p.add(0,.65,0),v))continue;v.hurt(damageSources().mobAttack(this),damage);v.knockback(push,p.x-v.getX(),p.z-v.getZ());}
 }
 private void particles(ServerLevel l,int element,int count){l.sendParticles(ServerBallisticsEngine.getElementParticle(element),getX(),getY()+getBbHeight()*.6,getZ(),count,getBbWidth()*.5,getBbHeight()*.3,getBbWidth()*.5,.02);}
 public static void ring(ServerLevel l,Vec3 p,double radius,int element){for(int i=0;i<20;i++){double a=i*Math.PI/10;l.sendParticles(ServerBallisticsEngine.getElementParticle(element),p.x+Math.cos(a)*radius,p.y+.15,p.z+Math.sin(a)*radius,1,0,0,0,0);}}
 private static void line(ServerLevel l,Vec3 a,Vec3 b,int el){for(int i=0;i<=20;i++){Vec3 p=a.lerp(b,i/20.);l.sendParticles(ServerBallisticsEngine.getElementParticle(el),p.x,p.y,p.z,1,0,0,0,0);}}
 @Override public boolean hurt(DamageSource src,float damage){
  if(isInvulnerableTo(src))return false;
  if(!com.jax.destiny2mc.atlas.DungeonEncounters.allowDamage(this,src))return false;
  Entity attacker=src.getEntity();chestKill=false;
  if(attacker instanceof Player player){
   Vec3 eye=player.getEyePosition();Vec3 hit=getBoundingBox().clip(eye,eye.add(player.getLookAngle().scale(128))).orElse(getBoundingBox().getCenter());double fraction=(hit.y-getY())/getBbHeight();
   chestKill=unit.move().equals("blades")&&fraction>.42&&fraction<.75;
   if(unit.move().equals("torment")||unit.move().equals("nezarec")){
    double side=(hit.x-getX())*Math.cos(Math.toRadians(getYRot()))+(hit.z-getZ())*Math.sin(Math.toRadians(getYRot()));
    if(fraction>.65&&Math.abs(side)>getBbWidth()*.18){if(side<0)leftWeak+=damage;else rightWeak+=damage;int mask=(leftWeak>=18?1:0)|(rightWeak>=18?2:0);int previous=weakMask();entityData.set(WEAK,mask);if(mask==3&&previous!=3)begin(STAGGER,45);}
    if(weakMask()!=3)damage*=.35f;
   }
  }
  if(shieldTicks>0){if(unit.move().equals("templar")&&!orbsAlive())shieldTicks=0;else damage*=.2f;}
  if(move()==SHIELD&&(unit.move().equals("shield")||unit.move().equals("repulse"))&&attacker!=null){Vec3 facing=getLookAngle().multiply(1,0,1).normalize(),to=attacker.position().subtract(position()).multiply(1,0,1).normalize();if(facing.dot(to)>.25)damage*=.15f;}
  if(unit.move().equals("crota")&&move()!=STAGGER)damage*=.4f;
  if(unit.move().equals("golgoroth")&&attacker!=null){Vec3 to=attacker.position().subtract(position()).normalize();if(getLookAngle().dot(to)<-.3){damage*=1.5f;setTarget(attacker instanceof LivingEntity v?v:null);begin(STAGGER,55);}}
  damage=CorpusCombat.filterDamage(this,src,damage);if(damage<=0f)return true;
  return super.hurt(src,damage);
 }
 @Override public void die(DamageSource s){
  if(!level().isClientSide){
   com.jax.destiny2mc.atlas.DungeonEncounters.complete(this);
   if(unit.move().equals("blades")&&!chestKill){FrontierMob geist=FrontierContent.ENTITIES.get("dread_geist").create(level());if(geist!=null){geist.moveTo(getX(),getY()+1,getZ());geist.setTarget(getTarget());level().addFreshEntity(geist);}}
   spawnAtLocation(new net.minecraft.world.item.ItemStack(FrontierContent.materialItem(unit.faction()),unit.boss()?8:1+random.nextInt(2)));CorpusCombat.onDeath(this);
   if(unit.boss()){spawnAtLocation(new net.minecraft.world.item.ItemStack(FrontierContent.item("conquest_token"),unit.raid()?4:com.jax.destiny2mc.atlas.DungeonEncounters.isDungeon(this)?3:2));spawnAtLocation((net.minecraft.world.item.Item)ModRegistries.EXOTIC_ENGRAM.get());}
   if(level() instanceof ServerLevel l)for(UUID id:anchors){Entity e=l.getEntity(id);if(e!=null)e.discard();}
  }
  bar.removeAllPlayers();super.die(s);
 }
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(unit.boss())bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override public boolean causeFallDamage(float a,float b,DamageSource d){return false;}
 @Override public boolean removeWhenFarAway(double distance){return !unit.boss();}
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("FrontierPhase",phase);t.putInt("FrontierCycle",cycle);t.putInt("FrontierShield",shieldTicks);t.putFloat("LeftWeak",leftWeak);t.putFloat("RightWeak",rightWeak);if(home!=null){t.putDouble("HomeX",home.x);t.putDouble("HomeY",home.y);t.putDouble("HomeZ",home.z);}ListTag ids=new ListTag();for(UUID id:anchors){CompoundTag a=new CompoundTag();a.putUUID("Id",id);ids.add(a);}t.put("Orbs",ids);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);phase=t.getInt("FrontierPhase");cycle=t.getInt("FrontierCycle");shieldTicks=t.getInt("FrontierShield");leftWeak=t.getFloat("LeftWeak");rightWeak=t.getFloat("RightWeak");entityData.set(WEAK,(leftWeak>=18?1:0)|(rightWeak>=18?2:0));if(t.contains("HomeX"))home=new Vec3(t.getDouble("HomeX"),t.getDouble("HomeY"),t.getDouble("HomeZ"));anchors.clear();for(Tag tag:t.getList("Orbs",10))if(tag instanceof CompoundTag c&&c.hasUUID("Id"))anchors.add(c.getUUID("Id"));finish(35);}
}
