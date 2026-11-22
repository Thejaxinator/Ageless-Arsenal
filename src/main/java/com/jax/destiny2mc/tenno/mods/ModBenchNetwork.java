package com.jax.destiny2mc.tenno.mods;

import com.jax.destiny2mc.ageless.materials.BenchAccess;
import com.jax.destiny2mc.block.GunsmithBlock;
import com.jax.destiny2mc.tenno.client.TennoModPage;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Network guard for Mods and Fusion. Only the server thread writes the table below. A bind
 * captures the exact authoritative stack in a slot: two identically named Bratons remain
 * distinct through delayed packets, inventory churn, and other mods moving equipment. */
@EventBusSubscriber(modid="destiny2mc", bus=EventBusSubscriber.Bus.MOD)
public final class ModBenchNetwork {
    public static final byte BIND=0, INSTALL=1, REMOVE=2, FUSE=3, CLOSE=4;
    public static final byte OK=0, NO_STATION=1, STALE=2, INVALID=3, DECLINED=4;
    public static final ResourceLocation AIR=ResourceLocation.withDefaultNamespace("air");
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();

    private static final class Session {
        long serial, token;
        int slot=-1;
        ItemStack target=ItemStack.EMPTY;
    }
    public record Command(long serial,long token,byte op,int slot,ResourceLocation item,String mod,int rank) implements CustomPacketPayload {
        public static final Type<Command> TYPE=new Type<>(key("weapon_mod_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Command> CODEC=StreamCodec.of(
            (b,m)->{b.writeVarLong(m.serial);b.writeLong(m.token);b.writeByte(m.op);b.writeVarInt(m.slot);
                    b.writeResourceLocation(m.item);b.writeUtf(m.mod,64);b.writeVarInt(m.rank);},
            b->new Command(b.readVarLong(),b.readLong(),b.readByte(),b.readVarInt(),b.readResourceLocation(),b.readUtf(64),b.readVarInt()));
        public Type<Command> type(){return TYPE;}
    }

    public record Reply(long serial,long token,byte status) implements CustomPacketPayload {
        public static final Type<Reply> TYPE=new Type<>(key("weapon_mod_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Reply> CODEC=StreamCodec.of(
            (b,m)->{b.writeVarLong(m.serial);b.writeLong(m.token);b.writeByte(m.status);},
            b->new Reply(b.readVarLong(),b.readLong(),b.readByte()));
        public Type<Reply> type(){return TYPE;}
    }

    private static ResourceLocation key(String path){return ResourceLocation.fromNamespaceAndPath("destiny2mc",path);}
    private static boolean carrySlot(int slot){return slot>=0 && (slot<36 || slot==40);}
    private static Reply answer(Command c,Session session,byte code){return new Reply(c.serial,session==null?0:session.token,code);}

    /** The item ID is descriptive. Lookup and mutation always use the inventory's actual stack. */
    public static Reply handle(ServerPlayer player,Command c) {
        Session s=SESSIONS.get(player.getUUID());
        if(s==null||player.isSpectator()||!player.isAlive()||player.containerMenu!=player.inventoryMenu||!BenchAccess.permits(player))
            return answer(c,null,NO_STATION);
        if(c.serial<=s.serial)return answer(c,s,STALE);
        s.serial=c.serial;
        if(c.op==CLOSE){
            SESSIONS.remove(player.getUUID());
            return answer(c,null,OK);
        }
        if(c.slot!=-1&&!carrySlot(c.slot))return answer(c,s,INVALID);
        var current=c.slot==-1?ItemStack.EMPTY:player.getInventory().getItem(c.slot);
        if(c.op==BIND) {
            if(c.slot==-1?!c.item.equals(AIR):
                    !TennoBlueprints.isTenno(current.getItem())||!BuiltInRegistries.ITEM.getKey(current.getItem()).equals(c.item))
                return answer(c,s,INVALID);
            s.slot=c.slot;s.target=current;
            do {s.token=player.getRandom().nextLong();} while(s.token==0);
            return answer(c,s,OK);
        }
        if(c.token==0||c.token!=s.token||c.slot!=s.slot||current!=s.target||
                !BuiltInRegistries.ITEM.getKey(current.getItem()).equals(c.item))return answer(c,s,STALE);
        WeaponMod mod=WeaponMod.byId(c.mod);
        if(mod==null||ModCollection.rank(player,c.mod)!=c.rank)return answer(c,s,DECLINED);
        boolean applied=switch(c.op){
            case INSTALL -> c.slot>=0&&c.rank>=0&&mod.fits(current)&&WeaponMods.install(player,current,mod);
            case REMOVE -> c.slot>=0&&WeaponMods.remove(current,mod);
            case FUSE -> ModCollection.rankUp(player,c.mod,c.rank);
            default -> false;
        };
        if(applied){
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
        }
        return answer(c,s,applied?OK:DECLINED);
    }

    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        var r=event.registrar("weapon-mod-workbench-1");
        r.playToServer(Command.TYPE,Command.CODEC,(m,c)->c.enqueueWork(()->{
            if(c.player() instanceof ServerPlayer p)PacketDistributor.sendToPlayer(p,handle(p,m));
        }));
        r.playToClient(Reply.TYPE,Reply.CODEC,(m,c)->c.enqueueWork(()->TennoModPage.receive(m)));
    }

    @EventBusSubscriber(modid="destiny2mc")
    public static final class Sessions {
        @SubscribeEvent(priority=EventPriority.LOWEST) public static void opened(PlayerInteractEvent.RightClickBlock e){
            if(e.getEntity() instanceof ServerPlayer p&&e.getLevel().getBlockState(e.getPos()).getBlock() instanceof GunsmithBlock)
                SESSIONS.put(p.getUUID(),new Session());
        }
        @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SESSIONS.remove(e.getEntity().getUUID());}
        @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){SESSIONS.remove(e.getEntity().getUUID());}
        @SubscribeEvent public static void clone(PlayerEvent.Clone e){SESSIONS.remove(e.getEntity().getUUID());}
        @SubscribeEvent public static void stop(ServerStoppedEvent e){SESSIONS.clear();}
    }
}
