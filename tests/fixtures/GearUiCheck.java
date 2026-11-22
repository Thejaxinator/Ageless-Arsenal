package ageless.gearqa;

import com.jax.destiny2mc.binder.BinderCatalog;
import com.jax.destiny2mc.binder.BinderClientState;
import com.jax.destiny2mc.binder.client.BinderProgressView;
import com.jax.destiny2mc.binder.client.EnergyBinderScreen;
import com.jax.destiny2mc.client.HudConfig;
import com.jax.destiny2mc.client.ui.GunsmithScreen;
import net.minecraft.resources.ResourceLocation;
import com.jax.destiny2mc.client.HudSettingsScreen;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import java.nio.file.Path;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** Native graphical client fixture. The cursor moves through GLFW and normal menu rendering.
 * Run against an isolated localhost server using -Dbinder.port and -Dbinder.shots. */
@EventBusSubscriber(modid="ageless_gear_ui",value=Dist.CLIENT)
public final class GearUiCheck {
    private static final String[] POWERS={"marksmans_dodge","healing_rift","towering_barricade","solar_grenade","triple_jump","ember_of_solace"};
    private static final String[] SKINS={"Destiny","Compact","Minimal","BL2","BL4","Tenno"};
    private static final String[] TEST_ITEMS={"ace_of_spades","battlecage_of_kabr","mask_of_bakris","bread"};
    private static final String[] ITEM_SHOTS={"Weapon-Card","Armor-Card","Exotic-Card","Vanilla-Tooltip"};
    private static final Path SHOTS=Path.of(System.getProperty("binder.shots"));
    private static final BlockPos BINDER=new BlockPos(0,121,0);
    private static final boolean PREFLIGHT=Boolean.getBoolean("binder.preflight");
    private static Minecraft mc(){return Minecraft.getInstance();}
    private static boolean connecting, started;
    private static int stage, age, index, step, style, frame, itemIndex;
    private static String pending="", lastItemHover="";
    private static boolean lastItemCanceled;
    private static String frameTooltip="";
    private static boolean frameCanceled;
    private static InventoryScreen inventory;

    private static void jump(int next){stage=next;age=0;}
    private static void command(String value){mc().player.connection.sendCommand(value);}
    private static void capture(String name){pending=name;frame=0;}
    private static void require(boolean condition,String message){
        if(!condition)throw new AssertionError(message);
        System.out.println("GEAR UI "+message);
    }
    private static String itemId(ItemStack stack){return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();}
    private static AbstractWidget widget(String prefix){
        return mc().screen.children().stream().filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast)
            .filter(w->w.active && w.visible && w.getMessage().getString().startsWith(prefix)).findFirst()
            .orElseThrow(()->new AssertionError("Missing "+prefix+" on "+mc().screen+" "+mc().screen.children().stream().filter(AbstractWidget.class::isInstance).map(w->((AbstractWidget)w).getMessage().getString()).collect(Collectors.toList())));
    }
    private static void click(String prefix){
        var screen=mc().screen;var w=widget(prefix);double x=w.getX()+w.getWidth()/2d,y=w.getY()+w.getHeight()/2d;
        if(!screen.mouseClicked(x,y,0))throw new AssertionError("widget did not accept "+prefix);
        screen.mouseReleased(x,y,0);
    }
    private static void move(double x,double y){
        var w=mc().getWindow();
        double px=x*w.getScreenWidth()/w.getGuiScaledWidth(), py=y*w.getScreenHeight()/w.getGuiScaledHeight();
        GLFW.glfwSetCursorPos(w.getWindow(),px,py);
        // Headless GLFW windows can be unfocused. Deliver the stored native callback too,
        // so Minecraft's actual MouseHandler receives the position on every test display.
        var callback=GLFW.glfwSetCursorPosCallback(w.getWindow(),null);
        GLFW.glfwSetCursorPosCallback(w.getWindow(),callback);
        callback.invoke(w.getWindow(),px,py);
    }
    private static void hoverWidget(String name){
        var w=widget(name);move(w.getX()+w.getWidth()/2d,w.getY()+w.getHeight()/2d);
    }
    private static void hoverSlot(String name){
        Slot slot=inventory.getMenu().slots.stream().filter(s->itemId(s.getItem()).equals(name)).findFirst()
            .orElseThrow(()->new AssertionError("no slot with "+name));
        move(inventory.getGuiLeft()+slot.x+8,inventory.getGuiTop()+slot.y+8);
        lastItemHover="";
    }
    private static void prepareWorld(){
        command("gamerule sendCommandFeedback false");command("gamerule doDaylightCycle false");
        command("gamerule doWeatherCycle false");command("gamerule doMobSpawning false");
        command("gamerule fallDamage false");command("weather clear");command("time set noon");
        command("gamemode creative");command("forceload add -16 -16 31 31");command("tp @s 0.5 132 -2.5 0 0");
    }
    private static void placeStation(){
        command("fill -14 120 -12 14 120 25 minecraft:moss_block");
        command("fill -2 120 -12 2 120 13 minecraft:mossy_stone_bricks");
        command("place template destiny2mc:frontier/vex_conduit -12 120 7");
        command("setblock 0 121 0 destiny2mc:energy_binder");
        command("setblock 2 121 0 destiny2mc:gunsmith_fui_workbench");
        command("tp @s 0.5 121 -2.5 0 12");command("gamemode survival");command("clear @s");
        command("experience set @s 50 levels");command("give @s destiny2mc:mote_of_light 160");
    }
    private static void giveTestEquipment(){
        // The stack has a legal nondefault reload masterwork, major spec and seven loaded rounds.
        var spec=((DestinyGunItem) BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc","ace_of_spades"))).spec;
        int trait1=WeaponPerkRegistry.getPoolOption(spec.index,2,1), trait2=WeaponPerkRegistry.getPoolOption(spec.index,3,2);
        command("item replace entity @s hotbar.0 with destiny2mc:ace_of_spades[minecraft:custom_data={d2_pk2:"+trait1+",d2_pk3:"+trait2+",d2_pk4:503,d2_pk5:604,d2_mag:7}]");
        command("item replace entity @s hotbar.1 with destiny2mc:battlecage_of_kabr");
        command("item replace entity @s hotbar.2 with destiny2mc:mask_of_bakris");
        command("item replace entity @s hotbar.3 with minecraft:bread 3");
    }
    private static void sendShift(boolean down)throws Exception{
        int code=new ProcessBuilder("python3",System.getProperty("gear.shift.helper"),down?"down":"up").inheritIO().start().waitFor();
        if(code!=0)throw new AssertionError("Could not deliver Shift to the test X display");
    }
    private static boolean atStation(){
        return BuiltInRegistries.BLOCK.getKey(mc().level.getBlockState(BINDER).getBlock()).getPath().equals("energy_binder")
            && mc().player.distanceToSqr(BINDER.getX()+.5,BINDER.getY()+.5,BINDER.getZ()+.5)<24;
    }
    private static void nextTheme(){mc().setScreen(new HudSettingsScreen());click("Theme");click("Done");}
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void observeTooltip(RenderTooltipEvent.Pre event){
        if(!event.getItemStack().isEmpty()){
            lastItemHover=itemId(event.getItemStack());lastItemCanceled=event.isCanceled();
            frameTooltip=lastItemHover;frameCanceled=lastItemCanceled;
        }
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen a && m.getOverlay()==null){a.onClose();return;}
        if(!connecting && m.screen instanceof TitleScreen && m.getOverlay()==null){
            connecting=true;String a="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(a),new ServerData("Gear QA",a,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null || m.level==null)return;
        if(!started){started=true;m.setScreen(null);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;prepareWorld();return;}
        if(++age>430)throw new AssertionError("stage="+stage+" step="+step+" index="+index+" screen="+m.screen+" hovered="+lastItemHover);
        if(!pending.isEmpty())return;
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        var s=BinderClientState.snapshot();
        switch(stage){
            case 0->{
                if(age<28 || !m.level.hasChunkAt(BINDER))return;
                placeStation();jump(1);
            }
            case 1->{
                if(age<22 || !atStation())return;m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,121.5,.0),Direction.NORTH,BINDER,false));jump(2);
            }
            case 2->{
                if(!(m.screen instanceof EnergyBinderScreen)){
                    if(age%25==0 && m.screen==null && atStation())m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,121.5,0),Direction.NORTH,BINDER,false));
                    return;
                }
                var a=BinderCatalog.get(POWERS[index]);
                if(step==0){BinderClientState.purchase(a.id());step++;return;}
                if(step==1){if(!s.unlocked().contains(a.id()))return;BinderClientState.equip(a.id(),index);step++;return;}
                if(!(index<4?s.active().get(index):s.passive().get(index-4)).equals(a.id()))return;
                step=0;if(++index<6)return;
                click("Class abilities");hoverWidget("Healing Rift,");jump(3);
            }
            case 3->{if(age<14)return;capture("Ability-Binder");jump(4);}
            case 4->{
                m.screen.onClose();giveTestEquipment();command("tp @s -6.5 121 -8.5 -19 0");jump(5);
            }
            case 5->{
                if(age<15 || !(m.player.getMainHandItem().getItem() instanceof DestinyGunItem gun))return;
                require(WeaponPerkRegistry.getActivePerk(m.player.getMainHandItem(),gun.spec,4)==503 && WeaponPerkRegistry.getActivePerk(m.player.getMainHandItem(),gun.spec,5)==604,"weapon is carrying the nondefault reload masterwork and Major spec");
                HudConfig.ensureLoaded();if(HudConfig.hudStyle!=0){HudConfig.hudStyle=0;HudConfig.save();}
                jump(6);
            }
            case 6->{if(age<3)return;require(HudConfig.hudStyle==style,"captured live "+SKINS[style]+" ability row with gun");capture("HUD-"+SKINS[style]);jump(7);}
            case 7->{
                if(++style<6){nextTheme();jump(6);return;}nextTheme();
                inventory=new InventoryScreen(m.player);m.setScreen(inventory);move(440,245);jump(8);
            }
            case 8->{
                if(age<8)return;
                if(HudConfig.inventoryDrawerOpen){click("Hide abilities");jump(8);return;}
                var w=widget("Show abilities");
                if(!PREFLIGHT)require(w.getWidth()<=18 && w.getHeight()<=25 && w.getX()+w.getWidth()<=inventory.getGuiLeft()+1,"closed notch is a small attached left-edge control");
                capture("Notch-Closed");jump(9);
            }
            case 9->{hoverWidget("Show abilities");jump(25);}
            case 25->{if(age<7)return;capture("Notch-Hover");jump(26);}
            case 26->{click("Show abilities");move(450,245);jump(10);}
            case 10->{
                if(age<9)return;var w=widget("Hide abilities");
                require(m.screen==inventory && HudConfig.inventoryDrawerOpen,"notch opens the drawer on the same InventoryScreen");move(450,245);capture("Notch-Open");jump(11);
            }
            case 11->{
                var w=widget("Hide abilities");
                int x=PREFLIGHT?inventory.getGuiLeft()-130:w.getX()+w.getWidth()+13;
                move(x,inventory.getGuiTop()+19+16+6);jump(12);
            }
            case 12->{if(age<10)return;capture("Ability-Inventory");jump(13);}
            case 13->{click("Hide abilities");move(450,245);jump(14);}
            case 14->{
                if(age<8)return;
                if(PREFLIGHT && (itemIndex==1 || itemIndex==2)){itemIndex=3;}
                hoverSlot(TEST_ITEMS[itemIndex]);jump(15);
            }
            case 15->{
                if(age<10)return;
                if(inventory.getSlotUnderMouse()==null || !itemId(inventory.getSlotUnderMouse().getItem()).equals(TEST_ITEMS[itemIndex])){
                    if(age%50==0){var w=m.getWindow();System.out.println("Cursor "+m.mouseHandler.xpos()+","+m.mouseHandler.ypos()+" window "+w.getScreenWidth()+"x"+w.getScreenHeight()+" hovered "+inventory.getSlotUnderMouse());hoverSlot(TEST_ITEMS[itemIndex]);}
                    return;
                }
                require(lastItemHover.equals(TEST_ITEMS[itemIndex]),"real item-tooltip pipeline observed "+TEST_ITEMS[itemIndex]);
                if(!PREFLIGHT)require(lastItemCanceled==(itemIndex!=3),"Destiny card intercept respects item ownership for "+TEST_ITEMS[itemIndex]);
                capture(ITEM_SHOTS[itemIndex]);jump(itemIndex==0 && !PREFLIGHT && System.getProperty("gear.shift.helper")!=null ? 22:16);
            }
            case 16->{
                if(++itemIndex<TEST_ITEMS.length){jump(14);return;}
                m.screen.onClose();command("tp @s 2.5 121 -2.5 0 10");jump(17);
            }
            case 17->{
                if(age<8)return;
                m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(2.5,121.5,0),Direction.NORTH,new BlockPos(2,121,0),false));jump(18);
            }
            case 18->{
                if(!(m.screen instanceof GunsmithScreen g))return;
                g.keyPressed(GLFW.GLFW_KEY_TAB,0,0);
                float scale=Math.min(g.width/800f,g.height/450f);
                move((g.width-800*scale)/2+100*scale,(g.height-450*scale)/2+125*scale);
                jump(19);
            }
            case 19->{if(age<10)return;capture("Gunsmith-Card");jump(20);}
            case 20->{System.out.println("GEAR UI COMPLETE progression="+(BinderProgressView.read()!=null));m.stop();jump(99);}
            case 22->{sendShift(true);hoverSlot("ace_of_spades");jump(23);}
            case 23->{
                if(age<12 || !Screen.hasShiftDown())return;
                if(age%18==0 && (!lastItemHover.equals("ace_of_spades") || lastItemCanceled)){
                    System.out.println("Shift cursor refresh "+lastItemHover+" / "+lastItemCanceled+", next "+inventory.getSlotUnderMouse());
                    hoverSlot("ace_of_spades"); return;
                }
                if(!lastItemHover.equals("ace_of_spades") || lastItemCanceled)return;
                require(lastItemHover.equals("ace_of_spades") && !lastItemCanceled,"Shift reveals complete original items/perks");
                capture("Weapon-Expanded");jump(24);
            }
            case 24->{sendShift(false);jump(16);}
            default->{}
        }
    }
    @SubscribeEvent public static void beforeFrame(RenderFrameEvent.Pre event){frameTooltip="";}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        if(pending.isEmpty() || ++frame<3)return;
        String expected=switch(pending){
            case "Weapon-Card","Weapon-Expanded" -> "ace_of_spades";
            case "Armor-Card" -> "battlecage_of_kabr";
            case "Exotic-Card" -> "mask_of_bakris";
            case "Vanilla-Tooltip" -> "bread";
            default -> "";
        };
        if(!expected.isEmpty() && (!expected.equals(frameTooltip) || (!PREFLIGHT && frameCanceled!=(pending.endsWith("-Card"))))){
            if(frame%10==0)hoverSlot(expected);
            return;
        }
        try(var image=Screenshot.takeScreenshot(mc().getMainRenderTarget())){image.writeToFile(SHOTS.resolve(pending+".png"));}
        pending="";
    }
}
