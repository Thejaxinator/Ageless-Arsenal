package com.jax.destiny2mc.client.ui;

/** Persistent two-tier header for both Gunsmith blocks. Drawing and input share the same
 * rectangles in the 800 x 450 design space; GunsmithScreen supplies the inverse transform. */
public final class GunsmithNavigation {
    public static final int DESTINY = 0, WARFRAME = 1, FIRST_TOOL = 0, SECOND_TOOL = 1;
    private record Tab(int x, int y, int width, int height) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + width && my >= y && my < y + height;
        }
    }
    private static final Tab[] FRANCHISES = {new Tab(397, 6, 184, 30), new Tab(588, 6, 184, 30)};
    private static final Tab[] TOOLS = {new Tab(421, 40, 159, 24), new Tab(597, 40, 175, 24)};
    private static final String[] FRANCHISE_NAMES = {"Destiny", "Warframe"};
    private static final String[][] TOOL_NAMES = {{"Crafting", "Perks"}, {"Blueprints", "Weapon Mods"}};
    private static final int IVORY = 0xfff2f4f7;
    private GunsmithNavigation() {}

    public static int franchiseAt(double x, double y) { return hit(FRANCHISES, x, y); }
    public static int toolAt(double x, double y) { return hit(TOOLS, x, y); }
    private static int hit(Tab[] tabs, double x, double y) {
        for (int i = 0; i < tabs.length; i++) if (tabs[i].contains(x, y)) return i;
        return -1;
    }
    public static String location(int franchise, int tool) {
        return FRANCHISE_NAMES[franchise] + ", " + TOOL_NAMES[franchise][tool];
    }

    public static void draw(UiCanvas c, int franchise, int tool, double mx, double my) {
        boolean tenno = franchise == WARFRAME;
        if (!tenno) {
            drawDestiny(c, franchise, tool, mx, my);
            return;
        }
        int field = 0xff1b2825;
        int edge = 0xff52645a;
        int text = 0xffafc3b5;
        int accent = 0xffa8d6c0;
        int active = 0xffddcfad;
        int ink = 0xff24352b;
        c.text(27, 13, "Gunsmith", 0xffebe8db, 2.05f);
        c.text(28, 43, "TENNO FOUNDRY", text, .88f);
        c.fill(27, 68, 745, 1, edge);

        for (int i = 0; i < FRANCHISES.length; i++) {
            Tab box = FRANCHISES[i];
            boolean current = franchise == i;
            boolean hovered = box.contains(mx, my);
            int color = current ? ink : (hovered ? 0xffebe8db : text);
            c.fill(box.x, box.y, box.width, box.height, current ? active : field);
            c.outline(box.x, box.y, box.width, box.height, current ? active : (hovered ? accent : edge));
            mark(c, i, box.x + 18, box.y + 15, color);
            c.text(box.x + 38, box.y + 9, FRANCHISE_NAMES[i], color, 1.30f);
            if (current) c.fill(box.x, box.y + box.height - 3, box.width, 3, accent);
        }
        int branch = FRANCHISES[franchise].x + 9;
        c.fill(branch, 36, 1, 3, accent);
        c.fill(Math.min(408, branch), 38, Math.abs(branch - 408) + 1, 1, edge);
        c.fill(408, 38, 1, 15, edge);
        c.fill(408, 52, 11, 1, edge);
        for (int i = 0; i < TOOLS.length; i++) {
            Tab box = TOOLS[i];
            boolean current = i == tool, hovered = box.contains(mx, my);
            int color = current || hovered ? 0xffebe8db : text;
            if (current || hovered) c.fill(box.x, box.y, box.width, box.height, field);
            int x = box.x + 11, y = box.y + 12;
            c.diamond(x, y, 3, current ? accent : edge);
            if (current) c.fill(x - 1, y - 1, 3, 3, accent);
            c.text(x + 12, box.y + 7, TOOL_NAMES[franchise][i], color, 1.02f);
            c.fill(box.x, box.y + box.height - 2, box.width, current ? 2 : 1,
                current || hovered ? accent : edge);
        }
    }

    private static void drawDestiny(UiCanvas c, int franchise, int tool, double mx, double my) {
        int headerGlass = 0xdc0c1016;
        int hairline = 0x38ffffff;
        int hairlineSoft = 0x20ffffff;
        int muted = 0xff9aa7b6;
        int gold = 0xffceae33;
        c.fill(0, 0, 800, 69, headerGlass);
        c.fill(0, 68, 800, 1, hairline);
        c.text(27, 13, "Gunsmith", IVORY, 2.05f);
        c.text(28, 43, "BANSHEE-44  /  THE TOWER", muted, .88f);

        for (int i = 0; i < FRANCHISES.length; i++) {
            Tab box = FRANCHISES[i];
            boolean current = franchise == i;
            boolean hovered = box.contains(mx, my);
            int bg = current ? 0x2effffff : (hovered ? 0x1effffff : 0x10ffffff);
            int border = current ? 0xffffffff : (hovered ? 0x80ffffff : hairlineSoft);
            int color = current || hovered ? 0xffffffff : muted;
            c.fill(box.x, box.y, box.width, box.height, bg);
            c.outline(box.x, box.y, box.width, box.height, border);
            mark(c, i, box.x + 18, box.y + 15, current ? gold : color);
            c.text(box.x + 38, box.y + 9, FRANCHISE_NAMES[i], color, 1.30f);
            if (current) c.fill(box.x, box.y + box.height - 2, box.width, 2, 0xffffffff);
        }
        int branch = FRANCHISES[franchise].x + 9;
        c.fill(branch, 36, 1, 3, 0xffffffff);
        c.fill(Math.min(408, branch), 38, Math.abs(branch - 408) + 1, 1, hairline);
        c.fill(408, 38, 1, 15, hairline);
        c.fill(408, 52, 11, 1, hairline);
        for (int i = 0; i < TOOLS.length; i++) {
            Tab box = TOOLS[i];
            boolean current = i == tool, hovered = box.contains(mx, my);
            int bg = current ? 0x26ffffff : (hovered ? 0x18ffffff : 0x0cffffff);
            int color = current || hovered ? 0xffffffff : muted;
            c.fill(box.x, box.y, box.width, box.height, bg);
            if (current || hovered) c.outline(box.x, box.y, box.width, box.height, current ? 0xffffffff : hairline);
            int x = box.x + 11, y = box.y + 12;
            c.diamond(x, y, 3, current ? 0xffffffff : hairline);
            if (current) c.fill(x - 1, y - 1, 3, 3, 0xffffffff);
            c.text(x + 12, box.y + 7, TOOL_NAMES[franchise][i], color, 1.02f);
            c.fill(box.x, box.y + box.height - 2, box.width, current ? 2 : 1,
                current ? 0xffffffff : (hovered ? 0x80ffffff : hairlineSoft));
        }
    }

    private static void mark(UiCanvas c, int franchise, int x, int y, int color) {
        if (franchise == DESTINY) {
            c.fill(x - 7, y - 6, 4, 6, color); c.fill(x + 3, y - 6, 4, 6, color);
            c.line(x - 7, y, x - 2, y + 5, color); c.line(x + 7, y, x + 2, y + 5, color);
            c.fill(x - 1, y - 3, 3, 10, color);
        } else {
            c.diamond(x, y, 5, color);
            c.line(x - 8, y - 5, x - 5, y + 2, color); c.line(x + 8, y - 5, x + 5, y + 2, color);
            c.line(x - 5, y + 2, x, y + 7, color); c.line(x + 5, y + 2, x, y + 7, color);
        }
    }
}
