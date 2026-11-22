package com.jax.destiny2mc.client.frontier;

import com.jax.destiny2mc.client.FallenStudyModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.jax.destiny2mc.client.combat.CombatModel;
import com.jax.destiny2mc.combat.CombatMoves;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;

import static net.minecraft.util.Mth.DEG_TO_RAD;
import static com.jax.destiny2mc.frontier.FrontierMob.*;

/** Poses native bones on the authoritative Frontier action clock, sharing the original artists' armatures. */
public final class EclipseModel extends HierarchicalModel<FrontierMob> {
    private final ModelPart root, body;
    private final ModelPart eyeSocket, eyeCore;
    private final int kind;
    private final CabalStudyModel cabal;
    private final FallenStudyModel captain;
    private final CombatModel combat;
    private final Map<String,ModelPart> parts=new HashMap<>();
    private final ModelPart[] forelegs=new ModelPart[4], knees=new ModelPart[4], paws=new ModelPart[4];
    private final ModelPart[] wisps=new ModelPart[3];

    public EclipseModel(ModelPart root,int kind) {
        this.root=root;this.kind=kind;this.body=root.getChild("body");
        ModelPart head=body.getChild("head");
        eyeSocket=head.hasChild("taken_eye")?head.getChild("taken_eye"):null;
        eyeCore=eyeSocket==null?null:eyeSocket.getChild("taken_eye_core");
        this.cabal=switch(kind){case 102,104,106,128,130 -> new CabalStudyModel(root,kind);
            case 112 -> new CabalStudyModel(root,101);default -> null;};
        this.captain=kind==110?new FallenStudyModel(root,3):null;
        int donor=switch(kind){case 107->21;case 108->23;case 109->32;case 111->24;case 139->26;default->-1;};
        this.combat=donor<0?null:new CombatModel(root,donor);
        for(String n:new String[]{"head","jaw","tail","tail_tip","r_arm","l_arm","r_forearm","l_forearm","r_hand","l_hand",
                "l_lowerarm","l_lowerforearm","r_leg","l_leg","r_shin","l_shin","r_missile_pod","l_missile_pod","rotary_barrel",
                "pilot_flame","r_tank_vent","l_tank_vent","forehead_lens","barrel_ring","r_rocket_pod","l_rocket_pod",
                "r_banner","l_banner","tabard","taken_eye","taken_repulsor","taken_summon","taken_darkness","taken_tether",
                "taken_fire","eye_focus","r_jet","l_jet","taken_muzzle"})
            getAnyDescendantWithName(n).ifPresent(p->parts.put(n,p));
        for(int i=0;i<3;i++)wisps[i]=getAnyDescendantWithName("taken_wisp_"+i).orElse(null);
        if(kind==105){
            int i=0;
            for(String lane:new String[]{"front","hind"})for(String side:new String[]{"r_","l_"}){
                forelegs[i]=getAnyDescendantWithName(side+lane+"leg").orElseThrow();
                knees[i]=forelegs[i].getChild(side+lane+"shin");
                paws[i]=knees[i].getChild(side+lane+"foot");
                i++;
            }
        }
    }

    @Override public ModelPart root(){return root;}

    /** Only draw the pupil; the separate translucent pass animates the weaker ice-blue body markings. */
    public void renderPupil(PoseStack stack,VertexConsumer vertices) {
        if(eyeCore==null)return;
        stack.pushPose();
        root.translateAndRotate(stack);
        body.translateAndRotate(stack);
        body.getChild("head").translateAndRotate(stack);
        eyeSocket.translateAndRotate(stack);
        eyeCore.render(stack,vertices,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
        stack.popPose();
    }

    @Override public void setupAnim(FrontierMob e,float swing,float amount,float age,float yaw,float pitch){
        pose(swing,amount,age,yaw,pitch,e.move(),e.moveAge()+age-e.tickCount,e.isAggressive(),!e.isAddedToLevel());
    }

    public void pose(float swing,float amount,float age,float yaw,float pitch,int move,float t,boolean aiming,boolean preview) {
        if(kind==105){
            beast(swing,amount,age,yaw,pitch,move,t,preview);return;
        }
        if(cabal!=null) {
            cabal.pose(swing,amount,age,yaw,pitch,move,t,aiming,preview);
            if(kind==112){
                float impact=move==SHIELD?triangle(t,32,40,52):0;
                body.z-=impact*1.4F;
                body.xRot+=.075F*impact;
                effect("taken_repulsor",move==SHIELD?.38F+.62F*triangle(t,12,40,62):0);
                takenDrift(age,move);
            } else cabalEquipment(age,move,t);
            return;
        }
        if(captain!=null){
            int state=donorState(move);
            float left=CombatMoves.duration(state)-donorAge(move,t);
            captain.pose(swing,amount,age,yaw,pitch,state,left,aiming,0);
            float lift=move==CAST ? triangle(t,0,24,49) : 0;
            rotate("l_lowerarm",-1.08F*lift,-.28F*lift,.17F*lift);
            rotate("l_lowerforearm",-.69F*lift,0,0);
            effect("taken_darkness",move==CAST?triangle(t,8,26,36):0);
            takenDrift(age,move);return;
        }

        int state=donorState(move);
        // Taken Thralls have one gameplay contact per swipe at tick 14.
        // Only the normal Hive Thrall uses the two-hit combo from the previous release.
        combat.pose(swing,amount,age,yaw,pitch,state,CombatMoves.duration(state)-donorAge(move,t),aiming);
        if(kind==107){
            float strike=move==MELEE?triangle(t,6,14,24):0, curl=move==BLINK?triangle(t,0,11,21):0;
            body.xRot+=.16F*strike+.17F*curl;body.y+=1.3F*curl;
            rotate("r_arm",-.80F*strike-.35F*curl,.22F*strike,-.22F*curl);
            rotate("l_arm",-.38F*curl,-.16F*strike,.27F*curl);
            rotate("r_forearm",-.22F*strike,0,0);
        } else if(kind==108 || kind==111 || kind==109) {
            handCaster(age,move,t);
        } else if(kind==139){
            ogre(age,swing,amount,move,t);
        }
        takenDrift(age,move);
    }

    private static int donorState(int move){
        return switch(move){
            case FIRE->CombatMoves.FIRE;case MELEE->CombatMoves.MELEE;case BLINK->CombatMoves.BLINK;
            case STAGGER->CombatMoves.STAGGER;default->CombatMoves.IDLE;
        };
    }
    private static float donorAge(int move,float t){
        if(move==FIRE)return t<12?t:t<18?12+(t-12)*5/6:t<24?17+(t-18)*5/6:Math.min(27,22+t-24);
        if(move==MELEE)return Math.min(22,t*12/14);
        if(move==BLINK)return t*20/28;
        return t;
    }

    private void cabalEquipment(float age,int state,float t){
        float deploy=state==CAST?smooth(t/17)*smooth((52-t)/15):0;
        float kick=state==FIRE?Math.max(pulse(t,12,4),Math.max(pulse(t,18,4),pulse(t,24,4))):0;
        switch(kind){
            case 102 -> {
                if(state==CAST){
                    float charge=triangle(t,2,27,39);
                    rotate("l_arm",-1.22F*charge,-.33F*charge,.41F*charge);
                    rotate("l_forearm",-.68F*charge,0,0);
                    rotate("head",-.16F*charge,0,0);
                    scale("forehead_lens",1+.34F*charge);
                }
            }
            case 104,128 -> {
                String pod=kind==104?"_missile_pod":"_rocket_pod";
                float shot=state==CAST?pulse(t,28,6):0;
                for(String side:new String[]{"r","l"}){
                    float sign=side.equals("r")?-1:1;
                    rotate(side+pod,-.42F*deploy+.15F*shot,sign*.12F*deploy,0);
                    moveZ(side+pod,.75F*shot);
                }
                if(kind==104 && state==FIRE)rotate("rotary_barrel",0,0,Math.max(0,Math.min(24,t-4))*.65F);
                if(kind==128){
                    moveZ("barrel_ring",.90F*kick);
                    rotate("r_banner",.04F*sin(age*.10F),0,-.065F*deploy);
                    rotate("l_banner",.06F*sin(age*.10F+.6F),0,.065F*deploy);
                }
                body.xRot-=shot*.075F;
            }
            case 106 -> {
                float flame=state==FIRE?Math.max(pulse(t,12,5),Math.max(pulse(t,18,5),pulse(t,24,5))):
                            state==CAST?pulse(t,28,8):0;
                ModelPart pilot=parts.get("pilot_flame");
                if(pilot!=null){
                    pilot.zScale=.51F+.06F*sin(age*.26F)+flame*2.5F;
                    pilot.xScale=pilot.yScale=.63F+.42F*flame;
                    pilot.visible=true;
                }
                rotate("r_tank_vent",-.38F*flame,0,0);rotate("l_tank_vent",-.38F*flame,0,0);
            }
            case 130 -> {
                rotate("tabard",.045F*sin(age*.12F)+.12F*deploy,0,0);
                if(state==CAST){
                    // Enact the command and shield call on the cast bar.
                    float command=triangle(t,4,26,45);
                    rotate("l_arm",-1.18F*command,-.23F*command,.49F*command);
                    rotate("l_forearm",-.44F*command,0,0);
                    rotate("head",-.14F*command,0,0);
                }
            }
            default -> {}
        }
    }

    private void beast(float swing,float amount,float age,float yaw,float pitch,int move,float t,boolean preview){
        root.getAllParts().forEach(ModelPart::resetPose);
        float weight=Math.clamp(amount,0,1), phase=swing*.82F;
        float coil=move==LEAP?triangle(t,0,13,18):0;
        float air=move==LEAP?smooth((t-14)/5)*smooth((36-t)/9):0;
        float dive=move==LEAP?triangle(t,22,28,37):0;
        float bite=move==MELEE?triangle(t,5,14,24):dive;
        float hop=Math.abs(sin(phase))*.47F*weight;
        body.xRot+=.10F*weight*sin(phase*2)+.18F*coil-.26F*air+.51F*dive;
        body.y+=1.32F*coil-hop-(preview?4.8F*air:0);
        body.z-=1.3F*bite;
        rotate("head",pitch*DEG_TO_RAD*.68F-.19F*bite-.10F*coil,yaw*DEG_TO_RAD*.62F,0);
        rotate("jaw",-.57F*(move==MELEE?triangle(t,0,10,14):air)+.055F*sin(age*.13F),0,0);
        rotate("tail",.12F*weight-.24F*air,.21F*sin(age*.16F)+weight*.14F*sin(phase),0);
        rotate("tail_tip",.12F*air,.23F*sin(age*.16F-.7F),0);
        for(int i=0;i<4;i++){
            boolean front=i<2;
            float walk=sin(phase+(i==0||i==3?0:3.14159F)+(front?0:.28F))*weight;
            forelegs[i].xRot+=walk*.62F+(front?-.33F:.41F)*coil+(front?-.96F:.65F)*air;
            knees[i].xRot+=(front?.43F:-.58F)*Math.max(0,-walk)+(front?.61F:-.82F)*coil+(front?.49F:-.60F)*air;
            paws[i].xRot-=walk*.33F+(front?.14F:-.20F)*air;
        }
    }

    private void handCaster(float age,int state,float t){
        boolean active=kind==109?state==SHIELD:state==CAST;
        float lift=active ? (kind==109?smooth(t/12)*smooth((80-t)/12):triangle(t,4,26,50)) : 0;
        rotate("l_arm",-1.10F*lift,-.40F*lift,.22F*lift);
        rotate("l_forearm",-.42F*lift,0,0);
        if(kind==108){
            effect("taken_summon",active?triangle(t,4,27,47):0);
            if(active && t>=28)rotate("r_arm",-.12F*(pulse(t,28,4)+pulse(t,40,4)),0,0);
        }else if(kind==111){
            effect("taken_fire",active?triangle(t,8,27,39):0);
            body.y+=.76F*lift; rotate("head",.10F*lift,0,0);
        }else{
            effect("taken_tether",lift*(.80F+.12F*sin(age*.12F)));
            rotate("head",-.08F*lift,-.14F*lift,0);
        }
    }

    private void ogre(float age,float swing,float weight,int state,float t){
        float step=sin(swing*.61F)*Math.min(1,weight);
        body.zRot+=.065F*step;body.x+=step*.45F;
        rotate("r_arm",step*.13F,0,-.07F);rotate("l_arm",-step*.13F,0,.07F);
        float charge=state==FIRE?triangle(t,1,11,27):0;
        float kick=state==FIRE?Math.max(pulse(t,12,5),pulse(t,20,5)):0;
        scale("eye_focus",1+.20F*charge+.18F*kick);
        rotate("head",-.13F*kick,0,0);
        if(state==SLAM){
            float wind=triangle(t,0,20,26), hit=triangle(t,22,26,38);
            rotate("r_arm",-1.65F*wind+.26F*hit,.13F*wind,-.23F*wind);
            rotate("l_arm",-1.22F*wind+.24F*hit,-.12F*wind,.19F*wind);
            rotate("r_forearm",-.48F*wind,0,0);
            body.xRot+=-.22F*wind+.50F*hit;body.y+=hit*2.2F;
        }else if(state==CAST){
            float pulse=triangle(t,0,14,32);
            rotate("r_arm",-.57F*pulse,0,-.35F*pulse);rotate("l_arm",-.57F*pulse,0,.35F*pulse);
        }
    }

    private void takenDrift(float age,int state){
        float phase=age*.079F+kind*.47F;
        // Different periods for the eye and wisps keep the body readable while the surface moves.
        for(int i=0;i<wisps.length;i++){
            ModelPart p=wisps[i];
            if(p==null)continue;
            float bend=sin(phase+i*1.6F);
            p.xRot+=.14F*bend;p.zRot+=.15F*sin(phase*.81F+i*.9F);
            p.yScale=1+.095F*bend;
        }
        scale("taken_eye",1+.035F*sin(phase)+((kind==139&&state==FIRE)?.13F:0));
    }

    private void effect(String name,float strength){
        ModelPart p=parts.get(name);
        if(p!=null){p.visible=strength>.025F;p.xScale=p.yScale=p.zScale=.33F+.85F*strength;}
    }
    private void rotate(String n,float x,float y,float z){ModelPart p=parts.get(n);if(p!=null){p.xRot+=x;p.yRot+=y;p.zRot+=z;}}
    private void moveZ(String n,float z){ModelPart p=parts.get(n);if(p!=null)p.z+=z;}
    private void scale(String n,float s){ModelPart p=parts.get(n);if(p!=null)p.xScale=p.yScale=p.zScale=s;}
    private static float sin(float t){return (float)Math.sin(t);}
    private static float smooth(float t){float x=Math.clamp(t,0,1);return x*x*(3-2*x);}
    private static float pulse(float t,float center,float decay){return t<center?0:Math.max(0,1-(t-center)/decay);}
    private static float triangle(float t,float start,float peak,float end){return smooth((t-start)/(peak-start))*smooth((end-t)/(end-peak));}
}
