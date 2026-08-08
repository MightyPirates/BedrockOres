package li.cil.bedrockores.fabric;

import li.cil.bedrockores.common.block.entity.BedrockOreMinerBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public final class MinerEnergyStorage extends SnapshotParticipant<Integer> implements EnergyStorage {
    private final BedrockOreMinerBlockEntity miner;

    public MinerEnergyStorage(final BedrockOreMinerBlockEntity miner) {
        this.miner = miner;
    }

    // --------------------------------------------------------------------- //

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        final var requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        final var accepted = miner.receiveEnergy(requested, true);
        if (accepted <= 0) {
            return 0;
        }

        updateSnapshots(transaction);
        miner.receiveEnergy(accepted, false);
        return accepted;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(final long maxAmount, final TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmount() {
        return miner.getEnergyStored();
    }

    @Override
    public long getCapacity() {
        return miner.getEnergyCapacity();
    }

    // --------------------------------------------------------------------- //

    @Override
    protected Integer createSnapshot() {
        return miner.getEnergyStored();
    }

    @Override
    protected void readSnapshot(final Integer snapshot) {
        miner.setEnergyStored(snapshot);
    }
}
