package com.jax.destiny2mc.client;

import java.util.function.Supplier;

public class V200FactionLayerSupplier implements Supplier {
   private final int rig;

   public V200FactionLayerSupplier(int var1) {
      this.rig = var1;
   }

   @Override
   public Object get() {
      return V200FactionModel.createBodyLayer(this.rig);
   }
}
