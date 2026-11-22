package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.combat.CombatRenderer;
import com.jax.destiny2mc.client.model.ConvertedRig100;
import com.jax.destiny2mc.client.model.ConvertedRig101;
import com.jax.destiny2mc.client.model.ConvertedRig102;
import com.jax.destiny2mc.client.model.ConvertedRig103;
import com.jax.destiny2mc.client.model.ConvertedRig104;
import com.jax.destiny2mc.client.model.ConvertedRig105;
import com.jax.destiny2mc.client.model.ConvertedRig106;
import com.jax.destiny2mc.client.model.ConvertedRig107;
import com.jax.destiny2mc.client.model.ConvertedRig108;
import com.jax.destiny2mc.client.model.ConvertedRig109;
import com.jax.destiny2mc.client.model.ConvertedRig110;
import com.jax.destiny2mc.client.model.ConvertedRig111;
import com.jax.destiny2mc.client.model.ConvertedRig112;
import com.jax.destiny2mc.client.model.ConvertedRig113;
import com.jax.destiny2mc.client.model.ConvertedRig114;
import com.jax.destiny2mc.client.model.ConvertedRig115;
import com.jax.destiny2mc.client.model.ConvertedRig116;
import com.jax.destiny2mc.client.model.ConvertedRig117;
import com.jax.destiny2mc.client.model.ConvertedRig118;
import com.jax.destiny2mc.client.model.ConvertedRig119;
import com.jax.destiny2mc.client.model.ConvertedRig120;
import com.jax.destiny2mc.client.model.ConvertedRig121;
import com.jax.destiny2mc.client.model.ConvertedRig122;
import com.jax.destiny2mc.client.model.ConvertedRig123;
import com.jax.destiny2mc.client.model.ConvertedRig124;
import com.jax.destiny2mc.client.model.ConvertedRig125;
import com.jax.destiny2mc.client.model.ConvertedRig126;
import com.jax.destiny2mc.client.model.ConvertedRig127;
import com.jax.destiny2mc.client.model.ConvertedRig128;
import com.jax.destiny2mc.client.model.ConvertedRig129;
import com.jax.destiny2mc.client.model.ConvertedRig130;
import com.jax.destiny2mc.client.model.ConvertedRig131;
import com.jax.destiny2mc.client.model.ConvertedRig132;
import com.jax.destiny2mc.client.model.ConvertedRig133;
import com.jax.destiny2mc.client.model.ConvertedRig134;
import com.jax.destiny2mc.client.model.ConvertedRig135;
import com.jax.destiny2mc.client.model.ConvertedRig136;
import com.jax.destiny2mc.client.model.ConvertedRig137;
import com.jax.destiny2mc.client.model.ConvertedRig138;
import com.jax.destiny2mc.client.model.ConvertedRig139;
import com.jax.destiny2mc.client.model.ConvertedRig140;
import com.jax.destiny2mc.client.model.ConvertedRig141;
import com.jax.destiny2mc.client.model.ConvertedRig142;
import com.jax.destiny2mc.client.model.ConvertedRig21;
import com.jax.destiny2mc.client.model.ConvertedRig22;
import com.jax.destiny2mc.client.model.ConvertedRig23;
import com.jax.destiny2mc.client.model.ConvertedRig24;
import com.jax.destiny2mc.client.model.ConvertedRig25;
import com.jax.destiny2mc.client.model.ConvertedRig26;
import com.jax.destiny2mc.client.model.ConvertedRig27;
import com.jax.destiny2mc.client.model.ConvertedRig28;
import com.jax.destiny2mc.client.model.ConvertedRig29;
import com.jax.destiny2mc.client.model.ConvertedRig30;
import com.jax.destiny2mc.client.model.ConvertedRig31;
import com.jax.destiny2mc.client.model.ConvertedRig32;
import com.jax.destiny2mc.client.model.ConvertedRig33;
import com.jax.destiny2mc.client.model.ConvertedRig34;
import com.jax.destiny2mc.client.model.ConvertedRig35;
import com.jax.destiny2mc.client.model.ConvertedRig36;
import com.jax.destiny2mc.client.model.ConvertedRig37;
import com.jax.destiny2mc.client.model.ConvertedRig38;
import com.jax.destiny2mc.client.model.ConvertedRig39;
import com.jax.destiny2mc.client.model.ConvertedRig40;
import com.jax.destiny2mc.client.model.ConvertedRig41;
import com.jax.destiny2mc.client.model.ConvertedRig45;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class V200FactionRendererProvider implements EntityRendererProvider {
   private final int kind;

   public V200FactionRendererProvider(int var1) {
      this.kind = var1;
   }

   public EntityRenderer create(Context var1) {
      if (this.kind == 21) {
         return new CombatRenderer(var1, ConvertedRig21.create().bakeRoot(), 21, "hive_thrall");
      } else if (this.kind == 22) {
         return new CombatRenderer(var1, ConvertedRig22.create().bakeRoot(), 22, "hive_cursed_thrall");
      } else if (this.kind == 23) {
         return new CombatRenderer(var1, ConvertedRig23.create().bakeRoot(), 23, "hive_acolyte");
      } else if (this.kind == 24) {
         return new CombatRenderer(var1, ConvertedRig24.create().bakeRoot(), 24, "hive_knight");
      } else if (this.kind == 25) {
         return new CombatRenderer(var1, ConvertedRig25.create().bakeRoot(), 25, "hive_wizard");
      } else if (this.kind == 26) {
         return new CombatRenderer(var1, ConvertedRig26.create().bakeRoot(), 26, "hive_ogre");
      } else if (this.kind == 27) {
         return new CombatRenderer(var1, ConvertedRig27.create().bakeRoot(), 27, "hive_shrieker");
      } else if (this.kind == 28) {
         return new CombatRenderer(var1, ConvertedRig28.create().bakeRoot(), 28, "lucent_knight");
      } else if (this.kind == 29) {
         return new CombatRenderer(var1, ConvertedRig29.create().bakeRoot(), 29, "hive_ghost");
      } else if (this.kind == 30) {
         return new CombatRenderer(var1, ConvertedRig30.create().bakeRoot(), 30, "lucent_moth");
      } else if (this.kind == 31) {
         return new CombatRenderer(var1, ConvertedRig31.create().bakeRoot(), 31, "oryx_boss");
      } else if (this.kind == 32) {
         return new CombatRenderer(var1, ConvertedRig32.create().bakeRoot(), 32, "vex_goblin");
      } else if (this.kind == 33) {
         return new CombatRenderer(var1, ConvertedRig33.create().bakeRoot(), 33, "vex_hobgoblin");
      } else if (this.kind == 34) {
         return new CombatRenderer(var1, ConvertedRig34.create().bakeRoot(), 34, "vex_harpy");
      } else if (this.kind == 35) {
         return new CombatRenderer(var1, ConvertedRig35.create().bakeRoot(), 35, "vex_minotaur");
      } else if (this.kind == 36) {
         return new CombatRenderer(var1, ConvertedRig36.create().bakeRoot(), 36, "vex_hydra");
      } else if (this.kind == 37) {
         return new CombatRenderer(var1, ConvertedRig37.create().bakeRoot(), 37, "vex_wyvern");
      } else if (this.kind == 38) {
         return new CombatRenderer(var1, ConvertedRig38.create().bakeRoot(), 38, "vex_oracle");
      } else if (this.kind == 39) {
         return new CombatRenderer(var1, ConvertedRig39.create().bakeRoot(), 39, "persys_boss");
      } else if (this.kind == 40) {
         return new CombatRenderer(var1, ConvertedRig40.create().bakeRoot(), 40, "atheon_boss");
      } else if (this.kind == 41) {
         return new CombatRenderer(var1, ConvertedRig41.create().bakeRoot(), 41, "protheon_boss");
      } else if (this.kind == 45) {
         return new CombatRenderer(var1, ConvertedRig45.create().bakeRoot(), 45, "vex_sphinx");
      } else if (this.kind == 142) {
         return new CombatRenderer(var1, ConvertedRig142.create().bakeRoot(), 142, "akelous");
      } else if (this.kind == 131) {
         return new CombatRenderer(var1, ConvertedRig131.create().bakeRoot(), 131, "alak_hul");
      } else if (this.kind == 130) {
         return new CombatRenderer(var1, ConvertedRig130.create().bakeRoot(), 130, "bracus_zahn");
      } else if (this.kind == 103) {
         return new CombatRenderer(var1, ConvertedRig103.create().bakeRoot(), 103, "cabal_centurion");
      } else if (this.kind == 104) {
         return new CombatRenderer(var1, ConvertedRig104.create().bakeRoot(), 104, "cabal_colossus");
      } else if (this.kind == 106) {
         return new CombatRenderer(var1, ConvertedRig106.create().bakeRoot(), 106, "cabal_incendior");
      } else if (this.kind == 100) {
         return new CombatRenderer(var1, ConvertedRig100.create().bakeRoot(), 100, "cabal_legionary");
      } else if (this.kind == 101) {
         return new CombatRenderer(var1, ConvertedRig101.create().bakeRoot(), 101, "cabal_phalanx");
      } else if (this.kind == 102) {
         return new CombatRenderer(var1, ConvertedRig102.create().bakeRoot(), 102, "cabal_psion");
      } else if (this.kind == 105) {
         return new CombatRenderer(var1, ConvertedRig105.create().bakeRoot(), 105, "cabal_warbeast");
      } else if (this.kind == 134) {
         return new CombatRenderer(var1, ConvertedRig134.create().bakeRoot(), 134, "crota");
      } else if (this.kind == 122) {
         return new CombatRenderer(var1, ConvertedRig122.create().bakeRoot(), 122, "dread_attendant");
      } else if (this.kind == 127) {
         return new CombatRenderer(var1, ConvertedRig127.create().bakeRoot(), 127, "dread_geist");
      } else if (this.kind == 120) {
         return new CombatRenderer(var1, ConvertedRig120.create().bakeRoot(), 120, "dread_grim");
      } else if (this.kind == 125) {
         return new CombatRenderer(var1, ConvertedRig125.create().bakeRoot(), 125, "dread_harbinger");
      } else if (this.kind == 121) {
         return new CombatRenderer(var1, ConvertedRig121.create().bakeRoot(), 121, "dread_husk");
      } else if (this.kind == 124) {
         return new CombatRenderer(var1, ConvertedRig124.create().bakeRoot(), 124, "dread_omen");
      } else if (this.kind == 126) {
         return new CombatRenderer(var1, ConvertedRig126.create().bakeRoot(), 126, "dread_tormentor");
      } else if (this.kind == 123) {
         return new CombatRenderer(var1, ConvertedRig123.create().bakeRoot(), 123, "dread_weaver");
      } else if (this.kind == 133) {
         return new CombatRenderer(var1, ConvertedRig133.create().bakeRoot(), 133, "fikrul");
      } else if (this.kind == 136) {
         return new CombatRenderer(var1, ConvertedRig136.create().bakeRoot(), 136, "golgoroth");
      } else if (this.kind == 132) {
         return new CombatRenderer(var1, ConvertedRig132.create().bakeRoot(), 132, "hashladun");
      } else if (this.kind == 138) {
         return new CombatRenderer(var1, ConvertedRig138.create().bakeRoot(), 138, "nezarec");
      } else if (this.kind == 141) {
         return new CombatRenderer(var1, ConvertedRig141.create().bakeRoot(), 141, "phryzhia");
      } else if (this.kind == 137) {
         return new CombatRenderer(var1, ConvertedRig137.create().bakeRoot(), 137, "rhulk");
      } else if (this.kind == 119) {
         return new CombatRenderer(var1, ConvertedRig119.create().bakeRoot(), 119, "scorn_abomination");
      } else if (this.kind == 117) {
         return new CombatRenderer(var1, ConvertedRig117.create().bakeRoot(), 117, "scorn_chieftain");
      } else if (this.kind == 115) {
         return new CombatRenderer(var1, ConvertedRig115.create().bakeRoot(), 115, "scorn_lurker");
      } else if (this.kind == 114) {
         return new CombatRenderer(var1, ConvertedRig114.create().bakeRoot(), 114, "scorn_raider");
      } else if (this.kind == 116) {
         return new CombatRenderer(var1, ConvertedRig116.create().bakeRoot(), 116, "scorn_ravager");
      } else if (this.kind == 118) {
         return new CombatRenderer(var1, ConvertedRig118.create().bakeRoot(), 118, "scorn_screeb");
      } else if (this.kind == 113) {
         return new CombatRenderer(var1, ConvertedRig113.create().bakeRoot(), 113, "scorn_stalker");
      } else if (this.kind == 108) {
         return new CombatRenderer(var1, ConvertedRig108.create().bakeRoot(), 108, "taken_acolyte");
      } else if (this.kind == 110) {
         return new CombatRenderer(var1, ConvertedRig110.create().bakeRoot(), 110, "taken_captain");
      } else if (this.kind == 109) {
         return new CombatRenderer(var1, ConvertedRig109.create().bakeRoot(), 109, "taken_goblin");
      } else if (this.kind == 111) {
         return new CombatRenderer(var1, ConvertedRig111.create().bakeRoot(), 111, "taken_knight");
      } else if (this.kind == 112) {
         return new CombatRenderer(var1, ConvertedRig112.create().bakeRoot(), 112, "taken_phalanx");
      } else if (this.kind == 107) {
         return new CombatRenderer(var1, ConvertedRig107.create().bakeRoot(), 107, "taken_thrall");
      } else if (this.kind == 129) {
         return new CombatRenderer(var1, ConvertedRig129.create().bakeRoot(), 129, "taniks");
      } else if (this.kind == 135) {
         return new CombatRenderer(var1, ConvertedRig135.create().bakeRoot(), 135, "templar");
      } else if (this.kind == 128) {
         return new CombatRenderer(var1, ConvertedRig128.create().bakeRoot(), 128, "valus_ta_aurc");
      } else if (this.kind == 139) {
         return new CombatRenderer(var1, ConvertedRig139.create().bakeRoot(), 139, "vorgeth");
      } else {
         return (EntityRenderer)(this.kind == 140
            ? new CombatRenderer(var1, ConvertedRig140.create().bakeRoot(), 140, "zulmak")
            : new V200LegacyFactionRendererProvider(this.kind).create(var1));
      }
   }
}
