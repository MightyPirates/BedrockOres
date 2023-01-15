package li.cil.bedrockores.common.network;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.network.message.AbstractMessage;
import li.cil.bedrockores.common.network.message.InfoRequestMessage;
import li.cil.bedrockores.common.network.message.InfoResponseMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public final class Network {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // --------------------------------------------------------------------- //

    private static int nextPacketId = 1;

    // --------------------------------------------------------------------- //

    public static void initialize() {
        registerMessage(InfoRequestMessage.class, InfoRequestMessage::new, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(InfoResponseMessage.class, InfoResponseMessage::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T extends AbstractMessage> void sendToServer(final T message) {
        Network.INSTANCE.sendToServer(message);
    }

    // --------------------------------------------------------------------- //

    private static <T extends AbstractMessage> void registerMessage(final Class<T> type, final Function<FriendlyByteBuf, T> decoder, final NetworkDirection direction) {
        INSTANCE.messageBuilder(type, getNextPacketId(), direction)
                .encoder(AbstractMessage::toBytes)
                .decoder(decoder)
                .consumerNetworkThread(AbstractMessage::handleMessage)
                .add();
    }

    private static int getNextPacketId() {
        return nextPacketId++;
    }
}
