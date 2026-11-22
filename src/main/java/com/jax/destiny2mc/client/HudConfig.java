package com.jax.destiny2mc.client;

import java.util.Properties;
import net.minecraft.client.Minecraft;

public class HudConfig {
   public static int hudStyle;
   public static int scalePreset;
   public static int recoilPreset;
   public static boolean showIcon;
   public static boolean showPerks;
   public static boolean customCrosshair;
   public static boolean showDamageNums;
   /** The inventory toggle and in-world visibility are independent per-client preferences. */
   public static boolean showAbilities = true;
   public static boolean inventoryDrawerOpen = false;
   public static boolean loaded = false;

   public static void resetDefaults() {
      hudStyle = 0;
      scalePreset = 1;
      recoilPreset = 1;
      showIcon = true;
      showPerks = true;
      customCrosshair = true;
      showDamageNums = true;
   }

   static {
      resetDefaults();
   }

   public static float getScale() {
      int var0 = scalePreset;
      if (var0 == 0) {
         return 0.7F;
      } else if (var0 == 1) {
         return 0.82F;
      } else {
         return var0 == 2 ? 0.95F : 1.1F;
      }
   }

   public static float getRecoilMultiplier() {
      int var0 = recoilPreset;
      if (var0 == 0) {
         return 0.75F;
      } else if (var0 == 1) {
         return 1.0F;
      } else {
         return var0 == 2 ? 1.35F : 1.7F;
      }
   }

   public static String getStyleName() {
      int var0 = hudStyle;
      if (var0 == 0) {
         return "Destiny 2 FUI";
      } else if (var0 == 1) {
         return "Minecraft Compact";
      } else if (var0 == 2) {
         return "Simple Minimal";
      } else if (var0 == 3) {
         return "Borderlands 2";
      } else {
         return var0 == 4 ? "Borderlands 4" : "Warframe Tenno";
      }
   }

   public static String getScaleName() {
      int var0 = scalePreset;
      if (var0 == 0) {
         return "Small (70%)";
      } else if (var0 == 1) {
         return "Compact (82%)";
      } else {
         return var0 == 2 ? "Normal (95%)" : "Large (110%)";
      }
   }

   public static String getRecoilName() {
      int var0 = recoilPreset;
      if (var0 == 0) {
         return "Soft (0.75x)";
      } else if (var0 == 1) {
         return "Standard (1.0x)";
      } else {
         return var0 == 2 ? "Heavy (1.35x)" : "Brutal (1.7x)";
      }
   }

   private static int getPropInt(Properties var0, String var1, int var2, int var3) {
      String var4 = var0.getProperty(var1);
      if (var4 != null && var4.length() == 1) {
         int var5 = var4.charAt(0) - '0';
         if (var5 >= 0 && var5 <= var3) {
            return var5;
         }
      }

      return var2;
   }

   // Keep the existing filename so upgrades retain the player's HUD and input preferences.
   private static java.nio.file.Path preferences() {
      return Minecraft.getInstance().gameDirectory.toPath().resolve("config/destiny2mc_client.properties");
   }

   public static void ensureLoaded() {
      if (loaded) return;
      loaded = true;
      var path = preferences();
      if (!java.nio.file.Files.exists(path)) return;
      Properties properties = new Properties();
      try (var stream = java.nio.file.Files.newInputStream(path)) {
         properties.load(stream);
         hudStyle = getPropInt(properties, "hudStyle", 0, 5);
         scalePreset = getPropInt(properties, "scalePreset", 1, 3);
         recoilPreset = getPropInt(properties, "recoilPreset", 1, 3);
         showIcon = getPropInt(properties, "showIcon", 1, 1) != 0;
         showAbilities = getPropInt(properties, "showAbilities", 1, 1) != 0;
         inventoryDrawerOpen = getPropInt(properties, "inventoryDrawerOpen", 0, 1) != 0;
         showPerks = getPropInt(properties, "showPerks", 1, 1) != 0;
         customCrosshair = getPropInt(properties, "customCrosshair", 1, 1) != 0;
         showDamageNums = getPropInt(properties, "showDamageNums", 1, 1) != 0;
      } catch (java.io.IOException | IllegalArgumentException failure) {
         org.slf4j.LoggerFactory.getLogger("Ageless Arsenals").warn("Could not read HUD preferences", failure);
      }
   }

   public static void save() {
      Properties properties = new Properties();
      properties.setProperty("hudStyle", Integer.toString(hudStyle));
      properties.setProperty("scalePreset", Integer.toString(scalePreset));
      properties.setProperty("recoilPreset", Integer.toString(recoilPreset));
      properties.setProperty("showIcon", showIcon ? "1" : "0");
      properties.setProperty("showAbilities", showAbilities ? "1" : "0");
      properties.setProperty("inventoryDrawerOpen", inventoryDrawerOpen ? "1" : "0");
      properties.setProperty("showPerks", showPerks ? "1" : "0");
      properties.setProperty("customCrosshair", customCrosshair ? "1" : "0");
      properties.setProperty("showDamageNums", showDamageNums ? "1" : "0");
      var path = preferences();
      try {
         java.nio.file.Files.createDirectories(path.getParent());
         try (var stream = java.nio.file.Files.newOutputStream(path)) {
            properties.store(stream, "Ageless Arsenals HUD and recoil preferences");
         }
      } catch (java.io.IOException failure) {
         org.slf4j.LoggerFactory.getLogger("Ageless Arsenals").warn("Could not save HUD preferences", failure);
      }
   }
}
