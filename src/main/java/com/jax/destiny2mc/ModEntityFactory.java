package com.jax.destiny2mc;

import com.jax.destiny2mc.entity.ArchonPriestEntity;
import com.jax.destiny2mc.entity.AtheonBossEntity;
import com.jax.destiny2mc.entity.BlastServitorEntity;
import com.jax.destiny2mc.entity.DevilWalkerEntity;
import com.jax.destiny2mc.entity.DevilsServitorEntity;
import com.jax.destiny2mc.entity.ExploderShankEntity;
import com.jax.destiny2mc.entity.FallenBruiserEntity;
import com.jax.destiny2mc.entity.FallenCaptainRifleEntity;
import com.jax.destiny2mc.entity.FallenCaptainScorchEntity;
import com.jax.destiny2mc.entity.FallenDregEntity;
import com.jax.destiny2mc.entity.FallenShankEntity;
import com.jax.destiny2mc.entity.FallenVandalSniperEntity;
import com.jax.destiny2mc.entity.FallenWretchEntity;
import com.jax.destiny2mc.entity.HeavyShankEntity;
import com.jax.destiny2mc.entity.HiveAcolyteEntity;
import com.jax.destiny2mc.entity.HiveCursedThrallEntity;
import com.jax.destiny2mc.entity.HiveGhostEntity;
import com.jax.destiny2mc.entity.HiveKnightEntity;
import com.jax.destiny2mc.entity.HiveOgreEntity;
import com.jax.destiny2mc.entity.HiveShriekerEntity;
import com.jax.destiny2mc.entity.HiveSoulfireBoltEntity;
import com.jax.destiny2mc.entity.HiveThrallEntity;
import com.jax.destiny2mc.entity.HiveWizardEntity;
import com.jax.destiny2mc.entity.LucentKnightEntity;
import com.jax.destiny2mc.entity.LucentMothEntity;
import com.jax.destiny2mc.entity.OryxBossEntity;
import com.jax.destiny2mc.entity.PersysBossEntity;
import com.jax.destiny2mc.entity.ProtheonBossEntity;
import com.jax.destiny2mc.entity.ScorchRocketEntity;
import com.jax.destiny2mc.entity.SepiksPrimeEntity;
import com.jax.destiny2mc.entity.ShockBoltEntity;
import com.jax.destiny2mc.entity.ShockGrenadeEntity;
import com.jax.destiny2mc.entity.ShrapnelPelletEntity;
import com.jax.destiny2mc.entity.TracerShankEntity;
import com.jax.destiny2mc.entity.VanokArcliteEntity;
import com.jax.destiny2mc.entity.VexGoblinEntity;
import com.jax.destiny2mc.entity.VexHarpyEntity;
import com.jax.destiny2mc.entity.VexHobgoblinEntity;
import com.jax.destiny2mc.entity.VexHydraEntity;
import com.jax.destiny2mc.entity.VexMinotaurEntity;
import com.jax.destiny2mc.entity.VexOracleEntity;
import com.jax.destiny2mc.entity.VexSlapBoltEntity;
import com.jax.destiny2mc.entity.VexWyvernEntity;
import com.jax.destiny2mc.entity.VoidAxiomBoltEntity;
import com.jax.destiny2mc.entity.WireRifleBoltEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.level.Level;

public class ModEntityFactory implements EntityFactory {
   private final int kind;

   public ModEntityFactory(int var1) {
      this.kind = var1;
   }

   public Entity create(EntityType var1, Level var2) {
      int var3 = this.kind;
      if (var3 == 0) {
         return new FallenDregEntity(var1, var2);
      } else if (var3 == 1) {
         return new FallenWretchEntity(var1, var2);
      } else if (var3 == 2) {
         return new FallenVandalSniperEntity(var1, var2);
      } else if (var3 == 3) {
         return new FallenCaptainRifleEntity(var1, var2);
      } else if (var3 == 4) {
         return new FallenCaptainScorchEntity(var1, var2);
      } else if (var3 == 5) {
         return new FallenShankEntity(var1, var2);
      } else if (var3 == 6) {
         return new FallenBruiserEntity(var1, var2);
      } else if (var3 == 7) {
         return new DevilWalkerEntity(var1, var2);
      } else if (var3 == 8) {
         return new SepiksPrimeEntity(var1, var2);
      } else if (var3 == 9) {
         return new BlastServitorEntity(var1, var2);
      } else if (var3 == 15) {
         return new VanokArcliteEntity(var1, var2);
      } else if (var3 == 16) {
         return new ExploderShankEntity(var1, var2);
      } else if (var3 == 17) {
         return new TracerShankEntity(var1, var2);
      } else if (var3 == 18) {
         return new HeavyShankEntity(var1, var2);
      } else if (var3 == 19) {
         return new DevilsServitorEntity(var1, var2);
      } else if (var3 == 20) {
         return new ArchonPriestEntity(var1, var2);
      } else if (var3 == 21) {
         return new HiveThrallEntity(var1, var2);
      } else if (var3 == 22) {
         return new HiveCursedThrallEntity(var1, var2);
      } else if (var3 == 23) {
         return new HiveAcolyteEntity(var1, var2);
      } else if (var3 == 24) {
         return new HiveKnightEntity(var1, var2);
      } else if (var3 == 25) {
         return new HiveWizardEntity(var1, var2);
      } else if (var3 == 26) {
         return new HiveOgreEntity(var1, var2);
      } else if (var3 == 27) {
         return new HiveShriekerEntity(var1, var2);
      } else if (var3 == 28) {
         return new LucentKnightEntity(var1, var2);
      } else if (var3 == 29) {
         return new HiveGhostEntity(var1, var2);
      } else if (var3 == 30) {
         return new LucentMothEntity(var1, var2);
      } else if (var3 == 31) {
         return new OryxBossEntity(var1, var2);
      } else if (var3 == 32) {
         return new VexGoblinEntity(var1, var2);
      } else if (var3 == 33) {
         return new VexHobgoblinEntity(var1, var2);
      } else if (var3 == 34) {
         return new VexHarpyEntity(var1, var2);
      } else if (var3 == 35) {
         return new VexMinotaurEntity(var1, var2);
      } else if (var3 == 36) {
         return new VexHydraEntity(var1, var2);
      } else if (var3 == 37) {
         return new VexWyvernEntity(var1, var2);
      } else if (var3 == 38) {
         return new VexOracleEntity(var1, var2);
      } else if (var3 == 39) {
         return new PersysBossEntity(var1, var2);
      } else if (var3 == 40) {
         return new AtheonBossEntity(var1, var2);
      } else if (var3 == 41) {
         return new ProtheonBossEntity(var1, var2);
      } else if (var3 == 10) {
         return new ShockBoltEntity(var1, var2);
      } else if (var3 == 11) {
         return new ShockGrenadeEntity(var1, var2);
      } else if (var3 == 12) {
         return new WireRifleBoltEntity(var1, var2);
      } else if (var3 == 13) {
         return new ShrapnelPelletEntity(var1, var2);
      } else if (var3 == 14) {
         return new ScorchRocketEntity(var1, var2);
      } else if (var3 == 42) {
         return new HiveSoulfireBoltEntity(var1, var2);
      } else {
         return (Entity)(var3 == 43 ? new VexSlapBoltEntity(var1, var2) : new VoidAxiomBoltEntity(var1, var2));
      }
   }
}
