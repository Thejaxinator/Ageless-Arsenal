package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.combat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.jax.destiny2mc.ageless.fx.VoxelCue.Kind.*;

@Mixin(value=CombatMobEntity.class,remap=false)
abstract class CombatCuesMixin {
    @Redirect(method="safeBlink",at=@At(value="INVOKE",target="Lcom/jax/destiny2mc/combat/CombatSupport;safeEntityTeleport(Lnet/minecraft/world/entity/Entity;DDD)V"))
    private void ageless$blink(Entity mob,double x,double y,double z){
        Vec3 old=mob.position();
        CombatSupport.safeEntityTeleport(mob,x,y,z);
        // Emit both flashes only if the combat system accepted a safe destination.
        if(mob.position().distanceToSqr(old)>.25 && mob.level() instanceof ServerLevel l){
            int kind=((CombatMobEntity)mob).unitKind;
            EffectProfile style=kind==3||kind==4?EffectProfile.ETHER:EffectProfile.ARC;
            VoxelEffects.emit(l,BLINK_OUT,style,old,old,1);
            VoxelEffects.emit(l,BLINK_IN,style,mob.position(),mob.position(),1);
        }
    }
    @Inject(method="blast",at=@At("HEAD"))
    private void ageless$blast(ServerLevel l,Vec3 point,double radius,float damage,double knockback,CallbackInfo ci){
        CombatMobEntity self=(CombatMobEntity)(Object)this;
        EffectProfile profile=self.unitKind<21?EffectProfile.ETHER:self.unitKind<=31?EffectProfile.SOULFIRE:EffectProfile.ARC;
        VoxelEffects.emit(l,BURST,profile,point,point,(float)radius*.55f);
    }
}
