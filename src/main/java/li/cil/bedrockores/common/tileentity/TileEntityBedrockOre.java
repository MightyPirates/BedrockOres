package li.cil.bedrockores.common.tileentity;

import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    private static final Set<IBlockState> loggedWarningFor = Collections.synchronizedSet(new HashSet<>());

    @Nullable
    private ItemStack droppedStack;

    // --------------------------------------------------------------------- //

    @Nullable
    public IBlockState getOreBlockState() {
        return oreBlockState;
    }

    @SuppressWarnings("deprecation")
    public void setOreBlockState(@Nullable final IBlockState state, final int amount) {
        //noinspection VariableNotUsedInsideIf
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

    @SuppressWarnings("deprecation")
    @Nullable
    public ItemStack getDroppedStack() {
        if (droppedStack == null) {
            if (oreBlockState != null) {
                final Block block = oreBlockState.getBlock();
                try {
                    droppedStack = block.getPickBlock(oreBlockState, null, getWorld(), getPos(), null);
                } catch (final Throwable t) {
                    try {
                        final Item item = Item.getItemFromBlock(block);
                        final int damage = block.damageDropped(oreBlockState);
                        final ItemStack stack = new ItemStack(item, 1, damage);
                        final int meta = item.getMetadata(stack);
                        if (Objects.equals(block.getStateFromMeta(meta), oreBlockState)) {
                            droppedStack = stack;
                        } else {
                            throw new Exception("Block/Item implementation does not allow round-trip via Block.damageDropped/Item.getMetadata/Block.getStateFromMeta: " + block.toString() + ", " + item.toString());
                        }
                    } catch (final Throwable t2) {
                        if (loggedWarningFor.add(oreBlockState)) {
                            // Log twice to get both stack traces. Don't log first trace if second lookup succeeds.
                            BedrockOres.getLog().warn("Failed determining dropped block for " + oreBlockState.toString() + " via getPickBlock, trying to resolve via meta.", t);
                            BedrockOres.getLog().error("Failed determining dropped block for " + oreBlockState.toString() + " via meta, clearing bedrock ore.", t2);
                        }
                    }
                }
            }
        }
        if (droppedStack == null) {
            amount = 0;
        }
        return droppedStack;
    }

    public List<ItemStack> extract() {
        if (world.isRemote) {
            return Collections.emptyList();
        }

        final ItemStack stack = getDroppedStack();

        --amount;
        if (amount < 1) {
            getWorld().setBlockState(getPos(), Blocks.BEDROCK.getDefaultState());
        } else {
            getWorld().markChunkDirty(getPos(), this);
        }

        if (stack.isEmpty()) {
            return Collections.emptyList();
        }

        if (!(world instanceof WorldServer)) {
            return Collections.singletonList(stack.copy());
        }

        final FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) getWorld());
        final BlockEvent.HarvestDropsEvent event = new BlockEvent.HarvestDropsEvent(getWorld(), getPos(), getOreBlockState(), 0, 1, Collections.singletonList(stack.copy()), fakePlayer, true);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.getDropChance() < 1f && event.getDropChance() > getWorld().rand.nextFloat()) {
            return Collections.emptyList();
        } else {
            return event.getDrops();
        }
    }

    // --------------------------------------------------------------------- //
    // LookAtInfoProvider

    @SideOnly(Side.CLIENT)
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
}
