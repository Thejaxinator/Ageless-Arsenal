/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.GunsmithFuiScreen;
import com.jax.destiny2mc.client.GunsmithMcScreen;

import com.jax.destiny2mc.network.GunsmithCraftPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientScreenHelper {
    public static void openGunsmith(boolean bl) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            if (bl) {
                minecraft.setScreen((Screen)new GunsmithFuiScreen());
                return;
            }
            minecraft.setScreen((Screen)new GunsmithMcScreen());
        }
    }

    public static void sendCraftPacket(int n) {
        PacketDistributor.sendToServer((CustomPacketPayload)new GunsmithCraftPayload(n), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void openStrikeDirector() {
        Minecraft.getInstance().setScreen(new com.jax.destiny2mc.client.frontier.FieldGuideScreen());
    }
}

