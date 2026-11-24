package com.jax.destiny2mc.client.sound;

import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TACZ-inspired layered client gun sound engine with OpenAL channel concurrency limits
 * and automatic custom Destiny .ogg override support.
 */
public final class GunSoundEngine {
    private static final int MAX_CONCURRENT_FIRE = 4;
    private static final int MAX_CONCURRENT_HIT = 3;
    private static final Map<String, ArrayDeque<GunSoundInstance>> ACTIVE = new HashMap<>();

    private GunSoundEngine() {}

    public static void playFire(LocalPlayer player, WeaponSpec spec, float aim) {
        if (player == null) return;
        var mc = Minecraft.getInstance();
        float jitter = (player.getRandom().nextFloat() - 0.5f) * 0.09f;

        ResourceLocation exactId = ResourceLocation.fromNamespaceAndPath("destiny2mc", "weapon." + spec.id + ".fire");
        if (hasSoundEvent(mc, exactId)) {
            playCapped(player, exactId, 0.92f, 1.0f + jitter * 0.6f, 96, true, MAX_CONCURRENT_FIRE);
            return;
        }
        String archKey = archetypeSoundKey(spec);
        ResourceLocation archId = ResourceLocation.fromNamespaceAndPath("destiny2mc", "weapon.archetype." + archKey + ".fire");
        if (hasSoundEvent(mc, archId)) {
            playCapped(player, archId, 0.88f, 1.0f + jitter * 0.7f, 96, true, MAX_CONCURRENT_FIRE);
            return;
        }

        float adsTighten = 1.0f + aim * 0.04f;
        switch (spec.archetype) {
            case 1 -> {
                boolean sidearm = spec.rpm >= 200;
                playCapped(player, mcId("entity.firework_rocket.blast"), sidearm ? 0.58f : 0.82f, (sidearm ? 1.55f : 1.08f) * adsTighten + jitter, 80, true, MAX_CONCURRENT_FIRE);
                playCapped(player, mcId("block.netherite_block.hit"), sidearm ? 0.45f : 0.65f, (sidearm ? 1.85f : 1.35f) + jitter, 48, true, MAX_CONCURRENT_FIRE);
                playCapped(player, mcId("item.flintandsteel.use"), 0.32f, 1.65f + jitter, 24, true, 2);
            }
            case 2 -> {
                boolean sniper = spec.fovZoom <= 0.44f;
                playCapped(player, mcId("entity.firework_rocket.large_blast"), sniper ? 0.95f : 0.72f, (sniper ? 0.72f : 1.25f) + jitter * 0.6f, 128, true, MAX_CONCURRENT_FIRE);
                playCapped(player, mcId("entity.generic.explode"), sniper ? 0.38f : 0.16f, (sniper ? 1.55f : 1.9f) + jitter, 128, true, 2);
                playCapped(player, mcId("block.anvil.land"), sniper ? 0.22f : 0.12f, 1.95f + jitter, 40, true, 2);
            }
            case 3 -> {
                playCapped(player, mcId("entity.generic.explode"), 0.55f, 1.18f + jitter, 96, true, MAX_CONCURRENT_FIRE);
                playCapped(player, mcId("entity.firework_rocket.large_blast"), 0.78f, 0.85f + jitter, 96, true, MAX_CONCURRENT_FIRE);
                playCapped(player, mcId("item.shield.block"), 0.30f, 1.55f + jitter, 32, true, 2);
            }
            case 4, 5 -> {
                playCapped(player, mcId("entity.warden.sonic_boom"), 0.28f, (spec.archetype == 5 ? 1.85f : 1.55f) + jitter, 96, true, 3);
                playCapped(player, mcId("entity.firework_rocket.blast"), 0.72f, 1.32f + jitter, 80, true, MAX_CONCURRENT_FIRE);
            }
            case 6 -> {
                playCapped(player, mcId("block.beacon.ambient"), 0.42f, 1.88f + jitter * 0.4f, 48, true, 2);
                playCapped(player, mcId("entity.firework_rocket.twinkle"), 0.28f, 1.75f + jitter, 48, true, 2);
            }
            case 7 -> {
                playCapped(player, mcId("entity.firework_rocket.launch"), 0.95f, 0.68f + jitter, 110, true, 3);
                playCapped(player, mcId("entity.generic.explode"), 0.45f, 0.92f + jitter, 110, true, 2);
            }
            case 8 -> {
                playCapped(player, mcId("entity.firework_rocket.blast"), 0.75f, 0.62f + jitter, 80, true, 3);
                playCapped(player, mcId("block.piston.extend"), 0.55f, 0.82f + jitter, 32, true, 2);
            }
            case 9 -> {
                playCapped(player, mcId("entity.arrow.shoot"), 0.90f, 1.15f + jitter, 48, true, 3);
                playCapped(player, mcId("block.amethyst_block.chime"), 0.32f, 1.65f + jitter, 32, true, 2);
            }
            default -> {
                boolean heavyMg = spec.magSize >= 55;
                boolean smg = spec.rpm >= 600 && spec.range <= 48.0f;
                float pitch = heavyMg ? 0.88f : (smg ? 1.62f : 1.28f);
                playCapped(player, mcId("entity.firework_rocket.blast"), heavyMg ? 0.82f : 0.62f, pitch * adsTighten + jitter, 88, true, MAX_CONCURRENT_FIRE);
                playCapped(player, mcId("block.copper.hit"), heavyMg ? 0.45f : 0.30f, (pitch + 0.35f) + jitter, 32, true, 3);
            }
        }
    }

    public static void playAdsToggle(LocalPlayer player, boolean entering, boolean sniper) {
        if (player == null) return;
        var mc = Minecraft.getInstance();
        ResourceLocation custom = ResourceLocation.fromNamespaceAndPath("destiny2mc", entering ? "weapon.ads_in" : "weapon.ads_out");
        if (hasSoundEvent(mc, custom)) {
            playCapped(player, custom, 0.45f, 1.0f, 16, true, 2);
            return;
        }
        float pitch = entering ? (sniper ? 1.35f : 1.55f) : (sniper ? 0.95f : 1.15f);
        playCapped(player, mcId("item.armor.equip_iron"), sniper ? 0.22f : 0.14f, pitch, 16, true, 2);
    }

    public static void playHitFeedback(LocalPlayer player, boolean crit, boolean kill) {
        if (player == null) return;
        var mc = Minecraft.getInstance();
        if (crit) {
            ResourceLocation custom = ResourceLocation.fromNamespaceAndPath("destiny2mc", "weapon.hit_crit");
            if (hasSoundEvent(mc, custom)) {
                playCapped(player, custom, 0.75f, 1.0f, 16, true, MAX_CONCURRENT_HIT);
            } else {
                playCapped(player, mcId("entity.arrow.hit_player"), 0.52f, 1.92f, 16, true, MAX_CONCURRENT_HIT);
                playCapped(player, mcId("block.amethyst_cluster.hit"), 0.35f, 1.85f, 16, true, 2);
            }
        } else {
            ResourceLocation custom = ResourceLocation.fromNamespaceAndPath("destiny2mc", "weapon.hit_body");
            if (hasSoundEvent(mc, custom)) {
                playCapped(player, custom, 0.55f, 1.0f, 16, true, MAX_CONCURRENT_HIT);
            } else {
                playCapped(player, mcId("block.note_block.hat"), 0.34f, 1.85f, 16, true, MAX_CONCURRENT_HIT);
            }
        }
        if (kill) {
            ResourceLocation killId = ResourceLocation.fromNamespaceAndPath("destiny2mc", "weapon.kill_confirm");
            if (hasSoundEvent(mc, killId)) {
                playCapped(player, killId, 0.75f, 1.0f, 16, true, 2);
            } else {
                playCapped(player, mcId("block.amethyst_block.chime"), 0.45f, 1.48f, 16, true, 2);
            }
        }
    }

    private static void playCapped(Entity entity, ResourceLocation soundId, float volume, float pitch,
                                   int maxDistance, boolean relative, int limit) {
        var mc = Minecraft.getInstance();
        var sm = mc.getSoundManager();
        String key = entity.getId() + "|" + soundId;
        ArrayDeque<GunSoundInstance> queue = ACTIVE.computeIfAbsent(key, k -> new ArrayDeque<>());
        Iterator<GunSoundInstance> it = queue.iterator();
        while (it.hasNext()) {
            if (!sm.isActive(it.next())) it.remove();
        }
        while (queue.size() >= limit) {
            GunSoundInstance oldest = queue.pollFirst();
            if (oldest != null) oldest.stopNow();
        }
        GunSoundInstance instance = new GunSoundInstance(
                SoundEvent.createVariableRangeEvent(soundId),
                SoundSource.PLAYERS, volume, pitch, entity, maxDistance, relative
        );
        sm.play(instance);
        queue.addLast(instance);
    }

    private static boolean hasSoundEvent(Minecraft mc, ResourceLocation id) {
        return mc.getSoundManager().getSoundEvent(id) != null;
    }

    private static ResourceLocation mcId(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    private static String archetypeSoundKey(WeaponSpec spec) {
        return switch (spec.archetype) {
            case 1 -> spec.rpm >= 200 ? "sidearm" : "hand_cannon";
            case 2 -> spec.fovZoom <= 0.44f ? "sniper_rifle" : "scout_rifle";
            case 3 -> "shotgun";
            case 4 -> "fusion_rifle";
            case 5 -> "linear_fusion";
            case 6 -> "trace_rifle";
            case 7 -> "rocket_launcher";
            case 8 -> "grenade_launcher";
            case 9 -> "bow";
            default -> spec.burstCount > 1 ? "pulse_rifle"
                    : (spec.magSize >= 55 ? "machine_gun" : (spec.range <= 48.0f ? "smg" : "auto_rifle"));
        };
    }
}
