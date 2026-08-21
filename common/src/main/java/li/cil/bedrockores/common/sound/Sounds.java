/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.sound;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class Sounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Constants.MOD_ID, Registries.SOUND_EVENT);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<SoundEvent> MINER = SOUND_EVENTS.register("bedrock_miner", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bedrock_miner")));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        SOUND_EVENTS.register();
    }
}
