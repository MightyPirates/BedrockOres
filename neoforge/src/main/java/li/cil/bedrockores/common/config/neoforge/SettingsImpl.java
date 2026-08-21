/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.config.neoforge;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import static java.util.Objects.requireNonNull;

public final class SettingsImpl {
    private static ModContainer modContainer;

    public static void setModContainer(final ModContainer value) {
        modContainer = value;
    }

    public static void registerCommonConfig(final ModConfigSpec spec) {
        requireNonNull(modContainer, "Mod container not set yet.")
                .registerConfig(ModConfig.Type.COMMON, spec);
    }

    private SettingsImpl() {
    }
}
