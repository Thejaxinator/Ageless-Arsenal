package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ShockGrenadeEntity extends ThrowableItemProjectile {
   private boolean planted = false;
   private int fuseTicks = 24;

   public ShockGrenadeEntity(EntityType var1, Level var2) {
      super(var1, var2);
   }

   public ShockGrenadeEntity(Level var1, LivingEntity var2) {
      super((EntityType)ModRegistries.SHOCK_GRENADE_ENTITY.get(), var2, var1);
   }

   protected Item getDefaultItem() {
      return (Item)ModRegistries.SHOCK_GRENADE_ITEM.get();
   }

   protected double getDefaultGravity() {
      return 0.045;
   }

   private void detonateArcBurst() {
      Level var1 = this.level();
      if (var1 instanceof ServerLevel var2) {
         double var3 = this.getX();
         double var5 = this.getY();
         double var7 = this.getZ();
         var2.sendParticles(ParticleTypes.ELECTRIC_SPARK, var3, var5 + 0.3, var7, 45, 1.2, 0.8, 1.2, 0.25);
         var2.sendParticles(ParticleTypes.END_ROD, var3, var5 + 0.4, var7, 18, 0.7, 0.7, 0.7, 0.12);
         ServerBallisticsEngine.playSound(var1, var3, var5, var7, "entity.lightning_bolt.impact", 0.75F, 1.55F);
         AABB var9 = new AABB(var3 - 3.8, var5 - 2.5, var7 - 3.8, var3 + 3.8, var5 + 3.0, var7 + 3.8);
         List var10 = var1.getEntities(this, var9);

         for (int var11 = 0; var11 < var10.size(); var11++) {
            Entity var12 = (Entity)var10.get(var11);
            if (var12 instanceof LivingEntity && !(var12 instanceof FallenMobEntity)) {
               var12.hurt(this.damageSources().thrown(this, this.getOwner()), 3.2F);
            }
         }
      }

      this.discard();
   }

   public void tick() {
      if (!this.planted) {
         super.tick();
         if (super.tickCount > 100) {
            this.detonateArcBurst();
            return;
         }

         Level var1 = this.level();
         if (var1 instanceof ServerLevel) {
            ((ServerLevel)var1).sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
            return;
         }
      } else {
         this.setDeltaMovement(0.0, 0.0, 0.0);
         Level var3 = this.level();
         if (!var3.isClientSide) {
            int var2 = this.fuseTicks - 1;
            this.fuseTicks = var2;
            ((ServerLevel)var3).sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.25, this.getZ(), 6, 0.25, 0.35, 0.25, 0.06);
            if (var2 % 4 == 0) {
               ServerBallisticsEngine.playSound(var3, this.getX(), this.getY(), this.getZ(), "block.note_block.hat", 0.75F, 1.85F);
            }

            if (var2 <= 0) {
               this.detonateArcBurst();
            }
         }
      }
   }

   protected void onHitBlock(BlockHitResult var1) {
      super.onHitBlock(var1);
      if (!this.planted) {
         this.planted = true;
         this.setNoGravity(true);
         this.setDeltaMovement(0.0, 0.0, 0.0);
      }
   }

   protected void onHitEntity(EntityHitResult var1) {
      if (!(var1.getEntity() instanceof FallenMobEntity)) {
         this.planted = true;
         this.fuseTicks = 10;
         this.setNoGravity(true);
         this.setDeltaMovement(0.0, 0.0, 0.0);
      }
   }
}
