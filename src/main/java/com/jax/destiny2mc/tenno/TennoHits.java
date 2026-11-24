package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import static com.jax.destiny2mc.tenno.TennoGunSpec.Family;

/** Collisions, field queries and chains use server positions. Each ray ends at its first solid
 * block. A shotgun accumulates per-victim pellet damage before entering vanilla armor/shield logic. */
final class TennoHits {
    static final ResourceKey<DamageType> ROUND=ResourceKey.create(Registries.DAMAGE_TYPE,TennoGunRegistration.id("tenno_round"));
    record Contact(Vec3 point, LivingEntity entity, boolean block) {}
    record Result(int weakHits,boolean killed) {}
    static final Result MISS=new Result(0,false);
    static boolean valid(ServerPlayer owner, LivingEntity victim) {
        if(victim==owner || !victim.isAlive() || victim.isSpectator() || owner.isAlliedTo(victim))return false;
        if(victim instanceof TamableAnimal pet && pet.isOwnedBy(owner))return false;
        return !(victim instanceof Player other) || !other.isCreative()&&owner.canHarmPlayer(other);
    }
    static boolean clear(ServerPlayer owner,Vec3 from,Vec3 to) {
        return owner.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner)).getType()==HitResult.Type.MISS;
    }
    static Contact contact(ServerPlayer owner,Vec3 from,Vec3 end,double padding,Set<UUID> ignored) {
        var level=owner.serverLevel();
        var block=level.clip(new ClipContext(from,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner));
        Vec3 stop=block.getLocation(),point=stop;
        boolean blocked=block.getType()!=HitResult.Type.MISS;
        double nearest=from.distanceToSqr(stop)+1e-8;
        LivingEntity hit=null;
        for(var target:level.getEntitiesOfClass(LivingEntity.class,new AABB(from,stop).inflate(padding+.05),
            e->valid(owner,e)&&!ignored.contains(e.getUUID()))) {
            var box=target.getBoundingBox().inflate(padding);
            var clipped=box.contains(from)?Optional.of(from):box.clip(from,stop);
            if(clipped.isEmpty())continue;
            double dist=clipped.get().distanceToSqr(from);
            if(dist>=nearest)continue;
            nearest=dist;point=clipped.get();hit=target;
        }
        return new Contact(point,hit,hit==null&&blocked);
    }
    static List<LivingEntity> near(ServerPlayer owner,Vec3 center,double radius,Set<UUID> ignored) {
        var bounds=new AABB(center,center).inflate(radius);
        var targets=owner.serverLevel().getEntitiesOfClass(LivingEntity.class,bounds,e->valid(owner,e)&&
            !ignored.contains(e.getUUID())&&e.getBoundingBox().distanceToSqr(center)<=radius*radius&&clear(owner,center,e.getBoundingBox().getCenter()));
        targets.sort(Comparator.comparingDouble(e->e.distanceToSqr(center)));
        return targets;
    }
    static void effect(LivingEntity target, Holder<MobEffect> effect,int ticks,int strength,ServerPlayer owner){
        target.addEffect(new MobEffectInstance(effect,ticks,strength,false,true),owner);
    }
    static boolean hurt(ServerPlayer owner,LivingEntity target,float hp) {
        var holder=owner.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ROUND);
        return target.hurt(new DamageSource(holder,owner,owner),hp);
    }
    static void trace(ServerPlayer p,Vec3 from,Vec3 to,EffectProfile color) {
        VoxelEffects.emit(p.serverLevel(),VoxelCue.Kind.TRACE,color,from,to,1);
    }
    static void flash(ServerPlayer p,Vec3 at,EffectProfile color,float radius,VoxelCue.Kind kind){
        VoxelEffects.emit(p.serverLevel(),kind,color,at,at,radius);
    }
    static Result trigger(ServerPlayer owner,TennoGunStats stats,boolean aim,int hand){
        TennoGunSpec spec=stats.spec();
        Vec3 eye=owner.getEyePosition(),dir=owner.getLookAngle().normalize();
        Vec3 right=new Vec3(-Math.cos(Math.toRadians(owner.getYRot())),0,-Math.sin(Math.toRadians(owner.getYRot())));
        Vec3 up=right.cross(dir).normalize();
        double sideOff=hand==0?(aim?0.0:0.105):hand*(aim?0.10:0.16);
        double upOff=aim?-0.05:-0.11;
        Vec3 muzzle=eye.add(right.scale(sideOff)).add(up.scale(upOff)).add(dir.scale(.45));
        if(!clear(owner,eye,muzzle))muzzle=eye;
        if(spec.family==Family.FLAME){
            Vec3 stop=owner.level().clip(new ClipContext(eye,eye.add(dir.scale(spec.range())),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner)).getLocation();
            trace(owner,muzzle,stop,EffectProfile.SOLAR);
            int streams=stats.projectileCount(1,owner.getRandom());
            for(var target:near(owner,eye,spec.range(),Set.of())){
                Vec3 delta=target.getBoundingBox().getCenter().subtract(eye);double z=delta.dot(dir);
                if(z<0||delta.subtract(dir.scale(z)).length()>.55+z*.17)continue;
                if(stats.hit(owner,target,spec.damage()*streams))target.igniteForSeconds(2);
            }
            return MISS;
        }
        int pellets=switch(spec.family){case BRAKK->10;case CEDO->6;case BUBONICO->7;default->1;};
        double spread=pellets>1?(aim?.042:.076):aim?0:(spec.dual()||spec.family==Family.GRAKATA?.039:.015);
        var impact=new LinkedHashMap<LivingEntity,Float>(); int weakHits=0;
        LivingEntity preciseTarget=null;
        int projected=stats.projectileCount(pellets,owner.getRandom());
        for(int i=0;i<projected;i++){
            var r=owner.getRandom();
            Vec3 ray=dir.add((i==0&&pellets>1?0:r.nextGaussian()*spread),
                    (i==0&&pellets>1?0:r.nextGaussian()*spread),(i==0&&pellets>1?0:r.nextGaussian()*spread)).normalize();
            Contact c=contact(owner,eye,eye.add(ray.scale(spec.range())),.03,Set.of());
            EffectProfile color=switch(spec.family){case MICROWAVE->EffectProfile.ARC;case BUBONICO->EffectProfile.STRAND;
                    case AKSONDOL->EffectProfile.STASIS; default->EffectProfile.KINETIC;};
            if(i<3)trace(owner,muzzle,c.point(),color);
            var target=c.entity();if(target==null)continue;
            boolean head=c.point().y>=target.getY()+target.getBbHeight()*.74;
            if(head){weakHits++;preciseTarget=target;}
            float multiplier=head?(aim&&(spec.family==Family.AX||spec.family==Family.VESPER)?2:spec.family==Family.SOMA?1.9f:1.4f):1;
            if(spec.family==Family.CEDO) multiplier*=1+.18f*Math.min(5,target.getActiveEffects().size()+(target.isOnFire()?1:0));
            if(spec.family==Family.BRAKK) multiplier*=(float)Math.clamp(1-(eye.distanceTo(c.point())-7)/35,.4,1);
            impact.merge(target,spec.damage()*multiplier/pellets,Float::sum);
        }
        for(var entry:impact.entrySet()){
            var target=entry.getKey();if(!stats.hit(owner,target,entry.getValue()))continue;
            if(spec.family==Family.MICROWAVE){
                effect(target,MobEffects.WEAKNESS,50,0,owner);
                if(spec==TennoGunSpec.KUVA_NUKOR){
                    var seen=new HashSet<UUID>();seen.add(target.getUUID());
                    var previous=target;
                    for(int hop=1;hop<=2;hop++){
                        Vec3 source=previous.getBoundingBox().getCenter();
                        var neighbors=near(owner,source,5,seen);
                        if(neighbors.isEmpty())break;
                        previous=neighbors.getFirst();seen.add(previous.getUUID());
                        trace(owner,source,previous.getBoundingBox().getCenter(),EffectProfile.ARC);
                        if(stats.hit(owner,previous,entry.getValue()*(hop==1?.65f:.42f)))effect(previous,MobEffects.WEAKNESS,50,0,owner);
                    }
                }
            }else if(spec.family==Family.AKSONDOL)effect(target,MobEffects.MOVEMENT_SLOWDOWN,40,0,owner);
            else if(spec.family==Family.BUBONICO)effect(target,MobEffects.POISON,45,0,owner);
        }
        return new Result(weakHits,preciseTarget!=null&&!preciseTarget.isAlive());
    }
}
