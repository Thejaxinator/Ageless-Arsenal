package com.jax.destiny2mc.weapon;

import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.entity.VexGoblinEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BungieBallisticsHelper {
   public static float computeDynamicBloom(ItemStack var0, WeaponSpec var1, int var2, long var3) {
      int var5 = GunDataHelper.getInt(var0, "d2_bloom", 0);
      long var6 = GunDataHelper.getLong(var0, "d2_btime");
      long var8 = var3 - var6;
      if (var8 < 0L) {
         var8 = 0L;
      }

      if (var8 > 60L) {
         var8 = 60L;
      }

      int var10 = (int)var8 * (65 + var1.stability);
      var5 -= var10;
      if (var5 < 0) {
         var5 = 0;
      }

      float var11 = (float)var5 * 0.001F;
      if (var2 != 0) {
         var11 *= 0.32F;
      }

      int var12 = var5 + (125 - var1.stability) * 4;
      if (var12 > 2600) {
         var12 = 2600;
      }

      GunDataHelper.setInt(var0, "d2_bloom", var12);
      GunDataHelper.setLong(var0, "d2_btime", var3);
      return var11;
   }

   public static Vec3 applyBulletMagnetism(ServerLevel var0, ServerPlayer var1, WeaponSpec var2, Vec3 var3, Vec3 var4, int var5, float var6) {
      if (var2.pellets <= 1) {
         double var7 = var4.x - var3.x;
         double var9 = var4.y - var3.y;
         double var11 = var4.z - var3.z;
         double var13 = var3.distanceTo(var4);
         if (!(var13 <= 0.5)) {
            var7 /= var13;
            var9 /= var13;
            var11 /= var13;
            double var15 = Math.cos(
               Math.toRadians((double)((1.15F + 1.75F * (float)var2.aimAssist * 0.01F) * (var5 != 0 ? 1.22F : 0.9F) / (1.0F + var6 * 0.3F)))
            );
            Vec3 var17 = null;
            AABB var18 = new AABB(var3, var4).inflate(2.5);
            List var19 = var0.getEntities(var1, var18);

            for (int var20 = 0; var20 < var19.size(); var20++) {
               Entity var21 = (Entity)var19.get(var20);
               if (var21 instanceof LivingEntity && !(var21 instanceof Player)) {
                  LivingEntity var22 = (LivingEntity)var21;
                  if (var22.isAlive()) {
                     double var23 = var22.getX();
                     double var25 = var22.getY() + (double)(var22.getBbHeight() * (var22 instanceof VexGoblinEntity ? 0.48F : 0.76F));
                     double var27 = var22.getZ();
                     double var29 = var23 - var3.x;
                     double var31 = var25 - var3.y;
                     double var33 = var27 - var3.z;
                     double var35 = Math.sqrt(var29 * var29 + var31 * var31 + var33 * var33);
                     if (!(var35 <= 1.0) && !(var35 > var13)) {
                        double var37 = (var29 * var7 + var31 * var9 + var33 * var11) / var35;
                        if (var37 > var15) {
                           var15 = var37;
                           var17 = var3.add(var29 / var35 * (var35 + 1.5), var31 / var35 * (var35 + 1.5), var33 / var35 * (var35 + 1.5));
                        }
                     }
                  }
               }
            }

            if (var17 != null) {
               return var17;
            }
         }
      }

      return var4;
   }

   public static void onWallBlockHit(ServerLevel var0, ServerPlayer var1, Vec3 var2, Vec3 var3) {
      double var4 = var2.distanceTo(var3);
      if (!(var4 <= 0.2)) {
         int var6 = (int)Math.floor(var3.x + (var3.x - var2.x) / var4 * 0.18);
         int var7 = (int)Math.floor(var3.y + (var3.y - var2.y) / var4 * 0.18);
         int var8 = (int)Math.floor(var3.z + (var3.z - var2.z) / var4 * 0.18);
         CombatSupport.onPuzzleBlockShot(var0, var1, var6, var7, var8);
      }
   }

   public static boolean handleVexMythoclastAltToggle(ServerPlayer var0, ItemStack var1, WeaponSpec var2, ServerLevel var3) {
      if (var2.perkId == 36) {
         int var4 = GunDataHelper.getInt(var1, "d2_alt", 0);
         if (var4 == 0) {
            if (GunDataHelper.getInt(var1, "d2_pstack", 0) > 0) {
               GunDataHelper.setInt(var1, "d2_alt", 1);
               var0.displayClientMessage(Component.literal("◆ Vex Mythoclast: LINEAR FUSION OVERCHARGE ENGAGED!").withStyle(ChatFormatting.GOLD), true);
               ServerBallisticsEngine.playSound(var3, var0.getX(), var0.getY(), var0.getZ(), "block.beacon.activate", 0.85F, 1.75F);
               return true;
            } else {
               var0.displayClientMessage(
                  Component.literal("Vex Mythoclast requires Overcharge stacks (defeat foes first)!").withStyle(ChatFormatting.RED), true
               );
               return true;
            }
         } else {
            GunDataHelper.setInt(var1, "d2_alt", 0);
            var0.displayClientMessage(Component.literal("◆ Vex Mythoclast: Full-Auto Repeater Mode").withStyle(ChatFormatting.YELLOW), true);
            return true;
         }
      } else {
         return false;
      }
   }

   public static float computeV200ShotMultiplier(ServerPlayer var0, ItemStack var1, WeaponSpec var2, long var3) {
      float var5 = 1.0F;
      if (CombatSupport.hasTimesVengeance(var0)) {
         var5 *= 2.5F;
      }

      int var6 = var2.perkId;
      if (var6 == 36) {
         if (GunDataHelper.getInt(var1, "d2_alt", 0) > 0) {
            int var7 = GunDataHelper.getInt(var1, "d2_pstack", 0);
            if (var7 > 0) {
               GunDataHelper.setInt(var1, "d2_pstack", --var7);
               if (var7 <= 0) {
                  GunDataHelper.setInt(var1, "d2_alt", 0);
               }

               var5 *= 3.15F;
            } else {
               GunDataHelper.setInt(var1, "d2_alt", 0);
            }
         }
      } else if (var6 == 31 || var6 == 33) {
         if (GunDataHelper.getLong(var1, "d2_pexp") > var3) {
            var5 *= 1.25F;
         }
      } else if (var6 == 37) {
         var5 *= 1.15F;
      }

      return var5;
   }

   public static int applyV200PostHitPerks(
      ServerLevel var0, ServerPlayer var1, ItemStack var2, WeaponSpec var3, LivingEntity var4, Vec3 var5, int var6, int var7, long var8
   ) {
      label79: {
         int var10 = var3.perkId;
         if (var10 >= 29) {
            if (var10 == 29) {
               if (var7 != 0) {
                  GunDataHelper.setLong(var2, "d2_pexp", var8 + 140L);
                  if (var6 != 0) {
                     GunDataHelper.setInt(var2, "d2_mag", var3.magSize);
                     ServerBallisticsEngine.triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 5.0, 8.5F, 5);
                     return 4;
                  }
               }
            } else if (var10 != 30 && var10 != 37 && var10 != 39) {
               if (var10 == 36) {
                  if (var7 != 0) {
                     int var11 = GunDataHelper.getInt(var2, "d2_pstack", 0) + 1;
                     if (var11 > 3) {
                        var11 = 3;
                     }

                     GunDataHelper.setInt(var2, "d2_pstack", var11);
                     GunDataHelper.setLong(var2, "d2_pexp", var8 + 300L);
                     return 4;
                  }
               } else if (var7 != 0) {
                  GunDataHelper.setLong(var2, "d2_pexp", var8 + 120L);
                  int var12 = GunDataHelper.getMag(var2, var3) + (var10 == 35 ? 8 : 2);
                  if (var12 > var3.magSize) {
                     var12 = var3.magSize;
                  }

                  GunDataHelper.setInt(var2, "d2_mag", var12);
                  if (var10 == 32 || var10 == 35 || var10 == 40) {
                     ServerBallisticsEngine.triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.0, 5.5F, 2);
                     return 4;
                  }

                  if (var10 == 33) {
                     ServerBallisticsEngine.triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 3.8, 5.0F, 3);
                     return 4;
                  }
               }
            } else if (var7 != 0) {
               if (var10 == 30) {
                  break label79;
               }

               if (var6 != 0) {
                  GunDataHelper.setLong(var2, "d2_outlaw", var8 + 100L);
                  break label79;
               }
            } else if (var10 == 37) {
               ServerBallisticsEngine.triggerAoE(var0, var1, var5.x, var5.y, var5.z, 2.2, 1.8F, 1);
               return 0;
            }
         }

         return 0;
      }

      ServerBallisticsEngine.triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.6, 7.0F, 1);
      return 4;
   }
}
