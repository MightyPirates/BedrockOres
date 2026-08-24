/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity;

import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.network.message.InfoRequestMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class BlockEntityWithInfo extends BlockEntity {
    private static final long UPDATE_INTERVAL_TICKS = 10;

    @Nullable
    private Component currentInfo;
    private long infoValidUntilTick = Long.MIN_VALUE;

    // --------------------------------------------------------------------- //

    protected BlockEntityWithInfo(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    // --------------------------------------------------------------------- //
    // LookAtInfoProvider

    @Nullable
    public final Component getLookAtInfo() {
        final var level = getLevel();
        if (level == null) {
            return null;
        }

        final var now = level.getGameTime();
        if (now >= infoValidUntilTick) {
            infoValidUntilTick = now + UPDATE_INTERVAL_TICKS;
            if (level.isClientSide()) {
                Network.sendToServer(new InfoRequestMessage(getBlockPos()));
            } else {
                currentInfo = buildInfo();
            }
        }

        return currentInfo;
    }

    public void setInfoClient(@Nullable final Component info) {
        this.currentInfo = info;
    }

    protected abstract Component buildInfo();
}
