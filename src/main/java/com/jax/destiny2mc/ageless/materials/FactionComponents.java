package com.jax.destiny2mc.ageless.materials;

import com.jax.destiny2mc.ModRegistries;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The namespace is retained so established worlds and packs share stable registry keys. */
@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD)
public final class FactionComponents {
    public record Salvage(String faction, String id, TagKey<EntityType<?>> source) {
        Salvage(String faction, String id) {
            this(faction, id, TagKey.create(Registries.ENTITY_TYPE, key("salvage/" + faction)));
        }
        public Item item() { return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(key(id)); }
    }

    public static final List<Salvage> ALL = List.of(
        new Salvage("fallen", "ether_induction_coil"),
        new Salvage("vex", "conduit_lens"),
        new Salvage("hive", "runed_marrow"),
        new Salvage("cabal", "breech_seal"),
        new Salvage("taken", "inversion_knot"),
        new Salvage("scorn", "dark_ether_bellows"),
        new Salvage("dread", "resonant_tine")
    );

    public static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", path);
    }

    private static final class Part extends Item {
        private final String faction;
        Part(String faction) {
            super(new Properties().rarity(Rarity.UNCOMMON));
            this.faction = faction;
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
            lines.add(Component.translatable("salvage.destiny2mc." + faction).withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, h -> ALL.forEach(part -> h.register(key(part.id()), new Part(part.faction()))));
        event.register(Registries.RECIPE_TYPE, h -> h.register(key("gunsmith"), GunsmithBlueprint.TYPE));
        event.register(Registries.RECIPE_SERIALIZER, h -> h.register(key("gunsmith"), GunsmithBlueprint.SERIALIZER));
    }

    @SubscribeEvent public static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey())) {
            ALL.forEach(part -> event.accept(part.item()));
        }
    }
}
