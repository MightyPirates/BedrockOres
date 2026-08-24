/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.neoforge;

import li.cil.bedrockores.common.block.entity.BedrockOreMinerBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class ModCapabilities {
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntities.MINER.get(),
                (miner, side) -> side == null ? null : new WorldlyContainerWrapper(miner, side));

        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntities.MINER.get(),
                (miner, side) -> side != null && side.getAxis().isHorizontal() && miner.getEnergyCapacity() > 0
                        ? new MinerEnergyHandler(miner)
                        : null);
    }

    // --------------------------------------------------------------------- //

    private static final class MinerEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        private final BedrockOreMinerBlockEntity miner;

        MinerEnergyHandler(final BedrockOreMinerBlockEntity miner) {
            this.miner = miner;
        }

        @Override
        public long getAmountAsLong() {
            return miner.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return miner.getEnergyCapacity();
        }

        @Override
        public int insert(final int amount, final TransactionContext transaction) {
            final var accepted = miner.receiveEnergy(amount, true);
            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            miner.receiveEnergy(accepted, false);
            return accepted;
        }

        @Override
        public int extract(final int amount, final TransactionContext transaction) {
            return 0;
        }

        @Override
        protected Integer createSnapshot() {
            return miner.getEnergyStored();
        }

        @Override
        protected void revertToSnapshot(final Integer snapshot) {
            miner.setEnergyStored(snapshot);
        }
    }

    private ModCapabilities() {
    }
}
