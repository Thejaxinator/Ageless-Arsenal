package com.jax.destiny2mc.tenno.abilities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum WarframeAbilityId {
    URIEL_INFERNALIS(WarframeId.URIEL, "uriel_infernalis", "Infernalis", 30.0f, 80),
    URIEL_BRIMSTONE(WarframeId.URIEL, "uriel_brimstone", "Brimstone", 55.0f, 180),
    EXCALIBUR_SLASH_DASH(WarframeId.EXCALIBUR, "excalibur_slash_dash", "Slash Dash", 25.0f, 45),
    MAG_PULL(WarframeId.MAG, "mag_pull", "Pull", 25.0f, 55),
    VOLT_SPEED(WarframeId.VOLT, "volt_speed", "Speed", 25.0f, 120),
    NYX_CHAOS(WarframeId.NYX, "nyx_chaos", "Chaos", 45.0f, 180),
    KULLERVO_WRATHFUL_ADVANCE(WarframeId.KULLERVO, "kullervo_wrathful_advance", "Wrathful Advance", 25.0f, 65),
    VORUNA_SHROUD_OF_DYNAR(WarframeId.VORUNA, "voruna_shroud_of_dynar", "Shroud of Dynar", 25.0f, 110),
    VALKYR_WARCRY(WarframeId.VALKYR, "valkyr_warcry", "Warcry", 45.0f, 160),
    FROST_SNOW_GLOBE(WarframeId.FROST, "frost_snow_globe", "Snow Globe", 45.0f, 180),
    FOLLIE_INKWELL_MIRAGE(WarframeId.FOLLIE, "follie_inkwell_mirage", "Inkwell Mirage", 30.0f, 110),
    CITRINE_PRISMATIC_GEM(WarframeId.CITRINE, "citrine_prismatic_gem", "Prismatic Gem", 45.0f, 200),
    CITRINE_CRYSTALLIZE(WarframeId.CITRINE, "citrine_crystallize", "Crystallize", 55.0f, 220),
    STYANAX_THARROS_STRIKE(WarframeId.STYANAX, "styanax_tharros_strike", "Tharros Strike", 25.0f, 80),
    STYANAX_FINAL_STAND(WarframeId.STYANAX, "styanax_final_stand", "Final Stand", 60.0f, 220),
    GYRE_COIL_HORIZON(WarframeId.GYRE, "gyre_coil_horizon", "Coil Horizon", 35.0f, 110),
    GYRE_ROTORSWELL(WarframeId.GYRE, "gyre_rotorswell", "Rotorswell", 55.0f, 240),
    CALIBAN_SENTIENT_WRATH(WarframeId.CALIBAN, "caliban_sentient_wrath", "Sentient Wrath", 40.0f, 150),
    CALIBAN_RAZOR_GYRE(WarframeId.CALIBAN, "caliban_razor_gyre", "Razor Gyre", 30.0f, 100),
    YARELI_AQUABLADES(WarframeId.YARELI, "yareli_aquablades", "Aquablades", 35.0f, 160),
    YARELI_RIPTIDE(WarframeId.YARELI, "yareli_riptide", "Riptide", 55.0f, 200),
    LAVOS_VIAL_RUSH(WarframeId.LAVOS, "lavos_vial_rush", "Vial Rush", 0.0f, 90),
    LAVOS_CATALYZE(WarframeId.LAVOS, "lavos_catalyze", "Catalyze", 0.0f, 220);

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
