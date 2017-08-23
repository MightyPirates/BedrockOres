package li.cil.bedrockores.common.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import joptsimple.internal.Strings;
import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.json.OreConfigAdapter;
import li.cil.bedrockores.common.json.ResourceLocationAdapter;
import li.cil.bedrockores.common.json.Types;
import li.cil.bedrockores.common.json.WrappedBlockStateAdapter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class VeinConfig {
    private static ArrayList<OreConfig> allOres = new ArrayList<>();
    private static ArrayList<OreConfig> overworldOres = new ArrayList<>();
    private static ArrayList<OreConfig> netherOres = new ArrayList<>();
    private static int overworldOreWeightSum;
    private static int netherOreWeightSum;

    public static List<OreConfig> getOres() {
        return ImmutableList.copyOf(allOres);
    }

    @Nullable
    public static OreConfig getOre(final DimensionType dimensionType, final float r) {
        final ArrayList<OreConfig> list;
        final int oreWeightSum;
        switch (dimensionType) {
            case OVERWORLD:
                list = overworldOres;
                oreWeightSum = overworldOreWeightSum;
                break;
            case NETHER:
                list = netherOres;
                oreWeightSum = netherOreWeightSum;
                break;
            default:
                return null;
        }

        if (list.isEmpty() || oreWeightSum == 0) {
            return null;
        }

        final int wantWeightSum = (int) (r * oreWeightSum);
        int weightSum = 0;
        for (final OreConfig ore : list) {
            weightSum += ore.weight;
            if (weightSum > wantWeightSum) {
                return ore;
            }
        }

        return null;
    }

    public static void load() {
        final String configDirectory = Loader.instance().getConfigDir().getPath();

        final Gson gson = new GsonBuilder().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(IBlockState.class, new WrappedBlockStateAdapter()).
                registerTypeAdapter(OreConfig.class, new OreConfigAdapter()).
                setPrettyPrinting().
                disableHtmlEscaping().
                create();

        loadDefaultOres(gson);
        loadOres(configDirectory, gson);

        BedrockOres.getLog().info("Done loading ore config, got {} ores. Filtering...", allOres.size());

        // Remove entries where block state could not be loaded ore have no weight.
        allOres.removeIf(ore -> !ore.enabled || ore.weight < 1 || ore.state.getBlockState().getBlock() == Blocks.AIR);

        BedrockOres.getLog().info("After removing disabled and unavailable ores, got {} ores.", allOres.size());

        // Remove grouped entries where a group entry with a lower order exists.
        for (int i = allOres.size() - 1; i >= 0; i--) {
            final OreConfig ore = allOres.get(i);
            if (Strings.isNullOrEmpty(ore.group)) {
                continue;
            }
            for (int j = 0; j < i; j++) {
                final OreConfig otherOre = allOres.get(j);

                if (!Objects.equals(ore.group, otherOre.group)) {
                    continue;
                }

                if (otherOre.groupOrder <= ore.groupOrder) {
                    allOres.remove(i);
                    break;
                }
            }
        }

        BedrockOres.getLog().info("After removing duplicate ores, got {} ores.", allOres.size());

        // Order by weight
        allOres.sort(Comparator.comparingInt(a -> a.weight));

        // Build overworld list.
        overworldOres.addAll(allOres);
        overworldOres.removeIf(ore -> !Strings.isNullOrEmpty(ore.dimension) &&
                                      !Objects.equals(ore.dimension, "overworld") &&
                                      !Objects.equals(ore.dimension, "*"));

        overworldOreWeightSum = overworldOres.stream().
                map(ore -> ore.weight).
                reduce((a, b) -> a + b).
                orElse(0);

        // Build nether type list.
        netherOres.addAll(allOres);
        netherOres.removeIf(ore -> !Strings.isNullOrEmpty(ore.dimension) &&
                                   !Objects.equals(ore.dimension, "nether") &&
                                   !Objects.equals(ore.dimension, "*"));

        netherOreWeightSum = netherOres.stream().
                map(ore -> ore.weight).
                reduce((a, b) -> a + b).
                orElse(0);
    }

    // --------------------------------------------------------------------- //

    private static void loadDefaultOres(final Gson gson) {
        try {
            final ArrayList<OreConfig> result = loadDefault(Constants.BEDROCK_VEINS_FILENAME, Types.LIST_ORE, gson);
            allOres.clear();
            allOres.addAll(result);
        } catch (final IOException | JsonSyntaxException e) {
            BedrockOres.getLog().warn("Failed reading " + Constants.BEDROCK_VEINS_FILENAME + ".", e);
        }
    }

    private static void loadOres(final String basePath, final Gson gson) {
        final ArrayList<OreConfig> result = load(allOres, Constants.BEDROCK_VEINS_FILENAME, Types.LIST_ORE, basePath, gson);
        if (result != allOres) {
            allOres.clear();
            allOres.addAll(result);
        }
    }

    private static <T> T load(T value, final String fileName, final Type type, final String basePath, final Gson gson) {
        final File path = Paths.get(basePath, Constants.MOD_ID, fileName).toFile();
        try {
            if (path.exists()) {
                value = load(path, type, gson);
            } else {
                value = loadDefault(fileName, type, gson);
            }
            save(value, path, gson);
        } catch (final IOException | JsonSyntaxException e) {
            BedrockOres.getLog().warn("Failed reading " + fileName + ".", e);
        }
        return value;
    }

    private static <T> T load(final File path, final Type type, final Gson gson) throws IOException, JsonSyntaxException {
        try (final InputStream stream = new FileInputStream(path)) {
            return gson.fromJson(new InputStreamReader(stream), type);
        }
    }

    private static <T> T loadDefault(final String fileName, final Type type, final Gson gson) throws IOException, JsonSyntaxException {
        try (final InputStream stream = Settings.class.getResourceAsStream("/assets/" + Constants.MOD_ID + "/config/" + fileName)) {
            return gson.fromJson(new InputStreamReader(stream), type);
        }
    }

    private static void save(final Object value, final File path, final Gson gson) {
        try {
            FileUtils.writeStringToFile(path, gson.toJson(value), Charset.defaultCharset());
        } catch (final IOException e) {
            BedrockOres.getLog().warn("Failed writing " + path.toString() + ".", e);
        }
    }

    // --------------------------------------------------------------------- //

    private VeinConfig() {
    }
}
