package com.jax.destiny2mc.armor;

import com.jax.destiny2mc.ageless.materials.FactionComponents;
import com.jax.destiny2mc.binder.FactionEnemies;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Supplements the existing loot collection, so later handlers can still change or cancel it. */
@EventBusSubscriber(modid="destiny2mc")
public final class ArmorDrops {
    private static List<ArmorPiece> set(ArmorPiece.Suit suit) {
        return Arrays.stream(ArmorPiece.values()).filter(p->p.suit==suit).toList();
    }
    private static final Map<String,List<ArmorPiece>> BOSS_SETS=Map.of(
        "vex",set(ArmorPiece.Suit.KABR),"hive",set(ArmorPiece.Suit.WILLBREAKER),"cabal",set(ArmorPiece.Suit.RULL));
    private ArmorDrops() {}
    @SubscribeEvent public static void drops(LivingDropsEvent event) {
        var mob=event.getEntity();
        if(!(mob.level() instanceof ServerLevel level) || !event.isRecentlyHit()
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) || !FactionEnemies.isBoss(mob))return;
        for(var faction:FactionComponents.ALL) {
            var set=BOSS_SETS.get(faction.faction());
            if(set==null || !mob.getType().is(faction.source())) continue;
            var picked=set.get(mob.getRandom().nextInt(set.size()));
            var item=BuiltInRegistries.ITEM.get(ArmorPiece.key(picked.id));
            var drop=new ItemEntity(level,mob.getX(),mob.getY()+.25,mob.getZ(),new ItemStack(item));
            drop.setDefaultPickUpDelay();event.getDrops().add(drop);
            return;
        }
    }
}
