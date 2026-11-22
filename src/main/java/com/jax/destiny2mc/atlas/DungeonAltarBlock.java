package com.jax.destiny2mc.atlas;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
public final class DungeonAltarBlock extends Block {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 private final String site;
 public DungeonAltarBlock(String site){super(Properties.of().strength(4,1200).lightLevel(s->9));this.site=site;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH));}
 @Override protected MapCodec<? extends Block> codec(){return simpleCodec(p->new DungeonAltarBlock(site));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
  if(p.isSpectator())return InteractionResult.FAIL;
  if(l instanceof ServerLevel server&&p instanceof ServerPlayer player){
   if(p.isShiftKeyDown()){p.displayClientMessage(Component.literal("Open the field guide for this encounter's mechanics."),true);return InteractionResult.CONSUME;}
   DungeonEncounters.start(server,player,DungeonSites.site(site),pos,pos.relative(s.getValue(FACING),12));
  }
  return InteractionResult.sidedSuccess(l.isClientSide);
 }
}
