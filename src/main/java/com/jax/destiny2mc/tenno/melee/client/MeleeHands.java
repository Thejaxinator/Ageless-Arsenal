package com.jax.destiny2mc.tenno.melee.client;

import com.jax.destiny2mc.tenno.melee.MeleeProfile;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Mount the second blade as a visual attachment. It never consumes the actual offhand slot. */
@EventBusSubscriber(modid="destiny2mc", value=Dist.CLIENT)
public final class MeleeHands {
    private static boolean secondStroke;
    private MeleeHands() {}
    private static ModelResourceLocation secondary(String id) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("destiny2mc","item/tenno_blades/"+id+"_off"));
    }
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event) {
        for (var p : MeleeProfile.ALL) if (p.paired()) event.register(secondary(p.id()));
    }
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent event) {
        Item[] items = MeleeProfile.ALL.stream()
                .map(p -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",p.id())))
                .toArray(Item[]::new);
        event.registerItem(new IClientItemExtensions() {
            @Override public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                    ItemStack stack, float partial, float equip, float swing) {
                var p = ((TennoMeleeItem)stack.getItem()).profile();
                float guard = guard(player, stack, partial);
                if (guard == 0 && swing <= 0) return false;
                // Use the same resting origin as the native first-person arm to avoid an attack pop.
                float strike = (p.paired() && secondStroke && player.getOffhandItem().isEmpty()) ? swing * .15f : swing;
                transform(pose,arm,equip,strike,guard,player.isShiftKeyDown(),p.family().equals("Scythe"));
                return true;
            }
        }, items);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer player = event.getSkin(skin);
            if (player != null) player.addLayer(new SecondBlade(player));
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof LocalPlayer p && p.swinging && p.swingTime == 1
                && p.getMainHandItem().getItem() instanceof TennoMeleeItem weapon && weapon.profile().paired())
            secondStroke = !secondStroke;
    }
    private static float guard(AbstractClientPlayer p,ItemStack stack,float partial) {
        return p.isUsingItem() && p.getUseItem() == stack ? Math.min(1f,(p.getTicksUsingItem()+partial)/5f) : 0;
    }
    private static void transform(PoseStack pose,HumanoidArm arm,float equip,float swing,float guard,boolean heavy,boolean scythe) {
        float side = arm == HumanoidArm.RIGHT ? 1 : -1;
        float cut = Mth.sin((float)Math.PI*swing);
        pose.translate(side*(.56-.31*guard-.32*cut), -.52-.6*equip+.20*guard+.19*cut, -.72+.05*guard-.16*cut);
        pose.mulPose(Axis.YP.rotationDegrees(side*(15*guard-39*cut)));
        pose.mulPose(Axis.ZP.rotationDegrees(side*(64*guard+(heavy ? 67 : scythe ? 52 : 37)*cut)));
        pose.mulPose(Axis.XP.rotationDegrees(-16*guard-(heavy ? 64 : 37)*cut));
    }
    @SubscribeEvent public static void offHand(RenderHandEvent event) {
        var p = Minecraft.getInstance().player;
        if (p == null || event.getHand() != InteractionHand.OFF_HAND || !p.getOffhandItem().isEmpty()
                || !event.getItemStack().isEmpty()) return;
        var stack = p.getMainHandItem();
        if (!(stack.getItem() instanceof TennoMeleeItem weapon) || !weapon.profile().paired()) return;
        var arm = p.getMainArm().getOpposite();
        var pose = event.getPoseStack();
        pose.pushPose();
        float swing = p.getAttackAnim(event.getPartialTick()) * (secondStroke ? 1 : .15f);
        transform(pose,arm,event.getEquipProgress(),swing,guard(p,stack,event.getPartialTick()),p.isShiftKeyDown(),false);
        renderSecondary(stack,weapon.profile(),arm,true,pose,event.getMultiBufferSource(),event.getPackedLight());
        pose.popPose();
        event.setCanceled(true);
    }
    private static void renderSecondary(ItemStack stack,MeleeProfile p,HumanoidArm arm,boolean first,PoseStack pose,MultiBufferSource buffers,int light) {
        boolean left = arm == HumanoidArm.LEFT;
        var context = first ? (left ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                            : (left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        var game = Minecraft.getInstance();
        game.getItemRenderer().render(stack,context,left,pose,buffers,light,OverlayTexture.NO_OVERLAY,game.getModelManager().getModel(secondary(p.id())));
    }
    private static final class SecondBlade extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        SecondBlade(PlayerRenderer parent) { super(parent); }
        @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer p,
                float limb,float amount,float partial,float age,float yaw,float pitch) {
            if (!p.getOffhandItem().isEmpty() || p.isInvisible() || p.isSpectator()) return;
            var stack = p.getMainHandItem();
            if (!(stack.getItem() instanceof TennoMeleeItem weapon) || !weapon.profile().paired()) return;
            var arm = p.getMainArm().getOpposite();
            boolean left = arm == HumanoidArm.LEFT;
            pose.pushPose();
            getParentModel().translateToHand(arm,pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.mulPose(Axis.YP.rotationDegrees(180));
            pose.translate((left ? -1.0 : 1.0)/16,.125,-.625);
            renderSecondary(stack,weapon.profile(),arm,false,pose,buffers,light);
            pose.popPose();
        }
    }
}
