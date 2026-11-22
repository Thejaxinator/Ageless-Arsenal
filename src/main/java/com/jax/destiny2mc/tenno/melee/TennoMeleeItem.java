package com.jax.destiny2mc.tenno.melee;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.SimpleTier;

public final class TennoMeleeItem extends SwordItem {
    private final MeleeProfile profile;
    public TennoMeleeItem(MeleeProfile profile) {
        super(tier(profile), new Properties().attributes(attributes(profile)));
        this.profile = profile;
    }
    // TieredItem takes maximum wear from Tier.getUses() at construction.
    // Preserve vanilla iron repair and enchantment properties across the six distinct lifetimes.
    private static SimpleTier tier(MeleeProfile profile) {
        var iron = Tiers.IRON;
        return new SimpleTier(iron.getIncorrectBlocksForDrops(), profile.durability(), iron.getSpeed(),
                iron.getAttackDamageBonus(), iron.getEnchantmentValue(), iron::getRepairIngredient);
    }
    public MeleeProfile profile() { return profile; }
    static ResourceLocation key(String path) { return ResourceLocation.fromNamespaceAndPath("destiny2mc", path); }
    private static ItemAttributeModifiers attributes(MeleeProfile p) {
        var builder = ItemAttributeModifiers.builder();
        var op = AttributeModifier.Operation.ADD_VALUE;
        var slot = EquipmentSlotGroup.MAINHAND;
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, p.damage() - 1, op), slot);
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, p.attackSpeed() - 4, op), slot);
        builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(key("tenno/melee_reach"), p.reach() - 3, op), slot);
        return builder.build();
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BLOCK; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == ItemAbilities.SHIELD_BLOCK || super.canPerformAction(stack, action);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, context, lines, flags);
        lines.add(Component.literal(profile.trait() + " • " + profile.description()));
        lines.add(Component.literal("Hold Use to guard • Crouch and make a charged attack for a heavy"));
    }
}
