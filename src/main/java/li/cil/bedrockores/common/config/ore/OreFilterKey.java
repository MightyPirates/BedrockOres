package li.cil.bedrockores.common.config.ore;

import li.cil.bedrockores.util.BiomeUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class OreFilterKey {
    final int dimension;
    final DimensionType dimensionType;
    @Nullable
    final ResourceLocation biome;
    final EnumSet<BiomeManager.BiomeType> biomeTypes;
    final Set<BiomeDictionary.Type> biomeDictTypes;

    public OreFilterKey(final World world, final ChunkPos pos) {
        final Biome biome = world.getBiome(pos.getBlock(8, 0, 8));
        this.dimension = world.provider.getDimension();
        this.dimensionType = world.provider.getDimensionType();
        this.biome = biome.getRegistryName();
        this.biomeTypes = BiomeUtils.getBiomeTypes(biome);
        this.biomeDictTypes = BiomeDictionary.getTypes(biome);
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
        if (!Objects.equals(biome, that.biome)) {
            return false;
        }
        if (!biomeTypes.equals(that.biomeTypes)) {
            return false;
        }
        if (!biomeDictTypes.equals(that.biomeDictTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = dimension;
        hashCode = 31 * hashCode + dimensionType.hashCode();
        hashCode = 31 * hashCode + (biome != null ? biome.hashCode() : 0);
        hashCode = 31 * hashCode + biomeTypes.hashCode();
        hashCode = 31 * hashCode + biomeDictTypes.hashCode();
        return hashCode;
    }
}
