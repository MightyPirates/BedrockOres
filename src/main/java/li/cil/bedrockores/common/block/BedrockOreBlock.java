package li.cil.bedrockores.common.block;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

/**
 * This block itself is configured like bedrock; however, it forwards pretty
 * much everything logic related to the underlying ore block, such as hardness
 * checks, harvesting (drops). It only does slightly custom rendering (a mask
 * over the underlying ore's model) and not actually breaking when harvested,
 * but instead reducing the remaining amount by one.
 */
public final class BedrockOreBlock extends BaseEntityBlock {
    public BedrockOreBlock() {
        super(Properties
                .of(Material.STONE)
                .strength(-1F, 3600000)
                .noLootTable()
                .isValidSpawn((state, reader, pos, entity) -> false));
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.BEDROCK_ORE.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // Block

    @Override
    public boolean onDestroyedByPlayer(final BlockState state, final Level level, final BlockPos pos, final Player player, final boolean willHarvest, final FluidState fluid) {
        if (player.isCreative()) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }

        if (!Settings.allowPlayerMining.get()) {
            return false;
        }

        final var tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof BedrockOreBlockEntity bedrockOre) {
            final var oreBlockState = bedrockOre.getOreBlockState();

            // Ignore result, expect drops to be handled by underlying ore.
            bedrockOre.extract();

            oreBlockState.getBlock().playerWillDestroy(level, pos, oreBlockState, player);
            oreBlockState.getBlock().playerDestroy(level, player, pos, oreBlockState, null, player.getMainHandItem().copy());
        }

        return true;
    }

    // --------------------------------------------------------------------- //
    // Forwarding to actual ore's Block

    // --------------------------------------------------------------------- //
    // BlockBehaviour

    @SuppressWarnings("deprecation")
    @Override
    public int getLightBlock(final BlockState state, final BlockGetter level, final BlockPos pos) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getLightBlock(level, pos);
        } else {
            return super.getLightBlock(state, level, pos);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getDestroyProgress(final BlockState state, final Player player, final BlockGetter level, final BlockPos pos) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getDestroyProgress(player, level, pos);
        } else {
            return super.getDestroyProgress(state, player, level, pos);
        }
    }

    // --------------------------------------------------------------------- //
    // IForgeBlock

    @Override
    public float getFriction(final BlockState state, final LevelReader level, final BlockPos pos, @Nullable final Entity entity) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getFriction(level, pos, entity);
        } else {
            return super.getFriction(state, level, pos, entity);
        }
    }


    @Override
    public int getLightEmission(final BlockState state, final BlockGetter level, final BlockPos pos) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getLightEmission(level, pos);
        } else {
            return super.getLightEmission(state, level, pos);
        }
    }

    @Override
    public boolean canHarvestBlock(final BlockState state, final BlockGetter level, final BlockPos pos, final Player player) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.canHarvestBlock(level, pos, player);
        } else {
            return super.canHarvestBlock(state, level, pos, player);
        }
    }

    @Override
    public boolean isValidSpawn(final BlockState state, final BlockGetter level, final BlockPos pos, final SpawnPlacements.Type type, final EntityType<?> entityType) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.isValidSpawn(level, pos, entityType);
        } else {
            return super.isValidSpawn(state, level, pos, type, entityType);
        }
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final BlockGetter level, final BlockPos pos, final Player player) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getCloneItemStack(target, level, pos, player);
        } else {
            return super.getCloneItemStack(state, target, level, pos, player);
        }
    }

    @Override
    public boolean addLandingEffects(final BlockState state, final ServerLevel level, final BlockPos pos, final BlockState stateOverride, final LivingEntity entity, final int numberOfParticles) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.addLandingEffects(level, pos, stateOverride, entity, numberOfParticles);
        } else {
            return super.addLandingEffects(state, level, pos, stateOverride, entity, numberOfParticles);
        }
    }

    @Override
    public boolean addRunningEffects(final BlockState state, final Level level, final BlockPos pos, final Entity entity) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.addRunningEffects(level, pos, entity);
        } else {
            return super.addRunningEffects(state, level, pos, entity);
        }
    }

    @Override
    public int getExpDrop(final BlockState state, final LevelReader level, final RandomSource randomSource, final BlockPos pos, final int fortuneLevel, final int silkTouchLevel) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getExpDrop(level, randomSource, pos, fortuneLevel, silkTouchLevel);
        } else {
            return super.getExpDrop(state, level, randomSource, pos, fortuneLevel, silkTouchLevel);
        }
    }

    @Override
    public SoundType getSoundType(final BlockState state, final LevelReader level, final BlockPos pos, @org.jetbrains.annotations.Nullable final Entity entity) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getSoundType(level, pos, entity);
        } else {
            return super.getSoundType(state, level, pos, entity);
        }
    }

    @Override
    public boolean canStickTo(final BlockState state, final BlockState other) {
        return false;
    }

    @Override
    public MaterialColor getMapColor(final BlockState state, final BlockGetter level, final BlockPos pos, final MaterialColor defaultColor) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getMapColor(level, pos);
        } else {
            return super.getMapColor(state, level, pos, defaultColor);
        }
    }

    @Override
    public BlockState getAppearance(final BlockState state, final BlockAndTintGetter level, final BlockPos pos, final Direction side, @org.jetbrains.annotations.Nullable final BlockState queryState, @org.jetbrains.annotations.Nullable final BlockPos queryPos) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getAppearance(level, pos, side, queryState, queryPos);
        } else {
            return super.getAppearance(state, level, pos, side, queryState, queryPos);
        }
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static BlockState getOreBlockState(@Nullable final BlockEntity tileEntity) {
        if (tileEntity instanceof BedrockOreBlockEntity bedrockOre) {
            return bedrockOre.getOreBlockState();
        }
        return null;
    }
}
