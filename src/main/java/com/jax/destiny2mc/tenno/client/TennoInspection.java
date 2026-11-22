package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.TennoGunSpec;
import com.jax.destiny2mc.tenno.TennoGunStats;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.melee.MeleeModStats;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.ModPacks;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Warframe hover inspection cards for Tenno weapons, melee blades, mod packs,
 * and Gunsmith mod cards. Boosted modded stats highlight in Warframe HUD green (#4CD964).
 * Always reads the displayed stack, including partial magazines and durability. */
@EventBusSubscriber(modid = "destiny2mc", value = Dist.CLIENT)
public final class TennoInspection {
    private static final DecimalFormat NUMBER = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private TennoInspection() {}

    record Stat(String title, String value) {}
    record Info(String category, String variant, String note, List<Stat> stats, String technique) {}

    private static String number(double n) { return NUMBER.format(n); }
    private static boolean detailed() {
        return Screen.hasShiftDown() || Minecraft.getInstance().options.advancedItemTooltips;
    }

    static Info describe(ItemStack stack) {
        var rows = new ArrayList<Stat>();
        var player = Minecraft.getInstance().player;
        var fitted = WeaponMods.installed(stack);
        int active = (int) fitted.stream().filter(mod -> ModCollection.rank(player, mod.id()) >= 0).count();
        String damageLabel = active > 0 ? "Damage" : "Base damage";
        if (stack.getItem() instanceof TennoGunItem gun) {
            var p = gun.profile();
            var stats = TennoGunStats.of(player, stack);
            rows.add(new Stat(damageLabel, number(stats.damage()) + " HP"));
            boolean capacitor = p.family == TennoGunSpec.Family.BUBONICO;
            rows.add(new Stat(capacitor ? "Battery" : "Magazine", TennoGunItem.magazine(player, stack) + " / " + stats.magazine()));
            if (!capacitor) rows.add(new Stat("Reload", number(stats.reloadTicks() / 20.0) + " s"));
            rows.add(new Stat("Range", p.range() + " m"));
            rows.add(new Stat("Trigger", p.mode()));
            if (stats.multishot() > 0) rows.add(new Stat("Multishot", "+" + number(100 * stats.multishot()) + "%"));
            if (stats.rateFactor() > 1.00001) rows.add(new Stat("Fire rate", "×" + number(stats.rateFactor())));
            addSocketCount(rows, active, fitted.size());
            String slot = switch (p.family) {
                case TWINS, BRAKK, MICROWAVE, VESPER, CANTARE, AKSONDOL -> "Secondary";
                default -> "Primary";
            };
            return new Info(slot + "  /  " + p.kind(), p.variant(), p.note(), rows, "");
        }
        var p = ((TennoMeleeItem) stack.getItem()).profile();
        var stats = MeleeModStats.of(player, stack);
        rows.add(new Stat(damageLabel, number(stats.damage()) + " HP"));
        rows.add(new Stat("Attack speed", number(stats.speed()) + " / s"));
        rows.add(new Stat("Reach", number(stats.reach()) + " m"));
        rows.add(new Stat("Blocking angle", p.guardAngle() + "°"));
        rows.add(new Stat("Durability", (stack.getMaxDamage() - stack.getDamageValue()) + " / " + stack.getMaxDamage()));
        if (stats.heat() > 0) rows.add(new Stat("Bonus heat", "+" + number(100 * stats.heat()) + "%"));
        if (stats.heavy() > 0) rows.add(new Stat("Heavy attack", "+" + number(100 * stats.heavy()) + "%"));
        if (stats.parry() > 0) rows.add(new Stat("Parry chance", number(100 * stats.parry()) + "%"));
        addSocketCount(rows, active, fitted.size());
        return new Info("Melee  /  " + p.family(), "", p.description(), rows, p.trait());
    }

    private static void addSocketCount(List<Stat> rows, int active, int fitted) {
        if (fitted > 0) rows.add(new Stat("Installed mods", active == fitted ? Integer.toString(fitted) : active + " / " + fitted + " active"));
    }

    /** Returns true when a stat row reflects a live mod improvement over the weapon's unmodded baseline. */
    static boolean isBoosted(ItemStack stack, Stat stat) {
        var player = Minecraft.getInstance().player;
        if (WeaponMods.installed(stack).isEmpty()) return false;
        String label = stat.title();
        if (label.equals("Multishot") || label.equals("Fire rate") || label.equals("Bonus heat")
                || label.equals("Heavy attack") || label.equals("Parry chance")) return true;
        if (stack.getItem() instanceof TennoGunItem gun) {
            var base = gun.profile();
            var mod = TennoGunStats.of(player, stack);
            return switch (label) {
                case "Damage" -> mod.damage() > base.damage() + 0.001f;
                case "Magazine", "Battery" -> mod.magazine() > base.magazine();
                case "Reload" -> mod.reloadTicks() < base.reloadTicks();
                default -> false;
            };
        }
        if (stack.getItem() instanceof TennoMeleeItem melee) {
            var base = melee.profile();
            var mod = MeleeModStats.of(player, stack);
            return switch (label) {
                case "Damage" -> mod.damage() > base.damage() + 0.001d;
                case "Attack speed" -> mod.speed() > base.attackSpeed() + 0.001d;
                case "Reach" -> mod.reach() > base.reach() + 0.001d;
                default -> false;
            };
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void inventory(RenderTooltipEvent.Pre event) {
        var stack = event.getItemStack();
        if (!TennoBlueprints.isTenno(stack.getItem()) || detailed()
                || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) || stack.has(DataComponents.HIDE_TOOLTIP)) return;
        event.setCanceled(true);
        paint(event.getGraphics(), stack, event.getX(), event.getY());
    }

    /** Applies Warframe neutral charcoal & accent border colors to Shift-detailed Tenno tooltips and Mod Pack tooltips. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void detailedColors(RenderTooltipEvent.Color event) {
        var stack = event.getItemStack();
        if (!TennoBlueprints.isTenno(stack.getItem()) && !(stack.getItem() instanceof ModPacks.PackItem)) return;
        int border = VitruvianUi.WHITE_RAIL;
        if (stack.getItem() instanceof TennoGunItem gun) border = trim(gun.profile().variant());
        event.setBackgroundStart(0xF21B212A);
        event.setBackgroundEnd(0xF611151B);
        event.setBorderStart(border);
        event.setBorderEnd(VitruvianUi.BORDER_HAIRLINE);
    }

    public static void draw(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
        if (stack.isEmpty() || stack.has(DataComponents.HIDE_TOOLTIP)) return;
        if (detailed() || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
        } else paint(graphics, stack, mouseX, mouseY);
    }

    /** Hover inspection card for Warframe mods inside the Gunsmith Modding page. */
    public static void drawModTooltip(GuiGraphics g, WeaponMod mod, int rank, boolean installed, int mx, int my) {
        if (mod == null) return;
        Font f = Minecraft.getInstance().font;
        boolean locked = rank < 0;
        int metal = VitruvianUi.rarityFrame(mod.rarity());
        int width = Mth.clamp(g.guiWidth() / 2, 178, 212);
        var effect = f.split(Component.literal(mod.effect(Math.max(0, rank))), width - 20);
        var desc = f.split(Component.literal(mod.description()), width - 20);
        boolean hasNext = !locked && rank < mod.maxRank();
        var nextEffect = hasNext ? f.split(Component.literal(mod.effect(rank + 1)), width - 20) : List.<FormattedCharSequence>of();
        int step = f.lineHeight + 2;
        int h = 46 + effect.size() * step + (hasNext ? 14 + nextEffect.size() * step : 0) + 10 + desc.size() * step + 20;
        float scale = Math.min(1f, Math.min((g.guiWidth() - 4f) / width, (g.guiHeight() - 4f) / h));
        if (scale <= 0) return;
        int dw = (int) Math.ceil(width * scale), dh = (int) Math.ceil(h * scale);
        int x = mx + 12 + dw <= g.guiWidth() - 2 ? mx + 12 : mx - 8 - dw;
        x = Mth.clamp(x, 2, Math.max(2, g.guiWidth() - dw - 2));
        int y = Mth.clamp(my - 9, 2, Math.max(2, g.guiHeight() - dh - 2));

        g.pose().pushPose();
        g.pose().translate(x, y, 420);
        g.pose().scale(scale, scale, 1);
        VitruvianUi.drawVitruvianBox(g, width, h, metal);

        VitruvianUi.Polarity pol = VitruvianUi.polarityOf(mod);
        VitruvianUi.drawPolarity(g, pol, width - 16, 7, installed ? VitruvianUi.POSITIVE : metal);
        String header = VitruvianUi.rarityName(mod.rarity()) + "  /  " + mod.category().toUpperCase(Locale.ROOT);
        tiny(g, f, header, 10, 7, metal, .76f);
        g.drawString(f, mod.title(), 10, 18, VitruvianUi.IVORY, false);
        String status = locked ? "LOCKED" : (installed ? "EQUIPPED  ·  " : "") + "RANK " + rank + " / " + mod.maxRank();
        tiny(g, f, status, 10, 30, locked ? VitruvianUi.DISABLED : (installed ? VitruvianUi.CYAN : VitruvianUi.MUTED), .78f);

        drawDivider(g, 10, 41, width - 20, metal);

        int rowY = 47;
        for (var line : effect) {
            g.drawString(f, line, 10, rowY, locked ? VitruvianUi.MUTED : VitruvianUi.POSITIVE, false);
            rowY += step;
        }
        if (hasNext) {
            rowY += 3;
            tiny(g, f, "NEXT RANK  (+" + (rank + 1) + ")", 10, rowY, VitruvianUi.GOLD, .74f);
            rowY += 10;
            for (var line : nextEffect) {
                g.drawString(f, line, 10, rowY, VitruvianUi.IVORY, false);
                rowY += step;
            }
        }
        drawDivider(g, 10, rowY + 2, width - 20, VitruvianUi.BORDER_HAIRLINE);
        rowY += 8;
        for (var line : desc) {
            g.drawString(f, line, 10, rowY, VitruvianUi.MUTED, false);
            rowY += step;
        }
        tiny(g, f, pol.label.toUpperCase(Locale.ROOT) + " POLARITY", 10, h - 11, VitruvianUi.MUTED, .72f);
        VitruvianUi.drawRarityDiamonds(g, width - 26, h - 8, mod.rarity(), metal);
        g.pose().popPose();
    }

    static int trim(String variant) {
        return VitruvianUi.variantTrim(variant);
    }

    private static String controls(ItemStack stack) {
        var options = Minecraft.getInstance().options;
        if (stack.getItem() instanceof TennoMeleeItem)
            return options.keyUse.getTranslatedKeyMessage().getString() + "  Guard   ·   "
                    + options.keyShift.getTranslatedKeyMessage().getString() + " + "
                    + options.keyAttack.getTranslatedKeyMessage().getString() + "  Charged heavy";
        if (stack.getItem() instanceof TennoGunItem gun && gun.profile().alternate())
            for (var mapping : options.keyMappings)
                if (mapping.getName().equals("key.destiny2mc.tenno_alt"))
                    return mapping.getTranslatedKeyMessage().getString() + "  Alternate fire";
        return "";
    }

    private static void paint(GuiGraphics g, ItemStack stack, int mx, int my) {
        Font f = Minecraft.getInstance().font;
        Info info = describe(stack);
        int width = Mth.clamp(g.guiWidth() / 2, 188, 228);
        List<FormattedCharSequence> title = f.split(stack.getHoverName(), width - 54);
        int titleRows = Math.min(2, Math.max(1, title.size()));
        int categoryY = 21 + titleRows * 11;
        int headerBottom = Math.max(50, categoryY + 13);
        int step = f.lineHeight + 2;
        int statStart = headerBottom + 8;
        int bodyStart = statStart + step * info.stats().size() + 10;
        var note = f.split(Component.literal(info.note()), width - 20);
        String controls = controls(stack);
        var binding = controls.isBlank() ? List.<FormattedCharSequence>of() : f.split(Component.literal(controls), width - 20);
        int bindingHeight = binding.isEmpty() ? 0 : binding.size() * step + 5;
        int h = bodyStart + (info.technique().isBlank() ? 0 : step + 2) + note.size() * step + bindingHeight + 21;
        float scale = Math.min(1f, Math.min((g.guiWidth() - 4f) / width, (g.guiHeight() - 4f) / h));
        if (scale <= 0) return;
        int dw = (int) Math.ceil(width * scale), dh = (int) Math.ceil(h * scale);
        int x = mx + 12 + dw <= g.guiWidth() - 2 ? mx + 12 : mx - 8 - dw;
        x = Mth.clamp(x, 2, Math.max(2, g.guiWidth() - dw - 2));
        int y = Mth.clamp(my - 9, 2, Math.max(2, g.guiHeight() - dh - 2));
        int accent = trim(info.variant());

        g.pose().pushPose();
        g.pose().translate(x, y, 420);
        g.pose().scale(scale, scale, 1);
        VitruvianUi.drawVitruvianBox(g, width, h, accent);

        leaf(g, 9, 6, accent);
        String edition = info.variant().isBlank() || "Standard".equals(info.variant())
                ? "ARSENAL" : "ARSENAL   /   " + info.variant().toUpperCase(Locale.ROOT);
        tiny(g, f, edition, 22, 7, accent, .76f);
        for (int i = 0; i < titleRows && i < title.size(); i++)
            g.drawString(f, title.get(i), 10, 19 + i * 11, VitruvianUi.IVORY, false);
        tiny(g, f, info.category().toUpperCase(Locale.ROOT), 10, categoryY, VitruvianUi.MUTED, .80f);

        // Framed 1:1 weapon icon well
        g.fill(width - 41, 10, width - 8, 43, VitruvianUi.PANEL_WELL);
        g.fill(width - 41, 10, width - 8, 11, VitruvianUi.BORDER_HAIRLINE);
        g.fill(width - 41, 42, width - 8, 43, VitruvianUi.BORDER_HAIRLINE);
        g.fill(width - 41, 10, width - 40, 43, VitruvianUi.BORDER_HAIRLINE);
        g.fill(width - 9, 10, width - 8, 43, VitruvianUi.BORDER_HAIRLINE);
        g.pose().pushPose();
        g.pose().translate(width - 39, 12, 0);
        g.pose().scale(1.85f, 1.85f, 1);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();

        drawDivider(g, 10, headerBottom, width - 20, accent);

        int rowY = statStart;
        for (Stat stat : info.stats()) {
            boolean boosted = isBoosted(stack, stat);
            int valColor = boosted ? VitruvianUi.POSITIVE
                    : ("Installed mods".equals(stat.title()) ? VitruvianUi.SKY_BLUE_SOFT : VitruvianUi.IVORY);
            g.drawString(f, stat.title(), 10, rowY, VitruvianUi.MUTED, false);
            g.drawString(f, stat.value(), width - 10 - f.width(stat.value()), rowY, valColor, false);
            rowY += step;
        }
        drawDivider(g, 10, rowY + 3, width - 20, VitruvianUi.BORDER_HAIRLINE);

        int noteY = bodyStart;
        if (!info.technique().isBlank()) {
            g.drawString(f, info.technique(), 10, noteY, accent, false);
            noteY += step + 2;
        }
        for (var line : note) {
            g.drawString(f, line, 10, noteY, VitruvianUi.IVORY, false);
            noteY += step;
        }
        if (!binding.isEmpty()) {
            noteY += 4;
            for (var line : binding) {
                g.drawString(f, line, 10, noteY, VitruvianUi.SKY_BLUE_SOFT, false);
                noteY += step;
            }
        }
        tiny(g, f, "SHIFT   ITEM DETAILS", 10, h - 12, VitruvianUi.MUTED, .76f);
        g.pose().popPose();
    }

    private static void drawDivider(GuiGraphics g, int x, int y, int w, int accent) {
        int mid = x + w / 2;
        g.fill(x, y, mid - 5, y + 1, VitruvianUi.BORDER_HAIRLINE);
        g.fill(mid + 6, y, x + w, y + 1, VitruvianUi.BORDER_HAIRLINE);
        VitruvianUi.diamond(g, mid, y, 2, accent);
    }

    private static void leaf(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 4, y, x + 5, y + 7, color);
        for (int i = 0; i < 4; i++) {
            g.fill(x + i, y + 1 + i, x + i + 1, y + 3 + i, color);
            g.fill(x + 8 - i, y + 1 + i, x + 9 - i, y + 3 + i, color);
        }
    }

    private static void tiny(GuiGraphics g, Font font, String text, int x, int y, int color, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }
}
