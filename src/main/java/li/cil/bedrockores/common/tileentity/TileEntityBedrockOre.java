package li.cil.bedrockores.common.tileentity;

import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
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
import java.util.ArrayList;
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

    private static final String TAG_ORE = "ore";
    private static final String TAG_ORE_ID = "oreId";
    private static final String TAG_ORE_META = "oreMeta";
    private static final String TAG_AMOUNT = "amount";
    @Deprecated
    private static final String TAG_ORE_BLOCK_STATE_ID = "id";

    private static final Set<IBlockState> loggedWarningFor = Collections.synchronizedSet(new HashSet<>());

    @Nullable
    private ItemStack droppedStack;

    // --------------------------------------------------------------------- //

    @Nullable
    public IBlockState getOreBlockState() {
        return oreBlockState;
    }

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
            if (oreBlockState != null && oreBlockState.getLightValue(this.world, this.pos) > 0) { // because the ore block state locates at the same position...
                getWorld().checkLight(getPos());
            }
        }
    }

    public int getAmount() {
        return amount;
    }

    @SuppressWarnings("deprecation")
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
                            final int guessedItemDamage = block.getMetaFromState(oreBlockState);
                            final ItemStack guessedItemStack = new ItemStack(item, 1, guessedItemDamage);
                            final int guessedMeta = item.getMetadata(stack);
                            if (Objects.equals(block.getStateFromMeta(guessedMeta), oreBlockState)) {
                                droppedStack = guessedItemStack;
                            } else {
                                throw new Exception("Block/Item implementation does not allow round-trip via Block.damageDropped/Item.getMetadata/Block.getStateFromMeta: " + block.toString() + ", " + item.toString());
                            }
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
            droppedStack = ItemStack.EMPTY;
        }
        if (droppedStack.isEmpty()) {
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
            return new ArrayList<>();
        }

        final ArrayList<ItemStack> drops = new ArrayList<>();
        drops.add(stack.copy());

        if (!(world instanceof WorldServer)) {
            return drops;
        }

        final FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) getWorld());
        final BlockEvent.HarvestDropsEvent event = new BlockEvent.HarvestDropsEvent(getWorld(), getPos(), getOreBlockState(), 0, 1, drops, fakePlayer, true);
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
            result.setTag(TAG_ORE, NBTUtil.writeBlockState(new NBTTagCompound(), this.oreBlockState));
            result.setInteger(TAG_AMOUNT, amount);
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_ORE)) {
            setOreBlockState(NBTUtil.readBlockState(compound.getCompoundTag(TAG_ORE)), compound.getInteger(TAG_AMOUNT));
        } else {
            final int oreId = compound.getInteger(TAG_ORE_ID);
            final int oreMeta = compound.getInteger(TAG_ORE_META);
            final Block oreBlock = Block.getBlockById(oreId);
            if (oreBlock == Blocks.AIR) {
                setOreBlockState(null, 0);
            } else {
                setOreBlockState(oreBlock.getStateFromMeta(oreMeta), compound.getInteger(TAG_AMOUNT));
            }
        }
    }

    // --------------------------------------------------------------------- //

    private NBTTagCompound writeToNBTForClient(final NBTTagCompound compound) {
        if (oreBlockState != null) {
            compound.setTag(TAG_ORE, NBTUtil.writeBlockState(new NBTTagCompound(), this.oreBlockState));
            compound.setInteger(TAG_AMOUNT, amount);
        }
        return compound;
    }

    private void readFromNBTForClient(final NBTTagCompound compound) {
        setOreBlockState(NBTUtil.readBlockState(compound.getCompoundTag(TAG_ORE)), compound.getInteger(TAG_AMOUNT));
    }
}
