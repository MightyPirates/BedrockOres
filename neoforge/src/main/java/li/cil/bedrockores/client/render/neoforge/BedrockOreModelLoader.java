package li.cil.bedrockores.client.render.neoforge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public final class BedrockOreModelLoader implements IGeometryLoader<BedrockOreModel> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bedrock_ore");

    @Override
    public BedrockOreModel read(final JsonObject modelContents, final JsonDeserializationContext context) throws JsonParseException {
        return new BedrockOreModel();
    }
}
