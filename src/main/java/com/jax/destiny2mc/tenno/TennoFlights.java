package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.weapon.GunDataHelper;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Short-lived, level-local swept projectiles. No chunk tickets or saved-world effects.
 * Blades retain owner and originating weapon identity, including when several players recall at once. */
@EventBusSubscriber(modid="destiny2mc")
public final class TennoFlights {
    enum Type { GLAIVE, KNIFE, SPORE, VIRAL, FROST, POOL }
    private static final class Flight {
        final UUID owner, weapon; final Type type; final TennoGunSpec spec; final TennoGunStats stats;
        final Set<UUID> visited=new HashSet<>();
        Vec3 at, velocity; int age, duration; double radius, travel;
        boolean stuck, returning;
        Flight(ServerPlayer p,UUID weapon,TennoGunStats stats,Type type,Vec3 at,Vec3 velocity) {
            this.owner=p.getUUID();this.weapon=weapon;this.stats=stats;this.spec=stats==null?null:stats.spec();this.type=type;
            this.at=at;this.velocity=velocity;this.duration=type==Type.KNIFE?360:80;
        }
    }
    private static final Map<ServerLevel,List<Flight>> ACTIVE=new WeakHashMap<>();
    private static List<Flight> list(ServerPlayer p) {return ACTIVE.computeIfAbsent(p.serverLevel(),k->new ArrayList<>());}
    private static UUID identity(ItemStack stack) {
        var n=GunDataHelper.readTag(stack);
        if(!n.hasUUID("wf_identity")) { n.putUUID("wf_identity",UUID.randomUUID());GunDataHelper.writeTag(stack,n); }
        return n.getUUID("wf_identity");
    }
    static void launch(ServerPlayer p,ItemStack stack,TennoGunStats stats,Type type) {
        double speed=switch(type){case KNIFE->2.7;case GLAIVE->1.8;case SPORE->1.65;default->2.0;};
        var d=p.getLookAngle().normalize();
        UUID id=identity(stack);
        int count=stats.projectileCount(1,p.getRandom());
        for(int i=0;i<count;i++){
            // Extra projectiles follow independent flight and collision paths. The original aims
            // exactly along the reticle, preserving the original projectile's collision contract.
            Vec3 direction=i==0?d:d.add(p.getRandom().nextGaussian()*.012,
                p.getRandom().nextGaussian()*.012,p.getRandom().nextGaussian()*.012).normalize();
            list(p).add(new Flight(p,id,stats,type,p.getEyePosition(),direction.scale(speed)));
        }
    }
    static void recall(ServerPlayer p,ItemStack stack) {
        UUID id=identity(stack);
        for(var f:list(p))if(f.type==Type.KNIFE&&f.owner.equals(p.getUUID())&&id.equals(f.weapon)){
            f.returning=true; f.visited.clear(); f.age=0; f.duration=65;
        }
    }
    static void field(ServerPlayer p,Vec3 center,double radius,int duration,Type type) {
        var f=new Flight(p,null,null,type,center,Vec3.ZERO);f.radius=radius;f.duration=duration;
        list(p).add(f);
    }
    private static void fieldTick(ServerPlayer p,Flight f) {
        boolean frost=f.type==Type.FROST;
        var color=frost?EffectProfile.STASIS:EffectProfile.STRAND;
        TennoHits.flash(p,f.at,color,(float)f.radius,VoxelCue.Kind.CLOUD);
        for(var e:TennoHits.near(p,f.at,f.radius,Set.of())){
            if(frost)TennoHits.effect(e,MobEffects.MOVEMENT_SLOWDOWN,30,1,p);
            else if(f.stats.hit(p,e,1.3f))TennoHits.effect(e,MobEffects.POISON,45,0,p);
        }
    }
    private static boolean step(ServerPlayer owner,Flight f) {
        if(++f.age>f.duration || !owner.serverLevel().hasChunkAt(BlockPos.containing(f.at)))return false;
        if(f.type==Type.FROST||f.type==Type.POOL) {
            if(f.age%10==1)fieldTick(owner,f);return true;
        }
        if(f.returning) {
            var dest=owner.getEyePosition();var delta=dest.subtract(f.at);double distance=delta.length();
            Vec3 end=distance<=3.5?dest:f.at.add(delta.scale(3.5/distance));
            var hit=TennoHits.contact(owner,f.at,end,.18,f.visited);
            TennoHits.trace(owner,f.at,hit.block()?hit.point():end,EffectProfile.RESONANCE);
            while(hit.entity()!=null){
                f.visited.add(hit.entity().getUUID());f.stats.hit(owner,hit.entity(),f.spec.damage()*2.1f);
                hit=TennoHits.contact(owner,f.at,end,.18,f.visited);
            }
            if(hit.block())return false;
            f.at=end;return distance>3.5;
        }
        if(f.stuck) {
            if(f.age%14==0)TennoHits.flash(owner,f.at,EffectProfile.RESONANCE,.14f,VoxelCue.Kind.BURST);
            return true;
        }
        if(f.travel>=f.spec.range()&&f.type!=Type.KNIFE)return false;
        Vec3 end=f.at.add(f.velocity);
        var hit=TennoHits.contact(owner,f.at,end,f.type==Type.GLAIVE?.22:.08,f.visited);
        EffectProfile color=switch(f.type){case KNIFE,GLAIVE->EffectProfile.RESONANCE;case VIRAL->EffectProfile.DARK_ETHER;default->EffectProfile.STRAND;};
        TennoHits.trace(owner,f.at,hit.point(),color);
        f.travel+=f.at.distanceTo(hit.point());f.at=hit.point();
        if(hit.entity()==null&&!hit.block()&&f.travel<f.spec.range()){
            if(f.type==Type.SPORE||f.type==Type.VIRAL)f.velocity=f.velocity.add(0,f.type==Type.SPORE?-.045:-.03,0);
            return true;
        }
        if(f.type==Type.KNIFE){
            if(hit.entity()!=null)f.stats.hit(owner,hit.entity(),f.spec.damage());
            f.at=f.at.subtract(f.velocity.normalize().scale(.10));f.stuck=true;return true;
        }
        if(f.type==Type.GLAIVE){
            if(hit.entity()!=null){
                var victim=hit.entity();f.visited.add(victim.getUUID());
                f.stats.hit(owner,victim,3);
                switch(f.visited.size()%4){
                    case 1->TennoHits.effect(victim,MobEffects.MOVEMENT_SLOWDOWN,100,0,owner);
                    case 2->victim.igniteForSeconds(4);
                    case 3->TennoHits.effect(victim,MobEffects.POISON,80,0,owner);
                    default->TennoHits.effect(victim,MobEffects.WEAKNESS,100,0,owner);
                }
            }
            TennoHits.flash(owner,f.at,color,.85f,VoxelCue.Kind.RING);
            if(f.visited.size()>=5)return false;
            Vec3 bounce=f.at.subtract(f.velocity.normalize().scale(.12));
            var next=TennoHits.near(owner,bounce,8,f.visited);if(next.isEmpty())return false;
            f.at=bounce;f.velocity=next.getFirst().getBoundingBox().getCenter().subtract(bounce).normalize().scale(2.2);
            return true;
        }
        Vec3 center=f.at.subtract(f.velocity.normalize().scale(.12));
        boolean spore=f.type==Type.SPORE;double radius=spore?3.1:(f.spec==TennoGunSpec.CODA_BUBONICO?4.2:3.5);
        TennoHits.flash(owner,center,color,(float)radius,VoxelCue.Kind.BURST);
        for(var e:TennoHits.near(owner,center,radius,Set.of())){
            float scale=(float)Math.clamp(1-e.getBoundingBox().distanceToSqr(center)/(2*radius*radius),.5,1);
            if(f.stats.hit(owner,e,f.spec.damage()*scale))
                TennoHits.effect(e,spore?MobEffects.POISON:MobEffects.WEAKNESS,spore?60:f.spec==TennoGunSpec.CODA_BUBONICO?130:70,0,owner);
        }
        if(spore){f.radius=radius;f.duration=80;f.age=0;f.at=center;f.stuck=true;return true;}
        return false;
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event){
        if(!(event.getLevel() instanceof ServerLevel level))return;
        var flights=ACTIVE.get(level);if(flights==null)return;
        flights.removeIf(f->{
            if(!(level.getPlayerByUUID(f.owner) instanceof ServerPlayer owner)||!owner.isAlive())return true;
            if(f.type==Type.SPORE&&f.stuck){
                if(++f.age>f.duration)return true;
                if(f.age%10==1)fieldTick(owner,f);return false;
            }
            return !step(owner,f);
        });
        if(flights.isEmpty())ACTIVE.remove(level);
    }
    @SubscribeEvent public static void unload(LevelEvent.Unload e){
        if(e.getLevel() instanceof ServerLevel level)ACTIVE.remove(level);
    }
}
