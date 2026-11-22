package com.jax.destiny2mc.client.atlas;
import com.jax.destiny2mc.atlas.DungeonNode;
import com.jax.destiny2mc.frontier.FrontierContent;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
public final class DungeonNodeRenderer extends MobRenderer<DungeonNode,DungeonNodeRenderer.Model> {
 private static final ResourceLocation TEXTURE=FrontierContent.id("textures/entity/dungeon_node.png");
 public DungeonNodeRenderer(EntityRendererProvider.Context c){super(c,new Model(),0);addLayer(new EyesLayer<DungeonNode,Model>(this){public RenderType renderType(){return RenderType.eyes(TEXTURE);}});}
 @Override public ResourceLocation getTextureLocation(DungeonNode e){return TEXTURE;}
 public static final class Model extends HierarchicalModel<DungeonNode>{
  private final ModelPart root;private int tint;
  Model(){MeshDefinition mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("core",CubeListBuilder.create().texOffs(0,0).addBox(-3,-3,-3,6,6,6).texOffs(0,16).addBox(-5,-1,-1,10,2,2).addBox(-1,-5,-1,2,10,2),PartPose.offset(0,18,0));root=LayerDefinition.create(mesh,32,32).bakeRoot();}
  public ModelPart root(){return root;}
  public void setupAnim(DungeonNode e,float a,float b,float age,float yaw,float pitch){ModelPart p=root.getChild("core");p.yRot=age*.045f;p.zRot=age*.025f;p.y=18+(float)Math.sin(age*.12)*1.1f;tint=switch(e.mode()){case 1->0xffffba6b;case 2->0xff7ee6e7;case 3->0xffffd16b;case 4->0xffff785e;default->0xffc6b0ee;};}
  @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,tint);}
 }
}
