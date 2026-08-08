package li.cil.bedrockores.common.network.message;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record InfoResponseMessage(BlockPos position, Optional<Component> info) implements CustomPacketPayload {
    public static final Type<InfoResponseMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "info_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfoResponseMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, InfoResponseMessage::position,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), InfoResponseMessage::info,
            InfoResponseMessage::new);

    // --------------------------------------------------------------------- //

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
