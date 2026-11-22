package com.jax.destiny2mc.binder.client;

/** The weapon card anchors the lower-left group. The weapon renderer works upwards from a baseline;
 * the ability renderer works downwards from its top. All output coordinates are GUI pixels. */
public record CombatHudLayout(int left, int ammoBaseline, int abilityTop, float scale) {
    private static final int MARGIN = 8;
    private static final int LEFT = 10;

    public static int ammoWidth(int style) {
        return switch (style) {
            case 0 -> 134;
            case 1 -> 128;
            case 2 -> 112; // Also covers the bounded, 17-character weapon name.
            case 3 -> 139;
            case 4 -> 142;
            default -> 136;
        };
    }

    public static int ammoHeight(int style) {
        return switch (style) {
            case 0 -> 30;
            case 1 -> 34;
            case 2 -> 28;
            case 3 -> 21;
            case 4 -> 55;
            default -> 32;
        };
    }

    /** A row uses the themed ammo card as its horizontal reference, even when a weapon is
     * holstered. Minimal and Borderlands 2 deliberately keep a flush left edge. */
    public static int abilityLeft(int left, int style, int rowWidth, float scale) {
        return switch (style) {
            case 2, 3 -> left;
            default -> left + Math.max(0, Math.round((ammoWidth(style) - rowWidth) * scale * 0.5f));
        };
    }

    public static int gap(float scale) {
        return Math.max(1, (int)Math.ceil(3 * scale));
    }

    /** reservedBottom includes vanilla status rows, armor and offhand when the left HUD extends
     * into the centered status strip. On wider screens the user's original anchor stays intact. */
    public static CombatHudLayout place(int guiWidth, int guiHeight, int style, float scale,
                                         int abilityWidth, int abilityHeight, int reservedBottom) {
        return place(guiWidth, guiHeight, style, scale, abilityWidth, abilityHeight, reservedBottom, true, false);
    }

    public static CombatHudLayout place(int guiWidth, int guiHeight, int style, float scale,
                                         int abilityWidth, int abilityHeight, int reservedBottom,
                                         boolean armed, boolean hasOffhand) {
        int baseWidth = armed ? ammoWidth(style) : 0;
        int rowEnd = abilityWidth == 0 ? 0 : abilityLeft(0, style, abilityWidth, 1f) + abilityWidth;
        int contentWidth = Math.max(baseWidth, rowEnd);
        int left = Math.min(LEFT, Math.max(0, guiWidth / 8));
        float fitted = Math.min(scale, (float)Math.max(1, guiWidth - left - 2) / Math.max(1, contentWidth));
        int ammoRight = left + (int)Math.ceil(baseWidth * fitted);
        int abilityRight = abilityWidth == 0 ? left : abilityLeft(left, style, abilityWidth, fitted)
            + (int)Math.ceil(abilityWidth * fitted);
        int estimatedRight = Math.max(ammoRight, abilityRight);
        int hotbarLeft = guiWidth / 2 - (hasOffhand ? 121 : 91);
        int bottom = guiHeight - (estimatedRight + 4 > hotbarLeft ? Math.max(MARGIN, reservedBottom) : MARGIN);
        // Keep room for a live perk banner at -66 above the ammo baseline. On a short window,
        // fit the group vertically before letting it draw over a vanilla row.
        int logicalHeight = (armed ? 66 : 0) + abilityHeight + (armed && abilityHeight > 0 ? 3 : 0);
        if (bottom < 5) bottom = Math.max(4, guiHeight - MARGIN);
        fitted = Math.min(fitted, (float)Math.max(1, bottom - 3) / Math.max(1, logicalHeight));
        int rowPixels = (int)Math.ceil(abilityHeight * fitted);
        int gap = armed && abilityHeight > 0 ? gap(fitted) : 0;
        return new CombatHudLayout(left, bottom - rowPixels - gap, bottom - rowPixels, fitted);
    }
}
