package com.jax.destiny2mc.tenno.melee;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="destiny2mc")
public final class MeleeCombat {
    private static final String DICE = "AgelessAmanata";
    private static final String BLEED = "AgelessReapingCut";
    private static final String[] FACES = {"", "Edge", "Frost", "Force", "Reach", "Tempo", "Fivefold"};
    private static final int WINDOW = 100;
    // AttackEntityEvent runs before vanilla records a hit and resets the attack timer. One player can
    // create a sweep and unrelated damage within the same tick. Only the clicked target may advance.
    private record Strike(UUID target, String id, long tick, boolean heavy, int chain, int sameTarget) {}
    private record Chain(String id, UUID target, int hits, int sameTarget, long tick) {}
    private static final Map<ServerPlayer, Strike> PENDING = new WeakHashMap<>();
    private static final Map<ServerPlayer, Chain> CHAINS = new WeakHashMap<>();
    private MeleeCombat() {}

    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        MeleeModEffects.refreshHeld(p);
        PENDING.remove(p);
        if (!(p.getMainHandItem().getItem() instanceof TennoMeleeItem weapon)) { CHAINS.remove(p); return; }
        if (p.isUsingItem()) { event.setCanceled(true); return; }
        if (!(event.getTarget() instanceof LivingEntity target) || p.getAttackStrengthScale(.5f) < .90f) return;
        long now = p.level().getGameTime();
        var chain = CHAINS.get(p);
        String id = weapon.profile().id();
        boolean active = chain != null && chain.id().equals(id) && now - chain.tick() <= WINDOW;
        PENDING.put(p, new Strike(target.getUUID(), id, now, p.isShiftKeyDown(),
                active ? chain.hits() : 0, active && chain.target().equals(target.getUUID()) ? chain.sameTarget() : 0));
    }
    private static Strike pending(ServerPlayer p, LivingEntity target, DamageSource source) {
        var hit = PENDING.get(p);
        if (hit == null || !source.is(DamageTypes.PLAYER_ATTACK) || source.getDirectEntity() != p
                || hit.tick() != p.level().getGameTime() || !hit.target().equals(target.getUUID())
                || !(p.getMainHandItem().getItem() instanceof TennoMeleeItem w)
                || !hit.id().equals(w.profile().id())) return null;
        return hit;
    }
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer p)) return;
        var held = p.getMainHandItem();
        var source = event.getSource();
        if (!(held.getItem() instanceof TennoMeleeItem weapon) || source.getDirectEntity() != p
                || !source.is(DamageTypes.PLAYER_ATTACK)) return;
        var target = event.getEntity();
        double heat = MeleeModEffects.acceptsHeat(target) ? WeaponMods.bonus(p,held,ModStat.HEAT) : 0;
        double amount = event.getAmount() * (1 + heat);
        var hit = pending(p, target, source);
        if (hit == null) {
            if (heat != 0) event.setAmount((float)amount);
            return;
        }
        var profile = weapon.profile();
        if (hit.heavy()) amount *= (1.25 + .15 * Math.min(3, hit.chain())) *
                (1 + WeaponMods.bonus(p,held,ModStat.HEAVY_DAMAGE));
        switch (profile.technique()) {
            case resonance -> { if (hit.heavy()) amount += statusBurst(target); }
            case crosscut -> { if (!hit.heavy() && hit.sameTarget() % 2 == 1) amount += 2; }
            case dice -> {
                int face = p.getPersistentData().getCompound(DICE).getInt("face");
                if (face == 1 || face == 6) amount *= 1.2;
                if ((face == 3 || face == 6) && hit.heavy()) amount += 2;
            }
            default -> { }
        }
        event.setAmount((float)amount);
    }
    private static int ticks(LivingEntity target, Holder<MobEffect> effect, int interval) {
        var e = target.getEffect(effect);
        if (e == null) return 0;
        if (e.isInfiniteDuration()) return 8;
        return Math.min(8, (e.getDuration() + interval - 1) / interval) * (1 + Math.min(e.getAmplifier(), 3));
    }
    private static int statusBurst(LivingEntity target) {
        return Math.min(8, (Math.max(0,target.getRemainingFireTicks()) + 19) / 20
                + ticks(target,MobEffects.POISON,25) + ticks(target,MobEffects.WITHER,40)
                + target.getPersistentData().getCompound(BLEED).getInt("ticks"));
    }
    @SubscribeEvent public static void landed(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer p)) return;
        var target = event.getEntity();
        var hit = pending(p, target, event.getSource());
        if (hit == null) return;
        PENDING.remove(p);
        if (event.getNewDamage() <= 0) return;
        var profile = ((TennoMeleeItem)p.getMainHandItem().getItem()).profile();
        int n = hit.chain() + 1;
        if (hit.heavy()) CHAINS.remove(p);
        else CHAINS.put(p, new Chain(hit.id(), hit.target(), n, hit.sameTarget() + 1, hit.tick()));
        switch (profile.technique()) {
            case dice -> {
                var die = p.getPersistentData().getCompound(DICE);
                int face = die.getInt("face");
                int count = die.getInt("hits");
                if (face == 0 || ++count >= 30) {
                    face = 1 + p.getRandom().nextInt(6);
                    die.putInt("face", face);
                    count = 0;
                    p.displayClientMessage(Component.literal("Amanata  " + face + "  ·  " + FACES[face]), true);
                }
                die.putInt("hits", count);
                p.getPersistentData().put(DICE, die);
                if ((face == 2 || face == 6) && (n % 3 == 0 || hit.heavy()))
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 1));
            }
            case reap -> {
                if (n % 3 == 0 || hit.heavy()) {
                    var bleed = new CompoundTag();
                    bleed.putInt("ticks",3);
                    bleed.putLong("next",hit.tick()+20);
                    bleed.putUUID("source",p.getUUID());
                    target.getPersistentData().put(BLEED,bleed);
                }
            }
            case resonance -> {
                if (hit.heavy() && statusBurst(target) > 0) {
                    target.clearFire();
                    target.removeEffect(MobEffects.POISON);
                    target.removeEffect(MobEffects.WITHER);
                    target.getPersistentData().remove(BLEED);
                    cue(p, target, "strand", VoxelCue.Kind.BURST, 1.1f);
                }
            }
            case ignition, embers -> {
                if (hit.heavy()) {
                    int seconds = profile.technique() == MeleeProfile.Technique.ignition ? 4 : 3;
                    target.igniteForSeconds(seconds);
                    double radius = profile.technique() == MeleeProfile.Technique.ignition ? 2.6 : 1.9;
                    for (var mob : p.serverLevel().getEntitiesOfClass(Mob.class, target.getBoundingBox().inflate(radius),
                            mob -> mob != target && mob.isAlive() && !mob.isAlliedTo(p)
                                && (mob instanceof Enemy || mob.getTarget() == p)
                                && target.distanceToSqr(mob) <= radius * radius && p.hasLineOfSight(mob)))
                        mob.igniteForSeconds(seconds);
                    cue(p, target, "solar", VoxelCue.Kind.RING, (float)radius);
                } else if (profile.technique() == MeleeProfile.Technique.embers && n % 2 == 0)
                    target.igniteForSeconds(2);
            }
            case crosscut -> {
                if (hit.sameTarget() > 0 && (hit.sameTarget() % 2 == 1 || hit.heavy()))
                    cue(p,target,"arc",VoxelCue.Kind.BURST,.55f);
            }
        }
        // Apply new heat only after Harmony cashes out the statuses present before this strike.
        double heat = WeaponMods.bonus(p,p.getMainHandItem(),ModStat.HEAT);
        if (heat > 0 && MeleeModEffects.acceptsHeat(target)) {
            target.igniteForSeconds((int)Math.ceil(heat * 5));
        }
    }
    @SubscribeEvent public static void bleed(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity victim) || !(victim.level() instanceof ServerLevel level)
                || !victim.getPersistentData().contains(BLEED)) return;
        var data = victim.getPersistentData().getCompound(BLEED);
        if (level.getGameTime() < data.getLong("next")) return;
        int remaining = data.getInt("ticks") - 1;
        if (remaining <= 0) victim.getPersistentData().remove(BLEED);
        else { data.putInt("ticks", remaining); data.putLong("next",level.getGameTime()+20); }
        var owner = data.hasUUID("source") ? level.getPlayerByUUID(data.getUUID("source")) : null;
        victim.hurt(owner == null ? victim.damageSources().magic() : victim.damageSources().indirectMagic(owner,owner),1);
    }
    @SubscribeEvent public static void blessings(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        var data = p.getPersistentData();
        if (!data.contains(DICE)) return;
        var held = p.getMainHandItem();
        int face = held.getItem() instanceof TennoMeleeItem weapon
                && weapon.profile().technique() == MeleeProfile.Technique.dice ? data.getCompound(DICE).getInt("face") : 0;
        modifier(p,Attributes.ENTITY_INTERACTION_RANGE,"tenno/amanata_reach",face == 4 || face == 6,.65);
        modifier(p,Attributes.ATTACK_SPEED,"tenno/amanata_tempo",face == 5 || face == 6,.35);
    }
    private static void modifier(ServerPlayer p, Holder<Attribute> attribute, String key, boolean active, double amount) {
        var instance = p.getAttribute(attribute);
        if (instance == null) return;
        var id = TennoMeleeItem.key(key);
        if (active && !instance.hasModifier(id)) instance.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
        if (!active && instance.hasModifier(id)) instance.removeModifier(id);
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void guard(LivingShieldBlockEvent event) {
        var wearer = event.getEntity();
        if (!event.getOriginalBlock() || !(wearer.getUseItem().getItem() instanceof TennoMeleeItem weapon)) return;
        // Retain the vanilla five-tick windup, shield-bypass tags, penetration, and impact durability.
        Vec3 origin = event.getDamageSource().getSourcePosition();
        if (origin == null) {event.setBlocked(false); return;}
        Vec3 direction = origin.subtract(wearer.position()).multiply(1,0,1).normalize();
        Vec3 forward = Vec3.directionFromRotation(0,wearer.getYHeadRot());
        if (direction.dot(forward) < Math.cos(Math.toRadians(weapon.profile().guardAngle() * .5))) {
            event.setBlocked(false);
        }
    }
    private static void cue(ServerPlayer player, LivingEntity target, String color, VoxelCue.Kind effect, float radius) {
        Vec3 at = target.position().add(0,target.getBbHeight()*.45,0);
        VoxelEffects.emit(player.serverLevel(),effect,EffectProfile.named(color,0),at,at,radius);
    }
}
