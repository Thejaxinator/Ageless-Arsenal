package com.jax.destiny2mc.client;

import com.jax.destiny2mc.ageless.*;
import com.jax.destiny2mc.client.frontier.FrontierWeaponInput;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/** Sends intent only. Ammunition, charge deadlines and every round of a burst belong to the server. */
public final class ProgramInput {
    private static int previous=-1,slot=-1,heldTicks;
    private static long lastTick=Long.MIN_VALUE;
    private static boolean wasHeld;
    public static boolean tick(LocalPlayer p,ItemStack stack,WeaponSpec spec,boolean reloading,float aim){
        var program=WeaponPrograms.client(spec);
        if(program==null){previous=-1;return false;}
        long now=p.level().getGameTime();
        if(previous!=spec.index ||slot!=p.getInventory().selected||now-lastTick>1||now<lastTick){
            previous=spec.index;slot=p.getInventory().selected;heldTicks=0;wasHeld=false;
        }
        lastTick=now;
        boolean held=Minecraft.getInstance().options.keyAttack.isDown();
        boolean pressed=held&&!wasHeld||ClientWeaponState.semiTriggerQueued;
        ClientWeaponState.semiTriggerQueued=false;
        FrontierWeaponInput.duration=program.chargeTicks();
        if(reloading){heldTicks=0;}
        else if(program.chargeTicks()>0){
            if(held&&ClientWeaponState.fireCooldown<=0){
                // Refresh even after predicted completion. Network latency can delay the authoritative
                // start by a tick; the server clears the lease when the stack or slot changes.
                if(heldTicks%3==0)ClientWeaponState.sendAction(3,p,aim>.55);
                heldTicks++;
                if(program.trigger()==WeaponProgram.Trigger.CHARGE && heldTicks>=program.chargeTicks()){
                    ClientWeaponState.kickOnly(p,stack,spec,aim);
                    ClientWeaponState.fireCooldown=program.cooldown();
                    heldTicks=0;
                }
            }
            if(!held&&wasHeld){
                if(program.trigger()==WeaponProgram.Trigger.RELEASE&&heldTicks>=program.chargeTicks()){
                    ClientWeaponState.fireSingleShotAndKick(p,stack,spec,aim);
                    ClientWeaponState.fireCooldown=program.cooldown();
                } else ClientWeaponState.sendAction(4,p,false);
                heldTicks=0;
            }
        }else if((program.trigger()==WeaponProgram.Trigger.AUTOMATIC?held:pressed)&&ClientWeaponState.fireCooldown<=0){
            if(GunDataHelper.getMag(stack,spec)<program.cost())ClientWeaponState.sendAction(1,p,false);
            else ClientWeaponState.fireSingleShotAndKick(p,stack,spec,aim);
            ClientWeaponState.fireCooldown=program.cooldown();
        }
        FrontierWeaponInput.charge=Math.min(program.chargeTicks(),heldTicks);
        wasHeld=held;ClientWeaponState.attackHeldLastTick=held;
        return true;
    }
}
