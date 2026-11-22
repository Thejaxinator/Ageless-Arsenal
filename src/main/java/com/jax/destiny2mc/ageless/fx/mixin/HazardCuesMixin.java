package com.jax.destiny2mc.ageless.fx.mixin;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.frontier.FrontierHazard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

@Mixin(value=FrontierHazard.class,remap=false)
abstract class HazardCuesMixin {
    @Shadow private float radius;
    @Shadow private int element;
    @Shadow private UUID owner;
    @Inject(method="tick",at=@At("HEAD"))
    private void ageless$field(CallbackInfo ci){
        FrontierHazard h=(FrontierHazard)(Object)this;
        if((h.tickCount%8)==0 && h.level() instanceof ServerLevel l && owner!=null){
            Vec3 c=h.position();VoxelEffects.emit(l,VoxelCue.Kind.RING,VoxelEffects.faction(l.getEntity(owner),element),c,c,radius);
        }
    }
}
