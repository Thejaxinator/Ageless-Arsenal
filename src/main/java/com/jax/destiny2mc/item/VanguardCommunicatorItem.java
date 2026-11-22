package com.jax.destiny2mc.item;
import java.util.List;
import com.jax.destiny2mc.client.ClientScreenHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** Old save identity now opens a Ageless Arsenals Field Guide. */
public class VanguardCommunicatorItem extends Item {
 public VanguardCommunicatorItem(){super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){if(l.isClientSide)ClientScreenHelper.openStrikeDirector();return InteractionResultHolder.sidedSuccess(p.getItemInHand(h),l.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){out.add(Component.literal("Right-click: locate ruins and dungeons, inspect 3D enemies, read boss mechanics."));}
}
