package ageless.validation;

import com.jax.destiny2mc.tenno.TennoGunStats;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.ModCollection;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid="arsenal_transfer_check")
public final class ModTransferCheck {
    static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",id));}
    static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);}
    @SubscribeEvent public static void verify(ServerStartedEvent e){
        var level=e.getServer().overworld();
        var owner=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CardOwner"));
        var friend=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CardFriend"));
        owner.setGameMode(GameType.SURVIVAL);friend.setGameMode(GameType.SURVIVAL);
        var first = item("braton").getDefaultInstance();
        var second = item("braton").getDefaultInstance();
        var serration=WeaponMod.byId("serration");
        check(!WeaponMods.install(owner,first,serration),"locked");
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item("primary_mod_pack"),11));
        for(int i=0;i<11 && ModCollection.rank(owner,"serration")<0;i++)
            item("primary_mod_pack").use(level,owner,InteractionHand.MAIN_HAND);
        check(WeaponMods.install(owner,first,serration)&&WeaponMods.install(owner,second,serration),"two physical rifles reuse card");
        check(!WeaponMods.install(owner,first,serration),"duplicate on one rifle");
        double base=TennoGunStats.of(friend,first).damage();
        double rankZero=TennoGunStats.of(owner,first).damage();
        owner.giveExperienceLevels(2);
        check(ModCollection.rankUp(owner,"serration",0)&&owner.experienceLevel==1,"one level spent");
        check(TennoGunStats.of(owner,second).damage()>rankZero && rankZero>base,"both builds share rank");
        var saved=(CompoundTag)first.save(level.registryAccess());
        var loaded=ItemStack.parseOptional(level.registryAccess(),saved);
        check(WeaponMods.installed(loaded).contains(serration),"saved socket ID");
        check(TennoGunStats.of(friend,loaded).damage()==base && TennoGunStats.of(owner,loaded).damage()>rankZero,"gift keeps socket, active rank belongs to holder");
        owner.getInventory().clearContent();owner.setItemInHand(InteractionHand.MAIN_HAND,loaded);
        check(TennoBlueprints.available(owner,item("braton"))==0,"modded item protected from variant recipe");
        check(WeaponMods.remove(loaded,serration) && TennoBlueprints.available(owner,item("braton"))==1,"stripped weapon available for upgrade");
        check(ModCollection.rank(owner,"serration")==1 && WeaponMods.installed(second).contains(serration),"unslotting preserves unlock and second build");
        System.out.printf(java.util.Locale.ROOT,"TRANSFER PASS stock %.2f HP; rank 0 %.2f HP; rank 1 %.2f HP. Save/load, reuse, trade and Foundry payment verified.%n",base,rankZero,TennoGunStats.of(owner,second).damage());
    }
}
