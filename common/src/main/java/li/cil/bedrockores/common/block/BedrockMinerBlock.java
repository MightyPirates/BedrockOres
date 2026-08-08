package li.cil.bedrockores.common.block;

import com.mojang.serialization.MapCodec;
import li.cil.bedrockores.common.block.entity.BedrockOreMinerBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
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
    public int getAnalogOutputSignal(final BlockState blockState, final Level level, final BlockPos pos, final Direction side) {
        if (level.getBlockEntity(pos) instanceof final BedrockOreMinerBlockEntity miner) {
            return miner.isWorking() ? 15 : 0;
        } else {
            return super.getAnalogOutputSignal(blockState, level, pos, side);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(final BlockState state, final ServerLevel level, final BlockPos pos, final boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof final BedrockOreMinerBlockEntity miner) {
            Containers.dropContents(level, pos, miner);
        }
        level.updateNeighbourForOutputSignal(pos, this);
    }
}
