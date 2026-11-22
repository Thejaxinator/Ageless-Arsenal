package com.jax.destiny2mc.binder.progression;

import com.jax.destiny2mc.binder.client.BinderProgressView;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Clients can only receive XP. A single lifetime counter determines every sidebar value. */
@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD)
public record CareerSync(int total) implements CustomPacketPayload {
    public static final Type<CareerSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc", "career_xp"));
    public static final StreamCodec<ByteBuf, CareerSync> CODEC = ByteBufCodecs.VAR_INT.map(CareerSync::new, CareerSync::total);
    @Override public Type<CareerSync> type() { return TYPE; }

    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (packet, ctx) -> ctx.enqueueWork(() -> {
            int total = CareerCurve.clampPoints(packet.total());
            int level = CareerCurve.level(total);
            int floor = CareerCurve.threshold(level);
            int cost = CareerCurve.threshold(level + 1) - floor;
            BinderProgressView.publish(new BinderProgressView.Info(level, total - floor, cost,
                CareerCurve.healthBonus(level), CareerCurve.armorBonus(level), CareerCurve.veteranChance(level)));
        }));
    }
}
