package ageless.test;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.ageless.materials.*;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import com.jax.destiny2mc.weapon.*;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid="smith_check")
public class GunsmithCheck {
    private static final String[] SAMPLE = {
        "fallen_dreg","vex_goblin","hive_thrall","cabal_legionary","taken_thrall","scorn_stalker","dread_husk"
    };
    private static void ok(boolean success, String what) {
        if (!success) throw new IllegalStateException(what);
    }
    @SubscribeEvent public static void start(ServerStartedEvent event) {
        var server = event.getServer();
        ServerLevel level=server.overworld();
        try {
            var p=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("60000000-0000-4000-b000-000000000006"),"Armorer"));
            p.setGameMode(GameType.SURVIVAL);
            level.setChunkForced(0,0,true);
            level.getChunk(0,0);
            level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(true,server);
            p.setPos(8.5,65,8.5);
            ok(GunsmithRecipes.COUNT==77,"77 stable recipe indices");
            int visible=0;
            for(int i=0;i<GunsmithRecipes.COUNT;i++) {
                if(GunsmithRecipes.createResultStack(i).getItem() instanceof com.jax.destiny2mc.item.DestinyGunItem
                    && !GunsmithRecipes.costs(p,i).isEmpty()) visible++;
            }
            ok(visible==70,"70 visible blueprints, got "+visible);
            for(int recipe=59;recipe<77;recipe++) craft(p,recipe);
            System.out.println("SMITH 18 native blueprints, 108 paid perk swaps, 18 network recipe round trips");
            p.getInventory().clearContent();
            ok(!GunsmithRecipes.canCraft(p,-1) && !GunsmithRecipes.canCraft(p,77),"out-of-range recipes");
            verifyBench(p);
            for(int i=0;i<SAMPLE.length;i++) {
                var part=FactionComponents.ALL.get(i);int count=killAndCount(level,p,SAMPLE[i],part.item());
                ok(count==1,"one real death drop "+SAMPLE[i]+", got "+count);
                System.out.println("SMITH "+SAMPLE[i]+" dropped "+part.id());
            }
            ok(killAndCount(level,p,"valus_ta_aurc",FactionComponents.ALL.get(3).item())==3,"boss gives three components");
            level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(false,server);
            ok(killAndCount(level,p,"scorn_stalker",FactionComponents.ALL.get(5).item())==0,"doMobLoot=false");
            System.out.println("SMITH COMPLETE");
        } catch(Throwable t) {t.printStackTrace();System.out.println("SMITH FAILED "+t);}
    }
    private static void craft(ServerPlayer p,int recipe) {
        WeaponSpec spec=WeaponSpec.ALL[recipe-7];
        var blueprint=GunsmithBlueprint.find(p,spec.id);
        ok(blueprint!=null,"missing blueprint "+spec.id);
        var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),p.registryAccess());
        try {
            Recipe.STREAM_CODEC.encode(buf,blueprint);
            ok(blueprint.equals(Recipe.STREAM_CODEC.decode(buf)),"client recipe wire round trip "+spec.id);
        } finally { buf.release(); }
        var costs=GunsmithRecipes.costs(p,recipe);
        p.getInventory().clearContent();
        for(int i=0;i<3;i++) {var c=costs.get(i);p.getInventory().setItem(i,new ItemStack(c.item(),c.count()-(i==2?1:0)));}
        GunsmithRecipes.tryCraftOnServer(p,recipe);
        ok(GunsmithRecipes.countInInventory(p,costs.get(0).item())==costs.get(0).count(),"partial charge");
        ok(GunsmithRecipes.countInInventory(p,blueprint.result())==0,"craft accepted without faction component");
        p.getInventory().getItem(2).grow(1);
        GunsmithRecipes.tryCraftOnServer(p,recipe);
        ok(GunsmithRecipes.countInInventory(p,blueprint.result())==1,"crafted "+spec.id);
        for(var c:costs)ok(GunsmithRecipes.countInInventory(p,c.item())==0,"exact blueprint cost "+spec.id);
        int slot=0;while(slot<36&&p.getInventory().getItem(slot).getItem()!=blueprint.result())slot++;
        var gun=p.getInventory().getItem(slot);
        Item alloy=(Item)ModRegistries.PLUNDERED_ETHER_ALLOY.get();
        p.getInventory().setItem(31,new ItemStack(alloy,6));p.getInventory().setItem(32,new ItemStack(Items.COPPER_INGOT,12));
        WeaponPerkRegistry.serverTrySocketPerk(p,slot,5,999);
        ok(GunsmithRecipes.countInInventory(p,alloy)==6,"illegal perk charged");
        for(int socket=0;socket<6;socket++) {
            HashSet<Integer> ids=new HashSet<>();
            for(int i=0;i<WeaponPerkRegistry.optionCount(socket);i++)ids.add(WeaponPerkRegistry.getPoolOption(spec.index,socket,i));
            ok(ids.size()==WeaponPerkRegistry.optionCount(socket),"duplicated slot pool "+spec.id+"/"+socket);
            int opt=WeaponPerkRegistry.getPoolOption(spec.index,socket,socket==5?2:1);
            int before=WeaponPerkRegistry.getEffectiveMagSize(gun,spec);
            WeaponPerkRegistry.serverTrySocketPerk(p,slot,socket,opt);
            ok(WeaponPerkRegistry.getActivePerk(gun,spec,socket)==opt,"applied "+spec.id+"/"+socket);
            if(socket<4)ok(LegacyWeaponPerks.getActivePerk(gun,spec,socket)==opt,"real stat calculation sees "+spec.id+"/"+socket);
            if(socket==5)ok(WeaponPerkRegistry.getEffectiveMagSize(gun,spec)>before,"backup magazine actual capacity "+spec.id);
        }
        ok(GunsmithRecipes.countInInventory(p,alloy)==0&&GunsmithRecipes.countInInventory(p,Items.COPPER_INGOT)==0,"exact perk cost "+spec.id);
    }
    private static void verifyBench(ServerPlayer p) {
        var level=p.serverLevel();var pos=new BlockPos(8,65,9);
        level.setBlockAndUpdate(pos,((Block)ModRegistries.GUNSMITH_FUI_BLOCK.get()).defaultBlockState());
        ok(!BenchAccess.permits(p),"packet without interaction");
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false)));
        ok(BenchAccess.permits(p),"normal bench interaction");
        p.setPos(22.5,65,8.5);ok(!BenchAccess.permits(p),"range");
        p.setPos(8.5,65,8.5);level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());ok(!BenchAccess.permits(p),"destroyed bench");
    }
    private static LivingEntity observed;
    private static int salvaged;
    private static Item expected;
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void observedDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (event.getEntity()!=observed) return;
        salvaged = event.getDrops().stream().filter(e -> e.getItem().is(expected)).mapToInt(e -> e.getItem().getCount()).sum();
        System.out.println("SMITH DROP "+observed.getType()+" playerCredit="+event.isRecentlyHit()+" salvage="+salvaged);
    }

    private static int killAndCount(ServerLevel level,ServerPlayer p,String id,Item item) {
        var aabb=new AABB(5,60,5,13,75,13);
        for(var drop:level.getEntitiesOfClass(ItemEntity.class,aabb))drop.discard();
        var type=BuiltInRegistries.ENTITY_TYPE.get(FactionComponents.key(id));
        var mob=(LivingEntity)type.create(level);
        mob.setPos(8.5,65,8.5);level.addFreshEntity(mob);
        observed=mob;expected=item;salvaged=-1;
        mob.setHealth(1);
        ok(mob.hurt(level.damageSources().playerAttack(p),4000),"mob accepted a lethal player hit");
        ok(mob.isDeadOrDying(),"lethal player strike");
        return salvaged;
    }
}
