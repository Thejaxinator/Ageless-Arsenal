package ageless.validation;

import com.jax.destiny2mc.tenno.foundry.TennoBlueprints;
import com.jax.destiny2mc.tenno.mods.ModPacks;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Loaded only by the disposable release validation server. */
@EventBusSubscriber(modid = "creative_tab_check")
public final class WarframeCreativeTabCheck {
    private static final List<String> EXPECTED = Arrays.asList(("""
        ax_52 grakata prisma_grakata braton braton_prime braton_vandal burston burston_prime ignis ignis_wraith
        cedo cedo_prime perigale perigale_prime soma soma_prime torid bubonico coda_bubonico
        twin_grakatas brakk kuva_brakk nukor kuva_nukor vesper_77 cantare aksondol
        amanata hate harmony heat_sword dual_heat_swords sun_and_moon
        mod_pack primary_mod_pack secondary_mod_pack melee_mod_pack
        """).strip().split("\\s+"));
    private static final List<ResourceLocation> WARFRAME = EXPECTED.stream().map(WarframeCreativeTabCheck::key).toList();

    private static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc",path);
    }
    private static List<ResourceLocation> ids(Collection<ItemStack> stacks) {
        return stacks.stream().map(s -> BuiltInRegistries.ITEM.getKey(s.getItem())).toList();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void note(String text) {
        System.out.println("CREATIVE TAB " + text);
    }
    private static String digest(List<String> lines) {
        try {
            var hash = MessageDigest.getInstance("SHA-256").digest(String.join("\n",lines).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static void verifySurvival(ServerLevel level) {
        var player = new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CreativeCatalog"));
        player.setGameMode(GameType.SURVIVAL);
        var arms = TennoBlueprints.entries(player);
        require(arms.size() == 33,"survival blueprints " + arms.size());
        var ax = BuiltInRegistries.ITEM.get(key("ax_52"));
        require(!TennoBlueprints.canCraft(player,ax),"empty inventory craft");
        for (var material : TennoBlueprints.cost(player,ax)) player.getInventory().add(material);
        require(TennoBlueprints.craft(player,ax) && player.getInventory().countItem(ax) == 1,"survival Foundry transaction");
        for (var pack : ModPacks.Pool.values()) {
            var base = BuiltInRegistries.ITEM.get(key("mod_pack"));
            var mats = switch (pack) {
                case ALL -> List.of(Items.PAPER,Items.PAPER,Items.COPPER_INGOT,Items.REDSTONE);
                case PRIMARY -> List.of(base,Items.IRON_INGOT);
                case SECONDARY -> List.of(base,Items.COPPER_INGOT);
                case MELEE -> List.of(base,Items.FLINT);
            };
            var grid = CraftingInput.of(2,mats.size()/2,mats.stream().map(Item::getDefaultInstance).toList());
            var match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,level).orElseThrow();
            require(match.id().equals(key(pack.id)),pack + " recipe");
            require(match.value().assemble(grid,level.registryAccess()).is(BuiltInRegistries.ITEM.get(key(pack.id))),pack + " result");
        }
        note("Survival lists 33 blueprints, crafts AX-52 with ingredients and assembles all four mod packs.");
    }

    @SubscribeEvent
    public static void run(ServerStartedEvent event) {
        var level = event.getServer().overworld();
        boolean baseline = Boolean.getBoolean("arsenal.navigation.base");
        var tabs = BuiltInRegistries.CREATIVE_MODE_TAB;
        var wf = tabs.get(key("warframe"));
        require((wf == null) == baseline, "Warframe registration differs from requested test mode");
        for (boolean operator : List.of(false,true)) {
            CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(),operator,level.registryAccess());
            var legacy = tabs.get(key("arsenal"));
            var legacyItems = ids(legacy.getDisplayItems());
            require(legacyItems.containsAll(List.of(key("energy_binder"),key("battlecage_of_kabr"),key("vex_sphinx_spawn_egg"))),"Paracausal assets");
            require(ids(tabs.getOrThrow(CreativeModeTabs.SPAWN_EGGS).getDisplayItems()).contains(key("vex_sphinx_spawn_egg")),"Sphinx egg location");
            require(ids(tabs.getOrThrow(CreativeModeTabs.COMBAT).getDisplayItems()).contains(ResourceLocation.withDefaultNamespace("iron_sword")),"combat sword");
            require(ids(tabs.getOrThrow(CreativeModeTabs.TOOLS_AND_UTILITIES).getDisplayItems()).contains(ResourceLocation.withDefaultNamespace("diamond_pickaxe")),"tools pickaxe");
            if (!baseline) {
                require(ids(wf.getDisplayItems()).equals(WARFRAME),"dedicated category membership or order " + ids(wf.getDisplayItems()));
                require(ids(wf.getSearchTabDisplayItems()).equals(WARFRAME),"native default search visibility");
                require(wf.getIconItem().is(BuiltInRegistries.ITEM.get(key("twin_grakatas"))),"Twin Grakatas icon");
                require(wf.getDisplayName().getContents() instanceof TranslatableContents title
                    && title.getKey().equals("itemGroup.destiny2mc.warframe"),"translated title");
                require(wf.tabsBefore.contains(key("arsenal")),"after Paracausal ordering");
            }
            var search = ids(CreativeModeTabs.searchTab().getDisplayItems());
            var legacyFingerprint = new ArrayList<String>();
            tabs.keySet().stream().sorted().forEach(id -> {
                var tab = tabs.get(id);
                if (tab == wf || tab.getType() != CreativeModeTab.Type.CATEGORY) return;
                var old = ids(tab.getDisplayItems());
                if (!baseline) require(Collections.disjoint(old,WARFRAME),id + " wrongly lists Warframe items");
                legacyFingerprint.add(id + "=" + old.stream().filter(i -> !WARFRAME.contains(i)).toList());
            });
            for (var id : WARFRAME) require(Collections.frequency(search,id) == 1,"search missing/duplicated " + id);
            // The driver compares the digest against a separate server running the original release.
            note("Legacy fingerprint operator=" + operator + " " + digest(legacyFingerprint));
        }
        if (!baseline) note("Ordered Warframe-only category: 19 primary, 8 secondary, 6 melee, 4 packs. All 37 appear once in native Search and in no other category; operator and standard players.");
        verifySurvival(level);
        note("COMPLETE");
    }
}
