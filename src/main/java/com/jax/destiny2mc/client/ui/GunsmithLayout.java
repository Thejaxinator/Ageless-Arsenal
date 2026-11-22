package com.jax.destiny2mc.client.ui;

import java.util.List;

public final class GunsmithLayout {
    private static final int BACKDROP = 0xff0c1016, PANE = 0xa8111720, FOOTER = 0xd80a0e13,
        HAIRLINE = 0x38ffffff, HAIRLINE_SOFT = 0x1effffff,
        WHITE = 0xffffffff, TEXT = 0xfff2f4f7, MUTED = 0xff9aa7b6, DIM = 0xff707d8b,
        PERK_BLUE = 0xff4f8cc9, EXOTIC_GOLD = 0xffceae33, EXOTIC_WASH = 0x42ceae33,
        LEGENDARY_PURPLE = 0xff8452a6, LEGENDARY_WASH = 0x52522f65, DANGER = 0xffe06c6c;

    public record Row(int id, String title, String subtitle) {}
    public record Cost(String name, int have, int need) {}
    public record State(int page, int selected, int scroll, List<Row> rows, String title, String subtitle, String intrinsic, int item,
        List<Cost> costs, boolean canApply, String[] sockets, int socket, List<Row> options, int chosen, String description, String status) {}

    public static int rowHit(double x, double y, int scroll, int count) {
        if (x < 26 || x > 224 || y < 115 || y >= 379) return -1;
        int i = scroll + (int) ((y - 115) / 22);
        return i < count ? i : -1;
    }

    public static int socketHit(double x, double y) {
        if (x < 246 || x >= 766 || y < 244 || y >= 342) return -1;
        int col = (int) ((x - 246) / 176), row = (int) ((y - 244) / 52);
        if (col > 2 || y >= 244 + row * 52 + 44 || x >= 246 + col * 176 + 168) return -1;
        return row * 3 + col;
    }

    public static int optionHit(double x, double y, int count) {
        if (x < 526 || x >= 772 || y < 117 || y >= 117 + count * 21) return -1;
        return (int) ((y - 117) / 21);
    }

    public static void draw(UiCanvas c, State s) {
        c.fill(0, 0, 800, 450, BACKDROP);
        c.image("stars", 0, 0, 800, 450);
        c.fill(0, 0, 800, 450, 0xb80c1118);

        // Translucent dark footer drawn before buttons so APPLY PERK renders cleanly above it
        c.fill(0, 418, 800, 32, FOOTER);
        c.fill(0, 418, 800, 1, HAIRLINE_SOFT);

        // Left translucent weapon rail
        c.fill(26, 84, 200, 318, PANE);
        c.fill(26, 84, 200, 1, HAIRLINE);
        c.fill(26, 108, 200, 1, HAIRLINE_SOFT);
        c.text(36, 93, s.page == 0 ? "WEAPON PATTERNS" : "CARRIED WEAPONS", MUTED, .85f);

        int end = Math.min(s.rows.size(), s.scroll + 12);
        for (int i = s.scroll; i < end; i++) {
            Row r = s.rows.get(i);
            int y = 115 + (i - s.scroll) * 22;
            boolean sel = i == s.selected;
            boolean rowExotic = r.subtitle != null && r.subtitle.toLowerCase().contains("exotic");
            c.fill(29, y, 194, 21, sel ? 0x2effffff : 0x0effffff);
            c.fill(29, y, 2, 21, rowExotic ? EXOTIC_GOLD : LEGENDARY_PURPLE);
            if (sel) c.outline(29, y, 194, 21, WHITE);
            c.text(37, y + 7, r.title, sel ? WHITE : TEXT, .85f);
        }
        if (s.rows.isEmpty()) c.wrap(37, 140, "No guns in your inventory. Craft a weapon first.", 25, MUTED);
        c.text(36, 386, s.rows.size() + " weapons  /  SCROLL", DIM, .75f);

        // Center rarity header wash
        boolean exotic = s.subtitle != null && s.subtitle.toLowerCase().contains("exotic");
        int headerW = s.page == 0 ? 526 : 264;
        c.fill(246, 82, headerW, 38, exotic ? EXOTIC_WASH : LEGENDARY_WASH);
        c.fill(246, 82, 3, 38, exotic ? EXOTIC_GOLD : LEGENDARY_PURPLE);
        c.fill(246, 82, headerW, 1, HAIRLINE);
        c.text(256, 87, s.title, WHITE, 1.15f);
        c.text(256, 106, s.subtitle, TEXT, .82f);

        if (s.page == 0) {
            c.ring(374, 224, 112, 94, 0x26ffffff);
            c.ring(374, 224, 118, 100, 0x14ffffff);
            c.item(s.item, 272, 137, 202);

            c.fill(526, 128, 246, 224, PANE);
            c.fill(526, 128, 246, 1, HAIRLINE);
            c.text(540, 139, "SHAPING MATERIALS", MUTED, .85f);
            c.line(540, 158, 757, 158, HAIRLINE_SOFT);
            int i = 0;
            for (Cost cost : s.costs) {
                int y = 175 + i++ * 43;
                boolean enough = cost.have >= cost.need;
                c.text(540, y, cost.name, TEXT, .8f);
                c.text(540, y + 13, cost.have + " / " + cost.need, enough ? WHITE : DANGER, .95f);
                c.fill(540, y + 27, 217, 2, 0x26ffffff);
                int fillW = cost.need <= 0 ? 217 : Math.min(217, (int) Math.round(217.0 * cost.have / cost.need));
                c.fill(540, y + 27, fillW, 2, enough ? WHITE : DANGER);
            }
            c.fill(246, 336, 266, 66, PANE);
            c.fill(246, 336, 266, 1, HAIRLINE_SOFT);
            c.wrap(254, 344, s.intrinsic, 38, TEXT);
            button(c, 526, 363, 246, s.canApply ? "CRAFT WEAPON" : "MATERIALS MISSING", s.canApply);
        } else {
            c.ring(375, 182, 70, 47, 0x26ffffff);
            c.item(s.item, 297, 123, 155);

            c.fill(520, 82, 253, 154, PANE);
            c.fill(520, 82, 253, 1, HAIRLINE);
            String[] titles = {"BARREL", "MAGAZINE", "TRAIT 1", "TRAIT 2", "MASTERWORK", "MOD"};
            c.text(532, 94, s.sockets.length == 6 ? "SELECT " + titles[s.socket] : "SELECT A WEAPON", MUTED, .82f);
            c.line(532, 110, 761, 110, HAIRLINE_SOFT);
            int oi = 0;
            for (Row r : s.options) {
                int y = 117 + oi++ * 21;
                boolean sel = r.id == s.chosen;
                c.fill(526, y, 240, 19, sel ? 0x384f8cc9 : 0x10ffffff);
                c.outline(526, y, 240, 19, sel ? WHITE : HAIRLINE_SOFT);
                c.fill(532, y + 6, 7, 7, sel ? PERK_BLUE : 0x38ffffff);
                c.text(544, y + 6, r.title, sel ? WHITE : TEXT, .82f);
            }

            for (int slot = 0; slot < 6; slot++) {
                int x = 246 + (slot % 3) * 176, y = 244 + (slot / 3) * 52;
                boolean sel = slot == s.socket;
                c.fill(x, y, 168, 44, sel ? 0x384f8cc9 : PANE);
                c.outline(x, y, 168, 44, sel ? WHITE : HAIRLINE_SOFT);
                c.fill(x + 8, y + 8, 8, 8, sel ? PERK_BLUE : 0x664f8cc9);
                c.text(x + 21, y + 8, titles[slot], sel ? WHITE : MUTED, .72f);
                c.text(x + 9, y + 25, s.sockets.length == 6 ? s.sockets[slot] : "--", TEXT, .8f);
            }
            c.wrap(248, 356, s.description, 72, TEXT);
            c.text(248, 391, "Reshape cost: 1 Ether Alloy + 2 Copper Ingots  /  Creative: free", MUTED, .78f);
            button(c, 631, 411, 141, s.canApply ? "APPLY PERK" : "NO CHANGE", s.canApply);
        }

        if (!s.status.isEmpty()) c.text(246, 429, s.status, 0xff8fd0ff, .82f);
        c.text(26, 429, "[ESC] Close    [TAB] Switch Franchise", MUTED, .82f);
    }

    private static void button(UiCanvas c, int x, int y, int w, String label, boolean enabled) {
        c.fill(x, y, w, 26, enabled ? 0x26ffffff : 0x0cffffff);
        c.outline(x, y, w, 26, enabled ? WHITE : HAIRLINE_SOFT);
        c.text(x + 12, y + 9, label, enabled ? WHITE : DIM, .85f);
    }
}
