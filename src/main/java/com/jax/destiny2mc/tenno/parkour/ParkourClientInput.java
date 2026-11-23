package com.jax.destiny2mc.tenno.parkour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ParkourClientInput {
    private static boolean prevJumpDown;
    private static boolean prevUseDown;
    private static boolean aimGlideSent;
    private static boolean wallLatchSent;
    private static boolean slideSent;

    private ParkourClientInput() {
    }

    public static void onClientTick() {
        ParkourClientState.tickAll();

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || player.isSpectator() || player.isPassenger()) {
            resetHeldStates();
            return;
        }

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean crouchDown = mc.options.keyShift.isDown();
        boolean useDown = mc.options.keyUse.isDown();
        boolean jumpPressed = jumpDown && !prevJumpDown;

        boolean nearWall = ParkourServerHandler.findAdjacentWall(player) != null;

        if (jumpPressed) {
            if (crouchDown) {
                PacketDistributor.sendToServer(new ParkourNetwork.ParkourActionPayload(ParkourPose.BULLET_JUMP, true));
            } else if (!player.onGround() && nearWall) {
                PacketDistributor.sendToServer(new ParkourNetwork.ParkourActionPayload(ParkourPose.WALL_HOP, true));
            } else if (!player.onGround()) {
                PacketDistributor.sendToServer(new ParkourNetwork.ParkourActionPayload(ParkourPose.DOUBLE_JUMP, true));
            }
        }

        boolean shouldWallLatch = !player.onGround() && useDown && nearWall;
        if (shouldWallLatch != wallLatchSent) {
            wallLatchSent = shouldWallLatch;
            PacketDistributor.sendToServer(new ParkourNetwork.ParkourActionPayload(ParkourPose.WALL_LATCH, shouldWallLatch));
        }

        boolean shouldAimGlide = !player.onGround() && useDown && !nearWall;
        if (shouldAimGlide != aimGlideSent) {
            aimGlideSent = shouldAimGlide;
            PacketDistributor.sendToServer(new ParkourNetwork.ParkourActionPayload(ParkourPose.AIM_GLIDE, shouldAimGlide));
        }

        boolean shouldSlide = player.onGround() && crouchDown && player.isSprinting();
        if (shouldSlide != slideSent) {
            slideSent = shouldSlide;
            PacketDistributor.sendToServer(new ParkourNetwork.ParkourActionPayload(ParkourPose.SLIDE, shouldSlide));
        }

        prevJumpDown = jumpDown;
        prevUseDown = useDown;
    }

    private static void resetHeldStates() {
        prevJumpDown = false;
        prevUseDown = false;
        aimGlideSent = false;
        wallLatchSent = false;
        slideSent = false;
    }
}
