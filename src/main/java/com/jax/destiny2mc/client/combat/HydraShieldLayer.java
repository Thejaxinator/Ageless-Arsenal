package com.jax.destiny2mc.client.combat;

import com.jax.destiny2mc.entity.FallenMobEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

final class HydraShieldLayer extends RenderLayer<FallenMobEntity, CombatModel> {
    private static final RenderType SHIELD = RenderType.entityTranslucentEmissive(
        ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/entity/vex_hydra_shield.png")
    );
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart ring;

    HydraShieldLayer(CombatRenderer renderer, ModelPart root) {
        super(renderer);
        this.root = root;
        this.body = root.getChild("body");
        this.ring = body.getChild("shield_ring");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, FallenMobEntity entity,
                       float swing, float amount, float partial, float age, float yaw, float pitch) {
        if (entity.isInvisible()) return;
        pose.pushPose();
        root.translateAndRotate(pose);
        body.translateAndRotate(pose);
        ring.render(pose, buffers.getBuffer(SHIELD), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
