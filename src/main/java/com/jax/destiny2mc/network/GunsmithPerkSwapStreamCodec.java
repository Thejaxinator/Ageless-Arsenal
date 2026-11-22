package com.jax.destiny2mc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class GunsmithPerkSwapStreamCodec implements StreamCodec {
   public Object decode(Object var1) {
      FriendlyByteBuf var2 = (FriendlyByteBuf)var1;
      return new GunsmithPerkSwapPayload(var2.readInt(), var2.readInt(), var2.readInt());
   }

   public void encode(Object var1, Object var2) {
      FriendlyByteBuf var3 = (FriendlyByteBuf)var1;
      GunsmithPerkSwapPayload var4 = (GunsmithPerkSwapPayload)var2;
      var3.writeInt(var4.invSlot);
      var3.writeInt(var4.perkSlot);
      var3.writeInt(var4.perkId);
   }
}
