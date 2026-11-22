package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.BinderAbility;
import com.jax.destiny2mc.binder.BinderCatalog;
import com.jax.destiny2mc.binder.BinderClientState;
import com.jax.destiny2mc.binder.BinderSnapshot;
import com.jax.destiny2mc.client.DestinyHudRenderer;
import com.jax.destiny2mc.client.HudConfig;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/** The existing post-hotbar callback owns this whole HUD, so switching theme, holding a new
 * weapon, or removing the last ability never leaves an independently positioned second strip. */
public final class BinderHud {
    private BinderHud() {}

    public static void render(GuiGraphics g, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null || mc.options.hideGui || player.isSpectator()) return;
        HudConfig.ensureLoaded();
        ItemStack held = player.getMainHandItem();
        WeaponSpec weapon = held.getItem() instanceof DestinyGunItem gun ? gun.spec : null;
        BinderSnapshot snap = BinderClientState.snapshot();
        boolean any = HudConfig.showAbilities && hasEquipped(snap);
        boolean tenno = held.getItem() instanceof com.jax.destiny2mc.tenno.TennoGunItem;
        if (weapon == null && !tenno && !any) return;

        long time = mc.level.getGameTime();
        int theme = HudConfig.hudStyle;
        int rowWidth = any ? AbilityHudStyles.width(theme) : 0;
        int rowHeight = any ? AbilityHudStyles.height(theme) : 0;
        boolean leftOffhand = player.getMainArm() == HumanoidArm.RIGHT && !player.getOffhandItem().isEmpty();
        CombatHudLayout at = CombatHudLayout.place(g.guiWidth(), g.guiHeight(), theme, HudConfig.getScale(),
            rowWidth, rowHeight, statusSpace(mc, player), weapon != null || tenno, leftOffhand);
        if (weapon != null)
            DestinyHudRenderer.renderWeaponCardAt(g, mc.font, held, weapon, time, partialTick,
                at.left(), at.ammoBaseline(), at.scale(), theme);
        if (tenno) com.jax.destiny2mc.tenno.client.TennoWeaponHud.draw(g,held,time,at,theme);
        if (any) drawRow(g, theme, CombatHudLayout.abilityLeft(at.left(), theme, rowWidth, at.scale()), at.abilityTop(), at.scale(), resolveSlots(snap, time));
    }

    /** Vanilla stacks high-health/absorption hearts more tightly. A safe vertical reserve keeps
     * the new stack clear of that cluster on narrow displays, including the progression edition. */
    private static int statusSpace(Minecraft mc, LocalPlayer player) {
        if (mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return 27;
        int rows = Math.max(1, (int)Math.ceil((Math.max(player.getMaxHealth(), player.getHealth())
            + player.getAbsorptionAmount()) / 20f));
        int extra = (rows - 1) * Math.max(3, 12 - rows);
        return 53 + extra + (player.getArmorValue() > 0 ? 10 : 0);
    }

    private static boolean hasEquipped(BinderSnapshot snap) {
        for (String id : snap.active()) if (BinderCatalog.get(id) != null) return true;
        return false;
    }

    /** The binding order stays fixed, including gaps and duplicate skills. A duplicate reads the
     * same synchronized cooldown as its other slot. Remapped keys come from live Minecraft input. */
    private static List<AbilityHudStyles.Slot> resolveSlots(BinderSnapshot snap, long now) {
        List<AbilityHudStyles.Slot> slots = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            BinderAbility ability = BinderCatalog.get(snap.active().get(i));
            long deadline = ability == null ? 0 : snap.readyAt().getOrDefault(ability.id(), 0L);
            int remaining = (int)Math.clamp(deadline - now, 0L, Integer.MAX_VALUE);
            slots.add(new AbilityHudStyles.Slot(ability, BinderKeys.keyName(i), remaining));
        }
        return slots;
    }

    private static void drawRow(GuiGraphics g, int theme, int left, int top, float scale, List<AbilityHudStyles.Slot> slots) {
        var pose = g.pose();
        pose.pushPose();
        pose.translate(left, top, 0);
        pose.scale(scale, scale, 1);
        AbilityHudStyles.draw(g, theme, 0, 0, slots);
        pose.popPose();
    }

    /** Renders the actual weapon card and ability painter inside the settings viewport. Characters
     * with no assignments get an illustrative row. Previewing performs no purchases or casts. */
    public static void renderPreview(GuiGraphics g, ItemStack gun, WeaponSpec spec, float partialTick,
                                     int x, int y, int width, int height) {
        if (width < 2 || height < 2) return;
        var mc = Minecraft.getInstance();
        HudConfig.ensureLoaded();
        int style = HudConfig.hudStyle;
        int rowHeight = HudConfig.showAbilities ? AbilityHudStyles.height(style) : 0;
        int w = Math.max(CombatHudLayout.ammoWidth(style), HudConfig.showAbilities ? AbilityHudStyles.width(style) : 0);
        int h = CombatHudLayout.ammoHeight(style) + rowHeight + (rowHeight > 0 ? 3 : 0);
        float scale = Math.min(HudConfig.getScale(), Math.min((width-1f)/w, (height-1f)/h));
        int left = x + (width - (int)Math.ceil(w * scale)) / 2;
        int top = y + (height - (int)Math.ceil(h * scale)) / 2;
        int baseline = top + (int)Math.ceil(CombatHudLayout.ammoHeight(style) * scale);
        long now = mc.level == null ? 0 : mc.level.getGameTime();
        g.enableScissor(x, y, x+width, y+height);
        DestinyHudRenderer.renderWeaponCardAt(g, mc.font, gun, spec, now, partialTick, left, baseline, scale, style);
        if (HudConfig.showAbilities) {
            BinderSnapshot snap = BinderClientState.snapshot();
            List<AbilityHudStyles.Slot> slots = hasEquipped(snap) ? resolveSlots(snap, now) : sampleSlots();
            drawRow(g, style, CombatHudLayout.abilityLeft(left, style, AbilityHudStyles.width(style), scale), baseline + CombatHudLayout.gap(scale), scale, slots);
        }
        g.disableScissor();
    }

    private static List<AbilityHudStyles.Slot> sampleSlots() {
        String[] ids = {"solar_grenade", "chain_lightning", "healing_rift", "towering_barricade"};
        List<AbilityHudStyles.Slot> demo = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) demo.add(new AbilityHudStyles.Slot(BinderCatalog.get(ids[i]), BinderKeys.keyName(i), i == 1 ? 86 : 0));
        return demo;
    }
}
