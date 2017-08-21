package li.cil.bedrockores.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.bedrockores.common.BedrockOres;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class BlockStateAdapter implements JsonSerializer<IBlockState>, JsonDeserializer<IBlockState> {
    private static final String KEY_NAME = "name";
    private static final String KEY_PROPERTIES = "properties";

    // --------------------------------------------------------------------- //
    // JsonSerializer

    @SuppressWarnings("unchecked")
    @Override
    public JsonElement serialize(final IBlockState src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();

        final Block block = src.getBlock();
        final ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(block);
        if (registryName == null) {
            return JsonNull.INSTANCE;
        }

        result.add(KEY_NAME, context.serialize(registryName));

        final JsonObject properties = new JsonObject();
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : src.getProperties().entrySet()) {
            final IProperty property = entry.getKey();
            final Comparable<?> value = entry.getValue();
            properties.addProperty(property.getName(), property.getName(value));
        }
        if (!properties.entrySet().isEmpty()) {
            result.add(KEY_PROPERTIES, properties);
        }

        return result;
    }

    // --------------------------------------------------------------------- //
    // JsonDeserializer

    @SuppressWarnings("unchecked")
    @Override
    public IBlockState deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final ResourceLocation name = context.deserialize(jsonObject.get(KEY_NAME), ResourceLocation.class);
        final Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null) {
            return Blocks.AIR.getDefaultState();
        }

        IBlockState state = block.getDefaultState();
        final JsonObject properties = jsonObject.getAsJsonObject(KEY_PROPERTIES);
        if (properties != null) {
            final Collection<IProperty<?>> blockProperties = state.getPropertyKeys();
            outer:
            for (final Map.Entry<String, JsonElement> entry : properties.entrySet()) {
                final String value = entry.getValue().getAsString();
                for (final IProperty property : blockProperties) {
                    if (Objects.equals(property.getName(), entry.getKey())) {
                        final Comparable originalValue = state.getValue(property);
                        do {
                            if (Objects.equals(property.getName(originalValue), value)) {
                                continue outer;
                            }
                            state = state.cycleProperty(property);
                        }
                        while (!Objects.equals(state.getValue(property), originalValue));
                        BedrockOres.getLog().warn("Cannot parse property value '{}' for property '{}' of block {}.", value, entry.getKey(), name);
                        continue outer;
                    }
                }
                BedrockOres.getLog().warn("Block {} has no property '{}'.", name, entry.getKey());
            }
        }
        return state;
    }
}
