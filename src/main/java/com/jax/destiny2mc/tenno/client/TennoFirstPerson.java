package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.FirstPersonWeaponHands;
import com.jax.destiny2mc.client.gunfeel.SniperScopeOverlay;
import com.jax.destiny2mc.client.gunfeel.WeaponSwayController;
import com.jax.destiny2mc.tenno.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class TennoFirstPerson {
    private static final ModelResourceLocation[][] PARTS=new ModelResourceLocation[TennoGunSpec.values().length][3];
    @EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Models {
        @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event){
            for(var spec:TennoGunSpec.values()){
                int n=0;
                for(String part:new String[]{"body","action","load"}){
                    var key=ModelResourceLocation.standalone(TennoGunRegistration.id("armory/"+spec.id()+"/"+part));
                    PARTS[spec.ordinal()][n++]=key;event.register(key);
                }
            }
        }
    }
    @SubscribeEvent public static void hand(RenderHandEvent event) {
        var player=Minecraft.getInstance().player;
        if(player==null||!(player.getMainHandItem().getItem() instanceof TennoGunItem gun))return;
        var spec=gun.profile();var stack=player.getMainHandItem();float partial=event.getPartialTick();
        float ads=TennoGunClient.aim(partial);
        if(event.getHand()==InteractionHand.OFF_HAND){if(spec.dual()||ads>.15)event.setCanceled(true);return;}
        event.setCanceled(true);
        long end=TennoGunItem.reloadUntil(stack),now=player.level().getGameTime();
        float progress=end>now?Mth.clamp(1-(end-now-partial)/TennoGunStats.of(player,stack).reloadTicks(),0,1):0;
        float load=Mth.sin(progress*(float)Math.PI);float kick=Mth.lerp(partial,TennoGunClient.previousKick,TennoGunClient.kick);
        var side=player.getMainArm();
        render(event,player,stack,spec,side,ads,load,kick,partial,true);
        if(spec.dual())render(event,player,stack,spec,side.getOpposite(),ads,load,kick,partial,false);
    }
    private static void render(RenderHandEvent e,LocalPlayer player,ItemStack stack,TennoGunSpec spec,HumanoidArm side,float ads,float load,float kick,float partial,boolean stepSway){
        var mc=Minecraft.getInstance();var pose=e.getPoseStack();int sign=side==HumanoidArm.RIGHT?1:-1;
        boolean dual=spec.dual();boolean main=side==player.getMainArm();
        if(dual && spec.family!=TennoGunSpec.Family.TWINS && (main?1:-1)!=TennoGunClient.muzzleSide)kick*=.1f;
        float scale=dual?.72f:.77f;
        float yAim=-(sightY(spec)-8)/16*scale;
        pose.pushPose();
        try{
            if(stepSway){
                float w=spec.family==TennoGunSpec.Family.PERIGALE?1.22f:(dual?0.80f:1.0f);
                WeaponSwayController.applySway(pose,player,ads,load,partial,sign,w);
            }
            if(SniperScopeOverlay.shouldHideFirstPersonModel(stack,ads))return;

            float ax=WeaponSwayController.adsX(ads);
            float ay=WeaponSwayController.adsY(ads);
            float az=WeaponSwayController.adsZ(ads);
            float ar=WeaponSwayController.adsRot(ads);

            pose.translate(sign*Mth.lerp(ax,.35f,dual?.22f:0),Mth.lerp(ay,-.33f,dual?-.28f:yAim)-load*.1f,
                Mth.lerp(az,-.97f,-.86f)+kick*.055f);
            pose.mulPose(Axis.YP.rotationDegrees(sign*(dual?12:15)*(1-ar)));
            pose.mulPose(Axis.XP.rotationDegrees(kick*(dual?4.8f:2.6f)+2*(1-ar)));
            pose.mulPose(Axis.ZP.rotationDegrees(sign*load*20));pose.scale(scale,scale,scale);
            var out=e.getMultiBufferSource();int light=e.getPackedLight();
            if(!player.isInvisible()&&!player.isSpectator()&&mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer){
                FirstPersonWeaponHands.arm(pose,out,renderer,player,side,light,sign*.045f,-.16f,.24f,sign*23,-58,sign*8);
                if(!dual&&player.getOffhandItem().isEmpty())FirstPersonWeaponHands.arm(pose,out,renderer,player,side.getOpposite(),light,-sign*.07f,-.13f-load*.22f,-.19f+load*.14f,-sign*32,-57,-sign*13);
            }
            if(mc.getModelManager().getModel(PARTS[spec.ordinal()][0])==mc.getModelManager().getMissingModel()) {
                mc.getItemRenderer().renderStatic(stack,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,out,player.level(),0);return;
            }
            piece(pose,out,stack,spec,0,light);
            pose.pushPose();pose.translate(0,0,kick*.075f+load*.025f);piece(pose,out,stack,spec,1,light);pose.popPose();
            pose.pushPose();
            if(spec.family==TennoGunSpec.Family.BUBONICO)pose.scale(1+kick*.05f,1+kick*.04f,1);
            else{pose.translate(-sign*.035f*load,-.28f*load,0);pose.mulPose(Axis.ZP.rotationDegrees(sign*load*6));}
            piece(pose,out,stack,spec,2,light);pose.popPose();
        }finally{pose.popPose();}
    }
    private static float sightY(TennoGunSpec s){
        return switch(s){
            case BRATON -> 13.7f;
            case BRATON_PRIME -> 14.5f;
            case BRATON_VANDAL -> 14.65f;
            case BURSTON -> 13.9f;
            case BURSTON_PRIME -> 14.23f;
            case PERIGALE -> 13.44f;
            case PERIGALE_PRIME -> 14.14f;
            case VESPER_77 -> 12.15f;
            default -> 12.8f;
        };
    }
    private static void piece(PoseStack p,MultiBufferSource out,ItemStack stack,TennoGunSpec spec,int part,int light){
        var mc=Minecraft.getInstance();var model=mc.getModelManager().getModel(PARTS[spec.ordinal()][part]);
        mc.getItemRenderer().render(stack,ItemDisplayContext.NONE,false,p,out,light,OverlayTexture.NO_OVERLAY,model);
    }
}
