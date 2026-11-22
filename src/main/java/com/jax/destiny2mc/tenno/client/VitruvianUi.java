package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;

/** Shared Warframe UI design tokens and procedural chrome helpers modeled directly
 * after Warframe's Arsenal Loadout, Upgrades, Modding, and Foundry screens:
 * neutral translucent charcoal backdrops, frosted off-white loadout & foundry cards
 * with thin white overhang rails, sky-blue progress bars, smoky notched mod sockets,
 * metallic Bronze/Silver/Gold mod frames, and Warframe gold action bars. */
public final class VitruvianUi {
    // Translucent neutral charcoal backdrop & panels (no heavy color tint)
    public static final int BACKDROP = 0xC40D1015;
    public static final int BACKDROP_DEEP = 0xE60B0E13;
    public static final int INK = 0xFF14181E;
    public static final int PANEL_FILL = 0xCC161B22;
    public static final int PANEL_ELEVATED = 0xDD1E242D;
    public static final int PANEL_WELL = 0xD811151B;
    public static final int ROW_ACTIVE = 0xE82A2F38;
    public static final int ROW_HOVER = 0xCC222831;

    // Frosted off-white / silver Warframe Arsenal & Foundry cards
    public static final int FROST_LIGHT = 0xECE5E8EB;
    public static final int FROST_MID = 0xE0CFD5DA;
    public static final int FROST_DIM = 0xC8B8C0C7;
    public static final int FROST_INK = 0xFF1D2228;
    public static final int FROST_SUB = 0xFF47505A;
    public static final int SLATE_BAR = 0xFF6C7A89;
    public static final int SLATE_BAR_DARK = 0xFF4A5561;

    // Borders, overhang rails & rules
    public static final int WHITE_RAIL = 0xFFEFF3F6;
    public static final int BORDER_HAIRLINE = 0xFF49525E;
    public static final int BORDER_SOFT = 0x66C8D2DC;
    public static final int RULE_SUBTLE = 0xFF2B313B;

    // Warframe Gold (ready / fuse / active CTA) & Sky-Blue (capacity / loadout progress)
    public static final int GOLD = 0xFFD9C05E;
    public static final int GOLD_ARMOR = 0xFFE0A635;
    public static final int GOLD_BRIGHT = 0xFFF2DE82;
    public static final int GOLD_DARK = 0xFF7D6B2A;
    public static final int SKY_BLUE = 0xFF3FA9D6;
    public static final int SKY_BLUE_SOFT = 0xFF7EC8E8;

    // Typography
    public static final int IVORY = 0xFFF4F6F8;
    public static final int MUTED = 0xFFA2ABB5;
    public static final int DISABLED = 0xFF6A737D;

    // Verified Warframe HUD semantic accents
    public static final int POSITIVE = 0xFF4CD964;
    public static final int POSITIVE_HUD = 0xFF43B306;
    public static final int NEGATIVE = 0xFFCC2A28;
    public static final int CYAN = 0xFF38C9E8;
    public static final int CYAN_CORE = 0xFFE5FAFF;
    public static final int CYAN_SOFT = 0xFF7DD8EC;
    public static final int TEAL_BADGE = 0xFF2CA89D;
    public static final int RIVEN_PURPLE = 0xFFB201FE;

    // Mod rarity metallics (Common Bronze, Uncommon Silver, Rare Gold, Legendary Platinum, Riven)
    public static final int BRONZE = 0xFFB68356;
    public static final int BRONZE_DEEP = 0xFF7E522C;
    public static final int SILVER = 0xFFC5CED8;
    public static final int SILVER_DEEP = 0xFF8893A0;
    public static final int RARE_GOLD = 0xFFDCC062;
    public static final int PRIMED_PLATINUM = 0xFFDCE6F0;

    public enum Polarity {
        MADURAI("Madurai"),
        NARAMON("Naramon"),
        VAZARIN("Vazarin");

        public final String label;
        Polarity(String label) { this.label = label; }
    }

    private VitruvianUi() {}

    public static Polarity polarityOf(WeaponMod mod) {
        if (mod == null || mod.steps().isEmpty()) return Polarity.MADURAI;
        ModStat primary = mod.steps().get(0).stat();
        return switch (primary) {
            case DAMAGE, MULTISHOT, FIRE_RATE, MELEE_SPEED, HEAVY_DAMAGE -> Polarity.MADURAI;
            case PARRY -> Polarity.VAZARIN;
            default -> Polarity.NARAMON;
        };
    }

    public static int rarityFrame(int rarity) {
        return switch (rarity) {
            case 0 -> BRONZE;
            case 1 -> SILVER;
            case 2 -> RARE_GOLD;
            case 3 -> PRIMED_PLATINUM;
            default -> RIVEN_PURPLE;
        };
    }

    public static int rarityBgTop(int rarity) {
        return switch (rarity) {
            case 0 -> 0xF034261B;
            case 1 -> 0xF0262D38;
            case 2 -> 0xF0362D18;
            case 3 -> 0xF02A3440;
            default -> 0xF02C1B3A;
        };
    }

    public static int rarityBgBottom(int rarity) {
        return switch (rarity) {
            case 0 -> 0xF517120E;
            case 1 -> 0xF513171F;
            case 2 -> 0xF518150D;
            case 3 -> 0xF5151A22;
            default -> 0xF5150E1D;
        };
    }

    public static String rarityName(int rarity) {
        return switch (rarity) {
            case 0 -> "COMMON";
            case 1 -> "UNCOMMON";
            case 2 -> "RARE";
            case 3 -> "LEGENDARY";
            default -> "RIVEN";
        };
    }

    public static int rarityDiamonds(int rarity) {
        return Math.max(1, Math.min(5, rarity + 1));
    }

    public static int variantTrim(String variant) {
        if (variant == null) return GOLD;
        return switch (variant) {
            case "Prime" -> GOLD_BRIGHT;
            case "Kuva" -> 0xFFD9534F;
            case "Coda" -> RIVEN_PURPLE;
            case "Prisma", "Vandal" -> SKY_BLUE;
            case "Wraith" -> NEGATIVE;
            default -> GOLD;
        };
    }

    /** Translucent neutral charcoal Warframe panel with subtle hairline border and top sheen. */
    public static void panel(UiCanvas c, int x, int y, int w, int h) {
        c.fill(x, y, w, h, PANEL_FILL);
        c.outline(x, y, w, h, BORDER_HAIRLINE);
        c.fill(x + 1, y + 1, w - 2, 1, 0x22FFFFFF);
    }

    /** Frosted off-white / silver Warframe card with crisp thin white top & bottom overhang rails. */
    public static void frostedCard(UiCanvas c, int x, int y, int w, int h, boolean selected) {
        c.fill(x, y, w, h, selected ? FROST_LIGHT : FROST_MID);
        c.outline(x, y, w, h, selected ? WHITE_RAIL : 0xAA8E98A3);
        // Signature Warframe thin white top & bottom overhang rails
        c.fill(x - 2, y - 2, w + 4, 1, selected ? WHITE_RAIL : 0x88CFD6DE);
        c.fill(x - 2, y + h + 1, w + 4, 1, selected ? WHITE_RAIL : 0x88CFD6DE);
    }

    /** Smoky notched empty Warframe mod slot silhouette with metallic top/bottom bevel highlights. */
    public static void smokySlot(UiCanvas c, int x, int y, int w, int h) {
        c.fill(x + 3, y, w - 6, h, 0xB8181D24);
        c.fill(x, y + 3, w, h - 6, 0xB8181D24);
        // Top & bottom metallic bevel edge
        c.fill(x + 6, y, w - 12, 1, 0xFF6B7582);
        c.fill(x + 6, y + h - 1, w - 12, 1, 0xFF474F59);
        // Chamfered shoulders
        c.line(x + 3, y, x, y + 3, 0xFF555E69);
        c.line(x + w - 4, y, x + w - 1, y + 3, 0xFF555E69);
        c.line(x, y + 3, x, y + h - 4, 0xFF3E454E);
        c.line(x + w - 1, y + 3, x + w - 1, y + h - 4, 0xFF3E454E);
        c.line(x, y + h - 4, x + 3, y + h - 1, 0xFF474F59);
        c.line(x + w - 1, y + h - 4, x + w - 4, y + h - 1, 0xFF474F59);
        // Subtle top-center notch accent
        int mid = x + w / 2;
        c.fill(mid - 8, y + 1, 16, 1, 0x55A7B3C0);
    }

    /** Clean Warframe hairline divider with optional white/accent center pip. */
    public static void divider(UiCanvas c, int x, int y, int w, int accent) {
        int mid = x + w / 2;
        if (w >= 24) {
            c.fill(x, y, mid - x - 5, 1, BORDER_HAIRLINE);
            c.fill(mid + 6, y, x + w - (mid + 6), 1, BORDER_HAIRLINE);
            c.diamond(mid, y, 2, accent);
            c.fill(mid, y, 1, 1, accent);
        } else {
            c.fill(x, y, w, 1, BORDER_HAIRLINE);
        }
    }

    /** Filled micro-diamond marker (GuiGraphics). */
    public static void diamond(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int span = r - Math.abs(dy);
            g.fill(cx - span, cy + dy, cx + span + 1, cy + dy + 1, color);
        }
    }

    /** Draws Warframe rarity diamonds centered at (cx, cy). */
    public static void drawRarityDiamonds(UiCanvas c, int cx, int cy, int rarity, int color) {
        int count = rarityDiamonds(rarity);
        int step = 6;
        int start = cx - (count - 1) * step / 2;
        for (int i = 0; i < count; i++) {
            int dx = start + i * step;
            c.diamond(dx, cy, 2, color);
            c.fill(dx, cy, 1, 1, color);
        }
    }

    /** Draws Warframe rarity diamonds centered at (cx, cy) via GuiGraphics. */
    public static void drawRarityDiamonds(GuiGraphics g, int cx, int cy, int rarity, int color) {
        int count = rarityDiamonds(rarity);
        int step = 7;
        int start = cx - (count - 1) * step / 2;
        for (int i = 0; i < count; i++) {
            diamond(g, start + i * step, cy, 2, color);
        }
    }

    /** Draws a compact 7x7 pixel Warframe polarity glyph on UiCanvas. */
    public static void drawPolarity(UiCanvas c, Polarity p, int x, int y, int color) {
        switch (p) {
            case MADURAI -> {
                c.fill(x, y, 2, 1, color);
                c.fill(x + 5, y, 2, 1, color);
                c.line(x + 1, y + 1, x + 3, y + 6, color);
                c.line(x + 5, y + 1, x + 3, y + 6, color);
            }
            case NARAMON -> {
                c.fill(x + 1, y + 3, 5, 1, color);
                c.fill(x, y + 2, 2, 1, color);
                c.fill(x + 5, y + 4, 2, 1, color);
            }
            case VAZARIN -> {
                c.fill(x + 1, y, 1, 7, color);
                c.fill(x + 2, y, 2, 1, color);
                c.fill(x + 2, y + 6, 2, 1, color);
                c.fill(x + 4, y + 1, 1, 1, color);
                c.fill(x + 5, y + 2, 1, 3, color);
                c.fill(x + 4, y + 5, 1, 1, color);
            }
        }
    }

    /** Draws a compact 7x7 pixel Warframe polarity glyph on GuiGraphics. */
    public static void drawPolarity(GuiGraphics g, Polarity p, int x, int y, int color) {
        switch (p) {
            case MADURAI -> {
                g.fill(x, y, x + 2, y + 1, color);
                g.fill(x + 5, y, x + 7, y + 1, color);
                g.fill(x + 1, y + 1, x + 2, y + 3, color);
                g.fill(x + 5, y + 1, x + 6, y + 3, color);
                g.fill(x + 2, y + 3, x + 3, y + 5, color);
                g.fill(x + 4, y + 3, x + 5, y + 5, color);
                g.fill(x + 3, y + 5, x + 4, y + 7, color);
            }
            case NARAMON -> {
                g.fill(x + 1, y + 3, x + 6, y + 4, color);
                g.fill(x, y + 2, x + 2, y + 3, color);
                g.fill(x + 5, y + 4, x + 7, y + 5, color);
            }
            case VAZARIN -> {
                g.fill(x + 1, y, x + 2, y + 7, color);
                g.fill(x + 2, y, x + 4, y + 1, color);
                g.fill(x + 2, y + 6, x + 4, y + 7, color);
                g.fill(x + 4, y + 1, x + 5, y + 2, color);
                g.fill(x + 5, y + 2, x + 6, y + 5, color);
                g.fill(x + 4, y + 5, x + 5, y + 6, color);
            }
        }
    }

    /** Draws Warframe-style bottom rank pips: glowing cyan-white when lit, dark charcoal when unlit. */
    public static void drawRankPips(UiCanvas c, int cx, int y, int maxRank, int currentRank, int maxWidth, boolean large) {
        int n = Math.max(1, maxRank);
        int pipSize = large ? 4 : 3;
        int step = Math.min(large ? 10 : 7, Math.max(pipSize + 1, maxWidth / n));
        int totalW = (n - 1) * step + pipSize;
        int startX = cx - totalW / 2;
        for (int i = 0; i < n; i++) {
            int px = startX + i * step;
            boolean lit = currentRank >= 0 && i < currentRank;
            if (lit) {
                c.fill(px, y, pipSize, pipSize, CYAN);
                if (large) c.fill(px + 1, y + 1, pipSize - 2, pipSize - 2, CYAN_CORE);
            } else {
                c.fill(px, y, pipSize, pipSize, 0xFF232831);
                c.outline(px, y, pipSize, pipSize, 0xFF4A5360);
            }
        }
    }

    /** Animated horizontal rarity shimmer bar (used on selected rare cards and pack reveals). */
    public static void drawShimmerBar(GuiGraphics g, int x, int y, int w, int h, int baseColor) {
        g.fill(x, y, x + w, y + h, baseColor);
        if (w < 12) return;
        float t = (Util.getMillis() % 2200L) / 2200f;
        int sweepW = Math.max(10, w / 4);
        int sx = x - sweepW + Math.round(t * (w + sweepW * 2));
        int left = Math.max(x, sx);
        int right = Math.min(x + w, sx + sweepW);
        if (right > left) {
            g.fill(left, y, right, y + h, 0x88FFFDF0);
        }
    }

    /** Neutral charcoal Warframe tooltip/inspection box with crisp top/bottom overhang rails. */
    public static void drawVitruvianBox(GuiGraphics g, int width, int height, int accent) {
        g.fill(2, 2, width + 2, height + 2, 0x88000000);
        g.fillGradient(0, 0, width, height, 0xF21B212A, 0xF611151B);
        g.fill(0, 0, width, 1, accent);
        g.fill(0, height - 1, width, height, BORDER_HAIRLINE);
        g.fill(0, 1, 1, height - 1, BORDER_HAIRLINE);
        g.fill(width - 1, 1, width, height - 1, BORDER_HAIRLINE);
        g.fill(2, 1, width - 2, 2, 0x26FFFFFF);
        // Subtle chamfered corner ticks
        for (int i = 0; i < 3; i++) {
            g.fill(i, 1, 1 + i, 4 - i, accent);
            g.fill(width - i - 1, 1, width - i, 4 - i, accent);
        }
    }
}
