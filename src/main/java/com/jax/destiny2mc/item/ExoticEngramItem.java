package com.jax.destiny2mc.item;
import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** Reward pool expands with the registered arsenal, instead of stopping at index 39. */
public class ExoticEngramItem extends Item {
 public ExoticEngramItem(){super(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){ItemStack s=p.getItemInHand(h);if(!l.isClientSide&&!p.isSpectator()){
  ItemStack reward=ModRegistries.createWeaponStackByIndex(l.random.nextInt(WeaponSpec.ALL.length));if(!p.addItem(reward))p.drop(reward,false);if(!p.isCreative())s.shrink(1);
  ServerBallisticsEngine.playSound(l,p.getX(),p.getY(),p.getZ(),"block.amethyst_block.chime",1.2f,1.25f);
 }return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
}
