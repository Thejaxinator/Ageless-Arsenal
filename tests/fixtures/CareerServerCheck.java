package ageless.validation.career;

import com.jax.destiny2mc.binder.progression.*;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Test-only mod; installs a disposable fake player and damages registered mobs. */
@EventBusSubscriber(modid="career_check")
public final class CareerServerCheck {
    private static void check(boolean what, String msg) {
        if (!what) throw new AssertionError(msg);
        System.out.println("CAREER NATIVE " + msg);
    }
    private static LivingEntity dreg(ServerLevel level) {
        LivingEntity e = (LivingEntity)BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("destiny2mc","fallen_dreg")).create(level);
        e.setPos(8.5,65,8.5);
        level.addFreshEntity(e);
        e.setHealth(1);
        return e;
    }

    /** Force the next RNG roll into the veteran range, then enter the same
     * NeoForge finalize hook used when a mob naturally joins a spawn group. */
    private static boolean spawnRoll(ServerLevel world, MobSpawnType type) {
        Mob mob = (Mob)BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("destiny2mc", "fallen_dreg")).create(world);
        mob.setPos(8.5, 65, 8.5);
        long seed = 0;
        do {
            mob.getRandom().setSeed(++seed);
        } while (mob.getRandom().nextInt(100) >= 28);
        mob.getRandom().setSeed(seed);
        EventHooks.finalizeMobSpawn(mob, world, world.getCurrentDifficultyAt(mob.blockPosition()), type, null);
        return CareerVeterans.isVeteran(mob);
    }
    @SubscribeEvent public static void run(ServerStartedEvent e) {
        var server=e.getServer();
        var world=server.overworld();
        try {
            world.setChunkForced(0,0,true);
            world.getChunk(0,0);
            world.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(true,server);
            ServerPlayer p=FakePlayerFactory.get(world,new GameProfile(UUID.fromString("4a0f11e8-4b8b-40f8-890d-29a56fd9b53e"),"CurveCheck"));
            p.setPos(8,65,8);
            p.setGameMode(GameType.SURVIVAL);
            p.getPersistentData().putInt("AgelessCareerXp",0);
            int vanilla=p.totalExperience;
            dreg(world).hurt(world.damageSources().playerAttack(p),4000);
            check(CareerProgress.points(p)==12 && p.totalExperience==vanilla+1,"Dreg pays 12 career XP and 1 vanilla XP alongside ordinary drops");
            p.getPersistentData().putInt("AgelessCareerXp",CareerCurve.threshold(20)-12);
            dreg(world).hurt(world.damageSources().playerAttack(p),4000);
            check(p.getMaxHealth()==40 && p.getAttributeValue(Attributes.ARMOR)==2,"Level 20 gives 40 health and 2 armor");
            p.giveExperienceLevels(12); p.giveExperienceLevels(-8);
            check(CareerProgress.level(p)==20,"Vanilla level spending preserves career level");
            p.getPersistentData().putInt("AgelessCareerXp",CareerCurve.threshold(50)-12);
            dreg(world).hurt(world.damageSources().playerAttack(p),4000);
            CareerProgress.respawn(new PlayerEvent.PlayerRespawnEvent(p,false));
            check(p.getMaxHealth()==60 && p.getAttributeValue(Attributes.ARMOR)==5,"Level 50 gives 60 health and 5 armor without stacking");
            ServerPlayer clone=FakePlayerFactory.get(world,new GameProfile(UUID.fromString("16388b84-f2d2-4cd4-9b63-c8dbf37fe4b3"),"CurveClone"));
            CareerProgress.clone(new PlayerEvent.Clone(clone,p,true));
            CareerProgress.respawn(new PlayerEvent.PlayerRespawnEvent(clone,false));
            check(CareerProgress.level(clone)==50 && clone.getHealth()==60,"Death clone keeps level and restores bonus hearts");
            p.setHealth(49);
            var playerSave=new net.minecraft.nbt.CompoundTag();
            p.saveWithoutId(playerSave);
            clone.getAttribute(Attributes.MAX_HEALTH).removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("destiny2mc","career_vitality"));
            clone.setHealth(20);
            clone.load(playerSave);
            check(clone.getMaxHealth()==60 && clone.getHealth()==49,"Periodic player NBT save restores injured bonus hearts without a logout event");
            dreg(world).hurt(world.damageSources().playerAttack(p),4000);
            check(CareerProgress.points(p)==16072,"Lifetime points capped");
            var veteran=(net.minecraft.world.entity.Mob)dreg(world);
            float baseHealth=veteran.getMaxHealth();
            double armor=veteran.getAttributeValue(Attributes.ARMOR);
            CareerVeterans.applyRank(veteran);
            CareerVeterans.applyRank(veteran);
            check(Math.abs(veteran.getMaxHealth()-baseHealth*1.25)<0.01 && veteran.getAttributeValue(Attributes.ARMOR)==armor+2,
                "Veterans gain 25% base health and 2 armor once");
            var tag=new net.minecraft.nbt.CompoundTag();
            veteran.saveWithoutId(tag);
            var restored=(net.minecraft.world.entity.Mob)veteran.getType().create(world);
            restored.load(tag);
            check(CareerVeterans.isVeteran(restored) && restored.getMaxHealth()==veteran.getMaxHealth() &&
                restored.getCustomName()!=null && restored.getCustomName().getString().startsWith("Veteran "),
                "Veteran rank, attributes and name survive saving");
            world.addNewPlayer(p);
            check(spawnRoll(world, MobSpawnType.NATURAL), "Level 50 player can promote a naturally spawning Dreg");
            check(!spawnRoll(world, MobSpawnType.SPAWN_EGG), "Spawn eggs skip veteran promotion");
            p.getPersistentData().putInt("AgelessCareerXp", 0);
            check(!spawnRoll(world, MobSpawnType.NATURAL), "Level 1 players retain ordinary natural spawns");
            world.removePlayerImmediately(p, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            System.out.println("CAREER NATIVE COMPLETE");
        } catch(Throwable t) {
            t.printStackTrace();
            System.out.println("CAREER NATIVE FAILED: "+t);
        }
    }
}
