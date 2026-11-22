package com.jax.destiny2mc.atlas;

import com.jax.destiny2mc.frontier.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Four bounded encounters, entirely in the ordinary world. Every phase is entity-persistent. */
@EventBusSubscriber(modid="destiny2mc")
public final class DungeonEncounters {
 public static final int PREPARE=0,EYES=1,DAMAGE=2;
 public static boolean isDungeon(FrontierMob b){return b.unit.move().startsWith("dungeon_");}
 public static CompoundTag data(FrontierMob b){CompoundTag root=b.getPersistentData();if(!root.contains("AtlasDungeon"))root.put("AtlasDungeon",new CompoundTag());return root.getCompound("AtlasDungeon");}
 public static BlockPos center(FrontierMob b){CompoundTag d=data(b);return d.contains("Center")?BlockPos.of(d.getLong("Center")):b.blockPosition();}
 public static int phase(FrontierMob b){return data(b).getInt("Phase");}
 public static boolean start(ServerLevel l,ServerPlayer p,DungeonSites.Site site,BlockPos altar,BlockPos center){
  if(site==null||p.isSpectator())return false;
  long wait=DungeonMemory.get(l).readyAt(altar)-l.getGameTime();
  if(wait>0){tell(p,"This altar recharges in "+((wait+19)/20)+" seconds.");return false;}
  if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(center).inflate(64),e->e.isAlive()&&(e instanceof FrontierMob m&&m.unit.boss()||BossSigilItem.isEncounter(e))).isEmpty()){
   tell(p,"An active boss is nearby. Finish or reset that encounter first.");return false;
  }
  FrontierMob boss=FrontierContent.ENTITIES.get(site.boss()).create(l);if(boss==null)return false;
  boss.moveTo(center.getX()+.5,center.getY()+(boss.unit.flying()?2.2:.05),center.getZ()+.5);
  if(!l.getWorldBorder().isWithinBounds(center)||!l.noCollision(boss)||!l.getBlockState(center.below()).isSolidRender(l,center.below())){
   tell(p,"The arena center is obstructed. Clear the center before activating this altar.");return false;
  }
  int count=l.getEntitiesOfClass(Player.class,new AABB(center).inflate(32),v->!v.isSpectator()).size();
  double total=boss.getMaxHealth()*(1+Math.min(3,Math.max(0,count-1))*.45);
  boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(1024,total));boss.setHealth(boss.getMaxHealth());
  boss.getPersistentData().putFloat("frontier_damage_scale",(float)(total/boss.getMaxHealth()));
  boss.getPersistentData().putBoolean("frontier_encounter",true);boss.setPersistenceRequired();
  CompoundTag d=data(boss);d.putLong("Center",center.asLong());d.putLong("Altar",altar.asLong());d.putString("Site",site.id());
  var altarState=l.getBlockState(altar);if(altarState.hasProperty(DungeonAltarBlock.FACING))d.putString("Facing",altarState.getValue(DungeonAltarBlock.FACING).getName());
  if(!p.isCreative())boss.setTarget(p);
  if(!l.addFreshEntity(boss))return false;
  tell(p,site.name()+" awakened. Open the field guide for mechanics.");return true;
 }
 public static List<Player> players(FrontierMob b){
  List<Player> out=new ArrayList<>(b.level().getEntitiesOfClass(Player.class,new AABB(center(b)).inflate(40),p->p.isAlive()&&!p.isSpectator()&&!p.isCreative()));
  if(b.getTarget() instanceof Player p&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(center(b)))<1600&&!out.contains(p))out.add(p);
  return out;
 }
 private static void tell(Player p,String s){p.displayClientMessage(Component.literal(s),true);}
 private static void announce(FrontierMob b,String s){for(Player p:players(b))tell(p,s);}
 private static Vec3 point(FrontierMob b,double x,double z,double y){BlockPos c=center(b);double rx=x,rz=z;switch(data(b).getString("Facing")){case "north"->{rx=-x;rz=-z;}case "east"->{rx=z;rz=-x;}case "west"->{rx=-z;rz=x;}}return new Vec3(c.getX()+.5+rx,c.getY()+y,c.getZ()+.5+rz);}
 private static Vec3 seal(FrontierMob b,int i,int n){double a=i*Math.PI*2/n;return point(b,Math.sin(a)*10,Math.cos(a)*10,.08);}
 private static boolean near(Player p,Vec3 at,double radius){return Math.abs(p.getY()-at.y)<3&&p.position().multiply(1,0,1).distanceTo(at.multiply(1,0,1))<radius;}
 private static List<Entity> owned(FrontierMob b){return b.level().getEntitiesOfClass(Entity.class,new AABB(center(b)).inflate(48),e->e instanceof DungeonNode n&&b.getUUID().equals(n.owner)||e.getPersistentData().hasUUID("AtlasOwner")&&b.getUUID().equals(e.getPersistentData().getUUID("AtlasOwner")));}
 private static boolean roleAlive(FrontierMob b,String role){return owned(b).stream().anyMatch(e->e.isAlive()&&e.getPersistentData().getString("AtlasRole").equals(role));}
 private static boolean nodesAlive(FrontierMob b,int mode){return owned(b).stream().anyMatch(e->e instanceof DungeonNode n&&n.isAlive()&&n.mode()==mode);}
 public static void cleanup(FrontierMob b){for(Entity e:owned(b))e.discard();}
 private static Mob helper(FrontierMob b,String id,Vec3 p,String role,int index,String label){
  Entity e=BuiltInRegistries.ENTITY_TYPE.get(FrontierContent.id(id)).create(b.level());if(!(e instanceof Mob m))return null;
  m.moveTo(p.x,p.y,p.z);if(!b.level().noCollision(m))return null;
  m.setPersistenceRequired();m.setTarget(b.getTarget());m.setCustomName(Component.literal(label));m.setCustomNameVisible(true);
  m.getPersistentData().putUUID("AtlasOwner",b.getUUID());m.getPersistentData().putString("AtlasRole",role);m.getPersistentData().putInt("AtlasIndex",index);
  b.level().addFreshEntity(m);return m;
 }
 public static DungeonNode node(FrontierMob b,Vec3 p,int mode,int index,String label){
  DungeonNode n=AtlasContent.NODE.create(b.level());if(n==null)return null;n.owner=b.getUUID();n.index=index;n.mode(mode);n.moveTo(p.x,p.y,p.z);n.setCustomName(Component.literal(label));n.setCustomNameVisible(mode!=3);
  b.level().addFreshEntity(n);return n;
 }
 public static void setup(FrontierMob b){
  cleanup(b);CompoundTag d=data(b);if(!d.contains("Center"))d.putLong("Center",b.blockPosition().asLong());
  d.putBoolean("Started",true);d.putInt("Phase",PREPARE);d.putInt("Ticks",0);d.putInt("Killed",0);d.putInt("Deposited",0);d.putInt("MarkTime",0);d.putInt("Stand",0);d.putInt("Eyes",0);
  for(int i=0;i<4;i++)d.putInt("Lane"+i,0);
  switch(b.unit.id()){
   case "vorgeth"->{for(int i=0;i<4;i++)helper(b,"taken_acolyte",seal(b,i,4),"keeper",i,"Keeper of Petitions "+(i+1));announce(b,"Defeat four Keepers, then cleanse the violet basin. Marks last 45s.");}
   case "zulmak"->{for(int i=0;i<3;i++)helper(b,"hive_knight",seal(b,i,3),"heretic",i,"Heretical Keeper "+(i+1));announce(b,"Defeat each Keeper, then step into its outer seal. Three seals unlock damage.");}
   case "phryzhia"->{receivers(b);announce(b,"Break the side receivers. Collect cursed engrams and deposit 12 at the crystal.");}
   case "akelous"->{for(int i=0;i<8;i++){int lane=i/2;double a=lane*Math.PI/2;double r=i%2==0?5:11;node(b,point(b,Math.sin(a)*r,Math.cos(a)*r,1.3),2,i,"Circuit "+(lane+1)+" / "+(i%2+1));}conduit(b);announce(b,"Defeat the Conduit Minotaur for Arctrician. Shoot each circuit from 1 to 2.");}
  }
 }
 private static void receivers(FrontierMob b){for(int i=0;i<2;i++)node(b,point(b,i==0?-11:11,0,1.4),1,i,"Scorch receiver "+(i+1));}
 private static void conduit(FrontierMob b){helper(b,"vex_minotaur",point(b,7,7,.05),"conduit",0,"Conduit Minotaur");}
 public static void tick(FrontierMob b,ServerLevel l){
  CompoundTag d=data(b);if(!d.contains("Center"))d.putLong("Center",b.blockPosition().asLong());
  List<Player> players=players(b);
  if(players.isEmpty()){
   b.setTarget(null);b.setAggressive(false);b.getNavigation().stop();b.setDeltaMovement(0,b.unit.flying()?0:b.getDeltaMovement().y,0);
   int idle=d.getInt("Idle")+1;d.putInt("Idle",idle);
   if(idle==200){cleanup(b);d.putBoolean("Started",false);d.putInt("Phase",PREPARE);b.setHealth(b.getMaxHealth());b.teleportTo(center(b).getX()+.5,center(b).getY()+(b.unit.flying()?2.2:.05),center(b).getZ()+.5);}
   b.encounterTitle(idle>=200?"RESET - approach to begin":"WAITING FOR GUARDIANS");return;
  }
  d.putInt("Idle",0);Player target=players.stream().min(Comparator.comparingDouble(b::distanceToSqr)).orElseThrow();b.setTarget(target);b.setAggressive(true);
  if(!d.getBoolean("Started"))setup(b);
  int age=d.getInt("Ticks")+1;d.putInt("Ticks",age);
  if(b.distanceToSqr(Vec3.atCenterOf(center(b)))>24*24||Math.abs(b.getY()-center(b).getY())>16)b.teleportTo(center(b).getX()+.5,center(b).getY()+(b.unit.flying()?2.2:.05),center(b).getZ()+.5);
  b.getLookControl().setLookAt(target,30,30);b.setYRot((float)Math.toDegrees(Math.atan2(target.getZ()-b.getZ(),target.getX()-b.getX()))-90);b.yBodyRot=b.getYRot();
  b.getNavigation().stop();if(b.unit.flying())b.setDeltaMovement(0,Math.sin(age*.05)*.015,0);
  if(phase(b)==DAMAGE&&b.unit.id().equals("zulmak")&&age%10==0&&b.distanceTo(target)>3&&near(target,point(b,0,0,0),12))b.getNavigation().moveTo(target,1.1);
  if(b.moveAge()>38)b.animate(FrontierMob.IDLE,0);
  if(age%60==0)b.animate(FrontierMob.FIRE,30);
  if((age%60==12||age%60==20)&&b.hasLineOfSight(target)){
   Vec3 from=b.getEyePosition();int mode=b.unit.id().equals("vorgeth")&&phase(b)==DAMAGE?11:0;
   var shot=FrontierProjectile.launch(l,b,from,target.getEyePosition().subtract(from).normalize(),.7f,b.unit.damage()*.55f,b.unit.element(),mode,0);
  }
  if(age%100==70&&b.distanceTo(target)<5){b.animate(FrontierMob.SLAM,38);d.putDouble("MarkX",target.getX());d.putDouble("MarkZ",target.getZ());}
  if(age%100>=70&&age%100<96&&age%4==0)FrontierMob.ring(l,new Vec3(d.getDouble("MarkX"),center(b).getY(),d.getDouble("MarkZ")),3,b.unit.element());
  if(age%100==96&&d.contains("MarkX")){b.blast(l,new Vec3(d.getDouble("MarkX"),center(b).getY(),d.getDouble("MarkZ")),3,b.unit.damage(),.65);d.remove("MarkX");}
  if(phase(b)==DAMAGE){damageTick(b,l,players);return;}
  switch(b.unit.id()){
   case "vorgeth"->{int marks=Integer.bitCount(d.getInt("Killed"));int time=d.getInt("MarkTime");if(time>0)d.putInt("MarkTime",--time);b.encounterTitle("PETITIONS "+marks+"/4"+(marks>0?" | "+((time+19)/20)+"s":""));
    Vec3 basin=point(b,0,-7,.08);if(marks==4){if(age%8==0)FrontierMob.ring(l,basin,2,3);int stand=players.stream().anyMatch(p->near(p,basin,2))?d.getInt("Stand")+1:0;d.putInt("Stand",stand);if(stand>=20)beginDamage(b,400);}
    if(marks>0&&time<=0){announce(b,"Petitioner's Mark expired. The court resets.");b.blast(l,point(b,0,0,0),16,8,0);setup(b);}
    if(age%120==0)recoverKeepers(b,"taken_acolyte","keeper",4,"Keeper of Petitions");
   }
   case "zulmak"->{int mask=d.getInt("Deposited"),killed=d.getInt("Killed");for(int i=0;i<3;i++)if((killed&(1<<i))!=0&&(mask&(1<<i))==0){Vec3 s=seal(b,i,3);if(age%8==0)FrontierMob.ring(l,s,2,5);if(players.stream().anyMatch(p->near(p,s,2))){mask|=1<<i;d.putInt("Deposited",mask);announce(b,"Seal extinguished: "+Integer.bitCount(mask)+" / 3");}}
    b.encounterTitle("SEALS "+Integer.bitCount(mask)+"/3");if(mask==7)beginDamage(b,360);
    if(age%120==0)recoverKeepers(b,"hive_knight","heretic",3,"Heretical Keeper");
   }
   case "phryzhia"->{Vec3 crystal=point(b,0,-7,.08);if(age%8==0)FrontierMob.ring(l,crystal,2,1);int bank=d.getInt("Deposited");
    for(Player p:players){CompoundTag pt=p.getPersistentData();if(!pt.hasUUID("AtlasRichOwner")||!pt.getUUID("AtlasRichOwner").equals(b.getUUID()))continue;int carried=pt.getInt("AtlasRiches");if(carried<=0)continue;
     long remaining=pt.getLong("AtlasRichUntil")-l.getGameTime();if(remaining<=0){pt.putInt("AtlasRiches",0);p.hurt(b.damageSources().mobAttack(b),6);tell(p,"Burdened by Riches expired. Return to the receivers.");continue;}
     if(age%10==0&&near(p,crystal,2)){pt.putInt("AtlasRiches",carried-1);bank++;d.putInt("Deposited",bank);}if(age%20==0)tell(p,"Burdened by Riches x"+pt.getInt("AtlasRiches")+" | "+((remaining+19)/20)+"s | Crystal "+bank+"/12");
    }
    b.encounterTitle("CRYSTAL "+Math.min(12,bank)+"/12");if(bank>=12)beginDamage(b,400);
    else if(age%100==0&&!nodesAlive(b,1)&&!nodesAlive(b,3)&&!roleAlive(b,"rich"))receivers(b);
   }
   case "akelous"->{if(phase(b)==PREPARE){int powered=0;for(int i=0;i<4;i++)if(d.getInt("Lane"+i)>=2)powered++;b.encounterTitle("CIRCUITS "+powered+"/4");if(powered==4){cleanup(b);d.putInt("Phase",EYES);d.putInt("EyeTime",300);for(int i=0;i<6;i++)node(b,eye(b,i),4,i,"Exposed eye "+(i+1));announce(b,"Six eyes exposed! Destroy all six within 15 seconds.");}else if(age%160==0&&!roleAlive(b,"conduit"))conduit(b);}
    else {int t=d.getInt("EyeTime")-1;d.putInt("EyeTime",t);b.encounterTitle("EYES "+Integer.bitCount(d.getInt("Eyes"))+"/6 | "+((t+19)/20)+"s");for(Entity e:owned(b))if(e instanceof DungeonNode n&&n.mode()==4)n.setPos(eye(b,n.index));if(d.getInt("Eyes")==63)beginDamage(b,300);else if(t<=0){repulse(b,l);setup(b);}}
   }
  }
 }
 private static void recoverKeepers(FrontierMob b,String entity,String role,int count,String label){
  int killed=data(b).getInt("Killed");for(int i=0;i<count;i++){final int index=i;if((killed&(1<<i))==0&&owned(b).stream().noneMatch(e->e.isAlive()&&role.equals(e.getPersistentData().getString("AtlasRole"))&&e.getPersistentData().getInt("AtlasIndex")==index))helper(b,entity,seal(b,i,count),role,i,label+" "+(i+1));}
 }
 private static Vec3 eye(FrontierMob b,int i){double a=i*Math.PI/3;return b.position().add(Math.cos(a)*2.3,1.8+Math.sin(a)*1.2,-1.8);}
 public static void beginDamage(FrontierMob b,int ticks){cleanup(b);CompoundTag d=data(b);d.putInt("Phase",DAMAGE);d.putInt("DamageTime",ticks);b.animate(FrontierMob.STAGGER,35);announce(b,"SHIELD DOWN - "+(ticks/20)+" seconds"+(b.unit.id().equals("zulmak")?". Deal damage from inside the ritual ring.":". Aim at the exposed core."));}
 private static void damageTick(FrontierMob b,ServerLevel l,List<Player> players){
  CompoundTag d=data(b);int t=d.getInt("DamageTime")-1;d.putInt("DamageTime",t);b.encounterTitle((t<=60?"RETREAT - ":"DAMAGE - ")+((Math.max(0,t)+19)/20)+"s");
  if(b.unit.id().equals("zulmak")){
   if(t%8==0)FrontierMob.ring(l,point(b,0,0,.05),8,t<=60?1:5);
   if(t==260||t==160)for(int i=0;i<2;i++)helper(b,"hive_cursed_thrall",point(b,i==0?-9:9,8,.05),"adds",i,"Accursed Thrall");
   if(t==60)announce(b,"Zulmak raises his blade. Leave the eight-block ring!");
   if(t==0)b.blast(l,point(b,0,0,0),8,14,.8);
  }
  if(b.unit.id().equals("akelous")){Vec3 dst=point(b,0,0,2.4);b.setDeltaMovement(dst.subtract(b.position()).scale(.02));if(t==60)announce(b,"Repulsion charging. Retreat beyond 13 blocks or use solid cover.");if(t==0)repulse(b,l);}
  if(t<=0)setup(b);
 }
 private static void repulse(FrontierMob b,ServerLevel l){b.animate(FrontierMob.CAST,35);b.blast(l,b.position(),13,7,1.4);}
 public static boolean allowDamage(FrontierMob b,DamageSource source){
  if(!isDungeon(b)||source.is(DamageTypes.GENERIC_KILL))return true;
  boolean allowed=phase(b)==DAMAGE;
  if(allowed&&b.unit.id().equals("zulmak"))allowed=source.getEntity() instanceof Player p&&near(p,point(b,0,0,0),8);
  if(!allowed&&source.getEntity() instanceof Player p&&b.tickCount%10==0)tell(p,phase(b)==DAMAGE?"Stand inside Zulmak's ritual ring to deal damage.":"IMMUNE - follow the mechanic shown on the boss bar / Atlas.");
  return allowed;
 }
 public static boolean canHit(DungeonNode n,DamageSource source){
  if(!(n.level() instanceof ServerLevel l)||!(source.getEntity() instanceof Player p)||n.owner==null||!(l.getEntity(n.owner) instanceof FrontierMob b)||!b.isAlive())return false;
  if(n.mode()==3)return false;
  if(n.mode()==1)return phase(b)==PREPARE;
  if(n.mode()==4)return phase(b)==EYES;
  if(n.mode()==2){CompoundTag t=p.getPersistentData();if(phase(b)!=PREPARE)return false;if(!t.hasUUID("AtlasArcOwner")||!t.getUUID("AtlasArcOwner").equals(b.getUUID())||t.getLong("AtlasArcUntil")<=l.getGameTime()){tell(p,"Defeat a Conduit Minotaur to gain 30 seconds of Arctrician.");return false;}if(data(b).getInt("Lane"+(n.index/2))!=n.index%2){tell(p,"Circuit order: shoot terminal 1, then terminal 2.");return false;}return true;}
  return false;
 }
 public static void nodeBroken(DungeonNode n,DamageSource source){
  if(!(n.level() instanceof ServerLevel l)||n.owner==null||!(l.getEntity(n.owner) instanceof FrontierMob b)||!b.isAlive())return;CompoundTag d=data(b);
  if(n.mode()==1&&phase(b)==PREPARE){for(int j=0;j<3;j++)helper(b,"hive_acolyte",point(b,n.index==0?-9:9,-4+j*4,.05),"rich",n.index*3+j,"Avarice hoarder");}
  if(n.mode()==2&&phase(b)==PREPARE){d.putInt("Lane"+(n.index/2),n.index%2+1);announce(b,"Circuit "+(n.index/2+1)+": "+(n.index%2+1)+" / 2");}
  if(n.mode()==4&&phase(b)==EYES)d.putInt("Eyes",d.getInt("Eyes")|(1<<n.index));
 }
 public static void collect(DungeonNode n,FrontierMob b){if(phase(b)!=PREPARE){n.discard();return;}for(Player p:players(b))if(p.distanceToSqr(n)<2.8){CompoundTag t=p.getPersistentData();int prior=t.hasUUID("AtlasRichOwner")&&t.getUUID("AtlasRichOwner").equals(b.getUUID())?t.getInt("AtlasRiches"):0;t.putUUID("AtlasRichOwner",b.getUUID());t.putInt("AtlasRiches",Math.min(24,prior+1));t.putLong("AtlasRichUntil",p.level().getGameTime()+600);tell(p,"Burdened by Riches x"+(prior+1)+" - deposit at the amber crystal within 30s.");n.discard();break;}}
 @SubscribeEvent public static void helperDeath(LivingDeathEvent e){
  LivingEntity mob=e.getEntity();if(!(mob.level() instanceof ServerLevel l))return;CompoundTag t=mob.getPersistentData();if(!t.hasUUID("AtlasOwner")||t.getBoolean("AtlasCredited")||!(l.getEntity(t.getUUID("AtlasOwner")) instanceof FrontierMob b)||!b.isAlive())return;
  t.putBoolean("AtlasCredited",true);CompoundTag d=data(b);String role=t.getString("AtlasRole");int i=t.getInt("AtlasIndex");
  if((role.equals("keeper")||role.equals("heretic"))&&phase(b)==PREPARE){d.putInt("Killed",d.getInt("Killed")|(1<<i));if(role.equals("keeper"))d.putInt("MarkTime",900);announce(b,role.equals("keeper")?"Petitioner's Mark x"+Integer.bitCount(d.getInt("Killed")):"Keeper defeated - claim its glowing outer seal.");}
  if(role.equals("rich")&&phase(b)==PREPARE)for(int j=0;j<2;j++)node(b,mob.position().add(j==0?-.4:.4,.6,0),3,i*2+j,"Cursed engram");
  if(role.equals("conduit")&&phase(b)==PREPARE)for(Player p:players(b)){p.getPersistentData().putUUID("AtlasArcOwner",b.getUUID());p.getPersistentData().putLong("AtlasArcUntil",l.getGameTime()+600);tell(p,"ARCTRICIAN - 30 seconds. Shoot terminal 1 then 2 on all four circuits.");}
 }
 public static void complete(FrontierMob b){if(!isDungeon(b)||!(b.level() instanceof ServerLevel l))return;cleanup(b);CompoundTag d=data(b);if(d.contains("Altar"))DungeonMemory.get(l).complete(BlockPos.of(d.getLong("Altar")),l.getGameTime());announce(b,"Dungeon complete. Three Conquest Tokens and an Exotic Engram earned. Altar recharges in 5 minutes.");}
}
