package ageless.mods.qa;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** Development-only game driver. Commands provision inputs, vanilla interaction packets
 * craft/open them, and public replicated profile/stack views verify the resulting state. */
@EventBusSubscriber(modid="ageless_mod_qa",value=Dist.CLIENT)
public final class UpgradeClientCheck {
    private static final BlockPos BENCH=new BlockPos(0,121,0), TABLE=new BlockPos(2,121,0);
    private static final Path DIR=Path.of(System.getProperty("binder.shots"));
    private static final boolean PREFLIGHT=Boolean.getBoolean("ageless.preflight");
    private static final boolean MC_BENCH=Boolean.getBoolean("ageless.mcbench");
    private static int phase,age,frame,opens,sinceOpen;
    private static boolean joined;
    private static String capture="", chosen="",hover="",sourcePack="";
    private static int countBefore,rankSize,copyCount,cardIndex;
    private static InventoryScreen inventory;
    private static Minecraft mc(){return Minecraft.getInstance();}
    private static void set(int next){phase=next;age=0;}
    private static void command(String command){mc().player.connection.sendCommand(command);}
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);log(message);}
    private static void log(String message){System.out.println("UPGRADE QA "+message);}
    private static void shot(String slug){capture=slug;frame=0;}
    private static ResourceLocation key(String s){return s.contains(":")?ResourceLocation.parse(s):ResourceLocation.fromNamespaceAndPath("destiny2mc",s);}
    private static Item item(String s){return BuiltInRegistries.ITEM.get(key(s));}
    private static String id(ItemStack s){return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();}
    private static int count(String name){return mc().player.getInventory().countItem(item(name));}
    private static ItemStack hot(int n){return mc().player.getInventory().getItem(n);}
    private static Class<?> modsType(String name)throws Exception{return Class.forName("com.jax.destiny2mc.tenno.mods."+name);}
    @SuppressWarnings("unchecked") private static Map<String,Integer> collection()throws Exception{
        return (Map<String,Integer>)modsType("ModCollection").getMethod("ranks",Player.class).invoke(null,mc().player);
    }
    private static List<String> installed(ItemStack stack)throws Exception{
        var cards=(List<?>)modsType("WeaponMods").getMethod("installed",ItemStack.class).invoke(null,stack);
        var names=new ArrayList<String>();for(var card:cards)names.add((String)card.getClass().getMethod("id").invoke(card));return names;
    }
    private static int rank(String id)throws Exception{return collection().getOrDefault(id,-1);}
    // Wait for the acknowledgement that enables the actual widgets. Server tick pacing varies on CI.
    private static Object page() throws Exception {
        var type=Class.forName("com.jax.destiny2mc.tenno.client.TennoModPage");
        var ref=type.getDeclaredField("focused");ref.setAccessible(true);return ((WeakReference<?>)ref.get(null)).get();
    }
    private static long state(Object page,String field) throws Exception {
        var f=page.getClass().getDeclaredField(field);f.setAccessible(true);return ((Number)f.get(page)).longValue();
    }
    private static boolean interactive(int slot) throws Exception {
        var p=page();return p!=null && state(p,"slot")==slot && state(p,"pending")==0 && state(p,"token")!=0;
    }
    private static void move(double x,double y){
        var w=mc().getWindow();double px=x*w.getScreenWidth()/w.getGuiScaledWidth(),py=y*w.getScreenHeight()/w.getGuiScaledHeight();
        GLFW.glfwSetCursorPos(w.getWindow(),px,py);
        var cb=GLFW.glfwSetCursorPosCallback(w.getWindow(),null);GLFW.glfwSetCursorPosCallback(w.getWindow(),cb);cb.invoke(w.getWindow(),px,py);
    }
    private static double[] point(double x,double y){var s=mc().screen;float scale=Math.min(s.width/800f,s.height/450f);return new double[]{(s.width-800*scale)/2+x*scale,(s.height-450*scale)/2+y*scale};}
    private static void hover(double x,double y){var pt=point(x,y);move(pt[0],pt[1]);}
    private static boolean click(double x,double y){var pt=point(x,y);move(pt[0],pt[1]);var screen=mc().screen;boolean ok=screen.mouseClicked(pt[0],pt[1],0);screen.mouseReleased(pt[0],pt[1],0);return ok;}
    private static void press(int key){mc().screen.keyPressed(key,0,0);mc().screen.keyReleased(key,0,0);}
    private static void close(){if(mc().screen!=null)mc().screen.onClose();mc().setScreen(null);}
    private static void interact(BlockPos pos){
        var m=mc();m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
    }
    private static void hold(int slot){mc().player.getInventory().selected=slot;}
    private static void use(){var m=mc();m.gameMode.useItem(m.player,InteractionHand.MAIN_HAND);}
    private static boolean selectHot(String name){
        for(int i=0;i<9;i++)if(hot(i).is(item(name))){hold(i);return true;}return false;
    }
    private static void hoverItem(String slug){
        var s=inventory.getMenu().slots.stream().filter(v->id(v.getItem()).equals(slug)).findFirst().orElseThrow();
        move(inventory.getGuiLeft()+s.x+7,inventory.getGuiTop()+s.y+7);
    }
    private static RecipeHolder<?> recipe(String name){
        return mc().level.getRecipeManager().byKey(key(name)).orElseThrow(()->new AssertionError("missing recipe "+name));
    }
    private static void fillRecipe(String name){
        var p=mc().player;mc().gameMode.handlePlaceRecipe(p.containerMenu.containerId,recipe(name),false);
    }
    private static void takeOutput(){var p=mc().player;mc().gameMode.handleInventoryMouseClick(p.containerMenu.containerId,0,0,ClickType.QUICK_MOVE,p);}
    private static ItemStack result(){return mc().player.containerMenu.getSlot(0).getItem();}
    private static void world(){
        command("gamerule sendCommandFeedback false");command("gamerule doMobSpawning false");command("gamerule fallDamage false");
        command("gamerule doDaylightCycle false");command("time set 4500");command("weather clear");
        command("gamemode creative");command("forceload add -16 -16 31 31");command("tp @s 0.5 125 -2.5 0 5");
    }
    private static void prepare(){
        command("fill -9 120 -8 9 120 10 minecraft:polished_deepslate");
        command("fill -5 121 5 5 125 5 minecraft:tuff_bricks");
        for(int x:new int[]{-4,0,4}){command("fill "+x+" 121 4 "+x+" 124 4 minecraft:waxed_cut_copper");command("setblock "+x+" 125 4 sea_lantern");}
        command("setblock 0 121 0 destiny2mc:"+(MC_BENCH?"gunsmith_mc_workbench":"gunsmith_fui_workbench"));
        command("setblock 2 121 0 crafting_table");command("tp @s 0.5 121 -2.5 0 9");command("gamemode survival");
        command("clear @s");command("xp set @s 0 levels");command("xp set @s 0 points");
        command("give @s paper 2");command("give @s copper_ingot");command("give @s redstone");command("give @s iron_ingot");
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void tooltip(RenderTooltipEvent.Pre event){if(!event.getItemStack().isEmpty())hover=id(event.getItemStack());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post tick)throws Exception{
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen intro && m.getOverlay()==null){intro.onClose();return;}
        if(!joined&&m.screen instanceof TitleScreen&&m.getOverlay()==null){
            joined=true;String a="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(a),new ServerData("Upgrade QA",a,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null||m.level==null)return;
        if(++age>350)throw new AssertionError("timeout phase="+phase+" screen="+m.screen+" held="+id(m.player.getMainHandItem())+" XP="+m.player.experienceLevel+" chosen="+chosen);
        if(!capture.isEmpty())return;
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        switch(phase){
            case 0->{m.setScreen(null);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;world();set(1);}
            case 1->{if(age<12||!m.level.hasChunkAt(TABLE))return;prepare();set(2);}
            case 2->{if(age<8||count("minecraft:paper")!=2||m.player.distanceToSqr(.5,121,-2.5)>3)return;hold(8);interact(TABLE);set(3);}
            case 3->{
                if(!(m.screen instanceof CraftingScreen)){if(age%30==0)interact(TABLE);return;}
                if(PREFLIGHT){fillRecipe("minecraft:paper");log("dedicated connection and vanilla crafting screen ready");set(80);return;}
                for(String n:List.of("mod_pack","primary_mod_pack","secondary_mod_pack","melee_mod_pack")){
                    var stack=recipe(n).value().getResultItem(m.level.registryAccess());
                    check(stack.is(item(n)),"synced crafting recipe for "+n);
                    check(m.getItemRenderer().getModel(stack,m.level,m.player,0)!=m.getModelManager().getMissingModel(),"baked pack artwork "+n);
                }
                check(collection().isEmpty(),"new survival player has an empty collection");
                command("recipe give @s destiny2mc:mod_pack");command("recipe give @s destiny2mc:primary_mod_pack");set(4);
            }
            case 4->{if(age<6)return;fillRecipe("mod_pack");set(5);}
            case 5->{if(!result().is(item("mod_pack"))){if(age%25==0)fillRecipe("mod_pack");return;}shot("01-Craft-Pack");set(6);}
            case 6->{takeOutput();set(7);}
            case 7->{if(age<5||count("mod_pack")!=1||count("minecraft:paper")!=0||count("minecraft:copper_ingot")!=0||count("minecraft:redstone")!=0)return;check(count("minecraft:paper")==0&&count("minecraft:copper_ingot")==0&&count("minecraft:redstone")==0,"ordinary table consumed paper, copper and redstone for one pack");fillRecipe("primary_mod_pack");set(8);}
            case 8->{if(!result().is(item("primary_mod_pack"))){if(age%25==0)fillRecipe("primary_mod_pack");return;}takeOutput();set(9);}
            case 9->{if(age<5||count("primary_mod_pack")!=1||count("mod_pack")!=0||count("minecraft:iron_ingot")!=0)return;check(count("mod_pack")==0&&count("minecraft:iron_ingot")==0,"focused crafting consumed the generic pack and iron");close();set(10);}
            case 10->{if(age<5||!selectHot("primary_mod_pack"))return;use();set(11);}
            case 11->{if(age<12||collection().size()!=1||count("primary_mod_pack")!=0)return;check(collection().values().iterator().next()==0,"opening a crafted survival pack grants one new rank-zero card");shot("02-Reveal");set(12);}
            case 12->{if(age<4)return;command("give @s destiny2mc:primary_mod_pack 24");set(13);}
            case 13->{if(count("primary_mod_pack")==0||!selectHot("primary_mod_pack"))return;rankSize=collection().size();countBefore=count("primary_mod_pack");use();set(14);}
            case 14->{
                int size=collection().size();
                if(size>rankSize){
                    if(count("primary_mod_pack")!=countBefore-1)return;
                    check(size==rankSize+1&&count("primary_mod_pack")==countBefore-1,"a new card consumed exactly one primary pack");opens++;sinceOpen=0;set(13);return;
                }
                if(age<20)return;
                if(++sinceOpen<3){set(13);return;}
                check(opens>=2 && count("primary_mod_pack")==countBefore,"completed primary collection keeps unopened spare packs");
                if(Boolean.getBoolean("ageless.packonly")){set(80);return;}
                command("clear @s");command("item replace entity @s hotbar.0 with destiny2mc:braton");command("item replace entity @s hotbar.1 with destiny2mc:soma");command("item replace entity @s hotbar.2 with destiny2mc:heat_sword");
                command("item replace entity @s hotbar.3 with destiny2mc:mod_pack");command("item replace entity @s hotbar.4 with destiny2mc:primary_mod_pack");command("item replace entity @s hotbar.5 with destiny2mc:secondary_mod_pack");command("item replace entity @s hotbar.6 with destiny2mc:melee_mod_pack");set(15);
            }
            case 15->{if(!hot(0).is(item("braton"))||count("melee_mod_pack")!=1)return;hold(8);interact(BENCH);set(16);}
            case 16->{if(m.screen==null){if(age%20==0)interact(BENCH);return;}if(age<8)return;check(click(450,40),"native gunsmith opens Warframe tab");set(17);}
            case 17->{if(age<5)return;check(click(324,40),"native Gunsmith opens weapon Mods");set(18);}
            case 18->{if(age<8)return;click(80,118);click(250,272);hover(781,405);shot("03-Collection");set(19);}
            case 19->{if(age<5)return;click(632,368);set(20);}
            case 20->{if(age<8)return;check(m.player.experienceLevel==0&&collection().values().stream().allMatch(v->v==0),"rank request with zero XP leaves every card at rank zero");click(632,325);set(21);}
            case 21->{if(installed(hot(0)).isEmpty())return;chosen=installed(hot(0)).getFirst();check(rank(chosen)==0,"installing owned rank-zero "+chosen+" writes a real weapon socket");command("experience set @s 8 levels");set(22);}
            case 22->{if(m.player.experienceLevel!=8||!interactive(0))return;click(632,368);set(23);}
            case 23->{if(rank(chosen)!=1 || m.player.experienceLevel!=7||!interactive(0))return;check(true,"rank one spent exactly one Minecraft level");press(GLFW.GLFW_KEY_F);set(24);}
            case 24->{if(rank(chosen)!=2 || m.player.experienceLevel!=5||!interactive(0))return;check(true,"rank two spent exactly two Minecraft levels");hover(783,405);shot("04-Fusion");set(25);}
            case 25->{click(84,148);set(51);}
            case 51->{if(!interactive(1))return;click(250,272);press(GLFW.GLFW_KEY_ENTER);set(26);}
            case 26->{if(!installed(hot(1)).contains(chosen)||!interactive(1))return;check(installed(hot(0)).contains(chosen)&&rank(chosen)==2,"one rank-two unlock serves Braton and Soma simultaneously");click(632,325);set(27);}
            case 27->{if(installed(hot(1)).contains(chosen)||!interactive(1))return;check(installed(hot(0)).contains(chosen)&&rank(chosen)==2,"removing a socket on Soma preserves Braton and owned rank");click(84,118);cardIndex=1;set(52);}
            case 52->{if(!interactive(0))return;click(250+114*(cardIndex%3),272+65*(cardIndex/3));click(632,325);set(53);}
            case 53->{
                if(installed(hot(0)).size()!=cardIndex+1||!interactive(0))return;
                if(++cardIndex<6){set(52);return;}
                check(m.player.experienceLevel==5,"six compatible Braton cards equipped with no level or capacity charge");
                click(250,120);hover(780,407);shot("05-All-Equipped");set(54);
            }
            case 54->{close();inventory=new InventoryScreen(m.player);m.setScreen(inventory);set(28);}
            case 28->{if(age<6)return;hoverItem("braton");set(29);}
            case 29->{if(!hover.equals("braton")){hoverItem("braton");return;}if(age<7)return;shot("05-Weapon-Inspect");set(30);}
            case 30->{hoverItem("primary_mod_pack");set(31);}
            case 31->{if(!hover.equals("primary_mod_pack")){hoverItem("primary_mod_pack");return;}if(age<7)return;shot("06-Pack-Inspect");set(32);}
            case 32->{close();set(80);}
            case 80->{check(true,"COMPLETE");m.stop();set(99);}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        if(capture.isEmpty()||++frame<3)return;
        try(var raw=Screenshot.takeScreenshot(mc().getMainRenderTarget())){raw.writeToFile(DIR.resolve(capture+".png"));}
        capture="";
    }
}
