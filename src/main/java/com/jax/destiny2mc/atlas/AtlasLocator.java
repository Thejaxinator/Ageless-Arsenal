package com.jax.destiny2mc.atlas;

import com.jax.destiny2mc.frontier.FrontierContent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "destiny2mc"
)
public final class AtlasLocator {
   public static final int RANGE = 4096;
   public static final int MAX_CANDIDATES = 128;
   private static final LinkedHashMap<UUID, AtlasLocator.Job> JOBS = new LinkedHashMap<>();
   private static final Map<UUID, Long> COOLDOWN = new HashMap<>();
   private static final Map<UUID, Long> MESSAGES = new HashMap<>();
   private static final Map<UUID, String> LAST_REQUEST = new HashMap<>();
   private static AtlasLocator.Job inFlight;

   private static CompoundTag state(ServerPlayer p) {
      CompoundTag root = p.getPersistentData();
      if (!root.contains("AgelessArsenals")) {
         root.put("AgelessArsenals", new CompoundTag());
      }

      return root.getCompound("AgelessArsenals");
   }

   private static void send(ServerPlayer p, String id, String status, String message) {
      PacketDistributor.sendToPlayer(
         p, new AtlasNetwork.Reply(id, status, 0, 0, 0, p.level().dimension().location().toString(), message, false), new CustomPacketPayload[0]
      );
   }

   public static void request(ServerPlayer p, AtlasNetwork.Request r) {
      if (r.action() >= 0 && r.action() <= 4 && !p.isSpectator()) {
         long now = p.level().getGameTime();
         String requestKey = r.action() + ":" + r.site();
         if (r.action() == 0 || !requestKey.equals(LAST_REQUEST.get(p.getUUID())) || MESSAGES.getOrDefault(p.getUUID(), -4611686018427387904L) + 5L <= now) {
            MESSAGES.put(p.getUUID(), now);
            LAST_REQUEST.put(p.getUUID(), requestKey);
            if (r.action() == 3) {
               sync(p);
            } else if (r.action() == 2) {
               state(p).remove("Track");
               send(p, "", "cleared", "Waypoint cleared.");
            } else if (r.action() == 4) {
               cancel(p.getUUID());
               send(p, r.site(), "cancelled", "Scan cancelled.");
            } else if (!DungeonSites.locatable(r.site())) {
               send(p, r.site(), "unavailable", "Unknown structure.");
            } else if (r.action() == 1) {
               CompoundTag cached = state(p).getCompound(r.site());
               if (!cached.contains("Position")) {
                  send(p, r.site(), "unavailable", "Locate this structure first.");
               } else {
                  state(p).putString("Track", r.site());
                  sendCached(p, r.site(), cached, true);
               }
            } else if (p.level().dimension() != Level.OVERWORLD) {
               send(p, r.site(), "wrong_dimension", "These structures generate in the Overworld. Return there to scan.");
            } else if (!p.serverLevel().getServer().getWorldData().worldGenOptions().generateStructures()) {
               send(p, r.site(), "unavailable", "Structure generation is disabled in this world.");
            } else {
               long next = COOLDOWN.getOrDefault(p.getUUID(), 0L);
               if (now < next) {
                  send(p, r.site(), "cooldown", "Scanner recharges in " + (next - now + 19L) / 20L + " seconds.");
               } else if (JOBS.containsKey(p.getUUID())) {
                  send(p, r.site(), "busy", "A scan is already active. Cancel it before starting another.");
               } else if (JOBS.size() >= 8) {
                  send(p, r.site(), "busy", "The scanner queue is full. Try again shortly.");
               } else {
                  Registry<Structure> registry = p.registryAccess().registryOrThrow(Registries.STRUCTURE);
                  Optional<Reference<Structure>> holder = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, FrontierContent.id(r.site())));
                  if (holder.isEmpty()) {
                     send(p, r.site(), "unavailable", "This world's data packs do not contain that structure.");
                  } else {
                     ChunkGeneratorStructureState structureState = p.serverLevel().getChunkSource().getGeneratorState();
                     List<AtlasLocator.Candidate> candidates = new ArrayList<>();
                     Set<Long> seen = new HashSet<>();

                     for (StructurePlacement placement : structureState.getPlacementsForStructure(holder.get())) {
                        if (placement instanceof RandomSpreadStructurePlacement) {
                           RandomSpreadStructurePlacement spread = (RandomSpreadStructurePlacement)placement;
                           int span = (int)Math.ceil(4096.0 / ((double)spread.spacing() * 16.0)) + 1;
                           ChunkPos origin = p.chunkPosition();

                           for (int dx = -span; dx <= span; dx++) {
                              for (int dz = -span; dz <= span; dz++) {
                                 ChunkPos c = spread.getPotentialStructureChunk(
                                    p.serverLevel().getSeed(), origin.x + dx * spread.spacing(), origin.z + dz * spread.spacing()
                                 );
                                 if (!(c.getMiddleBlockPosition(64).distSqr(new BlockPos(p.getBlockX(), 64, p.getBlockZ())) > 1.6777216E7)
                                    && p.serverLevel().getWorldBorder().isWithinBounds(c)
                                    && seen.add(c.toLong())
                                    && spread.isStructureChunk(structureState, c.x, c.z)) {
                                    candidates.add(new AtlasLocator.Candidate(c, spread));
                                 }
                              }
                           }
                        }
                     }

                     candidates.sort(Comparator.comparingDouble(cx -> cx.chunk.getMiddleBlockPosition(p.getBlockY()).distSqr(p.blockPosition())));
                     if (candidates.size() > 128) {
                        candidates = new ArrayList<>(candidates.subList(0, 128));
                     }

                     if (candidates.isEmpty()) {
                        send(p, r.site(), "not_found", "No eligible generation sites within 4,096 blocks. Travel farther into suitable biomes.");
                     } else {
                        JOBS.put(p.getUUID(), new AtlasLocator.Job(p, r.site(), (Holder<Structure>)holder.get(), candidates));
                        COOLDOWN.put(p.getUUID(), now + 300L);
                        send(p, r.site(), "scanning", "Scanning nearby generation sites...");
                     }
                  }
               }
            }
         }
      }
   }

   private static void sendCached(ServerPlayer p, String id, CompoundTag t, boolean track) {
      BlockPos pos = BlockPos.of(t.getLong("Position"));
      PacketDistributor.sendToPlayer(
         p,
         new AtlasNetwork.Reply(
            id,
            "found",
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            t.getString("Dimension"),
            track ? "Waypoint tracked. Follow the HUD bearing." : "Confirmed structure. Travel here to explore.",
            track
         ),
         new CustomPacketPayload[0]
      );
   }

   public static void sync(ServerPlayer p) {
      String tracked = state(p).getString("Track");
      send(p, "", "cleared", "");

      for (String id : state(p).getAllKeys()) {
         CompoundTag t = state(p).getCompound(id);
         if (DungeonSites.locatable(id) && t.contains("Position")) {
            sendCached(p, id, t, id.equals(tracked));
         }
      }
   }

   private static void found(AtlasLocator.Job j, ServerPlayer p, StructureStart start) {
      BlockPos at = start.getBoundingBox().getCenter();
      CompoundTag t = new CompoundTag();
      t.putLong("Position", at.asLong());
      t.putString("Dimension", j.level.dimension().location().toString());
      state(p).put(j.id, t);
      sendCached(p, j.id, t, j.id.equals(state(p).getString("Track")));
      JOBS.remove(j.player);
   }

   private static void cancel(UUID id) {
      AtlasLocator.Job j = JOBS.remove(id);
      if (j != null && inFlight == j) {
      }
   }

   @SubscribeEvent
   public static void tick(Post e) {
      if (inFlight != null) {
         AtlasLocator.Job j = inFlight;
         if (!j.future.isDone()) {
            return;
         }

         inFlight = null;
         ServerPlayer p = e.getServer().getPlayerList().getPlayer(j.player);
         if (p != null && JOBS.get(j.player) == j && p.level() == j.level) {
            try {
               ChunkAccess chunk = (ChunkAccess)j.future.join().orElse(null);
               if (chunk != null) {
                  StructureStart start = j.level.structureManager().getStartForStructure(SectionPos.bottomOf(chunk), (Structure)j.structure.value(), chunk);
                  if (start != null && start.isValid()) {
                     found(j, p, start);
                     return;
                  }
               }
            } catch (RuntimeException var7) {
               send(p, j.id, "unavailable", "The world could not read this structure start. Try another scan.");
               JOBS.remove(j.player);
            }
         }

         j.future = null;
      }

      int budget = 8;

      for (AtlasLocator.Job jx : new ArrayList<>(JOBS.values())) {
         ServerPlayer p = e.getServer().getPlayerList().getPlayer(jx.player);
         if (p == null || p.level() != jx.level) {
            JOBS.remove(jx.player);
         } else if (jx.level.getGameTime() - jx.started <= 600L && !jx.candidates.isEmpty()) {
            while (!jx.candidates.isEmpty() && budget-- > 0) {
               AtlasLocator.Candidate candidate = jx.candidates.removeFirst();
               jx.checked++;
               StructureCheckResult result = jx.level
                  .structureManager()
                  .checkStructurePresence(candidate.chunk, (Structure)jx.structure.value(), candidate.placement, false);
               if (result != StructureCheckResult.START_NOT_PRESENT) {
                  jx.current = candidate;
                  jx.future = jx.level.getChunkSource().getChunkFuture(candidate.chunk.x, candidate.chunk.z, ChunkStatus.STRUCTURE_STARTS, true);
                  inFlight = jx;
                  if (jx.checked % 8 == 1) {
                     send(p, jx.id, "scanning", "Checking site " + jx.checked + " / " + (jx.checked + jx.candidates.size()) + "...");
                  }

                  return;
               }
            }

            if (budget <= 0) {
               return;
            }
         } else {
            send(p, jx.id, "not_found", "No confirmed site in this scan. Explore new chunks in the listed biomes, then scan again.");
            JOBS.remove(jx.player);
         }
      }
   }

   @SubscribeEvent
   public static void logout(PlayerLoggedOutEvent e) {
      cancel(e.getEntity().getUUID());
      COOLDOWN.remove(e.getEntity().getUUID());
      MESSAGES.remove(e.getEntity().getUUID());
      LAST_REQUEST.remove(e.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void login(PlayerLoggedInEvent e) {
      if (e.getEntity() instanceof ServerPlayer p) {
         sync(p);
      }
   }

   @SubscribeEvent
   public static void clonePlayer(Clone e) {
      if (e.getOriginal().getPersistentData().contains("AgelessArsenals")) {
         e.getEntity().getPersistentData().put("AgelessArsenals", e.getOriginal().getPersistentData().getCompound("AgelessArsenals").copy());
      }
   }

   @SubscribeEvent
   public static void stopped(ServerStoppedEvent e) {
      JOBS.clear();
      COOLDOWN.clear();
      MESSAGES.clear();
      LAST_REQUEST.clear();
      inFlight = null;
   }

   private static record Candidate(ChunkPos chunk, RandomSpreadStructurePlacement placement) {
   }

   private static final class Job {
      final UUID player;
      final ServerLevel level;
      final String id;
      final Holder<Structure> structure;
      final ArrayDeque<AtlasLocator.Candidate> candidates;
      final long started;
      int checked;
      CompletableFuture<ChunkResult<ChunkAccess>> future;
      AtlasLocator.Candidate current;

      Job(ServerPlayer p, String id, Holder<Structure> s, List<AtlasLocator.Candidate> c) {
         this.player = p.getUUID();
         this.level = p.serverLevel();
         this.id = id;
         this.structure = s;
         this.candidates = new ArrayDeque<>(c);
         this.started = this.level.getGameTime();
      }
   }
}
