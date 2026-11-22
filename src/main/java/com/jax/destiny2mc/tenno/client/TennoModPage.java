package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.client.ui.GameCanvas;
import com.jax.destiny2mc.client.ui.UiCanvas;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.ModBenchNetwork;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import static com.jax.destiny2mc.tenno.client.ModCardArt.*;

/** Warframe Arsenal Loadout & Upgrades Modding page inside both Gunsmith blocks.
 * Left sidebar models Warframe's TENNO LOADOUT frosted off-white weapon cards with
 * sky-blue progress bars + a live modded stats readout; center shows smoky notched
 * upgrade sockets and metallic mod cards with a pure-white scrollbar track; right
 * shows the expanded mod card and Warframe gold Install/Fuse action bars. */
public final class TennoModPage {
    private static final String[] FILTERS = {"Usable", "Owned", "All"};
    private static final int LIST_Y = 101, LOADOUT_PAGE = 5, CARD_X = 196, CARD_Y = 244;
    private static final int COLUMNS = 3, CARD_STEP = 114, ROW_STEP = 65;
    private static final int LIBRARY_PAGE = 6, SLOTS_PAGE = 6;
    private static long serial;
    private static WeakReference<TennoModPage> focused = new WeakReference<>(null);
    private List<Integer> weapons = List.of();
    private List<WeaponMod> visible = List.of(), installed = List.of();
    private int slot = -1, inventoryOffset = 0, libraryOffset = 0, socketOffset = 0, filter = 0;
    private int boundSlot = Integer.MIN_VALUE;
    private ResourceLocation boundItem = ModBenchNetwork.AIR;
    private long pending, token;
    private byte requested;
    private WeaponMod selected;
    private String feedback = "";
    private boolean inspectInstalled = false;

    public static void receive(ModBenchNetwork.Reply reply) {
        var ui = focused.get();
        if (ui == null || ui.pending != reply.serial()) return;
        ui.pending = 0;
        if (reply.status() == ModBenchNetwork.OK) {
            ui.token = reply.token();
            ui.feedback = switch (ui.requested) {
                case ModBenchNetwork.INSTALL -> "Mod installed.";
                case ModBenchNetwork.REMOVE -> "Mod removed.";
                case ModBenchNetwork.FUSE -> "Fusion complete.";
                default -> "";
            };
        } else {
            ui.feedback = switch (reply.status()) {
                case ModBenchNetwork.NO_STATION -> "Open a nearby Gunsmith to make changes.";
                case ModBenchNetwork.STALE -> "Weapon changed. Select it again.";
                default -> "Change declined. Check the selected weapon and rank.";
            };
            if (reply.status() == ModBenchNetwork.STALE || reply.status() == ModBenchNetwork.INVALID) {
                ui.token = 0;
                ui.boundSlot = Integer.MIN_VALUE;
            }
        }
    }

    private static ItemStack weapon(Player p, int slot) { return slot < 0 ? ItemStack.EMPTY : p.getInventory().getItem(slot); }
    private static ResourceLocation id(ItemStack s) { return s.isEmpty() ? ModBenchNetwork.AIR : BuiltInRegistries.ITEM.getKey(s.getItem()); }
    private static String slotLabel(int slot) { return slot == 40 ? "OFFHAND" : slot < 9 ? "HOTBAR " + (slot + 1) : "INVENTORY " + (slot + 1); }
    private static int lastPage(int count, int page) { return Math.max(0, (count - 1) / page) * page; }

    private void bind(Player p) {
        focused = new WeakReference<>(this);
        token = 0;
        boundSlot = slot;
        boundItem = id(weapon(p, slot));
        dispatch(ModBenchNetwork.BIND, null, p);
    }

    private void refresh(Player p) {
        if (p == null) return;
        var inventory = p.getInventory();
        List<Integer> carried = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++)
            if ((i < 36 || i == 40) && TennoBlueprints.isTenno(inventory.getItem(i).getItem())) carried.add(i);
        weapons = carried;
        if (!weapons.contains(slot)) {
            slot = weapons.contains(inventory.selected) ? inventory.selected : weapons.isEmpty() ? -1 : weapons.get(0);
            socketOffset = 0;
            inspectInstalled = false;
        }
        inventoryOffset = Mth.clamp(inventoryOffset, 0, Math.max(0, weapons.size() - LOADOUT_PAGE));
        ItemStack item = weapon(p, slot);
        if (slot != boundSlot || !id(item).equals(boundItem)) bind(p);
        installed = WeaponMods.installed(item);
        visible = Arrays.stream(WeaponMod.values()).filter(m -> switch (filter) {
            case 0 -> ModCollection.rank(p, m.id()) >= 0 && (item.isEmpty() || m.fits(item));
            case 1 -> ModCollection.rank(p, m.id()) >= 0;
            default -> true;
        }).toList();
        socketOffset = Math.min(socketOffset, lastPage(installed.size(), SLOTS_PAGE));
        libraryOffset = Math.min(libraryOffset, lastPage(visible.size(), LIBRARY_PAGE));
        if (!visible.contains(selected) && !(inspectInstalled && installed.contains(selected)))
            selected = visible.isEmpty() ? null : visible.get(libraryOffset);
    }

    public void enter() { boundSlot = Integer.MIN_VALUE; token = 0; pending = 0; feedback = ""; focused = new WeakReference<>(this); }

    public void close() {
        if (focused.get() != this) return;
        PacketDistributor.sendToServer(new ModBenchNetwork.Command(++serial, token, ModBenchNetwork.CLOSE,
                slot, boundItem, "", -1));
        focused.clear(); token = 0; pending = 0;
    }

    private void dispatch(byte op, WeaponMod card, Player p) {
        requested = op; pending = ++serial;
        var stack = weapon(p, slot);
        PacketDistributor.sendToServer(new ModBenchNetwork.Command(pending, token, op, slot, id(stack),
                card == null ? "" : card.id(), card == null ? -1 : ModCollection.rank(p, card.id())));
    }

    private boolean canToggle(Player p) {
        if (selected == null || slot < 0 || pending != 0 || token == 0) return false;
        return installed.contains(selected) || (ModCollection.rank(p, selected.id()) >= 0 && selected.fits(weapon(p, slot)));
    }

    private boolean canRank(Player p) {
        if (selected == null || pending != 0 || token == 0) return false;
        int rank = ModCollection.rank(p, selected.id());
        return rank >= 0 && rank < selected.maxRank() && p.experienceLevel >= ModCollection.rankCost(rank);
    }

    private void toggle(Player p) {
        if (canToggle(p)) dispatch(installed.contains(selected) ? ModBenchNetwork.REMOVE : ModBenchNetwork.INSTALL, selected, p);
    }

    private void fuse(Player p) { if (canRank(p)) dispatch(ModBenchNetwork.FUSE, selected, p); }

    public void draw(GuiGraphics g, Font font, Player p) {
        if (p == null) return;
        refresh(p);
        UiCanvas c = new GameCanvas(g, font, i -> weapon(p, i));

        // Neutral translucent charcoal backdrop & three balanced panels
        c.fill(0, 70, 800, 380, VitruvianUi.BACKDROP);
        VitruvianUi.panel(c, 21, 76, 157, 329);
        VitruvianUi.panel(c, 185, 76, 359, 329);
        VitruvianUi.panel(c, 550, 76, 226, 329);

        // ── LEFT SIDEBAR: Warframe Arsenal TENNO LOADOUT + Live Modded Stats ──
        c.fill(26, 86, 20, 1, VitruvianUi.WHITE_RAIL);
        c.text(50, 83, "TENNO LOADOUT", VitruvianUi.IVORY, .76f);
        c.fill(149, 86, 24, 1, VitruvianUi.WHITE_RAIL);

        int end = Math.min(weapons.size(), inventoryOffset + LOADOUT_PAGE);
        for (int i = inventoryOffset; i < end; i++) {
            int n = weapons.get(i), y = LIST_Y + (i - inventoryOffset) * 30;
            boolean active = n == slot;
            ItemStack wStack = weapon(p, n);
            int modCount = WeaponMods.installed(wStack).size();

            // Frosted off-white Arsenal weapon card (selected brighter with top white cap rule)
            c.fill(26, y, 147, 27, active ? VitruvianUi.FROST_LIGHT : VitruvianUi.FROST_DIM);
            if (active) c.fill(25, y - 1, 149, 1, VitruvianUi.WHITE_RAIL);
            // Weapon sprite on right edge of card (like Excalibur/weapon thumbnail in Arsenal)
            c.item(n, 148, y + 2, 22);
            c.text(31, y + 4, font.plainSubstrByWidth(wStack.getHoverName().getString().toUpperCase(Locale.ROOT), 134),
                    VitruvianUi.FROST_INK, .80f);
            String sub = slotLabel(n) + (modCount > 0 ? "  ·  " + modCount + " MOD" + (modCount == 1 ? "" : "S") : "");
            c.text(31, y + 14, sub, VitruvianUi.FROST_SUB, .64f);
            // Sky-blue bottom progress strip (reflecting fitted sockets)
            c.fill(26, y + 25, 147, 2, 0xFF8C97A3);
            int stripW = Math.round(147 * Math.min(1f, Math.max(0.15f, modCount / 6f)));
            c.fill(26, y + 25, stripW, 2, VitruvianUi.SKY_BLUE);
        }
        if (weapons.isEmpty()) {
            ModCardArt.wrapped(c, font, 31, 114, 134, "Carry a Warframe weapon to install mods.", .90f, 5, MUTED);
        } else {
            drawSidebarStats(c, font, weapon(p, slot));
        }

        // ── TOP-CENTER: Warframe Upgrades Socket Capacity Bar + Smoky Notched Slots ──
        c.text(196, 84, "UPGRADES", IVORY, .92f);
        String capLabel = "SOCKETS  " + installed.size() + " / " + SLOTS_PAGE;
        int capW = Math.round(font.width(capLabel) * .74f);
        c.text(532 - capW, 85, capLabel, IVORY, .74f);
        // Signature Warframe sky-blue capacity bar
        c.fill(196, 97, 336, 3, 0xFF28313C);
        if (!installed.isEmpty()) {
            int fillW = Math.min(336, Math.round(336 * Math.min(1f, installed.size() / (float) SLOTS_PAGE)));
            c.fill(196, 97, fillW, 3, VitruvianUi.SKY_BLUE);
        }

        itemSlots(c, font, p);

        // ── BOTTOM-CENTER: Modding Collection Tray + White Scrollbar Track ──
        c.fill(196, 200, 336, 1, VitruvianUi.BORDER_HAIRLINE);
        c.text(196, 207, "MOD COLLECTION", IVORY, .90f);
        String ownedLabel = ModCollection.ranks(p).size() + " / " + WeaponMod.values().length + " OWNED";
        c.text(532 - Math.round(font.width(ownedLabel) * .72f), 208, ownedLabel, MUTED, .72f);

        for (int i = 0; i < 3; i++) {
            int x = 196 + i * 75;
            boolean active = i == filter;
            c.fill(x, 222, 71, 17, active ? VitruvianUi.ROW_ACTIVE : VitruvianUi.PANEL_WELL);
            c.outline(x, 222, 71, 17, active ? VitruvianUi.BORDER_HAIRLINE : VitruvianUi.RULE_SUBTLE);
            if (active) c.fill(x, 237, 71, 2, VitruvianUi.WHITE_RAIL);
            c.text(x + 8, 227, FILTERS[i].toUpperCase(Locale.ROOT), active ? IVORY : MUTED, .76f);
        }

        for (int i = 0; i < LIBRARY_PAGE && libraryOffset + i < visible.size(); i++) {
            var m = visible.get(libraryOffset + i);
            int x = CARD_X + (i % COLUMNS) * CARD_STEP;
            int y = CARD_Y + i / COLUMNS * ROW_STEP;
            ModCardArt.paint(g, c, font, m, ModCollection.rank(p, m.id()), x, y, 108, 61,
                    m == selected, false, installed.contains(m));
        }
        if (visible.isEmpty()) {
            String empty = filter == 0 && ModCollection.ranks(p).size() > 0
                    ? "No owned cards fit this weapon. Choose Owned or All."
                    : "Craft a mod pack and use it to add a card. Choose All to preview the collection.";
            ModCardArt.wrapped(c, font, 203, 264, 294, empty, .96f, 5, MUTED);
        }
        if (visible.size() > LIBRARY_PAGE) pageControls(c, libraryOffset, visible.size(), LIBRARY_PAGE, 196, 379);

        // Signature Warframe pure-white horizontal scrollbar track along the bottom of the mod tray
        c.fill(196, 398, 336, 2, 0x448B96A3);
        int totalPages = Math.max(1, (visible.size() + LIBRARY_PAGE - 1) / LIBRARY_PAGE);
        int curPage = libraryOffset / LIBRARY_PAGE;
        int thumbW = Math.max(36, 336 / totalPages);
        int thumbX = 196 + (totalPages == 1 ? 0 : Math.round((336 - thumbW) * curPage / (float) (totalPages - 1)));
        c.fill(thumbX, 398, thumbW, 2, VitruvianUi.WHITE_RAIL);

        drawDetail(g, c, font, p);

        // Footer bar
        c.fill(27, 414, 746, 1, VitruvianUi.BORDER_HAIRLINE);
        c.text(27, 428, "ESC   CLOSE", MUTED, .78f);
        boolean error = !feedback.isEmpty() && !feedback.endsWith("installed.") && !feedback.endsWith("removed.") && !feedback.endsWith("complete.");
        int fbColor = feedback.isEmpty() ? MUTED : (error ? VitruvianUi.NEGATIVE : VitruvianUi.POSITIVE);
        c.text(196, 428, feedback.isEmpty() ? "ENTER   INSTALL / REMOVE     ·     F   FUSE RANK" : feedback, fbColor, .80f);
    }

    /** Translucent live stats block below the Loadout cards (modeled after Warframe's Arsenal/Upgrades stats). */
    private void drawSidebarStats(UiCanvas c, Font font, ItemStack stack) {
        if (stack.isEmpty()) return;
        int top = 258;
        c.fill(26, top, 147, 141, 0xB810141A);
        c.outline(26, top, 147, 141, VitruvianUi.RULE_SUBTLE);
        c.fill(26, top, 147, 2, VitruvianUi.SKY_BLUE);

        TennoInspection.Info info = TennoInspection.describe(stack);
        c.text(31, top + 6, font.plainSubstrByWidth(stack.getHoverName().getString().toUpperCase(Locale.ROOT), 160), IVORY, .76f);
        c.fill(31, top + 17, 137, 1, VitruvianUi.RULE_SUBTLE);

        int y = top + 22;
        int maxRows = Math.min(8, info.stats().size());
        for (int i = 0; i < maxRows; i++) {
            TennoInspection.Stat st = info.stats().get(i);
            boolean boosted = TennoInspection.isBoosted(stack, st);
            int valColor = boosted ? VitruvianUi.POSITIVE
                    : ("Installed mods".equals(st.title()) ? VitruvianUi.SKY_BLUE_SOFT : IVORY);
            c.text(31, y, font.plainSubstrByWidth(st.title(), 98), MUTED, .72f);
            String val = font.plainSubstrByWidth(st.value(), 78);
            int vw = Math.round(font.width(val) * .72f);
            c.text(168 - vw, y, val, valColor, .72f);
            y += 14;
        }
    }

    private void itemSlots(UiCanvas c, Font font, Player p) {
        for (int i = 0; i < SLOTS_PAGE; i++) {
            int x = 196 + i % 3 * 114, y = 105 + i / 3 * 34;
            if (socketOffset + i < installed.size()) {
                WeaponMod m = installed.get(socketOffset + i);
                boolean active = m == selected;
                int metal = VitruvianUi.rarityFrame(m.rarity());
                int rank = ModCollection.rank(p, m.id());
                c.fill(x, y, 108, 30, VitruvianUi.rarityBgTop(m.rarity()));
                c.fill(x + 1, y + 1, 106, 2, active ? VitruvianUi.WHITE_RAIL : metal);
                c.outline(x, y, 108, 30, active ? VitruvianUi.GOLD : metal);
                VitruvianUi.drawPolarity(c, VitruvianUi.polarityOf(m), x + 96, y + 5,
                        rank < 0 ? VitruvianUi.DISABLED : VitruvianUi.POSITIVE);
                c.text(x + 5, y + 6, font.plainSubstrByWidth(m.title(), 102), IVORY, .82f);
                c.text(x + 5, y + 18, rank < 0 ? "DORMANT" : "RANK " + rank,
                        rank < 0 ? VitruvianUi.DISABLED : VitruvianUi.CYAN, .68f);
                VitruvianUi.drawRankPips(c, x + 78, y + 20, m.maxRank(), rank, 44, false);
            } else {
                VitruvianUi.smokySlot(c, x, y, 108, 30);
            }
        }
        if (installed.size() > SLOTS_PAGE) pageControls(c, socketOffset, installed.size(), SLOTS_PAGE, 196, 177);
        else if (slot >= 0) {
            c.text(196, 180, font.plainSubstrByWidth(weapon(p, slot).getHoverName().getString().toUpperCase(Locale.ROOT), 265),
                    VitruvianUi.MUTED, .76f);
        }
        if(installed.size()>SLOTS_PAGE){
            pageControls(c,socketOffset,installed.size(),SLOTS_PAGE,196,177);
            if(slot>=0){
                String elem=WeaponMods.elementalSummary(p,weapon(p,slot));
                if(!elem.isEmpty())c.text(308,181,font.plainSubstrByWidth(elem,220),JADE,.74f);
            }
        }else if(slot>=0){
            String elem=WeaponMods.elementalSummary(p,weapon(p,slot));
            if(!elem.isEmpty())c.text(196,178,font.plainSubstrByWidth(elem,330),JADE,.8f);
            else c.text(196,178,font.plainSubstrByWidth(weapon(p,slot).getHoverName().getString(),265),MUTED,.8f);
        }
    }

    private static void pageControls(UiCanvas c, int off, int count, int size, int x, int y) {
        c.fill(x, y, 24, 17, VitruvianUi.PANEL_WELL);
        c.outline(x, y, 24, 17, VitruvianUi.BORDER_HAIRLINE);
        c.text(x + 8, y + 4, "<", IVORY, .9f);
        c.text(x + 33, y + 4, (off / size + 1) + " / " + ((count + size - 1) / size), MUTED, .82f);
        c.fill(x + 75, y, 24, 17, VitruvianUi.PANEL_WELL);
        c.outline(x + 75, y, 24, 17, VitruvianUi.BORDER_HAIRLINE);
        c.text(x + 83, y + 4, ">", IVORY, .9f);
    }

    private void drawDetail(GuiGraphics g, UiCanvas c, Font f, Player p) {
        if (selected == null) {
            c.text(565, 92, "MOD INSPECTION", IVORY, .96f);
            c.fill(565, 108, 196, 1, VitruvianUi.BORDER_HAIRLINE);
            ModCardArt.wrapped(c, f, 565, 120, 190, "Select a card to inspect its effect and fusion rank.", .94f, 4, MUTED);
            return;
        }
        var m = selected;
        int rank = ModCollection.rank(p, m.id());
        ModCardArt.paint(g, c, f, m, rank, 565, 83, 197, 172, false, true, installed.contains(m));
        if (rank >= 0 && rank < m.maxRank()) {
            c.text(565, 263, "NEXT RANK  (+" + (rank + 1) + ")", VitruvianUi.GOLD, .78f);
            ModCardArt.wrapped(c, f, 565, 276, 198, m.effect(rank + 1), .88f, 2, VitruvianUi.POSITIVE);
        } else {
            ModCardArt.wrapped(c, f, 565, 264, 198,
                    rank < 0 ? "Unlock this card by opening a compatible mod pack." : m.description(), .86f, 3, MUTED);
        }
        String place = installed.contains(m) ? "REMOVE MOD" : rank < 0 ? "CARD LOCKED"
                : slot < 0 ? "SELECT A WEAPON" : !m.fits(weapon(p, slot)) ? "INCOMPATIBLE" : "INSTALL MOD";
        button(c, 565, 312, 198, place, canToggle(p));
        c.text(565, 343, "AVAILABLE: " + p.experienceLevel + " XP LEVELS", VitruvianUi.MUTED, .74f);
        int cost = rank >= 0 ? ModCollection.rankCost(rank) : 0;
        String fusion = rank < 0 ? "FUSION LOCKED" : rank >= m.maxRank() ? "MAX RANK"
                : "FUSE   ·   " + cost + " XP LEVEL" + (cost == 1 ? "" : "S");
        button(c, 565, 354, 198, fusion, canRank(p));
    }

    private static void button(UiCanvas c, int x, int y, int w, String s, boolean enabled) {
        c.fill(x, y, w, 27, enabled ? VitruvianUi.GOLD : VitruvianUi.SLATE_BAR_DARK);
        c.outline(x, y, w, 27, enabled ? VitruvianUi.WHITE_RAIL : VitruvianUi.BORDER_HAIRLINE);
        if (enabled) c.fill(x - 1, y - 1, w + 2, 1, VitruvianUi.WHITE_RAIL);
        c.text(x + 12, y + 9, s, enabled ? VitruvianUi.FROST_INK : VitruvianUi.MUTED, .88f);
    }

    public void hover(GuiGraphics g, double x, double y, int mx, int my, Player p) {
        if (p == null) return;
        if (x >= 26 && x < 173 && y >= LIST_Y && y < LIST_Y + LOADOUT_PAGE * 30) {
            int i = inventoryOffset + (int) (y - LIST_Y) / 30;
            if (i >= 0 && i < weapons.size()) {
                TennoInspection.draw(g, weapon(p, weapons.get(i)), mx, my);
                return;
            }
        }
        if (x >= 196 && x < 533 && y >= 105 && y < 169) {
            int col = (int) (x - 196) / 114, row = (int) (y - 105) / 34, i = socketOffset + row * 3 + col;
            if (col < 3 && (x - 196) % 114 < 108 && (y - 105) % 34 < 30 && i < installed.size()) {
                WeaponMod m = installed.get(i);
                TennoInspection.drawModTooltip(g, m, ModCollection.rank(p, m.id()), true, mx, my);
                return;
            }
        }
        if (x >= CARD_X && x < CARD_X + 3 * CARD_STEP && y >= CARD_Y && y < CARD_Y + 2 * ROW_STEP) {
            int col = (int) (x - CARD_X) / CARD_STEP, row = (int) (y - CARD_Y) / ROW_STEP;
            int i = libraryOffset + row * 3 + col;
            if ((x - CARD_X) % CARD_STEP < 108 && (y - CARD_Y) % ROW_STEP < 61 && i >= 0 && i < visible.size()) {
                WeaponMod m = visible.get(i);
                TennoInspection.drawModTooltip(g, m, ModCollection.rank(p, m.id()), installed.contains(m), mx, my);
            }
        }
    }

    public boolean click(double x, double y, Player p) {
        refresh(p);
        if (x >= 26 && x < 173 && y >= LIST_Y && y < LIST_Y + LOADOUT_PAGE * 30) {
            int i = inventoryOffset + (int) (y - LIST_Y) / 30;
            if (i < weapons.size()) {
                slot = weapons.get(i);
                socketOffset = libraryOffset = 0;
                selected = null; feedback = ""; inspectInstalled = false;
                refresh(p);
            }
            return true;
        }
        if (x >= 196 && x < 533 && y >= 105 && y < 169) {
            int col = (int) (x - 196) / 114, row = (int) (y - 105) / 34, i = socketOffset + row * 3 + col;
            if (col < 3 && (x - 196) % 114 < 108 && (y - 105) % 34 < 30 && i < installed.size()) {
                selected = installed.get(i); inspectInstalled = true; feedback = "";
            }
            return true;
        }
        if (y >= 177 && y < 194 && installed.size() > SLOTS_PAGE) {
            int step = pageDirection(x);
            if (step != 0) { socketOffset = clampPage(socketOffset, step, SLOTS_PAGE, installed.size()); return true; }
        }
        if (x >= 196 && x < 417 && y >= 222 && y < 239) {
            int i = (int) (x - 196) / 75;
            if (i < 3 && (x - 196) % 75 < 71) {
                filter = i; libraryOffset = 0; selected = null; inspectInstalled = false; feedback = "";
                refresh(p);
            }
            return true;
        }
        if (x >= CARD_X && x < CARD_X + 3 * CARD_STEP && y >= CARD_Y && y < CARD_Y + 2 * ROW_STEP) {
            int col = (int) (x - CARD_X) / CARD_STEP, row = (int) (y - CARD_Y) / ROW_STEP;
            int i = libraryOffset + row * 3 + col;
            if ((x - CARD_X) % CARD_STEP < 108 && (y - CARD_Y) % ROW_STEP < 61 && i < visible.size()) {
                selected = visible.get(i); inspectInstalled = false; feedback = "";
            }
            return true;
        }
        if (y >= 379 && y < 396 && visible.size() > LIBRARY_PAGE) {
            int step = pageDirection(x);
            if (step != 0) { libraryOffset = clampPage(libraryOffset, step, LIBRARY_PAGE, visible.size()); return true; }
        }
        if (x >= 565 && x < 763 && y >= 312 && y < 339) { toggle(p); return true; }
        if (x >= 565 && x < 763 && y >= 354 && y < 381) { fuse(p); return true; }
        return false;
    }

    private static int pageDirection(double x) { return x >= 196 && x < 220 ? -1 : x >= 271 && x < 295 ? 1 : 0; }
    private static int clampPage(int offset, int step, int size, int total) { return Mth.clamp(offset + step * size, 0, lastPage(total, size)); }

    public boolean scroll(double x, double y, double dy) {
        int step = dy > 0 ? -1 : 1;
        if (dy == 0) return false;
        if (x >= 25 && x < 176 && y >= 100 && y <= 255) {
            inventoryOffset = Mth.clamp(inventoryOffset + step, 0, Math.max(0, weapons.size() - LOADOUT_PAGE));
            return true;
        }
        if (x >= 192 && x < 542 && y >= 101 && y < 195) {
            socketOffset = clampPage(socketOffset, step, SLOTS_PAGE, installed.size());
            return true;
        }
        if (x >= 192 && x < 542 && y >= 222 && y < 404) {
            libraryOffset = clampPage(libraryOffset, step, LIBRARY_PAGE, visible.size());
            return true;
        }
        return false;
    }

    public boolean key(int key, Player p) {
        refresh(p);
        switch (key) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> toggle(p);
            case GLFW.GLFW_KEY_F -> fuse(p);
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN -> {
                if (!visible.isEmpty()) {
                    int delta = key == GLFW.GLFW_KEY_LEFT ? -1 : key == GLFW.GLFW_KEY_RIGHT ? 1 : key == GLFW.GLFW_KEY_UP ? -COLUMNS : COLUMNS;
                    int i = Mth.clamp(Math.max(0, visible.indexOf(selected)) + delta, 0, visible.size() - 1);
                    selected = visible.get(i);
                    libraryOffset = i / LIBRARY_PAGE * LIBRARY_PAGE;
                    inspectInstalled = false;
                }
            }
            default -> { return false; }
        }
        return true;
    }
}
