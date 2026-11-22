package ageless.validation;

import com.jax.destiny2mc.tenno.mods.*;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Test-only event subscriber installed beside the release in a disposable dedicated server. */
@EventBusSubscriber(modid="mod_collection_check")
public final class ModCollectionCheck {
    private static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc",path);
    }
    private static void require(boolean ok,String description){
        if(!ok)throw new AssertionError(description);
    }
    private static void note(String message){System.out.println("MOD COLLECTION "+message);}
    private static Item pack(ModPacks.Pool pool){return BuiltInRegistries.ITEM.get(key(pool.id));}
    private static FakePlayer player(ServerLevel level){
        var p=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"PackTest")) {
            // Use the ordinary advancement manager; NeoForge normally discards all fake-player awards.
            @Override public boolean isFakePlayer() { return false; }
        };
        p.setGameMode(GameType.SURVIVAL);
        return p;
    }
    private static void verifyPackRecipes(ServerLevel level) {
        for(var pool:ModPacks.Pool.values()){
            var held=pack(pool).getDefaultInstance();
            require(held.is(TagKey.create(Registries.ITEM,key("warframe/mod_packs"))),pool+" tag");
            var mats=switch(pool){
                case ALL -> List.of(Items.PAPER,Items.PAPER,Items.COPPER_INGOT,Items.REDSTONE);
                case PRIMARY -> List.of(pack(ModPacks.Pool.ALL),Items.IRON_INGOT);
                case SECONDARY -> List.of(pack(ModPacks.Pool.ALL),Items.COPPER_INGOT);
                case MELEE -> List.of(pack(ModPacks.Pool.ALL),Items.FLINT);
            };
            var inputs=mats.stream().map(Item::getDefaultInstance).toList();
            var grid=CraftingInput.of(2,mats.size()/2,inputs);
            var recipe=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,level).orElseThrow();
            require(recipe.id().equals(key(pool.id)),pool+" matched wrong recipe");
            require(recipe.value().assemble(grid,level.registryAccess()).is(held.getItem()),pool+" craft output");
            require(level.getServer().getAdvancements().get(key("recipes/misc/"+pool.id))!=null,pool+" advancement");
        }
        note("All four recipes assemble through the real recipe manager; native recipe discoveries and pack tag present.");
    }
    private static void open(FakePlayer p, ModPacks.Pool pool, InteractionHand hand, boolean canUnlock){
        var i=pack(pool);
        var stack=new ItemStack(i,2);p.setItemInHand(hand,stack);
        var before=ModCollection.ranks(p);
        var result=stack.use(p.level(),p,hand);
        var after=ModCollection.ranks(p);
        require(result.getResult().consumesAction()==canUnlock,pool+" use result");
        require(stack.getCount()==(canUnlock?1:2),pool+" consumed correct number");
        require(after.size()==before.size()+(canUnlock?1:0),pool+" unique");
        for(var old:before.entrySet())require(old.getValue().equals(after.get(old.getKey())),pool+" existing rank preserved");
        for(String id:after.keySet())if(!before.containsKey(id))
            require(pool.includes(WeaponMod.byId(id)) && after.get(id)==0,pool+" initial rank and family");
    }

    @SubscribeEvent public static void run(ServerStartedEvent event) {
        var w=event.getServer().overworld();
        verifyPackRecipes(w);
        var p=player(w);
        var paper = new ItemStack(Items.PAPER);
        p.setItemInHand(InteractionHand.MAIN_HAND,paper);
        CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),paper);
        require(p.getRecipeBook().contains(key("mod_pack")),"paper reveals general recipe");
        require(!p.getRecipeBook().contains(key("melee_mod_pack")),"focused recipe waits for pack");
        var booster = new ItemStack(pack(ModPacks.Pool.ALL));
        p.setItemInHand(InteractionHand.MAIN_HAND,booster);
        CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),booster);
        for(var pool:ModPacks.Pool.values())
            require(p.getRecipeBook().contains(key(pool.id)),"pack reveals " + pool.id);
        note("Collecting paper then an unopened pack awarded all four recipe-book entries.");
        require(ModCollection.ranks(p).isEmpty(),"fresh save");
        p.giveExperienceLevels(7);
        require(!ModCollection.rankUp(p,"serration",0) && p.experienceLevel==7,"cannot pay to bypass pack unlock");
        for(var pool:List.of(ModPacks.Pool.PRIMARY,ModPacks.Pool.SECONDARY,ModPacks.Pool.MELEE)){
            long n=Arrays.stream(WeaponMod.values()).filter(pool::includes).count();
            require(n>0,pool+" nonempty pool");
            for(int i=0;i<n;i++)open(p,pool,i%2==0?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND,true);
            open(p,pool,InteractionHand.MAIN_HAND,false);
            note(pool+" pool dealt exactly "+n+" distinct eligible cards; both hands; completed pack preserved.");
        }
        require(ModCollection.ranks(p).size()==WeaponMod.values().length,"full set");
        open(p,ModPacks.Pool.ALL,InteractionHand.OFF_HAND,false);
        var other=player(w);
        open(other,ModPacks.Pool.ALL,InteractionHand.MAIN_HAND,true);
        require(ModCollection.ranks(other).size()==1 && ModCollection.ranks(p).size()>1,"player ownership isolated");
        other.setGameMode(GameType.SPECTATOR);
        open(other,ModPacks.Pool.ALL,InteractionHand.MAIN_HAND,false);
        require(!ModCollection.rankUp(other,ModCollection.ranks(other).keySet().iterator().next(),0),"spectator");
        note("General pack and spectator behavior; two player collections kept isolated.");

        var shortCard=WeaponMod.REACH;
        int max=shortCard.maxRank();
        p.experienceLevel=0;
        require(!ModCollection.rankUp(p,shortCard.id(),0),"XP required");
        p.giveExperienceLevels(33);
        for(int k=0;k<max;k++){
            int old=p.experienceLevel;
            require(ModCollection.rankUp(p,shortCard.id(),k),"legitimate upgrade to "+(k+1));
            require(p.experienceLevel==old-k-1,"rank price");
            require(!ModCollection.rankUp(p,shortCard.id(),k),"duplicate command refused");
            require(p.experienceLevel==old-k-1,"duplicate did not spend levels");
        }
        int capXp=p.experienceLevel;
        require(!ModCollection.rankUp(p,shortCard.id(),max)&&capXp==p.experienceLevel,"max rank");
        require(!ModCollection.rankUp(p,"unknown_card",0),"unknown card");
        require(!ModCollection.rankUp(p,"reach",-1),"negative rank");
        note("Rank 0 to "+max+" cost "+(max*(max+1)/2)+" Minecraft levels; unowned/unknown, spectator, stale, insufficient, and capped attempts rejected.");

        var clone=player(w);
        NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(clone,p,true));
        require(ModCollection.ranks(clone).equals(ModCollection.ranks(p)),"death clone copy");
        clone.giveExperienceLevels(19);
        require(ModCollection.rankUp(clone,"pressure_point",0) && ModCollection.rank(p,"pressure_point")==0,"independent deep copy");
        var saved=new CompoundTag();clone.saveWithoutId(saved);
        var relog=player(w);
        relog.load(saved);
        require(ModCollection.ranks(relog).equals(ModCollection.ranks(clone)),"save/load");
        var traveled=player(w);
        NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(traveled,relog,false));
        require(ModCollection.ranks(traveled).equals(ModCollection.ranks(relog)),"dimension clone copy");
        note("Native clone events and saved-player NBT round trip preserved all 22 cards and ranks with independent copies.");

        var packet=new ModCollectionSync(ModCollection.ranks(relog),"reach");
        var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),w.registryAccess());
        try {
            ModCollectionSync.CODEC.encode(buf,packet);
            require(packet.equals(ModCollectionSync.CODEC.decode(buf)),"wire snapshot/reveal round trip");
        } finally { buf.release(); }
        note("Owner-only full snapshot plus revealed-card ID survives network codec round-trip.");
        System.out.println("MOD COLLECTION COMPLETE");
    }
}
