/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Settings {
    public static ModConfigSpec.IntValue minerExtractionCooldown;
    public static ModConfigSpec.DoubleValue minerEfficiency;
    public static ModConfigSpec.DoubleValue minerEfficiencyInternalPower;
    public static ModConfigSpec.DoubleValue minerEfficiencyExternalPower;
    public static ModConfigSpec.IntValue minerAreaRadius;
    public static ModConfigSpec.IntValue minerAreaLayers;

    public static ModConfigSpec.BooleanValue allowPlayerMining;

    public static ModConfigSpec.BooleanValue uiOnlyWhenSneaking;

    public static void initialize() {
        var builder = new ModConfigSpec.Builder();

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
        allowPlayerMining = builder
                .comment("Whether to allow players to directly mine bedrock ores. " +
                        "Disabling this will force players to use the bedrock miner.")
                .define("player_mining", true);

        builder.pop().push("ui");
        uiOnlyWhenSneaking = builder
                .comment("Whether to only show the floating UI indicating ore yield when sneaking.")
                .define("info_only_when_sneaking", true);

        registerCommonConfig(builder.build());
    }

    // --------------------------------------------------------------------- //

    @ExpectPlatform
    public static void registerCommonConfig(final ModConfigSpec spec) {
        throw new AssertionError();
    }

    private Settings() {
    }
}
