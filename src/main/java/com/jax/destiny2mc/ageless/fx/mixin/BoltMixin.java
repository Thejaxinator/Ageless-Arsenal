package com.jax.destiny2mc.ageless.fx.mixin;

import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.frontier.FrontierProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=FrontierProjectile.class,remap=false)
abstract class BoltMixin implements ProjectileLook {
    @Shadow private float radius;
    @Unique private static final EntityDataAccessor<Integer> AGELESS_FX_PROFILE =
        SynchedEntityData.defineId(FrontierProjectile.class,EntityDataSerializers.INT);
    @Unique private FrontierProjectile ageless$self(){return (FrontierProjectile)(Object)this;}
    @Inject(method="defineSynchedData",at=@At("RETURN"))
    private void ageless$define(SynchedEntityData.Builder builder,CallbackInfo ci){
        builder.define(AGELESS_FX_PROFILE,0);
    }
    public EffectProfile ageless$profile(){
        FrontierProjectile p=ageless$self();return EffectProfile.fromTag(p.getEntityData().get(AGELESS_FX_PROFILE),p.element());
    }
    public void ageless$sync(EffectProfile profile){ageless$self().getEntityData().set(AGELESS_FX_PROFILE,profile.ordinal()+1);}
    public boolean ageless$stuck(){return ageless$self().attached();}
    @Inject(method="launch",at=@At("RETURN"))
    private static void ageless$factionBolt(ServerLevel level,LivingEntity owner,Vec3 start,Vec3 dir,float speed,float damage,
            int el,int mode,float radius,CallbackInfoReturnable<FrontierProjectile> ci){
        ((ProjectileLook)ci.getReturnValue()).ageless$sync(VoxelEffects.faction(owner,el));
    }
    @Inject(method="detonate",at=@At("HEAD"))
    private void ageless$impact(Vec3 point,CallbackInfo ci){
        if(ageless$self().level() instanceof ServerLevel l)
            VoxelEffects.emit(l,VoxelCue.Kind.BURST,ageless$profile(),point,point,Math.max(.6f,radius*.65f));
    }
    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    private void ageless$save(CompoundTag n,CallbackInfo ci){
        n.putString("AgelessVisualProfile",ageless$profile().name());
    }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    private void ageless$load(CompoundTag n,CallbackInfo ci){
        ageless$sync(EffectProfile.named(n.getString("AgelessVisualProfile"),ageless$self().element()));
    }
}
