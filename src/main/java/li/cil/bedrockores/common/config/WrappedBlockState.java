package li.cil.bedrockores.common.config;

import li.cil.bedrockores.common.BedrockOres;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WrappedBlockState {
    public static final List<WrappedBlockState> ERRORED = new ArrayList<>();

    public ResourceLocation name;
    public Map<String, String> properties;

    private IBlockState resolved;

    public IBlockState getBlockState() {
        if (resolved == null) {
            resolved = resolveBlockState();
        }

        return resolved;
    }

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

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder(32);
        s.append(name);
        if (!properties.isEmpty()) {
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
}
