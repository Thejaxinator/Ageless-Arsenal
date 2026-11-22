package com.jax.destiny2mc.binder.difficulty;

import java.util.EnumMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** The buttons display the server's last acknowledged preset. Non-operators may inspect all four. */
public final class DifficultyScreen extends Screen {
    private final Screen previous;
    private final EnumMap<EncounterPreset, Button> choices=new EnumMap<>(EncounterPreset.class);
    private EncounterPreset current,selected;
    private int revision;
    private boolean editable,waiting;
    private Button apply;
    public DifficultyScreen(Screen previous){super(Component.literal("Encounter difficulty"));this.previous=previous;}
    @Override public boolean isPauseScreen(){return false;}
    private int panelWidth(){return Math.min(350,width-12);}
    private int top(){return Math.max(1,(height-188)/2);}
    @Override protected void init(){
        choices.clear();
        int w=panelWidth(),x=(width-w)/2+12,y=top(),buttonW=(w-28)/2;
        for(var p:EncounterPreset.values()){
            int i=p.ordinal();
            choices.put(p,addRenderableWidget(new PresetButton(Button.builder(Component.literal(p.title),b->{selected=p;refresh();})
                .bounds(x+(i%2)*(buttonW+4),y+38+(i/2)*22,buttonW,20),p)));
        }
        apply=addRenderableWidget(new PresetButton(Button.builder(Component.literal("Apply to world"),b->{
            if(!editable||selected==null||waiting)return;
            waiting=true;refresh();
            PacketDistributor.sendToServer(new DifficultyNetwork.Request(selected.id,revision));
        }).bounds(x,y+154,buttonW,20),null));
        addRenderableWidget(new PresetButton(Button.builder(Component.literal("Back"),b->onClose()).bounds(x+buttonW+4,y+154,buttonW,20),null));
        refresh();
        PacketDistributor.sendToServer(new DifficultyNetwork.Request("",-1));
    }
    /** A vanilla Button retains focus, narration, keyboard and click semantics. Match the station theme in its paint. */
    private final class PresetButton extends Button {
        private final EncounterPreset preset;
        PresetButton(Builder builder, EncounterPreset preset){super(builder);this.preset=preset;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
            boolean chosen=preset!=null && preset==selected;
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();
            int edge=isHoveredOrFocused()?0xffefeee7:chosen?0xff85e2ce:0xff40515a;
            g.fill(x,y,x+w,y+h,edge);
            g.fill(x+1,y+1,x+w-1,y+h-1,chosen?0xff85e2ce:active?0xff1c2a32:0xff263037);
            int color=chosen?0xff0c2423:active?0xffefeee7:0xffa2a9ab;
            g.drawString(font,getMessage(),x+(w-font.width(getMessage()))/2,y+(h-font.lineHeight)/2+1,color,false);
        }
    }
    private void refresh(){
        if(apply!=null)apply.active=editable && !waiting && selected!=null && selected!=current;
    }
    public static void receive(DifficultyNetwork.State msg){
        var mc=Minecraft.getInstance();
        if(msg.open() && !(mc.screen instanceof DifficultyScreen))mc.setScreen(new DifficultyScreen(mc.screen));
        if(!(mc.screen instanceof DifficultyScreen s))return;
        var next=EncounterPreset.find(msg.key());if(next==null)return;
        if(s.selected==null||s.waiting||s.current!=next)s.selected=next;
        s.current=next;s.revision=msg.revision();s.editable=msg.editable();s.waiting=false;s.refresh();
    }
    @Override public void onClose(){minecraft.setScreen(previous);}
    private static int percent(float n){return Math.round(100*n);}
    // Screen.render calls renderBackground before widgets; this screen paints its own backdrop.
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        int w=panelWidth(),x=(width-w)/2,y=top();
        g.fill(0,0,width,height,0xff0b1318);g.fillGradient(x,y,x+w,y+186,0xff213039,0xff111c22);
        g.fill(x,y,x+2,y+186,0xff85e2ce); g.fill(x+12,y+132,x+w-12,y+133,0xff40515a);
        g.drawString(font,title,x+12,y+9,0xfff0f5ee,false);
        String live=current==null?"Reading world settings…":"World setting   /   "+current.title;
        g.drawString(font,live,x+12,y+24,0xff9cd6cb,false);
        if(selected!=null){
            String limits=selected==EncounterPreset.ORIGINAL?"Hit caps   /   Uncapped"
                :"Hit caps   /   "+(int)selected.rankCap+" HP regular, "+(int)selected.bossCap+" HP boss";
            g.drawString(font,"Enemy damage   /   "+percent(selected.rankScale)+"% regular, "+percent(selected.bossScale)+"% boss",x+12,y+89,0xfff0f5ee,false);
            g.drawString(font,limits,x+12,y+103,0xffc8d9d6,false);
            g.drawString(font,"Rapid follow-up   /   "+percent(selected.followUp)+"% damage",x+12,y+117,0xffc8d9d6,false);
        }
        if(current!=null)g.drawString(font,editable?"Applies to everyone in this world.":"World owner or operator access required to edit.",x+12,y+137,0xff9cd6cb,false);
        super.render(g,mx,my,dt);
    }
}
