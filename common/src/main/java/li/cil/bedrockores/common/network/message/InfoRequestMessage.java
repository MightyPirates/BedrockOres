package li.cil.bedrockores.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public record InfoRequestMessage(BlockPos position) implements CustomPacketPayload {
    public static final Type<InfoRequestMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "info_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfoRequestMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, InfoRequestMessage::position,
            InfoRequestMessage::new);

    private static final double REACH_PADDING = 3;
    private static final long MIN_REQUEST_INTERVAL_MS = 200;
    private static final Map<ServerPlayer, Long> LAST_REQUEST = new WeakHashMap<>();

    // --------------------------------------------------------------------- //

    public static void handle(final InfoRequestMessage message, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof final ServerPlayer player)) {
                return;
            }

            if (!player.isWithinBlockInteractionRange(message.position(), REACH_PADDING)) {
                return;
            }

            final var now = System.currentTimeMillis();
            final var last = LAST_REQUEST.get(player);
            if (last != null && now - last < MIN_REQUEST_INTERVAL_MS) {
                return;
            }
            LAST_REQUEST.put(player, now);

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
