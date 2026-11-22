package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.TennoGunSpec;
import com.jax.destiny2mc.tenno.TennoGunStats;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.jax.destiny2mc.tenno.melee.MeleeModStats;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Slim metal borders, a diegetic equipment icon and right-aligned Arsenal statistics.
 * Inspection always reads the displayed stack, including partial magazines and durability. */
@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class TennoInspection {
    private static final int GOLD=0xffdbc695, IVORY=0xfff0ecdf, PARCHMENT=0xffc9c2aa,
        PALE=0xffc1d9ce, RULE=0xff53564b, INK=0xff121918, DEEP=0xff1b2521;
    private static final DecimalFormat NUMBER=new DecimalFormat("0.##",DecimalFormatSymbols.getInstance(Locale.ROOT));
    private TennoInspection() {}

    record Stat(String title,String value) {}
    record Info(String category,String variant,String note,List<Stat> stats,String technique) {}
    private static String number(double n) {return NUMBER.format(n);}
    private static boolean detailed() {
        return Screen.hasShiftDown()||Minecraft.getInstance().options.advancedItemTooltips;
    }

    static Info describe(ItemStack stack) {
        var rows=new ArrayList<Stat>();
        var player=Minecraft.getInstance().player;
        var fitted=WeaponMods.installed(stack);
        int active=(int)fitted.stream().filter(mod -> ModCollection.rank(player,mod.id())>=0).count();
        String damageLabel=active>0?"Damage":"Base damage";
        if(stack.getItem() instanceof TennoGunItem gun) {
            var p=gun.profile();
            var stats=TennoGunStats.of(player,stack);
            rows.add(new Stat(damageLabel,number(stats.damage())+" HP"));
            boolean capacitor=p.family==TennoGunSpec.Family.BUBONICO;
            rows.add(new Stat(capacitor?"Battery":"Magazine",TennoGunItem.magazine(player,stack)+" / "+stats.magazine()));
            if(!capacitor)rows.add(new Stat("Reload",number(stats.reloadTicks()/20.0)+" s"));
            rows.add(new Stat("Range",p.range()+" m"));
            rows.add(new Stat("Trigger",p.mode()));
            if(stats.multishot()>0)rows.add(new Stat("Multishot","+"+number(100*stats.multishot())+"%"));
            if(stats.rateFactor()>1.00001)rows.add(new Stat("Fire rate","×"+number(stats.rateFactor())));
            addSocketCount(rows,active,fitted.size());
            String slot=switch(p.family){
                case TWINS,BRAKK,MICROWAVE,VESPER,CANTARE,AKSONDOL -> "Secondary";
                default -> "Primary";
            };
            return new Info(slot+"  /  "+p.kind(),p.variant(),p.note(),rows,"");
        }
        var p=((TennoMeleeItem)stack.getItem()).profile();
        var stats=MeleeModStats.of(player,stack);
        rows.add(new Stat(damageLabel,number(stats.damage())+" HP"));
        rows.add(new Stat("Attack speed",number(stats.speed())+" / s"));
        rows.add(new Stat("Reach",number(stats.reach())+" m"));
        rows.add(new Stat("Blocking angle",p.guardAngle()+"°"));
        rows.add(new Stat("Durability",(stack.getMaxDamage()-stack.getDamageValue())+" / "+stack.getMaxDamage()));
        if(stats.heat()>0)rows.add(new Stat("Bonus heat","+"+number(100*stats.heat())+"%"));
        if(stats.heavy()>0)rows.add(new Stat("Heavy attack","+"+number(100*stats.heavy())+"%"));
        if(stats.parry()>0)rows.add(new Stat("Parry chance",number(100*stats.parry())+"%"));
        addSocketCount(rows,active,fitted.size());
        return new Info("Melee  /  "+p.family(),"",p.description(),rows,p.trait());
    }

    private static void addSocketCount(List<Stat> rows,int active,int fitted) {
        if(fitted>0)rows.add(new Stat("Installed mods",active==fitted?Integer.toString(fitted):active+" / "+fitted+" active"));
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void inventory(RenderTooltipEvent.Pre event) {
        var stack=event.getItemStack();
        if(!TennoBlueprints.isTenno(stack.getItem())||detailed()
                ||stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)||stack.has(DataComponents.HIDE_TOOLTIP))return;
        event.setCanceled(true);
        paint(event.getGraphics(),stack,event.getX(),event.getY());
    }

    public static void draw(GuiGraphics graphics,ItemStack stack,int mouseX,int mouseY) {
        if(stack.isEmpty()||stack.has(DataComponents.HIDE_TOOLTIP))return;
        if(detailed()||stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            graphics.renderTooltip(Minecraft.getInstance().font,stack,mouseX,mouseY);
        }else paint(graphics,stack,mouseX,mouseY);
    }

    static int trim(String variant) {
        return switch(variant) {
            case "Kuva" -> 0xffdd998b;
            case "Coda" -> 0xffb8b0d8;
            case "Prime" -> 0xffeed08a;
            case "Prisma","Vandal" -> 0xff96c8ce;
            case "Wraith" -> 0xffc89195;
            default -> GOLD;
        };
    }

    private static String controls(ItemStack stack) {
        var options=Minecraft.getInstance().options;
        if(stack.getItem() instanceof TennoMeleeItem)
            return options.keyUse.getTranslatedKeyMessage().getString()+"  Guard   ·   "
                +options.keyShift.getTranslatedKeyMessage().getString()+" + "
                +options.keyAttack.getTranslatedKeyMessage().getString()+"  Charged heavy";
        if(stack.getItem() instanceof TennoGunItem gun && gun.profile().alternate())
            for(var mapping:options.keyMappings) if(mapping.getName().equals("key.destiny2mc.tenno_alt"))
                return mapping.getTranslatedKeyMessage().getString()+"  Alternate fire";
        return "";
    }

    private static void paint(GuiGraphics g,ItemStack stack,int mx,int my) {
        Font f=Minecraft.getInstance().font;
        Info info=describe(stack);
        int width=Mth.clamp(g.guiWidth()/2,184,224);
        List<FormattedCharSequence> title=f.split(stack.getHoverName(),width-53);
        int titleRows=Math.min(2,Math.max(1,title.size()));
        int categoryY=21+titleRows*11;
        int headerBottom=Math.max(49,categoryY+13);
        int step=f.lineHeight+2;
        int statStart=headerBottom+7;
        int bodyStart=statStart+step*info.stats().size()+8;
        var note=f.split(Component.literal(info.note()),width-19);
        String controls=controls(stack);
        var binding=controls.isBlank()?List.<FormattedCharSequence>of():f.split(Component.literal(controls),width-19);
        int bindingHeight=binding.isEmpty()?0:binding.size()*step+5;
        int h=bodyStart+(info.technique().isBlank()?0:step+2)+note.size()*step+bindingHeight+20;
        float scale=Math.min(1f,Math.min((g.guiWidth()-4f)/width,(g.guiHeight()-4f)/h));
        if(scale<=0)return;
        int dw=(int)Math.ceil(width*scale),dh=(int)Math.ceil(h*scale);
        int x=mx+12+dw<=g.guiWidth()-2?mx+12:mx-8-dw;
        x=Mth.clamp(x,2,Math.max(2,g.guiWidth()-dw-2));
        int y=Mth.clamp(my-9,2,Math.max(2,g.guiHeight()-dh-2));
        int accent=trim(info.variant());
        g.pose().pushPose();g.pose().translate(x,y,420);g.pose().scale(scale,scale,1);
        g.fill(2,2,width+2,h+2,0x79000000);
        g.fillGradient(0,0,width,h,DEEP,INK);
        g.fill(0,0,width,1,accent);
        g.fill(0,h-1,width,h,accent);
        // The twin notches and the little leaf motif are original pixel motifs.
        for(int i=0;i<4;i++) {
            g.fill(i,1,1+i,5-i,accent);g.fill(width-i-1,1,width-i,5-i,accent);
            g.fill(i,h-5+i,1+i,h-1,accent);g.fill(width-1-i,h-5+i,width-i,h-1,accent);
        }
        leaf(g,9,5,accent);
        String edition=info.variant().isBlank()||"Standard".equals(info.variant())?"ARSENAL":"ARSENAL   /   "+info.variant().toUpperCase(Locale.ROOT);
        tiny(g,f,edition,22,6,accent,.78f);
        for(int i=0;i<titleRows&&i<title.size();i++)g.drawString(f,title.get(i),9,19+i*11,IVORY,false);
        tiny(g,f,info.category(),9,categoryY,PARCHMENT,.86f);
        g.pose().pushPose();g.pose().translate(width-39,12,0);g.pose().scale(1.9f,1.9f,1);
        g.renderItem(stack,0,0);g.pose().popPose();
        g.fill(9,headerBottom,width-9,headerBottom+1,RULE);
        int rowY=statStart;
        for(Stat stat:info.stats()) {
            g.drawString(f,stat.title(),9,rowY,PARCHMENT,false);
            g.drawString(f,stat.value(),width-10-f.width(stat.value()),rowY,PALE,false);
            rowY+=step;
        }
        g.fill(9,rowY+2,width-9,rowY+3,RULE);
        int noteY=bodyStart;
        if(!info.technique().isBlank()){g.drawString(f,info.technique(),9,noteY,accent,false);noteY+=step+2;}
        for(var line:note){g.drawString(f,line,9,noteY,IVORY,false);noteY+=step;}
        if(!binding.isEmpty()) {
            noteY+=4;
            for(var line:binding){g.drawString(f,line,9,noteY,PALE,false);noteY+=step;}
        }
        tiny(g,f,"Shift   Item details",9,h-12,PARCHMENT,.82f);
        g.pose().popPose();
    }

    private static void leaf(GuiGraphics g,int x,int y,int color) {
        g.fill(x+4,y,x+5,y+7,color);
        for(int i=0;i<4;i++){
            g.fill(x+i,y+1+i,x+i+1,y+3+i,color);
            g.fill(x+8-i,y+1+i,x+9-i,y+3+i,color);
        }
    }

    private static void tiny(GuiGraphics g,Font font,String text,int x,int y,int color,float scale) {
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);
        g.drawString(font,text,0,0,color,false);g.pose().popPose();
    }
}
