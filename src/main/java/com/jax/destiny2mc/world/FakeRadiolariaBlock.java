package com.jax.destiny2mc.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Luminous white Vex radiolaria ("fake radiolaria") hazard block.
 * Non-fluid passable block that emits bright light and deals minor arc-like contact damage
 * to non-Vex entities while touching it.
 */
public class FakeRadiolariaBlock extends Block {
    public static final ResourceKey<DamageType> RADIOLARIA_DAMAGE = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath("destiny2mc", "radiolaria")
    );

    private static final float CONTACT_DAMAGE = 1.0F;
    private static final int DAMAGE_INTERVAL_TICKS = 12;

    public FakeRadiolariaBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(1.5F, 6.0F)
            .lightLevel(state -> 14)
            .noCollission()
            .noOcclusion()
            .sound(SoundType.AMETHYST)
            .isValidSpawn((state, level, pos, type) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
        );
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.84D, 0.80D, 0.84D));
        if (level.isClientSide) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        if (isVexEntity(entity)) {
            return;
        }
        if (entity.tickCount % DAMAGE_INTERVAL_TICKS == 0) {
            DamageSource source = createDamageSource(level);
            living.hurt(source, CONTACT_DAMAGE);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0 && level.getBlockState(pos.above()).isAir()) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.96D;
            double z = pos.getZ() + random.nextDouble();
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.04D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.02D, 0.0D);
            }
        }
    }

    private static DamageSource createDamageSource(Level level) {
        var holder = level.registryAccess()
            .registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolder(RADIOLARIA_DAMAGE);
        if (holder.isPresent()) {
            return new DamageSource(holder.get());
        }
        return level.damageSources().magic();
    }

    private static boolean isVexEntity(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (!"destiny2mc".equals(key.getNamespace())) {
            return false;
        }
        String path = key.getPath();
        return path.startsWith("vex_")
            || path.equals("atheon_boss")
            || path.equals("protheon_boss")
            || path.equals("persys_boss")
            || path.equals("templar");
    }
}
