package ageless.navigation.qa;

import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="ageless_navigation_qa",value=Dist.CLIENT)
public final class NavigationClientCheck {
    private static final BlockPos BENCH=new BlockPos(0,121,0);
    private static final Path SHOTS=Path.of(System.getProperty("binder.shots"));
    private static final boolean MC_BENCH=Boolean.getBoolean("ageless.mcbench");
    private static final boolean CREATIVE_ONLY=Boolean.getBoolean("nav.creativeonly");
    private static String capture="";
    private static int frame, phase, age, oldPerk, newPerk;
    private static boolean joined;
    private static Item blueprint;
    private static List<ItemStack> ingredients;
    private static CreativeModeInventoryScreen creative;
    private static Set<Item> warframe=new LinkedHashSet<>();
    private static final List<String> PACKS=List.of("mod_pack","primary_mod_pack","secondary_mod_pack","melee_mod_pack");

    private static Minecraft mc(){return Minecraft.getInstance();}
    private static void step(int value){phase=value;age=0;}
    private static void command(String s){mc().player.connection.sendCommand(s);}
    private static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);System.out.println("NAV QA "+msg);}
    private static void shot(String name){capture=name;frame=0;}
    private static ResourceLocation key(String id){return id.contains(":")?ResourceLocation.parse(id):ResourceLocation.fromNamespaceAndPath("destiny2mc",id);}
    private static Item item(String id){return BuiltInRegistries.ITEM.get(key(id));}
    private static CreativeModeTab tab(String id){return BuiltInRegistries.CREATIVE_MODE_TAB.get(key(id));}
    private static String id(Item item){return BuiltInRegistries.ITEM.getKey(item).toString();}
    private static int count(Item item){return mc().player.getInventory().countItem(item);}
    private static ItemStack hot(int n){return mc().player.getInventory().getItem(n);}
    private static void equip(int n,String id,int qty){command("item replace entity @s hotbar."+n+" with destiny2mc:"+id+" "+qty);}
    private static void open(){var m=mc();m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(BENCH),Direction.NORTH,BENCH,false));}
    private static void close(){var m=mc();if(m.screen!=null)m.screen.onClose();m.setScreen(null);}
    private static void move(double x,double y){
        var w=mc().getWindow();
        double px=x*w.getScreenWidth()/w.getGuiScaledWidth(),py=y*w.getScreenHeight()/w.getGuiScaledHeight();
        GLFW.glfwSetCursorPos(w.getWindow(),px,py);
        var callback=GLFW.glfwSetCursorPosCallback(w.getWindow(),null);
        GLFW.glfwSetCursorPosCallback(w.getWindow(),callback);callback.invoke(w.getWindow(),px,py);
    }
    private static boolean tap(double x,double y){move(x,y);var s=mc().screen;boolean ok=s.mouseClicked(x,y,0);s.mouseReleased(x,y,0);return ok;}
    private static boolean click(double x,double y){
        var s=mc().screen;float scale=Math.min(s.width/800f,s.height/450f);
        return tap((s.width-800*scale)/2+x*scale,(s.height-450*scale)/2+y*scale);
    }
    private static void clearHover(){move(3,mc().screen.height-4);}
    private static void expectPage(int franchise,int tool,String label)throws Exception{
        var screen=mc().screen;
        int f=(Integer)screen.getClass().getMethod("activeFranchise").invoke(screen);
        int t=(Integer)screen.getClass().getMethod("activeTool").invoke(screen);
        check(f==franchise&&t==tool,label+" active ("+f+","+t+")");
        clearHover();
    }
    private static void stageWorld(){
        command("gamerule sendCommandFeedback false");command("gamerule doMobSpawning false");command("gamerule fallDamage false");
        command("gamerule doDaylightCycle false");command("gamerule doWeatherCycle false");command("time set 4200");command("weather clear");
        command("gamemode creative");command("forceload add -16 -16 31 31");command("tp @s 0.5 126 -2.5 0 6");
    }
    private static void prepare(){
        command("fill -8 120 -8 8 120 10 polished_deepslate");command("fill -7 121 8 7 126 8 tuff_bricks");
        for(int x:new int[]{-5,0,5}){command("fill "+x+" 121 7 "+x+" 125 7 waxed_cut_copper");command("setblock "+x+" 126 7 sea_lantern");}
        command("setblock 0 121 0 destiny2mc:"+(MC_BENCH?"gunsmith_mc_workbench":"gunsmith_fui_workbench"));
        command("tp @s 0.5 121 -2.5 0 4");command("gamemode survival");command("clear @s");
        equip(0,"ace_of_spades",1);equip(1,"braton",1);equip(2,"heat_sword",1);equip(3,"primary_mod_pack",14);
        command("give @s destiny2mc:plundered_ether_alloy");command("give @s copper_ingot 2");command("xp set @s 8 levels");
    }
    private static int perk(){
        var st=hot(0);var gun=(DestinyGunItem)st.getItem();
        return WeaponPerkRegistry.getActivePerk(st,gun.spec,0);
    }
    private static String installed(){
        return WeaponMods.installed(hot(1)).stream().map(card->card.id()).findFirst().orElse("");
    }
    private static Object field(Object value,Class<?> type,String name)throws Exception{
        var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(value);
    }
    private static boolean modReady()throws Exception{
        var t=Class.forName("com.jax.destiny2mc.tenno.client.TennoModPage");
        var ref=(java.lang.ref.WeakReference<?>)field(null,t,"focused");var page=ref.get();if(page==null)return false;
        return (Integer)field(page,t,"slot")==1 && (Long)field(page,t,"pending")==0L && (Long)field(page,t,"token")!=0L;
    }
    private static Set<Item> setOf(Collection<ItemStack> stacks){
        var items=new LinkedHashSet<Item>();for(var stack:stacks)items.add(stack.getItem());return items;
    }
    private static void visitCreative(CreativeModeTab goal)throws Exception{
        if(goal==null)throw new AssertionError("creative tab missing");
        var pages=(List<?>)field(creative,CreativeModeInventoryScreen.class,"pages");
        int dest=-1;
        for(int i=0;i<pages.size();i++){
            var p=(net.neoforged.neoforge.client.gui.CreativeTabsScreenPage)pages.get(i);
            if(p.getVisibleTabs().contains(goal)){dest=i;break;}
        }
        if(dest<0)throw new AssertionError("tab is unreachable: "+goal.getDisplayName().getString());
        for(int n=0;n<pages.size() && !creative.getCurrentPage().getVisibleTabs().contains(goal);n++){
            String dir=pages.indexOf(creative.getCurrentPage())<dest?">":"<";
            var b=creative.children().stream().filter(v->v instanceof Button w&&w.getMessage().getString().equals(dir))
                        .map(v->(Button)v).findFirst().orElseThrow();
            if(!tap(b.getX()+b.getWidth()/2d,b.getY()+b.getHeight()/2d))throw new AssertionError("creative page turn failed");
        }
        var type=CreativeModeInventoryScreen.class;
        int[] point=new int[2];int i=0;
        for(String name:List.of("getTabX","getTabY")){
            Method method=type.getDeclaredMethod(name,CreativeModeTab.class);method.setAccessible(true);point[i++]=(Integer)method.invoke(creative,goal);
        }
        tap(creative.getGuiLeft()+point[0]+13,creative.getGuiTop()+point[1]+16);clearHover();
        check(field(null,type,"selectedTab")==goal,"clicked creative tab "+goal.getDisplayName().getString());
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen a&&m.getOverlay()==null){a.onClose();return;}
        if(!joined&&m.screen instanceof TitleScreen&&m.getOverlay()==null){
            joined=true;String addr="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(addr),new ServerData("Navigation QA",addr,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null||m.level==null)return;
        if(++age>400)throw new AssertionError("timed out phase="+phase+" screen="+m.screen);
        if(!capture.isEmpty())return;
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        switch(phase){
            case 0 -> {m.setScreen(null);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;stageWorld();step(1);}
            case 1 -> {if(age<18||!m.level.hasChunkAt(BENCH))return;prepare();step(2);}
            case 2 -> {
                if(!hot(3).is(item("primary_mod_pack"))||m.player.experienceLevel!=8)return;
                if(CREATIVE_ONLY){step(30);return;}
                if(ModCollection.ranks(m.player).size()<11){
                    if(age%3==0){m.player.getInventory().selected=3;m.gameMode.useItem(m.player,InteractionHand.MAIN_HAND);}return;
                }
                m.player.getInventory().selected=8;open();step(3);
            }
            case 3 -> {
                if(m.screen==null){if(age%30==0)open();return;}
                expectPage(0,0,"Destiny / Crafting");shot("01-Destiny-Crafting");step(4);
            }
            case 4 -> {
                check(click(685,52),"Destiny Perks subtab accepted a left click");
                expectPage(0,1,"Destiny / Perks");oldPerk=perk();
                var gun=(DestinyGunItem)hot(0).getItem();int i=0;
                while(WeaponPerkRegistry.getPoolOption(gun.spec.index,0,i)==oldPerk)i++;
                newPerk=WeaponPerkRegistry.getPoolOption(gun.spec.index,0,i);
                click(590,128+i*21);clearHover();shot("02-Destiny-Perks");step(5);
            }
            case 5 -> {check(click(710,425),"Destiny Apply Perk button accepts chosen change");step(6);}
            case 6 -> {
                if(perk()!=newPerk)return;
                check(newPerk!=oldPerk,"server changed the owned Ace of Spades perk");
                click(680,21);expectPage(1,0,"Warframe / Blueprints");blueprint=TennoBlueprints.entries(m.player).getFirst();
                ingredients=TennoBlueprints.cost(m.player,blueprint);
                for(var st:ingredients)command("give @s "+id(st.getItem())+" "+st.getCount());step(7);
            }
            case 7 -> {if(!TennoBlueprints.canCraft(m.player,blueprint))return;shot("03-Warframe-Blueprints");step(8);}
            case 8 -> {check(click(585,376),"Assemble Weapon button accepts click");step(9);}
            case 9 -> {
                if(count(blueprint)==0)return;check(count(blueprint)==1,"server assembled "+id(blueprint)+" at Warframe Blueprints");
                click(685,52);expectPage(1,1,"Warframe / Weapon Mods");step(10);
            }
            case 10 -> {
                if(!modReady())return;click(247,266);click(635,325);step(11);
            }
            case 11 -> {
                if(!installed().equals("serration")||!modReady())return;
                check(true,"Serration installed on Braton");click(635,368);step(12);
            }
            case 12 -> {
                if(ModCollection.rank(m.player,"serration")!=1||m.player.experienceLevel!=7||!modReady())return;
                check(true,"fusion spent one level and activated rank one on Braton");clearHover();shot("04-Warframe-Mods");step(13);
            }
            case 13 -> {
                click(488,21);expectPage(0,1,"return to Destiny remembers Perks");
                click(500,52);expectPage(0,0,"left subtab returns to Destiny Crafting");
                click(680,21);expectPage(1,1,"return to Warframe remembers Mods");
                click(500,52);expectPage(1,0,"left subtab returns to Warframe Blueprints");
                close();step(30);
            }
            case 30 -> {
                command("gamemode creative");command("clear @s");step(31);
            }
            case 31 -> {
                if(!m.player.isCreative()||!m.player.getInventory().isEmpty())return;
                creative=new CreativeModeInventoryScreen(m.player,m.level.enabledFeatures(),false);m.setScreen(creative);step(32);
            }
            case 32 -> {
                if(age<5)return;
                var tag=TagKey.create(Registries.ITEM,key("warframe/weapons"));
                BuiltInRegistries.ITEM.stream().filter(i->i.builtInRegistryHolder().is(tag)).forEach(warframe::add);
                PACKS.forEach(n->warframe.add(item(n)));
                check(warframe.size()==37,"33 tagged weapons and four mod packs");
                visitCreative(tab("minecraft:combat"));step(33);
            }
            case 33 -> {
                var present=setOf(creative.getMenu().items);
                check(present.contains(Items.DIAMOND_SWORD)&&Collections.disjoint(present,warframe),"vanilla Combat contains its sword and zero Warframe items");
                shot("06-Creative-Combat");step(34);
            }
            case 34 -> {
                var w=tab("warframe");if(CREATIVE_ONLY&&w==null){visitCreative(tab("arsenal"));step(91);return;}
                visitCreative(w);step(35);
            }
            case 35 -> {
                var grid=creative.getMenu().items;
                check(grid.size()==37&&setOf(grid).equals(warframe),"real Warframe item picker contains all 37 unique items");
                check(grid.get(0).is(item("ax_52"))&&grid.get(19).is(item("twin_grakatas"))&&grid.get(27).is(item("amanata")),"primary, secondary and melee groups start with AX-52, Twin Grakatas and Amanata");
                for(int i=0;i<4;i++)check(grid.get(33+i).is(item(PACKS.get(i))),"pack group item "+PACKS.get(i));
                check(tab("warframe").getIconItem().is(item("twin_grakatas")),"Warframe tab shows the Twin Grakatas icon");
                check(Collections.disjoint(setOf(tab("arsenal").getDisplayItems()),warframe)&&Collections.disjoint(setOf(tab("minecraft:tools_and_utilities").getDisplayItems()),warframe),"Paracausal and vanilla Tools omit all 37 Warframe items");
                check(setOf(CreativeModeTabs.searchTab().getDisplayItems()).containsAll(warframe),"global Creative Search includes all 37 Warframe items");
                shot("05-Creative-Warframe");step(91);
            }
            case 91 -> {check(true,"COMPLETE");m.stop();step(92);}
        }
    }
    @SubscribeEvent public static void screenshot(RenderFrameEvent.Post e)throws Exception{
        if(capture.isEmpty()||++frame<3)return;
        try(var image=Screenshot.takeScreenshot(mc().getMainRenderTarget())){image.writeToFile(SHOTS.resolve(capture+".png"));}
        capture="";
    }
}
