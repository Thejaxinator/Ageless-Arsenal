package com.jax.destiny2mc.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class StrikeBarrierBlock extends Block {
   public StrikeBarrierBlock() {
      super(Properties.of().strength(-1.0F, 3600000.0F).noOcclusion());
   }
}
