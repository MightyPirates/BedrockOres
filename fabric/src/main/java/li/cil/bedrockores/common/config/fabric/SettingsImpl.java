package li.cil.bedrockores.common.config.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import li.cil.bedrockores.common.config.Constants;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SettingsImpl {
    public static void registerCommonConfig(final ModConfigSpec spec) {
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.COMMON, spec);
    }

    private SettingsImpl() {
    }
}
