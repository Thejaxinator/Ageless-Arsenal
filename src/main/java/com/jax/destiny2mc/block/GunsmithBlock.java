package com.jax.destiny2mc.block;

import com.jax.destiny2mc.client.ClientScreenHelper;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GunsmithBlock extends Block {
   public final boolean isFui;
   private static final VoxelShape SHAPE_FUI = Block.box(0.5, 0.0, 1.0, 15.5, 16.0, 15.0);
   private static final VoxelShape SHAPE_MC = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public GunsmithBlock(boolean var1) {
      super(Properties.of().strength(3.0F, 6.0F).noOcclusion());
      this.isFui = var1;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder var1) {
      var1.add(new Property[]{HorizontalDirectionalBlock.FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext var1) {
      return (BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, var1.getHorizontalDirection().getOpposite());
   }

   protected List getDrops(BlockState var1, net.minecraft.world.level.storage.loot.LootParams.Builder var2) {
      List var3 = super.getDrops(var1, var2);
      return var3.isEmpty() ? Collections.singletonList(new ItemStack(this)) : var3;
   }

   protected VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      return this.isFui ? SHAPE_FUI : SHAPE_MC;
   }

   protected InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      if (var2.isClientSide) {
         ClientScreenHelper.openGunsmith(this.isFui);
      }

      return InteractionResult.sidedSuccess(var2.isClientSide);
   }
}
