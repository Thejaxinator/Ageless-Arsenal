package com.jax.destiny2mc.ageless.materials;

import com.jax.destiny2mc.block.GunsmithBlock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** These older screens do not open a server container. Retain the same single bench position
 * a vanilla menu would track, and check live reach/block identity on each mutation request. */
@EventBusSubscriber(modid="destiny2mc")
public final class BenchAccess {
    private static final Map<UUID,GlobalPos> OPENED = new HashMap<>();

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void opened(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof GunsmithBlock) {
            OPENED.put(p.getUUID(), GlobalPos.of(p.level().dimension(), event.getPos().immutable()));
        }
    }

    public static boolean permits(ServerPlayer p) {
        GlobalPos bench = OPENED.get(p.getUUID());
        return bench != null && p.isAlive() && !p.isSpectator() && p.level().dimension().equals(bench.dimension())
            && p.canInteractWithBlock(bench.pos(), 1.0)
            && p.level().getBlockState(bench.pos()).getBlock() instanceof GunsmithBlock;
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        OPENED.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { OPENED.clear(); }
}
