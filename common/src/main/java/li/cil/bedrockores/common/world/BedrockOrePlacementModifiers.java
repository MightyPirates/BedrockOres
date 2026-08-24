/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.world;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class BedrockOrePlacementModifiers {
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES =
        DeferredRegister.create(Constants.MOD_ID, Registries.PLACEMENT_MODIFIER_TYPE);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<PlacementModifierType<AboveWorldBottomPlacement>> ABOVE_WORLD_BOTTOM =
        PLACEMENT_MODIFIER_TYPES.register("above_world_bottom", () -> () -> AboveWorldBottomPlacement.CODEC);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        PLACEMENT_MODIFIER_TYPES.register();
    }
}
