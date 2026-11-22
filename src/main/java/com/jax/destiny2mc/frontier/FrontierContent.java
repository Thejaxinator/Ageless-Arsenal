package com.jax.destiny2mc.frontier;

import com.jax.destiny2mc.ModRegistries;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class FrontierContent {
 public static final Map<String,EntityType<FrontierMob>> ENTITIES=new LinkedHashMap<>();
 public static final Map<String,Item> ITEMS=new LinkedHashMap<>();
 public static EntityType<FrontierProjectile> BOLT;
 public static EntityType<FrontierHazard> HAZARD;
 public static final String[] LEGACY_BOSSES={"sepiks_prime","protheon_boss","oryx_boss","atheon_boss"};
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("destiny2mc",s);}
 public static Item item(String s){return ITEMS.get(s);}
 @SubscribeEvent public static void register(RegisterEvent e){
  e.register(Registries.ENTITY_TYPE,h->{
   for(var u:FrontierCatalog.UNITS){EntityType<FrontierMob> t=EntityType.Builder.<FrontierMob>of((type,l)->new FrontierMob(type,l,u),MobCategory.MONSTER).sized(u.width(),u.height()).clientTrackingRange(u.boss()?12:8).build(id(u.id()).toString());ENTITIES.put(u.id(),t);h.register(id(u.id()),t);}
   BOLT=EntityType.Builder.<FrontierProjectile>of(FrontierProjectile::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(10).updateInterval(1).build(id("frontier_bolt").toString());h.register(id("frontier_bolt"),BOLT);
   HAZARD=EntityType.Builder.<FrontierHazard>of(FrontierHazard::new,MobCategory.MISC).sized(.1f,.1f).clientTrackingRange(10).updateInterval(5).build(id("frontier_hazard").toString());h.register(id("frontier_hazard"),HAZARD);
  });
  e.register(Registries.ITEM,h->{
   for(String name:List.of("cabal_plating","taken_residue","dark_ether","resonant_shard","conquest_token"))ITEMS.put(name,new Item(new Item.Properties().rarity(name.equals("conquest_token")?Rarity.RARE:Rarity.COMMON)));
   for(var u:FrontierCatalog.UNITS){
    int shell=switch(u.faction()){case "cabal"->0x8e302e;case "scorn"->0x9b793e;case "taken"->0x131827;case "hive"->0x585c38;case "fallen"->0x814c3c;case "vex"->0x9c8655;case "corpus"->0x2b6273;default->0x49394c;};
    ITEMS.put(u.id()+"_spawn_egg",new DeferredSpawnEggItem(()->ENTITIES.get(u.id()),shell,u.boss()?0xf2c36b:0xb8d5df,new Item.Properties()));
    if(u.boss()&&!u.move().startsWith("dungeon_"))ITEMS.put("summon_"+u.id(),new BossSigilItem(u.id(),u.name(),u.raid()));
   }
   for(String b:LEGACY_BOSSES)ITEMS.put("summon_"+b,new BossSigilItem(b,BossSigilItem.legacyName(b),b.equals("oryx_boss")||b.equals("atheon_boss")));
   ITEMS.put("frontier_bolt",new Item(new Item.Properties()));
   ITEMS.forEach((n,i)->h.register(id(n),i));
  });
 }
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){for(var u:FrontierCatalog.UNITS)e.put(ENTITIES.get(u.id()),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,u.health()).add(Attributes.MOVEMENT_SPEED,u.speed()).add(Attributes.ATTACK_DAMAGE,u.damage()).add(Attributes.FOLLOW_RANGE,u.boss()?48:32).add(Attributes.KNOCKBACK_RESISTANCE,u.boss()?.8:.15).add(Attributes.ARMOR,u.faction().equals("cabal")?5:u.boss()?4:1).build());}
 @SubscribeEvent public static void spawns(RegisterSpawnPlacementsEvent e){for(var u:FrontierCatalog.UNITS)if(!u.boss()&&!u.id().equals("dread_geist"))e.register(ENTITIES.get(u.id()),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Monster::checkMonsterSpawnRules,RegisterSpawnPlacementsEvent.Operation.REPLACE);}
 @SubscribeEvent public static void tab(BuildCreativeModeTabContentsEvent e){if(e.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey()))ITEMS.forEach((n,i)->{if(!n.equals("frontier_bolt"))e.accept(i);});}
 public static Item materialItem(String faction){return switch(faction){case "fallen"->(Item)com.jax.destiny2mc.ModRegistries.PLUNDERED_ETHER_ALLOY.get();case "hive"->(Item)com.jax.destiny2mc.ModRegistries.HIVE_CHITIN_FRAGMENT.get();case "vex"->(Item)com.jax.destiny2mc.ModRegistries.VEX_RADIOLARIAN_CORE.get();default->item(material(faction));};}
 public static String material(String faction){return switch(faction){case "cabal"->"cabal_plating";case "taken"->"taken_residue";case "scorn"->"dark_ether";default->"resonant_shard";};}
}
