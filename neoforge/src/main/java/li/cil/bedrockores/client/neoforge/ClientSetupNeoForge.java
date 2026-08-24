/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.client.neoforge;

import li.cil.bedrockores.client.render.neoforge.BedrockOreUnbakedModel;
import li.cil.bedrockores.common.config.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientSetupNeoForge {
    @SubscribeEvent
    public static void handleRegisterBlockStateModels(final RegisterBlockStateModels event) {
        event.registerModel(BedrockOreUnbakedModel.ID, BedrockOreUnbakedModel.CODEC);
    }

    private ClientSetupNeoForge() {
    }
}
