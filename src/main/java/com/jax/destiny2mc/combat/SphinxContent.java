package com.jax.destiny2mc.combat;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class SphinxContent {
 // AutomaticEventSubscriber initializes this class while vanilla registries
 // are frozen. Construct intrusive registry entries only in their own event.
 // Keep the field descriptors used by the generated strike bytecode intact.
 public static EntityType<SphinxEntity> ENTITY;
 public static Item EGG;
 @SubscribeEvent public static void register(RegisterEvent e) {
  e.register(Registries.ENTITY_TYPE,h->{
   ENTITY=EntityType.Builder.of(SphinxEntity::new,MobCategory.MONSTER).sized(1.35f,2.9f).clientTrackingRange(10).build("destiny2mc:vex_sphinx");
   h.register(ResourceLocation.fromNamespaceAndPath("destiny2mc","vex_sphinx"),ENTITY);
  });
  e.register(Registries.ITEM,h->{
   // Resolve the entity lazily so the egg does not depend on registry order.
   EGG=new DeferredSpawnEggItem(()->ENTITY,0x9a804f,0xff471d,new Item.Properties());
   h.register(ResourceLocation.fromNamespaceAndPath("destiny2mc","vex_sphinx_spawn_egg"),EGG);
  });
 }
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY,SphinxEntity.createAttributes());}
 @SubscribeEvent public static void tab(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS||e.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey()))e.accept(EGG);}
}
