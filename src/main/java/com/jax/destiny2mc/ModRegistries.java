/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.jax.destiny2mc;

import com.jax.destiny2mc.GunItemSupplier;
import com.jax.destiny2mc.SimpleItemSupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {
    public static final DeferredRegister BLOCKS = DeferredRegister.create((ResourceKey)Registries.BLOCK, (String)"destiny2mc");
    public static final DeferredRegister ITEMS = DeferredRegister.create((ResourceKey)Registries.ITEM, (String)"destiny2mc");
    public static final DeferredRegister BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"destiny2mc");
    public static final DeferredRegister ENTITIES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"destiny2mc");
    public static final DeferredRegister TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"destiny2mc");
    public static final DeferredHolder[] GUN_HOLDERS = new DeferredHolder[com.jax.destiny2mc.weapon.WeaponSpec.ALL.length];
    public static final DeferredHolder SPECIAL_AMMO;
    public static final DeferredHolder EXOTIC_ENGRAM;
    public static final DeferredHolder VANGUARD_COMMUNICATOR;
    public static final DeferredHolder GUNSMITH_FUI_BLOCK;
    public static final DeferredHolder GUNSMITH_MC_BLOCK;
    public static final DeferredHolder COSMODROME_CHEST_BLOCK;
    public static final DeferredHolder FALLEN_CHEST_BLOCK;
    public static final DeferredHolder AWOKEN_CHEST_BLOCK;
    public static final DeferredHolder LEVIATHAN_CHEST_BLOCK;
    public static final DeferredHolder STRIKE_BARRIER_BLOCK;
    public static final DeferredHolder GUNSMITH_FUI_ITEM;
    public static final DeferredHolder GUNSMITH_MC_ITEM;
    public static final DeferredHolder COSMODROME_CHEST_ITEM;
    public static final DeferredHolder FALLEN_CHEST_ITEM;
    public static final DeferredHolder AWOKEN_CHEST_ITEM;
    public static final DeferredHolder LEVIATHAN_CHEST_ITEM;
    public static final DeferredHolder STRIKE_BARRIER_ITEM;
    public static final DeferredHolder DREG_SPAWN_EGG;
    public static final DeferredHolder WRETCH_SPAWN_EGG;
    public static final DeferredHolder VANDAL_SNIPER_SPAWN_EGG;
    public static final DeferredHolder CAPTAIN_RIFLE_SPAWN_EGG;
    public static final DeferredHolder CAPTAIN_SCORCH_SPAWN_EGG;
    public static final DeferredHolder SHANK_SPAWN_EGG;
    public static final DeferredHolder BRUISER_SPAWN_EGG;
    public static final DeferredHolder DEVIL_WALKER_SPAWN_EGG;
    public static final DeferredHolder SEPIKS_PRIME_SPAWN_EGG;
    public static final DeferredHolder BLAST_SERVITOR_SPAWN_EGG;
    public static final DeferredHolder VANOK_ARCLITE_SPAWN_EGG;
    public static final DeferredHolder PLUNDERED_ETHER_ALLOY;
    public static final DeferredHolder SCORCH_ETHER_CORE;
    public static final DeferredHolder ECLIPSE_MONOLITH_CIPHER;
    public static final DeferredHolder SHOCK_BOLT_ITEM;
    public static final DeferredHolder SHOCK_GRENADE_ITEM;
    public static final DeferredHolder WIRE_BOLT_ITEM;
    public static final DeferredHolder SHRAPNEL_PELLET_ITEM;
    public static final DeferredHolder SCORCH_ROCKET_ITEM;
    public static final DeferredHolder COSMODROME_CHEST_LID_ITEM;
    public static final DeferredHolder FALLEN_CHEST_LID_ITEM;
    public static final DeferredHolder AWOKEN_CHEST_LID_ITEM;
    public static final DeferredHolder LEVIATHAN_CHEST_LID_ITEM;
    public static final DeferredHolder HIVE_CHITIN_FRAGMENT;
    public static final DeferredHolder VEX_RADIOLARIAN_CORE;
    public static final DeferredHolder HIVE_SOULFIRE_BOLT_ITEM;
    public static final DeferredHolder VEX_SLAP_BOLT_ITEM;
    public static final DeferredHolder VOID_AXIOM_BOLT_ITEM;
    public static final DeferredHolder LOOT_CHEST_BE;
    public static final DeferredHolder ARSENAL_TAB;
    public static final DeferredHolder EXPLODER_SHANK_SPAWN_EGG;
    public static final DeferredHolder TRACER_SHANK_SPAWN_EGG;
    public static final DeferredHolder HEAVY_SHANK_SPAWN_EGG;
    public static final DeferredHolder DEVILS_SERVITOR_SPAWN_EGG;
    public static final DeferredHolder ARCHON_PRIEST_SPAWN_EGG;
    public static final DeferredHolder HIVE_THRALL_SPAWN_EGG;
    public static final DeferredHolder HIVE_CURSED_THRALL_SPAWN_EGG;
    public static final DeferredHolder HIVE_ACOLYTE_SPAWN_EGG;
    public static final DeferredHolder HIVE_KNIGHT_SPAWN_EGG;
    public static final DeferredHolder HIVE_WIZARD_SPAWN_EGG;
    public static final DeferredHolder HIVE_OGRE_SPAWN_EGG;
    public static final DeferredHolder HIVE_SHRIEKER_SPAWN_EGG;
    public static final DeferredHolder LUCENT_KNIGHT_SPAWN_EGG;
    public static final DeferredHolder ORYX_BOSS_SPAWN_EGG;
    public static final DeferredHolder VEX_GOBLIN_SPAWN_EGG;
    public static final DeferredHolder VEX_HOBGOBLIN_SPAWN_EGG;
    public static final DeferredHolder VEX_HARPY_SPAWN_EGG;
    public static final DeferredHolder VEX_MINOTAUR_SPAWN_EGG;
    public static final DeferredHolder VEX_HYDRA_SPAWN_EGG;
    public static final DeferredHolder VEX_WYVERN_SPAWN_EGG;
    public static final DeferredHolder PERSYS_BOSS_SPAWN_EGG;
    public static final DeferredHolder ATHEON_BOSS_SPAWN_EGG;
    public static final DeferredHolder PROTHEON_BOSS_SPAWN_EGG;
    public static final DeferredHolder FALLEN_DREG;
    public static final DeferredHolder FALLEN_WRETCH;
    public static final DeferredHolder FALLEN_VANDAL_SNIPER;
    public static final DeferredHolder FALLEN_CAPTAIN_RIFLE;
    public static final DeferredHolder FALLEN_CAPTAIN_SCORCH;
    public static final DeferredHolder FALLEN_SHANK;
    public static final DeferredHolder FALLEN_BRUISER;
    public static final DeferredHolder DEVIL_WALKER;
    public static final DeferredHolder SEPIKS_PRIME;
    public static final DeferredHolder BLAST_SERVITOR;
    public static final DeferredHolder VANOK_ARCLITE;
    public static final DeferredHolder EXPLODER_SHANK;
    public static final DeferredHolder TRACER_SHANK;
    public static final DeferredHolder HEAVY_SHANK;
    public static final DeferredHolder DEVILS_SERVITOR;
    public static final DeferredHolder ARCHON_PRIEST;
    public static final DeferredHolder HIVE_THRALL;
    public static final DeferredHolder HIVE_CURSED_THRALL;
    public static final DeferredHolder HIVE_ACOLYTE;
    public static final DeferredHolder HIVE_KNIGHT;
    public static final DeferredHolder HIVE_WIZARD;
    public static final DeferredHolder HIVE_OGRE;
    public static final DeferredHolder HIVE_SHRIEKER;
    public static final DeferredHolder LUCENT_KNIGHT;
    public static final DeferredHolder HIVE_GHOST;
    public static final DeferredHolder LUCENT_MOTH;
    public static final DeferredHolder ORYX_BOSS;
    public static final DeferredHolder VEX_GOBLIN;
    public static final DeferredHolder VEX_HOBGOBLIN;
    public static final DeferredHolder VEX_HARPY;
    public static final DeferredHolder VEX_MINOTAUR;
    public static final DeferredHolder VEX_HYDRA;
    public static final DeferredHolder VEX_WYVERN;
    public static final DeferredHolder VEX_ORACLE;
    public static final DeferredHolder PERSYS_BOSS;
    public static final DeferredHolder ATHEON_BOSS;
    public static final DeferredHolder PROTHEON_BOSS;
    public static final DeferredHolder SHOCK_BOLT_ENTITY;
    public static final DeferredHolder SHOCK_GRENADE_ENTITY;
    public static final DeferredHolder WIRE_BOLT_ENTITY;
    public static final DeferredHolder SHRAPNEL_PELLET_ENTITY;
    public static final DeferredHolder SCORCH_ROCKET_ENTITY;
    public static final DeferredHolder HIVE_SOULFIRE_BOLT_ENTITY;
    public static final DeferredHolder VEX_SLAP_BOLT_ENTITY;
    public static final DeferredHolder VOID_AXIOM_BOLT_ENTITY;

    public static DeferredHolder getStrikeEntityHolder(String string) {
        if ("FALLEN_DREG".equals(string)) {
            return FALLEN_DREG;
        }
        if ("FALLEN_WRETCH".equals(string)) {
            return FALLEN_WRETCH;
        }
        if ("FALLEN_VANDAL_SNIPER".equals(string)) {
            return FALLEN_VANDAL_SNIPER;
        }
        if ("FALLEN_CAPTAIN_RIFLE".equals(string)) {
            return FALLEN_CAPTAIN_RIFLE;
        }
        if ("FALLEN_CAPTAIN_SCORCH".equals(string)) {
            return FALLEN_CAPTAIN_SCORCH;
        }
        if ("FALLEN_SHANK".equals(string)) {
            return FALLEN_SHANK;
        }
        if ("FALLEN_BRUISER".equals(string)) {
            return FALLEN_BRUISER;
        }
        if ("DEVIL_WALKER".equals(string)) {
            return DEVIL_WALKER;
        }
        if ("SEPIKS_PRIME".equals(string)) {
            return SEPIKS_PRIME;
        }
        if ("BLAST_SERVITOR".equals(string)) {
            return BLAST_SERVITOR;
        }
        if ("VANOK_ARCLITE".equals(string)) {
            return VANOK_ARCLITE;
        }
        if ("EXPLODER_SHANK".equals(string)) {
            return EXPLODER_SHANK;
        }
        if ("TRACER_SHANK".equals(string)) {
            return TRACER_SHANK;
        }
        if ("HEAVY_SHANK".equals(string)) {
            return HEAVY_SHANK;
        }
        if ("DEVILS_SERVITOR".equals(string)) {
            return DEVILS_SERVITOR;
        }
        if ("ARCHON_PRIEST".equals(string)) {
            return ARCHON_PRIEST;
        }
        if ("HIVE_THRALL".equals(string)) {
            return HIVE_THRALL;
        }
        if ("HIVE_CURSED_THRALL".equals(string)) {
            return HIVE_CURSED_THRALL;
        }
        if ("HIVE_ACOLYTE".equals(string)) {
            return HIVE_ACOLYTE;
        }
        if ("HIVE_KNIGHT".equals(string)) {
            return HIVE_KNIGHT;
        }
        if ("HIVE_WIZARD".equals(string)) {
            return HIVE_WIZARD;
        }
        if ("HIVE_OGRE".equals(string)) {
            return HIVE_OGRE;
        }
        if ("HIVE_SHRIEKER".equals(string)) {
            return HIVE_SHRIEKER;
        }
        if ("LUCENT_KNIGHT".equals(string)) {
            return LUCENT_KNIGHT;
        }
        if ("HIVE_GHOST".equals(string)) {
            return HIVE_GHOST;
        }
        if ("LUCENT_MOTH".equals(string)) {
            return LUCENT_MOTH;
        }
        if ("ORYX_BOSS".equals(string)) {
            return ORYX_BOSS;
        }
        if ("VEX_GOBLIN".equals(string)) {
            return VEX_GOBLIN;
        }
        if ("VEX_HOBGOBLIN".equals(string)) {
            return VEX_HOBGOBLIN;
        }
        if ("VEX_HARPY".equals(string)) {
            return VEX_HARPY;
        }
        if ("VEX_MINOTAUR".equals(string)) {
            return VEX_MINOTAUR;
        }
        if ("VEX_HYDRA".equals(string)) {
            return VEX_HYDRA;
        }
        if ("VEX_WYVERN".equals(string)) {
            return VEX_WYVERN;
        }
        if ("VEX_ORACLE".equals(string)) {
            return VEX_ORACLE;
        }
        if ("PERSYS_BOSS".equals(string)) {
            return PERSYS_BOSS;
        }
        if ("ATHEON_BOSS".equals(string)) {
            return ATHEON_BOSS;
        }
        if ("PROTHEON_BOSS".equals(string)) {
            return PROTHEON_BOSS;
        }
        return FALLEN_DREG;
    }

    public static ItemStack createWeaponStackByIndex(int n) {
        return new ItemStack((ItemLike)GUN_HOLDERS[n].get());
    }

    static {
        DeferredHolder[] deferredHolderArray = GUN_HOLDERS;
        deferredHolderArray[0] = ITEMS.register("khvostov_7g_0x", (Supplier)new GunItemSupplier(0));
        deferredHolderArray[1] = ITEMS.register("ammit_ar2", (Supplier)new GunItemSupplier(1));
        deferredHolderArray[2] = ITEMS.register("sweet_business", (Supplier)new GunItemSupplier(2));
        deferredHolderArray[3] = ITEMS.register("monte_carlo", (Supplier)new GunItemSupplier(3));
        deferredHolderArray[4] = ITEMS.register("cerberus_plus_1", (Supplier)new GunItemSupplier(4));
        deferredHolderArray[5] = ITEMS.register("ace_of_spades", (Supplier)new GunItemSupplier(5));
        deferredHolderArray[6] = ITEMS.register("sunshot", (Supplier)new GunItemSupplier(6));
        deferredHolderArray[7] = ITEMS.register("thorn", (Supplier)new GunItemSupplier(7));
        deferredHolderArray[8] = ITEMS.register("crimson", (Supplier)new GunItemSupplier(8));
        deferredHolderArray[9] = ITEMS.register("the_last_word", (Supplier)new GunItemSupplier(9));
        deferredHolderArray[10] = ITEMS.register("izanagis_burden", (Supplier)new GunItemSupplier(10));
        deferredHolderArray[11] = ITEMS.register("cloudstrike", (Supplier)new GunItemSupplier(11));
        deferredHolderArray[12] = ITEMS.register("whisper_of_the_worm", (Supplier)new GunItemSupplier(12));
        deferredHolderArray[13] = ITEMS.register("darci", (Supplier)new GunItemSupplier(13));
        deferredHolderArray[14] = ITEMS.register("ice_breaker", (Supplier)new GunItemSupplier(14));
        deferredHolderArray[15] = ITEMS.register("the_chaperone", (Supplier)new GunItemSupplier(15));
        deferredHolderArray[16] = ITEMS.register("ikelos_sg_v103", (Supplier)new GunItemSupplier(16));
        deferredHolderArray[17] = ITEMS.register("conditional_finality", (Supplier)new GunItemSupplier(17));
        deferredHolderArray[18] = ITEMS.register("tractor_cannon", (Supplier)new GunItemSupplier(18));
        deferredHolderArray[19] = ITEMS.register("lord_of_wolves", (Supplier)new GunItemSupplier(19));
        deferredHolderArray[20] = ITEMS.register("blood_feud", (Supplier)new GunItemSupplier(20));
        deferredHolderArray[21] = ITEMS.register("brigands_law", (Supplier)new GunItemSupplier(21));
        deferredHolderArray[22] = ITEMS.register("tarnished_mettle", (Supplier)new GunItemSupplier(22));
        deferredHolderArray[23] = ITEMS.register("no_reprieve", (Supplier)new GunItemSupplier(23));
        deferredHolderArray[24] = ITEMS.register("wrongful_ingress", (Supplier)new GunItemSupplier(24));
        deferredHolderArray[25] = ITEMS.register("parole", (Supplier)new GunItemSupplier(25));
        deferredHolderArray[26] = ITEMS.register("total_lockdown", (Supplier)new GunItemSupplier(26));
        deferredHolderArray[27] = ITEMS.register("eternal_retribution", (Supplier)new GunItemSupplier(27));
        deferredHolderArray[28] = ITEMS.register("necrochasm", (Supplier)new GunItemSupplier(28));
        deferredHolderArray[29] = ITEMS.register("abyss_defiant", (Supplier)new GunItemSupplier(29));
        deferredHolderArray[30] = ITEMS.register("fang_of_ir_yut", (Supplier)new GunItemSupplier(30));
        deferredHolderArray[31] = ITEMS.register("oversoul_edict", (Supplier)new GunItemSupplier(31));
        deferredHolderArray[32] = ITEMS.register("word_of_crota", (Supplier)new GunItemSupplier(32));
        deferredHolderArray[33] = ITEMS.register("swordbreaker", (Supplier)new GunItemSupplier(33));
        deferredHolderArray[34] = ITEMS.register("song_of_ir_yut", (Supplier)new GunItemSupplier(34));
        deferredHolderArray[35] = ITEMS.register("vex_mythoclast", (Supplier)new GunItemSupplier(35));
        deferredHolderArray[36] = ITEMS.register("fatebringer", (Supplier)new GunItemSupplier(36));
        deferredHolderArray[37] = ITEMS.register("vision_of_confluence", (Supplier)new GunItemSupplier(37));
        deferredHolderArray[38] = ITEMS.register("praedyths_revenge", (Supplier)new GunItemSupplier(38));
        deferredHolderArray[39] = ITEMS.register("found_verdict", (Supplier)new GunItemSupplier(39));
        for (int i=40;i<deferredHolderArray.length;i++) { final int index=i; deferredHolderArray[i]=ITEMS.register(com.jax.destiny2mc.weapon.WeaponSpec.ALL[i].id, (Supplier)new GunItemSupplier(index)); }
        SPECIAL_AMMO = ITEMS.register("special_ammo", (Supplier)new SimpleItemSupplier(0));
        EXOTIC_ENGRAM = ITEMS.register("exotic_engram", (Supplier)new SimpleItemSupplier(1));
        VANGUARD_COMMUNICATOR = ITEMS.register("vanguard_communicator", (Supplier)new SimpleItemSupplier(3));
        GUNSMITH_FUI_BLOCK = BLOCKS.register("gunsmith_fui_workbench", (Supplier)new SimpleItemSupplier(10));
        GUNSMITH_MC_BLOCK = BLOCKS.register("gunsmith_mc_workbench", (Supplier)new SimpleItemSupplier(11));
        COSMODROME_CHEST_BLOCK = BLOCKS.register("cosmodrome_chest", (Supplier)new SimpleItemSupplier(12));
        FALLEN_CHEST_BLOCK = BLOCKS.register("fallen_chest", (Supplier)new SimpleItemSupplier(13));
        AWOKEN_CHEST_BLOCK = BLOCKS.register("awoken_chest", (Supplier)new SimpleItemSupplier(14));
        LEVIATHAN_CHEST_BLOCK = BLOCKS.register("leviathan_chest", (Supplier)new SimpleItemSupplier(15));
        STRIKE_BARRIER_BLOCK = BLOCKS.register("strike_barrier", (Supplier)new SimpleItemSupplier(16));
        GUNSMITH_FUI_ITEM = ITEMS.register("gunsmith_fui_workbench", (Supplier)new SimpleItemSupplier(20));
        GUNSMITH_MC_ITEM = ITEMS.register("gunsmith_mc_workbench", (Supplier)new SimpleItemSupplier(21));
        COSMODROME_CHEST_ITEM = ITEMS.register("cosmodrome_chest", (Supplier)new SimpleItemSupplier(22));
        FALLEN_CHEST_ITEM = ITEMS.register("fallen_chest", (Supplier)new SimpleItemSupplier(23));
        AWOKEN_CHEST_ITEM = ITEMS.register("awoken_chest", (Supplier)new SimpleItemSupplier(24));
        LEVIATHAN_CHEST_ITEM = ITEMS.register("leviathan_chest", (Supplier)new SimpleItemSupplier(25));
        STRIKE_BARRIER_ITEM = ITEMS.register("strike_barrier", (Supplier)new SimpleItemSupplier(26));
        DREG_SPAWN_EGG = ITEMS.register("fallen_dreg_spawn_egg", (Supplier)new SimpleItemSupplier(30));
        WRETCH_SPAWN_EGG = ITEMS.register("fallen_wretch_spawn_egg", (Supplier)new SimpleItemSupplier(31));
        VANDAL_SNIPER_SPAWN_EGG = ITEMS.register("fallen_vandal_sniper_spawn_egg", (Supplier)new SimpleItemSupplier(32));
        CAPTAIN_RIFLE_SPAWN_EGG = ITEMS.register("fallen_captain_rifle_spawn_egg", (Supplier)new SimpleItemSupplier(33));
        CAPTAIN_SCORCH_SPAWN_EGG = ITEMS.register("fallen_captain_scorch_spawn_egg", (Supplier)new SimpleItemSupplier(34));
        SHANK_SPAWN_EGG = ITEMS.register("fallen_shank_spawn_egg", (Supplier)new SimpleItemSupplier(35));
        BRUISER_SPAWN_EGG = ITEMS.register("fallen_bruiser_spawn_egg", (Supplier)new SimpleItemSupplier(36));
        DEVIL_WALKER_SPAWN_EGG = ITEMS.register("devil_walker_spawn_egg", (Supplier)new SimpleItemSupplier(90));
        SEPIKS_PRIME_SPAWN_EGG = ITEMS.register("sepiks_prime_spawn_egg", (Supplier)new SimpleItemSupplier(91));
        BLAST_SERVITOR_SPAWN_EGG = ITEMS.register("blast_servitor_spawn_egg", (Supplier)new SimpleItemSupplier(92));
        VANOK_ARCLITE_SPAWN_EGG = ITEMS.register("vanok_arclite_spawn_egg", (Supplier)new SimpleItemSupplier(93));
        PLUNDERED_ETHER_ALLOY = ITEMS.register("plundered_ether_alloy", (Supplier)new SimpleItemSupplier(40));
        SCORCH_ETHER_CORE = ITEMS.register("scorch_ether_core", (Supplier)new SimpleItemSupplier(41));
        ECLIPSE_MONOLITH_CIPHER = ITEMS.register("eclipse_monolith_cipher", (Supplier)new SimpleItemSupplier(42));
        SHOCK_BOLT_ITEM = ITEMS.register("shock_bolt_item", (Supplier)new SimpleItemSupplier(43));
        SHOCK_GRENADE_ITEM = ITEMS.register("shock_grenade_item", (Supplier)new SimpleItemSupplier(44));
        WIRE_BOLT_ITEM = ITEMS.register("wire_rifle_bolt_item", (Supplier)new SimpleItemSupplier(45));
        SHRAPNEL_PELLET_ITEM = ITEMS.register("shrapnel_pellet_item", (Supplier)new SimpleItemSupplier(46));
        SCORCH_ROCKET_ITEM = ITEMS.register("scorch_rocket_item", (Supplier)new SimpleItemSupplier(47));
        COSMODROME_CHEST_LID_ITEM = ITEMS.register("cosmodrome_chest_lid", (Supplier)new SimpleItemSupplier(48));
        FALLEN_CHEST_LID_ITEM = ITEMS.register("fallen_chest_lid", (Supplier)new SimpleItemSupplier(49));
        AWOKEN_CHEST_LID_ITEM = ITEMS.register("awoken_chest_lid", (Supplier)new SimpleItemSupplier(50));
        LEVIATHAN_CHEST_LID_ITEM = ITEMS.register("leviathan_chest_lid", (Supplier)new SimpleItemSupplier(51));
        HIVE_CHITIN_FRAGMENT = ITEMS.register("hive_chitin_fragment", (Supplier)new SimpleItemSupplier(52));
        VEX_RADIOLARIAN_CORE = ITEMS.register("vex_radiolarian_core", (Supplier)new SimpleItemSupplier(53));
        HIVE_SOULFIRE_BOLT_ITEM = ITEMS.register("hive_soulfire_bolt_item", (Supplier)new SimpleItemSupplier(54));
        VEX_SLAP_BOLT_ITEM = ITEMS.register("vex_slap_bolt_item", (Supplier)new SimpleItemSupplier(55));
        VOID_AXIOM_BOLT_ITEM = ITEMS.register("void_axiom_bolt_item", (Supplier)new SimpleItemSupplier(56));
        LOOT_CHEST_BE = BLOCK_ENTITIES.register("destiny_loot_chest", (Supplier)new SimpleItemSupplier(60));
        ARSENAL_TAB = TABS.register("arsenal", (Supplier)new SimpleItemSupplier(2));
        EXPLODER_SHANK_SPAWN_EGG = ITEMS.register("exploder_shank_spawn_egg", (Supplier)new SimpleItemSupplier(120));
        TRACER_SHANK_SPAWN_EGG = ITEMS.register("tracer_shank_spawn_egg", (Supplier)new SimpleItemSupplier(121));
        HEAVY_SHANK_SPAWN_EGG = ITEMS.register("heavy_shank_spawn_egg", (Supplier)new SimpleItemSupplier(122));
        DEVILS_SERVITOR_SPAWN_EGG = ITEMS.register("devils_servitor_spawn_egg", (Supplier)new SimpleItemSupplier(123));
        ARCHON_PRIEST_SPAWN_EGG = ITEMS.register("archon_priest_spawn_egg", (Supplier)new SimpleItemSupplier(124));
        HIVE_THRALL_SPAWN_EGG = ITEMS.register("hive_thrall_spawn_egg", (Supplier)new SimpleItemSupplier(125));
        HIVE_CURSED_THRALL_SPAWN_EGG = ITEMS.register("hive_cursed_thrall_spawn_egg", (Supplier)new SimpleItemSupplier(126));
        HIVE_ACOLYTE_SPAWN_EGG = ITEMS.register("hive_acolyte_spawn_egg", (Supplier)new SimpleItemSupplier(127));
        HIVE_KNIGHT_SPAWN_EGG = ITEMS.register("hive_knight_spawn_egg", (Supplier)new SimpleItemSupplier(128));
        HIVE_WIZARD_SPAWN_EGG = ITEMS.register("hive_wizard_spawn_egg", (Supplier)new SimpleItemSupplier(129));
        HIVE_OGRE_SPAWN_EGG = ITEMS.register("hive_ogre_spawn_egg", (Supplier)new SimpleItemSupplier(130));
        HIVE_SHRIEKER_SPAWN_EGG = ITEMS.register("hive_shrieker_spawn_egg", (Supplier)new SimpleItemSupplier(131));
        LUCENT_KNIGHT_SPAWN_EGG = ITEMS.register("lucent_knight_spawn_egg", (Supplier)new SimpleItemSupplier(132));
        ORYX_BOSS_SPAWN_EGG = ITEMS.register("oryx_boss_spawn_egg", (Supplier)new SimpleItemSupplier(133));
        VEX_GOBLIN_SPAWN_EGG = ITEMS.register("vex_goblin_spawn_egg", (Supplier)new SimpleItemSupplier(134));
        VEX_HOBGOBLIN_SPAWN_EGG = ITEMS.register("vex_hobgoblin_spawn_egg", (Supplier)new SimpleItemSupplier(135));
        VEX_HARPY_SPAWN_EGG = ITEMS.register("vex_harpy_spawn_egg", (Supplier)new SimpleItemSupplier(136));
        VEX_MINOTAUR_SPAWN_EGG = ITEMS.register("vex_minotaur_spawn_egg", (Supplier)new SimpleItemSupplier(137));
        VEX_HYDRA_SPAWN_EGG = ITEMS.register("vex_hydra_spawn_egg", (Supplier)new SimpleItemSupplier(138));
        VEX_WYVERN_SPAWN_EGG = ITEMS.register("vex_wyvern_spawn_egg", (Supplier)new SimpleItemSupplier(139));
        PERSYS_BOSS_SPAWN_EGG = ITEMS.register("persys_boss_spawn_egg", (Supplier)new SimpleItemSupplier(140));
        ATHEON_BOSS_SPAWN_EGG = ITEMS.register("atheon_boss_spawn_egg", (Supplier)new SimpleItemSupplier(141));
        PROTHEON_BOSS_SPAWN_EGG = ITEMS.register("protheon_boss_spawn_egg", (Supplier)new SimpleItemSupplier(142));
        FALLEN_DREG = ENTITIES.register("fallen_dreg", (Supplier)new SimpleItemSupplier(200));
        FALLEN_WRETCH = ENTITIES.register("fallen_wretch", (Supplier)new SimpleItemSupplier(201));
        FALLEN_VANDAL_SNIPER = ENTITIES.register("fallen_vandal_sniper", (Supplier)new SimpleItemSupplier(202));
        FALLEN_CAPTAIN_RIFLE = ENTITIES.register("fallen_captain_rifle", (Supplier)new SimpleItemSupplier(203));
        FALLEN_CAPTAIN_SCORCH = ENTITIES.register("fallen_captain_scorch", (Supplier)new SimpleItemSupplier(204));
        FALLEN_SHANK = ENTITIES.register("fallen_shank", (Supplier)new SimpleItemSupplier(205));
        FALLEN_BRUISER = ENTITIES.register("fallen_bruiser", (Supplier)new SimpleItemSupplier(206));
        DEVIL_WALKER = ENTITIES.register("devil_walker", (Supplier)new SimpleItemSupplier(207));
        SEPIKS_PRIME = ENTITIES.register("sepiks_prime", (Supplier)new SimpleItemSupplier(208));
        BLAST_SERVITOR = ENTITIES.register("blast_servitor", (Supplier)new SimpleItemSupplier(209));
        VANOK_ARCLITE = ENTITIES.register("vanok_arclite", (Supplier)new SimpleItemSupplier(215));
        EXPLODER_SHANK = ENTITIES.register("exploder_shank", (Supplier)new SimpleItemSupplier(216));
        TRACER_SHANK = ENTITIES.register("tracer_shank", (Supplier)new SimpleItemSupplier(217));
        HEAVY_SHANK = ENTITIES.register("heavy_shank", (Supplier)new SimpleItemSupplier(218));
        DEVILS_SERVITOR = ENTITIES.register("devils_servitor", (Supplier)new SimpleItemSupplier(219));
        ARCHON_PRIEST = ENTITIES.register("archon_priest", (Supplier)new SimpleItemSupplier(220));
        HIVE_THRALL = ENTITIES.register("hive_thrall", (Supplier)new SimpleItemSupplier(221));
        HIVE_CURSED_THRALL = ENTITIES.register("hive_cursed_thrall", (Supplier)new SimpleItemSupplier(222));
        HIVE_ACOLYTE = ENTITIES.register("hive_acolyte", (Supplier)new SimpleItemSupplier(223));
        HIVE_KNIGHT = ENTITIES.register("hive_knight", (Supplier)new SimpleItemSupplier(224));
        HIVE_WIZARD = ENTITIES.register("hive_wizard", (Supplier)new SimpleItemSupplier(225));
        HIVE_OGRE = ENTITIES.register("hive_ogre", (Supplier)new SimpleItemSupplier(226));
        HIVE_SHRIEKER = ENTITIES.register("hive_shrieker", (Supplier)new SimpleItemSupplier(227));
        LUCENT_KNIGHT = ENTITIES.register("lucent_knight", (Supplier)new SimpleItemSupplier(228));
        HIVE_GHOST = ENTITIES.register("hive_ghost", (Supplier)new SimpleItemSupplier(229));
        LUCENT_MOTH = ENTITIES.register("lucent_moth", (Supplier)new SimpleItemSupplier(230));
        ORYX_BOSS = ENTITIES.register("oryx_boss", (Supplier)new SimpleItemSupplier(231));
        VEX_GOBLIN = ENTITIES.register("vex_goblin", (Supplier)new SimpleItemSupplier(232));
        VEX_HOBGOBLIN = ENTITIES.register("vex_hobgoblin", (Supplier)new SimpleItemSupplier(233));
        VEX_HARPY = ENTITIES.register("vex_harpy", (Supplier)new SimpleItemSupplier(234));
        VEX_MINOTAUR = ENTITIES.register("vex_minotaur", (Supplier)new SimpleItemSupplier(235));
        VEX_HYDRA = ENTITIES.register("vex_hydra", (Supplier)new SimpleItemSupplier(236));
        VEX_WYVERN = ENTITIES.register("vex_wyvern", (Supplier)new SimpleItemSupplier(237));
        VEX_ORACLE = ENTITIES.register("vex_oracle", (Supplier)new SimpleItemSupplier(238));
        PERSYS_BOSS = ENTITIES.register("persys_boss", (Supplier)new SimpleItemSupplier(239));
        ATHEON_BOSS = ENTITIES.register("atheon_boss", (Supplier)new SimpleItemSupplier(240));
        PROTHEON_BOSS = ENTITIES.register("protheon_boss", (Supplier)new SimpleItemSupplier(241));
        SHOCK_BOLT_ENTITY = ENTITIES.register("shock_bolt", (Supplier)new SimpleItemSupplier(310));
        SHOCK_GRENADE_ENTITY = ENTITIES.register("shock_grenade", (Supplier)new SimpleItemSupplier(311));
        WIRE_BOLT_ENTITY = ENTITIES.register("wire_rifle_bolt", (Supplier)new SimpleItemSupplier(312));
        SHRAPNEL_PELLET_ENTITY = ENTITIES.register("shrapnel_pellet", (Supplier)new SimpleItemSupplier(313));
        SCORCH_ROCKET_ENTITY = ENTITIES.register("scorch_rocket", (Supplier)new SimpleItemSupplier(314));
        HIVE_SOULFIRE_BOLT_ENTITY = ENTITIES.register("hive_soulfire_bolt", (Supplier)new SimpleItemSupplier(342));
        VEX_SLAP_BOLT_ENTITY = ENTITIES.register("vex_slap_bolt", (Supplier)new SimpleItemSupplier(343));
        VOID_AXIOM_BOLT_ENTITY = ENTITIES.register("void_axiom_bolt", (Supplier)new SimpleItemSupplier(344));
    }
}

