package com.jax.destiny2mc.ageless.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import static com.jax.destiny2mc.ageless.fx.VoxelCue.Kind;
import com.jax.destiny2mc.frontier.FrontierMob;

/** Server-side only. Visuals describe accepted actions and never raycast for damage. */
public final class VoxelEffects {
    private VoxelEffects(){}
    public static void trace(ServerLevel level,Vec3 from,Vec3 to,String profile){
        emit(level,Kind.TRACE,EffectProfile.named(profile,0),from,to,1);
    }
    public static void burst(ServerLevel level,Vec3 at,String profile){
        emit(level,Kind.BURST,EffectProfile.named(profile,0),at,at,1);
    }
    public static EffectProfile faction(Entity owner,int element){
        if(owner instanceof FrontierMob mob) return switch(mob.unit.faction()){
            case "scorn" -> EffectProfile.DARK_ETHER; case "dread" -> EffectProfile.RESONANCE;
            case "taken" -> EffectProfile.TAKEN; case "hive" -> EffectProfile.SOULFIRE;
            default -> EffectProfile.fromElement(element);
        };
        return EffectProfile.fromElement(element);
    }
    public static void emit(ServerLevel level,Kind kind,EffectProfile profile,Vec3 from,Vec3 to,float radius){
        if (!Double.isFinite(from.lengthSqr()) || !Double.isFinite(to.lengthSqr()) || !Float.isFinite(radius)) return;
        Vec3 ray=to.subtract(from);
        if(ray.lengthSqr()>512*512) {ray=ray.normalize().scale(512); to=from.add(ray);}
        double length2=ray.lengthSqr();
        VoxelCue cue=new VoxelCue(level.dimension().location(),kind,profile,from,to,Math.clamp(radius,.1f,16f),level.random.nextInt());
        for(ServerPlayer player:level.players()){
            // Consider the nearest point along the trace. A spectator near a distant impact can see it.
            double t=length2<1e-8?0:Math.clamp(player.getEyePosition().subtract(from).dot(ray)/length2,0,1);
            if(player.getEyePosition().distanceToSqr(from.add(ray.scale(t)))<96*96)PacketDistributor.sendToPlayer(player,cue);
        }
    }
}
