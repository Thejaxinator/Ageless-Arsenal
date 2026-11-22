package com.jax.destiny2mc.client;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.block.DestinyLootChestBlock;
import com.jax.destiny2mc.block.DestinyLootChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class DestinyLootChestRenderer implements BlockEntityRenderer {
   public DestinyLootChestRenderer(Context var1) {
   }

   private static ItemStack getLidStack(int var0) {
      if (var0 == 0) {
         return new ItemStack((ItemLike)ModRegistries.COSMODROME_CHEST_LID_ITEM.get());
      } else if (var0 == 1) {
         return new ItemStack((ItemLike)ModRegistries.FALLEN_CHEST_LID_ITEM.get());
      } else {
         return var0 == 2
            ? new ItemStack((ItemLike)ModRegistries.AWOKEN_CHEST_LID_ITEM.get())
            : new ItemStack((ItemLike)ModRegistries.LEVIATHAN_CHEST_LID_ITEM.get());
      }
   }

   public void render(BlockEntity var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6) {
      if (var1 instanceof DestinyLootChestBlockEntity var7) {
         BlockState var8 = var7.getBlockState();
         Block var9 = var8.getBlock();
         if (var9 instanceof DestinyLootChestBlock) {
            int var10 = ((DestinyLootChestBlock)var9).chestType;
            Direction var11 = (Direction)var8.getValue(HorizontalDirectionalBlock.FACING);
            float var12 = 0.0F;
            if (var11 == Direction.EAST) {
               var12 = (float) (-Math.PI / 2);
            } else if (var11 == Direction.SOUTH) {
               var12 = (float) Math.PI;
            } else if (var11 == Direction.WEST) {
               var12 = (float) (Math.PI / 2);
            }

            float var13 = var7.getOpenProgress(var2) * 1.22F;
            var3.pushPose();
            var3.translate(0.5F, 0.5F, 0.5F);
            var3.mulPose(new Quaternionf().rotateY(var12));
            var3.translate(0.0F, 0.0625F, 0.375F);
            var3.mulPose(new Quaternionf().rotateX(var13));
            var3.translate(0.0F, -0.0625F, -0.375F);
            Minecraft.getInstance()
               .getItemRenderer()
               .renderStatic(null, getLidStack(var10), ItemDisplayContext.NONE, false, var3, var4, var7.getLevel(), var5, var6, 0);
            var3.popPose();
         }
      }
   }
}
