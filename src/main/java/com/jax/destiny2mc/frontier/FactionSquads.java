package com.jax.destiny2mc.frontier;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import com.jax.destiny2mc.binder.FactionEnemies;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Coordinates squad-based natural spawning (1 leader + 2-5 smaller units) and
 * staggered faction-themed materialization animations across all 7 factions.
 */
@EventBusSubscriber(modid = "destiny2mc")
public final class FactionSquads {
    public static final String SQUAD_LEADER_TAG = "AgelessSquadLeader";
    public static final String SQUAD_MEMBER_TAG = "AgelessSquadMember";
    public static final int MIN_MINIONS = 2;
    public static final int MAX_MINIONS = 5;
    private static final int STAGGER_TICKS = 4;
    private static final int MAX_PENDING = 256;

    public record SquadRoster(
        String faction,
        EffectProfile fx,
        String sound,
        float soundPitch,
        boolean orbitalDropBeam,
        List<String> leaders,
        List<String> minions
    ) {
        public boolean isLeader(String id) {
            return leaders.contains(id);
        }

        public boolean isMinion(String id) {
            return minions.contains(id);
        }

        public String pickLeader(RandomSource random) {
            return leaders.get(random.nextInt(leaders.size()));
        }

        public String pickMinion(RandomSource random) {
            return minions.get(random.nextInt(minions.size()));
        }
    }

    public static final List<SquadRoster> ROSTERS = List.of(
        new SquadRoster(
            "fallen",
            EffectProfile.ETHER,
            "entity.enderman.teleport",
            1.25f,
            true,
            List.of("fallen_captain_rifle", "fallen_captain_scorch", "fallen_bruiser", "devils_servitor", "heavy_shank"),
            List.of("fallen_dreg", "fallen_wretch", "fallen_vandal_sniper", "fallen_shank", "tracer_shank", "exploder_shank")
        ),
        new SquadRoster(
            "hive",
            EffectProfile.SOULFIRE,
            "block.respawn_anchor.charge",
            0.75f,
            false,
            List.of("hive_knight", "hive_wizard", "hive_ogre", "lucent_knight"),
            List.of("hive_thrall", "hive_cursed_thrall", "hive_acolyte")
        ),
        new SquadRoster(
            "vex",
            EffectProfile.ARC,
            "block.beacon.activate",
            1.55f,
            true,
            List.of("vex_minotaur", "vex_hydra", "vex_wyvern"),
            List.of("vex_goblin", "vex_hobgoblin", "vex_harpy")
        ),
        new SquadRoster(
            "cabal",
            EffectProfile.SOLAR,
            "entity.blaze.shoot",
            0.65f,
            true,
            List.of("cabal_centurion", "cabal_colossus", "cabal_incendior"),
            List.of("cabal_legionary", "cabal_phalanx", "cabal_psion", "cabal_warbeast")
        ),
        new SquadRoster(
            "dread",
            EffectProfile.RESONANCE,
            "block.amethyst_block.chime",
            0.55f,
            true,
            List.of("dread_omen", "dread_harbinger", "dread_tormentor"),
            List.of("dread_grim", "dread_husk", "dread_attendant", "dread_weaver")
        ),
        new SquadRoster(
            "scorn",
            EffectProfile.DARK_ETHER,
            "entity.phantom.swoop",
            0.75f,
            false,
            List.of("scorn_chieftain", "scorn_abomination"),
            List.of("scorn_stalker", "scorn_raider", "scorn_lurker", "scorn_ravager", "scorn_screeb")
        ),
        new SquadRoster(
            "taken",
            EffectProfile.TAKEN,
            "entity.enderman.teleport",
            0.60f,
            false,
            List.of("taken_captain", "taken_knight", "taken_phalanx"),
            List.of("taken_thrall", "taken_acolyte", "taken_goblin")
        )
    );

    private static final Map<String, SquadRoster> BY_UNIT = buildIndex();

    private static Map<String, SquadRoster> buildIndex() {
        var map = new java.util.HashMap<String, SquadRoster>();
        for (SquadRoster roster : ROSTERS) {
            for (String id : roster.leaders()) map.put(id, roster);
            for (String id : roster.minions()) map.put(id, roster);
        }
        return Map.copyOf(map);
    }

    public static SquadRoster rosterFor(String unitPath) {
        return BY_UNIT.get(unitPath);
    }

    public static Set<String> allSquadUnits() {
        return BY_UNIT.keySet();
    }

    private static final class PendingMember {
        final ServerLevel level;
        final BlockPos anchor;
        final SquadRoster roster;
        final String unitId;
        final boolean asLeader;
        final float yaw;
        int ticksRemaining;

        PendingMember(
            ServerLevel level,
            BlockPos anchor,
            SquadRoster roster,
            String unitId,
            boolean asLeader,
            float yaw,
            int ticksRemaining
        ) {
            this.level = level;
            this.anchor = anchor;
            this.roster = roster;
            this.unitId = unitId;
            this.asLeader = asLeader;
            this.yaw = yaw;
            this.ticksRemaining = ticksRemaining;
        }
    }

    private static final List<PendingMember> PENDING = new ArrayList<>();

    private FactionSquads() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (!(mob.level() instanceof ServerLevel level)) return;
        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.STRUCTURE) return;
        if (!FactionEnemies.isEnemy(mob) || FactionEnemies.isBoss(mob)) return;
        if (mob.getPersistentData().getBoolean(SQUAD_MEMBER_TAG)
            || mob.getPersistentData().getBoolean(SQUAD_LEADER_TAG)) return;

        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (key == null || !"destiny2mc".equals(key.getNamespace())) return;
        SquadRoster roster = rosterFor(key.getPath());
        if (roster == null) return;

        RandomSource random = mob.getRandom();
        boolean spawnedAsLeader = roster.isLeader(key.getPath());
        mob.getPersistentData().putBoolean(spawnedAsLeader ? SQUAD_LEADER_TAG : SQUAD_MEMBER_TAG, true);
        applyMaterializePose(mob, spawnedAsLeader ? 16 : 12);
        playSpawnAnimation(level, mob.position(), roster, spawnedAsLeader);

        // Always form 1 leader + 2-5 smaller units total.
        int minionCount = MIN_MINIONS + random.nextInt(MAX_MINIONS - MIN_MINIONS + 1);
        BlockPos anchor = mob.blockPosition();
        float yaw = mob.getYRot();
        int step = 1;

        if (!spawnedAsLeader) {
            // If a structure override spawned a minion first, warp in its leader immediately next,
            // then remaining smaller units so the squad has 1 leader + 2-5 smaller units.
            enqueue(new PendingMember(level, anchor, roster, roster.pickLeader(random), true, yaw, step * STAGGER_TICKS));
            step++;
            minionCount = Math.max(1, minionCount - 1);
        }

        for (int i = 0; i < minionCount; i++) {
            String minionId = roster.pickMinion(random);
            enqueue(new PendingMember(level, anchor, roster, minionId, false, yaw, step * STAGGER_TICKS));
            step++;
        }
    }

    private static void enqueue(PendingMember member) {
        if (PENDING.size() >= MAX_PENDING) return;
        PENDING.add(member);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) return;
        Iterator<PendingMember> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingMember next = it.next();
            if (--next.ticksRemaining > 0) continue;
            it.remove();
            materializeMember(next);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }

    private static void materializeMember(PendingMember next) {
        ServerLevel level = next.level;
        if (!level.isLoaded(next.anchor)) return;
        ResourceLocation id = FrontierContent.id(next.unitId);
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) return;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        Entity created = type.create(level);
        if (!(created instanceof Mob mob)) return;

        Vec3 spawnPos = findSquadSpot(level, next.anchor, mob, level.random);
        mob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, next.yaw + (level.random.nextFloat() - 0.5f) * 60f, 0f);
        mob.getPersistentData().putBoolean(next.asLeader ? SQUAD_LEADER_TAG : SQUAD_MEMBER_TAG, true);
        applyMaterializePose(mob, next.asLeader ? 16 : 10);

        if (level.addFreshEntity(mob)) {
            playSpawnAnimation(level, spawnPos, next.roster, next.asLeader);
        }
    }

    /**
     * Brief materialize settle so units animate in cleanly rather than snapping immediately.
     * Uses short timed effects (never permanent NoAI) so chunk unloads cannot freeze mobs.
     */
    private static void applyMaterializePose(Mob mob, int ticks) {
        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 4, false, false, false));
        mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 2, false, false, false));
    }

    /**
     * Emits faction-themed voxel spawn animations (transmat beam or warp burst, ground ring,
     * arrival column, and faction arrival audio) via the mod's native VoxelEffects pipeline.
     */
    public static void playSpawnAnimation(ServerLevel level, Vec3 pos, SquadRoster roster, boolean leader) {
        EffectProfile profile = roster.fx();
        Vec3 center = pos.add(0.0, 0.2, 0.0);
        float ringRadius = leader ? 2.1f : 1.25f;

        VoxelEffects.emit(level, VoxelCue.Kind.RING, profile, center, center, ringRadius);
        VoxelEffects.emit(level, VoxelCue.Kind.BLINK_IN, profile, pos, pos, leader ? 1.5f : 1.0f);

        if (roster.orbitalDropBeam()) {
            Vec3 skyStart = pos.add(0.0, leader ? 7.5 : 4.5, 0.0);
            VoxelEffects.emit(level, VoxelCue.Kind.TRACE, profile, skyStart, center, leader ? 1.3f : 0.85f);
            if (leader) {
                VoxelEffects.emit(level, VoxelCue.Kind.BURST, profile, center, center, 1.35f);
            }
        } else {
            VoxelEffects.emit(level, VoxelCue.Kind.BURST, profile, pos.add(0.0, 0.9, 0.0), pos.add(0.0, 0.9, 0.0), leader ? 1.4f : 0.9f);
            if (leader) {
                VoxelEffects.emit(level, VoxelCue.Kind.CLOUD, profile, center, center, 1.6f);
            }
        }

        float volume = leader ? 0.65f : 0.35f;
        float pitch = roster.soundPitch() + (leader ? 0.0f : 0.12f);
        ServerBallisticsEngine.playSound(level, pos.x, pos.y, pos.z, roster.sound(), volume, pitch);
    }

    private static Vec3 findSquadSpot(ServerLevel level, BlockPos anchor, Mob mob, RandomSource random) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 2.2 + random.nextDouble() * 3.3;
            int x = anchor.getX() + (int) Math.round(Math.cos(angle) * dist);
            int z = anchor.getZ() + (int) Math.round(Math.sin(angle) * dist);
            for (int dy = 3; dy >= -3; dy--) {
                BlockPos candidate = new BlockPos(x, anchor.getY() + dy, z);
                if (!level.isLoaded(candidate)) continue;
                if (!level.getBlockState(candidate.below()).isSolidRender(level, candidate.below())) continue;
                mob.moveTo(candidate.getX() + 0.5, candidate.getY() + 0.05, candidate.getZ() + 0.5, 0f, 0f);
                if (level.noCollision(mob)) {
                    return new Vec3(candidate.getX() + 0.5, candidate.getY() + 0.05, candidate.getZ() + 0.5);
                }
            }
        }
        return new Vec3(anchor.getX() + 0.5, anchor.getY() + 0.05, anchor.getZ() + 0.5);
    }
}
