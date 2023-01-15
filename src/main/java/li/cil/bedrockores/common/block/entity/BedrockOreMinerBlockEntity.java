package li.cil.bedrockores.common.block.entity;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.sound.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

public final class BedrockOreMinerBlockEntity extends BlockEntityWithInfo {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final FuelItemHandler fuelInventory = new FuelItemHandler();
    private final OutputItemHandler outputInventory = new OutputItemHandler();
    private final EnergyStorageMiner energyStorage = new EnergyStorageMiner();

    private int remainingBurnTime = 0;
    private int extractionCooldown = -1;
    private int transferCooldown = 20;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final String TAG_FUEL_INVENTORY = "input";
    private static final String TAG_OUTPUT_INVENTORY = "output";
    private static final String TAG_ENERGY_STORAGE = "energyStorage";
    private static final String TAG_REMAINING_BURN_TIME = "burnTime";
    private static final String TAG_EXTRACTION_COOLDOWN = "extractionCooldown";
    private static final String TAG_WORKING = "working";

    private static final TemporalAmount SEND_WORKING_STATE_DELAY = Duration.ofSeconds(1);

    private static final int SLOT_FUEL_COUNT = 1;
    private static final int SLOT_OUTPUT_COUNT = 6;

    private static final int SCAN_RADIUS = 2; // 0 is only straight down
    private static final int SCAN_DEPTH = 3; // 0 is empty

    private static final int RF_PER_BURN_TIME = 10;

    private static final int SOUND_INTERVAL = 30; // in ticks

    @Nullable
    private BedrockOreBlockEntity currentOre;
    private boolean hasNoMoreOres;

    // We delay sending the working state to clients a little to avoid small
    // hiccups causing unnecessary update packets being sent.
    private boolean isWorkingServer, isWorkingClient;
    @Nullable
    private Instant sendUpdateTagAfter;

    private int soundCooldown;

    // --------------------------------------------------------------------- //

    public BedrockOreMinerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.MINER.get(), pos, state);
    }

    // --------------------------------------------------------------------- //

    public boolean isWorking() {
        return isWorkingServer;
    }

    // --------------------------------------------------------------------- //

    public static void clientTick(final Level ignoredLevel, final BlockPos ignoredPos, final BlockState ignoredState, final BedrockOreMinerBlockEntity miner) {
        miner.clientTick();
    }

    public static void serverTick(final Level ignoredLevel, final BlockPos ignoredPos, final BlockState ignoredState, final BedrockOreMinerBlockEntity miner) {
        miner.serverTick();
    }

    private void clientTick() {
        updateEffects();
    }

    private void serverTick() {
        flushOutput();
        if (!hasAvailableOutputSlot()) {
            setWorking(false);
            return;
        }

        findBedrockOre();
        if (!hasAvailableInputOre()) {
            setWorking(false);
            return;
        }

        if (getInternalPowerEfficiency() > 0 || getExternalPowerEfficiency() > 0) {
            updateBurnTime();
            if (!hasRemainingBurnTime()) {
                setWorking(false);
                return;
            }
        }

        extractBedrockOre();
        setWorking(true);
    }

    // --------------------------------------------------------------------- //
    // BlockEntityWithInfo

    @Override
    protected Component buildInfo() {
        final int yield = findBedrockOres().
                map(BedrockOreBlockEntity::getAmount).
                reduce(Integer::sum).
                orElse(0);
        if (yield > 0) {
            return Component.translatable(Constants.GUI_EXPECTED_YIELD, yield);
        } else {
            return Component.translatable(Constants.GUI_EXHAUSTED);
        }
    }

    // --------------------------------------------------------------------- //
    // BlockEntity

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        final var tag = new CompoundTag();
        tag.putBoolean(TAG_WORKING, isWorkingServer);
        return tag;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put(TAG_FUEL_INVENTORY, fuelInventory.serializeNBT());
        tag.put(TAG_OUTPUT_INVENTORY, outputInventory.serializeNBT());
        tag.putInt(TAG_ENERGY_STORAGE, energyStorage.getEnergyStored());
        tag.putInt(TAG_REMAINING_BURN_TIME, remainingBurnTime);
        tag.putInt(TAG_EXTRACTION_COOLDOWN, extractionCooldown);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        fuelInventory.deserializeNBT(tag.getCompound(TAG_FUEL_INVENTORY));
        outputInventory.deserializeNBT(tag.getCompound(TAG_OUTPUT_INVENTORY));
        energyStorage.setEnergy(tag.getInt(TAG_ENERGY_STORAGE));
        remainingBurnTime = tag.getInt(TAG_REMAINING_BURN_TIME);
        extractionCooldown = tag.getInt(TAG_EXTRACTION_COOLDOWN);
        isWorkingClient = tag.getBoolean(TAG_WORKING);
    }

    // --------------------------------------------------------------------- //
    // ICapabilityProvider

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap, @org.jetbrains.annotations.Nullable final Direction side) {
        if (side != null) {
            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                if (side == Direction.UP) {
                    return LazyOptional.of(() -> outputInventory).cast();
                }
                if (side.getAxis().isHorizontal() && getInternalPowerEfficiency() > 0) {
                    return LazyOptional.of(() -> fuelInventory).cast();
                }
            } else if (cap == ForgeCapabilities.ENERGY) {
                if (side.getAxis().isHorizontal() && getExternalPowerEfficiency() > 0) {
                    return LazyOptional.of(() -> energyStorage).cast();
                }
            }
        }
        return super.getCapability(cap, side);
    }

    // --------------------------------------------------------------------- //

    private void updateEffects() {
        if (!isWorkingClient) {
            return;
        }

        if (soundCooldown > 0) {
            soundCooldown--;
        }

        final var level = getLevel();
        if (level == null) {
            return;
        }

        if (soundCooldown <= 0) {
            soundCooldown = SOUND_INTERVAL;
            final var player = Minecraft.getInstance().player;
            if (player != null) {
                final var pos = Vec3.atCenterOf(getBlockPos());
                final var volume = 1.0f;
                final var range = Sounds.MINER.get().getRange(volume);
                if (player.distanceToSqr(pos) < range * range) {
                    level.playLocalSound(pos.x(), pos.y(), pos.z(), Sounds.MINER.get(), SoundSource.BLOCKS, volume, 1, false);
                }
            }
        }

        final var rng = level.random;
        for (final var facing : Direction.Plane.HORIZONTAL) {
            final var direction = new Vec3(facing.step());

            final var up = new Vec3(0, 1, 0);
            final var right = direction.cross(up);
            final var dx = (rng.nextFloat() - 0.5f) * 0.3f;
            final var dy = (rng.nextFloat() - 0.5f) * 0.3f;

            final var origin = Vec3.atCenterOf(getBlockPos()).
                    add(direction.scale(0.5)).
                    add(right.scale(dx)).
                    add(up.scale(dy));
            final var velocity = direction.scale(0.05);

            level.addParticle(ParticleTypes.SMOKE, origin.x, origin.y, origin.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void flushOutput() {
        final var level = getLevel();
        if (level == null) {
            return;
        }

        final var outputSlot = findFirstNonEmptyOutputSlot();
        if (outputSlot < 0) {
            return;
        }

        if (transferCooldown > 0) {
            --transferCooldown;
        }
        if (transferCooldown > 0) {
            return;
        }

        LazyOptional<IItemHandler> optionalItemhandler = LazyOptional.empty();

        final var blockPos = getBlockPos().above();
        final var blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            optionalItemhandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN);
        }

        if (!optionalItemhandler.isPresent()) {
            final var entities = level.getEntities((Entity) null, new AABB(blockPos), entity -> entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).isPresent());
            if (!entities.isEmpty()) {
                final Entity entity = entities.get(level.random.nextInt(entities.size()));
                optionalItemhandler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN);
            }
        }

        if (!optionalItemhandler.isPresent()) {
            transferCooldown = 20;
            return;
        }

        final var stack = outputInventory.getStackInSlot(outputSlot);
        if (optionalItemhandler.isPresent()) {
            final var itemHandler = optionalItemhandler.orElseThrow(AssertionError::new);
            final ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), false);
            if (!ItemStack.matches(stack, remainder)) {
                outputInventory.setStackInSlot(outputSlot, remainder);
                setChanged();
            }
        }

        transferCooldown = 10;
    }

    private int findFirstNonEmptyOutputSlot() {
        for (var slot = 0; slot < outputInventory.getSlots(); ++slot) {
            final var stack = outputInventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private boolean hasAvailableOutputSlot() {
        for (var slot = 0; slot < outputInventory.getSlots(); ++slot) {
            final var stack = outputInventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void findBedrockOre() {
        if (hasNoMoreOres) {
            return;
        }

        if (currentOre == null || currentOre.isRemoved() || currentOre.getAmount() <= 0) {
            currentOre = findBedrockOres().findFirst().orElse(null);
            if (currentOre == null) {
                hasNoMoreOres = true;
                setWorking(false);
            }
        }
    }

    private boolean hasAvailableInputOre() {
        return currentOre != null;
    }

    private void updateBurnTime() {
        if (remainingBurnTime > 0) {
            --remainingBurnTime;
        }

        if (remainingBurnTime <= 0 && getExternalPowerEfficiency() > 0) {
            final var energyBurnTime = energyStorage.consumeEnergyForBurnTime();
            final var scaledBurnTime = Mth.ceil(energyBurnTime * getExternalPowerEfficiency());
            if (scaledBurnTime > 0) {
                remainingBurnTime = scaledBurnTime;
                setChanged();
            }
        }

        if (remainingBurnTime <= 0 && getInternalPowerEfficiency() > 0) {
            final var stack = fuelInventory.extractItem(0, Integer.MAX_VALUE, false);
            final var stackBurnTime = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
            final var scaledBurnTime = Mth.ceil(stackBurnTime * getInternalPowerEfficiency());
            if (scaledBurnTime > 0) {
                remainingBurnTime = scaledBurnTime;
                setChanged();
            }
        }
    }

    private boolean hasRemainingBurnTime() {
        return remainingBurnTime > 0;
    }

    private void extractBedrockOre() {
        final var bedrockOre = requireNonNull(currentOre);

        if (extractionCooldown > 0) {
            extractionCooldown--;
            return;
        }

        final var level = requireNonNull(getLevel());
        final var pos = bedrockOre.getBlockPos();

        ItemHandlerHelper.insertItem(outputInventory, bedrockOre.extract(), false);
        setChanged();

        extractionCooldown = Settings.minerExtractionCooldown.get();

        final var oreState = bedrockOre.getOreBlockState();
        final var soundType = oreState.getSoundType(level, pos, null);
        final var blockCenter = Vec3.atCenterOf(pos);
        level.playSound(null, blockCenter.x(), blockCenter.y(), blockCenter.z(), soundType.getBreakSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
    }

    private Stream<BedrockOreBlockEntity> findBedrockOres() {
        return StreamSupport.stream(new ScanAreaSpliterator(), false);
    }

    private void setWorking(final boolean value) {
        isWorkingServer = value;

        if (isWorkingServer == isWorkingClient) {
            sendUpdateTagAfter = null;
        } else if (sendUpdateTagAfter == null) {
            sendUpdateTagAfter = Instant.now().plus(SEND_WORKING_STATE_DELAY);
        }

        if (sendUpdateTagAfter != null && Instant.now().isAfter(sendUpdateTagAfter)) {
            sendUpdateTagAfter = null;
            isWorkingClient = isWorkingServer;
            requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    private static double getInternalPowerEfficiency() {
        return Settings.minerEfficiency.get() * Settings.minerEfficiencyInternalPower.get();
    }

    private static double getExternalPowerEfficiency() {
        return Settings.minerEfficiency.get() * Settings.minerEfficiencyExternalPower.get();
    }

    // --------------------------------------------------------------------- //

    private final class ScanAreaSpliterator extends Spliterators.AbstractSpliterator<BedrockOreBlockEntity> {
        private int x, y, z;

        ScanAreaSpliterator() {
            super((SCAN_RADIUS * 2 + 1) * (SCAN_RADIUS * 2 + 1) * SCAN_DEPTH, ORDERED | DISTINCT | SIZED | NONNULL | IMMUTABLE | SUBSIZED);
            this.x = -SCAN_RADIUS;
            this.z = -SCAN_RADIUS;
            this.y = 0;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super BedrockOreBlockEntity> action) {
            final var scanLevel = requireNonNull(getLevel());
            while (y < SCAN_DEPTH) {
                final var pos = getBlockPos().below().offset(x, -y, z);

                x++;
                if (x > SCAN_RADIUS) {
                    x = -SCAN_RADIUS;
                    z++;
                    if (z > SCAN_RADIUS) {
                        z = -SCAN_RADIUS;
                        y++;
                    }
                }

                final var tileEntity = scanLevel.getBlockEntity(pos);
                if (tileEntity instanceof BedrockOreBlockEntity bedrockOre) {
                    action.accept(bedrockOre);
                    return true;
                }
            }
            return false;
        }
    }

    private final class FuelItemHandler extends ItemStackHandler {
        FuelItemHandler() {
            super(SLOT_FUEL_COUNT);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
            if (getInternalPowerEfficiency() <= 0) {
                return stack;
            }

            final int stackBurnTime = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
            final var scaledBurnTime = Mth.ceil(stackBurnTime * getInternalPowerEfficiency());
            if (scaledBurnTime <= 0) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public int getSlotLimit(final int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(final int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    }

    private final class OutputItemHandler extends ItemStackHandler {
        OutputItemHandler() {
            super(SLOT_OUTPUT_COUNT);
        }

        @Override
        public int getSlotLimit(final int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(final int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    }

    private static final class EnergyStorageMiner extends EnergyStorage {
        public EnergyStorageMiner() {
            super(computeCapacity(), computeCapacity(), 0);
        }

        public void setEnergy(final int value) {
            this.energy = value;
        }

        public int consumeEnergyForBurnTime() {
            final var availableBurnTime = energy / RF_PER_BURN_TIME;
            final var usedEnergy = Math.min(energy, availableBurnTime * RF_PER_BURN_TIME);
            energy -= usedEnergy;
            return availableBurnTime;
        }

        private static int computeCapacity() {
            return Math.max(100, Mth.ceil(ForgeHooks.getBurnTime(new ItemStack(Items.COAL), RecipeType.SMELTING) / (RF_PER_BURN_TIME * getExternalPowerEfficiency())));
        }
    }
}
