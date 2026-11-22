package ageless.validation;

import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Disposable test mod against a real Minecraft registry and 1.21.1 item component serializer. */
@EventBusSubscriber(modid="tenno_socket_check")
public final class ModSocketCheck {
    static ItemStack stack(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",id)).getDefaultInstance();
    }
    static FakePlayer player(ServerLevel level,String name) {
        return new FakePlayer(level,new GameProfile(UUID.randomUUID(),name));
    }
    static void unlock(FakePlayer p,WeaponMod mod,int rank) {
        var data=p.getPersistentData();var ledger=data.getCompound("AgelessWarframeMods");
        ledger.putInt(mod.id(),rank);data.put("AgelessWarframeMods",ledger);
    }
    static void ok(boolean cond,String message){if(!cond)throw new AssertionError(message);}
    static boolean close(double a,double b){return Math.abs(a-b)<.000001;}
    @SubscribeEvent public static void run(ServerStartedEvent event) {
        var world=event.getServer().overworld();var owner=player(world,"SocketQA1");var borrower=player(world,"SocketQA2");
        var braton=stack("braton");var baked=braton.copy();
        ok(WeaponMods.installed(braton).isEmpty()&&!WeaponMods.install(owner,braton,WeaponMod.SERRATION),"locked card refused");
        var name=Component.literal("Keep this inscription");braton.set(DataComponents.CUSTOM_NAME,name);
        CustomData.update(DataComponents.CUSTOM_DATA,braton,t->t.putInt("wf_mag",7));
        for(var card:WeaponMod.values())unlock(owner,card,0);
        ok(WeaponMod.values().length==22,"full catalogue");
        int entries=0,accepted=0;
        for(var item:TennoBlueprints.entries(owner)){
            var w=item.getDefaultInstance();
            ok(WeaponMods.installed(w).isEmpty(),"new stack default");
            for(var mod:WeaponMod.values()){
                boolean installed=WeaponMods.install(owner,w,mod);
                ok(installed==mod.fits(w),"compatibility on "+w+" for "+mod);
                if(installed){accepted++;ok(!WeaponMods.install(owner,w,mod),"duplicate refused");}
            }
            List<WeaponMod> saved=WeaponMods.installed(w);
            ok(saved.size()==5||saved.size()==6,"all fitting cards install");
            var disk=ItemStack.parseOptional(world.registryAccess(),(CompoundTag)w.save(world.registryAccess()));
            ok(saved.equals(WeaponMods.installed(disk)),"saved/loaded ordered sockets "+item);
            entries++;
        }
        ok(entries==33,"33 family stacks");
        System.out.println("SOCKET CHECK 33 physical weapons x 22 mod definitions: "+accepted+" type-compatible installs, other pairs rejected, duplicate exclusion, ordering and Minecraft serialization.");
        for(var target:List.of(new ItemStack(Items.IRON_SWORD),stack("ace_of_spades")))for(var mod:WeaponMod.values())
            ok(!mod.fits(target)&&!WeaponMods.install(owner,target,mod),"other game immunities");
        ok(!WeaponMod.POINT_BLANK.fits(stack("brakk"))&&WeaponMod.HORNET_STRIKE.fits(stack("brakk")),"shotgun sidearm");
        ok(WeaponMod.VIGILANTE_ARMAMENTS.fits(stack("bubonico"))&&WeaponMod.VIGILANTE_ARMAMENTS.fits(braton),"shared primary card");
        ok(WeaponMods.install(owner,braton,WeaponMod.SERRATION),"owner install");
        ok(WeaponMods.install(owner,baked,WeaponMod.SERRATION),"the same unlock can be installed on a second stack");
        ok(close(WeaponMods.bonus(owner,braton,ModStat.DAMAGE),.1),"unranked mod effective");
        ok(WeaponMods.installed(braton).contains(WeaponMod.SERRATION)&&WeaponMods.bonuses(borrower,braton).isEmpty(),"borrowed mod dormant");
        unlock(borrower,WeaponMod.SERRATION,2);
        ok(close(WeaponMods.bonus(borrower,braton,ModStat.DAMAGE),.3),"borrower's rank");
        unlock(owner,WeaponMod.SERRATION,5);
        ok(close(WeaponMods.bonus(owner,braton,ModStat.DAMAGE),.6)&&close(WeaponMods.bonus(owner,baked,ModStat.DAMAGE),.6),"all owned rank updates");
        // Creative commands or future versions may supply unknown keys. Preserve opaque IDs when editing.
        CustomData.update(DataComponents.CUSTOM_DATA,braton,t->{
            ListTag ids=t.getList("ageless_weapon_mods",Tag.TAG_STRING);
            ids.add(StringTag.valueOf("future_mod"));ids.add(StringTag.valueOf("serration"));ids.add(StringTag.valueOf("parry"));
        });
        ok(WeaponMods.installed(braton).equals(List.of(WeaponMod.SERRATION)),"malformed duplicate or improper class suppressed");
        ok(close(WeaponMods.bonus(owner,braton,ModStat.DAMAGE),.6),"malformed duplicate cannot increase damage");
        ok(WeaponMods.remove(braton,WeaponMod.SERRATION)&&!WeaponMods.remove(braton,WeaponMod.SERRATION),"remove all identical entries once");
        var remaining=braton.get(DataComponents.CUSTOM_DATA).copyTag();
        ok(remaining.getList("ageless_weapon_mods",Tag.TAG_STRING).contains(StringTag.valueOf("future_mod")),"unknown IDs preserved");
        ok(remaining.getInt("wf_mag")==7&&braton.getHoverName().equals(name),"existing magazine and anvil name preserved");
        ok(WeaponMods.installed(baked).size()==1,"second stack independent");
        ok(WeaponMods.remove(baked,WeaponMod.SERRATION)&&!baked.has(DataComponents.CUSTOM_DATA),"empty default restored");
        var pistol=stack("nukor");unlock(owner,WeaponMod.LETHAL_TORRENT,3);
        ok(WeaponMods.install(owner,pistol,WeaponMod.LETHAL_TORRENT),"multi-stat card");
        var snap=WeaponMods.bonuses(owner,pistol);
        WeaponMods.remove(pistol,WeaponMod.LETHAL_TORRENT);
        ok(close(snap.get(ModStat.FIRE_RATE),.24)&&close(snap.get(ModStat.MULTISHOT),.32),"launched snapshot remains immutable");
        ok(WeaponMods.bonuses(owner,pistol).isEmpty(),"next attack uses new loadout");
        System.out.println("SOCKET CHECK owner and borrower ranks, whole-collection updates, defensive NBT filtering, unknown-data preservation, multi-stat launch snapshots and independent removal.");
        System.out.println("SOCKET CHECK PASS");
    }
}
