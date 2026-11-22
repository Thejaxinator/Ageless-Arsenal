/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.CreativeModeTab$DisplayItemsGenerator
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.level.ItemLike
 */
package com.jax.destiny2mc;

import com.jax.destiny2mc.ModRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;

public class TabDisplayGenerator
implements CreativeModeTab.DisplayItemsGenerator {
    public void accept(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        for (int i = 0; i < ModRegistries.GUN_HOLDERS.length; ++i) {
            output.accept((ItemLike)ModRegistries.GUN_HOLDERS[i].get());
        }
        output.accept((ItemLike)ModRegistries.VANGUARD_COMMUNICATOR.get());
        output.accept((ItemLike)ModRegistries.SPECIAL_AMMO.get());
        output.accept((ItemLike)ModRegistries.EXOTIC_ENGRAM.get());
        output.accept((ItemLike)ModRegistries.PLUNDERED_ETHER_ALLOY.get());
        output.accept((ItemLike)ModRegistries.SCORCH_ETHER_CORE.get());
        output.accept((ItemLike)ModRegistries.ECLIPSE_MONOLITH_CIPHER.get());
        output.accept((ItemLike)ModRegistries.HIVE_CHITIN_FRAGMENT.get());
        output.accept((ItemLike)ModRegistries.VEX_RADIOLARIAN_CORE.get());
        output.accept((ItemLike)ModRegistries.GUNSMITH_FUI_ITEM.get());
        output.accept((ItemLike)ModRegistries.GUNSMITH_MC_ITEM.get());
        output.accept((ItemLike)ModRegistries.COSMODROME_CHEST_ITEM.get());
        output.accept((ItemLike)ModRegistries.FALLEN_CHEST_ITEM.get());
        output.accept((ItemLike)ModRegistries.AWOKEN_CHEST_ITEM.get());
        output.accept((ItemLike)ModRegistries.LEVIATHAN_CHEST_ITEM.get());
        output.accept((ItemLike)ModRegistries.DREG_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.WRETCH_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VANDAL_SNIPER_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.CAPTAIN_RIFLE_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.CAPTAIN_SCORCH_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.SHANK_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.BRUISER_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.DEVIL_WALKER_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.SEPIKS_PRIME_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.BLAST_SERVITOR_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VANOK_ARCLITE_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.EXPLODER_SHANK_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.TRACER_SHANK_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HEAVY_SHANK_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.DEVILS_SERVITOR_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.ARCHON_PRIEST_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_THRALL_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_CURSED_THRALL_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_ACOLYTE_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_KNIGHT_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_WIZARD_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_OGRE_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.HIVE_SHRIEKER_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.LUCENT_KNIGHT_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.ORYX_BOSS_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VEX_GOBLIN_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VEX_HOBGOBLIN_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VEX_HARPY_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VEX_MINOTAUR_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VEX_HYDRA_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.VEX_WYVERN_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.PERSYS_BOSS_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.ATHEON_BOSS_SPAWN_EGG.get());
        output.accept((ItemLike)ModRegistries.PROTHEON_BOSS_SPAWN_EGG.get());
    }
}
