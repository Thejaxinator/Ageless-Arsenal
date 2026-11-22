package aa.validation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayDeque;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.client.WorldEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid="atlas_validation",value=Dist.CLIENT)
public final class FieldClient {
    static boolean connecting,setup,done,request;static int phase,tick,frames;
    static String[] stages={"kinetic","arc","blink","volley"};
    static Minecraft m(){return Minecraft.getInstance();}
    static void cmd(String cmd){m().player.connection.sendCommand(cmd);}
    static int cues(String kind)throws Exception{
        Field field=WorldEffects.class.getDeclaredField("active");field.setAccessible(true);
        int n=0;
        for(var live:(ArrayDeque<?>)field.get(null)){
            var member=live.getClass().getDeclaredField("cue");member.setAccessible(true);
            VoxelCue cue=(VoxelCue)member.get(live);
            if(cue.kind().name().equals(kind))n++;
        }
        return n;
    }
    static void save(String name)throws Exception{
        try(var im=Screenshot.takeScreenshot(m().getMainRenderTarget())){im.writeToFile(Path.of(System.getProperty("check.shots"),name+".png"));}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=m();if(mc.screen instanceof AccessibilityOnboardingScreen a&&mc.getOverlay()==null){a.onClose();return;}
        if(!connecting&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){connecting=true;
            String host="127.0.0.1:25658";ConnectScreen.startConnecting(mc.screen,mc,ServerAddress.parseString(host),new ServerData("Ageless FX",host,ServerData.Type.OTHER),false,null);
        }
        if(mc.player==null||mc.level==null||done)return;
        if(!setup){
            setup=true;mc.setScreen(null);mc.options.fov().set(58);mc.options.hideGui=true;mc.options.bobView().set(false);
            mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            cmd("gamerule sendCommandFeedback false");cmd("gamemode creative");cmd("gamerule doMobSpawning false");
            cmd("gamerule doDaylightCycle false");cmd("time set 6000");cmd("weather clear");
            cmd("kill @e[tag=fx_camera]");cmd("kill @e[tag=fx_captain]");cmd("tp @s -6.5 60 4.5 -90 0");
            cmd("fill -14 60 -10 13 69 16 minecraft:air");
            cmd("fill -14 59 -10 13 59 16 minecraft:polished_deepslate");
            cmd("fill -13 60 14 13 68 14 minecraft:cyan_terracotta");
            cmd("fill -13 60 14 13 60 14 minecraft:waxed_exposed_cut_copper");
            cmd("fill 8 60 3 8 62 5 minecraft:gray_concrete");cmd("fill 8 60 2 8 60 6 minecraft:waxed_cut_copper");
            cmd("summon minecraft:armor_stand 0 62.1 -8.3 {Tags:[\"fx_camera\"],NoGravity:1b,Marker:1b,Invisible:1b,Rotation:[0f,4f]}");
        }
        for(Entity ent:mc.level.entitiesForRendering())if(ent.getType()==net.minecraft.world.entity.EntityType.ARMOR_STAND&&ent.getZ()< -7.9 && ent.getZ()>-8.9){if(mc.getCameraEntity()!=ent)mc.setCameraEntity(ent);break;}
        if(mc.getCameraEntity()==mc.player){if(++tick>350)throw new IllegalStateException("camera unavailable");return;}
        if(++tick==30&&phase==2)cmd("summon destiny2mc:fallen_captain_rifle 0 60 4 {Tags:[\"fx_captain\"],NoAI:1b,PersistenceRequired:1b,Rotation:[135f,0f]}");
        if(tick==30&&phase==3){
            cmd("tp @e[tag=fx_captain] 45 60 45");cmd("fill 8 60 1 8 65 8 minecraft:gray_concrete");
            cmd("tp @e[tag=fx_camera] 0.2 61.95 -2.6 -46 1");
        }
        if(request&&frames==0&&(phase==0||phase==1)&&tick%16==0)cmd("aa_fx_test "+stages[phase]);
        if(tick>420)throw new IllegalStateException("no visual cue for "+stages[phase]);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(!setup||done||m().player==null||m().level==null||m().screen!=null||m().getCameraEntity()==m().player||tick<65)return;
        String stage=stages[phase];
        if(!request){save(stage+"-clear");request=true;cmd("aa_fx_test "+stage);return;}
        int n=cues(stage.equals("blink")?"BLINK_OUT":"TRACE");
        if(stage.equals("volley")){
            n=0;int stuck=0;for(Entity bolt:m().level.entitiesForRendering())
                if(bolt instanceof com.jax.destiny2mc.frontier.FrontierProjectile p){n++;if(p.attached())stuck++;}
            if(n>=7&&stuck>=5&&frames<60)save("volley-mines");
            if(n>=7&&frames==0)System.out.println("FX NATIVE client renders "+n+" tracked physical bolts");
            if(n<7&&frames==0)return;
        }
        if(n>0){
            if(stage.equals("blink")&&cues("BLINK_IN")==0) return;
            if(frames<4)save(stage+"-"+frames);
            if(frames==0 && !stage.equals("volley"))System.out.println("FX NATIVE queue length "+n+" for "+stage);
            frames++;
            if(frames==1)System.out.println("FX NATIVE client received "+stage+" cue");
        } else if(frames>0){
            if(++phase==stages.length){done=true;System.out.println("FX NATIVE COMPLETE");m().stop();return;}
            request=false;tick=0;frames=0;
        }
    }
}
