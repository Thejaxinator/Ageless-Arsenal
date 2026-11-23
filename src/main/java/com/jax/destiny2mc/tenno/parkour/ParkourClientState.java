package com.jax.destiny2mc.tenno.parkour;

import net.minecraft.core.Direction;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ParkourClientState {
    private static final Map<UUID, Entry> ENTRIES = new ConcurrentHashMap<>();

    private ParkourClientState() {
    }

    public static final class Entry {
        public ParkourPose pose = ParkourPose.NONE;
        public Direction wallNormal = Direction.NORTH;
        public int poseTicks;
        public float blend;
    }

    public static void update(UUID playerId, ParkourPose pose, Direction wallNormal) {
        Entry entry = ENTRIES.computeIfAbsent(playerId, id -> new Entry());
        if (entry.pose != pose) {
            entry.pose = pose;
            entry.poseTicks = 0;
        }
        entry.wallNormal = wallNormal;
    }

    public static Entry get(UUID playerId) {
        return ENTRIES.get(playerId);
    }

    public static void tickAll() {
        for (Entry entry : ENTRIES.values()) {
            entry.poseTicks++;
            float target = entry.pose == ParkourPose.NONE ? 0.0f : 1.0f;
            entry.blend += (target - entry.blend) * 0.35f;
            if (entry.pose == ParkourPose.NONE && entry.blend < 0.01f) {
                entry.blend = 0.0f;
            }
        }
    }

    public static void clear() {
        ENTRIES.clear();
    }
}
