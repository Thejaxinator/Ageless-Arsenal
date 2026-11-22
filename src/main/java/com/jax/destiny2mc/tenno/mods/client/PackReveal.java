package com.jax.destiny2mc.tenno.mods.client;

import com.jax.destiny2mc.tenno.mods.WeaponMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** A pack's server-confirmed unlock appears once, without opening a screen or capturing input. */
@EventBusSubscriber(modid="destiny2mc", value=Dist.CLIENT)
public final class PackReveal {
    private static final int DURATION = 76;
    private static WeaponMod shown;
    private static LocalPlayer owner;
    private static int started;

    private PackReveal() {}

    public static void reveal(String id) {
        Minecraft mc = Minecraft.getInstance();
        WeaponMod mod = WeaponMod.byId(id);
        if (mc.player == null || mod == null) return;
        owner = mc.player;
        shown = mod;
        started = owner.tickCount;
        mc.getNarrator().sayNow(Component.literal("Mod unlocked. " + mod.title() + ". " + mod.effect(0)));
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        shown = null;
        owner = null;
    }

    @SubscribeEvent public static void render(RenderGuiLayerEvent.Post event) {
        if (shown == null || !VanillaGuiLayers.HOTBAR.equals(event.getName())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || owner != mc.player || owner.tickCount-started >= DURATION) {
            shown = null;
            owner = null;
            return;
        }
        if (mc.options.hideGui || mc.screen != null) return;
        GuiGraphics g = event.getGuiGraphics();
        int width = Math.min(192,g.guiWidth()-12);
        if (width < 100) return;
        int textWidth = width-52;
        var titleLines=mc.font.split(Component.literal(shown.title()),textWidth);
        var effectLines=mc.font.split(Component.literal(shown.effect(0)),textWidth);
        int titleY=19;
        int effectY=titleY+11*titleLines.size()+3;
        int height=Math.max(64,effectY+10*effectLines.size()+16);
        float age=owner.tickCount-started+event.getPartialTick().getGameTimeDeltaPartialTick(false);
        // Each open replaces the last reveal; the collection stores every acquired card.
        float phase=Mth.clamp(Math.min(age/5f,(DURATION-age)/7f),0,1);
        float slide=1-(phase*phase*(3-2*phase));
        int x=g.guiWidth()-width-6+Math.round(slide*(width+8));
        int y=Math.max(3,Math.min(g.guiHeight()-height-3,g.guiHeight()/4));
        var pose=g.pose();
        pose.pushPose();
        try {
            pose.translate(x,y,0);
            int gold=0xffdfc595,muted=0xffb8c7c2;
            g.fill(2,2,width+2,height+2,0x76101415);
            g.fillGradient(0,0,width,height,0xf42a3838,0xf1121e24);
            g.fill(0,0,width,1,gold);
            g.fill(0,height-1,width,height,0xff687068);
            g.fill(0,1,2,height-1,0xff8c8064);
            g.fill(5,6,38,height-6,0xff1a2629);
            g.blit(shown.icon(),6,(height-32)/2,0,0,32,32,32,32);
            g.drawString(mc.font,"MOD UNLOCKED",44,6,gold,false);
            int lineY=titleY;
            for(var line:titleLines){g.drawString(mc.font,line,44,lineY,0xfff8f2e2,false);lineY+=11;}
            lineY=effectY;
            for(var line:effectLines){g.drawString(mc.font,line,44,lineY,muted,false);lineY+=10;}
            String footer=shown.category()+"  ·  Rank 0";
            g.drawString(mc.font,footer,44,height-12,0xff9caeaa,false);
        } finally { pose.popPose(); }
    }
}
