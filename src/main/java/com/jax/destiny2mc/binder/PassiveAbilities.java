package com.jax.destiny2mc.binder;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Passive definitions and server-owned proc conditions. A style consumes one of the
 * same two passive sockets as a subclass perk. Vanilla status effects keep their
 * own expiration and normal effect precedence, including potions from other mods. */
@EventBusSubscriber(modid="destiny2mc")
public final class PassiveAbilities {
    private PassiveAbilities(){}
    private static final List<BinderAbility> CATALOG=List.of(
        entry("triple_jump","Triple Jump","kinetic","movement","Tap Jump in midair for two extra jumps. Steer each with your movement keys.",12,3),
        entry("strafe_lift","Strafe Lift","kinetic","movement","Press and hold Jump in midair to thrust upward for up to 12 ticks. Steer with movement keys.",12,3),
        entry("burst_glide","Burst Glide","kinetic","movement","Press and hold Jump in midair for a forward burst and up to 24 ticks of slow descent.",12,3),
        entry("ember_of_solace","Ember of Solace","solar","passive","Defeating a burning foe or scoring a Solar ability kill restores 3 health. Recharges in 4 seconds.",16,4),
        entry("spark_of_haste","Spark of Haste","arc","passive","Sprint for one second to gain Speed I. The effect lasts while sprinting and briefly afterward.",14,3),
        entry("echo_of_reprisal","Echo of Reprisal","void","passive","A crouched kill grants 3 seconds of invisibility and breaks nearby enemies’ target lock. Recharges in 8 seconds.",18,4),
        entry("whisper_of_chains","Whisper of Chains","stasis","passive","Gain Resistance I while within 6 blocks of a slowed hostile.",16,4),
        entry("thread_of_warding","Thread of Warding","strand","passive","An experience orb grants two absorption hearts for 6 seconds. Recharges in 12 seconds.",16,4)
    );
    private static BinderAbility entry(String id,String label,String element,String category,String hint,int motes,int levels){
        return new BinderAbility(id,label,element,category,hint,motes,levels,0);
    }
    public static List<BinderAbility> catalog(){return CATALOG;}
    public static boolean movement(String id){return id.equals("triple_jump")||id.equals("strafe_lift")||id.equals("burst_glide");}

    static void ring(ServerPlayer p,EffectProfile profile,double y,float radius){
        var point=p.position().add(0,y,0);
        VoxelEffects.emit(p.serverLevel(),VoxelCue.Kind.RING,profile,point,point,radius);
    }
    /** Absolute world time preserves short proc lockouts over logout and login. */
    private static boolean ready(ServerPlayer p,String id,int ticks){
        var n=p.getPersistentData();
        String key="aa_binder_proc_"+id;
        long now=p.serverLevel().getGameTime();
        if(n.getLong(key)>now)return false;
        n.putLong(key,now+ticks);
        return true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void defeat(LivingDeathEvent event){
        var target=event.getEntity();
        if(!(event.getSource().getEntity() instanceof ServerPlayer player) || player==target || player.isSpectator() || player.isAlliedTo(target))return;
        boolean hostile=target instanceof Enemy
            || target instanceof net.minecraft.world.entity.Mob mob && mob.getTarget()==player
            || target instanceof net.minecraft.world.entity.player.Player rival && player.canHarmPlayer(rival);
        if(!hostile)return;
        var n=target.getPersistentData();
        long age=player.serverLevel().getGameTime()-n.getLong("aa_binder_last_tick");
        boolean solar=n.hasUUID("aa_binder_last_owner") && player.getUUID().equals(n.getUUID("aa_binder_last_owner"))
            && n.getString("aa_binder_last_element").equals("solar") && age==0;
        if ((target.isOnFire() || solar) && player.getHealth()<player.getMaxHealth() && BinderState.hasPassive(player,"ember_of_solace") && ready(player,"solace",80)){
            player.heal(3f);ring(player,EffectProfile.SOLAR,.75f,.75f);
        }
        if(player.isShiftKeyDown() && BinderState.hasPassive(player,"echo_of_reprisal") && ready(player,"reprisal",160)){
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,60,0,false,false,true));
            for(var mob:player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(8), mob->mob.getTarget()==player))mob.setTarget(null);
            VoxelEffects.burst(player.serverLevel(),player.position().add(0,.8,0),"void");
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void pickup(PlayerXpEvent.PickupXp event){
        if(event.getEntity() instanceof ServerPlayer p && !p.isSpectator() && BinderState.hasPassive(p,"thread_of_warding") && ready(p,"warding",240)){
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,120,0,false,false,true));ring(p,EffectProfile.STRAND,.8f,.9f);
        }
    }
    @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){GuardianMovement.dimension(e);}
    @SubscribeEvent public static void update(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        GuardianMovement.tick(p);
        if(p.isSpectator()||!p.isAlive())return;
        var n=p.getPersistentData();
        if(BinderState.hasPassive(p,"spark_of_haste") && p.isSprinting()){
            int previous=n.getInt("aa_binder_sprint");
            int ticks=Math.min(21,previous+1);
            if(previous!=ticks)n.putInt("aa_binder_sprint",ticks);
            if(ticks==20)ring(p,EffectProfile.ARC,.1f,.7f);
            if(ticks==20||(ticks==21&&p.tickCount%10==0))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,0,false,false,true));
        }else if(n.contains("aa_binder_sprint"))n.remove("aa_binder_sprint");
        if(p.tickCount%10==0 && BinderState.hasPassive(p,"whisper_of_chains") &&
            !p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(6), e-> e!=p && e.isAlive() && (e instanceof Enemy || (e instanceof net.minecraft.world.entity.Mob mob && mob.getTarget()==p)) && !e.isAlliedTo(p) && e.distanceToSqr(p)<=36 && e.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)).isEmpty()){
            if(!p.hasEffect(MobEffects.DAMAGE_RESISTANCE))ring(p,EffectProfile.STASIS,.4,.9f);
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,17,0,false,false,true));
        }
    }
}
