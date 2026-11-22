package com.jax.destiny2mc.binder.difficulty;

import com.jax.destiny2mc.client.HudSettingsScreen;
import java.util.EnumMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Destiny 2 styled Settings pane for Encounter Difficulty. Non-operators may inspect all four presets. */
public final class DifficultyScreen extends Screen {
    private static final int BACKDROP = 0xdc090d13, PANE = 0xe8101620, TOPBAR = 0xf20b1017,
        ROW = 0x14ffffff, ROW_HOVER = 0x2cffffff, ROW_ACTIVE = 0x36ffffff,
        HAIRLINE = 0x40ffffff, HAIRLINE_SOFT = 0x20ffffff,
        TEXT = 0xfff2f4f7, MUTED = 0xff9aa7b6, GOLD = 0xffceae33;

    private final Screen previous;
    private final EnumMap<EncounterPreset, Button> choices = new EnumMap<>(EncounterPreset.class);
    private EncounterPreset current, selected;
    private int revision;
    private boolean editable, waiting;
    private Button apply;
    private int left, top, panelWidth, panelHeight, railWidth, contentLeft, contentWidth;

    public DifficultyScreen(Screen previous) {
        super(Component.literal("Settings"));
        this.previous = previous;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override protected void init() {
        choices.clear();
        panelWidth = Math.min(440, width - 16);
        left = (width - panelWidth) / 2;
        panelHeight = Math.min(252, height - 12);
        top = (height - panelHeight) / 2;
        railWidth = panelWidth >= 340 ? 114 : 92;
        contentLeft = left + railWidth + 10;
        contentWidth = panelWidth - railWidth - 20;

        int catY = top + 39;
        addRenderableWidget(new CategoryButton(
            Button.builder(Component.literal("HUD & Recoil"), b -> {
                if (previous instanceof HudSettingsScreen) minecraft.setScreen(previous);
                else minecraft.setScreen(new HudSettingsScreen());
            }).bounds(left + 8, catY, railWidth - 12, 22), false));
        addRenderableWidget(new CategoryButton(
            Button.builder(Component.literal("Difficulty"), b -> {})
                .bounds(left + 8, catY + 26, railWidth - 12, 22), true));

        int presetW = (contentWidth - 12) / 4;
        int presetY = top + 52;
        for (var p : EncounterPreset.values()) {
            int i = p.ordinal();
            choices.put(p, addRenderableWidget(new PresetButton(
                Button.builder(Component.literal(p.title), b -> { selected = p; refresh(); })
                    .bounds(contentLeft + i * (presetW + 4), presetY, presetW, 24), p)));
        }

        int footerY = top + panelHeight - 27;
        int actionW = (contentWidth - 6) / 2;
        apply = addRenderableWidget(new ActionButton(
            Button.builder(Component.literal("Apply to world"), b -> {
                if (!editable || selected == null || waiting) return;
                waiting = true; refresh();
                PacketDistributor.sendToServer(new DifficultyNetwork.Request(selected.id, revision));
            }).bounds(contentLeft, footerY, actionW, 19), true));
        addRenderableWidget(new ActionButton(
            Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(contentLeft + actionW + 6, footerY, actionW, 19), false));
        refresh();
        PacketDistributor.sendToServer(new DifficultyNetwork.Request("", -1));
    }

    private final class CategoryButton extends Button {
        private final boolean selectedCat;
        CategoryButton(Builder builder, boolean selectedCat) {
            super(builder);
            this.selectedCat = selectedCat;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            boolean over = isHoveredOrFocused();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            g.fill(x, y, x + w, y + h, selectedCat ? ROW_ACTIVE : over ? ROW_HOVER : ROW);
            g.renderOutline(x, y, w, h, selectedCat || over ? 0xffffffff : HAIRLINE_SOFT);
            if (selectedCat) g.fill(x, y, x + 2, y + h, GOLD);
            g.drawString(font, getMessage(), x + 8, y + (h - font.lineHeight) / 2 + 1, selectedCat || over ? 0xffffffff : MUTED, false);
        }
    }

    private final class PresetButton extends Button {
        private final EncounterPreset preset;
        PresetButton(Builder builder, EncounterPreset preset) {
            super(builder);
            this.preset = preset;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            boolean chosen = preset != null && preset == selected;
            boolean over = isHoveredOrFocused();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            g.fill(x, y, x + w, y + h, chosen ? ROW_ACTIVE : over ? ROW_HOVER : ROW);
            g.renderOutline(x, y, w, h, chosen || over ? 0xffffffff : HAIRLINE_SOFT);
            if (chosen) g.fill(x, y + h - 2, x + w, y + h, GOLD);
            int color = chosen || over ? 0xffffffff : TEXT;
            g.drawString(font, getMessage(), x + (w - font.width(getMessage())) / 2, y + (h - font.lineHeight) / 2, color, false);
        }
    }

    private final class ActionButton extends Button {
        private final boolean primary;
        ActionButton(Builder builder, boolean primary) {
            super(builder);
            this.primary = primary;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            boolean over = isHoveredOrFocused() && active;
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int bg = !active ? 0x0cffffff : over ? ROW_HOVER : primary ? 0x24ffffff : ROW;
            int border = !active ? HAIRLINE_SOFT : (over || primary ? 0xffffffff : HAIRLINE);
            int fg = !active ? 0xff6e7b8b : (over ? 0xffffffff : TEXT);
            g.fill(x, y, x + w, y + h, bg);
            g.renderOutline(x, y, w, h, border);
            g.drawString(font, getMessage(), x + (w - font.width(getMessage())) / 2, y + (h - font.lineHeight) / 2 + 1, fg, false);
        }
    }

    private void refresh() {
        if (apply != null) apply.active = editable && !waiting && selected != null && selected != current;
    }

    public static void receive(DifficultyNetwork.State msg) {
        var mc = Minecraft.getInstance();
        if (msg.open() && !(mc.screen instanceof DifficultyScreen)) mc.setScreen(new DifficultyScreen(mc.screen));
        if (!(mc.screen instanceof DifficultyScreen s)) return;
        var next = EncounterPreset.find(msg.key());
        if (next == null) return;
        if (s.selected == null || s.waiting || s.current != next) s.selected = next;
        s.current = next; s.revision = msg.revision(); s.editable = msg.editable(); s.waiting = false; s.refresh();
    }

    @Override public void onClose() { minecraft.setScreen(previous); }
    private static int percent(float n) { return Math.round(100 * n); }
    @Override public void renderBackground(GuiGraphics g, int mx, int my, float dt) {}

    @Override public void render(GuiGraphics g, int mx, int my, float dt) {
        g.fill(0, 0, width, height, BACKDROP);
        g.fill(left, top, left + panelWidth, top + panelHeight, PANE);
        g.fill(left, top, left + panelWidth, top + 24, TOPBAR);
        g.renderOutline(left, top, panelWidth, panelHeight, HAIRLINE);
        g.fill(left, top + 24, left + panelWidth, top + 25, HAIRLINE);
        g.fill(left + railWidth, top + 25, left + railWidth + 1, top + panelHeight - 1, HAIRLINE_SOFT);
        g.drawString(font, "SETTINGS  /  ENCOUNTER DIFFICULTY", left + 10, top + 8, TEXT, false);
        g.drawString(font, "CATEGORIES", left + 10, top + 28, MUTED, false);

        String live = current == null ? "Reading world settings..." : "Active world preset: " + current.title;
        g.drawString(font, live, contentLeft, top + 36, current == null ? MUTED : GOLD, false);

        if (selected != null) {
            int rowY = top + 88;
            drawStatRow(g, contentLeft, rowY, contentWidth, "ENEMY DAMAGE",
                percent(selected.rankScale) + "% regular, " + percent(selected.bossScale) + "% boss");
            String limits = selected == EncounterPreset.ORIGINAL ? "Uncapped"
                : (int) selected.rankCap + " HP regular, " + (int) selected.bossCap + " HP boss";
            drawStatRow(g, contentLeft, rowY + 26, contentWidth, "HIT CAPS", limits);
            drawStatRow(g, contentLeft, rowY + 52, contentWidth, "RAPID FOLLOW-UP",
                percent(selected.followUp) + "% damage");
        }
        if (current != null) {
            String note = editable ? "Applies to all players in this world." : "World owner or operator access required to edit.";
            g.drawString(font, note, contentLeft, top + panelHeight - 44, MUTED, false);
        }
        super.render(g, mx, my, dt);
    }

    private void drawStatRow(GuiGraphics g, int x, int y, int w, String label, String value) {
        g.fill(x, y, x + w, y + 22, 0x10ffffff);
        g.renderOutline(x, y, w, 22, HAIRLINE_SOFT);
        g.drawString(font, label, x + 8, y + 7, MUTED, false);
        g.drawString(font, value, x + w - font.width(value) - 8, y + 7, TEXT, false);
    }
}
