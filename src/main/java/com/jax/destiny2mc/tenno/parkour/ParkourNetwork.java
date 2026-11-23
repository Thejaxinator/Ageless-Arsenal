package com.jax.destiny2mc.tenno.parkour;

import com.jax.destiny2mc.tenno.abilities.WarframeAbilityId;
import com.jax.destiny2mc.tenno.abilities.WarframeActiveAbilities;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

public final class ParkourNetwork {
    private static final String MOD_ID = "destiny2mc";
    private static final String PROTOCOL_VERSION = "1";

    private ParkourNetwork() {
    }

    public record ParkourActionPayload(ParkourPose action, boolean active) implements CustomPacketPayload {
        public static final Type<ParkourActionPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "parkour_action")
        );
        public static final StreamCodec<FriendlyByteBuf, ParkourActionPayload> STREAM_CODEC = StreamCodec.composite(
                ParkourPose.STREAM_CODEC, ParkourActionPayload::action,
                ByteBufCodecs.BOOL, ParkourActionPayload::active,
                ParkourActionPayload::new
        );

        @Override
        public Type<ParkourActionPayload> type() {
            return TYPE;
        }
    }

    public record ParkourStateSyncPayload(UUID playerId, ParkourPose pose, byte wallNormalOrdinal) implements CustomPacketPayload {
        public static final Type<ParkourStateSyncPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "parkour_state_sync")
        );
        public static final StreamCodec<FriendlyByteBuf, ParkourStateSyncPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeUUID(payload.playerId);
                    ParkourPose.STREAM_CODEC.encode(buf, payload.pose);
                    buf.writeByte(payload.wallNormalOrdinal);
                },
                buf -> new ParkourStateSyncPayload(
                        buf.readUUID(),
                        ParkourPose.STREAM_CODEC.decode(buf),
                        buf.readByte()
                )
        );

        @Override
        public Type<ParkourStateSyncPayload> type() {
            return TYPE;
        }
    }

    public record WarframeCastPayload(WarframeAbilityId ability) implements CustomPacketPayload {
        public static final Type<WarframeCastPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "warframe_cast")
        );
        public static final StreamCodec<FriendlyByteBuf, WarframeCastPayload> STREAM_CODEC = StreamCodec.composite(
                WarframeAbilityId.STREAM_CODEC, WarframeCastPayload::ability,
                WarframeCastPayload::new
        );

        @Override
        public Type<WarframeCastPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(ParkourActionPayload.TYPE, ParkourActionPayload.STREAM_CODEC, ParkourNetwork::handleParkourAction);
        registrar.playToServer(WarframeCastPayload.TYPE, WarframeCastPayload.STREAM_CODEC, ParkourNetwork::handleWarframeCast);
        registrar.playToClient(ParkourStateSyncPayload.TYPE, ParkourStateSyncPayload.STREAM_CODEC, ParkourNetwork::handleParkourSync);
    }

    public static void broadcastPose(ServerPlayer player, ParkourState state) {
        Direction normal = state.wallNormal() != null ? state.wallNormal() : Direction.NORTH;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new ParkourStateSyncPayload(player.getUUID(), state.pose(), (byte) normal.ordinal())
        );
    }

    private static void handleParkourAction(ParkourActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ParkourServerHandler.handleAction(serverPlayer, payload.action(), payload.active());
            }
        });
    }

    private static void handleWarframeCast(WarframeCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                WarframeActiveAbilities.tryCast(serverPlayer, payload.ability());
            }
        });
    }

    private static void handleParkourSync(ParkourStateSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ParkourClientPacketHandler.handleSync(payload));
    }
}
