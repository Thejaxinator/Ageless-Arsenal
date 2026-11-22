package com.jax.destiny2mc.item;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class FallenSpawnEggItem extends Item {
   private final int mobKind;

   public FallenSpawnEggItem(int var1) {
      super(new Properties());
      this.mobKind = var1;
   }

   public InteractionResult useOn(UseOnContext var1) {
      Level var2 = var1.getLevel();
      if (var2.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockPos var3 = var1.getClickedPos().relative(var1.getClickedFace());
         int var4 = this.mobKind;
         EntityType var5;
         if (var4 == 0) {
            var5 = (EntityType)ModRegistries.FALLEN_DREG.get();
         } else if (var4 == 1) {
            var5 = (EntityType)ModRegistries.FALLEN_WRETCH.get();
         } else if (var4 == 2) {
            var5 = (EntityType)ModRegistries.FALLEN_VANDAL_SNIPER.get();
         } else if (var4 == 3) {
            var5 = (EntityType)ModRegistries.FALLEN_CAPTAIN_RIFLE.get();
         } else if (var4 == 4) {
            var5 = (EntityType)ModRegistries.FALLEN_CAPTAIN_SCORCH.get();
         } else if (var4 == 5) {
            var5 = (EntityType)ModRegistries.FALLEN_SHANK.get();
         } else if (var4 == 6) {
            var5 = (EntityType)ModRegistries.FALLEN_BRUISER.get();
         } else if (var4 == 7) {
            var5 = (EntityType)ModRegistries.DEVIL_WALKER.get();
         } else if (var4 == 8) {
            var5 = (EntityType)ModRegistries.SEPIKS_PRIME.get();
         } else if (var4 == 9) {
            var5 = (EntityType)ModRegistries.BLAST_SERVITOR.get();
         } else if (var4 == 10) {
            var5 = (EntityType)ModRegistries.VANOK_ARCLITE.get();
         } else if (var4 == 11) {
            var5 = (EntityType)ModRegistries.EXPLODER_SHANK.get();
         } else if (var4 == 12) {
            var5 = (EntityType)ModRegistries.TRACER_SHANK.get();
         } else if (var4 == 13) {
            var5 = (EntityType)ModRegistries.HEAVY_SHANK.get();
         } else if (var4 == 14) {
            var5 = (EntityType)ModRegistries.DEVILS_SERVITOR.get();
         } else if (var4 == 15) {
            var5 = (EntityType)ModRegistries.ARCHON_PRIEST.get();
         } else if (var4 == 16) {
            var5 = (EntityType)ModRegistries.HIVE_THRALL.get();
         } else if (var4 == 17) {
            var5 = (EntityType)ModRegistries.HIVE_CURSED_THRALL.get();
         } else if (var4 == 18) {
            var5 = (EntityType)ModRegistries.HIVE_ACOLYTE.get();
         } else if (var4 == 19) {
            var5 = (EntityType)ModRegistries.HIVE_KNIGHT.get();
         } else if (var4 == 20) {
            var5 = (EntityType)ModRegistries.HIVE_WIZARD.get();
         } else if (var4 == 21) {
            var5 = (EntityType)ModRegistries.HIVE_OGRE.get();
         } else if (var4 == 22) {
            var5 = (EntityType)ModRegistries.HIVE_SHRIEKER.get();
         } else if (var4 == 23) {
            var5 = (EntityType)ModRegistries.LUCENT_KNIGHT.get();
         } else if (var4 == 24) {
            var5 = (EntityType)ModRegistries.ORYX_BOSS.get();
         } else if (var4 == 25) {
            var5 = (EntityType)ModRegistries.VEX_GOBLIN.get();
         } else if (var4 == 26) {
            var5 = (EntityType)ModRegistries.VEX_HOBGOBLIN.get();
         } else if (var4 == 27) {
            var5 = (EntityType)ModRegistries.VEX_HARPY.get();
         } else if (var4 == 28) {
            var5 = (EntityType)ModRegistries.VEX_MINOTAUR.get();
         } else if (var4 == 29) {
            var5 = (EntityType)ModRegistries.VEX_HYDRA.get();
         } else if (var4 == 30) {
            var5 = (EntityType)ModRegistries.VEX_WYVERN.get();
         } else if (var4 == 31) {
            var5 = (EntityType)ModRegistries.PERSYS_BOSS.get();
         } else if (var4 == 32) {
            var5 = (EntityType)ModRegistries.ATHEON_BOSS.get();
         } else {
            var5 = (EntityType)ModRegistries.PROTHEON_BOSS.get();
         }

         Entity var6 = var5.create(var2);
         if (var6 != null) {
            var6.moveTo((double)var3.getX() + 0.5, (double)var3.getY() + 0.05, (double)var3.getZ() + 0.5, 0.0F, 0.0F);
            var2.addFreshEntity(var6);
            Player var7 = var1.getPlayer();
            if (var7 != null && !var7.isCreative()) {
               var1.getItemInHand().shrink(1);
            }
         }

         return InteractionResult.CONSUME;
      }
   }
}
