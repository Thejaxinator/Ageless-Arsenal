package com.jax.destiny2mc.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public class GunsmithPerkSwapPayload implements CustomPacketPayload {
   public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("destiny2mc", "gunsmith_perk_swap"));
   public static final StreamCodec STREAM_CODEC = new GunsmithPerkSwapStreamCodec();
   public final int invSlot;
   public final int perkSlot;
   public final int perkId;

   public GunsmithPerkSwapPayload(int var1, int var2, int var3) {
      this.invSlot = var1;
      this.perkSlot = var2;
      this.perkId = var3;
   }

   public Type type() {
      return TYPE;
   }
}
