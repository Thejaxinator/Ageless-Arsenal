package com.jax.destiny2mc.tenno.abilities;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class WarframeActiveAbilities {
    public static final String TAG_BLEED_UNTIL = "aa_wf_bleed_until";
    public static final String TAG_BLEED_DMG = "aa_wf_bleed_dmg";
    public static final String TAG_BLEED_OWNER = "aa_wf_bleed_owner";
    public static final String TAG_CRYSTAL_UNTIL = "aa_wf_crystal_until";
    public static final String TAG_WRATH_UNTIL = "aa_wf_wrath_until";
    public static final String TAG_THARROS_UNTIL = "aa_wf_tharros_until";
    public static final String TAG_CHAOS_UNTIL = "aa_wf_chaos_until";

    private WarframeActiveAbilities() {
    }

    /** Cast entry point used by the Energy Binder after BinderState validates ownership and cooldown. */
    public static boolean castFromBinder(ServerPlayer player, WarframeAbilityId ability) {
        if (player == null || ability == null || player.isSpectator() || !player.isAlive()) {
            return false;
        }
        WarframeAbilityState state = WarframeAbilityState.get(player.getUUID());
        executeCast(player, player.serverLevel(), state, ability);
        return true;
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
        executeCast(player, player.serverLevel(), state, ability);
        return true;
    }

    private static void executeCast(ServerPlayer player, ServerLevel level, WarframeAbilityState state, WarframeAbilityId ability) {
        switch (ability) {
            case URIEL_INFERNALIS -> castUrielInfernalis(player, level, state);
            case URIEL_BRIMSTONE -> castUrielBrimstone(player, level, state);
            case EXCALIBUR_SLASH_DASH -> castExcaliburSlashDash(player, level, state);
            case MAG_PULL -> castMagPull(player, level);
            case VOLT_SPEED -> castVoltSpeed(player, level, state);
            case NYX_CHAOS -> castNyxChaos(player, level, state);
            case KULLERVO_WRATHFUL_ADVANCE -> castKullervoWrathfulAdvance(player, level, state);
            case VORUNA_SHROUD_OF_DYNAR -> castVorunaShroud(player, level, state);
            case VALKYR_WARCRY -> castValkyrWarcry(player, level, state);
            case FROST_SNOW_GLOBE -> castFrostSnowGlobe(player, level, state);
            case FOLLIE_INKWELL_MIRAGE -> castFollieInkwellMirage(player, level, state);
            case CITRINE_PRISMATIC_GEM -> castCitrinePrismaticGem(player, level, state);
            case CITRINE_CRYSTALLIZE -> castCitrineCrystallize(player, level);
            case STYANAX_THARROS_STRIKE -> castStyanaxTharrosStrike(player, level);
            case STYANAX_FINAL_STAND -> castStyanaxFinalStand(player, level, state);
            case GYRE_COIL_HORIZON -> castGyreCoilHorizon(player, level, state);
            case GYRE_ROTORSWELL -> castGyreRotorswell(player, level, state);
            case CALIBAN_SENTIENT_WRATH -> castCalibanSentientWrath(player, level);
            case CALIBAN_RAZOR_GYRE -> castCalibanRazorGyre(player, level, state);
            case YARELI_AQUABLADES -> castYareliAquablades(player, level, state);
            case YARELI_RIPTIDE -> castYareliRiptide(player, level, state);
            case LAVOS_VIAL_RUSH -> castLavosVialRush(player, level, state);
            case LAVOS_CATALYZE -> castLavosCatalyze(player, level);
        }
    }

    public static void tickActiveEffects(ServerPlayer player) {
        WarframeAbilityState state = WarframeAbilityState.get(player.getUUID());
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();

        // Tick Slash Bleed DoTs owned by this player on nearby hostiles
        if (player.tickCount % 10 == 0) {
            for (LivingEntity target : hostilesInBox(level, player, 28.0)) {
                CompoundTag data = target.getPersistentData();
                if (data.getLong(TAG_BLEED_UNTIL) >= now
                        && data.hasUUID(TAG_BLEED_OWNER)
                        && player.getUUID().equals(data.getUUID(TAG_BLEED_OWNER))) {
                    float bleed = Math.max(2.0f, data.getFloat(TAG_BLEED_DMG));
                    target.hurt(level.damageSources().playerAttack(player), bleed);
                    level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.25, 0.3, 0.25, 0.04);
                }
            }
        }

        // Uriel: Infernalis blazing flight aura
        if (state.infernalisTicks() > 0) {
            Vec3 vel = player.getDeltaMovement();
            Vec3 look = player.getLookAngle().normalize();
            if (vel.y < -0.05) {
                double lift = look.y > 0.25 ? 0.08 : -0.05;
                player.setDeltaMovement(vel.x * 1.04, lift, vel.z * 1.04);
                player.hurtMarked = true;
                player.fallDistance = 0.0f;
            }
            if (player.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(), 5, 0.4, 0.2, 0.4, 0.03);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.35, 0.25, 0.35, 0.02);
            }
            if (player.tickCount % 8 == 0) {
                VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.SOLAR, player.position().add(0, 0.2, 0), player.position().add(0, 0.2, 0), 4.5f);
                for (LivingEntity target : hostilesInBox(level, player, 5.5)) {
                    target.igniteForSeconds(6.0f);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true));
                    target.hurt(level.damageSources().playerAttack(player), 5.5f);
                }
            }
        }

        // Uriel: Brimstone persistent erupting hellfire zone
        if (state.brimstoneTicks() > 0 && state.brimstoneCenter() != null) {
            tickBrimstone(player, level, state.brimstoneCenter(), state.brimstoneTicks());
        }

        // Volt: Shocking Speed active aura
        if (state.voltSpeedTicks() > 0) {
            if (player.tickCount % 3 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.8, player.getZ(), 4, 0.4, 0.5, 0.4, 0.06);
            }
            if (player.tickCount % 10 == 0) {
                for (LivingEntity target : hostilesInBox(level, player, 3.8)) {
                    target.hurt(level.damageSources().magic(), 6.0f);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, true));
                    VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, centerOf(player), centerOf(target), 0.7f);
                }
            }
        }

        // Nyx: Chaos persistent psychic re-aggro field
        if (state.chaosTicks() > 0 && state.chaosCenter() != null && player.tickCount % 15 == 0) {
            tickNyxChaosField(player, level, state.chaosCenter());
        }

        // Frost: Snow Globe cryo dome + shatter on expiry
        if (state.snowGlobeTicks() > 0 && state.snowGlobeCenter() != null) {
            tickSnowGlobe(player, level, state.snowGlobeCenter(), state.snowGlobeTicks());
        }

        // Follie: Inkwell Mirage taunting Ink Effigy + detonation on expiry
        if (state.inkwellTicks() > 0 && state.inkwellCenter() != null) {
            tickInkwellEffigy(player, level, state.inkwellCenter(), state.inkwellTicks());
        }

        // Citrine: Prismatic Gem hovering turret + ally aura
        if (state.prismaticGemTicks() > 0 && state.prismaticGemCenter() != null) {
            tickPrismaticGem(player, level, state);
        }

        // Styanax: Final Stand midair javelin barrage
        if (state.finalStandTicks() > 0) {
            tickFinalStand(player, level, state.finalStandTicks());
        }

        // Gyre: Coil Horizon delayed vortex implosion
        if (state.coilHorizonTicks() > 0 && state.coilHorizonCenter() != null) {
            tickCoilHorizon(player, level, state.coilHorizonCenter(), state.coilHorizonTicks());
        }

        // Gyre: Rotorswell overcharged Arc field
        if (state.rotorswellTicks() > 0) {
            if (player.tickCount % 4 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.6, 0.7, 0.6, 0.1);
            }
            if (player.tickCount % 15 == 0) {
                VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.ARC, player.position().add(0, 0.8, 0), player.position().add(0, 0.8, 0), 6.5f);
                for (LivingEntity target : hostilesInBox(level, player, 7.0)) {
                    target.hurt(level.damageSources().magic(), 7.0f);
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                    VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, centerOf(player), centerOf(target), 0.9f);
                }
            }
        }

        // Caliban: Razor Gyre spinning Tau vortex dash
        if (state.razorGyreTicks() > 0 && state.razorGyreDir() != null) {
            tickRazorGyre(player, level, state.razorGyreDir(), state.razorGyreTicks());
        }

        // Yareli: Aquablades orbiting water blades
        if (state.aquabladesTicks() > 0) {
            tickAquablades(player, level, state.aquabladesTicks());
        }

        // Yareli: Riptide crushing water cyclone + scaling Cold detonation
        if (state.riptideTicks() > 0 && state.riptideCenter() != null) {
            tickRiptide(player, level, state.riptideCenter(), state.riptideTicks());
        }

        // Lavos: Vial Rush high-speed alchemical dash + lingering cryo-toxin pools
        if (state.vialRushTicks() > 0 && state.vialRushDir() != null) {
            tickVialRush(player, level, state);
        }
        if (state.vialPoolTicks() > 0 && state.vialPoolCenter() != null) {
            tickVialPool(player, level, state.vialPoolCenter());
        }
    }

    // ==================== REWORKED EXISTING 11 ABILITIES ====================

    private static void castUrielInfernalis(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 look = player.getLookAngle().normalize();
        player.setDeltaMovement(look.x * 1.55, Math.max(0.70, look.y * 1.25), look.z * 1.55);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        state.setInfernalisTicks(140);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 180, 0, false, false));

        for (LivingEntity target : hostilesInBox(level, player, 6.5)) {
            target.igniteForSeconds(6.0f);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true));
            target.hurt(level.damageSources().playerAttack(player), 14.0f);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.SOLAR, player.position().add(0, 0.8, 0), player.position().add(0, 0.8, 0), 5.5f);
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 40, 1.2, 0.8, 1.2, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.1f, 0.8f);
    }

    private static void castUrielBrimstone(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 origin = player.position();
        state.setBrimstone(origin, 120);
        for (LivingEntity target : hostilesInBox(level, player, 10.0)) {
            target.igniteForSeconds(8.0f);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true));
            target.hurt(level.damageSources().playerAttack(player), 20.0f);
            Vec3 push = target.position().subtract(origin).normalize().scale(0.85);
            target.push(push.x, 0.45, push.z);
            target.hurtMarked = true;
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.SOLAR, origin.add(0, 0.3, 0), origin.add(0, 0.3, 0), 10.0f);
        VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.SOLAR, origin.add(0, 1.0, 0), origin.add(0, 1.0, 0), 7.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1f, 0.65f);
    }

    private static void tickBrimstone(ServerPlayer owner, ServerLevel level, Vec3 center, int remainingTicks) {
        double radius = 10.0;
        if (owner.tickCount % 5 == 0) {
            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2.0 * i) / 24.0;
                double rx = center.x + Math.cos(angle) * radius;
                double rz = center.z + Math.sin(angle) * radius;
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, center.y + 0.4, rz, 1, 0.2, 0.3, 0.2, 0.02);
            }
        }
        if (owner.tickCount % 15 == 0) {
            float progress = 1.0f - (remainingTicks / 120.0f);
            float pulseDmg = 6.0f + progress * 6.0f;
            VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.SOLAR, center.add(0, 0.25, 0), center.add(0, 0.25, 0), (float) radius);
            for (LivingEntity target : hostilesAround(level, owner, center, radius)) {
                target.igniteForSeconds(6.0f);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, false));
                target.hurt(level.damageSources().playerAttack(owner), pulseDmg);
                level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 0.5, target.getZ(), 4, 0.3, 0.4, 0.3, 0.06);
            }
            level.playSound(null, BlockPos.containing(center), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6f, 0.7f);
        }
    }

    private static void castExcaliburSlashDash(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 cursor = player.position();
        state.setSlashDashInvulnTicks(24);
        player.fallDistance = 0.0f;

        List<LivingEntity> candidates = hostilesInBox(level, player, 14.0).stream()
                .filter(e -> e.position().subtract(player.position()).normalize().dot(look) > 0.35)
                .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .limit(5)
                .toList();

        if (!candidates.isEmpty()) {
            Vec3 lastPos = cursor;
            for (LivingEntity target : candidates) {
                Vec3 targetPos = target.position();
                VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.KINETIC, lastPos.add(0, 1.0, 0), centerOf(target), 1.2f);
                target.hurt(level.damageSources().playerAttack(player), 18.0f);
                applyBleed(player, target, 60, 2.5f);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                lastPos = targetPos;
            }
            Vec3 endPos = lastPos.add(look.scale(1.6));
            player.teleportTo(endPos.x, lastPos.y + 0.1, endPos.z);
            player.setDeltaMovement(look.scale(0.85));
            player.hurtMarked = true;
        } else {
            player.setDeltaMovement(look.x * 2.1, Math.max(0.25, look.y * 0.9), look.z * 2.1);
            player.hurtMarked = true;
            AABB sweep = player.getBoundingBox().expandTowards(look.scale(11.0)).inflate(2.0);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sweep, e -> isValidHostile(player, e))) {
                target.hurt(level.damageSources().playerAttack(player), 18.0f);
                applyBleed(player, target, 60, 2.5f);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.25, 0.25, 0.25, 0.0);
            }
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.KINETIC, cursor.add(0, 1.0, 0), cursor.add(look.scale(11.0)).add(0, 1.0, 0), 1.3f);
        }

        // Exalted energy wave finisher along sightline
        Vec3 waveOrigin = player.getEyePosition();
        Vec3 waveEnd = waveOrigin.add(look.scale(12.0));
        VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, waveOrigin, waveEnd, 1.4f);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().expandTowards(look.scale(12.0)).inflate(1.5), e -> isValidHostile(player, e))) {
            target.hurt(level.damageSources().magic(), 8.0f);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 1.3f);
    }

    private static void castMagPull(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 vortex = player.position().add(look.scale(2.6)).add(0, 0.5, 0);
        int pulled = 0;

        for (LivingEntity target : hostilesInBox(level, player, 16.0)) {
            Vec3 toTarget = target.position().subtract(player.position()).normalize();
            if (toTarget.dot(look) < 0.05) {
                continue;
            }
            Vec3 delta = vortex.subtract(target.position());
            target.setDeltaMovement(delta.x * 0.42, Math.min(0.55, Math.max(0.28, delta.y * 0.25 + 0.22)), delta.z * 0.42);
            target.hurtMarked = true;
            float dmg = (target.getArmorValue() > 0 || target.hasEffect(MobEffects.GLOWING)) ? 24.0f : 14.0f;
            target.hurt(level.damageSources().magic(), dmg);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, centerOf(target), vortex, 0.9f);
            pulled++;
        }

        // Also vacuum nearby items and XP orbs into the player
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(14.0))) {
            item.setDeltaMovement(player.position().add(0, 0.6, 0).subtract(item.position()).scale(0.32));
        }
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(14.0))) {
            orb.setDeltaMovement(player.position().add(0, 0.6, 0).subtract(orb.position()).scale(0.32));
        }

        if (pulled > 0) {
            grantAbsorption(player, Math.min(8.0f, pulled * 2.0f + 2.0f));
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.ARC, vortex, vortex, 3.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    private static void castVoltSpeed(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        int duration = 240;
        state.setVoltSpeedTicks(duration);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 0, false, false));
        for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(16.0))) {
            if (ally != player && ally.isAlive() && !ally.isSpectator()) {
                ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2, false, true));
                ally.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 2, false, true));
                ally.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 0, false, false));
            }
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.ARC, player.position().add(0, 0.8, 0), player.position().add(0, 0.8, 0), 4.0f);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 32, 0.7, 0.9, 0.7, 0.14);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85f, 1.6f);
    }

    private static void castNyxChaos(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 origin = player.position();
        state.setChaos(origin, 240);
        long until = level.getGameTime() + 240;
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(18.0), m -> isValidHostile(player, m));
        for (int i = 0; i < mobs.size(); i++) {
            Mob mob = mobs.get(i);
            mob.getPersistentData().putLong(TAG_CHAOS_UNTIL, until);
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1, false, false));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, false));
            mob.hurt(level.damageSources().magic(), 10.0f);
            if (mobs.size() > 1) {
                mob.setTarget(mobs.get((i + 1) % mobs.size()));
            } else {
                mob.setTarget(null);
            }
            level.sendParticles(ParticleTypes.WITCH, mob.getX(), mob.getY() + 1.2, mob.getZ(), 12, 0.35, 0.5, 0.35, 0.05);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.VOID, origin.add(0, 1.0, 0), origin.add(0, 1.0, 0), 14.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.1f, 0.85f);
    }

    private static void tickNyxChaosField(ServerPlayer owner, ServerLevel level, Vec3 center) {
        long now = level.getGameTime();
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, new AABB(center, center).inflate(18.0),
                m -> isValidHostile(owner, m) && m.getPersistentData().getLong(TAG_CHAOS_UNTIL) >= now);
        if (mobs.size() < 2) return;
        for (int i = 0; i < mobs.size(); i++) {
            Mob mob = mobs.get(i);
            if (mob.getTarget() == null || mob.getTarget() == owner || !mob.getTarget().isAlive()) {
                mob.setTarget(mobs.get((i + 1) % mobs.size()));
            }
        }
    }

    private static void castKullervoWrathfulAdvance(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.position();
        LivingEntity target = hostilesInBox(level, player, 24.0).stream()
                .filter(e -> e.position().subtract(player.position()).normalize().dot(look) > 0.50)
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

        VoxelEffects.emit(level, VoxelCue.Kind.BLINK_OUT, EffectProfile.NANITE, start.add(0, 1.0, 0), start.add(0, 1.0, 0), 1.4f);
        if (target != null) {
            Vec3 behind = target.position().subtract(look.scale(1.3));
            player.teleportTo(behind.x, target.getY() + 0.1, behind.z);
            player.fallDistance = 0.0f;
            VoxelEffects.emit(level, VoxelCue.Kind.BLINK_IN, EffectProfile.NANITE, behind.add(0, 1.0, 0), behind.add(0, 1.0, 0), 1.6f);
            target.hurt(level.damageSources().playerAttack(player), 26.0f);
            for (LivingEntity cleave : hostilesAround(level, player, target.position(), 4.5)) {
                if (cleave != target) {
                    cleave.hurt(level.damageSources().playerAttack(player), 14.0f);
                }
            }
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 28, 0.5, 0.6, 0.5, 0.2);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.5, 0.3, 0.5, 0.0);
        } else {
            Vec3 dest = raycastPoint(player, level, 12.0);
            player.teleportTo(dest.x, dest.y + 0.1, dest.z);
            player.fallDistance = 0.0f;
            VoxelEffects.emit(level, VoxelCue.Kind.BLINK_IN, EffectProfile.NANITE, dest.add(0, 1.0, 0), dest.add(0, 1.0, 0), 1.5f);
        }
        state.setWrathfulCritTicks(200);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false));
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.05f);
    }

    private static void castVorunaShroud(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setShroudTicks(200);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2, false, true));
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(20.0))) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }
        VoxelEffects.emit(level, VoxelCue.Kind.CLOUD, EffectProfile.SOULFIRE, player.position().add(0, 0.9, 0), player.position().add(0, 0.9, 0), 2.5f);
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.9, player.getZ(), 28, 0.6, 0.7, 0.6, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 0.95f, 0.8f);
    }

    private static void castValkyrWarcry(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        int duration = 280;
        state.setWarcryTicks(duration);
        applyWarcryBuffs(player, duration);
        for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(16.0))) {
            if (ally != player && ally.isAlive() && !ally.isSpectator()) {
                applyWarcryBuffs(ally, duration);
            }
        }
        for (LivingEntity target : hostilesInBox(level, player, 14.0)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 3, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 0, false, false));
            target.hurt(level.damageSources().playerAttack(player), 8.0f);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.SOLAR, player.position().add(0, 1.0, 0), player.position().add(0, 1.0, 0), 10.0f);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.4, player.getZ(), 18, 0.8, 0.5, 0.8, 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0f, 1.15f);
    }

    public static void applyWarcryBuffs(ServerPlayer target, int duration) {
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 3, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1, false, true));
        grantAbsorption(target, 4.0f);
    }

    private static void castFrostSnowGlobe(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        if (state.snowGlobeTicks() > 0 && state.snowGlobeCenter() != null) {
            shatterSnowGlobe(player, level, state.snowGlobeCenter());
        }
        Vec3 center = player.position();
        state.setSnowGlobe(center, 360);
        for (LivingEntity target : hostilesInBox(level, player, 6.8)) {
            Vec3 push = target.position().subtract(center).normalize().scale(1.45);
            target.push(push.x, 0.42, push.z);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3, false, true));
            target.setTicksFrozen(220);
            target.hurt(level.damageSources().freeze(), 16.0f);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.STASIS, center.add(0, 0.4, 0), center.add(0, 0.4, 0), 6.5f);
        level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.3f);
    }

    private static void tickSnowGlobe(ServerPlayer owner, ServerLevel level, Vec3 center, int remainingTicks) {
        double radius = 6.5;
        AABB bounds = new AABB(center.x - radius, center.y - 1.5, center.z - radius, center.x + radius, center.y + radius, center.z + radius);

        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, bounds)) {
            if (projectile.getOwner() != owner && projectile.position().distanceToSqr(center) <= radius * radius) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 5, 0.12, 0.12, 0.12, 0.02);
                projectile.discard();
            }
        }

        if (owner.tickCount % 10 == 0) {
            for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, bounds)) {
                if (ally.isAlive() && !ally.isSpectator() && ally.position().distanceToSqr(center) <= radius * radius) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, true, false));
                }
            }
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, e -> isValidHostile(owner, e))) {
                if (target.position().distanceToSqr(center) <= radius * radius) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, false));
                    target.setTicksFrozen(Math.min(240, target.getTicksFrozen() + 25));
                    if (owner.tickCount % 20 == 0) {
                        target.hurt(level.damageSources().freeze(), 3.5f);
                    }
                }
            }
            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2.0 * i) / 24.0;
                level.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        center.x + Math.cos(angle) * radius,
                        center.y + 0.8 + (i % 4) * 0.75,
                        center.z + Math.sin(angle) * radius,
                        1, 0.05, 0.1, 0.05, 0.01
                );
            }
        }

        if (remainingTicks == 1) {
            shatterSnowGlobe(owner, level, center);
        }
    }

    private static void shatterSnowGlobe(ServerPlayer owner, ServerLevel level, Vec3 center) {
        VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.STASIS, center.add(0, 1.0, 0), center.add(0, 1.0, 0), 7.5f);
        for (LivingEntity target : hostilesAround(level, owner, center, 8.0)) {
            target.hurt(level.damageSources().freeze(), 14.0f);
            target.setTicksFrozen(200);
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.9f);
    }

    private static void castFollieInkwellMirage(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 effigyPos = player.position();
        state.setInkwell(effigyPos, 160);
        Vec3 back = player.getLookAngle().normalize().scale(-1.15);
        player.setDeltaMovement(back.x, 0.42, back.z);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false));

        for (LivingEntity target : hostilesInBox(level, player, 9.0)) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2, false, true));
            target.hurt(level.damageSources().magic(), 10.0f);
            if (target instanceof Mob mob && mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }
        VoxelEffects.emit(level, VoxelCue.Kind.CLOUD, EffectProfile.DARK_ETHER, effigyPos.add(0, 1.0, 0), effigyPos.add(0, 1.0, 0), 3.5f);
        level.sendParticles(ParticleTypes.SQUID_INK, effigyPos.x, effigyPos.y + 1.0, effigyPos.z, 45, 1.5, 0.9, 1.5, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.SQUID_SQUIRT, SoundSource.PLAYERS, 1.0f, 0.85f);
    }

    private static void tickInkwellEffigy(ServerPlayer owner, ServerLevel level, Vec3 center, int remainingTicks) {
        if (owner.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 1.0, center.z, 8, 0.45, 0.7, 0.45, 0.02);
        }
        if (owner.tickCount % 20 == 0) {
            for (LivingEntity target : hostilesAround(level, owner, center, 11.0)) {
                Vec3 pull = center.subtract(target.position()).normalize().scale(0.35);
                target.push(pull.x, 0.1, pull.z);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
                target.hurt(level.damageSources().magic(), 6.0f);
                if (target instanceof Mob mob && mob.getTarget() == owner) {
                    mob.setTarget(null);
                }
            }
        }
        if (remainingTicks == 1) {
            VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.DARK_ETHER, center.add(0, 1.0, 0), center.add(0, 1.0, 0), 8.0f);
            for (LivingEntity target : hostilesAround(level, owner, center, 8.5)) {
                target.hurt(level.damageSources().magic(), 16.0f);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, true));
            }
            level.playSound(null, BlockPos.containing(center), SoundEvents.SQUID_SQUIRT, SoundSource.PLAYERS, 1.1f, 0.65f);
        }
    }

    // ==================== 12 NEW PRIME WARFRAME ABILITIES ====================

    private static void castCitrinePrismaticGem(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 gemPos = player.position().add(0, 2.0, 0);
        state.setPrismaticGem(gemPos, 240);
        VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.RESONANCE, gemPos, gemPos, 4.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.2f);
    }

    private static void tickPrismaticGem(ServerPlayer owner, ServerLevel level, WarframeAbilityState state) {
        Vec3 gemPos = state.prismaticGemCenter();
        if (owner.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, gemPos.x, gemPos.y, gemPos.z, 5, 0.3, 0.3, 0.3, 0.02);
        }
        if (owner.tickCount % 20 == 0) {
            for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, new AABB(gemPos, gemPos).inflate(12.0))) {
                if (ally.isAlive() && !ally.isSpectator()) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false));
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false));
                }
            }
        }
        if (owner.tickCount % 14 == 0) {
            LivingEntity target = hostilesAround(level, owner, gemPos, 15.0).stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(gemPos)))
                    .orElse(null);
            if (target != null) {
                firePrismaticGemBeam(owner, level, state, target);
            }
        }
    }

    public static void firePrismaticGemBeam(ServerPlayer owner, ServerLevel level, WarframeAbilityState state, LivingEntity target) {
        Vec3 gemPos = state.prismaticGemCenter();
        if (gemPos == null || target.position().distanceToSqr(gemPos) > 16.0 * 16.0) return;
        int elem = state.nextPrismaticElement();
        EffectProfile profile = switch (elem) {
            case 0 -> EffectProfile.SOLAR;
            case 1 -> EffectProfile.STASIS;
            case 2 -> EffectProfile.STRAND;
            default -> EffectProfile.ARC;
        };
        VoxelEffects.emit(level, VoxelCue.Kind.TRACE, profile, gemPos, centerOf(target), 0.9f);
        target.hurt(level.damageSources().magic(), 9.0f);
        switch (elem) {
            case 0 -> target.igniteForSeconds(5.0f);
            case 1 -> {
                target.setTicksFrozen(160);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, true));
            }
            case 2 -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true));
            default -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, true));
        }
    }

    private static void castCitrineCrystallize(ServerPlayer player, ServerLevel level) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        long until = level.getGameTime() + 120;
        List<LivingEntity> targets = hostilesInBox(level, player, 16.0).stream()
                .filter(e -> e.position().subtract(player.position()).normalize().dot(look) > 0.1 || e.distanceToSqr(player) < 36.0)
                .toList();

        for (LivingEntity target : targets) {
            target.getPersistentData().putLong(TAG_CRYSTAL_UNTIL, until);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, true));
            target.hurt(level.damageSources().magic(), 12.0f);
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.RESONANCE, start, centerOf(target), 1.0f);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 14, 0.4, 0.5, 0.4, 0.08);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.RESONANCE, player.position().add(0, 0.4, 0), player.position().add(0, 0.4, 0), 10.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.2f, 1.1f);
    }

    private static void castStyanaxTharrosStrike(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        long until = level.getGameTime() + 160;
        int hits = 0;

        for (LivingEntity target : hostilesInBox(level, player, 11.0)) {
            Vec3 toTarget = target.position().subtract(player.position()).normalize();
            if (toTarget.dot(look) < 0.2 && target.distanceToSqr(player) > 9.0) continue;
            target.getPersistentData().putLong(TAG_THARROS_UNTIL, until);
            target.hurt(level.damageSources().playerAttack(player), 16.0f);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, true));
            Vec3 push = toTarget.scale(1.3);
            target.push(push.x, 0.35, push.z);
            target.hurtMarked = true;
            hits++;
        }

        if (hits > 0) {
            player.heal(Math.min(12.0f, hits * 3.5f));
        }
        VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, player.getEyePosition(), player.getEyePosition().add(look.scale(10.0)), 2.2f);
        level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2f, 0.8f);
    }

    private static void castStyanaxFinalStand(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setFinalStandTicks(70);
        player.setDeltaMovement(player.getDeltaMovement().x * 0.5, 0.78, player.getDeltaMovement().z * 0.5);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        grantAbsorption(player, 4.0f);
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.ARC, player.position(), player.position(), 3.5f);
        level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8f, 1.3f);
    }

    private static void tickFinalStand(ServerPlayer player, ServerLevel level, int remainingTicks) {
        Vec3 vel = player.getDeltaMovement();
        player.setDeltaMovement(vel.x * 0.85, Math.max(-0.02, vel.y * 0.7), vel.z * 0.85);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        if (remainingTicks % 5 == 0) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            Vec3 side = look.cross(new Vec3(0, 1, 0)).normalize();
            if (side.lengthSqr() < 1e-4) side = new Vec3(1, 0, 0);

            for (int j = -1; j <= 1; j += 2) {
                Vec3 jitter = side.scale((player.getRandom().nextDouble() - 0.5) * 0.14 + j * 0.06);
                Vec3 dir = look.add(jitter).add(0, (player.getRandom().nextDouble() - 0.5) * 0.08, 0).normalize();
                Vec3 impact = raycastPoint(player, level, eye, dir, 28.0);
                LivingEntity direct = hostilesInBox(level, player, 28.0).stream()
                        .filter(e -> centerOf(e).subtract(eye).normalize().dot(dir) > 0.94)
                        .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                        .orElse(null);
                if (direct != null) {
                    impact = centerOf(direct);
                    direct.hurt(level.damageSources().playerAttack(player), 6.0f);
                }
                VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, eye.add(side.scale(j * 0.45)), impact, 0.9f);
                VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.KINETIC, impact, impact, 3.2f);
                for (LivingEntity splash : hostilesAround(level, player, impact, 4.0)) {
                    splash.hurt(level.damageSources().playerAttack(player), 12.0f);
                }
            }
            level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.9f, 1.35f);
        }
    }

    private static void castGyreCoilHorizon(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 targetPos = raycastPoint(player, level, 12.0).add(0, 0.8, 0);
        state.setCoilHorizon(targetPos, 20);
        VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, player.getEyePosition(), targetPos, 1.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.6f);
    }

    private static void tickCoilHorizon(ServerPlayer owner, ServerLevel level, Vec3 center, int remainingTicks) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 8, 0.6, 0.6, 0.6, 0.08);
        if (remainingTicks == 1) {
            VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.ARC, center, center, 9.0f);
            for (LivingEntity target : hostilesAround(level, owner, center, 11.0)) {
                Vec3 pull = center.subtract(target.position());
                target.setDeltaMovement(pull.x * 0.48, Math.min(0.45, Math.max(0.2, pull.y * 0.3)), pull.z * 0.48);
                target.hurtMarked = true;
                target.hurt(level.damageSources().magic(), 18.0f);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, true));
                VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, center, centerOf(target), 0.8f);
            }
            level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0f, 1.4f);
        }
    }

    private static void castGyreRotorswell(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setRotorswellTicks(280);
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.ARC, player.position().add(0, 1.0, 0), player.position().add(0, 1.0, 0), 7.5f);
        for (LivingEntity target : hostilesInBox(level, player, 7.5)) {
            target.hurt(level.damageSources().magic(), 12.0f);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void castCalibanSentientWrath(ServerPlayer player, ServerLevel level) {
        Vec3 origin = player.position();
        long until = level.getGameTime() + 140;
        for (LivingEntity target : hostilesInBox(level, player, 14.0)) {
            target.getPersistentData().putLong(TAG_WRATH_UNTIL, until);
            target.hurt(level.damageSources().magic(), 16.0f);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 140, 0, false, true));
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.VOID, origin.add(0, 0.5, 0), centerOf(target), 1.0f);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.VOID, origin.add(0, 0.3, 0), origin.add(0, 0.3, 0), 14.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.85f, 1.15f);
    }

    private static void castCalibanRazorGyre(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 dir = player.getLookAngle().multiply(1.0, 0.25, 1.0).normalize();
        if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, 0, 1);
        state.setRazorGyre(dir, 35);
        player.fallDistance = 0.0f;
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.1f, 0.85f);
    }

    private static void tickRazorGyre(ServerPlayer player, ServerLevel level, Vec3 dir, int remainingTicks) {
        Vec3 steer = player.getLookAngle().multiply(1.0, 0.2, 1.0).normalize();
        Vec3 move = steer.lengthSqr() > 1e-4 ? steer : dir;
        player.setDeltaMovement(move.x * 1.15, Math.max(-0.08, player.getDeltaMovement().y * 0.5), move.z * 1.15);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        if (remainingTicks % 2 == 0) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 2, 1.2, 0.3, 1.2, 0.0);
        }
        if (remainingTicks % 4 == 0) {
            long now = level.getGameTime();
            VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.VOID, player.position().add(0, 0.9, 0), player.position().add(0, 0.9, 0), 5.0f);
            for (LivingEntity target : hostilesInBox(level, player, 5.5)) {
                boolean lifted = target.getPersistentData().getLong(TAG_WRATH_UNTIL) >= now || target.hasEffect(MobEffects.LEVITATION);
                float dmg = lifted ? 16.0f : 8.0f;
                target.hurt(level.damageSources().magic(), dmg);
                player.heal(2.5f);
                grantAbsorption(player, 2.0f);
            }
        }
    }

    private static void castYareliAquablades(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        state.setAquabladesTicks(260);
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.STASIS, player.position().add(0, 0.9, 0), player.position().add(0, 0.9, 0), 3.6f);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0f, 1.3f);
    }

    private static void tickAquablades(ServerPlayer player, ServerLevel level, int remainingTicks) {
        double radius = 3.6;
        double baseAngle = (player.tickCount * 0.45);
        for (int i = 0; i < 3; i++) {
            double angle = baseAngle + (Math.PI * 2.0 * i) / 3.0;
            double bx = player.getX() + Math.cos(angle) * radius;
            double bz = player.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.BUBBLE_POP, bx, player.getY() + 0.95, bz, 2, 0.15, 0.1, 0.15, 0.02);
            level.sendParticles(ParticleTypes.SPLASH, bx, player.getY() + 0.95, bz, 2, 0.15, 0.1, 0.15, 0.04);
        }
        if (remainingTicks % 8 == 0) {
            for (LivingEntity target : hostilesInBox(level, player, 4.2)) {
                target.hurt(level.damageSources().playerAttack(player), 9.0f);
                applyBleed(player, target, 60, 2.0f);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.2, 0.2, 0.2, 0.0);
            }
        }
    }

    private static void castYareliRiptide(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 targetPos = raycastPoint(player, level, 18.0).add(0, 1.0, 0);
        state.setRiptide(targetPos, 50);
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.STASIS, targetPos, targetPos, 11.0f);
        level.playSound(null, BlockPos.containing(targetPos), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.2f, 0.8f);
    }

    private static void tickRiptide(ServerPlayer owner, ServerLevel level, Vec3 center, int remainingTicks) {
        if (remainingTicks % 3 == 0) {
            for (int i = 0; i < 12; i++) {
                double angle = (Math.PI * 2.0 * i) / 12.0 + remainingTicks * 0.3;
                double r = 1.5 + (remainingTicks / 50.0) * 4.5;
                level.sendParticles(ParticleTypes.SPLASH, center.x + Math.cos(angle) * r, center.y + 0.5, center.z + Math.sin(angle) * r, 2, 0.1, 0.3, 0.1, 0.05);
            }
        }
        if (remainingTicks > 1 && remainingTicks % 5 == 0) {
            for (LivingEntity target : hostilesAround(level, owner, center, 11.0)) {
                Vec3 pull = center.add(0, 0.6, 0).subtract(target.position());
                target.setDeltaMovement(pull.x * 0.38, pull.y * 0.32 + 0.08, pull.z * 0.38);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3, false, false));
            }
        }
        if (remainingTicks == 1) {
            List<LivingEntity> captives = hostilesAround(level, owner, center, 12.0);
            float dmg = 18.0f + captives.size() * 6.0f;
            VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.STASIS, center, center, 11.0f);
            for (LivingEntity target : captives) {
                target.hurt(level.damageSources().freeze(), dmg);
                target.setTicksFrozen(220);
                Vec3 blast = target.position().subtract(center).normalize().scale(1.1);
                target.push(blast.x, 0.45, blast.z);
                target.hurtMarked = true;
            }
            level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1f, 1.25f);
        }
    }

    private static void castLavosVialRush(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 dir = player.getLookAngle().multiply(1.0, 0.15, 1.0).normalize();
        if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, 0, 1);
        state.setVialRush(dir, 24);
        state.setVialPool(player.position(), 120);
        level.playSound(null, player.blockPosition(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.1f, 0.9f);
    }

    private static void tickVialRush(ServerPlayer player, ServerLevel level, WarframeAbilityState state) {
        Vec3 dir = state.vialRushDir();
        player.setDeltaMovement(dir.x * 1.65, Math.max(-0.08, player.getDeltaMovement().y), dir.z * 1.65);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        if (state.vialRushTicks() % 4 == 0) {
            state.setVialPool(player.position(), 120);
            level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.3, player.getZ(), 10, 1.0, 0.3, 1.0, 0.04);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 0.3, player.getZ(), 8, 1.0, 0.2, 1.0, 0.02);
            for (LivingEntity target : hostilesInBox(level, player, 4.0)) {
                target.hurt(level.damageSources().freeze(), 14.0f);
                target.setTicksFrozen(180);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true));
            }
        }
    }

    private static void tickVialPool(ServerPlayer owner, ServerLevel level, Vec3 center) {
        if (owner.tickCount % 6 == 0) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.2, center.z, 8, 3.0, 0.15, 3.0, 0.01);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 0.2, center.z, 6, 3.0, 0.15, 3.0, 0.01);
        }
        if (owner.tickCount % 12 == 0) {
            for (LivingEntity target : hostilesAround(level, owner, center, 5.5)) {
                target.hurt(level.damageSources().magic(), 4.5f);
                target.setTicksFrozen(Math.min(220, target.getTicksFrozen() + 30));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1, false, false));
            }
        }
    }

    private static void castLavosCatalyze(ServerPlayer player, ServerLevel level) {
        Vec3 origin = player.position();
        long now = level.getGameTime();
        for (LivingEntity target : hostilesInBox(level, player, 16.0)) {
            int statuses = countActiveStatuses(target, now);
            float dmg = 18.0f * (1.0f + statuses * 0.50f);
            target.igniteForSeconds(6.0f);
            target.hurt(level.damageSources().playerAttack(player), dmg);
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, statuses > 0 ? EffectProfile.SOLAR : EffectProfile.STRAND, origin.add(0, 1.0, 0), centerOf(target), 1.1f);
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 10 + statuses * 4, 0.4, 0.5, 0.4, 0.06);
        }
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.SOLAR, origin.add(0, 0.4, 0), origin.add(0, 0.4, 0), 15.0f);
        VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.STRAND, origin.add(0, 0.9, 0), origin.add(0, 0.9, 0), 12.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1f, 0.95f);
    }

    // ==================== SHARED HELPERS ====================

    public static void applyBleed(ServerPlayer owner, LivingEntity target, int durationTicks, float tickDamage) {
        CompoundTag data = target.getPersistentData();
        data.putLong(TAG_BLEED_UNTIL, owner.serverLevel().getGameTime() + durationTicks);
        data.putFloat(TAG_BLEED_DMG, tickDamage);
        data.putUUID(TAG_BLEED_OWNER, owner.getUUID());
    }

    public static void grantAbsorption(ServerPlayer player, float amount) {
        float next = Math.min(12.0f, player.getAbsorptionAmount() + amount);
        player.setAbsorptionAmount(next);
    }

    public static void chainArcLightning(ServerPlayer owner, ServerLevel level, LivingEntity primary, float damage, int extraTargets) {
        Set<UUID> hit = new HashSet<>();
        hit.add(primary.getUUID());
        Vec3 from = centerOf(primary);
        for (int i = 0; i < extraTargets; i++) {
            Vec3 origin = from;
            LivingEntity next = hostilesAround(level, owner, origin, 7.0).stream()
                    .filter(e -> !hit.contains(e.getUUID()))
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(origin)))
                    .orElse(null);
            if (next == null) break;
            hit.add(next.getUUID());
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, origin, centerOf(next), 0.9f);
            next.hurt(level.damageSources().magic(), damage);
            next.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
            from = centerOf(next);
        }
    }

    private static int countActiveStatuses(LivingEntity target, long now) {
        int count = 0;
        if (target.isOnFire()) count++;
        if (target.getTicksFrozen() > 0) count++;
        if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) count++;
        if (target.hasEffect(MobEffects.POISON)) count++;
        if (target.hasEffect(MobEffects.WITHER)) count++;
        if (target.hasEffect(MobEffects.WEAKNESS)) count++;
        if (target.hasEffect(MobEffects.GLOWING)) count++;
        if (target.hasEffect(MobEffects.BLINDNESS)) count++;
        if (target.hasEffect(MobEffects.LEVITATION)) count++;
        CompoundTag data = target.getPersistentData();
        if (data.getLong(TAG_BLEED_UNTIL) >= now) count++;
        if (data.getLong(TAG_CRYSTAL_UNTIL) >= now) count++;
        if (data.getLong(TAG_WRATH_UNTIL) >= now) count++;
        if (data.getLong(TAG_THARROS_UNTIL) >= now) count++;
        if (data.getLong(TAG_CHAOS_UNTIL) >= now) count++;
        return count;
    }

    private static Vec3 raycastPoint(ServerPlayer player, ServerLevel level, double maxDist) {
        return raycastPoint(player, level, player.getEyePosition(), player.getLookAngle().normalize(), maxDist);
    }

    private static Vec3 raycastPoint(ServerPlayer player, ServerLevel level, Vec3 start, Vec3 dir, double maxDist) {
        Vec3 end = start.add(dir.scale(maxDist));
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getLocation();
    }

    public static Vec3 centerOf(LivingEntity e) {
        return e.getBoundingBox().getCenter();
    }

    public static List<LivingEntity> hostilesInBox(ServerLevel level, ServerPlayer player, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> isValidHostile(player, e));
    }

    public static List<LivingEntity> hostilesAround(ServerLevel level, ServerPlayer owner, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        List<LivingEntity> found = new ArrayList<>();
        double r2 = radius * radius;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> isValidHostile(owner, e))) {
            if (e.position().distanceToSqr(center) <= r2) {
                found.add(e);
            }
        }
        return found;
    }

    public static boolean isValidHostile(ServerPlayer player, LivingEntity entity) {
        return entity != player && entity.isAlive() && !entity.isAlliedTo(player)
                && (entity instanceof Enemy || (entity instanceof Mob m && m.getTarget() == player));
    }
}
