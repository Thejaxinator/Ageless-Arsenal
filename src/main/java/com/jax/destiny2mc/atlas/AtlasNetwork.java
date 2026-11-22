package com.jax.destiny2mc.atlas;
import com.jax.destiny2mc.frontier.FrontierContent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class AtlasNetwork {
 public record Request(int action,String site) implements CustomPacketPayload {
  public static final Type<Request> TYPE=new Type<>(FrontierContent.id("atlas_request"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.action);b.writeUtf(v.site,64);},b->new Request(b.readVarInt(),b.readUtf(64)));
  @Override public Type<Request> type(){return TYPE;}
 }
 public record Reply(String site,String status,int x,int y,int z,String dimension,String message,boolean tracked) implements CustomPacketPayload {
  public static final Type<Reply> TYPE=new Type<>(FrontierContent.id("atlas_reply"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Reply> CODEC=StreamCodec.of((b,v)->{b.writeUtf(v.site,64);b.writeUtf(v.status,32);b.writeInt(v.x);b.writeInt(v.y);b.writeInt(v.z);b.writeUtf(v.dimension,128);b.writeUtf(v.message,512);b.writeBoolean(v.tracked);},b->new Reply(b.readUtf(64),b.readUtf(32),b.readInt(),b.readInt(),b.readInt(),b.readUtf(128),b.readUtf(512),b.readBoolean()));
  @Override public Type<Reply> type(){return TYPE;}
 }
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("atlas-1");r.playToServer(Request.TYPE,Request.CODEC,(m,c)->{if(c.player() instanceof ServerPlayer p)AtlasLocator.request(p,m);});r.playToClient(Reply.TYPE,Reply.CODEC,(m,c)->com.jax.destiny2mc.client.atlas.AtlasClient.receive(m));}
}
