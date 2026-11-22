package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Warframe-style metallic mod cards: stepped shoulder silhouette, Bronze/Silver/Gold
 * metallic rails with corner flares, top rarity diamonds, top-right rank + polarity tab,
 * teal EQUIPPED pill, chamfered compatibility footer, and glowing cyan rank pips. */
final class ModCardArt {
    static final int INK = VitruvianUi.INK;
    static final int BRASS = VitruvianUi.GOLD;
    static final int IVORY = VitruvianUi.IVORY;
    static final int MUTED = VitruvianUi.MUTED;
    static final int JADE = VitruvianUi.CYAN;
    static final int DULL = VitruvianUi.BORDER_HAIRLINE;

    private ModCardArt() {}

    static void paint(GuiGraphics g, UiCanvas c, Font font, WeaponMod mod, int rank,
                      int x, int y, int w, int h, boolean selected, boolean large, boolean installed) {
        boolean locked = rank < 0;
        int metal = VitruvianUi.rarityFrame(mod.rarity());
        int frame = locked ? 0xFF555E6B : metal;
        int left = x + 3, top = y + 3, right = x + w - 3, bottom = y + h - 3;
        int mid = x + w / 2;

        // 1. Stepped-shoulder charcoal card base
        c.fill(x + 5, y, w - 10, h, VitruvianUi.INK);
        c.fill(x, y + 5, w, h - 10, VitruvianUi.INK);

        // 2. Rarity-tinted metallic vertical gradient interior
        int bgTop = locked ? 0xE81E222A : VitruvianUi.rarityBgTop(mod.rarity());
        int bgBot = locked ? 0xF011141A : VitruvianUi.rarityBgBottom(mod.rarity());
        g.fillGradient(left, top, right, bottom, bgTop, bgBot);

        // 3. Metallic rarity frame, side flares & chamfered corner notches
        c.fill(x + 5, y, w - 10, 1, frame);
        c.fill(x + 5, y + h - 1, w - 10, 1, frame);
        for (int side = 0; side < 2; side++) {
            int edge = side == 0 ? x : x + w - 1, sign = side == 0 ? 1 : -1;
            c.line(edge + 5 * sign, y, edge, y + 5, frame);
            c.line(edge, y + 5, edge, y + h - 6, frame);
            c.line(edge, y + h - 6, edge + 5 * sign, y + h - 1, frame);
            c.line(edge + 2 * sign, y + 10, edge + 2 * sign, y + 18, frame);
            c.line(edge + 2 * sign, y + h - 11, edge + 2 * sign, y + h - 18, frame);
        }

        // Selected highlight: crisp white/gold top rail + shimmer sweep
        if (selected) {
            c.fill(x + 5, y, w - 10, 2, VitruvianUi.WHITE_RAIL);
            c.fill(x + 5, y + h - 2, w - 10, 2, VitruvianUi.GOLD);
            VitruvianUi.drawShimmerBar(g, x + 6, y, w - 12, 2, VitruvianUi.GOLD);
        }

        // 4. Top-right rank + polarity tab (matches Warframe's chamfered drain/polarity pill)
        VitruvianUi.Polarity polarity = VitruvianUi.polarityOf(mod);
        int polColor = locked ? VitruvianUi.DISABLED : (installed ? VitruvianUi.POSITIVE : IVORY);
        if (large) {
            c.fill(x + w - 30, y + 6, 24, 13, 0xE014181F);
            c.outline(x + w - 30, y + 6, 24, 13, frame);
            VitruvianUi.drawPolarity(c, polarity, x + w - 16, y + 9, polColor);
            String rankTag = locked ? "-" : Integer.toString(rank);
            c.text(x + w - 26, y + 9, rankTag, locked ? VitruvianUi.DISABLED : IVORY, .78f);
        } else {
            c.fill(x + w - 22, y + 3, 18, 10, 0xD814181F);
            c.outline(x + w - 22, y + 3, 18, 10, frame);
            c.text(x + w - 20, y + 5, locked ? "-" : Integer.toString(rank), locked ? VitruvianUi.DISABLED : IVORY, .66f);
            VitruvianUi.drawPolarity(c, polarity, x + w - 12, y + 4, polColor);
        }

        // 5. Central art window
        int glyph = large ? 54 : 24;
        int gx = x + (w - glyph) / 2;
        int gy = y + (large ? 21 : 5);
        if (large) {
            c.fill(gx - 5, gy - 3, glyph + 10, glyph + 6, 0xCC0E1217);
            c.outline(gx - 5, gy - 3, glyph + 10, glyph + 6, locked ? 0xFF3A414C : frame);
        }
        // Keep each graphic crisp at Minecraft GUI scales by sampling the complete 32px tile.
        g.pose().pushPose();
        g.pose().translate(gx, gy, 0);
        g.pose().scale(glyph / 32f, glyph / 32f, 1);
        g.blit(mod.icon(), 0, 0, 0, 0, 32, 32, 32, 32);
        g.pose().popPose();

        if (large) {
            // 6. Top-center rarity diamonds flanked by metallic rails
            int diamondSpan = VitruvianUi.rarityDiamonds(mod.rarity()) * 3 + 4;
            c.line(x + 14, y + 11, mid - diamondSpan, y + 11, frame);
            c.line(mid + diamondSpan, y + 11, x + w - 34, y + 11, frame);
            VitruvianUi.drawRarityDiamonds(c, mid, y + 11, mod.rarity(), frame);

            // 7. Title & effect lines
            centered(c, font, mid, y + 81, mod.title(), 1.14f, IVORY, w - 16);
            wrapped(c, font, x + 13, y + 98, w - 26, mod.effect(Math.max(0, rank)), .95f, 3,
                    locked ? MUTED : VitruvianUi.POSITIVE);

            // 8. Chamfered compatibility pill near foot (like "WARFRAME" / "RIFLE" on Warframe cards)
            String pill = mod.category().toUpperCase(Locale.ROOT) + "  ·  " + VitruvianUi.rarityName(mod.rarity());
            int pillW = Math.min(w - 22, Math.round(font.width(pill) * .72f) + 14);
            c.fill(mid - pillW / 2, y + h - 30, pillW, 12, 0xE614181F);
            c.outline(mid - pillW / 2, y + h - 30, pillW, 12, frame);
            centered(c, font, mid, y + h - 27, pill, .72f, IVORY, pillW - 4);
        } else {
            // Compact card: rarity diamonds on top rail + title + teal EQUIPPED pill or category
            VitruvianUi.drawRarityDiamonds(c, mid - 6, y + 2, mod.rarity(), frame);
            centered(c, font, mid, y + 30, mod.title(), .82f, locked ? MUTED : IVORY, w - 10);
            if (installed) {
                int pillW = 46;
                c.fill(mid - pillW / 2, y + 40, pillW, 9, VitruvianUi.TEAL_BADGE);
                centered(c, font, mid, y + 42, "EQUIPPED", .64f, VitruvianUi.INK, pillW - 2);
            } else {
                String label = locked ? "LOCKED" : mod.category().toUpperCase(Locale.ROOT);
                centered(c, font, mid, y + 41, label, .68f, locked ? VitruvianUi.DISABLED : MUTED, w - 10);
            }
        }

        // 9. Bottom row of glowing cyan-white rank pips
        VitruvianUi.drawRankPips(c, mid, y + h - (large ? 13 : 8), mod.maxRank(), rank, w - 22, large);
    }

    static void centered(UiCanvas c, Font f, int cx, int y, String text, float scale, int color, int maxWidth) {
        String fit = f.plainSubstrByWidth(text, (int) (maxWidth / scale));
        c.text(cx - Math.round(f.width(fit) * scale / 2), y, fit, color, scale);
    }

    static void wrapped(UiCanvas c, Font font, int x, int y, int width, String text, float scale, int lines, int color) {
        int max = (int) (width / scale), row = 0;
        String buffer = "";
        for (String word : text.split(" ")) {
            String next = buffer.isEmpty() ? word : buffer + " " + word;
            if (!buffer.isEmpty() && font.width(next) > max) {
                c.text(x, y + row * 12, buffer, color, scale);
                if (++row == lines) return;
                buffer = word;
            } else buffer = next;
        }
        if (!buffer.isEmpty() && row < lines) c.text(x, y + row * 12, font.plainSubstrByWidth(buffer, max), color, scale);
    }
}
