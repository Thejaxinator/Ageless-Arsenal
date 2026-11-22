package com.jax.destiny2mc.overdrive;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public record AimIntent(int weapon,boolean held) implements CustomPacketPayload {
    public static final Type<AimIntent> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc","visual_aim"));
    private static final StreamCodec<ByteBuf,AimIntent> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,AimIntent::weapon,ByteBufCodecs.BOOL,AimIntent::held,AimIntent::new);
    public Type<AimIntent> type(){return TYPE;}

    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){
        e.registrar("1").playToServer(TYPE,CODEC,(msg,ctx)->{
            if(ctx.player() instanceof ServerPlayer p)ServerAimState.accept(p,msg);
        });
    }
}
