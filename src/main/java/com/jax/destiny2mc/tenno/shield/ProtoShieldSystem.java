package com.jax.destiny2mc.tenno.shield;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Registers the 4-tier Proto-Shield items and runs player Proto-Shield regeneration,
 * damage absorption, Warframe Shield-Gating, and Prime Arc Retaliation via Curios API.
 */
public final class ProtoShieldSystem {
    private static final String TAG_SHIELD = "AgelessProtoShield";
    private static final String TAG_DELAY = "AgelessProtoShieldDelay";
    private static final String TAG_GATE = "AgelessProtoShieldGate";
    private static final String TAG_TIER = "AgelessProtoShieldTier";

    private static final Map<ProtoShieldItem.Tier, ProtoShieldItem> ITEMS = new EnumMap<>(ProtoShieldItem.Tier.class);

    private ProtoShieldSystem() {}

    public static Item itemFor(ProtoShieldItem.Tier tier) {
        return ITEMS.get(tier);
    }

    /** Look up the equipped Proto-Shield tier from the player's Curios inventory. */
    public static ProtoShieldItem.Tier equippedTier(Player player) {
        if (player == null) return null;
        return CuriosApi.getCuriosInventory(player)
            .flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof ProtoShieldItem))
            .map(slot -> ((ProtoShieldItem) slot.stack().getItem()).tier())
            .orElse(null);
    }

    /** Called by CorpusCombat when a player stands inside a Nullifier Crewman's energy dome. */
    public static void drainFromNullifier(Player player, float amount) {
        if (!(player instanceof ServerPlayer sp)) return;
        ProtoShieldItem.Tier tier = equippedTier(sp);
        if (tier == null) return;
        CompoundTag data = sp.getPersistentData();
        float current = data.getFloat(TAG_SHIELD);
        float next = Math.max(0f, current - amount);
        data.putFloat(TAG_SHIELD, next);
        data.putInt(TAG_DELAY, tier.regenDelayTicks);
        sendGauge(sp, tier, next, false);
    }

    @EventBusSubscriber(modid = "destiny2mc", bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.ITEM, helper -> {
                for (ProtoShieldItem.Tier tier : ProtoShieldItem.Tier.values()) {
                    ProtoShieldItem item = new ProtoShieldItem(tier);
                    ITEMS.put(tier, item);
                    helper.register(ResourceLocation.fromNamespaceAndPath("destiny2mc", tier.id), item);
                }
            });
        }
    }

    @EventBusSubscriber(modid = "destiny2mc")
    public static final class Combat {
        private Combat() {}

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return;
            CompoundTag data = player.getPersistentData();
            ProtoShieldItem.Tier tier = equippedTier(player);
            if (tier == null) {
                if (data.contains(TAG_SHIELD)) {
                    data.remove(TAG_SHIELD);
                    data.remove(TAG_DELAY);
                    data.remove(TAG_GATE);
                    data.remove(TAG_TIER);
                }
                return;
            }

            // Initialize or handle tier swap
            String prevTier = data.getString(TAG_TIER);
            if (!tier.id.equals(prevTier)) {
                data.putString(TAG_TIER, tier.id);
                float clamped = data.contains(TAG_SHIELD) ? Math.min(tier.maxShield, data.getFloat(TAG_SHIELD)) : tier.maxShield;
                data.putFloat(TAG_SHIELD, clamped);
                data.putInt(TAG_DELAY, 0);
                data.putInt(TAG_GATE, 0);
            }

            // Tick shield-gate invulnerability window
            int gate = data.getInt(TAG_GATE);
            if (gate > 0) {
                data.putInt(TAG_GATE, gate - 1);
            }

            float shield = data.getFloat(TAG_SHIELD);
            int delay = data.getInt(TAG_DELAY);
            if (delay > 0) {
                data.putInt(TAG_DELAY, delay - 1);
                if (player.tickCount % 10 == 0 && shield < tier.maxShield) {
                    sendGauge(player, tier, shield, gate > 0);
                }
            } else if (shield < tier.maxShield) {
                float next = Math.min(tier.maxShield, shield + (tier.regenPerSecond / 20f));
                data.putFloat(TAG_SHIELD, next);
                if (player.tickCount % 5 == 0 || next >= tier.maxShield) {
                    sendGauge(player, tier, next, false);
                }
                if (player.tickCount % 12 == 0 && player.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        2, 0.35, 0.4, 0.35, 0.01);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0f) return;
            if (player.isCreative() || player.isSpectator()) return;
            ProtoShieldItem.Tier tier = equippedTier(player);
            if (tier == null) return;

            DamageSource src = event.getSource();
            // Void, starvation, drown, and Toxin/Poison bypass Proto-Shields (authentic Warframe rule)
            if (src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || src.is(DamageTypes.STARVE)
                || src.is(DamageTypes.DROWN)
                || src.is(DamageTypes.MAGIC) && player.hasEffect(MobEffects.POISON)) {
                return;
            }

            CompoundTag data = player.getPersistentData();
            int gate = data.getInt(TAG_GATE);
            if (gate > 0) {
                // Active Shield-Gate window blocks incoming damage
                event.setCanceled(true);
                return;
            }

            float shield = data.contains(TAG_SHIELD) ? data.getFloat(TAG_SHIELD) : tier.maxShield;
            data.putInt(TAG_DELAY, tier.regenDelayTicks);
            if (shield <= 0f) return;

            float incoming = event.getAmount();
            ServerLevel level = player.serverLevel();
            if (incoming < shield) {
                float remaining = shield - incoming;
                data.putFloat(TAG_SHIELD, remaining);
                event.setCanceled(true);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.1, player.getZ(),
                    6, 0.35, 0.4, 0.35, 0.06);
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.55f, 1.5f);
                sendGauge(player, tier, remaining, false);
            } else {
                // Shield break! Trigger Warframe Shield-Gating: absorb breaking hit overflow & grant brief i-frames
                data.putFloat(TAG_SHIELD, 0f);
                data.putInt(TAG_GATE, tier.gateTicks);
                event.setCanceled(true);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    22, 0.55, 0.55, 0.55, 0.14);
                level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK,
                    SoundSource.PLAYERS, 0.85f, 1.35f);
                if (tier.retaliationPulse) {
                    level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0, 0, 0, 0);
                    for (LivingEntity hostile : level.getEntitiesOfClass(LivingEntity.class,
                            player.getBoundingBox().inflate(4.2),
                            e -> e != player && e.isAlive() && !e.isAlliedTo(player) && e instanceof Enemy)) {
                        hostile.hurt(player.damageSources().indirectMagic(player, player), 6.0f);
                        hostile.knockback(1.1, player.getX() - hostile.getX(), player.getZ() - hostile.getZ());
                    }
                }
                sendGauge(player, tier, 0f, true);
            }
        }
    }

    private static void sendGauge(ServerPlayer player, ProtoShieldItem.Tier tier, float current, boolean gateActive) {
        int bars = 10;
        int filled = Math.max(0, Math.min(bars, Math.round((current / tier.maxShield) * bars)));
        String bar = "\u2588".repeat(filled) + "\u2591".repeat(bars - filled);
        String text = gateActive
            ? String.format("PROTO-SHIELD [%s] GATE ACTIVE", tier.label)
            : String.format("PROTO-SHIELD [%s] [%s] %.0f/%.0f", tier.label, bar, current, tier.maxShield);
        player.displayClientMessage(Component.literal(text).withStyle(gateActive ? ChatFormatting.GOLD : tier.color), true);
    }
}
