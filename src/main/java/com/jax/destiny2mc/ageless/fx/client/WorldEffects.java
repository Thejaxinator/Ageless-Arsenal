package com.jax.destiny2mc.ageless.fx.client;

import com.jax.destiny2mc.ageless.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
        double now=mc.level.getGameTime()+e.getPartialTick().getGameTimeDeltaPartialTick(false);
        // New telegraphs remain legible in a crowded encounter even if older fragments exhaust the budget.
        var it=active.descendingIterator();
        while(it.hasNext()&&!mesh.full()){
            Live live=it.next();VoxelCue cue=live.cue;double ticks=Math.max(0,now-live.birth);
            // The client can catch up several simulation ticks before presenting a single frame.
            // Keep a late short cue bright for its first presentation, then resume the world clock.
            if(!live.presented&&live.life<=17)ticks=Math.min(ticks,live.life*.24);
            live.presented=true;double t=ticks/live.life;if(t>=1)continue;
            Vec3 a=cue.start().subtract(eye),b=cue.end().subtract(eye);EffectProfile p=cue.profile();
            switch(cue.kind()){
                case TRACE ->{
                    // Move only the near visual origin to the firing hand. The real endpoint remains authoritative.
                    if(a.lengthSqr()<.65*.65 && mc.options.getCameraType().isFirstPerson() && mc.player!=null){
                        Vec3 dir=b.subtract(a).normalize();int hand=mc.player.getMainArm()==HumanoidArm.RIGHT?1:-1;
                        a=a.add(dir.scale(.56)).add(VoxelMesh.side(dir).scale(-.19*hand)).add(0,-.16,0);
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
