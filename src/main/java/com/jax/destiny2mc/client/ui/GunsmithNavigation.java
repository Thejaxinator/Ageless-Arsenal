package com.jax.destiny2mc.client.ui;

/** Persistent two-tier header for both Gunsmith blocks. Drawing and input share the same
 * rectangles in the 800 × 450 design space; GunsmithScreen supplies the inverse transform. */
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
    private static final int IVORY = 0xffebe8db;
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
        int field = tenno ? 0xff1b2825 : 0xff1e2c35;
        int edge = tenno ? 0xff52645a : 0xff526772;
        int text = tenno ? 0xffafc3b5 : 0xffb4c3c9;
        int accent = tenno ? 0xffa8d6c0 : 0xffdac492;
        int active = tenno ? 0xffddcfad : 0xffe8e3d6;
        int ink = tenno ? 0xff24352b : 0xff25323a;
        c.text(27, 13, "Gunsmith", IVORY, 2.05f);
        c.text(28, 43, tenno ? "TENNO FOUNDRY" : "BANSHEE-44  /  THE TOWER", text, .88f);
        c.fill(27, 68, 745, 1, edge);

        for (int i = 0; i < FRANCHISES.length; i++) {
            Tab box = FRANCHISES[i];
            boolean current = franchise == i;
            boolean hovered = box.contains(mx, my);
            int color = current ? ink : (hovered ? IVORY : text);
            c.fill(box.x, box.y, box.width, box.height, current ? active : field);
            c.outline(box.x, box.y, box.width, box.height, current ? active : (hovered ? accent : edge));
            mark(c, i, box.x + 18, box.y + 15, color);
            c.text(box.x + 38, box.y + 9, FRANCHISE_NAMES[i], color, 1.30f);
            if (current) c.fill(box.x, box.y + box.height - 3, box.width, 3, accent);
        }
        // Indent the tool bar and join it to its active game. A solid pip and underline
        // distinguish the active tool even when both franchise palettes appear in grayscale.
        int branch = FRANCHISES[franchise].x + 9;
        c.fill(branch, 36, 1, 3, accent);
        c.fill(Math.min(408, branch), 38, Math.abs(branch - 408) + 1, 1, edge);
        c.fill(408, 38, 1, 15, edge);
        c.fill(408, 52, 11, 1, edge);
        for (int i = 0; i < TOOLS.length; i++) {
            Tab box = TOOLS[i];
            boolean current = i == tool, hovered = box.contains(mx, my);
            int color = current ? IVORY : (hovered ? IVORY : text);
            if (current || hovered) c.fill(box.x, box.y, box.width, box.height, field);
            int x = box.x + 11, y = box.y + 12;
            c.diamond(x, y, 3, current ? accent : edge);
            if (current) c.fill(x - 1, y - 1, 3, 3, accent);
            c.text(x + 12, box.y + 7, TOOL_NAMES[franchise][i], color, 1.02f);
            c.fill(box.x, box.y + box.height - 2, box.width, current ? 2 : 1,
                current || hovered ? accent : edge);
        }
    }

    private static void mark(UiCanvas c, int franchise, int x, int y, int color) {
        if (franchise == DESTINY) {
            // Three shield plates form a small Tower insignia.
            c.fill(x - 7, y - 6, 4, 6, color); c.fill(x + 3, y - 6, 4, 6, color);
            c.line(x - 7, y, x - 2, y + 5, color); c.line(x + 7, y, x + 2, y + 5, color);
            c.fill(x - 1, y - 3, 3, 10, color);
        } else {
            // The Foundry sign traces an Orokin arch with a pointed central petal.
            c.diamond(x, y, 5, color);
            c.line(x - 8, y - 5, x - 5, y + 2, color); c.line(x + 8, y - 5, x + 5, y + 2, color);
            c.line(x - 5, y + 2, x, y + 7, color); c.line(x + 5, y + 2, x, y + 7, color);
        }
    }
}
