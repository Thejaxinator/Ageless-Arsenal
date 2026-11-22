package com.jax.destiny2mc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class GunsmithCraftStreamCodec implements StreamCodec {
   public Object decode(Object var1) {
      FriendlyByteBuf var2 = (FriendlyByteBuf)var1;
      return new GunsmithCraftPayload(var2.readInt());
   }

   public void encode(Object var1, Object var2) {
      ((FriendlyByteBuf)var1).writeInt(((GunsmithCraftPayload)var2).recipeIndex);
   }
}
