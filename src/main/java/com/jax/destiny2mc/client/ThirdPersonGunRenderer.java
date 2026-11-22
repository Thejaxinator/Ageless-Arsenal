package com.jax.destiny2mc.client;

import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.WeakHashMap;

public final class ThirdPersonGunRenderer {
    private static final WeakHashMap<Player,Rotor[]> ROTORS=new WeakHashMap<>();
    private static final class Rotor {
        double lastTime=Double.NaN;
        float speed,angle;
        float advance(double now,boolean firing){
            if(!Double.isFinite(lastTime)||now<lastTime||now-lastTime>8){speed=0;lastTime=now;}
            float dt=(float)(now-lastTime);
            speed=Mth.clamp(speed+dt*(firing?.09f:-.12f),0,1);
            angle=(angle+46*speed*dt)%360;
            lastTime=now;
            return angle;
        }
    }

    public static MechanismPose pose(Player player,ItemStack stack,WeaponSpec spec,float partial){
        long tick=player.level().getGameTime();
        float reload=GunMechanism.progress(stack,spec,tick,partial);
        boolean main=stack==player.getMainHandItem();
        if(main&&player==Minecraft.getInstance().player)return GunMechanism.localPose(spec,reload,partial);
        // The legacy volley stamp and the Frontier fire clock are written after the server accepts a shot.
        // The latter also advances on precision refunds, where magazine count alone can stay constant.
        long shot=GunDataHelper.getLong(stack,"d2_visual_shot");
        int delay=Math.max(2,(int)spec.fireDelayTicks);
        if(com.jax.destiny2mc.ageless.WeaponPrograms.client(spec)==null && spec.index>=40){
            if(spec.archetype==4||spec.archetype==5)delay=8;
            shot=GunDataHelper.getLong(stack,"frontier_next")-delay;
        }
        double now=tick+(double)partial;
        double age=shot>0 && shot<=tick+1?Math.max(0,now-shot):Double.POSITIVE_INFINITY;
        float stroke=Mth.clamp((float)(1-age/5),0,1);
        float cycle=age<delay?(float)Math.sin(Math.PI*age/delay):0;
        long start=GunDataHelper.getLong(stack,"frontier_charge");
        int duration=GunDataHelper.getInt(stack,"frontier_duration",0);
        float charge=start>0&&duration>0?Mth.clamp((float)(now-start)/duration,0,1):0;
        float rotor=0;
        if(SightFit.ACTION[spec.index]==GunMechanism.Action.SPIN){
            var pair=ROTORS.computeIfAbsent(player,unused->new Rotor[]{new Rotor(),new Rotor()});
            rotor=pair[main?0:1].advance(now,age<6 && reload==0);
        }
        return new MechanismPose(reload,stroke,cycle,charge,rotor);
    }

    public static float aim(Player player,ItemStack stack,float partial){
        if(stack!=player.getMainHandItem())return 0;
        if(player==Minecraft.getInstance().player)return ClientWeaponState.getSmoothAds(partial);
        var tag=GunDataHelper.readTag(stack);
        if(!tag.hasUUID("d2_aim_owner")||!player.getUUID().equals(tag.getUUID("d2_aim_owner")))return 0;
        double now=player.level().getGameTime()+(double)partial;
        double remaining=tag.getLong("d2_aim_until")-now;
        if(remaining>com.jax.destiny2mc.overdrive.ServerAimState.LEASE)return 0;
        return Mth.clamp((float)Math.min(remaining,now-tag.getLong("d2_aim_since"))/4f,0,1);
    }

    private static boolean ready(Player player){
        return !player.isSwimming()&&!player.isFallFlying()&&!player.isAutoSpinAttack()&&
            !player.isSleeping()&&!player.isUsingItem();
    }
    public static void arms(PlayerModel<?> model,Player player,float partial){
        if(!ready(player)||!(player.getMainHandItem().getItem() instanceof DestinyGunItem gun))return;
        boolean right=player.getMainArm()==HumanoidArm.RIGHT;
        float side=right?1:-1;
        var primary=right?model.rightArm:model.leftArm;
        var support=right?model.leftArm:model.rightArm;
        var state=pose(player,player.getMainHandItem(),gun.spec,partial);
        float reach=GunMechanism.envelope(state.reload());
        float aim=aim(player,player.getMainHandItem(),partial);
        primary.xRot=model.head.xRot-1.18f-.37f*aim+reach*.29f-state.stroke()*.09f;
        primary.yRot=model.head.yRot-side*(.13f+.13f*aim+reach*.14f);
        primary.zRot=side*(.035f+reach*.24f);
        if(player.getOffhandItem().isEmpty()){
            support.xRot=model.head.xRot-1.28f-.23f*aim+.61f*reach;
            support.yRot=model.head.yRot+side*(.38f+.10f*aim+.18f*reach);
            support.zRot=side*(.07f+.17f*reach);
        }
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    public static void render(Player player,ItemStack stack,DestinyGunItem gun,ItemDisplayContext ctx,
                              boolean left,PoseStack p,MultiBufferSource out,int light){
        var mc=Minecraft.getInstance();
        float partial=mc.getTimer().getGameTimeDeltaPartialTick(false);
        var state=pose(player,stack,gun.spec,partial);
        p.pushPose();
        try {
            var model=mc.getItemRenderer().getModel(stack,player.level(),player,player.getId()+ctx.ordinal());
            model.applyTransform(ctx,p,left);
            if(stack==player.getMainHandItem() && ready(player)){
                // Built-in 74 degree carry transform assumes an arm hanging at the player's side.
                // The raised arm supplies that rotation before the native hand layer calls us.
                p.mulPose(Axis.XP.rotationDegrees(-73f));
                p.translate(0,.10f,-.17f);
            }
            GunMechanism.renderPose(p,out,stack,gun.spec,light,state);
        } finally {p.popPose();}
    }
}
