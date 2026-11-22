package com.jax.destiny2mc.ageless;

import com.google.gson.*;
import java.util.*;

/** A trigger starts bursts, a burst consumes rounds, and each round delivers one or more pellets.
 * Keeping these levels distinct lets a pulse rifle consume three rounds while Telesto consumes one. */
public record WeaponProgram(Trigger trigger, Feed feed, Delivery delivery, Flight flight, Pattern pattern, Hit onHit,
                            int cooldown, int chargeTicks, int burst, int interval, int count, int pierce,
                            int cost, int rechargeTicks, float speed, float angle, float radius, float damageScale, float lastMult, String fx) {
    public enum Trigger { PRESS, AUTOMATIC, CHARGE, RELEASE }
    public enum Feed { MAGAZINE, CAPACITOR }
    public enum Delivery { RAY, PROJECTILE }
    public enum Pattern { RING, FAN, CONE }
    /** IDs are saved by the existing projectile entity. Append types rather than renumbering them. */
    public enum Flight {
        BOLT(0), STICKY(1), SEEKER(2), ROCKET(3), BLIGHT(6), ARROW(7), FROST(9), PULL(10), DARK(11), LOB(12);
        public final int mode;
        Flight(int mode){this.mode=mode;}
    }
    public enum Hit { NONE, SWARM, COLLAPSE, CONDUCTOR, PRECISION_STACK, UNARMORED_PRECISION, ANTI_ARMOR, REFUND, MASTER }

    private static final Set<String> KEYS=Set.of("version","trigger","delivery","flight","pattern","on_hit","cooldown","charge_ticks","burst","interval","count","pierce","speed","angle","radius","damage_scale","last_mult","fx","feed","cost","recharge_ticks");
    public static WeaponProgram parse(JsonObject j) {
        for(String key:j.keySet()) if(!KEYS.contains(key))throw new IllegalArgumentException("Unknown program key "+key);
        if(!j.has("version") || integer(j,"version",0,1,1)!=1)throw new IllegalArgumentException("Expected version 1");
        var trigger=enumeration(j,"trigger","press",Trigger.class);
        var feed=enumeration(j,"feed","magazine",Feed.class);
        int cost=integer(j,"cost",1,1,128), recharge=integer(j,"recharge_ticks",feed==Feed.CAPACITOR?20:0,0,1200);
        if((feed==Feed.CAPACITOR)!=(recharge>0))throw new IllegalArgumentException("Only a capacitor takes positive recharge_ticks");
        var delivery=enumeration(j,"delivery","ray",Delivery.class);
        var flight=enumeration(j,"flight","bolt",Flight.class);
        var pattern=enumeration(j,"pattern","cone",Pattern.class);
        var hit=enumeration(j,"on_hit","none",Hit.class);
        int cooldown=integer(j,"cooldown",6,1,600),charge=integer(j,"charge_ticks",0,0,200);
        int burst=integer(j,"burst",1,1,12),interval=integer(j,"interval",1,1,40),count=integer(j,"count",1,1,16),pierce=integer(j,"pierce",1,1,8);
        float speed=number(j,"speed",1.5f,.05f,5),angle=number(j,"angle",0,0,45),radius=number(j,"radius",0,0,8);
        float scale=number(j,"damage_scale",1,.01f,10),last=number(j,"last_mult",1,.1f,5);
        String fx=j.has("fx")?j.get("fx").getAsString():"kinetic";
        if(!fx.matches("[a-z][a-z0-9_/.-]{0,63}")) throw new IllegalArgumentException("Invalid fx profile");
        if((trigger==Trigger.CHARGE||trigger==Trigger.RELEASE)!=(charge>0))throw new IllegalArgumentException("Only charged triggers take charge_ticks");
        if((burst-1)*interval>=cooldown && burst>1)throw new IllegalArgumentException("Burst must end before next trigger");
        if(delivery==Delivery.RAY && (j.has("flight")||j.has("speed")||j.has("radius")))throw new IllegalArgumentException("Flight properties require projectile delivery");
        if(delivery==Delivery.PROJECTILE && (pierce>1 || hit!=Hit.NONE))throw new IllegalArgumentException("Projectile impacts use flight; pierce/on_hit are ray options");
        return new WeaponProgram(trigger,feed,delivery,flight,pattern,hit,cooldown,charge,burst,interval,count,pierce,cost,recharge,speed,angle,radius,scale,last,fx);
    }
    private static <E extends Enum<E>> E enumeration(JsonObject j,String key,String fallback,Class<E> type) {
        return Enum.valueOf(type,(j.has(key)?j.get(key).getAsString():fallback).toUpperCase(Locale.ROOT));
    }
    private static float number(JsonObject j,String key,float fallback,float min,float max) {
        if(!j.has(key))return fallback;
        var e=j.get(key);
        if(!e.isJsonPrimitive()||!e.getAsJsonPrimitive().isNumber())throw new IllegalArgumentException(key+" must be a number");
        float n=e.getAsFloat();
        if(!Float.isFinite(n)||n<min||n>max)throw new IllegalArgumentException(key+" outside "+min+".."+max);
        return n;
    }
    private static int integer(JsonObject j,String key,int fallback,int min,int max){
        float n=number(j,key,fallback,min,max);
        if(n!=Math.rint(n))throw new IllegalArgumentException(key+" must be an integer");
        return (int)n;
    }
}
