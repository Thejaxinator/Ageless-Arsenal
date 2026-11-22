package ageless.tenno.validation;

import com.jax.destiny2mc.tenno.melee.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid="tenno_melee_check")
public class MeleeFrameworkCheck {
    static void ok(boolean b,String s){ if(!b)throw new AssertionError(s);System.out.println("MELEE CHECK "+s);}
    static class User extends FakePlayer {
        User(ServerLevel w,String id) throws Exception {
            super(w,new GameProfile(UUID.randomUUID(),"DojoTester"));
            setGameMode(GameType.SURVIVAL);
            var field=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");field.setAccessible(true);field.setInt(this,0);
            setPos(5,105,5); setYRot(0);setYHeadRot(0);
            var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",id)));
            setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.forEachModifier(EquipmentSlot.MAINHAND,(type,modifier)->getAttribute(type).addTransientModifier(modifier));
        }
        @Override public boolean isInvulnerableTo(DamageSource s){return false;}
        void guardTime(int t){useItemRemaining=getUseItem().getUseDuration(this)-t;}
        float cut(Mob target,boolean heavy,boolean charge){
            setShiftKeyDown(heavy);attackStrengthTicker=charge?100:0;target.invulnerableTime=0;
            float hp=target.getHealth();attack(target);return hp-target.getHealth();
        }
    }
    static Mob target(ServerLevel w){
        var cow=EntityType.COW.create(w);cow.setPos(5,105,7);cow.setNoAi(true);cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);cow.setHealth(1000);return cow;
    }
    static void guards(ServerLevel w)throws Exception{
        for (var spec:MeleeProfile.ALL){
            User p=new User(w,spec.id()); var mob=target(w);
            var weapon=(TennoMeleeItem)p.getMainHandItem().getItem();
            ok(p.getMainHandItem().getMaxDamage()==spec.durability(),spec.id()+" native maximum durability "+spec.durability());
            ok(Math.abs(p.getAttributeValue(Attributes.ATTACK_DAMAGE)-spec.damage())<.001 &&
                Math.abs(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)-spec.reach())<.001,spec.id()+" equips correct native damage and reach");
            weapon.use(w,p,InteractionHand.MAIN_HAND);p.guardTime(3);p.hurt(w.damageSources().mobAttack(mob),4);
            if(p.getHealth()>=20)throw new AssertionError("premature guard "+spec.id());
            p.guardTime(7);p.setHealth(20);p.invulnerableTime=0;
            p.hurt(w.damageSources().mobAttack(mob),4);
            if(p.getHealth()!=20 || p.getMainHandItem().getDamageValue()==0)throw new AssertionError("front block "+spec.id()+" "+p.getHealth());
            ok(true,spec.id()+" native guard windup and frontal block with durability loss");
            mob.setPos(5,105,3);p.invulnerableTime=0;p.hurt(w.damageSources().mobAttack(mob),4);
            if(p.getHealth()>=20)throw new AssertionError("rear block "+spec.id());
            p.setHealth(20);p.invulnerableTime=0;mob.setPos(7,105,5.2);
            p.hurt(w.damageSources().mobAttack(mob),4);
            ok(p.getHealth()<20,spec.id()+" rear and outside-cone hits pass");
            p.setHealth(20);p.invulnerableTime=0;p.hurt(w.damageSources().magic(),4);
            if(p.getHealth()>=20)throw new AssertionError("bypass blocked "+spec.id());
            if(p.cut(mob,false,true)!=0)throw new AssertionError("attacked while guarding");
            p.stopUsingItem();p.cut(mob,false,true);
        }
    }
    static void moves(ServerLevel w)throws Exception{
        var p=new User(w,"harmony");var mob=target(w);
        var plain=p.cut(mob,true,true);mob.igniteForSeconds(4);mob.addEffect(new MobEffectInstance(MobEffects.POISON,100,0));
        var heavy=p.cut(mob,true,true);
        ok(heavy >= plain+7.9f && !mob.hasEffect(MobEffects.POISON) && !mob.isOnFire(),"Harmony cashes out eight status HP only on a landed heavy");
        p=new User(w,"hate");mob=target(w);
        p.cut(mob,false,true);p.cut(mob,false,true);
        p.cut(mob,false,false);if(mob.getPersistentData().contains("AgelessReapingCut"))throw new AssertionError("spam advanced combo");
        p.cut(mob,false,true);ok(mob.getPersistentData().getCompound("AgelessReapingCut").getInt("ticks")==3,"Hate third charged cut applies three bleed ticks; weak hits skipped");
        p=new User(w,"sun_and_moon");mob=target(w);
        float first=p.cut(mob,false,true), second=p.cut(mob,false,true);float spent=p.cut(mob,true,true);
        ok(Math.abs(second-first-2)<.01 && spent>second && Math.abs(p.cut(mob,false,true)-first)<.01,
                "Sun & Moon same-target alternation, combo heavy and reset");
        var swap=target(w);float open=p.cut(swap,false,true),paired=p.cut(swap,false,true);
        ok(Math.abs(paired-open-2)<.01,"Sun & Moon starts a new sequence on a different target");
        p=new User(w,"heat_sword");mob=target(w);
        p.cut(mob,false,true);
        if(mob.isOnFire())throw new AssertionError("heat light ignites");
        p.cut(mob,true,true);ok(mob.getRemainingFireTicks()>=80,"Heat Sword heavy lights its primary target");
        p=new User(w,"dual_heat_swords");mob=target(w);p.cut(mob,false,true);p.cut(mob,false,true);
        ok(mob.getRemainingFireTicks()>=40,"Dual Heat Swords second charged cut ignites");
        p=new User(w,"amanata");mob=target(w);p.cut(mob,false,true);
        int face=p.getPersistentData().getCompound("AgelessAmanata").getInt("face");
        for(int i=0;i<29;i++)p.cut(mob,false,true);
        if(p.getPersistentData().getCompound("AgelessAmanata").getInt("hits")!=29)throw new AssertionError("early reroll");
        p.cut(mob,false,true);ok(face>=1 && face<=6 && p.getPersistentData().getCompound("AgelessAmanata").getInt("hits")==0,"Amanata first roll and 30 confirmed hit reroll");
    }
    @SubscribeEvent public static void run(ServerStartedEvent e) throws Exception {
        var world=e.getServer().overworld();
        guards(world);moves(world);System.out.println("TENNO MELEE PASS");
    }
}
