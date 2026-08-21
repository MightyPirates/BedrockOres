/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.world;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class BedrockOreFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Constants.MOD_ID, Registries.FEATURE);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<BedrockOreFeature> BEDROCK_ORE = FEATURES.register("bedrock_ore", () -> new BedrockOreFeature(BedrockOreConfiguration.CODEC));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        FEATURES.register();
    }
}
