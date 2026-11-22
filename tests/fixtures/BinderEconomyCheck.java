package ageless.validation;

import com.jax.destiny2mc.binder.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Disposable native server fixture. Requires a real registry, inventory, XP account and block state. */
@EventBusSubscriber(modid="binder_economy_test")
public final class BinderEconomyCheck {
    private static ResourceLocation key(String path){return ResourceLocation.fromNamespaceAndPath("destiny2mc",path);}
    private static void ok(boolean yes,String label){if(!yes)throw new IllegalStateException(label);}
    @SubscribeEvent public static void run(ServerStartedEvent event){
        var level=event.getServer().overworld();
        var p=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("805fb961-6ed9-4d19-ac5d-000000000001"),"BinderOwner"));
        var other=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("805fb961-6ed9-4d19-ac5d-000000000002"),"BinderClone"));
        var station=new BlockPos(8,70,8);
        level.setBlockAndUpdate(station,BuiltInRegistries.BLOCK.get(key("energy_binder")).defaultBlockState());
        p.setGameMode(GameType.SURVIVAL);p.setPos(8.5,70,10.5);
        List<String> build=List.of("marksmans_dodge","healing_rift","towering_barricade","solar_grenade","triple_jump","ember_of_solace");
        Item mote=BuiltInRegistries.ITEM.get(key("mote_of_light"));ok(mote!=Items.AIR,"mote registered");
        BinderAbility first=BinderCatalog.get(build.get(0));
        p.getInventory().setItem(0,new ItemStack(mote,first.motes()));p.experienceLevel=first.levels();
        BinderState.purchase(p,first.id());
        ok(BinderState.snapshot(p).unlocked().isEmpty(),"remote purchase rejected");
        BinderState.open(p,station);p.experienceLevel=first.levels()-1;BinderState.purchase(p,first.id());
        ok(BinderState.snapshot(p).motes()==first.motes()&&BinderState.snapshot(p).unlocked().isEmpty(),"no partial debit on missing XP");
        p.experienceLevel=first.levels();p.getInventory().getItem(0).shrink(1);BinderState.purchase(p,first.id());
        ok(p.experienceLevel==first.levels()&&BinderState.snapshot(p).unlocked().isEmpty(),"no partial debit on missing motes");
        for(int i=0;i<build.size();i++){
            String id=build.get(i);var a=BinderCatalog.get(id);ok(a!=null,"catalog "+id);
            p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(mote,a.motes()-1));
            p.getInventory().setItem(40,new ItemStack(mote,3));p.experienceLevel=a.levels()+2;
            BinderState.purchase(p,id);
            ok(BinderState.snapshot(p).motes()==2&&p.experienceLevel==2&&BinderState.snapshot(p).unlocked().contains(id),"purchase "+id);
            BinderState.purchase(p,id);ok(BinderState.snapshot(p).motes()==2&&p.experienceLevel==2,"duplicate charge");
            BinderState.equip(p,id,i);
        }
        var before=BinderState.snapshot(p);ok(before.active().equals(build.subList(0,4))&&before.passive().equals(build.subList(4,6)),"free class/grenade build");
        BinderState.equip(p,build.get(4),0);BinderState.equip(p,build.get(0),4);
        ok(BinderState.snapshot(p).equals(before),"category boundaries");
        ok(BinderState.cast(p,3)!=null,"valid grenade");var deadline=BinderState.snapshot(p).readyAt().get("solar_grenade");
        ok(deadline!=null&&deadline>level.getGameTime(),"successful cast recorded");
        BinderState.equip(p,"solar_grenade",0);
        ok(BinderState.cast(p,0)==null&&BinderState.snapshot(p).readyAt().get("solar_grenade").equals(deadline),"duplicate slot cooldown");
        BinderState.equip(p,"triple_jump",5);
        ok(BinderState.passives(p).equals(List.of("","triple_jump")),"duplicate passive moves");
        p.setPos(30,70,30);BinderState.equip(p,"",5);ok(BinderState.hasPassive(p,"triple_jump"),"remote swap rejected");
        p.setPos(8.5,70,10.5);level.setBlockAndUpdate(station,Blocks.AIR.defaultBlockState());BinderState.equip(p,"",5);
        ok(BinderState.hasPassive(p,"triple_jump"),"removed station rejected");
        ok(BinderState.snapshot(other).unlocked().isEmpty(),"other player isolation");
        BinderState.clone(new PlayerEvent.Clone(other,p,true));
        ok(BinderState.snapshot(other).active().equals(BinderState.snapshot(p).active())&&BinderState.hasPassive(other,"triple_jump")
                && BinderState.snapshot(other).unlocked().equals(BinderState.snapshot(p).unlocked())
                && BinderState.snapshot(other).readyAt().equals(BinderState.snapshot(p).readyAt()),"clone persistence");
        System.out.println("BINDER ECONOMY PASS: exact two-currency purchase across main/offhand, insufficient funds and XP, duplicate purchases, bench distance/removal, mixed class loadout, passive uniqueness, cooldown across duplicate slots, character isolation and death clone.");
    }
}
