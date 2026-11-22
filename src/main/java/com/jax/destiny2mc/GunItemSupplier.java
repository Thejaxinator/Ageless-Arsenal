package com.jax.destiny2mc;

import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.function.Supplier;

public class GunItemSupplier implements Supplier {
   private final int idx;

   public GunItemSupplier(int var1) {
      this.idx = var1;
   }

   @Override
   public Object get() {
      return new DestinyGunItem(WeaponSpec.byIndex(this.idx));
   }
}
