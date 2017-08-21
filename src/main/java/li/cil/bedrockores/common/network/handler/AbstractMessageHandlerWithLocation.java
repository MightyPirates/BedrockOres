package li.cil.bedrockores.common.network.handler;

import li.cil.bedrockores.common.network.message.AbstractMessageWithLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithLocation<T extends AbstractMessageWithLocation> extends AbstractMessageHandlerWithDimension<T> {
    @Nullable
    protected TileEntity getTileEntity(final T message, final MessageContext context) {
        final World world = getWorld(message, context);
        if (world == null) {
            return null;
        }
        if (!world.isBlockLoaded(message.getPosition())) {
            return null;
        }
        return world.getTileEntity(message.getPosition());
    }
}
