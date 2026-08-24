/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity;

import com.mojang.logging.LogUtils;
import li.cil.bedrockores.common.block.BedrockOreBlock;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;
import static li.cil.bedrockores.common.block.Blocks.BEDROCK_ORE;

public class BedrockOreBlockEntity extends BlockEntityWithInfo {
    private static final Logger LOGGER = LogUtils.getLogger();

    // --------------------------------------------------------------------- //
    // Persisted data

    private BlockState oreBlockState = Blocks.AIR.defaultBlockState();
    @Nullable
    private Integer amount;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final String TAG_STATE = "state";
    private static final String TAG_AMOUNT = "amount";

    private ItemStack droppedStack = ItemStack.EMPTY;

    // --------------------------------------------------------------------- //

    public BedrockOreBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.BEDROCK_ORE.get(), pos, state);
    }

    // --------------------------------------------------------------------- //

    public BlockState getOreBlockState() {
        return oreBlockState;
    }

    public void setOreBlockState(final BlockState state) {
        if (state.is(BEDROCK_ORE.get())) {
            LOGGER.error("Tried to wrap a bedrock ore inside a bedrock ore at {}.", getBlockPos());
            return;
        }

        if (Objects.equals(state, oreBlockState)) {
            return;
        }

        oreBlockState = state;
        droppedStack = new ItemStack(state.getBlock().asItem());

        final var level = getLevel();
        if (level != null) {
            if (level.isClientSide()) {
                setChangedAndSendUpdateClient();
            } else {
                setChangedAndSendUpdateServer();
            }
            syncLightProperty();
        }
    }

    public OptionalInt getAmount() {
        if (oreBlockState.isAir()) {
            return OptionalInt.of(0);
        }
        if (isInfinite()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(amount);
    }

    public void setAmount(final int value) {
        amount = value;
    }

    public boolean isInfinite() {
        return amount == null;
    }

    public void setInfinite() {
        amount = null;
    }

    public boolean isUnconfigured() {
        return oreBlockState.isAir();
    }

    public boolean isEmpty() {
        final var amount = getAmount();
        if (amount.isPresent()) {
            return amount.getAsInt() <= 0;
        } else {
            return false; // infinite
        }
    }

    public ItemStack extract() {
        final Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return ItemStack.EMPTY;
        }

        final var wasEmpty = isEmpty();
        if (!isInfinite()) {
            if (!wasEmpty) { // paranoia underflow guard
                --amount;
            }
            if (isEmpty()) {
                level.setBlock(getBlockPos(), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                setChanged();
            }
        }

        return wasEmpty ? ItemStack.EMPTY : droppedStack.copy();
    }

    // --------------------------------------------------------------------- //
    // BlockEntityWithInfo

    @Override
    protected Component buildInfo() {
        if (isInfinite()) {
            return Component.translatable(Constants.GUI_EXPECTED_YIELD, Component.translatable(Constants.GUI_INFINITE));
        } else {
            return Component.translatable(Constants.GUI_EXPECTED_YIELD, amount);
        }
    }

    // --------------------------------------------------------------------- //
    // BlockEntity

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        scheduleRemoveUnconfigured();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        final var tag = new CompoundTag();
        BlockState.CODEC
                .encodeStart(NbtOps.INSTANCE, oreBlockState).result()
                .ifPresent(encoded -> tag.put(TAG_STATE, encoded));
        return tag;
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);

        output.store(TAG_STATE, BlockState.CODEC, oreBlockState);
        if (!isInfinite()) {
            output.putInt(TAG_AMOUNT, amount);
        }
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);

        final var oldState = oreBlockState;

        oreBlockState = input.read(TAG_STATE, BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        droppedStack = new ItemStack(oreBlockState.getBlock().asItem());
        input.getInt(TAG_AMOUNT).ifPresentOrElse(this::setAmount, this::setInfinite);

        // This is also the path network updates take, so the model has to be rebuilt when the
        // wrapped ore changed under us.
        final var level = getLevel();
        if (level != null && level.isClientSide() && oreBlockState != oldState) {
            onRenderDataChanged();
            level.setBlocksDirty(getBlockPos(), getBlockState(), getBlockState());
        }
    }

    // --------------------------------------------------------------------- //

    protected void onRenderDataChanged() {
    }

    private void syncLightProperty() {
        final var level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        final var state = getBlockState();
        if (!state.hasProperty(BedrockOreBlock.LIGHT)) {
            return;
        }

        final var light = oreBlockState.getLightEmission();
        if (state.getValue(BedrockOreBlock.LIGHT) != light) {
            level.setBlock(getBlockPos(), state.setValue(BedrockOreBlock.LIGHT, light), Block.UPDATE_ALL);
        }
    }

    private void scheduleRemoveUnconfigured() {
        if (getLevel() instanceof final ServerLevel level) {
            UnconfiguredOreCleanup.schedule(level, getBlockPos());
        }
    }

    private void setChangedAndSendUpdateServer() {
        setChanged();
        requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private void setChangedAndSendUpdateClient() {
        onRenderDataChanged();
        requireNonNull(getLevel()).setBlocksDirty(getBlockPos(), getBlockState(), getBlockState());
    }
}
