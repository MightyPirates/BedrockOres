package li.cil.bedrockores.common.tileentity;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Objects;

public final class TileEntityBedrockOre extends AbstractLookAtInfoProvider {
    // --------------------------------------------------------------------- //
    // Persisted data

    @Nullable
    private IBlockState oreBlockState;
    private int amount;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final String TAG_ORE_ID = "oreId";
    private static final String TAG_ORE_META = "oreMeta";
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_ORE_BLOCK_STATE_ID = "id";

    @Nullable
    private ItemStack droppedStack;

    // --------------------------------------------------------------------- //

    @Nullable
    public IBlockState getOreBlockState() {
        return oreBlockState;
    }

    @SuppressWarnings("deprecation")
    public void setOreBlockState(@Nullable final IBlockState state, final int amount) {
        this.amount = state != null ? amount : 0;

        if (Objects.equals(state, oreBlockState)) {
            return;
        }

        this.oreBlockState = state;
        this.droppedStack = null; // invalidate, lazy init in next getDroppedStack
        if (hasWorld()) {
            if (getWorld().isRemote) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            } else {
                getWorld().markChunkDirty(getPos(), this);
                sendBlockUpdatePacket();
            }
            if (oreBlockState != null && oreBlockState.getLightValue() > 0) {
                getWorld().checkLight(getPos());
            }
        }
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack extract() {
        final ItemStack stack = getDroppedStack();

        --amount;
        if (amount < 1) {
            getWorld().setBlockState(getPos(), Blocks.BEDROCK.getDefaultState());
        } else {
            getWorld().markChunkDirty(getPos(), this);
        }

        return stack.copy();
    }

    public void playBreakSound() {
        final IBlockState state = getOreBlockState();
        if (state != null) {
            final SoundType soundtype = state.getBlock().getSoundType(state, getWorld(), getPos(), null);
            getWorld().playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, soundtype.getBreakSound(), SoundCategory.BLOCKS, soundtype.getVolume(), soundtype.getPitch());
        }
    }

    // --------------------------------------------------------------------- //
    // LookAtInfoProvider

    @Override
    protected String getLookAtInfoImpl() {
        return new TextComponentTranslation(Constants.GUI_EXPECTED_YIELD, amount).getFormattedText();
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, writeToNBTForClient(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(final NetworkManager networkManager, final SPacketUpdateTileEntity packet) {
        super.onDataPacket(networkManager, packet);
        readFromNBTForClient(packet.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBTForClient(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(final NBTTagCompound compound) {
        super.handleUpdateTag(compound);
        readFromNBTForClient(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        final NBTTagCompound result = super.writeToNBT(compound);

        if (oreBlockState != null) {
            // Using the internal ID makes this world specific, but that's fine.
            final Block oreBlock = oreBlockState.getBlock();
            final int oreId = Block.getIdFromBlock(oreBlock);
            final int oreMeta = oreBlock.getMetaFromState(oreBlockState);
            result.setInteger(TAG_ORE_ID, oreId);
            result.setInteger(TAG_ORE_META, oreMeta);
            result.setInteger(TAG_AMOUNT, amount);
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);

        final int oreId = compound.getInteger(TAG_ORE_ID);
        final int oreMeta = compound.getInteger(TAG_ORE_META);
        final Block oreBlock = Block.getBlockById(oreId);
        if (oreBlock == Blocks.AIR) {
            setOreBlockState(null, 0);
        } else {
            setOreBlockState(oreBlock.getStateFromMeta(oreMeta), compound.getInteger(TAG_AMOUNT));
        }
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("deprecation")
    private NBTTagCompound writeToNBTForClient(final NBTTagCompound compound) {
        if (oreBlockState != null) {
            compound.setInteger(TAG_ORE_BLOCK_STATE_ID, Block.BLOCK_STATE_IDS.get(oreBlockState));
            compound.setInteger(TAG_AMOUNT, amount);
        }
        return compound;
    }

    @SuppressWarnings("deprecation")
    private void readFromNBTForClient(final NBTTagCompound compound) {
        final int oreBlockStateId = compound.getInteger(TAG_ORE_BLOCK_STATE_ID);
        setOreBlockState(Block.BLOCK_STATE_IDS.getByValue(oreBlockStateId), compound.getInteger(TAG_AMOUNT));
    }

    private ItemStack getDroppedStack() {
        if (droppedStack == null) {
            if (oreBlockState == null) {
                droppedStack = ItemStack.EMPTY;
            } else {
                droppedStack = oreBlockState.getBlock().getPickBlock(oreBlockState, null, getWorld(), BlockPos.ORIGIN, null);
            }
        }
        if (droppedStack.isEmpty()) {
            amount = 0;
        }
        return droppedStack;
    }
}
