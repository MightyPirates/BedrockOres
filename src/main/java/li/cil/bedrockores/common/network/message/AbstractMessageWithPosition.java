package li.cil.bedrockores.common.network.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Consumer;

public abstract class AbstractMessageWithPosition extends AbstractMessage {
    private BlockPos position;

    protected AbstractMessageWithPosition(final BlockPos position) {
        this.position = position;
    }

    protected AbstractMessageWithPosition(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //

    protected <T extends BlockEntity> void withBlockEntity(final NetworkEvent.Context context, final Class<T> type, final Consumer<T> callback) {
        final Level level = getLevel(context);
        if (level != null) {
            withBlockEntity(level, type, callback);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends BlockEntity> void withBlockEntity(final Level level, final Class<T> type, final Consumer<T> callback) {
        final ChunkPos chunkPos = new ChunkPos(position);
        if (level.hasChunk(chunkPos.x, chunkPos.z)) {
            final BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity != null && type.isAssignableFrom(blockEntity.getClass())) {
                callback.accept((T) blockEntity);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        position = buffer.readBlockPos();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }
}
