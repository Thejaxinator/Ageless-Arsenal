package com.jax.destiny2mc.atlas;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import com.jax.destiny2mc.frontier.FrontierMob;
/** Bounded encounter objects: receivers, circuit terminals, eyes and collectible riches. */
public final class DungeonNode extends Monster {
 private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(DungeonNode.class,EntityDataSerializers.INT);
 public UUID owner;public int index;private int orphanTicks;
 public DungeonNode(EntityType<? extends DungeonNode> t,Level l){super(t,l);setNoGravity(true);setPersistenceRequired();xpReward=0;}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MODE,0);}
 public int mode(){return entityData.get(MODE);}public void mode(int mode){entityData.set(MODE,mode);}
 @Override public void aiStep(){super.aiStep();setDeltaMovement(0,0,0);if(level() instanceof ServerLevel l){if(owner!=null&&l.getEntity(owner) instanceof FrontierMob boss&&boss.isAlive()){orphanTicks=0;if(mode()==3)DungeonEncounters.collect(this,boss);}else if(++orphanTicks>400)discard();}}
 @Override public boolean hurt(DamageSource source,float amount){if(source.is(DamageTypes.GENERIC_KILL))return super.hurt(source,amount);if(!DungeonEncounters.canHit(this,source))return false;return super.hurt(source,amount);}
 @Override public void die(DamageSource source){DungeonEncounters.nodeBroken(this,source);super.die(source);}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(owner!=null)t.putUUID("AtlasOwner",owner);t.putInt("AtlasIndex",index);t.putInt("AtlasMode",mode());}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);if(t.hasUUID("AtlasOwner"))owner=t.getUUID("AtlasOwner");index=t.getInt("AtlasIndex");mode(t.getInt("AtlasMode"));}
}
