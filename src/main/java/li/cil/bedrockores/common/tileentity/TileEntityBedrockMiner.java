package li.cil.bedrockores.common.tileentity;

import cofh.redstoneflux.api.IEnergyReceiver;
import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.OreConfigManager;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.integration.ModIDs;
import li.cil.bedrockores.common.sound.Sounds;
import li.cil.bedrockores.util.WorldUtils;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
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
import java.util.List;
import java.util.Random;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Optional.Interface(modid = ModIDs.REDSTONE_FLUX, iface = "cofh.redstoneflux.api.IEnergyReceiver")
public final class TileEntityBedrockMiner extends AbstractLookAtInfoProvider implements ITickable, IEnergyReceiver {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final ItemHandlerMiner inventory = new ItemHandlerMiner();
    private final EnergyStorageMiner energyStorage = new EnergyStorageMiner();

    private int remainingBurnTime = 0;
    private int extractionCooldown = -1;
    private int transferCooldown = 20;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_ENERGY_STORAGE = "energyStorage";
    private static final String TAG_REMAINING_BURN_TIME = "burnTime";
    private static final String TAG_EXTRACTION_COOLDOWN = "extractionCooldown";
    private static final String TAG_WORKING = "working";

    private static final int SEND_DELAY = 20; // in ticks

    private static final int SLOT_FUEL = 0;
    private static final int SLOT_OUTPUT_FIRST = 1;
    private static final int SLOT_OUTPUT_COUNT = 6;

    private static final int SCAN_RADIUS = 2; // 0 is only straight down
    private static final int SCAN_DEPTH = 3; // 0 is empty

    private static final float BURN_TIME_PER_RF = 0.05f;

    private static final int SOUND_INTERVAL = 30; // in ticks

    @Nullable
    private BlockPos currentOrePos = null;
    private boolean isExhausted = false;

    // We delay sending the working state to clients a little to avoid small
    // hiccups causing unnecessary update packets being sent.
    private boolean isWorkingServer, isWorkingClient;
    private long sendStateAt;

    private int soundCooldown;

    // --------------------------------------------------------------------- //

    public boolean isWorking() {
        return isWorkingServer;
    }

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

    // --------------------------------------------------------------------- //
    // LookAtInfoProvider

    @SideOnly(Side.CLIENT)
    @Override
    public String getLookAtInfoImpl() {
        final int yield = findBedrockOres(getWorld(), getPos()).
                map(TileEntityBedrockOre::getAmount).
                reduce((a, b) -> a + b).
                orElse(0);
        final ITextComponent textComponent;
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
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != EnumFacing.DOWN) ||
               (capability == CapabilityEnergy.ENERGY && facing != EnumFacing.UP && facing != EnumFacing.DOWN) ||
               super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != EnumFacing.DOWN) {
            return (T) inventory;
        }
        if (capability == CapabilityEnergy.ENERGY && facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
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

    private void updateEffects() {
        if (!isWorkingClient) {
            return;
        }

        if (soundCooldown > 0) {
            soundCooldown--;
        }
        if (soundCooldown <= 0) {
            soundCooldown = SOUND_INTERVAL;
            getWorld().playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, Sounds.INSTANCE.bedrockMiner, SoundCategory.BLOCKS, 1, 0.5f, false);
        }

        final Random rng = getWorld().rand;
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final Vec3i direction = facing.getDirectionVec();

            final Vec3d up = new Vec3d(0, 1, 0);
            final Vec3d right = new Vec3d(direction).crossProduct(up);
            final float dx = (rng.nextFloat() - 0.5f) * 0.3f;
            final float dy = (rng.nextFloat() - 0.5f) * 0.3f;

            final Vec3d origin = new Vec3d(getPos()).
                    add(0.5, 0.5, 0.5).
                    add(new Vec3d(direction).scale(0.5)).
                    add(right.scale(dx)).
                    add(up.scale(dy));
            final Vec3d velocity = new Vec3d(direction).scale(0.05);

            getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, origin.x, origin.y, origin.z, velocity.x, velocity.y, velocity.z);
        }
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
        int outputSlot = -1;
        for (int slot = SLOT_OUTPUT_FIRST; slot < inventory.getSlots(); ++slot) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                outputSlot = slot;
                break;
            }
        }
        if (outputSlot < 0) {
            return true;
        }

        final ItemStack stack = inventory.getStackInSlot(outputSlot);

        if (transferCooldown > 0) {
            --transferCooldown;
        }
        if (transferCooldown > 0) {
            return false;
        }

        IItemHandler itemHandler = null;

        final BlockPos blockPos = getPos().up();
        final TileEntity tileEntity = getWorld().getTileEntity(blockPos);
        if (tileEntity != null) {
            itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
        }

        if (itemHandler == null) {
            final List<Entity> entities = getWorld().getEntitiesInAABBexcluding(null, new AxisAlignedBB(blockPos), entity -> entity != null && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
            if (!entities.isEmpty()) {
                final Entity entity = entities.get(getWorld().rand.nextInt(entities.size()));
                itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            }
        }

        if (itemHandler == null) {
            setWorking(false);
            transferCooldown = 20;
            return false;
        }

        final ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, stack, false);
        inventory.setStackInSlot(outputSlot, remainder);
        markDirty();

        transferCooldown = 10;
        if (remainder.isEmpty()) {
            return true;
        } else {
            setWorking(false);
            return false;
        }
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
            beginBurnTime(energyBurnTime);
            return true;
        }

        final ItemStack stack = inventory.getStackInSlot(SLOT_FUEL);
        final int burnTime = Math.round(TileEntityFurnace.getItemBurnTime(stack) * Settings.minerEfficiency * Settings.minerEfficiencyInternalPower);

        // Either it was empty, invalid or we're consuming the fuel.
        inventory.setStackInSlot(SLOT_FUEL, ItemStack.EMPTY);

        if (burnTime > 0) {
            beginBurnTime(burnTime);
            return true;
        }

        return false;
    }

    private void updateExtraction(final TileEntityBedrockOre bedrockOre) {
        if (extractionCooldown < 0) {
            // First init, use scaled extraction time based on ore type.
            beginExtractionCooldown(bedrockOre);
        }

        if (extractionCooldown > 0) {
            extractionCooldown--;
            if (extractionCooldown > 0) {
                return;
            }
        }

        assert extractionCooldown == 0;

        inventory.isInsertingOutputs = true;
        try {
            final List<ItemStack> drops = bedrockOre.extract();
            for (final ItemStack drop : drops) {
                final ItemStack remainder = ItemHandlerHelper.insertItem(inventory, drop, false);
                if (!remainder.isEmpty()) {
                    BedrockOres.getLog().warn("Some mod crammed an unhealthy amount of drops into the drops list in the HarvestDropsEvent (more than {}). Which is more than the miner's buffer can hold. Surplus item is being deleted: {}", SLOT_OUTPUT_COUNT, remainder.getDisplayName());
                }
            }
        } finally {
            inventory.isInsertingOutputs = false;
        }
        markDirty();

        beginExtractionCooldown(bedrockOre);
        transferCooldown = 0;

        WorldUtils.playBreakSound(bedrockOre.getWorld(), bedrockOre.getPos());
    }

    private void beginBurnTime(final int value) {
        markDirty();
        remainingBurnTime = value - 1;
        transferCooldown = 0;
    }

    private void beginExtractionCooldown(final TileEntityBedrockOre bedrockOre) {
        if (!bedrockOre.isInvalid() && bedrockOre.getAmount() > 0) {
            final float cooldownScale = OreConfigManager.INSTANCE.getOreExtractionCooldownScale(bedrockOre.getOreBlockState());
            extractionCooldown = MathHelper.ceil(Settings.minerExtractionCooldown * cooldownScale);
        } else {
            // Exhausted this ore deposit, set to invalid to recompute based on
            // new ore (if any) next tick.
            extractionCooldown = -1;
        }
    }

    private static int getCoalEnergyValue() {
        return MathHelper.ceil(TileEntityFurnace.getItemBurnTime(new ItemStack(Items.COAL)) / (BURN_TIME_PER_RF * Settings.minerEfficiency * Settings.minerEfficiencyInternalPower));
    }

    private static Stream<TileEntityBedrockOre> findBedrockOres(final World world, final BlockPos center) {
        return StreamSupport.stream(new ScanAreaSpliterator(world, center.down()), false);
    }

    // --------------------------------------------------------------------- //

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

    private final class ItemHandlerMiner extends ItemStackHandler {
        boolean isInsertingOutputs;

        ItemHandlerMiner() {
            super(1 + SLOT_OUTPUT_COUNT);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
            if (isInsertingOutputs) {
                if (slot < SLOT_OUTPUT_FIRST) {
                    return stack;
                }
            } else {
                if (slot >= SLOT_OUTPUT_FIRST) {
                    return stack;
                }

                if (Settings.minerEfficiencyInternalPower <= 0) {
                    return stack;
                }

                if (Math.round(TileEntityFurnace.getItemBurnTime(stack) * Settings.minerEfficiency * Settings.minerEfficiencyInternalPower) < 1) {
                    return stack;
                }
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            if (slot == SLOT_FUEL) {
                return ItemStack.EMPTY;
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(final int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(final int slot) {
            super.onContentsChanged(slot);
            markDirty();
        }
    }

    private final class EnergyStorageMiner extends EnergyStorage {
        EnergyStorageMiner() {
            super(getCoalEnergyValue(), Math.max(getCoalEnergyValue() / 20, 1), 0);
        }

        void setEnergy(final int value) {
            this.energy = value;
        }

        @Override
        public boolean canReceive() {
            return super.canReceive() && Settings.minerEfficiencyExternalPower > 0;
        }

        @Override
        public int receiveEnergy(final int maxReceive, final boolean simulate) {
            final int result = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && result > 0) {
                markDirty();
            }
            return result;
        }

        int consumeEnergyForBurnTime() {
            final int burnTime = (int) (energy * (BURN_TIME_PER_RF * Settings.minerEfficiency * Settings.minerEfficiencyExternalPower));
            final int usedEnergy = Math.min(energy, Math.round(burnTime / (BURN_TIME_PER_RF * Settings.minerEfficiency * Settings.minerEfficiencyExternalPower)));
            energy -= usedEnergy;
            return burnTime;
        }
    }
}
