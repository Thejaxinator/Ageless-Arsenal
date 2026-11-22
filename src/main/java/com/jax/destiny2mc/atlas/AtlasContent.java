package com.jax.destiny2mc.atlas;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.frontier.FrontierContent;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class AtlasContent {
 public static final Map<String,Block> ALTARS=new LinkedHashMap<>();
 public static final Map<String,Item> ITEMS=new LinkedHashMap<>();
 public static EntityType<DungeonNode> NODE;
 @SubscribeEvent public static void register(RegisterEvent e){
  e.register(Registries.BLOCK,h->{for(var site:DungeonSites.ALL){Block block=new DungeonAltarBlock(site.id());ALTARS.put(site.id(),block);h.register(FrontierContent.id(site.id()+"_altar"),block);}});
  e.register(Registries.ITEM,h->{for(var site:DungeonSites.ALL){Item item=new BlockItem(ALTARS.get(site.id()),new Item.Properties().rarity(Rarity.RARE));ITEMS.put(site.id(),item);h.register(FrontierContent.id(site.id()+"_altar"),item);}});
  e.register(Registries.ENTITY_TYPE,h->{NODE=EntityType.Builder.<DungeonNode>of(DungeonNode::new,MobCategory.MISC).sized(.75f,.75f).clientTrackingRange(10).updateInterval(2).build("destiny2mc:dungeon_node");h.register(FrontierContent.id("dungeon_node"),NODE);});
 }
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(NODE,Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,8).add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).build());}
 @SubscribeEvent public static void tab(BuildCreativeModeTabContentsEvent e){if(e.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey()))ITEMS.values().forEach(e::accept);}
}
