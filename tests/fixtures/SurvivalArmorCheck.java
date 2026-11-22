package ageless.gearqa;

import com.jax.destiny2mc.binder.difficulty.EncounterPreset;
import com.jax.destiny2mc.binder.difficulty.EncounterSettings;
import com.mojang.authlib.GameProfile;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Measures one first impact per fresh, unenchanted character, including the actual Patrol
 * difficulty hook, set traits and vanilla armor/toughness calculation on a dedicated server. */
@EventBusSubscriber(modid="survival_gear_check")
public final class SurvivalArmorCheck {
    static final class Target extends FakePlayer {
        Target(ServerLevel world) throws ReflectiveOperationException {
            super(world,new GameProfile(UUID.randomUUID(),"GearTarget"));
            setGameMode(GameType.SURVIVAL);
            var field=net.minecraft.server.level.ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            field.setAccessible(true);
            field.setInt(this,0);
        }
        @Override public boolean isInvulnerableTo(DamageSource source) { return false; }
    }

    static float impact(ServerLevel world, DamageSource source, float incoming, String... ids) throws Exception {
        Target player=new Target(world);
        for (String key:ids) {
            var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)));
            var slot=((ArmorItem)stack.getItem()).getEquipmentSlot();
            player.setItemSlot(slot,stack);
            // Fake players do not tick equipment reconciliation. Use the same item modifiers.
            stack.forEachModifier(slot,(attribute,modifier)->player.getAttribute(attribute).addTransientModifier(modifier));
        }
        player.setHealth(20);
        if (!player.hurt(source,incoming))throw new AssertionError("Server rejected hit");
        return 20-player.getHealth();
    }

    @SubscribeEvent public static void run(ServerStartedEvent event) throws Exception {
        var server=event.getServer();var world=server.overworld();
        server.setDifficulty(Difficulty.NORMAL,true);
        if(EncounterSettings.get(world).preset()!=EncounterPreset.PATROL)throw new AssertionError("Expected default Patrol in fresh test world");
        var dreg=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("destiny2mc:fallen_dreg")).create(world);
        var bolt=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("destiny2mc:shock_bolt")).create(world);
        var shot=world.damageSources().thrown(bolt,dreg);
        String head="destiny2mc:battlecage_of_kabr",feet="destiny2mc:kabrs_forceful_greaves";
        float bare=impact(world,shot,4.5f);
        float first=impact(world,shot,4.5f,feet);
        float pair=impact(world,shot,4.5f,head,feet);
        float full=impact(world,shot,4.5f,head,feet,"destiny2mc:kabrs_wrath","destiny2mc:light_of_the_great_prism");
        if(!(Math.abs(bare-1.71f)<.005f && full>0 && full<pair && pair<first && first<bare))throw new AssertionError("Protection order");
        System.out.printf(Locale.ROOT,"SURVIVAL ARMOR in HP, first Dreg shock bolt on Normal/Patrol: unarmored %.4f; Kabr boots %.4f; Kabr helmet + boots %.4f; full Kabr %.4f.%n",bare,first,pair,full);
        System.out.println("SURVIVAL ARMOR PASS");
    }
}
