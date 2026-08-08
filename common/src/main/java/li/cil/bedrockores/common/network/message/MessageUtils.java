package li.cil.bedrockores.common.network.message;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class MessageUtils {
    public static <T extends BlockEntity> void withBlockEntity(@Nullable final Level level, final BlockPos position, final Class<T> type, final Consumer<T> callback) {
        if (level == null) {
            return;
        }

        final var chunkPos = new ChunkPos(position);
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return;
        }

        final var blockEntity = level.getBlockEntity(position);
        if (type.isInstance(blockEntity)) {
            callback.accept(type.cast(blockEntity));
        }
    }

    private MessageUtils() {
    }
}
