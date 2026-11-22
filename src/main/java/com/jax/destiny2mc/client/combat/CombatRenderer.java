package com.jax.destiny2mc.client.combat;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.combat.CombatMoves;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public final class CombatRenderer extends MobRenderer<FallenMobEntity,CombatModel> {
    private final ResourceLocation texture;
    public CombatRenderer(EntityRendererProvider.Context context,ModelPart root,int kind,String name) {
        super(context,new CombatModel(root,kind),.5f);
        texture=ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/entity/"+name+".png");
        ResourceLocation glow=ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/entity/"+name+"_glow.png");
        addLayer(new EyesLayer<FallenMobEntity,CombatModel>(this) {
            private final RenderType pass=RenderType.eyes(glow);
            @Override public RenderType renderType(){return pass;}
        });
        if (kind == 36) addLayer(new HydraShieldLayer(this, root));
    }
    @Override public ResourceLocation getTextureLocation(FallenMobEntity entity){return texture;}
    @Override protected void scale(FallenMobEntity entity,PoseStack stack,float partial) {
        if(entity.getAnimState()==CombatMoves.BLINK) {
            float factor=CombatMoves.blinkScale(CombatMoves.duration(CombatMoves.BLINK)-entity.getAnimTimer()+partial);
            stack.scale(factor,Math.max(.02f,factor),factor);
        }
    }
}
