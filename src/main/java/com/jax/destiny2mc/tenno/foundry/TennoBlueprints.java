package com.jax.destiny2mc.tenno.foundry;

import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
public final class TennoBlueprints {
    private TennoBlueprints() {}

    private static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", path);
    }

    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.RECIPE_TYPE, helper -> helper.register(key("tenno_foundry"), TennoBlueprint.TYPE));
        event.register(Registries.RECIPE_SERIALIZER, helper -> helper.register(key("tenno_foundry"), TennoBlueprint.SERIALIZER));
    }


    public static boolean isTenno(Item item) {
        return item instanceof TennoGunItem || item instanceof TennoMeleeItem;
    }

    // Registry entries are immutable once the game has finished bootstrapping. Recipes may
    // change on /reload, so only the item catalog is cached, never the player's recipe lookup.
    private static final class Catalog {
        private static final List<Item> ITEMS = BuiltInRegistries.ITEM.stream()
            .filter(TennoBlueprints::isTenno)
            .sorted(Comparator.comparing((Item item) -> item instanceof TennoMeleeItem)
                .thenComparing(BuiltInRegistries.ITEM::getKey))
            .toList();
    }

    public static List<Item> entries() { return Catalog.ITEMS; }

    public static List<Item> entries(Player player) {
        return entries().stream().filter(item -> find(player,item) != null).toList();
    }

    private static TennoBlueprint find(Player player, Item item) {
        if (player == null || !isTenno(item)) return null;
        var id = BuiltInRegistries.ITEM.getKey(item);
        var holder = player.level().getRecipeManager().byKey(key("tenno_foundry/" + id.getPath()));
        if (holder.isPresent() && holder.get().value() instanceof TennoBlueprint blueprint && blueprint.result() == item)
            return blueprint;
        return null;
    }

    public static List<ItemStack> cost(Player player, Item item) {
        var blueprint = find(player,item);
        return blueprint == null ? List.of() : blueprint.materials().stream()
            .map(row -> new ItemStack(row.item(),row.count())).toList();
    }

    private static boolean usable(ItemStack stack, Item item) {
        return stack.is(item) && !(isTenno(item) && (stack.has(DataComponents.CUSTOM_NAME)
            || stack.isEnchanted() || !WeaponMods.installed(stack).isEmpty()));
    }

    /** The Foundry consumes ordinary backpack and hotbar weapons. Named, enchanted and
     * modded weapons retain the player's build until explicitly stripped or replaced. */
    public static int available(Player player, Item item) {
        if (player == null) return 0;
        int count = 0;
        for (ItemStack stack : player.getInventory().items)
            if (usable(stack,item)) count += stack.getCount();
        return count;
    }

    private static boolean affordable(Player player, TennoBlueprint blueprint) {
        return blueprint != null && !player.isSpectator() && blueprint.materials().stream()
            .allMatch(material -> available(player,material.item()) >= material.count());
    }

    public static boolean canCraft(Player player, Item item) { return affordable(player,find(player,item)); }

    /** Call on the server thread after validating the Gunsmith session. Distinct recipe rows
     * are validated together. Partial payment and consuming a second input on replay are avoided
     * by this transaction and by the session's network sequence check, respectively. */
    public static boolean craft(ServerPlayer player, Item item) {
        var blueprint = find(player,item);
        if (!affordable(player,blueprint)) return false;
        var result = blueprint.result().getDefaultInstance();
        for (var material : blueprint.materials()) {
            int remaining = material.count();
            for (ItemStack stack : player.getInventory().items) {
                if (!usable(stack,material.item())) continue;
                int payment = Math.min(remaining,stack.getCount());
                stack.shrink(payment);
                remaining -= payment;
                if (remaining == 0) break;
            }
        }
        if (!player.getInventory().add(result) && !result.isEmpty()) player.drop(result,false);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.level().playSound(null,player.blockPosition(),SoundEvents.SMITHING_TABLE_USE,SoundSource.BLOCKS,.8f,1.15f);
        return true;
    }
}
