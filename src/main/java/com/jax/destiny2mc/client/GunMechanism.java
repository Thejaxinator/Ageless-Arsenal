package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.frontier.FrontierWeaponInput;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class GunMechanism {
    enum Action {SPIN,SLIDE,BOLT,LATCH,CHARGE,HAMMER,LEVER,PUMP,DRAW}
    enum Load {CELL,CYLINDER,BREAK,GATE,PORT,NOCK}
    private static final ModelResourceLocation[][] MODELS=new ModelResourceLocation[WeaponSpec.ALL.length][4];
    private static final String[] PIECES={"body","action","load","lower_string"};
    private static float rotorAngle;
    private static double lastRotorTime;
    @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event) {
        for(var spec:WeaponSpec.ALL) for(int n=0;n<PIECES.length;n++){
            if(n==3 && !spec.id.equals("le_monarque"))continue;
            var id=ResourceLocation.fromNamespaceAndPath("destiny2mc","armory/"+spec.id+"/"+PIECES[n]);
            MODELS[spec.index][n]=ModelResourceLocation.standalone(id);
            event.register(MODELS[spec.index][n]);
        }
    }
    public static float progress(ItemStack stack,WeaponSpec spec,long now,float partial) {
        long end=GunDataHelper.getLong(stack,"d2_rel_end");
        if(end<=now)return 0;
        int duration=Math.max(1,GunDataHelper.getInt(stack,"d2_rel_dur",spec.reloadTicks));
        return Mth.clamp(1-((float)(end-now)-partial)/duration,0,1);
    }
    static float envelope(float t){return Mth.sin(t*(float)Math.PI);}
    private static void pivot(PoseStack p,float y,float z,Axis axis,float angle){
        p.translate(0,(y-8)/16,z);p.mulPose(axis.rotationDegrees(angle));p.translate(0,(8-y)/16,-z);
    }
    private static void piece(Minecraft mc,PoseStack p, MultiBufferSource out,ItemStack stack,int id,int n,int light){
        var model=mc.getModelManager().getModel(MODELS[id][n]);
        mc.getItemRenderer().render(stack,ItemDisplayContext.NONE,false,p,out,light,OverlayTexture.NO_OVERLAY,model);
    }
    // Shear the cord from its fixed cam to the moving nock. The arrow follows the same Z displacement.
    private static void drawString(PoseStack p,int side,float charge){
        float tip=side*12.25f/16;
        p.translate(0,tip,0);
        var stretch=new org.joml.Matrix4f().m12(-side*.18f*charge/(12.25f/16));
        p.mulPose(stretch);
        p.translate(0,-tip,0);
    }
    public static void render(PoseStack p,MultiBufferSource out,ItemStack stack,WeaponSpec spec,int light,float reload,float partial){
        var pose=localPose(spec,reload,partial);
        renderPose(p,out,stack,spec,light,pose);
    }
    static MechanismPose localPose(WeaponSpec spec,float reload,float partial){
        var mc=Minecraft.getInstance();
        float impulse=Mth.clamp((ClientWeaponState.muzzleFlashTicks-partial)/2f,0,1);
        float charge=FrontierWeaponInput.duration>0?Mth.clamp((float)FrontierWeaponInput.charge/FrontierWeaponInput.duration,0,1):0;
        float stroke=Math.max(impulse,Math.min(1,ClientWeaponState.getSmoothRecoil(partial)*.50f));
        float cooldown=Mth.clamp((ClientWeaponState.fireCooldown-partial)/Math.max(1f,spec.fireDelayTicks),0,1);
        if(SightFit.ACTION[spec.index]==Action.SPIN){
            double clock=mc.player.tickCount+(double)partial;
            double delta=Mth.clamp(clock-lastRotorTime,0,2);
            rotorAngle=(rotorAngle+(float)delta*46*ClientWeaponState.spinUp)%360;
            lastRotorTime=clock;
        }
        return new MechanismPose(reload,stroke,(float)Math.sin(cooldown*Math.PI),charge,rotorAngle);
    }
    public static void renderPose(PoseStack p,MultiBufferSource out,ItemStack stack,WeaponSpec spec,int light,MechanismPose pose){
        var mc=Minecraft.getInstance();
        int i=spec.index;
        float reload=pose.reload(),stroke=pose.stroke(),cycle=pose.cycle(),charge=pose.charge();
        float r=envelope(reload),length=SightFit.LENGTH[i];
        piece(mc,p,out,stack,i,0,light);
        p.pushPose();
        switch(SightFit.ACTION[i]){
            case SPIN -> pivot(p,8.7f,0,Axis.ZP,pose.rotorAngle());
            case HAMMER -> pivot(p,9.7f,(20.2f-12)*length/384,Axis.XP,-31*stroke);
            case SLIDE -> p.translate(0,0,stroke*.115f);
            case BOLT -> p.translate(0,0,.12f*stroke+.08f*r);
            case LATCH -> p.translate(.015f*stroke,0,.09f*stroke);
            case CHARGE -> p.translate(0,0,-.105f*charge+.065f*stroke);
            case PUMP -> p.translate(0,0,.145f*cycle);
            case LEVER -> pivot(p,6.8f,(15-12)*length/384,Axis.XP,-41*cycle);
            case DRAW -> drawString(p,1,charge);
        }
        int actionLight=light;
        if(SightFit.ACTION[i]==Action.CHARGE) {
            int coreLight=Math.round(15*Math.max(charge,stroke));
            actionLight=LightTexture.pack(Math.max(LightTexture.block(light),coreLight),LightTexture.sky(light));
        }
        piece(mc,p,out,stack,i,1,actionLight);p.popPose();
        if(SightFit.ACTION[i]==Action.DRAW){p.pushPose();drawString(p,-1,charge);piece(mc,p,out,stack,i,3,light);p.popPose();}
        p.pushPose();
        switch(SightFit.LOAD[i]){
            case CELL -> {p.translate(-.045f*r,-.31f*r,0);p.mulPose(Axis.ZP.rotationDegrees(7*r));}
            case CYLINDER -> {p.translate(-.19f*r,0,0);pivot(p,8.53f,0,Axis.ZP,360*reload*reload*(3-2*reload));}
            case BREAK -> pivot(p,7.6f,(13.1f-12)*length/384,Axis.XP,-33*r);
            case GATE -> pivot(p,7.2f,(13.5f-12)*length/384,Axis.XP,47*r);
            case PORT -> p.translate(0,0,.23f*r);
            case NOCK -> p.translate(0,-.16f*r,.18f*charge+.11f*r);
        }
        piece(mc,p,out,stack,i,2,light);p.popPose();
    }
}
