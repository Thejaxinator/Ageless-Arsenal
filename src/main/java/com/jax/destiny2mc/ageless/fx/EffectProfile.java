package com.jax.destiny2mc.ageless.fx;

import java.util.Locale;

/** Stable wire keys and a three-tone ramp. Never reorder an existing key on one side only. */
public enum EffectProfile {
    KINETIC(0x464951, 0x9599a2, 0xd8dbe0),
    SOLAR(0xa8321b, 0xff9437, 0xffe8a0),
    ARC(0x285eaf, 0x57c9ff, 0xd9f9ff),
    VOID(0x502787, 0xaa67fb, 0xf1d7ff),
    STASIS(0x385295, 0x819cff, 0xd9edff),
    STRAND(0x12684e, 0x35e591, 0xcbffd1),
    TAKEN(0x101823, 0x46616d, 0xebfff7),
    SOULFIRE(0x315844, 0x85cf83, 0xe1ffd2),
    RESONANCE(0x553924, 0xd08a40, 0xffda84),
    ETHER(0x465c97, 0x83cee7, 0xd6fcff),
    DARK_ETHER(0x3b274c, 0xb16aaf, 0xe1a8eb),
    NANITE(0x421923, 0xef3445, 0xffafa1);

    public final int shade, color, core;
    EffectProfile(int shade, int color, int core) {
        this.shade = shade; this.color = color; this.core = core;
    }
    public static EffectProfile fromElement(int element) {
        return switch (element) {
            case 1 -> SOLAR; case 2 -> ARC; case 3 -> VOID;
            case 4 -> STASIS; case 5 -> STRAND; default -> KINETIC;
        };
    }
    public static EffectProfile named(String name, int fallback) {
        String key = name.toUpperCase(Locale.ROOT);
        for (EffectProfile profile : values()) if (profile.name().equals(key)) return profile;
        return fromElement(fallback);
    }
    /** Sync-data value zero means the elemental fallback; profiles are encoded one-based. */
    public static EffectProfile fromTag(int tag, int fallback) {
        return tag > 0 && tag <= values().length ? values()[tag-1] : fromElement(fallback);
    }
}
