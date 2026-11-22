package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.BinderAbility;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Pixel motifs use the same shape in the station, in the inventory and in loadout slots.
 * Their shape encodes the element; color augments it, including for unowned abilities. */
final class BinderGraphics {
    static final int BG=0xff111c22, PANEL=0xff1c2a32, LINE=0xff40515a, LIGHT=0xffefeee7,
            DIM=0xffb9c6c8, ACCENT=0xff85e2ce, INK=0xff0c2423, GOLD=0xfff5c177;
    private static final String[][] GLYPHS={
        {"...##..","..##...","..#.#..",".##.##.","##...##",".#...#.","..###.."},
        {"....##.","...##..","..##...","..####.","....#..","...#...","..#...."},
        {"..###..",".#...#.","#...#.#","#..#..#","#.#...#",".#...#.","..###.."},
        {"...#...",".#.#.#.","..###..","#######","..###..",".#.#.#.","...#..."},
        {"##...##","..#.#..","...#...",".##.##.","...#...","..#.#..","##...##"},
        {"...#...","..#.#..",".#.#.#.","#..#..#",".#.#.#.","..#.#..","...#..."}
    };
    static int symbol(String element){
        return switch(element.toLowerCase(Locale.ROOT)){
            case "solar"->0;case "arc"->1;case "void"->2;case "stasis"->3;case "strand"->4;default->5;
        };
    }
    static int color(String element){
        return switch(symbol(element)){case 0->0xffffac65;case 1->0xff85ddf6;case 2->0xffc3a4ff;case 3->0xff93afff;case 4->0xffa9e889;default->ACCENT;};
    }
    static void glyph(GuiGraphics g,String element,int x,int y,int pixel){
        int s=symbol(element),color=color(element);
        for(int r=0;r<7;r++) for(int c=0;c<7;c++) if(GLYPHS[s][r].charAt(c)=='#')
            g.fill(x+c*pixel,y+r*pixel,x+(c+1)*pixel,y+(r+1)*pixel,color);
    }
    static void frame(GuiGraphics g,int x,int y,int w,int h,int col){
        g.fill(x,y,x+w,y+1,col);g.fill(x,y+h-1,x+w,y+h,col);
        g.fill(x,y,x+1,y+h,col);g.fill(x+w-1,y,x+w,y+h,col);
    }
    static void text(GuiGraphics g,Font f,String s,int x,int y,int col){g.drawString(f,s,x,y,col,false);}
    static String clip(Font f,String s,int width){return f.width(s)<=width?s:f.plainSubstrByWidth(s,Math.max(0,width-f.width("…")))+"…";}
    static String title(String s){
        s=s.replace('_',' ').trim();
        return s.isEmpty()?"":s.substring(0,1).toUpperCase(Locale.ROOT)+s.substring(1);
    }
    static boolean passive(BinderAbility a){return a.isPassive();}
}
