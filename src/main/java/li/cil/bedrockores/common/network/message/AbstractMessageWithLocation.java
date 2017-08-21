package li.cil.bedrockores.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractMessageWithLocation extends AbstractMessageWithDimension {
    private BlockPos position;

    protected AbstractMessageWithLocation(final World world, final BlockPos position) {
        super(world);
        this.position = position;
    }

    protected AbstractMessageWithLocation() {
    }

    // --------------------------------------------------------------------- //

    public BlockPos getPosition() {
        return position;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);
        final PacketBuffer buffer = new PacketBuffer(buf);
        position = buffer.readBlockPos();

    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeBlockPos(position);
    }
}
