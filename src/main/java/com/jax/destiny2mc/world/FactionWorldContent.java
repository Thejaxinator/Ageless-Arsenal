package com.jax.destiny2mc.world;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Registers worldgen blocks/items (Fake Radiolaria).
 * Natural spawn placements for legacy Fallen, Hive, and Vex mobs are managed by
 * {@link com.jax.destiny2mc.frontier.FactionSpawnPlacements}.
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
}
