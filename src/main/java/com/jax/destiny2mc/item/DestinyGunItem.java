/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.jax.destiny2mc.item;

import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DestinyGunItem
extends Item {
    public final WeaponSpec spec;

    public DestinyGunItem(WeaponSpec weaponSpec) {
        super(new Item.Properties().stacksTo(1).rarity(weaponSpec.isExotic ? Rarity.EPIC : Rarity.RARE));
        this.spec = weaponSpec;
    }

    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return false;
    }

    public boolean shouldCauseReequipAnimation(ItemStack itemStack, ItemStack itemStack2, boolean bl) {
        return bl;
    }

    public boolean isBarVisible(ItemStack itemStack) {
        return false;
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int n, boolean bl) {
        if (!level.isClientSide) {
            if(this.spec.index>=40||com.jax.destiny2mc.ageless.WeaponPrograms.server(this.spec)!=null) com.jax.destiny2mc.frontier.FrontierBallistics.inventoryTick(this.spec,itemStack,level,entity,bl);
            else ServerBallisticsEngine.serverInventoryTick(this.spec, itemStack, level, entity, bl);
        }
    }

    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List list, TooltipFlag tooltipFlag) {
        list.add(Component.literal((String)this.spec.frameLabel).withStyle(this.spec.isExotic ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE));
        list.add(Component.literal((String)this.spec.perkTitle).withStyle(ChatFormatting.AQUA));
        list.add(Component.literal((String)this.spec.perkDesc).withStyle(ChatFormatting.GRAY));
        WeaponPerkRegistry.appendPerkTooltips(itemStack, this.spec, list);
        list.add(Component.literal((String)"[LMB] Fire  [RMB] ADS  [R] Reload  [O] HUD Settings").withStyle(ChatFormatting.DARK_GRAY));
    }
}

