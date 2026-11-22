package ageless.smithclient;

import com.jax.destiny2mc.ageless.materials.GunsmithBlueprint;
import com.jax.destiny2mc.client.ui.GunsmithScreen;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid="smith_client_check",value=Dist.CLIENT)
public final class SmithScreenCheck {
    private static final BlockPos BENCH=new BlockPos(0,60,0);
    private static boolean connecting,setup;
    private static int stage,age;
    private static String pending;
    private static Minecraft m(){return Minecraft.getInstance();}
    private static void cmd(String value){m().player.connection.sendCommand(value);}
    private static void next(){stage++;age=0;}
    private static double scale(){return Math.min(m().screen.width/800d,m().screen.height/450d);}
    private static double x(double logical){return (m().screen.width-800*scale())/2+logical*scale();}
    private static double y(double logical){return (m().screen.height-450*scale())/2+logical*scale();}
    private static void click(double lx,double ly){m().screen.mouseClicked(x(lx),y(ly),0);}
    private static ItemStack wire() {
        for(var stack:m().player.getInventory().items){
            if(stack.getItem() instanceof DestinyGunItem gun && gun.spec.id.equals("wire_rifle"))return stack;
        }
        return ItemStack.EMPTY;
    }
    private static int count(String id) {
        var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return m().player.getInventory().countItem(item);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var game=m();
        if(game.screen instanceof AccessibilityOnboardingScreen a && game.getOverlay()==null){a.onClose();return;}
        if(!connecting && game.screen instanceof TitleScreen && game.getOverlay()==null){
            connecting=true;String h="127.0.0.1:25728";
            ConnectScreen.startConnecting(game.screen,game,ServerAddress.parseString(h),new ServerData("Ageless QA",h,ServerData.Type.OTHER),false,null);
        }
        if(game.player==null||game.level==null||stage>=7)return;
        if(!setup){
            setup=true;game.setScreen(null);game.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;game.options.hideGui=false;
            cmd("gamerule sendCommandFeedback false");cmd("gamerule doMobSpawning false");cmd("forceload add 0 0");cmd("tp @s 0.5 60 -2.5 0 23");cmd("gamemode survival");
            cmd("fill -5 59 -6 5 59 5 minecraft:smooth_stone");cmd("setblock 0 60 0 destiny2mc:gunsmith_fui_workbench");
            cmd("tp @s 0.5 60 -2.5 0 23");cmd("clear @s");cmd("give @s minecraft:iron_ingot 10");
            cmd("give @s minecraft:amethyst_shard 4");cmd("give @s destiny2mc:ether_induction_coil 4");
            cmd("give @s destiny2mc:plundered_ether_alloy 1");cmd("give @s minecraft:copper_ingot 2");
            return;
        }
        game.gui.getChat().clearMessages(false);game.getToasts().clear();
        if(++age>260)throw new IllegalStateException("Gunsmith stage "+stage+" wait expired, screen="+game.screen+", wire="+wire()+", iron="+count("minecraft:iron_ingot")+", cost="+count("destiny2mc:ether_induction_coil"));
        if(stage==0&&age==20){cmd("fill -5 59 -6 5 59 5 minecraft:smooth_stone");cmd("setblock 0 60 0 destiny2mc:gunsmith_fui_workbench");cmd("tp @s 0.5 60 -2.5 0 23");}
        if(stage==0&&age>40&&game.level.getBlockState(BENCH).getBlock() instanceof com.jax.destiny2mc.block.GunsmithBlock&&count("destiny2mc:ether_induction_coil")==4){
            if(GunsmithBlueprint.find(game.player,"wire_rifle")==null)throw new IllegalStateException("Recipe did not synchronize");
            game.player.getInventory().selected=8;
            game.gameMode.useItemOn(game.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,60.65,0),Direction.NORTH,BENCH,false));next();return;
        }
        if(stage==1&&game.screen instanceof GunsmithScreen&&age>8){
            for(int i=0;i<16;i++)game.screen.mouseScrolled(x(150),y(250),0,-1);
            click(123,115+(52-48)*22+10);pending="Ageless-Gunsmith-Craft";next();return;
        }
        if(stage==2&&pending==null&&age>4){click(625,376);next();return;}
        if(stage==3&&!wire().isEmpty()){
            if(count("destiny2mc:ether_induction_coil")!=0||count("minecraft:iron_ingot")!=0||count("minecraft:amethyst_shard")!=0)
                throw new IllegalStateException("Wrong survival cost");
            System.out.println("SMITH UI survival interaction, synchronized recipe and paid Wire Rifle craft");
            click(700,43);click(657,315);click(626,166);pending="Ageless-Gunsmith-Customize";next();return;
        }
        if(stage==4&&pending==null&&age>15){click(700,422);next();return;}
        if(stage==5&&!wire().isEmpty()){
            var gun=(DestinyGunItem)wire().getItem();
            if(WeaponPerkRegistry.getActivePerk(wire(),gun.spec,5)==602){
                if(count("destiny2mc:plundered_ether_alloy")!=0||count("minecraft:copper_ingot")!=0)
                    throw new IllegalStateException("Wrong perk payment");
                System.out.println("SMITH UI paid Backup Mag selected and persisted on client after server mutation");
                System.out.println("SMITH UI COMPLETE");stage=7;game.stop();
            }
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(pending==null||age<3||!(m().screen instanceof GunsmithScreen))return;
        try(var image=Screenshot.takeScreenshot(m().getMainRenderTarget())){
            image.writeToFile(Path.of(System.getProperty("smith.shots"),pending+".png"));
        }
        pending=null;
    }
}
