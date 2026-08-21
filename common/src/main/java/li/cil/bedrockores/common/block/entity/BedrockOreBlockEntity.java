/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;
import static li.cil.bedrockores.common.block.Blocks.BEDROCK_ORE;

public class BedrockOreBlockEntity extends BlockEntityWithInfo {
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
            throw new IllegalArgumentException("Bedrock ore cannot contain itself.");
        }

        if (Objects.equals(state, oreBlockState)) {
            return;
        }

        final var oldState = oreBlockState;

        oreBlockState = state;
        droppedStack = new ItemStack(state.getBlock().asItem());

        final var level = getLevel();
        if (level != null) {
            if (level.isClientSide()) {
                setChangedAndSendUpdateClient();
            } else {
                setChangedAndSendUpdateServer();
            }
            if (oreBlockState.getLightEmission() != oldState.getLightEmission() ||
                    oreBlockState.getLightBlock(level, getBlockPos()) != oldState.getLightBlock(level, getBlockPos())) {
                level.getChunkSource().getLightEngine().checkBlock(getBlockPos());
            }
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

        refreshLighting();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        BlockState.CODEC
                .encodeStart(NbtOps.INSTANCE, oreBlockState).result()
                .ifPresent(stateNbt -> tag.put(TAG_STATE, stateNbt));
        if (!isInfinite()) {
            tag.putInt(TAG_AMOUNT, amount);
        } else {
            tag.remove(TAG_AMOUNT);
        }
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        final var oldState = oreBlockState;

        oreBlockState = BlockState.CODEC
                .parse(NbtOps.INSTANCE, tag.get(TAG_STATE)).result()
                .orElse(Blocks.AIR.defaultBlockState());
        droppedStack = new ItemStack(oreBlockState.getBlock().asItem());
        if (tag.contains(TAG_AMOUNT, Tag.TAG_INT)) {
            setAmount(tag.getInt(TAG_AMOUNT));
        } else {
            setInfinite();
        }

        // This is also the path network updates take, so the model has to be rebuilt when the
        // wrapped ore changed under us.
        final var level = getLevel();
        if (level != null && level.isClientSide() && oreBlockState != oldState) {
            onRenderDataChanged();
            level.setBlocksDirty(getBlockPos(), getBlockState(), getBlockState());
            refreshLighting();
        }
    }

    // --------------------------------------------------------------------- //

    protected void onRenderDataChanged() {
    }

    /**
     * Chunk lighting is computed before block entities are loaded, so at that point nothing knows
     * which ore this block wraps and a glowing one contributes no light. Once the block entity is
     * in place, nudge the light engine so it picks the emission up.
     */
    private void refreshLighting() {
        final var level = getLevel();
        if (level == null || oreBlockState.getLightEmission() <= 0) {
            return;
        }

        final var pos = getBlockPos();
        if (level instanceof final ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                if (!isRemoved()) {
                    serverLevel.getChunkSource().getLightEngine().checkBlock(pos);
                }
            });
        } else {
            level.getChunkSource().getLightEngine().checkBlock(pos);
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
