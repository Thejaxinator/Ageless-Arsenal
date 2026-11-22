package ageless.binderqa;
import com.jax.destiny2mc.binder.*;
import com.jax.destiny2mc.binder.client.*;
import com.jax.destiny2mc.binder.difficulty.DifficultyScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** Test-only native client. Drive rendered controls, wait for replication, and capture the actual frame. */
@EventBusSubscriber(modid="binder_screen_check",value=Dist.CLIENT)
public final class BinderScreenCheck {
    static final String[] IDS={"marksmans_dodge","healing_rift","towering_barricade","solar_grenade","triple_jump","ember_of_solace"};
    static boolean connected,started;
    static int stage,age,slot,step,frames,priorLevels,priorMotes;
    static String pending="";
    static final BlockPos POS=new BlockPos(0,60,0);
    static Minecraft mc(){return Minecraft.getInstance();}
    static void cmd(String cmd){mc().player.connection.sendCommand(cmd);}
    static void next(int s){stage=s;age=0;}
    static void click(String prefix){
        var screen=mc().screen;
        for(var node:screen.children())if(node instanceof AbstractWidget w && w.active && w.getMessage().getString().startsWith(prefix)){
            if(screen.mouseClicked(w.getX()+w.getWidth()/2d,w.getY()+w.getHeight()/2d,0))return;
        }
        throw new IllegalStateException("Could not click "+prefix+"; "+screen+" "+screen.children().stream().filter(n->n instanceof AbstractWidget).map(n->((AbstractWidget)n).getMessage().getString()).toList());
    }
    static void shoot(String label){pending=label;frames=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen s && m.getOverlay()==null){s.onClose();return;}
        if(!connected && m.screen instanceof TitleScreen && m.getOverlay()==null){
            connected=true;String addr="127.0.0.1:"+Integer.getInteger("binder.port",25733);
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(addr),new ServerData("Ageless QA",addr,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null||m.level==null)return;
        if(!started){
            started=true;m.setScreen(null);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            cmd("gamerule sendCommandFeedback false");cmd("gamerule fallDamage false");cmd("gamerule doMobSpawning false");cmd("gamerule doDaylightCycle false");cmd("time set noon");
            cmd("gamemode creative");cmd("forceload add 0 0");cmd("tp @s 0.5 60 -2.5 0 18");return;
        }
        if(++age>380)throw new IllegalStateException("Timeout stage "+stage+" slot "+slot+" step "+step+" "+BinderClientState.snapshot());
        if(!pending.isEmpty())return;
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        var s=BinderClientState.snapshot();
        switch(stage){
            case 0->{
                if(age<36)return;
                cmd("fill -5 59 -6 5 59 5 minecraft:smooth_stone");cmd("setblock 0 60 0 destiny2mc:energy_binder");
                cmd("tp @s 0.5 60 -2.5 0 18");cmd("gamemode survival");cmd("clear @s");cmd("experience set @s 50 levels");
                cmd("give @s destiny2mc:mote_of_light 128");next(1);
            }
            case 1->{
                if(age<30)return;
                m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,60.5,0),Direction.NORTH,POS,false));next(2);
            }
            case 2->{
                if(!(m.screen instanceof EnergyBinderScreen)||age<8)return;
                // Choose a subclass perk from the visible first page. All early rows are actual game definitions.
                IDS[5]=BinderCatalog.all().stream().filter(a->a.category().equals("passive")).findFirst().orElseThrow().id();
                next(3);
            }
            case 3->{
                var a=BinderCatalog.get(IDS[slot]);String tab=slot<3?"Class":slot==3?"Grenade":"Passive";
                if(step==0){click(tab+" abilities");step++;return;}
                if(step==1){click(a.name()+",");step++;if(slot==0)shoot("Unlocking-an-Ability");return;}
                if(step==2){
                    priorLevels=s.levels();priorMotes=s.motes();click("Unlock "+a.name()+" for");step++;return;
                }
                if(step==3){
                    if(!s.unlocked().contains(a.id()))return;
                    if(s.levels()!=priorLevels-a.levels()||s.motes()!=priorMotes-a.motes())throw new IllegalStateException("Purchase cost "+a.id());
                    click((slot<4?"active "+(slot+1):"passive "+(slot-3))+",");step++;return;
                }
                if(step==4){click("Bind "+a.name()+" to");step++;return;}
                if(!(slot<4?s.active().get(slot):s.passive().get(slot-4)).equals(a.id()))return;
                System.out.println("BINDER UI paid and bound "+a.id()+" slot "+slot);
                slot++;step=0;age=0;
                if(slot==6){click("Grenade abilities");shoot("Energy-Binder");next(4);}
            }
            case 4->{click("Encounter difficulty");next(5);}
            case 5->{if(m.screen instanceof DifficultyScreen && age>10){shoot("Encounter-Difficulty");next(6);}}
            case 6->{m.screen.onClose();next(7);}
            case 7->{if(m.screen instanceof EnergyBinderScreen && age>5){m.screen.onClose();next(8);}}
            case 8->{
                if(age<5)return;
                for(var key:m.options.keyMappings)if(key.getName().equals("key.destiny2mc.binder.slot2")){
                    key.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K));
                    KeyMapping.resetMapping();
                }
                KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K));next(9);
            }
            case 9->{
                if(s.readyAt().getOrDefault("healing_rift",0L)<=m.level.getGameTime())return;
                System.out.println("BINDER UI rebound K key cast equipped Healing Rift");
                shoot("Rift-and-Ability-HUD");
                if(BinderProgressView.read()!=null){
                    // Four credited normal faction defeats cross the first threshold via real drop events.
                    for(int i=0;i<4;i++){
                        String tag="binder_qa_"+i;
                        cmd("summon destiny2mc:fallen_dreg 0.5 60 1.5 {NoAI:1b,Tags:[\""+tag+"\"]}");
                        cmd("damage @e[tag="+tag+",limit=1] 500 minecraft:player_attack by @s");
                    }
                }
                next(14);
            }
            case 14->{
                if(age<10)return;
                var rank=BinderProgressView.read();
                if(rank!=null && rank.level()<2)return;
                m.setScreen(new InventoryScreen(m.player));next(10);
            }
            case 10->{
                if (!(m.screen instanceof InventoryScreen)) return;
                if (!com.jax.destiny2mc.client.HudConfig.inventoryDrawerOpen) click("Show abilities");
                next(11);
            }
            case 11->{if(m.screen instanceof InventoryScreen && com.jax.destiny2mc.client.HudConfig.inventoryDrawerOpen && age>7){shoot("Character-Loadout");next(12);}}
            case 12->{System.out.println("BINDER UI COMPLETE "+s.active()+" "+s.passive()+" career="+BinderProgressView.read());m.stop();next(13);}
            default->{}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(pending.isEmpty()||++frames<4)return;
        try(var image=Screenshot.takeScreenshot(mc().getMainRenderTarget())){image.writeToFile(Path.of(System.getProperty("binder.shots"),pending+".png"));}
        pending="";
    }
}
