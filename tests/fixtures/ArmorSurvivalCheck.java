package ageless.validation;

import com.jax.destiny2mc.armor.*;
import com.jax.destiny2mc.binder.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid="armor_survival_check")
public final class ArmorSurvivalCheck {
    static class Target extends FakePlayer {
        Target(ServerLevel world) throws Exception {
            super(world,new GameProfile(UUID.randomUUID(),"ArmorCheck"));
            setGameMode(GameType.SURVIVAL);
            var grace=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");grace.setAccessible(true);grace.setInt(this,0);
        }
        @Override public boolean isInvulnerableTo(DamageSource source){return false;}
    }
    static void ok(boolean value,String name){if(!value)throw new AssertionError(name);System.out.println("ARMOR CHECK "+name);}
    static ItemStack piece(ArmorPiece p){return new ItemStack(BuiltInRegistries.ITEM.get(ArmorPiece.key(p.id)));}
    static Mob mob(ServerLevel w,String id){return (Mob)BuiltInRegistries.ENTITY_TYPE.get(ArmorPiece.key(id)).create(w);}
    static float hit(Target p,DamageSource src,float damage){p.invulnerableTime=0;p.setHealth(20);if(!p.hurt(src,damage))throw new AssertionError("damage rejected "+src);return 20-p.getHealth();}
    static void equip(Target p,ArmorPiece... gear) {
        for(var data:gear){
            var stack=piece(data);var slot=data.type.getSlot();p.setItemSlot(slot,stack);
            // FakePlayer.tick() is empty; apply native item modifiers like the next living-entity tick.
            stack.forEachModifier(slot,(attr,modifier)->{
                var instance=p.getAttribute(attr);if(instance!=null && !instance.hasModifier(modifier.id()))instance.addTransientModifier(modifier);
            });
        }
    }
    static void grant(Target p,String id,int slot) {
        var a=BinderCatalog.get(id);
        p.getInventory().add(new ItemStack(BuiltInRegistries.ITEM.get(BinderContent.MOTE),a.motes()));
        p.experienceLevel=a.levels()+10;BinderState.purchase(p,id);BinderState.equip(p,id,slot);
        if(!BinderState.snapshot(p).active().get(slot).equals(id))throw new AssertionError("grant "+id);
    }
    static void sets(ServerLevel w)throws Exception{
        var dreg=mob(w,"fallen_dreg");
        var bolt=BuiltInRegistries.ENTITY_TYPE.get(ArmorPiece.key("shock_bolt")).create(w);
        var shot=w.damageSources().thrown(bolt,dreg);
        Target bare=new Target(w),kabr=new Target(w),single=new Target(w),hive=new Target(w),rull=new Target(w);
        equip(kabr,ArmorPiece.BATTLECAGE_OF_KABR,ArmorPiece.KABRS_FORCEFUL_GREAVES);
        equip(single,ArmorPiece.BATTLECAGE_OF_KABR);
        equip(hive,ArmorPiece.WILLBREAKERS_WATCH,ArmorPiece.WILLBREAKERS_GREAVES);
        equip(rull,ArmorPiece.MASK_OF_RULL,ArmorPiece.GREAVES_OF_RULL);
        float noGear=hit(bare,shot,4.5f), protectedHit=hit(kabr,shot,4.5f), halfPair=hit(single,shot,4.5f);
        ok(Math.abs(noGear-1.71)<.002 && protectedHit<halfPair && halfPair<noGear,"Dreg shock bolt Patrol unarmored="+noGear+" HP; single helmet="+halfPair+" HP; starter Kabr pair="+protectedHit+" HP");
        var explode=w.damageSources().explosion(dreg,dreg);
        var smash=w.damageSources().mobAttack(dreg);
        // Isolate each perk from material statistics via a fresh event, including mixed sets and bypass types.
        var damage=new net.neoforged.neoforge.common.damagesource.DamageContainer(smash,10);
        var e=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(hive,damage);
        ArmorEffects.setProtection(e);ok(Math.abs(e.getAmount()-8.2f)<.001,"Willbreaker two-piece melee mitigation");
        damage=new net.neoforged.neoforge.common.damagesource.DamageContainer(explode,10);
        e=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(rull,damage);
        ArmorEffects.setProtection(e);ok(Math.abs(e.getAmount()-7.5f)<.001,"Rull two-piece blast mitigation");
        damage=new net.neoforged.neoforge.common.damagesource.DamageContainer(w.damageSources().fellOutOfWorld(),10);
        e=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(rull,damage);
        ArmorEffects.setProtection(e);ok(e.getAmount()==10,"armor-bypassing damage unaffected");
    }
    static void recipes(ServerLevel w)throws Exception{
        for(var p:ArmorPiece.values()){
            var id=ArmorPiece.key(p.id);
            var recipe=(ShapedRecipe)w.getRecipeManager().byKey(id).orElseThrow().value();
            var inputs=recipe.getIngredients().stream().map(i->i.isEmpty()?ItemStack.EMPTY:i.getItems()[0].copy()).toList();
            var grid=CraftingInput.of(recipe.getWidth(),recipe.getHeight(),inputs);
            var matched=w.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,w).orElseThrow();
            var made=matched.value().assemble(grid,w.registryAccess());var item=(ArsenalArmorItem)made.getItem();
            var tag=switch(p.type){case HELMET->ItemTags.HEAD_ARMOR;case CHESTPLATE->ItemTags.CHEST_ARMOR;case LEGGINGS->ItemTags.LEG_ARMOR;default->ItemTags.FOOT_ARMOR;};
            var repair=new ItemStack(BuiltInRegistries.ITEM.get(ArmorPiece.key(p.suit.repairItem)));
            if(item.piece!=p || made.getMaxDamage()!=p.durability || item.getDefense()!=p.defense || !made.is(tag)
                    || !made.is(ItemTags.ARMOR_ENCHANTABLE) || !made.is(ItemTags.TRIMMABLE_ARMOR) || !item.isValidRepairItem(made,repair)
                    || !item.getMaterial().is(ArmorPiece.key(p.suit.id)))throw new AssertionError("item/recipe contract "+p);
            Target wearer=new Target(w);wearer.setItemInHand(InteractionHand.MAIN_HAND,made);
            item.use(w,wearer,InteractionHand.MAIN_HAND);
            if(wearer.getItemBySlot(p.type.getSlot()).getItem()!=item)throw new AssertionError("Native equip "+p);
        }
        ok(true,"15 distinct grid recipes, correct native equip slots, armor materials, durability, repair and protection enchantment tags");
    }
    static void powers(ServerLevel w)throws Exception{
        Target p=new Target(w);
        // FakePlayerNetHandler deliberately discards teleport(). Exercise the real server teleport path
        // with a local, unconnected transport so the collision assertion observes the server position.
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(w.getServer(),
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,
            net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        p.setPos(8.5,100,8.5);p.setYRot(0);p.setXRot(0);
        var bench=new BlockPos(8,100,6);
        w.setBlockAndUpdate(bench,BuiltInRegistries.BLOCK.get(BinderContent.STATION).defaultBlockState());
        BinderState.open(p,bench);
        grant(p,"solar_grenade",0);grant(p,"pulse_grenade",1);grant(p,"towering_barricade",2);grant(p,"marksmans_dodge",3);
        p.setItemSlot(EquipmentSlot.CHEST,piece(ArmorPiece.THE_ARMAMENTARIUM));
        ok("".equals(BinderState.cast(p,0)) && !BinderState.snapshot(p).readyAt().containsKey("solar_grenade"),"Armamentarium grants the first grenade; its HUD slot remains ready");
        ok("".equals(BinderState.cast(p,0)) && BinderState.snapshot(p).readyAt().containsKey("solar_grenade")
            && BinderState.cast(p,0)==null,"second grenade starts the ordinary cooldown; third cast rejected");
        p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.CHEST,piece(ArmorPiece.THE_ARMAMENTARIUM));
        ok("".equals(BinderState.cast(p,1)) && BinderState.snapshot(p).readyAt().containsKey("pulse_grenade"),"reserve shared across elements and chest swaps");
        Target clone=new Target(w);ArmorEffects.clone(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(clone,p,true));
        ok(clone.getPersistentData().getCompound("AgelessArmorClocks").equals(p.getPersistentData().getCompound("AgelessArmorClocks")),"reserve survives death clone");
        var grunt=EntityType.ZOMBIE.create(w);grunt.setPos(9.5,100,9.5);w.addFreshEntity(grunt);grunt.setTarget(p);
        p.setItemSlot(EquipmentSlot.HEAD,piece(ArmorPiece.HELM_OF_SAINT_14));
        ok("".equals(BinderState.cast(p,2)) && p.getAbsorptionAmount()==4 && grunt.hasEffect(MobEffects.BLINDNESS) && grunt.getTarget()==null,
            "Saint-14 barricade grants two absorption hearts and blinds nearby hostiles");
        p.removeEffect(MobEffects.ABSORPTION);
        var sc=new net.neoforged.neoforge.common.damagesource.DamageContainer(w.damageSources().mobAttack(grunt),5);
        var blocking=new net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent(p,sc,true);
        blocking.setBlockedDamage(5);ArmorEffects.shieldBlock(blocking);
        ok(!p.hasEffect(MobEffects.ABSORPTION),"Saint ward cannot retrigger during its lockout");
        var shieldUser=new Target(w);shieldUser.setPos(8,100,8);shieldUser.setItemSlot(EquipmentSlot.HEAD,piece(ArmorPiece.HELM_OF_SAINT_14));
        blocking=new net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent(shieldUser,sc,true);
        blocking.setBlockedDamage(5);ArmorEffects.shieldBlock(blocking);
        ok(shieldUser.getAbsorptionAmount()==4,"Saint ward accepts a successful vanilla shield block event");
        grunt.discard();p.setItemSlot(EquipmentSlot.HEAD,piece(ArmorPiece.MASK_OF_BAKRIS));
        var obstacle=new BlockPos(8,100,12);w.setBlockAndUpdate(obstacle,Blocks.STONE.defaultBlockState());w.setBlockAndUpdate(obstacle.above(),Blocks.STONE.defaultBlockState());
        p.setDeltaMovement(Vec3.ZERO);var before=p.position();
        ok("".equals(BinderState.cast(p,3)) && p.getZ()>before.z+1 && p.getBoundingBox().maxZ<=12.001,
            "Bakris shifts forward and stops its entire collision box before a solid wall");
        var moved=p.position();ok(BinderState.cast(p,3)==null && p.position().equals(moved),"failed repeat cast cannot shift twice");
        var border=w.getWorldBorder();double oldSize=border.getSize(),oldX=border.getCenterX(),oldZ=border.getCenterZ();
        try{
            border.setCenter(8.5,8.5);border.setSize(4);p.setPos(8.5,100,8.5);p.setDeltaMovement(0,0,.96);
            ArmorEffects.afterCast(p,BinderCatalog.get("marksmans_dodge"));
            ok(p.getZ()>8.5 && p.getBoundingBox().maxZ<=10.501,"Bakris respects the world border");
        }finally{border.setSize(oldSize);border.setCenter(oldX,oldZ);}
    }
    static Mob observed;
    static final Map<String,Integer> found=new HashMap<>();
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void collect(LivingDropsEvent e){
        if(e.getEntity()!=observed)return;
        for(var drop:e.getDrops()) {
            var stack=drop.getItem();
            String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            found.merge(key,stack.getCount(),Integer::sum);
        }
    }
    static void starter(ServerLevel world)throws Exception {
        var player=new Target(world);player.setPos(8,90,7);
        for(int i=0;i<2;i++){
            observed=mob(world,"vex_goblin");observed.setPos(8,90,8);observed.tickCount=12;
            world.addFreshEntity(observed);observed.hurt(world.damageSources().playerAttack(player),5000);
        }
        ok(found.getOrDefault("destiny2mc:conduit_lens",0)==2 && found.getOrDefault("destiny2mc:mote_of_light",0)==2,
            "two player-credited Vex Goblin deaths pay both starter piece salvage and mote costs");
        observed=null;
    }
    static void loot(ServerLevel w)throws Exception{
        var killer=new Target(w);for(String boss:List.of("atheon_boss","crota","valus_ta_aurc")){
            var mob=mob(w,boss);var items=new ArrayList<ItemEntity>();
            var damage=w.damageSources().playerAttack(killer);
            ArmorDrops.drops(new LivingDropsEvent(mob,damage,items,true));ok(items.size()==1&&items.getFirst().getItem().getItem() instanceof ArsenalArmorItem,"boss awards a raid piece "+boss);
            items.clear();ArmorDrops.drops(new LivingDropsEvent(mob,damage,items,false));if(!items.isEmpty())throw new AssertionError("uncredited loot");
        }
        var items=new ArrayList<ItemEntity>();var dreg=mob(w,"fallen_dreg");ArmorDrops.drops(new LivingDropsEvent(dreg,w.damageSources().playerAttack(killer),items,true));
        ok(items.isEmpty(),"ordinary kills keep ordinary salvage");
    }
    @SubscribeEvent public static void run(ServerStartedEvent event)throws Exception{
        var w=event.getServer().overworld();w.getChunk(0,0);event.getServer().setDifficulty(Difficulty.NORMAL,true);
        try{recipes(w);sets(w);powers(w);loot(w);starter(w);System.out.println("ARMOR SURVIVAL PASS");}
        catch(Throwable t){t.printStackTrace();throw new AssertionError("Armor fixture failed",t);}
    }
}
