package li.cil.bedrockores.common.config.ore;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import joptsimple.internal.Strings;
import li.cil.bedrockores.common.BedrockOres;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.BiomeManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class OreConfigFilter {
    EnumSet<DimensionType> dimensionTypes;
    TIntSet dimensionIds;
    EnumSet<BiomeManager.BiomeType> biomeTypes;
    Set<ResourceLocation> biomeIds;

    OreConfigFilter(final OreConfig config) {
        switch (config.dimensionSelector) {
            case Type:
                if (dimensionTypes == null) {
                    dimensionTypes = buildEnumSet(DimensionType.class, config.dimension);
                }
                break;
            case Id:
                if (dimensionIds == null) {
                    dimensionIds = buildIntSet(config.dimension);
                }
                break;
        }

        switch (config.biomeSelector) {
            case Type:
                if (biomeTypes == null) {
                    biomeTypes = buildEnumSet(BiomeManager.BiomeType.class, config.biome);
                }
                break;
            case Id:
                if (biomeIds == null) {
                    biomeIds = buildResourceLocationSet(config.biome);
                }
                break;
        }
    }

    private static <E extends Enum<E>> EnumSet<E> buildEnumSet(final Class<E> clazz, final Collection<String> values) {
        final EnumSet<E> result = EnumSet.noneOf(clazz);

        final Map<String, E> dimensionTypeLookup = new HashMap<>();
        for (final E dimensionType : clazz.getEnumConstants()) {
            dimensionTypeLookup.put(dimensionType.name().toLowerCase(Locale.US), dimensionType);
        }

        for (final String value : values) {
            assert !Strings.isNullOrEmpty(value);

            final String dimensionTypeName = value.toLowerCase(Locale.US);
            if (dimensionTypeLookup.containsKey(dimensionTypeName)) {
                final E dimensionType = dimensionTypeLookup.get(dimensionTypeName);
                result.add(dimensionType);
            } else {
                BedrockOres.getLog().warn("Unknown dimension type '{}', ignoring.", value);
            }
        }

        return result;
    }

    private static TIntSet buildIntSet(final Set<String> values) {
        final TIntSet result = new TIntHashSet();

        for (final String value : values) {
            assert !Strings.isNullOrEmpty(value);

            try {
                final int id = Integer.parseInt(value, 10);
                result.add(id);
            } catch (final NumberFormatException e) {
                BedrockOres.getLog().warn("Failed parsing dimension id '{}', ignoring.", value);
            }
        }

        return result;
    }

    private static Set<ResourceLocation> buildResourceLocationSet(final Set<String> values) {
        final Set<ResourceLocation> result = new HashSet<>();

        for (final String value : values) {
            assert !Strings.isNullOrEmpty(value);

            final ResourceLocation location = new ResourceLocation(value);
            result.add(location);
        }

        return result;
    }
}
