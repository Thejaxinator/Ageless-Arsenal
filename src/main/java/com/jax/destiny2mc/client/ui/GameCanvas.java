package com.jax.destiny2mc.client.ui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.function.IntFunction;
public final class GameCanvas implements UiCanvas {
 private final GuiGraphics g;private final Font font;private final IntFunction<ItemStack> items;
 public GameCanvas(GuiGraphics g,Font f,IntFunction<ItemStack> items){this.g=g;this.font=f;this.items=items;}
 public void fill(int x,int y,int w,int h,int c){g.fill(x,y,x+w,y+h,c);}
 public void text(int x,int y,String s,int c,float z){g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(z,z,1);g.drawString(font,s,0,0,c,false);g.pose().popPose();}
 public void image(String n,int x,int y,int w,int h){var r=ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/gui/director/"+n+".png");g.blit(r,x,y,0,0,w,h,w,h);}
 public void item(int id,int x,int y,int size){ItemStack s=items.apply(id);if(s==null||s.isEmpty())return;g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(size/16f,size/16f,1);g.renderItem(s,0,0);g.pose().popPose();}
}
