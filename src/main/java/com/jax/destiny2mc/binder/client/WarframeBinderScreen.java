package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.BinderAbility;
import com.jax.destiny2mc.binder.BinderCatalog;
import com.jax.destiny2mc.binder.BinderClientState;
import com.jax.destiny2mc.binder.BinderSnapshot;
import com.jax.destiny2mc.binder.WarframeBinderCatalog;
import com.jax.destiny2mc.tenno.abilities.WarframeId;
import com.jax.destiny2mc.tenno.client.VitruvianUi;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import static com.jax.destiny2mc.binder.client.BinderGraphics.clip;
import static com.jax.destiny2mc.binder.client.BinderGraphics.color;
import static com.jax.destiny2mc.binder.client.BinderGraphics.frame;
import static com.jax.destiny2mc.binder.client.BinderGraphics.text;

/** Separate Void-Bound powers screen inside the Energy Binder station, sharing the same
 * 4 active slots, 2 passive slots, Motes/XP unlock ledger, and Z/X/C/V HUD bindings. */
public final class WarframeBinderScreen extends Screen {
    private static final int W = 560, H = 330, ROWS = 6;
    private static final String[] TABS = {"Active", "Passive"};
    private int tab, scroll, slot;
    private String selected = "", focus = "", lastMessage = "";
    private Object signature;
    private boolean switchingScreen, pending, inspectFocus;
    private int pendingTicks;
    private BinderSnapshot beforeRequest;
    private final List<Control> controls = new ArrayList<>();
    private float scale;
    private int ox, oy;

    public WarframeBinderScreen() {
        super(Component.literal("Void-Bound Powers"));
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override protected void init() {
        scale = Math.min(width / (float) W, height / (float) H);
        ox = Math.round((width - W * scale) / 2);
        oy = Math.round((height - H * scale) / 2);
        signature = null;
        switchingScreen = false;
        BinderClientState.refresh();
        sync();
    }

    @Override public void removed() {
        if (!switchingScreen && minecraft != null && minecraft.getConnection() != null) {
            BinderClientState.close();
        }
    }

    @Override public void tick() {
        if (pending && (BinderClientState.snapshot() != beforeRequest || --pendingTicks <= 0)) pending = false;
        sync();
    }

    private List<BinderAbility> entries() {
        return BinderCatalog.all().stream()
                .filter(BinderAbility::isWarframe)
                .filter(a -> tab == 0 ? !a.isPassive() : a.isPassive())
                .toList();
    }

    private BinderAbility byId(String id) { return BinderCatalog.get(id); }

    private String installed(BinderSnapshot s, int index) {
        return index < 4 ? s.active().get(index) : s.passive().get(index - 4);
    }

    private void tab(int next) {
        tab = next;
        scroll = 0;
        selected = "";
        slot = next == 1 ? 4 : 0;
        focus = "tab" + tab;
        signature = null;
        sync();
    }

    private void scroll(int amount) {
        scroll = Math.clamp(scroll + amount, 0, Math.max(0, entries().size() - ROWS));
        signature = null;
        sync();
    }

    private void send(Runnable packet) {
        if (pending) return;
        beforeRequest = BinderClientState.snapshot();
        pending = true;
        pendingTicks = 40;
        packet.run();
        signature = null;
    }

    private void sync() {
        List<BinderAbility> list = entries();
        if (!list.isEmpty() && list.stream().noneMatch(a -> a.id().equals(selected))) {
            selected = list.getFirst().id();
        }
        BinderSnapshot state = BinderClientState.snapshot();
        Object next = List.of(tab, scroll, slot, selected, state, pending);
        if (next.equals(signature)) return;
        signature = next;
        if (getFocused() instanceof Control c) focus = c.id;
        clearWidgets();
        controls.clear();

        for (int i = 0; i < TABS.length; i++) {
            int t = i;
            add("tab" + i, TABS[i] + " Void-Bound abilities", 14 + i * 106, 50, 102, 19, true, () -> tab(t));
        }
        add("destiny", "Destiny abilities", 449, 50, 97, 19, true, () -> {
            switchingScreen = true;
            minecraft.setScreen(new EnergyBinderScreen());
        });

        for (int i = 0; i < ROWS && scroll + i < list.size(); i++) {
            BinderAbility a = list.get(scroll + i);
            add("entry" + a.id(), a.name() + ", " + (state.unlocked().contains(a.id()) ? "owned" : "locked"),
                    14, 79 + i * 24, 204, 22, true, () -> { selected = a.id(); }).ability = a;
        }
        add("prev", "Previous abilities", 14, 225, 24, 15, scroll > 0, () -> scroll(-ROWS));
        add("next", "More abilities", 193, 225, 25, 15, scroll + ROWS < list.size(), () -> scroll(ROWS));

        BinderAbility a = byId(selected);
        if (a != null) {
            boolean owned = state.unlocked().contains(selected);
            boolean available = owned
                    ? !installed(state, slot).equals(selected)
                    : state.motes() >= a.motes() && state.levels() >= a.levels();
            add("commit", owned ? "Bind " + a.name() + " to " + slotLabel(slot)
                            : "Unlock " + a.name() + " for " + a.motes() + " motes and " + a.levels() + " levels",
                    235, 211, 185, 27, available && !pending,
                    () -> send(() -> {
                        if (owned) BinderClientState.equip(a.id(), slot);
                        else BinderClientState.purchase(a.id());
                    }));
            add("clear", "Clear " + slotLabel(slot), 425, 211, 114, 27,
                    !installed(state, slot).isEmpty() && !pending,
                    () -> send(() -> BinderClientState.equip("", slot)));
        }

        for (int i = 0; i < 6; i++) {
            final int k = i;
            String id = installed(state, i);
            BinderAbility cur = byId(id);
            add("slot" + i, slotLabel(i) + ", " + (cur == null ? "empty" : cur.name()),
                    14 + i * 89, 267, 85, 40, true, () -> {
                        if ((tab == 1) != (k >= 4)) {
                            tab = k >= 4 ? 1 : 0;
                            scroll = 0;
                            selected = "";
                        }
                        slot = k;
                    }).ability = cur;
        }

        for (var c : controls) {
            if (c.id.equals(focus) && c.active) {
                setFocused(c);
                break;
            }
        }
        if (!state.message().isBlank() && !state.message().equals(lastMessage)) {
            lastMessage = state.message();
            minecraft.getNarrator().sayNow(Component.literal(lastMessage));
        }
    }

    private static String slotLabel(int i) {
        return i < 4 ? "active " + (i + 1) : "passive " + (i - 3);
    }

    private static String subtitleFor(BinderAbility a) {
        WarframeId wf = WarframeBinderCatalog.warframeFor(a.id());
        String frame = wf != null ? wf.displayName().toUpperCase(Locale.ROOT) : "VOID-BOUND";
        String role = switch (a.category()) {
            case "warframe_movement" -> "PARKOUR";
            case "warframe_passive" -> "PASSIVE";
            default -> "ABILITY";
        };
        return frame + "  /  " + role;
    }

    private final class Control extends AbstractWidget {
        final String id;
        final Runnable action;
        BinderAbility ability;

        Control(String id, String name, int x, int y, int w, int h, boolean enabled, Runnable action) {
            super(ox + Math.round(x * scale), oy + Math.round(y * scale),
                    Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)),
                    Component.literal(name));
            this.id = id;
            this.action = action;
            active = enabled;
        }

        void activate() {
            if (active) {
                focus = id;
                action.run();
                signature = null;
                sync();
            }
        }

        @Override public void onClick(double x, double y) { activate(); }

        @Override public boolean keyPressed(int k, int sc, int mods) {
            if (k == GLFW.GLFW_KEY_ENTER || k == GLFW.GLFW_KEY_SPACE || k == GLFW.GLFW_KEY_KP_ENTER) {
                activate();
                return true;
            }
            return false;
        }

        @Override protected void renderWidget(GuiGraphics g, int x, int y, float t) {}
        @Override protected void updateWidgetNarration(NarrationElementOutput out) { defaultButtonNarrationText(out); }
    }

    private Control add(String id, String name, int x, int y, int w, int h, boolean enabled, Runnable r) {
        var c = new Control(id, name, x, y, w, h, enabled, r);
        controls.add(c);
        addWidget(c);
        return c;
    }

    private boolean hover(String id, int mx, int my) {
        for (var c : controls) {
            if (c.id.equals(id)) return c.active && (c.isMouseOver(mx, my) || c.isFocused());
        }
        return false;
    }

    private void button(GuiGraphics g, String id, String label, int x, int y, int w, int h,
                        boolean primary, boolean enabled, int mx, int my) {
        boolean over = hover(id, mx, my);
        int bg = !enabled ? VitruvianUi.SLATE_BAR_DARK : primary ? VitruvianUi.GOLD : over ? VitruvianUi.ROW_ACTIVE : VitruvianUi.PANEL_ELEVATED;
        int fg = !enabled ? VitruvianUi.MUTED : primary ? VitruvianUi.FROST_INK : VitruvianUi.IVORY;
        int border = over ? VitruvianUi.WHITE_RAIL : primary && enabled ? VitruvianUi.WHITE_RAIL : VitruvianUi.BORDER_HAIRLINE;
        g.fill(x, y, x + w, y + h, bg);
        frame(g, x, y, w, h, border);
        if (primary && enabled) g.fill(x - 1, y - 1, x + w + 1, y, VitruvianUi.WHITE_RAIL);
        text(g, font, clip(font, label, w - 6), x + (w - Math.min(w - 6, font.width(label))) / 2, y + (h - 8) / 2, fg);
    }

    @Override public void renderBackground(GuiGraphics g, int x, int y, float t) {}

    @Override public void render(GuiGraphics g, int mx, int my, float t) {
        sync();
        var s = BinderClientState.snapshot();
        var list = entries();
        BinderAbility a = byId(selected);

        g.fill(0, 0, width, height, VitruvianUi.BACKDROP_DEEP);
        g.pose().pushPose();
        g.pose().translate(ox, oy, 0);
        g.pose().scale(scale, scale, 1);

        // Translucent charcoal Void-Bound station frame with crisp top overhang rail
        g.fillGradient(0, 0, W, H, 0xF0161B23, 0xF60C1016);
        g.fill(0, 0, W, 1, VitruvianUi.WHITE_RAIL);
        g.fill(14, 10, 17, 41, VitruvianUi.SKY_BLUE);
        text(g, font, "VOID-BOUND  ·  ENERGY BINDER", 23, 10, VitruvianUi.SKY_BLUE);
        g.pose().pushPose();
        g.pose().translate(22, 24, 0);
        g.pose().scale(1.7f, 1.7f, 1);
        text(g, font, "Void-Bound Powers", 0, 0, VitruvianUi.IVORY);
        g.pose().popPose();

        text(g, font, "MOTES OF LIGHT", 363, 12, VitruvianUi.MUTED);
        text(g, font, Integer.toString(s.motes()), 363, 27, VitruvianUi.GOLD);
        text(g, font, "XP LEVELS", 475, 12, VitruvianUi.MUTED);
        text(g, font, Integer.toString(s.levels()), 475, 27, VitruvianUi.IVORY);

        // Tabs & Destiny switch button
        for (int i = 0; i < TABS.length; i++) {
            int tx = 14 + i * 106;
            boolean activeTab = tab == i;
            boolean over = hover("tab" + i, mx, my);
            g.fill(tx, 50, tx + 102, 69, activeTab ? VitruvianUi.ROW_ACTIVE : over ? VitruvianUi.ROW_HOVER : VitruvianUi.PANEL_WELL);
            frame(g, tx, 50, 102, 19, over ? VitruvianUi.WHITE_RAIL : VitruvianUi.BORDER_HAIRLINE);
            if (activeTab) g.fill(tx, 67, tx + 102, 69, VitruvianUi.WHITE_RAIL);
            String lbl = TABS[i].toUpperCase(Locale.ROOT);
            text(g, font, lbl, tx + (102 - font.width(lbl)) / 2, 56, activeTab ? VitruvianUi.IVORY : VitruvianUi.MUTED);
        }
        button(g, "destiny", "‹ Destiny", 449, 50, 97, 19, false, true, mx, my);

        // Left Void-Bound ability catalog
        for (int i = 0; i < ROWS && scroll + i < list.size(); i++) {
            var row = list.get(scroll + i);
            int y = 79 + i * 24;
            boolean chosen = row.id().equals(selected);
            boolean owned = s.unlocked().contains(row.id());
            int accent = WarframeBinderCatalog.accentColor(row.id());
            if (chosen) {
                g.fill(14, y, 218, y + 22, VitruvianUi.FROST_LIGHT);
                g.fill(13, y - 1, 219, y, VitruvianUi.WHITE_RAIL);
                g.fill(14, y, 17, y + 22, accent);
            } else {
                g.fill(14, y, 218, y + 22, VitruvianUi.PANEL_FILL);
                g.fill(14, y, 16, y + 22, accent);
                g.fill(14, y + 21, 218, y + 22, VitruvianUi.RULE_SUBTLE);
            }
            AbilityGlyph.draw(g, row.id(), 20, y + 3, 16, chosen ? VitruvianUi.FROST_INK : accent);
            text(g, font, clip(font, row.name(), 142), 38, y + 7, chosen ? VitruvianUi.FROST_INK : VitruvianUi.IVORY);
            text(g, font, owned ? "✓" : "", 196, y + 7, chosen ? 0xFF1D6E2E : VitruvianUi.POSITIVE);
            if (hover("entry" + row.id(), mx, my)) frame(g, 14, y, 204, 22, VitruvianUi.WHITE_RAIL);
        }

        button(g, "prev", "‹", 14, 225, 24, 15, false, scroll > 0, mx, my);
        button(g, "next", "›", 193, 225, 25, 15, false, scroll + ROWS < list.size(), mx, my);
        text(g, font, (list.isEmpty() ? 0 : scroll + 1) + "–" + Math.min(scroll + ROWS, list.size()) + " / " + list.size(),
                86, 229, VitruvianUi.MUTED);

        // Right detail panel
        g.fill(229, 79, 546, 243, VitruvianUi.PANEL_FILL);
        frame(g, 229, 79, 317, 164, VitruvianUi.BORDER_HAIRLINE);
        if (a != null) {
            int accent = WarframeBinderCatalog.accentColor(a.id());
            g.fill(229, 79, 546, 81, accent);
            g.fill(235, 86, 276, 127, VitruvianUi.PANEL_WELL);
            frame(g, 235, 86, 41, 41, accent);
            AbilityGlyph.draw(g, a.id(), 239, 90, 32, accent);

            text(g, font, subtitleFor(a), 284, 89, accent);
            String n = clip(font, a.name(), 186);
            g.pose().pushPose();
            g.pose().translate(284, 104, 0);
            g.pose().scale(1.35f, 1.35f, 1);
            text(g, font, n, 0, 0, VitruvianUi.IVORY);
            g.pose().popPose();

            int yy = 133;
            for (var line : font.split(Component.literal(a.description()), 295)) {
                if (yy >= 175) break;
                g.drawString(font, line, 236, yy, VitruvianUi.MUTED, false);
                yy += 10;
            }
            String cooldown = a.cooldownTicks() > 0
                    ? "Cooldown  " + (a.cooldownTicks() / 20f == a.cooldownTicks() / 20
                            ? Integer.toString(a.cooldownTicks() / 20)
                            : String.format(Locale.ROOT, "%.1f", a.cooldownTicks() / 20f)) + " s"
                    : "Always on while bound";
            text(g, font, cooldown, 236, 176, VitruvianUi.SKY_BLUE_SOFT);
            g.fill(235, 189, 539, 190, VitruvianUi.BORDER_HAIRLINE);

            boolean owned = s.unlocked().contains(selected);
            boolean bound = installed(s, slot).equals(selected);
            boolean canPay = s.motes() >= a.motes() && s.levels() >= a.levels();
            text(g, font,
                    owned ? "Unlocked   /   Selected " + slotLabel(slot) : a.motes() + " Motes of Light  +  " + a.levels() + " XP levels",
                    236, 196, owned ? VitruvianUi.POSITIVE : canPay ? VitruvianUi.GOLD : VitruvianUi.MUTED);
            button(g, "commit", owned ? (bound ? "Bound" : "Bind to " + slotLabel(slot)) : "Unlock",
                    235, 211, 185, 27, true, !pending && (owned ? !bound : canPay), mx, my);
            button(g, "clear", "Clear slot",
                    425, 211, 114, 27, false, !pending && !installed(s, slot).isEmpty(), mx, my);
        }

        // Shared 4 Active + 2 Passive loadout slots
        text(g, font, "ACTIVE SLOTS", 14, 253, VitruvianUi.MUTED);
        text(g, font, "PASSIVES", 370, 253, VitruvianUi.MUTED);
        for (int i = 0; i < 6; i++) {
            int x = 14 + i * 89;
            var current = byId(installed(s, i));
            boolean chosen = slot == i;
            g.fill(x, 267, x + 85, 307, chosen ? VitruvianUi.ROW_ACTIVE : VitruvianUi.PANEL_FILL);
            frame(g, x, 267, 85, 40, hover("slot" + i, mx, my) ? VitruvianUi.WHITE_RAIL : chosen ? VitruvianUi.SKY_BLUE : VitruvianUi.BORDER_HAIRLINE);
            if (chosen) g.fill(x, 267, x + 85, 269, VitruvianUi.SKY_BLUE);
            String key = i < 4 ? BinderKeys.keyName(i) : "P" + (i - 3);
            text(g, font, clip(font, key, 69), x + 6, 272, chosen ? VitruvianUi.SKY_BLUE_SOFT : VitruvianUi.MUTED);
            if (current != null) {
                int glyphColor = current.isWarframe() ? WarframeBinderCatalog.accentColor(current.id()) : color(current.element());
                AbilityGlyph.draw(g, current.id(), x + 65, 268, 16, glyphColor);
            }
            String label = current == null ? "Empty" : current.name();
            int y = 284;
            for (var line : font.split(Component.literal(label), 74)) {
                if (y > 294) break;
                g.drawString(font, line, x + 6, y, current == null ? VitruvianUi.DISABLED : VitruvianUi.IVORY, false);
                y += 9;
            }
        }

        text(g, font, clip(font, pending ? "Waiting for the station…"
                : s.message().isBlank() ? "Mix Destiny & Void-Bound powers across your 6 binder slots." : s.message(), W - 28),
                14, 315, VitruvianUi.MUTED);
        g.pose().popPose();
        drawInspectCard(g, mx, my, a);
    }

    private void drawInspectCard(GuiGraphics g, int mx, int my, BinderAbility selectedAbility) {
        if (inspectFocus && getFocused() instanceof Control focused) {
            if (focused.ability != null) GearHoverCard.drawAbility(g, focused.ability, focused.getX() + focused.getWidth(), focused.getY());
            return;
        }
        for (Control c : controls) {
            if (c.isMouseOver(mx, my)) {
                if (c.ability != null) GearHoverCard.drawAbility(g, c.ability, mx, my);
                return;
            }
        }
        double x = (mx - ox) / scale, y = (my - oy) / scale;
        if (selectedAbility != null && x >= 235 && x < 540 && y >= 86 && y < 127) {
            GearHoverCard.drawAbility(g, selectedAbility, mx, my);
        }
    }

    @Override public void mouseMoved(double x, double y) { inspectFocus = false; super.mouseMoved(x, y); }

    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (x >= ox + 14 * scale && x < ox + 219 * scale && y >= oy + 79 * scale && y < oy + 240 * scale && dy != 0) {
            scroll(dy > 0 ? -2 : 2);
            return true;
        }
        return super.mouseScrolled(x, y, dx, dy);
    }

    @Override public boolean keyPressed(int key, int scan, int mods) {
        inspectFocus = true;
        if (key == GLFW.GLFW_KEY_PAGE_DOWN || key == GLFW.GLFW_KEY_PAGE_UP) {
            scroll(key == GLFW.GLFW_KEY_PAGE_DOWN ? ROWS : -ROWS);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }
}
