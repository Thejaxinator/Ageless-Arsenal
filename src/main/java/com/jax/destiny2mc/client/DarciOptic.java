package com.jax.destiny2mc.client;

import com.jax.destiny2mc.item.DestinyGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Personal Assistant's sight display. Tracking is visual and uses the weapon's own range. */
@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class DarciOptic {
    private static final int ACQUIRE_TICKS = 10;
    private static LivingEntity target;
    private static int trackingTicks;
    private static int distance;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null || !mc.options.getCameraType().isFirstPerson()
                || !mc.options.keyUse.isDown() || ClientWeaponState.ads < .8f
                || !(player.getMainHandItem().getItem() instanceof DestinyGunItem gun) || !gun.spec.id.equals("darci")) {
            target = null;
            trackingTicks = 0;
            return;
        }
        var eye = player.getEyePosition();
        var delta = player.getLookAngle().scale(gun.spec.range);
        var end = eye.add(delta);
        var wall = player.level().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if (wall.getType() != HitResult.Type.MISS) end = wall.getLocation();
        var bounds = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1);
        var hit = ProjectileUtil.getEntityHitResult(player,eye,end,bounds,
                entity -> entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator() && living.isPickable(),
                eye.distanceToSqr(end));
        LivingEntity next = hit == null ? null : (LivingEntity) hit.getEntity();
        if (next != target) trackingTicks = 0;
        target = next;
        if (target != null) {
            trackingTicks = Math.min(ACQUIRE_TICKS, trackingTicks+1);
            distance = (int)Math.round(eye.distanceTo(hit.getLocation()));
        }
    }
    static void draw(GuiGraphics gui, int cx, int cy) {
        if (target == null || !target.isAlive()) return;
        var font = Minecraft.getInstance().font;
        // A small instrument panel fits along the lower lip of the physical optic at any GUI scale.
        float scale = Mth.clamp(gui.guiHeight()/455f,.55f,1.5f);
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(cx,cy+gui.guiHeight()*.105f,0);
        pose.scale(scale,scale,1);
        int color = 0xff91e5d2;
        gui.fill(-32,0,32,13,0xe519262c);
        gui.fill(-32,0,-32+64*trackingTicks/ACQUIRE_TICKS,1,color);
        if (trackingTicks == ACQUIRE_TICKS) {
            String range = distance+"m";
            String health = Mth.ceil(100*target.getHealth()/Math.max(1,target.getMaxHealth()))+"%";
            gui.drawString(font,range,-28,3,color,false);
            gui.drawString(font,health,28-font.width(health),3,0xffffd7b0,false);
        } else {
            gui.drawCenteredString(font,"SCAN",0,3,color);
        }
        pose.popPose();
    }
}
