package com.jax.destiny2mc.client.atlas;

import com.jax.destiny2mc.client.ui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Atlas-only typography. Does not replace Minecraft's global UI font. */
public final class AtlasCanvas implements UiCanvas {
 public static final ResourceLocation REGULAR=ResourceLocation.fromNamespaceAndPath("destiny2mc","atlas_regular");
 public static final ResourceLocation LIGHT=ResourceLocation.fromNamespaceAndPath("destiny2mc","atlas_light");
 public static final ResourceLocation SEMIBOLD=ResourceLocation.fromNamespaceAndPath("destiny2mc","atlas_semibold");
 private final GuiGraphics g;private final Font font;private final GameCanvas base;
 public AtlasCanvas(GuiGraphics g,Font font){this.g=g;this.font=font;base=new GameCanvas(g,font,i->ItemStack.EMPTY);}
 private static boolean compact(){return Minecraft.getInstance().getWindow().getWidth()<1100;}
 private static ResourceLocation readingFace(){return compact()?SEMIBOLD:REGULAR;}
 public static Component styled(String s){return Component.literal(s).withStyle(style->style.withFont(readingFace()));}
 public void fill(int x,int y,int w,int h,int colour){base.fill(x,y,w,h,colour);}
 public void image(String name,int x,int y,int w,int h){base.image(name,x,y,w,h);}
 public void item(int id,int x,int y,int size){}
 public void text(int x,int y,String s,int colour,float scale){draw(x,y,s,colour,scale,readingFace());}
 public void title(int x,int y,String s,int colour,float scale){draw(x,y,s,colour,scale,compact()?REGULAR:LIGHT);}
 public void strong(int x,int y,String s,int colour,float scale){draw(x,y,s,colour,scale,SEMIBOLD);}
 private void draw(int x,int y,String s,int colour,float scale,ResourceLocation face){
  g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);
  g.drawString(font,Component.literal(s).withStyle(style->style.withFont(face)),0,0,colour,false);g.pose().popPose();
 }
 public void label(int x,int y,String s,int colour,float scale,float spacing){
  g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);float at=0;
  for(int cp:s.codePoints().toArray()){String ch=new String(Character.toChars(cp));Component c=Component.literal(ch).withStyle(style->style.withFont(SEMIBOLD));g.pose().pushPose();g.pose().translate(at,0,0);g.drawString(font,c,0,0,colour,false);g.pose().popPose();at+=font.width(c)+spacing;}
  g.pose().popPose();
 }
}
