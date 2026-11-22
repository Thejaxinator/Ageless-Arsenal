package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ShockBoltEntity extends ThrowableItemProjectile {
   public ShockBoltEntity(EntityType var1, Level var2) {
      super(var1, var2);
   }

   public ShockBoltEntity(Level var1, LivingEntity var2) {
      super((EntityType)ModRegistries.SHOCK_BOLT_ENTITY.get(), var2, var1);
   }

   protected Item getDefaultItem() {
      return (Item)ModRegistries.SHOCK_BOLT_ITEM.get();
   }

   protected double getDefaultGravity() {
      return 0.005;
   }

   public void tick() {
      super.tick();
      if (super.tickCount > 70) {
         this.discard();
      } else {
         Level var1 = this.level();
         if (var1 instanceof ServerLevel) {
            ((ServerLevel)var1).sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 1, 0.03, 0.03, 0.03, 0.01);
         }
      }
   }

   protected void onHitEntity(EntityHitResult var1) {
      super.onHitEntity(var1);
      Level var2 = this.level();
      if (!var2.isClientSide) {
         Entity var3 = var1.getEntity();
         if (!(var3 instanceof FallenMobEntity)) {
            var3.hurt(this.damageSources().thrown(this, this.getOwner()), 4.5F);
            if (var2 instanceof ServerLevel) {
               ((ServerLevel)var2).sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 10, 0.18, 0.18, 0.18, 0.08);
            }
         }

         this.discard();
      }
   }

   protected void onHitBlock(BlockHitResult var1) {
      super.onHitBlock(var1);
      Level var2 = this.level();
      if (!var2.isClientSide) {
         if (var2 instanceof ServerLevel) {
            ((ServerLevel)var2).sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 6, 0.12, 0.12, 0.12, 0.04);
         }

         this.discard();
      }
   }
}
