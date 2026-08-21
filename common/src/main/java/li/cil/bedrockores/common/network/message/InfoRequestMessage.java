/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public record InfoRequestMessage(BlockPos position) implements CustomPacketPayload {
    public static final Type<InfoRequestMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "info_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfoRequestMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, InfoRequestMessage::position,
            InfoRequestMessage::new);

    // --------------------------------------------------------------------- //

    public static void handle(final InfoRequestMessage message, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof final ServerPlayer player)) {
                return;
            }

            MessageUtils.withBlockEntity(player.level(), message.position(), BlockEntityWithInfo.class, blockEntity ->
                    Network.sendToPlayer(player, new InfoResponseMessage(
                            blockEntity.getBlockPos(),
                            Optional.ofNullable(blockEntity.getLookAtInfo()))));
        });
    }

    // --------------------------------------------------------------------- //

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
