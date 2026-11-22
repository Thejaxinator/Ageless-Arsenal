package com.jax.destiny2mc.ageless.materials;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Vanilla loads and synchronizes this custom recipe, including after /reload. Each blueprint
 * has three distinct counted costs because the existing bench presents precisely three rows.
 * Disallow duplicate inputs so one inventory stack can never satisfy two rows independently.
 */
public record GunsmithBlueprint(Item result, List<Cost> costs) implements Recipe<RecipeInput> {
    public record Cost(Item item, int count) {
        static final Codec<Cost> CODEC = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Cost::item),
            Codec.intRange(1, 64).fieldOf("count").forGetter(Cost::count)
        ).apply(i, Cost::new));
    }

    static final RecipeType<GunsmithBlueprint> TYPE = new RecipeType<>() {};
    private static final MapCodec<GunsmithBlueprint> CODEC = RecordCodecBuilder
        .<GunsmithBlueprint>mapCodec(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(GunsmithBlueprint::result),
            Cost.CODEC.listOf(3, 3).fieldOf("costs").forGetter(GunsmithBlueprint::costs)
        ).apply(i, GunsmithBlueprint::new)).validate(recipe -> {
            if (!(recipe.result instanceof DestinyGunItem)) return DataResult.error(() -> "Result must be a registered gun");
            var inputs = new HashSet<Item>();
            for (Cost cost : recipe.costs) {
                if (cost.item == Items.AIR || !inputs.add(cost.item)) return DataResult.error(() -> "Each cost needs a distinct, non-air item");
            }
            return DataResult.success(recipe);
        });
    static final RecipeSerializer<GunsmithBlueprint> SERIALIZER = new RecipeSerializer<>() {
        public MapCodec<GunsmithBlueprint> codec() { return CODEC; }
        public StreamCodec<RegistryFriendlyByteBuf,GunsmithBlueprint> streamCodec() {
            return ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        }
    };

    public static GunsmithBlueprint find(Player player, String weaponId) {
        var holder = player.level().getRecipeManager().byKey(FactionComponents.key("gunsmith/" + weaponId));
        if (holder.isPresent() && holder.get().value() instanceof GunsmithBlueprint bp
                && bp.result instanceof DestinyGunItem gun && gun.spec.id.equals(weaponId)) return bp;
        return null;
    }

    // No crafting grid can claim this recipe. The bench validates and consumes inventory costs.
    public boolean matches(RecipeInput input, Level level) { return false; }
    public boolean canCraftInDimensions(int w, int h) { return false; }
    public boolean isSpecial() { return true; }
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider lookup) { return getResultItem(lookup); }
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
        return ModRegistries.createWeaponStackByIndex(((DestinyGunItem)result).spec.index);
    }
    public RecipeType<?> getType() { return TYPE; }
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
}
