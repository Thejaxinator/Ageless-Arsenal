package ageless.validation;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.ageless.*;
import com.jax.destiny2mc.frontier.*;
import com.jax.destiny2mc.weapon.*;
import com.jax.destiny2mc.item.DestinyGunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.*;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Dedicated-server fixture, isolated world only. Uses the same action + inventory methods as the
 * ordinary item and network handler. No mocked ballistics, timers, entity persistence or pack reload. */
@EventBusSubscriber(modid="ageless_check")
public final class AgelessWeaponCheck {
    static ServerLevel l;
    static FakePlayer p;
    static int t,mag,index;
    static ItemStack s;
    static WeaponSpec w;
    static List<FrontierProjectile> volley;
    static net.minecraft.world.entity.monster.Husk target;
    static float health;
    static Path override(){return Path.of("world/datapacks/ageless_qa/data/destiny2mc/weapon_programs/hung_jury_sr4.json");}
    static void target(){
        if(target!=null)target.discard();
        target=EntityType.HUSK.create(l);target.moveTo(4.5,100,10.5,180,0);target.setNoAi(true);target.setNoGravity(true);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(700);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        target.setHealth(700);health=target.getHealth();l.addFreshEntity(target);
    }
    static CompletableFuture<Void> reload;
    static boolean failed,done;
    static void ok(boolean result,String message){
        if(!result)throw new AssertionError(message);
        System.out.println("WEAPON CHECK "+message);
    }
    static void equip(int id){
        w=WeaponSpec.ALL[id];s=ModRegistries.createWeaponStackByIndex(id);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,s);mag=GunDataHelper.getMag(s,w);
    }
    static void action(int action){FrontierBallistics.action(p,(byte)action,p.getYRot(),p.getXRot(),(byte)1);}
    static List<FrontierProjectile> projectiles(){return l.getEntitiesOfClass(FrontierProjectile.class,new AABB(p.position(),p.position()).inflate(80),q->q.isAlive()&&q.sourceWeaponIndex()==w.index);}
    @SubscribeEvent public static void start(ServerStartedEvent event){
        l=event.getServer().overworld();p=FakePlayerFactory.getMinecraft(l);p.setGameMode(GameType.SURVIVAL);
        p.moveTo(4.5,100,4.5,0,0);p.getInventory().clearContent();p.getInventory().selected=0;p.setHealth(20);
        l.setChunkForced(0,0,true);l.setChunkForced(0,1,true);
        for(int x=0;x<=8;x++)for(int y=98;y<107;y++)l.setBlock(new BlockPos(x,y,15),Blocks.STONE.defaultBlockState(),2);
        var ids=new HashSet<String>();
        for(var spec:WeaponSpec.ALL){
            var stack=ModRegistries.createWeaponStackByIndex(spec.index);
            if(!(stack.getItem() instanceof DestinyGunItem gun)||gun.spec!=spec||!BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals(spec.id)||!ids.add(spec.id))throw new AssertionError(spec.id);
        }
        ok(ids.size()==70,"all 70 item IDs and held specs match");
        equip(40);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        if(l==null||failed||done)return;
        try { step(); }catch(Throwable e){failed=true;e.printStackTrace();System.out.println("WEAPON CHECK FAILED");}
    }
    static void step() throws Exception{
        t++;
        if(t==1)action(0);
        if(t==2){ok(projectiles().isEmpty()&&GunDataHelper.getMag(s,w)==mag,"Telesto rejects an uncharged trigger");action(3);}
        if(t>=3&&t<=20&&t%3==0)action(3);
        if(t<=20)FrontierBallistics.inventoryTick(w,s,l,p,true);
        if(t==20){
            volley=projectiles();
            ok(volley.size()==7&&GunDataHelper.getMag(s,w)==mag-1,"one Telesto round created seven independent Void entities");
            ok(volley.stream().allMatch(q->q.behaviorMode()==1&&q.element()==3&&q.getOwner()==p)&&volley.stream().map(Entity::getUUID).distinct().count()==7,"seven owned sticky projectiles with distinct entity IDs");
            ok(volley.stream().map(q->q.getDeltaMovement().normalize()).distinct().count()==7,"seven distinct flight directions");
            double max=0;for(var a:volley)for(var b:volley)max=Math.max(max,a.getDeltaMovement().normalize().distanceTo(b.getDeltaMovement().normalize()));
            ok(max>.14&&max<.20,"opposing Telesto paths span the authored spread");
            action(0);ok(projectiles().size()==7,"repeat input cannot duplicate the accepted volley");
        }
        if(t==32){
            ok(volley.stream().allMatch(FrontierProjectile::attached),"all seven bolts attached to the test wall");
            var orig=volley.getFirst();var saved=new CompoundTag();orig.saveWithoutId(saved);
            var copy=FrontierContent.BOLT.create(l);copy.load(saved);
            ok(copy.attached()&&copy.sourceWeaponIndex()==40&&copy.behaviorMode()==1,"sticky projectile survives save/load");
        }
        if(t==50){
            ok(volley.stream().noneMatch(Entity::isAlive),"all seven wall charges detonated after their fuse");
            equip(59);action(0);ok(GunDataHelper.getMag(s,w)==mag-1,"pulse trigger spends first round immediately");
        }
        if(t>=51&&t<=54)FrontierBallistics.inventoryTick(w,s,l,p,true);
        if(t==54)ok(GunDataHelper.getMag(s,w)==mag-3,"two subsequent pulse rounds spend ammo at tick intervals");
        if(t==61){action(0);FrontierBallistics.inventoryTick(w,s,l,p,false);}
        if(t>=62&&t<=66)FrontierBallistics.inventoryTick(w,s,l,p,true);
        if(t==66){
            ok(GunDataHelper.getMag(s,w)==mag-4,"holstering cancels pending burst rounds");
            action(1);ok(GunDataHelper.getLong(s,"d2_rel_end")>l.getGameTime(),"normal reload starts after a burst");
        }
        if(t>=67&&t<120)FrontierBallistics.inventoryTick(w,s,l,p,true);
        if(t==119)ok(GunDataHelper.getMag(s,w)==mag,"reload refills magazine through the existing ammo path");
        if(t>=120&&t<120+18*28){
            int frame=(t-120)%28;
            if(frame==0){index=52+(t-120)/28;equip(index);target();var pg=WeaponPrograms.server(w);action(pg.chargeTicks()>0?3:0);}
            if(frame>0 &&frame<=WeaponPrograms.server(w).chargeTicks()&&frame%3==0)action(3);
            FrontierBallistics.inventoryTick(w,s,l,p,true);
            if(frame==26)ok(GunDataHelper.getMag(s,w)<mag&&target.getHealth()<health,"new weapon fired and damaged survival target "+w.id);
        }
        if(t==624){
            // Runner provides an enabled empty pack; replacing a file at runtime exercises real /reload.
            var file=override();
            Files.createDirectories(file.getParent());
            Files.writeString(file,"{\"version\":1,\"trigger\":\"release\",\"feed\":\"capacitor\",\"cost\":2,\"recharge_ticks\":6,\"charge_ticks\":4,\"cooldown\":8,\"delivery\":\"projectile\",\"flight\":\"pull\",\"count\":3,\"pattern\":\"fan\",\"angle\":6,\"fx\":\"strand\"}");
            Files.copy(file,file.resolveSibling("arbalest.json"),StandardCopyOption.REPLACE_EXISTING);
            var server=l.getServer();reload=server.reloadResources(server.getPackRepository().getSelectedIds());
        }
        if(t>624&&t<700&&reload!=null&&reload.isDone()){
            reload.join();reload=null;equip(68);ok(WeaponPrograms.server(w).trigger()==WeaponProgram.Trigger.RELEASE,"real datapack reload replaced scout trigger and delivery");action(3);t=700;
        }
        if(t>=701&&t<=704){
            action(3);FrontierBallistics.inventoryTick(w,s,l,p,true);
            if(t==704){ok(projectiles().isEmpty(),"release program holds its charged salvo");action(0);
                ok(projectiles().size()==3&&projectiles().stream().allMatch(q->q.behaviorMode()==10),"same item fires three pull bolts after release");}
        }
        if(t==705){
            ok(GunDataHelper.getMag(s,w)==mag-2,"capacitor spends two units for an entire fan");
            action(1);ok(GunDataHelper.getLong(s,"d2_rel_end")==0,"capacitor ignores manual magazine reload");
        }
        if(t>=705&&t<=710)FrontierBallistics.inventoryTick(w,s,l,p,false);
        if(t==710)ok(GunDataHelper.getMag(s,w)==mag-1,"holstered capacitor recovered one unit after six ticks");
        if(t==711){
            equip(64);
            GunDataHelper.setInt(s,"d2_res",0);
            var brick=new ItemStack((Item)ModRegistries.SPECIAL_AMMO.get(),2);
            FrontierAmmo.use(p,brick);
            ok(brick.getCount()==2&&GunDataHelper.getReserve(s,w)==0,"special gun converted to capacitor preserves ammo bricks");
            equip(68);
            Files.writeString(override(),"{\"version\":1,\"trigger\":\"press\",\"cooldown\":0}");
            var server=l.getServer();
            try{reload=server.reloadResources(server.getPackRepository().getSelectedIds());}
            catch(RuntimeException bad){reload=CompletableFuture.failedFuture(bad);}
        }
        if(t>711&&t<1100&&reload!=null&&reload.isDone()){
            ok(reload.isCompletedExceptionally(),"invalid definition rejects reload");
            ok(WeaponPrograms.server(w).trigger()==WeaponProgram.Trigger.RELEASE,"failed reload retains last valid program");
            Files.delete(override());Files.delete(override().resolveSibling("arbalest.json"));var server=l.getServer();reload=server.reloadResources(server.getPackRepository().getSelectedIds());t=1100;
        }
        if(t>1100&&reload!=null&&reload.isDone()){
            reload.join();ok(WeaponPrograms.server(w).delivery()==WeaponProgram.Delivery.RAY,"removing pack override restores shipped scout behavior");
            done=true;System.out.println("WEAPON CHECK COMPLETE");
        }
    }
}
