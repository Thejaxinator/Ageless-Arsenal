package com.jax.destiny2mc.tenno.melee;

import com.jax.destiny2mc.tenno.mods.ModStat;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Native player attributes preserve the attack meter, range checks, vanilla damage scaling and
 * third-party attribute modifiers. Transient bonuses follow the holder's collection, never the item registry. */
@EventBusSubscriber(modid="destiny2mc")
public final class MeleeModEffects {
    private static final ResourceLocation PRESSURE = TennoMeleeItem.key("tenno/mods/pressure_point");
    private static final ResourceLocation FURY = TennoMeleeItem.key("tenno/mods/fury");
    private static final ResourceLocation REACH = TennoMeleeItem.key("tenno/mods/reach");
    private static final Set<ServerPlayer> ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());
    private MeleeModEffects() {}

    @SubscribeEvent public static void tick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) refreshHeld(player);
    }

    /** Also called before vanilla samples attack strength so a mid-tick item swap takes effect immediately. */
    static void refreshHeld(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof TennoMeleeItem weapon) {
            var profile = weapon.profile();
            var mods = WeaponMods.bonuses(player,held);
            double damage = profile.damage() * mods.getOrDefault(ModStat.DAMAGE,0d);
            double speed = profile.attackSpeed() * mods.getOrDefault(ModStat.MELEE_SPEED,0d);
            double reach = mods.getOrDefault(ModStat.REACH,0d);
            set(player,Attributes.ATTACK_DAMAGE,PRESSURE,damage);
            set(player,Attributes.ATTACK_SPEED,FURY,speed);
            set(player,Attributes.ENTITY_INTERACTION_RANGE,REACH,reach);
            if (damage != 0 || speed != 0 || reach != 0) ACTIVE.add(player);
            else ACTIVE.remove(player);
        } else if (ACTIVE.remove(player)) {
            set(player,Attributes.ATTACK_DAMAGE,PRESSURE,0);
            set(player,Attributes.ATTACK_SPEED,FURY,0);
            set(player,Attributes.ENTITY_INTERACTION_RANGE,REACH,0);
        }
    }

    private static void set(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation key, double amount) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        var previous = instance.getModifier(key);
        if (amount == 0) {
            if (previous != null) instance.removeModifier(key);
        } else if (previous == null || previous.amount() != amount) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(key,amount,AttributeModifier.Operation.ADD_VALUE));
        }
    }

    static boolean acceptsHeat(LivingEntity target) {
        return !target.fireImmune() && !target.hasEffect(MobEffects.FIRE_RESISTANCE);
    }

    /** A successful guard may stagger the attacker. The native block and the weapon's cone resolve first. */
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void parry(LivingShieldBlockEvent event) {
        if (!event.getOriginalBlock() || !event.getBlocked() || event.getBlockedDamage() <= 0
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.getUseItem().getItem() instanceof TennoMeleeItem)) return;
        var source = event.getDamageSource();
        if (!(source.getDirectEntity() instanceof LivingEntity attacker) || source.getEntity() != attacker
                || !(source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                    || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.STING))) return;
        double chance = WeaponMods.bonus(player,player.getUseItem(),ModStat.PARRY);
        if (chance > 0 && player.getRandom().nextFloat() < chance) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2),player);
        }
    }
}
