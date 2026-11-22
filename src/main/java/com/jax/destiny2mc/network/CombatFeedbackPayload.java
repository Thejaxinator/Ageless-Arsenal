package com.jax.destiny2mc.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public class CombatFeedbackPayload implements CustomPacketPayload {
   public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("destiny2mc", "combat_feedback"));
   public static final StreamCodec STREAM_CODEC = new CombatFeedbackStreamCodec();
   public final float damage;
   public final byte flags;
   public final float hitX;
   public final float hitY;
   public final float hitZ;

   public CombatFeedbackPayload(float var1, byte var2, float var3, float var4, float var5) {
      this.damage = var1;
      this.flags = var2;
      this.hitX = var3;
      this.hitY = var4;
      this.hitZ = var5;
   }

   public Type type() {
      return TYPE;
   }
}
