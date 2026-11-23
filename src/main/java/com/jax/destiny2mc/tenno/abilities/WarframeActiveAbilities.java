package com.jax.destiny2mc.tenno.abilities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class WarframeActiveAbilities {
    private WarframeActiveAbilities() {
    }

    public static boolean tryCast(ServerPlayer player, WarframeAbilityId ability) {
        if (player == null || ability == null || player.isSpectator() || !player.isAlive()) {
            return false;
        }
        WarframeAbilityState state = WarframeAbilityState.get(player.getUUID());
        int cd = state.cooldown(ability);
        if (cd > 0) {
            player.displayClientMessage(
                    Component.literal(ability.displayName() + " recharging (" + String.format("%.1fs", cd / 20.0f) + ")"),
                    true
            );
            return false;
        }
        if (!player.isCreative() && !state.consumeEnergy(ability.energyCost())) {
            player.displayClientMessage(Component.literal("Not enough energy for " + ability.displayName()), true);
            return false;
        }

        state.setCooldown(ability, ability.cooldownTicks());
        ServerLevel level = player.serverLevel();

        switch (ability) {
            case URIEL_INFERNALIS -> castUrielInfernalis(player, level, state);
            case URIEL_BRIMSTONE -> castUrielBrimstone(player, level);
            case EXCALIBUR_SLASH_DASH -> castExcaliburSlashDash(player, level);
            case MAG_PULL -> castMagPull(player, level);
            case VOLT_SPEED -> castVoltSpeed(player, level);
            case NYX_CHAOS -> castNyxChaos(player, level);
            case KULLERVO_WRATHFUL_ADVANCE -> castKullervoWrathfulAdvance(player, level, state);
            case VORUNA_SHROUD_OF_DYNAR -> castVorunaShroud(player, level, state);
            case VALKYR_WARCRY -> castValkyrWarcry(player, level, state);
            case FROST_SNOW_GLOBE -> castFrostSnowGlobe(player, level, state);
            case FOLLIE_INKWELL_MIRAGE -> castFollieInkwellMirage(player, level, state);
        }
        return true;
    }

    public static void tickActiveEffects(ServerPlayer player) {
        WarframeAbilityState state = WarframeAbilityState.get(player.getUUID());
        ServerLevel level = player.serverLevel();

        if (state.infernalisTicks() > 0) {
            Vec3 vel = player.getDeltaMovement();
            if (vel.y < -0.08) {
                player.setDeltaMovement(vel.x * 1.03, -0.08, vel.z * 1.03);
                player.hurtMarked = true;
            }
            if (player.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(), 4, 0.35, 0.2, 0.35, 0.02);
            }
            if (player.tickCount % 10 == 0) {
                for (LivingEntity target : hostilesInBox(level, player, 3.5)) {
                    target.igniteForSeconds(3.0f);
                    target.hurt(level.damageSources().onFire(), 3.0f);
                }
            }
        }

        if (state.snowGlobeTicks() > 0 && state.snowGlobeCenter() != null) {
            tickSnowGlobe(player, level, state.snowGlobeCenter());
        }

        if (state.inkwellTicks() > 0 && player.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 0.4, player.getZ(), 5, 0.4, 0.3, 0.4, 0.01);
        }
    }

    private static void castUrielInfernalis(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 look = player.getLookAngle().normalize();
        player.setDeltaMovement(look.x * 1.35, Math.max(0.55, look.y * 1.15), look.z * 1.35);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        state.setInfernalisTicks(100);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, false));

        for (LivingEntity target : hostilesInBox(level, player, 5.0)) {
            target.igniteForSeconds(4.0f);
            target.hurt(level.damageSources().playerAttack(player), 6.0f);
        }
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 32, 1.0, 0.6, 1.0, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.85f);
    }

    private static void castUrielBrimstone(ServerPlayer player, ServerLevel level) {
        Vec3 origin = player.position();
        for (LivingEntity target : hostilesInBox(level, player, 9.0)) {
            target.igniteForSeconds(6.0f);
            target.hurt(level.damageSources().playerAttack(player), 12.0f);
            Vec3 push = target.position().subtract(origin).normalize().scale(0.7);
            target.push(push.x, 0.35, push.z);
        }
        for (int i = 0; i < 36; i++) {
            double angle = (Math.PI * 2.0 * i) / 36.0;
            double rx = origin.x + Math.cos(angle) * 6.0;
            double rz = origin.z + Math.sin(angle) * 6.0;
            level.sendParticles(ParticleTypes.LAVA, rx, origin.y + 0.4, rz, 2, 0.2, 0.3, 0.2, 0.05);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, origin.y + 0.8, rz, 2, 0.15, 0.4, 0.15, 0.03);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 0.7f);
    }

    private static void castExcaliburSlashDash(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.position();
        player.setDeltaMovement(look.x * 1.65, Math.max(0.2, look.y * 0.8), look.z * 1.65);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        AABB sweep = player.getBoundingBox().expandTowards(look.scale(7.5)).inflate(1.6);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sweep, e -> isValidHostile(player, e))) {
            target.hurt(level.damageSources().playerAttack(player), 10.0f);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        }
        for (int step = 1; step <= 8; step++) {
            Vec3 p = start.add(look.scale(step * 0.85));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 1.0, p.z, 2, 0.1, 0.1, 0.1, 0.01);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.25f);
    }

    private static void castMagPull(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 pullPoint = player.position().add(look.scale(2.2));
        for (LivingEntity target : hostilesInBox(level, player, 12.0)) {
            Vec3 toTarget = target.position().subtract(player.position()).normalize();
            if (toTarget.dot(look) < 0.2) {
                continue;
            }
            Vec3 delta = pullPoint.subtract(target.position());
            target.setDeltaMovement(delta.x * 0.24, Math.min(0.45, Math.max(0.22, delta.y * 0.2)), delta.z * 0.24);
            target.hurtMarked = true;
            target.hurt(level.damageSources().magic(), 6.0f);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.04);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9f, 1.4f);
    }

    private static void castVoltSpeed(ServerPlayer player, ServerLevel level) {
        int duration = 180;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 1, false, true));
        for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(10.0))) {
            if (ally != player) {
                ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, true));
                ally.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 1, false, true));
            }
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.6, 0.8, 0.6, 0.12);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.7f, 1.6f);
    }

    private static void castNyxChaos(ServerPlayer player, ServerLevel level) {
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(14.0), m -> isValidHostile(player, m));
        for (int i = 0; i < mobs.size(); i++) {
            Mob mob = mobs.get(i);
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false));
            if (mobs.size() > 1) {
                Mob nextTarget = mobs.get((i + 1) % mobs.size());
                mob.setTarget(nextTarget);
            } else {
                mob.setTarget(null);
            }
            level.sendParticles(ParticleTypes.WITCH, mob.getX(), mob.getY() + 1.2, mob.getZ(), 10, 0.3, 0.5, 0.3, 0.04);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.9f);
    }

    private static void castKullervoWrathfulAdvance(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity target = hostilesInBox(level, player, 18.0).stream()
                .filter(e -> e.position().subtract(player.position()).normalize().dot(look) > 0.65)
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

        if (target != null) {
            Vec3 behind = target.position().subtract(look.scale(1.2));
            player.teleportTo(behind.x, target.getY() + 0.1, behind.z);
            target.hurt(level.damageSources().playerAttack(player), 14.0f);
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 18, 0.4, 0.5, 0.4, 0.15);
        } else {
            Vec3 dest = player.position().add(look.scale(8.0));
            player.setDeltaMovement(look.scale(1.3));
            player.hurtMarked = true;
            level.sendParticles(ParticleTypes.SMOKE, dest.x, dest.y + 1.0, dest.z, 12, 0.3, 0.3, 0.3, 0.05);
        }
        state.setWrathfulCritTicks(140);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1, false, true));
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 1.1f);
    }

    private static void castVorunaShroud(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setShroudTicks(160);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true));
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16.0))) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.9, player.getZ(), 20, 0.5, 0.6, 0.5, 0.04);
        level.playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 0.8f, 0.85f);
    }

    private static void castValkyrWarcry(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setWarcryTicks(200);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, true));
        for (LivingEntity target : hostilesInBox(level, player, 10.0)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1, false, true));
        }
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.4, player.getZ(), 12, 0.6, 0.4, 0.6, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.85f, 1.2f);
    }

    private static void castFrostSnowGlobe(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 center = player.position();
        state.setSnowGlobe(center, 240);
        for (LivingEntity target : hostilesInBox(level, player, 5.5)) {
            Vec3 push = target.position().subtract(center).normalize().scale(1.0);
            target.push(push.x, 0.3, push.z);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true));
            target.setTicksFrozen(140);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.4f);
    }

    private static void castFollieInkwellMirage(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setInkwellTicks(120);
        Vec3 back = player.getLookAngle().normalize().scale(-0.95);
        player.setDeltaMovement(back.x, 0.35, back.z);
        player.hurtMarked = true;

        for (LivingEntity target : hostilesInBox(level, player, 8.0)) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true));
            target.hurt(level.damageSources().magic(), 5.0f);
        }
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0, player.getZ(), 35, 1.5, 0.8, 1.5, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.SQUID_SQUIRT, SoundSource.PLAYERS, 1.0f, 0.9f);
    }

    private static void tickSnowGlobe(ServerPlayer owner, ServerLevel level, Vec3 center) {
        double radius = 5.0;
        AABB bounds = new AABB(center.x - radius, center.y - 1.0, center.z - radius, center.x + radius, center.y + radius, center.z + radius);

        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, bounds)) {
            if (projectile.getOwner() != owner && projectile.position().distanceToSqr(center) <= radius * radius) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
                projectile.discard();
            }
        }

        if (owner.tickCount % 8 == 0) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, e -> isValidHostile(owner, e))) {
                if (target.position().distanceToSqr(center) <= radius * radius) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false));
                }
            }
            for (int i = 0; i < 20; i++) {
                double angle = (Math.PI * 2.0 * i) / 20.0;
                level.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        center.x + Math.cos(angle) * radius,
                        center.y + 1.0 + (i % 3) * 0.7,
                        center.z + Math.sin(angle) * radius,
                        1, 0.05, 0.1, 0.05, 0.01
                );
            }
        }
    }

    private static List<LivingEntity> hostilesInBox(ServerLevel level, ServerPlayer player, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> isValidHostile(player, e));
    }

    private static boolean isValidHostile(ServerPlayer player, LivingEntity entity) {
        return entity != player && entity.isAlive() && (entity instanceof Enemy || (entity instanceof Mob m && m.getTarget() == player));
    }
}
