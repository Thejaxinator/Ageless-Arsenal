package com.jax.destiny2mc.armor;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Canonical registry, equipment and inspection metadata. IDs belong to saved ItemStacks. */
public enum ArmorPiece {
    BATTLECAGE_OF_KABR("Battlecage of Kabr", Suit.KABR, Type.HELMET),
    KABRS_WRATH("Kabr's Wrath", Suit.KABR, Type.CHESTPLATE),
    LIGHT_OF_THE_GREAT_PRISM("Light of the Great Prism", Suit.KABR, Type.LEGGINGS),
    KABRS_FORCEFUL_GREAVES("Kabr's Forceful Greaves", Suit.KABR, Type.BOOTS),
    WILLBREAKERS_WATCH("Willbreaker's Watch", Suit.WILLBREAKER, Type.HELMET),
    WILLBREAKERS_RESOLVE("Willbreaker's Resolve", Suit.WILLBREAKER, Type.CHESTPLATE),
    MARK_OF_THE_PIT("Mark of the Pit", Suit.WILLBREAKER, Type.LEGGINGS),
    WILLBREAKERS_GREAVES("Willbreaker's Greaves", Suit.WILLBREAKER, Type.BOOTS),
    MASK_OF_RULL("Mask of Rull", Suit.RULL, Type.HELMET),
    CHASSIS_OF_RULL("Chassis of Rull", Suit.RULL, Type.CHESTPLATE),
    SHADOWS_MARK("Shadow's Mark", Suit.RULL, Type.LEGGINGS),
    GREAVES_OF_RULL("Greaves of Rull", Suit.RULL, Type.BOOTS),
    HELM_OF_SAINT_14("Helm of Saint-14", Suit.SAINT14, Type.HELMET),
    THE_ARMAMENTARIUM("The Armamentarium", Suit.ARMAMENTARIUM, Type.CHESTPLATE),
    MASK_OF_BAKRIS("Mask of Bakris", Suit.BAKRIS, Type.HELMET);

    public final String id, displayName, setName, source, trait, description;
    public final boolean exotic;
    public final int defense, durability;
    public final float toughness, knockback;
    public final Suit suit;
    public final Type type;

    ArmorPiece(String name, Suit suit, Type type) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.displayName = name;
        this.suit = suit;
        this.type = type;
        this.setName = suit.label;
        this.source = suit.source;
        this.trait = suit.trait;
        this.description = suit.description;
        this.exotic = suit.exotic;
        this.defense = suit.protection.get(type);
        this.durability = type.getDurability(suit.multiplier);
        this.toughness = suit.toughness;
        this.knockback = suit.knockback;
    }

    public static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc",path);
    }

    public enum Suit {
        KABR("Kabr Suit", "Vault of Glass • Vex", "Glass Lattice",
            "Wear any two Kabr pieces to take 18% less projectile damage.", "conduit_lens",
            2,6,5,2,17,.5f,0,false),
        WILLBREAKER("Willbreaker", "Crota's End • Hive", "Sword Logic",
            "Wear any two Willbreaker pieces to take 18% less damage from direct attacks by hostile creatures.", "runed_marrow",
            3,6,5,2,19,1,0,false),
        RULL("Rull Suit", "Leviathan • Cabal", "Siege Plating",
            "Wear any two Rull pieces to take 25% less explosion damage.", "breech_seal",
            3,7,5,3,22,1,.025f,false),
        SAINT14("Saint-14", "The Last City • Titan", "Starless Night",
            "Block a hit with a shield or cast a barricade for 2 absorption hearts for 5s. Nearby hostiles are blinded and slowed for 3s. Recharges in 12s.", "conduit_lens",
            3,8,6,3,30,2,0,true),
        ARMAMENTARIUM("Armamentarium", "The Last City • Titan", "And Another Thing",
            "Gain one bonus grenade cast every 40s. A charged reserve leaves the thrown grenade ready for a second cast. All grenades share this reserve.", "breech_seal",
            3,8,6,3,30,2,0,true),
        BAKRIS("House Salvation", "Europa • Fallen", "Light Shift",
            "A Binder dodge shifts you up to 5 blocks along a clear path. Nearby hostiles at the destination are slowed for 3s.", "ether_induction_coil",
            3,8,6,3,30,2,0,true);

        public final String id,label,source,trait,description,repairItem;
        public final Map<Type,Integer> protection;
        public final int multiplier;
        public final float toughness,knockback;
        public final boolean exotic;
        public final Holder<ArmorMaterial> holder;
        Suit(String label,String source,String trait,String description,String repairItem,int head,int chest,int legs,int feet,
             int multiplier,float toughness,float knockback,boolean exotic) {
            this.id=name().toLowerCase(Locale.ROOT);
            this.label=label;this.source=source;this.trait=trait;this.description=description;this.repairItem=repairItem;
            this.protection=Map.of(Type.HELMET,head,Type.CHESTPLATE,chest,Type.LEGGINGS,legs,Type.BOOTS,feet,Type.BODY,0);
            this.multiplier=multiplier;this.toughness=toughness;this.knockback=knockback;this.exotic=exotic;
            this.holder=DeferredHolder.create(Registries.ARMOR_MATERIAL,ResourceLocation.fromNamespaceAndPath("destiny2mc",id));
        }
        ArmorMaterial material() {
            Supplier<Ingredient> repair=()->Ingredient.of(BuiltInRegistries.ITEM.get(key(repairItem)));
            return new ArmorMaterial(protection,exotic?16:12,SoundEvents.ARMOR_EQUIP_IRON,repair,
                List.of(new ArmorMaterial.Layer(key(id))),toughness,knockback);
        }
    }
}
