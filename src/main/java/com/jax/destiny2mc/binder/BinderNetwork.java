package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.tenno.parkour.ParkourNetwork;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Request carries intent only. Block position is recorded during genuine server-side block use.
 * Client packets never set prices, balances, equipped data or effects. */
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class BinderNetwork {
    static final int REFRESH=0, PURCHASE=1, EQUIP=2, CAST=3, CLOSE=4;
    record Request(int op,int slot,String id) implements CustomPacketPayload {
        static final Type<Request> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc","binder_intent"));
        static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of(
            (b,r)->{b.writeVarInt(r.op);b.writeVarInt(r.slot);b.writeUtf(r.id,48);},
            b->new Request(b.readVarInt(),b.readVarInt(),b.readUtf(48)));
        public Type<Request> type(){return TYPE;}
    }

    record Reply(BinderSnapshot data,boolean open) implements CustomPacketPayload {
        static final Type<Reply> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc","binder_character"));
        static final StreamCodec<RegistryFriendlyByteBuf,Reply> CODEC=StreamCodec.of(
            (b,r)->{
                var s=r.data;
                b.writeBoolean(r.open);b.writeVarInt(s.unlocked().size());
                for(String id:s.unlocked()) b.writeUtf(id,48);
                for(String id:s.active()) b.writeUtf(id,48);
                for(String id:s.passive()) b.writeUtf(id,48);
                b.writeVarInt(s.readyAt().size());
                for(var e:s.readyAt().entrySet()){b.writeUtf(e.getKey(),48);b.writeVarLong(e.getValue());}
                b.writeVarInt(s.motes());b.writeVarInt(s.levels());b.writeUtf(s.message(),256);
            },
            b->{
                boolean open=b.readBoolean();
                var unlocked=new LinkedHashSet<String>();
                for(int n=boundedCount(b);n>0;n--) unlocked.add(b.readUtf(48));
                var active=new ArrayList<String>();var passive=new ArrayList<String>();
                for(int i=0;i<4;i++) active.add(b.readUtf(48));
                for(int i=0;i<2;i++) passive.add(b.readUtf(48));
                var ready=new LinkedHashMap<String,Long>();
                for(int n=boundedCount(b);n>0;n--) ready.put(b.readUtf(48),b.readVarLong());
                return new Reply(new BinderSnapshot(unlocked,active,passive,ready,b.readVarInt(),b.readVarInt(),b.readUtf(256)),open);
            });
        private static int boundedCount(RegistryFriendlyByteBuf b){
            int n=b.readVarInt();if(n<0||n>512)throw new IllegalArgumentException("Invalid binder field length");return n;
        }
        public Type<Reply> type(){return TYPE;}
    }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){
        var registrar=e.registrar("binder-1");
        registrar.playToServer(Request.TYPE,Request.CODEC,(r,ctx)->{
            if(ctx.player() instanceof ServerPlayer p) BinderState.handle(p,r);
        });
        registrar.playToClient(Reply.TYPE,Reply.CODEC,(r,ctx)->BinderClientState.receive(r.data(),r.open()));
        ParkourNetwork.register(e);
    }
}
