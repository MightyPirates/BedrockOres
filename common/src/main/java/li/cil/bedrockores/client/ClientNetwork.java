package li.cil.bedrockores.client;

import dev.architectury.networking.NetworkManager;
import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import li.cil.bedrockores.common.network.message.InfoResponseMessage;
import li.cil.bedrockores.common.network.message.MessageUtils;
import net.minecraft.client.Minecraft;

public final class ClientNetwork {
    public static void initialize() {
        NetworkManager.registerReceiver(NetworkManager.s2c(),
                InfoResponseMessage.TYPE, InfoResponseMessage.STREAM_CODEC, ClientNetwork::handleInfoResponse);
    }

    // --------------------------------------------------------------------- //

    private static void handleInfoResponse(final InfoResponseMessage message, final NetworkManager.PacketContext context) {
        context.queue(() -> MessageUtils.withBlockEntity(
                Minecraft.getInstance().level,
                message.position(),
                BlockEntityWithInfo.class,
                blockEntity -> blockEntity.setInfoClient(message.info().orElse(null))));
    }

    private ClientNetwork() {
    }
}
