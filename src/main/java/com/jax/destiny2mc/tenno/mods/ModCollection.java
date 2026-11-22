package com.jax.destiny2mc.tenno.mods;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** One rank for each unlocked card on its owner's character. Clients receive display-only copies. */
@EventBusSubscriber(modid = "destiny2mc")
public final class ModCollection {
    private static final String KEY = "AgelessWarframeMods";
    public static final int LOCKED = -1;

    private ModCollection() {}

    private static CompoundTag ledger(Player player) {
        return player.getPersistentData().getCompound(KEY);
    }

    private static int readRank(CompoundTag data, WeaponMod mod) {
        if (mod == null || !data.contains(mod.id(), Tag.TAG_INT)) return LOCKED;
        int stored = data.getInt(mod.id());
        return stored < 0 ? LOCKED : Math.min(stored, mod.maxRank());
    }

    /** Rank zero is an owned card with its first effect. A missing or invalid card returns -1. */
    public static int rank(Player player, String id) {
        if (player == null || id == null) return LOCKED;
        return readRank(ledger(player), WeaponMod.byId(id));
    }

    public static Map<String, Integer> ranks(Player player) {
        if (player == null) return Map.of();
        var all = new LinkedHashMap<String, Integer>();
        var data = ledger(player);
        for (WeaponMod mod : WeaponMod.values()) {
            int rank = readRank(data, mod);
            if (rank >= 0) all.put(mod.id(), rank);
        }
        return Map.copyOf(all);
    }

    /** The next rank costs its rank number in vanilla levels, e.g. 0 -> 1 costs 1. */
    public static int rankCost(int currentRank) { return currentRank + 1; }

    /** The bench validates station access before invoking this compare-and-set on the server thread. */
    public static boolean rankUp(ServerPlayer player, String id, int expectedRank) {
        if (id == null || expectedRank < 0 || player.isSpectator() || !player.isAlive()) return false;
        var mod = WeaponMod.byId(id);
        var data = ledger(player);
        if (mod == null || expectedRank >= mod.maxRank() || readRank(data, mod) != expectedRank) return false;
        int cost = rankCost(expectedRank);
        if (player.experienceLevel < cost) return false;
        player.giveExperienceLevels(-cost);
        data.putInt(id, expectedRank + 1);
        player.getPersistentData().put(KEY, data);
        sync(player);
        return true;
    }

    /** Only an actual held pack calls this method. No unlock operation is exposed as a client request. */
    static boolean unlock(ServerPlayer player, WeaponMod mod) {
        var data = ledger(player);
        if (readRank(data, mod) != LOCKED) return false;
        data.putInt(mod.id(), 0);
        player.getPersistentData().put(KEY, data);
        return true;
    }

    static void receive(Player owner, Map<String, Integer> ranks) {
        if (!owner.level().isClientSide()) return;
        var view = new CompoundTag();
        for (var entry : ranks.entrySet()) {
            var mod = WeaponMod.byId(entry.getKey());
            if (mod != null && entry.getValue() >= 0 && entry.getValue() <= mod.maxRank())
                view.putInt(mod.id(), entry.getValue());
        }
        owner.getPersistentData().put(KEY, view);
    }

    static void sync(ServerPlayer player) { sync(player, ""); }

    static void sync(ServerPlayer player, String revealedId) {
        if (!(player instanceof FakePlayer))
            PacketDistributor.sendToPlayer(player, new ModCollectionSync(ranks(player), revealedId));
    }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        var old = event.getOriginal().getPersistentData();
        if (old.contains(KEY, Tag.TAG_COMPOUND))
            event.getEntity().getPersistentData().put(KEY, old.getCompound(KEY).copy());
    }

    @SubscribeEvent public static void joined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    @SubscribeEvent public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    @SubscribeEvent public static void traveled(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
}
