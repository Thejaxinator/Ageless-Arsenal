package com.jax.destiny2mc.ageless.scorn;

import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Pose the eight Tangled Shore ranks using the move and elapsed ticks already
 * replicated by FrontierMob. No client clock controls attacks or damage.
 * Rotations below are degrees relative to each bone's authored bind pose.
 */
public final class ScornPose {
    private static final String[] SIDES={"r","l"};
    private final Map<String,ModelPart> parts=new HashMap<>();
    private final ModelPart[] all;
    private final int kind;

    public ScornPose(FrontierModel model,int kind) {
        this.kind=kind;
        all=model.root().getAllParts().toArray(ModelPart[]::new);
        for(String bone:new String[]{"body","thorax","head","gun","guard","apron","chain","censer","sacs","staff"})
            bind(model,bone);
        for(String side:SIDES)
            for(String suffix:new String[]{"_leg","_shin","_foot","_frontleg","_frontshin","_midleg","_midshin",
                    "_arm","_forearm","_hand","_underarm","_underforearm","_coat"})
                bind(model,side+suffix);
    }
    private void bind(FrontierModel model,String bone){
        model.getAnyDescendantWithName(bone).ifPresent(p->parts.put(bone,p));
    }
    private void turn(String n,float x,float y,float z){
        ModelPart p=parts.get(n);
        if(p!=null){p.xRot+=x*Mth.DEG_TO_RAD;p.yRot+=y*Mth.DEG_TO_RAD;p.zRot+=z*Mth.DEG_TO_RAD;}
    }
    private static float pulse(float t,float peak,float width){return Math.max(0,1-Math.abs(t-peak)/width);}
    private static float up(float t,float duration){float x=Mth.clamp(t/duration,0,1);return x*x*(3-2*x);}
    private static float arc(float t,float finish){return Mth.sin(Mth.PI*Mth.clamp(t/finish,0,1));}

    public void sample(float swing,float amount,float age,float yaw,float pitch,int move,float t,boolean aimed){
        for(ModelPart part:all)part.resetPose();
        float walk=Mth.sin(swing*.68F)*Mth.clamp(amount,0,1);
        float breath=Mth.sin(age*.088F);
        ModelPart body=parts.get("body");
        if(kind==118){crawl(walk,breath,age,move,t);return;}
        boolean brute=kind==119;
        float length=brute?24:36;
        body.y-=Math.abs(walk)*(brute?.48F:.64F);
        turn("thorax",1.9F*breath,walk*(brute?4:7),walk*2);
        turn("head",Mth.clamp(pitch,-46,46)*.78F,Mth.clamp(yaw,-64,64)*.85F,0);
        for(int i=0;i<2;i++){
            String side=SIDES[i];int sign=i==0?-1:1;float stride=walk*sign;
            turn(side+"_leg",stride*length,0,0);
            turn(side+"_shin",Math.max(0,-stride)*29,0,0);
            turn(side+"_foot",-Math.max(0,-stride)*17,0,0);
            turn(side+"_arm",-stride*(brute?15:21),0,sign*breath*1.8F);
            turn(side+"_underarm",stride*13+breath*2,0,-sign*4);
            turn(side+"_underforearm",-stride*8,0,0);
            turn(side+"_coat",Math.max(0,-stride)*16,0,-sign*breath);
        }
        turn("apron",-Math.abs(walk)*9-breath,0,walk*5);
        if(kind==116){
            // Let the chain lag behind the hand and the caged lamp lag behind the chain.
            turn("r_arm",-24,0,6);turn("chain",17+walk*13,0,6*walk);
            turn("censer",-5-walk*6,0,-3*walk);
        }
        if(kind==133){turn("r_forearm",-17,0,2);turn("staff",30-walk*7,0,4);}
        if((aimed||move==FrontierMob.FIRE)&&(kind<=115||kind==117)){
            float kick=move==FrontierMob.FIRE?Math.max(pulse(t,12,3),Math.max(pulse(t,18,3),pulse(t,24,3))):0;
            turn("r_arm",-48+pitch*.55F-7*kick,4,4);turn("r_forearm",-16,0,0);
            turn("r_hand",75+6*kick,0,0);
            if(kind!=115){turn("l_arm",-31,7,-5);turn("l_forearm",-23,0,0);}
        }
        if(move==FrontierMob.MELEE){
            // The native melee contact is tick 14. Fast downswing, slower recovery.
            float wind=up(t,9)*(1-up(t-9,5));
            float hit=up(t-9,5)*(1-up(t-15,12));
            turn("thorax",-9*wind+18*hit,-23*wind+22*hit,0);
            turn("r_arm",-84*wind-42*hit,8*wind-22*hit,-22*wind);
            turn("r_forearm",-17*wind+18*hit,0,0);
            turn("chain",-32*wind-61*hit,0,19*hit);
            turn("censer",17*wind+12*hit,0,8*hit);
            turn("staff",15*wind-32*hit,0,-19*hit);
        }
        if(kind==115){
            // The shield's bind plane faces forward. Counterrotate at the pivot
            // after lifting the forearm to keep that plane between shooter and shield.
            float lift=move==FrontierMob.SHIELD?up(t,8)*(1-up(t-73,7)):0;
            turn("l_arm",-29-38*lift,2+12*lift,7-9*lift);
            turn("l_forearm",-11-15*lift,0,0);turn("guard",50+50*lift,0,0);
            turn("head",6*lift,-10*lift,0);
        }
        if(kind==114&&move==FrontierMob.BLINK){
            float gather=pulse(t,11,11);
            turn("thorax",23*gather,10*gather,0);turn("r_leg",-19*gather,0,0);turn("l_shin",28*gather,0,0);
        }
        if(kind==117&&move==FrontierMob.SHIELD){
            float lift=up(t,14)*(1-up(t-68,12));
            turn("l_arm",-78*lift,0,-25*lift);turn("l_forearm",-28*lift,0,0);
            turn("l_underarm",-21*lift,0,-14*lift);turn("r_underarm",-21*lift,0,14*lift);
        }
        if(brute&&(move==FrontierMob.CAST||move==FrontierMob.FIRE)){
            float rise=move==FrontierMob.CAST?arc(t,58):arc(t,32);
            turn("thorax",-9*rise,0,0);
            for(int i=0;i<2;i++){
                int sign=i==0?-1:1;turn(SIDES[i]+"_arm",-54*rise,sign*8*rise,-sign*16*rise);
                turn(SIDES[i]+"_forearm",-22*rise,0,0);
                turn(SIDES[i]+"_hand",-12*rise,0,sign*13*rise);
            }
        }
        if(kind==133&&(move==FrontierMob.CAST||move==FrontierMob.FIRE)){
            float cast=arc(t,move==FrontierMob.CAST?58:36);
            turn("r_arm",-61*cast,12*cast,8*cast);turn("r_forearm",-13*cast,0,0);
            turn("staff",37*cast,0,-17*cast);turn("l_arm",-44*cast,0,-31*cast);
            turn("l_underarm",-19*cast,0,-19*cast);
        }
    }

    private void crawl(float walk,float breath,float age,int move,float t){
        ModelPart body=parts.get("body"); body.y-=.41F*Math.abs(walk);
        turn("thorax",walk*2+breath,walk*4,walk*2);
        for(int i=0;i<2;i++){
            String s=SIDES[i];float stride=walk*(i==0?1:-1);
            turn(s+"_frontleg",stride*26,0,0);turn(s+"_frontshin",Math.max(0,-stride)*21,0,0);
            turn(s+"_midleg",-stride*21,0,0);turn(s+"_midshin",Math.max(0,stride)*15,0,0);
            turn(s+"_leg",stride*32,0,0);turn(s+"_shin",-stride*19,0,0);
        }
        float fuse=move==FrontierMob.CAST?up(t,26):0;
        float beat=breath*.023F+fuse*(.14F+.055F*Mth.sin(age*.76F));
        ModelPart sacs=parts.get("sacs");sacs.xScale=1+beat;sacs.zScale=1+beat;sacs.yScale=1+beat*1.6F;
        body.y+=.78F*fuse;turn("head",-3*breath+13*fuse,0,0);
    }
}
