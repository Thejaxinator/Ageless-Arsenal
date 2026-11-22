package com.jax.destiny2mc.client.frontier;
import java.util.*;
import com.jax.destiny2mc.frontier.*;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
/** Animation sampler is also used by the shipped offline rig previews. */
public class FrontierModel extends HierarchicalModel<FrontierMob> {
 private final ModelPart root;private final FrontierCatalog.Unit unit;private final Map<String,ModelPart> parts=new HashMap<>();
 public FrontierModel(ModelPart root,FrontierCatalog.Unit u){this.root=root;unit=u;for(String n:new String[]{"body","head","r_arm","l_arm","r_forearm","l_forearm","r_hand","l_hand","r_leg","l_leg","r_shin","l_shin","r_wing","l_wing","r_wingtip","l_wingtip","guard","r_blade","l_blade","r_lowerarm","l_lowerarm","tail","r_weak","l_weak","ring","gun","r_frontleg","l_frontleg"})getAnyDescendantWithName(n).ifPresent(p->parts.put(n,p));}
 @Override public ModelPart root(){return root;}
 private void rot(String n,float x,float y,float z){ModelPart p=parts.get(n);if(p!=null){p.xRot+=x;p.yRot+=y;p.zRot+=z;}}
 private static float sin(float v){return (float)Math.sin(v);}
 @Override public void setupAnim(FrontierMob e,float swing,float amount,float age,float yaw,float pitch){pose(swing,amount,age,yaw,pitch,e.move(),e.moveAge()+age-e.tickCount,e.getTarget()!=null||e.isAggressive(),e.weakMask());}
 public void pose(float swing,float amount,float age,float yaw,float pitch,int move,float t,boolean aim,int weak){
  root.getAllParts().forEach(ModelPart::resetPose);float walk=sin(swing*.6662f)*Math.min(1,amount),breath=sin(age*.08f);ModelPart body=parts.get("body");
  rot("head",pitch*.0174533f,yaw*.0174533f,0);
  if(unit.flying()){if(body!=null)body.y-=.7f+.5f*breath;rot("r_leg",.22f,0,0);rot("l_leg",.22f,0,0);}else{
   rot("r_leg",walk*.7f,0,0);rot("l_leg",-walk*.7f,0,0);rot("r_shin",Math.max(0,-walk)*.75f,0,0);rot("l_shin",Math.max(0,walk)*.75f,0,0);
   rot("r_frontleg",-walk*.8f,0,0);rot("l_frontleg",walk*.8f,0,0);if(body!=null)body.y-=Math.abs(walk)*.4f;
  }
  rot("r_arm",-walk*.22f,0,-breath*.025f);rot("l_arm",walk*.22f,0,breath*.025f);rot("r_forearm",-.13f,0,0);rot("l_forearm",-.13f,0,0);
  boolean armed=parts.containsKey("gun");
  if(armed&&(aim||move==FrontierMob.FIRE)){float kick=move==FrontierMob.FIRE?Math.max(0,1-Math.abs((t-12)%6)/3)*.12f:0;rot("r_arm",-1.35f+pitch*.01f-kick,0,.08f);rot("r_forearm",-.15f,0,0);rot("r_hand",1.63f+kick,0,0);if(move!=FrontierMob.SHIELD){rot("l_arm",-1.15f+pitch*.008f,-.22f,-.35f);rot("l_forearm",-.35f,0,0);}}
  for(int sign:new int[]{-1,1}){String s=sign<0?"r":"l";float flap=sin(age*.32f);rot(s+"_wing",.08f*flap,sign*(.18f+.35f*flap),sign*.08f*flap);rot(s+"_wingtip",0,sign*.24f*sin(age*.32f-.7f),0);rot(s+"_lowerarm",-.4f-walk*.18f,0,sign*.12f);}
  rot("tail",0,sin(age*.12f)*.18f,0);rot("ring",0,age*.025f,0);
  if(parts.containsKey("l_weak"))parts.get("l_weak").visible=(weak&2)==0;
  if(parts.containsKey("r_weak"))parts.get("r_weak").visible=(weak&1)==0;
  switch(move){
   case FrontierMob.MELEE->{float strike=sin(Math.min(1,t/28)*(float)Math.PI);rot("r_arm",-1.7f+2.5f*strike,-.2f+.5f*strike,-.3f);rot("l_arm",-.7f+1.4f*strike,.2f,.2f);rot("body",.18f*strike,.22f*strike,0);}
   case FrontierMob.CAST->{float lift=sin(Math.min(1,t/60)*(float)Math.PI);rot("r_arm",-1.8f*lift,0,-.45f*lift);rot("l_arm",-1.8f*lift,0,.45f*lift);rot("head",-.2f*lift,0,0);}
   case FrontierMob.SHIELD->{rot("l_arm",-1.25f,0,-.35f);rot("l_forearm",-.45f,.15f,0);rot("guard",1.7f,0,.35f);rot("head",.15f,0,0);}
   case FrontierMob.BLINK->{rot("body",.2f,0,.05f*sin(t));rot("r_arm",-.5f,0,.2f);rot("l_arm",-.5f,0,-.2f);}
   case FrontierMob.LEAP->{float crouch=t<15?t/15:t<26?1:Math.max(0,(40-t)/14);rot("r_leg",-.9f*crouch,0,0);rot("l_leg",-.9f*crouch,0,0);rot("r_shin",1.4f*crouch,0,0);rot("l_shin",1.4f*crouch,0,0);rot("body",t<15?.25f:t<26?-.25f:.5f,0,0);rot("r_arm",-1.3f,0,-.4f);rot("l_arm",-1.3f,0,.4f);}
   case FrontierMob.SLAM->{float lift=t<30?Math.min(1,t/16):Math.max(0,(42-t)/12);rot("r_arm",-2.7f*lift,0,-.2f);rot("l_arm",-2.2f*lift,0,.2f);if(t>=30)rot("body",.5f*sin(Math.min(1,(t-30)/20)*(float)Math.PI),0,0);}
   case FrontierMob.CHARGE->{rot("body",.35f,0,0);rot("r_arm",-.6f,0,-.4f);rot("l_arm",.4f,0,.3f);rot("r_forearm",-.5f,0,0);}
   case FrontierMob.STAGGER->{rot("head",.6f,0,0);rot("body",.25f,0,.05f*sin(t*.8f));rot("r_arm",.4f,0,-.25f);rot("l_arm",.4f,0,.25f);}
  }
 }
}
