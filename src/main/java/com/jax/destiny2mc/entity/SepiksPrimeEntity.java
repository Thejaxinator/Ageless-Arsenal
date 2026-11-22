/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.registries.DeferredHolder
 */
package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.entity.ShockBoltEntity;

import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import java.util.ArrayList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SepiksPrimeEntity
extends FallenMobEntity {
    public int phaseStage;
    public int shieldFailSafe;
    public ArrayList tetherGuards;

    public SepiksPrimeEntity(EntityType entityType, Level level) {
        super(entityType, level, 11, 0.0f);
        this.setNoGravity(true);
        this.phaseStage = 0;
        this.shieldFailSafe = 0;
        this.tetherGuards = new ArrayList();
    }

    public static AttributeSupplier createAttributes() {
        return FallenMobEntity.makeAttrs(400.0, 0.24, 9.0, 64.0);
    }

    protected LivingEntity summonAdd(ServerLevel serverLevel, DeferredHolder deferredHolder, double d, double d2, double d3) {
        Entity entity;
        EntityType entityType;
        if (deferredHolder != null && (entityType = (EntityType)deferredHolder.get()) != null && (entity = entityType.create((Level)serverLevel)) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.moveTo(this.getX() + d, this.getY() + d2, this.getZ() + d3);
            serverLevel.addFreshEntity((Entity)livingEntity);

            return livingEntity;
        }
        return null;
    }

    public boolean hasAliveTetherGuards() {
        ArrayList arrayList = this.tetherGuards;
        for (int i = 0; i < arrayList.size(); ++i) {
            LivingEntity livingEntity = (LivingEntity)arrayList.get(i);
            if (livingEntity == null || !livingEntity.isAlive()) {
                continue;
            }
            return true;
        }
        arrayList.clear();
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (!this.level().isClientSide && this.shieldFailSafe > 0) {
            if (this.hasAliveTetherGuards()) {
                ServerBallisticsEngine.playSound(this.level(), this.getX(), this.getY(), this.getZ(), "block.amethyst_block.chime", 1.0f, 0.6f);
                return false;
            }
            this.shieldFailSafe = 0;
            this.setAnim(6, 60);
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected void tickFallenCombat(ServerLevel serverLevel) {
        int n;
        LivingEntity livingEntity;
        float f = this.getHealth() / this.getMaxHealth();
        if (this.phaseStage == 0) {
            if (f <= 0.7f) {
                this.phaseStage = 1;
                this.shieldFailSafe = 380;
                this.setAnim(5, 380);
                livingEntity = this.summonAdd(serverLevel, ModRegistries.FALLEN_CAPTAIN_SCORCH, -5.0, -2.0, -6.0);
                if (livingEntity != null) {
                    this.tetherGuards.add(livingEntity);
                }
                this.summonAdd(serverLevel, ModRegistries.FALLEN_SHANK, 5.0, 0.0, -5.0);
            }
        } else if (this.phaseStage == 1 && f <= 0.35f) {
            LivingEntity livingEntity2;
            this.phaseStage = 2;
            this.shieldFailSafe = 380;
            this.setAnim(5, 380);
            livingEntity = this.summonAdd(serverLevel, ModRegistries.FALLEN_CAPTAIN_SCORCH, -5.5, -2.0, -6.0);
            if (livingEntity != null) {
                this.tetherGuards.add(livingEntity);
            }
            if ((livingEntity2 = this.summonAdd(serverLevel, ModRegistries.FALLEN_CAPTAIN_RIFLE, 5.5, -2.0, -6.0)) != null) {
                this.tetherGuards.add(livingEntity2);
            }
        }
        if ((n = this.shieldFailSafe) > 0) {
            this.shieldFailSafe = --n;
            if (!this.hasAliveTetherGuards()) {
                this.shieldFailSafe = 0;
                this.setAnim(6, 45);
            } else {
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.2, this.getZ(), 6, 1.2, 1.2, 1.2, 0.05);
            }
        }
        if ((livingEntity = this.getTarget()) != null && livingEntity.isAlive()) {
            this.getLookControl().setLookAt((Entity)livingEntity, 30.0f, 30.0f);
            if (this.dodgeCd <= 0) {
                this.dodgeCd = 110;
                this.blinkTeleport(serverLevel, 4.5, ParticleTypes.PORTAL);
            }
            if (this.specialCd <= 0 && !(this.distanceTo((Entity)livingEntity) > 5.5f)) {
                this.specialCd = 65;
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 35, 2.5, 1.5, 2.5, 0.2);
                livingEntity.hurt(this.damageSources().mobAttack((LivingEntity)this), 5.5f);
                this.heal(4.0f);
            }
            if (this.attackCd <= 0) {
                this.attackCd = 34;
                this.setAnim(1, 10);
                this.fireAimedProjectile((Projectile)new ShockBoltEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 1.55f, 0.6f);
                this.fireAimedProjectile((Projectile)new ShockBoltEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 1.55f, 3.2f);
                this.fireAimedProjectile((Projectile)new ShockBoltEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 1.55f, 5.4f);
                ServerBallisticsEngine.playSound((Level)serverLevel, this.getX(), this.getY(), this.getZ(), "entity.shulker.shoot", 1.1f, 0.7f);
            }
        }
    }
}

