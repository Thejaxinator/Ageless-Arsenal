package com.jax.destiny2mc.client.frontier;
import com.jax.destiny2mc.frontier.*;
import com.jax.destiny2mc.client.FallenStudyRenderer;
import com.jax.destiny2mc.item.DestinyGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FrontierClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
  for(var u:FrontierCatalog.UNITS) {
   var type=FrontierContent.ENTITIES.get(u.id());
   if(u.id().equals("taniks")) e.registerEntityRenderer(type,c->new FallenStudyRenderer(c,110));
   else if(CabalStudyRenderer.supports(u.kind())) e.registerEntityRenderer(type,c->new CabalStudyRenderer(c,u));
   else if(EclipseRenderer.supports(u.kind())) e.registerEntityRenderer(type,c->new EclipseRenderer(c,u));
   else e.registerEntityRenderer(type,c->new FrontierRenderer(c,u));
  }
  e.registerEntityRenderer(FrontierContent.BOLT,c->new ThrownItemRenderer<>(c,.65f,true));
  e.registerEntityRenderer(FrontierContent.HAZARD,NoopRenderer::new);
 }
 @SubscribeEvent public static void hud(RegisterGuiLayersEvent e){e.registerAboveAll(FrontierContent.id("charge_meter"),(g,delta)->{
  var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.screen!=null||!(mc.player.getMainHandItem().getItem() instanceof DestinyGunItem gun)||gun.spec.index<40||FrontierWeaponInput.charge<=0||FrontierWeaponInput.duration<=0)return;
  int x=g.guiWidth()/2,y=g.guiHeight()/2+24;float fill=Math.min(1,FrontierWeaponInput.charge/(float)FrontierWeaponInput.duration);g.fill(x-30,y,x+30,y+3,0xaa17202b);g.fill(x-30,y,x-30+(int)(fill*60),y+3,fill==1?0xffffd383:0xff83d6e4);g.drawCenteredString(mc.font,gun.spec.index==51&&fill==1?"RELEASE":"CHARGING",x,y+7,0xffd9e4e9);
 });}
}
