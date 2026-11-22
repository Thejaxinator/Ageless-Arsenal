package ageless.validation;

import com.jax.destiny2mc.tenno.mods.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Separate test mod exercising authoritative edits through the exact command handler. */
@EventBusSubscriber(modid="mod_station_check")
public final class ModStationCheck {
    static ResourceLocation key(String n){return ResourceLocation.fromNamespaceAndPath("destiny2mc",n);}
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);System.out.println("MOD STATION "+s);}
    static ModBenchNetwork.Command cmd(long serial,long token,byte op,int slot,ResourceLocation id,String mod,int rank){
        return new ModBenchNetwork.Command(serial,token,op,slot,id,mod,rank);
    }
    @SubscribeEvent public static void test(ServerStartedEvent event){
        var world=event.getServer().overworld();
        var player=new FakePlayer(world,new GameProfile(UUID.randomUUID(),"FusionStation"));
        player.setGameMode(GameType.SURVIVAL);
        var pos=new BlockPos(8,65,8);
        player.setPos(8.5,65,8.5);
        // Obtain Serration using the real random source; each pool guarantees missing cards.
        var pack=BuiltInRegistries.ITEM.get(key("primary_mod_pack"));
        for(int n=0;n<11&&ModCollection.rank(player,"serration")<0;n++){
            player.setItemInHand(InteractionHand.MAIN_HAND,pack.getDefaultInstance());
            pack.use(world,player,InteractionHand.MAIN_HAND);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        player.giveExperienceLevels(10);
        var empty=cmd(1,0,ModBenchNetwork.BIND,-1,ModBenchNetwork.AIR,"",-1);
        check(ModBenchNetwork.handle(player,empty).status()==ModBenchNetwork.NO_STATION,"declines unopened station");
        var table=BuiltInRegistries.BLOCK.get(key("gunsmith_fui_workbench"));
        world.setBlockAndUpdate(pos,table.defaultBlockState());
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false)));
        var session=ModBenchNetwork.handle(player,empty);
        check(session.status()==ModBenchNetwork.OK&&session.token()!=0,"empty-handed collection session");
        var fuse=cmd(2,session.token(),ModBenchNetwork.FUSE,-1,ModBenchNetwork.AIR,"serration",0);
        check(ModBenchNetwork.handle(player,fuse).status()==ModBenchNetwork.OK&&ModCollection.rank(player,"serration")==1&&player.experienceLevel==9,"empty-handed fusion charges one XP level");
        check(ModBenchNetwork.handle(player,fuse).status()==ModBenchNetwork.STALE&&player.experienceLevel==9,"duplicate request cannot debit again");
        var braton=key("braton");var gun=BuiltInRegistries.ITEM.get(braton);
        var a=gun.getDefaultInstance();var b=gun.getDefaultInstance();
        player.getInventory().setItem(0,a);player.getInventory().setItem(1,b);
        var target=ModBenchNetwork.handle(player,cmd(3,0,ModBenchNetwork.BIND,0,braton,"",-1));
        check(target.status()==ModBenchNetwork.OK,"specific physical Braton bound");
        var install=cmd(4,target.token(),ModBenchNetwork.INSTALL,0,braton,"serration",1);
        player.getInventory().setItem(0,b);player.getInventory().setItem(1,a);
        check(ModBenchNetwork.handle(player,install).status()==ModBenchNetwork.STALE&&WeaponMods.installed(b).isEmpty(),"same-ID replacement stack protected");
        target=ModBenchNetwork.handle(player,cmd(5,0,ModBenchNetwork.BIND,0,braton,"",-1));
        check(ModBenchNetwork.handle(player,cmd(6,target.token(),ModBenchNetwork.INSTALL,0,braton,"serration",1)).status()==ModBenchNetwork.OK&&WeaponMods.installed(b).size()==1&&WeaponMods.installed(a).isEmpty(),"only selected Braton changed");
        check(ModBenchNetwork.handle(player,cmd(7,target.token(),ModBenchNetwork.FUSE,0,braton,"serration",0)).status()==ModBenchNetwork.DECLINED&&player.experienceLevel==9,"stale rank cannot spend XP");
        player.setPos(28,65,8);
        check(ModBenchNetwork.handle(player,cmd(8,target.token(),ModBenchNetwork.REMOVE,0,braton,"serration",1)).status()==ModBenchNetwork.NO_STATION&&WeaponMods.installed(b).size()==1,"out-of-reach removal declined");
        player.setPos(8.5,65,8.5);
        check(ModBenchNetwork.handle(player,cmd(9,target.token(),ModBenchNetwork.REMOVE,0,braton,"serration",1)).status()==ModBenchNetwork.OK&&WeaponMods.installed(b).isEmpty()&&ModCollection.rank(player,"serration")==1,"removal preserves permanent rank");
        check(ModBenchNetwork.handle(player,cmd(10,target.token(),ModBenchNetwork.CLOSE,0,braton,"",-1)).status()==ModBenchNetwork.OK&&ModBenchNetwork.handle(player,cmd(11,0,ModBenchNetwork.BIND,0,braton,"",-1)).status()==ModBenchNetwork.NO_STATION,"closing session revokes further edits and binds");
        System.out.println("MOD STATION COMPLETE");
    }
}
