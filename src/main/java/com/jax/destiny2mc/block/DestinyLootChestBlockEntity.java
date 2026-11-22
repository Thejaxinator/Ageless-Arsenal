package com.jax.destiny2mc.block;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DestinyLootChestBlockEntity extends BlockEntity {
   public boolean opened = false;
   public long openStartTick = -1L;

   public DestinyLootChestBlockEntity(BlockPos var1, BlockState var2) {
      super((BlockEntityType)ModRegistries.LOOT_CHEST_BE.get(), var1, var2);
   }

   protected void loadAdditional(CompoundTag var1, Provider var2) {
      super.loadAdditional(var1, var2);
      this.opened = var1.getBoolean("d2_opened");
   }

   protected void saveAdditional(CompoundTag var1, Provider var2) {
      super.saveAdditional(var1, var2);
      var1.putBoolean("d2_opened", this.opened);
   }

   public CompoundTag getUpdateTag(Provider var1) {
      CompoundTag var2 = super.getUpdateTag(var1);
      var2.putBoolean("d2_opened", this.opened);
      return var2;
   }

   public Packet getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public boolean triggerEvent(int var1, int var2) {
      if (var1 == 1) {
         this.opened = true;
         Level var3 = this.getLevel();
         if (var3 != null) {
            this.openStartTick = var3.getGameTime();
         }

         return true;
      } else {
         return super.triggerEvent(var1, var2);
      }
   }

   public float getOpenProgress(float var1) {
      if (!this.opened) {
         return 0.0F;
      } else {
         long var2 = this.openStartTick;
         if (var2 >= 0L) {
            Level var4 = this.getLevel();
            if (var4 != null) {
               float var5 = ((float)(var4.getGameTime() - var2) + var1) / 18.0F;
               if (var5 <= 0.0F) {
                  return 0.0F;
               }

               if (!(var5 >= 1.0F)) {
                  float var6 = 1.0F - var5;
                  return 1.0F - var6 * var6 * var6;
               }
            }
         }

         return 1.0F;
      }
   }

   private static void launchFountainStack(ServerLevel var0, double var1, double var3, double var5, ItemStack var7, double var8) {
      if (!var7.isEmpty()) {
         ItemEntity var10 = new ItemEntity(var0, var1, var3, var5, var7);
         var10.setDeltaMovement(Math.cos(var8) * 0.11, 0.32, Math.sin(var8) * 0.11);
         var0.addFreshEntity(var10);
      }
   }

   public void claimRewardsAndFountain(ServerLevel var1, BlockPos var2, int var3, boolean var4) {
      this.opened = true;
      this.setChanged();
      var1.blockEvent(var2, this.getBlockState().getBlock(), 1, 1);
      double var5 = (double)var2.getX() + 0.5;
      double var7 = (double)var2.getY() + 0.85;
      double var9 = (double)var2.getZ() + 0.5;
      ServerBallisticsEngine.playSound(var1, var5, var7, var9, "block.ender_chest.open", 0.95F, 1.18F);
      ServerBallisticsEngine.playSound(var1, var5, var7, var9, "block.beacon.power_select", 0.85F, 1.42F);
      var1.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, var5, var7, var9, 36, 0.35, 0.45, 0.35, 0.16);
      var1.sendParticles(ParticleTypes.END_ROD, var5, var7, var9, 24, 0.3, 0.4, 0.3, 0.1);
      launchFountainStack(var1, var5, var7, var9, new ItemStack((ItemLike)ModRegistries.SPECIAL_AMMO.get(), 10), 0.0);
      int var11;
      if (var3 == 0) {
         var11 = (int)(Math.random() * 10.0);
         launchFountainStack(var1, var5, var7, var9, new ItemStack(Items.GOLD_INGOT, 5), 2.1);
         launchFountainStack(var1, var5, var7, var9, new ItemStack((ItemLike)ModRegistries.PLUNDERED_ETHER_ALLOY.get(), 3), 4.2);
      } else if (var3 == 1) {
         var11 = 20 + (int)(Math.random() * 4.0);
         launchFountainStack(var1, var5, var7, var9, new ItemStack(Items.COPPER_INGOT, 10), 2.1);
         launchFountainStack(var1, var5, var7, var9, new ItemStack((ItemLike)ModRegistries.PLUNDERED_ETHER_ALLOY.get(), 4), 4.2);
      } else if (var3 == 2) {
         var11 = 10 + (int)(Math.random() * 10.0);
         launchFountainStack(var1, var5, var7, var9, new ItemStack(Items.AMETHYST_SHARD, 8), 2.1);
         launchFountainStack(var1, var5, var7, var9, new ItemStack(Items.DIAMOND, 3), 4.2);
      } else {
         var11 = 24 + (int)(Math.random() * 4.0);
         launchFountainStack(var1, var5, var7, var9, new ItemStack((ItemLike)ModRegistries.EXOTIC_ENGRAM.get(), 1), 2.1);
         launchFountainStack(var1, var5, var7, var9, new ItemStack(Items.GOLD_INGOT, 8), 4.2);
      }

      if (var11 > 27) {
         var11 = 27;
      }

      launchFountainStack(var1, var5, var7, var9, GunsmithRecipes.createResultStack(var11), 1.05);
      if (var4) {
         int var12 = (int)(Math.random() * 28.0);
         if (var12 > 27) {
            var12 = 27;
         }

         launchFountainStack(var1, var5, var7, var9, GunsmithRecipes.createResultStack(var12), 5.25);
      }
   }
}
