package com.jax.destiny2mc.client;
import com.jax.destiny2mc.client.ui.GunsmithScreen;
import net.minecraft.resources.ResourceLocation;
/** Retains the original interactable's class identity. */
public class GunsmithFuiScreen extends GunsmithScreen {
 public static final ResourceLocation BANSHEE_TEX=ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/gui/director/stars.png");
 public static int activeTab,selectedIdx,flashTicks;
 public GunsmithFuiScreen(){super();}
 public static void handleAction(int action){}
}
