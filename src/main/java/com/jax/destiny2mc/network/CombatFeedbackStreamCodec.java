package com.jax.destiny2mc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class CombatFeedbackStreamCodec implements StreamCodec {
   public Object decode(Object var1) {
      FriendlyByteBuf var2 = (FriendlyByteBuf)var1;
      return new CombatFeedbackPayload(var2.readFloat(), var2.readByte(), var2.readFloat(), var2.readFloat(), var2.readFloat());
   }

   public void encode(Object var1, Object var2) {
      FriendlyByteBuf var3 = (FriendlyByteBuf)var1;
      CombatFeedbackPayload var4 = (CombatFeedbackPayload)var2;
      var3.writeFloat(var4.damage);
      var3.writeByte(var4.flags);
      var3.writeFloat(var4.hitX);
      var3.writeFloat(var4.hitY);
      var3.writeFloat(var4.hitZ);
   }
}
