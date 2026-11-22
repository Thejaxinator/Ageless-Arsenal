package com.jax.destiny2mc.tenno.foundry;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** A synchronized, datapack-defined recipe for the Warframe page at either Gunsmith. */
public record TennoBlueprint(Item result, List<Material> materials) implements Recipe<RecipeInput> {
    public record Material(Item item, int count) {
        private static final Codec<Material> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Material::item),
            Codec.intRange(1, 64).fieldOf("count").forGetter(Material::count)
        ).apply(builder, Material::new));
    }

    static final RecipeType<TennoBlueprint> TYPE = new RecipeType<>() {};
    private static final MapCodec<TennoBlueprint> CODEC = RecordCodecBuilder
        .<TennoBlueprint>mapCodec(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(TennoBlueprint::result),
            Material.CODEC.listOf(1, 6).fieldOf("materials").forGetter(TennoBlueprint::materials)
        ).apply(builder, TennoBlueprint::new)).validate(recipe -> {
            if (!TennoBlueprints.isTenno(recipe.result))
                return DataResult.error(() -> "The blueprint result must be a registered Warframe weapon");
            var seen = new HashSet<Item>();
            for (Material row : recipe.materials) {
                if (row.item == Items.AIR || row.item == recipe.result || !seen.add(row.item))
                    return DataResult.error(() -> "Use a distinct non-air ingredient in each row, separate from the result");
            }
            return DataResult.success(recipe);
        });

    static final RecipeSerializer<TennoBlueprint> SERIALIZER = new RecipeSerializer<>() {
        public MapCodec<TennoBlueprint> codec() { return CODEC; }
        public StreamCodec<RegistryFriendlyByteBuf, TennoBlueprint> streamCodec() {
            return ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        }
    };

    public boolean matches(RecipeInput input, Level level) { return false; }
    public boolean canCraftInDimensions(int width, int height) { return false; }
    public boolean isSpecial() { return true; }
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider lookup) { return result.getDefaultInstance(); }
    public ItemStack getResultItem(HolderLookup.Provider lookup) { return result.getDefaultInstance(); }
    public RecipeType<?> getType() { return TYPE; }
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
}
