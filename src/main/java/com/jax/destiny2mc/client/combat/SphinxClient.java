package com.jax.destiny2mc.client.combat;
import com.jax.destiny2mc.combat.SphinxContent;
import com.jax.destiny2mc.client.model.ConvertedRig45;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SphinxClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(SphinxContent.ENTITY,c->new CombatRenderer(c,ConvertedRig45.create().bakeRoot(),45,"vex_sphinx"));}
}
