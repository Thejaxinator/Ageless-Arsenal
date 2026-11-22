package com.jax.destiny2mc.client;

import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.overdrive.AimIntent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class ClientAimIntent {
    private static LocalPlayer previous;
    private static int weapon=-1,lastTick;
    private static boolean sent;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var player=mc.player;
        if(player!=previous){previous=player;weapon=-1;sent=false;}
        if(player==null||!(player.getMainHandItem().getItem() instanceof DestinyGunItem gun)){
            weapon=-1;sent=false;return;
        }
        boolean held=mc.screen==null&&mc.options.keyUse.isDown()&&ClientWeaponState.ads>.2f&&
            !player.isSpectator()&&player.isAlive();
        if(weapon!=gun.spec.index){weapon=gun.spec.index;sent=false;}
        if(held!=sent||(held&&player.tickCount-lastTick>=12)){
            PacketDistributor.sendToServer(new AimIntent(weapon,held));lastTick=player.tickCount;sent=held;
        }
    }
}
