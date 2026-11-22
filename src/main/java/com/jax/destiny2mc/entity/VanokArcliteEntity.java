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
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.neoforge.registries.DeferredHolder
 */
package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.entity.ShockBoltEntity;
import com.jax.destiny2mc.entity.WireRifleBoltEntity;

import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredHolder;

public class VanokArcliteEntity
extends FallenMobEntity {
    public int phaseStage = 0;
    public int shieldFailSafe = 0;
    public int thunderTicks = 0;
    public double thunderX;
    public double thunderY;
    public double thunderZ;
    public ArrayList tetherServitors = new ArrayList();

    public VanokArcliteEntity(EntityType entityType, Level level) {
        super(entityType, level, 13, 45.0f);
    }

    public static AttributeSupplier createAttributes() {
        return FallenMobEntity.makeAttrs(420.0, 0.29, 10.0, 64.0);
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

    public boolean hasAliveBlastServitors() {
        ArrayList arrayList = this.tetherServitors;
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
            if (this.hasAliveBlastServitors()) {
                ServerBallisticsEngine.playSound(this.level(), this.getX(), this.getY(), this.getZ(), "block.respawn_anchor.deplete", 0.9f, 1.65f);
                return false;
            }
            this.shieldFailSafe = 0;
            this.setAnim(6, 50);
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected void tickFallenCombat(ServerLevel serverLevel) {
        int n;
        int n2;
        LivingEntity livingEntity;
        float f = this.getHealth() / this.getMaxHealth();
        if (this.phaseStage == 0) {
            if (f <= 0.65f) {
                LivingEntity livingEntity2;
                this.phaseStage = 1;
                this.shieldFailSafe = 400;
                this.setAnim(5, 400);
                livingEntity = this.summonAdd(serverLevel, ModRegistries.BLAST_SERVITOR, -6.0, 1.5, -6.0);
                if (livingEntity != null) {
                    this.tetherServitors.add(livingEntity);
                }
                if ((livingEntity2 = this.summonAdd(serverLevel, ModRegistries.BLAST_SERVITOR, 6.0, 1.5, -6.0)) != null) {
                    this.tetherServitors.add(livingEntity2);
                    if (!(this.getZ() < 185.0)) {
                        this.blinkTeleport(serverLevel, 4.0, ParticleTypes.PORTAL);
                    }
                }
            }
        } else if (this.phaseStage == 1 && f <= 0.35f) {
            this.phaseStage = 2;
            this.shieldFailSafe = 360;
            livingEntity = this.summonAdd(serverLevel, ModRegistries.BLAST_SERVITOR, 0.0, 2.0, -5.0);
            if (livingEntity != null) {
                this.tetherServitors.add(livingEntity);
            }
            this.summonAdd(serverLevel, ModRegistries.FALLEN_BRUISER, -4.5, 0.0, -6.0);
        }
        if ((n2 = this.shieldFailSafe) > 0) {
            this.shieldFailSafe = --n2;
            if (!this.hasAliveBlastServitors()) {
                this.shieldFailSafe = 0;
                if (!(this.getY() < 80.5)) {
                    this.blinkTeleport(serverLevel, 4.0, ParticleTypes.PORTAL);
                }
            } else {
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.4, this.getZ(), 8, 0.7, 1.1, 0.7, 0.08);
            }
        }
        if ((n = this.thunderTicks) > 0) {
            this.thunderTicks = --n;
            double d = this.thunderX;
            double d2 = this.thunderY;
            double d3 = this.thunderZ;
            if (n <= 0) {
                ServerBallisticsEngine.spawnLightning(serverLevel, d, d2, d3);
                ServerBallisticsEngine.playSound((Level)serverLevel, d, d2, d3, "entity.lightning_bolt.thunder", 1.1f, 1.25f);
                AABB aABB = new AABB(d - 3.4, d2 - 1.5, d3 - 3.4, d + 3.4, d2 + 3.5, d3 + 3.4);
                List list = serverLevel.getEntities((Entity)this, aABB);
                for (int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity)list.get(i);
                    if (!(entity instanceof Player)) continue;
                    ((LivingEntity)entity).hurt(this.damageSources().mobAttack((LivingEntity)this), 9.5f);
                }
            } else {
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, d, d2 + 0.2, d3, 14, 1.8, 0.1, 1.8, 0.02);
            }
        }
        if ((livingEntity = this.getTarget()) != null && livingEntity.isAlive()) {
            this.getLookControl().setLookAt((Entity)livingEntity, 30.0f, 30.0f);
            if (this.specialCd <= 0) {
                this.specialCd = this.phaseStage == 2 ? 50 : 75;
                this.thunderTicks = 28;
                this.thunderX = livingEntity.getX();
                this.thunderY = livingEntity.getY();
                this.thunderZ = livingEntity.getZ();
                this.setAnim(3, 18);
                ServerBallisticsEngine.playSound((Level)serverLevel, this.getX(), this.getY(), this.getZ(), "block.beacon.activate", 1.0f, 1.85f);
            }
            if (this.attackCd <= 0) {
                this.attackCd = 30;
                this.setAnim(1, 12);
                this.fireAimedProjectile((Projectile)new WireRifleBoltEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 2.35f, 0.4f);
                if (this.phaseStage >= 2) {
                    this.fireAimedProjectile((Projectile)new ShockBoltEntity((Level)serverLevel, (LivingEntity)this), livingEntity, 1.9f, 2.8f);
                }
            }
            if (this.shieldFailSafe <= 0 && this.dodgeCd <= 0) {
                this.dodgeCd = 85;
                this.blinkTeleport(serverLevel, 4.0, ParticleTypes.ELECTRIC_SPARK);
            }
        }
    }
}

