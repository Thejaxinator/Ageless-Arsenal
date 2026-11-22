package com.jax.destiny2mc.client.atlas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class AtlasSession {
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){AtlasClient.RESULTS.clear();AtlasClient.CONFIRMED.clear();AtlasClient.waypoint=null;}
}
