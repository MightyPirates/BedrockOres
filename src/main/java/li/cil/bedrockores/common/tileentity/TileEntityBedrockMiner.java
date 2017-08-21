package li.cil.bedrockores.common.tileentity;

import cofh.api.energy.IEnergyReceiver;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.integration.ModIDs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Optional.Interface(modid = ModIDs.REDSTONE_FLUX, iface = "cofh.api.energy.IEnergyReceiver")
public final class TileEntityBedrockMiner extends AbstractLookAtInfoProvider implements ITickable, IEnergyReceiver {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final ItemHandlerMiner inventory = new ItemHandlerMiner();
    private final EnergyStorageMiner energyStorage = new EnergyStorageMiner();

    private int remainingBurnTime = 0;
    private int extractionCooldown = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_ENERGY_STORAGE = "energyStorage";
    private static final String TAG_REMAINING_BURN_TIME = "burnTime";
    private static final String TAG_EXTRACTION_COOLDOWN = "extractionCooldown";
    private static final String TAG_WORKING = "working";

    private static final int SEND_DELAY = 20;

    private static final int SLOT_FUEL = 0;
    private static final int SLOT_OUTPUT = 1;

    private static final int SCAN_RADIUS = 2; // 0 is only straight down
    private static final int SCAN_DEPTH = 3; // 0 is empty

    private static final float BURN_TIME_PER_RF = 0.05f;

    @Nullable
    private BlockPos currentOrePos = null;
    private boolean isExhausted = false;

    // We delay sending the working state to clients a little to avoid small
    // hiccups causing unnecessary update packets being sent.
    private boolean isWorkingServer, isWorkingClient;
    private long sendStateAt;

    // --------------------------------------------------------------------- //
    // ITickable

    @Override
    public void update() {
        if (getWorld().isRemote) {
            updateEffects();
            return;
        }

        updateClientState();

        if (!tryTransferOutput()) {
            setWorking(false);
            return;
        }

        final TileEntityBedrockOre bedrockOre = updateCurrentOrePos();
        if (bedrockOre == null) {
            setWorking(false);
            return;
        }

        if (!updateBurnTime()) {
            setWorking(false);
            return;
        }

        setWorking(true);

        updateExtraction(bedrockOre);
    }

    private void updateEffects() {
        if (!isWorkingClient) {
            return;
        }

        final Random rng = getWorld().rand;
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final Vec3i direction = facing.getDirectionVec();

            final Vec3d up = new Vec3d(0, 1, 0);
            final Vec3d right = new Vec3d(direction).crossProduct(up);
            final float dx = (rng.nextFloat() - 0.5f) * 0.3f;
            final float dy = (rng.nextFloat() - 0.5f) * 0.3f;

            final Vec3d origin = new Vec3d(getPos()).
                    addVector(0.5, 0.5, 0.5).
                    add(new Vec3d(direction).scale(0.5)).
                    add(right.scale(dx)).
                    add(up.scale(dy));
            final Vec3d velocity = new Vec3d(direction).scale(0.05);

            getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, origin.xCoord, origin.yCoord, origin.zCoord, velocity.xCoord, velocity.yCoord, velocity.zCoord);
        }
    }

    // --------------------------------------------------------------------- //
    // LookAtInfoProvider

    @SideOnly(Side.CLIENT)
    @Override
    public String getLookAtInfoImpl() {
        final int yield = findBedrockOres(getWorld(), getPos()).
                map(TileEntityBedrockOre::getAmount).
                reduce((a, b) -> a + b).
                orElse(0);
        ITextComponent textComponent;
        if (yield > 0) {
            textComponent = new TextComponentTranslation(Constants.GUI_EXPECTED_YIELD, yield);
        } else {
            textComponent = new TextComponentTranslation(Constants.GUI_EXHAUSTED);
        }
        return textComponent.getFormattedText();
    }

    @Override
    public void updateLookAtInfoImpl() {
        super.updateLookAtInfoImpl();
        findBedrockOres(getWorld(), getPos()).forEach(TileEntityBedrockOre::updateLookAtInfo);
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

        result.setTag(TAG_INVENTORY, inventory.serializeNBT());
        result.setInteger(TAG_ENERGY_STORAGE, energyStorage.getEnergyStored());
        result.setInteger(TAG_REMAINING_BURN_TIME, remainingBurnTime);
        result.setInteger(TAG_EXTRACTION_COOLDOWN, extractionCooldown);

        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);

        inventory.deserializeNBT(compound.getCompoundTag(TAG_INVENTORY));
        energyStorage.setEnergy(compound.getInteger(TAG_ENERGY_STORAGE));
        remainingBurnTime = compound.getInteger(TAG_REMAINING_BURN_TIME);
        extractionCooldown = compound.getInteger(TAG_EXTRACTION_COOLDOWN);
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
               capability == CapabilityEnergy.ENERGY ||
               super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) energyStorage;
        }
        return super.getCapability(capability, facing);
    }

    // --------------------------------------------------------------------- //
    // IEnergyReceiver

    @Override
    public int receiveEnergy(final EnumFacing from, final int maxReceive, final boolean simulate) {
        if (!from.getAxis().isHorizontal()) {
            return 0;
        }
        return energyStorage.receiveEnergy(maxReceive, simulate);
    }

    // --------------------------------------------------------------------- //
    // IEnergyHandler

    @Override
    public int getEnergyStored(final EnumFacing from) {
        if (!from.getAxis().isHorizontal()) {
            return 0;
        }
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(final EnumFacing from) {
        if (!from.getAxis().isHorizontal()) {
            return 0;
        }
        return energyStorage.getMaxEnergyStored();
    }

    // --------------------------------------------------------------------- //
    // IEnergyConnection

    @Override
    public boolean canConnectEnergy(final EnumFacing from) {
        return from.getAxis().isHorizontal();
    }

    // --------------------------------------------------------------------- //

    private NBTTagCompound writeToNBTForClient(final NBTTagCompound compound) {
        compound.setBoolean(TAG_WORKING, isWorkingServer);
        return compound;
    }

    private void readFromNBTForClient(final NBTTagCompound compound) {
        isWorkingClient = compound.getBoolean(TAG_WORKING);
    }

    private void updateClientState() {
        if (getWorld().getTotalWorldTime() >= sendStateAt) {
            isWorkingClient = isWorkingServer;
            sendStateAt = Long.MAX_VALUE;
            sendBlockUpdatePacket();
        }
    }

    private void setWorking(final boolean isWorking) {
        isWorkingServer = isWorking;
        if (isWorking == isWorkingClient) {
            sendStateAt = Long.MAX_VALUE;
        } else if (sendStateAt == Long.MAX_VALUE) {
            sendStateAt = getWorld().getTotalWorldTime() + SEND_DELAY;
        }
    }

    private boolean tryTransferOutput() {
        final ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT);
        if (stack == null) {
            return true;
        }

        final BlockPos blockPos = getPos().up();
        final TileEntity tileEntity = getWorld().getTileEntity(blockPos);
        if (tileEntity == null) {
            return false;
        }

        final IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
        final ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, stack, false);
        inventory.setStackInSlot(SLOT_OUTPUT, remainder);

        return remainder == null || remainder.stackSize < 1;
    }

    @Nullable
    private TileEntityBedrockOre updateCurrentOrePos() {
        if (isExhausted) {
            return null;
        }

        if (currentOrePos != null) {
            final TileEntity tileEntity = getWorld().getTileEntity(currentOrePos);
            if (tileEntity instanceof TileEntityBedrockOre) {
                return (TileEntityBedrockOre) tileEntity;
            }
        }

        final TileEntityBedrockOre bedrockOre = findBedrockOres(getWorld(), getPos()).
                findFirst().
                orElse(null);
        if (bedrockOre != null) {
            currentOrePos = bedrockOre.getPos();
            return bedrockOre;
        } else {
            isExhausted = true;
            currentOrePos = null;
            return null;
        }
    }

    private boolean updateBurnTime() {
        if (remainingBurnTime > 0) {
            --remainingBurnTime;
            return true;
        }

        final int energyBurnTime = energyStorage.consumeEnergyForBurnTime();
        if (energyBurnTime > 0) {
            remainingBurnTime = energyBurnTime;
            return true;
        }

        final ItemStack stack = inventory.getStackInSlot(SLOT_FUEL);
        final int burnTime = Math.round(TileEntityFurnace.getItemBurnTime(stack) * Settings.minerEfficiency);

        // Either it was empty, invalid or we're consuming the fuel.
        inventory.setStackInSlot(SLOT_FUEL, null);
        if (burnTime > 0) {
            remainingBurnTime = burnTime;
            return true;
        }

        return false;
    }

    private void updateExtraction(final TileEntityBedrockOre bedrockOre) {
        if (extractionCooldown > 0) {
            extractionCooldown--;
        } else {
            extractionCooldown = Settings.minerExtractionCooldown;

            final ItemStack stack = bedrockOre.extract();
            inventory.setStackInSlot(SLOT_OUTPUT, stack);
        }
    }

    private static Stream<TileEntityBedrockOre> findBedrockOres(final World world, final BlockPos center) {
        return StreamSupport.stream(new ScanAreaSpliterator(world, center.down()), false);
    }

    private static final class ScanAreaSpliterator extends Spliterators.AbstractSpliterator<TileEntityBedrockOre> {
        private final World world;
        private final BlockPos center;
        private int x, y, z;

        ScanAreaSpliterator(final World world, final BlockPos center) {
            super((SCAN_RADIUS * 2 + 1) * (SCAN_RADIUS * 2 + 1) * SCAN_DEPTH, ORDERED | DISTINCT | SIZED | NONNULL | IMMUTABLE | SUBSIZED);
            this.world = world;
            this.center = center;
            this.x = -SCAN_RADIUS;
            this.z = -SCAN_RADIUS;
            this.y = 0;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super TileEntityBedrockOre> action) {
            while (y < SCAN_DEPTH) {
                final BlockPos pos = center.add(x, -y, z);

                x++;
                if (x > SCAN_RADIUS) {
                    x = -SCAN_RADIUS;
                    z++;
                    if (z > SCAN_RADIUS) {
                        z = -SCAN_RADIUS;
                        y++;
                    }
                }

                final TileEntity tileEntity = world.getTileEntity(pos);
                if (tileEntity instanceof TileEntityBedrockOre) {
                    action.accept((TileEntityBedrockOre) tileEntity);
                    return true;
                }
            }
            return false;
        }
    }

    private static final class ItemHandlerMiner extends ItemStackHandler {
        ItemHandlerMiner() {
            super(2);
        }

        @Nullable
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
            if (slot == SLOT_OUTPUT) {
                return stack;
            }

            if (Math.round(TileEntityFurnace.getItemBurnTime(stack) * Settings.minerEfficiency) < 1) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Nullable
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            if (slot == SLOT_FUEL) {
                return null;
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected int getStackLimit(final int slot, final ItemStack stack) {
            return 1;
        }
    }

    private static final class EnergyStorageMiner extends EnergyStorage {
        EnergyStorageMiner() {
            super(getCoalEnergyValue(), Math.max(getCoalEnergyValue() / 20, 1), 0);
        }

        void setEnergy(final int value) {
            this.energy = value;
        }

        int consumeEnergyForBurnTime() {
            final int burnTime = (int) (energy * (BURN_TIME_PER_RF * Settings.minerEfficiency));
            final int usedEnergy = Math.min(energy, Math.round(burnTime / (BURN_TIME_PER_RF * Settings.minerEfficiency)));
            energy -= usedEnergy;
            return burnTime;
        }

        private static int getCoalEnergyValue() {
            return MathHelper.ceil(TileEntityFurnace.getItemBurnTime(new ItemStack(Items.COAL)) / (BURN_TIME_PER_RF * Settings.minerEfficiency));
        }
    }
}
