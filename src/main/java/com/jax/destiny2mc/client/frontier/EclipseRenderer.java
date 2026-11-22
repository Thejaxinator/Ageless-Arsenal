package com.jax.destiny2mc.client.frontier;

import com.jax.destiny2mc.frontier.FrontierCatalog;
import com.jax.destiny2mc.frontier.FrontierContent;
import com.jax.destiny2mc.frontier.FrontierMob;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class EclipseRenderer extends MobRenderer<FrontierMob,EclipseModel> {
    private final ResourceLocation texture;
    private final int kind;

    public static boolean supports(int kind) {
        return switch(kind) {
            case 102,104,105,106,107,108,109,110,111,112,128,130,139 -> true;
            default -> false;
        };
    }

    public EclipseRenderer(EntityRendererProvider.Context context,FrontierCatalog.Unit unit) {
        super(context,new EclipseModel(FrontierLayers.create(unit.kind()).bakeRoot(),unit.kind()),unit.width()*.5F);
        this.kind=unit.kind();
        texture=FrontierContent.id("textures/entity/"+unit.id()+".png");
        ResourceLocation mask=FrontierContent.id("textures/entity/"+unit.id()+"_glow.png");
        if (unit.faction().equals("taken")) {
            // Only the seams, optic and vapor have nonzero alpha in the mask.
            // Slow, low-amplitude phase drift keeps the detached wisps visible in dark areas.
            RenderType mist=RenderType.entityTranslucentEmissive(mask);
            RenderType pupil=RenderType.eyes(mask);
            addLayer(new RenderLayer<FrontierMob,EclipseModel>(this) {
                @Override public void render(PoseStack stack,MultiBufferSource buffers,int packedLight,FrontierMob entity,
                        float swing,float amount,float partial,float age,float yaw,float pitch) {
                    if(entity.isInvisible())return;
                    float phase=entity.getId()*.77F + age*.073F;
                    int alpha=201+Math.round(17*(float)Math.sin(phase));
                    int red=225+Math.round(11*(float)Math.sin(phase+.8F));
                    int rgba=(alpha<<24)|(red<<16)|(245<<8)|255;
                    getParentModel().renderToBuffer(stack,buffers.getBuffer(mist),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,rgba);
                    getParentModel().renderPupil(stack,buffers.getBuffer(pupil));
                }
            });
        } else {
            RenderType light=RenderType.eyes(mask);
            addLayer(new EyesLayer<FrontierMob,EclipseModel>(this) {
                @Override public RenderType renderType(){return light;}
            });
        }
    }

    @Override public ResourceLocation getTextureLocation(FrontierMob entity){return texture;}

    @Override protected void scale(FrontierMob entity,PoseStack stack,float partial){
        if(kind==107 && entity.move()==FrontierMob.BLINK){
            float s=blinkSize(entity.moveAge()+partial);
            stack.scale(s,s,s);
        }
    }
    static float blinkSize(float t){
        return t<12?Math.max(.012F,1-t/12):Math.clamp((t-12)/9,.012F,1F);
    }
}
