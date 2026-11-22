package ageless.validation.passive;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import com.jax.destiny2mc.binder.BinderState;
import com.jax.destiny2mc.binder.JumpIntent;
import com.jax.destiny2mc.binder.PassiveAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Isolated native test fixture; keep it out of normal mod builds. */
@EventBusSubscriber(modid="binder_passive_check")
public final class PassiveCheck {
    static ServerLevel level;
    static FakePlayer player;
    static boolean done;
    static int age;
    static final BlockPos station=new BlockPos(16,110,16);
    static void check(boolean ok,String what){if(!ok)throw new AssertionError(what);System.out.println("PASSIVE CHECK "+what);}
    static void atBinder(){
        player.setPos(station.getX()+1.5,station.getY(),station.getZ()+.5);
        player.setOnGround(true);
        PassiveAbilities.update(new PlayerTickEvent.Post(player));
        BinderState.open(player,station);
    }
    static void equip(String left,String right){
        atBinder();BinderState.equip(player,left,4);BinderState.equip(player,right,5);
        check(BinderState.passives(player).equals(java.util.List.of(left,right)),"equipped "+left+" and "+right);
    }
    static void airborne(){player.setPos(station.getX()+1.5,station.getY()+4,station.getZ()+.5);player.setOnGround(false);player.setDeltaMovement(Vec3.ZERO);}
    static void press(){new JumpIntent(true,(byte)1,(byte)0).apply(player);}
    static void release(){new JumpIntent(false,(byte)0,(byte)0).apply(player);}
    static void sample(){PassiveAbilities.update(new PlayerTickEvent.Post(player));}
    static Zombie foe(){
        var z=EntityType.ZOMBIE.create(level);z.setPos(player.getX()+2,player.getY(),player.getZ());
        z.setNoAi(true);z.setNoGravity(true);level.addFreshEntity(z);return z;
    }
    @SubscribeEvent public static void setup(ServerStartedEvent e){
        level=e.getServer().overworld();player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("aceabeef-0000-4000-8000-00000000bb12"),"PassiveTest"));
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(done||player==null)return;
        try{
            switch(++age){
                case 2 -> {
                    level.getChunkAt(station);level.setBlockAndUpdate(station.below().east(),Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(station,BuiltInRegistries.BLOCK.get(ResourceLocation.parse("destiny2mc:energy_binder")).defaultBlockState());
                    player.setGameMode(GameType.SURVIVAL);player.giveExperienceLevels(100);
                    var mote=BuiltInRegistries.ITEM.get(ResourceLocation.parse("destiny2mc:mote_of_light"));
                    for(int i=0;i<4;i++)player.getInventory().add(new ItemStack(mote,64));
                    atBinder();for(var def:PassiveAbilities.catalog())BinderState.purchase(player,def.id());
                    check(BinderState.snapshot(player).unlocked().size()==8,"eight bought from the actual block");
                    equip("triple_jump","strafe_lift");press();check(player.getDeltaMovement().y==0,"ground tap spends no charge");
                    airborne();press();check(player.getDeltaMovement().y==.45,"first airborne tap uses Hunter impulse");
                    player.setDeltaMovement(Vec3.ZERO);press();check(player.getDeltaMovement()==Vec3.ZERO,"repeat press suppressed");release();
                }
                case 6 -> {press();check(player.getDeltaMovement().y==.32,"second style follows slot order");}
                case 7 -> {sample();check(Math.abs(player.getDeltaMovement().y-.25)<.001,"lift held");release();}
                case 10 -> {press();check(player.getDeltaMovement().y==.45,"Hunter second extra jump");release();}
                case 14 -> {
                    player.setDeltaMovement(Vec3.ZERO);press();check(player.getDeltaMovement()==Vec3.ZERO,"airborne budget exhausted");
                    equip("burst_glide","spark_of_haste");airborne();press();check(player.getDeltaMovement().horizontalDistance()>.47,"Warlock forward impulse");
                }
                case 15 -> {sample();check(Math.abs(player.getDeltaMovement().y+.035)<.001,"glide descent");release();}
                case 16 -> {
                    var motion=player.getDeltaMovement();sample();check(player.getDeltaMovement().equals(motion),"releasing Jump ends glide");
                    player.setSprinting(true);
                    for(int i=0;i<20;i++)sample();check(player.hasEffect(MobEffects.MOVEMENT_SPEED),"Arc sprint buff");
                    player.setSprinting(false);equip("ember_of_solace","echo_of_reprisal");
                    player.setShiftKeyDown(true);player.setHealth(8);
                    Zombie victim=foe(),watcher=foe();watcher.setTarget(player);
                    victim.setRemainingFireTicks(60);victim.hurt(player.damageSources().playerAttack(player),500);
                    check(player.getHealth()==11f,"Solar kill restores three health");
                    check(watcher.getTarget()==null && player.hasEffect(MobEffects.INVISIBILITY),"Void kill clears target and cloaks");
                    player.setShiftKeyDown(false);
                    equip("whisper_of_chains","thread_of_warding");watcher.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80));
                    player.tickCount=100;sample();check(player.hasEffect(MobEffects.DAMAGE_RESISTANCE),"Stasis detects slowed hostile");
                    var orb=new ExperienceOrb(level,player.getX(),player.getY(),player.getZ(),3);level.addFreshEntity(orb);orb.playerTouch(player);
                    check(player.hasEffect(MobEffects.ABSORPTION)&&player.getAbsorptionAmount()==4,"Strand XP orb grants two shield hearts");
                    done=true;System.out.println("PASSIVE CHECK COMPLETE");
                }
            }
        }catch(Throwable err){done=true;System.out.println("PASSIVE CHECK FAILED "+err);err.printStackTrace();}
    }
}
