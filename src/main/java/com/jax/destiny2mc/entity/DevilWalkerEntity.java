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
import com.jax.destiny2mc.entity.ScorchRocketEntity;
import com.jax.destiny2mc.entity.ShockBoltEntity;
import com.jax.destiny2mc.entity.ShockGrenadeEntity;

import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
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

public class DevilWalkerEntity
extends FallenMobEntity {
    public float legIntegrity = 55.0f;
    public int legsBrokenCount = 0;
    public int cannonChargeTicks = 0;
    public int dronePhase = 0;

    public DevilWalkerEntity(EntityType entityType, Level level) {
        super(entityType, level, 10, 0.0f);
    }

    public static AttributeSupplier createAttributes() {
        return FallenMobEntity.makeAttrs(360.0, 0.14, 10.0, 64.0);
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

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (!this.level().isClientSide) {
            if (this.getAnimState() == 2) {
                f *= 2.4f;
                ((ServerLevel)this.level()).sendParticles((ParticleOptions)ParticleTypes.LAVA, this.getX(), this.getY() + 1.4, this.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            } else {
                float f2 = this.legIntegrity - f;
                if (!(f2 > 0.0f)) {
                    this.legIntegrity = 55.0f;
                    ++this.legsBrokenCount;
                    this.setAnim(2, 130);
                    this.cannonChargeTicks = 0;
                    ServerLevel serverLevel = (ServerLevel)this.level();
                    serverLevel.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 6, 0.9, 0.5, 0.9, 0.05);
                    ServerBallisticsEngine.playSound((Level)serverLevel, this.getX(), this.getY(), this.getZ(), "entity.iron_golem.damage", 1.4f, 0.55f);
                } else {
                    this.legIntegrity = f2;
                    f *= 0.55f;
                }
            }
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected void tickFallenCombat(ServerLevel serverLevel) {
        float f = this.getHealth() / this.getMaxHealth();
        if (this.dronePhase == 0) {
            if (f <= 0.65f) {
                this.dronePhase = 1;
                this.summonAdd(serverLevel, ModRegistries.FALLEN_SHANK, -3.0, 2.5, -2.0);
                this.summonAdd(serverLevel, ModRegistries.FALLEN_SHANK, 3.0, 2.5, -2.0);
            }
        } else if (this.dronePhase == 1 && f <= 0.3f) {
            this.dronePhase = 2;
            this.summonAdd(serverLevel, ModRegistries.FALLEN_SHANK, -3.5, 2.5, -2.5);
            this.summonAdd(serverLevel, ModRegistries.FALLEN_CAPTAIN_SCORCH, 0.0, 0.5, -4.0);
        }
        if (this.getAnimState() == 2) {
            if (((Entity)this).tickCount % 4 == 0) {
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.4, 0.3, 0.4, 0.04);
            }
            if (this.getAnimTimer() == 1) {
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5, this.getZ(), 4, 1.8, 0.2, 1.8, 0.05);
                return;
            }
        } else {
            LivingEntity livingEntity = this.getTarget();
            if (livingEntity != null && livingEntity.isAlive()) {
                this.getLookControl().setLookAt((Entity)livingEntity, 24.0f, 24.0f);
                int n = this.cannonChargeTicks;
                if (n > 0) {
                    this.cannonChargeTicks = --n;
                    serverLevel.sendParticles((ParticleOptions)ParticleTypes.FLAME, livingEntity.getX(), livingEntity.getEyeY(), livingEntity.getZ(), 3, 0.1, 0.1, 0.1, 0.01);
                    if (n <= 0) {
                        this.setAnim(1, 12);
                        this.fireAimedProjectile((Projectile)new ScorchRocketEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 2.15f, 0.6f);
                        ServerBallisticsEngine.playSound((Level)serverLevel, this.getX(), this.getY(), this.getZ(), "entity.generic.explode", 1.2f, 0.75f);
                        this.specialCd = 85;
                    }
                } else if (this.specialCd <= 0) {
                    this.cannonChargeTicks = 24;
                    this.setAnim(3, 24);
                    ServerBallisticsEngine.playSound((Level)serverLevel, this.getX(), this.getY(), this.getZ(), "block.beacon.power_select", 1.1f, 1.75f);
                }
                if (this.attackCd <= 0) {
                    this.attackCd = 32;
                    this.fireAimedProjectile((Projectile)new ShockBoltEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 1.65f, 1.8f);
                    if (this.dodgeCd <= 0) {
                        this.dodgeCd = 75;
                        this.fireAimedProjectile((Projectile)new ShockGrenadeEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 1.15f, 2.5f);
                    }
                }
            }
        }
    }
}

