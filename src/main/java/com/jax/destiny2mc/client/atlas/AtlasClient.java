package com.jax.destiny2mc.client.atlas;
import com.jax.destiny2mc.atlas.*;
import com.jax.destiny2mc.frontier.FrontierContent;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class AtlasClient {
 public static final Map<String,AtlasNetwork.Reply> RESULTS=new HashMap<>();
 public static final Map<String,AtlasNetwork.Reply> CONFIRMED=new HashMap<>();
 public static AtlasNetwork.Reply waypoint;
 public static void receive(AtlasNetwork.Reply r){if(r.status().equals("cleared")){waypoint=null;return;}RESULTS.put(r.site(),r);if(r.status().equals("found"))CONFIRMED.put(r.site(),r);if(r.tracked())waypoint=r;}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(AtlasContent.NODE,DungeonNodeRenderer::new);}
 @SubscribeEvent public static void hud(RegisterGuiLayersEvent e){e.registerAboveAll(FrontierContent.id("atlas_waypoint"),(g,d)->{
  var mc=Minecraft.getInstance();if(waypoint==null||mc.player==null||mc.options.hideGui||mc.screen!=null)return;
  int x=g.guiWidth()-182,y=g.guiHeight()-62;g.fill(x-5,y-5,g.guiWidth()-8,y+30,0xbb111b23);var s=DungeonSites.site(waypoint.site());String title=s!=null?s.name():waypoint.site().replace('_',' ');
  g.drawString(mc.font,title,x,y,0xffe1ce9d,false);
  if(!mc.player.level().dimension().location().toString().equals(waypoint.dimension())){g.drawString(mc.font,"Destination: Overworld",x,y+13,0xffaebfcd,false);return;}
  double dx=waypoint.x()+.5-mc.player.getX(),dz=waypoint.z()+.5-mc.player.getZ();int distance=(int)Math.hypot(dx,dz);
  float desired=(float)Math.toDegrees(Math.atan2(-dx,dz));float delta=net.minecraft.util.Mth.wrapDegrees(desired-mc.player.getYRot());
  String bearing=Math.abs(delta)<15?"AHEAD":delta>0?"TURN RIGHT":"TURN LEFT";
  g.drawString(mc.font,(distance<24?"ARRIVED":bearing)+"  |  "+distance+" m",x,y+13,0xffd3e4e9,false);
 });}
}
