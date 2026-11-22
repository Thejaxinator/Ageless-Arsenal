package com.jax.destiny2mc.client.frontier;

import com.jax.destiny2mc.frontier.FrontierMob;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Heavy infantry share a pose sampler across the three armor studies. Coordinates are native model pixels. */
public final class CabalStudyModel extends HierarchicalModel<FrontierMob> {
    private final int kind;
    private final ModelPart root, body, head, gun, shield, rightJet, leftJet, rightFlame, leftFlame;
    private final Arm right, left;
    private final Leg rightLeg, leftLeg;
    private final Vector3f foregrip;

    public CabalStudyModel(ModelPart root, int kind) {
        this.root = root;
        this.kind = kind;
        body = root.getChild("body");
        head = body.getChild("head");
        right = new Arm(body, "r", -1);
        left = new Arm(body, "l", 1);
        rightLeg = Leg.fromBody(body, "r");
        leftLeg = Leg.fromBody(body, "l");
        gun = right.wrist.getChild("gun");
        shield = kind == 101 ? left.wrist.getChild("shield") : null;
        if (kind == 101) {
            foregrip = null;
        } else if (right.wrist.hasChild("support_grip")) {
            foregrip = position(right.wrist.getChild("support_grip"));
        } else {
            foregrip = rotation(gun).transform(position(gun.getChild("support_grip"))).add(position(gun));
        }
        rightJet = optional("r_jet"); leftJet = optional("l_jet");
        rightFlame = optional("r_exhaust"); leftFlame = optional("l_exhaust");
    }

    private ModelPart optional(String name) {
        return getAnyDescendantWithName(name).orElse(null);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(FrontierMob mob, float swing, float amount, float age, float yaw, float pitch) {
        pose(swing, amount, age, yaw, pitch, mob.move(), mob.moveAge() + age - mob.tickCount,
             mob.isAggressive(), !mob.isAddedToLevel());
    }

    public void pose(float swing, float amount, float age, float yaw, float pitch, int state,
                     float actionAge, boolean alert, boolean preview) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float breath = sin(age*.083F), weight = clamp(amount, 0, 1);
        float beat = swing*.60F, step = sin(beat)*weight;
        float sway = sin(beat+.43F)*weight;
        float gaze = clamp((float)Math.toRadians(yaw), -.90F, .90F);
        float elevation = clamp((float)Math.toRadians(pitch), -.72F, .72F);
        float brace = shield != null && state == FrontierMob.SHIELD
            ? .50F+.50F*smooth(actionAge/7)*smooth((80-actionAge)/5) : 0;
        float strike = state == FrontierMob.MELEE ? triangle(actionAge, 8, 14, 23) : 0;
        float windup = state == FrontierMob.MELEE ? triangle(actionAge, 0, 7, 14) : 0;
        boolean leap = kind == 103 && state == FrontierMob.LEAP;
        float load = leap ? triangle(actionAge, 0, 12, 17) : 0;
        float air = leap ? smooth((actionAge-14)/5)*smooth((39-actionAge)/10) : 0;
        float landing = leap ? triangle(actionAge, 27, 36, 45) : 0;

        rightLeg.stride(step, weight, brace, load, air, landing, -.20F);
        leftLeg.stride(-step, weight, brace, load, air, landing, .42F);
        body.y += weight*.77F*(1-Math.abs(sin(beat))) + breath*.11F + 1.15F*brace
                + 2.45F*load + 1.45F*landing - (preview ? 5.1F*air : 0);
        body.x += sway*.33F;
        body.xRot += weight*.048F + .10F*brace + .16F*load - .12F*air + .18F*strike;
        body.yRot += gaze*.20F + sway*.055F + (shield == null ? -.17F : .23F)*strike;
        body.zRot += sway*.035F - .055F*brace;
        head.xRot += elevation - body.xRot*.68F + .07F*brace;
        head.yRot += gaze*.80F;
        head.zRot += -.68F*body.zRot;
        float ignition = leap ? smooth((actionAge-12)/3)*smooth((36-actionAge)/8) : 0;
        jets(rightJet, rightFlame, ignition, breath, age, 0);
        jets(leftJet, leftFlame, ignition, breath, age, 1.4F);

        boolean fire = state == FrontierMob.FIRE;
        float recoil = fire ? volley(actionAge,12,18,24) : leap ? volley(actionAge,21,28,35) : 0;
        if (state == FrontierMob.CAST) recoil = pulse(actionAge,28);
        float ready = fire ? .64F+.36F*smooth(actionAge/7) : state == FrontierMob.CAST ? .74F : alert ? .74F : .12F;
        ready = Math.max(ready, .85F*air);
        float gunYaw = mix(-.66F, gaze-body.yRot, ready) - .10F*windup;
        float gunPitch = mix(.30F, elevation-body.xRot, ready) - recoil*.095F - .16F*windup;
        Quaternionf desired = new Quaternionf().rotationYXZ(gunYaw,gunPitch,-.11F*(1-ready)+sway*.025F);
        Quaternionf wristRotation = desired.mul(rotation(gun).conjugate(), new Quaternionf());

        // Carry low across the chest; bring the stock up and back against the firing shoulder.
        Vector3f point = new Vector3f(right.shoulder.x*mix(.57F,.42F,ready), right.shoulder.y+mix(6.0F,1.65F,ready),
                                      right.shoulder.z-mix(4.4F,3.8F,ready)+.93F*recoil);
        point.y += .20F*breath + sway*.18F + .8F*brace;
        point.z += windup*.85F - (shield==null ? strike*2.45F : 0);
        Vector3f support = foregrip == null ? null : wristRotation.transform(new Vector3f(foregrip));
        if (support != null) {
            Vector3f otherCenter = position(left.shoulder).sub(support);
            Vector3f nearCenter = position(right.shoulder);
            for (int i=0; i<12; i++) {
                if (point.distanceSquared(otherCenter)<=left.reach*left.reach &&
                    point.distanceSquared(nearCenter)<=right.reach*right.reach) break;
                withinReach(point,otherCenter,left.reach);
                withinReach(point,nearCenter,right.reach);
            }
        }
        right.reach(point);
        right.orientHand(wristRotation);
        if (support != null) {
            left.reach(point.add(support));
            left.orientHand(wristRotation);
        } else {
            setShield(gaze,elevation,brace,windup,strike,step);
        }
    }

    private void setShield(float gaze,float lookPitch,float brace,float windup,float strike,float step) {
        Vector3f anchor = new Vector3f(left.shoulder.x*mix(.94F,.36F,brace), left.shoulder.y+mix(5.6F,2.65F,brace),
                                       left.shoulder.z-mix(3.25F,6.15F,brace));
        anchor.y += step*.50F + windup*.5F;
        anchor.z += windup*1.15F - strike*3.4F;
        Quaternionf board = new Quaternionf().rotationYXZ(mix(-.68F,-.04F,brace)+gaze*.33F, lookPitch*.20F-.12F*strike,
                                                              mix(.14F,-.025F,brace)-windup*.12F);
        left.reach(anchor);
        left.orientHand(board.mul(rotation(shield).conjugate()));
    }

    private static void jets(ModelPart nozzle,ModelPart flame,float ignition,float idle,float age,float phase) {
        if(nozzle!=null) nozzle.xRot -= ignition*.16F;
        if(flame==null) return;
        flame.visible = ignition>.008F;
        flame.xScale = flame.zScale = .70F+.30F*ignition;
        flame.yScale = .12F+ignition*(.90F+.13F*sin(age*1.1F+phase)+idle*.025F);
    }

    private record Leg(ModelPart thigh, ModelPart shin, ModelPart foot) {
        static Leg fromBody(ModelPart body,String side) {
            ModelPart thigh = body.getChild(side+"_leg");
            ModelPart shin = thigh.getChild(side+"_shin");
            return new Leg(thigh,shin,shin.getChild(side+"_foot"));
        }
        void stride(float step,float weight,float brace,float load,float air,float landing,float offset) {
            float swing = Math.max(0, step);
            thigh.xRot += step*.62F - .22F*brace - .52F*load - air*(.75F+offset) - .31F*landing;
            shin.xRot += swing*.66F + .13F*weight + .42F*brace + .90F*load + air*(1.08F-offset*.45F) + .59F*landing;
            foot.xRot += -.55F*step-.56F*swing - .20F*brace - .38F*load + air*.26F - .26F*landing;
        }
    }

    /** Two-link inverse kinematics in torso coordinates. Limb lengths come from each style's bind pose. */
    private static final class Arm {
        final ModelPart shoulder, elbow, wrist;
        final Vector3f upper, lower;
        final float reach, upperLength, lowerLength, side;
        Arm(ModelPart body,String prefix,float side) {
            this.side = side;
            shoulder=body.getChild(prefix+"_arm");
            elbow=shoulder.getChild(prefix+"_forearm");
            wrist=elbow.getChild(prefix+"_hand");
            upper=position(elbow); lower=position(wrist);
            upperLength=upper.length(); lowerLength=lower.length(); reach=upperLength+lowerLength-.015F;
        }
        void reach(Vector3f target) {
            Vector3f ray = new Vector3f(target).sub(position(shoulder));
            float span = clamp(ray.length(),Math.abs(upperLength-lowerLength)+.015F,reach);
            ray.normalize();
            float along=(upperLength*upperLength-lowerLength*lowerLength+span*span)/(2*span);
            float rise=(float)Math.sqrt(Math.max(0,upperLength*upperLength-along*along));
            Vector3f bend = new Vector3f(side*.63F,1,.08F);
            bend.sub(new Vector3f(ray).mul(ray.dot(bend)));
            if(bend.lengthSquared()<.00001F) bend.set(1,0,0).cross(ray);
            Vector3f toElbow = new Vector3f(ray).mul(along).add(bend.normalize(rise));
            Quaternionf shoulderRotation = new Quaternionf().rotationTo(upper,toElbow);
            orient(shoulder,shoulderRotation);
            Vector3f remaining = new Vector3f(ray).mul(span).sub(toElbow);
            shoulderRotation.conjugate().transform(remaining);
            orient(elbow,new Quaternionf().rotationTo(lower,remaining));
        }
        void orientHand(Quaternionf target) {
            orient(wrist,rotation(shoulder).mul(rotation(elbow)).conjugate().mul(target));
        }
    }

    private static void withinReach(Vector3f point,Vector3f center,float radius) {
        Vector3f delta = new Vector3f(point).sub(center);
        if(delta.lengthSquared()>radius*radius) point.set(center).add(delta.normalize(radius));
    }
    private static void orient(ModelPart p,Quaternionf q) {
        q.normalize();
        float x=q.x,y=q.y,z=q.z,w=q.w;
        p.xRot=(float)Math.atan2(2*(w*x+y*z),1-2*(x*x+y*y));
        p.yRot=(float)Math.asin(clamp(2*(w*y-z*x),-1,1));
        p.zRot=(float)Math.atan2(2*(w*z+x*y),1-2*(y*y+z*z));
    }
    private static Vector3f position(ModelPart p) {return new Vector3f(p.x,p.y,p.z);}
    private static Quaternionf rotation(ModelPart p) {return new Quaternionf().rotationZYX(p.zRot,p.yRot,p.xRot);}
    private static float clamp(float x,float low,float high){return Math.max(low,Math.min(high,x));}
    private static float sin(float t){return (float)Math.sin(t);}
    private static float mix(float a,float b,float t){return a+(b-a)*t;}
    private static float smooth(float t){float x=clamp(t,0,1);return x*x*(3-2*x);}
    private static float triangle(float t,float start,float peak,float end){return smooth((t-start)/(peak-start))*smooth((end-t)/(end-peak));}
    private static float pulse(float t,float contact){return t<contact?0:Math.max(0,1-(t-contact)/3.1F);}
    private static float volley(float t,int a,int b,int c){return Math.max(pulse(t,a),Math.max(pulse(t,b),pulse(t,c)));}
}
