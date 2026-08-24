/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import li.cil.bedrockores.common.network.message.InfoRequestMessage;
import li.cil.bedrockores.common.network.message.InfoResponseMessage;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class Network {
    public static void initialize() {
        NetworkManager.registerReceiver(NetworkManager.c2s(),
                InfoRequestMessage.TYPE, InfoRequestMessage.STREAM_CODEC, InfoRequestMessage::handle);

        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(InfoResponseMessage.TYPE, InfoResponseMessage.STREAM_CODEC);
        }
    }

    // --------------------------------------------------------------------- //

    public static <T extends CustomPacketPayload> void sendToServer(final T message) {
        NetworkManager.sendToServer(message);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(final ServerPlayer player, final T message) {
        NetworkManager.sendToPlayer(player, message);
    }

    private Network() {
    }
}
