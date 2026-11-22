package ageless.armorqa;

import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Native graphical regression fixture. Each change goes through the dedicated server's
 * equipment command. Wait for the real slot packet before dispatching the equipped player
 * through Minecraft's own entity/armor renderers. The studio screen is exclusive to QA.
 */
@EventBusSubscriber(modid="ageless_armor_showcase",value=Dist.CLIENT)
public final class ArmorShowcaseCheck {
    record Outfit(String slug,String title,String source,String[] items,int tint,boolean chest) {}
    static String[] ids(String... names) { return names; }
    static final String[] KABR=ids("battlecage_of_kabr","kabrs_wrath","light_of_the_great_prism","kabrs_forceful_greaves");
    static final String[] HIVE=ids("willbreakers_watch","willbreakers_resolve","mark_of_the_pit","willbreakers_greaves");
    static final String[] RULL=ids("mask_of_rull","chassis_of_rull","shadows_mark","greaves_of_rull");
    static final String[] IRON=ids("minecraft:iron_helmet","minecraft:iron_chestplate","minecraft:iron_leggings","minecraft:iron_boots");
    static String[] with(String[] outfit,int ix,String replacement) { var copy=outfit.clone();copy[ix]=replacement;return copy;}
    static final List<Outfit> SETS=List.of(
        new Outfit("Kabr","Kabr's armor","Vault of Glass",KABR,0xff9ad8c4,false),
        new Outfit("Willbreaker","Willbreaker's armor","Crota's End",HIVE,0xffa4ce89,false),
        new Outfit("Rull","Armor of Rull","Leviathan",RULL,0xffcdac75,true),
        new Outfit("Saint-14","Helm of Saint-14","Exotic helmet",with(KABR,0,"helm_of_saint_14"),0xffe5be68,false),
        new Outfit("Armamentarium","The Armamentarium","Exotic chest armor",with(KABR,1,"the_armamentarium"),0xffe5be68,true),
        new Outfit("Bakris","Mask of Bakris","Exotic helmet",with(HIVE,0,"mask_of_bakris"),0xffe5be68,false));
    static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    static final String[] SLOT_CMD={"armor.head","armor.chest","armor.legs","armor.feet"};
    static final boolean PREFLIGHT=Boolean.getBoolean("binder.preflight");
    static final Path SHOTS=Path.of(System.getProperty("binder.shots"));
    static boolean connecting;
    static int stage,age,index,frames;
    static String pending="";
    static Minecraft mc(){return Minecraft.getInstance();}
    static ResourceLocation id(String item){return ResourceLocation.parse(item.contains(":")?item:"destiny2mc:"+item);}
    static void cmd(String s){mc().player.connection.sendCommand(s);}
    static void next(int s){stage=s;age=0;}
    static void shot(String slug){pending=slug;frames=0;}
    static String[] outfit(){return PREFLIGHT?IRON:SETS.get(index).items;}
    static void equip(String selector,String[] armor){
        for(int i=0;i<4;i++)cmd("item replace entity "+selector+" "+SLOT_CMD[i]+" with "+id(armor[i]));
    }
    static boolean matches(LivingEntity wearer,String[] chosen){
        for(int i=0;i<4;i++){
            ItemStack item=wearer.getItemBySlot(SLOTS[i]);
            if(!id(chosen[i]).equals(BuiltInRegistries.ITEM.getKey(item.getItem())))return false;
            if(!(item.getItem() instanceof ArmorItem armor)||armor.getEquipmentSlot()!=SLOTS[i])throw new IllegalStateException("Wrong armor slot for "+chosen[i]);
        }return true;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        var m=mc();
        if(m.screen instanceof AccessibilityOnboardingScreen s&&m.getOverlay()==null){s.onClose();return;}
        if(!connecting&&m.screen instanceof TitleScreen&&m.getOverlay()==null){
            connecting=true;String addr="127.0.0.1:"+Integer.getInteger("binder.port");
            ConnectScreen.startConnecting(m.screen,m,ServerAddress.parseString(addr),new ServerData("Armor preview",addr,ServerData.Type.OTHER),false,null);
        }
        if(m.level==null||m.player==null)return;
        if(++age>550)throw new IllegalStateException("Timeout stage="+stage+" outfit="+index);
        if(!pending.isEmpty())return;
        m.getToasts().clear();m.gui.getChat().clearMessages(false);
        switch(stage){
            case 0->{
                m.screen=null; m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
                cmd("gamerule sendCommandFeedback false");cmd("gamerule doDaylightCycle false");
                cmd("gamerule doMobSpawning false");cmd("gamemode creative");cmd("time set noon");
                cmd("forceload add 0 0");cmd("tp @s 0.5 90 0.5 180 0");next(1);
            }
            case 1->{
                if(age<10)return;
                // A single studio world can be replayed without keeping any previous display stands.
                cmd("kill @e[type=minecraft:armor_stand]");cmd("weather clear");
                cmd("fill -10 86 -2 10 86 12 minecraft:light_gray_concrete");
                cmd("fill -10 87 8 10 93 8 minecraft:gray_concrete");
                cmd("fill -10 94 8 10 94 8 minecraft:sea_lantern");
                cmd("fill -4 87 3 4 87 4 minecraft:polished_andesite");
                cmd("tp @s 0.5 87 -1.5 0 -2");next(2);
            }
            case 2->{
                var armor=outfit();
                for(String item:armor)if(!BuiltInRegistries.ITEM.containsKey(id(item)))throw new IllegalStateException("Missing "+item);
                equip("@s",armor);next(3);
            }
            case 3->{
                if(age<5||!matches(m.player,outfit()))return;
                m.setScreen(new Studio());shot(SETS.get(index).slug);
                System.out.println("ARMOR VIEW four slots replicated for "+SETS.get(index).title+": "+String.join(", ",outfit()));next(4);
            }
            case 4->{
                if(++index<SETS.size()&&!PREFLIGHT){next(2);return;}
                m.setScreen(null);m.options.hideGui=true;
                int[] x={-2,0,2};
                for(int i=0;i<3;i++){
                    String tag="gear_pose_"+i;
                    cmd("summon minecraft:armor_stand "+(x[i]+.5)+" 88 3.5 {Tags:[\""+tag+"\"],ShowArms:1b,NoBasePlate:1b,Invulnerable:1b,Rotation:[180f,0f],Pose:{LeftArm:[0f,0f,-5f],RightArm:[0f,0f,5f]}}");
                    equip("@e[type=minecraft:armor_stand,tag="+tag+",limit=1]",PREFLIGHT?IRON:SETS.get(i).items);
                }
                cmd("tp @s 0.5 87 -0.15 0 -5");next(5);
            }
            case 5->{
                if(age<18)return;
                var stands=m.level.getEntitiesOfClass(ArmorStand.class,new AABB(-5,85,-2,5,94,7));
                if(stands.size()!=3)return;
                stands.sort(java.util.Comparator.comparingDouble(ArmorStand::getX));
                for(int i=0;i<3;i++)if(!matches(stands.get(i),PREFLIGHT?IRON:SETS.get(i).items))return;
                shot("World-Display");System.out.println("ARMOR VIEW three equipped armor stands replicated through the dedicated server");next(6);
            }
            case 6->{m.stop();System.out.println("ARMOR VIEW COMPLETE");next(7);}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        if(pending.isEmpty()||++frames<5)return;
        try(var raw=Screenshot.takeScreenshot(mc().getMainRenderTarget())){raw.writeToFile(SHOTS.resolve(pending+".png"));}
        pending="";
    }

    static final class Studio extends Screen {
        Studio(){super(Component.literal("Armor preview"));}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void renderBackground(GuiGraphics g,int x,int y,float t){}
        private void name(GuiGraphics g,String text,int x,int y,float size,int color){
            var pose=g.pose();pose.pushPose();pose.translate(x,y,0);pose.scale(size,size,1);g.drawString(font,text,0,0,color,false);pose.popPose();
        }
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            var set=SETS.get(index);
            var p=minecraft.player;
            // Current wearer data comes from the server. The test changes only view orientation.
            float y=p.getYRot(),yo=p.yRotO,x=p.getXRot(),xo=p.xRotO,body=p.yBodyRot,bo=p.yBodyRotO,head=p.yHeadRot,ho=p.yHeadRotO;
            float sx=width/480f,sy=height/270f,scale=Math.min(sx,sy);
            var pose=g.pose();pose.pushPose();pose.translate((width-480*scale)/2,(height-270*scale)/2,0);pose.scale(scale,scale,1);
            g.fillGradient(0,0,480,270,0xff20282e,0xff131a1f);
            g.fill(14,12,16,34,set.tint);
            name(g,set.title,21,10,1.55f,0xfff1f1e9); name(g,set.source,21,27,.9f,set.tint);
            g.fill(14,40,466,41,0xff526066);
            try{
                p.setYRot(180);p.yRotO=180;p.setXRot(0);p.xRotO=0;p.yBodyRot=180;p.yBodyRotO=180;p.yHeadRot=180;p.yHeadRotO=180;
                drawModel(g,p,81,145,86,-26,0.95f);
                drawModel(g,p,223,145,86,154,0.95f);
                // Isolate the signature piece while keeping the actual same armor stack equipped.
                g.enableScissor(298,46,466,235);
                drawModel(g,p,382,set.slug.equals("Saint-14")?157:143,set.chest?177:216,-12,set.chest?1.24f:1.64f);
                g.disableScissor();
            }finally{
                p.setYRot(y);p.yRotO=yo;p.setXRot(x);p.xRotO=xo;p.yBodyRot=body;p.yBodyRotO=bo;p.yHeadRot=head;p.yHeadRotO=ho;
            }
            g.fill(14,240,466,241,0xff526066);
            name(g,"Front",16,249,.9f,0xffb6c1c5);name(g,"Reverse",160,249,.9f,0xffb6c1c5);
            name(g,set.chest?"Chest detail":"Helmet detail",300,249,.9f,0xffb6c1c5);
            g.pose().popPose();
        }
        private void drawModel(GuiGraphics g,LocalPlayer p,int x,int y,float size,int yaw,float pivot){
            var rot=new Quaternionf().rotateZ((float)Math.PI).rotateX((float)Math.toRadians(4)).rotateY((float)Math.toRadians(yaw));
            InventoryScreen.renderEntityInInventory(g,x,y,size,new Vector3f(0,pivot,0),rot,new Quaternionf().rotateX((float)Math.toRadians(4)),p);
        }
    }
}
