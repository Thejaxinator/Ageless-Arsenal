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

public class ScorchRocketEntity extends ThrowableItemProjectile {
   public ScorchRocketEntity(EntityType var1, Level var2) {
      super(var1, var2);
   }

   public ScorchRocketEntity(Level var1, LivingEntity var2) {
      super((EntityType)ModRegistries.SCORCH_ROCKET_ENTITY.get(), var2, var1);
   }

   protected Item getDefaultItem() {
      return (Item)ModRegistries.SCORCH_ROCKET_ITEM.get();
   }

   protected double getDefaultGravity() {
      return 0.004;
   }

   private void explodeScorch() {
      Level var1 = this.level();
      if (var1 instanceof ServerLevel var2) {
         double var3 = this.getX();
         double var5 = this.getY();
         double var7 = this.getZ();
         var2.sendParticles(ParticleTypes.EXPLOSION, var3, var5, var7, 3, 0.3, 0.3, 0.3, 0.02);
         var2.sendParticles(ParticleTypes.FLAME, var3, var5, var7, 32, 0.9, 0.7, 0.9, 0.14);
         ServerBallisticsEngine.playSound(var1, var3, var5, var7, "entity.generic.explode", 0.9F, 1.15F);
         AABB var9 = new AABB(var3 - 3.6, var5 - 3.6, var7 - 3.6, var3 + 3.6, var5 + 3.6, var7 + 3.6);
         List var10 = var1.getEntities(this, var9);

         for (int var11 = 0; var11 < var10.size(); var11++) {
            Entity var12 = (Entity)var10.get(var11);
            if (var12 instanceof LivingEntity && !(var12 instanceof FallenMobEntity)) {
               var12.igniteForSeconds(3.5F);
               var12.hurt(this.damageSources().thrown(this, this.getOwner()), 9.5F);
            }
         }
      }

      this.discard();
   }

   public void tick() {
      super.tick();
      if (super.tickCount > 90) {
         this.discard();
      } else {
         Level var1 = this.level();
         if (var1 instanceof ServerLevel) {
            ((ServerLevel)var1).sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 2, 0.03, 0.03, 0.03, 0.01);
         }
      }
   }

   protected void onHitEntity(EntityHitResult var1) {
      super.onHitEntity(var1);
      Level var2 = this.level();
      if (!var2.isClientSide) {
         Entity var3 = var1.getEntity();
         if (!(var3 instanceof FallenMobEntity)) {
            this.explodeScorch();
            return;
         }

         this.discard();
      }
   }

   protected void onHitBlock(BlockHitResult var1) {
      super.onHitBlock(var1);
      Level var2 = this.level();
      if (!var2.isClientSide) {
         this.explodeScorch();
      }
   }
}
