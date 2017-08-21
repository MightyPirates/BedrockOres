package li.cil.bedrockores.common.network.handler;

import li.cil.bedrockores.common.network.message.MessageRequestLookAtInfo;
import li.cil.bedrockores.common.tileentity.LookAtInfoProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHandlerRequestLookAtInfo extends AbstractMessageHandlerWithLocation<MessageRequestLookAtInfo> {
    @Override
    protected void onMessageSynchronized(final MessageRequestLookAtInfo message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (tileEntity instanceof LookAtInfoProvider) {
            ((LookAtInfoProvider) tileEntity).updateLookAtInfo();
        }
    }
}
