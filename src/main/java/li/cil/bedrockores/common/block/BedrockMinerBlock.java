package li.cil.bedrockores.common.block;

import li.cil.bedrockores.common.block.entity.BedrockOreMinerBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import java.util.List;

public final class BedrockMinerBlock extends BaseEntityBlock {
    public BedrockMinerBlock() {
        super(Properties
                .of(Material.METAL)
                .strength(5, 10)
                .sound(SoundType.METAL));
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.MINER.get().create(pos, state);
    }

    @Nullable
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

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(final BlockState blockState, final Level level, final BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BedrockOreMinerBlockEntity miner) {
            return miner.isWorking() ? 15 : 0;
        } else {
            return super.getAnalogOutputSignal(blockState, level, pos);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(final BlockState oldState, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BedrockOreMinerBlockEntity miner) {
            miner.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemHandler.getStackInSlot(slot));
                }
            });
        } else {
            super.onRemove(oldState, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final BlockGetter level, final List<Component> tooltip, final TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltip, flags);
        tooltip.add(Component.translatable(Constants.TOOLTIP_BEDROCK_MINER));
    }
}
