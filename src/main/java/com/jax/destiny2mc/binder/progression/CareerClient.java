package com.jax.destiny2mc.binder.progression;

import com.jax.destiny2mc.binder.client.BinderProgressView;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid="destiny2mc", value=Dist.CLIENT)
public final class CareerClient {
    private CareerClient() {}
    @SubscribeEvent public static void disconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        BinderProgressView.publish(null);
    }
}
