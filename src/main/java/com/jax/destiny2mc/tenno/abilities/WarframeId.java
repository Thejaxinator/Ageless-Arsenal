package com.jax.destiny2mc.tenno.abilities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum WarframeId {
    URIEL("uriel", "Uriel", 175.0f, 3.0f, 0xFF5A1F),
    EXCALIBUR("excalibur", "Excalibur", 150.0f, 2.5f, 0x7ED6FF),
    MAG("mag", "Mag", 175.0f, 3.0f, 0x4DA6FF),
    VOLT("volt", "Volt", 200.0f, 3.2f, 0x38E8FF),
    NYX("nyx", "Nyx", 175.0f, 3.0f, 0xB86BFF),
    KULLERVO("kullervo", "Kullervo", 150.0f, 2.5f, 0xD94B4B),
    VORUNA("voruna", "Voruna", 150.0f, 2.8f, 0x5CE6A0),
    VALKYR("valkyr", "Valkyr", 150.0f, 2.6f, 0xFF7A38),
    FROST("frost", "Frost", 150.0f, 2.6f, 0x8CE6FF),
    FOLLIE("follie", "Follie", 175.0f, 3.0f, 0xE8D27C),
    CITRINE("citrine", "Citrine", 180.0f, 3.2f, 0xFF5CE1),
    STYANAX("styanax", "Styanax", 175.0f, 3.5f, 0x46B8FF),
    GYRE("gyre", "Gyre", 200.0f, 3.4f, 0x5CC8FF),
    CALIBAN("caliban", "Caliban", 175.0f, 3.0f, 0x7A8BFF),
    YARELI("yareli", "Yareli", 180.0f, 3.2f, 0x38D9FF),
    LAVOS("lavos", "Lavos", 175.0f, 3.5f, 0x4CE88A);

    public static final StreamCodec<FriendlyByteBuf, WarframeId> STREAM_CODEC = StreamCodec.of(
            (buf, id) -> buf.writeVarInt(id.ordinal()),
            buf -> {
                int idx = buf.readVarInt();
                WarframeId[] values = values();
                return idx >= 0 && idx < values.length ? values[idx] : EXCALIBUR;
            }
    );

    private final String key;
    private final String displayName;
    private final float maxEnergy;
    private final float energyRegenPerSecond;
    private final int accentRgb;

    WarframeId(String key, String displayName, float maxEnergy, float energyRegenPerSecond, int accentRgb) {
        this.key = key;
        this.displayName = displayName;
        this.maxEnergy = maxEnergy;
        this.energyRegenPerSecond = energyRegenPerSecond;
        this.accentRgb = accentRgb;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public float maxEnergy() {
        return maxEnergy;
    }

    public float energyRegenPerSecond() {
        return energyRegenPerSecond;
    }

    public int accentRgb() {
        return accentRgb;
    }

    public static WarframeId byKey(String key) {
        if (key == null) {
            return EXCALIBUR;
        }
        for (WarframeId id : values()) {
            if (id.key.equalsIgnoreCase(key)) {
                return id;
            }
        }
        return EXCALIBUR;
    }
}
