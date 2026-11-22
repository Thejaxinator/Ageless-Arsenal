package com.jax.destiny2mc.block;

import com.jax.destiny2mc.item.ExoticEngramItem;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DestinyLootChestBlock extends Block implements EntityBlock {
   public final int chestType;
   private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 2.0, 15.0, 13.5, 14.0);

   public DestinyLootChestBlock(int var1) {
      super(Properties.of().strength(3.0F, 6.5F).noOcclusion());
      this.chestType = var1;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder var1) {
      var1.add(new Property[]{HorizontalDirectionalBlock.FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext var1) {
      return (BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, var1.getHorizontalDirection().getOpposite());
   }

   protected VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      return SHAPE;
   }

   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new DestinyLootChestBlockEntity(var1, var2);
   }

   protected boolean triggerEvent(BlockState var1, Level var2, BlockPos var3, int var4, int var5) {
      super.triggerEvent(var1, var2, var3, var4, var5);
      BlockEntity var6 = var2.getBlockEntity(var3);
      return var6 != null ? var6.triggerEvent(var4, var5) : false;
   }

   protected ItemInteractionResult useItemOn(ItemStack var1, BlockState var2, Level var3, BlockPos var4, Player var5, InteractionHand var6, BlockHitResult var7) {
      if (var1.getItem() instanceof ExoticEngramItem) {
         if (!var3.isClientSide) {
            BlockEntity var8 = var3.getBlockEntity(var4);
            if (var8 instanceof DestinyLootChestBlockEntity) {
               if (!var5.isCreative()) {
                  var1.shrink(1);
               }

               ((DestinyLootChestBlockEntity)var8).claimRewardsAndFountain((ServerLevel)var3, var4, this.chestType, true);
            }
         }

         return ItemInteractionResult.sidedSuccess(var3.isClientSide);
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   protected InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      if (!var2.isClientSide && var2.getBlockEntity(var3) instanceof DestinyLootChestBlockEntity var7) {
         if (!var7.opened) {
            var7.claimRewardsAndFountain((ServerLevel)var2, var3, this.chestType, false);
         } else {
            ServerBallisticsEngine.playSound(
               var2, (double)var3.getX() + 0.5, (double)var3.getY() + 0.5, (double)var3.getZ() + 0.5, "block.iron_trapdoor.open", 0.55F, 1.35F
            );
         }
      }

      return InteractionResult.sidedSuccess(var2.isClientSide);
   }

   protected void onRemove(BlockState var1, Level var2, BlockPos var3, BlockState var4, boolean var5) {
      if (!var1.is(var4.getBlock()) && !var2.isClientSide) {
         BlockEntity var6 = var2.getBlockEntity(var3);
         if (var6 instanceof DestinyLootChestBlockEntity) {
            ItemStack var7;
            if (!((DestinyLootChestBlockEntity)var6).opened) {
               var7 = new ItemStack(this);
            } else {
               var7 = new ItemStack(Items.COPPER_INGOT, 2);
            }

            var2.addFreshEntity(new ItemEntity(var2, (double)var3.getX() + 0.5, (double)var3.getY() + 0.5, (double)var3.getZ() + 0.5, var7));
         }
      }

      super.onRemove(var1, var2, var3, var4, var5);
   }
}
