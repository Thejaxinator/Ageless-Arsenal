package com.jax.destiny2mc.tenno.parkour;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum ParkourPose {
    NONE,
    BULLET_JUMP,
    DOUBLE_JUMP,
    AIM_GLIDE,
    WALL_LATCH,
    WALL_HOP,
    SLIDE,
    ROLL;

    public static final StreamCodec<FriendlyByteBuf, ParkourPose> STREAM_CODEC = StreamCodec.of(
            (buf, pose) -> buf.writeByte(pose.ordinal()),
            buf -> {
                int idx = buf.readByte() & 0xFF;
                ParkourPose[] values = values();
                return idx < values.length ? values[idx] : NONE;
            }
    );
}
