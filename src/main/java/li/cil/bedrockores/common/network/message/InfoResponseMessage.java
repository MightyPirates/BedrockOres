package li.cil.bedrockores.common.network.message;

import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.network.CustomPayloadEvent;

import javax.annotation.Nullable;

public final class InfoResponseMessage extends AbstractMessageWithPosition {
    @Nullable
    private Component info;

    // --------------------------------------------------------------------- //

    public InfoResponseMessage(final BlockPos position, @Nullable final Component info) {
        super(position);
        this.info = info;
    }

    public InfoResponseMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        super.fromBytes(buffer);
        final var hasInfo = buffer.readBoolean();
        if (hasInfo) {
            info = buffer.readComponent();
        } else {
            info = null;
        }
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        final var hasInfo = info != null;
        buffer.writeBoolean(hasInfo);
        if (hasInfo) {
            buffer.writeComponent(info);
        }
    }

    @Override
    public void handleMessage(final CustomPayloadEvent.Context context) {
        withBlockEntity(context, BlockEntityWithInfo.class, blockEntity -> blockEntity.setInfoClient(info));
    }
}
