package com.jax.destiny2mc.binder.progression;

import com.jax.destiny2mc.atlas.DungeonNode;
import com.jax.destiny2mc.binder.FactionEnemies;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="destiny2mc")
public final class CareerProgress {
    static final String POINTS = "AgelessCareerXp";
    private static final String REWARDED = "AgelessCareerRewarded";
    private static final ResourceLocation VITALITY = key("career_vitality");
    private static final ResourceLocation DEFENSE = key("career_defense");
    private CareerProgress() {}

    static ResourceLocation key(String path) { return ResourceLocation.fromNamespaceAndPath("destiny2mc", path); }
    public static int points(ServerPlayer player) { return CareerCurve.clampPoints(player.getPersistentData().getInt(POINTS)); }
    public static int level(ServerPlayer player) { return CareerCurve.level(points(player)); }
    /** Applying or removing the same stable modifier multiple times never stacks it.
     * Transient modifiers disappear when someone returns to the core-only edition. */
    static void attribute(LivingEntity mob, Holder<Attribute> attr, ResourceLocation id, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attr);
        if (instance == null) return;
        instance.removeModifier(id);
        if (value != 0) instance.addTransientModifier(new AttributeModifier(id, value, operation));
    }
    static void applyStats(ServerPlayer player, int level) {
        attribute(player, Attributes.MAX_HEALTH, VITALITY, CareerCurve.healthBonus(level), AttributeModifier.Operation.ADD_VALUE);
        attribute(player, Attributes.ARMOR, DEFENSE, CareerCurve.armorBonus(level), AttributeModifier.Operation.ADD_VALUE);
    }
    public static void restoreStats(ServerPlayer player) {
        applyStats(player, level(player));
    }

    private static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CareerSync(points(player)));
    }

    /** The regular combat-loot path already checks kill credit and only runs on a completed
     * death. XP paid here never consumes or substitutes the victim's vanilla XP orbs. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void earn(LivingDropsEvent e) {
        LivingEntity mob = e.getEntity();
        if (mob.level().isClientSide || !e.isRecentlyHit() || !mob.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        if (!(mob.getKillCredit() instanceof ServerPlayer player) || player.isCreative() || player.isSpectator()) return;
        boolean faction = FactionEnemies.isEnemy(mob);
        if (mob instanceof DungeonNode || (!faction && !(mob instanceof Enemy)) || mob.getPersistentData().getBoolean(REWARDED)) return;
        mob.getPersistentData().putBoolean(REWARDED, true);
        boolean boss = faction && FactionEnemies.isBoss(mob);
        int reward = faction ? (boss ? 90 : CareerVeterans.isVeteran(mob) ? 18 : 12) : 5;
        if (faction) player.giveExperiencePoints(boss ? 5 : 1);

        int oldPoints = points(player);
        int total = CareerCurve.clampPoints((long)oldPoints + reward);
        if (oldPoints == total) return;
        player.getPersistentData().putInt(POINTS, total);
        int level = CareerCurve.level(total);
        if (level != CareerCurve.level(oldPoints)) {
            float before = player.getMaxHealth();
            applyStats(player, level);
            player.heal(Math.max(0, player.getMaxHealth() - before));
            player.displayClientMessage(Component.literal("Paracausal level " + level), true);
        }
        sync(player);
    }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getOriginal() instanceof ServerPlayer original) {
            player.getPersistentData().putInt(POINTS, points(original));
            restoreStats(player);
            if (!event.isWasDeath()) player.setHealth(original.getHealth());
        }
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        restoreStats(player);
        sync(player);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        applyStats(player, level(player));
        if (!e.isEndConquered()) player.setHealth(player.getMaxHealth());
        sync(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            applyStats(player, level(player));
            sync(player);
        }
    }
}
