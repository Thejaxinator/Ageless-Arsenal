package com.jax.destiny2mc.tenno.abilities;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import com.jax.destiny2mc.binder.BinderState;
import com.jax.destiny2mc.binder.WarframeBinderCatalog;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/** Void-Bound passives are equipped in the Energy Binder's shared passive slots,
 * while active ability buff timers always tick for any player who cast them. */
public final class WarframePassiveAbilities {
    private static final ThreadLocal<Boolean> IN_PROC = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private WarframePassiveAbilities() {
    }

    public static void tick(ServerPlayer player) {
        if (player == null || player.isSpectator() || !player.isAlive()) {
            return;
        }
        WarframeAbilityState state = WarframeAbilityState.get(player.getUUID());
        state.tickTimers();
        WarframeActiveAbilities.tickActiveEffects(player);

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        Vec3 prev = state.lastPosition();
        double horizontalDist = (prev != null) ? Math.hypot(pos.x - prev.x, pos.z - prev.z) : 0.0;
        boolean moving = horizontalDist > 0.05 && horizontalDist < 6.0;

        // Uriel: Hellfire Body
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_URIEL)) {
            boolean nearFire = player.isOnFire() || player.isInLava();
            if (player.isOnFire()) {
                player.clearFire();
            }
            if (player.tickCount % 40 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0, true, false));
                if (!nearFire) {
                    for (LivingEntity e : WarframeActiveAbilities.hostilesInBox(level, player, 8.0)) {
                        if (e.isOnFire()) {
                            nearFire = true;
                            break;
                        }
                    }
                }
                if (nearFire) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                }
            }
        }

        // Excalibur: Swordsmanship
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_EXCALIBUR)) {
            if (player.getMainHandItem().getItem() instanceof SwordItem && player.tickCount % 20 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 1, true, false));
            }
        }

        // Mag: Magnetic Vacuum (11-block item + XP pull + short Absorption on loot)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_MAG)) {
            if (player.tickCount % 5 == 0) {
                Vec3 center = pos.add(0.0, 0.6, 0.0);
                boolean pulledAny = false;
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(11.0))) {
                    if (!item.hasPickUpDelay()) {
                        Vec3 pull = center.subtract(item.position()).normalize().scale(0.38);
                        item.setDeltaMovement(pull);
                        pulledAny = true;
                    }
                }
                for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(11.0))) {
                    Vec3 pull = center.subtract(orb.position()).normalize().scale(0.38);
                    orb.setDeltaMovement(pull);
                    pulledAny = true;
                }
                if (pulledAny && state.tryConsumeMagLootShield()) {
                    WarframeActiveAbilities.grantAbsorption(player, 1.5f);
                }
            }
        }

        // Volt: Static Discharge (faster buildup, cap 150)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VOLT)) {
            if (prev != null && player.onGround() && horizontalDist > 0.01 && horizontalDist < 4.5) {
                state.addVoltStaticCharge((float) (horizontalDist * 10.0));
            }
        }

        // Voruna: Pack Stride
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VORUNA)) {
            if (player.tickCount % 20 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, true, false));
            }
        }

        // Valkyr: Nimble Landing (fall damage immunity handled in onPlayerLanded / LivingFallEvent)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VALKYR)) {
            if (player.fallDistance > 3.0f && player.onGround()) {
                player.fallDistance = 0.0f;
            }
        }

        // Follie: Inkblot (damaging, blinding ink wake while sprinting)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_FOLLIE)) {
            if (player.isSprinting() && player.tickCount % 6 == 0) {
                Vec3 back = player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(-1.5);
                Vec3 wake = pos.add(back);
                level.sendParticles(ParticleTypes.SQUID_INK, wake.x, wake.y + 0.3, wake.z, 5, 0.45, 0.25, 0.45, 0.01);
                for (LivingEntity target : WarframeActiveAbilities.hostilesAround(level, player, wake, 3.5)) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1, false, false));
                    target.hurt(level.damageSources().magic(), 3.0f);
                }
            }
        }

        // Citrine: Geoluminescence (team health regen scaling with kills)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_CITRINE) && player.tickCount % 40 == 0) {
            float heal = 1.0f + Math.min(1.5f, state.citrineKillStacks() * 0.1f);
            for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(10.0))) {
                if (ally.isAlive() && !ally.isSpectator() && ally.getHealth() < ally.getMaxHealth()) {
                    ally.heal(heal);
                }
            }
        }

        // Caliban: Adaptive Armor (share Resistance I with nearby allies while stacks active)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_CALIBAN)
                && state.calibanAdaptiveStacks() > 0 && player.tickCount % 20 == 0) {
            for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(12.0))) {
                if (ally != player && ally.isAlive() && !ally.isSpectator()) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 35, 0, true, false));
                }
            }
        }

        // Yareli: Critical Flow (continuous movement buff)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_YARELI)) {
            if (state.updateYareliMovement(moving) && player.tickCount % 20 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, true, false));
            }
        }

        // Lavos: Valence Alchemist (automatic harmful status cleanse)
        if (BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_LAVOS) && player.tickCount % 15 == 0) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            player.removeEffect(MobEffects.HUNGER);
        }

        state.setLastPosition(pos);
    }

    public static float modifyOutgoingMeleeDamage(ServerPlayer attacker, LivingEntity target, float baseDamage) {
        if (IN_PROC.get()) {
            return baseDamage;
        }
        WarframeAbilityState state = WarframeAbilityState.get(attacker.getUUID());
        ServerLevel level = attacker.serverLevel();
        long now = level.getGameTime();
        float damage = baseDamage;
        boolean holdingSword = attacker.getMainHandItem().getItem() instanceof SwordItem;

        // Target vulnerability debuffs from Void-Bound abilities
        CompoundTag targetData = target.getPersistentData();
        if (targetData.getLong(WarframeActiveAbilities.TAG_WRATH_UNTIL) >= now) {
            damage *= 1.45f;
        }
        if (targetData.getLong(WarframeActiveAbilities.TAG_THARROS_UNTIL) >= now) {
            damage *= 1.40f;
        }
        if (targetData.getLong(WarframeActiveAbilities.TAG_CHAOS_UNTIL) >= now) {
            damage *= 1.35f;
        }
        boolean crystalShatter = targetData.getLong(WarframeActiveAbilities.TAG_CRYSTAL_UNTIL) >= now;
        if (crystalShatter) {
            damage *= 2.0f;
            targetData.remove(WarframeActiveAbilities.TAG_CRYSTAL_UNTIL);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.15);
            VoxelEffects.emit(level, VoxelCue.Kind.BURST, EffectProfile.RESONANCE, WarframeActiveAbilities.centerOf(target), WarframeActiveAbilities.centerOf(target), 3.5f);
        }

        // Excalibur: Swordsmanship (+25% with swords)
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_EXCALIBUR) && holdingSword) {
            damage *= 1.25f;
        }

        // Kullervo: Heavy Blade (+25% melee + every 3rd hit 1.5x heavy strike)
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_KULLERVO)) {
            damage *= 1.25f;
            if (state.incrementKullervoHits() == 0) {
                damage *= 1.50f;
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 14, 0.35, 0.4, 0.35, 0.12);
            }
        }

        // Kullervo: Wrathful Advance (+120% red-crit melee buff)
        if (state.wrathfulCritTicks() > 0) {
            damage *= 2.20f;
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.12);
        }

        // Voruna: Shroud of Dynar -> breaks stealth into 4s Ambush window (2x + heavy bleed)
        if (state.shroudTicks() > 0) {
            state.setShroudTicks(0);
            attacker.removeEffect(MobEffects.INVISIBILITY);
            state.setDynarAmbushTicks(80);
        }
        if (state.dynarAmbushTicks() > 0) {
            damage *= 2.0f;
            WarframeActiveAbilities.applyBleed(attacker, target, 80, 4.0f);
            level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.05);
        }

        // Valkyr: Warcry (18% melee lifesteal up to 5 HP/hit)
        if (state.warcryTicks() > 0) {
            attacker.heal(Math.min(5.0f, damage * 0.18f));
        }

        // Frost: Cryogenic Shell (+15% vs slowed/frozen foes)
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_FROST)
                && (target.getTicksFrozen() > 0 || target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))) {
            damage *= 1.15f;
        }

        // Styanax: Hoplite (+20% while >= 75% HP or shielded)
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_STYANAX)
                && (attacker.getHealth() >= attacker.getMaxHealth() * 0.75f || attacker.getAbsorptionAmount() > 0.0f)) {
            damage *= 1.20f;
        }

        // Yareli: Critical Flow (+35% while moving)
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_YARELI)
                && state.isYareliCriticalFlowActive()) {
            damage *= 1.35f;
        }

        // Gyre: Cathode Surge (+25% vs shocked/slowed/glowing)
        if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_GYRE)
                && (target.hasEffect(MobEffects.GLOWING) || target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))) {
            damage *= 1.25f;
        }

        // Secondary on-hit procs guarded against recursion
        IN_PROC.set(Boolean.TRUE);
        try {
            if (crystalShatter) {
                for (LivingEntity splash : WarframeActiveAbilities.hostilesAround(level, attacker, target.position(), 4.5)) {
                    if (splash != target) {
                        splash.hurt(level.damageSources().magic(), 10.0f);
                    }
                }
            }

            // Volt: Static Discharge (up to +18 bonus damage + chain to 2 foes)
            if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_VOLT)
                    && state.voltStaticCharge() >= 15.0f) {
                float bonus = state.consumeVoltStaticCharge() * 0.12f;
                damage += bonus;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 14, 0.35, 0.4, 0.35, 0.1);
                WarframeActiveAbilities.chainArcLightning(attacker, level, target, Math.max(4.0f, bonus * 0.6f), 2);
            }

            // Gyre: Rotorswell active lightning discharge on hit
            if (state.rotorswellTicks() > 0) {
                target.hurt(level.damageSources().magic(), 14.0f);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, false));
                VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.ARC, WarframeActiveAbilities.centerOf(attacker), WarframeActiveAbilities.centerOf(target), 1.1f);
                WarframeActiveAbilities.chainArcLightning(attacker, level, target, 8.0f, 2);
            } else if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_GYRE)
                    && attacker.getRandom().nextFloat() < 0.30f) {
                WarframeActiveAbilities.chainArcLightning(attacker, level, target, 6.0f, 1);
            }

            // Citrine: Prismatic Gem fires a beam whenever owner hits a hostile in range
            if (state.prismaticGemTicks() > 0 && state.prismaticGemCenter() != null) {
                WarframeActiveAbilities.firePrismaticGemBeam(attacker, level, state, target);
            }

            // Lavos: Valence Alchemist cycling elemental imbue every 3s
            if (BinderState.hasPassive(attacker, WarframeBinderCatalog.PASSIVE_LAVOS)
                    && state.tryConsumeLavosElement()) {
                int elem = state.nextLavosElement();
                switch (elem) {
                    case 0 -> target.igniteForSeconds(5.0f);
                    case 1 -> {
                        target.setTicksFrozen(140);
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
                    }
                    case 2 -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true));
                    default -> WarframeActiveAbilities.chainArcLightning(attacker, level, target, 5.0f, 1);
                }
            }
        } finally {
            IN_PROC.set(Boolean.FALSE);
        }

        return damage;
    }

    /** Returns -1f to cancel damage completely, or the modified incoming damage amount. */
    public static float modifyIncomingDamage(ServerPlayer defender, LivingEntity attacker, float amount) {
        WarframeAbilityState state = WarframeAbilityState.get(defender.getUUID());
        ServerLevel level = defender.serverLevel();

        // Excalibur Slash Dash brief i-frames
        if (state.slashDashInvulnTicks() > 0) {
            return -1.0f;
        }

        // Nyx: Psychic Evasion (25% negate + reflect weakening psychic bolt)
        if (BinderState.hasPassive(defender, WarframeBinderCatalog.PASSIVE_NYX)
                && defender.getRandom().nextFloat() < 0.25f) {
            level.sendParticles(ParticleTypes.WITCH, defender.getX(), defender.getY() + 1.0, defender.getZ(), 10, 0.35, 0.45, 0.35, 0.04);
            if (attacker != null && WarframeActiveAbilities.isValidHostile(defender, attacker)) {
                attacker.hurt(level.damageSources().magic(), 8.0f);
                attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true));
                VoxelEffects.emit(level, VoxelCue.Kind.TRACE, EffectProfile.VOID, WarframeActiveAbilities.centerOf(defender), WarframeActiveAbilities.centerOf(attacker), 0.8f);
            }
            return -1.0f;
        }

        // Frost: Cryogenic Shell (35% deep-freeze + cold retaliation)
        if (BinderState.hasPassive(defender, WarframeBinderCatalog.PASSIVE_FROST)
                && attacker != null && defender.getRandom().nextFloat() < 0.35f) {
            attacker.setTicksFrozen(160);
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3, false, true));
            attacker.hurt(level.damageSources().freeze(), 6.0f);
        }

        // Uriel: Hellfire Body (ignite attackers on contact)
        if (BinderState.hasPassive(defender, WarframeBinderCatalog.PASSIVE_URIEL)
                && attacker != null && WarframeActiveAbilities.isValidHostile(defender, attacker)) {
            attacker.igniteForSeconds(4.0f);
        }

        // Caliban: Adaptive Armor (up to 50% stacking damage reduction)
        if (BinderState.hasPassive(defender, WarframeBinderCatalog.PASSIVE_CALIBAN)) {
            int stacks = state.calibanAdaptiveStacks();
            state.addCalibanAdaptiveStack();
            if (stacks > 0) {
                float reduction = Math.min(0.50f, stacks * 0.10f);
                amount *= (1.0f - reduction);
            }
        }

        return amount;
    }

    public static void onEnemyKilled(ServerPlayer killer, LivingEntity victim) {
        if (killer == null || victim == null || !WarframeActiveAbilities.isValidHostile(killer, victim)) {
            return;
        }
        WarframeAbilityState state = WarframeAbilityState.get(killer.getUUID());

        // Valkyr Eternal Warcry: extend active Warcry by +2s on kill (cap 400t)
        if (state.warcryTicks() > 0) {
            int next = Math.min(400, state.warcryTicks() + 40);
            state.setWarcryTicks(next);
            WarframeActiveAbilities.applyWarcryBuffs(killer, next);
        }

        // Voruna Shroud of Dynar: extend Ambush by +3s and heal 3 HP on kill
        if (state.dynarAmbushTicks() > 0) {
            state.setDynarAmbushTicks(Math.min(160, state.dynarAmbushTicks() + 60));
            killer.heal(3.0f);
        }

        // Gyre Rotorswell: extend by +2s on kill (cap 360t)
        if (state.rotorswellTicks() > 0) {
            state.setRotorswellTicks(Math.min(360, state.rotorswellTicks() + 40));
        }

        // Citrine Geoluminescence: ramp team regen on kills
        if (BinderState.hasPassive(killer, WarframeBinderCatalog.PASSIVE_CITRINE)) {
            state.addCitrineKillStack();
        }

        // Styanax Hoplite: grant +2 Absorption on kills
        if (BinderState.hasPassive(killer, WarframeBinderCatalog.PASSIVE_STYANAX)) {
            WarframeActiveAbilities.grantAbsorption(killer, 2.0f);
        }
    }

    /** Returns true if fall damage should be cancelled (Valkyr Nimble Landing). */
    public static boolean onPlayerLanded(ServerPlayer player, float fallDistance) {
        if (!BinderState.hasPassive(player, WarframeBinderCatalog.PASSIVE_VALKYR)) {
            return false;
        }
        if (fallDistance >= 4.0f) {
            ServerLevel level = player.serverLevel();
            VoxelEffects.emit(level, VoxelCue.Kind.RING, EffectProfile.SOLAR, player.position().add(0, 0.2, 0), player.position().add(0, 0.2, 0), 5.0f);
            for (LivingEntity target : WarframeActiveAbilities.hostilesInBox(level, player, 5.5)) {
                target.hurt(level.damageSources().playerAttack(player), 10.0f);
                Vec3 push = target.position().subtract(player.position()).normalize().scale(0.9);
                target.push(push.x, 0.35, push.z);
                target.hurtMarked = true;
            }
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.75f, 1.3f);
        }
        return true;
    }

    /** Legacy boolean helper retained for compatibility. */
    public static boolean onPlayerDamaged(ServerPlayer defender, LivingEntity attacker) {
        return modifyIncomingDamage(defender, attacker, 1.0f) < 0.0f;
    }
}
