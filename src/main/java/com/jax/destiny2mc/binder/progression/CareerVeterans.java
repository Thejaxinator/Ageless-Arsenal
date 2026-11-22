package com.jax.destiny2mc.binder.progression;

import com.jax.destiny2mc.binder.FactionEnemies;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/** Promote an existing natural spawn. Select the nearest survival player so a distant
 * veteran player cannot silently raise the challenge across the whole dimension. */
@EventBusSubscriber(modid="destiny2mc")
public final class CareerVeterans {
    private static final String VETERAN = "AgelessVeteran";
    private static final ResourceLocation STAMINA = CareerProgress.key("veteran_stamina");
    private static final ResourceLocation PLATING = CareerProgress.key("veteran_plating");
    private CareerVeterans() {}
    public static boolean isVeteran(LivingEntity entity) { return entity.getPersistentData().getBoolean(VETERAN); }

    @SubscribeEvent public static void promote(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (!(mob.level() instanceof ServerLevel level) || event.getSpawnType() != MobSpawnType.NATURAL
                || !FactionEnemies.isEnemy(mob) || FactionEnemies.isBoss(mob) || mob.hasCustomName()) return;
        double limit = 96.0 * 96.0;
        ServerPlayer nearest = null;
        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(mob);
            if (player.isAlive() && !player.isCreative() && !player.isSpectator() && distance < limit) {
                nearest = player;
                limit = distance;
            }
        }
        if (nearest == null || mob.getRandom().nextInt(100) >= CareerCurve.veteranChance(CareerProgress.level(nearest))) return;
        applyRank(mob);
    }

    /** Apply the same saved veteran rank when authoring a future encounter. Idempotent. */
    public static void applyRank(Mob mob) {
        if (isVeteran(mob)) return;
        mob.getPersistentData().putBoolean(VETERAN, true);
        mob.setCustomName(Component.literal("Veteran ").append(mob.getName()));
        // Keep these on the mob itself so chunk unload/reload preserves both rank and current health.
        var health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && !health.hasModifier(STAMINA)) {
            health.addPermanentModifier(new AttributeModifier(STAMINA, .25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            mob.setHealth(mob.getMaxHealth());
        }
        var armor = mob.getAttribute(Attributes.ARMOR);
        if (armor != null && !armor.hasModifier(PLATING))
            armor.addPermanentModifier(new AttributeModifier(PLATING, 2, AttributeModifier.Operation.ADD_VALUE));
    }
}
