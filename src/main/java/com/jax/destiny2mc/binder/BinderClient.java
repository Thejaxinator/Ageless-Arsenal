package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.binder.client.EnergyBinderScreen;
import net.minecraft.client.Minecraft;

/** Only the server's successful block interaction opens the station. The same character
 * snapshot is available read-only in the inventory, regardless of distance from a station. */
public final class BinderClient {
    private BinderClient() {}
    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !(mc.screen instanceof EnergyBinderScreen))
            mc.setScreen(new EnergyBinderScreen());
    }
}
