package com.jax.destiny2mc.client;

import com.jax.destiny2mc.combat.CombatMoves;
import com.jax.destiny2mc.entity.FallenMobEntity;
import net.minecraft.world.entity.Mob;
import com.jax.destiny2mc.frontier.FrontierMob;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class FallenStudyModel extends HierarchicalModel<Mob> {
    private final ModelPart root;
    private final int kind;
    private final Map<String,ModelPart> parts = new HashMap<>();
    private final Map<Mob,Float> sniperRelease = new WeakHashMap<>();

    public FallenStudyModel(ModelPart root, int kind) {
        this.root=root;
        this.kind=kind;
        for (String name : new String[]{"body","head","support_grip","sash","cloak","cape","r_arm","r_forearm","r_hand",
                "l_arm","l_forearm","l_hand","propulsion_core","arc_spear","gun_turret","cannon","gun","core",
                "shell_ring","engine_left","engine_right","left_stabilizer","right_stabilizer","hatch","r_frontleg","l_frontleg","r_frontshin","l_frontshin",
                "r_midleg","l_midleg","r_midshin","l_midshin","r_hindleg","l_hindleg","r_hindshin","l_hindshin","r_lowerarm","r_lowerforearm","r_lowerhand","l_lowerarm","l_lowerforearm",
                "l_lowerhand","r_leg","l_leg","r_shin","l_shin","r_foot","l_foot"}) {
            getAnyDescendantWithName(name).ifPresent(part -> parts.put(name,part));
        }
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(Mob entity, float swing, float amount, float age, float yaw, float pitch) {
        float partial=age-entity.tickCount;
        int state;
        float remaining;
        if(entity instanceof FrontierMob frontier) {
            float phase=frontier.moveAge()+partial;
            // Taniks also fires a three-shot volley during rematerialization.
            state=switch(frontier.move()) {
                case FrontierMob.BLINK -> phase>=18 ? CombatMoves.FIRE : CombatMoves.BLINK;
                case FrontierMob.FIRE -> CombatMoves.FIRE;
                case FrontierMob.MELEE -> CombatMoves.MELEE;
                case FrontierMob.STAGGER -> CombatMoves.STAGGER;
                default -> 0;
            };
            remaining=CombatMoves.duration(state)-phase;
        } else if(entity instanceof FallenMobEntity fallen) {
            state=fallen.getAnimState();remaining=fallen.getAnimTimer()-partial;
        } else return;
        // The sniper's legacy controller fires and clears state 1 in the same server tick.
        // Track the sighting countdown per entity so separate Vandals have separate follow-through.
        if (kind==2 && state==1) sniperRelease.put(entity,age+remaining-2);
        float rifleRecoil=0;
        Float release=sniperRelease.get(entity);
        if (release!=null) rifleRecoil=pulse(age-release,0,4);
        pose(swing,amount,age,yaw,pitch,state,remaining,entity.isAggressive(),rifleRecoil);
    }

    private static float sin(float a) {return (float)Math.sin(a);}
    private static float clamp(float x,float lo,float hi) {return Math.max(lo,Math.min(hi,x));}
    private static float pulse(float elapsed,float start,float duration) {
        return elapsed<start ? 0 : Math.max(0,1-(elapsed-start)/duration);
    }
    private void turn(String name,float x,float y,float z) {
        ModelPart part=parts.get(name);
        if(part!=null) {part.xRot+=x;part.yRot+=y;part.zRot+=z;}
    }

    public void pose(float swing,float amount,float age,float yaw,float pitch,int state,float remaining,
                     boolean aggressive,float rifleRecoil) {
        root.getAllParts().forEach(ModelPart::resetPose);
        if (kind==5 || kind==101 || kind==102 || kind==103 || kind==105 || kind==106 || kind==107 || kind==108) {
            machine(swing,amount,age,yaw,pitch,state,remaining);
            return;
        }
        ModelPart body=parts.get("body");
        float step=sin(swing*.6662F), weight=clamp(amount,0,1), breath=sin(age*.105F);
        float stride=step*weight, recovery=Math.abs((float)Math.cos(swing*.6662F))*weight;
        for(int sign:new int[]{-1,1}) {
            String side=sign<0?"r":"l";
            float forward=sign*stride;
            turn(side+"_leg",-.72F*forward,0,sign*.025F*weight);
            turn(side+"_shin",.55F*Math.max(0,forward),0,0);
            turn(side+"_foot",.20F*forward-.25F*Math.max(0,forward),0,0);
            turn(side+"_arm",.26F*forward,0,sign*.019F*breath);
            turn(side+"_forearm",-.035F-.035F*breath,0,0);
            turn(side+"_lowerarm",-.43F*forward-.045F*breath,0,sign*(.04F+.022F*breath));
            turn(side+"_lowerforearm",-.18F+.19F*forward,0,0);
            turn(side+"_lowerhand",0,-sign*.045F*breath,0);
        }
        body.y-=recovery*.55F+breath*.10F;
        turn("sash",.12F*recovery+.025F*breath,0,0);
        turn("cloak",.11F*recovery+.018F*breath,0,0);
        turn("cape",.11F*recovery+.018F*breath,0,0);
        float lookYaw=clamp((float)Math.toRadians(yaw),-1.0F,1.0F);
        float lookPitch=clamp((float)Math.toRadians(pitch),-.74F,.74F);
        turn("head",lookPitch,lookYaw,0);
        if(kind==1) {spear(age,state,remaining,lookYaw,lookPitch,aggressive); return;}
        if(kind==6) {bruiser(age,state,remaining); return;}
        if(kind==104 && state==3) {
            float cast=clamp(remaining/18,0,1);
            turn("l_arm",-1.66F*cast,-.32F,.28F);turn("l_forearm",-.36F*cast,0,0);
            turn("r_lowerarm",-.56F*cast,0,-.38F);turn("l_lowerarm",-.7F*cast,0,.32F);
            turn("cape",.22F*cast,0,0);return;
        }
        if(kind==104 && state==5) {
            turn("l_arm",-.60F,-.17F,.38F);turn("r_arm",-.43F,.22F,-.32F);
            turn("l_lowerarm",-.8F,0,.30F);turn("r_lowerarm",-.8F,0,-.30F);return;
        }
        boolean firing=state==1 || state==CombatMoves.FIRE;
        boolean precision=kind!=0;
        boolean aiming=firing || (state==0 && (aggressive || rifleRecoil>0));
        if(aiming) {
            float recoil=rifleRecoil;
            if(state==CombatMoves.FIRE) {
                float t=CombatMoves.duration(state)-remaining;
                if(kind==2) recoil=pulse(t,21,4);
                else if (kind==110) recoil=Math.max(pulse(t,12,4),Math.max(pulse(t,18,4),Math.max(pulse(t,22,3),pulse(t,24,4))));
                else recoil=Math.max(pulse(t,12,3),Math.max(pulse(t,17,3),pulse(t,22,3)));
            } else if (state==1 && kind==0) {
                float t=16-remaining;
                recoil=pulse(t,0,2.7F);
                // Two follow-up bolts are scheduled on multiples of five of the entity's tick count.
                float start=age-t;
                float first=(float)Math.floor(start/5+1)*5-start;
                recoil=Math.max(recoil,Math.max(pulse(t,first,2.7F),pulse(t,first+5,2.7F)));
            }
            if (state==1 && (kind==104 || kind==109)) recoil=pulse((kind==104?12:10)-remaining,0,4);
            aim(lookYaw,lookPitch,recoil,precision);
        } else if(state==2 || state==CombatMoves.BLINK) {
            float curl=.35F+.45F*sin((state==2 ? 14-remaining : 20-remaining)*.15F);
            body.y+=1.3F*curl;
            turn("head",.15F*curl,0,0);
            turn("r_arm",-.53F*curl,0,-.26F);
            turn("l_arm",-.50F*curl,0,.23F);
            turn("r_lowerarm",-.36F*curl,0,-.45F*curl);
            turn("l_lowerarm",-.36F*curl,0,.45F*curl);
            turn("r_leg",-.24F*curl,0,0);turn("l_leg",-.16F*curl,0,0);
            turn("r_shin",.40F*curl,0,0);turn("l_shin",.35F*curl,0,0);
        } else if (state==3) {
            // Dreg grenade exits its left hand at the beginning of the 16-tick recovery.
            float snap=clamp(remaining/16,0,1);
            turn("l_arm",-1.85F*snap,-.22F*snap,.15F*snap);
            turn("l_forearm",-.42F*snap,0,0);
        } else if (state==4 || state==CombatMoves.MELEE) {
            float t=state==4?12-remaining:22-remaining;
            float impact=state==4 ? pulse(t,0,10) : Math.max(0,1-Math.abs(t-12)/8);
            turn("l_arm",-1.66F*impact,-.31F*impact,.14F*impact);
            turn("r_arm",-.60F*impact,0,-.22F*impact);
            turn("r_lowerarm",-1.13F*impact,.21F,0);
            turn("l_lowerarm",-.60F*impact,-.18F,0);
        } else if (state==6 || state==CombatMoves.STAGGER) {
            turn("head",-.26F,0,.09F);turn("l_arm",.24F,0,.47F);turn("r_arm",.24F,0,-.47F);
            turn("l_lowerarm",.18F,0,.32F);turn("r_lowerarm",.18F,0,-.32F);
        }
    }

    private void machine(float swing,float amount,float age,float yaw,float pitch,int state,float remaining) {
        ModelPart body=parts.get("body");
        float weight=clamp(amount,0,1), scan=sin(age*.073F), gaze=clamp((float)Math.toRadians(yaw),-.80F,.80F);
        float elevation=clamp((float)Math.toRadians(pitch),-.58F,.58F);
        if(kind==101) {
            float stride=swing*.48F;
            float exposed=state==2?clamp((130-remaining)/8,0,1)*clamp(remaining/8,0,1):0;
            body.y+=2.3F*exposed+.20F*weight*Math.abs(sin(stride));
            int row=0;
            for(String pair:new String[]{"front","mid","hind"}) {
                for(int mirror:new int[]{-1,1}) {
                    String side=mirror<0?"r_":"l_";
                    float phase=stride+row*2.09F+(mirror<0?0:3.1416F);
                    float contact=sin(phase)*weight;
                    float lift=Math.max(0,contact);
                    turn(side+pair+"leg",contact*.29F,gaze*.06F+contact*mirror*.13F,mirror*(exposed*.22F+lift*.18F));
                    turn(side+pair+"shin",lift*.32F,0,-mirror*(exposed*.24F+lift*.19F));
                }
                row++;
            }
            turn("hatch",-.92F*exposed,0,0);
            float charge=state==3?clamp((24-remaining)/18,0,1):0;
            float kick=state==1?pulse(12-remaining,0,5):state==CombatMoves.FIRE?pulse(28-remaining,12,5):0;
            turn("gun_turret",0,gaze*(1-exposed),0);
            turn("cannon",elevation-.14F*kick+.055F*charge,0,0);
            ModelPart gun=parts.get("cannon");if(gun!=null)gun.z+=1.35F*kick;
            return;
        }
        boolean orb=kind==102 || kind==103 || kind==108;
        body.y-=.4F+scan*(orb?.40F:.23F);
        float kick=state==1?pulse(10-remaining,0,4):state==CombatMoves.FIRE?pulse(28-remaining,12,4):0;
        if(orb) {
            float guard=kind==102&&state==5?1:0, stagger=state==6?1:0;
            turn("core",elevation*.35F,gaze*.48F,.12F*scan);
            turn("shell_ring",0,0,age*(kind==103?.064F:.029F)*(1+guard*.40F));
            ModelPart core=parts.get("core");
            if(core!=null) {float swell=1+kick*.10F+guard*(.06F+.024F*scan)-.06F*stagger;core.xScale=core.yScale=core.zScale=swell;}
            turn("body",stagger*.20F,0,.037F*scan);
        } else {
            turn("body",weight*.13F,0,.045F*sin(age*.13F));
            turn("engine_left",.11F*scan,0,-.09F*scan);
            turn("engine_right",-.10F*scan,0,.09F*scan);
            turn("left_stabilizer",.11F*scan,0,-.035F*scan);
            turn("right_stabilizer",.11F*scan,0,.035F*scan);
            boolean turret=parts.containsKey("gun_turret");
            turn("gun_turret",0,gaze,0);turn("gun",elevation-.12F*kick,turret?0:gaze,0);
            turn("cannon",elevation-.10F*kick,0,0);
            ModelPart gun=parts.get("gun");
            if(gun!=null)gun.z+=kick*(kind==107?.95F:.36F);
        }
    }

    private void spear(float age,int state,float remaining,float yaw,float pitch,boolean alert) {
        boolean spin=state==3;
        boolean thrust=state==4 || state==CombatMoves.MELEE || state==CombatMoves.FIRE;
        float hit=state==4?pulse(11-remaining,0,7):state==CombatMoves.MELEE?Math.max(0,1-Math.abs(22-remaining-12)/7):Math.max(0,1-Math.abs(28-remaining-12)/7);
        if (spin) {
            float progress=clamp((14-remaining)/14,0,1);
            turn("body",0,(float)Math.PI*2*progress,0);
            aim(.94F,pitch*.30F,0,true,1.15F);
        } else if(thrust || (state==0 && alert) || state==1) {
            aim(yaw,pitch,0,true,thrust?2.55F*hit:0);
        } else if (state==2) {
            bodyDip(.65F);turn("l_leg",-.65F,0,0);turn("r_leg",.4F,0,0);
            aim(-.34F,-.29F,0,true,-.45F);
        } else if (state==6) {turn("head",-.30F,0,0); turn("r_arm",.29F,0,-.20F);}
    }

    private void bodyDip(float depth) {parts.get("body").y+=depth;}
    private void bruiser(float age,int state,float remaining) {
        if(state==5) {
            float overhead=remaining>7?clamp((14-remaining)/3,0,1):clamp(remaining/7,0,1);
            for(int s:new int[]{-1,1}) {
                String side=s<0?"r":"l";
                turn(side+"_arm",-2.23F*overhead,0,s*.31F);
                turn(side+"_lowerarm",-1.84F*overhead,0,s*.36F);
            }
            bodyDip((1-overhead)*.7F);
        } else if (state==2 || state==CombatMoves.BLINK) {
            float load=clamp(remaining/14,0,1);
            bodyDip(load*1.1F);turn("r_leg",-.47F*load,0,0);turn("l_leg",-.38F*load,0,0);
            turn("r_arm",-.91F*load,0,-.31F);turn("l_arm",-.91F*load,0,.31F);
            turn("propulsion_core",-.16F*load,0,0);
        } else if(state==4 ||state==CombatMoves.MELEE||state==CombatMoves.FIRE) {
            float phase=state==4?12-remaining:CombatMoves.duration(state)-remaining;
            for(int s:new int[]{-1,1}) {
                String side=s<0?"r":"l";float sw=sin(phase*.76F+s*.9F);
                turn(side+"_arm",-1.0F-.53F*sw,0,s*.23F);
                turn(side+"_lowerarm",-.84F+.72F*sw,0,s*-.20F);
                turn(side+"_lowerforearm",-.31F-.18F*sw,0,0);
            }
        } else if(state==6){turn("head",-.30F,0,.1F);turn("l_arm",.2F,0,.5F);turn("r_arm",.2F,0,-.5F);}
    }
    private void aim(float yaw,float pitch,float recoil,boolean support) {aim(yaw,pitch,recoil,support,0);}
    private void aim(float yaw,float pitch,float recoil,boolean support,float extension) {
        ModelPart shoulder=parts.get("r_arm"), opposite=parts.get("l_arm");
        Vector3f grip=new Vector3f(shoulder.x*.24F,shoulder.y+.28F,shoulder.z-(support?3.25F:5.5F)+recoil*.75F-extension);
        Quaternionf gunRotation=new Quaternionf().rotationYXZ(yaw*.48F,pitch-recoil*.075F,0);
        // Rotate the forward reach about the shoulder so the muzzle and eyes follow the same aim.
        Vector3f reach=new Vector3f(grip).sub(shoulder.x,shoulder.y,shoulder.z);
        gunRotation.transform(reach);
        grip.set(reach).add(shoulder.x,shoulder.y,shoulder.z);
        Vector3f supportOffset=null;
        if (support) {
            ModelPart marker=parts.get("support_grip");
            supportOffset=marker==null ? new Vector3f(.15F,-.40F,-3.25F) : new Vector3f(marker.x,marker.y,marker.z);
            gunRotation.transform(supportOffset);
            // Project the main wrist into both arms' reach spheres, preserving the offset between grips.
            Vector3f otherCenter=new Vector3f(opposite.x,opposite.y,opposite.z).sub(supportOffset);
            Vector3f nearCenter=new Vector3f(shoulder.x,shoulder.y,shoulder.z);
            float r=reachLength("r")-.03F,l=reachLength("l")-.03F;
            for(int i=0;i<4;i++){limitReach(grip,otherCenter,l);limitReach(grip,nearCenter,r);}
        }
        solve("r",grip,-1);
        ModelPart wrist=parts.get("r_hand");
        Quaternionf armQ=rotation(shoulder).mul(rotation(parts.get("r_forearm")));
        orient(wrist,armQ.invert().mul(gunRotation));
        if (support) solve("l",new Vector3f(grip).add(supportOffset),1);
        turn("r_lowerarm",-.24F,0,-.10F);
        turn("l_lowerarm",-.35F,0,.13F);
        turn("head",.015F,0,support?-.08F:0);
    }

    private static Vector3f offset(ModelPart p) { return new Vector3f(p.x,p.y,p.z); }
    private float reachLength(String side) {
        return offset(parts.get(side+"_forearm")).length()+offset(parts.get(side+"_hand")).length();
    }
    private static void limitReach(Vector3f point,Vector3f center,float radius) {
        Vector3f delta=new Vector3f(point).sub(center);
        if (delta.lengthSquared()>radius*radius) point.set(center).add(delta.normalize(radius));
    }
    private static Quaternionf rotation(ModelPart p) {
        return new Quaternionf().rotationZYX(p.zRot,p.yRot,p.xRot);
    }
    private static void orient(ModelPart p,Quaternionf q) {
        q.normalize();
        float x=q.x,y=q.y,z=q.z,w=q.w;
        p.xRot=(float)Math.atan2(2*(w*x+y*z),1-2*(x*x+y*y));
        p.yRot=(float)Math.asin(clamp(2*(w*y-z*x),-1,1));
        p.zRot=(float)Math.atan2(2*(w*z+x*y),1-2*(y*y+z*z));
    }

    private void solve(String side,Vector3f target,int sign) {
        ModelPart arm=parts.get(side+"_arm"),forearm=parts.get(side+"_forearm"),hand=parts.get(side+"_hand");
        Vector3f upper=offset(forearm),lower=offset(hand);
        float a=upper.length(),b=lower.length();
        if(a<.001F || b<.001F)return;
        Vector3f ray=new Vector3f(target).sub(arm.x,arm.y,arm.z);
        if(ray.lengthSquared()<.0001F)ray.set(0,0,-1);
        float span=clamp(ray.length(),Math.abs(a-b)+.01F,a+b-.01F);
        ray.normalize();
        float along=(a*a-b*b+span*span)/(2*span);
        float rise=(float)Math.sqrt(Math.max(0,a*a-along*along));
        Vector3f bend=new Vector3f(sign*.70F,1,.12F);
        bend.sub(new Vector3f(ray).mul(ray.dot(bend)));
        if (bend.lengthSquared()<.0001F) bend.set(1,0,0).cross(ray);
        Vector3f elbow=new Vector3f(ray).mul(along).add(bend.normalize().mul(rise));
        Quaternionf top=new Quaternionf().rotationTo(upper,elbow);
        orient(arm,top);
        Vector3f second=new Vector3f(ray).mul(span).sub(elbow);
        top.conjugate().transform(second);
        orient(forearm,new Quaternionf().rotationTo(lower,second));
    }
}
