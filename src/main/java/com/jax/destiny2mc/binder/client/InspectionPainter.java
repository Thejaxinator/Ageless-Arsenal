package com.jax.destiny2mc.binder.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** The entire card is measured before it is positioned. All coordinates passed by callers
 * are screen coordinates, even when a workstation renders its content at a different scale. */
final class InspectionPainter {
    private static final int PAPER=0xf20e1218, RULE=0x38ffffff, WHITE=0xfff2f4f7;
    private static final int FAINT=0xffbbc0c5, DIM=0xff92999e;
    private InspectionPainter() {}

    private record Row(List<FormattedCharSequence> labels, List<FormattedCharSequence> values, double fraction, int textHeight, int height) {}
    private record Bounds(int x,int y,float scale) {}

    private static Bounds position(int guiWidth,int guiHeight,int mx,int my,int w,int h) {
        float scale=Math.min(1f, Math.min((guiHeight-8f)/h, (guiWidth-8f)/w));
        int width=(int)Math.ceil(w*scale),height=(int)Math.ceil(h*scale);
        int x=mx+12;
        if (x+width>guiWidth-3) x=mx-9-width;
        x=Math.clamp(x,3,Math.max(3,guiWidth-width-3));
        int y=my-11;
        if (x<=mx && mx<x+width) {
            if(my+13+height<=guiHeight-3)y=my+13;
            else if(my-height-9>=3)y=my-height-9;
        }
        y=Math.clamp(y,3,Math.max(3,guiHeight-height-3));
        return new Bounds(x,y,scale);
    }

    private static int mix(int a,int b,double t) {
        int r=(int)(((a>>>16)&255)*(1-t)+((b>>>16)&255)*t);
        int g=(int)(((a>>>8)&255)*(1-t)+((b>>>8)&255)*t);
        int blue=(int)((a&255)*(1-t)+(b&255)*t);
        return 0xff000000 | r<<16 | g<<8 | blue;
    }

    private static void text(GuiGraphics g,Font f,String value,int x,int y,int color,float scale) {
        var pose=g.pose();
        pose.pushPose();pose.translate(x,y,0);pose.scale(scale,scale,1);
        g.drawString(f,value,0,0,color,false);
        pose.popPose();
    }

    private static void lines(GuiGraphics g,Font f,List<FormattedCharSequence> text,int x,int y,int color) {
        for(var line:text){g.drawString(f,line,x,y,color,false);y+=f.lineHeight+1;}
    }

    private static void plaque(GuiGraphics g,int w,int h,int accent,int header) {
        g.fill(2,2,w+2,h+2,0x63000000);
        g.fill(0,0,w,h,PAPER);
        g.fillGradient(0,1,w,header,mix(accent,0xff22252b,.31),mix(accent,0xff1a202a,.58));
        g.fill(0,0,w,2,mix(accent,WHITE,.39));
        g.fill(0,header-1,w,header,mix(accent,WHITE,.25));
        g.fill(0,header,w,header+1,0xbe0e1012);
        g.fill(0,h-1,w,h,0xff606366);
    }

    static void draw(GuiGraphics g,int mx,int my,GearHoverCard.Card card) {
        Font f=Minecraft.getInstance().font;
        int w=Math.clamp(g.guiWidth()/2,173,223);
        int step=f.lineHeight+1;
        String name=card.title().toUpperCase(Locale.ROOT);
        float nameScale=Math.min(1.18f,(w-49f)/Math.max(1,f.width(name)));
        // Wrap extremely long player-supplied names rather than shrinking them beyond recognition.
        nameScale=Math.max(.91f,nameScale);
        int titleWidth=(int)((w-49)/nameScale);
        List<FormattedCharSequence> heading=f.split(Component.literal(name),titleWidth);
        boolean cut=heading.size()>2;
        int titleLines=Math.min(2,heading.size());
        List<FormattedCharSequence> kind=f.split(Component.literal(card.kind()),w-49);
        int titleY=16,kindY=titleY+(int)Math.ceil(titleLines*step*nameScale)+2;
        int head=Math.max(43,kindY+kind.size()*step+4);
        int y=head+5;
        int metricY=y;
        if(!card.metric().isEmpty())y+=19;
        int statY=y;
        List<Row> rows=new ArrayList<>();
        int keyWidth=0;
        for(var stat:card.stats())keyWidth=Math.max(keyWidth,f.width(stat.name()));
        keyWidth=Math.min(keyWidth,w/3);
        int valueX=10+keyWidth+9;
        for(var stat:card.stats()){
            var values=f.split(Component.literal(stat.value()),w-9-valueX);
            var labels=f.split(Component.literal(stat.name()),Math.max(1,keyWidth));
            int textHeight=Math.max(1,Math.max(values.size(),labels.size()))*step;
            int rowHeight=textHeight+2+(stat.fraction()>=0?4:0);
            y+=rowHeight;
            rows.add(new Row(labels,values,stat.fraction(),textHeight,rowHeight));
        }
        int dividerY=y+1;
        y+=6;
        int perkY=y;
        var trait=card.section().isEmpty()?List.<FormattedCharSequence>of():
            f.split(Component.literal(card.section()).withStyle(ChatFormatting.BOLD),w-29);
        if(!trait.isEmpty())y+=trait.size()*step+2;
        var passage=f.split(Component.literal(card.passage()),w-18);
        int passageY=y;
        y+=passage.size()*step+4;
        int footerY=y;
        var footer=f.split(Component.literal(card.footer()),w-18);
        int height=y+5+Math.max(1,footer.size())*step;
        var bounds=position(g.guiWidth(),g.guiHeight(),mx,my,w,height);

        g.pose().pushPose();
        g.pose().translate(bounds.x,bounds.y,410);g.pose().scale(bounds.scale,bounds.scale,1);
        plaque(g,w,height,card.accent(),head);
        text(g,f,card.grade().toUpperCase(Locale.ROOT),9,5,0xffe5e2dc,.82f);
        g.pose().pushPose();g.pose().translate(8,titleY,0);g.pose().scale(nameScale,nameScale,1);
        for(int i=0;i<titleLines;i++)g.drawString(f,heading.get(i),0,i*step,WHITE,false);
        if(cut)g.drawString(f,"…",titleWidth-6,step,WHITE,false);
        g.pose().popPose();
        lines(g,f,kind,9,kindY,0xffe8e7e1);
        int wellX=w-35,wellY=(head-28)/2;
        g.fill(wellX,wellY,wellX+28,wellY+28,0x480b1016);
        // A compact corner-cut frame leaves the color field continuous with the card.
        g.fill(wellX,wellY,wellX+7,wellY+1,0x91ffffff);
        g.fill(wellX,wellY+1,wellX+1,wellY+8,0x91ffffff);
        g.fill(wellX+21,wellY+27,wellX+28,wellY+28,0x91ffffff);
        g.fill(wellX+27,wellY+20,wellX+28,wellY+27,0x91ffffff);
        if(!card.icon().isEmpty()){
            g.pose().pushPose();g.pose().translate(wellX+2,wellY+2,0);g.pose().scale(1.5f,1.5f,1);
            g.renderItem(card.icon(),0,0);g.pose().popPose();
        }else AbilityGlyph.draw(g,card.glyph(),wellX+2,wellY+2,24,WHITE);

        if(!card.metric().isEmpty()){
            text(g,f,card.metric(),8,metricY,WHITE,1.55f);
            int next=13+(int)Math.ceil(f.width(card.metric())*1.55);
            text(g,f,card.unit(),next,metricY+4,mix(card.accent(),WHITE,.71),1);
        }
        int rowY=statY;
        for(var row:rows){
            lines(g,f,row.labels(),9,rowY,FAINT);
            lines(g,f,row.values(),valueX,rowY,WHITE);
            if(row.fraction()>=0){
                int barY=rowY+row.textHeight();
                g.fill(valueX,barY,w-9,barY+2,0x26ffffff);
                int amt=(int)Math.round((w-9-valueX)*Math.clamp(row.fraction(),0,1));
                if(amt>0)g.fill(valueX,barY,valueX+amt,barY+2,WHITE);
            }
            rowY+=row.height();
        }
        g.fill(8,dividerY,w-8,dividerY+1,RULE);
        if(!trait.isEmpty()){
            int cy=perkY+4,tint=mix(card.accent(),WHITE,.4);
            for(int i=-3;i<=3;i++) {int width=3-Math.abs(i);g.fill(12-width,cy+i,13+width,cy+i+1,tint);}
            g.fill(11,cy-1,14,cy+2,0xff343336);
            lines(g,f,trait,22,perkY,WHITE);
        }
        lines(g,f,passage,9,passageY,FAINT);
        g.fill(0,footerY,w,height-1,0xa11c1d20);
        g.fill(8,footerY,w-8,footerY+1,0xff414348);
        lines(g,f,footer,9,footerY+4,mix(card.accent(),WHITE,.6));
        g.pose().popPose();
    }

    static void hint(GuiGraphics g,int mx,int my,String verb,String slots) {
        var font=Minecraft.getInstance().font;
        int width=Math.max(font.width(slots),font.width(verb))+18,height=42;
        var at=position(g.guiWidth(),g.guiHeight(),mx,my,width,height);
        g.pose().pushPose();g.pose().translate(at.x,at.y,410);g.pose().scale(at.scale,at.scale,1);
        plaque(g,width,height,GearHoverCard.PARACAUSAL,26);
        text(g,font,"PARACAUSAL",8,4,0xffe1ede8,.72f);
        text(g,font,verb,8,13,WHITE,1);
        text(g,font,slots,8,30,FAINT,1);
        g.pose().popPose();
    }
}
