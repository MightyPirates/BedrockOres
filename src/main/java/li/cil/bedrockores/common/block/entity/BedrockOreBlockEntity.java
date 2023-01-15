package li.cil.bedrockores.common.block.entity;

import li.cil.bedrockores.client.render.Ore;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static li.cil.bedrockores.common.block.Blocks.BEDROCK_ORE;

public final class BedrockOreBlockEntity extends BlockEntityWithInfo {
    // --------------------------------------------------------------------- //
    // Persisted data

    private BlockState oreBlockState = Blocks.AIR.defaultBlockState();
    private int amount;

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
            if (oreBlockState.getLightEmission(level, getBlockPos()) != oldState.getLightEmission(level, getBlockPos()) ||
                    oreBlockState.getLightBlock(level, getBlockPos()) != oldState.getLightBlock(level, getBlockPos())) {
                level.getChunkSource().getLightEngine().checkBlock(getBlockPos());
            }
        }
    }

    public int getAmount() {
        return oreBlockState.isAir() ? 0 : amount;
    }

    public void setAmount(final int value) {
        amount = value;
    }

    public ItemStack extract() {
        final Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return ItemStack.EMPTY;
        }

        --amount;
        if (amount < 1) {
            level.setBlock(getBlockPos(), Blocks.BEDROCK.defaultBlockState(), level.isClientSide() ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
        } else {
            setChanged();
        }

        return droppedStack.copy();
    }

    // --------------------------------------------------------------------- //
    // BlockEntityWithInfo

    @Override
    protected Component buildInfo() {
        return Component.translatable(Constants.GUI_EXPECTED_YIELD, amount);
    }

    // --------------------------------------------------------------------- //
    // BlockEntity

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag) {
        final var oldState = oreBlockState;
        super.handleUpdateTag(tag);
        if (oreBlockState != oldState) {
            requestModelDataUpdate();
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);

        if (oreBlockState != null) {
            BlockState.CODEC.encodeStart(NbtOps.INSTANCE, oreBlockState)
                    .result().ifPresent(stateNbt -> tag.put(TAG_STATE, stateNbt));
            tag.putInt(TAG_AMOUNT, amount);
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        oreBlockState = BlockState.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_STATE))
                .result().orElse(Blocks.AIR.defaultBlockState());
        droppedStack = new ItemStack(oreBlockState.getBlock().asItem());
        amount = tag.getInt(TAG_AMOUNT);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return Ore.create(this).asModelData();
    }

    // --------------------------------------------------------------------- //

    private void setChangedAndSendUpdateServer() {
        setChanged();
        requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private void setChangedAndSendUpdateClient() {
        requestModelDataUpdate();
        requireNonNull(getLevel()).setBlocksDirty(getBlockPos(), getBlockState(), getBlockState());
    }
}
