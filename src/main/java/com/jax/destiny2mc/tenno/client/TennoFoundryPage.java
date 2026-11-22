package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.GameCanvas;
import com.jax.destiny2mc.client.ui.GunsmithLayout;
import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.foundry.FoundryNetwork;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import java.util.List;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Warframe Foundry page inside both Gunsmith blocks. Styled after Warframe's Foundry:
 * neutral translucent charcoal backdrop, frosted light-grey blueprint rows & staging card
 * with thin white overhang rails, gold REUSABLE BLUEPRINT badge, Minecraft resource rows
 * with checkmarks & readiness bars, and an attached Warframe gold / cool-slate status bar. */
public final class TennoFoundryPage {
    private static final String[] FILTERS = {"All", "Ranged", "Melee"};
    private static long serial;
    private static FoundryNetwork.Result latest;
    private int filter, index, offset;
    private long pending, nextClick;
    private String orderedName = "", feedback = "";
    private List<Item> entries = List.of();
    private List<ItemStack> costs = List.of();

    public static void receive(FoundryNetwork.Result reply) { latest = reply; }

    private void refresh(Player p) {
        if (p == null) { entries = List.of(); costs = List.of(); return; }
        List<Item> all = TennoBlueprints.entries(p);
        entries = switch (filter) {
            case 1 -> all.stream().filter(TennoGunItem.class::isInstance).toList();
            case 2 -> all.stream().filter(TennoMeleeItem.class::isInstance).toList();
            default -> all;
        };
        index = Mth.clamp(index, 0, Math.max(0, entries.size() - 1));
        offset = Mth.clamp(offset, 0, Math.max(0, entries.size() - 12));
        costs = entries.isEmpty() ? List.of() : TennoBlueprints.cost(p, entries.get(index));
        if (pending != 0 && latest != null && latest.serial() == pending) {
            feedback = latest.success() ? orderedName + " assembled." : "Assembly declined. Check materials and bench reach.";
            pending = 0;
        }
    }

    private ItemStack stack(int token) {
        if (token < 0) return costs.get(-token - 1);
        return token >= entries.size() ? ItemStack.EMPTY : entries.get(token).getDefaultInstance();
    }

    public void draw(GuiGraphics graphics, Font font, Player player) {
        refresh(player);
        UiCanvas c = new GameCanvas(graphics, font, this::stack);

        // Neutral translucent charcoal backdrop & three balanced Foundry panels
        c.fill(0, 70, 800, 380, VitruvianUi.BACKDROP);
        VitruvianUi.panel(c, 21, 76, 208, 333);
        VitruvianUi.panel(c, 236, 76, 271, 333);
        VitruvianUi.panel(c, 514, 76, 265, 333);

        // Category filter tabs with crisp Warframe white active underline
        for (int i = 0; i < FILTERS.length; i++) {
            int x = 27 + i * 66;
            boolean active = i == filter;
            c.fill(x, 84, 62, 23, active ? VitruvianUi.ROW_ACTIVE : VitruvianUi.PANEL_WELL);
            c.outline(x, 84, 62, 23, active ? VitruvianUi.BORDER_HAIRLINE : VitruvianUi.RULE_SUBTLE);
            if (active) c.fill(x, 105, 62, 2, VitruvianUi.WHITE_RAIL);
            c.text(x + 8, 92, FILTERS[i].toUpperCase(Locale.ROOT),
                    active ? VitruvianUi.IVORY : VitruvianUi.MUTED, .82f);
        }

        // Blueprint list: selected row uses Warframe frosted off-white card style; ready rows show sky-blue/green pip
        int end = Math.min(entries.size(), offset + 12);
        for (int i = offset; i < end; i++) {
            int y = 115 + (i - offset) * 22;
            boolean selectedRow = i == index;
            boolean craftable = player != null && TennoBlueprints.canCraft(player, entries.get(i));
            if (selectedRow) {
                c.fill(27, y, 196, 20, VitruvianUi.FROST_LIGHT);
                c.fill(27, y, 3, 20, craftable ? VitruvianUi.GOLD : VitruvianUi.SKY_BLUE);
                c.fill(26, y - 1, 198, 1, VitruvianUi.WHITE_RAIL);
            } else {
                c.fill(27, y, 196, 20, 0x881B212A);
                c.fill(27, y + 19, 196, 1, VitruvianUi.RULE_SUBTLE);
            }
            c.diamond(35, y + 10, 2, craftable ? VitruvianUi.POSITIVE : VitruvianUi.SLATE_BAR);
            if (craftable) c.fill(35, y + 10, 1, 1, VitruvianUi.POSITIVE);
            String name = font.plainSubstrByWidth(stack(i).getHoverName().getString(), 188);
            c.text(43, y + 6, name, selectedRow ? VitruvianUi.FROST_INK : VitruvianUi.IVORY, .86f);
        }
        c.text(30, 392, entries.size() + " BLUEPRINTS", VitruvianUi.MUTED, .76f);

        if (!entries.isEmpty()) {
            ItemStack selected = stack(index);
            TennoInspection.Info info = TennoInspection.describe(selected);
            boolean ready = TennoBlueprints.canCraft(player, selected.getItem());
            boolean active = ready && pending == 0 && Util.getMillis() >= nextClick;

            // Center frosted Warframe Foundry blueprint card with thin white overhang rails
            VitruvianUi.frostedCard(c, 246, 86, 251, 240, ready);
            c.text(255, 93, selected.getHoverName().getString().toUpperCase(Locale.ROOT), VitruvianUi.FROST_INK, 1.18f);
            c.text(256, 111, info.category().toUpperCase(Locale.ROOT), VitruvianUi.FROST_SUB, .78f);

            // Gold "REUSABLE BLUEPRINT" pill tag
            c.fill(255, 124, 110, 13, VitruvianUi.GOLD);
            c.text(261, 127, "REUSABLE BLUEPRINT", VitruvianUi.FROST_INK, .70f);

            // Clean calibration reticle behind the weapon sprite
            c.ring(371, 224, 68, 68, 0x443B4450);
            c.line(262, 224, 286, 224, 0x6647505A);
            c.line(456, 224, 480, 224, 0x6647505A);
            c.item(index, 272, 137, 202);

            // Attached Foundry card footer bar (Warframe gold when ready, cool slate when missing mats)
            c.fill(246, 308, 251, 18, ready ? VitruvianUi.GOLD : VitruvianUi.SLATE_BAR);
            c.text(256, 314, ready ? "READY TO BUILD" : "NOT ENOUGH COMPONENTS",
                    ready ? VitruvianUi.FROST_INK : VitruvianUi.IVORY, .76f);

            c.wrap(247, 338, info.note(), 43, VitruvianUi.MUTED);

            // Right panel: Fabrication requirements with frosted 1:1 resource slots & readiness bars
            c.text(528, 88, "COMPONENTS", VitruvianUi.IVORY, .94f);
            c.fill(528, 104, 238, 1, VitruvianUi.WHITE_RAIL);

            for (int i = 0; i < costs.size(); i++) {
                ItemStack material = costs.get(i);
                int y = 116 + 46 * i;
                int need = material.getCount();
                int have = TennoBlueprints.available(player, material.getItem());
                boolean met = have >= need;

                c.fill(526, y, 242, 38, met ? VitruvianUi.FROST_MID : 0xD8202630);
                c.outline(526, y, 242, 38, met ? VitruvianUi.WHITE_RAIL : VitruvianUi.BORDER_HAIRLINE);
                // 1:1 framed resource icon slot
                c.fill(531, y + 5, 28, 28, VitruvianUi.PANEL_WELL);
                c.outline(531, y + 5, 28, 28, met ? VitruvianUi.SKY_BLUE : VitruvianUi.NEGATIVE);
                c.item(-i - 1, 534, y + 8, 22);

                c.text(566, y + 6, font.plainSubstrByWidth(material.getHoverName().getString(), 188),
                        met ? VitruvianUi.FROST_INK : VitruvianUi.IVORY, .86f);
                String ratio = have + " / " + need + (met ? "  ✓" : "");
                c.text(566, y + 19, ratio, met ? 0xFF1D6E2E : 0xFFE57373, .84f);

                // Resource readiness progress bar
                int barW = 192;
                int fillW = Math.round(barW * Math.min(1f, have / (float) Math.max(1, need)));
                c.fill(566, y + 31, barW, 2, met ? 0x5547505A : VitruvianUi.RULE_SUBTLE);
                if (fillW > 0) c.fill(566, y + 31, fillW, 2, met ? VitruvianUi.SKY_BLUE : VitruvianUi.NEGATIVE);
            }

            // Warframe Gold / Slate BUILD action button (exact hitbox 526..772, 363..389)
            c.fill(526, 363, 246, 26, active ? VitruvianUi.GOLD : VitruvianUi.SLATE_BAR_DARK);
            c.outline(526, 363, 246, 26, active ? VitruvianUi.WHITE_RAIL : VitruvianUi.BORDER_HAIRLINE);
            if (active) c.fill(524, 361, 250, 1, VitruvianUi.WHITE_RAIL);
            String cta = pending != 0 ? "ASSEMBLING…" : ready ? "ASSEMBLE WEAPON" : "NOT ENOUGH COMPONENTS";
            c.text(541, 372, cta, active ? VitruvianUi.FROST_INK : VitruvianUi.MUTED, .88f);
        } else {
            c.text(248, 122, "No blueprints available.", VitruvianUi.MUTED, 1);
        }

        // Bottom footer
        c.fill(27, 415, 746, 1, VitruvianUi.BORDER_HAIRLINE);
        c.text(28, 428, "ESC   CLOSE     ·     ENTER   ASSEMBLE", VitruvianUi.MUTED, .78f);
        if (!feedback.isEmpty()) {
            boolean ok = feedback.endsWith("assembled.");
            c.text(247, 428, feedback, ok ? VitruvianUi.POSITIVE : VitruvianUi.NEGATIVE, .82f);
        }
    }

    public void hover(GuiGraphics g, double x, double y, int mx, int my) {
        int row = GunsmithLayout.rowHit(x, y, offset, entries.size());
        boolean model = x >= 272 && x < 474 && y >= 137 && y < 339;
        if (row >= 0 || model && !entries.isEmpty()) {
            TennoInspection.draw(g, stack(row < 0 ? index : row), mx, my);
            return;
        }
        if (!costs.isEmpty() && x >= 526 && x < 768 && y >= 116) {
            int i = (int) ((y - 116) / 46);
            if (i >= 0 && i < costs.size() && (y - 116) % 46 < 38) {
                g.renderTooltip(Minecraft.getInstance().font, costs.get(i), mx, my);
            }
        }
    }

    private void changeFilter(int next) {
        filter = Math.floorMod(next, FILTERS.length);
        index = 0; offset = 0; feedback = "";
    }

    public boolean click(double x, double y, Player player) {
        refresh(player);
        if (y >= 84 && y < 107 && x >= 27 && x < 221) {
            int relative = (int) x - 27;
            if (relative % 66 < 62) changeFilter(relative / 66);
            return true;
        }
        int row = GunsmithLayout.rowHit(x, y, offset, entries.size());
        if (row >= 0) { index = row; feedback = ""; return true; }
        if (x >= 526 && x < 772 && y >= 363 && y < 389) { assemble(player); return true; }
        return false;
    }

    public boolean scroll(double x, double y, double dy) {
        if (x < 26 || x >= 224 || y < 115 || y >= 402 || dy == 0) return false;
        offset = Mth.clamp(offset + (dy > 0 ? -3 : 3), 0, Math.max(0, entries.size() - 12));
        return true;
    }

    public boolean key(int key, Player player) {
        switch (key) {
            case GLFW.GLFW_KEY_RIGHT -> changeFilter(filter + 1);
            case GLFW.GLFW_KEY_LEFT -> changeFilter(filter - 1);
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_UP -> {
                index = Mth.clamp(index + (key == GLFW.GLFW_KEY_DOWN ? 1 : -1), 0, Math.max(0, entries.size() - 1));
                offset = Mth.clamp(offset, Math.max(0, index - 11), index);
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> { refresh(player); assemble(player); }
            default -> { return false; }
        }
        return true;
    }

    private void assemble(Player p) {
        if (entries.isEmpty() || p == null || pending != 0 || Util.getMillis() < nextClick) return;
        Item item = entries.get(index);
        if (!TennoBlueprints.canCraft(p, item)) return;
        pending = ++serial;
        nextClick = Util.getMillis() + 450;
        orderedName = item.getDescription().getString();
        feedback = "";
        PacketDistributor.sendToServer(new FoundryNetwork.Assemble(BuiltInRegistries.ITEM.getKey(item), pending));
    }
}
