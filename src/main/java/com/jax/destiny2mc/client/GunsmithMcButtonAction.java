package com.jax.destiny2mc.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;

public class GunsmithMcButtonAction implements OnPress {
   private final int actionId;

   public GunsmithMcButtonAction(int var1) {
      this.actionId = var1;
   }

   public void onPress(Button var1) {
      GunsmithMcScreen.handleAction(this.actionId);
   }
}
