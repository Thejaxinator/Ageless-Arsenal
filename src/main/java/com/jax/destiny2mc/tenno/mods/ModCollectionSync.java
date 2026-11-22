package com.jax.destiny2mc.tenno.mods;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Full owner-only snapshots also clear stale client state when joining a different world. */
@EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
public record ModCollectionSync(Map<String, Integer> ranks, String revealedId) implements CustomPacketPayload {
    public static final Type<ModCollectionSync> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath("destiny2mc", "mod_collection"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModCollectionSync> CODEC = StreamCodec.of(
        (buffer, packet) -> {
            buffer.writeUtf(packet.revealedId, 64);
            buffer.writeVarInt(packet.ranks.size());
            packet.ranks.forEach((id, rank) -> {
                buffer.writeUtf(id, 64);
                buffer.writeVarInt(rank);
            });
        },
        buffer -> {
            String revealedId = buffer.readUtf(64);
            int size = buffer.readVarInt();
            if (size < 0 || size > 512) throw new IllegalArgumentException("Invalid mod collection size");
            var ranks = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < size; i++) ranks.put(buffer.readUtf(64), buffer.readVarInt());
            return new ModCollectionSync(Map.copyOf(ranks), revealedId);
        });

    @Override public Type<ModCollectionSync> type() { return TYPE; }

    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("weapon-mods-1").playToClient(TYPE, CODEC, (packet, context) ->
            context.enqueueWork(() -> {
                ModCollection.receive(context.player(), packet.ranks);
                if (!packet.revealedId().isEmpty() && packet.ranks().containsKey(packet.revealedId())
                    && WeaponMod.byId(packet.revealedId()) != null)
                    com.jax.destiny2mc.tenno.mods.client.PackReveal.reveal(packet.revealedId());
            }));
    }
}
