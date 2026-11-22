package com.jax.destiny2mc.binder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.jax.destiny2mc.ageless.fx.EffectProfile;

/** Per-airborne-trip state never crosses dimension, respawn or a connection. Two
 * equipped jump styles alternate in socket order. Releasing Jump cuts lift/glide. */
final class GuardianMovement {
    private GuardianMovement(){}
    private static final class Flight {
        final Map<String,Integer> used=new HashMap<>();
        int next,remaining,forward,left;
        String style="";
        boolean held;
        long lastPress=Long.MIN_VALUE/2;
        double launchHeight;
    }
    private static final Map<ServerPlayer,Flight> flights=new WeakHashMap<>();
    private static boolean forbidden(ServerPlayer p){return !p.isAlive()||p.isSpectator()||p.getAbilities().flying||
        p.isPassenger()||p.isFallFlying()||p.isInWaterOrBubble()||p.isInLava()||p.onClimbable();}
    private static boolean landed(ServerPlayer p){
        return p.onGround()&&!p.serverLevel().noCollision(p,p.getBoundingBox().move(0,-.07,0));
    }
    static void input(ServerPlayer p,JumpIntent input){
        Flight state=flights.get(p);
        if(!input.pressed()){
            if(state!=null){state.held=false;state.remaining=0;}
            return;
        }
        if(forbidden(p)||landed(p))return;
        List<String> styles=BinderState.passives(p).stream().filter(PassiveAbilities::movement).toList();
        if(styles.isEmpty())return;
        if(state==null){state=new Flight();flights.put(p,state);}
        long now=p.level().getGameTime();
        if(state.held||now-state.lastPress<3)return;
        for(int offset=0;offset<styles.size();offset++){
            int index=(state.next+offset)%styles.size();
            String style=styles.get(index);
            int max=style.equals("triple_jump")?2:1;
            int spent=state.used.getOrDefault(style,0);
            if(spent>=max)continue;
            state.used.put(style,spent+1);state.next=(index+1)%styles.size();
            state.lastPress=now;state.held=true;state.style=style;state.launchHeight=p.getY();
            state.forward=Integer.signum(input.forward());state.left=Integer.signum(input.left());
            double y=style.equals("triple_jump")?.45:style.equals("strafe_lift")?.32:.13;
            double speed=style.equals("triple_jump")?.22:style.equals("strafe_lift")?.27:.48;
            state.remaining=style.equals("strafe_lift")?11:style.equals("burst_glide")?23:0;
            move(p,state,y,speed);
            PassiveAbilities.ring(p,style.equals("triple_jump")?EffectProfile.ARC:style.equals("strafe_lift")?EffectProfile.SOLAR:EffectProfile.VOID,.1f,.75f);
            return;
        }
    }
    private static void move(ServerPlayer p,Flight s,double vertical,double speed){
        double yaw=Math.toRadians(p.getYRot()),sn=Math.sin(yaw),cs=Math.cos(yaw);
        int f=s.forward,l=s.left;
        if(f==0&&l==0 && s.style.equals("burst_glide"))f=1;
        Vec3 horizontal=new Vec3(-sn*f+cs*l,0,cs*f+sn*l).normalize().scale(speed);
        p.setDeltaMovement(horizontal.x,vertical,horizontal.z);p.hurtMarked=true;
        p.fallDistance=0;
    }
    static void tick(ServerPlayer p){
        Flight s=flights.get(p);
        if(s==null)return;
        if(landed(p)||forbidden(p)){flights.remove(p);return;}
        if(s.remaining<=0||s.lastPress==p.level().getGameTime())return;
        if(!s.held||!BinderState.hasPassive(p,s.style)){s.remaining=0;return;}
        boolean lift=s.style.equals("strafe_lift");
        // Upward thrust tapers at the height limit. Cap climb from the activation point
        // so a long frame or a packet retry cannot accumulate extra altitude.
        double vertical=lift?Math.min(.25,Math.max(-.08,s.launchHeight+2.5-p.getY())):-.035;
        move(p,s,vertical,lift?.27:.38);
        s.remaining--;
    }
    static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){
        if(e.getEntity() instanceof ServerPlayer p)flights.remove(p);
    }
}
