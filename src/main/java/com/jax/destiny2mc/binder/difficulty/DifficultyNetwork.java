package com.jax.destiny2mc.binder.difficulty;

import com.jax.destiny2mc.frontier.FrontierContent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class DifficultyNetwork {
    /** An empty key requests the current server state. All other values go through choose(). */
    public record Request(String key,int revision) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(FrontierContent.id("encounter_difficulty_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of(
                (b,v)->{b.writeUtf(v.key,16);b.writeVarInt(v.revision);},
                b->new Request(b.readUtf(16),b.readVarInt()));
        public Type<Request> type(){return TYPE;}
    }
    public record State(String key,int revision,boolean editable,boolean open) implements CustomPacketPayload {
        public static final Type<State> TYPE=new Type<>(FrontierContent.id("encounter_difficulty_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=StreamCodec.of(
                (b,v)->{b.writeUtf(v.key,16);b.writeVarInt(v.revision);b.writeBoolean(v.editable);b.writeBoolean(v.open);},
                b->new State(b.readUtf(16),b.readVarInt(),b.readBoolean(),b.readBoolean()));
        public Type<State> type(){return TYPE;}
    }
    static void sync(ServerPlayer player,boolean open){
        var settings=EncounterSettings.get(player.serverLevel());
        PacketDistributor.sendToPlayer(player,new State(settings.preset().id,settings.revision(),EncounterSettings.mayEdit(player),open));
    }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){
        var r=event.registrar("encounters-1");
        r.playToServer(Request.TYPE,Request.CODEC,(message,context)->context.enqueueWork(()->{
            if(!(context.player() instanceof ServerPlayer player))return;
            if(EncounterSettings.get(player.serverLevel()).choose(player,message.key(),message.revision())){
                // Re-evaluate operator permissions for every viewer on every accepted edit.
                for(var other:player.getServer().getPlayerList().getPlayers())sync(other,false);
            }else sync(player,false);
        }));
        r.playToClient(State.TYPE,State.CODEC,(m,c)->c.enqueueWork(()->DifficultyScreen.receive(m)));
    }
}
