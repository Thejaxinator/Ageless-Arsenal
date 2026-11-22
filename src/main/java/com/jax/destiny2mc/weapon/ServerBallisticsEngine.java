package com.jax.destiny2mc.weapon;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.entity.VexGoblinEntity;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.item.SpecialAmmoItem;
import com.jax.destiny2mc.network.CombatFeedbackPayload;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerBallisticsEngine {
   public static SimpleParticleType getElementParticle(int var0) {
      if (var0 != 1) {
         if (var0 != 2) {
            if (var0 != 3) {
               if (var0 != 4) {
                  return var0 != 5 ? ParticleTypes.CRIT : ParticleTypes.HAPPY_VILLAGER;
               } else {
                  return ParticleTypes.SNOWFLAKE;
               }
            } else {
               return ParticleTypes.PORTAL;
            }
         } else {
            return ParticleTypes.ELECTRIC_SPARK;
         }
      } else {
         return ParticleTypes.FLAME;
      }
   }

   public static void playSound(Level var0, double var1, double var3, double var5, String var7, float var8, float var9) {
      var0.playSound(
         null, var1, var3, var5, SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("minecraft", var7)), SoundSource.PLAYERS, var8, var9
      );
   }

   public static void serverInventoryTick(WeaponSpec var0, ItemStack var1, Level var2, Entity var3, boolean var4) {
      long var5 = var2.getGameTime();
      if (var0.perkId == 17 && var5 % 50L == 0L) {
         int var11 = GunDataHelper.getMag(var1, var0);
         if (var11 < var0.magSize) {
            GunDataHelper.setInt(var1, "d2_mag", ++var11);
            if (var4) {
               playSound(var2, var3.getX(), var3.getY(), var3.getZ(), "block.note_block.chime", 0.45F, 1.75F);
            }
         }
      }

      if (var0.perkId == 3 && !var4 && GunDataHelper.getInt(var1, "d2_pstack", 0) > 0) {
         GunDataHelper.setInt(var1, "d2_pstack", 0);
      }

      long var7 = GunDataHelper.getLong(var1, "d2_pexp");
      if (var7 > 0L && var5 >= var7) {
         GunDataHelper.setLong(var1, "d2_pexp", 0L);
         if (var0.perkId != 3 && var0.perkId != 5 && var0.perkId != 1) {
            GunDataHelper.setInt(var1, "d2_pstack", 0);
         }
      }

      long var9 = GunDataHelper.getLong(var1, "d2_rel_end");
      if (var9 > 0L && var5 >= var9) {
         GunDataHelper.setLong(var1, "d2_rel_end", 0L);
         int var17 = GunDataHelper.getInt(var1, "d2_rel_mode", 0);
         int var12 = GunDataHelper.getMag(var1, var0);
         int var13 = GunDataHelper.getReserve(var1, var0);
         if (var17 == 2) {
            int var14 = var12 + var13;
            if (var14 >= 4) {
               var14 = 4;
            }

            if (var14 > 0) {
               int var15 = var12 + var13 - var14;
               GunDataHelper.setInt(var1, "d2_mag", 1);
               GunDataHelper.setInt(var1, "d2_res", var15);
               GunDataHelper.setInt(var1, "d2_honed", var14);
               GunDataHelper.setInt(var1, "d2_pstack", var14);
               GunDataHelper.setInt(var1, "d2_rel_mode", 0);
               playSound(var2, var3.getX(), var3.getY(), var3.getZ(), "block.anvil.place", 0.65F, 1.75F);
            }
         } else {
            int var21 = WeaponPerkRegistry.getEffectiveMagSize(var1, var0) - var12;
            if (var21 > 0) {
               if (var0.usesSpecialAmmo) {
                  if (var3 instanceof Player && var13 < var21) {
                     var13 += consumeInventoryBrickIfPresent((Player)var3);
                  }

                  if (var21 > var13) {
                     var21 = var13;
                  }

                  var12 += var21;
                  var13 -= var21;
                  GunDataHelper.setInt(var1, "d2_mag", var12);
                  GunDataHelper.setInt(var1, "d2_res", var13);
               } else {
                  GunDataHelper.setInt(var1, "d2_mag", WeaponPerkRegistry.getEffectiveMagSize(var1, var0));
               }

               WeaponPerkRegistry.onSwappableReloadComplete(var1, var0, var5);
               GunDataHelper.setInt(var1, "d2_honed", 0);
               var17 = var0.perkId;
               if (var17 == 3) {
                  if (GunDataHelper.getInt(var1, "d2_kc_arm", 0) > 0) {
                     GunDataHelper.setInt(var1, "d2_kc_arm", 0);
                     GunDataHelper.setInt(var1, "d2_pstack", 6);
                  }
               } else if ((var17 == 22 || var17 == 23) && GunDataHelper.getInt(var1, "d2_kc_arm", 0) > 0) {
                  GunDataHelper.setInt(var1, "d2_kc_arm", 0);
                  GunDataHelper.setInt(var1, "d2_pstack", 1);
               }

               playSound(var2, var3.getX(), var3.getY(), var3.getZ(), "item.crossbow.loading_end", 0.85F, 1.35F);
            }
         }
      }
   }

   public static int consumeInventoryBrickIfPresent(Player var0) {
      Inventory var1 = var0.getInventory();

      for (int var2 = 0; var2 < var1.getContainerSize(); var2++) {
         ItemStack var3 = var1.getItem(var2);
         if (!var3.isEmpty() && var3.getItem() instanceof SpecialAmmoItem) {
            var3.shrink(1);
            return 10;
         }
      }

      return 0;
   }

   public static void useSpecialAmmoBrick(Player var0, ItemStack var1) {
      Inventory var2 = var0.getInventory();
      boolean var3 = false;

      for (int var4 = 0; var4 < 36; var4++) {
         ItemStack var5 = var2.getItem(var4);
         if (var5.getItem() instanceof DestinyGunItem) {
            WeaponSpec var6 = ((DestinyGunItem)var5.getItem()).spec;
            if (var6.usesSpecialAmmo && var6.perkId != 17) {
               int var7 = GunDataHelper.getReserve(var5, var6);
               if (var6.perkId == 20) {
                  if (var7 >= 120) {
                     continue;
                  }

                  var7 += 30;
               } else {
                  if (var7 >= 28) {
                     continue;
                  }

                  var7 += 8;
               }

               GunDataHelper.setInt(var5, "d2_res", var7);
               var3 = true;
            }
         }
      }

      if (var3) {
         if (!var0.isCreative()) {
            var1.shrink(1);
         }

         playSound(var0.level(), var0.getX(), var0.getY(), var0.getZ(), "entity.experience_orb.pickup", 0.8F, 1.5F);
      }
   }

   public static void decryptExoticEngram(Level var0, Player var1, ItemStack var2) {
      int var3 = (int)(Math.random() * 40.0);
      if (var3 >= 40) {
         var3 = 39;
      }

      ItemStack var4 = ModRegistries.createWeaponStackByIndex(var3);
      if (!var1.addItem(var4)) {
         var1.drop(var4, false);
      }

      if (!var1.isCreative()) {
         var2.shrink(1);
      }

      playSound(var0, var1.getX(), var1.getY(), var1.getZ(), "block.amethyst_block.chime", 1.2F, 1.25F);
   }

   public static void triggerAoE(ServerLevel var0, ServerPlayer var1, double var2, double var4, double var6, double var8, float var10, int var11) {
      var0.sendParticles(getElementParticle(var11), var2, var4 + 0.5, var6, 28, 0.65, 0.55, 0.65, 0.14);
      var0.sendParticles(ParticleTypes.EXPLOSION, var2, var4 + 0.5, var6, 2, 0.2, 0.2, 0.2, 0.02);
      playSound(var0, var2, var4, var6, "entity.generic.explode", 0.85F, 1.35F);
      AABB var12 = new AABB(var2 - var8, var4 - var8, var6 - var8, var2 + var8, var4 + var8, var6 + var8);
      List var13 = var0.getEntities(var1, var12);

      for (int var14 = 0; var14 < var13.size(); var14++) {
         Entity var15 = (Entity)var13.get(var14);
         if (var15 instanceof LivingEntity) {
            LivingEntity var16 = (LivingEntity)var15;
            if (var16.isAlive()) {
               var16.invulnerableTime = 0;
               if (var11 == 1) {
                  var16.igniteForSeconds(4.0F);
               } else if (var11 == 4) {
                  var16.setTicksFrozen(280);
               }

               var16.hurt(var1.damageSources().playerAttack(var1), var10);
            }
         }
      }
   }

   public static void spawnLightning(ServerLevel var0, double var1, double var3, double var5) {
      LightningBolt var7 = (LightningBolt)EntityType.LIGHTNING_BOLT.create(var0);
      if (var7 != null) {
         var7.moveTo(var1, var3, var5);
         var7.setVisualOnly(true);
         var0.addFreshEntity(var7);
      }
   }

   public static void handleGunAction(ServerPlayer var0, byte var1, float var2, float var3, byte var4) {
      ServerLevel var7;
      label196: {
         ItemStack var5 = var0.getItemInHand(InteractionHand.MAIN_HAND);
         if (!var5.isEmpty() && var5.getItem() instanceof DestinyGunItem) {
            WeaponSpec var6 = ((DestinyGunItem)var5.getItem()).spec;
            var7 = var0.serverLevel();
            long var8 = var7.getGameTime();
            if (var1 != 1) {
               if (var1 != 2) {
                  if (GunDataHelper.getLong(var5, "d2_rel_end") > var8) {
                     return;
                  }

                  int var10 = GunDataHelper.getMag(var5, var6);
                  if (var10 > 0) {
                     int var15 = var10--;
                     GunDataHelper.setInt(var5, "d2_mag", var10);
                     float var11 = 1.0F;
                     short var12 = 0;
                     int var13 = var6.perkId;
                     if (var13 == 1) {
                        int var29 = GunDataHelper.getInt(var5, "d2_pstack", 0) + 1;
                        if (var29 >= 7) {
                           var11 = 2.0F;
                           var12 = 1;
                           var29 = 0;
                        }

                        GunDataHelper.setInt(var5, "d2_pstack", var29);
                     } else if (var13 == 3) {
                        int var27 = GunDataHelper.getInt(var5, "d2_pstack", 0);
                        if (var27 > 0) {
                           var11 = 1.35F;
                           var12 = 2;
                           GunDataHelper.setInt(var5, "d2_pstack", --var27);
                        }
                     } else if (var13 == 5) {
                        int var26 = GunDataHelper.getInt(var5, "d2_honed", 0);
                        if (var26 > 1) {
                           var11 = (float)var26;
                           var12 = 4;
                           GunDataHelper.setInt(var5, "d2_honed", 0);
                           GunDataHelper.setInt(var5, "d2_pstack", 0);
                        }
                     } else if (var13 == 7) {
                        if (GunDataHelper.getLong(var5, "d2_pexp") > var8) {
                           var11 = 1.25F;
                           var12 = 8;
                        }
                     } else if (var13 == 8) {
                        int var24 = GunDataHelper.getInt(var5, "d2_pstack", 0);
                        if (var24 > 0) {
                           var11 = 1.5F;
                           var12 = 16;
                           GunDataHelper.setInt(var5, "d2_pstack", --var24);
                        }
                     } else if (var13 == 10) {
                        int var23 = GunDataHelper.getInt(var5, "d2_pstack", 0);
                        if (var23 > 0) {
                           var11 = 1.0F + (float)var23 * 0.12F;
                        }
                     } else if (var13 == 12) {
                        if (GunDataHelper.getLong(var5, "d2_pexp") > var8) {
                           var11 = 1.35F;
                        }
                     } else if (var13 == 14) {
                        if (var4 == 0) {
                           var11 = 1.22F;
                        }
                     } else if (var13 == 16) {
                        if (var4 != 0) {
                           var11 = 1.35F;
                        }
                     } else if (var13 == 18) {
                        if (var15 >= 2) {
                           var12 = 32;
                        } else {
                           var12 = 64;
                        }
                     } else if (var13 == 19) {
                        if (GunDataHelper.getLong(var5, "d2_pexp") > var8) {
                           var11 = 1.25F;
                        }
                     } else if (var13 == 20) {
                        if (GunDataHelper.getInt(var5, "d2_alt", 0) > 0) {
                           var11 = 1.25F;
                        }
                     } else if (var13 != 22 && var13 != 23) {
                        if (var13 == 26) {
                           int var14 = GunDataHelper.getInt(var5, "d2_pstack", 0) + 1;
                           if (var14 >= 4) {
                              var11 = 3.2F;
                              var12 = 4;
                              var14 = 0;
                           }

                           GunDataHelper.setInt(var5, "d2_pstack", var14);
                        } else if (var13 == 27) {
                           int var22 = GunDataHelper.getInt(var5, "d2_pstack", 0);
                           if (var22 > 0) {
                              var11 = 1.0F + (float)var22 * 0.07F;
                           }
                        } else if (var13 == 28 && GunDataHelper.getLong(var5, "d2_pexp") <= var8) {
                           var11 = 1.5F;
                        }
                     } else if (GunDataHelper.getInt(var5, "d2_pstack", 0) > 0) {
                        GunDataHelper.setInt(var5, "d2_pstack", 0);
                        var11 = 1.3F;
                        var12 = 128;
                     }

                     var11 *= BungieBallisticsHelper.computeV200ShotMultiplier(var0, var5, var6, var8);
                     playWeaponFireSound(var7, var0, var6, var12);
                     fireHitscanVolley(var7, var0, var5, var6, var2, var3, var4, var11, var12);
                     if (GunDataHelper.getMag(var5, var6) <= 0 && var6.perkId != 17) {
                        handleGunAction(var0, (byte)1, var2, var3, var4);
                     }

                     return;
                  }
               } else {
                  if (var6.perkId == 20) {
                     int var30 = 1 - GunDataHelper.getInt(var5, "d2_alt", 0);
                     GunDataHelper.setInt(var5, "d2_alt", var30);
                     playSound(var7, var0.getX(), var0.getY(), var0.getZ(), "item.firecharge.use", 0.9F, 1.25F);
                     return;
                  }

                  if (var6.perkId == 5) {
                     if (GunDataHelper.getInt(var5, "d2_honed", 0) >= 4) {
                        return;
                     }

                     if (GunDataHelper.getMag(var5, var6) + GunDataHelper.getReserve(var5, var6) >= 2) {
                        GunDataHelper.setInt(var5, "d2_rel_mode", 2);
                        GunDataHelper.setInt(var5, "d2_rel_dur", 28);
                        GunDataHelper.setLong(var5, "d2_rel_end", var8 + 28L);
                        playSound(var7, var0.getX(), var0.getY(), var0.getZ(), "block.beacon.activate", 0.75F, 1.85F);
                        return;
                     }
                  }
               }
            }

            if (var6.perkId == 17) {
               break label196;
            }

            if (GunDataHelper.getLong(var5, "d2_rel_end") <= var8) {
               int var17 = GunDataHelper.getMag(var5, var6);
               if (var17 < WeaponPerkRegistry.getEffectiveMagSize(var5, var6)) {
                  if (var6.usesSpecialAmmo) {
                     int var19 = GunDataHelper.getReserve(var5, var6);
                     if (var19 <= 0) {
                        var19 = consumeInventoryBrickIfPresent(var0);
                        if (var19 <= 0) {
                           break label196;
                        }

                        GunDataHelper.setInt(var5, "d2_res", var19);
                     }
                  }

                  int var21 = WeaponPerkRegistry.getEffectiveReloadTicks(var5, var6, var8);
                  if (var6.perkId == 14 && GunDataHelper.getInt(var5, "d2_pstack", 0) > 0 || var6.perkId == 19 && GunDataHelper.getLong(var5, "d2_pexp") > var8
                     )
                   {
                     var21 = var21 * 62 / 100;
                  }

                  GunDataHelper.setInt(var5, "d2_rel_mode", 1);
                  GunDataHelper.setInt(var5, "d2_rel_dur", var21);
                  GunDataHelper.setLong(var5, "d2_rel_end", var8 + (long)var21);
                  playSound(var7, var0.getX(), var0.getY(), var0.getZ(), "item.crossbow.loading_start", 0.85F, 1.15F);
                  return;
               }
            }
         }

         return;
      }

      playSound(var7, var0.getX(), var0.getY(), var0.getZ(), "block.dispenser.fail", 0.75F, 1.6F);
   }

   public static void playWeaponFireSound(ServerLevel var0, ServerPlayer var1, WeaponSpec var2, int var3) {
      double var4 = var1.getX();
      double var6 = var1.getY();
      double var8 = var1.getZ();
      if (var2.perkId == 19) {
         playSound(var0, var4, var6, var8, "entity.warden.sonic_boom", 0.65F, 1.55F);
      } else {
         playSound(
            var0,
            var4,
            var6,
            var8,
            "item.crossbow.shoot",
            0.95F,
            var2.archetype != 2 ? (var2.archetype != 3 ? (var2.archetype != 1 ? 1.45F : 0.85F) : 0.58F) : 0.62F
         );
         if ((var3 & 4) != 0) {
            playSound(var0, var4, var6, var8, "entity.lightning_bolt.impact", 1.15F, 1.35F);
         } else if (var2.archetype != 0) {
            playSound(var0, var4, var6, var8, "entity.firework_rocket.blast", 0.85F, 0.75F);
         } else {
            playSound(var0, var4, var6, var8, "entity.firework_rocket.blast", 0.42F, 1.65F);
         }
      }
   }

   public static void fireHitscanVolley(
      ServerLevel var0, ServerPlayer var1, ItemStack var2, WeaponSpec var3, float var4, float var5, int var6, float var7, int var8
   ) {
      Vec3 var9 = var1.getEyePosition();
      long var44 = var0.getGameTime();
      double var10 = (double)WeaponPerkRegistry.getEffectiveRange(var2, var3, var44);
      float var12 = WeaponPerkRegistry.getEffectiveSpread(var2, var3, var6, var44) + BungieBallisticsHelper.computeDynamicBloom(var2, var3, var6, var44);
      int var37 = var3.element;
      if ((var8 & 64) != 0) {
         var37 = 1;
      }

      float var13 = 0.0F;
      int var14 = 0;
      LivingEntity var15 = null;
      Vec3 var16 = var9;

      for (int var17 = 0; var17 < var3.pellets; var17++) {
         float var18 = var4;
         float var19 = var5;
         if (!(var12 <= 0.02F)) {
            if (var3.pellets > 1) {
               double var40;
               float var42;
               if (var17 == 0) {
                  var40 = Math.random() * 6.2831853;
                  var42 = var12 * 0.18F * (float)Math.random();
               } else {
                  var40 = (double)var17 * 6.2831853 / (double)(var3.pellets - 1) + (Math.random() - 0.5) * 0.38;
                  var42 = var12 * (0.62F + (float)Math.random() * 0.38F);
               }

               var18 = var4 + (float)Math.cos(var40) * var42;
               var19 = var5 + (float)Math.sin(var40) * var42;
            } else {
               var18 = (float)((Math.random() - 0.5) * 2.0) * var12 + var4;
               var19 = (float)((Math.random() - 0.5) * 2.0) * var12 + var5;
            }
         }

         double var20 = Math.toRadians((double)var18);
         double var22 = Math.toRadians((double)var19);
         double var24 = -Math.sin(var20) * Math.cos(var22);
         double var26 = -Math.sin(var22);
         double var28 = Math.cos(var20) * Math.cos(var22);
         Vec3 var30 = var9.add(var24 * var10, var26 * var10, var28 * var10);
         Vec3 var31 = var0.clip(new ClipContext(var9, var30, Block.COLLIDER, Fluid.NONE, var1)).getLocation();
         var31 = BungieBallisticsHelper.applyBulletMagnetism(var0, var1, var3, var9, var31, var6, var12);
         Object[] var32 = raycastLivingEntity(var0, var1, var9, var31);
         if (var32 == null) {
            spawnTracerBeam(var0, var9, var31, var37);
            if (var17 == 0) {
               WeaponPerkRegistry.onSwappableWallHit(var0, var1, var2, var3, var31);
               BungieBallisticsHelper.onWallBlockHit(var0, var1, var9, var31);
            }
         } else {
            LivingEntity var33 = (LivingEntity)var32[0];
            Vec3 var34 = (Vec3)var32[1];
            spawnTracerBeam(var0, var9, var34, var37);
            var15 = var33;
            var16 = var34;
            byte var35;
            if (var34.y - var33.getY() >= (double)(var33.getBbHeight() * 0.68F)) {
               var35 = 1;
               var14 |= 1;
            } else if (var33 instanceof VexGoblinEntity && var34.y - var33.getY() >= (double)(var33.getBbHeight() * 0.32F)) {
               var35 = 1;
               var14 |= 1;
            } else {
               var35 = 0;
            }

            float var36 = var3.baseDamage * var7 * WeaponPerkRegistry.computeSwappableDamageMult(var2, var3, var33, var35, var44);
            if (var3.perkId == 25 && var33.getHealth() <= var33.getMaxHealth() * 0.5F) {
               var36 *= 1.45F;
            }

            if (var35 != 0) {
               var36 *= var3.critMult;
            }

            var33.invulnerableTime = 0;
            if (var37 == 1) {
               var33.igniteForSeconds(2.5F);
            } else if (var37 == 4) {
               var33.setTicksFrozen(320);
            }

            var33.hurt(var1.damageSources().playerAttack(var1), var36);
            var13 += var36;
            if (var3.perkId == 8 && (var8 & 16) == 0 && var9.distanceTo(var34) <= 3.8) {
               GunDataHelper.setInt(var2, "d2_pstack", 3);
               GunDataHelper.setLong(var2, "d2_pexp", var0.getGameTime() + 100L);
               var14 |= 4;
            }

            if (!var33.isAlive()) {
               var14 |= 2;
               if (var35 != 0 && var33 instanceof FallenMobEntity) {
                  var0.sendParticles(ParticleTypes.CLOUD, var33.getX(), var33.getEyeY(), var33.getZ(), 28, 0.28, 0.35, 0.28, 0.09);
                  var0.sendParticles(ParticleTypes.END_ROD, var33.getX(), var33.getEyeY(), var33.getZ(), 14, 0.22, 0.3, 0.22, 0.07);
                  playSound(var0, var33.getX(), var33.getEyeY(), var33.getZ(), "entity.generic.extinguish_fire", 0.95F, 1.45F);
               }
            }
         }
      }

      if (var15 == null) {
         if (var3.perkId == 15) {
            GunDataHelper.setInt(var2, "d2_pstack", 0);
         }
      } else {
         WeaponPerkRegistry.onSwappablePostHit(var0, var1, var2, var3, var15, var16, var14 & 1, var14 & 2, var44);
         var14 = applyExoticPerksPostHit(var0, var1, var2, var3, var15, var16, var14, var8, var6) | var14;
         var14 = BungieBallisticsHelper.applyV200PostHitPerks(var0, var1, var2, var3, var15, var16, var14 & 1, var14 & 2, var44) | var14;
         PacketDistributor.sendToPlayer(
            var1, new CombatFeedbackPayload(var13, (byte)var14, (float)var16.x, (float)var16.y, (float)var16.z), new CustomPacketPayload[0]
         );
      }
   }

   public static void spawnTracerBeam(ServerLevel var0, Vec3 var1, Vec3 var2, int var3) {
      double var4 = var1.distanceTo(var2);
      SimpleParticleType var6 = getElementParticle(var3);
      if (!(var4 < 1.2)) {
         for (byte var7 = 2; var7 <= 10; var7 += 2) {
            double var8 = (double)var7 / 10.0;
            double var10 = var1.x + (var2.x - var1.x) * var8;
            double var12 = var1.y + (var2.y - var1.y) * var8;
            double var14 = var1.z + (var2.z - var1.z) * var8;
            var0.sendParticles(var6, var10, var12, var14, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }

      var0.sendParticles(var6, var2.x, var2.y, var2.z, 3, 0.06, 0.06, 0.06, 0.02);
   }

   public static Object[] raycastLivingEntity(ServerLevel var0, ServerPlayer var1, Vec3 var2, Vec3 var3) {
      AABB var4 = new AABB(var2, var3).inflate(1.25);
      List var5 = var0.getEntities(var1, var4);
      double var6 = var2.distanceToSqr(var3);
      LivingEntity var8 = null;
      Vec3 var9 = null;

      for (int var10 = 0; var10 < var5.size(); var10++) {
         Entity var11 = (Entity)var5.get(var10);
         if (var11 instanceof LivingEntity) {
            LivingEntity var12 = (LivingEntity)var11;
            if (var12.isAlive() && !var12.isSpectator()) {
               Optional var13 = var12.getBoundingBox().inflate(0.24).clip(var2, var3);
               if (var13.isPresent()) {
                  Vec3 var14 = (Vec3)var13.get();
                  double var15 = var2.distanceToSqr(var14);
                  if (var15 < var6) {
                     var6 = var15;
                     var8 = var12;
                     var9 = var14;
                  }
               }
            }
         }
      }

      return var8 == null ? null : new Object[]{var8, var9};
   }

   public static int applyExoticPerksPostHit(
      ServerLevel var0, ServerPlayer var1, ItemStack var2, WeaponSpec var3, LivingEntity var4, Vec3 var5, int var6, int var7, int var8
   ) {
      int var9 = var3.perkId;
      int var10 = var6 & 1;
      int var11 = var6 & 2;
      long var12 = var0.getGameTime();
      if ((var7 & 1) != 0) {
         triggerAoE(var0, var1, var5.x, var5.y, var5.z, 8.5, 6.5F, 0);
         return 4;
      } else {
         if (var9 == 2) {
            if (var11 != 0) {
               triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.5, 6.5F, 1);
               return 4;
            }
         } else if (var9 == 3) {
            if (var11 != 0) {
               GunDataHelper.setInt(var2, "d2_kc_arm", 1);
               if (var10 != 0) {
                  GunDataHelper.setLong(var2, "d2_outlaw", var12 + 100L);
                  triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.8, 9.5F, 1);
               }

               return 4;
            }
         } else {
            if (var9 == 4) {
               if (var11 != 0) {
                  triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 5.2, 11.5F, 1);
                  return 4;
               }

               var0.sendParticles(ParticleTypes.FLAME, var5.x, var5.y, var5.z, 8, 0.2, 0.2, 0.2, 0.05);
               return 0;
            }

            if (var9 == 6) {
               if (var10 != 0) {
                  int var21 = GunDataHelper.getInt(var2, "d2_pstack", 0) + 1;
                  GunDataHelper.setLong(var2, "d2_pexp", var12 + 70L);
                  if (var21 < 3) {
                     if (var11 == 0) {
                        GunDataHelper.setInt(var2, "d2_pstack", var21);
                        return 0;
                     }

                     GunDataHelper.setInt(var2, "d2_pstack", var21);
                     spawnLightning(var0, var4.getX(), var4.getY(), var4.getZ());
                     triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.5, 13.0F, 2);
                     return 4;
                  }

                  GunDataHelper.setInt(var2, "d2_pstack", 0);
                  spawnLightning(var0, var4.getX(), var4.getY(), var4.getZ());
                  triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 6.0, 22.0F, 2);
                  return 4;
               }

               GunDataHelper.setInt(var2, "d2_pstack", 0);
               return 0;
            }

            if (var9 == 7) {
               if (var11 != 0 && var10 != 0) {
                  GunDataHelper.setInt(var2, "d2_pstack", 1);
                  GunDataHelper.setLong(var2, "d2_pexp", var12 + 160L);
                  playSound(var0, var1.getX(), var1.getY(), var1.getZ(), "entity.player.attack.sweep", 0.9F, 1.5F);
                  return 4;
               }
            } else if (var9 == 8) {
               if (var11 != 0) {
                  triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.2, 6.5F, 1);
                  return 4;
               }
            } else if (var9 == 10) {
               if (var11 != 0) {
                  int var14 = GunDataHelper.getInt(var2, "d2_pstack", 0) + 1;
                  if (var14 > 5) {
                     var14 = 5;
                  }

                  GunDataHelper.setInt(var2, "d2_pstack", var14);
                  GunDataHelper.setLong(var2, "d2_pexp", var12 + 140L);
                  return 4;
               }
            } else if (var9 == 12) {
               var4.igniteForSeconds(2.5F);
               var0.sendParticles(ParticleTypes.HAPPY_VILLAGER, var5.x, var5.y, var5.z, 6, 0.25, 0.25, 0.25, 0.05);
               if (var11 != 0) {
                  GunDataHelper.setInt(var2, "d2_pstack", 1);
                  GunDataHelper.setLong(var2, "d2_pexp", var12 + 140L);
                  int var16 = GunDataHelper.getMag(var2, var3) + 4;
                  if (var16 > var3.magSize) {
                     var16 = var3.magSize;
                  }

                  GunDataHelper.setInt(var2, "d2_mag", var16);
                  return 4;
               }
            } else if (var9 == 13) {
               if (var11 != 0) {
                  var1.heal(4.0F);
                  if (var10 != 0) {
                     GunDataHelper.setInt(var2, "d2_mag", var3.magSize);
                     playSound(var0, var1.getX(), var1.getY(), var1.getZ(), "entity.experience_orb.pickup", 0.75F, 1.75F);
                  }

                  return 4;
               }
            } else if (var9 == 14) {
               if (var8 == 0) {
                  int var17 = GunDataHelper.getInt(var2, "d2_pstack", 0) + 1;
                  if (var17 > 4) {
                     var17 = 4;
                  }

                  GunDataHelper.setInt(var2, "d2_pstack", var17);
                  GunDataHelper.setLong(var2, "d2_pexp", var12 + 100L);
                  return 4;
               }
            } else {
               if (var9 == 15) {
                  if (var10 != 0) {
                     int var20 = GunDataHelper.getInt(var2, "d2_pstack", 0) + 1;
                     GunDataHelper.setLong(var2, "d2_pexp", var12 + 100L);
                     if (var20 >= 3) {
                        GunDataHelper.setInt(var2, "d2_pstack", 0);
                        GunDataHelper.setInt(var2, "d2_mag", 3);
                        playSound(var0, var1.getX(), var1.getY(), var1.getZ(), "block.beacon.power_select", 0.95F, 1.65F);
                        return 4;
                     }

                     GunDataHelper.setInt(var2, "d2_pstack", var20);
                     return 0;
                  }

                  GunDataHelper.setInt(var2, "d2_pstack", 0);
                  return 0;
               }

               if (var9 == 16) {
                  if (var8 != 0 && var10 != 0) {
                     triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 3.8, 9.0F, 2);
                     return 4;
                  }
               } else if (var9 == 17) {
                  if (var11 != 0) {
                     triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 5.5, 15.0F, 1);
                     return 4;
                  }
               } else {
                  if (var9 == 18) {
                     if ((var7 & 64) != 0) {
                        triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 5.5, 16.0F, 1);
                        return 4;
                     }

                     var4.setTicksFrozen(340);
                     var0.sendParticles(ParticleTypes.SNOWFLAKE, var4.getX(), var4.getY() + 0.8, var4.getZ(), 24, 0.45, 0.55, 0.45, 0.06);
                     playSound(var0, var4.getX(), var4.getY(), var4.getZ(), "block.glass.break", 0.95F, 0.8F);
                     return 4;
                  }

                  if (var9 == 19) {
                     var4.knockback(2.75, var1.getX() - var4.getX(), var1.getZ() - var4.getZ());
                     var0.sendParticles(ParticleTypes.PORTAL, var5.x, var5.y, var5.z, 26, 0.5, 0.5, 0.5, 0.25);
                     GunDataHelper.setInt(var2, "d2_pstack", 1);
                     GunDataHelper.setLong(var2, "d2_pexp", var12 + 100L);
                     return 4;
                  }

                  if (var9 == 20) {
                     if (var11 != 0) {
                        triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.0, 7.5F, 1);
                        return 4;
                     }
                  } else if (var9 != 21 && var9 != 24) {
                     if (var9 != 22 && var9 != 23) {
                        if (var9 >= 25 && var9 <= 28) {
                           if (var9 == 27) {
                              int var18 = GunDataHelper.getInt(var2, "d2_pstack", 0) + 1;
                              if (var18 > 5) {
                                 var18 = 5;
                              }

                              GunDataHelper.setInt(var2, "d2_pstack", var18);
                           }

                           if (var9 == 28) {
                              GunDataHelper.setLong(var2, "d2_pexp", var12 + 65L);
                           }

                           if (var11 != 0) {
                              byte var15 = 2;
                              if (var9 == 25) {
                                 var15 = 12;
                              } else if (var9 == 27) {
                                 var15 = 4;
                              }

                              int var19 = GunDataHelper.getMag(var2, var3) + var15;
                              if (var19 > var3.magSize) {
                                 var19 = var3.magSize;
                              }

                              GunDataHelper.setInt(var2, "d2_mag", var19);
                              return 4;
                           }
                        }
                     } else {
                        if (var11 != 0) {
                           GunDataHelper.setInt(var2, "d2_kc_arm", 1);
                        }

                        if ((var7 & 128) != 0) {
                           spawnLightning(var0, var4.getX(), var4.getY(), var4.getZ());
                           triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 5.2, 12.0F, 2);
                           return 4;
                        }

                        if (var9 == 23) {
                           triggerAoE(var0, var1, var5.x, var5.y, var5.z, 2.6, 4.5F, 2);
                           return 0;
                        }
                     }
                  } else if (var11 != 0 && var10 != 0) {
                     triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.8, 10.5F, 4);
                     return 4;
                  }
               }
            }
         }

         return 0;
      }
   }
}
