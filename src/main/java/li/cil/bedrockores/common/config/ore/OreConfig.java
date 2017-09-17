package li.cil.bedrockores.common.config.ore;

import joptsimple.internal.Strings;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.BiomeManager;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class OreConfig extends WeightedRandom.Item {
    static final String ANY_VALUE = "*";

    // --------------------------------------------------------------------- //

    public String[] comment;
    public boolean enabled = true;

    public WrappedBlockState state;
    public float extractionCooldownScale = 1f;

    public FilterType dimensionFilter = FilterType.Whitelist;
    public SelectorType dimensionSelector = SelectorType.Type;
    public final Set<String> dimension = new LinkedHashSet<>();

    public FilterType biomeFilter = FilterType.Whitelist;
    public SelectorType biomeSelector = SelectorType.Id;
    public final Set<String> biome = new LinkedHashSet<>();

    public int widthMin = 2;
    public int widthMax = 4;

    public int heightMin = 2;
    public int heightMax = 4;

    public int countMin = 5;
    public int countMax = 10;

    public int yieldMin = 100;
    public int yieldMax = 125;

    public String group = "";
    public int groupOrder = 0;

    private OreConfigFilter filter;

    // --------------------------------------------------------------------- //

    public OreConfig() {
        super(10);
        dimension.addAll(Arrays.asList(Settings.defaultDimensionTypes));
        biome.add(ANY_VALUE);
    }

    public void buildFilter() {
        if (dimension.contains(ANY_VALUE)) {
            dimension.clear();
            dimensionFilter = dimensionFilter.getOpposite();
        } else {
            dimension.removeIf(Strings::isNullOrEmpty);
        }

        if (biome.contains(ANY_VALUE)) {
            biome.clear();
            biomeFilter = biomeFilter.getOpposite();
        } else {
            biome.removeIf(Strings::isNullOrEmpty);
        }

        filter = new OreConfigFilter(this);
    }

    public boolean matches(final int dimension, final DimensionType dimensionType, final ResourceLocation biome, final EnumSet<BiomeManager.BiomeType> biomeTypes) {
        return dimensionFilter.filter(dimensionSelector.select(filter, dimension, dimensionType)) &&
               biomeFilter.filter(biomeSelector.select(filter, biome, biomeTypes));
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return state.toString();
    }
}
