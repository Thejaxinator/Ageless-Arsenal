package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.BinderClientState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="destiny2mc", value=Dist.CLIENT, bus=EventBusSubscriber.Bus.MOD)
public final class BinderKeys {
    private static final int[] DEFAULTS={GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_V};
    private static final KeyMapping[] KEYS=new KeyMapping[4];

    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) {
        for (int i=0;i<KEYS.length;i++) {
            KEYS[i]=new KeyMapping("key.destiny2mc.binder.slot"+(i+1),DEFAULTS[i],"key.categories.destiny2mc");
            event.register(KEYS[i]);
        }
    }
    public static String keyName(int slot) {
        return KEYS[slot]==null ? Integer.toString(slot+1) : KEYS[slot].getTranslatedKeyMessage().getString();
    }

    @EventBusSubscriber(modid="destiny2mc", value=Dist.CLIENT)
    public static final class Input {
        @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
            Minecraft mc=Minecraft.getInstance();
            for (int i=0;i<KEYS.length;i++) {
                KeyMapping key=KEYS[i];
                // Drain GUI and paused clicks too, so closing a menu never fires a queued cast.
                while (key!=null && key.consumeClick()) {
                    if (mc.screen==null && mc.player!=null && mc.level!=null && !mc.isPaused())
                        BinderClientState.cast(i);
                }
            }
        }
    }
}
