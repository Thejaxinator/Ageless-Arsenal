package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.ageless.WeaponProgram;
import com.jax.destiny2mc.ageless.WeaponPrograms;
import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.frontier.CombatRules;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.core.Holder;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.*;
import static com.jax.destiny2mc.binder.ActiveAbilities.Action;
import static com.jax.destiny2mc.ageless.fx.VoxelCue.Kind;

/** Shared targeting and resolution. Bursts select visible targets from the impact rather than
 * through walls. Fields are short-lived server objects and never modify terrain or force chunks. */
final class AbilityCombat {
    private AbilityCombat(){}
    static Vec3 center(LivingEntity e){return e.getBoundingBox().getCenter();}
    static boolean target(ServerPlayer p,LivingEntity e){
        return CombatRules.canDamage(p,e) && (!(e instanceof Player) || p.serverLevel().getServer().isPvpAllowed());
    }
    static boolean friendly(ServerPlayer p,ServerPlayer e){
        return e.isAlive() && !e.isSpectator() && (e==p || p.isAlliedTo(e) || p.getTeam()==null && e.getTeam()==null);
    }
    static List<LivingEntity> near(ServerPlayer p,Vec3 pos,double radius){
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(pos,pos).inflate(radius),e->
            target(p,e) && center(e).distanceToSqr(pos)<=radius*radius && CombatRules.visible(p,pos,e))
            .stream().sorted(Comparator.comparingDouble(e->center(e).distanceToSqr(pos))).limit(12).toList();
    }
    static void hit(ServerPlayer p,LivingEntity e,String element,float damage){
        var data=e.getPersistentData();
        data.putString("aa_binder_last_element",element);
        data.putUUID("aa_binder_last_owner",p.getUUID());
        data.putLong("aa_binder_last_tick",p.level().getGameTime());
        e.hurt(p.damageSources().playerAttack(p),damage);
    }
    static void effect(LivingEntity e,Holder<MobEffect> type,int ticks,int amp){
        e.addEffect(new MobEffectInstance(type,ticks,amp,false,true,true));
    }
    static void chill(LivingEntity e,int ticks,int amp){
        effect(e,MobEffects.MOVEMENT_SLOWDOWN,ticks,amp);
        e.setTicksFrozen(Math.min(230,e.getTicksFrozen()+ticks));
    }
    static void move(LivingEntity e,Vec3 push){
        e.push(push.x,push.y,push.z);
        e.hurtMarked=true;
    }
    static EffectProfile profile(String element){return EffectProfile.named(element.equals("prismatic")?"resonance":element,0);}
    static void cue(ServerPlayer p,String el,Kind kind,Vec3 from,Vec3 to,float size){
        VoxelEffects.emit(p.serverLevel(),kind,profile(el),from,to,size);
    }
    static void burst(ServerPlayer p,String el,Vec3 pos,float radius){
        cue(p,el,Kind.BURST,pos,pos,radius);
    }
    static Vec3 horizontal(ServerPlayer p){
        double yaw=Math.toRadians(p.getYRot());
        return new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
    }
    static boolean cast(ServerPlayer p,Action a){
        Vec3 start=p.getEyePosition(),aim=p.getLookAngle().normalize(),foot=p.position().add(0,.12,0);
        switch(a){
            case SOLAR_GRENADE,HEALING_GRENADE,PULSE_GRENADE,FLASHBANG,VORTEX_GRENADE,
                 SUPPRESSOR_GRENADE,DUSKFIELD_GRENADE,COLDSNAP_GRENADE,SHACKLE_GRENADE,THREADLING_GRENADE,
                 FREEZING_SINGULARITY,HAILFIRE_SPIKE -> AbilityWorld.throwShot(p,a,start,aim,true);
            case THROWING_HAMMER,SHIELD_THROW,POCKET_SINGULARITY,PENUMBRAL_BLAST,THREADED_SPIKE,ARCANE_NEEDLE ->
                AbilityWorld.throwShot(p,a,start,aim,false);
            case INCINERATOR_SNAP,PRISM_CLEAVE -> {
                cone(p,a,start,aim,false);
                if(a==Action.PRISM_CLEAVE) AbilityWorld.field(p,a,start,aim,12);
            }
            case THUNDERCLAP -> {
                cue(p,"arc",Kind.RING,foot,foot,1.35f);
                AbilityWorld.field(p,a,start,aim,10);
            }
            case SHIVER_STRIKE -> {
                Vec3 dash=aim.multiply(1,.35,1).normalize();
                move(p,dash.scale(1.1));AbilityWorld.field(p,a,start,dash,6);
            }
            case CHAIN_LIGHTNING,STORM_SNARE -> {
                var primary=near(p,start,5).stream().filter(e->center(e).subtract(start).normalize().dot(aim)>.54).findFirst();
                if(primary.isEmpty())return false;
                LivingEntity e=primary.get();
                if(a==Action.STORM_SNARE){effect(e,MobEffects.LEVITATION,30,0);cue(p,"strand",Kind.TRACE,start,center(e),1);}
                chain(p,e,start,"arc",a==Action.CHAIN_LIGHTNING?7:5,3);
            }
            case MARKSMANS_DODGE,GAMBLERS_DODGE -> {
                Vec3 velocity=p.getDeltaMovement().multiply(1,0,1);
                Vec3 dir=velocity.lengthSqr()>.008?velocity.normalize():horizontal(p);
                move(p,dir.scale(.96).add(0,.13,0));cue(p,a.element(),Kind.BLINK_OUT,foot,foot,.8f);
                if(a==Action.MARKSMANS_DODGE)accelerateReload(p,true);
                else if(!near(p,start,7).isEmpty())BinderState.refreshMelee(p);
            }
            case HEALING_RIFT,EMPOWERING_RIFT -> AbilityWorld.field(p,a,foot,horizontal(p),160);
            case TOWERING_BARRICADE,RALLY_BARRICADE -> {
                Vec3 forward=horizontal(p),end=foot.add(forward.scale(1.8));
                if(p.level().clip(new ClipContext(foot.add(0,.5,0),end.add(0,.5,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)return false;
                AbilityWorld.field(p,a,end,forward,a==Action.TOWERING_BARRICADE?160:140);
            }
        }
        return true;
    }
    static void cone(ServerPlayer p,Action a,Vec3 at,Vec3 aim,boolean after){
        boolean snap=a==Action.INCINERATOR_SNAP;
        String el=snap||after?"solar":a==Action.THUNDERCLAP?"arc":"stasis";
        double range=a==Action.THUNDERCLAP?7:4.6;
        Vec3 side=aim.cross(new Vec3(0,1,0)).normalize();
        for(int i=-2;i<=2;i++){
            Vec3 end=at.add(aim.add(side.scale(.22*i)).normalize().scale(range));
            var ray=p.level().clip(new ClipContext(at,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            cue(p,el,Kind.TRACE,at,ray.getLocation(),1);
        }
        for(var e:near(p,at,range)){
            if(center(e).subtract(at).normalize().dot(aim)<.65)continue;
            hit(p,e,el,a==Action.THUNDERCLAP?10:after?6:5);
            if(el.equals("solar"))e.igniteForSeconds(3);
            else if(el.equals("stasis"))chill(e,55,2);
            else move(e,aim.scale(.9).add(0,.22,0));
        }
    }
    static void chain(ServerPlayer p,LivingEntity first,Vec3 from,String el,float damage,int count){
        Set<UUID> struck=new HashSet<>(); LivingEntity e=first;
        for(int i=0;i<count&&e!=null;i++){
            Vec3 end=center(e);cue(p,el,Kind.TRACE,from,end,1);
            hit(p,e,el,i==0?damage:damage*.7f);struck.add(e.getUUID());
            from=end;e=near(p,end,5).stream().filter(v->!struck.contains(v.getUUID())).findFirst().orElse(null);
        }
    }
    static void accelerateReload(ServerPlayer p,boolean start){
        var stack=p.getMainHandItem();
        if(!(stack.getItem() instanceof DestinyGunItem gun))return;
        var prog=WeaponPrograms.server(gun.spec);
        if(prog!=null && prog.feed()==WeaponProgram.Feed.CAPACITOR)return;
        long tick=p.level().getGameTime(),end=GunDataHelper.getLong(stack,"d2_rel_end");
        if(start && end<=tick && GunDataHelper.getMag(stack,gun.spec)<WeaponPerkRegistry.getEffectiveMagSize(stack,gun.spec))
            GunDataHelper.setLong(stack,"d2_rel_end",Math.max(1,tick));
        else if(end>tick)GunDataHelper.setLong(stack,"d2_rel_end",start?tick:Math.max(tick+1,end-3));
    }
}
