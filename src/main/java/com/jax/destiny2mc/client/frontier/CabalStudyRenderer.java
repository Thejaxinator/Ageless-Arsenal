package com.jax.destiny2mc.client.frontier;

import com.jax.destiny2mc.frontier.FrontierCatalog;
import com.jax.destiny2mc.frontier.FrontierContent;
import com.jax.destiny2mc.frontier.FrontierMob;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public final class CabalStudyRenderer extends MobRenderer<FrontierMob, CabalStudyModel> {
    private final ResourceLocation texture;

    static boolean supports(int kind) {
        return kind == 100 || kind == 101 || kind == 103;
    }

    public CabalStudyRenderer(EntityRendererProvider.Context context, FrontierCatalog.Unit unit) {
        super(context, new CabalStudyModel(FrontierLayers.create(unit.kind()).bakeRoot(), unit.kind()), unit.width() * .5F);
        texture = FrontierContent.id("textures/entity/" + unit.id() + ".png");
        RenderType light = RenderType.eyes(FrontierContent.id("textures/entity/" + unit.id() + "_glow.png"));
        addLayer(new EyesLayer<FrontierMob, CabalStudyModel>(this) {
            @Override public RenderType renderType() { return light; }
        });
    }

    @Override public ResourceLocation getTextureLocation(FrontierMob mob) {
        return texture;
    }
}
