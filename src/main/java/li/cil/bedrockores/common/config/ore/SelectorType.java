package li.cil.bedrockores.common.config.ore;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public enum SelectorType {
    Type((config, dimension, dimensionType) -> config.dimensionTypes.contains(dimensionType),
         ((config, biome, biomeTypes, biomeDictTypes) -> config.biomeTypes.stream().anyMatch(biomeTypes::contains))),
    Id(((config, dimension, dimensionType) -> config.dimensionIds.contains(dimension)),
       ((config, biome, biomeTypes, biomeDictTypes) -> biome == null ? config.biomeIds.isEmpty() : config.biomeIds.contains(biome))),
    Dictionary((config, dimension, dimensionType) -> false, // No dictionary support for world.
               ((config, biome, biomeTypes, biomeDictTypes) -> config.biomeDictTypes.stream().anyMatch(biomeDictTypes::contains)));

    // --------------------------------------------------------------------- //

    private final WorldPredicate worldPredicate;
    private final BiomePredicate biomePredicate;

    // --------------------------------------------------------------------- //

    SelectorType(final WorldPredicate worldPredicate,
                 final BiomePredicate biomePredicate) {
        this.worldPredicate = worldPredicate;
        this.biomePredicate = biomePredicate;
    }

    boolean select(final OreConfigFilter config, final int dimension, final DimensionType dimensionType) {
        return worldPredicate.test(config, dimension, dimensionType);
    }

    boolean select(final OreConfigFilter config, @Nullable final ResourceLocation biome, final EnumSet<BiomeManager.BiomeType> biomeTypes, final Set<BiomeDictionary.Type> biomeDictTypes) {
        return biomePredicate.test(config, biome, biomeTypes, biomeDictTypes);
    }

    // --------------------------------------------------------------------- //

    @FunctionalInterface
    private interface WorldPredicate {
        boolean test(final OreConfigFilter config, final int dimension, final DimensionType dimensionType);
    }

    @FunctionalInterface
    private interface BiomePredicate {
        boolean test(final OreConfigFilter config, @Nullable final ResourceLocation biome, final EnumSet<BiomeManager.BiomeType> biomeTypes, final Set<BiomeDictionary.Type> biomeDictTypes);
    }
}
