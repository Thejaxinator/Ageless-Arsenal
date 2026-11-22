package ageless.hudqa;

import com.jax.destiny2mc.binder.BinderCatalog;
import com.jax.destiny2mc.binder.BinderClientState;
import com.jax.destiny2mc.binder.client.BinderProgressView;
import com.jax.destiny2mc.binder.client.EnergyBinderScreen;
import com.jax.destiny2mc.client.HudConfig;
import com.jax.destiny2mc.client.HudSettingsScreen;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** In-game acceptance fixture. The game installs its normal catalogs and handles all purchases and casts. */
@EventBusSubscriber(modid="ageless_hud_check",value=Dist.CLIENT)
public final class ThemedHudCheck {
    static final String[] FORMS={"marksmans_dodge","healing_rift","towering_barricade","solar_grenade","triple_jump","ember_of_solace"};
    static final String[] SKINS={"Destiny","Compact","Minimal","Borderlands-2","Borderlands-4","Tenno"};
    static Minecraft mc(){return Minecraft.getInstance();}
    static boolean connecting,started;
    static int stage,age,index,theme,action,y=88,frame,elapsed;
    static long pulsed;
    static String pending="";
    static InventoryScreen inventory;
    static boolean filming;
    record MovieFrame(long ms,NativeImage image) {}
    static final ArrayList<MovieFrame> frames=new ArrayList<>();
    static final Path shots=Path.of(System.getProperty("binder.shots"));
    static void next(int phase){stage=phase;age=0;}
    static void cmd(String value){mc().player.connection.sendCommand(value);}
    static void shoot(String name){pending=name;frame=0;}
    static void check(boolean result,String what){if(!result)throw new AssertionError(what);System.out.println("HUD CHECK "+what);}
    static void click(String text){
        var screen=mc().screen;
        for(var child:screen.children())if(child instanceof AbstractWidget w && w.active && w.visible && w.getMessage().getString().startsWith(text)){
            if(screen.mouseClicked(w.getX()+w.getWidth()/2d,w.getY()+w.getHeight()/2d,0)){
                screen.mouseReleased(w.getX()+w.getWidth()/2d,w.getY()+w.getHeight()/2d,0);return;
            }
        }
        throw new AssertionError(text+" absent: "+mc().screen.children().stream().filter(c->c instanceof AbstractWidget).map(c->((AbstractWidget)c).getMessage().getString()).toList());
    }
    static void recipe(){
        for(var child:inventory.children())if(child instanceof ImageButton w && w.visible && w.active){
            double x=w.getX()+w.getWidth()/2d, y=w.getY()+w.getHeight()/2d;
            if(inventory.mouseClicked(x,y,0)){inventory.mouseReleased(x,y,0);return;}
        }
        throw new AssertionError("Recipe book button missing");
    }
    static boolean has(String prefix){
        return mc().screen.children().stream().anyMatch(c->c instanceof AbstractWidget w && w.visible && w.getMessage().getString().startsWith(prefix));
    }
    static void inventory(){inventory=new InventoryScreen(mc().player);mc().setScreen(inventory);}
    static void cycle(){
        mc().setScreen(new HudSettingsScreen());
        click("Theme");click("Done");
    }
    static void view(){
        cmd("tp @s -7.5 "+(y+1)+" -8.5 -17 -9");
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen a && m.getOverlay()==null){a.onClose();return;}
        if(!connecting && m.screen instanceof TitleScreen && m.getOverlay()==null){
            connecting=true;String a="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(a),new ServerData("HUD acceptance",a,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null || m.level==null)return;
        if(!started){
            started=true;m.setScreen(null);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            cmd("gamerule sendCommandFeedback false");cmd("gamerule fallDamage false");cmd("gamerule doMobSpawning false");cmd("gamerule doDaylightCycle false");cmd("gamerule doWeatherCycle false");cmd("weather clear");cmd("time set 7800");
            cmd("gamemode creative");cmd("forceload add -16 -16 31 31");cmd("tp @s 0.5 170 -2.5 0 10");return;
        }
        if(++age>420)throw new IllegalStateException("Timeout at stage "+stage+" / "+index+" / "+action+" screen="+m.screen+" snapshot="+BinderClientState.snapshot());
        if(!pending.isEmpty())return;
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        var s=BinderClientState.snapshot();
        switch(stage){
            case 0->{
                if(age<20 || !m.level.hasChunkAt(new BlockPos(0,64,0)))return;
                y=Math.max(68,m.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,8)+1);
                cmd("fill -16 "+y+" -13 16 "+y+" 32 minecraft:moss_block");cmd("fill -3 "+y+" -13 3 "+y+" 10 minecraft:mossy_stone_bricks");
                cmd("place template destiny2mc:frontier/vex_conduit -12 "+y+" 3");
                cmd("setblock 0 "+(y+1)+" 0 destiny2mc:energy_binder");
                cmd("summon destiny2mc:vex_goblin -4.2 "+(y+1)+" 10.2 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
                cmd("summon destiny2mc:vex_goblin 5.5 "+(y+1)+" 9.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Rotation:[160f,0f]}");
                cmd("tp @s 0.5 "+(y+1)+" -2.5 0 10");cmd("gamemode survival");cmd("clear @s");cmd("experience set @s 50 levels");cmd("give @s destiny2mc:mote_of_light 128");next(1);
            }
            case 1->{
                if(age<22)return;
                BlockPos pos=new BlockPos(0,y+1,0);
                m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,y+1.5,0),Direction.NORTH,pos,false));next(2);
            }
            case 2->{
                if(!(m.screen instanceof EnergyBinderScreen))return;
                var f=BinderCatalog.get(FORMS[index]);
                if(action==0){BinderClientState.purchase(f.id());action++;return;}
                if(action==1){if(!s.unlocked().contains(f.id()))return;BinderClientState.equip(f.id(),index);action++;return;}
                if(!(index<4?s.active().get(index):s.passive().get(index-4)).equals(f.id()))return;
                action=0;age=0;if(++index<6)return;
                m.screen.onClose();cmd("item replace entity @s hotbar.0 with destiny2mc:ace_of_spades");cmd("give @s minecraft:iron_pickaxe");cmd("give @s minecraft:bread 8");view();next(3);
            }
            case 3->{
                if(age<20 || !(m.player.getMainHandItem().getItem() instanceof DestinyGunItem))return;
                check(s.active().size()==4 && s.passive().size()==2,"replicated four active forms and two passives with Ace of Spades equipped");
                if(Boolean.getBoolean("binder.preflight")){shoot("Preflight");next(99);return;}
                for(var k:m.options.keyMappings)if(k.getName().equals("key.destiny2mc.binder.slot2"))k.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K));
                KeyMapping.resetMapping();KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K));next(4);
            }
            case 4->{
                if(age<66 || s.readyAt().getOrDefault("healing_rift",0L)<=m.level.getGameTime())return;
                check(true,"rebound K cast Healing Rift through the actual key mapping");
                HudConfig.ensureLoaded();check(HudConfig.hudStyle==0,"fresh client has Destiny HUD");
                // Capture game input only after the cast message has faded. The cooldown is server-owned.
                if(age<66)return;
                next(5);
            }
            case 5->{
                if(age<4)return;
                check(HudConfig.hudStyle==theme,"mode "+theme+" equals "+HudConfig.getStyleName()+" with gun");
                shoot("HUD-"+SKINS[theme]);next(6);
            }
            case 6->{
                if(++theme<6){cycle();next(5);return;}
                cycle();m.setScreen(new HudSettingsScreen());shoot("HUD-Settings");next(7);
            }
            case 7->{click("Ability HUD");click("Done");shoot("Weapon-Only");next(8);}
            case 8->{
                m.setScreen(new HudSettingsScreen());check(has("Ability HUD"),"ability HUD toggle present after reopening settings");
                click("Ability HUD");click("Done");m.options.hideGui=true;shoot("Hidden-F1");next(9);
            }
            case 9->{m.options.hideGui=false;cmd("item replace entity @s hotbar.0 with minecraft:air");next(10);}
            case 10->{if(age<5 || !m.player.getMainHandItem().isEmpty())return;shoot("Ability-Only");next(24);}
            case 24->{if(Boolean.getBoolean("binder.hudonly")){next(99);return;}cmd("item replace entity @s hotbar.0 with destiny2mc:ace_of_spades");next(11);}
            case 11->{if(age<8)return;inventory();next(12);}
            case 12->{
                if(age<5)return;
                if(has("Hide abilities")){click("Hide abilities");next(12);return;}
                check(has("Show abilities"),"inventory has visible native drawer toggle");
                shoot("Inventory-Closed");next(13);
            }
            case 13->{Files.createDirectories(shots.resolve("drawer-frames"));click("Show abilities");filming=true;pulsed=System.nanoTime();elapsed=0;next(14);}
            case 14->{
                if(filming || age<7)return;
                check(m.screen==inventory && has("Hide abilities"),"open drawer retains the same vanilla InventoryScreen");
                shoot("Inventory-Open");next(15);
            }
            case 15->{m.screen.onClose();inventory();next(16);}
            case 16->{
                if(age<8)return;check(has("Hide abilities"),"drawer preference survives inventory close and reopen");
                click("Hide abilities");click("Show abilities");click("Hide abilities");next(17);
            }
            case 17->{
                if(age<8)return;check(has("Show abilities"),"reversed slide settles closed");
                // 1000x720 at 3x GUI scale exercises the compact two-column layout.
                m.getWindow().setWindowed(1000,720);m.resizeDisplay();next(18);
            }
            case 18->{
                if(age<10)return;
                check(inventory.width<380,"narrow logical width "+inventory.width);
                click("Show abilities");next(19);
            }
            case 19->{
                if(age<10)return;check(m.screen==inventory && has("Hide abilities"),"compact left drawer opens on a 3x narrow window");shoot("Inventory-Narrow");next(20);
            }
            case 20->{recipe();next(21);}
            case 21->{
                if(age<8)return;check(inventory.getRecipeBookComponent().isVisible(),"vanilla recipe-book button remains usable with drawer");shoot("Inventory-Recipe");next(22);
            }
            case 22->{inventory.keyPressed(GLFW.GLFW_KEY_ESCAPE,0,0);next(23);}
            case 23->{
                if(age<8)return;check(!inventory.getRecipeBookComponent().isVisible(),"narrow recipe book closes via vanilla Escape");
                check(m.screen==inventory,"inventory remained interactive through recipe and resize");
                m.screen.onClose();cmd("gamemode creative");next(25);
            }
            case 25->{
                if(!m.gameMode.hasInfiniteItems())return;
                m.getWindow().setWindowed(1440,810);m.resizeDisplay();
                m.setScreen(new CreativeModeInventoryScreen(m.player,m.getConnection().enabledFeatures(),true));next(26);
            }
            case 26->{
                if(age<10)return;
                check(m.screen instanceof CreativeModeInventoryScreen && has("Hide abilities"),"creative inventory inherits drawer preference and toggle");
                click("Hide abilities");click("Show abilities");next(27);
            }
            case 27->{
                if(age<8)return;check(has("Hide abilities"),"creative drawer reopens without replacing the creative screen");shoot("Inventory-Creative");next(99);
            }
            case 99->{System.out.println("HUD CHECK COMPLETE edition progression="+(BinderProgressView.read()!=null));m.stop();next(100);}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(filming){
            long ms=(System.nanoTime()-pulsed)/1_000_000;
            if(ms>=elapsed*20){
                frames.add(new MovieFrame(ms,Screenshot.takeScreenshot(mc().getMainRenderTarget())));
                elapsed++;
                if(ms>=340 || frames.size()>=20){
                    filming=false;
                    for(var f:frames){try(var img=f.image()){img.writeToFile(shots.resolve("drawer-frames").resolve(String.format("%04d.png",f.ms())));}}
                    frames.clear();
                }
            }
        }
        if(pending.isEmpty() || ++frame<3)return;
        try(var img=Screenshot.takeScreenshot(mc().getMainRenderTarget())){img.writeToFile(shots.resolve(pending+".png"));}
        pending="";
    }
}
