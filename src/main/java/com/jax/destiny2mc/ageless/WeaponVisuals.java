package com.jax.destiny2mc.ageless;

import com.jax.destiny2mc.frontier.FrontierProjectile;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Presentation bridge for optional renderers. The firing path already clipped the ray or spawned
 * the real projectile. Replacing this visual should never simulate another hit. */
public final class WeaponVisuals {
    public static void trace(ServerLevel level,Vec3 start,Vec3 end,String profile,int element){
        ServerBallisticsEngine.spawnTracerBeam(level,start,end,element);
    }
    public static void projectile(FrontierProjectile bolt,String profile){
        bolt.getPersistentData().putString("ageless_fx",profile);
    }
}
