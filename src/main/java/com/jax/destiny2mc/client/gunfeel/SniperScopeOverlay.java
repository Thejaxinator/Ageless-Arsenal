package com.jax.destiny2mc.client.gunfeel;

import com.jax.destiny2mc.client.DarciOptic;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.TennoGunSpec;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Destiny-authentic optical sniper scope renderer with per-exotic reticles, matte optical bezel,
 * eye-relief vignette, chromatic lens rim, subtle sway parallax, and live weapon telemetry.
 */
public final class SniperScopeOverlay {
    private static final float HIDE_HAND_THRESHOLD = 0.78f;

    private SniperScopeOverlay() {}

    public static boolean isScopedSniper(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof DestinyGunItem gun) {
            return isDestinySniper(gun.spec);
        }
        if (stack.getItem() instanceof TennoGunItem tenno) {
            return tenno.profile().family == TennoGunSpec.Family.PERIGALE;
        }
        return false;
    }

    public static boolean isDestinySniper(WeaponSpec spec) {
        return switch (spec.id) {
            case "izanagis_burden", "cloudstrike", "whisper_of_the_worm",
                 "darci", "ice_breaker", "praedyths_revenge", "wire_rifle" -> true;
            default -> false;
        };
    }

    public static boolean shouldHideFirstPersonModel(ItemStack stack, float aim) {
        return aim >= HIDE_HAND_THRESHOLD && isScopedSniper(stack);
    }

    public static float getOpticalMagnification(ItemStack stack) {
        if (stack.getItem() instanceof DestinyGunItem gun) {
            return switch (gun.spec.id) {
                case "whisper_of_the_worm" -> 5.8f;
                case "izanagis_burden" -> 5.0f;
                case "ice_breaker" -> 4.8f;
                case "darci" -> 4.6f;
                case "cloudstrike" -> 4.5f;
                case "praedyths_revenge" -> 4.2f;
                case "wire_rifle" -> 3.6f;
                default -> {
                    float fz = Mth.clamp(gun.spec.fovZoom, 0.25f, 0.95f);
                    yield 1.0f / fz;
                }
            };
        }
        if (stack.getItem() instanceof TennoGunItem tenno) {
            return tenno.profile().family == TennoGunSpec.Family.PERIGALE ? 4.4f : 1.32f;
        }
        return 1.0f;
    }

    public static boolean renderIfScoped(GuiGraphics gui, ItemStack stack, float aim) {
        if (!isScopedSniper(stack) || aim < 0.62f) return false;
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) return false;

        float alpha = SpringDamper.smoothstep(SpringDamper.window(aim, 0.62f, 0.90f));
        int w = gui.guiWidth();
        int h = gui.guiHeight();
        int cx = w / 2 + Math.round(WeaponSwayController.scopeSwayX() * 0.45f);
        int cy = h / 2 + Math.round(WeaponSwayController.scopeSwayY() * 0.45f);
        int radius = Math.max(48, (int) (Math.min(w, h) * 0.44f));

        String id = stack.getItem() instanceof DestinyGunItem g ? g.spec.id : "perigale";
        int accent = scopeAccentColor(id, stack);
        boolean onTarget = mc.crosshairPickEntity instanceof LivingEntity;
        int reticleColor = onTarget ? 0xFFE84A3C : accent;

        int maskAlpha = (int) (alpha * 246.0f) << 24;
        int black = maskAlpha | 0x05070A;
        int topY = cy - radius;
        int botY = cy + radius;
        if (topY > 0) gui.fill(0, 0, w, topY, black);
        if (botY < h) gui.fill(0, botY, w, h, black);

        int rSq = radius * radius;
        int innerVigR = (int) (radius * 0.80f);
        int innerVigSq = innerVigR * innerVigR;
        int rimR = Math.max(1, radius - 2);
        int rimSq = rimR * rimR;
        int rimTint = ((int) (alpha * 80.0f) << 24) | (accent & 0x00FFFFFF);

        int yStart = Math.max(0, topY);
        int yEnd = Math.min(h, botY);
        for (int y = yStart; y < yEnd; y++) {
            int dy = y - cy;
            int dySq = dy * dy;
            int dx = (int) Math.sqrt(Math.max(0, rSq - dySq));
            int leftX = cx - dx;
            int rightX = cx + dx;
            if (leftX > 0) gui.fill(0, y, leftX, y + 1, black);
            if (rightX < w) gui.fill(rightX, y, w, y + 1, black);

            int vDx = dySq < innerVigSq ? (int) Math.sqrt(innerVigSq - dySq) : 0;
            int vigLeft = Math.max(0, cx - dx);
            int vigLeftEnd = Math.min(w, cx - vDx);
            int vigRight = Math.max(0, cx + vDx);
            int vigRightEnd = Math.min(w, cx + dx);
            int vigBand = ((int) (alpha * 72.0f) << 24) | 0x04060A;
            if (vigLeftEnd > vigLeft) gui.fill(vigLeft, y, vigLeftEnd, y + 1, vigBand);
            if (vigRightEnd > vigRight) gui.fill(vigRight, y, vigRightEnd, y + 1, vigBand);

            int rDx = dySq < rimSq ? (int) Math.sqrt(rimSq - dySq) : 0;
            if (cx - dx >= 0 && cx - rDx > cx - dx) gui.fill(cx - dx, y, cx - rDx, y + 1, rimTint);
            if (cx + rDx < w && cx + dx > cx + rDx) gui.fill(cx + rDx, y, cx + dx, y + 1, rimTint);
        }

        int glassTint = ((int) (alpha * 18.0f) << 24) | (accent & 0x00FFFFFF);
        gui.fill(cx - radius / 2, cy - radius / 2, cx + radius / 2, cy + radius / 2, glassTint & 0x0FFFFFFF);

        int lineCol = ((int) (alpha * 185.0f) << 24) | (reticleColor & 0x00FFFFFF);
        int dimCol = ((int) (alpha * 95.0f) << 24) | (accent & 0x00FFFFFF);
        int coreCol = ((int) (alpha * 255.0f) << 24) | (reticleColor & 0x00FFFFFF);

        gui.fill(cx - radius + 6, cy, cx - 14, cy + 1, dimCol);
        gui.fill(cx + 15, cy, cx + radius - 6, cy + 1, dimCol);
        gui.fill(cx, cy + 15, cx + 1, cy + radius - 6, dimCol);
        gui.fill(cx, cy - radius + 12, cx + 1, cy - 18, dimCol);

        for (int i = 1; i <= 4; i++) {
            int ty = cy + 18 + i * 11;
            if (ty < cy + radius - 8) {
                int half = (i % 2 == 0) ? 5 : 3;
                gui.fill(cx - half, ty, cx + half + 1, ty + 1, dimCol);
            }
        }

        switch (id) {
            case "izanagis_burden" -> {
                drawDiamondRing(gui, cx, cy, 10, lineCol);
                int honed = GunDataHelper.getInt(stack, "d2_honed", 0);
                if (honed > 1) {
                    int glow = ((int) (alpha * 235.0f) << 24) | 0xFFD94A;
                    drawDiamondRing(gui, cx, cy, 14, glow);
                    gui.drawCenteredString(mc.font, "HONED EDGE x" + honed, cx, cy + radius - 26, glow);
                }
            }
            case "whisper_of_the_worm" -> {
                drawBoxBrackets(gui, cx, cy, 13, 5, lineCol);
                int mag = stack.getItem() instanceof DestinyGunItem g ? GunDataHelper.getMag(stack, g.spec) : 3;
                for (int i = 0; i < 3; i++) {
                    int px = cx - 12 + i * 10;
                    int py = cy + 20;
                    int col = i < mag ? (((int) (alpha * 230.0f) << 24) | 0xE6F4FF) : dimCol;
                    gui.fill(px, py, px + 5, py + 3, col);
                }
            }
            case "cloudstrike" -> {
                drawChevron(gui, cx, cy, 9, lineCol);
                drawBoxBrackets(gui, cx, cy, 16, 4, dimCol);
            }
            case "darci" -> {
                drawBoxBrackets(gui, cx, cy, 18, 6, lineCol);
                DarciOptic.draw(gui, cx, cy);
            }
            case "ice_breaker" -> {
                drawChevron(gui, cx, cy, 8, lineCol);
                drawDiamondRing(gui, cx, cy, 15, dimCol);
                if (stack.getItem() instanceof DestinyGunItem g) {
                    int mag = GunDataHelper.getMag(stack, g.spec);
                    int barW = 48;
                    int fill = barW * Mth.clamp(mag, 0, g.spec.magSize) / Math.max(1, g.spec.magSize);
                    gui.fill(cx - barW / 2, cy + 24, cx + barW / 2, cy + 26, 0x66000000);
                    gui.fill(cx - barW / 2, cy + 24, cx - barW / 2 + fill, cy + 26, lineCol);
                }
            }
            case "praedyths_revenge" -> {
                drawBoxBrackets(gui, cx, cy, 11, 4, lineCol);
                drawDiamondRing(gui, cx, cy, 17, dimCol);
            }
            case "wire_rifle" -> {
                drawChevron(gui, cx, cy, 11, lineCol);
                gui.fill(cx - 10, cy + 9, cx + 11, cy + 10, lineCol);
            }
            default -> {
                drawDiamondRing(gui, cx, cy, 12, lineCol);
                drawBoxBrackets(gui, cx, cy, 20, 5, dimCol);
            }
        }

        int exactCx = w / 2;
        int exactCy = h / 2;
        gui.fill(exactCx - 1, exactCy - 1, exactCx + 2, exactCy + 2, coreCol);

        String zoomLabel = String.format("%.1fx", getOpticalMagnification(stack));
        gui.drawCenteredString(mc.font, zoomLabel, cx, cy - radius + 10, dimCol);

        return true;
    }

    private static int scopeAccentColor(String id, ItemStack stack) {
        return switch (id) {
            case "izanagis_burden" -> GunDataHelper.getInt(stack, "d2_honed", 0) > 1 ? 0xFFFFD84D : 0xFFE63946;
            case "whisper_of_the_worm" -> 0xFFD9EEFF;
            case "cloudstrike", "wire_rifle" -> 0xFF72D6FF;
            case "darci" -> 0xFF91E5D2;
            case "ice_breaker" -> 0xFFFF9A3C;
            case "praedyths_revenge" -> 0xFFE0B46A;
            default -> 0xFF8CE5FF;
        };
    }

    private static void drawDiamondRing(GuiGraphics gui, int cx, int cy, int r, int color) {
        for (int i = 0; i <= r; i++) {
            int j = r - i;
            gui.fill(cx + i, cy - j, cx + i + 1, cy - j + 1, color);
            gui.fill(cx - i, cy - j, cx - i + 1, cy - j + 1, color);
            gui.fill(cx + i, cy + j, cx + i + 1, cy + j + 1, color);
            gui.fill(cx - i, cy + j, cx - i + 1, cy + j + 1, color);
        }
    }

    private static void drawChevron(GuiGraphics gui, int cx, int cy, int size, int color) {
        for (int i = 0; i <= size; i++) {
            gui.fill(cx - i, cy + i, cx - i + 1, cy + i + 1, color);
            gui.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, color);
        }
    }

    private static void drawBoxBrackets(GuiGraphics gui, int cx, int cy, int half, int len, int color) {
        gui.fill(cx - half, cy - half, cx - half + len, cy - half + 1, color);
        gui.fill(cx - half, cy - half, cx - half + 1, cy - half + len, color);
        gui.fill(cx + half - len + 1, cy - half, cx + half + 1, cy - half + 1, color);
        gui.fill(cx + half, cy - half, cx + half + 1, cy - half + len, color);
        gui.fill(cx - half, cy + half, cx - half + len, cy + half + 1, color);
        gui.fill(cx - half, cy + half - len + 1, cx - half + 1, cy + half + 1, color);
        gui.fill(cx + half - len + 1, cy + half, cx + half + 1, cy + half + 1, color);
        gui.fill(cx + half, cy + half - len + 1, cx + half + 1, cy + half + 1, color);
    }
}
