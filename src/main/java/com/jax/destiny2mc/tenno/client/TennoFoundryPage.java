package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.GameCanvas;
import com.jax.destiny2mc.client.ui.GunsmithLayout;
import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.foundry.FoundryNetwork;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import java.util.List;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Warframe Foundry page inside both Gunsmith blocks. The same recipe lookup governs rows,
 * affordability and the authoritative craft. List indices never cross the network. */
public final class TennoFoundryPage {
    private static final int IVORY=0xffeee9d9, GOLD=0xffdcc292, DIM=0xffaaa996,
        MUTED=0xff798d86, AQUA=0xffb2d7c9, INK=0xff141c1c, RULE=0xff4b5650;
    private static final String[] FILTERS={"All","Ranged","Melee"};
    private static long serial;
    private static FoundryNetwork.Result latest;
    private int filter, index, offset;
    private long pending, nextClick;
    private String orderedName="", feedback="";
    private List<Item> entries=List.of();
    private List<ItemStack> costs=List.of();

    public static void receive(FoundryNetwork.Result reply) { latest=reply; }
    private void refresh(Player p) {
        if(p==null){entries=List.of(); costs=List.of(); return;}
        List<Item> all=TennoBlueprints.entries(p);
        entries=switch(filter) {
            case 1 -> all.stream().filter(TennoGunItem.class::isInstance).toList();
            case 2 -> all.stream().filter(TennoMeleeItem.class::isInstance).toList();
            default -> all;
        };
        index=Mth.clamp(index,0,Math.max(0,entries.size()-1));
        offset=Mth.clamp(offset,0,Math.max(0,entries.size()-12));
        costs=entries.isEmpty()?List.of():TennoBlueprints.cost(p,entries.get(index));
        if(pending!=0 && latest!=null && latest.serial()==pending) {
            feedback=latest.success()?orderedName+" assembled.":"Assembly declined. Check materials and bench reach.";
            pending=0;
        }
    }

    private ItemStack stack(int token) {
        if(token<0) return costs.get(-token-1);
        return token>=entries.size()?ItemStack.EMPTY:entries.get(token).getDefaultInstance();
    }

    public void draw(GuiGraphics graphics,Font font,Player player) {
        refresh(player);
        UiCanvas c=new GameCanvas(graphics,font,this::stack);
        c.fill(0,0,800,450,INK);
        c.fill(0,70,800,349,0xff1b2524);
        c.fill(0,70,230,349,0xff202a28);
        c.fill(511,70,289,349,0xff182120);
        for(int i=0;i<FILTERS.length;i++) {
            int x=27+i*66;
            c.fill(x,84,62,23,i==filter?0xff454b3e:0xff24312d);
            c.text(x+8,92,FILTERS[i].toUpperCase(Locale.ROOT),i==filter?GOLD:DIM,.82f);
            if(i==filter)c.fill(x,105,62,2,GOLD);
        }
        int end=Math.min(entries.size(),offset+12);
        for(int i=offset;i<end;i++){
            int y=115+(i-offset)*22;
            if(i==index){c.fill(27,y,196,20,0xff465044);c.fill(27,y,2,20,GOLD);}
            else c.fill(34,y+20,183,1,0xff313b36);
            c.text(38,y+7,stack(i).getHoverName().getString(),i==index?IVORY:DIM,.87f);
        }
        c.text(34,389,entries.size()+" blueprints",DIM,.8f);
        if(!entries.isEmpty()){
            ItemStack selected=stack(index);
            TennoInspection.Info info=TennoInspection.describe(selected);
            c.text(245,87,selected.getHoverName().getString(),IVORY,1.38f);
            c.text(246,110,info.category(),GOLD,.82f);
            // The blueprint is staged against a sparse calibration reticle.
            c.line(246,133,271,133,RULE);c.line(246,133,246,147,RULE);
            c.line(495,320,470,320,RULE);c.line(495,320,495,306,RULE);
            c.line(250,226,275,226,RULE);c.line(466,226,491,226,RULE);
            c.ring(371,228,76,76,0xff33403b);
            c.item(index,272,137,202);
            c.wrap(246,340,info.note(),43,DIM);
            c.text(530,90,"Fabrication requirements",IVORY,1.07f);
            c.fill(530,111,238,1,RULE);
            for(int i=0;i<costs.size();i++) {
                ItemStack material=costs.get(i);
                int y=126+42*i, need=material.getCount(), have=TennoBlueprints.available(player,material.getItem());
                c.item(-i-1,532,y,23);
                c.text(564,y+1,material.getHoverName().getString(),DIM,.92f);
                c.text(564,y+15,have+" / "+need,have>=need?AQUA:0xffe5aa94,.84f);
                c.fill(531,y+34,237,1,0xff2f3935);
            }
            boolean ready=TennoBlueprints.canCraft(player,selected.getItem());
            boolean active=ready && pending==0 && Util.getMillis()>=nextClick;
            c.fill(526,363,246,26,active?0xffd4c29a:0xff36413c);
            c.outline(526,363,246,26,active?GOLD:RULE);
            c.text(541,372,pending!=0?"ASSEMBLING…":ready?"ASSEMBLE WEAPON":"MATERIALS REQUIRED",active?INK:DIM,.92f);
        }else c.text(246,122,"No blueprints available.",DIM,1);
        c.fill(27,415,746,1,RULE);
        c.text(28,429,"Esc   Close",DIM,.81f);
        if(!feedback.isEmpty())c.text(245,429,feedback,AQUA,.84f);
    }

    public void hover(GuiGraphics g,double x,double y,int mx,int my) {
        int row=GunsmithLayout.rowHit(x,y,offset,entries.size());
        boolean model=x>=272&&x<474&&y>=137&&y<339;
        if(row>=0||model&&!entries.isEmpty())TennoInspection.draw(g,stack(row<0?index:row),mx,my);
    }

    private void changeFilter(int next) {
        filter=Math.floorMod(next,FILTERS.length);
        index=0;offset=0;feedback="";
    }

    public boolean click(double x,double y,Player player) {
        refresh(player);
        if(y>=84&&y<107&&x>=27&&x<221){
            int relative=(int)x-27;
            if(relative%66<62)changeFilter(relative/66);
            return true;
        }
        int row=GunsmithLayout.rowHit(x,y,offset,entries.size());
        if(row>=0){index=row;feedback="";return true;}
        if(x>=526&&x<772&&y>=363&&y<389){assemble(player);return true;}
        return false;
    }


    public boolean scroll(double x,double y,double dy) {
        if(x<26||x>=224||y<115||y>=402||dy==0)return false;
        offset=Mth.clamp(offset+(dy>0?-3:3),0,Math.max(0,entries.size()-12));
        return true;
    }

    public boolean key(int key,Player player) {
        switch(key) {
            case GLFW.GLFW_KEY_RIGHT -> changeFilter(filter+1);
            case GLFW.GLFW_KEY_LEFT -> changeFilter(filter-1);
            case GLFW.GLFW_KEY_DOWN,GLFW.GLFW_KEY_UP -> {
                index=Mth.clamp(index+(key==GLFW.GLFW_KEY_DOWN?1:-1),0,Math.max(0,entries.size()-1));
                offset=Mth.clamp(offset,Math.max(0,index-11),index);
            }
            case GLFW.GLFW_KEY_ENTER,GLFW.GLFW_KEY_KP_ENTER -> {refresh(player);assemble(player);}
            default -> {return false;}
        }
        return true;
    }

    private void assemble(Player p) {
        if(entries.isEmpty()||p==null||pending!=0||Util.getMillis()<nextClick)return;
        Item item=entries.get(index);
        if(!TennoBlueprints.canCraft(p,item))return;
        pending=++serial;
        nextClick=Util.getMillis()+450;
        orderedName=item.getDescription().getString();
        feedback="";
        PacketDistributor.sendToServer(new FoundryNetwork.Assemble(BuiltInRegistries.ITEM.getKey(item),pending));
    }
}
