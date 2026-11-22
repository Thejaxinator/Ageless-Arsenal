package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.tenno.melee.MeleeProfile;
import com.jax.destiny2mc.tenno.mods.ModPacks;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
public final class TennoCreativeTab {
    public static final ResourceKey<CreativeModeTab> KEY =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, TennoGunRegistration.id("warframe"));

    private TennoCreativeTab() {}

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(KEY.location(),
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.destiny2mc.warframe"))
                .icon(() -> stack(TennoGunSpec.TWIN_GRAKATAS.id()))
                .withTabsBefore(ModRegistries.ARSENAL_TAB.getId())
                .displayItems((parameters, output) -> {
                    addGuns(output, false);
                    addGuns(output, true);
                    for (var melee : MeleeProfile.ALL) output.accept(stack(melee.id()));
                    for (var pack : ModPacks.Pool.values()) output.accept(stack(pack.id));
                })
                .build()));
    }

    // Declaration order keeps related variants adjacent even when display names are translated.
    // Native output.accept includes the same default stack in Minecraft's global Search tab.
    private static void addGuns(CreativeModeTab.Output output, boolean secondary) {
        for (var gun : TennoGunSpec.values()) {
            var item = stack(gun.id());
            if (WeaponMod.isSecondary(item) == secondary) output.accept(item);
        }
    }

    private static ItemStack stack(String path) {
        return BuiltInRegistries.ITEM.get(TennoGunRegistration.id(path)).getDefaultInstance();
    }
}
