package com.jax.destiny2mc.client;

import java.util.function.Supplier;

public class StrikeBossLayerSupplier implements Supplier {
   private final int kind;

   public StrikeBossLayerSupplier(int var1) {
      this.kind = var1;
   }

   @Override
   public Object get() {
      int var1 = this.kind;
      if (var1 == 0) {
         return DevilWalkerModel.createBodyLayer();
      } else {
         return var1 == 1 ? SepiksServoModel.createBodyLayer() : VanokArcliteModel.createBodyLayer();
      }
   }
}
