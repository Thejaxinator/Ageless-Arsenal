package com.jax.destiny2mc.ageless.fx.client;
import com.jax.destiny2mc.ageless.fx.EffectProfile;
import net.minecraft.world.phys.Vec3;
import static java.lang.Math.*;

/** Seeded geometry never uses per-frame RNG. Each plume drifts coherently as its lifetime advances. */
final class EffectShapes {
    private static final double TAU=2*PI;
    private static double rand(int seed,int i){
        int h=seed+i*0x1b873593;h^=h>>>16;h*=0x7feb352d;h^=h>>>15;h*=0x846ca68b;h^=h>>>16;
        return (h&0xffffff)/(double)0x1000000;
    }
    private static Vec3 at(Vec3 c,double angle,double r,double y){return c.add(cos(angle)*r,y,sin(angle)*r);}
    private static int opacity(double value){return (int)(255*Math.clamp(value,0,1));}

    static void trace(VoxelMesh m,EffectProfile p,Vec3 a,Vec3 b,double t,int seed){
        Vec3 delta=b.subtract(a);double length=delta.length();if(length<.02)return;
        Vec3 dir=delta.scale(1/length),side=VoxelMesh.side(dir),up=dir.cross(side);
        double fade=pow(1-t,.65);
        if(p==EffectProfile.KINETIC){
            // The dark under-stroke keeps the alternating grey stripes visible against a bright sky.
            m.bar(a,b,.021,p.shade,opacity(fade*.8));
            m.bar(a,b,.012,p.color,opacity(fade));
            int n=(int)Math.clamp(ceil(length/1.45),1,44);
            for(int i=0;i<n;i++)m.bar(a.lerp(b,(i+.23)/n),a.lerp(b,(i+.69)/n),.024,p.core,opacity(fade*.93));
            return;
        }
        double width=(p==EffectProfile.RESONANCE||p==EffectProfile.TAKEN)?.066:.053;
        m.bar(a,b,width*1.75,p.color,opacity(fade*.16));
        m.bar(a,b,width*.48,p.core,opacity(fade));
        int n=(int)Math.clamp(ceil(length/1.18),2,38);
        Vec3 prev=a;
        for(int i=1;i<=n;i++){
            double v=(double)i/n;Vec3 q=a.lerp(b,v);
            switch(p){
                case ARC,ETHER ->{
                    double spread=i==n?0:.11+.06*rand(seed,i);
                    Vec3 tooth=q.add(side.scale((rand(seed,2*i)-.5)*spread*2)).add(up.scale((rand(seed,2*i+1)-.5)*spread*2));
                    m.bar(prev,tooth,.028,p.color,opacity(fade*.92));prev=tooth;
                }
                case STRAND,DARK_ETHER ->{
                    double phase=i*.95+t*2,r=(i==n?0:.12);
                    Vec3 braid=q.add(side.scale(cos(phase)*r)).add(up.scale(sin(phase)*r));
                    m.bar(prev,braid,.027,p.color,opacity(fade*.84));prev=braid;
                }
                case SOLAR,STASIS,VOID,TAKEN,RESONANCE,SOULFIRE,NANITE ->{
                    if(i==n)break;
                    double r=p==EffectProfile.RESONANCE?.12:.082;
                    m.bar(q.subtract(dir.scale(.07)),q.add(dir.scale(.08)),r,p.color,opacity(fade*.7));
                    if(p==EffectProfile.STASIS||p==EffectProfile.SOULFIRE)
                        m.cube(q.add(up.scale(.11*rand(seed,i))),.032,p.core,opacity(fade*.76));
                }
                default ->{}
            }
        }
    }
    static void ring(VoxelMesh m,EffectProfile p,Vec3 c,double r,double t,int seed){
        int sides=p==EffectProfile.RESONANCE?3:p==EffectProfile.DARK_ETHER?6:p==EffectProfile.TAKEN?4:12;
        double spin=seed%7*.16+t*.06,fade=1-t*.72;
        for(int i=0;i<sides;i++){
            Vec3 a=at(c,TAU*i/sides+spin,r,.10),b=at(c,TAU*(i+1)/sides+spin,r,.10);
            m.bar(a,b,.035,p.shade,opacity(fade));m.bar(a,a.lerp(b,.58),.048,p.color,opacity(fade*.87));
            if(sides<=6)m.cube(a.add(0,.09,0),.074,p.core,opacity(fade*.94));
        }
    }
    static void burst(VoxelMesh m,EffectProfile p,Vec3 c,double r,double t,int seed){
        double fade=1-t,s=sin(min(1,t*3)*PI/2);
        m.cube(c,.23*r*fade+.03,p.core,opacity(fade*.92));
        int count=12;
        for(int i=0;i<count;i++){
            double angle=TAU*rand(seed,i),h=(rand(seed,30+i)*1.4-.3),speed=r*(.4+.8*rand(seed,55+i));
            Vec3 dir=new Vec3(cos(angle),h,sin(angle)).normalize();
            Vec3 head=c.add(dir.scale(speed*s)).add(0,-t*t*.39,0),tail=head.subtract(dir.scale((.15+.22*fade)*r));
            m.bar(tail,head,.035+.035*fade,p.color,opacity(fade));m.cube(head,.045+.038*fade,p.core,opacity(fade));
        }
    }
    static void blink(VoxelMesh m,EffectProfile p,Vec3 c,double t,int seed,boolean arrival){
        // Two overlapping columns of charcoal and ether-blue blocks, crossed by a jagged arc.
        double fade=pow(1-t,.75),drift=arrival?max(0,1-3*t)*.5+t*.24:t*.65;
        for(int i=0;i<19;i++){
            double a=rand(seed,i)*TAU,y=.15+rand(seed,90+i)*1.8+t*.58;
            double spread=.12+rand(seed,40+i)*.48+drift;
            Vec3 v=at(c,a+t*(i%2==0?.7:-.7),spread,y);
            m.cube(v,(.09+rand(seed,125+i)*.13)*(.55+fade*.45),i%4==0?p.color:0x354255,opacity(fade*(i%4==0?.73:.55)));
        }
        Vec3 last=c.add(0,.08,0);
        for(int j=1;j<8;j++){
            Vec3 next=at(c,j*2.43,.33*fade,(2.35/7)*j);
            m.bar(last,next,.032,p.core,opacity(fade));m.bar(last,next,.079,p.color,opacity(.21*fade));last=next;
        }
        if(t<.55)ring(m,p,c,.3+t*1.75,t*1.7,seed);
    }
    static void cloud(VoxelMesh m,EffectProfile p,Vec3 c,double r,double t,double ticks,int seed){
        double fade=min(1,min(t*18,(1-t)*12));
        for(int i=0;i<20;i++){
            double a=rand(seed,i)*TAU+ticks*.009,dist=r*sqrt(rand(seed,i+30))*.90;
            double y=.21+.24*sin(a*2+ticks*.085)+rand(seed,i+80)*.65;
            m.cube(at(c,a,dist,y),.12+.13*rand(seed,i+110),i%4==0?p.color:p.shade,opacity(fade*.53));
        }
    }
}
