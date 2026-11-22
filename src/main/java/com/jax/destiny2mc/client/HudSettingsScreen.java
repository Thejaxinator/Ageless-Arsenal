package com.jax.destiny2mc.client;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.binder.client.BinderHud;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Client-only settings. The preview uses the gameplay telemetry renderer and current key bindings. */
public class HudSettingsScreen extends Screen {
    // IDs 0–8 are retained for older SettingsButtonAction callers.
    private static final int THEME=0, SIZE=1, RECOIL=2, ITEM_ICON=3, PERKS=4,
        RETICLE=5, DAMAGE=6, RESET=7, DONE=8, ABILITIES=9;
    private final Button[] controls = new Button[10];
    private int top, left, panelWidth, previewTop, previewHeight;
    private ItemStack sampleGun;

    public HudSettingsScreen() {
        super(Component.literal("HUD & recoil"));
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void init() {
        super.init();
        HudConfig.ensureLoaded();
        sampleGun = ModRegistries.createWeaponStackByIndex(5);
        panelWidth = Math.min(316, width - 12);
        left = (width - panelWidth) / 2;
        int heightAvailable = Math.min(229, height - 8);
        top = (height - heightAvailable) / 2;
        int buttonHeight = heightAvailable < 194 ? 16 : 20;
        int gap = heightAvailable < 194 ? 2 : 3;
        int buttonWidth = (panelWidth - 4) / 2;
        int right = left + panelWidth - buttonWidth;
        int y = top + 16;
        int[] order = {THEME, SIZE, RECOIL, ITEM_ICON, PERKS, RETICLE, DAMAGE, ABILITIES};
        for (int i=0; i<order.length; i++) {
            addControl(order[i], (i % 2 == 0) ? left : right, y + (i/2) * (buttonHeight + gap), buttonWidth, buttonHeight);
        }
        previewTop = y + 4 * (buttonHeight + gap) + 3;
        int footer = top + heightAvailable - buttonHeight;
        previewHeight = Math.max(0, footer - 4 - previewTop);
        addControl(RESET, left, footer, buttonWidth, buttonHeight);
        addControl(DONE, right, footer, buttonWidth, buttonHeight);
    }

    private void addControl(int id, int x, int y, int w, int h) {
        controls[id] = addRenderableWidget(Button.builder(label(id), new SettingsButtonAction(id))
            .bounds(x, y, w, h).build());
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
            case THEME -> HudConfig.hudStyle = (HudConfig.hudStyle+1) % 6;
            case SIZE -> HudConfig.scalePreset = (HudConfig.scalePreset+1) % 4;
            case RECOIL -> HudConfig.recoilPreset = (HudConfig.recoilPreset+1) % 4;
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
            for (int control=0; control<settings.controls.length; control++) {
                if (settings.controls[control] != null) settings.controls[control].setMessage(label(control));
            }
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        graphics.drawCenteredString(font, title, width/2, top+2, 0xfff4f4f4);
        if (previewHeight < 18) return;
        graphics.fill(left, previewTop, left+panelWidth, previewTop+previewHeight, 0xd31b2026);
        graphics.renderOutline(left, previewTop, panelWidth, previewHeight, 0xff535b61);
        boolean withLabel = previewHeight >= 51;
        if (withLabel) graphics.drawString(font, "Preview", left+6, previewTop+4, 0xffb7c2c4, false);
        var held = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getMainHandItem();
        ItemStack stack = held.getItem() instanceof DestinyGunItem ? held : sampleGun;
        WeaponSpec spec = ((DestinyGunItem)stack.getItem()).spec;
        int insetTop = withLabel ? 15 : 3;
        BinderHud.renderPreview(graphics, stack, spec, partial, left+5, previewTop+insetTop,
            panelWidth-10, previewHeight-insetTop-3);
    }
}
