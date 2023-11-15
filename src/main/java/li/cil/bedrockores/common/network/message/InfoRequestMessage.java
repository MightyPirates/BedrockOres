package li.cil.bedrockores.common.network.message;

import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import li.cil.bedrockores.common.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public final class InfoRequestMessage extends AbstractMessageWithPosition {
    public InfoRequestMessage(final BlockPos position) {
        super(position);
    }

    public InfoRequestMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final CustomPayloadEvent.Context context) {
        withBlockEntity(context, BlockEntityWithInfo.class, blockEntity ->
                Network.INSTANCE.reply(new InfoResponseMessage(blockEntity.getBlockPos(), blockEntity.getLookAtInfo()), context));
    }
}
