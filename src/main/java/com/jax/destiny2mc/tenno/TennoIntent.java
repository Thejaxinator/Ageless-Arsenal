package com.jax.destiny2mc.tenno;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** No client-supplied damage, origin, direction, pellet count or ammo. */
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public record TennoIntent(byte action, boolean aiming) implements CustomPacketPayload {
    public static final Type<TennoIntent> TYPE=new Type<>(TennoGunRegistration.id("tenno_trigger"));
    public static final StreamCodec<ByteBuf,TennoIntent> CODEC=StreamCodec.composite(ByteBufCodecs.BYTE,TennoIntent::action,ByteBufCodecs.BOOL,TennoIntent::aiming,TennoIntent::new);
    public Type<TennoIntent> type(){return TYPE;}
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToServer(TYPE,CODEC,(p,ctx)->{
            if(ctx.player() instanceof ServerPlayer player) TennoBallistics.action(player,p.action,p.aiming);
        });
    }
}
