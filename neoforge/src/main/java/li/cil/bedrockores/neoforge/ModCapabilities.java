/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.neoforge;

import li.cil.bedrockores.common.block.entity.BedrockOreMinerBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class ModCapabilities {
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BlockEntities.MINER.get(),
                (miner, side) -> side == null ? null : new SidedInvWrapper(miner, side));

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntities.MINER.get(),
                (miner, side) -> side != null && side.getAxis().isHorizontal() && miner.getEnergyCapacity() > 0
                        ? new MinerEnergyStorage(miner)
                        : null);
    }

    // --------------------------------------------------------------------- //

    private record MinerEnergyStorage(BedrockOreMinerBlockEntity miner) implements IEnergyStorage {
        @Override
        public int receiveEnergy(final int toReceive, final boolean simulate) {
            return miner.receiveEnergy(toReceive, simulate);
        }

        @Override
        public int extractEnergy(final int toExtract, final boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return miner.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return miner.getEnergyCapacity();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private ModCapabilities() {
    }
}
