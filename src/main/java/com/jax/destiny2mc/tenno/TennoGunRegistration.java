package com.jax.destiny2mc.tenno;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class TennoGunRegistration {
    public static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("destiny2mc", name); }
    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            for (var spec : TennoGunSpec.values()) helper.register(id(spec.id()),new TennoGunItem(spec));
        });
    }
}
