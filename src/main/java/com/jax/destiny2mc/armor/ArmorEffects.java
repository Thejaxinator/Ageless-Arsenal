package com.jax.destiny2mc.armor;

import com.jax.destiny2mc.ageless.fx.EffectProfile;
import com.jax.destiny2mc.ageless.fx.VoxelCue;
import com.jax.destiny2mc.ageless.fx.VoxelEffects;
import com.jax.destiny2mc.binder.BinderAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** All checks run against the current wearer's actual equipment on the server. A wearer owns
 * each exotic clock; swapping copies, dimensions or characters never shares or resets it. */
@EventBusSubscriber(modid="destiny2mc")
public final class ArmorEffects {
    private static final String CLOCKS="AgelessArmorClocks";
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private ArmorEffects() {}

    private static boolean wearing(ServerPlayer player, ArmorPiece piece) {
        return player.getItemBySlot(piece.type.getSlot()).getItem() instanceof ArsenalArmorItem armor && armor.piece==piece;
    }
    private static boolean pair(ServerPlayer player, ArmorPiece.Suit suit) {
        int count=0;
        for (var slot:SLOTS) {
            var stack=player.getItemBySlot(slot);
            if (stack.getItem() instanceof ArsenalArmorItem armor && armor.piece.suit==suit
                    && armor.getEquipmentSlot()==slot && ++count>=2) return true;
        }
        return false;
    }
    private static boolean spend(ServerPlayer player,String key,int period) {
        var data=player.getPersistentData();
        if(!data.contains(CLOCKS,Tag.TAG_COMPOUND))data.put(CLOCKS,new net.minecraft.nbt.CompoundTag());
        var clocks=data.getCompound(CLOCKS);
        long now=player.serverLevel().getServer().overworld().getGameTime();
        if(clocks.getLong(key)>now)return false;
        clocks.putLong(key,now+period);
        return true;
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        var old=event.getOriginal().getPersistentData();
        if(old.contains(CLOCKS,Tag.TAG_COMPOUND))event.getEntity().getPersistentData().put(CLOCKS,old.getCompound(CLOCKS).copy());
    }

    @SubscribeEvent(priority=EventPriority.LOW) public static void setProtection(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var source=event.getSource();
        if(source.is(DamageTypeTags.BYPASSES_ARMOR))return;
        // One damage category per hit. Classified explosions take priority over their carrier projectile.
        if(source.is(DamageTypeTags.IS_EXPLOSION)) {
            if(pair(player,ArmorPiece.Suit.RULL))event.setAmount(event.getAmount()*.75f);
        } else if(source.is(DamageTypeTags.IS_PROJECTILE)||source.getDirectEntity() instanceof Projectile) {
            if(pair(player,ArmorPiece.Suit.KABR))event.setAmount(event.getAmount()*.82f);
        } else if(source.getDirectEntity() instanceof Mob mob && source.getEntity()==mob
                && hostile(player,mob) && pair(player,ArmorPiece.Suit.WILLBREAKER)) {
            event.setAmount(event.getAmount()*.82f);
        }
    }
    private static boolean hostile(ServerPlayer player,Mob mob) {
        return mob.isAlive() && !mob.isAlliedTo(player) && (mob instanceof Enemy || mob.getTarget()==player);
    }
    private static void effect(LivingEntity e,Holder<MobEffect> type,int duration,int amplifier) {
        e.addEffect(new MobEffectInstance(type,duration,amplifier,false,true,true));
    }
    private static void nearby(ServerPlayer player,double radius,boolean blind) {
        for(var mob:player.serverLevel().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(radius),
                m->hostile(player,m) && player.distanceToSqr(m)<=radius*radius && player.hasLineOfSight(m))) {
            effect(mob,MobEffects.MOVEMENT_SLOWDOWN,60,1);
            if(blind) {
                effect(mob,MobEffects.BLINDNESS,60,0);
                if(mob.getTarget()==player)mob.setTarget(null);
            }
        }
    }
    private static void cue(ServerPlayer p,String element,VoxelCue.Kind kind,Vec3 at,float radius) {
        VoxelEffects.emit(p.serverLevel(),kind,EffectProfile.named(element,0),at,at,radius);
    }
    private static void ward(ServerPlayer p) {
        if(!spend(p,"saint_ward",240))return;
        effect(p,MobEffects.ABSORPTION,100,0);
        nearby(p,4.5,true);
        cue(p,"void",VoxelCue.Kind.RING,p.position().add(0,.4,0),4.5f);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void shieldBlock(LivingShieldBlockEvent event) {
        if(event.getEntity() instanceof ServerPlayer p && event.getBlocked() && event.getBlockedDamage()>0
                && wearing(p,ArmorPiece.HELM_OF_SAINT_14))ward(p);
    }

    /** One grant per successful Binder cast. true means the bonus reserve paid for the grenade. */
    public static boolean afterCast(ServerPlayer player,BinderAbility ability) {
        if(ability.category().equals("grenade") && wearing(player,ArmorPiece.THE_ARMAMENTARIUM))
            return spend(player,"reserve_grenade",800);
        switch(ability.id()) {
            case "towering_barricade","rally_barricade" -> { if(wearing(player,ArmorPiece.HELM_OF_SAINT_14))ward(player); }
            case "marksmans_dodge","gamblers_dodge" -> { if(wearing(player,ArmorPiece.MASK_OF_BAKRIS))shift(player); }
        }
        return false;
    }
    private static void shift(ServerPlayer p) {
        if (p.isPassenger() || p.isSleeping())return;
        // The underlying dodge supplies its validated movement direction. Sweep the player's
        // entire box in quarter-block increments so a narrow obstacle cannot be blinked through.
        var velocity=p.getDeltaMovement();
        Vec3 direction=velocity.multiply(1,0,1).normalize();
        if(direction.lengthSqr()<.9)return;
        var level=p.serverLevel();var origin=p.position();Vec3 destination=origin;
        for(int i=1;i<=20;i++) {
            var offset=direction.scale(i*.25);var candidate=origin.add(offset);
            AABB box=p.getBoundingBox().move(offset);
            if(!level.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))
                    || !level.getWorldBorder().isWithinBounds(box) || !level.noCollision(p,box))break;
            destination=candidate;
        }
        if(origin.distanceToSqr(destination)<.25)return;
        p.teleportTo(destination.x,destination.y,destination.z);
        p.setDeltaMovement(0,velocity.y,0);p.hurtMarked=true;
        cue(p,"stasis",VoxelCue.Kind.BLINK_OUT,origin.add(0,.8,0),1f);
        cue(p,"stasis",VoxelCue.Kind.BLINK_IN,destination.add(0,.8,0),1f);
        nearby(p,3.5,false);
    }
}
