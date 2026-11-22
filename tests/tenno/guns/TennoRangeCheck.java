package ageless.rangetest;

import com.jax.destiny2mc.tenno.*;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.*;

/** A real NeoForge Overworld and its native entity collision, status, registry and damage
 * pipeline. The local test player faces south; ticks advance deterministically between triggers. */
@EventBusSubscriber(modid="tenno_range_check")
public class TennoRangeCheck {
    static int sequence;
    static ServerLevel w;
    static FakePlayer p;
    static final List<Zombie> targets=new ArrayList<>();
    static void ok(boolean x,String note){if(!x)throw new AssertionError(note);System.out.println("TENNO RANGE "+note);}
    static ItemStack arm(TennoGunSpec spec) {
        var s=new ItemStack(BuiltInRegistries.ITEM.get(TennoGunRegistration.id(spec.id())));
        p.setItemInHand(InteractionHand.MAIN_HAND,s);advance(25);
        return s;
    }
    static void action(int op){TennoBallistics.action(p,op,true);}
    static void advance(int n){
        for(int i=0;i<n;i++){
            w.getServer().getWorldData().overworldData().setGameTime(w.getGameTime()+1);
            TennoBallistics.tick(new PlayerTickEvent.Post(p));
            var stack=p.getMainHandItem();
            if(!stack.isEmpty())stack.getItem().inventoryTick(stack,w,p,0,true);
            TennoFlights.tick(new LevelTickEvent.Post(()->true,w));
        }
    }
    static Zombie target(double x,double z) {
        Zombie mob=EntityType.ZOMBIE.create(w);mob.setNoAi(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);mob.setHealth(500);
        mob.moveTo(x,120,z,0,0);
        if(!w.addFreshEntity(mob))throw new AssertionError("target admission");
        targets.add(mob);return mob;
    }
    static void reset(){
        for(var t:targets)t.discard();targets.clear();
        p.setYRot(0);p.setXRot(0);
    }
    static void setup(ServerLevel level) {
        w=level;w.getChunk(0,0);w.getChunk(0,1);
        p=new FakePlayer(w,new GameProfile(UUID.randomUUID(),"RangeCheck"));
        p.setGameMode(GameType.SURVIVAL);p.setPos(4.5,120,4.5);p.setYRot(0);p.setXRot(0);
        w.addNewPlayer(p);
    }
    @SubscribeEvent public static void run(ServerStartedEvent e){
        setup(e.getServer().overworld());
        for(var spec:TennoGunSpec.values()){
            reset();ItemStack stack=arm(spec);Zombie mob=target(4.5,10.5);
            // For arcing weapons at six blocks aim level; body occupies from 120 to 121.95.
            action(0);advance(8);
            ok(mob.getHealth()<500,spec.id()+" primary damages native zombie; remaining="+TennoGunItem.magazine(stack));
        }
        reset();var gun=arm(TennoGunSpec.BURSTON);target(4.5,10.5);action(0);
        int first=TennoGunItem.magazine(gun);
        advance(1);ok(TennoGunItem.magazine(gun)==first,"burst first round stands alone");
        advance(3);ok(TennoGunItem.magazine(gun)==first-2,"burst schedules next two rounds");
        action(1);advance(39);ok(TennoGunItem.magazine(gun)==42,"reload is timed");
        advance(1);ok(TennoGunItem.magazine(gun)==45,"reload refills magazine");
        reset();gun=arm(TennoGunSpec.KUVA_NUKOR);var a=target(4.5,10.5);var b=target(6.5,11);var c=target(8.5,11);
        action(0);ok(a.hasEffect(MobEffects.WEAKNESS)&&b.hasEffect(MobEffects.WEAKNESS)&&c.hasEffect(MobEffects.WEAKNESS),"Kuva beam chains to two neighbors");
        reset();gun=arm(TennoGunSpec.NUKOR);a=target(4.5,10.5);b=target(6.5,11);action(0);
        ok(a.hasEffect(MobEffects.WEAKNESS)&&b.getHealth()==500,"base Nukor remains single-target");
        reset();gun=arm(TennoGunSpec.CEDO_PRIME);a=target(4.5,10.5);b=target(6.5,11);action(2);advance(12);
        ok(a.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&b.isOnFire()&&TennoGunItem.magazine(gun)==40,"Cedo free glaive ricochets and primes distinct statuses");
        reset();gun=arm(TennoGunSpec.CODA_BUBONICO);a=target(4.5,10.5);action(2);advance(7);
        ok(TennoGunItem.magazine(gun)==27&&a.hasEffect(MobEffects.WEAKNESS),"Coda three viral salvos spend nine charge");advance(60);
        ok(TennoGunItem.magazine(gun)==36,"Coda recharges a depleted partial battery");
        reset();gun=arm(TennoGunSpec.CANTARE);a=target(4.5,10.5);action(0);advance(5);float before=a.getHealth();
        p.setPos(4.5,120,15); action(1);advance(10);
        ok(a.getHealth()<before,"Cantare blade crosses a target on its return");p.setPos(4.5,120,4.5);
        reset();gun=arm(TennoGunSpec.AKSONDOL);a=target(4.5,6.1);action(0);advance(5);action(0);advance(5);action(1);advance(52);
        ok(a.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&a.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==1,"Aksondol spent-round reload mist");
        reset();gun=arm(TennoGunSpec.PERIGALE);a=target(4.5,10.5);action(0);advance(15);
        ok(GunDataHelper.getLong(gun,"wf_gale")>w.getGameTime(),"Perigale four headshots grant Gale Force");
        int cost=TennoGunItem.magazine(gun);action(0);advance(7);ok(TennoGunItem.magazine(gun)==cost,"Gale Force waives next burst cost");
        reset();gun=arm(TennoGunSpec.AX_52);
        for(int i=0;i<4;i++){TennoBallistics.action(p,0,false);advance(3);}
        ok(TennoGunItem.magazine(gun)==39,"AX-52 four hip shots cost one round");
        reset();gun=arm(TennoGunSpec.BRATON);a=target(4.5,10.5);
        var block=new BlockPos(4,121,8);w.setBlockAndUpdate(block,Blocks.STONE.defaultBlockState());action(0);advance(4);
        ok(a.getHealth()==500,"solid cover stops hitscan");w.removeBlock(block,false);
        System.out.println("TENNO RANGED PASS");
    }
}
