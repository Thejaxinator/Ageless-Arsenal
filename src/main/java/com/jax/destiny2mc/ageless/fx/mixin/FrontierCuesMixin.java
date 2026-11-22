package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.frontier.FrontierMob;
import com.jax.destiny2mc.combat.CombatSupport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.jax.destiny2mc.ageless.fx.VoxelCue.Kind.*;

@Mixin(value=FrontierMob.class,remap=false)
abstract class FrontierCuesMixin {
    @Redirect(method="tickMove",at=@At(value="INVOKE",target="Lcom/jax/destiny2mc/frontier/FrontierMob;ring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DI)V"))
    private void ageless$windup(ServerLevel l,Vec3 center,double radius,int element){
        FrontierMob self=(FrontierMob)(Object)this;
        VoxelEffects.emit(l,RING,VoxelEffects.faction(self,element),center,center,(float)radius);
    }
    @Inject(method="line",at=@At("HEAD"),cancellable=true)
    private static void ageless$line(ServerLevel l,Vec3 a,Vec3 b,int element,CallbackInfo ci){
        VoxelEffects.emit(l,TRACE,EffectProfile.fromElement(element),a,b,1);ci.cancel();
    }
    @Inject(method="blast",at=@At("HEAD"))
    private void ageless$blast(ServerLevel l,Vec3 center,float radius,float damage,double push,CallbackInfo ci){
        FrontierMob self=(FrontierMob)(Object)this;
        VoxelEffects.emit(l,BURST,VoxelEffects.faction(self,self.unit.element()),center,center,radius*.6f);
    }
    @Redirect(method="blink",at=@At(value="INVOKE",target="Lcom/jax/destiny2mc/combat/CombatSupport;safeEntityTeleport(Lnet/minecraft/world/entity/Entity;DDD)V"))
    private void ageless$blink(Entity e,double x,double y,double z){
        Vec3 old=e.position();CombatSupport.safeEntityTeleport(e,x,y,z);
        if(e.position().distanceToSqr(old)>.25&&e.level() instanceof ServerLevel l){
            EffectProfile style=VoxelEffects.faction(e,3);
            VoxelEffects.emit(l,BLINK_OUT,style,old,old,1);VoxelEffects.emit(l,BLINK_IN,style,e.position(),e.position(),1);
        }
    }
}
