package com.jax.destiny2mc.ageless.fx.client;

import com.jax.destiny2mc.ageless.fx.*;
import com.jax.destiny2mc.entity.*;
import com.jax.destiny2mc.frontier.FrontierProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Draw at the replicated projectile pose, so movement, wall attachment and detonation agree with gameplay. */
public final class BoltMesh {
    public static boolean draw(Entity e,float partial,PoseStack poses,MultiBufferSource buffers){
        EffectProfile p;
        boolean stuck=false;
        if(e instanceof FrontierProjectile bolt && bolt instanceof ProjectileLook look){p=look.ageless$profile();stuck=look.ageless$stuck();}
        else if(e instanceof HiveSoulfireBoltEntity)p=EffectProfile.SOULFIRE;
        else if(e instanceof ShockBoltEntity || e instanceof WireRifleBoltEntity || e instanceof ShockGrenadeEntity)p=EffectProfile.ARC;
        else if(e instanceof ScorchRocketEntity || e instanceof VexSlapBoltEntity)p=EffectProfile.SOLAR;
        else if(e instanceof VoidAxiomBoltEntity)p=EffectProfile.VOID;
        else if(e instanceof ShrapnelPelletEntity)p=EffectProfile.KINETIC;
        else return false;
        VoxelMesh m=new VoxelMesh(buffers.getBuffer(VoxelMesh.TYPE),poses.last().pose(),30);
        Vec3 dir=e.getDeltaMovement().normalize();if(dir.lengthSqr()<.5)dir=new Vec3(0,0,1);
        Vec3 side=VoxelMesh.side(dir),up=dir.cross(side),center=Vec3.ZERO;
        double time=(e.tickCount+partial)*.42, pulse=.5+.5*Math.sin(time+e.getId());
        if(p==EffectProfile.NANITE){
            // A small swarm cluster has a square dark center and three orbiting red motes.
            m.cube(center,.065,p.shade,252);
            for(int i=0;i<3;i++){
                double a=time*.7+i*Math.PI*2/3;
                Vec3 mot=side.scale(Math.cos(a)*.102).add(up.scale(Math.sin(a)*.102)).add(dir.scale(-.04*i));
                m.cube(mot,.037,p.color,249);m.bar(mot.subtract(dir.scale(.15)),mot,.016,p.core,180);
            }
            return true;
        }
        if(!stuck){
            double tail=Math.clamp(e.getDeltaMovement().length()*.75,.21,1.25);
            m.bar(dir.scale(-tail),center,.045,p.color,168);
            m.bar(dir.scale(-tail*.69),center,.021,p.core,218);
            if(p==EffectProfile.TAKEN || p==EffectProfile.RESONANCE){
                m.cube(dir.scale(-tail*.78),.054,p.shade,201);m.cube(dir.scale(-tail*.44),.079,p.color,192);
            }
        }
        double radius=stuck?.13:.102;
        m.cube(center,radius*(1+.15*pulse),p.shade,254);
        m.bar(dir.scale(-radius*1.6),dir.scale(radius*1.65),radius*.45,p.core,253);
        // Three free-floating prongs echo a charged fusion mine; an attached mine breathes in place.
        int points=(p==EffectProfile.VOID||p==EffectProfile.RESONANCE)?3:4;
        for(int i=0;i<points;i++){
            double phase=i*(2*Math.PI/points)+time*.26;
            Vec3 tip=side.scale(Math.cos(phase)).add(up.scale(Math.sin(phase))).scale(radius*(1.2+.20*pulse));
            m.bar(tip.add(dir.scale(-radius*.9)),tip.add(dir.scale(radius)),radius*.32,p.color,239);
        }
        return true;
    }
}
