package com.jax.destiny2mc.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public class GunsmithCraftPayload implements CustomPacketPayload {
   public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("destiny2mc", "gunsmith_craft"));
   public static final StreamCodec STREAM_CODEC = new GunsmithCraftStreamCodec();
   public final int recipeIndex;

   public GunsmithCraftPayload(int var1) {
      this.recipeIndex = var1;
   }

   public Type type() {
      return TYPE;
   }
}
