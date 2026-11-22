package com.jax.destiny2mc.network;

import com.jax.destiny2mc.ageless.materials.BenchAccess;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public final class ServerCraftPayloadHandler implements IPayloadHandler<GunsmithCraftPayload> {
    @Override public void handle(GunsmithCraftPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer p && BenchAccess.permits(p)) {
                GunsmithRecipes.tryCraftOnServer(p, payload.recipeIndex);
            }
        });
    }
}
