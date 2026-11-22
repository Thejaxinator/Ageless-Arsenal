package com.jax.destiny2mc.tenno.foundry;

import com.jax.destiny2mc.ageless.materials.BenchAccess;
import com.jax.destiny2mc.tenno.client.TennoFoundryPage;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** A request identifies the intended weapon. Its price and output are always server-owned. */
@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD)
public final class FoundryNetwork {
    private static final Map<UUID,Long> LAST_REQUEST = new ConcurrentHashMap<>();
    private FoundryNetwork() {}

    public record Assemble(ResourceLocation item, long serial) implements CustomPacketPayload {
        public static final Type<Assemble> TYPE = new Type<>(key("tenno_assemble"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Assemble> CODEC = StreamCodec.of(
            (buf,p) -> { buf.writeResourceLocation(p.item()); buf.writeVarLong(p.serial()); },
            buf -> new Assemble(buf.readResourceLocation(), buf.readVarLong()));
        public Type<Assemble> type() { return TYPE; }
    }

    public record Result(long serial, boolean success) implements CustomPacketPayload {
        public static final Type<Result> TYPE = new Type<>(key("tenno_assembly_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Result> CODEC = StreamCodec.of(
            (buf,p) -> { buf.writeVarLong(p.serial()); buf.writeBoolean(p.success()); },
            buf -> new Result(buf.readVarLong(),buf.readBoolean()));
        public Type<Result> type() { return TYPE; }
    }

    private static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", path);
    }

    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        var registration=event.registrar("tenno-foundry-1");
        registration.playToServer(Assemble.TYPE, Assemble.CODEC, (message,context) ->
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                long last = LAST_REQUEST.getOrDefault(player.getUUID(), 0L);
                if (message.serial() <= last) return;
                LAST_REQUEST.put(player.getUUID(), message.serial());
                Item item = BuiltInRegistries.ITEM.get(message.item());
                boolean ok = player.containerMenu == player.inventoryMenu && BenchAccess.permits(player)
                    && TennoBlueprints.isTenno(item)
                    && TennoBlueprints.craft(player,item);
                PacketDistributor.sendToPlayer(player,new Result(message.serial(),ok));
            }));
        registration.playToClient(Result.TYPE, Result.CODEC, (message,context) ->
            context.enqueueWork(() -> TennoFoundryPage.receive(message)));
    }

    @EventBusSubscriber(modid="destiny2mc")
    public static final class Connections {
        @SubscribeEvent public static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
            LAST_REQUEST.remove(event.getEntity().getUUID());
        }
        @SubscribeEvent public static void stop(ServerStoppedEvent event) { LAST_REQUEST.clear(); }
    }
}
