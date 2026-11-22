package com.jax.destiny2mc.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public class GunActionPayload implements CustomPacketPayload {
   public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("destiny2mc", "gun_action"));
   public static final StreamCodec STREAM_CODEC = new GunActionStreamCodec();
   public final byte action;
   public final float yaw;
   public final float pitch;
   public final byte ads;

   public GunActionPayload(byte var1, float var2, float var3, byte var4) {
      this.action = var1;
      this.yaw = var2;
      this.pitch = var3;
      this.ads = var4;
   }

   public Type type() {
      return TYPE;
   }
}
