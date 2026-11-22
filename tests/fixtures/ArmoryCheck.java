package atlas.validation;
import java.nio.file.Path;
import java.util.*;
import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid="atlas_validation", value=Dist.CLIENT)
public class ArmoryCheck {
    static Minecraft game(){return Minecraft.getInstance();}
    static boolean connecting,setup,done,held,seen;
    static int stage,age,index,frames; static String gear;
    static String[] ids=Arrays.stream(WeaponSpec.ALL).filter(w -> w.index >= Integer.getInteger("check.first", 52)).map(s->s.id).filter(id -> System.getProperty("check.ids","").isEmpty() || java.util.List.of(System.getProperty("check.ids").split(",")).contains(id)).toArray(String[]::new);
    static void cmd(String s){game().player.connection.sendCommand(s);}
    static void step(int s){stage=s;age=0;frames=0;seen=false;}
    static void save(String name)throws Exception{
        try(var im=Screenshot.takeScreenshot(game().getMainRenderTarget())){im.writeToFile(Path.of(System.getProperty("check.shots"),name+".png"));}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var m=game();
        if(m.screen instanceof AccessibilityOnboardingScreen s && m.getOverlay()==null){s.onClose();return;}
        if(!connecting && m.screen instanceof TitleScreen && m.getOverlay()==null){connecting=true;String h="127.0.0.1:25724";ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(h),new ServerData("Armory QA",h,ServerData.Type.OTHER),false,null);}
        if(m.level==null||m.player==null||done)return;
        if(!setup){setup=true;
            var info=net.neoforged.fml.ModList.get().getModContainerById("destiny2mc").orElseThrow().getModInfo();
            if(!info.getDisplayName().equals("Ageless Arsenals") || WeaponSpec.ALL.length!=70)throw new IllegalStateException("Release identity or weapon count");
            System.out.println("AGELESS CLIENT "+info.getVersion()+" 70 weapons "+info.getDisplayName());m.setScreen(null);m.options.fov().set(70);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.getToasts().clear();m.options.bobView().set(false);
            cmd("gamerule sendCommandFeedback false");cmd("gamemode creative");cmd("gamerule doDaylightCycle false");cmd("gamerule doMobSpawning false");cmd("time set noon");cmd("weather clear");
            cmd("fill -20 59 -8 20 59 30 minecraft:smooth_stone");cmd("fill -12 60 17 12 70 17 minecraft:light_gray_concrete");
            cmd("fill -2 60 16 2 64 16 minecraft:cyan_concrete");cmd("setblock 0 61 15 minecraft:target");cmd("tp @s 0.5 60 -3.5 0 0.3");step(-1);return;}
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        if(++age>260)throw new IllegalStateException("timed out "+gear+" "+stage+" "+ClientWeaponState.ads+" "+m.screen);
        if(stage==-1 && age>=45) step(0);
        if(stage==0){gear=ids[index];m.options.keyUse.setDown(false);cmd("item replace entity @s weapon.mainhand with destiny2mc:"+gear);step(1);return;}
        if(stage==1 && !BuiltInRegistries.ITEM.getKey(m.player.getMainHandItem().getItem()).getPath().equals(gear))return;
        if(stage==1 && age>5 && ClientWeaponState.ads<.015f)step(2);
        if(stage==4){m.options.keyUse.setDown(true);if(age>34 && ClientWeaponState.ads>.998f)step(5);}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void control(RenderHandEvent e){
        if(setup && (stage==3||stage==6))e.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true) public static void verify(RenderHandEvent e){
        if(e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND && e.isCanceled())seen=true;
    }
    @SubscribeEvent public static void gui(RenderGuiEvent.Post e){
        if(stage!=7||!setup)return;
        var g=e.getGuiGraphics();g.fill(0,0,g.guiWidth(),g.guiHeight(),0xffd7d8cd);
        var pose=g.pose();pose.pushPose();int s=Math.min(g.guiWidth(),g.guiHeight())/19;
        pose.translate((g.guiWidth()-16*s)/2f,(g.guiHeight()-16*s)/2f,0);pose.scale(s,s,s);g.renderItem(game().player.getMainHandItem(),0,0);pose.popPose();g.flush();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(!setup||done||game().screen!=null||++frames<3)return;
        if((stage==2||stage==5)&&!seen)return;
        if(stage==2){save(gear+"-hip");step(3);}
        else if(stage==3){save(gear+"-hipclear");step(4);}
        else if(stage==5){save(gear+"-ads");step(6);}
        else if(stage==6){save(gear+"-adsclear");step(7);}
        else if(stage==7){save(gear+"-inspect");System.out.println("ARMORY CHECK "+gear+" ads="+ClientWeaponState.ads);
            game().options.keyUse.setDown(false);
            if(++index==ids.length){done=true;System.out.println("ARMORY COMPLETE");game().stop();}else step(0);}
    }
}
