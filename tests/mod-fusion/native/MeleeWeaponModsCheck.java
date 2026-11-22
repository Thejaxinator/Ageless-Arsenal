package ageless.validation;

import com.jax.destiny2mc.tenno.melee.MeleeModEffects;
import com.jax.destiny2mc.tenno.melee.MeleeModStats;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="tenno_melee_mods")
public final class MeleeWeaponModsCheck {
    private static ItemStack weapon(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",id)));
    }

    private static final class Tester extends FakePlayer {
        Tester(ServerLevel level) throws ReflectiveOperationException {
            super(level,new GameProfile(UUID.randomUUID(),"BladeTester"));
            setGameMode(GameType.SURVIVAL);
            var grace=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime"); grace.setAccessible(true);grace.setInt(this,0);
            setPos(5,105,5); setYRot(0);setYHeadRot(0);
        }
        @Override public boolean isInvulnerableTo(DamageSource source) { return false; }
        // FakePlayer has no ordinary player tick. Apply vanilla's mainhand attributes and advance the mod's native event.
        void equip(ItemStack next) {
            getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND,(type,mod)->getAttribute(type).removeModifier(mod.id()));
            setItemInHand(InteractionHand.MAIN_HAND,next);
            next.forEachModifier(EquipmentSlot.MAINHAND,(type,mod)->getAttribute(type).addOrUpdateTransientModifier(mod));
            update();
        }
        void update() { MeleeModEffects.tick(new PlayerTickEvent.Pre(this)); }
        void unlock(WeaponMod mod,int rank) {
            CompoundTag ledger=getPersistentData().getCompound("AgelessWarframeMods");
            ledger.putInt(mod.id(),rank);getPersistentData().put("AgelessWarframeMods",ledger);
        }
        void socket(WeaponMod mod,int rank) {
            unlock(mod,rank);
            require(WeaponMods.install(this,getMainHandItem(),mod),"install "+mod.id());
            update();
        }
        float cut(Mob target,boolean heavy) {
            setShiftKeyDown(heavy);attackStrengthTicker=100;target.invulnerableTime=0;
            float hp=target.getHealth();attack(target);return hp-target.getHealth();
        }
        void guard(int elapsed) {
            var w=(TennoMeleeItem)getMainHandItem().getItem();
            w.use(level(),this,InteractionHand.MAIN_HAND);
            useItemRemaining=getUseItem().getUseDuration(this)-elapsed;
        }
    }
    private static Mob enemy(ServerLevel world) {
        var mob=EntityType.COW.create(world);mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(5,105,7);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);
        return mob;
    }
    private static void require(boolean b,String message) { if(!b)throw new AssertionError(message); }
    private static void near(double actual,double expected,String label) {
        if(Math.abs(actual-expected)>.003)throw new AssertionError(label+" expected="+expected+" actual="+actual);
    }
    private static void passed(String text) { System.out.println("MELEE MODS "+text); }

    private static void characterAndBlade(ServerLevel world) throws Exception {
        Tester p=new Tester(world);
        ItemStack blade=weapon("hate");p.equip(blade);
        near(p.cut(enemy(world),false),8,"base Hate attack");
        p.socket(WeaponMod.PRESSURE_POINT,0);
        near(p.cut(enemy(world),false),8.8,"rank zero physical damage");
        p.giveExperienceLevels(10);
        require(ModCollection.rankUp(p,"pressure_point",0),"upgrade held mod");
        near(p.cut(enemy(world),false),9.6,"upgrading global rank is immediate on attack");
        p.socket(WeaponMod.FURY,2);p.socket(WeaponMod.REACH,3);
        var stats=MeleeModStats.of(p,blade);
        near(p.getAttributeValue(Attributes.ATTACK_SPEED),1.25*1.18,"Fury native speed");
        near(p.getCurrentItemAttackStrengthDelay(),20/(1.25*1.18),"Fury vanilla cooldown");
        near(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE),4.35,"Reach native range");
        near(stats.speed(),p.getAttributeValue(Attributes.ATTACK_SPEED),"card speed");near(stats.reach(),4.35,"card reach");
        var distant=enemy(world);distant.setPos(5,105,9.52);
        require(p.canInteractWithEntity(distant,0),"hitbox inside extended reach");
        p.equip(weapon("hate"));
        require(!p.canInteractWithEntity(distant,0),"unmodded duplicate loses reach");
        near(p.getAttributeValue(Attributes.ATTACK_SPEED),1.25,"unmodded duplicate speed");
        near(p.cut(enemy(world),false),8,"unmodded duplicate damage");
        p.equip(blade);
        var second=new Tester(world);second.equip(blade.copy());
        near(second.cut(enemy(world),false),8,"locked socket on transfer is inactive");
        second.unlock(WeaponMod.PRESSURE_POINT,3);second.update();
        near(second.cut(enemy(world),false),11.2,"different holder's global rank");
        p.equip(new ItemStack(Items.IRON_SWORD));
        near(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE),3,"vanilla reach");
        near(p.getAttributeValue(Attributes.ATTACK_SPEED),1.6,"vanilla speed");
        near(p.cut(enemy(world),false),6,"vanilla damage");
        p.equip(weapon("ace_of_spades"));near(p.getAttributeValue(Attributes.ATTACK_SPEED),4,"Destiny speed");
        near(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE),3,"Destiny reach");
        passed("Pressure Point ranks 0/1/3 and per-holder transfer; Fury updates real attack meter; Reach extends actual targetability; duplicate, vanilla and Destiny stacks keep ordinary attributes");
    }
    private static void heatAndHeavy(ServerLevel world) throws Exception {
        Tester p=new Tester(world);p.equip(weapon("harmony"));p.socket(WeaponMod.KILLING_BLOW,1);
        near(p.cut(enemy(world),false),7,"heavy-only light unaffected");
        // A charged light preceded this heavy, so the combo contributes 0.15 to the base heavy multiplier.
        near(p.cut(enemy(world),true),7*1.4*1.24,"heavy combo × Killing Blow");
        near(p.cut(enemy(world),true),7*1.25*1.24,"spent combo");
        p.socket(WeaponMod.MOLTEN_IMPACT,2);
        Mob target=enemy(world);near(p.cut(target,false),7*1.24,"conditional Heat hit");
        require(target.getRemainingFireTicks()>=40,"Heat ignites a landed attack");
        target=enemy(world);target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200,0));
        near(p.cut(target,false),7,"heat-resistant target");require(!target.isOnFire(),"heat resisted");
        target=enemy(world);target.addEffect(new MobEffectInstance(MobEffects.POISON,100,0));target.igniteForSeconds(4);
        require(p.cut(target,true)>19 && !target.hasEffect(MobEffects.POISON),"Harmony cashout retained");
        require(target.getRemainingFireTicks()<=40 && target.isOnFire(),"new Heat added after clearing earlier status");
        passed("Killing Blow multiplies charged heavy after combo; Molten Impact deals conditional Heat, respects resistance and re-ignites after Harmony consumes pre-hit statuses");
    }
    private static void blockAndCounter(ServerLevel world) throws Exception {
        Tester p=new Tester(world);p.equip(weapon("heat_sword"));
        p.socket(WeaponMod.PARRY,5);p.guard(2);
        var attacker=enemy(world);p.hurt(world.damageSources().mobAttack(attacker),3);
        require(p.getHealth()<20 && !attacker.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"windup intact");
        p.guard(7);p.setHealth(20);
        p.getRandom().setSeed(41L);
        boolean yes=false,no=false;
        for(int n=0;n<40;n++){
            attacker.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);p.invulnerableTime=0;
            p.hurt(world.damageSources().mobAttack(attacker),3);
            var slow=attacker.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            require(p.getHealth()==20,"frontal guard failed");
            if(slow==null)no=true;else{
                require(slow.getAmplifier()==2 && slow.getDuration()==40,"parry stagger grade and duration");
                yes=true;
            }
        }
        require(yes && no,"Parry has chance and is not guaranteed");
        attacker.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        attacker.setPos(5,105,3);p.invulnerableTime=0;p.hurt(world.damageSources().mobAttack(attacker),3);
        require(p.getHealth()<20 && !attacker.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"rear attack not parried");
        attacker.setPos(5,105,7);p.setHealth(20);
        for(int n=0;n<8;n++){
            var arrow=new Arrow(EntityType.ARROW,world);arrow.setPos(5,106,7);p.invulnerableTime=0;
            p.hurt(world.damageSources().arrow(arrow,attacker),2);
        }
        require(p.getHealth()==20 && !attacker.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"arrows block without counter-stagger");
        p.stopUsingItem();p.setHealth(20);p.invulnerableTime=0;p.hurt(world.damageSources().mobAttack(attacker),3);
        require(p.getHealth()<20 && !attacker.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"released guard cannot stagger");
        passed("Right-click guard kept its 5-tick windup and frontal arc; successful melee guard can stagger for 40 ticks; projectiles, rear hits and released guard do not counter");
    }
    @SubscribeEvent public static void run(ServerStartedEvent event) throws Exception {
        var world=event.getServer().overworld();
        characterAndBlade(world);heatAndHeavy(world);blockAndCounter(world);
        System.out.println("MELEE MODS PASS");
    }
}
