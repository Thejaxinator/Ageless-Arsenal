package com.jax.destiny2mc.ageless.fx;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** One bounded visual instruction for the entire effect, independent of mesh detail or distance. */
@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD)
public record VoxelCue(ResourceLocation dimension, Kind kind, EffectProfile profile, Vec3 start, Vec3 end,
                       float radius, int seed) implements CustomPacketPayload {
    public enum Kind { TRACE, BURST, RING, BLINK_OUT, BLINK_IN, CLOUD }
    public static final Type<VoxelCue> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("destiny2mc","voxel_cue"));
    public static final StreamCodec<RegistryFriendlyByteBuf,VoxelCue> CODEC = CustomPacketPayload.codec(VoxelCue::write, VoxelCue::read);
    public Type<VoxelCue> type(){ return TYPE; }

    private static VoxelCue read(RegistryFriendlyByteBuf buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        Kind kind = buf.readEnum(Kind.class);
        EffectProfile profile = buf.readEnum(EffectProfile.class);
        Vec3 start = buf.readVec3();
        // Small relative floats retain sub-block endpoint precision at the world border.
        Vec3 end = start.add(buf.readFloat(), buf.readFloat(), buf.readFloat());
        return new VoxelCue(dimension, kind, profile, start, end, buf.readFloat(), buf.readInt());
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension); buf.writeEnum(kind); buf.writeEnum(profile); buf.writeVec3(start);
        Vec3 delta=end.subtract(start);
        buf.writeFloat((float)delta.x); buf.writeFloat((float)delta.y); buf.writeFloat((float)delta.z);
        buf.writeFloat(radius); buf.writeInt(seed);
    }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        // Client handler is reached exclusively by playToClient on the rendering distribution.
        event.registrar("1").playToClient(TYPE,CODEC,
            (cue,ctx)->com.jax.destiny2mc.ageless.fx.client.WorldEffects.receive(cue));
    }
}
