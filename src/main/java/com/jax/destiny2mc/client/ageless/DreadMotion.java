package com.jax.destiny2mc.client.ageless;

import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Per-instance additive poses in radians. Game ticks drive action timing, while partial ticks keep
 * a spectator's animation smooth between network updates. Animation never inflicts damage.
 */
public final class DreadMotion {
    private final ModelPart root;
    private final int kind;
    private final Map<String,ModelPart> bones=new HashMap<>();
    private static final String[] SIDES={"r","l"};

    public DreadMotion(FrontierModel model,int kind) {
        this.kind=kind;
        root=model.root();
        String parts="body thorax head jaw tail tailtip tabard gun weapon "
            +"r_arm l_arm r_forearm l_forearm r_hand l_hand r_leg l_leg r_shin l_shin r_foot l_foot "
            +"r_wing l_wing r_wingtip l_wingtip r_jaw l_jaw r_blade l_blade r_coat l_coat r_weak l_weak";
        for (String part:parts.split(" ")) model.getAnyDescendantWithName(part).ifPresent(p->bones.put(part,p));
    }
    private void turn(String name,float x,float y,float z) {
        ModelPart bone=bones.get(name);
        if(bone!=null){bone.xRot+=x;bone.yRot+=y;bone.zRot+=z;}
    }
    private static float wave(float t){return (float)Math.sin(t);}
    private static float clamp(float v){return Math.max(0,Math.min(1,v));}
    private static float ease(float v){v=clamp(v);return v*v*(3-2*v);}
    /** Attack envelope winds up and then releases on the recorded hit tick. */
    private static float strike(float t,float hit){return ease((t-hit+6)/6)*(1-ease((t-hit)/7));}
    private void walk(float stride,float step,float age) {
        boolean heavy=kind==126||kind==138;
        float angle=heavy?.49f:.67f;
        for(int i=0;i<2;i++){
            String side=SIDES[i];float sign=i==0?-1:1,phase=i==0?stride:-stride;
            turn(side+"_leg",angle*phase,0,0);
            turn(side+"_shin",Math.max(0,-phase)*.69f,0,0);
            turn(side+"_foot",Math.min(0,phase)*.26f,0,0);
            turn(side+"_arm",-phase*.24f,0,0);
            turn(side+"_coat",.09f+.14f*phase,0,sign*wave(age*.090f)*.035f);
        }
        bones.get("body").y-=Math.abs(stride)*(.35f+step*.16f);
        turn("tabard",-.04f+.15f*Math.abs(stride),0,0);
    }
    private void flight(float age,int state,float t) {
        boolean grim=kind==120;
        float pulse=state==FrontierMob.CAST?strike(t,28):state==FrontierMob.MELEE?strike(t,14):0;
        float beat=age*(grim?.34f:.45f);
        bones.get("body").y-=.73f+wave(beat*.50f)*(grim?.64f:.24f);
        for(int i=0;i<2;i++){
            float sign=i==0?-1:1;String side=SIDES[i];
            turn(side+"_wing",.07f*wave(beat),sign*.10f*wave(beat),sign*(.39f*wave(beat)+pulse*.3f));
            turn(side+"_wingtip",0,sign*.14f*wave(beat-.72f),sign*.36f*wave(beat-.62f));
            turn(side+"_leg",.11f*wave(beat*.48f+sign),0,0);
            turn(side+"_jaw",-.12f*wave(beat)-pulse*.30f,sign*.18f*pulse,0);
        }
        turn("tail",wave(beat*.63f)*.18f,0,0);turn("tailtip",wave(beat*.60f-.7f)*.32f,0,0);
        if(grim){
            turn("jaw",pulse*.64f,0,0);turn("head",-pulse*.25f,0,0);
            turn("thorax",-.21f*pulse,0,0);
            if(state==FrontierMob.FIRE){turn("r_arm",-.68f,0,.22f);turn("r_forearm",-.54f,0,0);turn("r_hand",.62f,0,0);}
        } else turn("head",pulse*.39f,0,0);
    }
    private void gunners(boolean aim,float pitch,int state,float t) {
        if(aim||state==FrontierMob.FIRE){
            // The Psion-derived rank points one hand-fork. Its other hand stays available to form Strand or Stasis.
            float recoil=state==FrontierMob.FIRE?strike(t,12)*.14f:0;
            turn("r_arm",-1.21f+pitch*.009f-recoil,0,.11f);
            turn("r_forearm",-.35f,0,0);turn("r_hand",1.76f+recoil,0,0);
        }
        if(state==FrontierMob.CAST){
            float reach=ease(t/18)*(1-ease((t-36)/20));
            turn("l_arm",-1.46f*reach,-.14f*reach,-.40f*reach);
            turn("l_forearm",-.28f*reach,0,0);turn("l_hand",.43f*reach,reach*.57f,0);
        }
    }
    private void disciples(int state,float t,float age,boolean aim) {
        if(kind==124||kind==125){
            turn("r_arm",-.18f,0,.18f);turn("r_forearm",-.27f,0,0);
            if(state==FrontierMob.FIRE||state==FrontierMob.CAST||aim){
                float cast=state==FrontierMob.CAST?ease(t/19)*(1-ease((t-42)/17)):.37f;
                turn("l_arm",-.32f-cast*1.13f,-.18f,-.29f*cast);
                turn("l_hand",-.15f,.32f+cast*.39f,0);turn("weapon",-.15f,0,.17f*cast);
            }
        } else if(kind==137){
            // His still, upright posture and backward-held forearms contrast with the advancing kick.
            turn("r_arm",.22f,0,.11f);turn("l_arm",.23f,0,-.16f);
            turn("l_forearm",-.44f,.05f,-.39f);turn("r_forearm",-.27f,0,.12f);
            if(state==FrontierMob.CHARGE){
                float lift=ease(t/10)*(1-ease((t-32)/14));
                turn("body",.17f*lift,.20f*lift,0);turn("l_leg",-1.61f*lift,0,0);
                turn("l_shin",.89f*lift*(1-ease((t-16)/9)),0,0);
                turn("r_arm",-.70f*lift,0,0);
            }
        } else {
            turn("r_forearm",-.36f,0,0);turn("l_forearm",-.24f,0,0);turn("weapon",0,0,-.23f);
            if(kind==138)for(int i=0;i<2;i++){
                float sign=i==0?-1:1;turn(SIDES[i]+"_wing",.07f*wave(age*.09f),sign*.09f*wave(age*.1f),sign*.08f*wave(age*.10f));
            }
            if(state==FrontierMob.LEAP||state==FrontierMob.SLAM){
                float lift=ease(t/12)*(1-ease((t-29)/13));
                turn("r_leg",-lift*.93f,0,0);turn("l_leg",-lift*.69f,0,0);
                turn("r_shin",lift*1.19f,0,0);turn("l_shin",lift*1.04f,0,0);
                turn("r_arm",-2.4f*lift,0,-.23f*lift);turn("l_arm",-1.02f*lift,0,.41f*lift);
                turn("body",-.18f*lift+.43f*strike(t,34),0,0);
            }
        }
    }
    public void sample(float swing,float amount,float age,float yaw,float pitch,int state,float t,boolean aim,int weak) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float stride=wave(swing*.6662f)*Math.min(1,amount);
        turn("head",pitch*.0174533f,Math.max(-72,Math.min(72,yaw))*.0174533f,0);
        if(kind==120||kind==127){flight(age,state,t);return;}
        walk(stride,amount,age);
        turn("body",wave(age*.083f)*.012f,0,0);
        if(kind==121){
            turn("body",.09f,0,0);
            turn("r_arm",-.20f,0,-.12f);turn("l_arm",-.20f,0,.12f);
            turn("r_forearm",-.36f,0,0);turn("l_forearm",-.32f,0,0);
        }
        if(kind==122||kind==123)gunners(aim,pitch,state,t);
        else if(kind!=121)disciples(state,t,age,aim);
        if(state==FrontierMob.MELEE){
            float right=strike(t,14),left=kind==121?strike(t,22):0;
            turn("r_arm",-right*1.66f,right*.40f,-right*.30f);
            turn("l_arm",-left*1.61f,-left*.45f,left*.23f);turn("body",.08f*(right+left),.26f*(right-left),0);
            turn("r_blade",-.28f*right,.22f*right,0);turn("l_blade",-.28f*left,-.22f*left,0);
        }
        if(state==FrontierMob.STAGGER){
            turn("body",.28f,0,wave(t*.52f)*.04f);turn("head",.32f,0,0);
        }
        if(bones.containsKey("r_weak")){
            bones.get("r_weak").visible=(weak&1)==0;
            bones.get("l_weak").visible=(weak&2)==0;
        }
    }
}
