package com.jax.destiny2mc.binder.difficulty;

import com.jax.destiny2mc.binder.FactionEnemies;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid="destiny2mc")
public final class EnemyDamage {
    // Weak server-player keys avoid carrying suppression through logout, respawn, or world unload.
    private static final Map<ServerPlayer,Long> lastDamaged=new WeakHashMap<>();
    private static final Set<String> ENEMY_BOLTS=Set.of("shock_bolt","shock_grenade","wire_rifle_bolt",
        "shrapnel_pellet","scorch_rocket","hive_soulfire_bolt","vex_slap_bolt","void_axiom_bolt");
    /** Resolves melee, indirect magic, encounter mechanics and owner-attributed projectiles.
     * Mod namespace of a projectile alone cannot establish allegiance: players use the same bolts. */
    private static Entity attacker(DamageSource source){
        Entity owner=source.getEntity();
        if(owner==null && source.getDirectEntity() instanceof Projectile p)owner=p.getOwner();
        if(owner==null && source.getDirectEntity() instanceof Projectile bolt){
            // These older types are exclusive to enemies. A projectile may briefly outlive its owner.
            var type=BuiltInRegistries.ENTITY_TYPE.getKey(bolt.getType());
            if(type.getNamespace().equals("destiny2mc")&&ENEMY_BOLTS.contains(type.getPath()))return bolt;
        }
        if(!(owner instanceof LivingEntity living))return null;
        return FactionEnemies.isEnemy(living)?living:null;
    }
    private static boolean boss(Entity enemy){
        return enemy instanceof LivingEntity living && FactionEnemies.isBoss(living);
    }
    @SubscribeEvent public static void reduce(LivingIncomingDamageEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        var enemy=attacker(event.getSource());if(enemy==null)return;
        long now=player.level().getGameTime();Long previous=lastDamaged.get(player);
        boolean followUp=previous!=null && now>=previous && now-previous<10;
        event.setAmount(EncounterSettings.get(player.serverLevel()).preset().incoming(event.getAmount(),boss(enemy),followUp));
    }
    @SubscribeEvent public static void recordImpact(LivingDamageEvent.Post event){
        if(event.getNewDamage()>0 && event.getEntity() instanceof ServerPlayer player && attacker(event.getSource())!=null){
            lastDamaged.put(player,player.level().getGameTime());
        }
    }
}
