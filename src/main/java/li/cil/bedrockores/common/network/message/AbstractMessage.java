package li.cil.bedrockores.common.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class AbstractMessage {
    protected AbstractMessage() {
    }

    protected AbstractMessage(final FriendlyByteBuf buffer) {
        fromBytes(buffer);
    }

    // --------------------------------------------------------------------- //

    public static boolean handleMessage(final AbstractMessage message, final Supplier<NetworkEvent.Context> contextSupplied) {
        message.handleMessage(contextSupplied);
        return true;
    }

    public abstract void fromBytes(final FriendlyByteBuf buffer);

    public abstract void toBytes(final FriendlyByteBuf buffer);

    // --------------------------------------------------------------------- //

    protected void handleMessage(final Supplier<NetworkEvent.Context> contextSupplier) {
        final NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleMessage(context));
    }

    protected void handleMessage(final NetworkEvent.Context context) {
        throw new NotImplementedException("Message implements neither asynchronous nor synchronous handleMessage() method.");
    }

    @Nullable
    protected static Level getLevel(final NetworkEvent.Context context) {
        if (context.getDirection().getReceptionSide().isClient()) {
            return getClientLevel();
        } else {
            return getServerLevel(context);
        }
    }

    @Nullable
    private static Level getServerLevel(final NetworkEvent.Context context) {
        final var sender = context.getSender();
        return sender != null ? sender.getLevel() : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
