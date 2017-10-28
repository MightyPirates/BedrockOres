package li.cil.bedrockores.common.config.ore;

import li.cil.bedrockores.util.BiomeUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;

import java.util.Arrays;
import java.util.EnumSet;

public final class OreFilterKey {
    final int dimension;
    final DimensionType dimensionType;
    final ResourceLocation biome;
    final EnumSet<BiomeManager.BiomeType> biomeTypes;
    final BiomeDictionary.Type[] biomeDictTypes;

    public OreFilterKey(final World world, final ChunkPos pos) {
        final Biome biome = world.getBiome(pos.getBlock(8, 0, 8));
        this.dimension = world.provider.getDimension();
        this.dimensionType = world.provider.getDimensionType();
        this.biome = biome.getRegistryName();
        this.biomeTypes = BiomeUtils.getBiomeTypes(biome);
        this.biomeDictTypes = BiomeDictionary.getTypesForBiome(biome);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OreFilterKey that = (OreFilterKey) o;

        if (dimension != that.dimension) {
            return false;
        }
        if (dimensionType != that.dimensionType) {
            return false;
        }
        if (!biome.equals(that.biome)) {
            return false;
        }
        if (!biomeTypes.equals(that.biomeTypes)) {
            return false;
        }
        if (!Arrays.equals(biomeDictTypes, that.biomeDictTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = dimension;
        hashCode = 31 * hashCode + dimensionType.hashCode();
        hashCode = 31 * hashCode + biome.hashCode();
        hashCode = 31 * hashCode + biomeTypes.hashCode();
        hashCode = 31 * hashCode + Arrays.hashCode(biomeDictTypes);
        return hashCode;
    }
}
