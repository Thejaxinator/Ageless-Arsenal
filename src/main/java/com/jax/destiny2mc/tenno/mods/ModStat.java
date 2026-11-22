package com.jax.destiny2mc.tenno.mods;

import java.math.BigDecimal;

/** Shared stat units. All values are additive fractions, except melee reach in blocks. */
public enum ModStat {
    DAMAGE("damage"), FIRE_RATE("fire rate"), RELOAD_SPEED("reload speed"), MAGAZINE("magazine"),
    MULTISHOT("multishot"), HEAT("Heat"), COLD("Cold"), MELEE_SPEED("attack speed"),
    REACH("reach"), HEAVY_DAMAGE("heavy damage"), PARRY("counter chance");

    private final String label;
    ModStat(String label) { this.label = label; }

    String format(double amount) {
        // Round to hundredths before presentation so binary floating point does not appear in a card.
        double units = this == REACH ? amount : amount * 100;
        String number = BigDecimal.valueOf(Math.round(units * 100), 2).stripTrailingZeros().toPlainString();
        return "+" + number + (this == REACH ? " block" + (number.equals("1") ? " " : "s ") : "% ") + label;
    }
}
