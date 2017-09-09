package li.cil.bedrockores.common.config.ore;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.BiomeManager;

import java.util.EnumSet;

public enum SelectorType {
    Type((config, dimension, dimensionType) -> config.dimensionTypes.contains(dimensionType),
         ((config, biome, biomeTypes) -> biomeTypes.stream().anyMatch(config.biomeTypes::contains))),
    Id(((config, dimension, dimensionType) -> config.dimensionIds.contains(dimension)),
       ((config, biome, biomeTypes) -> config.biomeIds.contains(biome)));

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

    boolean select(final OreConfigFilter config, final ResourceLocation biome, final EnumSet<BiomeManager.BiomeType> biomeTypes) {
        return biomePredicate.test(config, biome, biomeTypes);
    }

    // --------------------------------------------------------------------- //

    @FunctionalInterface
    private interface WorldPredicate {
        boolean test(final OreConfigFilter config, final int dimension, final DimensionType dimensionType);
    }

    @FunctionalInterface
    private interface BiomePredicate {
        boolean test(final OreConfigFilter config, final ResourceLocation biome, final EnumSet<BiomeManager.BiomeType> biomeTypes);
    }
}
