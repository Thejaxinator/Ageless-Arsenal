package com.jax.destiny2mc.tenno.parkour;

import com.jax.destiny2mc.tenno.abilities.WarframeAbilityState;
import com.jax.destiny2mc.tenno.abilities.WarframePassiveAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "destiny2mc")
public final class WarframeParkourEvents {
    private WarframeParkourEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WarframePassiveAbilities.tick(serverPlayer);
            ParkourServerHandler.tickPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (WarframePassiveAbilities.onPlayerLanded(serverPlayer, event.getDistance())
                    || ParkourServerHandler.shouldCancelFallDamage(serverPlayer)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer defender) {
            LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
            float modifiedIncoming = WarframePassiveAbilities.modifyIncomingDamage(defender, attacker, event.getAmount());
            if (modifiedIncoming < 0.0f) {
                event.setCanceled(true);
                return;
            }
            event.setAmount(modifiedIncoming);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getEntity() instanceof LivingEntity target) {
            float modified = WarframePassiveAbilities.modifyOutgoingMeleeDamage(attacker, target, event.getAmount());
            event.setAmount(modified);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer killer && event.getEntity() instanceof LivingEntity victim) {
            WarframePassiveAbilities.onEnemyKilled(killer, victim);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        WarframeAbilityState.remove(event.getEntity().getUUID());
        ParkourState.remove(event.getEntity().getUUID());
    }
}
