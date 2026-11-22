package com.jax.destiny2mc.ageless;

import com.google.gson.*;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.network.PacketDistributor;

/** Two immutable snapshots deliberately remain separate for an integrated server. The server is
 * authoritative for shots. The client receives the same programs to predict trigger and charge UI. */
@EventBusSubscriber(modid="destiny2mc")
public final class WeaponPrograms {
    private record Snapshot(Map<String,WeaponProgram> map,String wire) {}
    private static final Snapshot DEFAULTS=defaults();
    private static volatile Snapshot server=DEFAULTS, client=DEFAULTS;
    public static WeaponProgram server(WeaponSpec w){return server.map().get(w.id);}
    public static WeaponProgram client(WeaponSpec w){return client.map().get(w.id);}
    private static Snapshot defaults(){
        var rows=new JsonObject();
        for(var s:WeaponSpec.ALL)if(s.index==40||s.index>=52)rows.add(s.id,PackagedWeapons.read("weapon_programs/"+s.id));
        return snapshot(rows);
    }
    private static Snapshot snapshot(JsonObject rows){
        var map=new LinkedHashMap<String,WeaponProgram>();
        var specs=new HashMap<String,WeaponSpec>();for(var w:WeaponSpec.ALL)specs.put(w.id,w);
        for(var row:rows.entrySet()){
            if(!specs.containsKey(row.getKey()))throw new IllegalArgumentException("Unregistered weapon "+row.getKey());
            try{
                var program=WeaponProgram.parse(row.getValue().getAsJsonObject());
                if(program.cost()>specs.get(row.getKey()).magSize)throw new IllegalArgumentException("cost exceeds base capacity");
                map.put(row.getKey(),program);
            }
            catch(RuntimeException ex){throw new IllegalArgumentException("Weapon "+row.getKey()+": "+ex.getMessage(),ex);}
        }
        String wire=rows.toString();
        if(wire.length()>120000)throw new IllegalArgumentException("Weapon programs exceed synchronization budget");
        return new Snapshot(Map.copyOf(map),wire);
    }
    static void receive(String json){client=snapshot(JsonParser.parseString(json).getAsJsonObject());}
    @SubscribeEvent public static void register(AddReloadListenerEvent event){
        event.addListener(new SimplePreparableReloadListener<Snapshot>(){
            @Override protected Snapshot prepare(ResourceManager manager,ProfilerFiller profiler){
                // Read the highest-priority file ourselves. The vanilla JSON scanner logs and skips
                // malformed files; here a bad definition must fail /reload and leave the live table intact.
                var rows=JsonParser.parseString(DEFAULTS.wire).getAsJsonObject();
                for(var entry:manager.listResources("weapon_programs", id->id.getNamespace().equals("destiny2mc")&&id.getPath().endsWith(".json")).entrySet()){
                    String path=entry.getKey().getPath();
                    String item=path.substring("weapon_programs/".length(),path.length()-5);
                    try(var reader=entry.getValue().openAsReader()){
                        rows.add(item,JsonParser.parseReader(reader));
                    }catch(Exception ex){throw new IllegalArgumentException("Cannot read "+entry.getKey()+" from "+entry.getValue().sourcePackId(),ex);}
                }
                return snapshot(rows);
            }
            @Override protected void apply(Snapshot next,ResourceManager manager,ProfilerFiller profiler){server=next;}
        });
    }
    @SubscribeEvent public static void sync(OnDatapackSyncEvent event){
        var msg=new ProgramSync(server.wire());
        event.getRelevantPlayers().forEach(p->PacketDistributor.sendToPlayer(p,msg));
    }
}
