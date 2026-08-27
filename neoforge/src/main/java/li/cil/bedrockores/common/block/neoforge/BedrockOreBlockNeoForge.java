/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.neoforge;

import li.cil.bedrockores.common.block.BedrockOreBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

public final class BedrockOreBlockNeoForge extends BedrockOreBlock {
    @Override
    public boolean onDestroyedByPlayer(final BlockState state, final Level level, final BlockPos pos, final Player player, final ItemStack tool, final boolean willHarvest, final FluidState fluid) {
        if (level.isClientSide() && !player.isCreative()) {
            // Report the swing as handled so break progress resets, but leave the block alone.
            return true;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }

    @Override
    public float getFriction(final BlockState state, final LevelReader level, final BlockPos pos, @Nullable final Entity entity) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.getFriction(level, pos, entity);
        } else {
            return super.getFriction(state, level, pos, entity);
        }
    }

    @Override
    public boolean canHarvestBlock(final BlockState state, final BlockGetter level, final BlockPos pos, final Player player) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.canHarvestBlock(level, pos, player);
        } else {
            return super.canHarvestBlock(state, level, pos, player);
        }
    }

    @Override
    public boolean addLandingEffects(final BlockState state, final ServerLevel level, final BlockPos pos, final BlockState stateOverride, final LivingEntity entity, final int numberOfParticles) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.addLandingEffects(level, pos, stateOverride, entity, numberOfParticles);
        } else {
            return super.addLandingEffects(state, level, pos, stateOverride, entity, numberOfParticles);
        }
    }

    @Override
    public boolean addRunningEffects(final BlockState state, final Level level, final BlockPos pos, final Entity entity) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.addRunningEffects(level, pos, entity);
        } else {
            return super.addRunningEffects(state, level, pos, entity);
        }
    }

    @Override
    public SoundType getSoundType(final BlockState state, final LevelReader level, final BlockPos pos, @Nullable final Entity entity) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.getSoundType(level, pos, entity);
        } else {
            return super.getSoundType(state, level, pos, entity);
        }
    }

    @Override
    public MapColor getMapColor(final BlockState state, final BlockGetter level, final BlockPos pos, final MapColor defaultColor) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.getMapColor(level, pos);
        } else {
            return super.getMapColor(state, level, pos, defaultColor);
        }
    }

    @Override
    public BlockState getAppearance(final BlockState state, final BlockAndTintGetter level, final BlockPos pos, final Direction side, @Nullable final BlockState queryState, @Nullable final BlockPos queryPos) {
        final var ore = getOreBlockState(level, pos);
        if (ore != null) {
            return ore.getAppearance(level, pos, side, queryState, queryPos);
        } else {
            return super.getAppearance(state, level, pos, side, queryState, queryPos);
        }
    }
}
