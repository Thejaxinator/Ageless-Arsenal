package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.weapon.GunDataHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/** An independent weapon item. Existing Destiny perks and Gunsmith actions never mutate it. */
public final class TennoGunItem extends Item {
    private final TennoGunSpec spec;
    public TennoGunItem(TennoGunSpec spec) {
        super(new Properties().stacksTo(1).rarity(spec.variant().equals("Standard")?Rarity.COMMON:Rarity.RARE));
        this.spec=spec;
    }
    public TennoGunSpec profile() { return spec; }
    public static int magazine(ItemStack stack) {
        if (!(stack.getItem() instanceof TennoGunItem gun)) return 0;
        // With no owner supplied, report the persisted ammunition. Old stacks default to their
        // original full magazine; installing or ranking a capacity card does not create rounds.
        return Math.max(0,GunDataHelper.getInt(stack,"wf_mag",gun.spec.magazine()));
    }
    public static int magazine(Player holder,ItemStack stack) {
        if (!(stack.getItem() instanceof TennoGunItem)) return 0;
        return loaded(stack,TennoGunStats.of(holder,stack));
    }
    public static int loaded(ItemStack stack,TennoGunStats stats) {
        return Math.min(magazine(stack),stats.magazine());
    }
    public static long reloadUntil(ItemStack stack) { return GunDataHelper.getLong(stack,"wf_reload"); }
    @Override public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity holder,int slot,boolean selected) {
        if (!level.isClientSide) {
            if (spec.family==TennoGunSpec.Family.BUBONICO && holder instanceof Player player)
                TennoBallistics.charge(stack,player,level.getGameTime());
            if(!selected && reloadUntil(stack)>0) GunDataHelper.setLong(stack,"wf_reload",0);
        }
    }
    @Override public boolean canAttackBlock(BlockState state,Level level,BlockPos pos,Player player) { return false; }
}
