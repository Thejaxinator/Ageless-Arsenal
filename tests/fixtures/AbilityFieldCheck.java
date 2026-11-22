package ageless.validation;

import java.util.*;
import com.jax.destiny2mc.binder.*;
import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.weapon.*;
import com.jax.destiny2mc.item.DestinyGunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Real-level fixture. Only load in an isolated development server. Clears a small platform,
 * registers a synthetic Guardian in the level entity index, and resolves normal server ticks. */
@EventBusSubscriber(modid="binder_field_check")
public final class AbilityFieldCheck {
    static ServerLevel level;
    static FakePlayer p;
    static Husk victim;
    static int index,step;
    static boolean finished,hold,sampled;
    static double x;
    static BinderAbility a;
    static ItemStack weapon;
    static void check(boolean success,String label){
        if(!success)throw new AssertionError(label);
        System.out.println("FIELD CHECK "+label);
    }
    static void prepare(){
        var all=ActiveAbilities.catalog();
        check(all.size()==30&&all.stream().map(BinderAbility::id).distinct().count()==30,"30 unique active keys");
        for(String element:List.of("solar","arc","void","stasis","strand","prismatic"))
            for(String category:List.of("grenade","melee"))
                check(all.stream().filter(k->k.category().equals(category)&&k.element().equals(element)).count()==2,
                      element+" "+category+" pair");
        check(all.stream().filter(k->k.category().equals("class")).count()==6,"six class abilities");
        level.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false,level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,level.getServer());
        for(int i=0;i<3;i++){
            int cx=8+24*i;level.setChunkForced(cx>>4,0,true);level.setChunkForced((cx+9)>>4,0,true);
            for(int xx=cx-6;xx<=cx+6;xx++)for(int z=0;z<=15;z++)level.setBlockAndUpdate(new BlockPos(xx,99,z),Blocks.STONE.defaultBlockState());
        }
        p=FakePlayerFactory.getMinecraft(level);p.setGameMode(GameType.SURVIVAL);
        p.moveTo(8.5,100,3.5,0,0);
        level.addNewPlayer(p);
        check(level.getEntity(p.getUUID())==p,"caster tracked in server level");
        check(!ActiveAbilities.cast(p,"missing_identity"),"unknown ability rejected");
        // Shorten the disposable suite without changing the count or order of simulation ticks.
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"tick sprint 2400");
    }
    static void begin(){
        a=ActiveAbilities.catalog().get(index);x=8.5+24*(index%3);
        p.removeAllEffects();p.setHealth(10);p.invulnerableTime=0;p.setDeltaMovement(Vec3.ZERO);
        p.setPose(Pose.STANDING);p.setShiftKeyDown(false);p.moveTo(x,100,3.5,0,6);
        if(victim!=null)victim.discard();
        victim=EntityType.HUSK.create(level);victim.moveTo(x,100,5.75,180,0);victim.setNoAi(true);victim.setNoGravity(true);
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);victim.setHealth(80);
        level.addFreshEntity(victim);
        weapon=ModRegistries.createWeaponStackByIndex(5);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,weapon);
        GunDataHelper.setInt(weapon,"d2_mag",0);
        if(a.id().equals("gamblers_dodge")){
            // Seed a veteran's saved loadout with one cooling melee before casting the dodge.
            String melee="incinerator_snap";
            CompoundTag ledger=new CompoundTag(),owned=new CompoundTag(),clocks=new CompoundTag();
            owned.putBoolean(melee,true);clocks.putLong(melee,level.getGameTime()+180);
            ledger.put("Unlocked",owned);ledger.put("ReadyAt",clocks);ledger.putString("Slot2",melee);
            p.getPersistentData().put("AgelessEnergyBinder",ledger);
            check(BinderState.snapshot(p).readyAt().containsKey(melee),"equipped melee cooling before dodge");
        }
        if(a.id().equals("rally_barricade")){
            p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);
            GunDataHelper.setLong(weapon,"d2_rel_end",level.getGameTime()+100);
            victim.moveTo(x,99.45,6.75,180,0);
        }
        check(ActiveAbilities.cast(p,a.id()),a.id()+" accepted");sampled=false;hold=false;
    }
    static void sample(){
        // The fake connection does not perform client movement input. Advance held inventory using
        // the same item implementation and intentionally stay in the cast area.
        if(a.id().equals("marksmans_dodge"))
            ((DestinyGunItem)weapon.getItem()).inventoryTick(weapon,level,p,0,true);
        if(a.id().equals("towering_barricade")||a.id().equals("rally_barricade")){
            if(step==6){
                p.setDeltaMovement(Vec3.ZERO);p.moveTo(x,100,3.5,0,0);
                if(a.id().equals("rally_barricade")){p.setPose(Pose.CROUCHING);victim.moveTo(x,98.95,6.75,0,0);}
                float previous=p.getHealth();p.hurt(p.damageSources().mobAttack(victim),4);
                hold=p.getHealth()==previous;
            }
        }
        if(a.id().equals("empowering_rift")&&step==6)victim.hurt(p.damageSources().playerAttack(p),4);
        if(a.category().equals("grenade")||a.category().equals("melee")){
            if(a.id().equals("healing_grenade"))sampled|=p.getHealth()>10;
            else sampled|=victim.getHealth()<80;
        } else switch(a.id()){
            case "gamblers_dodge" -> sampled|=!BinderState.snapshot(p).readyAt().containsKey("incinerator_snap");
            case "healing_rift" -> sampled|=p.getHealth()>10;
            case "empowering_rift" -> sampled|=victim.getHealth()<75.8f;
            case "marksmans_dodge" -> sampled|=GunDataHelper.getMag(weapon,((DestinyGunItem)weapon.getItem()).spec)>0;
            case "towering_barricade","rally_barricade" -> sampled|=hold;
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(finished)return;
        try{
            if(level==null){level=e.getServer().overworld();prepare();}
            if(step==0)begin();else sample();
            if(++step>=68){
                check(sampled,a.id()+" gameplay response");
                if(++index>=ActiveAbilities.catalog().size()){
                    finished=true;System.out.println("FIELD COMPLETE");return;
                }
                step=0;
            }
        }catch(Throwable ex){finished=true;System.out.println("FIELD FAILED "+ex);ex.printStackTrace();}
    }
}
