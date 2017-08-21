package li.cil.bedrockores.common.tileentity;

import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.network.message.MessageRequestLookAtInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractLookAtInfoProvider extends TileEntity implements LookAtInfoProvider {
    // On the client this tracks when last we sent a info request, on the server
    // it tracks when last we responded to one.
    private long lastInfoPacket;

    // --------------------------------------------------------------------- //

    @SideOnly(Side.CLIENT)
    protected abstract String getLookAtInfoImpl();

    protected void updateLookAtInfoImpl() {
        sendBlockUpdatePacket();
    }

    protected final void sendBlockUpdatePacket() {
        final Chunk chunk = getWorld().getChunkFromBlockCoords(getPos());
        final IBlockState state = getWorld().getBlockState(getPos());
        getWorld().markAndNotifyBlock(getPos(), chunk, state, state, 3);
    }

    // --------------------------------------------------------------------- //
    // LookAtInfoProvider

    @SideOnly(Side.CLIENT)
    @Override
    public final String getLookAtInfo() {
        if (needsUpdate()) {
            Network.INSTANCE.getWrapper().sendToServer(new MessageRequestLookAtInfo(getWorld(), getPos()));
        }
        return getLookAtInfoImpl();
    }

    @Override
    public void updateLookAtInfo() {
        if (needsUpdate()) {
            lastInfoPacket = System.currentTimeMillis();
            updateLookAtInfoImpl();
        }
    }

    // --------------------------------------------------------------------- //

    private boolean needsUpdate() {
        return System.currentTimeMillis() - lastInfoPacket > 500;
    }
}
