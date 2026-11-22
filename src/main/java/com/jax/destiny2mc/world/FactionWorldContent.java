package com.jax.destiny2mc.world;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Registers worldgen blocks/items (Fake Radiolaria) and ensures legacy Fallen, Hive, and Vex
 * mobs have valid monster spawn placements so they spawn natively in the four faction biomes.
 */
@EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
public final class FactionWorldContent {
    public static Block FAKE_RADIOLARIA;
    public static Item FAKE_RADIOLARIA_ITEM;

    private FactionWorldContent() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", path);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            FAKE_RADIOLARIA = new FakeRadiolariaBlock();
            helper.register(id("fake_radiolaria"), FAKE_RADIOLARIA);
        });
        event.register(Registries.ITEM, helper -> {
            FAKE_RADIOLARIA_ITEM = new BlockItem(FAKE_RADIOLARIA, new Item.Properties());
            helper.register(id("fake_radiolaria"), FAKE_RADIOLARIA_ITEM);
        });
    }

    @SubscribeEvent
    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (FAKE_RADIOLARIA_ITEM != null && event.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey())) {
            event.accept(FAKE_RADIOLARIA_ITEM);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_DREG.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_WRETCH.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_VANDAL_SNIPER.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_CAPTAIN_RIFLE.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_CAPTAIN_SCORCH.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_SHANK.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.FALLEN_BRUISER.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.EXPLODER_SHANK.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.TRACER_SHANK.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HEAVY_SHANK.get());

        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HIVE_THRALL.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HIVE_CURSED_THRALL.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HIVE_ACOLYTE.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HIVE_KNIGHT.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HIVE_WIZARD.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.HIVE_OGRE.get());

        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.VEX_GOBLIN.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.VEX_HOBGOBLIN.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.VEX_HARPY.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.VEX_MINOTAUR.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.VEX_HYDRA.get());
        registerMonster(event, (EntityType<? extends Monster>) ModRegistries.VEX_WYVERN.get());
    }

    private static <T extends Monster> void registerMonster(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
        event.register(
            type,
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR
        );
    }
}
