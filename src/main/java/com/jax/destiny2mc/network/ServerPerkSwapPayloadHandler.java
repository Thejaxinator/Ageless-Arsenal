package com.jax.destiny2mc.network;

import com.jax.destiny2mc.ageless.materials.BenchAccess;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public final class ServerPerkSwapPayloadHandler implements IPayloadHandler<GunsmithPerkSwapPayload> {
    @Override public void handle(GunsmithPerkSwapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer p && BenchAccess.permits(p)) {
                WeaponPerkRegistry.serverTrySocketPerk(p, payload.invSlot, payload.perkSlot, payload.perkId);
            }
        });
    }
}
