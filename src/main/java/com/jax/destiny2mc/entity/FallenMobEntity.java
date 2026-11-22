package com.jax.destiny2mc.entity;

import com.jax.destiny2mc.combat.CombatSupport;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FallenMobEntity extends Monster {
   public static final EntityDataAccessor DATA_ANIM_STATE = SynchedEntityData.defineId(FallenMobEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor DATA_ANIM_TIMER = SynchedEntityData.defineId(FallenMobEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor DATA_SHIELD_HP = SynchedEntityData.defineId(FallenMobEntity.class, EntityDataSerializers.FLOAT);
   public final int unitKind;
   public final float maxShield;
   protected int shieldRegenDelay;
   protected int attackCd;
   protected int specialCd;
   protected int dodgeCd;
   protected int burstLeft;
   protected float strafeDir;

   public FallenMobEntity(EntityType var1, Level var2, int var3, float var4) {
      super(var1, var2);
      this.unitKind = var3;
      this.maxShield = var4;
      this.shieldRegenDelay = 0;
      this.attackCd = 18;
      this.specialCd = 55;
      this.dodgeCd = 30;
      this.burstLeft = 0;
      this.strafeDir = 1.0F;
      if (var4 > 0.0F) {
         this.getEntityData().set(DATA_SHIELD_HP, var4);
      }
   }

   protected void defineSynchedData(Builder var1) {
      super.defineSynchedData(var1);
      var1.define(DATA_ANIM_STATE, 0);
      var1.define(DATA_ANIM_TIMER, 0);
      var1.define(DATA_SHIELD_HP, 0.0F);
   }

   public int getAnimState() {
      return (Integer)this.getEntityData().get(DATA_ANIM_STATE);
   }

   public int getAnimTimer() {
      return (Integer)this.getEntityData().get(DATA_ANIM_TIMER);
   }

   public float getShieldHp() {
      return (Float)this.getEntityData().get(DATA_SHIELD_HP);
   }

   public void setShieldHp(float var1) {
      this.getEntityData().set(DATA_SHIELD_HP, var1);
   }

   public void setAnim(int var1, int var2) {
      this.getEntityData().set(DATA_ANIM_STATE, var1);
      this.getEntityData().set(DATA_ANIM_TIMER, var2);
   }

   protected void registerGoals() {
      super.goalSelector.addGoal(1, new FloatGoal(this));
      super.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85));
      super.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20.0F));
      super.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      super.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      super.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
   }

   protected void blinkTeleport(ServerLevel var1, double var2, SimpleParticleType var4) {
      double var5 = this.getX();
      double var7 = this.getY();
      double var9 = this.getZ();
      var1.sendParticles(var4, var5, var7 + 1.0, var9, 24, 0.35, 0.75, 0.35, 0.08);
      ServerBallisticsEngine.playSound(var1, var5, var7, var9, "entity.enderman.teleport", 0.85F, 1.25F);
      double var11 = Math.random() * 6.2831853;
      double var13 = var5 + Math.cos(var11) * var2;
      double var15 = var9 + Math.sin(var11) * var2;
      CombatSupport.safeEntityTeleport(this, var13, var7, var15);
      var1.sendParticles(var4, var13, var7 + 1.0, var15, 24, 0.35, 0.75, 0.35, 0.08);
   }

   protected void fireAimedProjectile(Projectile var1, LivingEntity var2, float var3, float var4) {
      var1.setPos(this.getX(), this.getEyeY() - 0.22, this.getZ());
      double var5 = var2.getX() - this.getX();
      double var7 = var2.getEyeY() - 0.25 - var1.getY();
      double var9 = var2.getZ() - this.getZ();
      var1.shoot(var5, var7, var9, var3, var4);
      this.level().addFreshEntity(var1);
   }

   public void tick() {
      super.tick();
      Level var1 = this.level();
      if (!var1.isClientSide) {
         if (this.attackCd > 0) {
            this.attackCd--;
         }

         if (this.specialCd > 0) {
            this.specialCd--;
         }

         if (this.dodgeCd > 0) {
            this.dodgeCd--;
         }

         if (this.shieldRegenDelay > 0) {
            this.shieldRegenDelay--;
         }

         int var2 = this.getAnimTimer();
         if (var2 > 0) {
            if (--var2 <= 0) {
               this.setAnim(0, 0);
            } else {
               this.getEntityData().set(DATA_ANIM_TIMER, var2);
            }
         }

         if (this.maxShield > 0.0F) {
            float var3 = this.getShieldHp();
            if (var3 > 0.0F && super.tickCount % 5 == 0) {
               ((ServerLevel)var1)
                  .sendParticles(
                     this.unitKind == 6 ? ParticleTypes.PORTAL : ParticleTypes.ELECTRIC_SPARK,
                     this.getX(),
                     this.getY() + 1.1,
                     this.getZ(),
                     4,
                     0.38,
                     0.55,
                     0.38,
                     0.02
                  );
            }

            if (this.shieldRegenDelay <= 0 && var3 < this.maxShield) {
               float var4 = var3 + 0.45F;
               if (var4 > this.maxShield) {
                  var4 = this.maxShield;
               }

               this.setShieldHp(var4);
            }
         }

         if (this.isAlive()) {
            this.tickFallenCombat((ServerLevel)var1);
         }
      }
   }

   protected void tickFallenCombat(ServerLevel var1) {
   }

   public boolean hurt(DamageSource var1, float var2) {
      if (!this.level().isClientSide) {
         this.shieldRegenDelay = 100;
         float var3 = this.getShieldHp();
         if (var3 > 0.0F) {
            float var4 = var2;
            Entity var5 = var1.getEntity();
            if (var5 instanceof LivingEntity) {
               ItemStack var6 = ((LivingEntity)var5).getItemInHand(InteractionHand.MAIN_HAND);
               if (var6.getItem() instanceof DestinyGunItem && ((DestinyGunItem)var6.getItem()).spec.element == 2) {
                  var4 = var2 * 2.5F;
               }
            }

            if (!(var3 <= var4)) {
               this.setShieldHp(var3 - var4);
               ServerBallisticsEngine.playSound(this.level(), this.getX(), this.getY(), this.getZ(), "block.amethyst_block.hit", 0.95F, 1.65F);
               return super.hurt(var1, var2 * 0.15F);
            }

            this.setShieldHp(0.0F);
            this.setAnim(6, 16);
            ServerLevel var7 = (ServerLevel)this.level();
            var7.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.1, this.getZ(), 40, 0.8, 0.8, 0.8, 0.22);
            ServerBallisticsEngine.playSound(var7, this.getX(), this.getY(), this.getZ(), "block.glass.break", 1.1F, 0.75F);
            var2 = (var4 - var3) * 0.5F + 1.0F;
         }

         if (!(var2 < 4.0F) && this.getAnimState() == 0) {
            this.setAnim(6, 8);
         }
      }

      return super.hurt(var1, var2);
   }

   public static AttributeSupplier makeAttrs(double var0, double var2, double var4, double var6) {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, var0)
         .add(Attributes.MOVEMENT_SPEED, var2)
         .add(Attributes.ATTACK_DAMAGE, var4)
         .add(Attributes.FOLLOW_RANGE, var6)
         .build();
   }
}
