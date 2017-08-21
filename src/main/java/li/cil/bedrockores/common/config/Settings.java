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
    public static float veinYieldScale = 2f;

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
