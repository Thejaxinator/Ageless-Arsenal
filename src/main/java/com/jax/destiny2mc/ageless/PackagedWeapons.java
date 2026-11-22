package com.jax.destiny2mc.ageless;

import com.google.gson.*;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/** Registry IDs freeze before datapacks load. This bundled manifest is the shared, append-only
 * identity/stat table. Live trigger and delivery definitions are loaded by WeaponPrograms. */
public final class PackagedWeapons {
    private static final JsonArray ENTRIES = read("armory_manifest").getAsJsonArray("weapons");
    static JsonObject read(String path) {
        String file = "/data/destiny2mc/" + path + ".json";
        try (var stream = PackagedWeapons.class.getResourceAsStream(file)) {
            if (stream == null) throw new IllegalStateException("Missing " + file);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) { throw new IllegalStateException(file, e); }
    }
    public static int count() { return ENTRIES.size(); }

    public static void append(WeaponSpec[] specs, int index) {
        var used = new HashSet<String>();
        for (int i=0; i<index; i++) used.add(specs[i].id);
        for (var row : ENTRIES) {
            var j=row.getAsJsonObject();
            String id=j.get("id").getAsString();
            if (!id.matches("[a-z0-9_]+") || !used.add(id)) throw new IllegalArgumentException("Duplicate or invalid weapon ID " + id);
            specs[index]=new WeaponSpec(index,id,j.get("name").getAsString(),j.get("frame").getAsString(),
                i(j,"archetype"),i(j,"element"),j.get("exotic").getAsBoolean(),j.get("special").getAsBoolean(),
                i(j,"rpm"),f(j,"cadence"),j.get("automatic").getAsBoolean(),i(j,"burst"),i(j,"magazine"),i(j,"reload"),
                f(j,"damage"),f(j,"precision"),i(j,"pellets"),f(j,"hip_spread"),f(j,"ads_spread"),f(j,"range"),
                f(j,"zoom"),f(j,"kick"),f(j,"kick")*.18f,72,70,85,200+index,
                j.get("trait").getAsString(),j.get("description").getAsString());
            index++;
        }
    }
    private static int i(JsonObject j,String key) { return j.get(key).getAsInt(); }
    private static float f(JsonObject j,String key) { return j.get(key).getAsFloat(); }
}
