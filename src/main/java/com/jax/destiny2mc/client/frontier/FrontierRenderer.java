package com.jax.destiny2mc.client.frontier;
import com.jax.destiny2mc.frontier.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
public class FrontierRenderer extends MobRenderer<FrontierMob,FrontierModel> {
 private final ResourceLocation texture;
 public FrontierRenderer(EntityRendererProvider.Context c,FrontierCatalog.Unit u){
  super(c,new FrontierModel(FrontierLayers.create(u.kind()).bakeRoot(),u),u.width()*.5f);texture=FrontierContent.id("textures/entity/"+u.id()+".png");
  ResourceLocation glow=FrontierContent.id("textures/entity/"+u.id()+"_glow.png");addLayer(new EyesLayer<FrontierMob,FrontierModel>(this){public RenderType renderType(){return RenderType.eyes(glow);}});
 }
 @Override public ResourceLocation getTextureLocation(FrontierMob e){return texture;}
 @Override protected void scale(FrontierMob e,PoseStack p,float partial){if(e.move()==FrontierMob.BLINK){float t=e.moveAge()+partial;float a=t<12?Math.max(.03f,1-t/12):Math.min(1,(t-12)/10);p.scale(a,a,a);}}
}
