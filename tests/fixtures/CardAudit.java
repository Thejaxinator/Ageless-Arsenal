package ageless.cardqa;

import com.jax.destiny2mc.armor.ArsenalArmorItem;
import com.jax.destiny2mc.binder.BinderCatalog;
import com.jax.destiny2mc.binder.client.GearHoverCard;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.*;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid="arsenal_card_audit",value=Dist.CLIENT)
public final class CardAudit {
    private static boolean connected,started,done,inspect,wasCancelled;
    private static RenderTooltipEvent.Color lastColor;
    private static int calls;
    static void pass(String message) {System.out.println("CARD AUDIT "+message);}
    static void require(boolean value,String msg) {if(!value) throw new AssertionError(msg);}
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void peek(RenderTooltipEvent.Pre e) {
        if(inspect){wasCancelled=e.isCanceled();calls++;}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void colors(RenderTooltipEvent.Color e){if(inspect)lastColor=e;}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        var mc=Minecraft.getInstance();
        if(mc.screen instanceof AccessibilityOnboardingScreen s && mc.getOverlay()==null){s.onClose();return;}
        if(!connected && mc.screen instanceof TitleScreen && mc.getOverlay()==null){
            connected=true;String addr="127.0.0.1:"+Integer.getInteger("binder.port",26355);
            ConnectScreen.startConnecting(mc.screen,mc,ServerAddress.parseString(addr),new ServerData("Inspection audit",addr,ServerData.Type.OTHER),false,null);
        }
        if(mc.player!=null && mc.level!=null && !started){started=true;mc.setScreen(new AuditScreen());}
        if(done) mc.stop();
    }
    static class AuditScreen extends Screen {
        AuditScreen(){super(Component.literal("Inspection audit"));}
        static ItemStack stack(String id) {return BuiltInRegistries.ITEM.get(ResourceLocation.parse("destiny2mc:"+id)).getDefaultInstance();}
        void nativeTooltip(GuiGraphics g,ItemStack stack,boolean expectedCancel,int x,int y) {
            calls=0;lastColor=null;wasCancelled=false;inspect=true;
            g.renderTooltip(font,stack,x,y);inspect=false;
            require(calls==1&&wasCancelled==expectedCancel,stack+" actual state="+wasCancelled+" calls="+calls);
            if(expectedCancel)require(lastColor==null,"Suppressed tooltip drawn twice");
        }
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            if(done)return;
            try{
                minecraft.options.advancedItemTooltips=false;
                int nGuns=0,nArmor=0,nAbilities=0,edge=0;
                for (var item:BuiltInRegistries.ITEM) {
                    if(!(item instanceof DestinyGunItem || item instanceof ArsenalArmorItem))continue;
                    if(item instanceof DestinyGunItem)nGuns++;else nArmor++;
                    nativeTooltip(g,item.getDefaultInstance(),true,edge%2==0?3:width-3,edge%3==0?3:height-3);
                    edge++;
                }
                for(var form:BinderCatalog.all()){
                    GearHoverCard.drawAbility(g,form,nAbilities%2==0?3:width-3,nAbilities%3==0?3:height-3);
                    nAbilities++;
                }
                require(nGuns==70 && nArmor==15 && nAbilities==38,"coverage "+nGuns+"/"+nArmor+"/"+nAbilities);
                pass("All 70 weapon stacks, 15 armor stacks and 38 abilities drew at viewport edges.");
                var ace=stack("ace_of_spades");var gun=(DestinyGunItem)ace.getItem();
                ace.set(DataComponents.CUSTOM_NAME,Component.literal("Dealer's Last Hand"));
                WeaponPerkRegistry.setActivePerk(ace,gun.spec,5,602);
                GunDataHelper.setInt(ace,"d2_mag",3);
                Method details=GearHoverCard.class.getDeclaredMethod("gun",ItemStack.class,DestinyGunItem.class);
                details.setAccessible(true);
                String description=details.invoke(null,ace,gun).toString();
                require(description.contains("Dealer's Last Hand") && description.contains("Memento Mori") &&
                    description.contains("value=3 / "+WeaponPerkRegistry.getEffectiveMagSize(ace,gun.spec)) && description.contains("Unlimited"),description);
                pass("Renamed Ace reports its actual partial magazine, Backup Mag capacity, primary reserve and intrinsic.");
                ItemStack helm=stack("helm_of_saint_14");var armor=(ArsenalArmorItem)helm.getItem();helm.setDamageValue(27);
                Method exotic=GearHoverCard.class.getDeclaredMethod("armor",ItemStack.class,ArsenalArmorItem.class);exotic.setAccessible(true);
                String stats=exotic.invoke(null,helm,armor).toString();
                require(stats.contains("Starless Night") && stats.contains("value="+(helm.getMaxDamage()-27)+" / "+helm.getMaxDamage()) && stats.contains("metric="+armor.piece.defense),stats);
                pass("Exotic armor reports the real perk, slot protection and damaged stack durability.");
                nativeTooltip(g,new ItemStack(Items.BREAD),false,16,16);
                int normal=lastColor.getBorderStart();
                minecraft.options.advancedItemTooltips=true;
                nativeTooltip(g,ace,false,16,height-10);
                require(lastColor!=null && lastColor.getBorderStart()==0xffcba441,"Exotic detailed border");
                int exoticColor=lastColor.getBorderStart();
                var detailed=Screen.getTooltipFromItem(minecraft,ace).toString();
                require(detailed.contains("Backup mag") && detailed.contains("Dealer's Last Hand") && detailed.contains("ace_of_spades"),detailed);
                calls=0;wasCancelled=false;lastColor=null;inspect=true;GearHoverCard.drawItem(g,ace,16,16);inspect=false;
                require(calls==1&&!wasCancelled&&lastColor!=null&&lastColor.getBorderStart()==exoticColor,"Gunsmith native advanced path");
                nativeTooltip(g,new ItemStack(Items.BREAD),false,16,16);
                require(lastColor.getBorderStart()==normal,"Vanilla frame state leaked");
                minecraft.options.advancedItemTooltips=false;
                ace.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP,Unit.INSTANCE);
                nativeTooltip(g,ace,false,16,16);
                pass("Item hide-additional-tooltip flag delegates to the original Minecraft components.");
                pass("F3+H preserves native roll, custom name and registry ID in inventory and Gunsmith. Bread keeps its original border after exotic gear.");
                done=true;pass("COMPLETE");
            }catch(Throwable failure){throw new IllegalStateException(failure);}
        }
    }
}
