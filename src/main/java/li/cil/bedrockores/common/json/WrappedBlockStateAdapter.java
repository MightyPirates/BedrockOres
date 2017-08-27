package li.cil.bedrockores.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.bedrockores.common.config.WrappedBlockState;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Map;

public class WrappedBlockStateAdapter implements JsonSerializer<WrappedBlockState>, JsonDeserializer<WrappedBlockState> {
    private static final String KEY_NAME = "name";
    private static final String KEY_PROPERTIES = "properties";

    // --------------------------------------------------------------------- //
    // JsonSerializer

    @Override
    public JsonElement serialize(final WrappedBlockState src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();

        result.add(KEY_NAME, context.serialize(src.getName()));
        final Map<String, String> properties = src.getProperties();
        if (properties != null && !properties.isEmpty()) {
            result.add(KEY_PROPERTIES, context.serialize(properties, Types.MAP_STRING_STRING));
        }

        return result;
    }

    // --------------------------------------------------------------------- //
    // JsonDeserializer

    @Override
    public WrappedBlockState deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final ResourceLocation name = context.deserialize(jsonObject.get(KEY_NAME), ResourceLocation.class);

        final Map<String, String> properties;
        if (jsonObject.has(KEY_PROPERTIES)) {
            properties = context.deserialize(jsonObject.get(KEY_PROPERTIES), Types.MAP_STRING_STRING);
        } else {
            properties = null;
        }

        return new WrappedBlockState(name, properties);
    }
}
