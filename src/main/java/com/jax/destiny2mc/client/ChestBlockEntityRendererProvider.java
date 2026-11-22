package com.jax.destiny2mc.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class ChestBlockEntityRendererProvider implements BlockEntityRendererProvider {
   public BlockEntityRenderer create(Context var1) {
      return new DestinyLootChestRenderer(var1);
   }
}
