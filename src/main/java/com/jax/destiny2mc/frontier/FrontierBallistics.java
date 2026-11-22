package com.jax.destiny2mc.frontier;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.*;
import com.jax.destiny2mc.network.CombatFeedbackPayload;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** All cadence, ammunition, charge and damage decisions are authoritative on the server. */
public final class FrontierBallistics {
 public static int chargeTicks(WeaponSpec w,ItemStack s){return switch(w.index){case 40->18;case 41->20;case 42->Math.max(9,18-GunDataHelper.getInt(s,"frontier_momentum",0)*2);case 43->10;case 44->22;case 45->12;case 51->16;default->0;};}
 public static void action(ServerPlayer p,byte action,float yaw,float pitch,byte ads){
  if(p.isSpectator()||!p.isAlive()||!Float.isFinite(yaw)||!Float.isFinite(pitch)||action<0||action>4)return;
  ItemStack s=p.getMainHandItem();if(!(s.getItem() instanceof DestinyGunItem g))return;WeaponSpec w=g.spec;
  var program=com.jax.destiny2mc.ageless.WeaponPrograms.server(w);
  if(program!=null){com.jax.destiny2mc.ageless.ProgramBallistics.action(p,s,w,program,action,ads);return;}
  if(w.index<40){if(action<=2)ServerBallisticsEngine.handleGunAction(p,action,yaw,pitch,ads);return;}
  long t=p.level().getGameTime();
  if(action==4){cancel(s);return;}
  if(action==1||action==2){cancel(s);reload(p,s,w);return;}
  if(GunDataHelper.getLong(s,"d2_rel_end")>t||GunDataHelper.getLong(s,"frontier_next")>t)return;
  if(GunDataHelper.getMag(s,w)<=0){reload(p,s,w);return;}
  int charge=chargeTicks(w,s);
  if(action==3&&charge>0){
   GunDataHelper.setLong(s,"frontier_heartbeat",t);GunDataHelper.setInt(s,"frontier_ads",ads!=0?1:0);
   if(GunDataHelper.getLong(s,"frontier_charge")==0){GunDataHelper.setLong(s,"frontier_charge",t);GunDataHelper.setInt(s,"frontier_duration",charge);ServerBallisticsEngine.playSound(p.level(),p.getX(),p.getY(),p.getZ(),"block.respawn_anchor.charge",.35f,1.6f);}
   return;
  }
  if(action!=0)return;
  if(charge>0){
   long start=GunDataHelper.getLong(s,"frontier_charge");
   if(w.index!=51||start==0||t-start<6||t-GunDataHelper.getLong(s,"frontier_heartbeat")>8){cancel(s);return;}
   float strength=Math.min(1,(t-start)/16f);boolean perfect=t-start>=16&&t-start<=24;
   fire(p,s,w,ads,strength,perfect);cancel(s);return;
  }
  fire(p,s,w,ads,1,false);
 }
 private static void cancel(ItemStack s){if(GunDataHelper.getLong(s,"frontier_charge")!=0)GunDataHelper.setLong(s,"frontier_charge",0);}
 public static void inventoryTick(WeaponSpec w,ItemStack s,Level l,Entity e,boolean selected){
  long t=l.getGameTime(),end=GunDataHelper.getLong(s,"d2_rel_end");
  var program=com.jax.destiny2mc.ageless.WeaponPrograms.server(w);
  // Profiles may change during an active reload. A capacitor must never consume reserve ammo left
  // over from its previous magazine profile. Charge recovery is entirely handled by ProgramBallistics.
  if(program!=null&&program.feed()==com.jax.destiny2mc.ageless.WeaponProgram.Feed.CAPACITOR){
   if(end>0)GunDataHelper.setLong(s,"d2_rel_end",0);
   com.jax.destiny2mc.ageless.ProgramBallistics.tick(w,s,l,e,selected,program);return;
  }
  if(end>0&&t>=end&&e instanceof Player p){
   GunDataHelper.setLong(s,"d2_rel_end",0);int need=WeaponPerkRegistry.getEffectiveMagSize(s,w)-GunDataHelper.getMag(s,w),reserve=GunDataHelper.getReserve(s,w);
   if(w.usesSpecialAmmo&&reserve<need)reserve+=FrontierAmmo.consume(p,w);
   int fill=w.usesSpecialAmmo?Math.min(need,reserve):need;GunDataHelper.setInt(s,"d2_mag",GunDataHelper.getMag(s,w)+Math.max(0,fill));
   if(w.usesSpecialAmmo)GunDataHelper.setInt(s,"d2_res",Math.max(0,reserve-fill));
   WeaponPerkRegistry.onSwappableReloadComplete(s,w,t);ServerBallisticsEngine.playSound(l,p.getX(),p.getY(),p.getZ(),"item.crossbow.loading_end",.7f,1.2f);p.getInventory().setChanged();
  }
  if(program!=null){com.jax.destiny2mc.ageless.ProgramBallistics.tick(w,s,l,e,selected,program);return;}
  long start=GunDataHelper.getLong(s,"frontier_charge");if(start==0)return;
  if(!(e instanceof ServerPlayer p)||!selected||p.getMainHandItem()!=s||!p.isAlive()||p.isSpectator()||l.getGameTime()-GunDataHelper.getLong(s,"frontier_heartbeat")>8||GunDataHelper.getLong(s,"d2_rel_end")>l.getGameTime()){cancel(s);return;}
  if(w.index!=51&&l.getGameTime()-start>=GunDataHelper.getInt(s,"frontier_duration",chargeTicks(w,s))){fire(p,s,w,(byte)GunDataHelper.getInt(s,"frontier_ads",0),1,false);cancel(s);}
 }
 public static void reload(ServerPlayer p,ItemStack s,WeaponSpec w){
  long t=p.level().getGameTime();if(GunDataHelper.getLong(s,"d2_rel_end")>t||GunDataHelper.getMag(s,w)>=WeaponPerkRegistry.getEffectiveMagSize(s,w))return;
  if(w.usesSpecialAmmo&&GunDataHelper.getReserve(s,w)<=0){int ammo=FrontierAmmo.consume(p,w);if(ammo<=0)return;GunDataHelper.setInt(s,"d2_res",ammo);}
  int duration=WeaponPerkRegistry.getEffectiveReloadTicks(s,w,t);GunDataHelper.setInt(s,"d2_rel_mode",1);GunDataHelper.setInt(s,"d2_rel_dur",duration);GunDataHelper.setLong(s,"d2_rel_end",t+duration);
  ServerBallisticsEngine.playSound(p.level(),p.getX(),p.getY(),p.getZ(),"item.crossbow.loading_start",.7f,1.15f);
 }
 private static Vec3 spread(ServerPlayer p,ItemStack s,WeaponSpec w,byte ads,long t){
  float spread=WeaponPerkRegistry.getEffectiveSpread(s,w,ads,t);return Vec3.directionFromRotation(p.getXRot()+(p.getRandom().nextFloat()-.5f)*spread,p.getYRot()+(p.getRandom().nextFloat()-.5f)*spread);
 }
 private static FrontierProjectile launch(ServerLevel l,ServerPlayer p,ItemStack s,WeaponSpec w,Vec3 from,Vec3 dir,float speed,float damage,int element,int mode,float radius){return FrontierProjectile.launch(l,p,from,dir,speed,damage,element,mode,radius).weapon(s,w);}
 private static void fire(ServerPlayer p,ItemStack s,WeaponSpec w,byte ads,float strength,boolean perfect){
  ServerLevel l=p.serverLevel();long t=l.getGameTime();int mag=GunDataHelper.getMag(s,w);
  if(mag<=0||GunDataHelper.getLong(s,"frontier_next")>t)return;
  GunDataHelper.setInt(s,"d2_mag",mag-1);GunDataHelper.setLong(s,"frontier_next",t+(w.archetype==4||w.archetype==5?8:Math.max(2,(int)w.fireDelayTicks)));
  Vec3 from=p.getEyePosition(),dir=spread(p,s,w,ads,t);float damage=w.baseDamage*strength;
  boolean hit=false,killed=false;
  if(w.archetype==4&&w.index!=41){
   for(int i=0;i<7;i++){Vec3 d=spread(p,s,w,ads,t);
    if(w.index==40)launch(l,p,s,w,from,d,1.6f,damage,3,1,1.5f);
    else {int r=beam(p,s,w,from,d,damage,ads,false,mag>w.magSize/2);hit|=(r&1)!=0;killed|=(r&2)!=0;}
   }
   if(w.index==42)GunDataHelper.setInt(s,"frontier_momentum",killed?0:hit?Math.min(5,GunDataHelper.getInt(s,"frontier_momentum",0)+1):0);
  } else if(w.archetype==5||w.archetype==6){beam(p,s,w,from,dir,damage,ads,w.index==44,false);}
  else switch(w.index){
   case 41->launch(l,p,s,w,from,dir,.9f,damage,1,2,2.8f);
   case 48->launch(l,p,s,w,from,dir,1.1f,damage,1,3,3.5f);
   case 49->{Vec3 side=dir.cross(new Vec3(0,1,0)).normalize().scale(.14);launch(l,p,s,w,from.add(side),dir,1f,damage,3,4,3);launch(l,p,s,w,from.subtract(side),dir,.9f,damage,1,5,3);}
   case 50->launch(l,p,s,w,from,dir,1.05f,damage,3,6,1.3f);
   case 51->launch(l,p,s,w,from,dir,2.5f*strength,damage,3,perfect?7:0,0);
  }
  if(w.archetype!=6||t%6==0)ServerBallisticsEngine.playSound(l,p.getX(),p.getY(),p.getZ(),w.archetype==7?"entity.firework_rocket.launch":w.archetype==6?"block.beacon.ambient":"item.crossbow.shoot",.6f,w.archetype==6?1.8f:.8f);
  p.getInventory().setChanged();p.containerMenu.broadcastChanges();
 }
 private static int beam(ServerPlayer p,ItemStack s,WeaponSpec w,Vec3 from,Vec3 dir,float damage,byte ads,boolean pierce,boolean chill){
  ServerLevel l=p.serverLevel();long t=l.getGameTime();double range=WeaponPerkRegistry.getEffectiveRange(s,w,t);
  Vec3 end=l.clip(new ClipContext(from,from.add(dir.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
  record Hit(LivingEntity entity,Vec3 point,double distance){}
  List<Hit> hits=new ArrayList<>();
  for(LivingEntity e:l.getEntitiesOfClass(LivingEntity.class,new AABB(from,end).inflate(.25),v->CombatRules.canDamage(p,v))){e.getBoundingBox().inflate(.05).clip(from,end).ifPresent(v->hits.add(new Hit(e,v,from.distanceTo(v))));}
  hits.sort(Comparator.comparingDouble(Hit::distance));int result=0,n=0;
  for(Hit h:hits){if(n++>0&&!pierce)break;if(n>4)break;LivingEntity e=h.entity;boolean crit=h.point.y>e.getY()+e.getBbHeight()*.78;
   float d=damage*(crit?w.critMult:1)*WeaponPerkRegistry.computeSwappableDamageMult(s,w,e,crit?1:0,t);
   if(w.index==46){int prev=GunDataHelper.getInt(s,"frontier_target",-1);long last=GunDataHelper.getLong(s,"frontier_beam_last");int ramp=prev==e.getId()&&t-last<=5?Math.min(40,GunDataHelper.getInt(s,"frontier_ramp",0)+1):0;GunDataHelper.setInt(s,"frontier_target",e.getId());GunDataHelper.setInt(s,"frontier_ramp",ramp);GunDataHelper.setLong(s,"frontier_beam_last",t);d*=1+ramp*.025f;}
   e.invulnerableTime=0;boolean accepted=e.hurt(p.damageSources().playerAttack(p),d);if(!accepted)continue;
   result|=1;if(!e.isAlive())result|=2;
   if(w.index==43&&chill){e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,1),p);e.setTicksFrozen(Math.min(200,e.getTicksFrozen()+25));}
   if(w.index==47){e.igniteForSeconds(2);if(t%6==0)for(LivingEntity other:l.getEntitiesOfClass(LivingEntity.class,e.getBoundingBox().inflate(1.8),v->v!=e&&CombatRules.canDamage(p,v)&&e.hasLineOfSight(v)))other.hurt(p.damageSources().playerAttack(p),1.5f);}
   if(w.index==45&&crit){int c=GunDataHelper.getInt(s,"frontier_precision",0)+1;GunDataHelper.setInt(s,"frontier_precision",c%3);if(c>=3)GunDataHelper.setInt(s,"d2_mag",Math.min(WeaponPerkRegistry.getEffectiveMagSize(s,w),GunDataHelper.getMag(s,w)+1));}
   WeaponPerkRegistry.onSwappablePostHit(l,p,s,w,e,h.point,crit?1:0,e.isAlive()?0:1,t);
   PacketDistributor.sendToPlayer(p,new CombatFeedbackPayload(d,(byte)(crit?1:0),(float)h.point.x,(float)h.point.y,(float)h.point.z));
   if(!pierce)end=h.point;
  }
  ServerBallisticsEngine.spawnTracerBeam(l,from,end,w.element);return result;
 }
}
