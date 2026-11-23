package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import static com.jax.destiny2mc.binder.client.BinderGraphics.*;

public final class EnergyBinderScreen extends Screen {
    private static final int W=560,H=330,ROWS=6;
    private static final String[] TABS={"Grenade","Melee","Class","Passive"};
    private int tab,scroll,slot;
    private String selected="",focus="",lastMessage="";
    private Object signature;
    private boolean switchingScreen, pending, inspectFocus;
    private int pendingTicks;
    private BinderSnapshot beforeRequest;
    private final List<Control> controls=new ArrayList<>();
    private float scale;
    private int ox,oy;
    public EnergyBinderScreen(){super(Component.literal("Energy Binder"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        scale=Math.min(width/(float)W,height/(float)H);
        ox=Math.round((width-W*scale)/2);oy=Math.round((height-H*scale)/2);
        signature=null;switchingScreen=false;
        BinderClientState.refresh();sync();
    }
    @Override public void removed(){if(!switchingScreen && minecraft!=null && minecraft.getConnection()!=null)BinderClientState.close();}
    @Override public void tick(){
        if(pending && (BinderClientState.snapshot()!=beforeRequest||--pendingTicks<=0))pending=false;
        sync();
    }
    private List<BinderAbility> entries(){
        return BinderCatalog.all().stream().filter(a->!a.isWarframe()).filter(a->switch(tab){
            case 0->a.category().equalsIgnoreCase("grenade");case 1->a.category().equalsIgnoreCase("melee");
            case 2->!passive(a)&&!a.category().equalsIgnoreCase("melee")&&!a.category().equalsIgnoreCase("grenade");
            default->passive(a);
        }).toList();
    }
    private BinderAbility byId(String id){return BinderCatalog.all().stream().filter(a->a.id().equals(id)).findFirst().orElse(null);}
    private String installed(BinderSnapshot s,int index){return index<4?s.active().get(index):s.passive().get(index-4);}
    private void tab(int next){
        tab=next;scroll=0;selected="";slot=next==3?4:0;focus="tab"+tab;signature=null;sync();
    }
    private void scroll(int amount){scroll=Math.clamp(scroll+amount,0,Math.max(0,entries().size()-ROWS));signature=null;sync();}
    private void send(Runnable packet){
        if(pending)return;
        beforeRequest=BinderClientState.snapshot();pending=true;pendingTicks=40;packet.run();signature=null;
    }
    private void sync(){
        List<BinderAbility> list=entries();
        if(!list.isEmpty() && list.stream().noneMatch(a->a.id().equals(selected)))selected=list.getFirst().id();
        BinderSnapshot state=BinderClientState.snapshot();
        Object next=List.of(tab,scroll,slot,selected,state,pending);
        if(next.equals(signature))return;
        signature=next;
        if(getFocused() instanceof Control c)focus=c.id;
        clearWidgets();controls.clear();
        for(int i=0;i<TABS.length;i++){int t=i;add("tab"+i,TABS[i]+" abilities",14+i*87,50,84,19,true,()->tab(t));}
        add("warframe","Warframe abilities",449,50,97,19,true,()->{switchingScreen=true;minecraft.setScreen(new WarframeBinderScreen());});
        for(int i=0;i<ROWS&&scroll+i<list.size();i++){
            BinderAbility a=list.get(scroll+i);
            add("entry"+a.id(),a.name()+", "+a.element()+", "+(state.unlocked().contains(a.id())?"owned":"locked"),14,79+i*24,204,22,true,()->{selected=a.id();}).ability=a;
        }
        add("prev","Previous abilities",14,225,24,15,scroll>0,()->scroll(-ROWS));
        add("next","More abilities",193,225,25,15,scroll+ROWS<list.size(),()->scroll(ROWS));
        BinderAbility a=byId(selected);
        if(a!=null){
            boolean owned=state.unlocked().contains(selected);
            boolean available=owned?!installed(state,slot).equals(selected):state.motes()>=a.motes()&&state.levels()>=a.levels();
            add("commit",owned?"Bind "+a.name()+" to "+slotLabel(slot):"Unlock "+a.name()+" for "+a.motes()+" motes and "+a.levels()+" levels",235,211,185,27,available&&!pending,
                ()->send(()->{if(owned)BinderClientState.equip(a.id(),slot);else BinderClientState.purchase(a.id());}));
            add("clear","Clear "+slotLabel(slot),425,211,114,27,!installed(state,slot).isEmpty()&&!pending,
                ()->send(()->BinderClientState.equip("",slot)));
        }
        for(int i=0;i<6;i++){
            final int k=i;String id=installed(state,i);BinderAbility cur=byId(id);
            add("slot"+i,slotLabel(i)+", "+(cur==null?"empty":cur.name()),14+i*89,267,85,40,true,()->{
                if((tab==3)!=(k>=4)){tab=k>=4?3:0;scroll=0;selected="";}
                slot=k;
            }).ability=cur;
        }
        for(var c:controls)if(c.id.equals(focus)&&c.active){setFocused(c);break;}
        if(!state.message().isBlank()&&!state.message().equals(lastMessage)){
            lastMessage=state.message();minecraft.getNarrator().sayNow(Component.literal(lastMessage));
        }
    }
    private static String slotLabel(int i){return i<4?"active "+(i+1):"passive "+(i-3);}
    private final class Control extends AbstractWidget{
        final String id;final Runnable action;
        BinderAbility ability;
        Control(String id,String name,int x,int y,int w,int h,boolean enabled,Runnable action){
            super(ox+Math.round(x*scale),oy+Math.round(y*scale),Math.max(1,Math.round(w*scale)),Math.max(1,Math.round(h*scale)),Component.literal(name));
            this.id=id;this.action=action;active=enabled;
        }
        void activate(){if(active){focus=id;action.run();signature=null;sync();}}
        @Override public void onClick(double x,double y){activate();}
        @Override public boolean keyPressed(int k,int sc,int mods){if(k==GLFW.GLFW_KEY_ENTER||k==GLFW.GLFW_KEY_SPACE||k==GLFW.GLFW_KEY_KP_ENTER){activate();return true;}return false;}
        @Override protected void renderWidget(GuiGraphics g,int x,int y,float t){}
        @Override protected void updateWidgetNarration(NarrationElementOutput out){defaultButtonNarrationText(out);}
    }
    private Control add(String id,String name,int x,int y,int w,int h,boolean enabled,Runnable r){
        var c=new Control(id,name,x,y,w,h,enabled,r);controls.add(c);addWidget(c);return c;
    }
    private boolean hover(String id,int mx,int my){
        for(var c:controls)if(c.id.equals(id))return c.active&&(c.isMouseOver(mx,my)||c.isFocused());return false;
    }
    private void button(GuiGraphics g,String id,String label,int x,int y,int w,int h,boolean selected,boolean enabled,int mx,int my){
        int bg=enabled?(selected?ACCENT:PANEL):0xff263037,fg=enabled?(selected?INK:LIGHT):0xffa2a9ab;
        g.fill(x,y,x+w,y+h,bg);frame(g,x,y,w,h,hover(id,mx,my)?LIGHT:selected?ACCENT:LINE);
        text(g,font,clip(font,label,w-6),x+(w-Math.min(w-6,font.width(label)))/2,y+(h-8)/2,fg);
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float t){}
    @Override public void render(GuiGraphics g,int mx,int my,float t){
        sync();var s=BinderClientState.snapshot();var list=entries();BinderAbility a=byId(selected);
        g.fill(0,0,width,height,0xff0b1318);
        g.pose().pushPose();g.pose().translate(ox,oy,0);g.pose().scale(scale,scale,1);
        g.fillGradient(0,0,W,H,0xff213039,BG);
        g.fill(14,10,17,41,ACCENT);text(g,font,"PARACAUSAL",23,10,ACCENT);
        g.pose().pushPose();g.pose().translate(22,24,0);g.pose().scale(1.7f,1.7f,1);text(g,font,"Energy Binder",0,0,LIGHT);g.pose().popPose();
        text(g,font,"MOTES OF LIGHT",363,12,DIM);text(g,font,Integer.toString(s.motes()),363,27,GOLD);
        text(g,font,"XP LEVELS",475,12,DIM);text(g,font,Integer.toString(s.levels()),475,27,LIGHT);
        for(int i=0;i<TABS.length;i++)button(g,"tab"+i,TABS[i],14+i*87,50,84,19,tab==i,true,mx,my);
        button(g,"warframe","Warframe ›",449,50,97,19,false,true,mx,my);
        for(int i=0;i<ROWS&&scroll+i<list.size();i++){
            var row=list.get(scroll+i);int y=79+i*24;boolean chosen=row.id().equals(selected),owned=s.unlocked().contains(row.id());
            g.fill(14,y,218,y+22,chosen?0xff33434b:PANEL);
            g.fill(14,y,16,y+22,chosen?color(row.element()):LINE);AbilityGlyph.draw(g,row.id(),19,y+3,16,color(row.element()));
            text(g,font,clip(font,row.name(),143),36,y+7,LIGHT);
            text(g,font,owned?"✓":"",196,y+7,ACCENT);
            if(hover("entry"+row.id(),mx,my))frame(g,14,y,204,22,LIGHT);
        }
        button(g,"prev","‹",14,225,24,15,false,scroll>0,mx,my);
        button(g,"next","›",193,225,25,15,false,scroll+ROWS<list.size(),mx,my);
        text(g,font,(list.isEmpty()?0:scroll+1)+"–"+Math.min(scroll+ROWS,list.size())+" / "+list.size(),86,229,DIM);
        g.fill(229,79,546,243,0xff17232b);
        if(a!=null){
            int tint=color(a.element());
            g.fill(229,79,546,81,tint);frame(g,235,86,41,41,LINE);AbilityGlyph.draw(g,a.id(),239,90,32,tint);
            text(g,font,title(a.element())+" / "+title(a.category()),284,89,tint);
            String n=clip(font,a.name(),186);
            g.pose().pushPose();g.pose().translate(284,104,0);g.pose().scale(1.35f,1.35f,1);text(g,font,n,0,0,LIGHT);g.pose().popPose();
            int yy=133;
            for(var line:font.split(Component.literal(a.description()),295)){if(yy>=175)break;g.drawString(font,line,236,yy,DIM,false);yy+=10;}
            String cooldown=a.cooldownTicks()>0?"Cooldown  "+(a.cooldownTicks()/20f==a.cooldownTicks()/20?Integer.toString(a.cooldownTicks()/20):String.format(Locale.ROOT,"%.1f",a.cooldownTicks()/20f))+" s":"Always on while bound";
            text(g,font,cooldown,236,176,DIM);
            g.fill(235,189,539,190,LINE);
            boolean owned=s.unlocked().contains(selected),bound=installed(s,slot).equals(selected),canPay=s.motes()>=a.motes()&&s.levels()>=a.levels();
            text(g,font,owned?"Unlocked   /   Selected "+slotLabel(slot):a.motes()+" Motes of Light  +  "+a.levels()+" XP levels",236,196,owned?ACCENT:canPay?GOLD:DIM);
            button(g,"commit",owned?(bound?"Bound":"Bind to "+slotLabel(slot)):"Unlock",235,211,185,27,true,!pending&&(owned?!bound:canPay),mx,my);
            button(g,"clear","Clear slot",425,211,114,27,false,!pending&&!installed(s,slot).isEmpty(),mx,my);
        }
        text(g,font,"ACTIVE SLOTS",14,253,DIM);text(g,font,"PASSIVES",370,253,DIM);
        for(int i=0;i<6;i++){
            int x=14+i*89;var current=byId(installed(s,i));boolean chosen=slot==i;
            g.fill(x,267,x+85,307,chosen?0xff30443f:PANEL);frame(g,x,267,85,40,hover("slot"+i,mx,my)?LIGHT:chosen?ACCENT:LINE);
            String key=i<4?BinderKeys.keyName(i):"P"+(i-3);
            text(g,font,clip(font,key,69),x+6,272,chosen?ACCENT:DIM);
            if(current!=null){
                int glyphColor=current.isWarframe()?WarframeBinderCatalog.accentColor(current.id()):color(current.element());
                AbilityGlyph.draw(g,current.id(),x+65,268,16,glyphColor);
            }
            String label=current==null?"Empty":current.name();int y=284;
            for(var line:font.split(Component.literal(label),74)){if(y>294)break;g.drawString(font,line,x+6,y,current==null?DIM:LIGHT,false);y+=9;}
        }
        text(g,font,clip(font,pending?"Waiting for the station…":s.message().isBlank()?"Select a slot to change its binding.":s.message(),W-28),14,315,DIM);
        g.pose().popPose();
        drawInspectCard(g,mx,my,a);
    }

    /** Inspect in screen coordinates after the scaled shop has finished. Only the catalog
     * rows, equipped slots and selected title/icon are targets, leaving buy/clear controls free. */
    private void drawInspectCard(GuiGraphics g,int mx,int my,BinderAbility selectedAbility){
        if (inspectFocus && getFocused() instanceof Control focused) {
            if (focused.ability!=null) GearHoverCard.drawAbility(g,focused.ability,focused.getX()+focused.getWidth(),focused.getY());
            return;
        }
        for (Control c : controls) {
            if (c.isMouseOver(mx,my)) {
                if (c.ability!=null) GearHoverCard.drawAbility(g,c.ability,mx,my);
                return;
            }
        }
        double x=(mx-ox)/scale, y=(my-oy)/scale;
        if (selectedAbility!=null && x>=235 && x<540 && y>=86 && y<127) {
            GearHoverCard.drawAbility(g,selectedAbility,mx,my);
        }
    }

    @Override public void mouseMoved(double x,double y){inspectFocus=false;super.mouseMoved(x,y);}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){if(x>=ox+14*scale&&x<ox+219*scale&&y>=oy+79*scale&&y<oy+240*scale&&dy!=0){scroll(dy>0?-2:2);return true;}return super.mouseScrolled(x,y,dx,dy);}
    @Override public boolean keyPressed(int key,int scan,int mods){
        inspectFocus=true;
        if(key==GLFW.GLFW_KEY_PAGE_DOWN||key==GLFW.GLFW_KEY_PAGE_UP){scroll(key==GLFW.GLFW_KEY_PAGE_DOWN?ROWS:-ROWS);return true;}
        return super.keyPressed(key,scan,mods);
    }
}
