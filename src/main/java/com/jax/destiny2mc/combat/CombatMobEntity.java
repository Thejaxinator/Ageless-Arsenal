package com.jax.destiny2mc.combat;

import com.jax.destiny2mc.entity.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import static com.jax.destiny2mc.combat.CombatMoves.*;

/** Server-authoritative moves. Clients consume FallenMobEntity's synchronized state/timer. */
public class CombatMobEntity extends FallenMobEntity {
    private int action, actionAge, cooldown=35, special=80, cycle;
    private Vec3 mark=Vec3.ZERO, home;
    private float chargeDamage;
    private boolean ghostSpawned;
    private static final ResourceKey<DamageType> THRALL_FOLLOW_THROUGH = ResourceKey.create(
        Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("destiny2mc","thrall_follow_through"));
    private LivingEntity swipeVictim;
    private DamageSource openingClaw;

    public CombatMobEntity(EntityType type, Level level, int kind, float shield) {
        super(type,level,kind,shield);
        if(floats()) setNoGravity(true);
    }
    // All concrete entities retain their original class/registry names and attribute suppliers.
    private boolean floats() { return unitKind==25||unitKind==27||unitKind==30||unitKind==31||unitKind==34||unitKind==36; }
    private boolean knight() { return unitKind==24||unitKind==28; }
    private boolean wyvern() { return unitKind==37||unitKind==39; }
    private boolean captain() { return unitKind==3||unitKind==4; }
    private boolean ranged() { return unitKind!=21&&unitKind!=22&&unitKind!=30; }
    private void start(int move) {
        swipeVictim=null;
        openingClaw=null;
        action=move;actionAge=0;setAnim(move,duration(move));
        if(getTarget()!=null) mark=getTarget().position();
        if(move==DARKNESS) chargeDamage=0;
        getNavigation().stop();
    }
    private void finish(int delay) { swipeVictim=null;openingClaw=null;action=0;actionAge=0;cooldown=delay;setAnim(0,0); }
    private void stop() { getNavigation().stop();setDeltaMovement(0,floats()?0:getDeltaMovement().y,0); }

    @Override protected void tickFallenCombat(ServerLevel level) {
        if(home==null) home=position();
        if(cooldown>0)cooldown--;
        if(special>0)special--;
        LivingEntity target=getTarget();
        setAggressive(target!=null&&target.isAlive());
        if(action!=0) {
            // A committed landing/heal/cloud finishes even if its original target dies.
            tickAction(level,target);return;
        }
        if(target==null||!target.isAlive()||target.isRemoved()) { if(floats()) idleFlight(level);return; }
        getLookControl().setLookAt(target,32,32);
        face(target);
        double distance=distanceTo(target);
        boolean sight=hasLineOfSight(target);
        if(special<=0) {
            if(knight()&&getHealth()<getMaxHealth()*.72f) {start(SHIELD);special=240;return;}
            if(unitKind==33&&getHealth()<getMaxHealth()*.68f) {start(STASIS);special=260;return;}
            if((captain()||unitKind==35||unitKind==45)&&sight&&distance<22) {start(BLINK);special=captain()?150:130;return;}
            if(unitKind==25&&sight&&distance<20) {start(CLOUD);special=180;return;}
            if(wyvern()&&onGround()&&sight&&distance>3&&distance<15) {start(CROUCH);special=180;return;}
            if(unitKind==31&&sight&&distance<30) {
                start(switch(cycle++%3){case 0->SLAM;case 1->DARKNESS;default->SWORD;});special=160;return;
            }
            if(unitKind==40) { CombatSupport.onAtheonOracleCycle(level,this);special=240; }
        }
        if(unitKind==45&&cooldown<=0&&sight&&distance<12){start(TENTACLE);return;}
        if((unitKind==22||unitKind==30)&&distance<2.6&&sight) {start(EXPLODE);return;}
        if(cooldown<=0&&distance<(unitKind==26?3.8:2.6)&&sight&&unitKind!=27&&unitKind!=36) {
            start(unitKind==21 ? THRALL_SWIPE : MELEE);return;
        }
        if(ranged()&&cooldown<=0&&sight&&distance<30) { start(FIRE);return; }
        moveToTarget(level,target,distance,sight);
    }
    private void face(LivingEntity target) {
        float yaw=(float)(Math.atan2(target.getZ()-getZ(),target.getX()-getX())*180/Math.PI)-90;
        setYRot(Mth.rotateIfNecessary(getYRot(),yaw,12));yBodyRot=getYRot();
    }
    private void idleFlight(ServerLevel level) {
        if(unitKind==31) fly(level,home.add(0,3,0),.07);
        else setDeltaMovement(getDeltaMovement().scale(.7));
    }
    private void fly(ServerLevel level,Vec3 destination,double speed) {
        getNavigation().stop();fallDistance=0;
        Vec3 d=destination.subtract(position());
        Vec3 velocity=new Vec3(Mth.clamp(d.x*speed,-.24,.24),Mth.clamp(d.y*.075,-.19,.19),Mth.clamp(d.z*speed,-.24,.24));
        if(!level.noCollision(this,getBoundingBox().move(velocity))) velocity=new Vec3(0,level.noCollision(this,getBoundingBox().move(0,.12,0))?.12:-.08,0);
        setDeltaMovement(velocity);
    }
    private void moveToTarget(ServerLevel level,LivingEntity target,double distance,boolean sight) {
        if(floats()) {
            if(unitKind==27) {stop();return;}
            Vec3 toward=position().subtract(target.position()).normalize();
            double range=unitKind==30?0:unitKind==31?8:7;
            Vec3 destination=target.position().add(toward.x*range,unitKind==31?3.4:1.1,toward.z*range);
            if(unitKind==31&&destination.distanceTo(home)>26) destination=home.add(0,3.4,0);
            fly(level,destination,unitKind==30?.09:.035);return;
        }
        if(!ranged()||distance>11||!sight) {
            if(tickCount%10==0) getNavigation().moveTo(target,unitKind==21?1.28:1.0);
        } else if(distance<4&&unitKind!=26) {
            getNavigation().stop();Vec3 away=position().subtract(target.position()).multiply(1,0,1).normalize().scale(.075);
            if(level.noCollision(this,getBoundingBox().move(away)))setDeltaMovement(away.x,getDeltaMovement().y,away.z);
        } else if(tickCount%24==0) {
            // Short alternating strafe while keeping the upper body aimed.
            Vec3 f=target.position().subtract(position()).multiply(1,0,1).normalize();double sign=((tickCount/72+getId())%2==0)?1:-1;
            getNavigation().moveTo(getX()+f.z*2*sign,getY(),getZ()-f.x*2*sign,.65);
        }
    }
    private void tickAction(ServerLevel level,LivingEntity target) {
        actionAge++;
        if(target!=null&&target.isAlive()&&action!=DIVE&&action!=SWORD&&action!=BLINK) {
            getLookControl().setLookAt(target,30,30);face(target);
        }
        boolean hit=hitFrame(action,actionAge);
        switch(action) {
            case FIRE -> {
                if(target!=null&&target.isAlive()) {
                    moveToTarget(level,target,distanceTo(target),hasLineOfSight(target));
                    if(hit&&hasLineOfSight(target)&&(actionAge==12||unitKind==23||unitKind==25||unitKind==26||wyvern()||unitKind==31)) shoot(level,target,0);
                } else {finish(20);return;}
            }
            case THRALL_SWIPE -> {
                stop();
                if(target!=null && target.isAlive()) {
                    if(actionAge==THRALL_LUNGE_TICK){
                        Vec3 d=target.position().subtract(position()).multiply(1,0,1).normalize().scale(.34);
                        setDeltaMovement(d.x,.12,d.z);
                    }
                    if(hit && distanceTo(target)<3 && hasLineOfSight(target))strikeClaw(level,target);
                }
            }
            case MELEE -> {
                stop();
                if(hit&&target!=null&&target.isAlive()&&distanceTo(target)<(unitKind==26?4:3)&&hasLineOfSight(target)) {
                    target.hurt(damageSources().mobAttack(this),unitKind==26?6:4);
                    target.knockback(.45,getX()-target.getX(),getZ()-target.getZ());
                }
            }
            case BLINK -> {
                stop();if(actionAge<10&&actionAge%2==0) particles(level,ParticleTypes.ELECTRIC_SPARK,12,.4,.8);
                if(hit) safeBlink(level,target);
                if(actionAge>10&&actionAge%2==0) particles(level,ParticleTypes.PORTAL,12,.45,.9);
            }
            case SHIELD,STASIS -> {
                stop();
                if(actionAge>10&&actionAge<duration(action)-10&&actionAge%10==0) {heal(action==SHIELD?2:1);particles(level,ParticleTypes.HAPPY_VILLAGER,4,.35,.6);}
                if(actionAge%4==0)particles(level,action==SHIELD?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME,4,.55,.7);
            }
            case CLOUD -> {
                stop();particles(level,ParticleTypes.SCULK_SOUL,3,.4,.7);
                if(hit) WizardClouds.add(level,this,mark);
                if(actionAge%4==0)ring(level,mark,3,ParticleTypes.SMOKE,12);
            }
            case CROUCH -> {stop();if(actionAge%4==0)ring(level,mark,2.6,ParticleTypes.ELECTRIC_SPARK,18);}
            case LEAP -> {
                getNavigation().stop();fallDistance=0;
                if(actionAge==1) {Vec3 d=mark.subtract(position()).multiply(1,0,1).normalize().scale(.16);setDeltaMovement(d.x,.92,d.z);}
            }
            case DIVE -> {
                getNavigation().stop();fallDistance=0;
                if(actionAge==1) {Vec3 d=mark.add(0,.2,0).subtract(position()).normalize().scale(1.05);setDeltaMovement(d.x,Math.min(-.3,d.y),d.z);}
                if(actionAge%2==0)particles(level,ParticleTypes.ELECTRIC_SPARK,5,.4,.3);
                if(diveImpact(actionAge,onGround(),horizontalCollision,target!=null&&distanceTo(target)<1.9)) {
                    blast(level,position(),3.6,unitKind==39?8:6,.85);start(LAND);return;
                }
            }
            case LAND -> {stop();fallDistance=0;}
            case SLAM -> {
                stop();fallDistance=0;
                if(actionAge%3==0)ring(level,mark,4,ParticleTypes.SOUL_FIRE_FLAME,24);
                if(hit) {blast(level,mark,4,9,.8);particles(level,ParticleTypes.EXPLOSION,2,.3,.2);}
            }
            case DARKNESS -> {
                stop();fallDistance=0;
                if(actionAge%4==0) {particles(level,ParticleTypes.SCULK_SOUL,8,1.2,1.3);ring(level,position(),7,ParticleTypes.SOUL_FIRE_FLAME,32);}
                if(hit) blast(level,position(),14,12,.6);
            }
            case SWORD -> {
                fallDistance=0;getNavigation().stop();
                if(actionAge<20) {stop();if(actionAge%3==0)ring(level,mark,3,ParticleTypes.END_ROD,16);}
                else if(actionAge<30) {
                    Vec3 d=mark.add(0,1,0).subtract(position()).normalize().scale(.6);
                    if(level.noCollision(this,getBoundingBox().move(d)))setDeltaMovement(d);else stop();
                } else stop();
                if(hit) blast(level,position().add(getLookAngle().scale(1.5)),4.5,8,.7);
            }
            case TENTACLE -> {
                stop();
                if(actionAge%3==0)ring(level,mark,1.8,ParticleTypes.END_ROD,12);
                if(hit) {
                    Vec3 forward=mark.subtract(position()).multiply(1,0,1).normalize();
                    Vec3 side=new Vec3(-forward.z,0,forward.x).scale(actionAge==20?-.6:.6);
                    Vec3 tip=position().add(0,2.8,0).add(side).add(forward.scale(.8));
                    Vec3 to=mark.add(0,.9,0).subtract(tip);
                    for(int i=0;i<=18;i++){Vec3 p=tip.add(to.scale(i/18.0));level.sendParticles(ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,1,0,0,0,0);}
                    if(mark.distanceTo(position())<13)blast(level,mark,1.8,4.5f,.4);
                }
            }
            case STAGGER -> {stop();if(actionAge%5==0)particles(level,ParticleTypes.END_ROD,8,.7,1);}
            case EXPLODE -> {
                stop();particles(level,ParticleTypes.SOUL_FIRE_FLAME,5,.4,.7);
                if(hit) {blast(level,position(),unitKind==30?2.5:3.5,unitKind==30?4:6,.5);discard();return;}
            }
        }
        if(actionAge>=duration(action)) {
            if(action==CROUCH) {start(LEAP);return;}
            if(action==LEAP) {start(DIVE);return;}
            // Timeout never deals dive damage in mid-air.
            if(action==DIVE) {start(LAND);return;}
            int delay=action==THRALL_SWIPE?THRALL_SWIPE_COOLDOWN:unitKind==33?36:unitKind==26?12:22;finish(delay);
        }
    }
    private void strikeClaw(ServerLevel level,LivingEntity target) {
        DamageSource source=damageSources().mobAttack(this);
        if(actionAge==THRALL_LEFT_HIT_TICK) {
            // A first hit landing during another attack's protection cannot earn a bypass.
            boolean fresh=target.invulnerableTime<=10;
            if(target.hurt(source,THRALL_CLAW_DAMAGE) && fresh && target.getLastDamageSource()==source) {
                swipeVictim=target;
                openingClaw=source;
            }
            return;
        }
        // Revalidate range/LOS in the caller at each contact. Use a distinct damage type only
        // while this very opening hit is still the victim's most recent accepted damage.
        if(target==swipeVictim && target.invulnerableTime>10 && target.getLastDamageSource()==openingClaw) {
            var holder=level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(THRALL_FOLLOW_THROUGH);
            source=new DamageSource(holder,this);
        }
        if(target.hurt(source,THRALL_CLAW_DAMAGE))
            target.knockback(.22,getX()-target.getX(),getZ()-target.getZ());
    }
    private void shoot(ServerLevel level,LivingEntity target,int side) {
        Projectile p;
        if(captain())p=unitKind==4?new ScorchRocketEntity(level,this):new ShrapnelPelletEntity(level,this);
        else if(unitKind==33)p=new WireRifleBoltEntity(level,this);
        else if(unitKind==23||unitKind==25||unitKind==31)p=new HiveSoulfireBoltEntity(level,this);
        else if(unitKind==32||unitKind==34||unitKind==40||unitKind==41)p=new VexSlapBoltEntity(level,this);
        else p=new VoidAxiomBoltEntity(level,this);
        Vec3 forward=target.getEyePosition().subtract(getEyePosition()).normalize();
        Vec3 right=new Vec3(-forward.z,0,forward.x);
        double offset=wyvern()?(actionAge%2==0?-.7:.7):unitKind==26?0:.23;
        Vec3 muzzle=position().add(0,unitKind==45?2.8:getBbHeight()*(unitKind==26?.84:.72),0).add(forward.scale(.55)).add(right.scale(offset));
        // Do not create a projectile on the far side of an adjacent wall.
        if(!level.noCollision(p,p.getBoundingBox().move(muzzle.subtract(p.position()))))muzzle=getEyePosition();
        p.setPos(muzzle.x,muzzle.y,muzzle.z);
        Vec3 aim=target.getEyePosition().add(0,-.2,0).subtract(muzzle);
        p.shoot(aim.x,aim.y,aim.z,unitKind==33?2.3f:1.6f,unitKind==33?.5f:2);
        level.addFreshEntity(p);
    }
    private void safeBlink(ServerLevel level,LivingEntity target) {
        Vec3 before=position();particles(level,ParticleTypes.PORTAL,30,.5,.9);
        for(int i=0;i<8;i++) {
            double angle=getRandom().nextDouble()*Math.PI*2,range=3+getRandom().nextDouble()*3;
            double x=getX()+Math.cos(angle)*range,z=getZ()+Math.sin(angle)*range;
            for(int dy=2;dy>=-3;dy--) {
                BlockPos floor=BlockPos.containing(x,getY()+dy-1,z);
                if(!level.hasChunkAt(floor)||!level.getBlockState(floor).isSolidRender(level,floor))continue;
                double y=floor.getY()+1;
                if(!level.noCollision(this,getBoundingBox().move(x-getX(),y-getY(),z-getZ())))continue;
                CombatSupport.safeEntityTeleport(this,x,y,z);
                if(position().distanceToSqr(before)>.25) {particles(level,ParticleTypes.ELECTRIC_SPARK,35,.5,1);return;}
            }
        }
    }
    private void blast(ServerLevel level,Vec3 center,double radius,float damage,double knockback) {
        ring(level,center,radius,ParticleTypes.SOUL_FIRE_FLAME,40);
        for(LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius))) {
            if(victim==this||!victim.isAlive()||victim.isAlliedTo(this)||!(victim instanceof Player)&&victim!=getTarget())continue;
            if(victim.position().distanceToSqr(center)>radius*radius||!hasLineOfSight(victim))continue;
            if(victim instanceof Player player&&(player.isCreative()||player.isSpectator()))continue;
            victim.hurt(damageSources().mobAttack(this),damage);
            victim.knockback(knockback,center.x-victim.getX(),center.z-victim.getZ());
        }
    }
    private void particles(ServerLevel l,net.minecraft.core.particles.SimpleParticleType p,int count,double xz,double y) {
        l.sendParticles(p,getX(),getY()+getBbHeight()*.55,getZ(),count,xz,y,xz,.025);
    }
    private static void ring(ServerLevel l,Vec3 center,double radius,net.minecraft.core.particles.SimpleParticleType p,int count) {
        for(int i=0;i<count;i++){double a=i*Math.PI*2/count;l.sendParticles(p,center.x+Math.cos(a)*radius,center.y+.12,center.z+Math.sin(a)*radius,1,0,0,0,0);}
    }
    @Override public boolean hurt(DamageSource source,float amount) {
        if(!level().isClientSide) {
            if(action==SHIELD&&actionAge>=10&&actionAge<=60&&source.getEntity()!=null) {
                Vec3 toward=source.getEntity().position().subtract(position()).multiply(1,0,1).normalize();
                if(toward.dot(getLookAngle().multiply(1,0,1).normalize())>.2) amount*=.12f;
            }
            if(action==STASIS&&actionAge>8&&actionAge<47)amount*=.18f;
            if(unitKind==36&&source.getEntity()!=null) {
                Vec3 d=source.getEntity().position().subtract(position());
                double angle=Math.atan2(d.x,d.z)-tickCount*.045;
                if(Math.abs(Math.sin(angle*1.5))<.43)amount*=.25f;
            }
            if(unitKind==31&&action==DARKNESS) {
                // Sustained damage interrupts the charged Darkness attack; no unavoidable wipe.
                chargeDamage+=amount;
                if(chargeDamage>=28){start(STAGGER);special=120;}
                amount*=1.5f;
            }
        }
        boolean result=super.hurt(source,amount);
        // Shield-break feedback in the old base may replace its animation; restore active timeline.
        if(result&&action!=0&&isAlive())setAnim(action,Math.max(1,duration(action)-actionAge));
        if(!level().isClientSide&&!isAlive()&&unitKind==28&&!ghostSpawned&&level() instanceof ServerLevel server){ghostSpawned=true;CombatSupport.spawnLucentHiveGhost(server,this);}
        return result;
    }
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source) {
        return (floats()||wyvern())?false:super.causeFallDamage(distance,multiplier,source);
    }
}
