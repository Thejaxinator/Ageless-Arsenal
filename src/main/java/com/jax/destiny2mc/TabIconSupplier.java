package com.jax.destiny2mc;

import java.util.function.Supplier;

public class TabIconSupplier implements Supplier {
   @Override
   public Object get() {
      return ModRegistries.createWeaponStackByIndex(35);
   }
}
