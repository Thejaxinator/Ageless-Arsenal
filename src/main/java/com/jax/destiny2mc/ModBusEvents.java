/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.handling.IPayloadHandler
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.jax.destiny2mc;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.client.ClientFeedbackPayloadHandler;
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
import com.jax.destiny2mc.entity.HiveThrallEntity;
import com.jax.destiny2mc.entity.HiveWizardEntity;
import com.jax.destiny2mc.entity.LucentKnightEntity;
import com.jax.destiny2mc.entity.LucentMothEntity;
import com.jax.destiny2mc.entity.OryxBossEntity;
import com.jax.destiny2mc.entity.PersysBossEntity;
import com.jax.destiny2mc.entity.ProtheonBossEntity;
import com.jax.destiny2mc.entity.SepiksPrimeEntity;
import com.jax.destiny2mc.entity.TracerShankEntity;
import com.jax.destiny2mc.entity.VanokArcliteEntity;
import com.jax.destiny2mc.entity.VexGoblinEntity;
import com.jax.destiny2mc.entity.VexHarpyEntity;
import com.jax.destiny2mc.entity.VexHobgoblinEntity;
import com.jax.destiny2mc.entity.VexHydraEntity;
import com.jax.destiny2mc.entity.VexMinotaurEntity;
import com.jax.destiny2mc.entity.VexOracleEntity;
import com.jax.destiny2mc.entity.VexWyvernEntity;
import com.jax.destiny2mc.network.CombatFeedbackPayload;
import com.jax.destiny2mc.network.GunActionPayload;
import com.jax.destiny2mc.network.GunsmithCraftPayload;
import com.jax.destiny2mc.network.GunsmithPerkSwapPayload;
import com.jax.destiny2mc.network.ServerCraftPayloadHandler;
import com.jax.destiny2mc.network.ServerGunPayloadHandler;
import com.jax.destiny2mc.network.ServerPerkSwapPayloadHandler;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModBusEvents {
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent registerPayloadHandlersEvent) {
        PayloadRegistrar payloadRegistrar = registerPayloadHandlersEvent.registrar("3");
        payloadRegistrar = payloadRegistrar.playToServer(GunActionPayload.TYPE, GunActionPayload.STREAM_CODEC, (IPayloadHandler)new ServerGunPayloadHandler());
        payloadRegistrar = payloadRegistrar.playToServer(GunsmithCraftPayload.TYPE, GunsmithCraftPayload.STREAM_CODEC, (IPayloadHandler)new ServerCraftPayloadHandler());
        payloadRegistrar = payloadRegistrar.playToServer(GunsmithPerkSwapPayload.TYPE, GunsmithPerkSwapPayload.STREAM_CODEC, (IPayloadHandler)new ServerPerkSwapPayloadHandler());
        payloadRegistrar.playToClient(CombatFeedbackPayload.TYPE, CombatFeedbackPayload.STREAM_CODEC, (IPayloadHandler)new ClientFeedbackPayloadHandler());
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent entityAttributeCreationEvent) {
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_DREG.get(), FallenDregEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_WRETCH.get(), FallenWretchEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_VANDAL_SNIPER.get(), FallenVandalSniperEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_CAPTAIN_RIFLE.get(), FallenCaptainRifleEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_CAPTAIN_SCORCH.get(), FallenCaptainScorchEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_SHANK.get(), FallenShankEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.FALLEN_BRUISER.get(), FallenBruiserEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.DEVIL_WALKER.get(), DevilWalkerEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.SEPIKS_PRIME.get(), SepiksPrimeEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.BLAST_SERVITOR.get(), BlastServitorEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VANOK_ARCLITE.get(), VanokArcliteEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.EXPLODER_SHANK.get(), ExploderShankEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.TRACER_SHANK.get(), TracerShankEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HEAVY_SHANK.get(), HeavyShankEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.DEVILS_SERVITOR.get(), DevilsServitorEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.ARCHON_PRIEST.get(), ArchonPriestEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_THRALL.get(), HiveThrallEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_CURSED_THRALL.get(), HiveCursedThrallEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_ACOLYTE.get(), HiveAcolyteEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_KNIGHT.get(), HiveKnightEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_WIZARD.get(), HiveWizardEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_OGRE.get(), HiveOgreEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_SHRIEKER.get(), HiveShriekerEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.LUCENT_KNIGHT.get(), LucentKnightEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.HIVE_GHOST.get(), HiveGhostEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.LUCENT_MOTH.get(), LucentMothEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.ORYX_BOSS.get(), OryxBossEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_GOBLIN.get(), VexGoblinEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_HOBGOBLIN.get(), VexHobgoblinEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_HARPY.get(), VexHarpyEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_MINOTAUR.get(), VexMinotaurEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_HYDRA.get(), VexHydraEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_WYVERN.get(), VexWyvernEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.VEX_ORACLE.get(), VexOracleEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.PERSYS_BOSS.get(), PersysBossEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.ATHEON_BOSS.get(), AtheonBossEntity.createAttributes());
        entityAttributeCreationEvent.put((EntityType)ModRegistries.PROTHEON_BOSS.get(), ProtheonBossEntity.createAttributes());
    }
}
