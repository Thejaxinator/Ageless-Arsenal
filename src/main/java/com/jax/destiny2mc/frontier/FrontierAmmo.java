package com.jax.destiny2mc.frontier;
import com.jax.destiny2mc.item.*;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class FrontierAmmo {
 public static int amount(WeaponSpec w){return w.index<40?w.perkId==20?30:8:w.archetype==6?100:w.archetype==7?2:w.archetype==8?4:8;}
 public static int capacity(WeaponSpec w){return w.index<40?w.perkId==20?120:w.perkId==35?144:28:w.archetype==6?400:w.archetype==7?8:w.archetype==8?16:32;}
 public static int consume(Player p,WeaponSpec w){for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack s=p.getInventory().getItem(i);if(s.getItem() instanceof SpecialAmmoItem){if(!p.isCreative())s.shrink(1);return amount(w);}}return 0;}
 /** A datapack can replace a special-ammo weapon's feed with a capacitor. A right-clicked
  * ammo brick must skip that stack even if its previous magazine left an empty reserve. */
 private static boolean acceptsBrick(WeaponSpec spec){
  var program=com.jax.destiny2mc.ageless.WeaponPrograms.server(spec);
  return program==null||program.feed()!=com.jax.destiny2mc.ageless.WeaponProgram.Feed.CAPACITOR;
 }
 public static void use(Player p,ItemStack brick){boolean changed=false;for(int i=0;i<36;i++){ItemStack s=p.getInventory().getItem(i);if(!(s.getItem() instanceof DestinyGunItem g)||!g.spec.usesSpecialAmmo||g.spec.perkId==17||!acceptsBrick(g.spec))continue;int n=GunDataHelper.getReserve(s,g.spec);if(n>=capacity(g.spec))continue;GunDataHelper.setInt(s,"d2_res",Math.min(capacity(g.spec),n+amount(g.spec)));changed=true;}if(changed){if(!p.isCreative())brick.shrink(1);p.getInventory().setChanged();p.containerMenu.broadcastChanges();ServerBallisticsEngine.playSound(p.level(),p.getX(),p.getY(),p.getZ(),"entity.experience_orb.pickup",.8f,1.5f);}}
}
