package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.ModRegistries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The Binder interaction opens a server-bound session at this exact block position. */
@EventBusSubscriber(modid="destiny2mc",bus=EventBusSubscriber.Bus.MOD)
public final class BinderContent {
    public static final ResourceLocation STATION = id("energy_binder");
    public static final ResourceLocation MOTE = id("mote_of_light");
    private static Block table;
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("destiny2mc",path); }
    @SubscribeEvent public static void register(RegisterEvent e) {
        e.register(Registries.BLOCK,h->{table=new Workstation(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.COPPER).noOcclusion().lightLevel(s->9));h.register(STATION,table);});
        e.register(Registries.ITEM,h->{h.register(MOTE,new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));h.register(STATION,new BlockItem(table,new Item.Properties()));});
    }
    @SubscribeEvent public static void tab(BuildCreativeModeTabContentsEvent e) {
        if(e.getTabKey().equals(ModRegistries.ARSENAL_TAB.getKey())){
            e.accept(BuiltInRegistries.ITEM.get(STATION)); e.accept(BuiltInRegistries.ITEM.get(MOTE));
        }
    }
    public static final class Workstation extends Block {
        private static final MapCodec<Workstation> CODEC=simpleCodec(Workstation::new);
        private static final VoxelShape SHAPE=Shapes.or(box(1,0,1,15,2,15), box(3,2,3,13,10,13), box(0,10,0,16,13,16));
        Workstation(Properties properties){super(properties);}
        @Override protected MapCodec<? extends Block> codec(){return CODEC;}
        @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
        @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
            if(player.isSpectator())return InteractionResult.FAIL;
            if(player instanceof ServerPlayer sp) BinderState.open(sp,pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
    @EventBusSubscriber(modid="destiny2mc")
    public static final class Loot {
        @SubscribeEvent public static void death(LivingDropsEvent e){
            var victim=e.getEntity();
            if(!(victim.level() instanceof ServerLevel level) || !FactionEnemies.isEnemy(victim) ||
               !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))return;
            boolean boss=FactionEnemies.isBoss(victim);
            var item=new ItemStack(BuiltInRegistries.ITEM.get(MOTE),boss?6:1);
            e.getDrops().add(new ItemEntity(level,victim.getX(),victim.getY()+0.25,victim.getZ(),item));
        }
    }
}
