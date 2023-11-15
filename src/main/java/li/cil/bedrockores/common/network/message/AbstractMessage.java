package li.cil.bedrockores.common.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;

public abstract class AbstractMessage {
    protected AbstractMessage() {
    }

    protected AbstractMessage(final FriendlyByteBuf buffer) {
        fromBytes(buffer);
    }

    // --------------------------------------------------------------------- //

    public static boolean handleMessageAsync(final AbstractMessage message, final CustomPayloadEvent.Context context) {
        message.handleMessageAsync(context);
        return true;
    }

    public abstract void fromBytes(final FriendlyByteBuf buffer);

    public abstract void toBytes(final FriendlyByteBuf buffer);

    // --------------------------------------------------------------------- //

    protected void handleMessageAsync(final CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> handleMessage(context));
    }

    protected void handleMessage(final CustomPayloadEvent.Context context) {
        throw new NotImplementedException("Message implements neither asynchronous nor synchronous handleMessage() method.");
    }

    @Nullable
    protected static Level getLevel(final CustomPayloadEvent.Context context) {
        if (context.getDirection().getReceptionSide().isClient()) {
            return getClientLevel();
        } else {
            return getServerLevel(context);
        }
    }

    @Nullable
    private static Level getServerLevel(final CustomPayloadEvent.Context context) {
        final var sender = context.getSender();
        return sender != null ? sender.level() : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
