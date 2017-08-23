package li.cil.bedrockores.common.config;

/**
 * Collection of constants used throughout the mod.
 */
public class Constants {
    // --------------------------------------------------------------------- //
    // Mod data

    public static final String MOD_ID = "bedrockores";
    public static final String MOD_NAME = "Bedrock Ores";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String PROXY_CLIENT = "li.cil.bedrockores.client.ProxyClient";
    public static final String PROXY_COMMON = "li.cil.bedrockores.common.ProxyCommon";

    // --------------------------------------------------------------------- //
    // Block, item, entity and container names

    public static final String NAME_BEDROCK_ORE = "bedrock_ore";
    public static final String NAME_BEDROCK_MINER = "bedrock_miner";

    // --------------------------------------------------------------------- //
    // GUI

    public static final String GUI_EXPECTED_YIELD = "gui.bedrockores.expected_yield";
    public static final String GUI_EXHAUSTED = "gui.bedrockores.exhausted";

    // --------------------------------------------------------------------- //
    // Tooltips

    public static final String TOOLTIP_BEDROCK_MINER = "tooltip.bedrockores.bedrock_miner";

    // --------------------------------------------------------------------- //
    // Config files

    public static final String BEDROCK_VEINS_FILENAME = "bedrock_veins.json";

    // --------------------------------------------------------------------- //
    // Commands

    public static final String COMMAND_USAGE = "commands.bedrockores.usage";
    public static final String COMMAND_SUB_USAGE = "commands.bedrockores.%s.usage";
    public static final String COMMAND_LIST = "commands.bedrockores.list";
    public static final String COMMAND_LIST_ITEM = "commands.bedrockores.list_item";
}
