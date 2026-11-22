package com.jax.destiny2mc.armor;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/** Ordinary ArmorItem behavior covers click-equipping, dispensing, protection, durability and repairs. */
public final class ArsenalArmorItem extends ArmorItem {
    public final ArmorPiece piece;
    public ArsenalArmorItem(ArmorPiece piece) {
        super(piece.suit.holder,piece.type,new Properties().durability(piece.durability)
            .rarity(piece.exotic ? Rarity.EPIC : Rarity.RARE));
        this.piece=piece;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal(piece.trait).withStyle(piece.exotic ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE));
        lines.add(Component.literal(piece.description).withStyle(ChatFormatting.GRAY));
    }
}
