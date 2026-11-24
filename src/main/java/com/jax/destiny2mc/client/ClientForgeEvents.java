package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.gunfeel.SniperScopeOverlay;
import com.jax.destiny2mc.client.gunfeel.SpringDamper;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.client.TennoGunClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ClientForgeEvents {
   @SubscribeEvent
   public static void onInteractionKey(InteractionKeyMappingTriggered var0) {
      LocalPlayer var1 = Minecraft.getInstance().player;
      if (var1 != null) {
         ItemStack var2 = var1.getItemInHand(InteractionHand.MAIN_HAND);
         if (var2.getItem() instanceof DestinyGunItem) {
            if (var0.isAttack()) {
               var0.setSwingHand(false);
               var0.setCanceled(true);
               ClientWeaponState.onAttackTriggered();
               return;
            }

            if (var0.isUseItem()) {
               var0.setSwingHand(false);
               var0.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(Post var0) {
      ClientWeaponState.tickClient();
   }

   @SubscribeEvent
   public static void onComputeFov(ComputeFovModifierEvent var0) {
      // World & Hand FOV are decoupled in onViewportFov (TACZ-style tangent magnification).
   }

   @SubscribeEvent
   public static void onViewportFov(ViewportEvent.ComputeFov event) {
      var entity = event.getCamera().getEntity();
      if (!(entity instanceof Player player)) return;
      ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
      float partial = (float) event.getPartialTick();
      float aim;
      if (stack.getItem() instanceof DestinyGunItem) {
         aim = ClientWeaponState.getSmoothAds(partial);
      } else if (stack.getItem() instanceof TennoGunItem) {
         aim = TennoGunClient.getAimProgress();
      } else {
         return;
      }
      if (aim <= 0.001f) return;

      if (event.usedConfiguredFov()) {
         float mag = SniperScopeOverlay.getOpticalMagnification(stack);
         float fovCurve = SpringDamper.easeInOutCubic(aim);
         double targetZoom = 1.0 + (mag - 1.0) * fovCurve;
         event.setFOV(SpringDamper.magnificationToFov(targetZoom, event.getFOV()));
      } else {
         double baseHandFov = event.getFOV();
         double targetHandFov = SniperScopeOverlay.isScopedSniper(stack) ? 48.0 : 58.0;
         float modelCurve = SpringDamper.easeInOutCubic(SpringDamper.window(aim, 0.15f, 1.0f));
         event.setFOV(Mth.lerp(modelCurve, baseHandFov, Math.min(baseHandFov, targetHandFov)));
      }
   }

   @SubscribeEvent
   public static void onCameraAngles(ComputeCameraAngles var0) {
      float var1 = ClientWeaponState.lastPartialTick = (float)var0.getPartialTick();
      float var2 = ClientWeaponState.prevCamPitch + (ClientWeaponState.camPitch - ClientWeaponState.prevCamPitch) * var1;
      if (var2 > 0.005F) {
         var0.setPitch(var0.getPitch() - var2);
      }

      float var3 = ClientWeaponState.prevRecoilRoll + (ClientWeaponState.recoilRoll - ClientWeaponState.prevRecoilRoll) * var1;
      if (var3 > 0.005F || var3 < -0.005F) {
         var0.setRoll(var0.getRoll() + var3);
      }
   }

   @SubscribeEvent
   public static void onRenderHand(RenderHandEvent var0) {
      FirstPersonGunRenderer.onRenderHand(var0);
   }

   @SubscribeEvent
   public static void onRenderGuiPre(Pre var0) {
      if (var0.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
         Minecraft var1 = Minecraft.getInstance();
         if (!var1.options.hideGui && var1.options.getCameraType().isFirstPerson()) {
            LocalPlayer var2 = var1.player;
            if (var2 != null) {
               ItemStack var3 = var2.getItemInHand(InteractionHand.MAIN_HAND);
               float partial = var0.getPartialTick().getGameTimeDeltaPartialTick(false);
               if (var3.getItem() instanceof DestinyGunItem) {
                  if (!HudConfig.customCrosshair) {
                     GuiGraphics var4 = var0.getGuiGraphics();
                     int var5 = var4.guiWidth() / 2;
                     int var6 = var4.guiHeight() / 2;
                     if (SniperScopeOverlay.renderIfScoped(var4, var3, ClientWeaponState.getSmoothAds(partial))) {
                        var0.setCanceled(true);
                     }
                     DestinyHudRenderer.renderHitmarker(var4, var5, var6);
                     DestinyHudRenderer.renderFloatingNumbers(var4, var1, var5, var6);
                     return;
                  }

                  var0.setCanceled(true);
                  DestinyHudRenderer.renderCenterReticleAndScope(var0.getGuiGraphics(), partial);
               } else if (var3.getItem() instanceof TennoGunItem) {
                  if (SniperScopeOverlay.renderIfScoped(var0.getGuiGraphics(), var3, TennoGunClient.getAimProgress())) {
                     var0.setCanceled(true);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRenderGuiPost(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post var0) {
      if (var0.getName().equals(VanillaGuiLayers.HOTBAR) && !Minecraft.getInstance().options.hideGui) {
         DestinyHudRenderer.renderWeaponTelemetryHud(var0.getGuiGraphics(), var0.getPartialTick().getGameTimeDeltaPartialTick(false));
      }
   }
}
