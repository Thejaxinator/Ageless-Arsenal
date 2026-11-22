package com.jax.destiny2mc.client.combat;

import java.util.*;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.combat.CombatMoves;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static com.jax.destiny2mc.combat.CombatMoves.*;

/** Pose-only code: also used by the offline preview sampler. No invented preview animation. */
public final class CombatModel extends HierarchicalModel<FallenMobEntity> {
    private final ModelPart root;
    private final int kind;
    private final Map<String,ModelPart> parts=new LinkedHashMap<>();
    public CombatModel(ModelPart root,int kind) {
        this.root=root;this.kind=kind;
        String[] names={"body","head","r_arm","l_arm","r_forearm","l_forearm","r_hand","l_hand","r_leg","l_leg","r_shin","l_shin","extra","l_wing","r_wing","l_wingtip","r_wingtip","shield_ring","guard","sword","l_tentacle0","l_tentacle1","l_tentacle2","l_tentacle3","l_tentacle4","l_tentacle5","l_tentacle6","r_tentacle0","r_tentacle1","r_tentacle2","r_tentacle3","r_tentacle4","r_tentacle5","r_tentacle6"};
        for(String n:names)getAnyDescendantWithName(n).ifPresent(p->parts.put(n,p));
        if(kind==27){
            for(String shell:new String[]{"upper_valve0","upper_valve1","upper_valve2","lower_valve0","lower_valve1","lower_valve2","dormant_husk"})
                getAnyDescendantWithName(shell).ifPresent(part->parts.put(shell,part));
        }
        if(kind==31){
            for(String cover:new String[]{"l_heart_cover","r_heart_cover"})
                getAnyDescendantWithName(cover).ifPresent(part->parts.put(cover,part));
        }
    }
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(FallenMobEntity entity,float swing,float amount,float age,float yaw,float pitch) {
        pose(swing,amount,age,yaw,pitch,entity.getAnimState(),entity.getAnimTimer()-(age-entity.tickCount),entity.isAggressive());
    }
    private void rot(String n,float x,float y,float z){ModelPart p=parts.get(n);if(p!=null){p.xRot+=x;p.yRot+=y;p.zRot+=z;}}
    private void visible(String n,boolean v){ModelPart p=parts.get(n);if(p!=null)p.visible=v;}
    private static float sin(float a){return (float)Math.sin(a);}
    private static float cos(float a){return (float)Math.cos(a);}
    public void pose(float swing,float amount,float age,float yaw,float pitch,int state,float remaining,boolean aiming) {
        root.getAllParts().forEach(ModelPart::resetPose);
        visible("guard",state==SHIELD);visible("sword",state==SWORD);
        float t=duration(state)-remaining,p=progress(state,remaining);
        float a=Math.min(1,amount),walk=sin(swing*.6662f),breath=sin(age*.09f);
        boolean floating=kind==25||kind==27||kind==29||kind==30||kind==31||kind==34||kind==36||kind==38;
        ModelPart body=parts.get("body");
        if(floating)body.y-=.6f+breath*.45f;
        else {rot("r_leg",walk*a*.72f,0,0);rot("l_leg",-walk*a*.72f,0,0);rot("r_shin",Math.max(0,-walk)*a*.62f,0,0);rot("l_shin",Math.max(0,walk)*a*.62f,0,0);body.y-=Math.abs(cos(swing*.6662f))*a*.45f;}
        rot("head",pitch*.0174533f,yaw*.0174533f,0);
        rot("r_arm",-walk*a*.22f,0,-.025f*breath);rot("l_arm",walk*a*.22f,0,.025f*breath);
        rot("r_forearm",-.06f-breath*.025f,0,0);rot("l_forearm",-.06f+breath*.025f,0,0);
        // Idle wing drift, flight strokes, then a distinct fold/spread for launch and impact.
        float flap=sin(age*(kind==30?.95f:kind==31?.18f:.12f));
        float wing=kind==30?.70f*flap:kind==31?.22f*flap:.055f*flap;
        if(state==CROUCH)wing=.38f*p;
        if(state==LEAP)wing=-.48f*sin(p*(float)Math.PI);
        if(state==DIVE)wing=.62f;
        if(state==LAND)wing=.32f*(1-p);
        for(int sign:new int[]{-1,1}) {
            String side=sign<0?"r":"l";
            rot(side+"_wing",.03f*flap,kind==30?0:sign*wing,sign*(kind==30?wing:kind==31?.06f*flap:0));
            rot(side+"_wingtip",0,sign*wing*.45f,sign*.035f*sin(age*.12f-1));
        }
        if(kind==27){
            // The shell opens before the first volley and folds in as the volley ends.
            float closed=aiming?.55f:1f;
            if(state==FIRE){
                float open=Math.clamp(Math.min(t/8f,remaining/5f),0f,1f);
                closed*=1f-open;
            }
            for(int i=0;i<3;i++){
                rot("upper_valve"+i,.57f*closed,0,0);
                rot("lower_valve"+i,-.57f*closed,0,0);
            }
            ModelPart core=parts.get("head");
            core.visible=closed<.985f;
            core.xScale=core.yScale=core.zScale=1f-.94f*closed;
            ModelPart husk=parts.get("dormant_husk");
            if(husk!=null)husk.xScale=husk.yScale=husk.zScale=Math.max(.01f,closed);
        }
        if(kind==31){
            float exposure=0f;
            if(state==DARKNESS)exposure=Math.clamp(Math.min((t-22f)/19f,remaining/7f),0f,1f);
            if(state==STAGGER)exposure=Math.clamp(remaining/8f,0f,1f);
            rot("r_heart_cover",0,1.03f*exposure,-.12f*exposure);
            rot("l_heart_cover",0,-1.03f*exposure,.12f*exposure);
        }
        if(kind==36)rot("shield_ring",0,age*.045f,0);
        if(kind==29||kind==38)rot("body",0,age*.018f,.035f*breath);
        boolean gunner=kind==23||kind==24||kind==28||kind==32||kind==33||kind==35||kind==40||kind==41;
        if(gunner&&(aiming||state==FIRE)) {
            // Wrists meet at the weapon's grip/foregrip; the weapon is a child of the wrist.
            ModelPart shoulder=parts.get("r_arm");
            float recoil=state==FIRE?Math.max(0,1-Math.abs((t-12)%5)/3)*.45f:0;
            Vector3f grip=new Vector3f(shoulder.x*.65f,shoulder.y-2.0f,shoulder.z-5.4f+recoil);
            float elevation=Math.max(-.6f,Math.min(.6f,pitch*.0174533f));grip.y+=elevation*6;
            solveArm("r",grip,1);
            Quaternionf wristOrientation=quat(parts.get("r_arm")).mul(quat(parts.get("r_forearm"))).invert().mul(new Quaternionf().rotationX((switch(kind){case 24,28->0f;case 32->-.69f;case 33->-.71f;case 35->-.49f;case 40->-.64f;default->-.60f;})+elevation));
            euler(parts.get("r_hand"),wristOrientation);
            solveArm("l",new Vector3f(grip).add(.5f,0,-3.0f),-1);
            rot("body",0,yaw*.003f,0);rot("head",-.06f,0,-.07f);
        }
        if(kind==25) {rot("r_arm",-.45f,0,-.20f);rot("l_arm",-.45f,0,.20f);rot("r_forearm",-.2f,0,0);rot("l_forearm",-.2f,0,0);}
        if(kind==31) {rot("r_arm",-.12f,0,-.12f);rot("l_arm",-.12f,0,.12f);rot("r_leg",.12f+breath*.03f,0,0);rot("l_leg",.12f-breath*.03f,0,0);}
        switch(state) {
            case FIRE -> {if(!gunner){rot("r_arm",-.3f,0,0);rot("l_arm",-.3f,0,0);} }
            case THRALL_SWIPE -> {
                // The left claw crosses first. It retracts as the opposite shoulder
                // winds back, leaving distinct contact silhouettes on the server hit ticks.
                float leftWind=swipePhase(t,0,THRALL_LUNGE_TICK,THRALL_LEFT_HIT_TICK);
                float left=swipePhase(t,THRALL_LUNGE_TICK,THRALL_LEFT_HIT_TICK,THRALL_RIGHT_HIT_TICK-1);
                float rightWind=swipePhase(t,THRALL_LEFT_HIT_TICK-1,THRALL_RIGHT_HIT_TICK-2,THRALL_RIGHT_HIT_TICK);
                float right=swipePhase(t,THRALL_RIGHT_HIT_TICK-2,THRALL_RIGHT_HIT_TICK,duration(THRALL_SWIPE));
                rot("l_arm",-.62f*leftWind-.72f*left,-.63f*leftWind-.28f*left,-.42f*leftWind+.03f*left);
                rot("l_forearm",-.70f*leftWind-.53f*left,0,-.22f*leftWind+.05f*left);
                rot("l_hand",-.23f*leftWind-.11f*left,-.04f*left,0);
                rot("r_arm",-.62f*rightWind-.72f*right,.63f*rightWind+.23f*right,.42f*rightWind-.02f*right);
                rot("r_forearm",-.70f*rightWind-.55f*right,-.11f*right,.22f*rightWind-.06f*right);
                rot("r_hand",-.23f*rightWind-.14f*right,-.07f*right,-.02f*right);
                rot("body",.18f*(left+right)-.06f*(leftWind+rightWind),.32f*(left-right)-.18f*(leftWind-rightWind),.04f*(left-right));
                rot("head",-.09f*(left+right),-.12f*(left-right),0);
            }
            case MELEE -> {
                float strike=sin(Math.min(1,t/13)*(float)Math.PI);
                rot("r_arm",-1.6f+2.4f*strike,0,-.25f);rot("l_arm",-.5f,0,.25f);rot("body",.18f*strike,0,0);
            }
            case BLINK -> {rot("body",.16f,0,0);rot("r_arm",-.45f,0,.15f);rot("l_arm",-.45f,0,-.15f);}
            case SHIELD -> {
                float rise=Math.min(1,t/10)*Math.min(1,remaining/10);
                ModelPart sh=parts.get("l_arm");solveArm("l",new Vector3f(0,sh.y+3,-5),-1);
                // Keep summoned shield vertical in world space despite forearm/elbow motion.
                ModelPart guard=parts.get("guard");ModelPart upper=parts.get("l_arm"),fore=parts.get("l_forearm"),hand=parts.get("l_hand");
                Quaternionf q=quat(upper).mul(quat(fore)).mul(quat(hand)).invert();euler(guard,q);guard.xScale=guard.yScale=guard.zScale=Math.max(.01f,rise);
                rot("r_arm",-.4f,0,0);rot("head",.12f,0,0);
            }
            case STASIS -> {rot("r_arm",-1.2f,-.45f,.35f);rot("l_arm",-1.2f,.45f,-.35f);rot("head",.6f,0,0);}
            case CLOUD -> {float cast=sin(p*(float)Math.PI);rot("r_arm",-1.7f*cast,0,-.45f*cast);rot("l_arm",-1.7f*cast,0,.45f*cast);rot("r_forearm",-.5f*cast,0,0);rot("l_forearm",-.5f*cast,0,0);}
            case CROUCH -> {body.y+=3*p;rot("r_leg",-.7f*p,0,0);rot("l_leg",-.7f*p,0,0);rot("r_shin",1.2f*p,0,0);rot("l_shin",1.2f*p,0,0);rot("body",.20f*p,0,0);}
            case LEAP -> {rot("r_leg",-.9f,0,0);rot("l_leg",-.9f,0,0);rot("r_shin",1.1f,0,0);rot("l_shin",1.1f,0,0);rot("body",-.18f,0,0);}
            case DIVE -> {rot("body",.68f,0,0);rot("r_leg",-.8f,0,0);rot("l_leg",-.8f,0,0);rot("r_shin",.5f,0,0);rot("l_shin",.5f,0,0);}
            case LAND -> {float f=1-p;body.y+=2.3f*f;rot("r_leg",-.55f*f,0,0);rot("l_leg",-.55f*f,0,0);rot("r_shin",1.1f*f,0,0);rot("l_shin",1.1f*f,0,0);}
            case SLAM -> {float f=t<30?Math.min(1,t/15):Math.max(0,(42-t)/12);rot("r_arm",-2.6f*f,0,-.2f);rot("l_arm",-.3f,0,.25f);if(t>=30)rot("body",.35f*sin((t-30)/20*(float)Math.PI),0,0);}
            case DARKNESS -> {rot("r_arm",-.3f,0,-.65f);rot("l_arm",-.3f,0,.65f);rot("head",-.18f,0,0);body.y-=.8f*p;}
            case STAGGER -> {rot("head",.55f,0,0);rot("body",.18f,0,.035f*sin(t*.8f));rot("r_arm",.3f,0,-.2f);rot("l_arm",.3f,0,.2f);}
            case SWORD -> {float slash=t<25?0:Math.min(1,(t-25)/10);rot("r_arm",-1.8f+2.1f*slash,-.5f+slash,-.4f);rot("body",.1f,-.3f+.6f*slash,0);}
            case EXPLODE -> {float swell=1+.09f*p;body.xScale=body.yScale=body.zScale=swell;rot("body",.06f*sin(t*1.5f),0,.04f*cos(t*1.2f));}
        }
        // Segmented Sphinx tentacles: opposing waves and two separately timed strikes.
        for(String side:new String[]{"l","r"})for(int i=0;i<7;i++){
            float sign=side.equals("l")?1:-1;
            float wave=sin(age*.11f-i*.55f+sign)*.055f;
            float strike=state==TENTACLE?Math.max(0,1-Math.abs(t-(sign>0?20:30))/9):0;
            rot(side+"_tentacle"+i,wave+strike*(i<2?.48f:-.08f),sign*.025f*sin(age*.08f+i),sign*wave*.35f);
        }
    }
    private static float swipePhase(float tick,float start,float peak,float end){
        return Math.clamp(Math.min((tick-start)/(peak-start),(end-tick)/(end-peak)),0f,1f);
    }
    private static Quaternionf quat(ModelPart p){return new Quaternionf().rotationZYX(p.zRot,p.yRot,p.xRot);}
    private static void euler(ModelPart p,Quaternionf q){q.normalize();float x=q.x,y=q.y,z=q.z,w=q.w;p.xRot=(float)Math.atan2(2*(w*x+y*z),1-2*(x*x+y*y));p.yRot=(float)Math.asin(Math.clamp(2*(w*y-z*x),-1f,1f));p.zRot=(float)Math.atan2(2*(w*z+x*y),1-2*(y*y+z*z));}
    private void solveArm(String side,Vector3f target,float bendSign) {
        ModelPart a=parts.get(side+"_arm"),b=parts.get(side+"_forearm"),h=parts.get(side+"_hand");
        if(a==null||b==null||h==null)return;
        Vector3f rest1=new Vector3f(b.x,b.y,b.z),rest2=new Vector3f(h.x,h.y,h.z);
        float l1=rest1.length(),l2=rest2.length();if(l1<.01f||l2<.01f)return;
        Vector3f d=new Vector3f(target).sub(a.x,a.y,a.z);float distance=Math.max(Math.abs(l1-l2)+.05f,Math.min(l1+l2-.05f,d.length()));d.normalize();
        float along=(l1*l1-l2*l2+distance*distance)/(2*distance),height=(float)Math.sqrt(Math.max(0,l1*l1-along*along));
        Vector3f bend=new Vector3f(-bendSign*.4f,1,.15f);bend.sub(new Vector3f(d).mul(bend.dot(d))).normalize();
        Vector3f elbow=new Vector3f(d).mul(along).add(bend.mul(height));
        Quaternionf qa=new Quaternionf().rotationTo(rest1,elbow);euler(a,qa);
        Vector3f lower=new Vector3f(d).mul(distance).sub(elbow);qa.conjugate(new Quaternionf()).transform(lower);
        euler(b,new Quaternionf().rotationTo(rest2,lower));
    }
}
