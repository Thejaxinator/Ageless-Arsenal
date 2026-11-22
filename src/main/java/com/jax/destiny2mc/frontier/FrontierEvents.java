package com.jax.destiny2mc.frontier;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
/** Boss bars for inherited bosses now work without a strike instance. */
@EventBusSubscriber(modid="destiny2mc")
public final class FrontierEvents {
 private static final Map<UUID,ServerBossEvent> bars=new HashMap<>();
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){
  if(e.getServer().getTickCount()%10!=0)return;Set<UUID> seen=new HashSet<>();
  for(ServerLevel l:e.getServer().getAllLevels())for(Entity entity:l.getAllEntities()){
   if(!(entity instanceof LivingEntity mob)||mob instanceof FrontierMob||!mob.isAlive()||!BossSigilItem.isEncounter(mob))continue;
   UUID id=mob.getUUID();seen.add(id);ServerBossEvent bar=bars.computeIfAbsent(id,k->new ServerBossEvent(mob.getName(),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS));bar.setProgress(Math.max(0,Math.min(1,mob.getHealth()/mob.getMaxHealth())));
   for(ServerPlayer p:l.players())if(p.distanceTo(mob)<64)bar.addPlayer(p);else bar.removePlayer(p);
   for(ServerPlayer p:new ArrayList<>(bar.getPlayers()))if(p.level()!=l||p.isRemoved())bar.removePlayer(p);
  }
  bars.entrySet().removeIf(x->{if(seen.contains(x.getKey()))return false;x.getValue().removeAllPlayers();return true;});
 }
 @SubscribeEvent public static void death(LivingDeathEvent e){LivingEntity mob=e.getEntity();if(mob.level().isClientSide||mob instanceof FrontierMob||!BossSigilItem.isEncounter(mob))return;String id=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();mob.spawnAtLocation(new ItemStack(FrontierContent.item("conquest_token"),id.equals("oryx_boss")||id.equals("atheon_boss")?4:2));}
 @SubscribeEvent public static void scaling(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){float scale=e.getEntity().getPersistentData().getFloat("frontier_damage_scale");if(scale>1)e.setAmount(e.getAmount()/scale);}
 @SubscribeEvent public static void stopped(ServerStoppedEvent e){for(var b:bars.values())b.removeAllPlayers();bars.clear();}
}
