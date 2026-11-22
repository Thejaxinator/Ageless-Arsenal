package ageless.validation;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.ageless.materials.BenchAccess;
import com.jax.destiny2mc.block.GunsmithBlock;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import com.jax.destiny2mc.network.GunsmithCraftPayload;
import com.jax.destiny2mc.network.GunsmithPerkSwapPayload;
import com.jax.destiny2mc.tenno.foundry.FoundryNetwork;
import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.ModBenchNetwork;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import com.jax.destiny2mc.tenno.mods.WeaponMods;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;

@EventBusSubscriber(modid = "arsenal_station_regression")
public class StationRegression {
    static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", name);
    }

    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    /** Run the real registered server handlers synchronously on the server's own thread.
     * Only delivery and the fake player's outgoing connection are replaced in this test. */
    @SuppressWarnings("unchecked")
    static void deliver(FakePlayer player, CustomPacketPayload message) throws ReflectiveOperationException {
        var registrations = NetworkRegistry.class.getDeclaredField("PAYLOAD_REGISTRATIONS");
        registrations.setAccessible(true);
        var byProtocol = (Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<CustomPacketPayload>>>) registrations.get(null);
        var handler = byProtocol.get(ConnectionProtocol.PLAY).get(message.type().id()).handler();
        var context = (IPayloadContext) Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),
            new Class<?>[]{IPayloadContext.class}, (proxy, method, args) -> switch (method.getName()) {
                case "player" -> player;
                case "listener" -> player.connection;
                case "enqueueWork" -> {
                    if (args[0] instanceof Runnable work) {
                        work.run();
                        yield CompletableFuture.completedFuture(null);
                    }
                    yield CompletableFuture.completedFuture(((Supplier<?>) args[0]).get());
                }
                default -> throw new AssertionError("Unexpected payload context method " + method.getName());
            });
        handler.handle(message, context);
    }

    static void useBlock(FakePlayer p, ServerLevel level, BlockPos bench) {
        var hit = new BlockHitResult(Vec3.atCenterOf(bench), Direction.SOUTH, bench, false);
        var result = p.gameMode.useItemOn(p, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
        check(result.consumesAction(), "physical use accepted");
        check(BenchAccess.permits(p), "real RightClickBlock event established session");
    }

    static void addDestinyCosts(FakePlayer p) {
        var costs = GunsmithRecipes.costs(p, 0);
        check(!costs.isEmpty(), "live Destiny recipe");
        for (var cost : costs) check(p.addItem(new ItemStack(cost.item(), cost.count())), "add cost");
    }

    static void addWarframeCosts(FakePlayer p, Item result) {
        for (var cost : TennoBlueprints.cost(p, result)) check(p.addItem(cost.copy()), "add blueprint cost");
    }

    static void verifyBench(ServerLevel level, String benchName, int x) throws ReflectiveOperationException {
        var bench = new BlockPos(x, 100, 3);
        var block = BuiltInRegistries.BLOCK.get(id(benchName));
        check(block instanceof GunsmithBlock, "registered physical block " + benchName);
        level.setBlockAndUpdate(bench, block.defaultBlockState());
        var p = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StationTest"));
        p.setGameMode(GameType.SURVIVAL);
        p.setPos(x + .5, 100, 4.5);
        p.getInventory().selected = 8;
        var destiny = GunsmithRecipes.createResultStack(0).getItem();
        var bratonId = id("braton");
        var braton = BuiltInRegistries.ITEM.get(bratonId);
        addDestinyCosts(p);
        addWarframeCosts(p, braton);
        var craft = new GunsmithCraftPayload(0);
        deliver(p, craft);
        deliver(p, new FoundryNetwork.Assemble(bratonId, 1));
        check(!BenchAccess.permits(p) && GunsmithRecipes.canCraft(p, 0) && TennoBlueprints.canCraft(p, braton),
            "unopened station refuses both funded crafts");
        check(p.getInventory().countItem(destiny) == 0 && p.getInventory().countItem(braton) == 0, "no items granted");

        useBlock(p, level, bench);
        deliver(p, craft);
        deliver(p, new FoundryNetwork.Assemble(bratonId, 2));
        check(p.getInventory().countItem(destiny) == 1 && p.getInventory().countItem(braton) == 1, "both survival outputs");
        check(!GunsmithRecipes.canCraft(p, 0) && !TennoBlueprints.canCraft(p, braton), "real materials debited");
        int destinySlot = p.getInventory().findSlotMatchingItem(GunsmithRecipes.createResultStack(0));
        int bratonSlot = p.getInventory().findSlotMatchingItem(braton.getDefaultInstance());
        check(destinySlot >= 0 && bratonSlot >= 0, "output stacks present");

        var weapon = p.getInventory().getItem(destinySlot);
        var spec = WeaponSpec.ALL[0];
        int nextPerk = WeaponPerkRegistry.getPoolOption(0, 4, 1);
        check(WeaponPerkRegistry.getActivePerk(weapon, spec, 4) != nextPerk, "initial perk");
        Item alloy = (Item) ModRegistries.PLUNDERED_ETHER_ALLOY.get();
        check(p.addItem(new ItemStack(alloy)) && p.addItem(new ItemStack(Items.COPPER_INGOT, 2)), "fund perk");
        deliver(p, new GunsmithPerkSwapPayload(destinySlot, 4, nextPerk));
        check(WeaponPerkRegistry.getActivePerk(weapon, spec, 4) == nextPerk &&
            p.getInventory().countItem(alloy) == 0 && p.getInventory().countItem(Items.COPPER_INGOT) == 0, "paid Destiny perk");

        var gun = p.getInventory().getItem(bratonSlot);
        ModSocketCheck.unlock(p, WeaponMod.SERRATION, 0);
        var bound = ModBenchNetwork.handle(p, new ModBenchNetwork.Command(1, 0, ModBenchNetwork.BIND, bratonSlot, bratonId, "", -1));
        check(bound.status() == ModBenchNetwork.OK && bound.token() != 0, "station bound the actual weapon");
        var install = new ModBenchNetwork.Command(2, bound.token(), ModBenchNetwork.INSTALL, bratonSlot, bratonId, "serration", 0);
        deliver(p, install);
        check(WeaponMods.installed(gun).contains(WeaponMod.SERRATION), "owned mod fitted through real packet handler");
        check(ModBenchNetwork.handle(p, install).status() == ModBenchNetwork.STALE, "replayed install rejected");

        // Keep both crafts funded so guard failures cannot be confused with insufficient funds.
        addDestinyCosts(p);
        addWarframeCosts(p, braton);
        deliver(p, new FoundryNetwork.Assemble(bratonId, 2));
        check(p.getInventory().countItem(braton) == 1 && TennoBlueprints.canCraft(p, braton), "replayed blueprint rejected");
        p.setPos(x + 30, 100, 4.5);
        check(!BenchAccess.permits(p), "remote access refused");
        deliver(p, craft);
        deliver(p, new FoundryNetwork.Assemble(bratonId, 3));
        check(TennoBlueprints.canCraft(p, braton) && GunsmithRecipes.canCraft(p, 0), "remote crafts preserve materials");
        p.setPos(x + .5, 100, 4.5);
        p.setGameMode(GameType.SPECTATOR);
        check(!BenchAccess.permits(p), "spectator guard");
        p.setGameMode(GameType.SURVIVAL);
        level.setBlockAndUpdate(bench, Blocks.AIR.defaultBlockState());
        check(!BenchAccess.permits(p), "removed block invalidates station");
        deliver(p, craft);
        deliver(p, new FoundryNetwork.Assemble(bratonId, 4));
        var remove = new ModBenchNetwork.Command(3,bound.token(),ModBenchNetwork.REMOVE,bratonSlot,bratonId,"serration",0);
        check(ModBenchNetwork.handle(p, remove).status() == ModBenchNetwork.NO_STATION, "stale station cannot remove card");
        check(WeaponMods.installed(gun).contains(WeaponMod.SERRATION) &&
            p.getInventory().countItem(destiny) == 1 && p.getInventory().countItem(braton) == 1 &&
            TennoBlueprints.canCraft(p, braton) && GunsmithRecipes.canCraft(p, 0), "no stale mutation or debit");
        System.out.println("STATION CHECK " + benchName + " opened by physical right-click; both crafts, paid perk and mod worked; unopened, distance, removed block, spectator and packet replay guards passed.");
    }

    @SubscribeEvent public static void run(ServerStartedEvent event) throws ReflectiveOperationException {
        WarframeFoundryCheck.verify(event);
        ModSocketCheck.run(event);
        verifyBench(event.getServer().overworld(), "gunsmith_fui_workbench", 5);
        verifyBench(event.getServer().overworld(), "gunsmith_mc_workbench", 9);
        System.out.println("STATION REGRESSION PASS");
    }
}
