package com.jax.destiny2mc.tenno.mods;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
public final class ModPacks {
    private ModPacks() {}
    public enum Pool {
        ALL("mod_pack", ""), PRIMARY("primary_mod_pack", "primary"),
        SECONDARY("secondary_mod_pack", "secondary"), MELEE("melee_mod_pack", "melee");
        public final String id;
        private final String family;
        Pool(String id, String family) { this.id = id; this.family = family; }
        public boolean includes(WeaponMod mod) { return this == ALL || mod.forPack(family); }
        ResourceLocation key() { return ResourceLocation.fromNamespaceAndPath("destiny2mc", id); }
    }

    public static final class PackItem extends Item {
        private final Pool pool;
        private PackItem(Pool pool) {
            super(new Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
            this.pool = pool;
        }
        public Pool pool() { return pool; }

        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            var stack = player.getItemInHand(hand);
            if (!stack.is(this) || player.isSpectator() || !player.isAlive()) return InteractionResultHolder.fail(stack);
            if (level.isClientSide()) return InteractionResultHolder.success(stack);
            if (!(player instanceof ServerPlayer owner)) return InteractionResultHolder.fail(stack);

            // Sample without replacement, directly from the currently missing kinds.
            var missing = new ArrayList<WeaponMod>();
            for (var mod : WeaponMod.values())
                if (pool.includes(mod) && ModCollection.rank(owner, mod.id()) == ModCollection.LOCKED) missing.add(mod);
            if (missing.isEmpty()) {
                owner.displayClientMessage(Component.translatable("message.destiny2mc.mod_pack.complete",
                    stack.getHoverName()), true);
                return InteractionResultHolder.fail(stack);
            }
            var unlocked = missing.get(owner.getRandom().nextInt(missing.size()));
            if (!ModCollection.unlock(owner, unlocked)) return InteractionResultHolder.fail(stack);
            if (!owner.getAbilities().instabuild) stack.shrink(1);
            owner.awardStat(Stats.ITEM_USED.get(this));
            owner.getInventory().setChanged();
            owner.containerMenu.broadcastChanges();
            ModCollection.sync(owner, unlocked.id());
            owner.displayClientMessage(Component.translatable("message.destiny2mc.mod_pack.unlocked",
                Component.literal(unlocked.title()).withStyle(ChatFormatting.GOLD)), true);
            level.playSound(null, owner.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, .75f, 1.35f);
            return InteractionResultHolder.consume(stack);
        }

        @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
            lines.add(Component.translatable("item.destiny2mc." + pool.id + ".hint").withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("tooltip.destiny2mc.mod_pack.duplicate").withStyle(ChatFormatting.GRAY));
        }
    }
    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            for (var pool : Pool.values()) helper.register(pool.key(), new PackItem(pool));
        });
    }
}
