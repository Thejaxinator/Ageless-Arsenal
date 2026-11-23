package com.jax.destiny2mc.tenno.abilities;

import com.jax.destiny2mc.binder.BinderState;
import com.jax.destiny2mc.binder.WarframeBinderCatalog;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/** Warframe passives are equipped in the Energy Binder's shared passive slots,
 * while active ability buff timers always tick for any player who cast them. */
public final class WarframePassiveAbilities {
    private WarframePassiveAbilities() {
    }

    public static void tick(ServerPlayer player) {
        if (player == null || player.isSpectator() || !player.isAlive()) {
            return;
        }
        WarframeAbilityState state = WarframeAbilityState.get(player.getUUID());
        state.tickTimers();
        WarframeActiveAbilities.tickActiveEffects(player);

        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_URIEL)) {
            if (player.isOnFire()) {
                player.clearFire();
            }
            if (player.tickCount % 40 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false));
            }
        }
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_EXCALIBUR)) {
            if (player.getMainHandItem().getItem() instanceof SwordItem && player.tickCount % 20 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 35, 0, true, false));
            }
        }
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_MAG)) {
            if (player.tickCount % 5 == 0) {
                Vec3 center = player.position().add(0.0, 0.6, 0.0);
                for (ItemEntity item : player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(6.0))) {
                    if (!item.hasPickUpDelay()) {
                        Vec3 pull = center.subtract(item.position()).normalize().scale(0.32);
                        item.setDeltaMovement(pull);
                    }
                }
            }
        }
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VOLT)) {
            Vec3 pos = player.position();
            Vec3 prev = state.lastPosition();
            if (prev != null && player.onGround()) {
                double horizontalDist = Math.hypot(pos.x - prev.x, pos.z - prev.z);
                if (horizontalDist > 0.01 && horizontalDist < 4.0) {
                    state.addVoltStaticCharge((float) (horizontalDist * 6.0));
                }
            }
            state.setLastPosition(pos);
        }
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VORUNA)) {
            if (player.tickCount % 20 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 35, 0, true, false));
            }
        }
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VALKYR)) {
            if (player.fallDistance > 3.0f) {
                player.fallDistance = 0.0f;
            }
        }
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_FOLLIE)) {
            if (player.isSprinting() && player.tickCount % 6 == 0) {
                player.serverLevel().sendParticles(
                        ParticleTypes.SQUID_INK,
                        player.getX(), player.getY() + 0.15, player.getZ(),
                        1, 0.15, 0.05, 0.15, 0.0
                );
            }
        }
    }

    public static float modifyOutgoingMeleeDamage(ServerPlayer attacker, LivingEntity target, float baseDamage) {
        WarframeAbilityState state = WarframeAbilityState.get(attacker.getUUID());
        float damage = baseDamage;

        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_EXCALIBUR)
                && attacker.getMainHandItem().getItem() instanceof SwordItem) {
            damage *= 1.15f;
        }
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_KULLERVO)) {
            damage *= 1.15f;
        }
        if (state.wrathfulCritTicks() > 0) {
            damage *= 1.6f;
        }
        if (state.shroudTicks() > 0) {
            damage *= 1.5f;
            state.setShroudTicks(0);
            attacker.removeEffect(MobEffects.INVISIBILITY);
        }
        if (state.warcryTicks() > 0) {
            attacker.heal(Math.min(2.0f, damage * 0.12f));
        }
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_VOLT)
                && state.voltStaticCharge() >= 15.0f) {
            float bonus = state.consumeVoltStaticCharge() * 0.08f;
            damage += bonus;
            ServerLevel level = attacker.serverLevel();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.08);
        }
        return damage;
    }

    public static boolean onPlayerDamaged(ServerPlayer defender, LivingEntity attacker) {
        if (BinderState.hasPassive(defender, WarframeBinderCatalog.PASSIVE_NYX)
                && defender.getRandom().nextFloat() < 0.20f) {
            defender.serverLevel().sendParticles(ParticleTypes.WITCH, defender.getX(), defender.getY() + 1.0, defender.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
            return true;
        }
        if (BinderState.hasPassive(defender, WarframeBinderCatalog.PASSIVE_FROST)
                && attacker != null && defender.getRandom().nextFloat() < 0.25f) {
            attacker.setTicksFrozen(120);
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
        }
        return false;
    }
}
