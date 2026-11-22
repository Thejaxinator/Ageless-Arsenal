package com.jax.destiny2mc.client.ui;

import com.jax.destiny2mc.binder.client.GearHoverCard;
import com.jax.destiny2mc.client.ClientScreenHelper;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.network.GunsmithPerkSwapPayload;
import com.jax.destiny2mc.tenno.client.TennoFoundryPage;
import com.jax.destiny2mc.tenno.client.TennoModPage;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import static com.jax.destiny2mc.client.ui.GunsmithNavigation.*;

/** Both Gunsmith blocks open this controller. Navigation is local to one visit. Craft,
 * perk and collection requests keep the server-side validation of their respective tools. */
public class GunsmithScreen extends Screen {
    private int franchise = DESTINY;
    private final int[] tools = {FIRST_TOOL, FIRST_TOOL};
    private int selected, scroll, socket, chosen = -1, cooldown;
    private final TennoFoundryPage foundry = new TennoFoundryPage();
    private final TennoModPage mods = new TennoModPage();
    private String status = "";

    public GunsmithScreen() { super(Component.literal("Gunsmith")); }
    public int activeFranchise() { return franchise; }
    public int activeTool() { return tools[franchise]; }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void tick() { if (cooldown > 0) cooldown--; }
    @Override public void removed() { mods.close(); super.removed(); }
    private float scale() { return Math.min(width / 800f, height / 450f); }
    private double mx(double x) { return (x - (width - 800 * scale()) / 2) / scale(); }
    private double my(double y) { return (y - (height - 450 * scale()) / 2) / scale(); }
    private boolean onMods() { return franchise == WARFRAME && activeTool() == SECOND_TOOL; }

    private void navigate(int nextFranchise, int nextTool) {
        if (nextFranchise == franchise && nextTool == activeTool()) return;
        if (nextFranchise == DESTINY && nextTool != tools[DESTINY]) {
            selected = 0; scroll = 0; socket = 0; chosen = -1; status = "";
        }
        franchise = nextFranchise;
        tools[franchise] = nextTool;
        // Returning to Mods reacquires the real carried stack. Closing its session when
        // changing tools would revoke access to the bench for the remainder of this visit.
        if (onMods()) mods.enter();
        minecraft.getNarrator().sayNow(Component.literal(location(franchise, activeTool())));
    }

    private ItemStack item(int id) {
        if (minecraft == null || minecraft.player == null || id < 0) return ItemStack.EMPTY;
        return id >= 1000 ? minecraft.player.getInventory().getItem(id - 1000) : GunsmithRecipes.createResultStack(id);
    }

    private GunsmithLayout.State snapshot() {
        Player player = minecraft.player;
        boolean crafting = activeTool() == FIRST_TOOL;
        List<GunsmithLayout.Row> rows = new ArrayList<>();
        if (player != null && crafting) {
            for (WeaponSpec gun : WeaponSpec.ALL) {
                if (GunsmithRecipes.visible(player, gun))
                    rows.add(new GunsmithLayout.Row(GunsmithRecipes.recipeForWeapon(gun), gun.displayName, gun.frameLabel));
            }
        } else if (player != null) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof DestinyGunItem gun)
                    rows.add(new GunsmithLayout.Row(1000 + i, gun.spec.displayName, "Inventory slot " + (i + 1)));
            }
        }
        selected = Mth.clamp(selected, 0, Math.max(0, rows.size() - 1));
        scroll = Mth.clamp(scroll, 0, Math.max(0, rows.size() - 12));
        int id = rows.isEmpty() ? -1 : rows.get(selected).id();
        ItemStack stack = item(id);
        WeaponSpec gun = stack.getItem() instanceof DestinyGunItem weapon ? weapon.spec : null;
        List<GunsmithLayout.Cost> costs = new ArrayList<>();
        List<GunsmithLayout.Row> options = new ArrayList<>();
        String[] sockets = gun == null ? new String[0] : new String[6];
        String description = "Select an owned weapon to customize it.";
        boolean canApply = false;
        if (gun != null && crafting) {
            for (var cost : GunsmithRecipes.costs(player, id)) {
                if (cost.item() != null && cost.count() > 0)
                    costs.add(new GunsmithLayout.Cost(cost.item().getDescription().getString(),
                        GunsmithRecipes.countInInventory(player, cost.item()), cost.count()));
            }
            canApply = GunsmithRecipes.canCraft(player, id) && cooldown == 0;
        } else if (gun != null) {
            for (int i = 0; i < sockets.length; i++)
                sockets[i] = WeaponPerkRegistry.getPerkName(WeaponPerkRegistry.getActivePerk(stack, gun, i));
            int active = WeaponPerkRegistry.getActivePerk(stack, gun, socket);
            if (!WeaponPerkRegistry.isValidPerkForWeapon(gun.index, socket, chosen)) chosen = active;
            for (int i = 0; i < WeaponPerkRegistry.optionCount(socket); i++) {
                int idOption = WeaponPerkRegistry.getPoolOption(gun.index, socket, i);
                options.add(new GunsmithLayout.Row(idOption, WeaponPerkRegistry.getPerkName(idOption), ""));
            }
            description = WeaponPerkRegistry.getPerkDesc(chosen);
            canApply = chosen != active && cooldown == 0;
        }
        return new GunsmithLayout.State(activeTool(), selected, scroll, rows,
            gun == null ? "NO WEAPON SELECTED" : gun.displayName, gun == null ? "" : gun.frameLabel,
            gun == null ? "" : gun.perkTitle + ": " + gun.perkDesc, id, costs, canApply,
            sockets, socket, options, chosen, description, status);
    }

    @Override public void renderBackground(GuiGraphics g, int x, int y, float t) {}
    @Override public void render(GuiGraphics g, int x, int y, float t) {
        boolean tenno = franchise == WARFRAME;
        double localX = mx(x), localY = my(y);
        GunsmithLayout.State state = tenno ? null : snapshot();
        g.fill(0, 0, width, height, tenno ? 0xff151f1c : 0xff111b22);
        float factor = scale();
        g.pose().pushPose();
        g.pose().translate((width - 800 * factor) / 2, (height - 450 * factor) / 2, 0);
        g.pose().scale(factor, factor, 1);
        UiCanvas canvas = new GameCanvas(g, font, this::item);
        if (!tenno) GunsmithLayout.draw(canvas, state);
        else if (onMods()) mods.draw(g, font, minecraft.player);
        else foundry.draw(g, font, minecraft.player);
        GunsmithNavigation.draw(canvas, franchise, activeTool(), localX, localY);
        g.pose().popPose();
        if (!tenno) hoverDestiny(g, state, localX, localY, x, y);
        else if (!onMods()) foundry.hover(g, localX, localY, x, y);
    }

    private void hoverDestiny(GuiGraphics g, GunsmithLayout.State state, double x, double y, int screenX, int screenY) {
        int row = GunsmithLayout.rowHit(x, y, scroll, state.rows().size());
        boolean model = activeTool() == FIRST_TOOL ? x >= 272 && x < 474 && y >= 137 && y < 339
            : x >= 297 && x < 452 && y >= 123 && y < 239;
        if (row < 0 && !model) return;
        ItemStack stack = item(row >= 0 ? state.rows().get(row).id() : state.item());
        if (stack.getItem() instanceof DestinyGunItem) GearHoverCard.drawItem(g, stack, screenX, screenY);
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(x, y, button);
        double a = mx(x), b = my(y);
        int group = franchiseAt(a, b);
        if (group >= 0) { navigate(group, tools[group]); return true; }
        int tool = toolAt(a, b);
        if (tool >= 0) { navigate(franchise, tool); return true; }
        if (franchise == WARFRAME) return onMods() ? mods.click(a, b, minecraft.player) : foundry.click(a, b, minecraft.player);
        var state = snapshot();
        int row = GunsmithLayout.rowHit(a, b, scroll, state.rows().size());
        if (row >= 0) { selected = row; chosen = -1; status = ""; return true; }
        if (activeTool() == SECOND_TOOL) {
            int slot = GunsmithLayout.socketHit(a, b);
            if (slot >= 0) { socket = slot; chosen = -1; status = ""; return true; }
            int option = GunsmithLayout.optionHit(a, b, state.options().size());
            if (option >= 0) { chosen = state.options().get(option).id(); return true; }
        }
        boolean applyBox = activeTool() == FIRST_TOOL ? a >= 526 && a < 772 && b >= 363 && b < 389
            : a >= 631 && a < 772 && b >= 411 && b < 437;
        if (applyBox) { applyDestiny(state); return true; }
        return super.mouseClicked(x, y, button);
    }

    private void applyDestiny(GunsmithLayout.State state) {
        if (!state.canApply()) return;
        if (activeTool() == FIRST_TOOL) {
            ClientScreenHelper.sendCraftPacket(state.item());
            status = "Craft requested.";
        } else {
            sendPerkSwapPacket(state.item() - 1000, socket, chosen);
            status = "Perk change requested.";
        }
        cooldown = 12;
    }

    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        double a = mx(x), b = my(y);
        if (franchise == WARFRAME) return onMods() ? mods.scroll(a, b, dy) : foundry.scroll(a, b, dy);
        if (a < 26 || a >= 224 || b < 115 || b >= 402 || dy == 0) return false;
        scroll = Mth.clamp(scroll + (dy > 0 ? -3 : 3), 0, Math.max(0, snapshot().rows().size() - 12));
        return true;
    }

    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_TAB) {
            int group = 1 - franchise;
            navigate(group, tools[group]);
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT_BRACKET || key == GLFW.GLFW_KEY_RIGHT_BRACKET ||
                (franchise == WARFRAME && key == GLFW.GLFW_KEY_M)) {
            navigate(franchise, 1 - activeTool());
            return true;
        }
        boolean used = franchise == DESTINY ? destinyKey(key, (modifiers & GLFW.GLFW_MOD_SHIFT) != 0)
            : onMods() ? mods.key(key, minecraft.player) : foundry.key(key, minecraft.player);
        return used || super.keyPressed(key, scan, modifiers);
    }

    private boolean destinyKey(int key, boolean shift) {
        var state = snapshot();
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { applyDestiny(state); return true; }
        if (activeTool() == SECOND_TOOL && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT)) {
            socket = Math.floorMod(socket + (key == GLFW.GLFW_KEY_RIGHT ? 1 : -1), 6);
            chosen = -1; status = ""; return true;
        }
        if (key != GLFW.GLFW_KEY_UP && key != GLFW.GLFW_KEY_DOWN) return false;
        int step = key == GLFW.GLFW_KEY_DOWN ? 1 : -1;
        if (shift && activeTool() == SECOND_TOOL && !state.options().isEmpty()) {
            int i = 0;
            while (i < state.options().size() && state.options().get(i).id() != chosen) i++;
            chosen = state.options().get(Math.floorMod(i + step, state.options().size())).id();
        } else {
            selected = Mth.clamp(selected + step, 0, Math.max(0, state.rows().size() - 1));
            scroll = Mth.clamp(scroll, Math.max(0, selected - 11), selected);
            chosen = -1;
        }
        status = ""; return true;
    }

    public static int findGunSlot(Player p) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++)
            if (p.getInventory().getItem(i).getItem() instanceof DestinyGunItem) return i;
        return -1;
    }
    public static void sendPerkSwapPacket(int inv, int slot, int perk) {
        PacketDistributor.sendToServer(new GunsmithPerkSwapPayload(inv, slot, perk));
    }
}
