package com.jax.destiny2mc;

import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.Collection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class CommonForgeEvents {
   private static void addDrop(Collection var0, Level var1, Entity var2, Item var3, int var4) {
      if (var4 > 0) {
         var0.add(new ItemEntity(var1, var2.getX(), var2.getY(), var2.getZ(), new ItemStack(var3, var4)));
      }
   }

   @SubscribeEvent
   public static void onPlayerMelee(AttackEntityEvent var0) {
      Player var1 = var0.getEntity();
      Level var2 = var1.level();
      if (!var2.isClientSide) {
         Inventory var3 = var1.getInventory();

         for (int var4 = 0; var4 < 9; var4++) {
            ItemStack var5 = var3.getItem(var4);
            if (var5.getItem() instanceof DestinyGunItem) {
               WeaponSpec var6 = ((DestinyGunItem)var5.getItem()).spec;
               int var7 = var6.perkId;
               if (var7 == 8 || var7 == 34) {
                  GunDataHelper.setInt(var5, "d2_pstack", 3);
                  GunDataHelper.setLong(var5, "d2_pexp", var2.getGameTime() + 100L);
               } else if (var7 == 10) {
                  GunDataHelper.setInt(var5, "d2_pstack", 5);
                  GunDataHelper.setLong(var5, "d2_pexp", var2.getGameTime() + 140L);
                  GunDataHelper.setInt(var5, "d2_mag", var6.magSize);
               } else if (var7 == 21) {
                  int var8 = GunDataHelper.getMag(var5, var6) + 10;
                  if (var8 > var6.magSize) {
                     var8 = var6.magSize;
                  }

                  GunDataHelper.setInt(var5, "d2_mag", var8);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDrops(LivingDropsEvent var0) {
      LivingEntity var1 = var0.getEntity();
      if (var1 instanceof Monster) {
         Level var2 = var1.level();
         Collection var3 = var0.getDrops();
         if (var1 instanceof FallenMobEntity) {
            int var4 = ((FallenMobEntity)var1).unitKind;
            if (var4 >= 32) {
               addDrop(var3, var2, var1, (Item)ModRegistries.VEX_RADIOLARIAN_CORE.get(), var4 >= 39 ? 4 : 2);
               addDrop(var3, var2, var1, Items.COPPER_INGOT, 3);
            } else if (var4 >= 21) {
               addDrop(var3, var2, var1, (Item)ModRegistries.HIVE_CHITIN_FRAGMENT.get(), var4 >= 28 ? 4 : 2);
            } else {
               addDrop(var3, var2, var1, Items.COPPER_INGOT, 4);
               addDrop(var3, var2, var1, (Item)ModRegistries.PLUNDERED_ETHER_ALLOY.get(), 2);
               if (var4 >= 3) {
                  addDrop(var3, var2, var1, (Item)ModRegistries.SCORCH_ETHER_CORE.get(), 1);
                  if (var4 >= 6) {
                     addDrop(var3, var2, var1, (Item)ModRegistries.ECLIPSE_MONOLITH_CIPHER.get(), 1);
                  }
               }
            }
         }

         double var6 = Math.random();
         if (var6 <= 0.45) {
            addDrop(var3, var2, var1, (Item)ModRegistries.SPECIAL_AMMO.get(), 1);
         }

         if (var6 <= 0.08) {
            addDrop(var3, var2, var1, (Item)ModRegistries.EXOTIC_ENGRAM.get(), 1);
         }
      }
   }
}
