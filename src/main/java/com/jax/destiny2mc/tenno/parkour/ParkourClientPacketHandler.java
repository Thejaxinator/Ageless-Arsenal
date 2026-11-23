package com.jax.destiny2mc.tenno.parkour;

import net.minecraft.core.Direction;

public final class ParkourClientPacketHandler {
    private ParkourClientPacketHandler() {
    }

    public static void handleSync(ParkourNetwork.ParkourStateSyncPayload payload) {
        Direction[] dirs = Direction.values();
        int idx = payload.wallNormalOrdinal() & 0xFF;
        Direction normal = idx < dirs.length ? dirs[idx] : Direction.NORTH;
        ParkourClientState.update(payload.playerId(), payload.pose(), normal);
    }
}
