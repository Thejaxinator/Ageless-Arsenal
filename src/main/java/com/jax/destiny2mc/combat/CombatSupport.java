package com.jax.destiny2mc.combat;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Combat utilities have no mission, instance, inventory-snapshot or chunk-ticket state. */
@EventBusSubscriber(modid="destiny2mc")
public final class CombatSupport {
 public static void safeEntityTeleport(Entity e,double x,double y,double z){
  BlockPos p=BlockPos.containing(x,y,z);Level l=e.level();
  if(!l.hasChunkAt(p)||!l.getWorldBorder().isWithinBounds(p)||y<l.getMinBuildHeight()+1||y>=l.getMaxBuildHeight()-e.getBbHeight())return;
  if(l.getBlockState(p.below()).isAir()||!l.noCollision(e,e.getBoundingBox().move(x-e.getX(),y-e.getY(),z-e.getZ())))return;
  e.teleportTo(x,y,z);e.setDeltaMovement(Vec3.ZERO);e.fallDistance=0;
 }
 public static void spawnLucentHiveGhost(ServerLevel l,LivingEntity knight){
  Entity e=((EntityType<?>)ModRegistries.HIVE_GHOST.get()).create(l);
  if(e!=null){e.moveTo(knight.getX(),knight.getY()+1.1,knight.getZ());l.addFreshEntity(e);}
 }
 public static void completeHiveGhostRevive(ServerLevel l,Entity ghost){
  Entity e=((EntityType<?>)ModRegistries.LUCENT_KNIGHT.get()).create(l);
  if(e instanceof LivingEntity knight){knight.moveTo(ghost.getX(),ghost.getY()-.8,ghost.getZ());knight.setHealth(Math.min(60,knight.getMaxHealth()));l.addFreshEntity(knight);}
 }
 public static void onAtheonOracleCycle(ServerLevel l,LivingEntity atheon){
  // An open-world damage window. No player teleport or dependency on a mission.
  for(ServerPlayer p:l.players())if(p.distanceTo(atheon)<28){p.getPersistentData().putLong("d2_fate_until",l.getGameTime()+160);p.displayClientMessage(Component.literal("Guardians make their own fate — 8 seconds of Time's Vengeance"),true);}
 }
 public static boolean hasTimesVengeance(Player p){return p.getPersistentData().getLong("d2_fate_until")>p.level().getGameTime();}
 public static void onPuzzleBlockShot(ServerLevel l,ServerPlayer p,int x,int y,int z){} // Old ballistics ABI only.
 @SubscribeEvent public static void migrate(PlayerEvent.PlayerLoggedInEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p)||!p.level().dimension().location().toString().equals("destiny2mc:strike_void"))return;
  CompoundTag tag=p.getPersistentData();ServerLevel target=p.server.overworld();
  ResourceLocation saved=ResourceLocation.tryParse(tag.getString("d2_ret_dim"));
  if(saved!=null&&!saved.toString().equals("destiny2mc:strike_void")){ServerLevel candidate=p.server.getLevel(ResourceKey.create(Registries.DIMENSION,saved));if(candidate!=null)target=candidate;}
  BlockPos spawn=target.getSharedSpawnPos();double x=spawn.getX()+.5,y=spawn.getY()+1,z=spawn.getZ()+.5;
  if(tag.contains("d2_ret_y")&&tag.getDouble("d2_ret_y")>target.getMinBuildHeight()+1){x=tag.getDouble("d2_ret_x");y=tag.getDouble("d2_ret_y");z=tag.getDouble("d2_ret_z");}
  p.teleportTo(target,x,y,z,tag.getFloat("d2_ret_yaw"),0);tag.remove("d2_in_strike");p.fallDistance=0;
  p.displayClientMessage(Component.literal("Returning to the Overworld."),false);
 }
}
