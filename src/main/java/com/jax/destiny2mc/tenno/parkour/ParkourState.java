package com.jax.destiny2mc.tenno.parkour;

import net.minecraft.core.Direction;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ParkourState {
    public static final int MAX_WALL_LATCH_TICKS = 120;
    public static final int MAX_AIM_GLIDE_TICKS = 60;

    private static final Map<UUID, ParkourState> STATES = new ConcurrentHashMap<>();

    private ParkourPose pose = ParkourPose.NONE;
    private int poseTicks;
    private int bulletJumpCooldown;
    private boolean airJumpAvailable = true;
    private int wallLatchTicks;
    private Direction wallNormal = Direction.NORTH;
    private int aimGlideTicks;
    private boolean dirty;

    public static ParkourState get(UUID playerId) {
        return STATES.computeIfAbsent(playerId, id -> new ParkourState());
    }

    public static void remove(UUID playerId) {
        STATES.remove(playerId);
    }

    public ParkourPose pose() {
        return pose;
    }

    public void setPose(ParkourPose nextPose) {
        if (this.pose != nextPose) {
            this.pose = nextPose;
            this.poseTicks = 0;
            this.dirty = true;
        }
    }

    public int poseTicks() {
        return poseTicks;
    }

    public void incrementPoseTicks() {
        this.poseTicks++;
    }

    public int bulletJumpCooldown() {
        return bulletJumpCooldown;
    }

    public void setBulletJumpCooldown(int ticks) {
        this.bulletJumpCooldown = Math.max(0, ticks);
    }

    public void decrementCooldowns() {
        if (bulletJumpCooldown > 0) {
            bulletJumpCooldown--;
        }
    }

    public boolean airJumpAvailable() {
        return airJumpAvailable;
    }

    public void setAirJumpAvailable(boolean available) {
        this.airJumpAvailable = available;
    }

    public int wallLatchTicks() {
        return wallLatchTicks;
    }

    public void setWallLatchTicks(int ticks) {
        this.wallLatchTicks = Math.max(0, ticks);
    }

    public Direction wallNormal() {
        return wallNormal;
    }

    public void setWallNormal(Direction normal) {
        if (normal != null && this.wallNormal != normal) {
            this.wallNormal = normal;
            this.dirty = true;
        }
    }

    public int aimGlideTicks() {
        return aimGlideTicks;
    }

    public void setAimGlideTicks(int ticks) {
        this.aimGlideTicks = Math.max(0, ticks);
    }

    public void resetAirCharges() {
        this.airJumpAvailable = true;
        this.wallLatchTicks = 0;
        this.aimGlideTicks = 0;
    }

    public boolean consumeDirty() {
        boolean wasDirty = this.dirty;
        this.dirty = false;
        return wasDirty;
    }
}
