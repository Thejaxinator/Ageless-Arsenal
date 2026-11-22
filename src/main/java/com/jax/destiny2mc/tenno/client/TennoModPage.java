package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.GameCanvas;
import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.ModBenchNetwork;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import static com.jax.destiny2mc.tenno.client.ModCardArt.*;

/** A character's reusable library and one physical weapon's installed cards are separate.
 * Snapshots are rebuilt from authoritative player/stack sync, including when the bench stays open. */
public final class TennoModPage {
    private static final String[] FILTERS={"Usable","Owned","All"};
    private static final int LIST_Y=103, CARD_X=196, CARD_Y=244, COLUMNS=3, CARD_STEP=114, ROW_STEP=65;
    private static final int LIBRARY_PAGE=6, SLOTS_PAGE=6;
    private static long serial;
    private static WeakReference<TennoModPage> focused=new WeakReference<>(null);
    private List<Integer> weapons=List.of();
    private List<WeaponMod> visible=List.of(),installed=List.of();
    private int slot=-1,inventoryOffset=0,libraryOffset=0,socketOffset=0,filter=0;
    private int boundSlot=Integer.MIN_VALUE;
    private ResourceLocation boundItem=ModBenchNetwork.AIR;
    private long pending,token;
    private byte requested;
    private WeaponMod selected;
    private String feedback="";
    private boolean inspectInstalled=false;

    public static void receive(ModBenchNetwork.Reply reply){
        var ui=focused.get();if(ui==null||ui.pending!=reply.serial())return;
        ui.pending=0;
        if(reply.status()==ModBenchNetwork.OK){
            ui.token=reply.token();
            ui.feedback=switch(ui.requested){
                case ModBenchNetwork.INSTALL -> "Mod installed.";
                case ModBenchNetwork.REMOVE -> "Mod removed.";
                case ModBenchNetwork.FUSE -> "Fusion complete.";
                default -> "";
            };
        }else{
            ui.feedback=switch(reply.status()){
                case ModBenchNetwork.NO_STATION -> "Open a nearby Gunsmith to make changes.";
                case ModBenchNetwork.STALE -> "Weapon changed. Select it again.";
                default -> "Change declined. Check the selected weapon and rank.";
            };
            if(reply.status()==ModBenchNetwork.STALE||reply.status()==ModBenchNetwork.INVALID){ui.token=0;ui.boundSlot=Integer.MIN_VALUE;}
        }
    }

    private static ItemStack weapon(Player p,int slot){return slot<0?ItemStack.EMPTY:p.getInventory().getItem(slot);}
    private static ResourceLocation id(ItemStack s){return s.isEmpty()?ModBenchNetwork.AIR:BuiltInRegistries.ITEM.getKey(s.getItem());}
    private static String slotLabel(int slot){return slot==40?"Offhand":slot<9?"Hotbar "+(slot+1):"Inventory "+(slot+1);}
    private static int lastPage(int count,int page){return Math.max(0,(count-1)/page)*page;}

    private void bind(Player p){
        focused=new WeakReference<>(this);
        token=0;
        boundSlot=slot;boundItem=id(weapon(p,slot));
        dispatch(ModBenchNetwork.BIND,null,p);
    }

    private void refresh(Player p){
        if(p==null)return;
        var inventory=p.getInventory();
        List<Integer> carried=new ArrayList<>();
        for(int i=0;i<inventory.getContainerSize();i++)
            if((i<36||i==40)&&TennoBlueprints.isTenno(inventory.getItem(i).getItem()))carried.add(i);
        weapons=carried;
        if(!weapons.contains(slot)){
            slot=weapons.contains(inventory.selected)?inventory.selected:weapons.isEmpty()?-1:weapons.get(0);
            socketOffset=0;inspectInstalled=false;
        }
        inventoryOffset=Mth.clamp(inventoryOffset,0,Math.max(0,weapons.size()-9));
        ItemStack item=weapon(p,slot);
        if(slot!=boundSlot||!id(item).equals(boundItem))bind(p);
        installed=WeaponMods.installed(item);
        visible=Arrays.stream(WeaponMod.values()).filter(m->switch(filter){
            case 0 -> ModCollection.rank(p,m.id())>=0&&(item.isEmpty()||m.fits(item));
            case 1 -> ModCollection.rank(p,m.id())>=0;
            default -> true;
        }).toList();
        socketOffset=Math.min(socketOffset,lastPage(installed.size(),SLOTS_PAGE));
        libraryOffset=Math.min(libraryOffset,lastPage(visible.size(),LIBRARY_PAGE));
        if(!visible.contains(selected)&&!(inspectInstalled&&installed.contains(selected)))
            selected=visible.isEmpty()?null:visible.get(libraryOffset);
    }

    public void enter(){boundSlot=Integer.MIN_VALUE;token=0;pending=0;feedback="";focused=new WeakReference<>(this);}

    public void close(){
        if(focused.get()!=this)return;
        PacketDistributor.sendToServer(new ModBenchNetwork.Command(++serial,token,ModBenchNetwork.CLOSE,
            slot,boundItem,"",-1));focused.clear();token=0;pending=0;
    }

    private void dispatch(byte op,WeaponMod card,Player p){
        requested=op;pending=++serial;
        var stack=weapon(p,slot);
        PacketDistributor.sendToServer(new ModBenchNetwork.Command(pending,token,op,slot,id(stack),
            card==null?"":card.id(),card==null?-1:ModCollection.rank(p,card.id())));
    }

    private boolean canToggle(Player p){
        if(selected==null||slot<0||pending!=0||token==0)return false;
        return installed.contains(selected)||(ModCollection.rank(p,selected.id())>=0&&selected.fits(weapon(p,slot)));
    }

    private boolean canRank(Player p){
        if(selected==null||pending!=0||token==0)return false;
        int rank=ModCollection.rank(p,selected.id());
        return rank>=0&&rank<selected.maxRank()&&p.experienceLevel>=ModCollection.rankCost(rank);
    }

    private void toggle(Player p){
        if(canToggle(p))dispatch(installed.contains(selected)?ModBenchNetwork.REMOVE:ModBenchNetwork.INSTALL,selected,p);
    }

    private void fuse(Player p){if(canRank(p))dispatch(ModBenchNetwork.FUSE,selected,p);}

    public void draw(GuiGraphics g,Font font,Player p){
        if(p==null)return;
        refresh(p);
        UiCanvas c=new GameCanvas(g,font,i->weapon(p,i));
        c.fill(0,0,800,450,0xff161f20);c.fill(21,76,157,329,0xff202c2a);
        c.fill(185,76,359,329,0xff1d2927);c.fill(550,76,226,329,0xff202c2a);
        c.text(30,85,"Carried weapons",IVORY,1);
        int end=Math.min(weapons.size(),inventoryOffset+9);
        for(int i=inventoryOffset;i<end;i++){
            int n=weapons.get(i),y=LIST_Y+(i-inventoryOffset)*30;
            boolean active=n==slot;
            if(active){c.fill(26,y,147,28,0xff425044);c.fill(26,y,2,28,JADE);}
            c.item(n,30,y+3,23);
            c.text(57,y+4,font.plainSubstrByWidth(weapon(p,n).getHoverName().getString(),134),active?IVORY:MUTED,.85f);
            c.text(57,y+17,slotLabel(n),active?JADE:MUTED,.72f);
        }
        if(weapons.isEmpty())ModCardArt.wrapped(c,font,31,114,134,"Carry a Warframe weapon to install mods.",.92f,5,MUTED);
        if(weapons.size()>9)c.text(30,386,"Scroll weapons",MUTED,.75f);

        c.text(196,86,"Installed",IVORY,1.03f);
        String count=installed.size()+" installed";
        c.text(524-Math.round(font.width(count)*.77f),86,count,MUTED,.77f);
        itemSlots(c,font,p);
        c.fill(196,199,337,1,DULL);
        c.text(196,208,"Collection",IVORY,1);
        c.text(462,208,ModCollection.ranks(p).size()+" / "+WeaponMod.values().length+" owned",MUTED,.75f);
        for(int i=0;i<3;i++){
            int x=196+i*75;
            c.fill(x,222,71,17,i==filter?0xff46594f:0xff2c3833);
            c.text(x+7,227,FILTERS[i],i==filter?IVORY:MUTED,.82f);
        }
        for(int i=0;i<LIBRARY_PAGE&&libraryOffset+i<visible.size();i++){
            var m=visible.get(libraryOffset+i);int x=CARD_X+(i%COLUMNS)*CARD_STEP,y=CARD_Y+i/COLUMNS*ROW_STEP;
            ModCardArt.paint(g,c,font,m,ModCollection.rank(p,m.id()),x,y,108,61,m==selected,false,installed.contains(m));
        }
        if(visible.isEmpty()){
            String empty=filter==0&&ModCollection.ranks(p).size()>0?"No owned cards fit this weapon. Choose Owned or All.":
                "Craft a mod pack and use it to add a card. Choose All to preview the collection.";
            ModCardArt.wrapped(c,font,203,264,294,empty,1f,5,MUTED);
        }
        if(visible.size()>LIBRARY_PAGE)pageControls(c,libraryOffset,visible.size(),LIBRARY_PAGE,196,380);
        drawDetail(g,c,font,p);
        c.fill(27,414,746,1,DULL);c.text(27,429,"Esc   Close",MUTED,.82f);
        c.text(196,429,feedback.isEmpty()?"Enter   Install / remove     F   Rank up":feedback,feedback.isEmpty()?MUTED:JADE,.82f);
    }

    private void itemSlots(UiCanvas c,Font font,Player p){
        for(int i=0;i<SLOTS_PAGE;i++){
            int x=196+i%3*114,y=103+i/3*34;
            c.fill(x,y,108,30,0xff18221f);
            if(socketOffset+i<installed.size()){
                WeaponMod m=installed.get(socketOffset+i);
                boolean active=m==selected;
                c.outline(x,y,108,30,active?JADE:BRASS);
                c.text(x+5,y+5,font.plainSubstrByWidth(m.title(),113),IVORY,.86f);
                int rank=ModCollection.rank(p,m.id());
                c.text(x+5,y+17,rank<0?"Dormant":"Rank "+rank,rank<0?MUTED:JADE,.72f);
            }else {c.outline(x,y,108,30,DULL);c.line(x+49,y+15,x+58,y+15,DULL);}
        }
        if(installed.size()>SLOTS_PAGE)pageControls(c,socketOffset,installed.size(),SLOTS_PAGE,196,177);
        else if(slot>=0)c.text(196,178,font.plainSubstrByWidth(weapon(p,slot).getHoverName().getString(),265),MUTED,.8f);
    }

    private static void pageControls(UiCanvas c,int off,int count,int size,int x,int y){
        c.fill(x,y,24,17,0xff32423b);c.text(x+8,y+4,"<",IVORY,.9f);
        c.text(x+33,y+4,(off/size+1)+" / "+((count+size-1)/size),MUTED,.84f);
        c.fill(x+75,y,24,17,0xff32423b);c.text(x+83,y+4,">",IVORY,.9f);
    }
    private void drawDetail(GuiGraphics g,UiCanvas c,Font f,Player p) {
        if(selected==null){
            c.text(565,94,"Mod inspection",IVORY,1.05f);
            ModCardArt.wrapped(c,f,565,119,190,"Select a card to inspect its effect and fusion rank.",1,4,MUTED);
            return;
        }
        var m=selected;int rank=ModCollection.rank(p,m.id());
        ModCardArt.paint(g,c,f,m,rank,565,83,197,172,false,true,installed.contains(m));
        if(rank>=0&&rank<m.maxRank()){
            c.text(565,265,"Next rank",BRASS,.82f);
            ModCardArt.wrapped(c,f,565,278,198,m.effect(rank+1),.9f,2,IVORY);
        }else ModCardArt.wrapped(c,f,565,265,198,rank<0?"Unlock this card by opening a compatible mod pack.":m.description(),.9f,3,MUTED);
        String place=installed.contains(m)?"Remove mod":rank<0?"Card locked":slot<0?"Select a weapon":!m.fits(weapon(p,slot))?"Incompatible weapon":"Install mod";
        button(c,565,312,198,place,canToggle(p));
        c.text(565,343,"Available: "+p.experienceLevel+" XP levels",MUTED,.8f);
        String fusion=rank<0?"Rank up":rank>=m.maxRank()?"Maximum rank":"Rank up   ·   "+ModCollection.rankCost(rank)+" XP level"+(ModCollection.rankCost(rank)==1?"":"s");
        button(c,565,354,198,fusion,canRank(p));
    }

    private static void button(UiCanvas c,int x,int y,int w,String s,boolean enabled){
        c.fill(x,y,w,27,enabled?0xffd8c59f:0xff33423c);c.outline(x,y,w,27,enabled?BRASS:DULL);
        c.text(x+12,y+9,s,enabled?INK:MUTED,.94f);
    }

    public boolean click(double x,double y,Player p){
        refresh(p);
        if(x>=26&&x<173&&y>=LIST_Y&&y<LIST_Y+9*30){
            int i=inventoryOffset+(int)(y-LIST_Y)/30;
            if(i<weapons.size()){slot=weapons.get(i);socketOffset=libraryOffset=0;selected=null;feedback="";inspectInstalled=false;refresh(p);}return true;
        }
        if(x>=196&&x<533&&y>=103&&y<167){
            int col=(int)(x-196)/114,row=(int)(y-103)/34,i=socketOffset+row*3+col;
            if(col<3&&(x-196)%114<108&&(y-103)%34<30&&i<installed.size()){selected=installed.get(i);inspectInstalled=true;feedback="";}
            return true;
        }
        if(y>=177&&y<194&&installed.size()>SLOTS_PAGE){int step=pageDirection(x);if(step!=0){socketOffset=clampPage(socketOffset,step,SLOTS_PAGE,installed.size());return true;}}
        if(x>=196&&x<417&&y>=222&&y<239){
            int i=(int)(x-196)/75;if(i<3&&(x-196)%75<71){filter=i;libraryOffset=0;selected=null;inspectInstalled=false;feedback="";refresh(p);}return true;
        }
        if(x>=CARD_X&&x<CARD_X+3*CARD_STEP&&y>=CARD_Y&&y<CARD_Y+2*ROW_STEP){
            int col=(int)(x-CARD_X)/CARD_STEP,row=(int)(y-CARD_Y)/ROW_STEP;
            int i=libraryOffset+row*3+col;
            if((x-CARD_X)%CARD_STEP<108&&(y-CARD_Y)%ROW_STEP<61&&i<visible.size()){selected=visible.get(i);inspectInstalled=false;feedback="";}
            return true;
        }
        if(y>=380&&y<397&&visible.size()>LIBRARY_PAGE){int step=pageDirection(x);if(step!=0){libraryOffset=clampPage(libraryOffset,step,LIBRARY_PAGE,visible.size());return true;}}
        if(x>=565&&x<763&&y>=312&&y<339){toggle(p);return true;}
        if(x>=565&&x<763&&y>=354&&y<381){fuse(p);return true;}
        return false;
    }

    private static int pageDirection(double x){return x>=196&&x<220?-1:x>=271&&x<295?1:0;}
    private static int clampPage(int offset,int step,int size,int total){return Mth.clamp(offset+step*size,0,lastPage(total,size));}
    public boolean scroll(double x,double y,double dy){
        int step=dy>0?-1:1;if(dy==0)return false;
        if(x>=25&&x<176&&y>=100&&y<=403){inventoryOffset=Mth.clamp(inventoryOffset+step*2,0,Math.max(0,weapons.size()-9));return true;}
        if(x>=192&&x<542&&y>=101&&y<195){socketOffset=clampPage(socketOffset,step,SLOTS_PAGE,installed.size());return true;}
        if(x>=192&&x<542&&y>=222&&y<404){libraryOffset=clampPage(libraryOffset,step,LIBRARY_PAGE,visible.size());return true;}
        return false;
    }

    public boolean key(int key,Player p){
        refresh(p);
        switch(key){
            case GLFW.GLFW_KEY_ENTER,GLFW.GLFW_KEY_KP_ENTER -> toggle(p);
            case GLFW.GLFW_KEY_F -> fuse(p);
            case GLFW.GLFW_KEY_LEFT,GLFW.GLFW_KEY_RIGHT,GLFW.GLFW_KEY_UP,GLFW.GLFW_KEY_DOWN -> {
                if(!visible.isEmpty()){
                    int delta=key==GLFW.GLFW_KEY_LEFT?-1:key==GLFW.GLFW_KEY_RIGHT?1:key==GLFW.GLFW_KEY_UP?-COLUMNS:COLUMNS;
                    int i=Mth.clamp(Math.max(0,visible.indexOf(selected))+delta,0,visible.size()-1);
                    selected=visible.get(i);libraryOffset=i/LIBRARY_PAGE*LIBRARY_PAGE;inspectInstalled=false;
                }
            }
            default -> {return false;}
        }
        return true;
    }
}
