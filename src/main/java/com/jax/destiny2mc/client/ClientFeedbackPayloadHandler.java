package com.jax.destiny2mc.client;

import com.jax.destiny2mc.network.CombatFeedbackPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class ClientFeedbackPayloadHandler implements IPayloadHandler {
   public void handle(CustomPacketPayload var1, IPayloadContext var2) {
      CombatFeedbackPayload var3 = (CombatFeedbackPayload)var1;
      ClientWeaponState.onCombatFeedback(var3.damage, var3.flags, var3.hitX, var3.hitY, var3.hitZ);
   }
}
