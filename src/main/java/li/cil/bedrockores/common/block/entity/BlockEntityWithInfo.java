package li.cil.bedrockores.common.block.entity;

import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.network.message.InfoRequestMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

public abstract class BlockEntityWithInfo extends BlockEntity {
    public static final TemporalAmount UPDATE_INTERVAL = Duration.ofMillis(500);

    private Component currentInfo;
    private Instant infoValidUntil = Instant.MIN;

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

        if (Instant.now().isAfter(infoValidUntil)) {
            infoValidUntil = Instant.now().plus(UPDATE_INTERVAL);
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
