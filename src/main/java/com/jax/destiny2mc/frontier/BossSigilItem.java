package com.jax.destiny2mc.frontier;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
public class BossSigilItem extends Item {
 public final String boss,name;public final boolean raid;
 public BossSigilItem(String id,String name,boolean raid){super(new Item.Properties().stacksTo(16).rarity(raid?Rarity.EPIC:Rarity.RARE));boss=id;this.name=name;this.raid=raid;}
 public static String legacyName(String s){return switch(s){case "sepiks_prime"->"Sepiks Prime";case "protheon_boss"->"Protheon, Modular Mind";case "oryx_boss"->"Oryx, the Taken King";default->"Atheon, Time's Conflux";};}
 public static boolean isEncounter(LivingEntity e){return e.getPersistentData().getBoolean("frontier_encounter")||java.util.Set.of("sepiks_prime","protheon_boss","oryx_boss","atheon_boss").contains(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath())&&BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("destiny2mc");}
 @Override public InteractionResult useOn(UseOnContext c){
  if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
  Player p=c.getPlayer();if(p==null||p.isSpectator())return InteractionResult.FAIL;
  ServerLevel l=(ServerLevel)c.getLevel();BlockPos at=c.getClickedPos().above();
  if(p.getCooldowns().isOnCooldown(this))return InteractionResult.FAIL;
  AABB area=new AABB(at).inflate(64);
  boolean busy=!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&(e instanceof FrontierMob m&&m.unit.boss()||isEncounter(e))).isEmpty();
  if(busy){p.displayClientMessage(Component.literal("An active boss is already nearby. Finish that encounter first."),true);return InteractionResult.FAIL;}
  EntityType<?> type=BuiltInRegistries.ENTITY_TYPE.get(FrontierContent.id(boss));Entity entity=type.create(l);
  if(!(entity instanceof Mob mob))return InteractionResult.FAIL;
  mob.moveTo(at.getX()+.5,at.getY()+.05,at.getZ()+.5,p.getYRot()+180,0);
  if(!l.getWorldBorder().isWithinBounds(at)||!l.noCollision(mob,mob.getBoundingBox().inflate(3,0,3).expandTowards(0,1,0))||!l.getBlockState(at.below()).isSolidRender(l,at.below())){
   p.displayClientMessage(Component.literal("Find open ground with at least eight blocks of clear space around the summon point."),true);return InteractionResult.FAIL;
  }
  int players=l.getEntitiesOfClass(Player.class,new AABB(at).inflate(32),v->!v.isSpectator()).size();
  double scale=1+Math.min(3,Math.max(0,players-1))*.45;
  var health=mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);if(health!=null){double total=health.getBaseValue()*scale;health.setBaseValue(Math.min(1024,total));mob.setHealth(mob.getMaxHealth());mob.getPersistentData().putFloat("frontier_damage_scale",(float)(total/mob.getMaxHealth()));}
  mob.setPersistenceRequired();mob.setCustomName(Component.literal(name));mob.getPersistentData().putBoolean("frontier_encounter",true);mob.getPersistentData().putString("frontier_boss_id",boss);
  if(!p.isCreative())mob.setTarget(p);
  if(!l.addFreshEntity(mob))return InteractionResult.FAIL;
  if(!p.isCreative())c.getItemInHand().shrink(1);p.getCooldowns().addCooldown(this,100);
  p.displayClientMessage(Component.literal(name+" has arrived.").withStyle(ChatFormatting.GOLD),false);return InteractionResult.CONSUME;
 }
 public static Item recipeCenter(String id){return switch(id){case "sepiks_prime"->Items.ENDER_PEARL;case "protheon_boss","taniks"->Items.REDSTONE_BLOCK;case "valus_ta_aurc"->Items.IRON_BLOCK;case "bracus_zahn"->Items.SPYGLASS;case "alak_hul"->Items.IRON_AXE;case "hashladun"->Items.GHAST_TEAR;case "fikrul"->Items.BLAZE_ROD;case "crota"->Items.DIAMOND_SWORD;case "templar"->Items.ENDER_EYE;case "golgoroth"->Items.SPIDER_EYE;case "rhulk"->Items.TRIDENT;case "nezarec"->Items.NETHER_STAR;case "oryx_boss"->Items.WITHER_SKELETON_SKULL;default->Items.CLOCK;};}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){out.add(Component.literal((raid?"Raid boss challenge: ":"Strike boss challenge: ")+name).withStyle(ChatFormatting.GOLD));out.add(Component.literal("Craft with four "+(raid?"Conquest Tokens":"faction materials")+" around one "+recipeCenter(boss).getDescription().getString()+"."));out.add(Component.literal("Use on open ground. Consumed on success; nearby players scale boss health."));out.add(Component.literal("Fight in the current world. No mission, portal or terrain destruction."));}
}
