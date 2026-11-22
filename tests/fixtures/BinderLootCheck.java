package ageless.validation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.util.*;
import com.jax.destiny2mc.binder.BinderContent;
@EventBusSubscriber(modid="binder_loot_check")
public final class BinderLootCheck {
 static LivingEntity observed;static int yield;
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void collect(LivingDropsEvent e){
  if(e.getEntity()==observed)yield=e.getDrops().stream().map(a->a.getItem()).filter(s->s.is(BuiltInRegistries.ITEM.get(BinderContent.MOTE))).mapToInt(ItemStack::getCount).sum();
 }
 @SubscribeEvent public static void verify(ServerStartedEvent e){
  var lv=e.getServer().overworld();lv.setChunkForced(0,0,true);lv.getChunk(0,0);
  var input=CraftingInput.of(3,3,List.of(ItemStack.EMPTY,new ItemStack(Items.AMETHYST_SHARD),ItemStack.EMPTY,new ItemStack(Items.COPPER_INGOT),new ItemStack(BuiltInRegistries.ITEM.get(BinderContent.MOTE)),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.SMOOTH_STONE),new ItemStack(Items.IRON_INGOT),new ItemStack(Items.SMOOTH_STONE)));
  var out=lv.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,lv).orElseThrow().value().assemble(input,lv.registryAccess());if(!out.is(BuiltInRegistries.ITEM.get(BinderContent.STATION)))throw new AssertionError("Wrong crafting result");
  for(var id:List.of("fallen_dreg","vex_goblin","hive_thrall","cabal_legionary","taken_thrall","scorn_stalker","dread_husk","devil_walker","dungeon_node","valus_ta_aurc","fallen_dreg")){
   var type=BuiltInRegistries.ENTITY_TYPE.get(BinderContent.id(id));observed=(LivingEntity)type.create(lv);observed.setPos(6,70,6);observed.tickCount=12;lv.addFreshEntity(observed);yield=-1;
   observed.hurt(lv.damageSources().genericKill(),99999);boolean loot=lv.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
   int expected= !loot || id.equals("dungeon_node") ? 0 : Set.of("valus_ta_aurc","devil_walker").contains(id) ? 6 : 1;
   // Puzzle nodes may suppress their entire death-drop event, as does doMobLoot=false.
   if(Math.max(yield,0)!=expected)throw new AssertionError(id+" motes="+yield);
   if(id.equals("valus_ta_aurc"))lv.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(false,e.getServer());
  }
  System.out.println("BINDER LOOT PASS: station crafted, seven faction deaths paid motes, field and encounter bosses each paid six, puzzle target paid zero, mob-loot gamerule respected.");
 }
}
