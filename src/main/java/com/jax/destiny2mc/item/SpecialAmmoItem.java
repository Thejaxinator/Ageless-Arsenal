package com.jax.destiny2mc.item;
import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.*;import net.minecraft.world.level.Level;
public class SpecialAmmoItem extends Item {
 public SpecialAmmoItem(){super(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){ItemStack s=p.getItemInHand(h);if(!l.isClientSide)com.jax.destiny2mc.frontier.FrontierAmmo.use(p,s);return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
}
