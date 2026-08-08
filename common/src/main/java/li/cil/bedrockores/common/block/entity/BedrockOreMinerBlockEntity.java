package li.cil.bedrockores.common.block.entity;

import dev.architectury.registry.fuel.FuelRegistry;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.sound.Sounds;
import li.cil.bedrockores.common.util.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.partitioningBy;

public final class BedrockOreMinerBlockEntity extends BlockEntityWithInfo implements WorldlyContainer {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int energyStored = 0;
    private int remainingBurnTime = 0;
    private int extractionCooldown = -1;
    private int transferCooldown = 20;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_ENERGY_STORAGE = "energyStorage";
    private static final String TAG_REMAINING_BURN_TIME = "burnTime";
    private static final String TAG_EXTRACTION_COOLDOWN = "extractionCooldown";
    private static final String TAG_WORKING = "working";

    private static final long SEND_WORKING_STATE_DELAY_TICKS = 20;

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_OUTPUT_FIRST = 1;
    private static final int SLOT_OUTPUT_COUNT = 6;
    private static final int SLOT_COUNT = SLOT_OUTPUT_FIRST + SLOT_OUTPUT_COUNT;

    private static final int[] SLOTS_FUEL = {SLOT_FUEL};
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4, 5, 6};
    private static final int[] SLOTS_NONE = {};

    private static final int RF_PER_BURN_TIME = 10;

    private static final int SOUND_INTERVAL = 30; // in ticks

    private static final long NO_PENDING_UPDATE = -1;

    @Nullable
    private BedrockOreBlockEntity currentOre;
    private boolean hasNoMoreOres;

    // We delay sending the working state to clients a little to avoid small
    // hiccups causing unnecessary update packets being sent.
    private boolean isWorkingServer, isWorkingClient;
    private long sendUpdateTagAtTick = NO_PENDING_UPDATE;

    private int soundCooldown;

    private int cachedEnergyCapacity = -1;
    private double cachedEnergyCapacityEfficiency = Double.NaN;

    // --------------------------------------------------------------------- //

    public BedrockOreMinerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.MINER.get(), pos, state);
    }

    // --------------------------------------------------------------------- //

    public boolean isWorking() {
        return isWorkingServer;
    }

    // --------------------------------------------------------------------- //
    // Energy

    public int getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(final int value) {
        energyStored = value;
    }

    public int getEnergyCapacity() {
        final var efficiency = getExternalPowerEfficiency();
        if (efficiency <= 0) {
            return 0;
        }

        if (cachedEnergyCapacity < 0 || cachedEnergyCapacityEfficiency != efficiency) {
            final var capacity = Math.max(100, Mth.ceil(getFuelBurnTime(new ItemStack(Items.COAL)) / (RF_PER_BURN_TIME * efficiency)));
            if (getLevel() == null) {
                return capacity;
            }
            cachedEnergyCapacityEfficiency = efficiency;
            cachedEnergyCapacity = capacity;
        }
        return cachedEnergyCapacity;
    }

    private int getFuelBurnTime(final ItemStack stack) {
        final var level = getLevel();
        if (level == null || stack.isEmpty()) {
            return 0;
        }
        return FuelRegistry.get(stack, RecipeType.SMELTING, level.fuelValues());
    }

    public int receiveEnergy(final int amount, final boolean simulate) {
        if (getExternalPowerEfficiency() <= 0) {
            return 0;
        }

        final var accepted = Math.min(amount, getEnergyCapacity() - energyStored);
        if (accepted > 0 && !simulate) {
            energyStored += accepted;
            setChanged();
        }
        return Math.max(0, accepted);
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
        flushWorkingState();

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
        final var ores = findBedrockOres().
                map(BedrockOreBlockEntity::getAmount).
                collect(partitioningBy(OptionalInt::isPresent));

        // If there are any infinite ore spawns within range, we can consider this miner to have infinite yield.
        final var infiniteOres = ores.get(false);
        if (!infiniteOres.isEmpty()) {
            return Component.translatable(Constants.GUI_EXPECTED_YIELD, Component.translatable(Constants.GUI_INFINITE));
        }

        final var finiteOres = ores.get(true);
        @SuppressWarnings("OptionalGetWithoutIsPresent") final int yield = finiteOres.stream().
                map(OptionalInt::getAsInt).
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
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        final var tag = new CompoundTag();
        tag.putBoolean(TAG_WORKING, isWorkingServer);
        return tag;
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, items);
        output.putInt(TAG_ENERGY_STORAGE, energyStored);
        output.putInt(TAG_REMAINING_BURN_TIME, remainingBurnTime);
        output.putInt(TAG_EXTRACTION_COOLDOWN, extractionCooldown);
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);

        // Both the full load and the network update tag come through here, and the update tag only
        // carries the working flag — so every field defaults to what it already holds rather than
        // to zero, or a client-side update would wipe the values it does not send.
        if (input.childrenList(TAG_ITEMS).isPresent()) {
            items.clear();
            ContainerHelper.loadAllItems(input, items);
        }
        energyStored = input.getIntOr(TAG_ENERGY_STORAGE, energyStored);
        remainingBurnTime = input.getIntOr(TAG_REMAINING_BURN_TIME, remainingBurnTime);
        extractionCooldown = input.getIntOr(TAG_EXTRACTION_COOLDOWN, extractionCooldown);
        isWorkingClient = input.getBooleanOr(TAG_WORKING, isWorkingClient);
    }

    // --------------------------------------------------------------------- //
    // Container

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (final var stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(final int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        final var result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize());
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(final Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return slot == SLOT_FUEL && getInternalPowerEfficiency() > 0 && getScaledFuelBurnTime(stack) > 0;
    }

    // --------------------------------------------------------------------- //
    // WorldlyContainer

    @Override
    public int[] getSlotsForFace(final Direction side) {
        if (side == Direction.UP) {
            return SLOTS_OUTPUT;
        }
        if (side.getAxis().isHorizontal() && getInternalPowerEfficiency() > 0) {
            return SLOTS_FUEL;
        }
        return SLOTS_NONE;
    }

    @Override
    public boolean canPlaceItemThroughFace(final int slot, final ItemStack stack, @Nullable final Direction side) {
        return side != null && side.getAxis().isHorizontal() && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(final int slot, final ItemStack stack, final Direction side) {
        return side == Direction.UP && slot >= SLOT_OUTPUT_FIRST;
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
            final var direction = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());

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

        final var stack = items.get(outputSlot);
        final var remainder = InventoryUtils.insert(level, getBlockPos().above(), Direction.DOWN, stack.copy());
        if (remainder == null) {
            transferCooldown = 20;
            return;
        }

        if (!ItemStack.matches(stack, remainder)) {
            items.set(outputSlot, remainder);
            setChanged();
        }

        transferCooldown = 10;
    }

    private int findFirstNonEmptyOutputSlot() {
        for (var slot = SLOT_OUTPUT_FIRST; slot < SLOT_COUNT; ++slot) {
            if (!items.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private boolean hasAvailableOutputSlot() {
        for (var slot = SLOT_OUTPUT_FIRST; slot < SLOT_COUNT; ++slot) {
            if (items.get(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void insertIntoOutput(final ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (var slot = SLOT_OUTPUT_FIRST; slot < SLOT_COUNT; ++slot) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, stack);
                return;
            }
        }
    }

    private void findBedrockOre() {
        if (hasNoMoreOres) {
            return;
        }

        if (currentOre == null || currentOre.isRemoved() || currentOre.isEmpty()) {
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
            final var energyBurnTime = consumeEnergyForBurnTime();
            final var scaledBurnTime = Mth.ceil(energyBurnTime * getExternalPowerEfficiency());
            if (scaledBurnTime > 0) {
                remainingBurnTime = scaledBurnTime;
                setChanged();
            }
        }

        if (remainingBurnTime <= 0 && getInternalPowerEfficiency() > 0) {
            final var scaledBurnTime = getScaledFuelBurnTime(items.get(SLOT_FUEL));
            if (scaledBurnTime > 0) {
                items.set(SLOT_FUEL, ItemStack.EMPTY);
                remainingBurnTime = scaledBurnTime;
                setChanged();
            }
        }
    }

    private int consumeEnergyForBurnTime() {
        final var availableBurnTime = energyStored / RF_PER_BURN_TIME;
        final var usedEnergy = Math.min(energyStored, availableBurnTime * RF_PER_BURN_TIME);
        energyStored -= usedEnergy;
        return availableBurnTime;
    }

    private int getScaledFuelBurnTime(final ItemStack stack) {
        return Mth.ceil(getFuelBurnTime(stack) * getInternalPowerEfficiency());
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

        insertIntoOutput(bedrockOre.extract());
        setChanged();

        extractionCooldown = Settings.minerExtractionCooldown.get();

        final var oreState = bedrockOre.getOreBlockState();
        final var soundType = oreState.getSoundType();
        final var blockCenter = Vec3.atCenterOf(pos);
        level.playSound(null, blockCenter.x(), blockCenter.y(), blockCenter.z(), soundType.getBreakSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
    }

    private Stream<BedrockOreBlockEntity> findBedrockOres() {
        return StreamSupport.stream(new ScanAreaSpliterator(), false);
    }

    private void setWorking(final boolean value) {
        isWorkingServer = value;

        if (isWorkingServer == isWorkingClient) {
            sendUpdateTagAtTick = NO_PENDING_UPDATE;
        } else if (sendUpdateTagAtTick == NO_PENDING_UPDATE) {
            sendUpdateTagAtTick = requireNonNull(getLevel()).getGameTime() + SEND_WORKING_STATE_DELAY_TICKS;
        }
    }

    private void flushWorkingState() {
        if (sendUpdateTagAtTick == NO_PENDING_UPDATE) {
            return;
        }

        final var level = requireNonNull(getLevel());
        if (level.getGameTime() < sendUpdateTagAtTick) {
            return;
        }

        sendUpdateTagAtTick = NO_PENDING_UPDATE;
        isWorkingClient = isWorkingServer;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private static double getInternalPowerEfficiency() {
        return Settings.minerEfficiency.get() * Settings.minerEfficiencyInternalPower.get();
    }

    private static double getExternalPowerEfficiency() {
        return Settings.minerEfficiency.get() * Settings.minerEfficiencyExternalPower.get();
    }

    // --------------------------------------------------------------------- //

    private final class ScanAreaSpliterator extends Spliterators.AbstractSpliterator<BedrockOreBlockEntity> {
        private final int radius, layers;
        private int x, y, z;

        ScanAreaSpliterator() {
            this(Settings.minerAreaRadius.get() - 1, Settings.minerAreaLayers.get());
        }

        private ScanAreaSpliterator(final int radius, final int layers) {
            super(numberOfBlocksInArea(radius, layers), ORDERED | DISTINCT | SIZED | NONNULL | IMMUTABLE | SUBSIZED);
            this.radius = radius;
            this.layers = layers;
            this.x = -radius;
            this.z = -radius;
            this.y = 0;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super BedrockOreBlockEntity> action) {
            final var scanLevel = requireNonNull(getLevel());
            while (y < layers) {
                final var pos = getBlockPos().below().offset(x, -y, z);

                x++;
                if (x > radius) {
                    x = -radius;
                    z++;
                    if (z > radius) {
                        z = -radius;
                        y++;
                    }
                }

                final var blockEntity = scanLevel.getBlockEntity(pos);
                if (blockEntity instanceof final BedrockOreBlockEntity bedrockOre) {
                    action.accept(bedrockOre);
                    return true;
                }
            }
            return false;
        }

        private static int numberOfBlocksInArea(final int radius, final int layers) {
            return (radius * 2 + 1) * (radius * 2 + 1) * layers;
        }
    }
}
