package li.cil.bedrockores.common.config;

import li.cil.bedrockores.common.BedrockOres;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WrappedBlockState {
    public static final List<WrappedBlockState> ERRORED = new ArrayList<>();

    // --------------------------------------------------------------------- //

    @Nullable
    private final ResourceLocation name;
    @Nullable
    private final Map<String, String> properties;

    @Nullable
    private IBlockState resolved;

    // --------------------------------------------------------------------- //

    public WrappedBlockState(@Nullable final ResourceLocation name, @Nullable final Map<String, String> properties) {
        this.name = name;
        this.properties = properties;
    }

    @Nullable
    public ResourceLocation getName() {
        return name;
    }

    @Nullable
    public Map<String, String> getProperties() {
        return properties;
    }

    public IBlockState getBlockState() {
        if (resolved == null) {
            resolved = resolveBlockState();
        }

        return resolved;
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WrappedBlockState that = (WrappedBlockState) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder(32);
        s.append(name);
        if (properties != null && !properties.isEmpty()) {
            s.append('[');
            boolean isFirst = true;
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    s.append(',');
                }
                s.append(entry.getKey());
                s.append('=');
                s.append(entry.getValue());
            }
            s.append(']');
        }
        return s.toString();
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("unchecked")
    private IBlockState resolveBlockState() {
        final Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null || block == Blocks.AIR) {
            return Blocks.AIR.getDefaultState();
        }

        IBlockState state = block.getDefaultState();
        if (properties != null) {
            final Collection<IProperty<?>> blockProperties = state.getPropertyKeys();
            outer:
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                final String serializedKey = entry.getKey();
                final String serializedValue = entry.getValue();

                for (final IProperty property : blockProperties) {
                    if (Objects.equals(property.getName(), serializedKey)) {
                        final Comparable originalValue = state.getValue(property);

                        do {
                            if (Objects.equals(property.getName(state.getValue(property)), serializedValue)) {
                                continue outer;
                            }
                            state = state.cycleProperty(property);
                        }
                        while (!Objects.equals(state.getValue(property), originalValue));

                        BedrockOres.getLog().warn("Cannot parse property value '{}' for property '{}' of block {}.", serializedValue, serializedKey, name);
                        ERRORED.add(this);
                        return Blocks.AIR.getDefaultState();
                    }
                }

                BedrockOres.getLog().warn("Block {} has no property '{}'.", name, serializedKey);
                ERRORED.add(this);
                return Blocks.AIR.getDefaultState();
            }
        }
        return state;
    }
}
