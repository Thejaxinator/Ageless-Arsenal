package ageless.drawerqa;
import com.jax.destiny2mc.binder.client.BinderInventory.DrawerToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** Drives the same pre/native/post dispatch as Minecraft's MouseHandler on a real connected client. */
@EventBusSubscriber(modid="ageless_drawer_check",value=Dist.CLIENT)
public class DrawerInputCheck {
    static Minecraft mc(){return Minecraft.getInstance();}
    static boolean connecting;
    static InventoryScreen screen;
    static int stage,age;
    static void next(){stage++;age=0;}
    static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);System.out.println("DRAWER CHECK "+msg);}
    static void cmd(String s){mc().player.connection.sendCommand(s);}
    static DrawerToggle toggle(){return (DrawerToggle)mc().screen.children().stream().filter(c->c instanceof DrawerToggle).findFirst().orElseThrow();}
    static boolean click(double x,double y,int button){
        boolean captured=ClientHooks.onScreenMouseClickedPre(screen,x,y,button);
        if(!captured){var handled=screen.mouseClicked(x,y,button);ClientHooks.onScreenMouseClickedPost(screen,x,y,button,handled);}
        if(!ClientHooks.onScreenMouseReleasedPre(screen,x,y,button)){
            var handled=screen.mouseReleased(x,y,button);ClientHooks.onScreenMouseReleasedPost(screen,x,y,button,handled);
        }
        return captured;
    }
    static void slot(int n,int button){
        Slot s=screen.getMenu().getSlot(n);click(screen.getGuiLeft()+s.x+8,screen.getGuiTop()+s.y+8,button);
    }
    static int carried(){return screen.getMenu().getCarried().getCount();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen a&&m.getOverlay()==null){a.onClose();return;}
        if(!connecting && m.screen instanceof TitleScreen && m.getOverlay()==null){
            connecting=true;String addr="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(addr),new ServerData("Drawer test",addr,ServerData.Type.OTHER),false,null);
        }
        if(m.level==null||m.player==null)return;
        if(++age>180)throw new AssertionError("Timeout stage "+stage+" screen "+m.screen+" cursor "+(screen==null?-1:carried()));
        switch(stage){
            case 0->{m.setScreen(null);cmd("gamemode survival");cmd("gamerule doMobSpawning false");cmd("effect give @s resistance infinite 4 true");cmd("clear @s");cmd("item replace entity @s hotbar.0 with minecraft:oak_log 4");next();}
            case 1->{if(age<10||!m.player.getInventory().getItem(0).is(Items.OAK_LOG))return;
                screen=new InventoryScreen(m.player);m.setScreen(screen);next();}
            case 2->{if(age<6)return;screen.setFocused(toggle());screen.keyPressed(GLFW.GLFW_KEY_ENTER,0,0);
                check(toggle().getMessage().getString().equals("Hide abilities"),"Native focused Enter opens the drawer");next();}
            case 3->{if(age<8)return;slot(36,0);check(carried()==4,"Native hotbar click picked up four logs");next();}
            case 4->{if(age<3)return;check(click(screen.getGuiLeft()-12,screen.getGuiTop()+75,0)&&carried()==4,"Drawer paper kept the outside cursor stack");slot(1,1);next();}
            case 5->{if(age<3)return;check(carried()==3&&screen.getMenu().getSlot(1).getItem().is(Items.OAK_LOG),"Right-click placed exactly one log into vanilla crafting");slot(36,0);next();}
            case 6->{if(age<3)return;check(carried()==0&&screen.getMenu().getSlot(0).getItem().is(Items.OAK_PLANKS),"Vanilla crafting result updated with live drawer");slot(0,0);next();}
            case 7->{if(age<3)return;check(carried()==4&&screen.getMenu().getCarried().is(Items.OAK_PLANKS),"Native output slot crafted four oak planks");slot(9,0);next();}
            case 8->{if(age<3)return;check(screen.getMenu().getSlot(9).getItem().getCount()==4,"Crafted output moved into an inventory slot");slot(36,0);next();}
            case 9->{if(age<3)return;check(carried()==3,"Remainder stayed in hotbar");check(!click(screen.width-2,screen.height-2,1),"True outside click reached the vanilla menu");next();}
            case 10->{if(age<4)return;check(carried()==2,"Vanilla outside drop removed exactly one log");slot(36,0);screen.onClose();cmd("gamemode creative");next();}
            case 11->{if(age<8||!m.player.isCreative())return;m.setScreen(new InventoryScreen(m.player));next();}
            case 12->{if(age<8)return;check(m.screen instanceof CreativeModeInventoryScreen && toggle().getMessage().getString().equals("Hide abilities"),"Creative inventory restores drawer and original creative screen");System.out.println("DRAWER CHECK COMPLETE");m.stop();next();}
        }
    }
}
