package li.cil.bedrockores.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.bedrockores.common.config.OreConfig;
import li.cil.bedrockores.common.config.OreConfigManager;
import li.cil.bedrockores.common.config.WrappedBlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;

// Reflection based adapter fails when trying to look up an adapter for runtime
// type of state, so we do it manually... also allows some other custom logic,
// so that's nice.
public class OreConfigAdapter implements JsonSerializer<OreConfig>, JsonDeserializer<OreConfig> {
    // --------------------------------------------------------------------- //
    // JsonSerializer

    @Override
    public JsonElement serialize(final OreConfig src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        for (final Field field : OreConfig.class.getFields()) {
            try {
                if (Objects.equals("enabled", field.getName()) && Objects.equals(true, field.get(src))) {
                    continue;
                }
                if (Objects.equals("dimension", field.getName()) && (Objects.equals(null, field.get(src)) || Objects.equals("", field.get(src)))) {
                    continue;
                }
                if (Objects.equals("groupOrder", field.getName()) && Objects.equals(0, field.get(src))) {
                    continue;
                }
                jsonObject.add(field.getName(), context.serialize(field.get(src), field.getType()));
            } catch (final IllegalAccessException ignored) {
            }
        }
        return jsonObject;
    }

    // --------------------------------------------------------------------- //
    // JsonDeserializer

    @Override
    public OreConfig deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        OreConfig dst = new OreConfig();
        final JsonObject jsonObject = json.getAsJsonObject();

        if (OreConfigManager.INSTANCE.shouldReuseOreConfigs()) {
            // Stuff with no block state def gets stripped out anyway.
            if (!jsonObject.has("state")) {
                return dst;
            }

            // See if we have an entry for this exact block state already, if so we
            // want to patch it. Yes, this is pretty evil, but whatever works.
            final WrappedBlockState state = context.deserialize(jsonObject.get("state"), WrappedBlockState.class);
            for (final OreConfig oreConfig : OreConfigManager.INSTANCE.getOres()) {
                if (oreConfig.state.equals(state)) {
                    dst = oreConfig;
                    break;
                }
            }
        }

        for (final Field field : OreConfig.class.getFields()) {
            final JsonElement jsonElement = jsonObject.get(field.getName());
            if (jsonElement == null) {
                continue;
            }
            try {
                field.set(dst, context.deserialize(jsonElement, field.getType()));
            } catch (final IllegalAccessException e) {
                assert false : "OreConfig contains non-accessible field: " + field.getName();
            }
        }

        return dst;
    }
}
