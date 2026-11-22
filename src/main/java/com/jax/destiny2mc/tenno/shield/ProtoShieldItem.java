package com.jax.destiny2mc.tenno.shield;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * Unlockable, tiered Corpus / Orokin Proto-Shield Generators equipped in the Curios Charm slot.
 * Provides regenerating Proto-Shields, Warframe-style Shield-Gating on break, and (at Prime tier)
 * an Arc Retaliation Pulse when shields shatter.
 */
public final class ProtoShieldItem extends Item {
    public enum Tier {
        MK1("proto_shield_mk1", "MK-I", 12f, 100, 3.0f, 10, false, Rarity.UNCOMMON, ChatFormatting.AQUA),
        MK2("proto_shield_mk2", "MK-II", 24f, 80, 5.0f, 15, false, Rarity.RARE, ChatFormatting.BLUE),
        MK3("proto_shield_mk3", "MK-III", 40f, 65, 8.0f, 20, false, Rarity.RARE, ChatFormatting.LIGHT_PURPLE),
        PRIME("proto_shield_prime", "PRIME", 60f, 50, 12.0f, 26, true, Rarity.EPIC, ChatFormatting.GOLD);

        public final String id;
        public final String label;
        public final float maxShield;
        public final int regenDelayTicks;
        public final float regenPerSecond;
        public final int gateTicks;
        public final boolean retaliationPulse;
        public final Rarity rarity;
        public final ChatFormatting color;

        Tier(String id, String label, float maxShield, int regenDelayTicks, float regenPerSecond,
             int gateTicks, boolean retaliationPulse, Rarity rarity, ChatFormatting color) {
            this.id = id;
            this.label = label;
            this.maxShield = maxShield;
            this.regenDelayTicks = regenDelayTicks;
            this.regenPerSecond = regenPerSecond;
            this.gateTicks = gateTicks;
            this.retaliationPulse = retaliationPulse;
            this.rarity = rarity;
            this.color = color;
        }
    }

    private final Tier tier;

    public ProtoShieldItem(Tier tier) {
        super(new Properties().stacksTo(1).rarity(tier.rarity));
        this.tier = tier;
    }

    public Tier tier() {
        return tier;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return tier == Tier.PRIME || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Equip in Curios Charm slot").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal(String.format("Proto-Shield Capacity: %.0f", tier.maxShield)).withStyle(tier.color));
        lines.add(Component.literal(String.format("Recharge: %.1f/s after %.2fs delay", tier.regenPerSecond, tier.regenDelayTicks / 20.0)).withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal(String.format("Shield-Gate: %.2fs invulnerability on break", tier.gateTicks / 20.0)).withStyle(ChatFormatting.DARK_AQUA));
        if (tier.retaliationPulse) {
            lines.add(Component.literal("Orokin Overload: Arc retaliation pulse on shield break").withStyle(ChatFormatting.GOLD));
        }
        lines.add(Component.literal("Bypassed by Toxin/Poison; drained inside Nullifier domes").withStyle(ChatFormatting.DARK_GRAY));
    }
}
