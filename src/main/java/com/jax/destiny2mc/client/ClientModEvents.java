package com.jax.destiny2mc.client;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

public class ClientModEvents {
   @SubscribeEvent
   public static void onRegisterKeys(RegisterKeyMappingsEvent var0) {
      KeyMapping var1 = ClientWeaponState.RELOAD_KEY = new KeyMapping("key.destiny2mc.reload", 82, "key.categories.destiny2mc");
      var0.register(var1);
      KeyMapping var2 = ClientWeaponState.SETTINGS_KEY = new KeyMapping("key.destiny2mc.hud_settings", 79, "key.categories.destiny2mc");
      var0.register(var2);
   }

   @SubscribeEvent
   public static void onRegisterLayers(RegisterLayerDefinitions var0) {
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[0], new FallenLayerSupplier(0));
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[1], new FallenLayerSupplier(1));
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[2], new FallenLayerSupplier(2));
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[3], new FallenLayerSupplier(3));
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[4], new FallenLayerSupplier(4));
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[5], new FallenLayerSupplier(5));
      var0.registerLayerDefinition(FallenHumanoidModel.LAYERS[6], new FallenLayerSupplier(6));
      var0.registerLayerDefinition(DevilWalkerModel.LAYER, new StrikeBossLayerSupplier(0));
      var0.registerLayerDefinition(SepiksServoModel.LAYER, new StrikeBossLayerSupplier(1));
      var0.registerLayerDefinition(VanokArcliteModel.LAYER, new StrikeBossLayerSupplier(2));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[0], new V200FactionLayerSupplier(0));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[1], new V200FactionLayerSupplier(1));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[2], new V200FactionLayerSupplier(2));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[3], new V200FactionLayerSupplier(3));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[4], new V200FactionLayerSupplier(4));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[5], new V200FactionLayerSupplier(5));
      var0.registerLayerDefinition(V200FactionModel.RIG_LAYERS[6], new V200FactionLayerSupplier(6));
   }

   @SubscribeEvent
   public static void onRegisterRenderers(RegisterRenderers var0) {
      var0.registerBlockEntityRenderer((BlockEntityType)ModRegistries.LOOT_CHEST_BE.get(), new ChestBlockEntityRendererProvider());
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_DREG.get(), new FallenEntityRendererProvider(0));
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_WRETCH.get(), new FallenEntityRendererProvider(1));
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_VANDAL_SNIPER.get(), new FallenEntityRendererProvider(2));
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_CAPTAIN_RIFLE.get(), new FallenEntityRendererProvider(3));
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_CAPTAIN_SCORCH.get(), new FallenEntityRendererProvider(4));
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_SHANK.get(), new FallenEntityRendererProvider(5));
      var0.registerEntityRenderer((EntityType)ModRegistries.FALLEN_BRUISER.get(), new FallenEntityRendererProvider(6));
      var0.registerEntityRenderer((EntityType)ModRegistries.SHOCK_BOLT_ENTITY.get(), new FallenEntityRendererProvider(10));
      var0.registerEntityRenderer((EntityType)ModRegistries.SHOCK_GRENADE_ENTITY.get(), new FallenEntityRendererProvider(11));
      var0.registerEntityRenderer((EntityType)ModRegistries.WIRE_BOLT_ENTITY.get(), new FallenEntityRendererProvider(12));
      var0.registerEntityRenderer((EntityType)ModRegistries.SHRAPNEL_PELLET_ENTITY.get(), new FallenEntityRendererProvider(13));
      var0.registerEntityRenderer((EntityType)ModRegistries.SCORCH_ROCKET_ENTITY.get(), new FallenEntityRendererProvider(14));
      var0.registerEntityRenderer((EntityType)ModRegistries.DEVIL_WALKER.get(), new StrikeBossRendererProvider(0));
      var0.registerEntityRenderer((EntityType)ModRegistries.SEPIKS_PRIME.get(), new StrikeBossRendererProvider(1));
      var0.registerEntityRenderer((EntityType)ModRegistries.BLAST_SERVITOR.get(), new StrikeBossRendererProvider(2));
      var0.registerEntityRenderer((EntityType)ModRegistries.VANOK_ARCLITE.get(), new StrikeBossRendererProvider(3));
      var0.registerEntityRenderer((EntityType)ModRegistries.EXPLODER_SHANK.get(), new V200FactionRendererProvider(16));
      var0.registerEntityRenderer((EntityType)ModRegistries.TRACER_SHANK.get(), new V200FactionRendererProvider(17));
      var0.registerEntityRenderer((EntityType)ModRegistries.HEAVY_SHANK.get(), new V200FactionRendererProvider(18));
      var0.registerEntityRenderer((EntityType)ModRegistries.DEVILS_SERVITOR.get(), new V200FactionRendererProvider(19));
      var0.registerEntityRenderer((EntityType)ModRegistries.ARCHON_PRIEST.get(), new V200FactionRendererProvider(20));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_THRALL.get(), new V200FactionRendererProvider(21));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_CURSED_THRALL.get(), new V200FactionRendererProvider(22));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_ACOLYTE.get(), new V200FactionRendererProvider(23));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_KNIGHT.get(), new V200FactionRendererProvider(24));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_WIZARD.get(), new V200FactionRendererProvider(25));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_OGRE.get(), new V200FactionRendererProvider(26));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_SHRIEKER.get(), new V200FactionRendererProvider(27));
      var0.registerEntityRenderer((EntityType)ModRegistries.LUCENT_KNIGHT.get(), new V200FactionRendererProvider(28));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_GHOST.get(), new V200FactionRendererProvider(29));
      var0.registerEntityRenderer((EntityType)ModRegistries.LUCENT_MOTH.get(), new V200FactionRendererProvider(30));
      var0.registerEntityRenderer((EntityType)ModRegistries.ORYX_BOSS.get(), new V200FactionRendererProvider(31));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_GOBLIN.get(), new V200FactionRendererProvider(32));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_HOBGOBLIN.get(), new V200FactionRendererProvider(33));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_HARPY.get(), new V200FactionRendererProvider(34));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_MINOTAUR.get(), new V200FactionRendererProvider(35));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_HYDRA.get(), new V200FactionRendererProvider(36));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_WYVERN.get(), new V200FactionRendererProvider(37));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_ORACLE.get(), new V200FactionRendererProvider(38));
      var0.registerEntityRenderer((EntityType)ModRegistries.PERSYS_BOSS.get(), new V200FactionRendererProvider(39));
      var0.registerEntityRenderer((EntityType)ModRegistries.ATHEON_BOSS.get(), new V200FactionRendererProvider(40));
      var0.registerEntityRenderer((EntityType)ModRegistries.PROTHEON_BOSS.get(), new V200FactionRendererProvider(41));
      var0.registerEntityRenderer((EntityType)ModRegistries.HIVE_SOULFIRE_BOLT_ENTITY.get(), new V200FactionRendererProvider(42));
      var0.registerEntityRenderer((EntityType)ModRegistries.VEX_SLAP_BOLT_ENTITY.get(), new V200FactionRendererProvider(43));
      var0.registerEntityRenderer((EntityType)ModRegistries.VOID_AXIOM_BOLT_ENTITY.get(), new V200FactionRendererProvider(44));
   }
}
