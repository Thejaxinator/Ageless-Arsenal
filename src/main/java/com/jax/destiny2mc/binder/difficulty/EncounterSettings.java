package com.jax.destiny2mc.binder.difficulty;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

/** All dimensions share the Overworld's difficulty record. Never mutate a client-side copy. */
public final class EncounterSettings extends SavedData {
    private EncounterPreset preset=EncounterPreset.PATROL;
    private int revision;
    public static EncounterSettings get(ServerLevel level){
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new Factory<>(EncounterSettings::new,(data,lookup)->load(data)),"ageless_encounter_settings");
    }
    private static EncounterSettings load(CompoundTag data){
        var settings=new EncounterSettings();var saved=EncounterPreset.find(data.getString("Preset"));
        if(saved!=null)settings.preset=saved;
        settings.revision=data.getInt("Revision");
        return settings;
    }
    public EncounterPreset preset(){return preset;}
    public int revision(){return revision;}
    public static boolean mayEdit(ServerPlayer player){
        return player.hasPermissions(2)||player.getServer().isSingleplayerOwner(player.getGameProfile());
    }
    /** Prevent simultaneous operators from silently overwriting one another's edits. */
    public boolean choose(ServerPlayer sender,String key,int expectedRevision){
        var next=EncounterPreset.find(key);
        if(next==null || !mayEdit(sender) || revision!=expectedRevision || preset==next)return false;
        preset=next;revision++;setDirty();return true;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){
        tag.putString("Preset",preset.id);tag.putInt("Revision",revision);return tag;
    }
}
