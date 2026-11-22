package com.jax.destiny2mc.atlas;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** Only completion cooldowns are world data; active encounter state lives on saved entities. */
public final class DungeonMemory extends SavedData {
 private final CompoundTag cooldowns;
 public DungeonMemory(){cooldowns=new CompoundTag();}
 private DungeonMemory(CompoundTag tag){cooldowns=tag.getCompound("Cooldowns").copy();}
 public static DungeonMemory get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new Factory<>(DungeonMemory::new,(t,p)->new DungeonMemory(t)),"destiny_atlas_dungeons");}
 public long readyAt(BlockPos pos){return cooldowns.getLong(Long.toString(pos.asLong()));}
 public void complete(BlockPos pos,long now){cooldowns.putLong(Long.toString(pos.asLong()),now+6000);setDirty();}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){tag.put("Cooldowns",cooldowns);return tag;}
}
