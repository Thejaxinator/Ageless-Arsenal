package com.jax.destiny2mc.tenno.abilities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

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

        switch (state.activeWarframe()) {
            case URIEL -> {
                if (player.isOnFire()) {
                    player.clearFire();
                }
                if (player.tickCount % 40 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false));
                }
            }
            case EXCALIBUR -> {
                if (player.getMainHandItem().getItem() instanceof SwordItem && player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 35, 0, true, false));
                }
            }
            case MAG -> {
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
            case VOLT -> {
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
            case VORUNA -> {
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 35, 0, true, false));
                }
            }
            case VALKYR -> {
                if (player.fallDistance > 3.0f) {
                    player.fallDistance = 0.0f;
                }
            }
            case FOLLIE -> {
                if (player.isSprinting() && player.tickCount % 6 == 0) {
                    player.serverLevel().sendParticles(
                            ParticleTypes.SQUID_INK,
                            player.getX(), player.getY() + 0.15, player.getZ(),
                            1, 0.15, 0.05, 0.15, 0.0
                    );
                }
            }
            case NYX, KULLERVO, FROST -> {
                // Handled in combat hooks below.
            }
        }
    }

    public static float modifyOutgoingMeleeDamage(ServerPlayer attacker, LivingEntity target, float baseDamage) {
        WarframeAbilityState state = WarframeAbilityState.get(attacker.getUUID());
        float damage = baseDamage;

        if (state.activeWarframe() == WarframeId.EXCALIBUR && attacker.getMainHandItem().getItem() instanceof SwordItem) {
            damage *= 1.15f;
        }
        if (state.activeWarframe() == WarframeId.KULLERVO) {
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
        if (state.activeWarframe() == WarframeId.VOLT && state.voltStaticCharge() >= 15.0f) {
            float bonus = state.consumeVoltStaticCharge() * 0.08f;
            damage += bonus;
            ServerLevel level = attacker.serverLevel();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.08);
        }
        return damage;
    }

    public static boolean onPlayerDamaged(ServerPlayer defender, LivingEntity attacker) {
        WarframeAbilityState state = WarframeAbilityState.get(defender.getUUID());
        if (state.activeWarframe() == WarframeId.NYX && defender.getRandom().nextFloat() < 0.20f) {
            defender.serverLevel().sendParticles(ParticleTypes.WITCH, defender.getX(), defender.getY() + 1.0, defender.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
            return true;
        }
        if (state.activeWarframe() == WarframeId.FROST && attacker != null && defender.getRandom().nextFloat() < 0.25f) {
            attacker.setTicksFrozen(120);
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
        }
        return false;
    }
}
