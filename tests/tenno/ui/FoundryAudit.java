package ageless.tennoaudit;

import com.jax.destiny2mc.client.ui.GunsmithScreen;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.client.TennoFoundryPage;
import com.jax.destiny2mc.tenno.client.TennoInspection;
import com.jax.destiny2mc.tenno.foundry.FoundryNetwork;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import org.lwjgl.glfw.GLFW;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;

/** Drive an ordinary connected game and inspect only synchronized state after each server reply. */
@EventBusSubscriber(modid="tenno_foundry_audit",value=Dist.CLIENT)
public class FoundryAudit {
    private static boolean joining,edgeDone,inspect,canceled;
    private static int step,age,calls;
    private static final BlockPos POS=new BlockPos(0,121,0);
    private static final ResourceLocation AX=ResourceLocation.parse("destiny2mc:ax_52");
    private static List<ItemStack> costs;
    private static void log(String s) {System.out.println("FOUNDRY AUDIT "+s);}
    private static void check(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
    private static void next(int s){age=0;step=s;}
    private static void cmd(String text) {Minecraft.getInstance().player.connection.sendCommand(text);}
    private static void packet(long n){PacketDistributor.sendToServer(new FoundryNetwork.Assemble(AX,n));}
    private static void bring(){cmd("tp @s 0.5 121 -2.5 0 10");}
    private static boolean reply(long n,boolean ok)throws Exception {
        Field f=TennoFoundryPage.class.getDeclaredField("latest");f.setAccessible(true);
        var reply=(FoundryNetwork.Result)f.get(null);
        return reply!=null&&reply.serial()==n&&ok==reply.success();
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void tooltip(RenderTooltipEvent.Pre event){if(inspect){canceled=event.isCanceled();calls++;}}

    private static final class Edge extends Screen {
        Edge(){super(Component.literal("Arsenal audit"));}
        private void tooltip(GuiGraphics g, ItemStack s, boolean expect, int x,int y) {
            inspect=true;calls=0;canceled=false;g.renderTooltip(font,s,x,y);inspect=false;
            check(calls==1&&canceled==expect,"native tooltip "+s+" count="+calls+" canceled="+canceled);
        }
        public void render(GuiGraphics g,int x,int y,float partial) {
            if(edgeDone)return;
            try {
                var m=Minecraft.getInstance();m.options.advancedItemTooltips=false;
                int n=0,nMelee=0;
                Method describe=TennoInspection.class.getDeclaredMethod("describe",ItemStack.class);
                describe.setAccessible(true);
                for(Item item:TennoBlueprints.entries()){
                    ItemStack s=item.getDefaultInstance();
                    s.set(DataComponents.CUSTOM_NAME,Component.literal("Foundry Test Item"));
                    if(item instanceof TennoGunItem gun){
                        GunDataHelper.setInt(s,"wf_mag",1);
                        String stats=describe.invoke(null,s).toString();
                        check(stats.contains("value=1 / "+gun.profile().magazine()+"]")&&stats.contains(gun.profile().note()),stats);
                    }else{
                        var p=((TennoMeleeItem)item).profile();
                        check(s.getMaxDamage()==p.durability(),"native durability "+p.id()+" = "+s.getMaxDamage()+", expected "+p.durability());
                        s.setDamageValue(13);
                        String stats=describe.invoke(null,s).toString();
                        check(stats.contains("value="+p.guardAngle()+"°]")&&stats.contains(""+(s.getMaxDamage()-13)+" / "+s.getMaxDamage()),stats);
                        nMelee++;
                    }
                    tooltip(g,s,true,n%2==0?2:width-2,n%3==0?2:height-2);
                    TennoInspection.draw(g,s,width-2,height-2);
                    n++;
                }
                check(n==33&&nMelee==6,"catalog sizes "+n+" "+nMelee);
                var ordinary=new ItemStack(Items.APPLE);tooltip(g,ordinary,false,3,4);
                var ax=BuiltInRegistries.ITEM.get(AX).getDefaultInstance();
                ax.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP,Unit.INSTANCE);
                tooltip(g,ax,false,1,2);
                ax.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP);m.options.advancedItemTooltips=true;
                tooltip(g,ax,false,1,2);m.options.advancedItemTooltips=false;
                log("All 27 ranged and six melee cards render at screen edges. Their real magazine, guard, durability and technique fields agree with the selected item.");
                log("Standard items, hide-additional and advanced-tooltip contexts retain vanilla tooltips.");
                edgeDone=true;
            }catch(Exception e){throw new IllegalStateException(e);}
        }
    }

    private static void clickAt(Screen screen,int x,int y) {
        float scale=Math.min(screen.width/800f,screen.height/450f);
        double sx=(screen.width-800*scale)/2+x*scale, sy=(screen.height-450*scale)/2+y*scale;
        check(screen.mouseClicked(sx,sy,0),"click "+x+"/"+y);
        screen.mouseReleased(sx,sy,0);
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception {
        Minecraft m=Minecraft.getInstance();
        if(m.screen instanceof AccessibilityOnboardingScreen a&&m.getOverlay()==null){a.onClose();return;}
        if(!joining&&m.screen instanceof TitleScreen&&m.getOverlay()==null){
            joining=true;String addr="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(addr),new ServerData("Foundry audit",addr,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null||m.level==null)return;
        if(++age>380)throw new AssertionError("Timeout stage "+step);
        Item item=BuiltInRegistries.ITEM.get(AX);
        switch(step){
            case 0 -> {
                m.setScreen(new Edge());
                cmd("gamemode creative");cmd("gamerule sendCommandFeedback false");
                cmd("gamerule doMobSpawning false");cmd("gamerule fallDamage false");bring();next(1);
            }
            case 1 -> {
                if(!edgeDone||age<12||!m.level.hasChunkAt(POS))return;
                m.setScreen(null);cmd("fill -3 120 -5 15 120 3 minecraft:stone");
                cmd("setblock 0 121 0 destiny2mc:gunsmith_fui_workbench");bring();
                cmd("gamemode survival");cmd("clear @s");
                costs=TennoBlueprints.cost(m.player,item);
                check(costs.size()==3,"missing built-in AX-52 blueprint");
                for(var cost:costs)cmd("give @s "+BuiltInRegistries.ITEM.getKey(cost.getItem())+" "+2*cost.getCount());
                next(2);
            }
            case 2 -> {
                if(age<15||!TennoBlueprints.canCraft(m.player,item))return;
                packet(100);next(3);
            }
            case 3 -> {
                if(!reply(100,false))return;
                check(m.player.getInventory().countItem(item)==0,"unopened bench");
                m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,121.5,0),Direction.NORTH,POS,false));
                next(4);
            }
            case 4 -> {
                if(age<8||!(m.screen instanceof GunsmithScreen))return;
                clickAt(m.screen,430,40);
                clickAt(m.screen,185,95);
                next(40);
            }
            case 40 -> {
                if(age<3)return;
                Field page=GunsmithScreen.class.getDeclaredField("foundry");page.setAccessible(true);
                var foundry=(TennoFoundryPage)page.get(m.screen);
                Field list=TennoFoundryPage.class.getDeclaredField("entries");list.setAccessible(true);
                var entries=(List<?>)list.get(foundry);
                if(entries.size()!=6)return;
                check(entries.stream().allMatch(TennoMeleeItem.class::isInstance),"melee filter");
                check(m.screen.keyPressed(GLFW.GLFW_KEY_LEFT,0,0),"handled left key");
                next(41);
            }
            case 41 -> {
                if(age<3)return;
                Field page=GunsmithScreen.class.getDeclaredField("foundry");page.setAccessible(true);
                var foundry=(TennoFoundryPage)page.get(m.screen);
                Field list=TennoFoundryPage.class.getDeclaredField("entries");list.setAccessible(true);
                var entries=(List<?>)list.get(foundry);
                if(entries.size()!=27)return;
                check(entries.stream().allMatch(TennoGunItem.class::isInstance),"ranged filter");
                log("Mouse and keyboard filters on the live bench reveal all six melee and 27 ranged blueprints.");
                packet(101);packet(101);next(5);
            }
            case 5 -> {
                if(age<10||!reply(101,true)||m.player.getInventory().countItem(item)==0)return;
                check(m.player.getInventory().countItem(item)==1,"repeat produced more than one result");
                for(var cost:costs)check(m.player.getInventory().countItem(cost.getItem())==cost.getCount(),"unexpected remaining materials");
                log("One repeated request consumed exactly one recipe and granted one weapon despite materials for two.");
                m.setScreen(null);cmd("tp @s 12.5 121 -2.5");next(6);
            }
            case 6 -> {
                if(m.player.getX()<11)return;
                packet(102);next(7);
            }
            case 7 -> {
                if(!reply(102,false))return;
                check(m.player.getInventory().countItem(item)==1,"out of reach craft");
                for(var cost:costs)check(m.player.getInventory().countItem(cost.getItem())==cost.getCount(),"lost materials out of reach");
                log("Unopened and out-of-reach stations declined the request without debiting any materials.");
                log("COMPLETE");m.stop();next(8);
            }
        }
    }
}
