package li.cil.bedrockores.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import li.cil.bedrockores.common.config.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public final class BedrockOreModelLoader implements IGeometryLoader<BedrockOreModel> {
    @SubscribeEvent
    public static void handleModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        event.register("bedrock_ore", new BedrockOreModelLoader());
    }

    @Override
    public BedrockOreModel read(final JsonObject modelContents, final JsonDeserializationContext context) throws JsonParseException {
        return new BedrockOreModel();
    }
}
