package ageless.tennoqa;

import com.jax.destiny2mc.tenno.TennoBallistics;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Read the outcome from live entities. Projectiles navigate ordinary server ticks and the
 * real connected player's identity, following the same path as a player firing a weapon. */
@EventBusSubscriber(modid="ageless_tenno_qa")
public final class TennoRangeCheck {
    static ServerPlayer player;
    static int step,ticks;
    static List<Ravager> targets=new ArrayList<>();
    static Ravager a,b,c;
    static final int X=4,Y=184,Z=4;
    static void ok(boolean value,String msg){if(!value)throw new AssertionError(msg);System.out.println("TENNO RANGE "+msg);}
    static void next(){step++;ticks=0;}
    static void clean(){
        for(var mob:targets)mob.discard();targets.clear();
    }
    static Ravager target(double dx,double dz){
        var w=player.serverLevel();var mob=EntityType.RAVAGER.create(w);
        mob.setNoAi(true);mob.setNoGravity(true);mob.setPersistenceRequired();mob.setSilent(true);
        mob.moveTo(X+.5+dx,Y,Z+.5+dz,180,0);w.addFreshEntity(mob);targets.add(mob);
        return mob;
    }
    static void weapon(String name){
        var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",name));
        if(!(item instanceof TennoGunItem))throw new AssertionError("Missing item "+name);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
    }
    static void fire(int mode){TennoBallistics.action(player,mode,true);}
    static void entrance(boolean block){
        for(int x=-2;x<=2;x++)for(int y=0;y<4;y++)
            player.serverLevel().setBlockAndUpdate(new BlockPos(X+x,Y+y,Z+2),block?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("tennoqaprove").requires(s->s.hasPermission(4)).executes(ctx->{
            player=ctx.getSource().getPlayerOrException();step=ticks=0;
            var w=player.serverLevel();for(int x=-3;x<=6;x++)for(int z=-2;z<=12;z++)
                w.setBlockAndUpdate(new BlockPos(X+x,Y-1,Z+z),Blocks.STONE.defaultBlockState());
            entrance(false);player.connection.teleport(X+.5,Y,Z+.5,0,0);return 1;
        }));
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(player==null)return;
        ticks++;
        switch(step){
            case 0->{if(ticks<22)return;weapon("kuva_nukor");a=target(0,4.5);b=target(2.5,5);c=target(5,5);next();}
            case 1->{if(ticks<3)return;fire(0);
                ok(a.hasEffect(MobEffects.WEAKNESS)&&b.hasEffect(MobEffects.WEAKNESS)&&c.hasEffect(MobEffects.WEAKNESS),"Kuva Nukor weakens its first target and chains across two visible targets");
                float hp=a.getHealth();a.getPersistentData().putFloat("qa_hp",hp);a.removeAllEffects();entrance(true);next();}
            case 2->{if(ticks<8)return;fire(0);ok(a.getHealth()==a.getPersistentData().getFloat("qa_hp")&&!a.hasEffect(MobEffects.WEAKNESS),"solid block stops the Kuva Nukor ray");
                entrance(false);clean();weapon("cedo_prime");a=target(0,4.5);b=target(2.7,5);c=target(5.6,5);next();}
            case 3->{if(ticks<15)return;fire(2);next();}
            case 4->{if(ticks<22)return;
                ok(a.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&b.isOnFire()&&c.hasEffect(MobEffects.POISON),"Cedo Prime glaive struck three distinct enemies and applied successive elements");
                clean();weapon("torid");a=target(0,5);next();}
            case 5->{if(ticks<9)return;fire(0);next();}
            case 6->{if(ticks<8)return;ok(a.hasEffect(MobEffects.POISON),"Torid spore applies poison on impact");
                b=target(1.2,5.1);next();}
            case 7->{if(ticks<15)return;ok(b.hasEffect(MobEffects.POISON)&&b.getHealth()<b.getMaxHealth(),"surviving Torid cloud affects a target arriving after impact");
                clean();weapon("aksondol");a=target(0,5);next();}
            case 8->{if(ticks<15)return;fire(0);ok(a.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"Aksondol projectile applies cold");next();}
            case 9->{if(ticks<6)return;b=target(1.4,.2);fire(1);next();}
            case 10->{if(ticks<56)return;
                var slow=b.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                ok(slow!=null&&slow.getAmplifier()==1,"Aksondol completed reload creates a freezing field near the shooter");
                clean();weapon("cantare");a=target(0,8);next();}
            case 11->{if(ticks<10)return;fire(0);next();}
            case 12->{if(ticks<10)return;ok(a.getHealth()<a.getMaxHealth(),"Cantare knife reached the impact target");
                b=target(0,3.5);fire(1);next();}
            case 13->{if(ticks<12)return;ok(b.getHealth()<b.getMaxHealth(),"reloaded Cantare recalled its thrown knife across a new target");
                clean();weapon("perigale");player.connection.teleport(X+.5,Y,Z+.5,0,-6);a=target(0,5);a.setHealth(1);next();}
            case 14->{if(ticks<15)return;fire(0);next();}
            case 15->{if(ticks<12)return;
                var stack=player.getMainHandItem();var mod=(TennoGunItem)stack.getItem();
                ok(GunDataHelper.getLong(stack,"wf_gale")>player.level().getGameTime()&&TennoGunItem.magazine(stack)==mod.profile().magazine()-1,
                    "Perigale weak-point kill granted Gale Force and conserved ammunition for the remainder of its burst");
                clean();player.getInventory().setItem(8,new ItemStack(Items.EMERALD));player.inventoryMenu.broadcastChanges();
                System.out.println("TENNO RANGE COMPLETE");player=null;}
        }
    }
}
