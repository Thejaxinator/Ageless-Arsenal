package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.binder.client.CombatHudLayout;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.TennoGunSpec;
import com.jax.destiny2mc.tenno.TennoGunStats;
import com.jax.destiny2mc.weapon.GunDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** The same lower-left layout owns ammo and Binder icons. The four ordinary themes
 * preserve centered ability slots while Minimal and BL2 keep the existing flush row.
 * Theme 5 (Tenno) applies the neutral charcoal, sky-blue, and crisp white Warframe HUD. */
public final class TennoWeaponHud {
    public static void draw(GuiGraphics g, ItemStack held, long now, CombatHudLayout layout, int theme) {
        var gun = (TennoGunItem) held.getItem();
        var spec = gun.profile();
        var stats = TennoGunStats.of(Minecraft.getInstance().player, held);
        int mag = TennoGunItem.loaded(held, stats), width = CombatHudLayout.ammoWidth(theme);
        boolean tennoTheme = theme == 5;
        int accent = switch (theme) {
            case 0 -> 0xffe5c076;
            case 1 -> 0xff91ad71;
            case 2 -> 0xffdbdbdb;
            case 3 -> 0xffffcf38;
            case 4 -> 0xfffb9154;
            default -> VitruvianUi.SKY_BLUE;
        };
        var pose = g.pose();
        pose.pushPose();
        pose.translate(layout.left(), layout.ammoBaseline(), 0);
        pose.scale(layout.scale(), layout.scale(), 1);
        try {
            var font = Minecraft.getInstance().font;
            int h = theme == 2 ? 22 : 27;
            if (tennoTheme) {
                g.fill(0, -h, width, 0, 0xB80E1218);
                g.fill(0, -h, width, -h + 1, VitruvianUi.WHITE_RAIL);
                g.fill(0, -h, 2, 0, VitruvianUi.SKY_BLUE);
            } else {
                if (theme != 2) g.fill(0, -h, width, 0, 0xbb11191d);
                if (theme != 2) g.fill(0, -h, 2, 0, accent);
            }
            String count = mag + " / " + stats.magazine();
            int textEnd = width - 3;
            g.drawString(font, count, textEnd - font.width(count), -12, VitruvianUi.IVORY, true);
            boolean reloading = TennoGunItem.reloadUntil(held) > now;
            String info = reloading ? "Reloading" : GunDataHelper.getLong(held, "wf_gale") > now ? "Gale Force"
                    : spec.family == TennoGunSpec.Family.BUBONICO ? "Battery" : spec.mode().split(" / ")[0];
            int room = width - font.width(count) - 13;
            int infoColor = tennoTheme ? (reloading ? VitruvianUi.GOLD : VitruvianUi.SKY_BLUE_SOFT) : accent;
            g.drawString(font, font.plainSubstrByWidth(info, room), 5, -12, infoColor, true);
            g.drawString(font, held.getHoverName().getVisualOrderText(), 5, -h + 2, VitruvianUi.IVORY, true);
            g.fill(4, -2, width - 2, -1, tennoTheme ? 0x6649525E : 0x55485860);
            int barFill = Math.round((width - 6) * mag / (float) Math.max(1, stats.magazine()));
            g.fill(4, -2, 4 + barFill, -1, accent);
            if (tennoTheme && barFill > 2) g.fill(3 + barFill, -3, 5 + barFill, 0, VitruvianUi.WHITE_RAIL);
        } finally {
            pose.popPose();
        }
    }
}
