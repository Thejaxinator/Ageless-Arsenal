package ageless.uiqa;

import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.client.HudConfig;
import com.jax.destiny2mc.client.HudSettingsScreen;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Exercise the public settings entry point, live native controls and legacy preference migration. */
@EventBusSubscriber(modid="hud_preferences_check",value=Dist.CLIENT)
public final class HudPreferencesCheck {
    static boolean connected;
    static int stage, age, frames;
    static String capture="";
    static void mark(String text){ System.out.println("HUD PREFERENCES "+text); }
    static void check(boolean b,String text){ if(!b)throw new AssertionError(text);mark(text); }
    static void next(int n){ stage=n;age=0; }
    static void click(String label) {
        var s=Minecraft.getInstance().screen;
        var b=s.children().stream().filter(n->n instanceof Button).map(n->(Button)n)
            .filter(n->n.getMessage().getString().startsWith(label)).findFirst().orElseThrow();
        if(!s.mouseClicked(b.getX()+b.getWidth()/2D,b.getY()+b.getHeight()/2D,0))throw new AssertionError(label);
        s.mouseReleased(b.getX()+b.getWidth()/2D,b.getY()+b.getHeight()/2D,0);
    }

    @SubscribeEvent public static void checkPreferences(ClientTickEvent.Post event)throws Exception {
        var m=Minecraft.getInstance();
        if(m.screen instanceof AccessibilityOnboardingScreen s && m.getOverlay()==null){s.onClose();return;}
        if(!connected && m.screen instanceof TitleScreen && m.getOverlay()==null) {
            connected=true;
            String address="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(address),
                new ServerData("HUD settings QA",address,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null||m.level==null)return;
        m.getToasts().clear();
        m.gui.getChat().clearMessages(false);
        if(++age>350)throw new AssertionError("Settings test timed out at stage "+stage);
        if(!capture.isEmpty())return;
        switch(stage){
            case 0 -> {
                if(age<20)return;
                m.setScreen(null); m.player.connection.sendCommand("clear @s");
                m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
                check(HudConfig.hudStyle==0 && HudConfig.showAbilities && !HudConfig.inventoryDrawerOpen,"fresh profile defaults to Destiny with visible abilities and closed drawer");
                ClientWeaponState.SETTINGS_KEY.setDown(true);next(1);
            }
            case 1 -> {
                if(!(m.screen instanceof HudSettingsScreen))return;
                ClientWeaponState.SETTINGS_KEY.setDown(false);
                check(m.player.getMainHandItem().isEmpty(),"settings hotkey works empty-handed");
                capture="HUD-Settings";next(2);
            }
            case 2 -> {
                if(age<8)return;
                click("Ability HUD");check(!HudConfig.showAbilities,"native visibility button hides strip");
                HudConfig.inventoryDrawerOpen=true;HudConfig.save();
                for(int i=0;i<5;i++)click("Theme");
                check(HudConfig.hudStyle==5,"native theme button cycles from Destiny to Tenno");
                click("Done");
                check(m.screen==null,"Done returns to world");
                // The historical file lacks either new key. Write the isolated test profile only.
                var file=m.gameDirectory.toPath().resolve("config/destiny2mc_client.properties");
                String saved=Files.readString(file);
                HudConfig.hudStyle=0;HudConfig.showAbilities=true;HudConfig.inventoryDrawerOpen=false;
                HudConfig.loaded=false;HudConfig.ensureLoaded();
                check(HudConfig.hudStyle==5 && !HudConfig.showAbilities && HudConfig.inventoryDrawerOpen,"reloading preferences restores all three choices independently");
                Files.writeString(file,"hudStyle=1\nscalePreset=3\nshowIcon=0\n");
                HudConfig.loaded=false;HudConfig.ensureLoaded();
                check(HudConfig.hudStyle==1 && HudConfig.scalePreset==3 && !HudConfig.showIcon && HudConfig.showAbilities && !HudConfig.inventoryDrawerOpen,"existing compact profile migrates and missing controls take defaults");
                Files.writeString(file,saved);
                HudConfig.loaded=false; HudConfig.ensureLoaded();
                m.setScreen(new HudSettingsScreen()); next(3);
            }
            case 3 -> {
                if(age<10)return;
                click("Reset appearance");
                check(HudConfig.hudStyle==0 && HudConfig.inventoryDrawerOpen && !HudConfig.showAbilities,"reset appearance leaves gameplay and inventory visibility choices intact");
                click("Ability HUD");
                var screen=m.screen;
                screen.resize(m,320,240);
                var buttons=screen.children().stream().filter(n->n instanceof Button).map(n->(Button)n).toList();
                check(buttons.size()==10 && buttons.stream().allMatch(b->b.getX()>=0&&b.getY()>=0&&b.getX()+b.getWidth()<=320&&b.getY()+b.getHeight()<=240),"all ten native buttons fit the minimum GUI size");
                click("Done");
                mark("COMPLETE");m.stop();next(4);
            }
            default -> {}
        }
    }
    @SubscribeEvent public static void screenshot(RenderFrameEvent.Post e)throws Exception {
        if(capture.isEmpty()||++frames<5)return;
        try(var im=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){
            im.writeToFile(Path.of(System.getProperty("binder.shots"),capture+".png"));
        }
        frames=0;capture="";
    }
}
