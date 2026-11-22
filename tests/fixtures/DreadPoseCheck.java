package com.jax.destiny2mc.client.ageless;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import com.jax.destiny2mc.client.frontier.FrontierLayers;
import com.jax.destiny2mc.client.frontier.FrontierModel;
import com.jax.destiny2mc.frontier.FrontierCatalog;
import com.jax.destiny2mc.frontier.FrontierMob;

/** Run with Java 21 and the development classpath. Checks the actual shipped baked model joints. */
public final class DreadPoseCheck {
    private static void check(boolean v,String msg){if(!v)throw new AssertionError(msg);}
    private static ModelPart bone(FrontierModel m,String name){return m.getAnyDescendantWithName(name).orElseThrow();}
    private static void pose(DreadMotion motion,int state,int time,int mask){motion.sample(0,0,time,0,0,state,time,false,mask);}
    public static void main(String[] args){
        int count=0;
        for(var unit:FrontierCatalog.UNITS){
            if(!unit.faction().equals("dread"))continue;
            var root=FrontierLayers.create(unit.kind()).bakeRoot();
            var model=new FrontierModel(root,unit);
            var motion=new DreadMotion(model,unit.kind());
            for(int state=0;state<10;state++)for(int t=0;t<=60;t+=3){
                motion.sample(t,.82f,t,-29,21,state,t,true,0);
                check(root.getAllParts().allMatch(p->Float.isFinite(p.xRot)&&Float.isFinite(p.yRot)&&Float.isFinite(p.zRot)),unit.id());
            }
            if (unit.kind()==121){
                pose(motion,FrontierMob.MELEE,14,0);float right=bone(model,"r_arm").xRot,left=bone(model,"l_arm").xRot;
                pose(motion,FrontierMob.MELEE,22,0);
                check(right<bone(model,"r_arm").xRot-1.0f&&left>bone(model,"l_arm").xRot+1.0f,"Husk blade hit timing");
            }
            if(unit.kind()==120||unit.kind()==138){
                pose(motion,0,0,0);float rest=bone(model,"r_wing").zRot;
                pose(motion,0,11,0);check(Math.abs(bone(model,"r_wing").zRot-rest)>.03f,"wing missed motion");
            }
            if(unit.kind()==126||unit.kind()==138){
                for(int mask=0;mask<=3;mask++){
                    pose(motion,FrontierMob.STAGGER,16,mask);
                    check(bone(model,"r_weak").visible==((mask&1)==0)&&bone(model,"l_weak").visible==((mask&2)==0),"weak mask "+mask);
                }
            }
            if(unit.kind()==137){
                pose(motion,0,17,0);float rest=bone(model,"l_leg").xRot;pose(motion,FrontierMob.CHARGE,17,0);
                check(rest-bone(model,"l_leg").xRot>1.4f,"Rhulk kick absent");
            }
            System.out.println(unit.id()+" / 210 pose samples / pass");count++;
        }
        check(count==10,"Dread registry count "+count);
    }
}
