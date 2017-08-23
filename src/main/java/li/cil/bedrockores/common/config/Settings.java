package li.cil.bedrockores.common.config;

import net.minecraftforge.common.config.Config;

@Config(modid = Constants.MOD_ID)
public final class Settings {
    @Config.Comment("The time in ticks between extraction operations performed by the miner.")
    public static int minerExtractionCooldown = 100;

    @Config.Comment("The fuel efficiency of the miner. Actual burn time is original burn time times this.\n" +
                    "Applies to both internal power generation as well as power supplied externally.")
    public static float minerEfficiency = 0.5f;

    @Config.Comment("The y level at which to start looking for bedrock to generate veins in, searching down.")
    @Config.RangeInt(min = 0, max = 255)
    public static int veinBaseY = 5;

    @Config.Comment("The probability that a bedrock vein spawns in a chunk. In other words, the relative\n" +
                    "amount of chunks a vein spawns in.")
    @Config.RangeDouble(min = 0, max = 1)
    public static float veinChance = 0.25f;

    @Config.Comment("A constant scaling factor applied to all yields. Intended to allow easily changing\n" +
                    "yields in general, while not messing with the balancing of ores against each other.")
    @Config.RangeDouble(min = 0, max = 10000)
    public static float veinYieldBaseScale = 10f;

    @Config.Comment("The distance from spawn at which to begin starting to scale up vein size and yield.")
    @Config.RangeDouble(min = 0)
    public static float veinDistanceScaleStart = 500;

    @Config.Comment("A constant scaling factor applied to distance based vein scaling. The final range based\n" +
                    "scaling is computed as 'logn((distanceToSpawn-veinYieldDistanceScaleStart)/10)*veinYieldDistanceScaleMultiplier'.")
    @Config.RangeDouble(min = 0)
    public static float veinDistanceScaleMultiplier = 1f;

    @Config.Comment("Whether to only show the floating UI indicating ore yield when sneaking.")
    public static boolean uiOnlyWhenSneaking = false;

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
