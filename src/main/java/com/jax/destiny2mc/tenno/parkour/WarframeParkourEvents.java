package com.jax.destiny2mc.tenno.parkour;

import com.jax.destiny2mc.tenno.abilities.WarframeAbilityState;
import com.jax.destiny2mc.tenno.abilities.WarframePassiveAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class WarframeParkourEvents {
    private WarframeParkourEvents() {
    }

    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WarframePassiveAbilities.tick(serverPlayer);
            ParkourServerHandler.tickPlayer(serverPlayer);
        }
    }

    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && ParkourServerHandler.shouldCancelFallDamage(serverPlayer)) {
            event.setCanceled(true);
        }
    }

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer defender) {
            LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
            if (WarframePassiveAbilities.onPlayerDamaged(defender, attacker)) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getEntity() instanceof LivingEntity target) {
            float modified = WarframePassiveAbilities.modifyOutgoingMeleeDamage(attacker, target, event.getAmount());
            event.setAmount(modified);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        WarframeAbilityState.remove(event.getEntity().getUUID());
        ParkourState.remove(event.getEntity().getUUID());
    }
}
