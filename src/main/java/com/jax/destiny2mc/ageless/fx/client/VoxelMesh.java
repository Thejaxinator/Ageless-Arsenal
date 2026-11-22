package com.jax.destiny2mc.ageless.fx.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Every tube has a real four-sided cross-section, including when looking straight along it. */
public final class VoxelMesh {
    // A single batched, depth-tested, color-only pass; soft shells cannot occlude opaque terrain.
    private static final class Types extends RenderType {
        private Types(){super("ageless_fx",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,4096,false,false,()->{},()->{});}
        static final RenderType FX=create("ageless_voxels",DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,65536,false,true,CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    }
    public static final RenderType TYPE=Types.FX;
    private final VertexConsumer buffer;
    private final Matrix4f pose;
    private final int budget;
    private int pieces;
    public VoxelMesh(VertexConsumer buffer,Matrix4f pose,int budget){this.buffer=buffer;this.pose=pose;this.budget=budget;}
    public int pieces(){return pieces;}
    public boolean full(){return pieces>=budget;}

    public static Vec3 side(Vec3 axis){
        // Pick an independent basis even for a vertical beam.
        return (Math.abs(axis.y)>.93?new Vec3(1,0,0):new Vec3(0,1,0)).cross(axis).normalize();
    }
    public void cube(Vec3 c,double r,int rgb,int alpha){
        prism(c.add(0,-r,0),c.add(0,r,0),new Vec3(r,0,0),new Vec3(0,0,r),rgb,alpha);
    }
    public void bar(Vec3 a,Vec3 b,double radius,int rgb,int alpha){
        if(full()||b.distanceToSqr(a)<1e-10||radius<.00001||alpha<1)return;
        Vec3 dir=b.subtract(a).normalize(),x=side(dir);
        prism(a,b,x.scale(radius),dir.cross(x).scale(radius),rgb,alpha);
    }
    private void prism(Vec3 a,Vec3 b,Vec3 s,Vec3 t,int rgb,int alpha){
        if(full()||alpha<1)return;pieces++;
        Vec3 p=a.subtract(s).subtract(t),q=a.add(s).subtract(t),r=a.add(s).add(t),u=a.subtract(s).add(t);
        Vec3 v=b.subtract(s).subtract(t),w=b.add(s).subtract(t),x=b.add(s).add(t),y=b.subtract(s).add(t);
        quad(p,q,r,u,rgb,alpha,.72f);quad(v,y,x,w,rgb,alpha,1);
        quad(p,v,w,q,rgb,alpha,.91f);quad(q,w,x,r,rgb,alpha,.78f);
        quad(r,x,y,u,rgb,alpha,1);quad(u,y,v,p,rgb,alpha,.87f);
    }
    private void quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,int rgb,int alpha,float shade){
        int r=(int)(((rgb>>16)&255)*shade),g=(int)(((rgb>>8)&255)*shade),v=(int)((rgb&255)*shade);
        vertex(a,r,g,v,alpha);vertex(b,r,g,v,alpha);vertex(c,r,g,v,alpha);vertex(d,r,g,v,alpha);
    }
    private void vertex(Vec3 p,int r,int g,int b,int alpha){
        buffer.addVertex(pose,(float)p.x,(float)p.y,(float)p.z).setColor(r,g,b,alpha);
    }
}
