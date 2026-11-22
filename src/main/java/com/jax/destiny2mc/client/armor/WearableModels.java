package com.jax.destiny2mc.client.armor;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Armor stays in the native armor layer so the parent model supplies its pose,
 * equipment visibility, light, enchantment glint and armor trim render passes. */
@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class WearableModels {
    private static final Map<String, HumanoidModel<?>> baked = new HashMap<>();

    private static ModelLayerLocation layer(String item) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("destiny2mc","armor/" + item),"main");
    }

    @SubscribeEvent public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (String item : ArmorGeometry.ITEMS) {
            event.registerLayerDefinition(layer(item), () -> ArmorGeometry.layer(item));
        }
    }

    @SubscribeEvent public static void onBaked(EntityRenderersEvent.AddLayers event) {
        baked.clear();
    }

    @SubscribeEvent public static void attach(RegisterClientExtensionsEvent event) {
        for (String id : ArmorGeometry.ITEMS) {
            var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("destiny2mc",id)).orElseThrow();
            event.registerItem(new IClientItemExtensions() {
                @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack,
                        EquipmentSlot slot, HumanoidModel<?> original) {
                    return baked.computeIfAbsent(id, key -> new HumanoidModel<>(
                        Minecraft.getInstance().getEntityModels().bakeLayer(layer(key))));
                }
            }, item);
        }
    }

    private WearableModels() {}
}
