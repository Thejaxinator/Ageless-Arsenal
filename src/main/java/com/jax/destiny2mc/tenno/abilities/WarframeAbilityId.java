package com.jax.destiny2mc.tenno.abilities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum WarframeAbilityId {
    URIEL_INFERNALIS(WarframeId.URIEL, "uriel_infernalis", "Infernalis", 35.0f, 80),
    URIEL_BRIMSTONE(WarframeId.URIEL, "uriel_brimstone", "Brimstone", 60.0f, 160),
    EXCALIBUR_SLASH_DASH(WarframeId.EXCALIBUR, "excalibur_slash_dash", "Slash Dash", 25.0f, 50),
    MAG_PULL(WarframeId.MAG, "mag_pull", "Pull", 25.0f, 60),
    VOLT_SPEED(WarframeId.VOLT, "volt_speed", "Speed", 25.0f, 120),
    NYX_CHAOS(WarframeId.NYX, "nyx_chaos", "Chaos", 50.0f, 180),
    KULLERVO_WRATHFUL_ADVANCE(WarframeId.KULLERVO, "kullervo_wrathful_advance", "Wrathful Advance", 30.0f, 70),
    VORUNA_SHROUD_OF_DYNAR(WarframeId.VORUNA, "voruna_shroud_of_dynar", "Shroud of Dynar", 30.0f, 110),
    VALKYR_WARCRY(WarframeId.VALKYR, "valkyr_warcry", "Warcry", 45.0f, 160),
    FROST_SNOW_GLOBE(WarframeId.FROST, "frost_snow_globe", "Snow Globe", 50.0f, 180),
    FOLLIE_INKWELL_MIRAGE(WarframeId.FOLLIE, "follie_inkwell_mirage", "Inkwell Mirage", 35.0f, 100);

    public static final StreamCodec<FriendlyByteBuf, WarframeAbilityId> STREAM_CODEC = StreamCodec.of(
            (buf, id) -> buf.writeVarInt(id.ordinal()),
            buf -> {
                int idx = buf.readVarInt();
                WarframeAbilityId[] values = values();
                return idx >= 0 && idx < values.length ? values[idx] : EXCALIBUR_SLASH_DASH;
            }
    );

    private final WarframeId warframe;
    private final String key;
    private final String displayName;
    private final float energyCost;
    private final int cooldownTicks;

    WarframeAbilityId(WarframeId warframe, String key, String displayName, float energyCost, int cooldownTicks) {
        this.warframe = warframe;
        this.key = key;
        this.displayName = displayName;
        this.energyCost = energyCost;
        this.cooldownTicks = cooldownTicks;
    }

    public WarframeId warframe() {
        return warframe;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public float energyCost() {
        return energyCost;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public static WarframeAbilityId primaryFor(WarframeId warframe) {
        for (WarframeAbilityId ability : values()) {
            if (ability.warframe == warframe) {
                return ability;
            }
        }
        return EXCALIBUR_SLASH_DASH;
    }

    public static WarframeAbilityId byKey(String key) {
        if (key == null) {
            return null;
        }
        for (WarframeAbilityId ability : values()) {
            if (ability.key.equalsIgnoreCase(key)) {
                return ability;
            }
        }
        return null;
    }
}
