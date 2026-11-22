package com.jax.destiny2mc.client;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.binder.client.BinderHud;
import com.jax.destiny2mc.binder.difficulty.DifficultyScreen;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Destiny 2 styled Settings screen hosting HUD & Recoil and linking to Encounter Difficulty. */
public class HudSettingsScreen extends Screen {
    private static final int BACKDROP = 0xdc090d13, PANE = 0xe8101620, TOPBAR = 0xf20b1017,
        ROW = 0x14ffffff, ROW_HOVER = 0x2cffffff, ROW_ACTIVE = 0x36ffffff,
        HAIRLINE = 0x40ffffff, HAIRLINE_SOFT = 0x20ffffff,
        TEXT = 0xfff2f4f7, MUTED = 0xff9aa7b6, GOLD = 0xffceae33, PERK_BLUE = 0xff5c9ed6;

    // IDs 0-8 are retained for older SettingsButtonAction callers.
    private static final int THEME = 0, SIZE = 1, RECOIL = 2, ITEM_ICON = 3, PERKS = 4,
        RETICLE = 5, DAMAGE = 6, RESET = 7, DONE = 8, ABILITIES = 9;
    private final Button[] controls = new Button[10];
    private int top, left, panelWidth, panelHeight, railWidth, contentLeft, contentWidth, previewTop, previewHeight;
    private ItemStack sampleGun;

    public HudSettingsScreen() {
        super(Component.literal("Settings"));
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void init() {
        super.init();
        HudConfig.ensureLoaded();
        sampleGun = ModRegistries.createWeaponStackByIndex(5);
        panelWidth = Math.min(440, width - 16);
        left = (width - panelWidth) / 2;
        panelHeight = Math.min(252, height - 12);
        top = (height - panelHeight) / 2;
        railWidth = panelWidth >= 340 ? 114 : 92;
        contentLeft = left + railWidth + 10;
        contentWidth = panelWidth - railWidth - 20;

        int catY = top + 39;
        addRenderableWidget(new CategoryButton(
            Button.builder(Component.literal("HUD & Recoil"), b -> {})
                .bounds(left + 8, catY, railWidth - 12, 22), true));
        addRenderableWidget(new CategoryButton(
            Button.builder(Component.literal("Difficulty"), b -> minecraft.setScreen(new DifficultyScreen(this)))
                .bounds(left + 8, catY + 26, railWidth - 12, 22), false));

        int buttonHeight = panelHeight < 212 ? 16 : 19;
        int gap = panelHeight < 212 ? 2 : 3;
        int buttonWidth = (contentWidth - 4) / 2;
        int right = contentLeft + contentWidth - buttonWidth;
        int y = top + 34;
        int[] order = {THEME, SIZE, RECOIL, ITEM_ICON, PERKS, RETICLE, DAMAGE, ABILITIES};
        for (int i = 0; i < order.length; i++) {
            addControl(order[i], (i % 2 == 0) ? contentLeft : right, y + (i / 2) * (buttonHeight + gap), buttonWidth, buttonHeight);
        }
        previewTop = y + 4 * (buttonHeight + gap) + 4;
        int footer = top + panelHeight - buttonHeight - 8;
        previewHeight = Math.max(0, footer - 5 - previewTop);
        addControl(RESET, contentLeft, footer, buttonWidth, buttonHeight);
        addControl(DONE, right, footer, buttonWidth, buttonHeight);
    }

    private void addControl(int id, int x, int y, int w, int h) {
        controls[id] = addRenderableWidget(new SettingButton(id, Button.builder(label(id), new SettingsButtonAction(id))
            .bounds(x, y, w, h)));
    }

    private final class CategoryButton extends Button {
        private final boolean selected;
        CategoryButton(Builder builder, boolean selected) {
            super(builder);
            this.selected = selected;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            boolean over = isHoveredOrFocused();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            g.fill(x, y, x + w, y + h, selected ? ROW_ACTIVE : over ? ROW_HOVER : ROW);
            g.renderOutline(x, y, w, h, selected || over ? 0xffffffff : HAIRLINE_SOFT);
            if (selected) g.fill(x, y, x + 2, y + h, GOLD);
            g.drawString(font, getMessage(), x + 8, y + (h - font.lineHeight) / 2 + 1, selected || over ? 0xffffffff : MUTED, false);
        }
    }

    private final class SettingButton extends Button {
        private final int controlId;
        SettingButton(int controlId, Builder builder) {
            super(builder);
            this.controlId = controlId;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            boolean over = isHoveredOrFocused();
            boolean primary = controlId == DONE;
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int bg = over ? ROW_HOVER : primary ? 0x24ffffff : ROW;
            int border = over || primary ? 0xffffffff : HAIRLINE_SOFT;
            g.fill(x, y, x + w, y + h, bg);
            g.renderOutline(x, y, w, h, border);
            if (isToggle(controlId)) {
                boolean on = toggleState(controlId);
                g.fill(x + 5, y + (h - 5) / 2, x + 9, y + (h + 5) / 2, on ? PERK_BLUE : 0x449aa7b6);
                g.drawString(font, getMessage(), x + 13, y + (h - font.lineHeight) / 2 + 1, over ? 0xffffffff : TEXT, false);
            } else {
                g.drawString(font, getMessage(), x + (w - font.width(getMessage())) / 2, y + (h - font.lineHeight) / 2 + 1, over ? 0xffffffff : TEXT, false);
            }
        }
    }

    private static boolean isToggle(int id) {
        return id == ITEM_ICON || id == PERKS || id == RETICLE || id == DAMAGE || id == ABILITIES;
    }

    private static boolean toggleState(int id) {
        return switch (id) {
            case ITEM_ICON -> HudConfig.showIcon;
            case PERKS -> HudConfig.showPerks;
            case RETICLE -> HudConfig.customCrosshair;
            case DAMAGE -> HudConfig.showDamageNums;
            case ABILITIES -> HudConfig.showAbilities;
            default -> false;
        };
    }

    private static Component label(int id) {
        return switch (id) {
            case THEME -> Component.literal("Theme: " + HudConfig.getStyleName());
            case SIZE -> Component.literal("HUD size: " + HudConfig.getScaleName());
            case RECOIL -> Component.literal("Recoil: " + HudConfig.getRecoilName());
            case ITEM_ICON -> toggle("3D gun icon", HudConfig.showIcon);
            case PERKS -> toggle("Perk banners", HudConfig.showPerks);
            case RETICLE -> toggle("Custom reticle", HudConfig.customCrosshair);
            case DAMAGE -> toggle("Damage popups", HudConfig.showDamageNums);
            case ABILITIES -> toggle("Ability HUD", HudConfig.showAbilities);
            case RESET -> Component.literal("Reset appearance");
            case DONE -> CommonComponents.GUI_DONE;
            default -> throw new IllegalArgumentException("Unknown HUD control " + id);
        };
    }

    private static Component toggle(String name, boolean enabled) {
        return Component.literal(name + ": ").append(enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    public static void handleAction(int id) {
        var mc = Minecraft.getInstance();
        switch (id) {
            case THEME -> HudConfig.hudStyle = (HudConfig.hudStyle + 1) % 6;
            case SIZE -> HudConfig.scalePreset = (HudConfig.scalePreset + 1) % 4;
            case RECOIL -> HudConfig.recoilPreset = (HudConfig.recoilPreset + 1) % 4;
            case ITEM_ICON -> HudConfig.showIcon = !HudConfig.showIcon;
            case PERKS -> HudConfig.showPerks = !HudConfig.showPerks;
            case RETICLE -> HudConfig.customCrosshair = !HudConfig.customCrosshair;
            case DAMAGE -> HudConfig.showDamageNums = !HudConfig.showDamageNums;
            case ABILITIES -> HudConfig.showAbilities = !HudConfig.showAbilities;
            case RESET -> HudConfig.resetDefaults();
            case DONE -> { mc.setScreen(null); return; }
            default -> { return; }
        }
        HudConfig.save();
        if (mc.screen instanceof HudSettingsScreen settings) {
            // Updating labels in place retains keyboard focus, narration and mouse position.
            for (int control = 0; control < settings.controls.length; control++) {
                if (settings.controls[control] != null) settings.controls[control].setMessage(label(control));
            }
        }
    }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partial) {}

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        graphics.fill(0, 0, width, height, BACKDROP);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, PANE);
        graphics.fill(left, top, left + panelWidth, top + 24, TOPBAR);
        graphics.renderOutline(left, top, panelWidth, panelHeight, HAIRLINE);
        graphics.fill(left, top + 24, left + panelWidth, top + 25, HAIRLINE);
        graphics.fill(left + railWidth, top + 25, left + railWidth + 1, top + panelHeight - 1, HAIRLINE_SOFT);
        graphics.drawString(font, "SETTINGS  /  HUD & RECOIL", left + 10, top + 8, TEXT, false);
        graphics.drawString(font, "CATEGORIES", left + 10, top + 28, MUTED, false);
        super.render(graphics, mouseX, mouseY, partial);
        if (previewHeight < 18) return;
        graphics.fill(contentLeft, previewTop, contentLeft + contentWidth, previewTop + previewHeight, 0xc8090d13);
        graphics.renderOutline(contentLeft, previewTop, contentWidth, previewHeight, HAIRLINE_SOFT);
        boolean withLabel = previewHeight >= 48;
        if (withLabel) graphics.drawString(font, "LIVE PREVIEW", contentLeft + 6, previewTop + 4, MUTED, false);
        var held = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getMainHandItem();
        ItemStack stack = held.getItem() instanceof DestinyGunItem ? held : sampleGun;
        WeaponSpec spec = ((DestinyGunItem) stack.getItem()).spec;
        int insetTop = withLabel ? 14 : 3;
        BinderHud.renderPreview(graphics, stack, spec, partial, contentLeft + 5, previewTop + insetTop,
            contentWidth - 10, previewHeight - insetTop - 3);
    }
}
