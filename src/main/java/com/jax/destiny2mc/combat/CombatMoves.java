package com.jax.destiny2mc.combat;

/** Shared timeline constants: server hit frames and client poses use the same clock. */
public final class CombatMoves {
    private CombatMoves() {}
    public static final int IDLE=0, FIRE=10, MELEE=11, BLINK=12, SHIELD=13, CLOUD=14,
        CROUCH=15, LEAP=16, DIVE=17, LAND=18, SLAM=19, DARKNESS=20, STAGGER=21,
        SWORD=22, TENTACLE=23, STASIS=24, EXPLODE=25, THRALL_SWIPE=26;
    public static final int THRALL_LUNGE_TICK=2, THRALL_LEFT_HIT_TICK=4, THRALL_RIGHT_HIT_TICK=8, THRALL_SWIPE_COOLDOWN=8;
    /** The old single-contact API now identifies the opening claw. */
    @Deprecated public static final int THRALL_SWIPE_HIT_TICK=THRALL_LEFT_HIT_TICK;
    public static final float THRALL_CLAW_DAMAGE=1.5f;
    public static int duration(int state) {
        return switch(state) {
            case FIRE -> 28; case MELEE -> 22; case THRALL_SWIPE -> 11; case BLINK -> 20; case SHIELD -> 70;
            case CLOUD -> 36; case CROUCH -> 20; case LEAP -> 14; case DIVE -> 36;
            case LAND -> 18; case SLAM -> 50; case DARKNESS -> 90; case STAGGER -> 45;
            case SWORD -> 48; case TENTACLE -> 42; case STASIS -> 55; case EXPLODE -> 18;
            default -> 0;
        };
    }
    public static float progress(int state, float remaining) {
        int d=duration(state); return d==0?0:Math.clamp((d-remaining)/d,0f,1f);
    }
    public static boolean hitFrame(int state,int age) {
        return switch(state) {
            case FIRE -> age==12 || age==17 || age==22;
            case MELEE -> age==12; case THRALL_SWIPE -> age==THRALL_LEFT_HIT_TICK || age==THRALL_RIGHT_HIT_TICK; case BLINK -> age==10; case CLOUD -> age==22;
            case SLAM -> age==34; case DARKNESS -> age==82; case SWORD -> age==30;
            case TENTACLE -> age==20 || age==30; case EXPLODE -> age==15;
            default -> false;
        };
    }
    public static boolean inCloud(double dx,double dy,double dz,double radius) {
        return dx*dx+dz*dz<=radius*radius && dy>=-0.6 && dy<=2.6;
    }
    public static boolean diveImpact(int age,boolean grounded,boolean horizontalCollision,boolean nearTarget) {
        return age>2 && (grounded||horizontalCollision||nearTarget);
    }
    public static float blinkScale(float elapsed) {
        // Departure collapses; two invisible ticks straddle the server teleport; reform expands.
        if(elapsed<8) return Math.max(.02f,1-elapsed/8);
        if(elapsed<12) return .001f;
        return Math.min(1,(elapsed-12)/8);
    }
    public static int legacyState(int state) {
        return switch(state) {case FIRE -> 1; case BLINK -> 2; case MELEE, THRALL_SWIPE -> 4; default -> state<10?state:0;};
    }
}
