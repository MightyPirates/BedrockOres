/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block;

import com.mojang.serialization.MapCodec;
import li.cil.bedrockores.common.block.entity.BedrockOreMinerBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.List;

public final class BedrockMinerBlock extends BaseEntityBlock {
    public static final MapCodec<BedrockMinerBlock> CODEC = MapCodec.unit(BedrockMinerBlock::new);

    // --------------------------------------------------------------------- //

    public BedrockMinerBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5, 10)
                .sound(SoundType.METAL));
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.MINER.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, BlockEntities.MINER.get(), BedrockOreMinerBlockEntity::clientTick);
        } else {
            return createTickerHelper(type, BlockEntities.MINER.get(), BedrockOreMinerBlockEntity::serverTick);
        }
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // Block

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState blockState, final Level level, final BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof final BedrockOreMinerBlockEntity miner) {
            return miner.isWorking() ? 15 : 0;
        } else {
            return super.getAnalogOutputSignal(blockState, level, pos);
        }
    }

    @Override
    public void onRemove(final BlockState oldState, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        if (!oldState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof final BedrockOreMinerBlockEntity miner) {
            Containers.dropContents(level, pos, miner);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(oldState, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltip, flags);
        final var edgeLength = (Settings.minerAreaRadius.get() - 1) * 2 + 1;
        final var layers = Settings.minerAreaLayers.get();
        tooltip.add(Component.translatable(Constants.TOOLTIP_BEDROCK_MINER, edgeLength, layers, edgeLength).withStyle(ChatFormatting.GRAY));
    }
}
