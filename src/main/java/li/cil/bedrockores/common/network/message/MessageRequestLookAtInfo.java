package li.cil.bedrockores.common.network.message;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class MessageRequestLookAtInfo extends AbstractMessageWithLocation {
    public MessageRequestLookAtInfo(final World world, final BlockPos position) {
        super(world, position);
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageRequestLookAtInfo() {
    }
}
