package com.jax.destiny2mc.ageless.materials;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The legacy stat calculators ask their own class for four perk sockets. The common mixin
 * delegates appended weapon IDs here, so the GUI, saved socket values and combat math agree.
 * These common definition assets belong in the matching client and server JAR.
 */
public final class AgelessPerkPools {
    private static final String PATH = "/assets/destiny2mc/perks/ageless_pools.json";
    private static final Map<String,int[][]> POOLS = readPools();

    private static Map<String,int[][]> readPools() {
        Map<String,int[][]> pools = new HashMap<>();
        try (var in = AgelessPerkPools.class.getResourceAsStream(PATH)) {
            if (in == null) throw new IllegalStateException("Missing " + PATH);
            var entries = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entry : entries.entrySet()) {
                var rows = entry.getValue().getAsJsonArray();
                if (rows.size() != 4) throw new IllegalArgumentException(entry.getKey() + " requires four socket pools");
                int[][] table = new int[4][4];
                for (int socket=0; socket<4; socket++) {
                    var row = rows.get(socket).getAsJsonArray();
                    if(row.size()!=4) throw new IllegalArgumentException(entry.getKey() + " requires four rolls per socket");
                    boolean[] seen = new boolean[7];
                    for(int option=0; option<4; option++) {
                        int value = row.get(option).getAsInt();
                        int n = value-(socket+1)*100;
                        if(n < 1 || n > 6 || seen[n]) throw new IllegalArgumentException("Invalid or repeated roll " + value + " for " + entry.getKey());
                        table[socket][option]=value;
                        seen[n]=true;
                    }
                }
                pools.put(entry.getKey(),table);
            }
        } catch (Exception ex) { throw new IllegalStateException("Cannot load common weapon perk pools", ex); }
        return Map.copyOf(pools);
    }

    public static int option(String id,int socket,int option) {
        int[][] pool = POOLS.get(id);
        // A new registered gun can adopt a neutral four-option pool while its creator authors
        // custom rolls. Each of the four positions still grants a valid, distinct perk.
        if (pool == null) return (socket+1)*100 + 1 + option;
        return pool[socket][option];
    }
}
