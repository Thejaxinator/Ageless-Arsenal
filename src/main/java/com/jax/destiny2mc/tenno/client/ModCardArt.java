package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Compact inventory cards and expanded fusion cards share the artist's texture, gilded rails
 * and rank lamps. The count along the foot is actual fusion rank; locked cards have no lamps. */
final class ModCardArt {
    static final int INK=0xff141d20, BRASS=0xffcfb78b, IVORY=0xffeee6cd, MUTED=0xffa9b7b2,
            JADE=0xff9ed9c9, DULL=0xff475854;
    private ModCardArt() {}

    static void paint(GuiGraphics g,UiCanvas c,Font font,WeaponMod mod,int rank,
                      int x,int y,int w,int h,boolean selected,boolean large,boolean installed) {
        int metal=switch(mod.rarity()){case 0->0xffbf906b;case 1->0xffb7c9cc;default->0xffdfbe79;};
        int frame=rank<0?0xff64706b:metal;
        int left=x+3,top=y+3,right=x+w-3,bottom=y+h-3;
        // Stepped shoulders and scalloped corners come from the Warframe mod-card silhouette.
        c.fill(x+5,y,w-10,h,INK);c.fill(x,y+5,w,h-10,INK);
        g.fillGradient(left,top,right,bottom,rank<0?0xff283333:0xff384b46,0xff182420);
        c.fill(x+5,y,w-10,1,frame);c.fill(x+5,y+h-1,w-10,1,frame);
        for(int side=0;side<2;side++){
            int edge=side==0?x:x+w-1,sign=side==0?1:-1;
            c.line(edge+5*sign,y,edge,y+5,frame);c.line(edge,y+5,edge,y+h-6,frame);
            c.line(edge,y+h-6,edge+5*sign,y+h-1,frame);
            c.line(edge+2*sign,y+10,edge+2*sign,y+17,frame);
            c.line(edge+2*sign,y+h-11,edge+2*sign,y+h-17,frame);
        }
        if(selected){c.fill(x+5,y,w-10,2,JADE);c.fill(x+5,y+h-2,w-10,2,JADE);}
        int glyph=large?53:24, gx=x+(w-glyph)/2, gy=y+(large?17:4);
        // Keep each graphic crisp at Minecraft GUI scales by sampling the complete 32px tile.
        g.pose().pushPose();g.pose().translate(gx,gy,0);g.pose().scale(glyph/32f,glyph/32f,1);
        g.blit(mod.icon(),0,0,0,0,32,32,32,32);g.pose().popPose();
        int mid=x+w/2;
        if(large){
            c.line(x+12,y+11,mid-7,y+11,frame);c.line(mid+7,y+11,x+w-12,y+11,frame);for(int i=0;i<=mod.rarity();i++)c.diamond(mid-mod.rarity()*3+6*i,y+11,2,frame);
            centered(c,font,mid,y+76,mod.title(),1.18f,IVORY,w-16);
            centered(c,font,mid,y+93,mod.category(),.82f,MUTED,w-16);
            c.fill(x+14,y+108,w-28,1,DULL);
            wrapped(c,font,x+13,y+119,w-26,mod.effect(Math.max(0,rank)),1f,3,JADE);
            String label=rank<0?"LOCKED":"RANK "+rank+" / "+mod.maxRank();
            centered(c,font,mid,y+h-30,label,.82f,IVORY,w-12);
        }else{
            centered(c,font,mid,y+30,mod.title(),.85f,IVORY,w-10);
            String label=rank<0?"Locked":installed?"Installed":"Rank "+rank;
            centered(c,font,mid,y+42,label,.72f,installed?JADE:MUTED,w-10);
        }
        int n=mod.maxRank(),step=Math.min(9,(w-22)/Math.max(1,n)),start=mid-n*step/2;
        for(int i=0;i<n;i++)c.fill(start+i*step,y+h-(large?13:8),Math.max(2,step-2),3,i<rank?JADE:0xff485751);
    }

    static void centered(UiCanvas c,Font f,int cx,int y,String text,float scale,int color,int maxWidth){
        String fit=f.plainSubstrByWidth(text,(int)(maxWidth/scale));
        c.text(cx-Math.round(f.width(fit)*scale/2),y,fit,color,scale);
    }

    static void wrapped(UiCanvas c,Font font,int x,int y,int width,String text,float scale,int lines,int color) {
        int max=(int)(width/scale),row=0;
        String buffer="";
        for(String word:text.split(" ")){
            String next=buffer.isEmpty()?word:buffer+" "+word;
            if(!buffer.isEmpty()&&font.width(next)>max){
                c.text(x,y+row*12,buffer,color,scale);
                if(++row==lines)return;
                buffer=word;
            }else buffer=next;
        }
        if(!buffer.isEmpty()&&row<lines)c.text(x,y+row*12,font.plainSubstrByWidth(buffer,max),color,scale);
    }
}
