package com.jax.destiny2mc.frontier;

import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Unique Warframe Corpus combat mechanics & Tenno WeaponMod elemental interactions:
 * - Regenerating Proto-Shields on all 10 Corpus units:
 *     - Magnetic (Cold+Electricity) shreds Proto-Shields (+125%) & Nullifier domes, and jams shield regen to 7s
 *     - Cold shreds Proto-Shields (+75%) & Nullifier domes (+50%)
 *     - Uncombined Toxin (or Toxin Tenno weapons / Poison) bypasses Proto-Shields & Bursa frontal shields straight to HP
 *     - Electricity deals +50% bonus damage to Corpus robotics (MOAs, Ospreys, Bursas, Hyenas, Raknoids)
 *     - Viral (+65%) and Heat (+35%) amplify damage to exposed Corpus health once shields are stripped
 * - Nullifier Crewman (149): destructible shrinking energy dome with per-hit damage cap, dorsal drone instant collapse, & buff suppression
 * - Isolator Bursa (150): 80% frontal riot-shield deflection + 2.0x rear console weakpoint crit
 * - Shockwave MOA (148): telegraphed ground-level jumpable Arc shockwave ring
 * - Hyena Proxy (151): fast predator flanking & Magnetic shield disruption
 * - Scyto Raknoid (152): Cryo-Web snare volley + 1.75x frosted dorsal dome weakpoint
 * - Warframe Mod Pack drops from elite Corpus units
 */
public final class CorpusCombat {
    private static final String TAG_SHIELD = "CorpusShield";
    private static final String TAG_DELAY = "CorpusShieldDelay";
    private static final String TAG_BUBBLE = "CorpusNullifierBubble";
    private static final String TAG_BUBBLE_CD = "CorpusNullifierCd";

    private CorpusCombat() {}

    public static boolean isCorpus(FrontierMob mob) {
        return mob != null && mob.unit != null && "corpus".equals(mob.unit.faction());
    }

    public static boolean isRobotic(FrontierCatalog.Unit unit) {
        return switch (unit.kind()) {
            case 144, 145, 148, 150, 151, 152 -> true; // MOA, Osprey, Shockwave MOA, Bursa, Hyena, Raknoid
            default -> false;
        };
    }

    public static float maxShield(FrontierCatalog.Unit unit) {
        return switch (unit.kind()) {
            case 143 -> 24f;  // Crewman
            case 144 -> 34f;  // MOA
            case 145 -> 42f;  // Shield Osprey
            case 146 -> 20f;  // Prod Crewman
            case 147 -> 72f;  // Corpus Tech
            case 148 -> 48f;  // Shockwave MOA
            case 149 -> 56f;  // Nullifier Crewman
            case 150 -> 140f; // Isolator Bursa
            case 151 -> 84f;  // Hyena Proxy
            case 152 -> 78f;  // Scyto Raknoid
            default -> 28f;
        };
    }

    public static float maxBubble(FrontierMob mob) {
        return mob.unit.kind() == 149 ? 40f : 0f;
    }

    private static void ensureInit(FrontierMob mob) {
        CompoundTag data = mob.getPersistentData();
        if (!data.contains(TAG_SHIELD)) {
            data.putFloat(TAG_SHIELD, maxShield(mob.unit));
            data.putInt(TAG_DELAY, 0);
        }
        if (mob.unit.kind() == 149 && !data.contains(TAG_BUBBLE)) {
            data.putFloat(TAG_BUBBLE, maxBubble(mob));
            data.putInt(TAG_BUBBLE_CD, 0);
        }
    }

    public static void tick(FrontierMob mob, ServerLevel level) {
        if (!isCorpus(mob) || !mob.isAlive()) return;
        ensureInit(mob);
        CompoundTag data = mob.getPersistentData();

        // 1. Regenerating Corpus Proto-Shields (faster when tethered by a Shield Osprey)
        float maxS = maxShield(mob.unit);
        float shield = data.getFloat(TAG_SHIELD);
        int delay = data.getInt(TAG_DELAY);
        if (delay > 0) {
            data.putInt(TAG_DELAY, delay - 1);
        } else if (shield < maxS) {
            boolean ospreyNearby = !level.getEntitiesOfClass(FrontierMob.class, mob.getBoundingBox().inflate(8.5),
                e -> e.isAlive() && e.unit.kind() == 145).isEmpty();
            float regen = (maxS * 0.012f) * (ospreyNearby ? 1.75f : 1.0f);
            float next = Math.min(maxS, shield + regen);
            data.putFloat(TAG_SHIELD, next);
            if (mob.tickCount % 12 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(),
                    2, mob.getBbWidth() * 0.4, mob.getBbHeight() * 0.25, mob.getBbWidth() * 0.4, 0.01);
            }
        }

        // 2. Nullifier Crewman (149): Shrinking energy dome visual & interior ability/buff suppression
        if (mob.unit.kind() == 149) {
            float bubble = data.getFloat(TAG_BUBBLE);
            int cd = data.getInt(TAG_BUBBLE_CD);
            if (bubble <= 0f) {
                if (cd > 0) {
                    data.putInt(TAG_BUBBLE_CD, cd - 1);
                } else {
                    data.putFloat(TAG_BUBBLE, maxBubble(mob) * 0.65f);
                }
            } else {
                float maxB = maxBubble(mob);
                if (delay <= 0 && bubble < maxB) {
                    bubble = Math.min(maxB, bubble + 0.12f);
                    data.putFloat(TAG_BUBBLE, bubble);
                }
                double radius = 1.6 + 2.1 * (bubble / maxB);
                if (mob.tickCount % 4 == 0) {
                    for (int i = 0; i < 16; i++) {
                        double a = (i * Math.PI * 2.0 / 16.0) + mob.tickCount * 0.08;
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            mob.getX() + Math.cos(a) * radius,
                            mob.getY() + 0.35 + (i % 3) * (radius * 0.28),
                            mob.getZ() + Math.sin(a) * radius,
                            1, 0, 0, 0, 0);
                    }
                }
                // Suppress player buffs & absorption inside the Nullifier field
                if (mob.tickCount % 10 == 0) {
                    for (Player p : level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(radius),
                        e -> !e.isSpectator() && !e.isCreative() && e.distanceTo(mob) <= radius)) {
                        if (p.getAbsorptionAmount() > 0f) p.setAbsorptionAmount(0f);
                        p.removeEffect(MobEffects.DAMAGE_BOOST);
                        p.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                        p.removeEffect(MobEffects.MOVEMENT_SPEED);
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0, false, true), mob);
                    }
                }
            }
        }
    }

    /**
     * Filter incoming damage through Corpus unique mechanics & Tenno WeaponMod elemental synergies.
     * Returns adjusted HP damage (0f if fully absorbed by Nullifier dome or Proto-Shields).
     */
    public static float filterDamage(FrontierMob mob, DamageSource src, float damage) {
        if (!isCorpus(mob) || damage <= 0f || !(mob.level() instanceof ServerLevel level)) return damage;
        ensureInit(mob);
        CompoundTag data = mob.getPersistentData();
        Entity attacker = src.getEntity();

        // Check Warframe Tenno WeaponMod elemental bonuses (base + combined) on the attacker's held weapon
        double coldBonus = 0.0;
        double heatBonus = 0.0;
        double elecBonus = 0.0;
        double toxinBonus = 0.0;
        double magneticBonus = 0.0;
        double viralBonus = 0.0;
        boolean toxinBypass = mob.hasEffect(MobEffects.POISON);
        if (attacker instanceof Player player) {
            ItemStack held = player.getMainHandItem();
            Map<ModStat, Double> mods = WeaponMods.bonuses(player, held);
            coldBonus = mods.getOrDefault(ModStat.COLD, 0.0);
            heatBonus = mods.getOrDefault(ModStat.HEAT, 0.0);
            elecBonus = mods.getOrDefault(ModStat.ELECTRICITY, 0.0);
            toxinBonus = mods.getOrDefault(ModStat.TOXIN, 0.0);
            magneticBonus = mods.getOrDefault(ModStat.MAGNETIC, 0.0);
            viralBonus = mods.getOrDefault(ModStat.VIRAL, 0.0);
            if (toxinBonus > 0.0) {
                toxinBypass = true;
            }
            String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
            if (itemId.contains("bubonico") || itemId.contains("torid") || itemId.contains("embolist")) {
                toxinBypass = true;
            }
        }

        // Compute hit position for weakpoints & directional shields
        Vec3 hitPos = mob.getBoundingBox().getCenter();
        if (attacker instanceof Player player) {
            Vec3 eye = player.getEyePosition();
            hitPos = mob.getBoundingBox().clip(eye, eye.add(player.getLookAngle().scale(128.0))).orElse(hitPos);
        }
        double hitFracY = (hitPos.y - mob.getY()) / Math.max(0.1, mob.getBbHeight());

        // 1. Nullifier Crewman (149): Shrinking energy dome intercepts outside attacks
        if (mob.unit.kind() == 149) {
            float bubble = data.getFloat(TAG_BUBBLE);
            if (bubble > 0f && attacker != null) {
                double radius = 1.6 + 2.1 * (bubble / maxBubble(mob));
                boolean inside = attacker.distanceTo(mob) <= radius;
                if (!inside) {
                    // Sniping the spinning dorsal emitter drone collapses the dome instantly!
                    if (hitFracY >= 0.86) {
                        data.putFloat(TAG_BUBBLE, 0f);
                        data.putInt(TAG_BUBBLE_CD, 220);
                        level.sendParticles(ParticleTypes.FLASH, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 1, 0, 0, 0, 0);
                        level.playSound(null, mob.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.1f, 1.3f);
                    } else {
                        // Per-hit cap rewards rapid-fire weapons; Magnetic lifts cap and shreds faster
                        float perHitCap = 8.0f + (float) magneticBonus * 4.0f;
                        float domeMult = 1.0f + (float) coldBonus * 0.5f + (float) magneticBonus * 0.85f;
                        float shrink = Math.min(perHitCap, damage * domeMult);
                        float nextB = Math.max(0f, bubble - shrink);
                        data.putFloat(TAG_BUBBLE, nextB);
                        data.putInt(TAG_DELAY, magneticBonus > 0.0 ? 140 : 80);
                        if (nextB <= 0f) {
                            data.putInt(TAG_BUBBLE_CD, 200);
                            level.playSound(null, mob.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0f, 1.15f);
                        } else {
                            level.playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 0.7f, 1.5f);
                        }
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hitPos.x, hitPos.y, hitPos.z, 8, 0.25, 0.25, 0.25, 0.08);
                        return 0f;
                    }
                }
            }
        }

        // 2. Isolator Bursa (150): Directional riot shields (front -80%) & glowing rear console weakpoint (back 2.0x)
        if (mob.unit.kind() == 150 && attacker != null) {
            Vec3 facing = mob.getLookAngle().multiply(1, 0, 1).normalize();
            Vec3 toAttacker = attacker.position().subtract(mob.position()).multiply(1, 0, 1).normalize();
            double dot = facing.dot(toAttacker);
            if (dot > 0.34 && !toxinBypass) {
                damage *= 0.20f;
                level.sendParticles(ParticleTypes.CRIT, hitPos.x, hitPos.y, hitPos.z, 6, 0.2, 0.2, 0.2, 0.06);
                level.playSound(null, mob.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.9f, 0.85f);
            } else if (dot < -0.35) {
                damage *= 2.0f;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hitPos.x, hitPos.y, hitPos.z, 12, 0.2, 0.2, 0.2, 0.1);
                level.playSound(null, mob.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.45f, 1.6f);
            }
        }

        // 3. Scyto Raknoid (152): Frosted-white dorsal abdomen dome weakpoint (1.75x crit)
        if (mob.unit.kind() == 152 && hitFracY >= 0.62) {
            damage *= 1.75f;
            level.sendParticles(ParticleTypes.SNOWFLAKE, hitPos.x, hitPos.y, hitPos.z, 10, 0.3, 0.2, 0.3, 0.04);
        }

        // 4. Electricity bonus against Corpus robotics (MOAs, Ospreys, Bursas, Hyenas, Raknoids)
        if (elecBonus > 0.0 && isRobotic(mob.unit)) {
            damage *= (1.0f + (float) elecBonus * 0.50f);
        }

        // 5. Corpus Proto-Shield Absorption (Magnetic/Cold shred faster; Toxin bypasses straight to HP)
        float shield = data.getFloat(TAG_SHIELD);
        data.putInt(TAG_DELAY, magneticBonus > 0.0 ? 140 : 80);
        if (shield > 0f && !toxinBypass) {
            float shieldMult = 1.0f + (float) coldBonus * 0.75f + (float) magneticBonus * 1.25f;
            float shieldDmg = damage * shieldMult;
            if (shieldDmg < shield) {
                data.putFloat(TAG_SHIELD, shield - shieldDmg);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hitPos.x, hitPos.y, hitPos.z,
                    magneticBonus > 0.0 ? 12 : 7, 0.2, 0.2, 0.2, 0.06);
                level.playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.55f, 1.45f);
                return 0f;
            } else {
                // Shield break! Carry overflow damage to health + spark burst
                float overflow = (shieldDmg - shield) / shieldMult;
                data.putFloat(TAG_SHIELD, 0f);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + mob.getBbHeight() * 0.55, mob.getZ(),
                    18, 0.45, 0.45, 0.45, 0.12);
                level.playSound(null, mob.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.85f, 1.4f);
                damage = overflow;
            }
        } else if (shield <= 0f || toxinBypass) {
            // Exposed Corpus health takes bonus Viral (+65%) and Heat (+35%) damage
            if (viralBonus > 0.0) damage *= (1.0f + (float) viralBonus * 0.65f);
            if (heatBonus > 0.0) damage *= (1.0f + (float) heatBonus * 0.35f);
        }

        return damage;
    }

    /**
     * Execute Corpus-specific special attacks called from FrontierMob.specialAttack / tickMove.
     * Returns true if handled as a Corpus signature move.
     */
    public static boolean specialAttack(FrontierMob mob, ServerLevel level, LivingEntity target, Vec3 mark) {
        if (!isCorpus(mob)) return false;
        String move = mob.unit.move();
        switch (move) {
            case "shockwave" -> {
                // Jumpable expanding Arc shockwave ring (only hits grounded targets!)
                for (double r = 1.5; r <= 6.5; r += 1.6) {
                    FrontierMob.ring(level, mob.position(), r, 2);
                }
                level.playSound(null, mob.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.9f, 1.35f);
                for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(6.5),
                    e -> CombatRules.canDamage(mob, e) && e.distanceTo(mob) <= 6.5)) {
                    if (v.onGround()) {
                        v.hurt(mob.damageSources().mobAttack(mob), mob.unit.damage() * 1.15f);
                        v.knockback(1.15, mob.getX() - v.getX(), mob.getZ() - v.getZ());
                    }
                }
                return true;
            }
            case "raknoid" -> {
                // Sticky Cryo-Web snare volley + frost hazard patch
                FrontierHazard.spawn(level, mob, mark, 3.0f, 80, 2, 4, 0);
                for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, new AABB(mark, mark).inflate(3.2),
                    e -> CombatRules.canDamage(mob, e))) {
                    v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2), mob);
                    level.sendParticles(ParticleTypes.SNOWFLAKE, v.getX(), v.getY() + 0.8, v.getZ(), 10, 0.3, 0.3, 0.3, 0.03);
                }
                return true;
            }
            case "hyena" -> {
                // Predator leap burst + brief Magnetic disruption
                if (target != null && target.isAlive()) {
                    mob.blast(level, mob.position(), 3.2f, mob.unit.damage(), 0.75);
                    if (mob.distanceTo(target) < 4.5) {
                        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0), mob);
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), mob);
                    }
                }
                return true;
            }
            case "bursa" -> {
                // Riot-shield shockwave push + dorsal mortar hazard
                mob.blast(level, mob.position(), 4.5f, mob.unit.damage() * 0.9f, 1.3);
                FrontierHazard.spawn(level, mob, mark, 2.8f, 70, 3, 2, 0);
                return true;
            }
            case "nullifier" -> {
                // Restore Nullifier dome pulse + high-velocity Lanka shot
                CompoundTag data = mob.getPersistentData();
                data.putFloat(TAG_BUBBLE, maxBubble(mob));
                if (target != null && target.isAlive()) {
                    Vec3 from = mob.getEyePosition().add(mob.getLookAngle().scale(0.5));
                    FrontierProjectile.launch(level, mob, from, target.getEyePosition().subtract(from).normalize(),
                        1.45f, mob.unit.damage() * 1.35f, 2, 0, 0);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /** Bonus Warframe Mod Pack drop chance for elite Corpus units. */
    public static void onDeath(FrontierMob mob) {
        if (!isCorpus(mob) || mob.level().isClientSide) return;
        int kind = mob.unit.kind();
        float packChance = switch (kind) {
            case 150 -> 0.45f; // Isolator Bursa
            case 151, 152 -> 0.30f; // Hyena Proxy, Scyto Raknoid
            case 147, 149 -> 0.22f; // Corpus Tech, Nullifier Crewman
            default -> 0.06f;
        };
        if (mob.getRandom().nextFloat() < packChance) {
            String[] pools = {"mod_pack", "primary_mod_pack", "secondary_mod_pack", "melee_mod_pack"};
            String pick = pools[mob.getRandom().nextInt(pools.length)];
            Item pack = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc", pick));
            if (pack != Items.AIR) {
                mob.spawnAtLocation(new ItemStack(pack, 1));
            }
        }
    }
}
