package li.cil.bedrockores.common.network;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.network.handler.MessageHandlerRequestLookAtInfo;
import li.cil.bedrockores.common.network.message.MessageRequestLookAtInfo;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum Network {
    INSTANCE;

    private SimpleNetworkWrapper wrapper;

    private enum Messages {
        RequestLookAtInfo
    }

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);

        wrapper.registerMessage(MessageHandlerRequestLookAtInfo.class, MessageRequestLookAtInfo.class, Messages.RequestLookAtInfo.ordinal(), Side.SERVER);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }
}
