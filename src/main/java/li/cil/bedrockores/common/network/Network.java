package li.cil.bedrockores.common.network;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.network.message.AbstractMessage;
import li.cil.bedrockores.common.network.message.InfoRequestMessage;
import li.cil.bedrockores.common.network.message.InfoResponseMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.Function;

public final class Network {
    private static final int PROTOCOL_VERSION = 1;

    private static final PacketDistributor.PacketTarget TO_SERVER = PacketDistributor.SERVER.noArg();

    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(new ResourceLocation(Constants.MOD_ID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .simpleChannel();

    // --------------------------------------------------------------------- //

    private static int nextPacketId = 1;

    // --------------------------------------------------------------------- //

    public static void initialize() {
        registerMessage(InfoRequestMessage.class, InfoRequestMessage::new, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(InfoResponseMessage.class, InfoResponseMessage::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T extends AbstractMessage> void sendToServer(final T message) {
        Network.INSTANCE.send(message, TO_SERVER);
    }

    // --------------------------------------------------------------------- //

    private static <T extends AbstractMessage> void registerMessage(final Class<T> type, final Function<FriendlyByteBuf, T> decoder, final NetworkDirection direction) {
        INSTANCE.messageBuilder(type, getNextPacketId(), direction)
                .encoder(AbstractMessage::toBytes)
                .decoder(decoder)
                .consumerNetworkThread(AbstractMessage::handleMessageAsync)
                .add();
    }

    private static int getNextPacketId() {
        return nextPacketId++;
    }
}
