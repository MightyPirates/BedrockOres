package li.cil.bedrockores.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class Settings {
    public static ForgeConfigSpec.IntValue minerExtractionCooldown;
    public static ForgeConfigSpec.DoubleValue minerEfficiency;
    public static ForgeConfigSpec.DoubleValue minerEfficiencyInternalPower;
    public static ForgeConfigSpec.DoubleValue minerEfficiencyExternalPower;
    public static ForgeConfigSpec.IntValue minerAreaRadius;
    public static ForgeConfigSpec.IntValue minerAreaLayers;

    public static ForgeConfigSpec.IntValue veinsPerChunk;
    public static ForgeConfigSpec.BooleanValue allowPlayerMining;

    public static ForgeConfigSpec.BooleanValue uiOnlyWhenSneaking;

    public static void initialize() {
        var builder = new ForgeConfigSpec.Builder();

        builder.push("miner");
        minerExtractionCooldown = builder
                .comment("The time in ticks between extraction operations performed by the miner.")
                .defineInRange("cooldown", 100, 0, Integer.MAX_VALUE);
        minerEfficiency = builder
                .comment("The power efficiency of the miner. Actual mining time per power unit is original value times this. " +
                        "Applies to both internal power generation as well as power supplied externally. Set to 0 to disable power requirement.")
                .defineInRange("efficiency", 1.0, 0, 100);
        minerEfficiencyInternalPower = builder
                .comment("The fuel efficiency of the miner. Total mining time is original burn time times this times `minerEfficiency`. " +
                        "Applies to internal power generation. Set to 0 to disable internal powering.")
                .defineInRange("fuel_efficiency", 1.0, 0, 100);
        minerEfficiencyExternalPower = builder
                .comment("The power efficiency of the miner. Total mining time is original powered time times this times `minerEfficiency`. " +
                        "Applies to power supplied externally. Set to 0 to disable external powering.")
                .defineInRange("energy_efficiency", 1.0, 0, 100);
        minerAreaRadius = builder
                .comment("The radius of the incircle of the square area around the miner in which it operates, in blocks. 1 is straight down.")
                .defineInRange("areaRadius", 3, 1, 16);
        minerAreaLayers = builder
                .comment("The number of layers below the miner in which it operates, in blocks. 1 is just the layer below the miner.")
                .defineInRange("areaLayers", 3, 1, 32);

        builder.pop().push("world");
        veinsPerChunk = builder
                .comment("The number of bedrock ore veins to attempt to generate per chunk.")
                .defineInRange("veins_per_chunk", 5, 0, 50);
        allowPlayerMining = builder
                .comment("Whether to allow players to directly mine bedrock ores. " +
                        "Disabling this will force players to use the bedrock miner.")
                .define("player_mining", true);

        builder.pop().push("ui");
        uiOnlyWhenSneaking = builder
                .comment("Whether to only show the floating UI indicating ore yield when sneaking.")
                .define("info_only_when_sneaking", true);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.build());
    }
}
