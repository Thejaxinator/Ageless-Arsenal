package com.jax.destiny2mc.tenno.parkour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

public final class ParkourServerHandler {
    private static final Direction[] HORIZONTAL_DIRS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private ParkourServerHandler() {
    }

    public static void handleAction(ServerPlayer player, ParkourPose requestedAction, boolean active) {
        if (player == null || player.isSpectator() || player.isPassenger() || player.isInWater()) {
            return;
        }
        ParkourState state = ParkourState.get(player.getUUID());

        switch (requestedAction) {
            case BULLET_JUMP -> tryBulletJump(player, state);
            case DOUBLE_JUMP -> tryDoubleJump(player, state);
            case WALL_HOP -> tryWallHop(player, state);
            case WALL_LATCH -> setWallLatch(player, state, active);
            case AIM_GLIDE -> setAimGlide(player, state, active);
            case ROLL -> tryRoll(player, state);
            case SLIDE -> state.setPose(active && player.onGround() ? ParkourPose.SLIDE : ParkourPose.NONE);
            case NONE -> state.setPose(ParkourPose.NONE);
        }

        if (state.consumeDirty()) {
            ParkourNetwork.broadcastPose(player, state);
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        if (player == null || !player.isAlive()) {
            return;
        }
        ParkourState state = ParkourState.get(player.getUUID());
        state.decrementCooldowns();
        state.incrementPoseTicks();

        if (player.onGround() || player.isInWater()) {
            state.resetAirCharges();
            if (state.pose() != ParkourPose.SLIDE && state.pose() != ParkourPose.ROLL) {
                state.setPose(ParkourPose.NONE);
            } else if (state.pose() == ParkourPose.ROLL && state.poseTicks() > 10) {
                state.setPose(ParkourPose.NONE);
            }
        } else {
            switch (state.pose()) {
                case BULLET_JUMP -> {
                    if (state.poseTicks() > 16) {
                        state.setPose(ParkourPose.NONE);
                    }
                }
                case DOUBLE_JUMP, WALL_HOP -> {
                    if (state.poseTicks() > 12) {
                        state.setPose(ParkourPose.NONE);
                    }
                }
                case WALL_LATCH -> tickWallLatch(player, state);
                case AIM_GLIDE -> tickAimGlide(player, state);
                case SLIDE, ROLL -> {
                    if (state.poseTicks() > 10) {
                        state.setPose(ParkourPose.NONE);
                    }
                }
                case NONE -> {
                }
            }
        }

        if (state.consumeDirty()) {
            ParkourNetwork.broadcastPose(player, state);
        }
    }

    public static boolean shouldCancelFallDamage(ServerPlayer player) {
        ParkourState state = ParkourState.get(player.getUUID());
        return state.pose() != ParkourPose.NONE || state.bulletJumpCooldown() > 0;
    }

    private static void tryBulletJump(ServerPlayer player, ParkourState state) {
        if (state.bulletJumpCooldown() > 0) {
            return;
        }
        Vec3 look = player.getLookAngle().normalize();
        double vertical = Math.max(0.62, look.y * 1.15 + 0.38);
        player.setDeltaMovement(look.x * 1.18, vertical, look.z * 1.18);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        state.setBulletJumpCooldown(14);
        state.setPose(ParkourPose.BULLET_JUMP);

        ServerLevel level = player.serverLevel();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.0), e -> e != player && e instanceof Enemy)) {
            Vec3 away = target.position().subtract(player.position()).normalize().scale(0.55);
            target.push(away.x, 0.25, away.z);
            target.hurt(level.damageSources().playerAttack(player), 2.5f);
        }
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.4, player.getZ(), 14, 0.4, 0.2, 0.4, 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.75f, 1.35f);
    }

    private static void tryDoubleJump(ServerPlayer player, ParkourState state) {
        if (player.onGround() || !state.airJumpAvailable()) {
            return;
        }
        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(current.x * 1.05, 0.62, current.z * 1.05);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        state.setAirJumpAvailable(false);
        state.setPose(ParkourPose.DOUBLE_JUMP);

        player.serverLevel().sendParticles(ParticleTypes.POOF, player.getX(), player.getY(), player.getZ(), 8, 0.25, 0.05, 0.25, 0.03);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    private static void tryWallHop(ServerPlayer player, ParkourState state) {
        Direction wallDir = findAdjacentWall(player);
        if (wallDir == null) {
            return;
        }
        Direction away = wallDir.getOpposite();
        Vec3 look = player.getLookAngle().normalize();
        double vx = away.getStepX() * 0.42 + look.x * 0.45;
        double vz = away.getStepZ() * 0.42 + look.z * 0.45;
        player.setDeltaMovement(vx, 0.66, vz);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        state.setWallNormal(away);
        state.setAirJumpAvailable(true);
        state.setPose(ParkourPose.WALL_HOP);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.STONE_STEP, SoundSource.PLAYERS, 0.8f, 1.2f);
    }

    private static void setWallLatch(ServerPlayer player, ParkourState state, boolean active) {
        if (!active || player.onGround()) {
            if (state.pose() == ParkourPose.WALL_LATCH) {
                state.setPose(ParkourPose.NONE);
            }
            return;
        }
        Direction wallDir = findAdjacentWall(player);
        if (wallDir != null && state.wallLatchTicks() < ParkourState.MAX_WALL_LATCH_TICKS) {
            state.setWallNormal(wallDir.getOpposite());
            state.setPose(ParkourPose.WALL_LATCH);
        }
    }

    private static void tickWallLatch(ServerPlayer player, ParkourState state) {
        Direction wallDir = findAdjacentWall(player);
        if (wallDir == null || state.wallLatchTicks() >= ParkourState.MAX_WALL_LATCH_TICKS) {
            state.setPose(ParkourPose.NONE);
            return;
        }
        state.setWallLatchTicks(state.wallLatchTicks() + 1);
        player.setDeltaMovement(0.0, 0.0, 0.0);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
    }

    private static void setAimGlide(ServerPlayer player, ParkourState state, boolean active) {
        if (!active || player.onGround()) {
            if (state.pose() == ParkourPose.AIM_GLIDE) {
                state.setPose(ParkourPose.NONE);
            }
            return;
        }
        if (state.aimGlideTicks() < ParkourState.MAX_AIM_GLIDE_TICKS) {
            state.setPose(ParkourPose.AIM_GLIDE);
        }
    }

    private static void tickAimGlide(ServerPlayer player, ParkourState state) {
        if (state.aimGlideTicks() >= ParkourState.MAX_AIM_GLIDE_TICKS) {
            state.setPose(ParkourPose.NONE);
            return;
        }
        state.setAimGlideTicks(state.aimGlideTicks() + 1);
        Vec3 vel = player.getDeltaMovement();
        if (vel.y < -0.07) {
            player.setDeltaMovement(vel.x * 1.02, -0.07, vel.z * 1.02);
            player.hurtMarked = true;
            player.fallDistance = 0.0f;
        }
    }

    private static void tryRoll(ServerPlayer player, ParkourState state) {
        Vec3 flatLook = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z);
        if (flatLook.lengthSqr() < 1.0e-4) {
            return;
        }
        Vec3 dir = flatLook.normalize();
        player.setDeltaMovement(dir.x * 0.92, player.getDeltaMovement().y, dir.z * 0.92);
        player.hurtMarked = true;
        state.setPose(ParkourPose.ROLL);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    public static Direction findAdjacentWall(LivingEntity entity) {
        BlockPos base = entity.blockPosition();
        for (Direction dir : HORIZONTAL_DIRS) {
            BlockPos check = base.relative(dir);
            if (!entity.level().getBlockState(check).getCollisionShape(entity.level(), check).isEmpty()
                    || !entity.level().getBlockState(check.above()).getCollisionShape(entity.level(), check.above()).isEmpty()) {
                return dir;
            }
        }
        return null;
    }
}
