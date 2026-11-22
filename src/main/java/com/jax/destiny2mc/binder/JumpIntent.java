package com.jax.destiny2mc.binder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** The server accepts a button edge and two digital direction axes. A client
 * supplies no velocity, airtime, jump count, ability ID or collision result. */
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public record JumpIntent(boolean pressed,byte forward,byte left) implements CustomPacketPayload {
    public static final Type<JumpIntent> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc","binder_jump"));
    public static final StreamCodec<ByteBuf,JumpIntent> CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,JumpIntent::pressed,
        ByteBufCodecs.BYTE,JumpIntent::forward,ByteBufCodecs.BYTE,JumpIntent::left,JumpIntent::new);
    public Type<JumpIntent> type(){return TYPE;}
    /** Apply the sampled edge on the server's game thread. */
    public void apply(ServerPlayer player){GuardianMovement.input(player,this);}
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){
        e.registrar("1").playToServer(TYPE,CODEC,(msg,ctx)->ctx.enqueueWork(()->{
            if(ctx.player() instanceof ServerPlayer player)msg.apply(player);
        }));
    }
}
