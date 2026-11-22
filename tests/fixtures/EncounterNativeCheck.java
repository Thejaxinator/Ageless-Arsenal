package ageless.validation;

import com.jax.destiny2mc.binder.difficulty.EncounterPreset;
import com.jax.destiny2mc.binder.difficulty.EncounterSettings;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Disposable native damage-pipeline test. Never install fixture mods on a valued world. */
@EventBusSubscriber(modid="encounter_check")
public final class EncounterNativeCheck {
    private static final class Target extends FakePlayer {
        Target(ServerLevel level,String id){
            super(level,new GameProfile(UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8)),id));
            setGameMode(GameType.SURVIVAL);
            // A freshly constructed server player has 60 ticks of join protection and FakePlayer.tick() is empty.
            try {
                var grace = net.minecraft.server.level.ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
                grace.setAccessible(true);
                grace.setInt(this, 0);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unable to expire the fixture join protection", e);
            }
        }
        // The production FakePlayer is deliberately invulnerable. Permit tests to exercise hurt().
        @Override public boolean isInvulnerableTo(DamageSource s){return false;}
    }
    static void check(boolean condition,String what){if(!condition)throw new AssertionError(what);}
    static void close(float a,float b,String what){check(Math.abs(a-b)<.005f,what+" expected "+b+" actual "+a);}
    static Mob mob(ServerLevel l,String id){return (Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",id)).create(l);}
    static float loss(Player p,DamageSource source,float raw){
        p.invulnerableTime=0;p.setHealth(20);
        check(p.hurt(source,raw),"damage rejected: "+source);
        return 20-p.getHealth();
    }
    static Target armor(ServerLevel l,String id){
        var target=new Target(l,id);
        var enchant=l.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.PROTECTION);
        var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        var items=new Item[]{Items.NETHERITE_HELMET,Items.NETHERITE_CHESTPLATE,Items.NETHERITE_LEGGINGS,Items.NETHERITE_BOOTS};
        for(int i=0;i<slots.length;i++){
            var piece=new ItemStack(items[i]);piece.enchant(enchant,4);target.setItemSlot(slots[i],piece);
            // FakePlayer.tick() is intentionally empty; apply the armor's real slot modifiers.
            piece.forEachModifier(slots[i],(attr,modifier)->{
                var instance=target.getAttribute(attr);
                if(instance!=null&&!instance.hasModifier(modifier.id()))instance.addTransientModifier(modifier);
            });
        }
        check(target.getArmorValue()==20 && target.getAttributeValue(Attributes.ARMOR_TOUGHNESS)==12,"netherite attributes");
        return target;
    }
    @SubscribeEvent public static void run(ServerStartedEvent e){
        try{
            var server=e.getServer();var world=server.overworld();server.setDifficulty(Difficulty.NORMAL,true);
            var admin=new Target(world,"encounter_admin");var normal=new Target(world,"encounter_guest");
            var settings=EncounterSettings.get(world);server.getPlayerList().op(admin.getGameProfile());
            var dreg=mob(world,"fallen_dreg");var valus=mob(world,"valus_ta_aurc");
            check(settings.preset()==EncounterPreset.PATROL,"Patrol default");
            check(!settings.choose(normal,"original",settings.revision()),"non-operator edit");
            check(!settings.choose(admin,"original",settings.revision()-1),"stale revision");
            var bolt=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("destiny2mc","shock_bolt")).create(world);
            var shot=world.damageSources().thrown(bolt,dreg);
            float dregHP=loss(normal,shot,4.5f);
            float nextHP=loss(normal,shot,4.5f);
            close(dregHP,1.71f,"unarmored Dreg bolt");close(nextHP,1.1115f,"rapid Dreg bolt");
            float bossHP=loss(armor(world,"armor_patrol"),world.damageSources().mobAttack(valus),10);
            check(bossHP>0 && bossHP<1,"Protection IV netherite on Patrol");
            var env=new Target(world,"environment");close(loss(env,world.damageSources().generic(),4),4,"unrelated damage");
            check(settings.choose(admin,"original",settings.revision()),"operator changed preset");
            close(loss(new Target(world,"original_dreg"),shot,4.5f),4.5f,"original Dreg bolt");
            float bossOriginal=loss(armor(world,"armor_original"),world.damageSources().mobAttack(valus),10);
            check(bossHP<bossOriginal,"boss mitigation");
            var nbt=settings.save(new net.minecraft.nbt.CompoundTag(),world.registryAccess());
            check(nbt.getString("Preset").equals("original") && nbt.getInt("Revision")>0,"saved preset");
            System.out.printf(java.util.Locale.ROOT,"ENCOUNTER Dreg %.4f HP; rapid bolt %.4f HP; Protection IV netherite Valus %.4f HP Patrol and %.4f HP Original%n",dregHP,nextHP,bossHP,bossOriginal);
            System.out.println("ENCOUNTER COMPLETE");
        }catch(Throwable failure){failure.printStackTrace();System.out.println("ENCOUNTER FAILED "+failure);}
    }
}
