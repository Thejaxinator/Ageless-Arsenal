package com.jax.destiny2mc.client.frontier;
import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.frontier.FrontierBallistics;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
public final class FrontierWeaponInput {
 private static int heldTicks,lastIndex=-1;private static boolean wasHeld;
 public static int charge;public static int duration;
 public static void tick(LocalPlayer p,ItemStack s,WeaponSpec w,boolean reloading,float ads){
  boolean held=Minecraft.getInstance().options.keyAttack.isDown();boolean pressed=held&&!wasHeld||ClientWeaponState.semiTriggerQueued;
  ClientWeaponState.semiTriggerQueued=false;
  if(lastIndex!=w.index){heldTicks=0;wasHeld=false;lastIndex=w.index;}
  int required=FrontierBallistics.chargeTicks(w,s);duration=required;
  if(reloading){heldTicks=0;charge=0;wasHeld=held;return;}
  if(required>0){
   if(held&&ClientWeaponState.fireCooldown<=0){
    if(heldTicks%4==0)ClientWeaponState.sendAction(3,p,ads>.55);
    heldTicks++;charge=Math.min(required,heldTicks);
    if(w.index!=51&&heldTicks>=required){ClientWeaponState.kickOnly(p,s,w,ads);ClientWeaponState.fireCooldown=8;heldTicks=0;charge=0;}
   }
   if(!held&&wasHeld){
    if(w.index==51&&heldTicks>=6){ClientWeaponState.fireSingleShotAndKick(p,s,w,ads);ClientWeaponState.fireCooldown=8;}
    else ClientWeaponState.sendAction(4,p,false);
    heldTicks=0;charge=0;
   }
  } else if((w.fullAuto?held:pressed)&&ClientWeaponState.fireCooldown<=0){
   if(GunDataHelper.getMag(s,w)<=0)ClientWeaponState.sendAction(1,p,false);
   else ClientWeaponState.fireSingleShotAndKick(p,s,w,ads);
   ClientWeaponState.fireCooldown=w.fireDelayTicks;charge=0;
  }
  wasHeld=held;ClientWeaponState.attackHeldLastTick=held;
 }
}
