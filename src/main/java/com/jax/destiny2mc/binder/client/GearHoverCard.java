package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.ageless.WeaponProgram;
import com.jax.destiny2mc.ageless.WeaponPrograms;
import com.jax.destiny2mc.armor.ArsenalArmorItem;
import com.jax.destiny2mc.binder.BinderAbility;
import com.jax.destiny2mc.binder.BinderClientState;
import com.jax.destiny2mc.binder.BinderSnapshot;
import com.jax.destiny2mc.binder.WarframeBinderCatalog;
import com.jax.destiny2mc.client.DestinyHudRenderer;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Shared inspection surface for native inventory slots, the Gunsmith, the Binder and its drawer.
 * Read state from the displayed stack, never from the player's selected hotbar slot. */
@EventBusSubscriber(modid="destiny2mc", value=Dist.CLIENT)
public final class GearHoverCard {
    static final int EXOTIC = 0xffcba441, LEGENDARY = 0xff806298, PARACAUSAL = 0xff559c98;
    static final int WHITE = 0xfff2f0e8, SUBTLE = 0xffc1c4c6;
    private static final DecimalFormat NUMBER = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private GearHoverCard() {}

    record Stat(String name, String value, double fraction) {
        Stat(String name, String value) { this(name,value,-1); }
    }
    record Card(String title, String kind, String grade, int accent, ItemStack icon, String glyph,
                String metric, String unit, String section, String passage, List<Stat> stats, String footer) {}

    private static boolean gear(ItemStack stack) {
        return stack.getItem() instanceof DestinyGunItem || stack.getItem() instanceof ArsenalArmorItem;
    }

    // Shift returns the gathered Minecraft components, including other mods' additions.
    private static boolean detailed() {
        return Screen.hasShiftDown() || Minecraft.getInstance().options.advancedItemTooltips;
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void inventoryTooltip(RenderTooltipEvent.Pre event) {
        ItemStack stack=event.getItemStack();
        if (!gear(stack) || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) || detailed()) return;
        event.setCanceled(true);
        paintItem(event.getGraphics(),stack,event.getX(),event.getY());
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void detailedColors(RenderTooltipEvent.Color event) {
        ItemStack stack=event.getItemStack();
        if (!gear(stack)) return;
        int color = stack.getItem() instanceof DestinyGunItem gun ? rarity(gun.spec.isExotic) :
            rarity(((ArsenalArmorItem)stack.getItem()).piece.exotic);
        event.setBackgroundStart(0xfa242731);
        event.setBackgroundEnd(0xf618191e);
        event.setBorderStart(color);
        event.setBorderEnd(0xff666a71);
    }

    /** For blueprint and actual item thumbnails outside vanilla containers. */
    public static void drawItem(GuiGraphics g, ItemStack stack, int mx, int my) {
        if (stack.isEmpty() || stack.has(DataComponents.HIDE_TOOLTIP)) return;
        if (!gear(stack) || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) || detailed()) {
            g.renderTooltip(Minecraft.getInstance().font,stack,mx,my);
        } else paintItem(g,stack,mx,my);
    }

    private static void paintItem(GuiGraphics g, ItemStack stack, int mx, int my) {
        if (stack.has(DataComponents.HIDE_TOOLTIP)) return;
        Card card = stack.getItem() instanceof DestinyGunItem gun ? gun(stack,gun) :
            armor(stack,(ArsenalArmorItem)stack.getItem());
        InspectionPainter.draw(g,mx,my,card);
    }

    private static int rarity(boolean exotic) { return exotic ? EXOTIC : LEGENDARY; }
    private static String grade(boolean exotic) { return exotic ? "Exotic" : "Legendary"; }
    private static String num(double value) { return NUMBER.format(value); }
    static String title(String id) {
        if (id.isEmpty()) return "";
        String text=id.replace('_',' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0))+text.substring(1);
    }
    private static String weaponElement(int element) {
        return switch (element) {
            case 1 -> "Solar"; case 2 -> "Arc"; case 3 -> "Void";
            case 4 -> "Stasis"; case 5 -> "Toxin"; default -> "Kinetic";
        };
    }
    private static long now() {
        var level=Minecraft.getInstance().level;
        return level==null ? 0 : level.getGameTime();
    }

    private static Card gun(ItemStack stack, DestinyGunItem item) {
        var w=item.spec;
        var program=WeaponPrograms.client(w);
        int loaded=GunDataHelper.getMag(stack,w), capacity=WeaponPerkRegistry.getEffectiveMagSize(stack,w);
        boolean capacitor=program!=null && program.feed()==WeaponProgram.Feed.CAPACITOR;
        int count=program==null ? w.pellets : program.count();
        float damage=w.baseDamage*(program==null ? 1f : program.damageScale());
        float range=WeaponPerkRegistry.getEffectiveRange(stack,w,now());
        var stats=new ArrayList<Stat>();
        stats.add(new Stat(capacitor ? "Charge" : "Magazine",loaded+" / "+capacity,loaded/(double)Math.max(1,capacity)));
        if (!capacitor) stats.add(new Stat("Reserves",w.usesSpecialAmmo ? Integer.toString(GunDataHelper.getReserve(stack,w)) : "Unlimited"));
        else stats.add(new Stat("Recovery","1 unit / "+num(program.rechargeTicks()/20.0)+" s"));
        stats.add(new Stat("Range",num(range)+" blocks"));
        if (!capacitor) stats.add(new Stat("Reload",num(WeaponPerkRegistry.getEffectiveReloadTicks(stack,w,now())/20.0)+" s"));
        stats.add(new Stat("Traits",WeaponPerkRegistry.getPerkName(WeaponPerkRegistry.getActivePerk(stack,w,2))
            +"  /  "+WeaponPerkRegistry.getPerkName(WeaponPerkRegistry.getActivePerk(stack,w,3))));
        String frame=w.frameLabel.startsWith("Exotic ") ? w.frameLabel.substring(7) : w.frameLabel;
        String unit=(count>1 ? "HP × "+count : "base HP")+"  •  "+weaponElement(DestinyHudRenderer.getEffectiveElement(stack,w));
        return new Card(stack.getHoverName().getString(),frame,grade(w.isExotic),rarity(w.isExotic),stack,"",
            num(damage),unit,w.perkTitle,w.perkDesc,stats,"Shift   Item details");
    }

    private static Card armor(ItemStack stack, ArsenalArmorItem armor) {
        var p=armor.piece;
        var stats=new ArrayList<Stat>();
        stats.add(new Stat("Durability",(stack.getMaxDamage()-stack.getDamageValue())+" / "+stack.getMaxDamage(),
            1-stack.getDamageValue()/(double)Math.max(1,stack.getMaxDamage())));
        if (p.toughness>0) stats.add(new Stat("Toughness",num(p.toughness)));
        if (p.knockback>0) stats.add(new Stat("Knockback resist",num(100*p.knockback)+"%"));
        if (stack.isEnchanted()) stats.add(new Stat("Enchantments",Integer.toString(stack.getEnchantments().size())));
        if (!p.source.isBlank()) stats.add(new Stat("Origin",p.source));
        String slot=switch (armor.getType()) {
            case HELMET -> "Helmet"; case CHESTPLATE -> "Chest armor"; case LEGGINGS -> "Leg armor"; case BOOTS -> "Boots"; default -> "Armor";
        };
        String family=(!p.exotic && !p.setName.isBlank()) ? p.setName+" • "+slot : slot;
        return new Card(stack.getHoverName().getString(),family,grade(p.exotic),rarity(p.exotic),stack,"",
            Integer.toString(p.defense),"armor",p.trait,p.description,stats,"Shift   Item details");
    }

    public static void drawAbility(GuiGraphics g, BinderAbility ability, int mx, int my) {
        if (ability==null) return;
        BinderSnapshot state=BinderClientState.snapshot();
        boolean owned=state.unlocked().contains(ability.id());
        String kind=switch (ability.category()) {
            case "class" -> "Class ability"; case "movement","jump" -> "Movement passive";
            case "passive" -> "Subclass passive";
            case "warframe_active" -> "Void-Bound ability";
            case "warframe_passive" -> "Void-Bound passive";
            case "warframe_movement" -> "Void-Bound parkour";
            default -> title(ability.category());
        };
        String element=title(ability.element());
        String grade=ability.isWarframe() ? "Void-Bound" : "Paracausal";
        int accent=ability.isWarframe() ? WarframeBinderCatalog.accentColor(ability.id()) : BinderGraphics.color(ability.element());
        String metric="", unit="";
        var stats=new ArrayList<Stat>();
        if (!ability.isPassive()) {
            metric=num(ability.cooldownTicks()/20.0)+"s";
            unit="cooldown";
            long remaining=state.readyAt().getOrDefault(ability.id(),0L)-now();
            if (remaining>0) stats.add(new Stat("Recharging",((remaining+19)/20)+" s"));
        }
        if (!owned) {
            stats.add(new Stat("Unlock",ability.motes()+" motes  •  "+ability.levels()+" XP levels"));
        }
        List<String> bindings=new ArrayList<>();
        for(int i=0;i<state.active().size();i++) if (state.active().get(i).equals(ability.id())) bindings.add(BinderKeys.keyName(i));
        for(int i=0;i<state.passive().size();i++) if (state.passive().get(i).equals(ability.id())) bindings.add("Passive "+(i+1));
        String footer=!bindings.isEmpty() ? "Equipped    "+String.join(" / ",bindings) : owned ? "Unlocked" : "Locked";
        InspectionPainter.draw(g,mx,my,new Card(ability.name(),element+"  •  "+kind,grade,accent,ItemStack.EMPTY,ability.id(),
            metric,unit,"",ability.description(),stats,footer));
    }

    public static void drawDrawerHint(GuiGraphics g, boolean open, int mx, int my) {
        var state=BinderClientState.snapshot();
        long active=state.active().stream().filter(s->!s.isEmpty()).count();
        long passive=state.passive().stream().filter(s->!s.isEmpty()).count();
        InspectionPainter.hint(g,mx,my,open ? "Hide abilities" : "Show abilities",active+"/4 active    "+passive+"/2 passive");
    }
}
