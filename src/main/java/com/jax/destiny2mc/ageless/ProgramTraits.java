package com.jax.destiny2mc.ageless;

import com.jax.destiny2mc.frontier.*;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import java.util.Comparator;

/** Optional data-selected ray reactions. Trait-free profiles never touch these counters. Secondary
 * hits deliberately bypass the trait callback, preventing a chain from recursively spawning chains. */
@EventBusSubscriber(modid="destiny2mc")
public final class ProgramTraits {
    @SubscribeEvent public static void arcDamage(LivingDamageEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getNewDamage()<=0)return;
        Entity direct=e.getSource().getDirectEntity();
        if(direct instanceof LightningBolt||direct instanceof FrontierProjectile bolt&&bolt.element()==2||
            direct!=null&&java.util.Set.of("shock_bolt","shock_grenade","wire_rifle_bolt").contains(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType()).getPath())){
            p.getPersistentData().putLong("aa_arc_until",p.level().getGameTime()+100);
        }
    }
    static float multiplier(WeaponProgram.Hit trait,ServerPlayer p,ItemStack s,LivingEntity v,boolean crit,long now){
        return switch(trait){
            case PRECISION_STACK -> 1f+(GunDataHelper.getLong(s,"aa_precision_until")>now?GunDataHelper.getInt(s,"aa_precision",0)*.07f:0);
            case MASTER -> GunDataHelper.getLong(s,"aa_master_until")>now?1.25f:1;
            case ANTI_ARMOR -> v.getArmorValue()>0?1.35f:1;
            case UNARMORED_PRECISION -> crit&&v.getArmorValue()==0?1.3f:1;
            default -> 1;
        };
    }
    static void hit(WeaponProgram prog,ServerPlayer p,ItemStack s,WeaponSpec w,LivingEntity v,Vec3 pos,boolean crit){
        long t=p.level().getGameTime();boolean kill=!v.isAlive();
        switch(prog.onHit()){
            case PRECISION_STACK -> {
                int prev=GunDataHelper.getLong(s,"aa_precision_until")>t?GunDataHelper.getInt(s,"aa_precision",0):0;
                GunDataHelper.setInt(s,"aa_precision",crit?Math.min(prev+1,5):0);GunDataHelper.setLong(s,"aa_precision_until",t+70);
            }
            case MASTER -> {if(kill)GunDataHelper.setLong(s,"aa_master_until",t+80);}
            case REFUND -> {if(kill&&crit)refund(s,w,2);}
            case CONDUCTOR -> {
                if(p.getPersistentData().getLong("aa_arc_until")>t){
                    refund(s,w,1);for(var q:near(p,v,pos,5,1))secondary(p,q,pos,2.8f,"arc",2);
                }
            }
            case COLLAPSE -> {if(kill)for(var q:near(p,v,pos,3.5,5))secondary(p,q,pos,5,"void",3);}
            case SWARM -> {
                if(kill&&crit)for(var q:near(p,v,pos,7,3)){
                    var bolt=FrontierProjectile.launch(p.serverLevel(),p,pos,q.getBoundingBox().getCenter().subtract(pos).normalize(),.85f,3,0,8,0);
                    bolt.target(q);WeaponVisuals.projectile(bolt,"nanite");
                }
            }
            default -> {}
        }
    }
    private static java.util.List<LivingEntity> near(ServerPlayer p,LivingEntity v,Vec3 pos,double r,int limit){
        return p.level().getEntitiesOfClass(LivingEntity.class,new AABB(pos,pos).inflate(r),e->e!=v&&CombatRules.canDamage(p,e)&&pos.distanceToSqr(e.getBoundingBox().getCenter())<=r*r&&CombatRules.visible(p,pos,e)).stream().sorted(Comparator.comparingDouble(e->e.distanceToSqr(pos))).limit(limit).toList();
    }
    private static void secondary(ServerPlayer p,LivingEntity q,Vec3 from,float damage,String fx,int el){
        if(q.hurt(p.damageSources().playerAttack(p),damage))WeaponVisuals.trace(p.serverLevel(),from,q.getBoundingBox().getCenter(),fx,el);
    }
    private static void refund(ItemStack s,WeaponSpec w,int count){
        GunDataHelper.setInt(s,"d2_mag",Math.min(WeaponPerkRegistry.getEffectiveMagSize(s,w),GunDataHelper.getMag(s,w)+count));
    }
}
