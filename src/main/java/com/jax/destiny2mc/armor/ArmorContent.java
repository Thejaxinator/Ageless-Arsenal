package com.jax.destiny2mc.armor;

import com.jax.destiny2mc.ModRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class ArmorContent {
    private ArmorContent() {}
    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.ARMOR_MATERIAL,registry->{
            for (var suit:ArmorPiece.Suit.values()) registry.register(ArmorPiece.key(suit.id),suit.material());
        });
        event.register(Registries.ITEM,registry->{
            for (var piece:ArmorPiece.values()) registry.register(ArmorPiece.key(piece.id),new ArsenalArmorItem(piece));
        });
    }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey())) return;
        for (var piece:ArmorPiece.values()) event.accept(BuiltInRegistries.ITEM.get(ArmorPiece.key(piece.id)));
    }
}
