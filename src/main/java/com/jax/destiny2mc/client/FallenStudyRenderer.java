package com.jax.destiny2mc.client;

import com.jax.destiny2mc.combat.CombatMoves;
import com.jax.destiny2mc.frontier.FrontierMob;
import net.minecraft.world.entity.Mob;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public final class FallenStudyRenderer extends MobRenderer<Mob,FallenStudyModel> {
    private final ResourceLocation texture;
    private final float rankScale;

    public FallenStudyRenderer(EntityRendererProvider.Context context, int kind) {
        super(context,new FallenStudyModel(kind<7 ? context.bakeLayer(FallenHumanoidModel.LAYERS[kind])
                : FallenLineRig.geometry(kind).bakeRoot(),kind),FallenLineRig.spec(kind).shadow());
        String unit=FallenLineRig.spec(kind).id();
        rankScale=FallenLineRig.spec(kind).rankScale();
        texture = ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/entity/"+unit+".png");
        RenderType light = RenderType.eyes(ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/entity/"+unit+"_glow.png"));
        addLayer(new EyesLayer<Mob,FallenStudyModel>(this) {
            @Override public RenderType renderType() { return light; }
        });
    }

    @Override public ResourceLocation getTextureLocation(Mob entity) { return texture; }

    @Override protected void scale(Mob entity, PoseStack stack, float partialTick) {
        float size=rankScale;
        if (entity instanceof FallenMobEntity fallen && fallen.getAnimState()==CombatMoves.BLINK) {
            float elapsed = CombatMoves.duration(CombatMoves.BLINK)-fallen.getAnimTimer()+partialTick;
            size *= CombatMoves.blinkScale(elapsed);
        }
        if (entity instanceof FrontierMob frontier && frontier.move()==FrontierMob.BLINK) {
            float t=frontier.moveAge()+partialTick;
            size*=t<12?Math.max(.03F,1-t/12):Math.min(1,(t-12)/10);
        }
        stack.scale(size,size,size);
    }
}
