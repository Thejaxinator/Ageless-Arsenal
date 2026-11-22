package com.jax.destiny2mc.ageless;

import com.jax.destiny2mc.frontier.*;
import com.jax.destiny2mc.network.CombatFeedbackPayload;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** Server-owned trigger and delivery pipeline. The action packet describes input, not shots or hits.
 * The visible direction always comes from the server's player. Item clocks also drive remote animation. */
public final class ProgramBallistics {
    public static void action(ServerPlayer player,ItemStack stack,WeaponSpec spec,WeaponProgram program,byte action,byte ads){
        long now=player.level().getGameTime();
        if(action==4){clearCharge(stack);return;}
        if(action==1||action==2){clear(stack);if(program.feed()==WeaponProgram.Feed.MAGAZINE)FrontierBallistics.reload(player,stack,spec);return;}
        if(GunDataHelper.getLong(stack,"d2_rel_end")>now||GunDataHelper.getLong(stack,"frontier_next")>now)return;
        if(GunDataHelper.getMag(stack,spec)<program.cost()){clear(stack);if(program.feed()==WeaponProgram.Feed.MAGAZINE)FrontierBallistics.reload(player,stack,spec);return;}
        if(program.chargeTicks()>0){
            if(action==3){
                var tag=GunDataHelper.readTag(stack);
                long start=tag.getLong("frontier_charge");
                if(start==0||now-tag.getLong("frontier_heartbeat")>8||!player.getUUID().equals(tag.hasUUID("aa_operator")?tag.getUUID("aa_operator"):null)){
                    tag.putLong("frontier_charge",Math.max(1L,now));tag.putInt("frontier_duration",program.chargeTicks());
                    ServerBallisticsEngine.playSound(player.level(),player.getX(),player.getY(),player.getZ(),"block.respawn_anchor.charge",.42f,1.45f);
                }
                tag.putUUID("aa_operator",player.getUUID());tag.putLong("frontier_heartbeat",now);tag.putInt("frontier_ads",ads!=0?1:0);
                GunDataHelper.writeTag(stack,tag);return;
            }
            if(action!=0||program.trigger()!=WeaponProgram.Trigger.RELEASE)return;
            long start=GunDataHelper.getLong(stack,"frontier_charge");
            boolean ready=start>0&&now-start>=program.chargeTicks()&&now-GunDataHelper.getLong(stack,"frontier_heartbeat")<=8&&owned(stack,player);
            clearCharge(stack);if(!ready)return;
        }else if(action!=0)return;
        begin(player,stack,spec,program,ads);
    }
    private static boolean owned(ItemStack s,ServerPlayer p){var tag=GunDataHelper.readTag(s);return tag.hasUUID("aa_operator")&&tag.getUUID("aa_operator").equals(p.getUUID());}
    private static void clearCharge(ItemStack s){if(GunDataHelper.getLong(s,"frontier_charge")!=0)GunDataHelper.setLong(s,"frontier_charge",0);}
    private static void clear(ItemStack s){
        clearCharge(s);if(GunDataHelper.getInt(s,"aa_burst",0)>0)GunDataHelper.setInt(s,"aa_burst",0);
    }
    public static void tick(WeaponSpec w,ItemStack s,Level l,Entity e,boolean selected,WeaponProgram prog){
        long now=l.getGameTime(),start=GunDataHelper.getLong(s,"frontier_charge");
        if(prog.feed()==WeaponProgram.Feed.CAPACITOR)recharge(s,w,prog,now);
        int remaining=GunDataHelper.getInt(s,"aa_burst",0);
        if(start==0&&remaining==0)return;
        if(!(e instanceof ServerPlayer player)||!selected||player.getMainHandItem()!=s||!player.isAlive()||player.isSpectator()||!owned(s,player)||GunDataHelper.getLong(s,"d2_rel_end")>now){
            clear(s);return;
        }
        byte ads=(byte)GunDataHelper.getInt(s,"frontier_ads",0);
        if(start>0){
            if(prog.chargeTicks()==0||now-GunDataHelper.getLong(s,"frontier_heartbeat")>8){clearCharge(s);return;}
            if(prog.trigger()==WeaponProgram.Trigger.CHARGE&&now-start>=prog.chargeTicks()){
                clearCharge(s);begin(player,s,w,prog,ads);
            }
        }else if(remaining>0&&now>=GunDataHelper.getLong(s,"aa_burst_tick")){
            // A dropped, unequipped or unloaded weapon may retain its NBT. Never resume a stale salvo.
            if(now-GunDataHelper.getLong(s,"aa_burst_tick")>2||remaining>=prog.burst()||GunDataHelper.getMag(s,w)<prog.cost()){clear(s);return;}
            round(player,s,w,prog,ads,remaining==1);queue(player,s,remaining-1,now+prog.interval(),ads);
        }
    }
    /** Charge units reuse the magazine component, so the HUD and replicated item already agree.
     * Recharge accrues while carried, including holstered inventory. A stale timestamp after storage
     * gives only one unit per tick cycle rather than a backlog of fabricated bursts or ammo bricks. */
    private static void recharge(ItemStack s,WeaponSpec w,WeaponProgram prog,long now){
        int amount=GunDataHelper.getMag(s,w),capacity=WeaponPerkRegistry.getEffectiveMagSize(s,w);
        if(amount>=capacity)return;
        long next=GunDataHelper.getLong(s,"aa_recharge");
        if(next==0||next>now+prog.rechargeTicks())GunDataHelper.setLong(s,"aa_recharge",now+prog.rechargeTicks());
        else if(next<=now){
            GunDataHelper.setInt(s,"d2_mag",Math.min(capacity,amount+1));
            GunDataHelper.setLong(s,"aa_recharge",now+prog.rechargeTicks());
        }
    }
    private static void queue(ServerPlayer p,ItemStack s,int remaining,long deadline,byte ads){
        var t=GunDataHelper.readTag(s);t.putUUID("aa_operator",p.getUUID());t.putInt("aa_burst",remaining);
        t.putLong("aa_burst_tick",deadline);t.putInt("frontier_ads",ads!=0?1:0);GunDataHelper.writeTag(s,t);
    }
    private static void begin(ServerPlayer p,ItemStack s,WeaponSpec w,WeaponProgram prog,byte ads){
        long now=p.level().getGameTime();
        if(GunDataHelper.getMag(s,w)<prog.cost()||GunDataHelper.getLong(s,"frontier_next")>now)return;
        GunDataHelper.setLong(s,"frontier_next",now+prog.cooldown());
        round(p,s,w,prog,ads,prog.burst()==1);
        queue(p,s,prog.burst()-1,now+prog.interval(),ads);
    }
    private static Vec3 direction(Vec3 axis,Vec3 right,Vec3 up,double x,double y){return axis.add(right.scale(x)).add(up.scale(y)).normalize();}
    /** Ring uses one center pellet plus an evenly spaced rim. Pattern spread is independent of player
     * bloom, so ADS tightens the overall aim but can't collapse a volume of bolts into one trajectory. */
    public static Vec3 directionFor(WeaponProgram prog,Vec3 axis,int i,net.minecraft.util.RandomSource rng){
        if(prog.count()==1||prog.angle()==0)return axis;
        Vec3 right=axis.cross(Math.abs(axis.y)>.99?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();
        Vec3 up=right.cross(axis).normalize();
        double slope=Math.tan(Math.toRadians(prog.angle()));
        if(prog.pattern()==WeaponProgram.Pattern.FAN)return direction(axis,right,up,slope*(2.0*i/(prog.count()-1)-1),0);
        if(prog.pattern()==WeaponProgram.Pattern.RING){
            if(i==0)return axis;double a=2*Math.PI*(i-1)/(prog.count()-1);
            return direction(axis,right,up,slope*Math.cos(a),slope*Math.sin(a));
        }
        double a=rng.nextDouble()*Math.PI*2,d=Math.sqrt(rng.nextDouble())*slope;
        return direction(axis,right,up,d*Math.cos(a),d*Math.sin(a));
    }
    private static void round(ServerPlayer p,ItemStack s,WeaponSpec w,WeaponProgram prog,byte ads,boolean last){
        var level=p.serverLevel();long now=level.getGameTime();
        GunDataHelper.setInt(s,"d2_mag",GunDataHelper.getMag(s,w)-prog.cost());GunDataHelper.setLong(s,"d2_visual_shot",now);
        if(prog.feed()==WeaponProgram.Feed.CAPACITOR)GunDataHelper.setLong(s,"aa_recharge",now+prog.rechargeTicks());
        float recoil=WeaponPerkRegistry.getEffectiveSpread(s,w,ads,now);
        Vec3 axis=Vec3.directionFromRotation(p.getXRot()+(p.getRandom().nextFloat()-.5f)*recoil,p.getYRot()+(p.getRandom().nextFloat()-.5f)*recoil);
        float damage=w.baseDamage*prog.damageScale()*(last?prog.lastMult():1);
        for(int i=0;i<prog.count();i++){
            Vec3 dir=directionFor(prog,axis,i,p.getRandom()),start=p.getEyePosition();
            if(prog.delivery()==WeaponProgram.Delivery.PROJECTILE){
                var bolt=FrontierProjectile.launch(level,p,start,dir,prog.speed(),damage,w.element,prog.flight().mode,prog.radius()).weapon(s,w);
                WeaponVisuals.projectile(bolt,prog.fx());
            }else ray(p,s,w,prog,start,dir,damage);
        }
        ServerBallisticsEngine.playSound(level,p.getX(),p.getY(),p.getZ(),w.element==2?"item.trident.throw":"item.crossbow.shoot",.48f,w.archetype==8?.58f:w.archetype==0?1.42f:1.08f);
        p.getInventory().setChanged();p.containerMenu.broadcastChanges();
    }
    private record Impact(LivingEntity target,Vec3 pos,double distance){}
    private static void ray(ServerPlayer p,ItemStack s,WeaponSpec w,WeaponProgram prog,Vec3 from,Vec3 dir,float base){
        var l=p.serverLevel();long now=l.getGameTime();double range=WeaponPerkRegistry.getEffectiveRange(s,w,now);
        var wall=l.clip(new ClipContext(from,from.add(dir.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        Vec3 end=wall.getLocation();List<Impact> hits=new ArrayList<>();
        for(var v:l.getEntitiesOfClass(LivingEntity.class,new AABB(from,end).inflate(.3),e->CombatRules.canDamage(p,e))){
            v.getBoundingBox().inflate(.035).clip(from,end).ifPresent(q->hits.add(new Impact(v,q,from.distanceToSqr(q))));
        }
        hits.sort(Comparator.comparingDouble(Impact::distance));
        int taken=0;
        for(var h:hits){
            var v=h.target();boolean crit=h.pos().y>v.getY()+v.getBbHeight()*.78;
            float damage=base*(crit?w.critMult:1)*WeaponPerkRegistry.computeSwappableDamageMult(s,w,v,crit?1:0,now)*ProgramTraits.multiplier(prog.onHit(),p,s,v,crit,now);
            v.invulnerableTime=0;
            if(v.hurt(p.damageSources().playerAttack(p),damage)){
                WeaponPerkRegistry.onSwappablePostHit(l,p,s,w,v,h.pos(),crit?1:0,v.isAlive()?0:1,now);
                ProgramTraits.hit(prog,p,s,w,v,h.pos(),crit);
                PacketDistributor.sendToPlayer(p,new CombatFeedbackPayload(damage,(byte)(crit?1:0),(float)h.pos().x,(float)h.pos().y,(float)h.pos().z));
            }
            if(++taken>=prog.pierce()){end=h.pos();break;}
        }
        if(taken<prog.pierce()&&wall.getType()!=HitResult.Type.MISS)WeaponPerkRegistry.onSwappableWallHit(l,p,s,w,end);
        WeaponVisuals.trace(l,from,end,prog.fx(),w.element);
    }
}
