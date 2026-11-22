package com.jax.destiny2mc.binder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Character ledger. Every write runs on the server game thread: read, validate, debit, commit,
 * then return the owner's snapshot. Clients cannot announce a position to open a shop.
 * Cooldowns key off ability ID so changing keys, equipping duplicates or reconnecting cannot reset them. */
@EventBusSubscriber(modid="destiny2mc")
public final class BinderState {
    private static final String LEDGER="AgelessEnergyBinder", OWNED="Unlocked", CLOCKS="ReadyAt";
    private static final ResourceLocation BINDER=id("energy_binder"), MOTE=id("mote_of_light");
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("destiny2mc",path);}
    private static final class Session {
        GlobalPos station;
        long lastRefresh=-100, window=-1;
        int budget, motes=-1, levels=-1;
    }
    private static Session session(ServerPlayer p){return SESSIONS.computeIfAbsent(p.getUUID(),key->new Session());}
    private static long now(ServerPlayer p){return p.serverLevel().getServer().overworld().getGameTime();}
    private static String key(int slot){return "Slot"+slot;}
    private static CompoundTag section(CompoundTag root,String key){
        if(!root.contains(key,Tag.TAG_COMPOUND))root.put(key,new CompoundTag());
        return root.getCompound(key);
    }
    private static CompoundTag ledger(Player p){return section(p.getPersistentData(),LEDGER);}
    private static boolean owns(CompoundTag ledger,String id){return ledger.getCompound(OWNED).getBoolean(id);}

    public static boolean hasPassive(ServerPlayer p,String id){
        if(id==null||id.isEmpty())return false;
        var d=ledger(p);
        return owns(d,id) && (id.equals(d.getString(key(4)))||id.equals(d.getString(key(5))));
    }
    public static List<String> passives(ServerPlayer p){
        var d=ledger(p);return List.of(equipped(d,4),equipped(d,5));
    }
    private static String equipped(CompoundTag d,int slot){
        String id=d.getString(key(slot));BinderAbility a=BinderCatalog.get(id);
        return a!=null && a.isPassive()==(slot>=4) && owns(d,id) ? id : "";
    }
    public static boolean refreshMelee(ServerPlayer p){
        var d=ledger(p);var clocks=section(d,CLOCKS);
        for(int i=0;i<4;i++){
            var a=BinderCatalog.get(equipped(d,i));
            if(a!=null && a.category().equals("melee") && clocks.getLong(a.id())>now(p)){
                clocks.remove(a.id());return true;
            }
        }
        return false;
    }
    private static Item mote(){return BuiltInRegistries.ITEM.get(MOTE);}
    private static int motes(ServerPlayer p){
        // Match the exact index range used by the debit, including the offhand.
        var inventory=p.getInventory();Item currency=mote();int count=0;
        for(int i=0;i<inventory.getContainerSize();i++){
            var stack=inventory.getItem(i);
            if(stack.is(currency))count+=stack.getCount();
        }
        return count;
    }
    public static BinderSnapshot snapshot(ServerPlayer p){return snapshot(p,"");}
    private static BinderSnapshot snapshot(ServerPlayer p,String message){
        var d=ledger(p);var clocks=d.getCompound(CLOCKS);
        var owned=new LinkedHashSet<String>();var ready=new LinkedHashMap<String,Long>();
        long current=now(p);
        for(var a:BinderCatalog.all()){
            if(owns(d,a.id()))owned.add(a.id());
            if(clocks.getLong(a.id())>current)ready.put(a.id(),clocks.getLong(a.id()));
        }
        var active=new ArrayList<String>();for(int i=0;i<4;i++)active.add(equipped(d,i));
        return new BinderSnapshot(owned,active,passives(p),ready,motes(p),p.experienceLevel,message);
    }
    private static void send(ServerPlayer p,String message,boolean open){
        if(p instanceof FakePlayer)return;
        var snapshot=snapshot(p,message);
        var session=session(p);session.motes=snapshot.motes();session.levels=snapshot.levels();
        PacketDistributor.sendToPlayer(p,new BinderNetwork.Reply(snapshot,open));
    }
    private static boolean permitted(ServerPlayer p,GlobalPos bench){
        return bench!=null && p.isAlive() && !p.isSpectator() && bench.dimension().equals(p.level().dimension())
            && p.canInteractWithBlock(bench.pos(),1.0)
            && BINDER.equals(BuiltInRegistries.BLOCK.getKey(p.level().getBlockState(bench.pos()).getBlock()));
    }
    public static void open(ServerPlayer p,BlockPos pos){
        var bench=GlobalPos.of(p.level().dimension(),pos.immutable());
        if(!permitted(p,bench))return;
        session(p).station=bench;send(p,"",true);
    }
    static void handle(ServerPlayer p,BinderNetwork.Request r){
        var s=session(p);long tick=now(p);
        if(s.window!=tick){s.window=tick;s.budget=0;}
        if(s.budget++>=8)return;
        if(r.op()==BinderNetwork.CLOSE){s.station=null;return;}
        if(r.op()==BinderNetwork.REFRESH){
            if(tick-s.lastRefresh>=5){s.lastRefresh=tick;send(p,"",false);}
            return;
        }
        if(r.op()==BinderNetwork.CAST){
            String message=cast(p,r.slot());
            if(message!=null)send(p,message,false);
            return;
        }
        if(r.op()==BinderNetwork.PURCHASE)send(p,purchase(p,r.id()),false);
        if(r.op()==BinderNetwork.EQUIP)send(p,equip(p,r.id(),r.slot()),false);
    }
    public static String purchase(ServerPlayer p,String id){
        if(!permitted(p,session(p).station))return "Return to the Energy Binder.";
        var a=BinderCatalog.get(id);var d=ledger(p);
        if(a==null)return "Unknown ability.";
        if(owns(d,id))return "Already unlocked.";
        if(motes(p)<a.motes()||p.experienceLevel<a.levels())return "More Motes of Light or XP levels required.";
        int remaining=a.motes();var inventory=p.getInventory();Item currency=mote();
        for(int i=0;i<inventory.getContainerSize()&&remaining>0;i++){
            ItemStack stack=inventory.getItem(i);
            if(stack.is(currency)){
                int amount=Math.min(remaining,stack.getCount());
                inventory.removeItem(i,amount);remaining-=amount;
            }
        }
        if(remaining!=0)throw new IllegalStateException("Energy Binder currency count mismatch");
        p.giveExperienceLevels(-a.levels());
        section(d,OWNED).putBoolean(id,true);inventory.setChanged();p.containerMenu.broadcastChanges();
        return "Unlocked "+a.name()+".";
    }
    public static String equip(ServerPlayer p,String id,int slot){
        if(!permitted(p,session(p).station))return "Return to the Energy Binder.";
        if(slot<0||slot>=6)return "Invalid slot.";
        var d=ledger(p);
        if(id.isEmpty()){d.remove(key(slot));return "Slot cleared.";}
        var a=BinderCatalog.get(id);
        if(a==null||!owns(d,id))return "Unlock this ability first.";
        if(a.isPassive()!=(slot>=4))return "Choose a matching slot.";
        // A passive can move between the two slots. Repeating a movement upgrade cannot stack its budget.
        if(a.isPassive() && id.equals(d.getString(key(slot==4?5:4))))d.remove(key(slot==4?5:4));
        d.putString(key(slot),id);
        return a.name()+" equipped.";
    }
    public static String cast(ServerPlayer p,int slot){
        if(slot<0||slot>=4||!p.isAlive()||p.isSpectator())return null;
        var d=ledger(p);var a=BinderCatalog.get(equipped(d,slot));
        if(a==null)return null;
        var clocks=section(d,CLOCKS);long tick=now(p);
        if(clocks.getLong(a.id())>tick)return null;
        if(!ActiveAbilities.cast(p,a.id()))return "No valid target or space.";
        // A successful cast is the sole authority for worn exotic effects and the cooldown
        // reported to the HUD. Buying, rebinding or swapping armor cannot regenerate the reserve.
        boolean usedReserve=com.jax.destiny2mc.armor.ArmorEffects.afterCast(p,a);
        if(!usedReserve)clocks.putLong(a.id(),tick+a.cooldownTicks());
        return "";
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%10!=0)return;
        var s=SESSIONS.get(p.getUUID());if(s==null||s.station==null)return;
        if(!permitted(p,s.station)){s.station=null;return;}
        if(p.experienceLevel!=s.levels||motes(p)!=s.motes)send(p,"",false);
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event){
        var old=event.getOriginal().getPersistentData();
        if(old.contains(LEDGER,Tag.TAG_COMPOUND))event.getEntity().getPersistentData().put(LEDGER,old.getCompound(LEDGER).copy());
        SESSIONS.remove(event.getOriginal().getUUID());
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)send(p,"",false);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)send(p,"",false);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){
        if(e.getEntity() instanceof ServerPlayer p){session(p).station=null;send(p,"",false);}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SESSIONS.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stop(ServerStoppedEvent e){SESSIONS.clear();}
}
