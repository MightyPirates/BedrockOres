package li.cil.bedrockores.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.bedrockores.common.config.ore.FilterType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FilterTypeAdapter implements JsonSerializer<FilterType>, JsonDeserializer<FilterType> {
    @Override
    public JsonElement serialize(final FilterType src, final Type typeOfSrc, final JsonSerializationContext context) {
        return context.serialize(src.name().toLowerCase(Locale.US));
    }

    @Override
    public FilterType deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final String name = json.getAsString();
        for (final FilterType type : FilterType.values()) {
            if (Objects.equals(type.name().toLowerCase(Locale.US), name)) {
                return type;
            }
        }

        throw new JsonParseException("Invalid value for FilterType: got '" + name + "', must be one of {" + String.join(", ", Arrays.stream(FilterType.values()).map(e -> e.name().toLowerCase(Locale.US)).collect(Collectors.toList())) + "}.");
    }
}
