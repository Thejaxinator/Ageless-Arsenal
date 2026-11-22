package ageless.modrangetest;

import com.jax.destiny2mc.tenno.mods.*;

import com.jax.destiny2mc.tenno.*;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import static com.jax.destiny2mc.tenno.mods.WeaponMod.*;

/** A separate, disposable dedicated-server fixture. Uses native shots, timed reloads, actual
 * entity health, active status effects and the same character rank ledger used by pack opening. */
@EventBusSubscriber(modid="modded_ranged_check")
public final class ModdedRangedCheck {
    private static ServerLevel world;
    private static FakePlayer player;
    private static final List<Zombie> mobs = new ArrayList<>();

    private static void ensure(boolean value,String intent) {if (!value) throw new AssertionError(intent);}
    private static void pass(boolean value,String text) {ensure(value,text);System.out.println("WF RANGED MODS "+text);}
    private static void approx(double actual,double expected,String what) {
        ensure(Math.abs(actual-expected)<.015,what+" actual="+actual+", expected="+expected);
        System.out.println("WF RANGED MODS "+what+" = "+actual);
    }
    private static void tick(int n) {
        for(int i=0;i<n;i++){
            world.getServer().getWorldData().overworldData().setGameTime(world.getGameTime()+1);
            TennoBallistics.tick(new PlayerTickEvent.Post(player));
            var stack=player.getMainHandItem();
            if(!stack.isEmpty())stack.getItem().inventoryTick(stack,world,player,0,true);
            TennoFlights.tick(new LevelTickEvent.Post(()->true,world));
        }
    }
    private static ItemStack gun(TennoGunSpec spec) {
        for (var mob:mobs) mob.discard();mobs.clear();
        var gun=new ItemStack(BuiltInRegistries.ITEM.get(TennoGunRegistration.id(spec.id())));
        player.setItemInHand(InteractionHand.MAIN_HAND,gun);tick(30);return gun;
    }
    private static Zombie target(double range) {
        var z=EntityType.ZOMBIE.create(world);
        z.setNoAi(true);z.setNoGravity(true);
        z.getAttribute(Attributes.MAX_HEALTH).setBaseValue(950);z.setHealth(950);
        z.getAttribute(Attributes.ARMOR).setBaseValue(0);
        z.moveTo(4.5,120,4.5+range,0,0);
        ensure(world.addFreshEntity(z),"target admitted");mobs.add(z);return z;
    }
    private static void mount(ItemStack stack,WeaponMod... mods){
        for(var m:mods)ensure(WeaponMods.install(player,stack,m),"install "+m);
    }
    private static void fire(int mode){TennoBallistics.action(player,mode,true);}
    private static float loss(Zombie target,int after){
        float before=target.getHealth();fire(0);tick(after);return before-target.getHealth();
    }
    private static void rifle(){
        var stock=gun(TennoGunSpec.BRATON);var mob=target(6);
        double basis=loss(mob,4);
        double critical=basis/TennoGunSpec.BRATON.damage();
        ensure(critical>=.99,"direct shot on bare mob "+critical);
        var fitted=gun(TennoGunSpec.BRATON);mount(fitted,SERRATION,FAST_HANDS,SPEED_TRIGGER,MAGAZINE_WARP);
        var stats=TennoGunStats.of(player,fitted);mob=target(6);
        approx(loss(mob,4),basis*1.6,"rank-five Serration damage");
        pass(stats.magazine()==67 && TennoGunItem.magazine(player,fitted)==44,"capacity 45→67 without granting free ammo");
        fire(1);tick(24);pass(TennoGunItem.magazine(player,fitted)==44,"modded reload stays pending at 24 ticks");
        tick(1);pass(TennoGunItem.magazine(player,fitted)==67,"modded reload finishes in 25 ticks");
        int rounds=67;
        for(int i=0;i<30;i++) {fire(0);tick(1);}
        int fired=rounds-TennoGunItem.magazine(player,fitted);
        pass(fired==15,"Speed Trigger fired 15 paid rounds in 30 ticks, stock would fire 10");
        var next=new FakePlayer(world,new GameProfile(UUID.randomUUID(),"Borrower"));
        pass(TennoGunStats.of(next,fitted).magazine()==45 && TennoGunItem.magazine(next,fitted)<=45,"borrower cannot inherit owner capacity");
        approx(TennoGunStats.of(next,fitted).damage(),TennoGunSpec.BRATON.damage(),"borrowed rifle's damage");
        fitted=gun(TennoGunSpec.BRATON);mount(fitted,VIGILANTE_ARMAMENTS);mob=target(6);
        boolean multiple=false,single=false;
        for(int i=0;i<32;i++){float hit=loss(mob,3);multiple|=Math.abs(hit-2*basis)<.015;single|=Math.abs(hit-basis)<.015;}
        pass(multiple&&single&&TennoGunItem.magazine(player,fitted)==13,"Vigilante added individual rays while charging one round per shot");
        fitted=gun(TennoGunSpec.PERIGALE);mount(fitted,VIGILANTE_ARMAMENTS);mob=target(6);
        for (int attempt=0;attempt<12;attempt++) {
            GunDataHelper.setLong(fitted,"wf_gale",0);
            GunDataHelper.setInt(fitted,"wf_mag",12);
            fire(0);tick(4);
            if(GunDataHelper.getLong(fitted,"wf_gale")>world.getGameTime())break;
            tick(20);
        }
        pass(GunDataHelper.getLong(fitted,"wf_gale")>world.getGameTime(),"extra multishot weak-point contacts count toward Gale Force within the same burst");
        fitted=gun(TennoGunSpec.BRATON);mount(fitted,HELLFIRE);mob=target(6);
        approx(loss(mob,3),basis*1.48,"Hellfire additional elemental damage");
        for(int i=0;i<24&&!mob.isOnFire();i++)loss(mob,3);
        pass(mob.getRemainingFireTicks()>=59,"Hellfire applied the three-second burn on a real target");
    }
    private static void shotgun(){
        var fitted=gun(TennoGunSpec.CODA_BUBONICO);
        mount(fitted,POINT_BLANK,AMMO_STOCK,TACTICAL_PUMP,CHILLING_GRASP);
        var stats=TennoGunStats.of(player,fitted);
        pass(stats.magazine()==58,"Ammo Stock scales Coda battery 36→58");
        GunDataHelper.setInt(fitted,"wf_mag",0);
        GunDataHelper.setLong(fitted,"wf_charge_at",world.getGameTime()+stats.rechargeTicks(30));
        tick(18);pass(TennoGunItem.magazine(player,fitted)==0,"battery recharge waits 19 ticks with Tactical Pump");
        tick(5);pass(TennoGunItem.magazine(player,fitted)==5,"Coda gains one charge per tick after the modded wait");
        tick(60);pass(TennoGunItem.magazine(player,fitted)==58,"battery fills its expanded capacity");
        var mob=target(4);
        fire(2);tick(12);
        pass(mob.getHealth()<930 && mob.hasEffect(MobEffects.WEAKNESS),"Coda alternate volley preserves innate Viral effect and scaled blast damage");
        for(int i=0;i<28 && !mob.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);i++)loss(mob,6);
        pass(mob.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"Chilling Grasp applies Slowness on pellet impact");
    }
    private static void projectiles(){
        var stock=gun(TennoGunSpec.CANTARE);
        var victim=target(8);float base=loss(victim,5);
        var fitted=gun(TennoGunSpec.CANTARE);mount(fitted,HORNET_STRIKE);victim=target(8);
        fire(0);ensure(WeaponMods.remove(fitted,HORNET_STRIKE),"remove while projectile is airborne");tick(5);
        approx(950-victim.getHealth(),base*1.6,"Cantare retains launch damage after socket removal");
        player.setPos(4.5,120,17);float before=victim.getHealth();fire(1);tick(6);
        approx(before-victim.getHealth(),base*2.1*1.6,"returning knife retains the original damage snapshot");
        player.setPos(4.5,120,4.5);
        var sec=gun(TennoGunSpec.KUVA_NUKOR);mount(sec,HORNET_STRIKE,HEATED_CHARGE);
        victim=target(4);var neighbor=target(6);fire(0);
        pass(neighbor.hasEffect(MobEffects.WEAKNESS)&&neighbor.getHealth()<946,"modded Kuva microwave chains while preserving Weakness");
        var spores=gun(TennoGunSpec.TORID);mount(spores,SERRATION);
        victim=target(5);fire(0);ensure(WeaponMods.remove(spores,SERRATION),"unmount Torid card");
        tick(5);float beforeDot=victim.getHealth();tick(12);
        approx(beforeDot-victim.getHealth(),1.3*1.6,"Torid field tick retains launch modifier");
    }
    @SubscribeEvent public static void test(ServerStartedEvent e) {
        world=e.getServer().overworld();world.getChunk(0,0);world.getChunk(0,1);
        player=new FakePlayer(world,new GameProfile(UUID.randomUUID(),"RangedMods"));
        player.setGameMode(GameType.SURVIVAL);player.setPos(4.5,120,4.5);player.setYRot(0);player.setXRot(0);world.addNewPlayer(player);
        player.giveExperienceLevels(1000);
        var pack=BuiltInRegistries.ITEM.get(TennoGunRegistration.id("mod_pack"));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(pack,WeaponMod.values().length));
        for (int i=0;i<WeaponMod.values().length;i++) ensure(pack.use(world,player,InteractionHand.MAIN_HAND).getResult().consumesAction(),"open pack");
        for(var mod:WeaponMod.values()){
            for(int r=0;r<mod.maxRank();r++)ensure(ModCollection.rankUp(player,mod.id(),r),"rank "+mod);
        }
        player.getRandom().setSeed(0x5746524dL);
        rifle();shotgun();projectiles();
        System.out.println("WF RANGED MODS PASS");
    }
}
