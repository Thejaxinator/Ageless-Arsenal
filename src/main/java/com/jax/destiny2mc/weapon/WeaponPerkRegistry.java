package com.jax.destiny2mc.weapon;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import com.jax.destiny2mc.item.DestinyGunItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Six validated, persistent sockets. Existing four-slot saves retain their rolls. */
public class WeaponPerkRegistry {
 public static final int SLOT_COUNT=6;
 public static final int[] POOLS=LegacyWeaponPerks.POOLS;
 public static boolean jsonLoaded=true;
 private static final String[] TITLES={"BARREL","MAGAZINE","TRAIT 1","TRAIT 2","MASTERWORK","MOD"};
 public static void ensureJsonLoaded(){LegacyWeaponPerks.ensureJsonLoaded();}
 public static String getSlotKey(int s){return "d2_pk"+s;}
 public static String getSlotTitle(int s){return s>=0&&s<6?TITLES[s]:"UNKNOWN";}
 public static int optionCount(int slot){return slot>=4&&slot<6?5:slot>=0&&slot<4?4:0;}
 public static int getPoolOption(int w,int s,int o){
  if(w<0||w>=WeaponSpec.ALL.length||s<0||s>=6||o<0||o>=optionCount(s))return 0;
  return s<4?LegacyWeaponPerks.getPoolOption(w,s,o):(s==4?500:600)+o;
 }
 public static boolean isValidPerkForWeapon(int w,int s,int p){
  if(w<0||w>=WeaponSpec.ALL.length||s<0||s>=6)return false;
  for(int i=0;i<optionCount(s);i++)if(getPoolOption(w,s,i)==p)return true;
  return false;
 }
 public static int getActivePerk(ItemStack st,WeaponSpec sp,int s){
  int def=getPoolOption(sp.index,s,0);
  int v=st==null?def:GunDataHelper.getInt(st,getSlotKey(s),def);
  return isValidPerkForWeapon(sp.index,s,v)?v:def;
 }
 public static void setActivePerk(ItemStack st,WeaponSpec sp,int s,int p){
  if(st==null||!isValidPerkForWeapon(sp.index,s,p))return;
  GunDataHelper.setInt(st,getSlotKey(s),p);
  int mag=GunDataHelper.getInt(st,"d2_mag",sp.magSize);
  GunDataHelper.setInt(st,"d2_mag",Math.max(0,Math.min(mag,getEffectiveMagSize(st,sp))));
 }
 public static String getPerkName(int p){return switch(p){
  case 500->"Factory finish";case 501->"Range masterwork";case 502->"Stability masterwork";
  case 503->"Reload masterwork";case 504->"Accuracy masterwork";
  case 600->"Empty mod socket";case 601->"Targeting adjuster";case 602->"Backup mag";
  case 603->"Counterbalance stock";case 604->"Major spec";
  default->LegacyWeaponPerks.getPerkName(p);};}
 public static String getPerkDesc(int p){return switch(p){
  case 500,600->"No additional modifier. Existing weapon traits remain active.";
  case 501->"Increases effective range by 10%.";
  case 502->"Reduces vertical and horizontal recoil by 12%.";
  case 503->"Reduces reload time by 12%.";
  case 504->"Reduces hip-fire and aimed spread by 12%.";
  case 601->"Reduces aimed spread by a further 15%.";
  case 602->"Adds 15% magazine capacity, rounded up; at least one round.";
  case 603->"Reduces horizontal recoil by a further 20%.";
  case 604->"Deals 8% more damage to targets with at least 40 maximum health.";
  default->LegacyWeaponPerks.getPerkDesc(p);};}
 private static boolean has(ItemStack s,WeaponSpec w,int p){return getActivePerk(s,w,p<600?4:5)==p;}
 public static int getEffectiveMagSize(ItemStack s,WeaponSpec w){int n=LegacyWeaponPerks.getEffectiveMagSize(s,w);return n+(has(s,w,602)?Math.max(1,(int)Math.ceil(n*.15)):0);}
 public static float getEffectiveRange(ItemStack s,WeaponSpec w,long t){return LegacyWeaponPerks.getEffectiveRange(s,w,t)*(has(s,w,501)?1.1f:1);}
 public static float getEffectiveSpread(ItemStack s,WeaponSpec w,int ads,long t){return LegacyWeaponPerks.getEffectiveSpread(s,w,ads,t)*(has(s,w,504)?.88f:1)*(ads!=0&&has(s,w,601)?.85f:1);}
 public static float getRecoilPitchMult(ItemStack s,WeaponSpec w){return LegacyWeaponPerks.getRecoilPitchMult(s,w)*(has(s,w,502)?.88f:1);}
 public static float getRecoilYawMult(ItemStack s,WeaponSpec w){return LegacyWeaponPerks.getRecoilYawMult(s,w)*(has(s,w,502)?.88f:1)*(has(s,w,603)?.8f:1);}
 public static int getEffectiveReloadTicks(ItemStack s,WeaponSpec w,long t){return Math.max(1,Math.round(LegacyWeaponPerks.getEffectiveReloadTicks(s,w,t)*(has(s,w,503)?.88f:1)));}
 public static float computeSwappableDamageMult(ItemStack s,WeaponSpec w,LivingEntity e,int crit,long t){return LegacyWeaponPerks.computeSwappableDamageMult(s,w,e,crit,t)*(e!=null&&e.getMaxHealth()>=40&&has(s,w,604)?1.08f:1);}
 public static void onSwappableReloadComplete(ItemStack s,WeaponSpec w,long t){LegacyWeaponPerks.onSwappableReloadComplete(s,w,t);}
 public static void onSwappableWallHit(ServerLevel l,ServerPlayer p,ItemStack s,WeaponSpec w,Vec3 v){LegacyWeaponPerks.onSwappableWallHit(l,p,s,w,v);}
 public static void onSwappablePostHit(ServerLevel l,ServerPlayer p,ItemStack s,WeaponSpec w,LivingEntity e,Vec3 v,int c,int k,long t){LegacyWeaponPerks.onSwappablePostHit(l,p,s,w,e,v,c,k,t);}
 public static void appendPerkTooltips(ItemStack s,WeaponSpec w,List<Component> out){
  for(int slot=0;slot<6;slot++)out.add(Component.literal(getSlotTitle(slot)+": "+getPerkName(getActivePerk(s,w,slot))).withStyle(slot<2?ChatFormatting.GRAY:slot<4?ChatFormatting.YELLOW:ChatFormatting.AQUA));
 }
 public static void serverTrySocketPerk(ServerPlayer p,int inventorySlot,int socket,int perk){
  if(p.isSpectator()||inventorySlot<0||inventorySlot>=p.getInventory().getContainerSize())return;
  ItemStack s=p.getInventory().getItem(inventorySlot);
  if(s.isEmpty()||!(s.getItem() instanceof DestinyGunItem gun))return;
  WeaponSpec w=gun.spec;
  if(!isValidPerkForWeapon(w.index,socket,perk)||getActivePerk(s,w,socket)==perk)return;
  Item alloy=(Item)ModRegistries.PLUNDERED_ETHER_ALLOY.get();
  if(!p.isCreative()){
   if(GunsmithRecipes.countInInventory(p,alloy)<1||GunsmithRecipes.countInInventory(p,Items.COPPER_INGOT)<2){
    p.displayClientMessage(Component.literal("Requires 1 Ether Alloy and 2 Copper Ingots."),true);return;
   }
   GunsmithRecipes.consumeFromInventory(p,alloy,1);GunsmithRecipes.consumeFromInventory(p,Items.COPPER_INGOT,2);
  }
  setActivePerk(s,w,socket,perk);p.getInventory().setChanged();p.containerMenu.broadcastChanges();
  ServerBallisticsEngine.playSound(p.level(),p.getX(),p.getY(),p.getZ(),"block.smithing_table.use",.9f,1.35f);
  p.displayClientMessage(Component.literal("Equipped "+getPerkName(perk)),true);
 }
}
