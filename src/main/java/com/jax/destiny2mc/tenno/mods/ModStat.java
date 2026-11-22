package com.jax.destiny2mc.tenno.mods;

import java.math.BigDecimal;

/** Shared stat units. All values are additive fractions, except melee reach in blocks. */
public enum ModStat {
    DAMAGE("damage"), FIRE_RATE("fire rate"), RELOAD_SPEED("reload speed"), MAGAZINE("magazine"),
    MULTISHOT("multishot"), HEAT("Heat"), COLD("Cold"), ELECTRICITY("Electricity"), TOXIN("Toxin"),
    MAGNETIC("Magnetic"), VIRAL("Viral"), RADIATION("Radiation"), GAS("Gas"), BLAST("Blast"), CORROSIVE("Corrosive"),
    MELEE_SPEED("attack speed"), REACH("reach"), HEAVY_DAMAGE("heavy damage"), PARRY("counter chance");

    private final String label;
    ModStat(String label) { this.label = label; }

    public String label() { return label; }

    public boolean isBaseElement() {
        return this == HEAT || this == COLD || this == ELECTRICITY || this == TOXIN;
    }

    public boolean isElemental() {
        return isBaseElement() || this == MAGNETIC || this == VIRAL || this == RADIATION
            || this == GAS || this == BLAST || this == CORROSIVE;
    }

    /** Warframe elemental combination table for any unordered pair of distinct base elements. */
    public static ModStat combine(ModStat a, ModStat b) {
        if (a == null || b == null || a == b) return a;
        if ((a == COLD && b == ELECTRICITY) || (a == ELECTRICITY && b == COLD)) return MAGNETIC;
        if ((a == COLD && b == TOXIN) || (a == TOXIN && b == COLD)) return VIRAL;
        if ((a == HEAT && b == ELECTRICITY) || (a == ELECTRICITY && b == HEAT)) return RADIATION;
        if ((a == HEAT && b == TOXIN) || (a == TOXIN && b == HEAT)) return GAS;
        if ((a == HEAT && b == COLD) || (a == COLD && b == HEAT)) return BLAST;
        if ((a == ELECTRICITY && b == TOXIN) || (a == TOXIN && b == ELECTRICITY)) return CORROSIVE;
        return a;
    }

    String format(double amount) {
        // Round to hundredths before presentation so binary floating point does not appear in a card.
        double units = this == REACH ? amount : amount * 100;
        String number = BigDecimal.valueOf(Math.round(units * 100), 2).stripTrailingZeros().toPlainString();
        return "+" + number + (this == REACH ? " block" + (number.equals("1") ? " " : "s ") : "% ") + label;
    }
}
