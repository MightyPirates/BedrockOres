package li.cil.bedrockores.common.config;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.config.Config;

import java.util.Locale;

@Config(modid = Constants.MOD_ID)
public final class Settings {
    @Config.LangKey(Constants.CONFIG_MINER_EXTRACTION_COOLDOWN)
    @Config.Comment("The time in ticks between extraction operations performed by the miner.")
    @Config.RangeInt(min = 0)
    public static int minerExtractionCooldown = 100;

    @Config.LangKey(Constants.CONFIG_MINER_EFFICIENCY)
    @Config.Comment("The fuel efficiency of the miner. Actual burn time is original burn time times this. " +
                    "Applies to both internal power generation as well as power supplied externally.")
    @Config.RangeDouble(min = 0)
    public static float minerEfficiency = 0.5f;

    @Config.LangKey(Constants.CONFIG_VEIN_BASE_Y)
    @Config.Comment("The y level at which to start looking for bedrock to generate veins in, searching down.")
    @Config.RangeInt(min = 0, max = 255)
    public static int veinBaseY = 5;

    @Config.LangKey(Constants.CONFIG_VEIN_CHANCE)
    @Config.Comment("The probability that a bedrock vein spawns in a chunk. In other words, the relative " +
                    "amount of chunks a vein spawns in.")
    @Config.RangeDouble(min = 0, max = 1)
    public static float veinChance = 0.5f;

    @Config.LangKey(Constants.CONFIG_VEIN_YIELD_BASE_SCALE)
    @Config.Comment("A constant scaling factor applied to all yields. Intended to allow easily changing " +
                    "yields in general, while not messing with the balancing of ores against each other.")
    @Config.RangeDouble(min = 0, max = 10000)
    public static float veinYieldConstScale = 2f;

    @Config.LangKey(Constants.CONFIG_VEIN_DISTANCE_SCALE_START)
    @Config.Comment("The distance from spawn at which to start scaling up vein size and yield.")
    @Config.RangeDouble(min = 0)
    public static float veinDistanceScaleStart = 300;

    @Config.LangKey(Constants.CONFIG_VEIN_DISTANCE_SCALE_MULTIPLIER)
    @Config.Comment("A constant scaling factor applied to distance based vein scaling. The final range based " +
                    "scaling is computed as 'logn((distanceToSpawn-veinYieldDistanceScaleStart)/10)*veinYieldDistanceScaleMultiplier'.")
    @Config.RangeDouble(min = 0)
    public static float veinDistanceScaleMultiplier = 2f;

    @Config.LangKey(Constants.CONFIG_UI_ONLY_WHEN_SNEAKING)
    @Config.Comment("Whether to only show the floating UI indicating ore yield when sneaking.")
    public static boolean uiOnlyWhenSneaking = false;

    @Config.LangKey(Constants.CONFIG_RETROGEN_SPEED)
    @Config.Comment("Maximum number of chunks to perform retrogen for per tick. 0 to disable retrogen.")
    @Config.RangeInt(min = 0)
    public static int retrogenSpeed = 10;

    @Config.LangKey(Constants.CONFIG_ALLOW_PLAYER_MINING)
    @Config.Comment("Whether to allow players to directly mine bedrock ores. " +
                    "Disabling this will force players to use the bedrock miner.")
    public static boolean allowPlayerMining = true;

    @Config.LangKey(Constants.CONFIG_WORLD_GENERATOR_WEIGHT)
    @Config.Comment("The 'weight' of the world generator placing bedrock ores. This is used when Forge " +
                    "sorts generators to know in what order they run. Higher weights run later.")
    public static int worldGeneratorWeight = 10;

    @Config.LangKey(Constants.CONFIG_ORE_PRIORITY)
    @Config.Comment("Controls the sort index the `groupOrder` field in a JSON config for ore gen evaluates " +
                    "to; specifically, the sort index will be the index in this list times five. So by default " +
                    "the sort index for ThermalFoundation ores will be 0 and the one for Immersive Engineering " +
                    "will be 10.")
    public static String[] orePriority = {
            "thermalfoundation",
            "ic2",
            "immersiveengineering",
            "techreborn",
            "forestry",
            "silentgems",
            "mekanism"
    };

    @Config.LangKey(Constants.CONFIG_ORE_MOD_BLACKLIST)
    @Config.Comment("A blacklist to easily disable ore generation for individual mods. Useful when you want " +
                    "to disable ores with default configurations without adding an `enabled: false` entry " +
                    "for each one to a JSON config. This must be the mod IDs of the mods to disable, i.e. " +
                    "the bit in the block state name before the colon, e.g. `thermalfoundation`.")
    public static String[] oreModBlacklist = {};

    @Config.LangKey(Constants.CONFIG_DEFAULT_DIMENSION_TYPES)
    @Config.Comment("The list of dimension *types* assigned to ore entries with no explicit dimension config. " +
                    "This includes the defaults for vanilla ores and non-dimension-specific mod ore defaults. " +
                    "Use this to make those ores also spawn in dimension types other than the overworld.")
    @Config.RequiresMcRestart
    public static String[] defaultDimensionTypes = {
            DimensionType.OVERWORLD.getName().toLowerCase(Locale.US),
            "miningworld" // Aroma1997
    };

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
