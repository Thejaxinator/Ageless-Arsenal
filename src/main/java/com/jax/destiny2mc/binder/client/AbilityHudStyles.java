package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.BinderAbility;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Draws the four character bindings in the active ammo HUD's coordinate system.
 * All skins use the same ability definition, rebound key and server cooldown.
 * The settings preview can supply example slots without changing the live character. */
public final class AbilityHudStyles {
    public record Slot(BinderAbility ability, String key, int remainingTicks) {
        public Slot {
            remainingTicks = Math.max(0, remainingTicks);
        }
        boolean empty() { return ability == null; }
        boolean available() { return !empty() && remainingTicks == 0; }
        String id() { return empty() ? "" : ability.id(); }
        int charge(int pixels) {
            if (empty()) return 0;
            if (remainingTicks == 0) return pixels;
            long filled = pixels - (long) Math.ceil((double) pixels * remainingTicks / Math.max(1, ability.cooldownTicks()));
            return (int) Math.clamp(filled, 0, pixels);
        }
    }

    private static final int WHITE = 0xfff7f7ef;
    private static final int MUTED = 0xffa0a9ad;
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation BL2_READY = plate("bl2_ready");
    private static final ResourceLocation BL2_SPENT = plate("bl2_spent");
    private static final ResourceLocation BL4_PLATE = plate("bl4_plate");
    private static final int[] WIDTH = {102, 82, 98, 123, 119, 103};
    private static final int[] HEIGHT = {34, 32, 25, 34, 34, 29};
    private AbilityHudStyles() {}

    private static ResourceLocation plate(String name) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/abilities/" + name + ".png");
    }

    private static int validStyle(int style) { return (style >= 0 && style < WIDTH.length) ? style : 1; }
    public static int width(int style) { return WIDTH[validStyle(style)]; }
    public static int height(int style) { return HEIGHT[validStyle(style)]; }

    public static void draw(GuiGraphics g, int style, int x, int y, List<Slot> slots) {
        if (slots.size() != 4) return;
        Font font = Minecraft.getInstance().font;
        int skin = validStyle(style);
        if (skin == 1) minecraftTray(g, x, y);
        for (int i = 0; i < 4; i++) {
            Slot slot = slots.get(i);
            switch (skin) {
                case 0 -> destiny(g, font, x + i * 26, y, slot);
                case 2 -> minimal(g, font, x + i * 25, y, slot);
                case 3 -> borderlands2(g, font, x + i * 31, y, slot);
                case 4 -> borderlands4(g, font, x + i * 30, y, slot);
                case 5 -> tenno(g, font, x + i * 26, y, slot);
                default -> minecraft(g, font, x + 1 + i * 20, y, slot);
            }
        }
    }

    private static int elementInk(Slot slot) {
        return slot.empty() ? MUTED : BinderGraphics.color(slot.ability.element());
    }

    private static int subclassTile(Slot slot) {
        if (slot.empty()) return 0x92212932;
        return switch (slot.ability.element()) {
            case "arc" -> 0xe345829f;
            case "solar" -> 0xe3b46235;
            case "void" -> 0xe37b5497;
            case "stasis" -> 0xe3445c9a;
            case "strand" -> 0xe345804f;
            default -> 0xe38b5e86;
        };
    }

    private static void glyph(GuiGraphics g, Slot slot, int x, int y, int size, int argb) {
        AbilityGlyph.draw(g, slot.id(), x, y, size, argb);
    }

    /** Short key names keep left/right modifiers and mouse button numbers distinguishable.
     * Localized labels still have the full width of the slot and may shrink before eliding. */
    private static String compactKey(String key) {
        if (key == null || key.isBlank()) return "–";
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "left shift" -> "LSh";
            case "right shift" -> "RSh";
            case "left control", "left ctrl" -> "LCtl";
            case "right control", "right ctrl" -> "RCtl";
            case "left alt" -> "LAlt";
            case "right alt" -> "RAlt";
            case "left button" -> "M1";
            case "right button" -> "M2";
            case "middle button" -> "M3";
            case "not bound" -> "–";
            default -> key.startsWith("Button ") ? "M" + key.substring(7) :
                key.startsWith("Mouse Button ") ? "M" + key.substring(13) : key;
        };
    }

    private static void label(GuiGraphics g, Font font, Slot slot, int cx, int y, int width, int color) {
        String key = compactKey(slot.key);
        float scale = Math.clamp(width / (float) Math.max(1, font.width(key)), 0.72f, 1f);
        String display = BinderGraphics.clip(font, key, (int) (width / scale));
        var pose = g.pose();
        pose.pushPose();
        pose.translate(cx, y, 0);
        pose.scale(scale, scale, 1);
        g.drawString(font, display, -font.width(display) / 2, 0, color, true);
        pose.popPose();
    }

    /** The remaining seconds stay readable while the technique symbol remains visible. */
    private static void timer(GuiGraphics g, Font font, Slot slot, int right, int y, int ink) {
        if (slot.empty() || slot.remainingTicks == 0) return;
        String seconds = Long.toString((slot.remainingTicks + 19L) / 20L);
        int x = right - font.width(seconds);
        // Cooldowns cross both the stencil and the world. A tight outline preserves the
        // counterform of each numeral against pale glyphs without boxing the Tenno row.
        int outline = 0xff202328;
        g.drawString(font, seconds, x - 1, y, outline, false);
        g.drawString(font, seconds, x + 1, y, outline, false);
        g.drawString(font, seconds, x, y - 1, outline, false);
        g.drawString(font, seconds, x, y + 1, outline, false);
        g.drawString(font, seconds, x, y, ink, false);
    }

    private static void destiny(GuiGraphics g, Font font, int x, int y, Slot slot) {
        g.fill(x, y, x + 24, y + 24, slot.empty() ? 0x788a9198 : 0xe4cbd5d9);
        g.fill(x + 1, y + 1, x + 23, y + 23, subclassTile(slot));
        int charge = slot.charge(22);
        if (charge < 22 && !slot.empty()) g.fill(x + 1, y + 1, x + 23, y + 23 - charge, 0xce22252d);
        glyph(g, slot, x + 4, y + 3, 16, slot.empty() ? 0x9086949b : WHITE);
        int line = slot.charge(20);
        if (line > 0) g.fill(x + 2, y + 21, x + 2 + line, y + 22, elementInk(slot));
        timer(g, font, slot, x + 22, y + 13, WHITE);
        label(g, font, slot, x + 12, y + 25, 23, slot.empty() ? MUTED : WHITE);
    }

    private static void minecraftTray(GuiGraphics g, int x, int y) {
        // Preserve vanilla/resource-pack hotbar corners and two intact interior separators.
        g.blitSprite(HOTBAR, 182, 22, 0, 0, x, y, 21, 22);
        g.blitSprite(HOTBAR, 182, 22, 21, 0, x + 21, y, 20, 22);
        g.blitSprite(HOTBAR, 182, 22, 21, 0, x + 41, y, 20, 22);
        g.blitSprite(HOTBAR, 182, 22, 161, 0, x + 61, y, 21, 22);
    }

    private static void minecraft(GuiGraphics g, Font font, int x, int y, Slot slot) {
        glyph(g, slot, x + 2, y + 3, 16, slot.empty() ? 0x686a7073 : elementInk(slot));
        int covered = 16 - slot.charge(16);
        if (!slot.empty() && covered > 0) g.fill(x + 2, y + 19 - covered, x + 18, y + 19, 0x867a818a);
        timer(g, font, slot, x + 19, y + 11, WHITE);
        label(g, font, slot, x + 10, y + 23, 19, slot.empty() ? MUTED : WHITE);
    }

    private static void minimal(GuiGraphics g, Font font, int x, int y, Slot slot) {
        int ink = slot.available() ? elementInk(slot) : MUTED;
        glyph(g, slot, x + 5, y + 1, 13, 0xaa18202b);
        glyph(g, slot, x + 4, y, 13, slot.empty() ? 0x787e919b : ink);
        g.fill(x + 1, y + 14, x + 22, y + 15, 0x99414a50);
        int fill = slot.charge(21);
        if (fill > 0) g.fill(x + 1, y + 14, x + 1 + fill, y + 15, ink);
        timer(g, font, slot, x + 22, y + 5, WHITE);
        label(g, font, slot, x + 11, y + 16, 23, slot.empty() ? MUTED : WHITE);
    }

    private static void borderlands2(GuiGraphics g, Font font, int x, int y, Slot slot) {
        ResourceLocation badge = slot.available() ? BL2_READY : BL2_SPENT;
        g.blit(badge, x, y, 0, 0, 30, 25, 30, 25);
        glyph(g, slot, x + 8, y + 5, 16, 0xff122025);
        glyph(g, slot, x + 7, y + 4, 16, slot.available() ? 0xffefffb3 : 0xff9ca5a3);
        int charge = slot.charge(18);
        if (charge > 0) g.fill(x + 5, y + 21, x + 5 + charge, y + 22, 0xffa6ee62);
        timer(g, font, slot, x + 26, y + 12, 0xfffffac6);
        label(g, font, slot, x + 15, y + 25, 27, slot.available() ? 0xffd6ffa4 : MUTED);
    }

    /** Three role inks echo the action-skill artwork. Newly added abilities default to offense. */
    private static int actionInk(Slot slot) {
        if (!slot.available()) return 0xffaab1ac;
        return switch (slot.id()) {
            case "healing_rift", "healing_grenade", "empowering_rift", "rally_barricade",
                "gamblers_dodge", "marksmans_dodge", "towering_barricade" -> 0xffb4de82;
            case "duskfield_grenade", "coldsnap_grenade", "vortex_grenade", "suppressor_grenade",
                "shackle_grenade", "flashbang", "penumbral_blast", "storm_snare",
                "freezing_singularity" -> 0xff90d9ee;
            default -> 0xffff9c77;
        };
    }

    private static void borderlands4(GuiGraphics g, Font font, int x, int y, Slot slot) {
        g.blit(BL4_PLATE, x, y, 0, 0, 29, 25, 29, 25);
        int ink = actionInk(slot);
        g.fill(x + 1, y + 3, x + 3, y + 9, ink);
        g.fill(x + 3, y + 1, x + 11, y + 3, ink);
        glyph(g, slot, x + 7, y + 4, 17, 0xff17181d);
        glyph(g, slot, x + 6, y + 3, 17, slot.empty() ? 0xff718082 : ink);
        // The three threshold marks remain dark until that third of the skill has recovered.
        int litSegments = slot.charge(3);
        for (int i = 0; i < 3; i++)
            g.fill(x + 4 + 7*i, y + 22, x + 10 + 7*i, y + 24, i < litSegments ? ink : 0xff3c4849);
        timer(g, font, slot, x + 27, y + 13, WHITE);
        label(g, font, slot, x + 14, y + 25, 27, slot.empty() ? MUTED : 0xfff3ead6);
    }

    private static void tenno(GuiGraphics g, Font font, int x, int y, Slot slot) {
        // Only the silhouette, its one-pixel shadow and its own binding occupy this channel.
        int ink = slot.empty() ? 0xaa8e989f : slot.available() ? WHITE : 0xff8597a6;
        glyph(g, slot, x + 4, y + 1, 18, 0xd21c2732);
        glyph(g, slot, x + 3, y, 18, ink);
        timer(g, font, slot, x + 24, y + 10, 0xffa5e9ff);
        label(g, font, slot, x + 12, y + 20, 24, slot.empty() ? MUTED : 0xffe0edf1);
    }
}
