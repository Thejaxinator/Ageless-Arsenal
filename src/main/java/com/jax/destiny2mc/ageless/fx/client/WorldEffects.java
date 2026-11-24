package com.jax.destiny2mc.ageless.fx.client;

import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.client.gunfeel.WeaponSwayController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.util.ArrayDeque;
import static com.jax.destiny2mc.ageless.fx.VoxelCue.Kind.*;

@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class WorldEffects {
    private static final int MAX_ACTIVE=160,MAX_PRISMS=1350;
    private static final ArrayDeque<Live> active=new ArrayDeque<>();
    private static ClientLevel owner;
    private static final class Live {
        final VoxelCue cue;final long birth;final int life;
        boolean presented;
        Live(VoxelCue cue,long birth,int life){this.cue=cue;this.birth=birth;this.life=life;}
    }
    private static int life(VoxelCue cue){
        return switch(cue.kind()){
            case TRACE->cue.profile()==EffectProfile.KINETIC?5:7;
            case BURST->13;case RING->9;case BLINK_IN,BLINK_OUT->17;case CLOUD->120;
        };
    }
    private static void changeWorld(ClientLevel l){if(owner!=l){active.clear();owner=l;}}
    public static void receive(VoxelCue cue){
        ClientLevel level=Minecraft.getInstance().level;changeWorld(level);
        if(level==null || !level.dimension().location().equals(cue.dimension())
            || !Double.isFinite(cue.start().lengthSqr()) || !Double.isFinite(cue.end().lengthSqr())
            || cue.start().distanceToSqr(cue.end())>513*513 || !Float.isFinite(cue.radius()))return;
        while(active.size()>=MAX_ACTIVE)active.removeFirst();
        active.addLast(new Live(cue,level.getGameTime(),life(cue)));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        ClientLevel l=Minecraft.getInstance().level;changeWorld(l);
        if(l!=null)active.removeIf(effect->l.getGameTime()-effect.birth>=effect.life+(effect.presented?0:8));
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES || active.isEmpty())return;
        Minecraft mc=Minecraft.getInstance();if(mc.level==null||mc.level!=owner)return;
        Vec3 eye=e.getCamera().getPosition();
        var buffers=mc.renderBuffers().bufferSource();
        VoxelMesh mesh=new VoxelMesh(buffers.getBuffer(VoxelMesh.TYPE),e.getPoseStack().last().pose(),MAX_PRISMS);
        float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        double now=mc.level.getGameTime()+partial;
        var it=active.descendingIterator();
        while(it.hasNext()&&!mesh.full()){
            Live live=it.next();VoxelCue cue=live.cue;double ticks=Math.max(0,now-live.birth);
            if(!live.presented&&live.life<=17)ticks=Math.min(ticks,live.life*.24);
            live.presented=true;double t=ticks/live.life;if(t>=1)continue;
            Vec3 a=cue.start().subtract(eye),b=cue.end().subtract(eye);EffectProfile p=cue.profile();
            switch(cue.kind()){
                case TRACE ->{
                    // Align eye-origin traces to the actual first-person barrel tip in camera space,
                    // interpolating cleanly to center during ADS and skipping already-offset Tenno muzzles.
                    if(a.lengthSqr()<.18*.18 && mc.options.getCameraType().isFirstPerson() && mc.player!=null){
                        Vec3 dir=b.subtract(a).normalize();
                        Vec3 left=VoxelMesh.side(dir);
                        Vec3 up=dir.cross(left).normalize();
                        int hand=mc.player.getMainArm()==HumanoidArm.RIGHT?1:-1;
                        float aim=ClientWeaponState.getSmoothAds(partial);
                        double forward=Mth.lerp(aim,0.62,0.56);
                        double rightOff=Mth.lerp(WeaponSwayController.adsX(aim),0.105,0.0)*hand;
                        double upOff=Mth.lerp(WeaponSwayController.adsY(aim),-0.115,-0.045);
                        a=a.add(dir.scale(forward)).add(left.scale(-rightOff)).add(up.scale(upOff));
                    }
                    EffectShapes.trace(mesh,p,a,b,t,cue.seed());
                }
                case RING ->EffectShapes.ring(mesh,p,a,cue.radius(),t,cue.seed());
                case BURST ->EffectShapes.burst(mesh,p,a,cue.radius(),t,cue.seed());
                case BLINK_IN,BLINK_OUT ->EffectShapes.blink(mesh,p,a,t,cue.seed(),cue.kind()==BLINK_IN);
                case CLOUD ->EffectShapes.cloud(mesh,p,a,cue.radius(),t,ticks,cue.seed());
            }
        }
        buffers.endBatch(VoxelMesh.TYPE);
    }
}
