package com.jax.destiny2mc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class GunActionStreamCodec implements StreamCodec {
   public Object decode(Object var1) {
      FriendlyByteBuf var2 = (FriendlyByteBuf)var1;
      return new GunActionPayload(var2.readByte(), var2.readFloat(), var2.readFloat(), var2.readByte());
   }

   public void encode(Object var1, Object var2) {
      FriendlyByteBuf var3 = (FriendlyByteBuf)var1;
      GunActionPayload var4 = (GunActionPayload)var2;
      var3.writeByte(var4.action);
      var3.writeFloat(var4.yaw);
      var3.writeFloat(var4.pitch);
      var3.writeByte(var4.ads);
   }
}
