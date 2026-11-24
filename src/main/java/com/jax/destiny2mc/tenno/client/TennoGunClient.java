package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.client.gunfeel.WeaponSwayController;
import com.jax.destiny2mc.tenno.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class TennoGunClient {
    static KeyMapping alternate;
    static float aim,previousAim,kick,previousKick;
    static int muzzleSide,localShots;
    private static double nextShot;
    private static boolean attackEdge,attackHeld,reloadHeld,altHeld;
    private static String last="";
    @EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Keys {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent event){
            alternate=new KeyMapping("key.destiny2mc.tenno_alt",GLFW.GLFW_KEY_G,"key.categories.destiny2mc");
            event.register(alternate);
        }
    }
    @SubscribeEvent public static void input(InputEvent.InteractionKeyMappingTriggered e){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||!(p.getMainHandItem().getItem() instanceof TennoGunItem)||mc.screen!=null)return;
        if(e.isAttack()){attackEdge=true;e.setCanceled(true);e.setSwingHand(false);}
        else if(e.isUseItem()&&!(p.isShiftKeyDown()&&mc.hitResult instanceof BlockHitResult&&mc.hitResult.getType()==HitResult.Type.BLOCK)){
            e.setCanceled(true);e.setSwingHand(false);
        }
    }
    private static void send(int op,boolean ads){
        PacketDistributor.sendToServer(new TennoIntent((byte)op,ads));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.level==null||mc.screen!=null||p.isSpectator()||!(p.getMainHandItem().getItem() instanceof TennoGunItem gun)){
            aim=previousAim=kick=previousKick=0; nextShot=0;last="";attackEdge=attackHeld=reloadHeld=altHeld=false;return;
        }
        var stack=p.getMainHandItem();var spec=gun.profile();
        if(!last.equals(spec.id())){
            last=spec.id();aim=previousAim=0;nextShot=0;attackHeld=false;localShots=0;muzzleSide=1;
        }
        long now=p.level().getGameTime();
        boolean loading=TennoGunItem.reloadUntil(stack)>now;
        boolean ads=mc.options.keyUse.isDown()&&!loading;
        previousAim=aim;
        float rate=spec.family==TennoGunSpec.Family.PERIGALE?.18f:.24f;
        aim=Mth.clamp(aim+(ads?rate:-rate*1.15f),0,1);
        previousKick=kick;kick*=.62f;
        boolean r=ClientWeaponState.RELOAD_KEY!=null&&ClientWeaponState.RELOAD_KEY.isDown();
        boolean alt=alternate!=null&&alternate.isDown();
        boolean held=mc.options.keyAttack.isDown();
        boolean pressed=attackEdge||(held&&!attackHeld);
        if(r&&!reloadHeld)send(1,false);
        else if(alt&&!altHeld&&spec.alternate()&&!loading&&now>=nextShot){
            send(2,ads);kick=1;WeaponSwayController.onShotFired();
            nextShot=TennoGunStats.of(p,stack).nextShot(now,nextShot,spec.family==TennoGunSpec.Family.CEDO?5:15);
        }else if(!loading&&now>=nextShot&&(pressed||(held&&spec.automatic()))){
            var stats=TennoGunStats.of(p,stack);
            int mag=TennoGunItem.loaded(stack,stats);
            if(mag>0){
                send(0,ads);kick=1;WeaponSwayController.onShotFired();localShots++;muzzleSide=localShots%2==0?-1:1;
            }else if(spec.family!=TennoGunSpec.Family.BUBONICO)send(1,false);
            nextShot=stats.nextShot(now,nextShot,spec.delay());
        }
        attackEdge=false;attackHeld=held;reloadHeld=r;altHeld=alt;
    }
    static float aim(float partial){float a=Mth.lerp(partial,previousAim,aim);return a*a*(3-2*a);}
    public static float getAimProgress(){return aim(ClientWeaponState.lastPartialTick);}
    @SubscribeEvent public static void fov(ComputeFovModifierEvent e){
        // Decoupled World/Hand optical FOV is handled in ClientForgeEvents.onViewportFov.
    }

    @SubscribeEvent public static void reticle(RenderGuiLayerEvent.Post event) {
        if(!event.getName().equals(VanillaGuiLayers.CROSSHAIR)||aim<.8||!Minecraft.getInstance().options.getCameraType().isFirstPerson())return;
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null || mc.screen!=null || mc.options.hideGui || !(p.getMainHandItem().getItem() instanceof TennoGunItem gun)||gun.profile()!=TennoGunSpec.VESPER_77)return;
        var g=event.getGuiGraphics();Vec3 start=p.getEyePosition(),forward=p.getLookAngle();
        double yaw=Math.toRadians(p.getYRot());Vec3 right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw));Vec3 up=right.cross(forward);
        double focus=.5*g.guiHeight()/Math.tan(Math.toRadians(mc.options.fov().get()*.76*.5));
        int drawn=0;
        for(var target:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(40),v->v!=p&&v.isAlive()&&!v.isSpectator())){
            Vec3 delta=target.getEyePosition().subtract(start);double depth=delta.dot(forward);
            if(depth<1||delta.dot(forward)/delta.length()<.94||!p.hasLineOfSight(target))continue;
            int x=g.guiWidth()/2+(int)(focus*delta.dot(right)/depth),y=g.guiHeight()/2-(int)(focus*delta.dot(up)/depth);
            int c=0xfffca256;g.fill(x-3,y,x-1,y+1,c);g.fill(x+2,y,x+4,y+1,c);g.fill(x,y-3,x+1,y-1,c);g.fill(x,y+2,x+1,y+4,c);
            if(++drawn>=16)break;
        }
    }
}
