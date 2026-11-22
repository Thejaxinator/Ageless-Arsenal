package com.jax.destiny2mc.tenno.melee;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD)
public final class MeleeRegistration {
    private MeleeRegistration() {}
    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, registry -> {
            for (var profile : MeleeProfile.ALL)
                registry.register(TennoMeleeItem.key(profile.id()), new TennoMeleeItem(profile));
        });
    }
}
