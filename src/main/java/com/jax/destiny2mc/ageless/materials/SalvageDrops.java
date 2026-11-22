package com.jax.destiny2mc.ageless.materials;

import com.jax.destiny2mc.frontier.BossSigilItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid="destiny2mc")
public final class SalvageDrops {
    /**
     * Add salvage to the existing cancellable death drop collection. Pack authors can set
     * the source tags without changing AI classes; vanilla player kill credit also covers
     * indirect kills. A duplicated entity in multiple tags resolves to the first faction.
     */
    @SubscribeEvent public static void onDrops(LivingDropsEvent event) {
        var mob = event.getEntity();
        if (!(mob.level() instanceof ServerLevel level) || !event.isRecentlyHit()
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        for (var part : FactionComponents.ALL) {
            if (mob.getType().is(part.source())) {
                var drop = new ItemEntity(level, mob.getX(), mob.getY(), mob.getZ(),
                    new ItemStack(part.item(), (mob instanceof com.jax.destiny2mc.frontier.FrontierMob f && f.unit.boss() || BossSigilItem.isEncounter(mob)) ? 3 : 1));
                drop.setDefaultPickUpDelay();
                event.getDrops().add(drop);
                return;
            }
        }
    }
}
