package com.jax.destiny2mc.tenno;

import com.jax.destiny2mc.weapon.GunDataHelper;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import static com.jax.destiny2mc.tenno.TennoGunSpec.Family;

/** Trigger, reload and battery state are authoritative. Scheduled rounds require the same
 * physical stack and hand. Equipment switching cancels a burst without refilling either gun. */
@EventBusSubscriber(modid="destiny2mc")
public final class TennoBallistics {
    private static final class Trigger {
        ItemStack stack=ItemStack.EMPTY;
        double ready, nextRound;
        long nextDisc, lastShot=-1000;
        int remaining, precise, spool;
        boolean ads, salvo;
    }
    private static final WeakHashMap<ServerPlayer,Trigger> TRIGGERS=new WeakHashMap<>();

    private static Trigger held(ServerPlayer p,ItemStack stack) {
        Trigger t=TRIGGERS.computeIfAbsent(p,unused->new Trigger());
        if(t.stack!=stack) {
            if(TennoGunItem.reloadUntil(t.stack)>0) GunDataHelper.setLong(t.stack,"wf_reload",0);
            t.stack=stack; t.remaining=0; t.precise=0; t.spool=0;
        }
        return t;
    }
    public static void action(ServerPlayer p,int action,boolean ads) {
        if(action<0||action>2||!p.isAlive()||p.isSpectator()||p.containerMenu!=p.inventoryMenu) return;
        ItemStack stack=p.getMainHandItem();
        if(!(stack.getItem() instanceof TennoGunItem item)) return;
        TennoGunSpec spec=item.profile();
        Trigger t=held(p,stack);
        long now=p.level().getGameTime();
        finishReload(p,stack,spec,now);
        if(TennoGunItem.reloadUntil(stack)>now||t.remaining>0)return;
        if(action==1) { startReload(p,stack,spec,now);return; }
        if(now<t.ready)return;
        if(action==2&&!spec.alternate())return;
        var stats=TennoGunStats.of(p,stack);
        if(action==2&&spec.family==Family.CEDO){
            if(now<t.nextDisc)return;
            t.nextDisc=now+stats.shotTicks(36); t.ready=stats.nextShot(now,t.ready,5);
            TennoFlights.launch(p,stack,stats,TennoFlights.Type.GLAIVE);
            sound(p,spec,true);return;
        }
        int minimum=action==2?3:1;
        if(TennoGunItem.loaded(stack,stats)<minimum){startReload(p,stack,spec,now);return;}
        t.ads=ads; t.salvo=action==2; t.precise=0;
        int count=t.salvo?3:switch(spec.family){case BURST->3; case PERIGALE->4;default->1;};
        t.remaining=count;
        if(spec.family==Family.SOMA){
            t.spool=now-t.lastShot>8?0:Math.min(4,t.spool+1);
        }
        int delay=spec.delay()+(spec.family==Family.SOMA?Math.max(0,4-t.spool):0);
        t.ready=stats.nextShot(now,t.ready,t.salvo?15:delay);
        t.nextRound=now;
        round(p,stack,stats,t,now);
    }

    private static void round(ServerPlayer p,ItemStack stack,TennoGunStats stats,Trigger t,long now) {
        var spec=stats.spec();
        int mag=TennoGunItem.loaded(stack,stats);
        int spend=t.salvo?3:spec.family==Family.TWINS?Math.min(2,mag):1;
        if(mag<spend||mag==0){t.remaining=0;return;}
        int sequence=GunDataHelper.getInt(stack,"wf_sequence",0)+1;
        GunDataHelper.setInt(stack,"wf_sequence",sequence);
        boolean gale=spec.family==Family.PERIGALE&&GunDataHelper.getLong(stack,"wf_gale")>now;
        boolean hipSaver=spec.family==Family.AX&&!t.ads&&Math.floorMod(sequence,4)!=1;
        if(!gale&&!hipSaver)GunDataHelper.setInt(stack,"wf_mag",mag-spend);
        if(spec.family==Family.BUBONICO)GunDataHelper.setLong(stack,"wf_charge_at",now+stats.rechargeTicks(TennoGunItem.magazine(stack)==0?2:30));
        t.remaining--;t.nextRound=stats.nextShot(now,t.nextRound,t.salvo?3:2);t.lastShot=now;
        // Emit both tracers from their own muzzles even when the last cycle contains only one bullet.
        if(spec.family==Family.TWINS){
            for(int i=0;i<spend;i++) TennoHits.trigger(p,stats,t.ads,i==0?-1:1);
        }else if(t.salvo){TennoFlights.launch(p,stack,stats,TennoFlights.Type.VIRAL);}
        else if(spec.family==Family.TORID){TennoFlights.launch(p,stack,stats,TennoFlights.Type.SPORE);}
        else if(spec.family==Family.CANTARE){TennoFlights.launch(p,stack,stats,TennoFlights.Type.KNIFE);}
        else{
            var hit=TennoHits.trigger(p,stats,t.ads,spec.dual()?(sequence%2==0?-1:1):0);
            if(spec.family==Family.PERIGALE&&hit.weakHits()>0){
                t.precise+=hit.weakHits();
                if(hit.killed()||t.precise>=4)GunDataHelper.setLong(stack,"wf_gale",now+80);
            }
        }
        sound(p,spec,t.salvo);
    }

    private static void sound(ServerPlayer p,TennoGunSpec spec,boolean alt) {
        var sound=switch(spec.family){
            case FLAME -> SoundEvents.FIRECHARGE_USE;
            case MICROWAVE -> SoundEvents.BEACON_AMBIENT;
            case BUBONICO,TORID -> SoundEvents.SLIME_SQUISH_SMALL;
            case CANTARE -> SoundEvents.TRIDENT_THROW.value();
            case AKSONDOL -> SoundEvents.AMETHYST_BLOCK_HIT;
            case CEDO -> alt?SoundEvents.TRIDENT_THROW.value():SoundEvents.FIREWORK_ROCKET_BLAST;
            default -> SoundEvents.FIREWORK_ROCKET_BLAST;
        };
        float volume=spec.family==Family.VESPER?.12f:.42f;
        float pitch=spec.family==Family.PERIGALE?.85f:spec.dual()?1.7f:1.25f;
        p.serverLevel().playSound(null,p.blockPosition(),sound,SoundSource.PLAYERS,volume,pitch+p.getRandom().nextFloat()*.09f);
    }

    private static void startReload(ServerPlayer p,ItemStack stack,TennoGunSpec spec,long now) {
        var stats=TennoGunStats.of(p,stack);
        int mag=TennoGunItem.loaded(stack,stats);
        if(spec.family==Family.BUBONICO||mag>=stats.magazine()||TennoGunItem.reloadUntil(stack)>now)return;
        GunDataHelper.setLong(stack,"wf_reload",now+stats.reloadTicks());
        if(spec.family==Family.CANTARE)TennoFlights.recall(p,stack);
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.PLAYERS,.34f,1.5f);
    }
    private static void finishReload(ServerPlayer p,ItemStack stack,TennoGunSpec spec,long now) {
        long end=TennoGunItem.reloadUntil(stack);
        if(end==0||now<end)return;
        var stats=TennoGunStats.of(p,stack);
        if(spec.family==Family.AKSONDOL) {
            int spent=stats.magazine()-TennoGunItem.loaded(stack,stats);
            if(spent>0)TennoFlights.field(p,p.position().add(0,.5,0),1.2+spent*.35,60,TennoFlights.Type.FROST);
        }
        GunDataHelper.setLong(stack,"wf_reload",0);
        GunDataHelper.setInt(stack,"wf_mag",stats.magazine());
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var stack=p.getMainHandItem();
        if(!(stack.getItem() instanceof TennoGunItem gun)||!p.isAlive()||p.isSpectator()){
            if(TRIGGERS.containsKey(p))held(p,ItemStack.EMPTY);
            return;
        }
        var spec=gun.profile();var t=held(p,stack);long now=p.level().getGameTime();
        if(p.containerMenu!=p.inventoryMenu)t.remaining=0;
        finishReload(p,stack,spec,now);
        if(t.remaining>0&&now>=t.nextRound)round(p,stack,TennoGunStats.of(p,stack),t,now);
    }
    /** A stowed arm-cannon also charges; one tick hook per stack, no global item search. */
    static void charge(ItemStack stack,net.minecraft.world.entity.player.Player holder,long now) {
        if(now<GunDataHelper.getLong(stack,"wf_charge_at"))return;
        var stats=TennoGunStats.of(holder,stack);
        if(TennoGunItem.magazine(stack)>=stats.magazine())return;
        GunDataHelper.setInt(stack,"wf_mag",TennoGunItem.magazine(stack)+1);
        GunDataHelper.setLong(stack,"wf_charge_at",now+stats.rechargeTicks(stats.spec()==TennoGunSpec.CODA_BUBONICO?2:3));
    }
    @SubscribeEvent public static void leave(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer p)TRIGGERS.remove(p);
    }
}
