package com.jax.destiny2mc.binder.client;

import com.jax.destiny2mc.binder.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Vanilla Jump remains rebindable and the first jump remains vanilla. Only an
 * additional airborne press is sent. Opening a menu releases a sustained style. */
@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class GuardianJumpInput {
    private static LocalPlayer previous;
    private static boolean held,sent;
    @SubscribeEvent public static void tick(ClientTickEvent.Pre event){
        Minecraft mc=Minecraft.getInstance();
        var p=mc.player;
        // Read before vanilla launches the ground jump. Reading after local physics
        // can misclassify the initial press as the first airborne charge.
        boolean down=p!=null&&mc.screen==null&&mc.options.keyJump.isDown();
        if(previous!=p){previous=p;held=down;sent=false;return;}
        if(p!=null && down!=held){
            if(!down&&sent){PacketDistributor.sendToServer(new JumpIntent(false,(byte)0,(byte)0));sent=false;}
            else if(down && !p.onGround() && !p.getAbilities().flying && BinderClientState.snapshot().passive().stream().anyMatch(PassiveAbilities::movement)){
                byte f=(byte)((mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0));
                byte l=(byte)((mc.options.keyLeft.isDown()?1:0)-(mc.options.keyRight.isDown()?1:0));
                PacketDistributor.sendToServer(new JumpIntent(true,f,l));sent=true;
            }
        }
        held=down;
    }
}
