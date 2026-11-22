package com.jax.destiny2mc.binder;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.*;
import static com.jax.destiny2mc.binder.AbilityCombat.*;
import static com.jax.destiny2mc.binder.ActiveAbilities.Action;
import static com.jax.destiny2mc.ageless.fx.VoxelCue.Kind;

/** Flight and field simulation runs exclusively on the logical server. Clients receive transient
 * geometry, never damage decisions. The weak world key has no reverse strong reference from its
 * records. A logout, death, dimension transition or unloaded chunk retires outstanding casts. */
@EventBusSubscriber(modid="destiny2mc")
public final class AbilityWorld {
    private interface Task { boolean tick(ServerPlayer owner,Session world); UUID owner(); }
    private static final class Session {
        final List<Task> tasks=new ArrayList<>();
        // New tasks during resolution are deferred until next tick, so a chain cannot recursively
        // acquire more time or travel just because its owner happens to be processed early.
        final List<Task> pending=new ArrayList<>();
        void add(Task task){pending.add(task);}
    }
    private static final Map<ServerLevel,Session> WORLDS=new WeakHashMap<>();
    private static Session session(ServerPlayer p){return WORLDS.computeIfAbsent(p.serverLevel(),k->new Session());}
    static void throwShot(ServerPlayer p,Action a,Vec3 from,Vec3 aim,boolean grenade){
        Shot shot=new Shot(p.getUUID(),a,from,aim.scale(grenade?.95:1.24).add(0,grenade?.18:.01,0),grenade);
        if(a==Action.ARCANE_NEEDLE){
            var victim=near(p,from,16).stream().filter(e->center(e).subtract(from).normalize().dot(aim)>.82).findFirst();
            victim.ifPresent(e->shot.seek=e.getUUID());
        }
        session(p).add(shot);
        burst(p,a.element(),from,.3f);
    }
    static void field(ServerPlayer p,Action a,Vec3 at,Vec3 facing,int ticks){
        session(p).add(new Field(p.getUUID(),a,at,facing,ticks));
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event){
        if(!(event.getLevel() instanceof ServerLevel level))return;
        Session work=WORLDS.get(level);if(work==null)return;
        work.tasks.addAll(work.pending);work.pending.clear();
        work.tasks.removeIf(task->{
            Entity found=level.getEntity(task.owner());
            return !(found instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||!task.tick(p,work);
        });
        if(work.tasks.isEmpty()&&work.pending.isEmpty())WORLDS.remove(level);
    }

    private static final class Shot implements Task {
        final UUID caster; final Action action; final boolean grenade;
        Vec3 pos,velocity; int age; UUID seek;
        boolean hatchling; final Set<UUID> struck=new HashSet<>();
        Shot(UUID caster,Action action,Vec3 pos,Vec3 velocity,boolean grenade){
            this.caster=caster;this.action=action;this.pos=pos;this.velocity=velocity;this.grenade=grenade;
        }
        public UUID owner(){return caster;}
        public boolean tick(ServerPlayer p,Session work){
            ServerLevel l=p.serverLevel();
            if(!l.hasChunkAt(BlockPos.containing(pos))||++age>60)return false;
            if(seek!=null && l.getEntity(seek) instanceof LivingEntity dest && target(p,dest)){
                Vec3 desired=center(dest).subtract(pos).normalize().scale(hatchling?.8:1.25);
                velocity=velocity.lerp(desired,hatchling?.48:.32);
            }
            Vec3 end=pos.add(velocity);
            var wall=l.clip(new ClipContext(pos,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            Vec3 stop=wall.getLocation();LivingEntity victim=null;double closest=stop.distanceToSqr(pos);
            for(var e:l.getEntitiesOfClass(LivingEntity.class,new AABB(pos,stop).inflate(.5),e->
                    target(p,e)&&!struck.contains(e.getUUID()))){
                AABB box=e.getBoundingBox().inflate(.13);
                var crossing=box.contains(pos)?Optional.of(pos):box.clip(pos,stop);
                if(crossing.isPresent()){
                    double d=pos.distanceToSqr(crossing.get());if(d<=closest){closest=d;stop=crossing.get();victim=e;}
                }
            }
            trail(p,stop);
            boolean contact=victim!=null||wall.getType()!=HitResult.Type.MISS;
            if(contact||age>=40 && grenade){
                // Move a face impact slightly towards open space for subsequent line-of-sight tests.
                Vec3 impact=stop.subtract(velocity.normalize().scale(.055));
                if(hatchling){if(victim!=null)hit(p,victim,"strand",3);burst(p,"strand",impact,.55f);return false;}
                if(grenade){grenadeImpact(p,work,this,impact);return false;}
                if(victim!=null && (action==Action.SHIELD_THROW||action==Action.THREADED_SPIKE)){
                    boolean shield=action==Action.SHIELD_THROW;hit(p,victim,action.element(),shield?6:5);
                    if(shield)effect(p,MobEffects.ABSORPTION,80,0);else effect(victim,MobEffects.WEAKNESS,80,0);
                    burst(p,action.element(),impact,.65f);struck.add(victim.getUUID());
                    var next=near(p,impact,6).stream().filter(e->!struck.contains(e.getUUID())).findFirst();
                    if(next.isPresent()&&struck.size()<(shield?3:4)){
                        pos=impact;seek=next.get().getUUID();velocity=center(next.get()).subtract(pos).normalize().scale(1.28);return true;
                    }
                    cue(p,action.element(),Kind.TRACE,impact,p.getEyePosition(),1);return false;
                }
                meleeImpact(p,action,impact,victim);return false;
            }
            pos=end;
            if(grenade)velocity=velocity.scale(.985).add(0,-.045,0);
            if(action==Action.THROWING_HAMMER)velocity=velocity.add(0,-.025,0);
            return true;
        }
        private void trail(ServerPlayer p,Vec3 end){
            // One short voxel ribbon per flight step gives nearby clients a continuous projectile.
            cue(p,action.element(),Kind.TRACE,pos,end,1);
            if(grenade && age%3==1)burst(p,action.element(),end,.28f);
        }
    }
    private static void grenadeImpact(ServerPlayer p,Session world,Shot shot,Vec3 at){
        Action a=shot.action;burst(p,a.element(),at,1.55f);
        switch(a){
            case SOLAR_GRENADE,PULSE_GRENADE,DUSKFIELD_GRENADE,VORTEX_GRENADE,FREEZING_SINGULARITY,HAILFIRE_SPIKE ->
                field(p,a,at,Vec3.ZERO,a==Action.HAILFIRE_SPIKE?20:a==Action.FREEZING_SINGULARITY?50:64);
            case HEALING_GRENADE -> {
                for(var ally:p.serverLevel().players())
                    if(friendly(p,ally)&&center(ally).distanceToSqr(at)<25&&com.jax.destiny2mc.frontier.CombatRules.visible(p,at,ally)){
                        ally.heal(5);effect(ally,MobEffects.REGENERATION,65,0);
                    }
                cue(p,"solar",Kind.RING,at,at,4.4f);
            }
            case FLASHBANG,SUPPRESSOR_GRENADE,SHACKLE_GRENADE -> {
                for(var e:near(p,at,a==Action.FLASHBANG?5:4)){
                    hit(p,e,a.element(),a==Action.SHACKLE_GRENADE?3:5);
                    switch(a){
                        case FLASHBANG -> {effect(e,MobEffects.BLINDNESS,65,0);effect(e,MobEffects.MOVEMENT_SLOWDOWN,50,2);
                            if(e instanceof Mob m){m.setTarget(null);m.getNavigation().stop();}}
                        case SUPPRESSOR_GRENADE -> {
                            effect(e,MobEffects.WEAKNESS,110,1);
                            // Weakness alone does not affect most ranged mod attacks. Suppression
                            // blocks the outgoing damage for a short, server-timed window.
                            e.getPersistentData().putLong("aa_binder_suppressed_until",p.level().getGameTime()+40);
                            if(e instanceof Mob m)m.setTarget(null);
                        }
                        default -> {effect(e,MobEffects.LEVITATION,34,0);effect(e,MobEffects.MOVEMENT_SLOWDOWN,55,3);}
                    }
                }
                cue(p,a.element(),Kind.RING,at,at,4);
            }
            case COLDSNAP_GRENADE -> {
                Vec3 origin=at;Set<UUID> frozen=new HashSet<>();
                for(int i=0;i<3;i++){
                    var next=near(p,origin,i==0?4.5:4).stream().filter(e->!frozen.contains(e.getUUID())).findFirst();
                    if(next.isEmpty())break;
                    var e=next.get();cue(p,"stasis",Kind.TRACE,origin,center(e),1);hit(p,e,"stasis",4);chill(e,68,4);
                    frozen.add(e.getUUID());origin=center(e);
                }
            }
            case THREADLING_GRENADE -> {
                var targets=near(p,at,8);Vec3 start=at.add(0,.28,0);
                for(int i=0;i<3;i++){
                    double angle=2*Math.PI*i/3;
                    Shot hatch=new Shot(p.getUUID(),a,start,new Vec3(.42*Math.cos(angle),.25,.42*Math.sin(angle)),false);
                    hatch.hatchling=true;if(!targets.isEmpty())hatch.seek=targets.get(i%targets.size()).getUUID();
                    world.add(hatch);
                }
            }
            default -> throw new IllegalStateException(a.name());
        }
    }
    private static void meleeImpact(ServerPlayer p,Action a,Vec3 pos,LivingEntity e){
        burst(p,a.element(),pos,.95f);
        switch(a){
            case POCKET_SINGULARITY -> {
                for(var other:near(p,pos,2.8)){hit(p,other,"void",6);move(other,center(other).subtract(pos).normalize().scale(.85).add(0,.26,0));}
            }
            case PENUMBRAL_BLAST -> {for(var other:near(p,pos,2.25)){hit(p,other,"stasis",5);chill(other,64,5);}}
            case THROWING_HAMMER -> {
                if(e!=null){hit(p,e,"solar",8);e.igniteForSeconds(2);}
                field(p,a,pos,Vec3.ZERO,15);
            }
            case ARCANE_NEEDLE -> {
                if(e!=null){
                    hit(p,e,"strand",5);Field mark=new Field(p.getUUID(),a,pos,Vec3.ZERO,26);
                    mark.attached=e.getUUID();session(p).add(mark);
                }
            }
            default -> {}
        }
    }

    private static final class Field implements Task {
        final UUID caster;final Action action;final Vec3 at,face;final int duration;
        int age;UUID attached;boolean shattered;
        Field(UUID caster,Action action,Vec3 at,Vec3 face,int duration){
            this.caster=caster;this.action=action;this.at=at;this.face=face;this.duration=duration;
        }
        public UUID owner(){return caster;}
        public boolean tick(ServerPlayer p,Session world){
            if(!p.serverLevel().hasChunkAt(BlockPos.containing(at)))return false;
            ++age;
            switch(action){
                case HEALING_RIFT,EMPOWERING_RIFT -> rift(p);
                case TOWERING_BARRICADE,RALLY_BARRICADE -> barricade(p);
                case SHIVER_STRIKE -> {
                    if(!shattered){
                        for(var e:near(p,p.getEyePosition(),2.8)){
                            if(center(e).subtract(p.getEyePosition()).normalize().dot(face)<.15)continue;
                            boolean cold=e.getTicksFrozen()>0||e.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
                            hit(p,e,"stasis",cold?11:6);chill(e,35,2);move(e,face.scale(.75).add(0,.25,0));shattered=true;
                            burst(p,"stasis",center(e),1);break;
                        }
                    }
                }
                case THUNDERCLAP,PRISM_CLEAVE -> {
                    if(age==duration)cone(p,action,p.getEyePosition(),face,action==Action.PRISM_CLEAVE);
                }
                case THROWING_HAMMER -> {
                    if(age==6 && at.distanceToSqr(p.getEyePosition())<20*20 &&
                        com.jax.destiny2mc.frontier.CombatRules.visible(p,at,p)){
                        cue(p,"solar",Kind.TRACE,at,p.getEyePosition(),1);p.heal(3);
                        return false;
                    }
                }
                case ARCANE_NEEDLE -> {
                    if(!(p.serverLevel().getEntity(attached) instanceof LivingEntity victim)||!target(p,victim))return false;
                    if(age==12||age==24){hit(p,victim,"strand",3);burst(p,"strand",center(victim),.8f);}
                }
                default -> grenadeField(p);
            }
            return age<duration;
        }
        boolean inRift(ServerPlayer p){Vec3 d=p.position().subtract(at);return Math.abs(d.y)<2 && d.horizontalDistanceSqr()<=10.24;}
        void rift(ServerPlayer p){
            if(age%10!=1)return;cue(p,action.element(),Kind.RING,at,at,3.2f);
            for(var ally:p.serverLevel().players())if(friendly(p,ally)&&inRift(ally)&&
                com.jax.destiny2mc.frontier.CombatRules.visible(p,at,ally)&&action==Action.HEALING_RIFT)ally.heal(1.5f);
        }
        double height(){return action==Action.TOWERING_BARRICADE?2.85:1.35;}
        boolean guard(){return action==Action.TOWERING_BARRICADE||action==Action.RALLY_BARRICADE;}
        boolean crosses(Vec3 source,Vec3 dest){
            double s=source.subtract(at).dot(face),d=dest.subtract(at).dot(face);
            if(s<=0||d>=0)return false; // Protect the caster-facing side. Allies can fire out through it.
            Vec3 spot=source.lerp(dest,s/(s-d)).subtract(at);Vec3 side=new Vec3(-face.z,0,face.x);
            return Math.abs(spot.dot(side))<2.6&&spot.y>=-.15&&spot.y<=height();
        }
        void barricade(ServerPlayer p){
            Vec3 side=new Vec3(-face.z,0,face.x),left=at.add(side.scale(-2.5)),right=at.add(side.scale(2.5));
            if(age%4==1){
                for(int i=0;i<=2;i++){double y=height()*i/2;cue(p,action.element(),Kind.TRACE,left.add(0,y,0),right.add(0,y,0),1);}
                cue(p,action.element(),Kind.TRACE,left,left.add(0,height(),0),1);cue(p,action.element(),Kind.TRACE,right,right.add(0,height(),0),1);
            }
            // Intercept before impact so explosive bolts approaching the front cannot detonate
            // against a player protected by the wall. Damage interception also covers hitscan.
            for(var bolt:p.serverLevel().getEntitiesOfClass(Projectile.class,new AABB(at,at).inflate(4.5),b->b.getOwner()!=p)){
                if(bolt.getOwner() instanceof ServerPlayer shooter && friendly(p,shooter))continue;
                if(crosses(bolt.position(),bolt.position().add(bolt.getDeltaMovement()))||
                    crosses(new Vec3(bolt.xo,bolt.yo,bolt.zo),bolt.position())){
                    burst(p,action.element(),bolt.position(),.5f);bolt.discard();
                }
            }
            if(age%10==1){
                // A barricade stays permeable to movement. Crossing its narrow plane slows
                // the opponent instead of placing blocks that could trap a player or a mount.
                for(var enemy:near(p,at.add(0,height()*.5,0),3.5)){
                    Vec3 delta=center(enemy).subtract(at);
                    if(Math.abs(delta.dot(face))<.48 && Math.abs(delta.dot(side))<2.5 && delta.y>0 && delta.y<height())
                        effect(enemy,MobEffects.MOVEMENT_SLOWDOWN,25,2);
                }
            }
            if(action==Action.RALLY_BARRICADE && age%4==1)
                for(var ally:p.serverLevel().players()){
                    Vec3 d=ally.position().subtract(at);double behind=d.dot(face);
                    if(friendly(p,ally)&&ally.isCrouching()&&behind<=0&&behind>=-3.2&&Math.abs(d.y)<2&&Math.abs(d.dot(side))<2.5)
                        accelerateReload(ally,false);
                }
        }
        void grenadeField(ServerPlayer p){
            if(action==Action.HAILFIRE_SPIKE){
                if(age!=1 && age!=20)return;
                String el=age==1?"stasis":"solar";burst(p,el,at,2.8f);
                for(var e:near(p,at,3.6)){hit(p,e,el,age==1?3:7);if(age==1)chill(e,55,3);else e.igniteForSeconds(3);}
                return;
            }
            if(action==Action.FREEZING_SINGULARITY && age==duration){
                burst(p,"stasis",at,3);
                for(var e:near(p,at,3.8)){hit(p,e,"stasis",6);chill(e,55,4);}
                return;
            }
            int period=action==Action.VORTEX_GRENADE||action==Action.FREEZING_SINGULARITY?13:16;
            if((age-1)%period!=0)return;
            double radius=action==Action.DUSKFIELD_GRENADE?2.5+age/28.0:3.75;
            String el=action==Action.FREEZING_SINGULARITY?"void":action.element();
            cue(p,el,Kind.RING,at,at,(float)radius);
            var enemies=near(p,at,radius);
            if(action==Action.PULSE_GRENADE && !enemies.isEmpty()){
                chain(p,enemies.get(0),at,"arc",4,2);return;
            }
            for(var e:enemies)switch(action){
                case SOLAR_GRENADE -> {hit(p,e,el,3.5f);e.igniteForSeconds(2);}
                case VORTEX_GRENADE,FREEZING_SINGULARITY -> {
                    hit(p,e,el,2);move(e,at.subtract(e.position()).normalize().scale(.52).add(0,.06,0));
                }
                case DUSKFIELD_GRENADE -> {hit(p,e,el,2);chill(e,55,Math.min(4,1+age/16));}
                default -> {}
            }
        }
    }
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent event){
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        if(event.getSource().getEntity() instanceof LivingEntity attacker &&
            attacker.getPersistentData().getLong("aa_binder_suppressed_until")>level.getGameTime()){
            event.setCanceled(true);return;
        }
        Session session=WORLDS.get(level);if(session==null)return;
        boolean empower=false;
        for(Task t:session.tasks){
            if(!(t instanceof Field f) || !(level.getEntity(f.caster) instanceof ServerPlayer owner)||!owner.isAlive())continue;
            if(f.action==Action.EMPOWERING_RIFT && event.getSource().getEntity() instanceof ServerPlayer shooter &&
               friendly(owner,shooter)&&f.inRift(shooter)&&com.jax.destiny2mc.frontier.CombatRules.visible(owner,f.at,shooter))empower=true;
            if(f.guard() && event.getEntity() instanceof ServerPlayer victim && friendly(owner,victim)){
                Entity attacker=event.getSource().getEntity();
                if(attacker==null || attacker==victim || attacker==owner)continue;
                // The physical projectile may already be inside the player. Use its previous
                // position for the crossing check, then fall back to the attacker's eye origin.
                Vec3 end=victim.getEyePosition();Entity direct=event.getSource().getDirectEntity();
                if((direct instanceof Projectile bolt && f.crosses(new Vec3(bolt.xo,bolt.yo,bolt.zo),end))||f.crosses(attacker.getEyePosition(),end)){
                    event.setCanceled(true);return;
                }
            }
        }
        if(empower)event.setAmount(event.getAmount()*1.25f);
    }
}
