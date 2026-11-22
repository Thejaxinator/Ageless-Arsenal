package com.jax.destiny2mc;

import com.jax.destiny2mc.block.DestinyLootChestBlock;
import com.jax.destiny2mc.block.GunsmithBlock;
import com.jax.destiny2mc.block.StrikeBarrierBlock;
import com.jax.destiny2mc.item.ExoticEngramItem;
import com.jax.destiny2mc.item.FallenSpawnEggItem;
import com.jax.destiny2mc.item.SpecialAmmoItem;
import com.jax.destiny2mc.item.VanguardCommunicatorItem;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;

public class SimpleItemSupplier implements Supplier {
   private final int kind;

   public SimpleItemSupplier(int var1) {
      this.kind = var1;
   }

   @Override
   public Object get() {
      int var1 = this.kind;
      if (var1 == 0) {
         return new SpecialAmmoItem();
      } else if (var1 == 1) {
         return new ExoticEngramItem();
      } else if (var1 == 3) {
         return new VanguardCommunicatorItem();
      } else if (var1 == 2) {
         return CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.destiny2mc"))
            .icon(new TabIconSupplier())
            .displayItems(new TabDisplayGenerator())
            .build();
      } else if (var1 == 10) {
         return new GunsmithBlock(true);
      } else if (var1 == 11) {
         return new GunsmithBlock(false);
      } else if (var1 == 16) {
         return new StrikeBarrierBlock();
      } else if (var1 >= 12 && var1 <= 15) {
         return new DestinyLootChestBlock(var1 - 12);
      } else if (var1 == 20) {
         return new BlockItem((Block)ModRegistries.GUNSMITH_FUI_BLOCK.get(), new Properties());
      } else if (var1 == 21) {
         return new BlockItem((Block)ModRegistries.GUNSMITH_MC_BLOCK.get(), new Properties());
      } else if (var1 == 22) {
         return new BlockItem((Block)ModRegistries.COSMODROME_CHEST_BLOCK.get(), new Properties());
      } else if (var1 == 23) {
         return new BlockItem((Block)ModRegistries.FALLEN_CHEST_BLOCK.get(), new Properties());
      } else if (var1 == 24) {
         return new BlockItem((Block)ModRegistries.AWOKEN_CHEST_BLOCK.get(), new Properties());
      } else if (var1 == 25) {
         return new BlockItem((Block)ModRegistries.LEVIATHAN_CHEST_BLOCK.get(), new Properties());
      } else if (var1 == 26) {
         return new BlockItem((Block)ModRegistries.STRIKE_BARRIER_BLOCK.get(), new Properties());
      } else if (var1 >= 30 && var1 <= 36) {
         return new FallenSpawnEggItem(var1 - 30);
      } else if (var1 >= 90 && var1 <= 93) {
         return new FallenSpawnEggItem(var1 - 83);
      } else if (var1 >= 120 && var1 <= 142) {
         return new FallenSpawnEggItem(var1 - 109);
      } else if (var1 >= 40 && var1 <= 59) {
         return new Item(new Properties());
      } else if (var1 == 60) {
         return Builder.of(
               new LootChestBeSupplier(),
               new Block[]{
                  (Block)ModRegistries.COSMODROME_CHEST_BLOCK.get(),
                  (Block)ModRegistries.FALLEN_CHEST_BLOCK.get(),
                  (Block)ModRegistries.AWOKEN_CHEST_BLOCK.get(),
                  (Block)ModRegistries.LEVIATHAN_CHEST_BLOCK.get()
               }
            )
            .build(null);
      } else if (var1 == 200) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(0), MobCategory.MONSTER)
            .sized(0.65F, 1.88F)
            .clientTrackingRange(10)
            .build("fallen_dreg");
      } else if (var1 == 201) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(1), MobCategory.MONSTER)
            .sized(0.65F, 1.88F)
            .clientTrackingRange(10)
            .build("fallen_wretch");
      } else if (var1 == 202) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(2), MobCategory.MONSTER)
            .sized(0.7F, 2.02F)
            .clientTrackingRange(10)
            .build("fallen_vandal_sniper");
      } else if (var1 == 203) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(3), MobCategory.MONSTER)
            .sized(0.85F, 2.35F)
            .clientTrackingRange(10)
            .build("fallen_captain_rifle");
      } else if (var1 == 204) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(4), MobCategory.MONSTER)
            .sized(0.88F, 2.38F)
            .clientTrackingRange(10)
            .build("fallen_captain_scorch");
      } else if (var1 == 205) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(5), MobCategory.MONSTER)
            .sized(0.75F, 0.95F)
            .clientTrackingRange(10)
            .build("fallen_shank");
      } else if (var1 == 206) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(6), MobCategory.MONSTER)
            .sized(0.85F, 2.22F)
            .clientTrackingRange(10)
            .build("fallen_bruiser");
      } else if (var1 == 207) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(7), MobCategory.MONSTER)
            .sized(4.2F, 3.2F)
            .clientTrackingRange(10)
            .build("devil_walker");
      } else if (var1 == 208) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(8), MobCategory.MONSTER)
            .sized(2.5F, 2.5F)
            .clientTrackingRange(10)
            .build("sepiks_prime");
      } else if (var1 == 209) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(9), MobCategory.MONSTER)
            .sized(1.45F, 1.45F)
            .clientTrackingRange(10)
            .build("blast_servitor");
      } else if (var1 == 215) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(15), MobCategory.MONSTER)
            .sized(1.05F, 2.65F)
            .clientTrackingRange(10)
            .build("vanok_arclite");
      } else if (var1 == 216) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(16), MobCategory.MONSTER)
            .sized(0.75F, 0.95F)
            .clientTrackingRange(10)
            .build("exploder_shank");
      } else if (var1 == 217) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(17), MobCategory.MONSTER)
            .sized(0.75F, 0.95F)
            .clientTrackingRange(10)
            .build("tracer_shank");
      } else if (var1 == 218) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(18), MobCategory.MONSTER)
            .sized(1.25F, 1.45F)
            .clientTrackingRange(10)
            .build("heavy_shank");
      } else if (var1 == 219) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(19), MobCategory.MONSTER)
            .sized(1.35F, 1.35F)
            .clientTrackingRange(10)
            .build("devils_servitor");
      } else if (var1 == 220) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(20), MobCategory.MONSTER)
            .sized(1.05F, 2.75F)
            .clientTrackingRange(10)
            .build("archon_priest");
      } else if (var1 == 221) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(21), MobCategory.MONSTER)
            .sized(0.65F, 1.85F)
            .clientTrackingRange(10)
            .build("hive_thrall");
      } else if (var1 == 222) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(22), MobCategory.MONSTER)
            .sized(0.68F, 1.85F)
            .clientTrackingRange(10)
            .build("hive_cursed_thrall");
      } else if (var1 == 223) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(23), MobCategory.MONSTER)
            .sized(0.72F, 1.98F)
            .clientTrackingRange(10)
            .build("hive_acolyte");
      } else if (var1 == 224) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(24), MobCategory.MONSTER)
            .sized(0.9F, 2.35F)
            .clientTrackingRange(10)
            .build("hive_knight");
      } else if (var1 == 225) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(25), MobCategory.MONSTER)
            .sized(0.8F, 2.2F)
            .clientTrackingRange(10)
            .build("hive_wizard");
      } else if (var1 == 226) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(26), MobCategory.MONSTER)
            .sized(1.65F, 3.1F)
            .clientTrackingRange(10)
            .build("hive_ogre");
      } else if (var1 == 227) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(27), MobCategory.MONSTER)
            .sized(1.15F, 1.35F)
            .clientTrackingRange(10)
            .build("hive_shrieker");
      } else if (var1 == 228) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(28), MobCategory.MONSTER)
            .sized(0.95F, 2.48F)
            .clientTrackingRange(10)
            .build("lucent_knight");
      } else if (var1 == 229) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(29), MobCategory.MONSTER)
            .sized(0.55F, 0.65F)
            .clientTrackingRange(10)
            .build("hive_ghost");
      } else if (var1 == 230) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(30), MobCategory.MONSTER)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(10)
            .build("lucent_moth");
      } else if (var1 == 231) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(31), MobCategory.MONSTER)
            .sized(1.45F, 3.5F)
            .clientTrackingRange(10)
            .build("oryx_boss");
      } else if (var1 == 232) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(32), MobCategory.MONSTER)
            .sized(0.72F, 1.95F)
            .clientTrackingRange(10)
            .build("vex_goblin");
      } else if (var1 == 233) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(33), MobCategory.MONSTER)
            .sized(0.72F, 2.02F)
            .clientTrackingRange(10)
            .build("vex_hobgoblin");
      } else if (var1 == 234) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(34), MobCategory.MONSTER)
            .sized(0.85F, 1.1F)
            .clientTrackingRange(10)
            .build("vex_harpy");
      } else if (var1 == 235) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(35), MobCategory.MONSTER)
            .sized(0.95F, 2.45F)
            .clientTrackingRange(10)
            .build("vex_minotaur");
      } else if (var1 == 236) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(36), MobCategory.MONSTER)
            .sized(1.5F, 2.4F)
            .clientTrackingRange(10)
            .build("vex_hydra");
      } else if (var1 == 237) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(37), MobCategory.MONSTER)
            .sized(1.15F, 2.55F)
            .clientTrackingRange(10)
            .build("vex_wyvern");
      } else if (var1 == 238) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(38), MobCategory.MONSTER)
            .sized(0.95F, 0.95F)
            .clientTrackingRange(10)
            .build("vex_oracle");
      } else if (var1 == 239) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(39), MobCategory.MONSTER)
            .sized(1.65F, 3.25F)
            .clientTrackingRange(10)
            .build("persys_boss");
      } else if (var1 == 240) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(40), MobCategory.MONSTER)
            .sized(1.55F, 3.55F)
            .clientTrackingRange(10)
            .build("atheon_boss");
      } else if (var1 == 241) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(41), MobCategory.MONSTER)
            .sized(1.35F, 3.15F)
            .clientTrackingRange(10)
            .build("protheon_boss");
      } else if (var1 == 310) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(10), MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("shock_bolt");
      } else if (var1 == 311) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(11), MobCategory.MISC)
            .sized(0.3F, 0.3F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("shock_grenade");
      } else if (var1 == 312) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(12), MobCategory.MISC)
            .sized(0.22F, 0.22F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("wire_rifle_bolt");
      } else if (var1 == 313) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(13), MobCategory.MISC)
            .sized(0.24F, 0.24F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("shrapnel_pellet");
      } else if (var1 == 314) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(14), MobCategory.MISC)
            .sized(0.38F, 0.38F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("scorch_rocket");
      } else if (var1 == 342) {
         return net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(42), MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("hive_soulfire_bolt");
      } else {
         return var1 == 343
            ? net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(43), MobCategory.MISC)
               .sized(0.25F, 0.25F)
               .clientTrackingRange(8)
               .updateInterval(2)
               .build("vex_slap_bolt")
            : net.minecraft.world.entity.EntityType.Builder.of(new ModEntityFactory(44), MobCategory.MISC)
               .sized(0.32F, 0.32F)
               .clientTrackingRange(8)
               .updateInterval(2)
               .build("void_axiom_bolt");
      }
   }
}
