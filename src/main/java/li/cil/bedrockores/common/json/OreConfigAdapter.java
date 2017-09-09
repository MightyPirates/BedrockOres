package li.cil.bedrockores.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.config.OreConfigManager;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.config.ore.OreConfig;
import li.cil.bedrockores.common.config.ore.WrappedBlockState;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

// Reflection based adapter fails when trying to look up an adapter for runtime
// type of state, so we do it manually... also allows some other custom logic,
// so that's nice.
public final class OreConfigAdapter implements JsonSerializer<OreConfig>, JsonDeserializer<OreConfig> {
    // --------------------------------------------------------------------- //
    // JsonSerializer

    @Override
    public JsonElement serialize(final OreConfig src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        for (final Field field : OreConfig.class.getFields()) {
            try {
                final String name = field.getName();

                if (Objects.equals("enabled", name) && src.enabled) {
                    continue;
                }

                if (Objects.equals("dimension", name)) {
                    if (src.dimension.isEmpty()) {
                        continue;
                    }
                    if (src.dimension.size() == 1) {
                        final String dimension = src.dimension.stream().findFirst().orElseThrow(AssertionError::new);
                        jsonObject.addProperty(name, dimension);
                        continue;
                    }
                }

                if (Objects.equals("biome", name)) {
                    if (src.biome.isEmpty()) {
                        continue;
                    }
                    if (src.biome.size() == 1) {
                        final String dimension = src.biome.stream().findFirst().orElseThrow(AssertionError::new);
                        jsonObject.addProperty(name, dimension);
                        continue;
                    }
                }

                if (Objects.equals("groupOrder", name) && src.groupOrder == 0) {
                    continue;
                }

                if (Objects.equals("itemWeight", name)) {
                    jsonObject.addProperty("name", src.itemWeight);
                    continue;
                }

                jsonObject.add(name, context.serialize(field.get(src), field.getType()));
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
            final String name = field.getName();
            final JsonElement jsonElement = jsonObject.get(name);
            if (jsonElement == null) {
                continue;
            }

            if (Objects.equals("dimension", name)) {
                dst.dimension.clear();
                if (jsonElement.isJsonPrimitive()) {
                    final String dimension = jsonElement.getAsString();
                    dst.dimension.add(dimension);
                } else {
                    dst.dimension.addAll(context.<List<String>>deserialize(jsonElement, Types.LIST_STRING));
                }
                continue;
            }

            if (Objects.equals("biome", name)) {
                dst.biome.clear();
                if (jsonElement.isJsonPrimitive()) {
                    final String dimension = jsonElement.getAsString();
                    dst.biome.add(dimension);
                } else {
                    dst.biome.addAll(context.<List<String>>deserialize(jsonElement, Types.LIST_STRING));
                }
                continue;
            }

            if (Objects.equals("groupOrder", name)) {
                try {
                    dst.groupOrder = jsonElement.getAsInt();
                } catch (final NumberFormatException e) {
                    final int groupOrder = ArrayUtils.indexOf(Settings.orePriority, jsonElement.getAsString());
                    if (groupOrder >= 0) {
                        dst.groupOrder = groupOrder * 5;
                    } else {
                        BedrockOres.getLog().warn("Failed looking up group order for '{}', ignoring.", jsonElement.getAsString());
                    }
                }
                continue;
            }

            if (Objects.equals("weight", name)) {
                dst.itemWeight = jsonElement.getAsInt();
                continue;
            }

            try {
                field.set(dst, context.deserialize(jsonElement, field.getType()));
            } catch (final IllegalAccessException e) {
                assert false : "OreConfig contains non-accessible field: " + name;
            }
        }

        return dst;
    }
}
