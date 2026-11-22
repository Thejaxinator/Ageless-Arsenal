package com.jax.destiny2mc.gunsmith;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class LegacyGunsmithRecipes {
   public static final int COUNT = 47;
   public static final int[] CODE0;
   public static final int[] CNT0;
   public static final int[] CODE1;
   public static final int[] CNT1;
   public static final int[] CODE2;
   public static final int[] CNT2;
   public static final int[] TAB_START;
   public static final int[] TAB_LEN;

   static {
      int[] var0 = CODE0 = new int[47];
      int[] var1 = CNT0 = new int[47];
      int[] var2 = CODE1 = new int[47];
      int[] var3 = CNT1 = new int[47];
      int[] var4 = CODE2 = new int[47];
      int[] var5 = CNT2 = new int[47];
      var0[0] = 0;
      var1[0] = 12;
      var2[0] = 1;
      var3[0] = 6;
      var4[0] = 3;
      var5[0] = 2;
      var0[1] = 0;
      var1[1] = 8;
      var2[1] = 4;
      var3[1] = 4;
      var4[1] = 5;
      var5[1] = 6;
      var0[2] = 0;
      var1[2] = 16;
      var2[2] = 1;
      var3[2] = 8;
      var4[2] = 5;
      var5[2] = 8;
      var0[3] = 0;
      var1[3] = 12;
      var2[3] = 6;
      var3[3] = 8;
      var4[3] = 3;
      var5[3] = 2;
      var0[4] = 0;
      var1[4] = 14;
      var2[4] = 2;
      var3[4] = 10;
      var4[4] = 3;
      var5[4] = 2;
      var0[5] = 0;
      var1[5] = 12;
      var2[5] = 1;
      var3[5] = 6;
      var4[5] = 3;
      var5[5] = 3;
      var0[6] = 0;
      var1[6] = 10;
      var2[6] = 1;
      var3[6] = 8;
      var4[6] = 4;
      var5[6] = 6;
      var0[7] = 0;
      var1[7] = 12;
      var2[7] = 8;
      var3[7] = 6;
      var4[7] = 3;
      var5[7] = 2;
      var0[8] = 0;
      var1[8] = 12;
      var2[8] = 5;
      var3[8] = 8;
      var4[8] = 3;
      var5[8] = 2;
      var0[9] = 0;
      var1[9] = 10;
      var2[9] = 1;
      var3[9] = 10;
      var4[9] = 3;
      var5[9] = 2;
      var0[10] = 0;
      var1[10] = 14;
      var2[10] = 6;
      var3[10] = 8;
      var4[10] = 3;
      var5[10] = 3;
      var0[11] = 0;
      var1[11] = 12;
      var2[11] = 7;
      var3[11] = 10;
      var4[11] = 3;
      var5[11] = 3;
      var0[12] = 0;
      var1[12] = 16;
      var2[12] = 4;
      var3[12] = 10;
      var4[12] = 3;
      var5[12] = 4;
      var0[13] = 0;
      var1[13] = 12;
      var2[13] = 5;
      var3[13] = 8;
      var4[13] = 3;
      var5[13] = 2;
      var0[14] = 0;
      var1[14] = 14;
      var2[14] = 1;
      var3[14] = 8;
      var4[14] = 3;
      var5[14] = 3;
      var0[15] = 0;
      var1[15] = 12;
      var2[15] = 2;
      var3[15] = 8;
      var4[15] = 3;
      var5[15] = 2;
      var0[16] = 0;
      var1[16] = 10;
      var2[16] = 5;
      var3[16] = 8;
      var4[16] = 2;
      var5[16] = 6;
      var0[17] = 0;
      var1[17] = 14;
      var2[17] = 4;
      var3[17] = 8;
      var4[17] = 3;
      var5[17] = 3;
      var0[18] = 0;
      var1[18] = 12;
      var2[18] = 4;
      var3[18] = 8;
      var4[18] = 8;
      var5[18] = 4;
      var0[19] = 2;
      var1[19] = 12;
      var2[19] = 9;
      var3[19] = 4;
      var4[19] = 1;
      var5[19] = 6;
      var0[20] = 2;
      var1[20] = 8;
      var2[20] = 9;
      var3[20] = 3;
      var4[20] = 4;
      var5[20] = 4;
      var0[21] = 2;
      var1[21] = 6;
      var2[21] = 9;
      var3[21] = 3;
      var4[21] = 5;
      var5[21] = 4;
      var0[22] = 2;
      var1[22] = 10;
      var2[22] = 9;
      var3[22] = 4;
      var4[22] = 10;
      var5[22] = 1;
      var0[23] = 2;
      var1[23] = 10;
      var2[23] = 9;
      var3[23] = 4;
      var4[23] = 10;
      var5[23] = 2;
      var0[24] = 2;
      var1[24] = 10;
      var2[24] = 11;
      var3[24] = 2;
      var4[24] = 9;
      var5[24] = 4;
      var0[25] = 2;
      var1[25] = 8;
      var2[25] = 11;
      var3[25] = 2;
      var4[25] = 1;
      var5[25] = 4;
      var0[26] = 2;
      var1[26] = 12;
      var2[26] = 11;
      var3[26] = 3;
      var4[26] = 10;
      var5[26] = 1;
      var0[27] = 2;
      var1[27] = 12;
      var2[27] = 11;
      var3[27] = 3;
      var4[27] = 9;
      var5[27] = 4;
      var0[28] = 12;
      var1[28] = 8;
      var2[28] = 8;
      var3[28] = 6;
      var4[28] = 3;
      var5[28] = 3;
      var0[29] = 12;
      var1[29] = 5;
      var2[29] = 1;
      var3[29] = 6;
      var4[29] = 8;
      var5[29] = 2;
      var0[30] = 12;
      var1[30] = 5;
      var2[30] = 0;
      var3[30] = 8;
      var4[30] = 8;
      var5[30] = 2;
      var0[31] = 12;
      var1[31] = 5;
      var2[31] = 2;
      var3[31] = 8;
      var4[31] = 5;
      var5[31] = 4;
      var0[32] = 12;
      var1[32] = 5;
      var2[32] = 4;
      var3[32] = 6;
      var4[32] = 8;
      var5[32] = 2;
      var0[33] = 12;
      var1[33] = 5;
      var2[33] = 0;
      var3[33] = 8;
      var4[33] = 2;
      var5[33] = 6;
      var0[34] = 12;
      var1[34] = 6;
      var2[34] = 1;
      var3[34] = 8;
      var4[34] = 5;
      var5[34] = 6;
      var0[35] = 13;
      var1[35] = 8;
      var2[35] = 1;
      var3[35] = 10;
      var4[35] = 3;
      var5[35] = 3;
      var0[36] = 13;
      var1[36] = 5;
      var2[36] = 2;
      var3[36] = 8;
      var4[36] = 1;
      var5[36] = 4;
      var0[37] = 13;
      var1[37] = 5;
      var2[37] = 2;
      var3[37] = 8;
      var4[37] = 5;
      var5[37] = 4;
      var0[38] = 13;
      var1[38] = 5;
      var2[38] = 6;
      var3[38] = 8;
      var4[38] = 3;
      var5[38] = 2;
      var0[39] = 13;
      var1[39] = 5;
      var2[39] = 2;
      var3[39] = 10;
      var4[39] = 7;
      var5[39] = 4;
      var0[40] = 0;
      var1[40] = 2;
      var2[40] = 2;
      var3[40] = 2;
      var4[40] = 5;
      var5[40] = 2;
      var0[41] = 1;
      var1[41] = 6;
      var2[41] = 4;
      var3[41] = 4;
      var4[41] = 3;
      var5[41] = 2;
      var0[42] = 2;
      var1[42] = 6;
      var2[42] = 9;
      var3[42] = 2;
      var4[42] = 0;
      var5[42] = 4;
      var0[43] = 2;
      var1[43] = 8;
      var2[43] = 9;
      var3[43] = 3;
      var4[43] = 10;
      var5[43] = 1;
      var0[44] = 4;
      var1[44] = 6;
      var2[44] = 10;
      var3[44] = 1;
      var4[44] = 1;
      var5[44] = 4;
      var0[45] = 1;
      var1[45] = 6;
      var2[45] = 11;
      var3[45] = 1;
      var4[45] = 3;
      var5[45] = 2;
      var0[46] = 2;
      var1[46] = 4;
      var2[46] = 9;
      var3[46] = 1;
      var4[46] = 0;
      var5[46] = 2;
      int[] var6 = TAB_START = new int[8];
      int[] var7 = TAB_LEN = new int[8];
      var6[0] = 0;
      var7[0] = 5;
      var6[1] = 5;
      var7[1] = 5;
      var6[2] = 10;
      var7[2] = 5;
      var6[3] = 15;
      var7[3] = 5;
      var6[4] = 20;
      var7[4] = 4;
      var6[5] = 24;
      var7[5] = 4;
      var6[6] = 28;
      var7[6] = 12;
      var6[7] = 40;
      var7[7] = 7;
   }

   public static Item resolveItemByCode(int var0) {
      if (var0 == 0) {
         return Items.IRON_INGOT;
      } else if (var0 == 1) {
         return Items.GOLD_INGOT;
      } else if (var0 == 2) {
         return Items.COPPER_INGOT;
      } else if (var0 == 3) {
         return Items.DIAMOND;
      } else if (var0 == 4) {
         return Items.AMETHYST_SHARD;
      } else if (var0 == 5) {
         return Items.REDSTONE;
      } else if (var0 == 6) {
         return Items.QUARTZ;
      } else if (var0 == 7) {
         return Items.LAPIS_LAZULI;
      } else if (var0 == 8) {
         return Items.EMERALD;
      } else if (var0 == 9) {
         return (Item)ModRegistries.PLUNDERED_ETHER_ALLOY.get();
      } else if (var0 == 10) {
         return (Item)ModRegistries.SCORCH_ETHER_CORE.get();
      } else if (var0 == 11) {
         return (Item)ModRegistries.ECLIPSE_MONOLITH_CIPHER.get();
      } else {
         return var0 == 12 ? (Item)ModRegistries.HIVE_CHITIN_FRAGMENT.get() : (Item)ModRegistries.VEX_RADIOLARIAN_CORE.get();
      }
   }

   public static Item getIngredientItem(int var0, int var1) {
      if (var1 == 0) {
         return resolveItemByCode(CODE0[var0]);
      } else {
         return var1 == 1 ? resolveItemByCode(CODE1[var0]) : resolveItemByCode(CODE2[var0]);
      }
   }

   public static int getIngredientCount(int var0, int var1) {
      if (var1 == 0) {
         return CNT0[var0];
      } else {
         return var1 == 1 ? CNT1[var0] : CNT2[var0];
      }
   }

   public static ItemStack createIngredientStack(int var0, int var1) {
      return new ItemStack(getIngredientItem(var0, var1), getIngredientCount(var0, var1));
   }

   public static ItemStack createResultStack(int var0) {
      if (var0 < 40) {
         ItemStack var1 = ModRegistries.createWeaponStackByIndex(var0);
         WeaponSpec var2 = WeaponSpec.byIndex(var0);
         GunDataHelper.setInt(var1, "d2_mag", var2.magSize);
         if (var2.usesSpecialAmmo && var2.perkId != 17) {
            GunDataHelper.setInt(var1, "d2_res", var2.magSize * 3);
         }

         return var1;
      } else if (var0 == 40) {
         return new ItemStack((ItemLike)ModRegistries.SPECIAL_AMMO.get(), 8);
      } else if (var0 == 41) {
         return new ItemStack((ItemLike)ModRegistries.EXOTIC_ENGRAM.get(), 1);
      } else if (var0 == 42) {
         return new ItemStack((ItemLike)ModRegistries.COSMODROME_CHEST_ITEM.get(), 1);
      } else if (var0 == 43) {
         return new ItemStack((ItemLike)ModRegistries.FALLEN_CHEST_ITEM.get(), 1);
      } else if (var0 == 44) {
         return new ItemStack((ItemLike)ModRegistries.AWOKEN_CHEST_ITEM.get(), 1);
      } else {
         return var0 == 45
            ? new ItemStack((ItemLike)ModRegistries.LEVIATHAN_CHEST_ITEM.get(), 1)
            : new ItemStack((ItemLike)ModRegistries.VANGUARD_COMMUNICATOR.get(), 1);
      }
   }

   public static String getResultTitle(int var0) {
      if (var0 < 40) {
         return WeaponSpec.byIndex(var0).displayName;
      } else if (var0 == 40) {
         return "Special Ammo Brick (x8)";
      } else if (var0 == 41) {
         return "Exotic Engram";
      } else if (var0 == 42) {
         return "Cosmodrome Cache";
      } else if (var0 == 43) {
         return "Eliksni Plunder Cache";
      } else if (var0 == 44) {
         return "Dreaming City Cache";
      } else {
         return var0 == 45 ? "Imperial Opulent Cache" : "Ageless Arsenals Field Guide";
      }
   }

   public static String getResultSubtitle(int var0) {
      if (var0 < 40) {
         return WeaponSpec.byIndex(var0).frameLabel;
      } else if (var0 == 40) {
         return "High-Density Green Munition Pack";
      } else if (var0 == 41) {
         return "Encrypted Golden Age Exotic Matrix";
      } else if (var0 == 42) {
         return "Animated Reward Cache — Vanguard Arms & Ammo";
      } else if (var0 == 43) {
         return "Animated Reward Cache — Guaranteed Plunder Weapon";
      } else {
         return var0 == 44 ? "Animated Reward Cache — Guaranteed Awoken Exotic" : "Animated Reward Cache — Guaranteed Eclipse Mythic";
      }
   }

   public static int countInInventory(Player var0, Item var1) {
      if (var0 != null) {
         Inventory var2 = var0.getInventory();
         int var3 = 0;

         for (int var4 = 0; var4 < 36; var4++) {
            ItemStack var5 = var2.getItem(var4);
            if (!var5.isEmpty() && var5.getItem() == var1) {
               var3 += var5.getCount();
            }
         }

         return var3;
      } else {
         return 0;
      }
   }

   public static boolean canCraftSlot(Player var0, int var1, int var2) {
      return var0 != null && (var0.isCreative() || countInInventory(var0, getIngredientItem(var1, var2)) >= getIngredientCount(var1, var2));
   }

   public static boolean canCraft(Player var0, int var1) {
      return var1 >= 0 && var1 < 47 && canCraftSlot(var0, var1, 0) && canCraftSlot(var0, var1, 1) && canCraftSlot(var0, var1, 2);
   }

   public static void consumeFromInventory(Player var0, Item var1, int var2) {
      if (var0 != null) {
         deductItem(var0.getInventory(), var1, var2);
      }
   }

   private static void deductItem(Inventory var0, Item var1, int var2) {
      int var3 = var2;

      for (int var4 = 0; var3 > 0 && var4 < 36; var4++) {
         ItemStack var5 = var0.getItem(var4);
         if (!var5.isEmpty() && var5.getItem() == var1) {
            int var6 = var5.getCount();
            if (var6 > var3) {
               var5.shrink(var3);
               var3 = 0;
            } else {
               var3 -= var6;
               var5.shrink(var6);
            }
         }
      }
   }

   public static void tryCraftOnServer(ServerPlayer var0, int var1) {
      if (canCraft(var0, var1)) {
         if (!var0.isCreative()) {
            Inventory var2 = var0.getInventory();
            deductItem(var2, getIngredientItem(var1, 0), getIngredientCount(var1, 0));
            deductItem(var2, getIngredientItem(var1, 1), getIngredientCount(var1, 1));
            deductItem(var2, getIngredientItem(var1, 2), getIngredientCount(var1, 2));
         }

         ItemStack var3 = createResultStack(var1);
         if (!var0.getInventory().add(var3)) {
            var0.drop(var3, false);
         }

         ServerBallisticsEngine.playSound(var0.level(), var0.getX(), var0.getY(), var0.getZ(), "block.smithing_table.use", 0.95F, 1.18F);
      } else {
         ServerBallisticsEngine.playSound(var0.level(), var0.getX(), var0.getY(), var0.getZ(), "block.dispenser.fail", 0.85F, 1.2F);
      }
   }
}
