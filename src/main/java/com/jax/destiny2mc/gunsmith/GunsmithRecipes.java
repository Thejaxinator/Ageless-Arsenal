package com.jax.destiny2mc.gunsmith;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.ageless.materials.GunsmithBlueprint;
import com.jax.destiny2mc.weapon.ServerBallisticsEngine;
import com.jax.destiny2mc.weapon.WeaponSpec;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class GunsmithRecipes extends LegacyGunsmithRecipes {
    // Seven non-weapon recipes occupy the saved positions 40..46. Do not renumber these.
    private static final int OFFSET = 7;
    private static final int FIRST_EXTENDED = 47;
    private static final int FIRST_BLUEPRINT = 59;
    public static final int COUNT = WeaponSpec.ALL.length + OFFSET;

    public static int recipeForWeapon(WeaponSpec weapon) {
        return weapon.index < 40 ? weapon.index : weapon.index + OFFSET;
    }

    /** Let the live UI omit pack-disabled rows without building 70 NBT-populated guns per frame. */
    public static boolean visible(Player player, WeaponSpec weapon) {
        return recipeForWeapon(weapon) < FIRST_BLUEPRINT || GunsmithBlueprint.find(player, weapon.id) != null;
    }

    public static ItemStack createResultStack(int recipe) {
        if (recipe < 0 || recipe >= COUNT) return ItemStack.EMPTY;
        if (recipe < FIRST_EXTENDED) return LegacyGunsmithRecipes.createResultStack(recipe);
        return ModRegistries.createWeaponStackByIndex(recipe - OFFSET);
    }

    // Retained two-argument accessors for earlier compiled callers asking for fixed recipes.
    // A pack-defined cost must be requested with a player/world via costs().
    public static Item getIngredientItem(int r, int s) {
        if (r < 0 || r >= FIRST_BLUEPRINT || s < 0 || s >= 3) return Items.AIR;
        if (r < FIRST_EXTENDED) return LegacyGunsmithRecipes.getIngredientItem(r,s);
        return switch(s) {
            case 0 -> Items.IRON_INGOT;
            case 1 -> Items.REDSTONE;
            default -> (Item) ModRegistries.PLUNDERED_ETHER_ALLOY.get();
        };
    }
    public static int getIngredientCount(int r, int s) {
        if (r < 0 || r >= FIRST_BLUEPRINT || s < 0 || s >= 3) return 0;
        if (r < FIRST_EXTENDED) return LegacyGunsmithRecipes.getIngredientCount(r,s);
        return switch(s) { case 0 -> 16; case 1 -> 12; default -> WeaponSpec.ALL[r-OFFSET].isExotic ? 8 : 4; };
    }
    public static String getResultTitle(int r) {
        if (r < 0 || r >= COUNT) return "";
        if (r >= FIRST_EXTENDED) return WeaponSpec.ALL[r-OFFSET].displayName;
        return r == 46 ? "Ageless Arsenals Field Guide" : LegacyGunsmithRecipes.getResultTitle(r);
    }
    public static String getResultSubtitle(int r) {
        if (r < 0 || r >= COUNT) return "";
        return r >= FIRST_EXTENDED ? WeaponSpec.ALL[r-OFFSET].frameLabel : LegacyGunsmithRecipes.getResultSubtitle(r);
    }

    public static List<GunsmithBlueprint.Cost> costs(Player p, int recipe) {
        if (p == null || recipe < 0 || recipe >= COUNT) return List.of();
        if (recipe >= FIRST_BLUEPRINT) {
            var blueprint = GunsmithBlueprint.find(p, WeaponSpec.ALL[recipe-OFFSET].id);
            return blueprint == null ? List.of() : blueprint.costs();
        }
        var costs = new java.util.ArrayList<GunsmithBlueprint.Cost>(3);
        for (int i = 0; i < 3; i++) {
            Item item = getIngredientItem(recipe, i);
            int count = getIngredientCount(recipe, i);
            if (item != null && item != Items.AIR && count > 0) costs.add(new GunsmithBlueprint.Cost(item,count));
        }
        return costs;
    }

    private static boolean affordable(Player p, List<GunsmithBlueprint.Cost> costs) {
        if (p == null || p.isSpectator() || costs.isEmpty()) return false;
        return p.isCreative() || costs.stream().allMatch(c -> countInInventory(p, c.item()) >= c.count());
    }
    public static boolean canCraft(Player p, int recipe) {
        return affordable(p, costs(p, recipe));
    }

    public static void tryCraftOnServer(ServerPlayer p, int recipe) {
        // One server-thread transaction. Resolve the authoritative recipe once so a rejected
        // request, depleted inventory or disabled data-pack entry consumes nothing.
        var costs = costs(p, recipe);
        if (!affordable(p, costs)) return;
        var result = createResultStack(recipe);
        if (result.isEmpty()) return;
        if (!p.isCreative()) for (var c : costs) consumeFromInventory(p,c.item(),c.count());
        if (!p.addItem(result)) p.drop(result,false);
        p.getInventory().setChanged();
        p.containerMenu.broadcastChanges();
        ServerBallisticsEngine.playSound(p.level(),p.getX(),p.getY(),p.getZ(),"block.smithing_table.use",.8f,1.2f);
    }
}
