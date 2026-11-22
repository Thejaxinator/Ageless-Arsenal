package com.jax.destiny2mc.binder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Only an acknowledged server response changes the displayed ownership, slots or cooldowns. */
@EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT)
public final class BinderClientState {
    private static BinderSnapshot view=BinderSnapshot.EMPTY;
    public static BinderSnapshot snapshot(){return view;}
    static void receive(BinderSnapshot next,boolean open){
        view=next;
        if(open){BinderClient.open();return;}
        var game=Minecraft.getInstance();
        if(game.screen==null&&game.player!=null&&!next.message().isBlank()){
            game.player.displayClientMessage(Component.literal(next.message()),true);
        }
    }
    private static void request(int op,int slot,String id){
        PacketDistributor.sendToServer(new BinderNetwork.Request(op,slot,id));
    }
    public static void refresh(){request(BinderNetwork.REFRESH,0,"");}
    public static void purchase(String id){request(BinderNetwork.PURCHASE,0,id);}
    public static void equip(String id,int slot){request(BinderNetwork.EQUIP,slot,id);}
    public static void cast(int slot){request(BinderNetwork.CAST,slot,"");}
    public static void close(){request(BinderNetwork.CLOSE,0,"");}
    @SubscribeEvent public static void disconnected(ClientPlayerNetworkEvent.LoggingOut e){view=BinderSnapshot.EMPTY;}
}
