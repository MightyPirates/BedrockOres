/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import li.cil.bedrockores.client.ClientSetup;
import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.neoforge.SettingsImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class BedrockOresNeoForge {
    public BedrockOresNeoForge(final IEventBus modEventBus, final ModContainer modContainer) {
        SettingsImpl.setModContainer(modContainer);

        BedrockOres.initialize();

        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientSetup.initialize();
        }

        modEventBus.addListener(ModCapabilities::registerCapabilities);
    }
}
