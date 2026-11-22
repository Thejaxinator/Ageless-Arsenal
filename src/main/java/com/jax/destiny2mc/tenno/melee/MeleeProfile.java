package com.jax.destiny2mc.tenno.melee;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Startup definitions, shared by registration, attributes, combat, and equipment cards. */
public record MeleeProfile(String id, String name, String family, double damage, double attackSpeed,
        double reach, int guardAngle, int durability, Technique technique, String trait, String description) {
    public enum Technique { dice, reap, resonance, ignition, embers, crosscut }

    public MeleeProfile {
        if (id == null || !id.matches("[a-z][a-z0-9_]*") || name == null || name.isBlank()
                || family == null || family.isBlank() || trait == null || trait.isBlank()
                || description == null || description.isBlank() || technique == null
                || !(damage > 0 && damage <= 40) || !(attackSpeed > 0 && attackSpeed <= 4)
                || !(reach >= 1 && reach <= 8) || guardAngle <= 0 || guardAngle > 180 || durability < 1)
            throw new IllegalArgumentException("Invalid Warframe melee definition " + id);
    }
    public boolean paired() { return technique == Technique.crosscut || technique == Technique.embers; }
    public static final List<MeleeProfile> ALL = read();
    private static List<MeleeProfile> read() {
        try (var stream = Objects.requireNonNull(MeleeProfile.class.getResourceAsStream("/tenno/melee.json"));
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var entries = List.of(new Gson().fromJson(reader, MeleeProfile[].class));
            var ids = new HashSet<String>();
            for (var p : entries) if (!ids.add(p.id())) throw new IllegalArgumentException("Duplicate melee " + p.id());
            return entries;
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
}
