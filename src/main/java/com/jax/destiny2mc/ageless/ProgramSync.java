package com.jax.destiny2mc.ageless;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** This payload only travels server to client. A client cannot submit damage or program definitions. */
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public record ProgramSync(String definitions) implements CustomPacketPayload {
    public static final Type<ProgramSync> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc","weapon_programs"));
    public static final StreamCodec<ByteBuf,ProgramSync> CODEC=ByteBufCodecs.stringUtf8(120000).map(ProgramSync::new,ProgramSync::definitions);
    public Type<ProgramSync> type(){return TYPE;}
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){
        e.registrar("1").playToClient(TYPE,CODEC,(p,ctx)->ctx.enqueueWork(()->WeaponPrograms.receive(p.definitions)));
    }
}
