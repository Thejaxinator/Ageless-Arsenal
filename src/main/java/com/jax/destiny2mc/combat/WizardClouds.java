package com.jax.destiny2mc.combat;

import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Clouds outlive their caster; level keys and owner UUIDs do not retain unloaded worlds/entities. */
@EventBusSubscriber(modid="destiny2mc")
public final class WizardClouds {
    private record Cloud(Vec3 center,long expires,UUID owner) {}
    private static final Map<ServerLevel,List<Cloud>> CLOUDS=new WeakHashMap<>();
    public static void add(ServerLevel level,CombatMobEntity owner,Vec3 center) {
        List<Cloud> list=CLOUDS.computeIfAbsent(level,k->new ArrayList<>());
        if(list.size()>=64)list.remove(0);
        list.add(new Cloud(center,level.getGameTime()+120,owner.getUUID()));
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;
        List<Cloud> list=CLOUDS.get(level);if(list==null)return;
        long now=level.getGameTime();list.removeIf(c->c.expires<=now);
        if(list.isEmpty()){CLOUDS.remove(level);return;}
        for(Cloud c:list) {
            if(now%4==0)level.sendParticles(ParticleTypes.SMOKE,c.center.x,c.center.y+.7,c.center.z,18,1.5,.6,1.5,.012);
            if(now%8==0)level.sendParticles(ParticleTypes.SCULK_SOUL,c.center.x,c.center.y+.25,c.center.z,6,1.4,.2,1.4,.01);
            for(Player player:level.players()) {
                if(player.isCreative()||player.isSpectator()||!player.isAlive())continue;
                Vec3 d=player.position().subtract(c.center);
                if(CombatMoves.inCloud(d.x,d.y,d.z,3)) {
                    if(player.getEffect(MobEffects.POISON)==null||player.getEffect(MobEffects.POISON).getDuration()<=20)
                        player.addEffect(new MobEffectInstance(MobEffects.POISON,40,0,false,true,true));
                    if(player.getEffect(MobEffects.DARKNESS)==null||player.getEffect(MobEffects.DARKNESS).getDuration()<=20)
                        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,40,0,false,false,true));
                }
            }
        }
    }
}
