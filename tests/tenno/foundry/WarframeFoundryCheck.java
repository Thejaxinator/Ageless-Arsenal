package ageless.validation;

import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.TennoGunItem;
import com.jax.destiny2mc.tenno.melee.TennoMeleeItem;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Isolated test mod running inside the real recipe and tag managers. */
@EventBusSubscriber(modid="warframe_foundry_check")
public final class WarframeFoundryCheck {
    static ResourceLocation id(String id){return ResourceLocation.fromNamespaceAndPath("destiny2mc",id);}
    static Item item(String id){return BuiltInRegistries.ITEM.get(id(id));}
    static TagKey<Item> tag(String path){return TagKey.create(Registries.ITEM,id("warframe/"+path));}
    static void check(boolean condition,String detail){if(!condition)throw new AssertionError(detail);}
    @SubscribeEvent public static void verify(ServerStartedEvent e){
        var p=new FakePlayer(e.getServer().overworld(),new GameProfile(UUID.randomUUID(),"FoundryQA"));
        p.setGameMode(GameType.SURVIVAL);
        List<Item> items=TennoBlueprints.entries(p);
        check(items.size()==33 && TennoBlueprints.entries().size()==33,"all 33 live recipes and registrations");
        int guns=0,blades=0;
        for(Item result:items){
            var stack=result.getDefaultInstance();
            check(stack.is(tag("weapons")),"family tag "+result);
            check(!TennoBlueprints.canCraft(p,result) && !TennoBlueprints.craft(p,result),"empty inventory "+result);
            var costs=TennoBlueprints.cost(p,result);
            check(costs.size()==3,"cost count "+result);
            for(var cost:costs){
                // Install separately even for the two unstackable Grakatas.
                for(int n=0;n<cost.getCount();n++) check(p.addItem(new ItemStack(cost.getItem())),"insert ingredient");
            }
            check(TennoBlueprints.craft(p,result) && TennoBlueprints.available(p,result)==1,"actual survival craft "+result);
            for(var cost:costs)check(TennoBlueprints.available(p,cost.getItem())==0,"debit exactly");
            if(result instanceof TennoGunItem) {guns++;check(stack.is(tag("ranged"))&&!stack.is(tag("melee")),"ranged tag");}
            if(result instanceof TennoMeleeItem) {blades++;check(stack.is(tag("melee"))&&!stack.is(tag("ranged")),"melee tag");}
            p.getInventory().clearContent();
        }
        check(guns==27 && blades==6,"roster totals");
        check(!TennoBlueprints.isTenno(Items.IRON_SWORD) && !TennoBlueprints.canCraft(p,Items.IRON_SWORD),"non-family");
        var grakata=item("grakata");var twins=item("twin_grakatas");
        for(var cost:TennoBlueprints.cost(p,twins))
            if(cost.getItem()!=grakata)p.addItem(cost.copy());
        var named=new ItemStack(grakata);named.set(DataComponents.CUSTOM_NAME,Component.literal("Clem's keepsake"));
        p.addItem(named.copy());p.addItem(new ItemStack(grakata));p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(grakata));
        check(TennoBlueprints.available(p,grakata)==1 && !TennoBlueprints.craft(p,twins),"named and offhand excluded");
        check(TennoBlueprints.available(p,Items.LEATHER)==2,"decline is side-effect free");
        p.addItem(new ItemStack(grakata));
        check(TennoBlueprints.craft(p,twins),"two paid Grakatas accepted");
        check(p.getOffhandItem().is(grakata)&&p.getInventory().items.stream().anyMatch(s->s.has(DataComponents.CUSTOM_NAME)),"protected gear preserved");
        check(TennoBlueprints.available(p,twins)==1 && !TennoBlueprints.craft(p,twins),"repeat cannot create items");
        System.out.println("TENNO FOUNDRY PASS: 33 exact survival crafts, 27 ranged, 6 melee, both Grakatas consumed; family tags, insufficient funds, protected stacks and repeat safe.");
    }
}
