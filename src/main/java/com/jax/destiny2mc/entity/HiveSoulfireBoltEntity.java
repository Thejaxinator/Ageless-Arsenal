package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class HiveSoulfireBoltEntity extends ThrowableItemProjectile {
   public HiveSoulfireBoltEntity(EntityType var1, Level var2) {
      super(var1, var2);
   }

   public HiveSoulfireBoltEntity(Level var1, LivingEntity var2) {
      super((EntityType)ModRegistries.HIVE_SOULFIRE_BOLT_ENTITY.get(), var2, var1);
      this.setNoGravity(true);
   }

   protected Item getDefaultItem() {
      return (Item)ModRegistries.HIVE_SOULFIRE_BOLT_ITEM.get();
   }

   public void tick() {
      super.tick();
      if (super.tickCount > 70) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult var1) {
      if (!this.level().isClientSide) {
         Entity var2 = var1.getEntity();
         if (var2 instanceof Player) {
            LivingEntity var3 = (LivingEntity)var2;
            Entity var4 = this.getOwner();
            if (var4 instanceof LivingEntity) {
               var3.hurt(this.damageSources().mobAttack((LivingEntity)var4), 2.4F);
            } else {
               // Keep the direct bolt on the damage source if its shooter has unloaded.
               var3.hurt(this.damageSources().indirectMagic(this, null), 2.4F);
            }
         }
      }
   }

   protected void onHit(HitResult var1) {
      super.onHit(var1);
      if (!this.level().isClientSide) {
         ServerLevel var2 = (ServerLevel)this.level();
         var2.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(), 5, 0.15, 0.15, 0.15, 0.02);
         this.discard();
      }
   }
}
