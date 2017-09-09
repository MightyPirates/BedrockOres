package li.cil.bedrockores.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BiomeUtils {
    private static final ThreadLocal<Map<Biome, EnumSet<BiomeManager.BiomeType>>> CACHED_TYPES = ThreadLocal.withInitial(HashMap::new);

    // --------------------------------------------------------------------- //

    public static EnumSet<BiomeManager.BiomeType> getBiomeTypes(final Biome biome) {
        return CACHED_TYPES.get().computeIfAbsent(biome, BiomeUtils::computeBiomeTypes);
    }

    // --------------------------------------------------------------------- //

    private static EnumSet<BiomeManager.BiomeType> computeBiomeTypes(final Biome biome) {
        final EnumSet<BiomeManager.BiomeType> biomeTypes = EnumSet.noneOf(BiomeManager.BiomeType.class);

        for (final BiomeManager.BiomeType biomeType : BiomeManager.BiomeType.values()) {
            final ImmutableList<BiomeManager.BiomeEntry> biomeEntries = BiomeManager.getBiomes(biomeType);
            if (biomeEntries == null) {
                continue;
            }

            for (final BiomeManager.BiomeEntry biomeEntry : biomeEntries) {
                if (Objects.equals(biomeEntry.biome, biome)) {
                    biomeTypes.add(biomeType);
                    break;
                }
            }
        }

        return biomeTypes;
    }

    private BiomeUtils() {
    }
}
