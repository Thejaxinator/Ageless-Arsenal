package com.jax.destiny2mc;

import com.jax.destiny2mc.block.DestinyLootChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

public class LootChestBeSupplier implements BlockEntitySupplier {
   public BlockEntity create(BlockPos var1, BlockState var2) {
      return new DestinyLootChestBlockEntity(var1, var2);
   }
}
