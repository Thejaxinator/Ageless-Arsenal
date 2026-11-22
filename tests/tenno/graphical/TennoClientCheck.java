package ageless.tennoqa;

import com.jax.destiny2mc.client.ui.GunsmithScreen;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** Disposable dedicated-server acceptance client. Input, inventory sync, native model baking
 * and the replicated player's first-person equipment all run through the vanilla game loop. */
@EventBusSubscriber(modid="ageless_tenno_qa",value=Dist.CLIENT)
public final class TennoClientCheck {
    private static final BlockPos TABLE=new BlockPos(0,121,0);
    private static final Path DIR=Path.of(System.getProperty("binder.shots"));
    private static final boolean PREFLIGHT=Boolean.getBoolean("binder.preflight");
    private static final boolean QUICK=Boolean.getBoolean("tenno.quick");
    private static final String ISOLATED=System.getProperty("tenno.studio","");
    private static int stage,age,index,shotFrame,modeIndex;
    private static boolean joining;
    private static String pending="",hovered="",frameHover="";
    private static boolean tooltipClaimed,frameClaimed;
    private static ItemStack studioStack=ItemStack.EMPTY;
    private static final String[] CARD_ITEMS={"cedo_prime","sun_and_moon","ace_of_spades","bread"};
    private static final String[] WORLD_ITEMS={"twin_grakatas","kuva_nukor","harmony","sun_and_moon"};
    private static List<Item> weapons;
    private static Item blueprint;
    private static List<ItemStack> blueprintCost;
    private static InventoryScreen inventory;
    private static int rounds;
    private static Minecraft mc(){return Minecraft.getInstance();}
    private static void set(int s){stage=s;age=0;}
    private static void cmd(String s){mc().player.connection.sendCommand(s);}
    private static void require(boolean p,String what){if(!p)throw new AssertionError(what);System.out.println("TENNO QA "+what);}
    private static ResourceLocation key(String s){return s.contains(":")?ResourceLocation.parse(s):ResourceLocation.fromNamespaceAndPath("destiny2mc",s);}
    private static String id(Item i){return BuiltInRegistries.ITEM.getKey(i).getPath();}
    private static Item item(String s){return BuiltInRegistries.ITEM.get(key(s));}
    private static int count(Item i){return mc().player.getInventory().countItem(i);}
    private static void shot(String slug){pending=slug;shotFrame=0;}
    private static void equip(String slug){cmd("item replace entity @s hotbar.0 with destiny2mc:"+slug);}
    private static void close(){var m=mc();if(m.screen!=null)m.screen.onClose();m.setScreen(null);}
    private static List<Item> members(String tag){
        return BuiltInRegistries.ITEM.stream().filter(i->i.builtInRegistryHolder().is(TagKey.create(Registries.ITEM,key("warframe/"+tag)))).toList();
    }
    // Reflective fixture bridge keeps the graphical preflight usable against the previous release.
    @SuppressWarnings("unchecked")
    private static Object foundry(String name, Class<?>[] signature,Object... args)throws Exception{
        return Class.forName("com.jax.destiny2mc.tenno.foundry.TennoBlueprints").getMethod(name,signature).invoke(null,args);
    }
    private static int magazine()throws Exception{
        return (int)Class.forName("com.jax.destiny2mc.tenno.TennoGunItem").getMethod("magazine",ItemStack.class).invoke(null,mc().player.getMainHandItem());
    }
    private static void keyEvent(String key,String direction)throws Exception{
        int r=new ProcessBuilder("python3",System.getProperty("tenno.key.helper"),key,direction).inheritIO().start().waitFor();
        if(r!=0)throw new AssertionError("X input failed: "+key);
    }
    private static void move(double x,double y){
        var w=mc().getWindow();
        double px=x*w.getScreenWidth()/w.getGuiScaledWidth(),py=y*w.getScreenHeight()/w.getGuiScaledHeight();
        GLFW.glfwSetCursorPos(w.getWindow(),px,py);
        var cb=GLFW.glfwSetCursorPosCallback(w.getWindow(),null);GLFW.glfwSetCursorPosCallback(w.getWindow(),cb);cb.invoke(w.getWindow(),px,py);
    }
    private static double[] point(int x,int y){
        var s=mc().screen;float scale=Math.min(s.width/800f,s.height/450f);
        return new double[]{(s.width-800*scale)/2+x*scale,(s.height-450*scale)/2+y*scale};
    }
    private static void hover(int x,int y){var pt=point(x,y);move(pt[0],pt[1]);}
    private static boolean click(int x,int y){var pt=point(x,y);move(pt[0],pt[1]);var s=mc().screen;boolean ok=s.mouseClicked(pt[0],pt[1],0);s.mouseReleased(pt[0],pt[1],0);return ok;}
    private static void openTable(){
        mc().gameMode.useItemOn(mc().player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,121.5,0),Direction.NORTH,TABLE,false));
    }
    private static void hoverSlot(String n){
        Slot slot=inventory.getMenu().slots.stream().filter(s->id(s.getItem().getItem()).equals(n)).findFirst().orElseThrow();
        move(inventory.getGuiLeft()+slot.x+7,inventory.getGuiTop()+slot.y+7);
    }
    private static void spawnWorld(){
        cmd("gamerule sendCommandFeedback false");cmd("gamerule doMobSpawning false");cmd("gamerule fallDamage false");
        cmd("gamerule doDaylightCycle false");cmd("gamerule doWeatherCycle false");cmd("weather clear");cmd("time set 4000");
        cmd("gamemode creative");cmd("forceload add -16 -16 31 31");cmd("tp @s 0.5 128 -2.5 0 0");
    }
    private static void buildRange(){
        cmd("fill -10 120 -9 10 120 18 minecraft:polished_deepslate");
        cmd("fill -10 121 14 10 127 14 minecraft:tuff_bricks");
        cmd("fill -10 128 14 10 128 14 minecraft:oxidized_copper");
        for(int x:new int[]{-7,0,7}){
            cmd("fill "+(x-2)+" 121 13 "+(x+2)+" 126 13 minecraft:waxed_cut_copper");
            cmd("fill "+(x-1)+" 122 12 "+(x+1)+" 125 12 minecraft:deepslate_tiles");
            cmd("setblock "+x+" 124 11 minecraft:target");cmd("setblock "+x+" 126 12 minecraft:sea_lantern");
        }
        cmd("fill -4 120 -8 -4 120 12 minecraft:oxidized_cut_copper");cmd("fill 4 120 -8 4 120 12 minecraft:oxidized_cut_copper");
        cmd("setblock 0 121 0 destiny2mc:gunsmith_fui_workbench");cmd("tp @s 0.5 121 -2.5 0 10");
        cmd("gamemode survival");cmd("clear @s");
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void tooltips(RenderTooltipEvent.Pre e){
        if(!e.getItemStack().isEmpty()){
            hovered=id(e.getItemStack().getItem());tooltipClaimed=e.isCanceled();frameHover=hovered;frameClaimed=tooltipClaimed;
        }
    }
    @SuppressWarnings("unchecked")
    @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen a && m.getOverlay()==null){a.onClose();return;}
        if(!ISOLATED.isEmpty() && m.getOverlay()==null){
            if(!joining && m.screen instanceof TitleScreen){
                joining=true;studioStack=item(ISOLATED).getDefaultInstance();m.setScreen(new Studio());
                shot("Mesh-"+ISOLATED);System.out.println("TENNO QA catalog recapture "+ISOLATED);
            }else if(joining && pending.isEmpty()){System.out.println("TENNO QA COMPLETE");m.stop();}
            return;
        }
        if(!joining && m.screen instanceof TitleScreen && m.getOverlay()==null){
            joining=true;String a="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(a),new ServerData("Foundry QA",a,ServerData.Type.OTHER),false,null);
        }
        if(m.player==null||m.level==null)return;
        if(++age>400)throw new AssertionError("timeout stage="+stage+" index="+index+" held="+id(m.player.getMainHandItem().getItem())+" active="+m.screen+" hover="+hovered);
        if(!pending.isEmpty())return;
        m.gui.getChat().clearMessages(false);m.getToasts().clear();
        switch(stage){
            case 0->{m.setScreen(null);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;spawnWorld();set(1);}
            case 1->{if(age<20||!m.level.hasChunkAt(TABLE))return;buildRange();set(2);}
            case 2->{
                if(age<15||m.player.distanceToSqr(.5,121,-2.5)>4)return;
                if(PREFLIGHT){weapons=List.of(item("ace_of_spades"),item("minecraft:iron_sword"));index=0;set(12);return;}
                weapons=members("weapons");List<Item> ranged=members("ranged"),melee=members("melee");
                require(weapons.size()==33 && ranged.size()==27 && melee.size()==6,"synced tags contain 33 weapons, 27 ranged and six melee");
                require(Collections.disjoint(ranged,melee) && weapons.containsAll(ranged)&&weapons.containsAll(melee),"ranged and melee tags partition the arsenal");
                var available=(List<Item>)foundry("entries",new Class<?>[]{Player.class},m.player);
                require(new HashSet<>(available).equals(new HashSet<>(weapons)),"all 33 synced foundry blueprints match registry tags");
                blueprint=available.getFirst();
                blueprintCost=(List<ItemStack>)foundry("cost",new Class<?>[]{Player.class,Item.class},m.player,blueprint);
                require(!blueprintCost.isEmpty(),"selected survival blueprint has a cost: "+id(blueprint));
                openTable();set(3);
            }
            case 3->{
                if(!(m.screen instanceof GunsmithScreen)){if(age%20==0&&m.screen==null)openTable();return;}keyEvent("shift","up");
                require(click(430,40),"existing Gunsmith accepts Warframe tab click");hover(780,411);set(4);
            }
            case 4->{if(age<9)return;shot("Foundry");set(5);}
            case 5->{click(600,376);hover(100,126);set(6);}
            case 6->{
                hover(100,126);if(age<12)return;require(count(blueprint)==0,"empty survival inventory cannot assemble a weapon");
                shot("Blueprint-Card");for(var cost:blueprintCost)cmd("give @s "+BuiltInRegistries.ITEM.getKey(cost.getItem())+" "+cost.getCount());set(7);
            }
            case 7->{
                if(age<5||!blueprintCost.stream().allMatch(st->count(st.getItem())>=st.getCount()))return;
                require(click(600,376),"exact ingredients enable assembly");set(8);
            }
            case 8->{
                if(count(blueprint)!=1)return;
                require(blueprintCost.stream().allMatch(st->count(st.getItem())==0),"server consumed exactly the ingredients and granted one "+id(blueprint));
                click(600,376);set(9);
            }
            case 9->{
                if(age<15)return;require(count(blueprint)==1,"duplicate assembly cannot spend the same ingredients twice");
                close();for(int i=0;i<CARD_ITEMS.length;i++){
                    String n=CARD_ITEMS[i];cmd("item replace entity @s hotbar."+i+" with "+(n.equals("bread")?"minecraft:":"destiny2mc:")+n);
                }set(10);
            }
            case 10->{
                if(age<10||!id(m.player.getInventory().getItem(1).getItem()).equals("sun_and_moon"))return;
                inventory=new InventoryScreen(m.player);m.setScreen(inventory);index=0;hoverSlot(CARD_ITEMS[index]);set(11);
            }
            case 11->{
                String n=CARD_ITEMS[index];if(age%25==0)hoverSlot(n);
                if(age<8||!hovered.equals(n)||inventory.getSlotUnderMouse()==null||!id(inventory.getSlotUnderMouse().getItem().getItem()).equals(n))return;
                require(tooltipClaimed==(!n.equals("bread")),"ordinary inventory dispatches proper tooltip for "+n);shot("Card-"+n);
                if(++index<CARD_ITEMS.length){set(23);}else{set(24);}
            }
            case 23->{hoverSlot(CARD_ITEMS[index]);set(11);}
            case 24->{close();index=0;set(12);}
            case 12->{
                if(index>=weapons.size()){
                    close();if(PREFLIGHT){set(30);return;}index=0;cmd("tp @s 0.5 121 1.5 0 -5");equip(WORLD_ITEMS[0]);set(14);return;
                }
                Item i=weapons.get(index);
                studioStack=new ItemStack(i);
                var model=m.getItemRenderer().getModel(studioStack,m.level,m.player,0).applyTransform(ItemDisplayContext.GUI,new com.mojang.blaze3d.vertex.PoseStack(),false);
                require(model!=m.getModelManager().getMissingModel() && !model.getQuads(null,null,net.minecraft.util.RandomSource.create(7)).isEmpty(),"baked mesh "+id(i));
                if(!QUICK){m.setScreen(new Studio());shot("Mesh-"+id(i));}
                index++;set(12);
            }
            case 14->{
                String n=WORLD_ITEMS[index];if(age<9||!id(m.player.getMainHandItem().getItem()).equals(n))return;
                if(index>=2){m.options.keyUse.setDown(true);m.gameMode.useItem(m.player,InteractionHand.MAIN_HAND);set(15);}
                else{shot("Equipped-"+n);set(16);}
            }
            case 15->{
                var held=m.player.getMainHandItem();
                if(!m.player.isUsingItem()&&age%3==0){m.options.keyUse.setDown(true);m.gameMode.useItem(m.player,InteractionHand.MAIN_HAND);}
                if(age<9||!m.player.isUsingItem()||m.player.getTicksUsingItem()<5)return;
                require(held.getUseAnimation()==UseAnim.BLOCK && m.player.getTicksUsingItem()>=5,"melee weapon enters sustained BLOCK guard: "+WORLD_ITEMS[index]);
                shot("Guard-"+WORLD_ITEMS[index]);set(16);
            }
            case 16->{
                m.options.keyUse.setDown(false);if(m.player.isUsingItem())m.gameMode.releaseUsingItem(m.player);
                if(++index<WORLD_ITEMS.length){equip(WORLD_ITEMS[index]);set(14);return;}
                equip("burston");set(17);
            }
            case 17->{
                if(age<8||!id(m.player.getMainHandItem().getItem()).equals("burston"))return;
                rounds=magazine();keyEvent("right","down");set(31);
            }
            case 31->{if(age<9)return;shot("ADS-Burston");set(32);}
            case 32->{keyEvent("left","down");set(18);}
            case 18->{keyEvent("left","up");keyEvent("right","up");set(19);}
            case 19->{
                if(age<10||magazine()>rounds-3)return;require(magazine()==rounds-3,"held attack input fired a scheduled Burston burst; magazine "+rounds+" to "+magazine());
                keyEvent("r","down");set(20);
            }
            case 20->{if(age<6)return;keyEvent("r","up");set(21);}
            case 21->{
                if(magazine()<rounds){if(age==85){keyEvent("r","down");set(20);}return;}require(magazine()==rounds,"shared reload binding refilled the Warframe magazine");
                equip("coda_bubonico");set(25);
            }
            case 25->{if(age<10||!id(m.player.getMainHandItem().getItem()).equals("coda_bubonico"))return;rounds=magazine();keyEvent("g","down");set(26);}
            case 26->{if(age<8)return;keyEvent("g","up");set(27);}
            case 27->{if(magazine()>=rounds){if(age==24){keyEvent("g","down");set(26);}return;}require(magazine()<rounds,"alternate binding consumed Coda Bubonico battery");shot("Alternate-Coda");cmd("tennoqaprove");set(29);}
            case 29->{if(!m.player.getInventory().getItem(8).is(Items.EMERALD))return;require(true,"dedicated server completed live target and projectile checks");set(30);}
            case 30->{System.out.println("TENNO QA COMPLETE");m.stop();set(99);}
            default->{}
        }
    }
    @SubscribeEvent public static void frameStart(RenderFrameEvent.Pre e){frameHover="";}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(pending.isEmpty()||++shotFrame<3)return;
        if(pending.equals("Blueprint-Card")){var pt=point(100,126);var ww=mc().getWindow();double posX=mc().mouseHandler.xpos()*ww.getGuiScaledWidth()/ww.getScreenWidth(),posY=mc().mouseHandler.ypos()*ww.getGuiScaledHeight()/ww.getScreenHeight();if(Math.abs(pt[0]-posX)>1||Math.abs(pt[1]-posY)>1){hover(100,126);return;}}
        if(pending.startsWith("Card-") && (!pending.substring(5).equals(frameHover)||frameClaimed==pending.endsWith("-bread")))return;
        try(var raw=Screenshot.takeScreenshot(mc().getMainRenderTarget())){
            if(pending.equals("Blueprint-Card")){
                int gold=0xff95c6db; // NativeImage.getPixelRGBA uses ABGR.
                int hits=0;
                for(int y=60;y<raw.getHeight()-30;y+=1)for(int x=100;x<raw.getWidth()-30;x+=8)
                    if(raw.getPixelRGBA(x,y)==gold)hits++;
                if(hits<80 && shotFrame<75){hover(100,126);return;}
                if(hits<80)throw new AssertionError("Foundry blueprint hover did not paint the Arsenal card ("+hits+")");
                System.out.println("TENNO QA verified Foundry card painted its Arsenal border from the real cursor");
            }
            raw.writeToFile(DIR.resolve(pending+".png"));
        }
        pending="";
    }
    /** Catalog photography only. The in-game baked stack uses the same item renderer and model transforms as the inventory. */
    static final class Studio extends Screen{
        Studio(){super(Component.literal("Arsenal equipment"));}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void renderBackground(GuiGraphics g,int x,int y,float t){}
        @Override public void render(GuiGraphics g,int x,int y,float t){
            g.fill(0,0,width,height,0xff192932);
            int size=Math.min(width/2,id(studioStack.getItem()).equals("harmony")?height/2:height*2/3);
            var p=g.pose();p.pushPose();p.translate((width-size)/2f,(height-size)/2f,0);
            p.scale(size/16f,size/16f,1);g.renderItem(studioStack,0,0);p.popPose();
        }
    }
}
