package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.BinderAbility;
import com.jax.destiny2mc.binder.BinderCatalog;
import com.jax.destiny2mc.binder.BinderClientState;
import com.jax.destiny2mc.binder.BinderContent;
import com.jax.destiny2mc.client.HudConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Client-side, read-only drawer. The inventory owns its screen, menu, widgets and slot coordinates. */
@EventBusSubscriber(modid = "destiny2mc", value = Dist.CLIENT)
public final class BinderInventory {
    private static final int PAPER_HEIGHT = 166;
    private static final int PAPER_WIDTH = 153;
    private static final int MIN_WIDTH = 54;
    private static final int TAB_WIDTH = 12;
    private static final int TAB_HEIGHT = 22;
    private static final int INK = 0xff404040;
    private static final int MUTED = 0xff515151;
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation XP_BASE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation XP_FILL = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    @SubscribeEvent public static void initialize(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen)) return;
        HudConfig.ensureLoaded();
        event.addListener(new DrawerToggle((AbstractContainerScreen<?>)event.getScreen()));
        if (HudConfig.inventoryDrawerOpen) BinderClientState.refresh();
    }

    private static DrawerToggle drawer(Screen screen) {
        if (!(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)) return null;
        for (var child : screen.children()) if (child instanceof DrawerToggle drawer) return drawer;
        return null;
    }

    @SubscribeEvent public static void beforeRender(ScreenEvent.Render.Pre event) {
        var drawer = drawer(event.getScreen());
        if (drawer != null) drawer.layout();
    }

    @SubscribeEvent public static void afterRender(ScreenEvent.Render.Post event) {
        var drawer = drawer(event.getScreen());
        if (drawer == null) return;
        // The narrow recipe book bypasses the vanilla screen's renderables loop.
        if (drawer.bookOnly()) drawer.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        drawer.tooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent public static void press(ScreenEvent.MouseButtonPressed.Pre event) {
        var drawer = drawer(event.getScreen());
        if (drawer == null) return;
        drawer.layout();
        double x = event.getMouseX(), y = event.getMouseY();
        if (!drawer.isMouseOver(x,y) && !drawer.onPaper(x,y)) return;
        if (event.getButton() == 0 && drawer.isMouseOver(x,y)) {
            drawer.mouseClicked(x,y,0);
            event.getScreen().setFocused(drawer);
        }
        // Without consuming the paper hit, vanilla treats the point as an outside click and drops the carried stack.
        drawer.pressButton = event.getButton();
        event.setCanceled(true);
    }

    @SubscribeEvent public static void release(ScreenEvent.MouseButtonReleased.Pre event) {
        var drawer = drawer(event.getScreen());
        if (drawer != null && drawer.pressButton == event.getButton()) {
            drawer.pressButton = -1;
            event.setCanceled(true);
        }
    }

    public static final class DrawerToggle extends Button {
        private final AbstractContainerScreen<?> inventory;
        private final ItemStack mote = new ItemStack(BuiltInRegistries.ITEM.get(BinderContent.MOTE));
        private int anchor, paperWidth, paperLeft, paperTop, shown, pressButton = -1;
        private boolean tucked;
        private float progress;
        private long lastFrame = Util.getMillis();

        DrawerToggle(AbstractContainerScreen<?> inventory) {
            super(0,0,TAB_WIDTH,TAB_HEIGHT,Component.literal("Show abilities"), b -> ((DrawerToggle)b).toggle(), DEFAULT_NARRATION);
            this.inventory = inventory;
            layout();
        }

        private boolean bookOpen() {
            return inventory instanceof InventoryScreen survival && survival.getRecipeBookComponent().isVisible();
        }
        private boolean bookOnly() { return inventory.width < 379 && bookOpen(); }

        private void layout() {
            boolean book = bookOpen();
            // Vanilla's recipe book is 147px wide. Its category tabs project up to 32px farther left.
            int bookLeft = (inventory.width - 147) / 2 - (inventory.width < 379 ? 0 : 86) - 33;
            anchor = Math.min(inventory.getGuiLeft(), book ? bookLeft : inventory.getGuiLeft());
            // Keep one notch-width clear. Even when fully extended on a compact display,
            // the handle stays outside the paper and never covers an ability slot.
            paperWidth = Math.min(PAPER_WIDTH, Math.max(0, anchor - TAB_WIDTH - 1));
            tucked = book && paperWidth < MIN_WIDTH;
            paperTop = Math.clamp(inventory.getGuiTop(), 0, Math.max(0, inventory.height - PAPER_HEIGHT));
            boolean expanded = HudConfig.inventoryDrawerOpen && !tucked;
            long now = Util.getMillis();
            float delta = Math.max(0, now - lastFrame) / 210f;
            lastFrame = now;
            progress = Math.clamp(progress + (expanded ? delta : -delta), 0f, 1f);
            float eased = progress * progress * (3 - 2 * progress);
            shown = tucked ? 0 : Math.round(paperWidth * eased);
            paperLeft = anchor - shown;
            int notchEdge = paperLeft;
            int notchY = paperTop + 30;
            // If the recipe tabs touch the display edge, use the book's bottom-left rail.
            // Vanilla category tabs occupy the upper part of that rail.
            if (tucked && notchEdge < TAB_WIDTH) {
                notchEdge = (inventory.width - 147) / 2 - (bookOnly() ? 0 : 86);
                notchY = paperTop + PAPER_HEIGHT - TAB_HEIGHT - 4;
            }
            setPosition(Math.clamp(notchEdge - TAB_WIDTH, 0, Math.max(0, inventory.width - TAB_WIDTH)),
                Math.clamp(notchY, 0, Math.max(0, inventory.height - TAB_HEIGHT)));
            String label = expanded ? "Hide abilities" : "Show abilities";
            if (!getMessage().getString().equals(label)) setMessage(Component.literal(label));
        }

        private void toggle() {
            layout();
            if (tucked) {
                // Invoke the original recipe button so vanilla recenters its inventory and updates recipe settings.
                if (inventory instanceof InventoryScreen survival) {
                    int x = survival.getGuiLeft() + 104, y = survival.height / 2 - 22;
                    for (var child : survival.children()) if (child instanceof ImageButton recipe && recipe.getX() == x && recipe.getY() == y) {
                        recipe.onPress();
                        break;
                    }
                }
                HudConfig.inventoryDrawerOpen = true;
            } else HudConfig.inventoryDrawerOpen = !HudConfig.inventoryDrawerOpen;
            HudConfig.save();
            if (HudConfig.inventoryDrawerOpen) BinderClientState.refresh();
            layout();
        }

        private boolean onPaper(double x, double y) {
            return shown > 0 && x >= paperLeft && x < anchor && y >= paperTop && y < paperTop + PAPER_HEIGHT;
        }

        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float tick) {
            if (shown > 0) {
                g.enableScissor(paperLeft,paperTop,anchor,paperTop + PAPER_HEIGHT);
                g.pose().pushPose();
                g.pose().translate(paperLeft,paperTop,0);
                drawPaper(g);
                g.pose().popPose();
                g.disableScissor();
            }
            drawNotch(g, isMouseOver(mx,my) || (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()));
        }

        /** A slice of the same inventory rail carries the handle. Keep the attaching edge flat,
         * and point the arrow toward the direction of the next slide. The action name remains
         * on this native Button for tab navigation, narration and the small hover hint. */
        private void drawNotch(GuiGraphics g, boolean highlighted) {
            int x=getX(), y=getY();
            var sheet=AbstractContainerScreen.INVENTORY_LOCATION;
            g.blit(sheet,x,y,0,0,TAB_WIDTH,4);
            g.blit(sheet,x,y+4,0,52,4,TAB_HEIGHT-8);
            g.blit(sheet,x+4,y+4,100,54,TAB_WIDTH-4,TAB_HEIGHT-8);
            g.blit(sheet,x,y+TAB_HEIGHT-4,0,PAPER_HEIGHT-4,TAB_WIDTH,4);

            if (highlighted) {
                g.fill(x+2,y+2,x+TAB_WIDTH,y+TAB_HEIGHT-2,0x44ffffff);
                g.fill(x+2,y+5,x+3,y+TAB_HEIGHT-5,0xff80652f);
            }
            boolean open=HudConfig.inventoryDrawerOpen && !tucked;
            int ink=highlighted ? 0xff735327 : 0xff444a49;
            int cy=y+TAB_HEIGHT/2;
            for (int i=-3; i<=3; i++) {
                int px=x+(open ? 9-Math.abs(i) : 5+Math.abs(i));
                g.fill(px,cy+i,px+2,cy+i+1,ink);
            }
        }

        private boolean compact() { return paperWidth < 114; }
        private Font font() { return Minecraft.getInstance().font; }

        private static int rowY(int i) { return 19 + 16 * i + (i >= 4 ? 4 : 0); }
        private int compactX(int i) { return 7 + (i % 2) * ((paperWidth - 13) / 2); }
        private static int compactY(int i) { return 29 + (i / 2) * 25 + (i >= 4 ? 12 : 0); }
        private static String key(int i) { return i < 4 ? BinderKeys.keyName(i) : "P" + (i - 3); }
        private static BinderAbility ability(int i) {
            var state = BinderClientState.snapshot();
            return BinderCatalog.get(i < 4 ? state.active().get(i) : state.passive().get(i - 4));
        }
        private void text(GuiGraphics g, String value, int x, int y, int color) {
            g.drawString(font(), value, x, y, color, false);
        }
        private void fit(GuiGraphics g, String value, int x, int y, int width, int color) {
            float scale = Math.min(1f, width / (float)Math.max(1,font().width(value)));
            g.pose().pushPose(); g.pose().translate(x,y+(1-scale)*4,0); g.pose().scale(scale,scale,1);
            text(g,value,0,0,color); g.pose().popPose();
        }

        private void drawPaper(GuiGraphics g) {
            int w = paperWidth, h = PAPER_HEIGHT;
            // Keep the original 4px rails and tile a blank area below the crafting inputs.
            // Using the vanilla sheet at native coordinates honors custom inventory textures.
            var sheet = AbstractContainerScreen.INVENTORY_LOCATION;
            g.blit(sheet,0,0,0,0,4,h);
            g.blit(sheet,w-4,0,172,0,4,h);
            g.blit(sheet,4,0,4,0,w-8,4);
            g.blit(sheet,4,h-4,4,162,w-8,4);
            for (int y=4; y<h-4; y+=24) {
                for (int x=4; x<w-4; x+=72) {
                    g.blit(sheet,x,y,96,54,Math.min(72,w-4-x),Math.min(24,h-4-y));
                }
            }
            text(g,"Abilities",7,7,INK);
            var snapshot = BinderClientState.snapshot();
            if (!compact() && BinderProgressView.read() != null) {
                String balance = snapshot.motes() + " / " + snapshot.levels();
                int x = w - 7 - font().width(balance);
                g.pose().pushPose(); g.pose().translate(x-13,5,0); g.pose().scale(.7f,.7f,1);
                g.renderItem(mote,0,0); g.pose().popPose();
                text(g,balance,x,7,MUTED);
            } else if (compact()) {
                text(g,"Active",7,19,INK);
                text(g,"Passive",7,80,INK);
            }
            for (int i = 0; i < 6; i++) {
                boolean small = compact();
                int x = small ? compactX(i) : 7, y = small ? compactY(i) : rowY(i);
                var ability = ability(i);
                int size = small ? 18 : 16;
                g.blitSprite(SLOT,x,y,size,size);
                int tint = ability == null ? 0xff515151 : BinderGraphics.color(ability.element());
                if (ability != null) AbilityGlyph.draw(g,ability.id(),x+2,y+1,size-3,0xff474747);
                AbilityGlyph.draw(g,ability == null ? "" : ability.id(),x+1,y+1,size-3,tint);
                if (small) {
                    // Leave full key names and ability details in the native hover tooltip.
                    fit(g,key(i),x,y+18,Math.max(18,(w-14)/2),MUTED);
                } else {
                    String k = key(i);
                    int keySpace = Math.min(24,font().width(k));
                    fit(g,k,w-7-keySpace,y+4,keySpace,MUTED);
                    fit(g,ability == null ? "Empty" : ability.name(),27,y+4,w-37-keySpace,ability == null ? MUTED : INK);
                }
            }
            if (!compact()) {
                g.fill(7,84,w-7,85,0xff999999);
                g.fill(7,85,w-7,86,0xffe6e6e6);
            }
            int line = 120;
            g.fill(7,line,w-7,line+1,0xff979797); g.fill(7,line+1,w-7,line+2,0xffeeeeee);
            var stats = BinderProgressView.read();
            if (stats != null) {
                String level = "Level " + stats.level();
                text(g,level,7,125,INK);
                String xp = stats.needed() == 0 ? "MAX" : stats.xp() + "/" + stats.needed();
                if (!compact()) text(g,xp,w-7-font().width(xp),125,MUTED);
                int bar = w-14, fill = stats.needed() <= 0 ? bar : (int)Math.clamp((long)bar * stats.xp() / stats.needed(),0,bar);
                g.blitSprite(XP_BASE,7,136,bar,5);
                if (fill > 0) g.blitSprite(XP_FILL,bar,5,0,0,7,136,fill,5);
                if (!compact()) {
                    text(g,"+"+number(stats.healthBonus()/2)+" hearts   +"+number(stats.armorBonus())+" armor",7,145,MUTED);
                    text(g,"Veteran chance  "+stats.veteranChance()+"%",7,155,MUTED);
                } else fit(g,xp+" XP",7,147,w-14,MUTED);
            } else {
                fit(g,snapshot.motes()+" motes",7,128,w-14,INK);
                fit(g,snapshot.levels()+" XP levels",7,144,w-14,MUTED);
            }
        }

        private static String number(float v) {
            return v == (int)v ? Integer.toString((int)v) : String.format(Locale.ROOT,"%.1f",v);
        }

        private void tooltip(GuiGraphics g, int mx, int my) {
            if (!inventory.getMenu().getCarried().isEmpty()) return;
            if (isMouseOver(mx,my)) {
                GearHoverCard.drawDrawerHint(g,HudConfig.inventoryDrawerOpen && !tucked,mx,my);
                return;
            }
            if (!onPaper(mx,my)) return;
            int x = mx-paperLeft, y = my-paperTop;
            List<Component> lines = new ArrayList<>();
            for (int i=0; i<6; i++) {
                int lx = compact() ? compactX(i) : 7, ly = compact() ? compactY(i) : rowY(i);
                int width = compact() ? Math.max(17,(paperWidth-13)/2) : paperWidth-14;
                if (x<lx || x>=lx+width || y<ly || y>=ly+(compact()?25:16)) continue;
                var a = ability(i);
                if (a != null) {
                    GearHoverCard.drawAbility(g,a,mx,my);
                    return;
                }
                lines.add(Component.literal("Empty slot"));
                lines.add(Component.literal(i<4 ? "Active "+(i+1)+"  ·  "+key(i) : "Passive "+(i-3)).withStyle(ChatFormatting.GRAY));
                break;
            }
            var s=BinderClientState.snapshot();
            if (lines.isEmpty() && y>=122) {
                var stats=BinderProgressView.read();
                if (stats!=null) {
                    lines.add(Component.literal("Career level "+stats.level()));
                    lines.add(Component.literal(stats.xp()+" / "+stats.needed()+" career XP").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.literal("+"+number(stats.healthBonus()/2)+" hearts, +"+number(stats.armorBonus())+" armor").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.literal("Veteran chance "+stats.veteranChance()+"%").withStyle(ChatFormatting.GRAY));
                }
            }
            if ((lines.isEmpty() && y<16) || y>=122) {
                lines.add(Component.literal(s.motes()+" Motes of Light").withStyle(ChatFormatting.YELLOW));
                lines.add(Component.literal(s.levels()+" Minecraft XP levels").withStyle(ChatFormatting.GREEN));
            }
            if (!lines.isEmpty()) g.renderComponentTooltip(font(),lines,mx,my);
        }

        @Override public void updateWidgetNarration(NarrationElementOutput out) {
            super.updateWidgetNarration(out);
            if (HudConfig.inventoryDrawerOpen && !tucked) {
                List<String> slots = new ArrayList<>();
                for (int i=0;i<6;i++) {
                    var a = ability(i);
                    slots.add((i<4?"Active "+(i+1)+", "+key(i):"Passive "+(i-3))+", "+(a==null?"Empty":a.name()));
                }
                out.add(NarratedElementType.HINT, Component.literal(String.join(". ",slots)));
            }
        }
    }
}
