package com.jax.destiny2mc.overdrive;

import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.WeakHashMap;

/** Uses the normal equipment snapshot, including the initial packet for a new observer. */
@EventBusSubscriber(modid="destiny2mc")
public final class ServerAimState {
    public static final int LEASE=28;
    private static final WeakHashMap<ServerPlayer,ItemStack> ACTIVE=new WeakHashMap<>();

    static void accept(ServerPlayer player,AimIntent intent){
        ItemStack stack=player.getMainHandItem();
        if(!(stack.getItem() instanceof DestinyGunItem gun)||gun.spec.index!=intent.weapon())return;
        var n=GunDataHelper.readTag(stack);
        long now=player.level().getGameTime(), end=n.getLong("d2_aim_until");
        if(intent.held() && player.isAlive()&&!player.isSpectator()&&n.getLong("d2_rel_end")<=now){
            var previous=ACTIVE.put(player,stack);
            if(previous!=null&&previous!=stack)clear(previous);
            if(previous==stack && end-now>LEASE-8)return;
            if(previous!=stack||end<=now+4)n.putLong("d2_aim_since",now);
            n.putUUID("d2_aim_owner",player.getUUID());
            n.putLong("d2_aim_until",now+LEASE);
        }else{
            if(ACTIVE.get(player)!=stack || end<=now+4)return;
            n.putLong("d2_aim_until",now+4);
        }
        GunDataHelper.writeTag(stack,n);
    }
    private static void clear(ItemStack stack){
        var n=GunDataHelper.readTag(stack);
        if(!n.contains("d2_aim_until"))return;
        n.remove("d2_aim_until");n.remove("d2_aim_since");n.remove("d2_aim_owner");
        GunDataHelper.writeTag(stack,n);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var stack=ACTIVE.get(p);
        if(stack==null)return;
        long now=p.level().getGameTime();var n=GunDataHelper.readTag(stack);
        if(p.getMainHandItem()!=stack||!p.isAlive()||p.isSpectator()||n.getLong("d2_rel_end")>now||n.getLong("d2_aim_until")<=now){
            ACTIVE.remove(p);clear(stack);
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){
        if(e.getEntity() instanceof ServerPlayer p){var stack=ACTIVE.remove(p);if(stack!=null)clear(stack);}
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity() instanceof ServerPlayer p && p.getMainHandItem().getItem() instanceof DestinyGunItem)clear(p.getMainHandItem());
    }
}
