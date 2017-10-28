package li.cil.bedrockores.common.config.ore;

import net.minecraft.util.WeightedRandom;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class OreFilterEntry {
    private final List<OreConfig> ores;
    private final int totalWeight;

    public OreFilterEntry(final OreFilterKey key, final List<OreConfig> ores) {
        this.ores = ores.stream().filter(ore -> ore.matches(key)).collect(Collectors.toList());
        totalWeight = WeightedRandom.getTotalWeight(this.ores);
    }

    @Nullable
    public OreConfig getOre(final Random random) {
        if (ores.size() == 0 || totalWeight <= 0) {
            return null;
        }
        return WeightedRandom.getRandomItem(random, ores, totalWeight);
    }

    public List<OreConfig> getOres() {
        return ores;
    }
}
